<template>
  <div class="cover-letter-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>AI求职信生成</h2>
      <p class="subtitle">根据简历和目标岗位，AI为你撰写专业求职信</p>
    </div>

    <!-- 输入区域 -->
    <el-card class="input-card" shadow="hover">
      <el-form :model="form" label-position="top">
        <el-row :gutter="20">
          <!-- 简历选择 -->
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
          <!-- 岗位名称 -->
          <el-col :span="8">
            <el-form-item label="目标岗位">
              <el-input v-model="form.jobTitle" placeholder="如：Java开发工程师" />
            </el-form-item>
          </el-col>
          <!-- 公司名称 -->
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
              :rows="4"
              placeholder="粘贴目标岗位的职责描述和任职要求，有JD生成质量更高..."
          />
        </el-form-item>

        <!-- 风格和语言选择 -->
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="求职信风格">
              <el-radio-group v-model="form.letterStyle">
                <el-radio-button value="formal">正式专业</el-radio-button>
                <el-radio-button value="casual">轻松自然</el-radio-button>
                <el-radio-button value="creative">创意独特</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="语言">
              <el-radio-group v-model="form.language">
                <el-radio-button value="zh">中文</el-radio-button>
                <el-radio-button value="en">英文</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 生成按钮 -->
        <el-form-item>
          <el-button
              type="primary"
              :loading="generating"
              :disabled="!canGenerate"
              @click="handleGenerate"
          >
            {{ generating ? 'AI生成中...' : '生成求职信' }}
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 生成结果区域 -->
    <el-card v-if="currentLetter" class="result-card" shadow="hover">
      <template #header>
        <div class="result-header">
          <div class="result-info">
            <span>{{ currentLetter.jobTitle }}</span>
            <span v-if="currentLetter.companyName" class="company-tag">
              @ {{ currentLetter.companyName }}
            </span>
            <el-tag size="small" type="info">
              {{ styleLabel(currentLetter.letterStyle) }} · {{ currentLetter.language === 'zh' ? '中文' : '英文' }}
            </el-tag>
          </div>
          <div class="result-actions">
            <!-- 重新生成按钮组 -->
            <el-dropdown @command="handleRegenerate" :disabled="regenerating">
              <el-button :loading="regenerating">
                {{ regenerating ? '重新生成中...' : '换风格重新生成' }}
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="formal">正式专业</el-dropdown-item>
                  <el-dropdown-item command="casual">轻松自然</el-dropdown-item>
                  <el-dropdown-item command="creative">创意独特</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <!-- 复制按钮 -->
            <el-button @click="handleCopy">
              <el-icon><DocumentCopy /></el-icon> 复制文本
            </el-button>
          </div>
        </div>
      </template>

      <!-- 求职信内容展示 -->
      <div class="letter-content" v-html="formatLetterContent(currentLetter.letterContent)"></div>
    </el-card>

    <!-- 历史记录 -->
    <el-card class="history-card" shadow="hover">
      <template #header>
        <div class="history-header">
          <span>历史生成记录</span>
          <el-button text type="primary" @click="loadHistory">
            <el-icon><Refresh /></el-icon> 刷新
          </el-button>
        </div>
      </template>

      <el-table :data="historyList" stripe empty-text="暂无生成记录">
        <el-table-column prop="jobTitle" label="岗位" min-width="120" />
        <el-table-column prop="companyName" label="公司" min-width="100">
          <template #default="{ row }">{{ row.companyName || '-' }}</template>
        </el-table-column>
        <el-table-column prop="letterStyle" label="风格" width="100" align="center">
          <template #default="{ row }">{{ styleLabel(row.letterStyle) }}</template>
        </el-table-column>
        <el-table-column prop="language" label="语言" width="100" align="center">
          <template #default="{ row }">{{ row.language === 'zh' ? '中文' : '英文' }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="生成时间" width="170" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="viewLetter(row)">查看</el-button>
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
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, DocumentCopy } from '@element-plus/icons-vue'
import {
  generateLetter,
  getLetter,
  getLetterHistory,
  regenerateLetter,
  toggleSave,
  deleteLetter
} from '@/api/coverLetter'
import { getResumeList } from '@/api/resume'

const route = useRoute()

// ==================== 表单数据 ====================
const form = ref({
  resumeId: null,
  jobTitle: '',
  companyName: '',
  jobDescription: '',
  letterStyle: 'formal',
  language: 'zh'
})

// ==================== 状态 ====================
const generating = ref(false)
const regenerating = ref(false)
const resumeList = ref([])
const historyList = ref([])
const currentLetter = ref(null)

