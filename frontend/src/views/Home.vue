<template>
  <div class="home-container">
    <!-- <div class="sub-header">
      <div class="sub-header-content">
        <h1 class="page-title">我的简历</h1>
        <el-button type="primary" class="upload-btn" @click="scrollToUpload">
          <el-icon><UploadFilled /></el-icon>
          上传简历
        </el-button>
      </div>
    </div> -->

    <div class="content-wrapper">
      <div class="welcome-card">
        <div class="welcome-text">
          <h2>欢迎回来</h2>
          <p class="welcome-subtitle">开始优化你的简历吧</p>
        </div>
        <div class="stats-row">
          <div class="stat-item">
            <span class="stat-number">{{ resumeCount }}</span>
            <span class="stat-label">简历数</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <span class="stat-number">{{ optimizeCount }}</span>
            <span class="stat-label">优化次数</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <span class="stat-number">0</span>
            <span class="stat-label">已用额度</span>
          </div>
        </div>
      </div>

      <div class="resume-section">
        <div v-if="pageLoading" class="skeleton-grid">
          <div v-for="i in 3" :key="i" class="skeleton-card">
            <el-skeleton :rows="4" animated />
          </div>
        </div>

        <div v-else-if="resumeList.length === 0" class="empty-state">
          <div class="empty-icon">
            <svg viewBox="0 0 120 80" class="scroll-svg">
              <rect x="10" y="10" width="100" height="60" rx="4" fill="none" stroke="currentColor" stroke-width="1.5"/>
              <line x1="10" y1="25" x2="110" y2="25" stroke="currentColor" stroke-width="1" stroke-dasharray="4,2"/>
              <line x1="10" y1="35" x2="110" y2="35" stroke="currentColor" stroke-width="1" stroke-dasharray="4,2"/>
              <line x1="10" y1="45" x2="110" y2="45" stroke="currentColor" stroke-width="1" stroke-dasharray="4,2"/>
              <line x1="10" y1="55" x2="110" y2="55" stroke="currentColor" stroke-width="1" stroke-dasharray="4,2"/>
              <path d="M10 15 Q5 15 5 20" fill="none" stroke="currentColor" stroke-width="1.5"/>
              <path d="M110 15 Q115 15 115 20" fill="none" stroke="currentColor" stroke-width="1.5"/>
              <path d="M10 65 Q5 65 5 60" fill="none" stroke="currentColor" stroke-width="1.5"/>
              <path d="M110 65 Q115 65 115 60" fill="none" stroke="currentColor" stroke-width="1.5"/>
            </svg>
          </div>
          <p class="empty-text">还没有简历，上传第一份吧</p>
          <el-button type="primary" class="empty-btn" @click="scrollToUpload">上传简历</el-button>
        </div>

        <div v-else class="resume-grid">
          <div class="upload-card" @click="scrollToUpload">
            <div class="upload-icon">+</div>
            <span class="upload-text">上传简历</span>
          </div>
          <div 
            v-for="(resume, index) in resumeList" 
            :key="resume.id" 
            class="resume-card"
            :style="{ animationDelay: index * 80 + 'ms' }"
          >
            <div class="card-left-bar"></div>
            <div class="card-content">
              <div class="card-header">
                <div class="file-icon-wrap">
                  <span class="file-icon-text">📄</span>
                </div>
                <h4 class="file-name">{{ resume.displayName || resume.fileName }}</h4>
              </div>
              <div class="time-row">
                <span class="upload-time">{{ formatDate(resume.createdAt) }}</span>
                <el-tag v-if="resume.optimizedStructuredData" class="optimize-tag" size="small">已优化</el-tag>
                <el-tag v-else class="optimize-tag pending" size="small">待优化</el-tag>
              </div>
              <div class="card-actions">
                <el-button text size="small" class="action-btn preview" @click="goPreview(resume.id)">预览</el-button>
                <el-button text size="small" class="action-btn optimize">优化</el-button>
                <el-popconfirm title="确定删除这份简历？" @confirm="handleDelete(resume.id)">
                  <template #reference>
                    <el-button text size="small" class="action-btn delete">删除</el-button>
                  </template>
                </el-popconfirm>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div ref="uploadAreaRef" class="upload-section">
      <el-card class="upload-card-form" shadow="hover">
        <template #header>
          <span>上传简历</span>
        </template>
        <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :on-change="handleFileChange"
            :limit="1"
            accept=".pdf,.docx"
            drag
            class="upload-drop-area"
        >
          <div class="upload-hint">
            <el-icon class="upload-icon-lg"><UploadFilled /></el-icon>
            <p>拖拽文件到此处，或点击上传</p>
            <span class="upload-tip">支持 PDF、DOCX 格式，最大 5MB</span>
          </div>
        </el-upload>
        <el-button type="primary" @click="handleUpload" :loading="uploading" class="submit-upload-btn" :disabled="!selectedFile">
          {{ uploading ? '上传中...' : '确认上传' }}
        </el-button>
      </el-card>
    </div>

    <transition name="fade">
      <div v-if="uploading" class="upload-overlay">
        <div class="upload-modal">
          <div class="upload-animation">
            <div class="upload-spinner"></div>
          </div>
          <h3 class="upload-title">正在上传简历</h3>
          <p class="upload-desc">{{ uploadTip }}</p>
          <div class="progress-bar">
            <div class="progress-fill" :style="{ width: progressWidth }"></div>
          </div>
        </div>
      </div>
    </transition>

    <el-dialog v-model="renameDialogVisible" title="重命名简历" width="400px">
      <el-input v-model="newDisplayName" placeholder="请输入新名称" maxlength="50" show-word-limit />
      <template #footer>
        <el-button @click="renameDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleRename" :loading="renaming">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import {
  getResumeList,
  uploadResume,
  deleteResume,
  batchDeleteResume,
  renameResume
} from '@/api/resume'
import { getUserProfile } from '@/api/user'

