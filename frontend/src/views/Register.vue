<template>
  <div class="register-page">
    <!-- 左侧装饰区 -->
    <div class="register-left">
      <div class="ink-decoration ink-1"></div>
      <div class="ink-decoration ink-2"></div>
      <div class="ink-decoration ink-3"></div>
      <div class="brand-section">
        <div class="logo-icon">
          <span class="logo-text-inner">墨</span>
        </div>
        <h1 class="brand-title">AI求职助手</h1>
        <p class="brand-desc">智能简历优化，助力职场进阶</p>
      </div>
    </div>

    <!-- 右侧注册区 -->
    <div class="register-right">
      <div class="register-card">
        <div class="card-header">
          <h2 class="card-title">注册</h2>
          <p class="card-subtitle">创建您的账号，开启求职之旅</p>
        </div>

        <el-form ref="registerFormRef" :model="registerForm" :rules="rules" class="register-form">
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
        <div class="login-link">
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

// 表单校验规则
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
  // 先做表单校验
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
.register-page {
  display: flex;
  min-height: 100vh;
}

/* 左侧装饰区 */
.register-left {
  width: 55%;
  background: linear-gradient(135deg, #1a1a2e 0%, #2c3e50 100%);
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.ink-decoration {
  position: absolute;
  border-radius: 50%;
  opacity: 0;
}

.ink-1 {
  width: 600px;
  height: 600px;
  top: 20%;
  left: 10%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.06) 0%, rgba(255, 255, 255, 0.02) 40%, transparent 70%);
  animation: inkSpread 2s ease-out 0.5s both;
}

.ink-2 {
  width: 400px;
  height: 400px;
  bottom: 10%;
  right: 15%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.08) 0%, rgba(255, 255, 255, 0.03) 50%, transparent 70%);
  animation: inkSpread 2s ease-out 1s both;
}

.ink-3 {
  width: 300px;
  height: 300px;
  top: 50%;
  right: 5%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.05) 0%, transparent 60%);
  animation: inkSpread 2s ease-out 1.5s both;
}

@keyframes inkSpread {
  from {
    opacity: 0;
    transform: scale(0.5);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.brand-section {
  text-align: center;
  z-index: 1;
}

.logo-icon {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 24px;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.logo-text-inner {
  color: #fff;
  font-family: 'Noto Serif SC', Georgia, serif;
  font-size: 28px;
  font-weight: 600;
}

.brand-title {
  font-size: 36px;
  font-weight: 700;
  color: #fff;
  font-family: 'Noto Serif SC', Georgia, serif;
  margin-bottom: 12px;
}

.brand-desc {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.7);
}

/* 右侧注册区 */
.register-right {
  width: 45%;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.register-card {
  width: 100%;
  max-width: 400px;
  background: #fff;
  border-radius: 16px;
  padding: 40px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.08);
}

.card-header {
  text-align: center;
  margin-bottom: 32px;
}

.card-title {
  font-size: 24px;
  font-weight: 600;
  color: #1a1a2e;
  font-family: 'Noto Serif SC', Georgia, serif;
  margin-bottom: 8px;
}

.card-subtitle {
  font-size: 14px;
  color: #999;
}

/* 输入框 */
.input-ink {
  height: 44px;
  border-radius: 8px;
  border: 1px solid #e8e8e8;
  transition: all 0.3s ease;
}

.input-ink:focus {
  border-color: #1a1a2e;
  box-shadow: 0 0 0 3px rgba(26, 26, 46, 0.08);
}

/* 同意协议 */
.agree-terms {
  margin-bottom: 24px;
}

.agree-checkbox {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #666;
  cursor: pointer;
  flex-wrap: wrap;
}

.agree-checkbox input {
  width: 16px;
  height: 16px;
  accent-color: #1a1a2e;
}

.terms-link {
  color: #1a1a2e;
  text-decoration: none;
}

.terms-link:hover {
  text-decoration: underline;
}

/* 按钮 */
.btn-primary-ink {
  height: 44px;
  background: #1a1a2e !important;
  border-color: #1a1a2e !important;
  color: #fff !important;
  border-radius: 8px !important;
  font-size: 15px !important;
  font-weight: 500 !important;
}

.btn-primary-ink:hover {
  background: #151525 !important;
  border-color: #151525 !important;
}

/* 登录链接 */
.login-link {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: #666;
}

.link-primary {
  color: #1a1a2e;
  text-decoration: none;
  font-weight: 500;
}

.link-primary:hover {
  text-decoration: underline;
}

/* 响应式 */
@media (max-width: 768px) {
  .register-page {
    flex-direction: column;
  }

  .register-left {
    width: 100%;
    min-height: 200px;
  }

  .register-right {
    width: 100%;
    padding: 24px;
  }

  .brand-title {
    font-size: 28px;
  }
}
</style>
