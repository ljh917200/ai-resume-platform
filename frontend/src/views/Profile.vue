<template>
  <div class="profile-container">
    <!-- 顶部导航栏 -->
    <div class="top-nav">
      <div class="nav-left">
        <el-button @click="goBack" text>
          <el-icon><i class="el-icon-arrow-left"></i></el-icon>
        </el-button>
        <span class="page-title">个人中心</span>
      </div>
      <div class="user-info">
        <el-dropdown>
          <div class="user-avatar">
            <!-- 顶部导航栏头像：优先显示用户头像，否则显示用户名首字母 -->
            <el-avatar :size="40" :src="fullAvatarUrl || undefined">
              {{ profile.username ? profile.username.charAt(0).toUpperCase() : 'U' }}
            </el-avatar>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="goHome">返回首页</el-dropdown-item>
              <el-dropdown-item @click="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <div class="main-content">
      <!-- 统计卡片区域 -->
      <div class="stats-section">
        <div class="stats-container">
          <div class="stat-card">
            <div class="stat-number">{{ statistics.resumeCount }}</div>
            <div class="stat-label">简历数量</div>
          </div>
          <div class="stat-card">
            <div class="stat-number">{{ statistics.optimizeCount }}</div>
            <div class="stat-label">优化次数</div>
          </div>
          <div class="stat-card">
            <div class="stat-number">{{ statistics.quotaUsed }}</div>
            <div class="stat-label">已用额度</div>
          </div>
          <div class="stat-card">
            <div class="stat-number">{{ statistics.joinDays }}</div>
            <div class="stat-label">加入天数</div>
          </div>
        </div>
      </div>

      <!-- 个人资料表单 -->
      <div class="form-card profile-card">
        <div class="card-header">
          <el-icon class="header-icon"><i class="el-icon-user"></i></el-icon>
          <span>个人资料</span>
        </div>

        <!-- ========== 头像上传区域 ========== -->
        <div class="avatar-section">
          <div class="avatar-main">
            <!-- 头像包装器 -->
            <div class="avatar-wrapper" :class="{ 'avatar-loading': avatarLoading }" @click="toggleAvatarMenu">
              <el-avatar
                  :size="100"
                  :src="fullAvatarUrl || undefined"
              >
                {{ profile.username ? profile.username.charAt(0).toUpperCase() : 'U' }}
              </el-avatar>

              <!-- 悬停遮罩（无头像时不显示） -->
              <div class="avatar-overlay" v-if="!avatarLoading && profile.avatarUrl">
                <el-icon><i class="el-icon-camera"></i></el-icon>
                <span>点击设置</span>
              </div>

              <!-- 上传中遮罩 -->
              <div class="avatar-overlay uploading" v-if="avatarLoading">
                <el-icon class="is-loading"><i class="el-icon-loading"></i></el-icon>
                <span>上传中...</span>
              </div>

              <!-- 自定义下拉菜单 -->
              <div v-if="showAvatarMenu" class="avatar-dropdown">
                <div class="avatar-dropdown-item" @click.stop="handlePreviewAvatar">
                  <el-icon><i class="el-icon-view"></i></el-icon>
                  <span>查看大图</span>
                </div>
                <div class="avatar-dropdown-item" @click.stop="triggerUpload">
                  <el-icon><i class="el-icon-camera"></i></el-icon>
                  <span>更换头像</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 头像操作区：提示 + 删除按钮 -->
          <div class="avatar-tips">
            <p>支持 jpg、png 格式，大小不超过 2MB</p>
            <div class="avatar-actions">
              <el-button
                  v-if="profile.avatarUrl"
                  type="danger"
                  text
                  size="small"
                  @click="handleDeleteAvatar"
                  :loading="avatarLoading"
              >
                <el-icon><i class="el-icon-delete"></i></el-icon>
                <span>删除头像</span>
              </el-button>
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
        </div>
        <!-- ========== 头像区域结束 ========== -->

        <el-form
            ref="profileFormRef"
            :model="profileForm"
            :rules="profileRules"
            label-width="100px"
            class="profile-form"
        >
          <el-form-item label="用户ID">
            <el-input v-model="profile.id" disabled />
          </el-form-item>
          <el-form-item label="用户名" prop="username">
            <el-input
                v-model="profileForm.username"
                placeholder="请输入用户名"
                maxlength="20"
                show-word-limit
            >
              <template #append>
                <el-button @click="handleUpdateUsername" :loading="usernameLoading">
                  保存
                </el-button>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input
                v-model="profileForm.email"
                placeholder="请输入邮箱"
                type="email"
            >
              <template #append>
                <el-button @click="handleUpdateEmail" :loading="emailLoading">
                  保存
                </el-button>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item label="注册时间">
            <el-input v-model="profile.createdAt" disabled />
          </el-form-item>
        </el-form>
      </div>

      <!-- 修改密码卡片 -->
      <div class="form-card password-card">
        <div class="card-header">
          <el-icon class="header-icon"><i class="el-icon-lock"></i></el-icon>
          <span>修改密码</span>
        </div>
        <el-form
            ref="passwordFormRef"
            :model="passwordForm"
            :rules="passwordRules"
            label-width="120px"
            class="password-form"
        >
          <el-form-item label="原密码" prop="oldPassword">
            <el-input
                v-model="passwordForm.oldPassword"
                type="password"
                placeholder="请输入原密码"
                show-password
            />
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input
                v-model="passwordForm.newPassword"
                type="password"
                placeholder="请输入新密码（6-20个字符）"
                show-password
            />
          </el-form-item>
          <el-form-item label="确认新密码" prop="confirmPassword">
            <el-input
                v-model="passwordForm.confirmPassword"
                type="password"
                placeholder="请再次输入新密码"
                show-password
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleUpdatePassword" :loading="passwordLoading">
              修改密码
            </el-button>
            <el-button @click="resetPasswordForm">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>

    <!-- 大图预览（放在最外层，确保覆盖所有内容，不被遮挡） -->
    <el-image-viewer
        v-if="showPreview"
        :url-list="[fullAvatarUrl]"
        :z-index="9999"
        @close="showPreview = false"
    />
  </div>
