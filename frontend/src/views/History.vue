<!-- 完整History.vue文件 -->

<template>
  <div class="history-container">
    <!-- 顶部导航栏 -->
    <el-header class="top-nav">
      <div class="nav-left">
        <el-button @click="goBack" text>
          <el-icon><i class="el-icon-arrow-left"></i></el-icon>
        </el-button>
        <span class="page-title">优化历史</span>
      </div>
      <div class="nav-right">
        <el-select v-model="currentTemplate" @change="handleTemplateChange" style="width: 120px;">
          <el-option :value="1" label="简约蓝" />
          <el-option :value="2" label="商务灰" />
          <el-option :value="3" label="创意橙" />
        </el-select>
      </div>
    </el-header>

    <el-main class="main-content">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-state">
        <el-icon class="is-loading" :size="40"><i class="el-icon-loading"></i></el-icon>
        <p>加载中...</p>
      </div>

      <!-- 空状态 -->
      <div v-else-if="historyList.length === 0" class="empty-state">
        <div class="empty-icon">📝</div>
        <p class="empty-text">暂无优化历史</p>
        <el-button type="primary" @click="goDetail">去优化简历</el-button>
      </div>

      <!-- 主内容：左侧历史 + 右侧对比 -->
      <div v-else class="main-layout">
        <!-- 左侧：历史记录列表（占2/10） -->
        <div class="history-list">
          <div class="list-header">
            <h3>优化记录（共 {{ historyList.length }} 条）</h3>
          </div>
          <div class="list-body">
            <div
                v-for="(item, index) in historyList"
                :key="item.id"
                class="history-card"
                :class="{ 'active': selectedHistory?.id === item.id }"
                @click="selectHistory(item)"
            >
              <div class="history-index">{{ index + 1 }}</div>
              <div class="history-info">
                <div class="history-time">{{ formatTime(item.createdAt) }}</div>
                <div v-if="item.targetRole" class="target-role">
                  目标岗位：{{ item.targetRole }}
                </div>
              </div>
              <el-icon class="arrow-icon"><i class="el-icon-arrow-right"></i></el-icon>
            </div>
          </div>
        </div>

        <!-- 右侧：简历对比（占8/10，左右平分） -->
        <div v-if="selectedHistory" class="compare-section">
          <!-- 顶部信息栏 -->
          <div class="compare-header">
            <div class="compare-info">
              <span class="compare-label">对比预览</span>
              <span class="compare-time">{{ formatTime(selectedHistory.createdAt) }}</span>
            </div>
            <div class="header-actions">
              <!-- ★ 新增：复制优化内容按钮 -->
              <el-button
                  type="default"
                  size="small"
                  @click="copyOptimizedText"
                  :disabled="!selectedHistory?.optimizedText"
              >
                <el-icon><i class="el-icon-document-copy"></i></el-icon>
                复制优化内容
              </el-button>
              <!-- 导出PDF按钮 -->
              <el-button
                  type="primary"
                  size="small"
                  @click="exportOptimized"
                  :disabled="!optimizedHtml"
              >
                <el-icon><i class="el-icon-download"></i></el-icon>
                导出PDF
              </el-button>
            </div>
          </div>

          <!-- 对比内容 -->
          <div class="compare-container">
            <!-- 左侧：原始简历 -->
            <div class="compare-panel original">
              <div class="panel-header">
                <span class="panel-title">原始简历</span>
              </div>
              <div class="panel-content">
                <div v-if="originalHtml" class="iframe-wrapper">
                  <iframe :srcdoc="addPaddingToHtml(originalHtml)" class="preview-iframe"></iframe>
                </div>
                <div v-else class="panel-loading">
                  <el-icon class="is-loading"><i class="el-icon-loading"></i></el-icon>
                  <span>加载中...</span>
                </div>
              </div>
            </div>

            <!-- 右侧：优化简历 -->
            <div class="compare-panel optimized">
              <div class="panel-header">
                <span class="panel-title">优化简历</span>
              </div>
              <div class="panel-content">
                <div v-if="optimizedHtml" class="iframe-wrapper">
                  <iframe :srcdoc="addPaddingToHtml(optimizedHtml)" class="preview-iframe"></iframe>
                </div>
                <div v-else class="panel-loading">
                  <el-icon class="is-loading"><i class="el-icon-loading"></i></el-icon>
                  <span>加载中...</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 未选中时 -->
        <div v-else class="no-selection">
          <el-icon :size="48"><i class="el-icon-document"></i></el-icon>
          <p>请选择一条历史记录查看对比</p>
        </div>
      </div>
    </el-main>
  </div>
