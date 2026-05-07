<template>
  <div class="auth-page">
    <!-- 左侧品牌展示区 -->
    <div class="brand-section">
      <div class="ink-decoration ink-1"></div>
      <div class="ink-decoration ink-2"></div>
      <div class="ink-decoration ink-3"></div>
      
      <div class="brand-content">
        <div class="logo-wrapper">
          <div class="logo-icon">
            <span class="logo-text">墨</span>
          </div>
        </div>
        <h1 class="brand-title">AI求职助手</h1>
        <p class="brand-slogan">以墨为笔，书写前程</p>
        
        <div class="features">
          <div class="feature-item">
            <div class="feature-icon">✦</div>
            <span class="feature-text">智能简历优化</span>
          </div>
          <div class="feature-item">
            <div class="feature-icon">✦</div>
            <span class="feature-text">岗位精准匹配</span>
          </div>
          <div class="feature-item">
            <div class="feature-icon">✦</div>
            <span class="feature-text">面试全程陪练</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧表单区 -->
    <div class="form-section">
      <div class="form-card">
        <div class="card-header">
          <h2 class="form-title">登录</h2>
          <p class="form-subtitle">欢迎回来</p>
        </div>

        <el-form ref="loginFormRef" :model="loginForm" :rules="rules" class="auth-form">
          <!-- 登录方式切换 -->
          <div class="login-tabs">
            <div
                :class="['tab-item', { active: loginType === 'username' }]"
                @click="loginType = 'username'"
            >
              用户名登录
            </div>
            <div
                :class="['tab-item', { active: loginType === 'email' }]"
                @click="loginType = 'email'"
            >
              邮箱登录
            </div>
          </div>

          <!-- 账号输入 -->
          <el-form-item prop="account">
            <el-input
                v-model="loginForm.account"
                :placeholder="loginType === 'username' ? '请输入用户名' : '请输入邮箱'"
                class="input-ink"
                :prefix-icon="loginType === 'username' ? User : Message"
            />
          </el-form-item>

          <!-- 密码输入 -->
          <el-form-item prop="password">
            <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                class="input-ink"
                :prefix-icon="Lock"
                show-password
            />
          </el-form-item>

          <!-- 记住我 & 忘记密码 -->
          <div class="form-footer">
            <label class="remember-checkbox">
              <input type="checkbox" v-model="rememberMe" />
              <span>记住我</span>
            </label>
            <a href="#" class="forgot-link">忘记密码？</a>
          </div>

          <!-- 登录按钮 -->
          <el-button
              class="btn-primary-ink"
              @click="handleLogin"
              :loading="loading"
              style="width: 100%"
          >
            {{ loading ? '登录中...' : '登录' }}
          </el-button>
        </el-form>

        <!-- 注册链接 -->
        <div class="auth-link">
          <span>还没有账号？</span>
          <router-link to="/register" class="link-primary">立即注册</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Message } from '@element-plus/icons-vue'
import { loginByUsername, loginByEmail } from '@/api/auth'

const router = useRouter()

const loginType = ref('username')
const rememberMe = ref(false)
const loading = ref(false)
const loginFormRef = ref(null)

const loginForm = reactive({
  account: '',
  password: ''
})