</template>

<script setup>
/**
 * 个人中心页面组件
 * 功能包括：
 * 1. 显示用户基本信息（ID、用户名、邮箱、注册时间）
 * 2. 头像上传、预览和删除
 * 3. 修改用户名
 * 4. 修改邮箱
 * 5. 修改密码（需验证原密码）
 * 6. 显示统计数据（简历数量、优化次数、已用额度、加入天数）
 */

import { ref, reactive, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'

// 导入用户相关API
import {
  getUserProfile,
  updateUsername,
  updateEmail,
  updatePassword,
  getUserStatistics,
  uploadAvatar,
  deleteAvatar
} from '../api/user'

// ==================== 常量定义 ====================

// API基础地址（用于拼接头像完整URL）
const API_BASE_URL = 'http://localhost:8080'

// ==================== 响应式数据定义 ====================

const router = useRouter()

// 用户资料表单引用
const profileFormRef = ref(null)
// 密码表单引用
const passwordFormRef = ref(null)
// 头像上传Input引用
const avatarInputRef = ref(null)

// 用户基本信息
const profile = ref({
  id: '',
  username: '',
  email: '',
  avatarUrl: '',
  quotaUsed: 0,
  createdAt: ''
})

// 用户名/邮箱编辑表单
const profileForm = reactive({
  username: '',
  email: ''
})

// 统计数据
const statistics = ref({
  resumeCount: 0,
  optimizeCount: 0,
  quotaUsed: 0,
  joinDays: 0
})

// 密码表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 加载状态
const usernameLoading = ref(false)
const emailLoading = ref(false)
const passwordLoading = ref(false)
const avatarLoading = ref(false)

// 头像菜单显示状态
const showAvatarMenu = ref(false)
// 大图预览显示状态
const showPreview = ref(false)

// ==================== 计算属性 ====================

/**
 * 计算属性：完整的头像URL
 * 用于拼接API_BASE_URL与相对路径，组成可访问的完整图片地址
 */
const fullAvatarUrl = computed(() => {
  if (!profile.value.avatarUrl) return ''
  // 如果已经是完整URL（http开头），直接返回
  if (profile.value.avatarUrl.startsWith('http')) {
    return profile.value.avatarUrl
  }
  // 否则拼接API_BASE_URL
  return `${API_BASE_URL}${profile.value.avatarUrl}`
})

// ==================== 表单验证规则 ====================

// 用户名验证规则：3-20个字符
const validateUsername = (rule, value, callback) => {
  if (!value || value.trim() === '') {
    callback(new Error('请输入用户名'))
  } else if (value.length < 3 || value.length > 20) {
    callback(new Error('用户名长度需要在3-20个字符之间'))
  } else {
    callback()
  }
}

// 邮箱验证规则
const validateEmail = (rule, value, callback) => {
  if (!value || value.trim() === '') {
    callback(new Error('请输入邮箱'))
  } else if (!/^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/.test(value)) {
    callback(new Error('请输入正确的邮箱格式'))
  } else {
    callback()
  }
}

// 原密码验证规则
const validateOldPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入原密码'))
  } else {
    callback()
  }
}