</template>

<script setup>
/**
 * 优化历史页面
 * ★ 修改：左侧2/10历史 + 右侧8/10对比；HTML iframe展示；添加复制按钮
 */
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getHistoryList } from '../api/history'
import { generateHtml, exportFromHtml } from '../api/resume'

const router = useRouter()
const route = useRoute()

// 响应式数据
const loading = ref(false)
const historyList = ref([])
const selectedHistory = ref(null)
const resumeId = ref(null)
const currentTemplate = ref(1)

// HTML内容
const originalHtml = ref('')
const optimizedHtml = ref('')
const exporting = ref(false)

/**
 * 组件挂载时加载数据
 */
onMounted(() => {
  resumeId.value = route.params.resumeId
  if (resumeId.value) {
    fetchHistoryList()
  }
})

/**
 * 获取优化历史列表
 */
const fetchHistoryList = async () => {
  loading.value = true
  try {
    const res = await getHistoryList(resumeId.value)
    if (res.code === 200) {
      historyList.value = res.data
      // 默认选中第一条
      if (historyList.value.length > 0) {
        selectHistory(historyList.value[0])
      }
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    ElMessage.error('获取历史记录失败')
  } finally {
    loading.value = false
  }
}

/**
 * 选择历史记录，加载对应的HTML
 */
const selectHistory = async (item) => {
  selectedHistory.value = item
  // 清空现有内容
  originalHtml.value = ''
  optimizedHtml.value = ''

  try {
    // 并行加载两个版本的HTML
    const [originalRes, optimizedRes] = await Promise.all([
      generateHtml(resumeId.value, 'original', currentTemplate.value),
      generateHtml(resumeId.value, 'optimized', currentTemplate.value)
    ])

    if (originalRes.code === 200) {
      originalHtml.value = originalRes.data.htmlContent
    }
    if (optimizedRes.code === 200) {
      optimizedHtml.value = optimizedRes.data.htmlContent
    }
  } catch (error) {
    console.error('加载HTML失败:', error)
  }
}

/**
 * 切换模板
 */
const handleTemplateChange = async () => {
  if (selectedHistory.value) {
    await selectHistory(selectedHistory.value)
  }
}

/**
 * ★ 新增：复制优化后的文本内容
 */
const copyOptimizedText = async () => {
  if (!selectedHistory.value?.optimizedText) {
    ElMessage.warning('没有可复制的优化内容')
    return
  }

  try {
    await navigator.clipboard.writeText(selectedHistory.value.optimizedText)
    ElMessage.success('已复制到剪贴板')
  } catch (error) {
    // 降级方案
    const textarea = document.createElement('textarea')
    textarea.value = selectedHistory.value.optimizedText
    document.body.appendChild(textarea)
    textarea.select()
    document.execCommand('copy')
    document.body.removeChild(textarea)
    ElMessage.success('已复制到剪贴板')
  }
}

/**
 * 导出优化版PDF
 */
const exportOptimized = async () => {
  if (!optimizedHtml.value) {
    ElMessage.warning('请等待内容加载完成')
    return
  }

  exporting.value = true
  try {
    const res = await exportFromHtml(resumeId.value, 'optimized', currentTemplate.value)

    const blob = new Blob([res], {type: 'application/pdf'})
    const url = window.URL.createObjectURL(blob)

    const link = document.createElement('a')
    link.href = url
    const date = new Date().toISOString().slice(0, 10).replace(/-/g, '')
    const templateName = ['', '简约蓝', '商务灰', '创意橙'][currentTemplate.value]
    link.setAttribute('download', `简历_优化版_${templateName}_${date}.pdf`)

    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)

    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
    console.error('导出失败:', error)
  } finally {
    exporting.value = false
  }
}

/**
 * 给HTML内容注入padding样式
 */
