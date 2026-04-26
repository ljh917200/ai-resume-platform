<template>
  <div class="home-container">
    <!-- 顶部导航栏 -->
    <el-header class="top-nav">
      <div class="logo">
        <el-icon class="logo-icon"><i class="el-icon-s-operation"></i></el-icon>
        <span>AI简历优化</span>
      </div>
      <el-dropdown>
        <div class="user-avatar">
          <el-avatar :size="40">
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
          <div class="stat-card">
            <div class="stat-number">{{ optimizeQuota }}</div>
            <div class="stat-label">可用优化额度</div>
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
        <div class="action-card secondary">
          <div class="action-icon">
            <el-icon><i class="el-icon-edit"></i></el-icon>
          </div>
          <h3>文本优化</h3>
          <p>快速优化简历文本内容</p>
          <el-input
              v-model="quickText"
              type="textarea"
              :rows="3"
              placeholder="输入要优化的简历内容..."
              class="quick-input"
          />
          <el-input v-model="targetRole" placeholder="目标岗位（可选）" class="target-role-input" />
          <el-button type="primary" @click="handleQuickOptimize" :loading="optimizing" class="action-button">
            优化文本
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
                <el-icon v-if="resume.fileFormat === 'pdf'"><i class="el-icon-document"></i></el-icon>
                <el-icon v-else><i class="el-icon-document"></i></el-icon>
              </div>
              <div class="resume-info">
                <!-- 显示用户自定义名称，为空则显示文件名 -->
                <h4 class="file-name">{{ resume.displayName || resume.fileName }}</h4>
                <div v-if="resume.displayName" class="original-name">原文件：{{ resume.fileName }}</div>
                <div v-if="resume.targetRole" class="target-role-tag">{{ resume.targetRole }}</div>
              </div>
            </div>
            <div class="resume-footer">
              <div class="upload-time">{{ resume.createdAt }}</div>
              <div class="resume-actions">
                <el-button type="primary" size="small" plain @click="goDetail(resume.id)">详情</el-button>
                <el-button type="primary" size="small" @click="goOptimize(resume.id)">优化</el-button>
                <!-- 重命名按钮 -->
                <el-button type="warning" size="small" plain @click="openRenameDialog(resume)">重命名</el-button>
                <el-button type="danger" size="small" @click="handleDelete(resume.id)">删除</el-button>
              </div>
            </div>
          </div>
        </div>
      </div>


    </el-main>

    <!-- 优化结果弹窗 -->
    <el-dialog v-model="resultDialogVisible" title="优化结果" width="60%">
      <div class="result-container">
        <h4>原始内容：</h4>
        <div class="original-text">{{ resultData.originalText }}</div>
        <h4>优化结果：</h4>
        <div class="optimized-text">{{ resultData.optimizedText }}</div>
      </div>
      <template #footer>
        <el-button @click="copyResult">复制结果</el-button>
        <el-button type="primary" @click="resultDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 重命名弹窗 -->
    <el-dialog v-model="renameDialogVisible" title="重命名简历" width="400px">
      <el-input
          v-model="newDisplayName"
          placeholder="请输入新的简历名称"
          maxlength="50"
          show-word-limit
      />
      <template #footer>
        <el-button @click="renameDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleRename" :loading="renaming">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {ref, onMounted} from 'vue'
import {useRouter} from 'vue-router'
import {ElMessage, ElMessageBox} from 'element-plus'
import {UploadFilled} from '@element-plus/icons-vue'
import {uploadResume, getResumeList, deleteResume, optimizeResume, renameResume, batchDeleteResume} from '../api/resume'
import {optimizeText} from '../api/ai'
import {getUserStatistics} from '../api/user'

const router = useRouter()
const uploadRef = ref()
const resumeList = ref([])
const selectedFile = ref(null)
const uploading = ref(false)
const optimizing = ref(false)
const quickText = ref('')
const targetRole = ref('')
const resultDialogVisible = ref(false)
const resultData = ref({
  originalText: '',
  optimizedText: ''
})

// 用户相关数据
const userName = ref('用户')
const resumeCount = ref(0)
const optimizeCount = ref(0)
const optimizeQuota = ref(10)

// ========== 新增：重命名相关 ==========
const renameDialogVisible = ref(false)
const newDisplayName = ref('')
const currentRenameId = ref(null)
const renaming = ref(false)

// ========== 新增：批量删除相关 ==========
const selectedIds = ref([])  // 选中的简历ID列表

// 页面加载状态
const pageLoading = ref(true)

