<template>
  <div class="job-match-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>AI岗位匹配分析</h2>
      <p class="subtitle">输入目标岗位JD，AI分析你的简历与岗位的匹配程度</p>
    </div>

    <!-- 输入区域 -->
    <el-card class="input-card" shadow="hover">
      <el-form :model="form" label-width="90px" label-position="top">
        <!-- 第一行：简历选择 + 岗位名称 + 公司名称 -->
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="选择简历">
              <el-select v-model="form.resumeId" placeholder="请选择简历" style="width: 100%">
                <el-option
                    v-for="item in resumeList"
                    :key="item.id"
                    :label="item.displayName || item.fileName"
                    :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="岗位名称">
              <el-input v-model="form.jobTitle" placeholder="如：Java开发工程师" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="公司名称">
              <el-input v-model="form.companyName" placeholder="选填" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- JD输入 -->
        <el-form-item label="岗位描述(JD)">
          <el-input
              v-model="form.jobDescription"
              type="textarea"
              :rows="6"
              placeholder="粘贴目标岗位的职责描述和任职要求..."
          />
        </el-form-item>

        <!-- 分析按钮 -->
        <el-form-item>
          <el-button
              type="primary"
              :loading="analyzing"
              :disabled="!canAnalyze"
              @click="handleAnalyze"
          >
            {{ analyzing ? 'AI分析中...' : '开始匹配分析' }}
          </el-button>
          <span v-if="analyzing" class="analyze-tip">
            <el-icon class="is-loading"><Loading /></el-icon>
            正在调用AI分析，请稍候...
          </span>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 分析结果区域 -->
    <el-card v-if="currentResult" class="result-card" shadow="hover">
      <template #header>
        <div class="result-header">
          <span>分析结果</span>
          <el-tag :type="scoreTagType" size="large">
            {{ currentResult.matchLevel === 'high' ? '高度匹配' : currentResult.matchLevel === 'middle' ? '中度匹配' : '低度匹配' }}
          </el-tag>
        </div>
      </template>

      <el-row :gutter="24">
        <!-- 左侧：匹配度圆环 -->
        <el-col :span="6" class="score-section">
          <div class="score-circle">
            <el-progress
                type="circle"
                :percentage="currentResult.matchScore"
                :width="150"
                :stroke-width="12"
                :color="scoreColor"
            >
              <template #default>
                <div class="score-inner">
                  <span class="score-num">{{ currentResult.matchScore }}</span>
                  <span class="score-label">匹配度</span>
                </div>
              </template>
            </el-progress>
          </div>
          <div class="score-meta">
            <p><strong>{{ currentResult.jobTitle }}</strong></p>
            <p v-if="currentResult.companyName">{{ currentResult.companyName }}</p>
          </div>
        </el-col>

        <!-- 右侧：详细分析 -->
        <el-col :span="18">
          <el-tabs v-model="activeTab">
            <el-tab-pane label="核心优势" name="strengths">
              <div class="tab-content" v-html="formatContent(currentResult.strengths)"></div>
            </el-tab-pane>
            <el-tab-pane label="不足之处" name="weaknesses">
              <div class="tab-content" v-html="formatContent(currentResult.weaknesses)"></div>
            </el-tab-pane>
            <el-tab-pane label="缺失技能" name="missingSkills">
              <div class="tab-content" v-html="formatContent(currentResult.missingSkills)"></div>
            </el-tab-pane>
            <el-tab-pane label="改进建议" name="suggestions">
              <div class="tab-content" v-html="formatContent(currentResult.suggestions)"></div>
            </el-tab-pane>
          </el-tabs>
        </el-col>
      </el-row>
    </el-card>

    <!-- 历史记录 -->
    <el-card class="history-card" shadow="hover">
      <template #header>
        <div class="history-header">
          <span>历史分析记录</span>
          <el-button text type="primary" @click="loadHistory">
            <el-icon><Refresh /></el-icon> 刷新
          </el-button>
        </div>
      </template>

      <el-table :data="historyList" stripe empty-text="暂无分析记录">
        <el-table-column prop="jobTitle" label="岗位" min-width="100" />
        <el-table-column prop="companyName" label="公司" min-width="100">
          <template #default="{ row }">{{ row.companyName || '-' }}</template>
        </el-table-column>
        <el-table-column prop="matchScore" label="匹配度" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getScoreType(row.matchScore)" size="small">{{ row.matchScore }}分</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="matchLevel" label="匹配等级" width="120" align="center">
          <template #default="{ row }">
            {{ row.matchLevel === 'high' ? '高' : row.matchLevel === 'middle' ? '中' : '低' }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="分析时间" width="170" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="viewResult(row)">查看</el-button>
            <el-button
                text :type="row.isSaved === 1 ? 'warning' : 'success'" size="small"
                @click="handleToggleSave(row)"
            >
              {{ row.isSaved === 1 ? '取消保存' : '保存' }}
            </el-button>
            <el-button text type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading, Refresh } from '@element-plus/icons-vue'
import { analyzeMatch, getResult, getHistory, toggleSave, deleteAnalysis } from '@/api/jobMatch'
import { getResumeList } from '@/api/resume'
import { useRoute } from 'vue-router'

const route = useRoute()

// ==================== 表单数据 ====================
const form = ref({
  resumeId: null,
  jobTitle: '',
  companyName: '',
  jobDescription: ''
})

// ==================== 状态 ====================
const analyzing = ref(false)
const resumeList = ref([])
const historyList = ref([])
const currentResult = ref(null)
const activeTab = ref('strengths')