const addPaddingToHtml = (html) => {
  if (!html) return ''

  if (!html.includes('xy-padding-injected')) {
    const paddingStyle = `
      <style id="xy-padding-injected">
        body {
          margin: 0 !important;
          padding: 0 40px !important;
          box-sizing: border-box !important;
        }
        .container {
          width: 100% !important;
          max-width: 700px !important;
          margin: 0 auto !important;
        }
      </style>
    `
    html = html.replace('</head>', paddingStyle + '</head>')
  }

  return html
}

/**
 * 格式化时间
 */
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hour = String(date.getHours()).padStart(2, '0')
  const minute = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hour}:${minute}`
}

/**
 * 返回上一页
 */
const goBack = () => {
  router.back()
}

/**
 * 跳转到简历详情页
 */
const goDetail = () => {
  router.push(`/resume/${resumeId.value}`)
}
</script>

<style scoped>
/* ========== 整体布局 ========== */
.history-container {
  height: 100vh;
  background: #f5f7fa;
  display: flex;
  flex-direction: column;
}

/* 顶部导航栏 */
.top-nav {
  background: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  height: 60px;
  flex-shrink: 0;
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

/* 主内容区 */
.main-content {
  flex: 1;
  padding: 20px;
  overflow: hidden;
}

/* 加载状态 */
.loading-state {
  text-align: center;
  padding: 100px 20px;
  color: #909399;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 100px 20px;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 20px;
}

.empty-text {
  color: #909399;
  margin-bottom: 20px;
}

/* ========== 主布局：左侧2/10 + 右侧8/10 ========== */
.main-layout {
  height: 100%;
  display: flex;
  gap: 20px;
  overflow: hidden;
}

/* ========== 左侧历史记录（占2/10） ========== */
.history-list {
  width: 20%;
  min-width: 200px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.list-header {
  padding: 16px 20px;
  border-bottom: 1px solid #e4e7ed;
  flex-shrink: 0;
}

.list-header h3 {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.list-body {
  flex: 1;
  overflow-y: auto;
}

.history-card {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: all 0.2s ease;
}

.history-card:hover {
  background: #f5f7fa;
}

.history-card.active {
  background: #ecf5ff;
  border-left: 3px solid #409eff;
}

.history-index {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #f0f2f5;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  color: #606266;
  margin-right: 10px;
  flex-shrink: 0;
}

.history-card.active .history-index {
  background: #409eff;
  color: #fff;
}

.history-info {
  flex: 1;
  min-width: 0;
}

.history-time {
  font-size: 13px;
  color: #303133;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.target-role {
  font-size: 11px;
  color: #909399;
  margin-top: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.arrow-icon {
  color: #c0c4cc;
  margin-left: 8px;
  flex-shrink: 0;
}

/* ========== 右侧对比区域（占8/10） ========== */
.compare-section {
  flex: 1;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.compare-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  border-bottom: 1px solid #e4e7ed;
  flex-shrink: 0;
}

.compare-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.compare-label {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.compare-time {
  font-size: 13px;
  color: #909399;
}

/* ★ 新增：操作按钮组 */
.header-actions {
  display: flex;
  gap: 8px;
}

/* 对比容器：左右平分 */
.compare-container {
  flex: 1;
  display: flex;
  gap: 16px;
  padding: 16px;
  overflow: hidden;
}

.compare-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #e4e7ed;
}

.compare-panel.original {
  background: #fafafa;
}

.compare-panel.optimized {
  background: #fff;
}

.panel-header {
  padding: 10px 14px;
  background: rgba(0, 0, 0, 0.02);
  border-bottom: 1px solid #e4e7ed;
  flex-shrink: 0;
}

.panel-title {
  font-size: 13px;
  font-weight: 600;
  color: #606266;
}

.panel-content {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

/* iframe包装器 - 实现左右padding */
.iframe-wrapper {
  flex: 1;
  padding: 16px;
  box-sizing: border-box;
  overflow-x: hidden;
}

.preview-iframe {
  width: 100%;
  height: 100%;
  border: none;
  display: block;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.panel-loading {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #909399;
}

/* 未选中状态 */
.no-selection {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
  gap: 12px;
}
</style>
