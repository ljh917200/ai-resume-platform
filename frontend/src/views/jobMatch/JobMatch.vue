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

// 匹配度颜色（水墨色系）
const scoreColor = computed(() => {
  const score = currentResult.value?.matchScore || 0
  if (score >= 70) return 'var(--ink-primary)'
  if (score >= 40) return 'var(--ink-secondary)'
  return '#999'
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


// 分数对应的标签类型（水墨色系）
const getScoreType = (score) => {
  if (score >= 70) return 'success'
  if (score >= 40) return 'warning'
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
  min-height: 100vh;
  background: var(--ink-bg);
  padding: 32px;
}

.job-match-container > * {
  max-width: 1200px;
  margin-left: auto;
  margin-right: auto;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  font-size: 22px;
  font-family: var(--ink-font-serif);
  color: var(--ink-primary);
}

.page-header .subtitle {
  margin: 0;
  font-size: 14px;
  color: #999;
}

.input-card,
.result-card,
.history-card {
  margin-bottom: 24px;
  background: #fff;
  border-radius: var(--ink-radius-card);
  box-shadow: var(--ink-shadow-card);
  border: none;
}

.input-card {
  padding: 24px;
}

.input-card :deep(.el-form-item__label) {
  color: var(--ink-text-secondary);
  font-size: 13px;
}

.input-card :deep(.el-input__wrapper),
.input-card :deep(.el-select__wrapper) {
  border-radius: var(--ink-radius-btn);
  border: 1px solid var(--ink-border);
  background: #f7f8fa;
}

.input-card :deep(.el-textarea__inner) {
  border-radius: var(--ink-radius-btn);
  border: 1px solid var(--ink-border);
  background: #f7f8fa;
}

.input-card .el-button--primary {
  background: var(--ink-primary) !important;
  border-color: var(--ink-primary) !important;
  color: #fff !important;
  border-radius: var(--ink-radius-btn);
  padding: 10px 24px;
  transition: all 0.2s ease;
}

.input-card .el-button--primary:hover:not(:disabled) {
  transform: scale(1.02);
}



.analyze-tip {
  margin-left: 16px;
  font-size: 13px;
  color: #999;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

/* 结果区域 */
.result-card {
  padding: 24px;
  animation: resultEnter 300ms var(--ink-ease);
}

@keyframes resultEnter {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}

.result-header {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 24px;
}

.result-header .el-tag {
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 12px;
  border: none;
}

.result-header .el-tag--success {
  background: var(--ink-primary);
  color: #fff;
}

.result-header .el-tag--warning {
  background: #f0f0f0;
  color: #666;
}

.result-header .el-tag--danger {
  background: #f7f7f7;
  color: #999;
}

.score-section {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.score-circle {
  margin-bottom: 16px;
}

.score-circle :deep(.el-progress-circle__track) {
  stroke: #f0f0f0;
}

.score-inner {
  text-align: center;
}

.score-num {
  display: block;
  font-size: 48px;
  font-family: var(--ink-font-serif);
  font-weight: 700;
  line-height: 1;
  color: var(--ink-primary);
}

.score-label {
  display: block;
  font-size: 14px;
  color: #999;
}

.score-meta {
  text-align: center;
  font-size: 13px;
  color: #999;
}

.score-meta p {
  margin: 4px 0;
}

.score-meta strong {
  color: #666;
}

/* Tab区域 */
:deep(.el-tabs__header) {
  margin-bottom: 0;
}

:deep(.el-tabs__nav-wrap) {
  border-bottom: 1px solid #e8e8e8;
}

:deep(.el-tabs__item) {
  font-size: 14px;
  color: #999;
  margin-right: 24px;
  padding-bottom: 8px;
}

:deep(.el-tabs__item.is-active) {
  color: var(--ink-primary);
  font-weight: 500;
}

:deep(.el-tabs__active-bar) {
  background: var(--ink-primary);
  height: 2px;
}

.tab-content {
  padding: 16px 0;
  line-height: 1.8;
  font-size: 14px;
  color: var(--ink-text);
  min-height: 150px;
}

.tab-content > div {
  padding-left: 20px;
}

.tab-content > div > div {
  position: relative;
  padding-left: 16px;
  margin: 12px 0;
}

.tab-content > div > div::before {
  content: '●';
  position: absolute;
  left: 0;
  color: var(--ink-primary);
  font-size: 6px;
  top: 50%;
  transform: translateY(-50%);
}

/* 历史区域 */
.history-card {
  padding: 24px;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 20px;
}

.history-card :deep(.el-button--text) {
  color: var(--ink-text-secondary);
}

.history-card :deep(.el-button--text):hover {
  color: var(--ink-primary);
}

.history-card :deep(.el-table) {
  border-radius: var(--ink-radius-card);
  overflow: hidden;
}

.history-card :deep(.el-table__header-wrapper) {
  background: #fafafa;
}

.history-card :deep(.el-table th) {
  background: #fafafa;
  color: #666;
  font-size: 13px;
  font-weight: 500;
}

.history-card :deep(.el-table td) {
  border-bottom: 1px solid #f0f0f0;
  padding: 14px 16px;
}

.history-card :deep(.el-table__row) {
  position: relative;
  transition: transform 0.2s ease;
}

.history-card :deep(.el-table__row):hover {
  transform: scale(1.005);
}

.history-card :deep(.el-tag--success) {
  background: var(--ink-primary);
  color: #fff;
  border: none;
}

.history-card :deep(.el-tag--warning) {
  background: #f0f0f0;
  color: #666;
  border: none;
}

.history-card :deep(.el-tag--danger) {
  background: #f7f7f7;
  color: #999;
  border: none;
}

.history-card :deep(.el-table__empty-text) {
  font-family: var(--ink-font-serif);
  color: #999;
  font-size: 16px;
}

.history-card :deep(.el-table__empty-wrapper)::after {
  content: '选择简历和岗位后，开始你的第一次匹配分析';
  display: block;
  font-size: 13px;
  color: #bbb;
  margin-top: 8px;
}
</style>