const rules = {
  account: [
    { required: true, message: '请输入账号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  try {
    await loginFormRef.value.validate()
  } catch {
    return
  }

  loading.value = true

  try {
    let res
    if (loginType.value === 'username') {
      res = await loginByUsername(loginForm.account, loginForm.password)
    } else {
      res = await loginByEmail(loginForm.account, loginForm.password)
    }

    if (res.code === 200) {
      localStorage.setItem('token', res.data.token)
      localStorage.setItem('user', JSON.stringify(res.data.user))
      ElMessage.success('登录成功')
      router.push('/dashboard')
    } else {
      ElMessage.error(res.message || '登录失败')
    }
  } catch (error) {
    ElMessage.error('登录失败，请稍后重试')
    console.error('登录失败:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* ========== 整体布局 ========== */
.auth-page {
  display: flex;
  min-height: 100vh;
  overflow: hidden;
}

/* ========== 左侧品牌展示区 ========== */
.brand-section {
  width: 50%;
  background: #fafafa;
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  animation: fadeInLeft 0.6s ease-out both;
}

@keyframes fadeInLeft {
  from {
    opacity: 0;
    transform: translateX(-30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

/* 水墨装饰背景 */
.ink-decoration {
  position: absolute;
  border-radius: 50%;
  opacity: 0;
  background: radial-gradient(circle, rgba(26, 26, 46, 0.08) 0%, rgba(26, 26, 46, 0.03) 50%, transparent 70%);
}

.ink-1 {
  width: 500px;
  height: 500px;
  top: 10%;
  left: -10%;
  animation: inkSpread 2s ease-out 0.3s both;
}

.ink-2 {
  width: 350px;
  height: 350px;
  bottom: 15%;
  right: -5%;
  animation: inkSpread 2s ease-out 0.8s both;
}

.ink-3 {
  width: 250px;
  height: 250px;
  top: 50%;
  left: 30%;
  background: radial-gradient(circle, rgba(26, 26, 46, 0.05) 0%, transparent 60%);
  animation: inkSpread 2s ease-out 1.3s both;
}

@keyframes inkSpread {
  from {
    opacity: 0;
    transform: scale(0.3);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

/* 品牌内容 */
.brand-content {
  text-align: center;
  z-index: 1;
  padding: 40px;
}

.logo-wrapper {
  margin-bottom: 24px;
}

.logo-icon {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: var(--ink-text-title);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
  box-shadow: 0 4px 20px rgba(26, 26, 46, 0.15);
}

.logo-text {
  color: #fff;
  font-family: var(--ink-font-serif);
  font-size: 36px;
  font-weight: 600;
}

.brand-title {
  font-size: 32px;
  font-weight: 700;
  color: var(--ink-text-title);
  font-family: var(--ink-font-serif);
  margin-bottom: 12px;
}

.brand-slogan {
  font-size: 16px;
  color: #999;
  margin-bottom: 40px;
}

/* 特性列表 */
.features {
  display: flex;
  justify-content: center;
  gap: 32px;
}

.feature-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.feature-icon {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: rgba(26, 26, 46, 0.05);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  color: var(--ink-text-title);
}

.feature-text {
  font-size: 13px;
  color: #666;
}

/* ========== 右侧表单区 ========== */
.form-section {
  width: 50%;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  animation: fadeInRight 0.6s ease-out 0.1s both;
}

@keyframes fadeInRight {
  from {
    opacity: 0;
    transform: translateX(30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.form-card {
  width: 100%;
  max-width: 380px;
  background: #fff;
  padding: 40px;
}

.card-header {
  text-align: center;
  margin-bottom: 32px;
}

.form-title {
  font-size: 24px;
  font-weight: 600;
  color: var(--ink-text-title);
  font-family: var(--ink-font-serif);
  margin-bottom: 8px;
}

.form-subtitle {
  font-size: 14px;
  color: #999;
}

/* 登录方式切换 */
.login-tabs {
  display: flex;
  background: #f7f8fa;
  border-radius: 8px;
  padding: 4px;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 10px;
  font-size: 14px;
  color: #666;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.tab-item.active {
  background: #fff;
  color: var(--ink-text-title);
  font-weight: 500;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

/* ========== 表单样式 ========== */
.auth-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 输入框 */
.input-ink {
  height: 44px;
}

.input-ink :deep(.el-input__wrapper) {
  border-radius: 8px;
  border: 1px solid #e8e8e8;
  background: #f7f8fa;
  padding: 0 14px;
  transition: all 0.3s ease;
}

.input-ink:focus :deep(.el-input__wrapper) {
  border-color: var(--ink-text-title);
  box-shadow: 0 0 0 3px rgba(26, 26, 46, 0.08);
  background: #fff;
}

.input-ink :deep(.el-input__inner) {
  height: 44px;
  line-height: 44px;
  padding: 0;
  background: transparent;
}

.input-ink :deep(.el-input__inner)::placeholder {
  color: #ccc;
}

/* 表单底部 */
.form-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.remember-checkbox {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #666;
  cursor: pointer;
}

.remember-checkbox input {
  width: 16px;
  height: 16px;
  accent-color: var(--ink-text-title);
}

.forgot-link {
  font-size: 13px;
  color: #666;
  text-decoration: none;
}

.forgot-link:hover {
  color: var(--ink-text-title);
  text-decoration: underline;
}

/* 按钮 */
.btn-primary-ink {
  height: 44px;
  background: var(--ink-text-title) !important;
  border-color: var(--ink-text-title) !important;
  color: #fff !important;
  border-radius: 8px !important;
  font-size: 15px !important;
  font-weight: 500 !important;
  transition: all 0.2s ease !important;
}

.btn-primary-ink:hover {
  background: #151525 !important;
  border-color: #151525 !important;
}

.btn-primary-ink:active {
  transform: scale(0.98);
}

/* 底部链接 */
.auth-link {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: #666;
}

.link-primary {
  color: #666;
  text-decoration: none;
  margin-left: 4px;
}

.link-primary:hover {
  color: var(--ink-text-title);
  text-decoration: underline;
}

/* ========== 响应式 ========== */
@media (max-width: 768px) {
  .brand-section {
    display: none;
  }

  .form-section {
    width: 100%;
    background: #fafafa;
    position: relative;
  }

  .form-section::before {
    content: '';
    position: absolute;
    top: 10%;
    left: -10%;
    width: 400px;
    height: 400px;
    border-radius: 50%;
    background: radial-gradient(circle, rgba(26, 26, 46, 0.05) 0%, transparent 60%);
  }

  .form-card {
    position: relative;
    z-index: 1;
  }
}
</style>