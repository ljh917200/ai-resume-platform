<template>
  <div class="home-container">
    <!-- 顶部导航栏 -->
    <el-header class="top-nav">
      <div class="logo">
        <el-icon class="logo-icon"><i class="el-icon-s-operation"></i></el-icon>
        <span>AI简历优化</span>
      </div>
      <el-dropdown>
        <!-- ★ 改造：有头像显示头像，没头像显示首字母 -->
        <div class="user-avatar">
          <el-avatar v-if="userAvatarUrl" :size="40" :src="userAvatarUrl" />
          <el-avatar v-else :size="40">
            {{ userName.charAt(0).toUpperCase() }}
          </el-avatar>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="goProfile">个人中心</el-dropdown-item>
            <el-dropdown-item @click="logout" divided>退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </el-header>

    <el-main>
      <!-- 欢迎区域 -->
      <div class="welcome-section">
        <div class="welcome-content">
          <h2>欢迎回来，{{ userName }}</h2>
          <p class="welcome-subtitle">开始优化你的简历吧</p>
        </div>
        <div class="stats-container">
          <div class="stat-card">
            <div class="stat-number">{{ resumeCount }}</div>
            <div class="stat-label">已上传简历</div>
          </div>
          <div class="stat-card">
            <div class="stat-number">{{ optimizeCount }}</div>
            <div class="stat-label">已优化次数</div>
          </div>
        </div>
      </div>

      <!-- 快捷操作区 -->
      <div class="quick-actions">
        <div class="action-card primary">
          <div class="action-icon">
            <el-icon><i class="el-icon-upload"></i></el-icon>
          </div>
          <h3>上传简历</h3>
          <p>上传PDF或Word格式简历</p>
          <el-upload
              ref="uploadRef"
              :auto-upload="false"
              :on-change="handleFileChange"
              :limit="1"
              accept=".pdf,.docx"
              drag
              class="upload-area"
          >
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div class="el-upload__text">
              拖拽文件到此处，或<em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">支持 PDF、DOCX 格式，最大 5MB</div>
            </template>
          </el-upload>
          <el-button type="primary" @click="handleUpload" :loading="uploading" class="action-button">
            上传简历
          </el-button>
        </div>
      </div>

      <!-- 我的简历列表 -->
      <div class="resume-section">
        <div class="section-header">
          <h3 class="section-title">我的简历</h3>
          <!-- 批量操作按钮（有选中时显示） -->
          <div v-if="selectedIds.length > 0" class="batch-actions">
            <span class="selected-count">已选择 {{ selectedIds.length }} 份</span>
            <el-button type="danger" size="small" @click="handleBatchDelete">批量删除</el-button>
            <el-button size="small" @click="clearSelection">取消选择</el-button>
          </div>
        </div>

        <!-- 骨架屏加载状态 -->
        <div v-if="pageLoading" class="skeleton-grid">
          <div v-for="i in 3" :key="i" class="skeleton-card">
            <el-skeleton :rows="4" animated />
          </div>
        </div>

        <!-- 空状态 -->
        <div v-else-if="resumeList.length === 0" class="empty-state">
          <div class="empty-icon">📄</div>
          <p class="empty-text">暂无简历，点击上传开始吧</p>
          <el-button type="primary" @click="scrollToUpload">上传简历</el-button>
        </div>

        <!-- 简历卡片列表 -->
        <div v-else class="resume-grid">
          <div v-for="resume in resumeList" :key="resume.id" class="resume-card" :class="{ 'selected': selectedIds.includes(resume.id) }">
            <!-- 多选框 -->
            <div class="select-checkbox">
              <el-checkbox v-model="selectedIds" :label="resume.id" @change="handleSelectChange">
                &nbsp;
              </el-checkbox>
            </div>

            <div class="resume-header">
              <div class="file-icon" :class="{ 'pdf': resume.fileFormat === 'pdf', 'docx': resume.fileFormat === 'docx' }">
                <el-icon><i class="el-icon-document"></i></el-icon>
              </div>
              <div class="resume-info">
                <h4 class="file-name">{{ resume.displayName || resume.fileName }}</h4>
                <div v-if="resume.displayName" class="original-name">原文件：{{ resume.fileName }}</div>
                <div v-if="resume.targetRole" class="target-role-tag">{{ resume.targetRole }}</div>
              </div>
            </div>

            <!-- 状态标签 -->
            <div class="resume-status">
              <el-tag v-if="resume.optimizedStructuredData" type="success" size="small">已优化</el-tag>
              <el-tag v-else type="info" size="small">待优化</el-tag>
            </div>

            <div class="resume-footer">
              <div class="upload-time">{{ formatDate(resume.createdAt) }}</div>
              <div class="resume-actions">
                <!-- 核心入口：查看简历 -->
                <el-button type="primary" size="small" @click="goPreview(resume.id)">
                  查看简历
                </el-button>
                <!-- 重命名按钮 -->
                <el-button type="warning" size="small" plain @click="openRenameDialog(resume)">重命名</el-button>
                <el-button type="danger" size="small" @click="handleDelete(resume.id)">删除</el-button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 上传全屏遮罩 -->
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

    </el-main>

    <!-- 重命名弹窗 -->
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
/**
 * 首页（v1.8.0）
 *
 * 新增：导航栏显示用户头像
 * - 有头像时显示头像图片
 * - 没头像时显示用户名首字母
 */
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
// ★ 新增：导入获取用户信息的接口
import { getUserProfile } from '@/api/user'

