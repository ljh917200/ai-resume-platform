<template>
  <div class="profile-container">
    <div class="content-wrapper">
      <!-- 返回按钮 -->
      <div class="back-btn-container">
        <el-button class="back-btn" @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
      </div>
      <!-- 页面顶部：头像和基本信息 -->
      <div class="profile-header">
        <div class="avatar-container">
          <div class="avatar-wrapper" @click="toggleAvatarMenu">
            <el-avatar :size="80" :src="fullAvatarUrl || undefined" class="main-avatar">
              {{ profile.username ? profile.username.charAt(0).toUpperCase() : 'U' }}
            </el-avatar>
            <div class="avatar-overlay" v-if="profile.avatarUrl">
              <el-icon><Camera /></el-icon>
              <span>更换头像</span>
            </div>
          </div>
          <div class="avatar-dropdown" v-if="showAvatarMenu">
            <div class="dropdown-item" @click.stop="triggerUpload">
              <el-icon><UploadFilled /></el-icon>
              <span>上传头像</span>
            </div>
            <div class="dropdown-item" @click.stop="handlePreviewAvatar">
              <el-icon><Search /></el-icon>
              <span>查看大图</span>
            </div>
            <div class="dropdown-divider"></div>
            <div class="dropdown-item danger" @click.stop="handleDeleteAvatar">
              <el-icon><Delete /></el-icon>
              <span>删除头像</span>
            </div>
          </div>
        </div>
        <div class="user-info">
          <h1 class="username">{{ profile.username }}</h1>
          <p class="register-date">加入于 {{ formatDate(profile.createdAt) }}</p>
        </div>
      </div>

      <!-- 统计卡片 -->
      <div class="stats-card">
        <div 
            v-for="(stat, index) in stats" 
            :key="stat.label"
            class="stat-item"
            :style="{ '--stagger-delay': `${index * 100}ms` }"
        >
          <div class="stat-number" :data-target="stat.value">{{ animatedNumbers[index] || 0 }}</div>
          <div class="stat-label">{{ stat.label }}</div>
          <div v-if="index < stats.length - 1" class="stat-divider"></div>
        </div>
      </div>

      <!-- 个人信息区 -->
      <div class="info-card">
        <div class="card-header">
          <h2 class="card-title">基本信息</h2>
          <el-button 
              v-if="!isEditing" 
              class="edit-btn" 
              @click="startEdit"
          >编辑</el-button>
          <div v-else class="edit-actions">
            <el-button class="save-btn" @click="saveProfile" :loading="saving">保存</el-button>
            <el-button class="cancel-btn" @click="cancelEdit">取消</el-button>
          </div>
        </div>
        <div class="card-divider"></div>
        
        <div class="info-grid">
          <div class="info-row">
            <span class="info-label">用户ID</span>
            <span class="info-value">{{ profile.id }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">用户名</span>
            <template v-if="isEditing">
              <el-input 
                  v-model="editForm.username" 
                  class="edit-input"
                  placeholder="请输入用户名"
              />
            </template>
            <span v-else class="info-value">{{ profile.username }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">邮箱</span>
            <template v-if="isEditing">
              <el-input 
                  v-model="editForm.email" 
                  class="edit-input"
                  placeholder="请输入邮箱"
              />
            </template>
            <span v-else class="info-value">{{ profile.email || '-' }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">注册时间</span>
            <span class="info-value">{{ formatDate(profile.createdAt) }}</span>
          </div>
        </div>
      </div>

      <!-- 修改密码区 -->
      <div class="password-card">
        <div class="card-header">
          <div class="header-line"></div>
          <h2 class="card-title">修改密码</h2>
        </div>
        <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" class="password-form">
          <el-form-item label="旧密码" prop="oldPassword">
            <el-input 
                v-model="passwordForm.oldPassword" 
                type="password" 
                class="form-input"
                placeholder="请输入旧密码"
                show-password
            />
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input 
                v-model="passwordForm.newPassword" 
                type="password" 
                class="form-input"
                placeholder="请输入新密码（6-20个字符）"
                show-password
                @input="updatePasswordStrength"
            />
            <div class="password-strength" v-if="passwordForm.newPassword">
              <div class="strength-label">密码强度：{{ strengthText }}</div>
              <div class="strength-bar">
                <div class="strength-fill" :class="strengthClass" :style="{ width: strengthWidth }"></div>
              </div>
            </div>
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input 
                v-model="passwordForm.confirmPassword" 
                type="password" 
                class="form-input"
                placeholder="请再次输入新密码"
                show-password
            />
          </el-form-item>
          <el-form-item class="form-actions">
            <el-button class="btn-primary-ink" @click="handleUpdatePassword" :loading="passwordLoading">
              确认修改
            </el-button>
            <el-button class="btn-outline-ink" @click="resetPasswordForm">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>

    <!-- 隐藏的文件输入 -->
    <input 
        ref="avatarInputRef" 
        type="file" 
        accept="image/jpeg,image/png" 
        style="display: none"
        @change="handleAvatarChange"
    />

    <!-- 头像大图预览 -->
    <el-image-viewer 
        v-if="showPreview" 
        :url-list="[fullAvatarUrl]" 
        :z-index="9999"
        @close="showPreview = false"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Camera, UploadFilled, Search, Delete, ArrowLeft } from '@element-plus/icons-vue'
import {
  getUserProfile,
  updateUsername,
  updateEmail,
  updatePassword,
  getUserStatistics,
  uploadAvatar,
  deleteAvatar
} from '../api/user'

const router = useRouter()

// 返回上一页
const goBack = () => {
  router.back()
}

// 响应式数据
const profileFormRef = ref(null)
const passwordFormRef = ref(null)
const avatarInputRef = ref(null)

const profile = ref({
  id: '',
  username: '',
  email: '',
  avatarUrl: '',
  createdAt: ''
})

const editForm = reactive({
  username: '',
  email: ''
})

const stats = ref([
  { label: '简历数', value: 0 },
  { label: '优化次数', value: 0 },
  { label: '加入天数', value: 0 }
])

const animatedNumbers = ref([0, 0, 0])

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const isEditing = ref(false)
const saving = ref(false)
const passwordLoading = ref(false)
const avatarLoading = ref(false)
const showAvatarMenu = ref(false)
const showPreview = ref(false)
const passwordStrength = ref(0)

// 计算属性
const fullAvatarUrl = computed(() => {
  if (!profile.value.avatarUrl) return ''
  if (profile.value.avatarUrl.startsWith('http')) {
    return profile.value.avatarUrl
  }
  return `http://localhost:8080${profile.value.avatarUrl}`
})

const strengthText = computed(() => {
  const texts = ['弱', '中等', '强']
  return texts[passwordStrength.value] || '弱'
})

const strengthWidth = computed(() => {
  const widths = ['33%', '66%', '100%']
  return widths[passwordStrength.value] || '33%'
})

const strengthClass = computed(() => {
  const classes = ['weak', 'medium', 'strong']
  return classes[passwordStrength.value] || 'weak'
})

// 表单验证规则
const passwordRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度需在6-20个字符之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: (rule, value, callback) => {
      if (value !== passwordForm.newPassword) {
        callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    }, trigger: 'blur' }
  ]
}

