<template>
  <div class="preview-container">
    <!-- 1. sub-header 顶部操作条 -->
    <header class="sub-header">
      <div class="sub-header-left">
        <a href="#" class="back-link" @click.prevent="goBack">
          <span class="back-icon">←</span>
          返回列表
        </a>
        <span class="separator"></span>
        <span class="file-name">{{ resumeData?.fileName || '未命名简历' }}</span>
      </div>

      <div class="sub-header-right">
        <!-- 模板选择下拉框 -->
        <el-select
            v-model="currentTemplate"
            placeholder="选择模板"
            class="template-select"
            @change="handleTemplateChange"
        >
          <el-option label="简约蓝" :value="1" />
          <el-option label="商务灰" :value="2" />
          <el-option label="创意橙" :value="3" />
        </el-select>

        <!-- 缩放控制 -->
        <div class="zoom-controls">
          <button class="zoom-btn" @click="zoomOut" :disabled="zoom <= 50">
            <el-icon><Minus /></el-icon>
          </button>
          <span class="zoom-value">{{ zoom }}%</span>
          <button class="zoom-btn" @click="zoomIn" :disabled="zoom >= 150">
            <el-icon><Plus /></el-icon>
          </button>
        </div>

        <!-- 分隔线 -->
        <span class="vertical-separator"></span>
        <!-- 显示头像开关 -->
        <div class="avatar-switch">
          <span class="switch-label">显示头像</span>
          <el-switch
              v-model="showAvatarInResume"
              @change="handleShowAvatarChange"
              active-color="#1a1a2e"
              inactive-color="#e8e8e8"
          />
        </div>

        <!-- 优化按钮 -->
        <el-button
            v-if="!hasOptimized"
            class="btn-optimize"
            @click="handleOptimize"
            :loading="optimizing"
        >
          <el-icon><EditPen /></el-icon>
          {{ optimizing ? 'AI优化中...' : '优化简历' }}
        </el-button>

        <!-- 重新优化按钮（已优化） -->
        <el-button
            v-else
            class="btn-reoptimize"
            @click="handleOptimize"
            :loading="optimizing"
        >
          <el-icon><EditPen /></el-icon>
          {{ optimizing ? 'AI优化中...' : '重新优化' }}
        </el-button>

        <!-- 优化历史按钮 -->
        <el-button
            v-if="resumeData?.optimizedText"
            class="btn-history"
            @click="goHistory"
        >
          
          优化历史
        </el-button>

        <!-- 导出PDF下拉按钮 -->
        <el-dropdown v-if="hasOptimized" @command="handleExport" trigger="click">
          <el-button class="btn-export">
            <el-icon><Download /></el-icon>
            导出PDF
            <el-icon class="dropdown-arrow"><ArrowDown /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu class="export-menu">
              <el-dropdown-item command="original">导出原始简历</el-dropdown-item>
              <el-dropdown-item command="optimized">导出优化简历</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <el-button
            v-else
            class="btn-export"
            @click="handleExport('original')"
            :disabled="!htmlContent"
            :loading="exporting"
        >
          <el-icon><Download /></el-icon>
          导出PDF
        </el-button>
      </div>
    </header>

    <!-- 3. 预览区 -->
    <main class="preview-area">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <el-icon class="is-loading" :size="40"><Loading /></el-icon>
        <p>{{ generating ? 'AI正在生成简历...' : '加载中...' }}</p>
      </div>

      <!-- 已优化：左右对比布局 -->
      <div v-else-if="hasOptimized && originalHtml && optimizedHtml" class="compare-layout">
        <!-- 左侧：原始版 -->
        <div class="compare-panel">
          <div class="panel-header">
            <div class="panel-title">
              原始简历
            </div>
          </div>
          <div class="paper-wrapper">
            <div class="paper" :style="{ transform: `scale(${zoom / 100})`, transformOrigin: 'top center' }">
              <div class="iframe-wrapper">
                <iframe
                    :key="`original-${currentTemplate}-${zoom}`"
                    :srcdoc="addPaddingToHtml(originalHtml)"
                    class="preview-iframe"
                    @load="(e) => adjustIframeHeight(e)"
                ></iframe>
              </div>
            </div>
          </div>
        </div>

        <!-- 分隔线 -->
        <div class="compare-divider">
          <div class="divider-line"></div>
          <div class="divider-label">VS</div>
          <div class="divider-line"></div>
        </div>

        <!-- 右侧：优化版 -->
        <div class="compare-panel">
          <div class="panel-header">
            <div class="panel-title">
              优化简历
            </div>
          </div>
          <div class="paper-wrapper">
            <div class="paper" :style="{ transform: `scale(${zoom / 100})`, transformOrigin: 'top center' }">
              <div class="iframe-wrapper">
                <iframe
                    :key="`optimized-${currentTemplate}-${zoom}`"
                    :srcdoc="addPaddingToHtml(optimizedHtml)"
                    class="preview-iframe"
                    @load="(e) => adjustIframeHeight(e)"
                ></iframe>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 未优化：单列布局（修复版） -->
      <div v-else-if="htmlContent" class="single-layout">
        <div class="single-layout-center">
          <div class="paper-wrapper">
            <div
              class="paper single-paper"
              :style="{
                transform: `scale(${zoom / 100})`,
                transformOrigin: 'top center',
                width: `${A4_WIDTH}px`
              }"
            >
              <div class="iframe-wrapper">
                <iframe
                    :key="`single-${currentTemplate}-${zoom}`"
                    :srcdoc="addPaddingToHtml(htmlContent)"
                    class="preview-iframe"
                    @load="(e) => adjustIframeHeight(e)"
                ></iframe>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-else class="empty-container">
        <el-empty description="暂无预览内容">
          <el-button class="btn-primary-ink" @click="generatePreview" :loading="generating">
            {{ generating ? '生成中...' : '生成预览' }}
          </el-button>
        </el-empty>
      </div>
    </main>

    <!-- 优化弹窗 -->
    <el-dialog v-model="showOptimizeDialog" title="优化简历" width="400px" class="optimize-dialog">
      <el-form>
        <el-form-item label="目标岗位">
          <el-input v-model="targetRole" placeholder="例如：前端开发工程师（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button class="btn-outline-ink" @click="showOptimizeDialog = false">取消</el-button>
        <el-button class="btn-primary-ink" @click="confirmOptimize" :loading="optimizing">
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
              <el-icon :size="48"><EditPen /></el-icon>
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
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Loading, EditPen, Download, ArrowDown, Plus, Minus
  
} from '@element-plus/icons-vue'
import {
  getResume,
  generateHtml,
  exportFromHtml,
  optimizeResume,
  switchTemplate,
  toggleShowAvatar
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

// 缩放控制
const zoom = ref(100)

// A4纸宽度基准（794px ≈ 210mm @96dpi）
const A4_WIDTH = 794

// 面板宽度（用于对比布局）
const panelWidth = computed(() => {
  return Math.min(700, window.innerWidth * 0.4) * zoom.value / 100
})

const showOptimizeDialog = ref(false)
const targetRole = ref('')
const showAvatarInResume = ref(false)
const hasUserAvatar = ref(false)

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
  window.addEventListener('resize', handleWindowResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleWindowResize)
  stopOptimizeAnimation()
})