const router = useRouter()

// ========== 响应式数据 ==========
const userName = ref(localStorage.getItem('username') || '用户')
// ★ 新增：用户头像URL
const userAvatarUrl = ref('')
const resumeList = ref([])
const selectedIds = ref([])
const pageLoading = ref(false)
const uploading = ref(false)
const selectedFile = ref(null)
const uploadRef = ref(null)

// 重命名相关
const renameDialogVisible = ref(false)
const newDisplayName = ref('')
const currentResume = ref(null)
const renaming = ref(false)

// 上传动画相关
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

// ========== 计算属性 ==========
const resumeCount = computed(() => resumeList.value.length)
const optimizeCount = computed(() => resumeList.value.filter(r => r.optimizedStructuredData).length)

// ========== 生命周期 ==========
onMounted(() => {
  loadResumeList()
  // ★ 加载用户头像
  loadUserProfile()
})

// ========== 方法 ==========

/**
 * ★ 新增：加载用户信息（获取头像）
 */
const loadUserProfile = async () => {
  try {
    const res = await getUserProfile()
    if (res.code === 200 && res.data) {
      userAvatarUrl.value = res.data.avatarUrl || ''
      // 同步更新 localStorage 中的用户名
      if (res.data.username) {
        userName.value = res.data.username
        localStorage.setItem('username', res.data.username)
      }
    }
  } catch (error) {
    // 获取失败不影响主流程，头像显示首字母就行
    console.error('获取用户信息失败', error)
  }
}

/**
 * 加载简历列表
 */
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

/**
 * 格式化日期
 */
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

/**
 * 选择文件
 */
const handleFileChange = (file) => {
  selectedFile.value = file.raw
}

/**
 * 上传简历
 */
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

/**
 * 跳转到预览页面（核心入口）
 */
const goPreview = (id) => {
  router.push(`/preview/${id}`)
}

/**
 * 打开重命名弹窗
 */
const openRenameDialog = (resume) => {
  currentResume.value = resume
  newDisplayName.value = resume.displayName || resume.fileName?.replace(/\.[^.]+$/, '') || ''
  renameDialogVisible.value = true
}

/**
 * 重命名简历
 */
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

/**
 * 删除简历
 */
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除这份简历吗？', '提示', {
      type: 'warning'
    })
    const res = await deleteResume(id)
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

/**
 * 批量删除
 */