const router = useRouter()

const userName = ref(localStorage.getItem('username') || '用户')
const resumeList = ref([])
const pageLoading = ref(false)
const uploading = ref(false)
const selectedFile = ref(null)
const uploadRef = ref(null)
const uploadAreaRef = ref(null)

const renameDialogVisible = ref(false)
const newDisplayName = ref('')
const currentResume = ref(null)
const renaming = ref(false)

const uploadTip = ref('正在解析简历内容...')
const progressWidth = ref('0%')
const uploadTips = [
  '正在解析简历内容...',
  '正在提取关键信息...',
  '正在结构化数据...',
  '正在生成预览...'
]
let uploadTipInterval = null
let uploadProgressInterval = null

const resumeCount = computed(() => resumeList.value.length)
const optimizeCount = computed(() => resumeList.value.filter(r => r.optimizedStructuredData).length)

onMounted(() => {
  loadResumeList()
  loadUserProfile()
})

const loadUserProfile = async () => {
  try {
    const res = await getUserProfile()
    if (res.code === 200 && res.data) {
      if (res.data.username) {
        userName.value = res.data.username
        localStorage.setItem('username', res.data.username)
      }
    }
  } catch (error) {
    console.error('获取用户信息失败', error)
  }
}

const loadResumeList = async () => {
  pageLoading.value = true
  try {
    const res = await getResumeList()
    resumeList.value = res.data || res || []
  } catch (error) {
    ElMessage.error('加载简历列表失败')
  } finally {
    pageLoading.value = false
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

const handleFileChange = (file) => {
  selectedFile.value = file.raw
}

const handleUpload = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择文件')
    return
  }

  uploading.value = true
  startUploadAnimation()

  try {
    const res = await uploadResume(selectedFile.value)
    if (res.code === 200) {
      completeProgress()
      await new Promise(resolve => setTimeout(resolve, 300))
      ElMessage.success('上传成功')
      selectedFile.value = null
      uploadRef.value?.clearFiles()
      await loadResumeList()
    } else {
      ElMessage.error(res.message || '上传失败')
    }
  } catch (error) {
    ElMessage.error('上传失败')
  } finally {
    stopUploadAnimation()
    uploading.value = false
  }
}

