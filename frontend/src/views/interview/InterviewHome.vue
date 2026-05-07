<template>
  <div class="interview-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>AI面试助手</h2>
      <p class="subtitle">根据简历和目标岗位，AI生成面试题，追踪你的准备进度</p>
    </div>

    <!-- 准备进度统计 -->
    <el-card v-if="stats.total > 0" class="stats-card" shadow="hover">
      <div class="stats-row">
        <div class="stat-item">
          <span class="stat-num">{{ stats.total }}</span>
          <span class="stat-label">总题数</span>
        </div>
        <div class="stat-item prepared">
          <span class="stat-num">{{ stats.prepared }}</span>
          <span class="stat-label">已准备</span>
        </div>
        <div class="stat-item preparing">
          <span class="stat-num">{{ stats.preparing }}</span>
          <span class="stat-label">准备中</span>
        </div>
        <div class="stat-item unprepared">
          <span class="stat-num">{{ stats.unprepared }}</span>
          <span class="stat-label">未准备</span>
        </div>
        <div class="stat-progress">
          <el-progress
              :percentage="stats.completionRate"
              :stroke-width="12"
              :color="progressColor"
          >
            <span class="progress-text">{{ stats.completionRate }}%</span>
          </el-progress>
        </div>
      </div>
    </el-card>

    <!-- 输入区域 -->
    <el-card class="input-card" shadow="hover">
      <el-form :model="form" label-position="top">
        <el-row :gutter="20">
          <el-col :span="6">
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
          <el-col :span="6">
            <el-form-item label="目标岗位">
              <el-input v-model="form.jobTitle" placeholder="如：Java开发工程师" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="公司名称">
              <el-input v-model="form.companyName" placeholder="选填" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="题目类型">
              <el-select v-model="form.questionType" placeholder="全部类型" clearable style="width: 100%">
                <el-option label="技术面" value="technical" />
                <el-option label="行为面" value="behavioral" />
                <el-option label="情景面" value="situational" />
                <el-option label="HR面" value="hr" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- JD输入 -->
        <el-form-item label="岗位描述(JD)">
          <el-input
              v-model="form.jobDescription"
              type="textarea"
              :rows="3"
              placeholder="粘贴岗位描述，有JD生成质量更高..."
          />
        </el-form-item>

        <!-- 生成按钮 -->
        <el-form-item>
          <el-button
              type="primary"
              :loading="generating"
              :disabled="!canGenerate"
              @click="handleGenerate"
          >
            {{ generating ? 'AI生成中...' : '生成面试题' }}
          </el-button>
          <span v-if="generating" class="generate-tip">
            <el-icon class="is-loading"><Loading /></el-icon>
            正在生成面试题，可能需要1-2分钟...
          </span>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 筛选栏 -->
    <!-- 改成：只要有题目数据就显示筛选栏 -->
    <div v-if="hasAnyQuestions" class="filter-bar">
      <el-radio-group v-model="filterType" @change="loadQuestions">
        <el-radio-button value="">全部</el-radio-button>
        <el-radio-button value="technical">技术面</el-radio-button>
        <el-radio-button value="behavioral">行为面</el-radio-button>
        <el-radio-button value="situational">情景面</el-radio-button>
        <el-radio-button value="hr">HR面</el-radio-button>
      </el-radio-group>
      <el-radio-group v-model="filterStatus" @change="loadQuestions" style="margin-left: 16px">
        <el-radio-button value="">全部状态</el-radio-button>
        <el-radio-button value="unprepared">未准备</el-radio-button>
        <el-radio-button value="preparing">准备中</el-radio-button>
        <el-radio-button value="prepared">已准备</el-radio-button>
      </el-radio-group>
    </div>
    <!-- 面试题列表区域（固定容器，防止抖动） -->
    <div class="question-list-wrapper">
      <!-- loading遮罩，叠在旧数据上面 -->
      <div v-if="listLoading" class="list-loading">
        <el-icon class="is-loading" :size="24"><Loading /></el-icon>
        <span>加载中...</span>
      </div>
      <div class="question-list" :class="{ 'is-loading': listLoading }">
        <el-card
            v-for="item in questionList"
            :key="item.id"
            class="question-card"
            shadow="hover"
        >
        <!-- 题目头部 -->
        <div class="question-header">
          <div class="question-meta">
            <el-tag :type="typeTagColor(item.questionType)" size="small">
              {{ typeLabel(item.questionType) }}
            </el-tag>
            <el-tag :type="diffTagColor(item.difficulty)" size="small" effect="plain">
              {{ diffLabel(item.difficulty) }}
            </el-tag>
            <span
                :class="['status-dot', item.prepStatus]"
                @click="cycleStatus(item)"
                title="点击切换状态"
            ></span>
            <span class="status-text" @click="cycleStatus(item)">
              {{ statusLabel(item.prepStatus) }}
            </span>
          </div>
          <div class="question-actions">
            <el-button text :type="item.isSaved === 1 ? 'warning' : 'success'" size="small" @click="handleToggleSave(item)">
              {{ item.isSaved === 1 ? '取消收藏' : '收藏' }}
            </el-button>
            <el-button text type="danger" size="small" @click="handleDelete(item)">删除</el-button>
          </div>
        </div>

        <!-- 题目内容 -->
        <div class="question-text">{{ item.questionText }}</div>

        <!-- 提示和得分点（可展开） -->
        <el-collapse class="question-detail">
          <el-collapse-item>
            <template #title>
              <span class="collapse-title">查看提示和得分点</span>
            </template>
            <div v-if="item.hint" class="hint-section">
              <strong>💡 答题提示：</strong>
              <p>{{ item.hint }}</p>
            </div>
            <div v-if="item.keyPoints && item.keyPoints.length > 0" class="keypoints-section">
              <strong>🎯 关键得分点：</strong>
              <ul>
                <li v-for="(point, idx) in item.keyPoints" :key="idx">{{ point }}</li>
              </ul>
            </div>
          </el-collapse-item>
        </el-collapse>

        <!-- 答题草稿 -->
        <div class="draft-section">
          <div class="draft-header">
            <span>📝 我的答案</span>
            <!-- 改成 -->
            <el-button v-if="item.answerDraft" text type="primary" size="small" @click="saveDraft(item)">
              保存草稿
            </el-button>
          </div>
          <el-input
              v-model="item.answerDraft"
              type="textarea"
              :rows="3"
              placeholder="写下你的答题思路..."
              @blur="saveDraft(item)"
          />
        </div>
      </el-card>
    </div>

    <!-- 空状态 -->
    <el-card v-if="questionList.length === 0 && !generating" class="empty-card" shadow="hover">
      <div class="empty-state">
        <p>还没有面试题，选择简历和岗位后点击"生成面试题"</p>
      </div>
    </el-card>
  </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import {
  generateQuestions,
  getQuestionList,
  updateDraft,
  updatePrepStatus,
  toggleSave,
  deleteQuestion,
  getInterviewStats
} from '@/api/interview'
import { getResumeList } from '@/api/resume'

