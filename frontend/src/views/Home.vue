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
        <h3 class="section-title">我的简历</h3>
        <div v-if="resumeList.length === 0" class="empty-state">
          <div class="empty-icon">📄</div>
          <p class="empty-text">暂无简历，点击上传开始吧</p>
          <el-button type="primary" @click="scrollToUpload">上传简历</el-button>
        </div>
        <div v-else class="resume-grid">
          <div v-for="resume in resumeList" :key="resume.id" class="resume-card">
            <div class="resume-header">
              <div class="file-icon" :class="{ 'pdf': resume.fileFormat === 'pdf', 'docx': resume.fileFormat === 'docx' }">
                <el-icon v-if="resume.fileFormat === 'pdf'"><i class="el-icon-document"></i></el-icon>
                <el-icon v-else><i class="el-icon-document"></i></el-icon>
              </div>
              <div class="resume-info">
                <h4 class="file-name">{{ resume.fileName }}</h4>
                <div v-if="resume.targetRole" class="target-role-tag">{{ resume.targetRole }}</div>
              </div>
            </div>
            <div class="resume-footer">
              <div class="upload-time">{{ resume.createdAt }}</div>
              <div class="resume-actions">
                <el-button type="primary" size="small" @click="goOptimize(resume.id)">优化</el-button>
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { uploadResume, getResumeList, deleteResume, optimizeResume } from '../api/resume'
import { optimizeText } from '../api/ai'
import { getUserStatistics } from '../api/user'

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

// 获取简历列表
const fetchResumeList = async () => {
  try {
    const res = await getResumeList()
    if (res.code === 200) {
      resumeList.value = res.data
      resumeCount.value = res.data.length
    }
  } catch (error) {
    console.error(error)
  }
}

// 获取用户统计数据
const fetchStatistics = async () => {
  try {
    const res = await getUserStatistics()
    if (res.code === 200) {
      optimizeCount.value = res.data.optimizeCount
      optimizeQuota.value = 100 - res.data.quotaUsed  // 假设总额度100
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
    await ElMessageBox.confirm('确定删除该简历？', '提示', { type: 'warning' })
    const res = await deleteResume(id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchResumeList()
      fetchStatistics()
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

// 滚动到上传区域
const scrollToUpload = () => {
  const uploadSection = document.querySelector('.action-card.primary')
  if (uploadSection) {
    uploadSection.scrollIntoView({ behavior: 'smooth' })
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
  gap:10px;
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
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  transition: transform 0.3s ease;
}

.action-card:hover {
  transform: translateY(-5px);
}

.action-card.primary {
  border-top: 4px solid #667eea;
}

.action-card.secondary {
  border-top: 4px solid #764ba2;
}

.action-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  margin-bottom: 16px;
}

.action-card.primary .action-icon {
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
}

.action-card.secondary .action-icon {
  background: rgba(118, 75, 162, 0.1);
  color: #764ba2;
}

.action-card h3 {
  font-size: 20px;
  color: #303133;
  margin-bottom: 8px;
}

.action-card p {
  font-size: 14px;
  color: #909399;
  margin-bottom: 20px;
}

.upload-area {
  margin-bottom: 16px;
}

.quick-input {
  margin-bottom: 12px;
}

.target-role-input {
  margin-bottom: 16px;
}

.action-button {
  width: 100%;
}

/* 简历列表 */
.resume-section {
  background: #fff;
  border-radius: 16px;
  padding: 30px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.section-title {
  font-size: 20px;
  color: #303133;
  margin-bottom: 20px;
}

.empty-state {
  text-align: center;
  padding: 60px 0;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.empty-text {
  font-size: 16px;
  color: #909399;
  margin-bottom: 20px;
}

.resume-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.resume-card {
  background: #f9f9f9;
  border-radius: 12px;
  padding: 20px;
  transition: all 0.3s ease;
}

.resume-card:hover {
  background: #f0f0f0;
  transform: translateY(-3px);
}

.resume-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.file-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: #fff;
}

.file-icon.pdf {
  background: #ff4d4f;
}

.file-icon.docx {
  background: #1890ff;
}

.resume-info {
  flex: 1;
}

.file-name {
  font-size: 16px;
  color: #303133;
  margin-bottom: 4px;
}

.target-role-tag {
  display: inline-block;
  padding: 2px 8px;
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
  border-radius: 4px;
  font-size: 12px;
}

.resume-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.upload-time {
  font-size: 12px;
  color: #909399;
}

.resume-actions {
  display: flex;
  gap: 8px;
}

/* 优化结果弹窗 */
.result-container {
  padding: 20px;
}

.result-container h4 {
  margin-bottom: 12px;
  color: #303133;
}

.original-text,
.optimized-text {
  background: #f5f5f5;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 20px;
  line-height: 1.6;
  white-space: pre-wrap;
}

.optimized-text {
  background: #e6f7ff;
  border: 1px solid #91d5ff;
}
</style>