// ==================== 计算属性 ====================
const canAnalyze = computed(() => {
  return form.value.resumeId && form.value.jobTitle.trim() && form.value.jobDescription.trim()
})

// 匹配度颜色
const scoreColor = computed(() => {
  const score = currentResult.value?.matchScore || 0
  if (score >= 75) return '#67C23A'
  if (score >= 50) return '#E6A23C'
  return '#F56C6C'
})

// 匹配等级标签类型
const scoreTagType = computed(() => {
  const level = currentResult.value?.matchLevel
  if (level === 'high') return 'success'
  if (level === 'middle') return 'warning'
  return 'danger'
})

// ==================== 方法 ====================

// 加载简历列表
const loadResumeList = async () => {
  try {
    const res = await getResumeList()
    if (res.code === 200) {
      resumeList.value = res.data || []
      // 如果只有一份简历，自动选中
      if (resumeList.value.length === 1) {
        form.value.resumeId = resumeList.value[0].id
      }
    }
  } catch (e) {
    console.error('加载简历列表失败', e)
  }
}

// 加载历史记录
const loadHistory = async () => {
  try {
    const res = await getHistory()
    if (res.code === 200) {
      historyList.value = res.data || []
    }
  } catch (e) {
    console.error('加载历史记录失败', e)
  }
}

// 发起分析
const handleAnalyze = async () => {
  if (!canAnalyze.value) return
  analyzing.value = true
  currentResult.value = null

  try {
    const res = await analyzeMatch({
      resumeId: form.value.resumeId,
      jobTitle: form.value.jobTitle,
      companyName: form.value.companyName,
      jobDescription: form.value.jobDescription
    })
    if (res.code === 200) {
      currentResult.value = res.data
      activeTab.value = 'strengths'
      ElMessage.success('分析完成')
      loadHistory()
    } else {
      ElMessage.error(res.message || '分析失败')
    }
  } catch (e) {
    console.error('分析失败', e)
    ElMessage.error('分析请求失败，请稍后重试')
  } finally {
    analyzing.value = false
  }
}

// 查看历史结果
const viewResult = async (row) => {
  try {
    const res = await getResult(row.id)
    if (res.code === 200) {
      currentResult.value = res.data
      activeTab.value = 'strengths'
      // 滚动到结果区域
      document.querySelector('.result-card')?.scrollIntoView({ behavior: 'smooth' })
    }
  } catch (e) {
    ElMessage.error('获取分析结果失败')
  }
}

// 保存/取消保存
const handleToggleSave = async (row) => {
  try {
    const newSaved = row.isSaved === 1 ? 0 : 1
    const res = await toggleSave(row.id, newSaved)
    if (res.code === 200) {
      row.isSaved = newSaved
      ElMessage.success(newSaved === 1 ? '已保存' : '已取消保存')
    }
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

// 删除记录
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该分析记录？', '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    const res = await deleteAnalysis(row.id)
    if (res.code === 200) {
      ElMessage.success('已删除')
      loadHistory()
      // 如果删除的是当前展示的结果，清空
      if (currentResult.value?.id === row.id) {
        currentResult.value = null
      }
    }
  } catch (e) {
    // 用户取消确认，不做处理
  }
}

// 格式化内容：将换行转为<br>，将列表项加上样式
// 格式化内容：支持数组和字符串
const formatContent = (data) => {
  if (!data) return '<p style="color:#999">暂无内容</p>'
  // 如果是数组
  if (Array.isArray(data)) {
    if (data.length === 0) return '<p style="color:#999">暂无内容</p>'
    return data.map(item => `<div style="padding-left:16px;margin:4px 0">• ${item}</div>`).join('')
  }
  // 如果是字符串
  let html = data.replace(/\n/g, '<br>')
  html = html.replace(/^[-*]\s+(.+)$/gm, '<div style="padding-left:16px;margin:4px 0">• $1</div>')
  return html
}


// 分数对应的标签类型
const getScoreType = (score) => {
  if (score >= 75) return 'success'
  if (score >= 50) return 'warning'
  return 'danger'
}

// ==================== 生命周期 ====================
onMounted(() => {
  loadResumeList()
  loadHistory()

  // 接收投递面板传来的参数，自动回填
  const { jobTitle, companyName, jobDescription, resumeId } = route.query
  if (jobTitle) form.value.jobTitle = jobTitle
  if (companyName) form.value.companyName = companyName
  if (jobDescription) form.value.jobDescription = jobDescription
  if (resumeId) form.value.resumeId = Number(resumeId)
})

</script>

<style scoped>
.job-match-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 6px 0;
  font-size: 22px;
}

.page-header .subtitle {
  margin: 0;
  font-size: 14px;
  color: #909399;
}

.input-card,
.result-card,
.history-card {
  margin-bottom: 20px;
}

.analyze-tip {
  margin-left: 12px;
  font-size: 13px;
  color: #909399;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

/* 结果区域 */
.result-header {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 16px;
  font-weight: 600;
}

.score-section {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.score-circle {
  margin-bottom: 12px;
}

.score-inner {
  text-align: center;
}

.score-num {
  display: block;
  font-size: 36px;
  font-weight: 700;
  line-height: 1.2;
}

.score-label {
  display: block;
  font-size: 13px;
  color: #909399;
}

.score-meta {
  text-align: center;
  font-size: 14px;
  color: #606266;
}

.score-meta p {
  margin: 2px 0;
}

.tab-content {
  padding: 8px 0;
  line-height: 1.8;
  font-size: 14px;
  color: #303133;
}

/* 历史区域 */
.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
}
</style>