const route = useRoute()

// ==================== 表单数据 ====================
const form = ref({
  resumeId: null,
  jobTitle: '',
  companyName: '',
  jobDescription: '',
  questionType: ''  // 空表示生成全部类型
})

// ==================== 状态 ====================
const generating = ref(false)
const resumeList = ref([])
const questionList = ref([])
const stats = ref({ total: 0, prepared: 0, preparing: 0, unprepared: 0, completionRate: 0 })

// 筛选条件
const filterType = ref('')
const filterStatus = ref('')

// ==================== 计算属性 ====================
/** 是否可以生成 */
const canGenerate = computed(() => {
  return form.value.resumeId && form.value.jobTitle.trim()
})

/** 进度条颜色 */
const progressColor = computed(() => {
  const rate = stats.value.completionRate
  if (rate >= 80) return '#67C23A'
  if (rate >= 50) return '#E6A23C'
  return '#409EFF'
})
// 是否有任何面试题（不受筛选影响）
const hasAnyQuestions = ref(false)

const listLoading = ref(false)

// ==================== 标签映射 ====================

function typeLabel(type) {
  const map = { technical: '技术面', behavioral: '行为面', situational: '情景面', hr: 'HR面' }
  return map[type] || type
}

function typeTagColor(type) {
  const map = { technical: '', behavioral: 'success', situational: 'warning', hr: 'info' }
  return map[type] || ''
}

