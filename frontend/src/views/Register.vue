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
          <h2 class="form-title">注册</h2>
          <p class="form-subtitle">创建你的账号</p>
        </div>

        <el-form ref="registerFormRef" :model="registerForm" :rules="rules" class="auth-form">
          <!-- 用户名输入 -->
          <el-form-item prop="username">
            <el-input
                v-model="registerForm.username"
                placeholder="请输入用户名"
                class="input-ink"
                :prefix-icon="User"
            />
          </el-form-item>

          <!-- 邮箱输入 -->
          <el-form-item prop="email">
            <el-input
                v-model="registerForm.email"
                placeholder="请输入邮箱"
                class="input-ink"
                :prefix-icon="Message"
            />
          </el-form-item>

          <!-- 密码输入 -->
          <el-form-item prop="password">
            <el-input
                v-model="registerForm.password"
                type="password"
                placeholder="请输入密码"
                class="input-ink"
                :prefix-icon="Lock"
                show-password
            />
          </el-form-item>

          <!-- 确认密码 -->
          <el-form-item prop="confirmPassword">
            <el-input
                v-model="registerForm.confirmPassword"
                type="password"
                placeholder="请确认密码"
                class="input-ink"
                :prefix-icon="Lock"
                show-password
            />
          </el-form-item>

          <!-- 同意协议 -->
          <div class="agree-terms">
            <label class="agree-checkbox">
              <input type="checkbox" v-model="agreeTerms" />
              <span>我已阅读并同意</span>
              <a href="#" class="terms-link">《用户协议》</a>
              <span>和</span>
              <a href="#" class="terms-link">《隐私政策》</a>
            </label>
          </div>

          <!-- 注册按钮 -->
          <el-button
              class="btn-primary-ink"
              @click="handleRegister"
              :loading="loading"
              style="width: 100%"
          >
            {{ loading ? '注册中...' : '注册' }}
          </el-button>
        </el-form>

        <!-- 登录链接 -->
        <div class="auth-link">
          <span>已有账号？</span>
          <router-link to="/login" class="link-primary">立即登录</router-link>
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
import { register } from '@/api/auth'

const router = useRouter()

const agreeTerms = ref(false)
const loading = ref(false)
const registerFormRef = ref(null)

const registerForm = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度2-20个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度6-20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const handleRegister = async () => {
  try {
    await registerFormRef.value.validate()
  } catch {
    return
  }

  if (!agreeTerms.value) {
    ElMessage.warning('请阅读并同意用户协议和隐私政策')
    return
  }

  loading.value = true

  try {
    const res = await register({
      username: registerForm.username,
      email: registerForm.email,
      password: registerForm.password
    })

    if (res.code === 200) {
      localStorage.setItem('token', res.data.token)
      localStorage.setItem('user', JSON.stringify(res.data.user))
      ElMessage.success('注册成功')
      router.push('/dashboard')
    } else {
      ElMessage.error(res.message || '注册失败')
    }
  } catch (error) {
    ElMessage.error('注册失败，请稍后重试')
    console.error('注册失败:', error)
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

/* 同意协议 */
.agree-terms {
  margin-top: 4px;
}

.agree-checkbox {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #666;
  cursor: pointer;
  flex-wrap: wrap;
}

.agree-checkbox input {
  width: 16px;
  height: 16px;
  accent-color: var(--ink-text-title);
}

.terms-link {
  color: var(--ink-text-title);
  text-decoration: none;
}

.terms-link:hover {
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