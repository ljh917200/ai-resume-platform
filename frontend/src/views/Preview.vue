<template>
  <div class="preview-container">
    <!-- 顶部导航栏 -->
    <el-header class="top-nav">
      <div class="logo" @click="goHome">
        <el-icon class="logo-icon"><i class="el-icon-s-operation"></i></el-icon>
        <span>AI简历优化</span>
      </div>
      <div class="nav-actions">
        <!-- 模板选择 -->
        <el-select v-model="currentTemplate" @change="handleTemplateChange" style="width: 120px;">
          <el-option :value="1" label="简约蓝" />
          <el-option :value="2" label="商务灰" />
          <el-option :value="3" label="创意橙" />
        </el-select>

        <!-- 优化按钮（没优化过才显示） -->
        <el-button v-if="!hasOptimized" type="warning" @click="handleOptimize" :loading="optimizing">
          <el-icon><i class="el-icon-magic-stick"></i></el-icon>
          {{ optimizing ? 'AI优化中...' : '优化简历' }}
        </el-button>

        <!-- ★ 新增：查看历史按钮（有优化历史才显示） -->
        <el-button
            v-if="resumeData?.optimizedText"
            @click="goHistory"
        >
          <el-icon><i class="el-icon-time"></i></el-icon>
          优化历史
        </el-button>

        <!-- 导出按钮 - 未优化：直接导出原始简历 -->
        <el-button
            v-if="!hasOptimized"
            type="success"
            @click="handleExport('original')"
            :disabled="!htmlContent"
            :loading="exporting"
        >
          <el-icon><i class="el-icon-download"></i></el-icon>
          导出PDF
        </el-button>

        <!-- 导出按钮 - 已优化：下拉选择导出哪个版本 -->
        <el-dropdown v-else @command="handleExport" trigger="click">
          <el-button type="success" :loading="exporting">
            <el-icon><i class="el-icon-download"></i></el-icon>
            导出PDF
            <el-icon class="el-icon--right"><i class="el-icon-arrow-down"></i></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="original">
                导出原始简历
              </el-dropdown-item>
              <el-dropdown-item command="optimized">
                导出优化简历
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>

    <!-- 主内容区 -->
    <el-main class="main-content">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <el-icon class="is-loading" :size="40"><Loading /></el-icon>
        <p>{{ generating ? 'AI正在生成简历...' : '加载中...' }}</p>
      </div>

      <!-- 有优化版：左右对比展示 -->
      <div v-else-if="hasOptimized" class="compare-wrapper">
        <!-- 左侧：原始简历 -->
        <div class="compare-item">
          <div class="compare-header">
            <span class="compare-title">原始简历</span>
            <el-tag size="small" type="info">原始版</el-tag>
          </div>
          <div class="compare-content">
            <div v-if="originalHtml" class="iframe-container">
              <iframe :srcdoc="addPaddingToHtml(originalHtml)" class="preview-iframe"></iframe>
            </div>
            <div v-else class="loading-inline">
              <el-icon class="is-loading"><Loading /></el-icon>
              <span>加载中...</span>
            </div>
          </div>
        </div>

        <!-- 右侧：优化简历 -->
        <div class="compare-item">
          <div class="compare-header">
            <span class="compare-title">优化简历</span>
            <el-tag size="small" type="success">AI优化版</el-tag>
          </div>
          <div class="compare-content">
            <div v-if="optimizedHtml" class="iframe-container">
              <iframe :srcdoc="addPaddingToHtml(optimizedHtml)" class="preview-iframe"></iframe>
            </div>
            <div v-else class="loading-inline">
              <el-icon class="is-loading"><Loading /></el-icon>
              <span>加载中...</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 无优化版：单列居中展示 -->
      <div v-else-if="htmlContent" class="single-wrapper">
        <div class="iframe-container">
          <iframe :srcdoc="addPaddingToHtml(htmlContent)" class="preview-iframe"></iframe>
        </div>
      </div>

      <!-- 无内容时 -->
      <div v-else class="empty-container">
        <el-empty description="暂无预览内容">
          <el-button type="primary" @click="generatePreview" :loading="generating">
            {{ generating ? '生成中...' : '生成预览' }}
          </el-button>
        </el-empty>
      </div>
    </el-main>

    <!-- 优化弹窗 -->
    <el-dialog v-model="showOptimizeDialog" title="优化简历" width="400px">
      <el-form>
        <el-form-item label="目标岗位">
          <el-input v-model="targetRole" placeholder="例如：前端开发工程师（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showOptimizeDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmOptimize" :loading="optimizing">
          {{ optimizing ? '优化中...' : '开始优化' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 优化全屏遮罩 -->
    <transition name="fade">
      <div v-if="optimizing" class="optimize-overlay">
        <div class="optimize-modal">
          <div class="optimize-animation">
            <div class="pulse-ring"></div>
            <div class="pulse-ring delay-1"></div>
            <div class="pulse-ring delay-2"></div>
            <div class="ai-icon">
              <el-icon :size="48"><i class="el-icon-magic-stick"></i></el-icon>
            </div>
          </div>
          <h3 class="optimize-title">AI 正在优化您的简历</h3>
          <p class="optimize-desc">{{ optimizeTip }}</p>
          <div class="progress-bar">
            <div class="progress-fill" :style="{ width: progressWidth }"></div>
          </div>
          <p class="optimize-time">预计需要 10-20 秒</p>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
/**
 * 简历预览页面（v1.7.0 核心页面）
 * ★ 修改：用JavaScript给HTML注入padding样式
 */
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import {
  getResume,
  generateHtml,
  exportFromHtml,
  optimizeResume,
  switchTemplate
} from '@/api/resume'

// ========== 路由 ==========
const route = useRoute()
const router = useRouter()

// ========== 响应式数据 ==========
const resumeId = route.params.id
const resumeData = ref(null)
const loading = ref(true)
const generating = ref(false)
const exporting = ref(false)
const optimizing = ref(false)

// HTML内容
const htmlContent = ref('')
const originalHtml = ref('')
const optimizedHtml = ref('')

const currentTemplate = ref(1)
const showOptimizeDialog = ref(false)
const targetRole = ref('')

// 优化动画相关
const optimizeTip = ref('正在分析简历结构...')
const progressWidth = ref('0%')
const optimizeTips = [
  '正在分析简历结构...',
  '正在提取核心亮点...',
  '正在优化语言表达...',
  '正在增强竞争力描述...',
  '正在润色排版建议...'
]
let tipInterval = null
let progressInterval = null

// ========== 计算属性 ==========
const hasOptimized = computed(() => {
  return resumeData.value?.optimizedStructuredData != null
})

// ========== 生命周期 ==========
onMounted(async () => {
  await loadResume()
})

// ========== 方法 ==========

/**
 * 给HTML内容注入padding样式
 * ★ 核心修改：用JS给HTML的body加左右padding
 */
const addPaddingToHtml = (html) => {
  if (!html) return ''

  // 如果HTML里还没有我们注入的padding样式，就加上
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
    // 插入到</head>之前
    html = html.replace('</head>', paddingStyle + '</head>')
  }

  return html
}

const loadResume = async () => {
  loading.value = true
  try {
    const res = await getResume(resumeId)
    resumeData.value = res.data || res
    currentTemplate.value = resumeData.value?.templateId || 1

    if (hasOptimized.value) {
      await loadBothVersions()
    } else {
      await generatePreview()
    }
  } catch (error) {
    ElMessage.error('加载简历失败')
    console.error('加载简历失败:', error)
  } finally {
    loading.value = false
  }
}

const loadBothVersions = async () => {
  generating.value = true
  try {
    const [originalRes, optimizedRes] = await Promise.all([
      generateHtml(resumeId, 'original', currentTemplate.value),
      generateHtml(resumeId, 'optimized', currentTemplate.value)
    ])

    if (originalRes.code === 200) {
      originalHtml.value = originalRes.data.htmlContent
    }
    if (optimizedRes.code === 200) {
      optimizedHtml.value = optimizedRes.data.htmlContent
    }
  } catch (error) {
    console.error('加载双版本失败:', error)
  } finally {
    generating.value = false
  }
}

const generatePreview = async () => {
  if (!resumeData.value) return

  generating.value = true
  try {
    const res = await generateHtml(resumeId, 'original', currentTemplate.value)

    if (res.code === 200) {
      htmlContent.value = res.data.htmlContent
    } else {
      ElMessage.error(res.message || '生成失败')
    }
  } catch (error) {
    ElMessage.error('生成预览失败')
    console.error('生成预览失败:', error)
  } finally {
    generating.value = false
  }
}

const handleTemplateChange = async () => {
  try {
    await switchTemplate(resumeId, currentTemplate.value)
  } catch (error) {
    console.error('保存模板失败:', error)
  }

  if (hasOptimized.value) {
    await loadBothVersions()
  } else {
    await generatePreview()
  }
}

const handleOptimize = () => {
  showOptimizeDialog.value = true
}

const startOptimizeAnimation = () => {
  let tipIndex = 0
  tipInterval = setInterval(() => {
    tipIndex = (tipIndex + 1) % optimizeTips.length
    optimizeTip.value = optimizeTips[tipIndex]
  }, 2000)

  let progress = 0
  progressInterval = setInterval(() => {
    progress += Math.random() * 8
    if (progress >= 90) {
      progress = 90
      clearInterval(progressInterval)
    }
    progressWidth.value = progress + '%'
  }, 500)
}

const stopOptimizeAnimation = () => {
  if (tipInterval) {
    clearInterval(tipInterval)
    tipInterval = null
  }
  if (progressInterval) {
    clearInterval(progressInterval)
    progressInterval = null
  }
  progressWidth.value = '100%'
}

const confirmOptimize = async () => {
  showOptimizeDialog.value = false
  optimizing.value = true
  startOptimizeAnimation()

  try {
    const res = await optimizeResume(resumeId, targetRole.value)

    stopOptimizeAnimation()
    await new Promise(resolve => setTimeout(resolve, 300))

    if (res.code === 200) {
      ElMessage.success('优化成功！')
      resumeData.value = {...resumeData.value, ...res.data}
      await loadBothVersions()
    } else {
      ElMessage.error(res.message || '优化失败')
    }
  } catch (error) {
    ElMessage.error('优化失败')
    console.error('优化失败:', error)
  } finally {
    stopOptimizeAnimation()
    optimizing.value = false
  }
}

/**
 * 跳转到优化历史页面
 */
const goHistory = () => {
  router.push(`/history/${resumeId}`)
}

/**
 * 导出 PDF
 * @param {string} type - 'original' 或 'optimized'
 */
const handleExport = async (type) => {
  let htmlToExport = ''
  if (type === 'optimized') {
    htmlToExport = optimizedHtml.value
  } else {
    htmlToExport = hasOptimized.value ? originalHtml.value : htmlContent.value
  }

  if (!htmlToExport) {
    ElMessage.warning('请先生成预览')
    return
  }

  exporting.value = true
  try {
    const res = await exportFromHtml(resumeId, type, currentTemplate.value)

    const blob = new Blob([res], {type: 'application/pdf'})
    const url = window.URL.createObjectURL(blob)

    const link = document.createElement('a')
    link.href = url
    const date = new Date().toISOString().slice(0, 10).replace(/-/g, '')
    const typeName = type === 'optimized' ? '优化版' : '原始版'
    const templateName = ['', '简约蓝', '商务灰', '创意橙'][currentTemplate.value]
    link.setAttribute('download', `简历_${typeName}_${templateName}_${date}.pdf`)

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

const goHome = () => {
  router.push('/home')
}
</script>

<style scoped>
/* ========== 整体布局 ========== */
.preview-container {
  min-height: 100vh;
  background: #f5f7fa;
  overflow: hidden;
}

.top-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  height: 60px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #333;
  cursor: pointer;
}

.logo:hover { color: #409eff; }

.logo-icon { font-size: 20px; color: #409eff; }

.nav-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* ========== 主内容区 ========== */
.main-content {
  padding: 20px;
  height: calc(100vh - 60px);
  box-sizing: border-box;
  overflow: hidden;
}

/* ========== 单列布局 ========== */
.single-wrapper {
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  overflow: auto;
}

/* ========== iframe容器 ========== */
.iframe-container {
  /* iframe自适应容器宽度 */
  width: 100%;
  min-width: 700px;
  max-width: 780px;  /* 最多780px，给padding留空间 */
  background: #fff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

/* ========== iframe样式 ========== */
.preview-iframe {
  border: none;
  display: block;
  /* iframe宽度100%，由容器决定 */
  width: 100%;
  /* 高度自适应内容，最小1000px */
  height: 1200px;
  background: #fff;
}

/* ========== 左右对比布局 ========== */
.compare-wrapper {
  display: flex;
  gap: 20px;
  height: 100%;
  overflow: hidden;
}

.compare-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.compare-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px;
  border-bottom: 1px solid #e4e7ed;
  background: #fafafa;
  flex-shrink: 0;
}

.compare-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.compare-content {
  flex: 1;
  overflow: auto;
  display: flex;
  justify-content: center;
  padding: 16px;
}

.compare-content .iframe-container {
  width: 100%;
  max-width: none;
  height: 100%;
}

.compare-content .preview-iframe {
  width: 100%;
  height: 100%;
}

/* ========== 加载状态 ========== */
.loading-container,
.loading-inline {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  gap: 12px;
  color: #909399;
}

.loading-inline { flex-direction: row; height: auto; padding: 40px; }

/* ========== 空状态 ========== */
.empty-container {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}

/* ========== 优化动画 ========== */
.optimize-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}

.optimize-modal {
  background: #fff;
  border-radius: 16px;
  padding: 48px;
  text-align: center;
  min-width: 360px;
}

.optimize-animation {
  position: relative;
  width: 120px;
  height: 120px;
  margin: 0 auto 24px;
}

.pulse-ring {
  position: absolute;
  width: 100%;
  height: 100%;
  border: 2px solid #F76B1C;
  border-radius: 50%;
  animation: pulse 2s ease-out infinite;
}

.pulse-ring.delay-1 { animation-delay: 0.5s; }
.pulse-ring.delay-2 { animation-delay: 1s; }

@keyframes pulse {
  0% { transform: scale(0.5); opacity: 1; }
  100% { transform: scale(1.5); opacity: 0; }
}

.ai-icon {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: #F76B1C;
}

.optimize-title { font-size: 20px; color: #303133; margin-bottom: 12px; }
.optimize-desc { color: #909399; margin-bottom: 24px; }

.progress-bar {
  width: 100%;
  height: 6px;
  background: #f0f0f0;
  border-radius: 3px;
  overflow: hidden;
  margin-bottom: 12px;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #F76B1C, #FF9A56);
  border-radius: 3px;
  transition: width 0.3s ease;
}

.optimize-time { font-size: 12px; color: #c0c4cc; }

/* ========== 过渡动画 ========== */
.fade-enter-active, .fade-leave-active { transition: opacity 0.3s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