function diffLabel(diff) {
  const map = { easy: '简单', medium: '中等', hard: '困难' }
  return map[diff] || '中等'
}

function diffTagColor(diff) {
  const map = { easy: 'success', medium: 'warning', hard: 'danger' }
  return map[diff] || 'warning'
}

function statusLabel(status) {
  const map = { unprepared: '未准备', preparing: '准备中', prepared: '已准备' }
  return map[status] || '未准备'
}

// ==================== 方法 ====================

/** 加载简历列表 */
async function loadResumeList() {
  try {
    const res = await getResumeList()
    if (res.code === 200) {
      resumeList.value = res.data || []
      if (resumeList.value.length === 1) {
        form.value.resumeId = resumeList.value[0].id
      }
    }
  } catch (e) {
    console.error('加载简历列表失败', e)
  }
}

/** 加载面试题列表 */
async function loadQuestions() {
  listLoading.value = true
  try {
    const res = await getQuestionList(filterType.value || undefined, filterStatus.value || undefined)
    if (res.code === 200) {
      questionList.value = res.data || []
    }
  } catch (e) {
    console.error('加载面试题列表失败', e)
  } finally {
    listLoading.value = false
  }
  // 判断是否有题目
  if (!hasAnyQuestions.value) {
    try {
      const allRes = await getQuestionList()
      if (allRes.code === 200) {
        hasAnyQuestions.value = (allRes.data || []).length > 0
      }
    } catch (e) {}
  }
}


/** 加载统计数据 */
async function loadStats() {
  try {
    const res = await getInterviewStats()
    if (res.code === 200) {
      stats.value = res.data || { total: 0, prepared: 0, preparing: 0, unprepared: 0, completionRate: 0 }
    }
  } catch (e) {
    console.error('加载统计失败', e)
  }
}

/** 生成面试题 */
async function handleGenerate() {
  if (!canGenerate.value) return
  generating.value = true

  try {
    const res = await generateQuestions({
      resumeId: form.value.resumeId,
      jobTitle: form.value.jobTitle,
      companyName: form.value.companyName,
      jobDescription: form.value.jobDescription,
      questionType: form.value.questionType || undefined,
      countPerType: 3
    })
    if (res.code === 200) {
      ElMessage.success(`生成完成，共${res.data.length}道面试题`)
      loadQuestions()
      loadStats()
    } else {
      ElMessage.error(res.message || '生成失败')
    }
  } catch (e) {
    console.error('生成面试题失败', e)
    ElMessage.error('生成请求失败，请稍后重试')
  } finally {
    generating.value = false
  }
}

/** 切换准备状态（循环：未准备→准备中→已准备→未准备） */
async function cycleStatus(item) {
  const cycle = { unprepared: 'preparing', preparing: 'prepared', prepared: 'unprepared' }
  const newStatus = cycle[item.prepStatus] || 'preparing'
  try {
    const res = await updatePrepStatus(item.id, newStatus)
    if (res.code === 200) {
      item.prepStatus = newStatus
      loadStats()
    }
  } catch (e) {
    ElMessage.error('状态更新失败')
  }
}

/** 保存答题草稿 */
async function saveDraft(item) {
  if (!item.answerDraft && item.answerDraft !== '') return
  try {
    await updateDraft(item.id, item.answerDraft)
  } catch (e) {
    // 草稿保存静默失败，不打扰用户
    console.error('草稿保存失败', e)
  }
}