// ========== 方法 ==========

/**
 * 窗口resize时重新调整所有iframe高度
 */
const handleWindowResize = () => {
  document.querySelectorAll('.preview-iframe').forEach(iframe => {
    adjustIframeHeight({ target: iframe })
  })
}

/**
 * 自适应iframe高度：读取iframe内容实际高度并设置
 */
const adjustIframeHeight = (event) => {
  const iframe = event.target
  if (!iframe) return
  try {
    const doc = iframe.contentDocument || iframe.contentWindow?.document
    if (!doc || !doc.body) return
    // 获取内容实际高度
    const contentHeight = Math.max(
      doc.body.scrollHeight,
      doc.body.offsetHeight,
      doc.documentElement.scrollHeight,
      doc.documentElement.offsetHeight
    )
    // 设置最小高度，避免内容太少时太矮
    iframe.style.height = Math.max(contentHeight, 600) + 'px'
  } catch (e) {
    // 跨域iframe无法访问，使用默认高度
    iframe.style.height = '1200px'
  }
}

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

const loadResume = async () => {
  loading.value = true
  try {
    const res = await getResume(resumeId)
    resumeData.value = res.data || res
    currentTemplate.value = resumeData.value?.templateId || 1

    showAvatarInResume.value = resumeData.value?.showAvatar === 1

    const userStr = localStorage.getItem('user')
    if (userStr) {
      try {
        const user = JSON.parse(userStr)
        hasUserAvatar.value = !!(user.avatarUrl && user.avatarUrl.trim() !== '')
      } catch (e) {
        hasUserAvatar.value = false
      }
    }

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

const handleTemplateChange = async (templateId) => {
  currentTemplate.value = templateId

  try {
    await switchTemplate(resumeId, templateId)
  } catch (error) {
    console.error('保存模板失败:', error)
  }

  if (hasOptimized.value) {
    await loadBothVersions()
  } else {
    await generatePreview()
  }
}

const zoomIn = () => {
  if (zoom.value < 150) {
    zoom.value += 10
  }
}

const zoomOut = () => {
  if (zoom.value > 50) {
    zoom.value -= 10
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

const goHistory = () => {
  router.push(`/history/${resumeId}`)
}

const goBack = () => {
  router.push('/home')
}

const handleShowAvatarChange = async (val) => {
  try {
    const res = await toggleShowAvatar(resumeId, val ? 1 : 0)
    if (res.code === 200) {
      ElMessage.success(val ? '已开启头像显示' : '已关闭头像显示')
      if (hasOptimized.value) {
        await loadBothVersions()
      } else {
        await generatePreview()
      }
    } else {
      showAvatarInResume.value = !val
      ElMessage.error(res.message || '设置失败')
    }
  } catch (error) {
    showAvatarInResume.value = !val
    ElMessage.error('设置失败')
    console.error('切换头像显示失败:', error)
  }
}

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
</script>

<style scoped>
/* ========== 整体布局 ========== */
.preview-container {
  min-height: 100vh;
  background: #f5f5f5;
  display: flex;
  flex-direction: column;
  margin: 0;
  padding: 0;
}

:deep(body) {
  margin: 0;
  padding: 0;
}

/* ========== 1. sub-header 顶部操作条 ========== */
.sub-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
  height: 48px;
  flex-shrink: 0;
}

.sub-header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.back-link {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #1a1a2e;
  text-decoration: none;
  font-size: 14px;
  transition: color 0.2s ease;
  position: relative;
}

.back-link::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 0;
  height: 1px;
  background: #1a1a2e;
  transition: width 0.2s ease;
}

.back-link:hover::after {
  width: 100%;
}

.back-icon {
  font-size: 14px;
}

.separator {
  width: 1px;
  height: 20px;
  background: #e8e8e8;
}

.file-name {
  font-size: 14px;
  color: #666;
}

/* 模板选择下拉框 */
.template-select {
  width: 140px;
}

.template-select :deep(.el-select__wrapper) {
  background: #fff;
  border-radius: 8px;
  border-color: #e8e8e8;
}

.template-select :deep(.el-select__wrapper.is-focus) {
  border-color: #1a1a2e;
  box-shadow: 0 0 0 3px rgba(26, 26, 46, 0.06);
}

/* 缩放控制 */
.zoom-controls {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #fff;
  border-radius: 8px;
  padding: 4px;
}

.zoom-btn {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #1a1a2e;
  background: transparent;
  border-radius: 50%;
  cursor: pointer;
  color: #1a1a2e;
  transition: all 0.1s ease;
}

.zoom-btn:hover:not(:disabled) {
  background: #1a1a2e;
  color: #fff;
}

.zoom-btn:active:not(:disabled) {
  transform: scale(0.97);
}

.zoom-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.zoom-value {
  font-size: 14px;
  color: #666;
  min-width: 50px;
  text-align: center;
}

.sub-header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* 垂直分隔线 */
.vertical-separator {
  width: 1px;
  height: 24px;
  background: #e8e8e8;
}

/* 头像开关 */
.avatar-switch {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 12px;
  border-right: 1px solid #e8e8e8;
}

.switch-label {
  font-size: 13px;
  color: #666;
}

/* ========== 3. 预览区 ========== */
.preview-area {
  flex: 1;
  background: #e8e8e8;
  overflow-y: auto;
  padding: 24px;
}

/* 加载状态 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 400px;
  gap: 12px;
  color: #666;
}

/* ========== 对比布局（已优化） ========== */
.compare-layout {
  display: flex;
  padding: 0 5%;
  gap: 10%;
}

.compare-panel {
  width: 40%;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  padding: 0 8px;
}

.panel-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 500;
  color: #1a1a2e;
}

.panel-badge {
  font-size: 12px;
  padding: 2px 8px;
  background: #f0f0f0;
  color: #666;
  border-radius: 4px;
}

.panel-badge.optimized {
  background: rgba(26, 26, 46, 0.1);
  color: #1a1a2e;
}

/* 分隔线 */
.compare-divider {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 0 16px;
  height: 100px;
}

.divider-line {
  flex: 1;
  width: 1px;
  background: #d0d0d0;
}

.divider-label {
  font-size: 14px;
  font-weight: 600;
  color: #999;
  padding: 8px 0;
}

/* ========== 单列布局（未优化）- 修复版 ========== */
.single-layout {
  display: flex;
  justify-content: center;
}

/* 关键修复：居中容器，约束纸张位置 */
.single-layout-center {
  display: flex;
  justify-content: center;
  width: 100%;
}

/* 单列布局的纸张：固定A4宽度 + 缩放transform */
.single-paper {
  /* A4宽度由inline style的 :style 绑定控制 */
  min-height: 600px;
}

/* 纸张包装器 */
.paper-wrapper {
  animation: paperEnter 500ms cubic-bezier(0.4, 0, 0.2, 1) both;
}

@keyframes paperEnter {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* A4纸张 */
.paper {
  background: #fff;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.08);
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 24px;
  transition: box-shadow 0.3s ease;
}

.paper:hover {
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.12);
}

.iframe-wrapper {
  width: 100%;
}

.preview-iframe {
  border: none;
  display: block;
  width: 100%;
  /* 不再写死高度，由 adjustIframeHeight 动态设置 */
  min-height: 600px;
  height: 1200px; /* 默认回退值，会被JS覆盖 */
  background: #fff;
}

/* 空状态 */
.empty-container {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 400px;
}

/* ========== 按钮样式 ========== */
.btn-optimize {
  background: #1a1a2e !important;
  border-color: #1a1a2e !important;
  color: #fff !important;
  border-radius: 8px !important;
  padding: 6px 16px !important;
  font-size: 14px !important;
  transition: transform 0.1s ease;
}

.btn-optimize:hover {
  background: #151525 !important;
  border-color: #151525 !important;
}

.btn-optimize:active {
  transform: scale(0.97);
}

.btn-reoptimize {
  background: transparent !important;
  border-color: #1a1a2e !important;
  color: #1a1a2e !important;
  border-radius: 8px !important;
  padding: 6px 16px !important;
  font-size: 14px !important;
  transition: all 0.1s ease;
}

.btn-reoptimize:hover {
  background: rgba(26, 26, 46, 0.05) !important;
}

.btn-reoptimize:active {
  transform: scale(0.97);
}

.btn-history {
  background: transparent !important;
  border-color: #1a1a2e !important;
  color: #1a1a2e !important;
  border-radius: 8px !important;
  padding: 6px 16px !important;
  font-size: 14px !important;
  transition: all 0.1s ease;
}

.btn-history:hover {
  background: rgba(26, 26, 46, 0.05) !important;
}

.btn-history:active {
  transform: scale(0.97);
}

.btn-export {
  background: transparent !important;
  border-color: #1a1a2e !important;
  color: #1a1a2e !important;
  border-radius: 8px !important;
  padding: 6px 16px !important;
  font-size: 14px !important;
  transition: all 0.1s ease;
}

.btn-export:hover {
  background: rgba(26, 26, 46, 0.05) !important;
}

.btn-export:active {
  transform: scale(0.97);
}

.dropdown-arrow {
  font-size: 12px;
  margin-left: 4px;
}

.btn-primary-ink {
  background: #1a1a2e !important;
  border-color: #1a1a2e !important;
  color: #fff !important;
  border-radius: 8px !important;
  transition: transform 0.1s ease;
}

.btn-primary-ink:hover {
  background: #151525 !important;
}

.btn-primary-ink:active {
  transform: scale(0.97);
}

.btn-outline-ink {
  background: transparent !important;
  border-color: #1a1a2e !important;
  color: #1a1a2e !important;
  border-radius: 8px !important;
  transition: all 0.1s ease;
}

.btn-outline-ink:hover {
  background: rgba(26, 26, 46, 0.05) !important;
}

.btn-outline-ink:active {
  transform: scale(0.97);
}

/* ========== 导出菜单 ========== */
.export-menu::deep .el-dropdown-menu__item {
  color: #1a1a2e;
}

/* ========== 优化弹窗 ========== */
.optimize-dialog::deep .el-dialog {
  border-radius: 16px;
  overflow: hidden;
}

.optimize-dialog::deep .el-dialog__header {
  border-bottom: 2px solid #1a1a2e;
  background: #fff;
}

.optimize-dialog::deep .el-dialog__title {
  font-family: "Noto Serif SC", Georgia, serif;
  color: #1a1a2e;
  font-size: 18px;
}

.optimize-dialog::deep .el-input__wrapper {
  background: #f7f8fa;
  border-radius: 8px;
}

.optimize-dialog::deep .el-input__wrapper.is-focus {
  border-color: #1a1a2e;
  box-shadow: 0 0 0 3px rgba(26, 26, 46, 0.06);
}

/* ========== 优化全屏遮罩 ========== */
.optimize-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(26, 26, 46, 0.6);
  backdrop-filter: blur(4px);
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
  animation: modalEnter 300ms cubic-bezier(0.4, 0, 0.2, 1) both;
}

