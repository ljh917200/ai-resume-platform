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
    </el-header>

    <el-main>
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

      <!-- 历史列表 -->
      <div v-else class="history-content">
        <!-- 历史记录列表 -->
        <div class="history-list">
          <div class="list-header">
            <h3>优化记录（共 {{ historyList.length }} 条）</h3>
          </div>
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

        <!-- 对比区域 -->
        <div v-if="selectedHistory" class="compare-section">
          <div class="compare-header">
            <h3>优化对比</h3>
            <span class="compare-time">{{ formatTime(selectedHistory.createdAt) }}</span>
          </div>

          <div class="compare-container">
            <!-- 左侧：原始版本 -->
            <div class="compare-panel original">
              <div class="panel-header">
                <span class="panel-title">原始版本</span>
              </div>
              <div class="panel-content">
                <pre>{{ selectedHistory.originalText }}</pre>
              </div>
            </div>

            <!-- 右侧：优化版本 -->
            <div class="compare-panel optimized">
              <div class="panel-header">
                <span class="panel-title">优化版本</span>
                <el-button
                    type="primary"
                    size="small"
                    @click="copyOptimized"
                >
                  复制优化内容
                </el-button>
              </div>
              <div class="panel-content">
                <pre>{{ selectedHistory.optimizedText }}</pre>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-main>
  </div>
</template>

<script setup>
/**
 * 优化历史页面
 * 功能：
 * 1. 显示某份简历的所有优化历史记录
 * 2. 点击历史记录进行前后对比
 * 3. 支持复制优化后的内容
 */

import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getHistoryList } from '../api/history'

const router = useRouter()
const route = useRoute()

// 响应式数据
const loading = ref(false)
const historyList = ref([])
const selectedHistory = ref(null)
const resumeId = ref(null)

/**
 * 组件挂载时加载数据
 */
onMounted(() => {
  // 从路由参数获取简历ID
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
        selectedHistory.value = historyList.value[0]
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
 * 选择历史记录
 */
const selectHistory = (item) => {
  selectedHistory.value = item
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
 * 复制优化后的内容
 */
const copyOptimized = () => {
  if (selectedHistory.value?.optimizedText) {
    navigator.clipboard.writeText(selectedHistory.value.optimizedText)
    ElMessage.success('已复制到剪贴板')
  }
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
.history-container {
  min-height: 100vh;
  background: #f5f5f5;
}

/* 顶部导航栏 */
.top-nav {
  background: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
  height: 60px;
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
.el-main {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
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

/* 历史内容 */
.history-content {
  display: flex;
  gap: 20px;
}

/* 历史列表 */
.history-list {
  width: 320px;
  flex-shrink: 0;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.list-header {
  padding: 20px;
  border-bottom: 1px solid #e4e7ed;
}

.list-header h3 {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.history-card {
  display: flex;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: all 0.3s ease;
}

.history-card:hover {
  background: #f5f7fa;
}

.history-card.active {
  background: #ecf5ff;
  border-left: 3px solid #409eff;
}

.history-index {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #f0f2f5;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  color: #606266;
  margin-right: 12px;
}

.history-card.active .history-index {
  background: #409eff;
  color: #fff;
}

.history-info {
  flex: 1;
}

.history-time {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

.target-role {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.arrow-icon {
  color: #c0c4cc;
}

/* 对比区域 */
.compare-section {
  flex: 1;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.compare-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #e4e7ed;
}

.compare-header h3 {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.compare-time {
  font-size: 14px;
  color: #909399;
}

.compare-container {
  display: flex;
  gap: 20px;
  padding: 20px;
  height: calc(100vh - 200px);
  overflow: hidden;
}

.compare-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  border-radius: 8px;
  overflow: hidden;
}

.compare-panel.original {
  background: #fafafa;
  border: 1px solid #e4e7ed;
}

.compare-panel.optimized {
  background: #f0f7ff;
  border: 1px solid #b3d8ff;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: rgba(0, 0, 0, 0.02);
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.panel-title {
  font-size: 14px;
  font-weight: 600;
  color: #606266;
}

.panel-content {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
}

.panel-content pre {
  white-space: pre-wrap;
  word-wrap: break-word;
  font-family: inherit;
  font-size: 14px;
  line-height: 1.8;
  color: #303133;
  margin: 0;
}
</style>