/** 保存/取消保存 */
async function handleToggleSave(item) {
  try {
    const newSaved = item.isSaved === 1 ? 0 : 1
    const res = await toggleSave(item.id, newSaved)
    if (res.code === 200) {
      item.isSaved = newSaved
      ElMessage.success(newSaved === 1 ? '已保存' : '已取消保存')
    }
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

/** 删除面试题 */
async function handleDelete(item) {
  try {
    await ElMessageBox.confirm('确定删除该面试题？', '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    const res = await deleteQuestion(item.id)
    if (res.code === 200) {
      ElMessage.success('已删除')
      loadQuestions()
      loadStats()
    }
  } catch (e) {
    // 用户取消
  }
}

// ==================== 生命周期 ====================
onMounted(() => {
  loadResumeList()
  loadQuestions()
  loadStats()

  // 接收投递面板传来的参数
  const { jobTitle, companyName, jobDescription, resumeId } = route.query
  if (jobTitle) form.value.jobTitle = jobTitle
  if (companyName) form.value.companyName = companyName
  if (jobDescription) form.value.jobDescription = jobDescription
  if (resumeId) form.value.resumeId = Number(resumeId)
})
</script>

<style scoped>
.interview-container {
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

/* 统计卡片 */
.stats-card {
  margin-bottom: 20px;
}

.stats-row {
  display: flex;
  align-items: center;
  gap: 32px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-num {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 2px;
}

.stat-item.prepared .stat-num { color: #67C23A; }
.stat-item.preparing .stat-num { color: #E6A23C; }
.stat-item.unprepared .stat-num { color: #909399; }

.stat-progress {
  flex: 1;
  min-width: 200px;
}

.progress-text {
  font-size: 13px;
  font-weight: 600;
}

/* 输入卡片 */
.input-card {
  margin-bottom: 20px;
}

.generate-tip {
  margin-left: 12px;
  font-size: 13px;
  color: #909399;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

/* 筛选栏 */
.filter-bar {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 8px;
}

/* 题目列表 */
.question-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.question-card {
  margin-bottom: 0;
}

/* 题目头部 */
.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.question-meta {
  display: flex;
  align-items: center;
  gap: 8px;
}

.question-actions {
  display: flex;
  gap: 4px;
}

/* 状态圆点 */
.status-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  display: inline-block;
  cursor: pointer;
  transition: all 0.2s;
}

.status-dot.unprepared { background: #dcdfe6; }
.status-dot.preparing { background: #e6a23c; }
.status-dot.prepared { background: #67c23a; }

.status-dot:hover {
  transform: scale(1.3);
}

.status-text {
  font-size: 12px;
  color: #909399;
  cursor: pointer;
}

/* 题目内容 */
.question-text {
  font-size: 15px;
  color: #303133;
  line-height: 1.6;
  margin-bottom: 12px;
  font-weight: 500;
}

/* 展开详情 */
.question-detail {
  margin-bottom: 12px;
  border: none;
}

.question-detail :deep(.el-collapse-item__header) {
  border: none;
  background: transparent;
  height: 32px;
  line-height: 32px;
}

.question-detail :deep(.el-collapse-item__wrap) {
  border: none;
}

.collapse-title {
  font-size: 13px;
  color: #409eff;
  cursor: pointer;
}

.hint-section {
  margin-bottom: 12px;
  padding: 10px 12px;
  background: #f0f9eb;
  border-radius: 6px;
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
}

.hint-section p {
  margin: 4px 0 0 0;
}

.keypoints-section {
  padding: 10px 12px;
  background: #ecf5ff;
  border-radius: 6px;
  font-size: 13px;
  color: #606266;
}

.keypoints-section ul {
  margin: 4px 0 0 0;
  padding-left: 16px;
}

.keypoints-section li {
  margin: 2px 0;
  line-height: 1.6;
}

/* 答题草稿 */
.draft-section {
  border-top: 1px solid #ebeef5;
  padding-top: 12px;
}

.draft-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 13px;
  color: #606266;
  font-weight: 500;
}

/* 空状态 */
.empty-card {
  text-align: center;
  padding: 40px 0;
}

.empty-state {
  color: #909399;
  font-size: 14px;
}
.list-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 40px 0;
  color: #909399;
  font-size: 14px;
}
/* 列表容器 */
.question-list-wrapper {
  position: relative;
  min-height: 100px;
}

/* loading遮罩 */
.list-loading {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  background: rgba(255, 255, 255, 0.7);
  z-index: 1;
  color: #909399;
  font-size: 14px;
}

/* loading时列表半透明 */
.question-list.is-loading {
  opacity: 0.5;
  pointer-events: none;
}

</style>