@keyframes modalEnter {
  from {
    opacity: 0;
    transform: scale(0.95);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
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
  border: 2px solid #1a1a2e;
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
  color: #1a1a2e;
}

.optimize-title {
  font-size: 20px;
  color: #1a1a2e;
  margin-bottom: 12px;
  font-family: "Noto Serif SC", Georgia, serif;
}

.optimize-desc { color: #666; margin-bottom: 24px; }

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
  background: #1a1a2e;
  border-radius: 3px;
  transition: width 0.3s ease;
}

.optimize-time { font-size: 12px; color: #999; }

/* ========== 过渡动画 ========== */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* ========== 响应式 ========== */
@media (max-width: 1024px) {
  .compare-layout {
    flex-direction: column;
    gap: 16px;
  }

  .compare-panel {
    max-width: 100%;
  }

  .compare-divider {
    flex-direction: row;
    height: auto;
    padding: 8px 0;
  }

  .divider-line {
    flex: 1;
    height: 1px;
    width: auto;
  }

  .divider-label {
    padding: 0 16px;
  }

  .paper {
    width: 100% !important;
  }
}

@media (max-width: 768px) {
  .sub-header {
    padding: 0 16px;
    flex-wrap: wrap;
    gap: 8px;
    height: auto;
    padding: 8px 16px;
  }

  .toolbar {
    padding: 0 16px;
    margin-top: 80px;
  }

  .btn-optimize,
  .btn-reoptimize,
  .btn-history,
  .btn-export {
    padding: 4px 12px !important;
    font-size: 13px !important;
  }

  .avatar-switch {
    padding: 0 8px;
  }

  .preview-area {
    padding: 16px;
  }

  /* 小屏下单列布局纸张自适应 */
  .single-paper {
    width: 100% !important;
  }
}
</style>