// 获取简历列表
const fetchResumeList = async () => {
  pageLoading.value = true
  try {
    const res = await getResumeList()
    if (res.code === 200) {
      resumeList.value = res.data
      resumeCount.value = res.data.length
    }
  } catch (error) {
    console.error(error)
  } finally {
    pageLoading.value = false
  }
}




// 获取用户统计数据
const fetchStatistics = async () => {
  try {
    const res = await getUserStatistics()
    if (res.code === 200) {
      optimizeCount.value = res.data.optimizeCount
      optimizeQuota.value = 100 - res.data.quotaUsed
    }
  } catch (error) {
    console.error(error)
  }
}

// 跳转个人中心
const goProfile = () => {
  router.push('/profile')
}

onMounted(() => {
  fetchResumeList()
  fetchStatistics()
  // 获取用户名
  const user = localStorage.getItem('user')
  if (user) {
    try {
      const userData = JSON.parse(user)
      userName.value = userData.username || '用户'
    } catch (e) {
      console.error(e)
    }
  }
})

// 文件选择
const handleFileChange = (file) => {
  selectedFile.value = file.raw
}

// 上传简历
const handleUpload = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择文件')
    return
  }

  uploading.value = true
  try {
    const res = await uploadResume(selectedFile.value)
    if (res.code === 200) {
      ElMessage.success('上传成功')
      fetchResumeList()
      fetchStatistics()
      uploadRef.value.clearFiles()
      selectedFile.value = null
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    ElMessage.error('上传失败')
  } finally {
    uploading.value = false
  }
}

// 快速优化
const handleQuickOptimize = async () => {
  if (!quickText.value.trim()) {
    ElMessage.warning('请输入要优化的内容')
    return
  }

  optimizing.value = true
  try {
    const res = await optimizeText(quickText.value, targetRole.value)
    if (res.code === 200) {
      resultData.value = res.data
      resultDialogVisible.value = true
      fetchStatistics()
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    ElMessage.error('优化失败')
  } finally {
    optimizing.value = false
  }
}

// 跳转优化页
const goOptimize = (id) => {
  router.push(`/optimize?id=${id}`)
}

// 删除简历
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除该简历？', '提示', {type: 'warning'})
    const res = await deleteResume(id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchResumeList()
      fetchStatistics()
      // 从选中列表中移除
      selectedIds.value = selectedIds.value.filter(item => item !== id)
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    // 取消删除
  }
}

// 复制结果
const copyResult = () => {
  navigator.clipboard.writeText(resultData.value.optimizedText)
  ElMessage.success('已复制到剪贴板')
}

// 退出登录
const logout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  router.push('/login')
}

// 跳转简历详情页
const goDetail = (id) => {
  router.push(`/resume/${id}`)
}

// 滚动到上传区域并触发文件选择
const scrollToUpload = () => {
  const uploadInput = document.querySelector('.upload-area input[type="file"]')
  if (uploadInput) {
    uploadInput.click()
  }
}

// ========== 新增：重命名功能 ==========
/**
 * 打开重命名弹窗
 * @param {Object} resume - 简历对象
 */
const openRenameDialog = (resume) => {
  currentRenameId.value = resume.id
  // 如果有自定义名称则显示，否则显示原文件名
  newDisplayName.value = resume.displayName || resume.fileName.replace(/\.[^/.]+$/, '')
  renameDialogVisible.value = true
}

/**
 * 执行重命名
 */
const handleRename = async () => {
  if (!newDisplayName.value.trim()) {
    ElMessage.warning('简历名称不能为空')
    return
  }

  renaming.value = true
  try {
    const res = await renameResume(currentRenameId.value, newDisplayName.value.trim())
    if (res.code === 200) {
      ElMessage.success('重命名成功')
      renameDialogVisible.value = false
      await fetchResumeList()  // 刷新列表
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    ElMessage.error('重命名失败')
  } finally {
    renaming.value = false
  }
}

// ========== 新增：批量删除功能 ==========
/**
 * 选择变化处理
 */
const handleSelectChange = () => {
  // checkbox自动处理了selectedIds，这里可以做一些额外操作
}

/**
 * 清除选择
 */
const clearSelection = () => {
  selectedIds.value = []
}

/**
 * 批量删除
 */
const handleBatchDelete = async () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请先选择要删除的简历')
    return
  }

  try {
    await ElMessageBox.confirm(
        `确定删除选中的 ${selectedIds.value.length} 份简历？`,
        '批量删除',
        {type: 'warning'}
    )

    const res = await batchDeleteResume(selectedIds.value)
    if (res.code === 200) {
      ElMessage.success(`成功删除 ${res.data.deletedCount} 份简历`)
      selectedIds.value = []  // 清空选择
      await fetchResumeList()  // 刷新列表
      await fetchStatistics()
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    // 取消删除
  }
}
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  background: #f5f5f5;
}