// 新密码验证规则：6-20个字符
const validateNewPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入新密码'))
  } else if (value.length < 6 || value.length > 20) {
    callback(new Error('新密码长度需要在6-20个字符之间'))
  } else {
    callback()
  }
}

// 确认新密码验证规则：必须与新密码相同
const validateConfirmPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请再次输入新密码'))
  } else if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

// 用户资料表单验证规则
const profileRules = {
  username: [{ validator: validateUsername, trigger: 'blur' }],
  email: [{ validator: validateEmail, trigger: 'blur' }]
}

// 密码表单验证规则
const passwordRules = {
  oldPassword: [{ validator: validateOldPassword, trigger: 'blur' }],
  newPassword: [{ validator: validateNewPassword, trigger: 'blur' }],
  confirmPassword: [{ validator: validateConfirmPassword, trigger: 'blur' }]
}

// ==================== 生命周期钩子 ====================

/**
 * 组件挂载时获取用户数据和统计数据
 */
onMounted(() => {
  // 添加点击监听，关闭菜单
  document.addEventListener('click', closeAvatarMenu)
  fetchUserProfile()
  fetchStatistics()
})

/**
 * 组件卸载时清理
 */
onUnmounted(() => {
  document.removeEventListener('click', closeAvatarMenu)
})

// ==================== 数据获取方法 ====================

/**
 * 获取用户基本信息
 */