// 生命周期
onMounted(() => {
  document.addEventListener('click', closeAvatarMenu)
  fetchUserProfile()
  fetchStatistics()
})

onUnmounted(() => {
  document.removeEventListener('click', closeAvatarMenu)
})

// 数据获取
const fetchUserProfile = async () => {
  try {
    const res = await getUserProfile()
    if (res.code === 200) {
      profile.value = res.data
      editForm.username = res.data.username
      editForm.email = res.data.email || ''
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
}

const fetchStatistics = async () => {
  try {
    const res = await getUserStatistics()
    if (res.code === 200) {
      stats.value = [
        { label: '简历数', value: res.data.resumeCount || 0 },
        { label: '优化次数', value: res.data.optimizeCount || 0 },
        { label: '加入天数', value: res.data.joinDays || 0 }
      ]
      animateNumbers()
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

// 数字滚动动画
const animateNumbers = () => {
  stats.value.forEach((stat, index) => {
    const target = stat.value
    const duration = 1200
    const startTime = performance.now()
    
    function update(currentTime) {
      const elapsed = currentTime - startTime
      const progress = Math.min(elapsed / duration, 1)
      const easeProgress = 1 - Math.pow(1 - progress, 3)
      animatedNumbers.value[index] = Math.floor(target * easeProgress)
      
      if (progress < 1) requestAnimationFrame(update)
    }
    
    requestAnimationFrame(update)
  })
}

// 头像相关
const toggleAvatarMenu = () => {
  if (avatarLoading.value) return
  showAvatarMenu.value = !showAvatarMenu.value
}

const closeAvatarMenu = (e) => {
  if (!e.target.closest('.avatar-wrapper')) {
    showAvatarMenu.value = false
  }
}

const handlePreviewAvatar = () => {
  if (!profile.value.avatarUrl) {
    ElMessage.warning('暂无头像可预览')
    return
  }
  showAvatarMenu.value = false
  showPreview.value = true
}

const triggerUpload = () => {
  if (avatarLoading.value) return
  showAvatarMenu.value = false
  avatarInputRef.value?.click()
}

const handleAvatarChange = async (event) => {
  const file = event.target.files?.[0]
  if (!file) return

  const allowedTypes = ['image/jpeg', 'image/png', 'image/jpg']
  if (!allowedTypes.includes(file.type)) {
    ElMessage.error('只支持 jpg、png 格式的图片')
    event.target.value = ''
    return
  }

  const maxSize = 2 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error('图片大小不能超过 2MB')
    event.target.value = ''
    return
  }

  avatarLoading.value = true
  try {
    const res = await uploadAvatar(file)
    if (res.code === 200) {
      ElMessage.success('头像上传成功')
      profile.value.avatarUrl = res.data
      const user = JSON.parse(localStorage.getItem('user') || '{}')
      user.avatarUrl = res.data
      localStorage.setItem('user', JSON.stringify(user))
    } else {
      ElMessage.error(res.message || '头像上传失败')
    }
  } catch (error) {
    ElMessage.error('头像上传失败')
  } finally {
    avatarLoading.value = false
    event.target.value = ''
  }
}

const handleDeleteAvatar = async () => {
  try {
    await ElMessageBox.confirm('确定要删除头像吗？', '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }

  avatarLoading.value = true
  try {
    const res = await deleteAvatar()
    if (res.code === 200) {
      ElMessage.success('头像已删除')
      profile.value.avatarUrl = null
      const user = JSON.parse(localStorage.getItem('user') || '{}')
      delete user.avatarUrl
      localStorage.setItem('user', JSON.stringify(user))
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    ElMessage.error('删除失败')
  } finally {
    avatarLoading.value = false
  }
}

// 编辑模式
const startEdit = () => {
  isEditing.value = true
}

const cancelEdit = () => {
  isEditing.value = false
  editForm.username = profile.value.username
  editForm.email = profile.value.email || ''
}

const saveProfile = async () => {
  saving.value = true
  try {
    const promises = []
    if (editForm.username !== profile.value.username) {
      promises.push(updateUsername(editForm.username))
    }
    if (editForm.email !== profile.value.email) {
      promises.push(updateEmail(editForm.email))
    }

    if (promises.length > 0) {
      await Promise.all(promises)
      ElMessage.success('保存成功')
      await fetchUserProfile()
    } else {
      ElMessage.warning('没有修改任何内容')
    }
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
    isEditing.value = false
  }
}

// 密码相关
const updatePasswordStrength = () => {
  const pwd = passwordForm.newPassword
  let score = 0
  if (pwd.length >= 8) score++
  if (/[A-Z]/.test(pwd)) score++
  if (/[a-z]/.test(pwd)) score++
  if (/[0-9]/.test(pwd)) score++
  if (/[^A-Za-z0-9]/.test(pwd)) score++
  
  if (score <= 2) passwordStrength.value = 0
  else if (score <= 3) passwordStrength.value = 1
  else passwordStrength.value = 2
}

const handleUpdatePassword = async () => {
  try {
    await passwordFormRef.value.validate()
  } catch {
    return
  }

  passwordLoading.value = true
  try {
    const res = await updatePassword(passwordForm.oldPassword, passwordForm.newPassword)
    if (res.code === 200) {
      ElMessage.success('密码修改成功，请重新登录')
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      router.push('/login')
      resetPasswordForm()
    } else {
      ElMessage.error(res.message || '密码修改失败')
    }
  } catch (error) {
    ElMessage.error('密码修改失败')
  } finally {
    passwordLoading.value = false
  }
}

const resetPasswordForm = () => {
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordStrength.value = 0
  passwordFormRef.value?.clearValidate()
}

// 工具方法
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return `${date.getFullYear()}年${date.getMonth() + 1}月${date.getDate()}日`
}
</script>

<style scoped>
.profile-container {
  min-height: 100vh;
  background: #f5f5f5;
}

.content-wrapper {
  max-width: 800px;
  margin: 0 auto;
  padding: 32px;
}

/* 返回按钮 */
.back-btn-container {
  margin-bottom: 20px;
}

.back-btn {
  background: transparent;
  border: none;
  color: var(--ink-text-secondary);
  font-size: 14px;
  padding: 8px 0;
  transition: color 0.2s var(--ink-ease);
}

.back-btn:hover {
  color: var(--ink-text-title);
  background: transparent;
}

.back-btn .el-icon {
  margin-right: 4px;
}

/* 页面顶部 */
.profile-header {
  display: flex;
  align-items: center;
  gap: 24px;
  padding-bottom: 32px;
  border-bottom: 1px solid #e8e8e8;
  margin-bottom: 24px;
}

.avatar-container {
  position: relative;
}

.avatar-wrapper {
  position: relative;
  cursor: pointer;
  transition: transform 0.3s var(--ink-ease);
}

.avatar-wrapper:hover {
  transform: scale(1.05);
}

.main-avatar {
  width: 80px;
  height: 80px;
  border: 1px solid #e8e8e8;
  border-radius: 50%;
}

.avatar-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
  opacity: 0;
  transition: opacity 0.3s var(--ink-ease);
}

.avatar-wrapper:hover .avatar-overlay {
  opacity: 1;
}

.avatar-overlay .el-icon {
  font-size: 20px;
  margin-bottom: 4px;
}

.avatar-overlay span {
  font-size: 12px;
}

/* 头像下拉菜单 */
.avatar-dropdown {
  position: absolute;
  top: calc(100% + 8px);
  left: 50%;
  transform: translateX(-50%);
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  padding: 8px 0;
  min-width: 140px;
  z-index: 1000;
  animation: dropdownEnter 200ms var(--ink-ease);
}

@keyframes dropdownEnter {
  from { opacity: 0; transform: translateX(-50%) translateY(-8px); }
  to { opacity: 1; transform: translateX(-50%) translateY(0); }
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 16px;
  cursor: pointer;
  transition: background 0.2s;
  color: #333;
  font-size: 14px;
}

.dropdown-item:hover {
  background: #f5f5f5;
}

.dropdown-item.danger {
  color: #c75b5b;
}

.dropdown-divider {
  height: 1px;
  background: #f0f0f0;
  margin: 8px 0;
}

.user-info {
  flex: 1;
}

.username {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-family: var(--ink-font-serif);
  color: var(--ink-text-title);
}

.register-date {
  margin: 0;
  font-size: 13px;
  color: #999;
}

/* 统计卡片 */
.stats-card {
  display: flex;
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.stat-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  animation: statEnter 400ms var(--ink-ease) both;
  animation-delay: var(--stagger-delay);
}

@keyframes statEnter {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}

.stat-number {
  font-size: 28px;
  font-family: var(--ink-font-serif);
  font-weight: 600;
  color: var(--ink-text-title);
  margin-bottom: 6px;
}

.stat-label {
  font-size: 12px;
  color: #999;
}

.stat-divider {
  position: absolute;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 1px;
  height: 40px;
  background: #f0f0f0;
}

/* 信息卡片 */
.info-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  animation: cardEnter 400ms var(--ink-ease) 200ms both;
}

@keyframes cardEnter {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.card-title {
  font-size: 18px;
  font-family: var(--ink-font-serif);
  color: var(--ink-text-title);
  margin: 0;
}

.card-divider {
  height: 1px;
  background: #f0f0f0;
  margin-bottom: 20px;
}

.edit-btn {
  background: transparent;
  border: 1px solid var(--ink-primary);
  color: var(--ink-primary);
  border-radius: 8px;
  padding: 6px 16px;
  font-size: 14px;
}

.edit-btn:hover {
  background: rgba(44, 62, 80, 0.05);
}

.edit-actions {
  display: flex;
  gap: 12px;
}

.save-btn {
  background: var(--ink-text-title);
  border: none;
  color: #fff;
  border-radius: 8px;
  padding: 6px 16px;
  font-size: 14px;
}

.save-btn:hover {
  background: var(--ink-dark);
}

.cancel-btn {
  background: transparent;
  border: 1px solid #ccc;
  color: #666;
  border-radius: 8px;
  padding: 6px 16px;
  font-size: 14px;
}

.info-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-row {
  display: flex;
  align-items: center;
}

.info-label {
  width: 100px;
  font-size: 14px;
  color: #999;
  flex-shrink: 0;
}

.info-value {
  flex: 1;
  font-size: 14px;
  color: #333;
}

.edit-input {
  flex: 1;
  max-width: 300px;
}

.edit-input :deep(.el-input__wrapper) {
  background: #f7f8fa;
  border-color: #e8e8e8;
  border-radius: 8px;
}

.edit-input :deep(.el-input__wrapper.is-focus) {
  border-color: var(--ink-primary);
  box-shadow: 0 0 0 3px rgba(26, 26, 46, 0.06);
}

/* 密码卡片 */
.password-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  animation: cardEnter 400ms var(--ink-ease) 300ms both;
}

.password-card .card-header {
  margin-bottom: 20px;
}

.header-line {
  width: 3px;
  height: 20px;
  background: #333;
  margin-right: 12px;
}

.password-card .card-header {
  display: flex;
  align-items: center;
}

.password-form {
  padding-top: 8px;
}

.form-input :deep(.el-input__wrapper) {
  background: #f7f8fa;
  border-color: #e8e8e8;
  border-radius: 8px;
  height: 40px;
}

.form-input :deep(.el-input__wrapper.is-focus) {
  border-color: var(--ink-primary);
  box-shadow: 0 0 0 3px rgba(26, 26, 46, 0.06);
}

.form-input :deep(.el-input__inner) {
  height: 40px;
}

.password-strength {
  margin-top: 12px;
}

.strength-label {
  font-size: 12px;
  color: #999;
  margin-bottom: 6px;
}

.strength-bar {
  height: 4px;
  background: #f0f0f0;
  border-radius: 2px;
  overflow: hidden;
}

.strength-fill {
  height: 100%;
  border-radius: 2px;
  transition: width 0.3s var(--ink-ease), background 0.3s var(--ink-ease);
}

.strength-fill.weak {
  background: #ccc;
}

.strength-fill.medium {
  background: #999;
}

.strength-fill.strong {
  background: var(--ink-text-title);
}

.form-actions {
  display: flex;
  gap: 12px;
  padding-top: 8px;
}

.btn-primary-ink {
  background: var(--ink-text-title) !important;
  border-color: var(--ink-text-title) !important;
  color: #fff !important;
  border-radius: 8px;
  padding: 10px 24px;
}

.btn-primary-ink:hover {
  background: var(--ink-dark) !important;
}

.btn-outline-ink {
  background: transparent !important;
  border-color: var(--ink-primary) !important;
  color: var(--ink-primary) !important;
  border-radius: 8px;
  padding: 10px 24px;
}

.btn-outline-ink:hover {
  background: rgba(44, 62, 80, 0.05) !important;
}

/* 响应式 */
@media (max-width: 600px) {
  .content-wrapper {
    padding: 16px;
  }
  
  .profile-header {
    flex-direction: column;
    text-align: center;
  }
  
  .stats-card {
    flex-direction: column;
    gap: 16px;
  }
  
  .stat-divider {
    display: none;
  }
  
  .stat-item {
    padding-bottom: 16px;
    border-bottom: 1px solid #f0f0f0;
  }
  
  .stat-item:last-child {
    border-bottom: none;
    padding-bottom: 0;
  }
  
  .info-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
  
  .info-label {
    width: auto;
  }
}
</style>