// ==================== 计算属性 ====================
/** 是否可以生成（简历和岗位必填） */
const canGenerate = computed(() => {
  return form.value.resumeId && form.value.jobTitle.trim()
})

// ==================== 方法 ====================

/** 风格中文标签 */
function styleLabel(style) {
  const map = { formal: '正式', casual: '轻松', creative: '创意' }
  return map[style] || style
}

/** 加载简历列表 */
async function loadResumeList() {
  try {
    const res = await getResumeList()
    if (res.code === 200) {
      resumeList.value = res.data || []
      // 只有一份简历时自动选中
      if (resumeList.value.length === 1) {
        form.value.resumeId = resumeList.value[0].id
      }
    }
  } catch (e) {
    console.error('加载简历列表失败', e)
  }
}

/** 加载历史记录 */
async function loadHistory() {
  try {
    const res = await getLetterHistory()
    if (res.code === 200) {
      historyList.value = res.data || []
    }
  } catch (e) {
    console.error('加载历史记录失败', e)
  }
}

/** 生成求职信 */
async function handleGenerate() {
  if (!canGenerate.value) return
  generating.value = true
  currentLetter.value = null

  try {
    const res = await generateLetter({
      resumeId: form.value.resumeId,
      jobTitle: form.value.jobTitle,
      companyName: form.value.companyName,
      jobDescription: form.value.jobDescription,
      letterStyle: form.value.letterStyle,
      language: form.value.language
    })
    if (res.code === 200) {
      currentLetter.value = res.data
      ElMessage.success('生成完成')
      loadHistory()
    } else {
      ElMessage.error(res.message || '生成失败')
    }
  } catch (e) {
    console.error('生成失败', e)
    ElMessage.error('生成请求失败，请稍后重试')
  } finally {
    generating.value = false
  }
}

/** 查看历史记录详情 */
async function viewLetter(row) {
  try {
    const res = await getLetter(row.id)
    if (res.code === 200) {
      currentLetter.value = res.data
      document.querySelector('.result-card')?.scrollIntoView({ behavior: 'smooth' })
    }
  } catch (e) {
    ElMessage.error('获取求职信失败')
  }
}

/** 换风格重新生成 */
async function handleRegenerate(style) {
  if (!currentLetter.value) return
  regenerating.value = true

  try {
    const res = await regenerateLetter(currentLetter.value.id, style, null)
    if (res.code === 200) {
      currentLetter.value = res.data
      ElMessage.success('重新生成完成')
    } else {
      ElMessage.error(res.message || '重新生成失败')
    }
  } catch (e) {
    ElMessage.error('重新生成失败')
  } finally {
    regenerating.value = false
  }
}

/** 复制求职信文本到剪贴板 */
async function handleCopy() {
  if (!currentLetter.value?.letterContent) return
  try {
    await navigator.clipboard.writeText(currentLetter.value.letterContent)
    ElMessage.success('已复制到剪贴板')
  } catch (e) {
    // 剪贴板API不可用时，用降级方案
    const textarea = document.createElement('textarea')
    textarea.value = currentLetter.value.letterContent
    document.body.appendChild(textarea)
    textarea.select()
    document.execCommand('copy')
    document.body.removeChild(textarea)
    ElMessage.success('已复制到剪贴板')
  }
}

/** 保存/取消保存 */
async function handleToggleSave(row) {
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

/** 删除记录 */
async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定删除该求职信？', '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    const res = await deleteLetter(row.id)
    if (res.code === 200) {
      ElMessage.success('已删除')
      loadHistory()
      // 删除的是当前展示的，清空结果区
      if (currentLetter.value?.id === row.id) {
        currentLetter.value = null
      }
    }
  } catch (e) {
    // 用户取消确认
  }
}

/** 格式化求职信内容（换行转HTML） */
function formatLetterContent(content) {
  if (!content) return '<p style="color:#999">暂无内容</p>'
  return content.replace(/\n/g, '<br>')
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
.cover-letter-container {
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

/* 结果区域 */
.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.result-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
}

.company-tag {
  color: #909399;
  font-weight: 400;
}

.result-actions {
  display: flex;
  gap: 8px;
}

/* 求职信内容 */
.letter-content {
  padding: 24px 32px;
  line-height: 2;
  font-size: 15px;
  color: #303133;
  background: #fafbfc;
  border-radius: 8px;
  border: 1px solid #ebeef5;
  white-space: pre-wrap;
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