const fetchUserProfile = async () => {
  try {
    const res = await getUserProfile()
    if (res.code === 200) {
      // 更新用户数据
      profile.value = res.data
      // 更新编辑表单
      profileForm.username = res.data.username
      profileForm.email = res.data.email || ''
    } else {
      ElMessage.error(res.message || '获取用户信息失败')
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    ElMessage.error('获取用户信息失败')
  }
}

/**
 * 获取用户统计数据
 */
const fetchStatistics = async () => {
  try {
    const res = await getUserStatistics()
    if (res.code === 200) {
      statistics.value = res.data
    } else {
      ElMessage.error(res.message || '获取统计数据失败')
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
    ElMessage.error('获取统计数据失败')
  }
}

// ==================== 头像相关方法 ====================

/**
 * 切换头像菜单显示状态
 */
const toggleAvatarMenu = () => {
  // 如果正在上传，禁止操作
  if (avatarLoading.value) return
  showAvatarMenu.value = !showAvatarMenu.value
}

/**
 * 点击其他地方关闭头像菜单
 */
const closeAvatarMenu = (e) => {
  if (!e.target.closest('.avatar-wrapper')) {
    showAvatarMenu.value = false
  }
}

/**
 * 预览头像（大图查看）
 * 使用 nextTick 延迟显示，避免事件穿透导致闪烁
 */
const handlePreviewAvatar = () => {
  if (!profile.value.avatarUrl) {
    ElMessage.warning('暂无头像可预览')
    return
  }
  // 关闭菜单
  showAvatarMenu.value = false
  // 延迟显示预览，避免鼠标事件穿透导致闪烁
  nextTick(() => {
    showPreview.value = true
  })
}

/**
 * 触发文件选择（更换头像）
 */
const triggerUpload = () => {
  // 如果正在上传，禁止重复点击
  if (avatarLoading.value) return
  // 关闭菜单
  showAvatarMenu.value = false
  // 触发隐藏的input点击事件
  avatarInputRef.value?.click()
}

/**
 * 处理头像文件选择
 *
 * @param {Event} event - 文件选择事件
 */
const handleAvatarChange = async (event) => {
  // 获取选中的文件
  const file = event.target.files?.[0]
  if (!file) return

  // ========== 前端验证 ==========
  // 1. 验证文件类型
  const allowedTypes = ['image/jpeg', 'image/png', 'image/jpg']
  if (!allowedTypes.includes(file.type)) {
    ElMessage.error('只支持 jpg、png 格式的图片')
    // 清空input，允许重复选择同一文件
    event.target.value = ''
    return
  }

  // 2. 验证文件大小（2MB）
  const maxSize = 2 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error('图片大小不能超过 2MB')
    event.target.value = ''
    return
  }

  // ========== 上传头像 ==========
  avatarLoading.value = true

  try {
    const res = await uploadAvatar(file)

    if (res.code === 200) {
      ElMessage.success('头像上传成功')
      // 更新本地头像URL
      profile.value.avatarUrl = res.data
      // 更新localStorage中的用户信息
      const user = JSON.parse(localStorage.getItem('user') || '{}')
      user.avatarUrl = res.data
      localStorage.setItem('user', JSON.stringify(user))
    } else {
      ElMessage.error(res.message || '头像上传失败')
    }
  } catch (error) {
    console.error('头像上传失败:', error)
    ElMessage.error('头像上传失败，请稍后重试')
  } finally {
    avatarLoading.value = false
    // 清空input，允许重复选择同一文件
    event.target.value = ''
  }
}

/**
 * 删除头像
 */
const handleDeleteAvatar = async () => {
  // 二次确认
  try {
    await ElMessageBox.confirm(
        '确定要删除头像吗？',
        '确认删除',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
    )
  } catch {
    return
  }

  avatarLoading.value = true

  try {
    const res = await deleteAvatar()

    if (res.code === 200) {
      ElMessage.success('头像已删除')
      // 清空本地头像URL
      profile.value.avatarUrl = null
      // 更新localStorage
      const user = JSON.parse(localStorage.getItem('user') || '{}')
      delete user.avatarUrl
      localStorage.setItem('user', JSON.stringify(user))
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    console.error('删除头像失败:', error)
    ElMessage.error('删除失败，请稍后重试')
  } finally {
    avatarLoading.value = false
  }
}

// ==================== 表单操作方法 ====================

/**
 * 修改用户名
 */
const handleUpdateUsername = async () => {
  // 验证表单
  try {
    await profileFormRef.value.validateField('username')
  } catch {
    return
  }

  // 检查是否有变化
  if (profileForm.username === profile.value.username) {
    ElMessage.warning('用户名没有变化')
    return
  }

  usernameLoading.value = true
  try {
    const res = await updateUsername(profileForm.username)
    if (res.code === 200) {
      ElMessage.success('用户名修改成功')
      // 更新本地数据
      profile.value.username = profileForm.username
      // 更新localStorage
      const user = JSON.parse(localStorage.getItem('user') || '{}')
      user.username = profileForm.username
      localStorage.setItem('user', JSON.stringify(user))
    } else {
      ElMessage.error(res.message || '用户名修改失败')
    }
  } catch (error) {
    console.error('用户名修改失败:', error)
    ElMessage.error('用户名修改失败')
  } finally {
    usernameLoading.value = false
  }
}

/**
 * 修改邮箱
 */
const handleUpdateEmail = async () => {
  // 验证表单
  try {
    await profileFormRef.value.validateField('email')
  } catch {
    return
  }

  // 检查是否有变化
  if (profileForm.email.toLowerCase() === profile.value.email?.toLowerCase()) {
    ElMessage.warning('邮箱没有变化')
    return
  }

  // 二次确认
  try {
    await ElMessageBox.confirm(
        `确定要将邮箱修改为 ${profileForm.email} 吗？`,
        '确认修改',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
    )
  } catch {
    return
  }

  emailLoading.value = true
  try {
    const res = await updateEmail(profileForm.email)
    if (res.code === 200) {
      ElMessage.success('邮箱修改成功')
      // 更新本地数据
      profile.value.email = profileForm.email
    } else {
      ElMessage.error(res.message || '邮箱修改失败')
    }
  } catch (error) {
    console.error('邮箱修改失败:', error)
    ElMessage.error('邮箱修改失败')
  } finally {
    emailLoading.value = false
  }
}

/**
 * 修改密码
 */
const handleUpdatePassword = async () => {
  // 验证表单
  try {
    await passwordFormRef.value.validate()
  } catch {
    return
  }

  passwordLoading.value = true
  try {
    const res = await updatePassword(
        passwordForm.oldPassword,
        passwordForm.newPassword
    )
    if (res.code === 200) {
      ElMessage.success('密码修改成功，请重新登录')
      // 清除登录状态
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      // 跳转登录页
      router.push('/login')
      // 清空密码表单
      resetPasswordForm()
    } else {
      ElMessage.error(res.message || '密码修改失败')
    }
  } catch (error) {
    console.error('密码修改失败:', error)
    ElMessage.error('密码修改失败')
  } finally {
    passwordLoading.value = false
  }
}

/**
 * 重置密码表单
 */
const resetPasswordForm = () => {
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  // 清除验证状态
  if (passwordFormRef.value) {
    passwordFormRef.value.clearValidate()
  }
}

// ==================== 页面导航方法 ====================

/**
 * 返回首页
 */
const goBack = () => {
  router.push('/home')
}

/**
 * 跳转首页
 */
const goHome = () => {
  router.push('/home')
}

/**
 * 退出登录
 */
const logout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  router.push('/login')
}
</script>

<style scoped>
.profile-container {
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
  height: 60px;
  z-index: 100;
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-title {
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

/* 主内容区 */
.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 30px;
}

/* 统计卡片区域 */
.stats-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 40px;
  margin-bottom: 30px;
  color: white;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.stats-container {
  display: flex;
  gap: 20px;
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

/* 表单卡片 */
.form-card {
  background: #fff;
  border-radius: 16px;
  padding: 30px;
  margin-bottom: 20px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.form-card:hover {
  transform: translateY(-5px);
}

.profile-card {
  border-top: 4px solid #667eea;
}

.password-card {
  border-top: 4px solid #764ba2;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.header-icon {
  font-size: 24px;
  color: #667eea;
}

.password-card .header-icon {
  color: #764ba2;
}

/* ========== 头像上传区域样式 ========== */
.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 30px;
  padding: 20px 0;
}

.avatar-main {
  display: flex;
  flex-direction: column;
  align-items: center;
}

/* 头像包装器 */
.avatar-wrapper {
  position: relative;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.avatar-wrapper:hover {
  transform: scale(1.05);
}

/* 悬停遮罩 */
.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.avatar-wrapper:hover .avatar-overlay {
  opacity: 1;
}

/* 上传中状态 */
.avatar-overlay.uploading {
  opacity: 1;
  background: rgba(0, 0, 0, 0.7);
}

.avatar-overlay .el-icon {
  font-size: 24px;
  margin-bottom: 4px;
}

.avatar-overlay span {
  font-size: 12px;
}

/* 头像加载状态 */
.avatar-loading .el-avatar {
  opacity: 0.6;
}

/* ========== 自定义下拉菜单样式 ========== */
.avatar-dropdown {
  position: absolute;
  top: calc(100% + 8px);
  left: 50%;
  transform: translateX(-50%);
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  padding: 6px 0;
  min-width: 140px;
  z-index: 1000;
}

.avatar-dropdown::before {
  content: '';
  position: absolute;
  top: -6px;
  left: 50%;
  transform: translateX(-50%);
  border-left: 6px solid transparent;
  border-right: 6px solid transparent;
  border-bottom: 6px solid #fff;
}

.avatar-dropdown-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 16px;
  cursor: pointer;
  transition: background 0.2s;
  color: #606266;
  font-size: 14px;
}

.avatar-dropdown-item:hover {
  background: #f5f7fa;
  color: #667eea;
}

.avatar-dropdown-item .el-icon {
  font-size: 16px;
}

/* ========== 头像提示区域样式 ========== */
.avatar-tips {
  margin-top: 12px;
  text-align: center;
}

.avatar-tips p {
  font-size: 12px;
  color: #909399;
  margin-bottom: 8px;
}

.avatar-actions {
  display: flex;
  justify-content: center;
  gap: 8px;
}

/* 删除按钮样式优化 */
.avatar-actions .el-button {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  border-radius: 4px;
  transition: all 0.2s;
}

.avatar-actions .el-button:hover {
  background: #fef0f0;
  color: #f56c6c;
}

.avatar-actions .el-button .el-icon {
  font-size: 14px;
}

.avatar-actions .el-button span {
  font-size: 12px;
}

/* ========== 头像区域样式结束 ========== */

.profile-form,
.password-form {
  padding: 0;
}

.el-form-item {
  margin-bottom: 24px;
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
}

:deep(.el-input.is-disabled .el-input__wrapper) {
  background: #f5f5f5;
}

:deep(.el-button--primary) {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
  border-radius: 8px;
  padding: 12px 24px;
}

:deep(.el-button--primary:hover) {
  background: linear-gradient(135deg, #764ba2, #667eea);
}

/* 响应式布局 */
@media (max-width: 768px) {
  .stats-container {
    flex-wrap: wrap;
  }

  .stat-card {
    flex: 1 1 45%;
  }

  .main-content {
    padding: 15px;
  }
}
</style>