const startUploadAnimation = () => {
  let tipIndex = 0
  uploadTipInterval = setInterval(() => {
    tipIndex = (tipIndex + 1) % uploadTips.length
    uploadTip.value = uploadTips[tipIndex]
  }, 2000)

  let progress = 0
  uploadProgressInterval = setInterval(() => {
    progress += Math.random() * 8
    if (progress >= 90) progress = 90
    progressWidth.value = progress + '%'
  }, 500)
}

const stopUploadAnimation = () => {
  if (uploadTipInterval) clearInterval(uploadTipInterval)
  if (uploadProgressInterval) clearInterval(uploadProgressInterval)
}

const completeProgress = () => {
  stopUploadAnimation()
  progressWidth.value = '100%'
}

const goPreview = (id) => {
  router.push(`/preview/${id}`)
}

const openRenameDialog = (resume) => {
  currentResume.value = resume
  newDisplayName.value = resume.displayName || resume.fileName?.replace(/\.[^.]+$/, '') || ''
  renameDialogVisible.value = true
}

const handleRename = async () => {
  if (!newDisplayName.value.trim()) {
    ElMessage.warning('名称不能为空')
    return
  }

  renaming.value = true
  try {
    const res = await renameResume(currentResume.value.id, newDisplayName.value.trim())
    if (res.code === 200) {
      ElMessage.success('重命名成功')
      renameDialogVisible.value = false
      await loadResumeList()
    } else {
      ElMessage.error(res.message || '重命名失败')
    }
  } catch (error) {
    ElMessage.error('重命名失败')
  } finally {
    renaming.value = false
  }
}