/* 顶部导航栏 */
.top-nav {
  background: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 30px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon {
  font-size: 24px;
  color: #667eea;
}

.logo span {
  font-size: 20px;
  font-weight: 700;
  color: #303133;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-avatar {
  cursor: pointer;
  transition: transform 0.3s ease;
}

.user-avatar:hover {
  transform: scale(1.05);
}

.user-actions {
  display: flex;
  gap: 5px;
}

/* 欢迎区域 */
.el-main {
  max-width: 1200px;
  margin: 0 auto;
  padding: 30px;
}

.welcome-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 40px;
  margin-bottom: 30px;
  color: white;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.welcome-content h2 {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 8px;
}

.welcome-subtitle {
  font-size: 18px;
  opacity: 0.9;
  margin-bottom: 30px;
}

.stats-container {
  display: flex;
  gap: 20px;
  margin-top: 20px;
}

.stat-card {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  padding: 20px;
  flex: 1;
  text-align: center;
  backdrop-filter: blur(10px);
  transition: transform 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-number {
  font-size: 32px;
  font-weight: 700;
}

.stat-label {
  font-size: 14px;
  opacity: 0.9;
  margin-top: 8px;
}

/* 快捷操作区 */
.quick-actions {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 30px;
  margin-bottom: 30px;
}

.action-card {
  background: #fff;
  border-radius: 16px;
  padding: 30px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.action-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.action-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
  font-size: 28px;
}

.primary .action-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.secondary .action-icon {
  background: #f0f2ff;
  color: #667eea;
}

.action-card h3 {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 8px;
  color: #303133;
}

.action-card p {
  color: #909399;
  margin-bottom: 20px;
}

.action-button {
  width: 100%;
  margin-top: 15px;
}

.upload-area {
  width: 100%;
}

.quick-input {
  margin-bottom: 15px;
}

.target-role-input {
  margin-bottom: 15px;
}

/* 简历列表区域 */
.resume-section {
  background: #fff;
  border-radius: 16px;
  padding: 30px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

/* 批量操作样式 */
.batch-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.selected-count {
  color: #909399;
  font-size: 14px;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 20px;
}

.empty-text {
  color: #909399;
  margin-bottom: 20px;
}

/* 简历卡片网格 */
.resume-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.resume-card {
  background: #f9fafb;
  border-radius: 12px;
  padding: 20px;
  border: 2px solid transparent;
  transition: all 0.3s ease;
  position: relative;
}

.resume-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.resume-card.selected {
  border-color: #409eff;
  background: #f0f7ff;
}

/* 多选框位置 */
.select-checkbox {
  position: absolute;
  top: 10px;
  right: 10px;
}

.resume-header {
  display: flex;
  gap: 15px;
  margin-bottom: 15px;
}

.file-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.file-icon.pdf {
  background: #fff1f0;
  color: #f56c6c;
}

.file-icon.docx {
  background: #e8f4ff;
  color: #409eff;
}

.resume-info {
  flex: 1;
  min-width: 0;
}

.file-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 5px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.original-name {
  font-size: 12px;
  color: #909399;
  margin-bottom: 5px;
}

.target-role-tag {
  display: inline-block;
  background: #f0f2ff;
  color: #667eea;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
}

.resume-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 15px;
  border-top: 1px solid #e4e7ed;
}

.upload-time {
  font-size: 12px;
  color: #909399;
}

.resume-actions {
  display: flex;
  gap: 5px;
}

/* 结果弹窗 */
.result-container {
  max-height: 400px;
  overflow-y: auto;
}

.result-container h4 {
  margin: 15px 0 10px;
  color: #303133;
}

.result-container h4:first-child {
  margin-top: 0;
}

.original-text {
  background: #f5f5f5;
  padding: 15px;
  border-radius: 8px;
  white-space: pre-wrap;
  color: #606266;
}

.optimized-text {
  background: #f0f7ff;
  padding: 15px;
  border-radius: 8px;
  white-space: pre-wrap;
  color: #303133;
}

/* 骨架屏样式 */
.skeleton-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.skeleton-card {
  background: #fff;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}
</style>