const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定删除选中的 ${selectedIds.value.length} 份简历吗？`, '提示', {
      type: 'warning'
    })
    const res = await batchDeleteResume(selectedIds.value)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      selectedIds.value = []
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

const handleSelectChange = () => {
}
const clearSelection = () => {
  selectedIds.value = []
}
const scrollToUpload = () => {
  document.querySelector('.upload-area')?.scrollIntoView({behavior: 'smooth'})
}
const goProfile = () => router.push('/profile')
const logout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  router.push('/login')
}

</script>

<style scoped>
/* ========== 整体布局 ========== */
.home-container {
  min-height: 100vh;
  background: #f5f7fa;
}

/* ========== 顶部导航栏 ========== */
.top-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.logo-icon {
  font-size: 20px;
  color: #409eff;
}

.user-avatar {
  cursor: pointer;
}

/* ★ 头像图片适配 */
.user-avatar .el-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* ========== 欢迎区域 ========== */
.welcome-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30px;
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  border-radius: 12px;
  margin-bottom: 24px;
  color: #fff;
}

.welcome-content h2 {
  margin: 0 0 8px 0;
  font-size: 24px;
}

.welcome-subtitle {
  margin: 0;
  opacity: 0.9;
}

.stats-container {
  display: flex;
  gap: 24px;
}

.stat-card {
  text-align: center;
  background: rgba(255, 255, 255, 0.2);
  padding: 16px 24px;
  border-radius: 8px;
}

.stat-number {
  font-size: 28px;
  font-weight: bold;
}

.stat-label {
  font-size: 12px;
  opacity: 0.9;
  margin-top: 4px;
}

/* ========== 快捷操作区 ========== */
.quick-actions {
  display: flex;
  gap: 24px;
  margin-bottom: 24px;
}

.action-card {
  flex: 1;
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.action-card h3 {
  margin: 16px 0 8px 0;
  font-size: 18px;
}

.action-card p {
  color: #666;
  margin: 0 0 16px 0;
}

.upload-area {
  margin-bottom: 16px;
}

.action-button {
  width: 100%;
}

/* ========== 简历列表 ========== */
.resume-section {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-title {
  margin: 0;
  font-size: 18px;
  color: #333;
}

.batch-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.selected-count {
  color: #666;
  font-size: 14px;
}

/* ========== 简历卡片 ========== */
.resume-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.resume-card {
  background: #fafafa;
  border-radius: 8px;
  padding: 16px;
  border: 2px solid transparent;
  transition: all 0.3s;
  position: relative;
}

.resume-card.selected {
  border-color: #409eff;
  background: #ecf5ff;
}

.resume-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.select-checkbox {
  position: absolute;
  top: 12px;
  right: 12px;
}

.resume-header {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
}

.file-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  font-size: 18px;
}

.file-icon.pdf {
  background: #fef0f0;
  color: #f56c6c;
}

.file-icon.docx {
  background: #ecf5ff;
  color: #409eff;
}

.resume-info {
  flex: 1;
  min-width: 0;
}

.file-name {
  margin: 0;
  font-size: 14px;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.original-name {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.target-role-tag {
  font-size: 12px;
  color: #409eff;
  margin-top: 4px;
}

.resume-status {
  margin-bottom: 12px;
}

.resume-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.upload-time {
  font-size: 12px;
  color: #999;
}

.resume-actions {
  display: flex;
  gap: 8px;
}

/* ========== 空状态 ========== */
.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.empty-icon {
  font-size: 60px;
  margin-bottom: 16px;
}

.empty-text {
  color: #666;
  margin-bottom: 20px;
}

/* ========== 骨架屏 ========== */
.skeleton-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.skeleton-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
}

/* ========== 上传遮罩 ========== */
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
  border-radius: 16px;
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
  border: 4px solid #e4e7ed;
  border-top-color: #409eff;
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
  color: #333;
}

.upload-desc {
  color: #999;
  margin: 0 0 20px 0;
  font-size: 14px;
}

.progress-bar {
  height: 6px;
  background: #e4e7ed;
  border-radius: 3px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #409eff, #66b1ff);
  border-radius: 3px;
  transition: width 0.3s ease;
}

/* ========== 过渡动画 ========== */
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s;
}

.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
</style>