const handleDelete = async (id) => {
  try {
    const res = await deleteResume(id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      await loadResumeList()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定删除选中的简历吗？`, '提示', {
      type: 'warning'
    })
    const res = await batchDeleteResume([])
    if (res.code === 200) {
      ElMessage.success('删除成功')
      await loadResumeList()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const scrollToUpload = () => {
  uploadAreaRef.value?.scrollIntoView({ behavior: 'smooth' })
}
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  background: #f5f5f5;
}

.sub-header {
  position: sticky;
  /* top: 60px; */
  z-index: 100;
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
}

.sub-header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 32px;
  height: 48px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-title {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
  color: var(--ink-primary);
}

.upload-btn {
  background: var(--ink-primary) !important;
  border-color: var(--ink-primary) !important;
  border-radius: 8px;
  padding: 6px 16px;
}

.content-wrapper {
  max-width: 1200px;
  margin: 0 auto;
  padding: 32px;
}

.welcome-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.welcome-text h2 {
  margin: 0;
  font-size: 24px;
  font-family: var(--ink-font-serif);
  color: var(--ink-primary);
}

.welcome-subtitle {
  margin: 8px 0 0 0;
  font-size: 14px;
  color: #999;
}

.stats-row {
  display: flex;
  align-items: center;
  gap: 32px;
}

.stat-item {
  text-align: center;
}

.stat-number {
  display: block;
  font-size: 24px;
  font-family: var(--ink-font-serif);
  color: var(--ink-primary);
}

.stat-label {
  display: block;
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.stat-divider {
  width: 1px;
  height: 40px;
  background: #e8e8e8;
}

.resume-section {
  margin-bottom: 32px;
}

.resume-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

@media (max-width: 992px) {
  .resume-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .resume-grid {
    grid-template-columns: 1fr;
  }
}

.resume-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  position: relative;
  animation: cardEnter 300ms var(--ink-ease) both;
}

@keyframes cardEnter {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.card-left-bar {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background: var(--ink-primary);
  transition: width 0.2s ease;
}

.resume-card:hover .card-left-bar {
  width: 4px;
}

.card-content {
  padding: 20px 24px;
  margin-left: 3px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.file-icon-wrap {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f7f8fa;
  border-radius: 8px;
}

.file-icon-text {
  font-size: 18px;
}

.file-name {
  flex: 1;
  margin: 0;
  font-size: 16px;
  color: var(--ink-primary);
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.time-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.upload-time {
  font-size: 13px;
  color: #999;
}

.optimize-tag {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
  background: var(--ink-primary);
  color: #fff;
  border: none;
}

.optimize-tag.pending {
  background: #f0f0f0;
  color: #666;
}

.card-actions {
  display: flex;
  gap: 12px;
}

.action-btn {
  padding: 4px 0;
  font-size: 13px;
}

.action-btn.preview {
  color: var(--ink-primary);
}

.action-btn.optimize {
  color: var(--ink-primary);
}

.action-btn.delete {
  color: #999;
}

.action-btn.delete:hover {
  color: #666;
}

.resume-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(26, 26, 46, 0.08);
  transition: all 0.2s ease;
}

.upload-card {
  background: #fff;
  border: 2px dashed #ccc;
  border-radius: 12px;
  padding: 32px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
}

.upload-card:hover {
  border-color: var(--ink-primary);
  background: #fafafa;
}

.upload-icon {
  font-size: 32px;
  color: #ccc;
  margin-bottom: 8px;
  transition: color 0.2s ease;
}

.upload-card:hover .upload-icon {
  color: var(--ink-primary);
}

.upload-text {
  font-size: 14px;
  color: #999;
}

.empty-state {
  text-align: center;
  padding: 80px 20px;
}

.empty-icon {
  margin-bottom: 16px;
  color: #999;
}

.scroll-svg {
  width: 120px;
  height: 80px;
}

.empty-text {
  font-family: var(--ink-font-serif);
  color: #999;
  font-size: 16px;
  margin-bottom: 20px;
}

.empty-btn {
  background: var(--ink-primary) !important;
  border-color: var(--ink-primary) !important;
  border-radius: 8px;
}

.skeleton-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

@media (max-width: 992px) {
  .skeleton-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .skeleton-grid {
    grid-template-columns: 1fr;
  }
}

.skeleton-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
}

.upload-section {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 32px 32px;
}

.upload-card-form {
  background: #fff;
  border-radius: 12px;
  border: none;
}

.upload-drop-area {
  border: 2px dashed #e8e8e8;
  border-radius: 12px;
  padding: 48px;
  margin-bottom: 20px;
}

.upload-hint {
  text-align: center;
}

.upload-icon-lg {
  font-size: 40px;
  color: #999;
  margin-bottom: 12px;
}

.upload-hint p {
  margin: 0 0 8px 0;
  font-size: 15px;
  color: #666;
}

.upload-tip {
  font-size: 13px;
  color: #999;
}

.submit-upload-btn {
  width: 100%;
  height: 44px;
  background: var(--ink-primary) !important;
  border-color: var(--ink-primary) !important;
  border-radius: 8px;
}

.upload-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.upload-modal {
  background: #fff;
  border-radius: 12px;
  padding: 40px 48px;
  text-align: center;
  min-width: 320px;
}

.upload-animation {
  margin-bottom: 20px;
}

.upload-spinner {
  width: 48px;
  height: 48px;
  border: 4px solid #f0f0f0;
  border-top-color: var(--ink-primary);
  border-radius: 50%;
  margin: 0 auto;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.upload-title {
  margin: 0 0 8px 0;
  font-size: 18px;
  color: var(--ink-primary);
}

.upload-desc {
  color: #999;
  margin: 0 0 20px 0;
  font-size: 14px;
}

.progress-bar {
  height: 6px;
  background: #f0f0f0;
  border-radius: 3px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: var(--ink-primary);
  border-radius: 3px;
  transition: width 0.3s ease;
}

.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s;
}

.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
</style>