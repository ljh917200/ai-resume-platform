<template>
  <div class="login-page">
    <!-- 左侧装饰区 -->
    <div class="login-left">
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

    <!-- 右侧登录区 -->
    <div class="login-right">
      <div class="login-card">
        <div class="card-header">
          <h2 class="card-title">登录</h2>
          <p class="card-subtitle">欢迎回来，请登录您的账号</p>
        </div>

        <el-form ref="loginFormRef" :model="loginForm" :rules="rules" class="login-form">
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

          <!-- 用户名/邮箱输入 -->
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
        <div class="register-link">
          <span>还没有账号？</span>
          <router-link to="/register" class="link-primary">立即注册</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {ref, reactive} from 'vue'
import {useRouter} from 'vue-router'
import {ElMessage} from 'element-plus'
import {User, Lock, Message} from '@element-plus/icons-vue'
import {loginByUsername, loginByEmail} from '@/api/auth'

const router = useRouter()

const loginType = ref('username')
const rememberMe = ref(false)
const loading = ref(false)
const loginFormRef = ref(null)

const loginForm = reactive({
  account: '',
  password: ''
})

// 表单校验规则
const rules = {
  account: [
    {required: true, message: '请输入账号', trigger: 'blur'}
  ],
  password: [
    {required: true, message: '请输入密码', trigger: 'blur'}
  ]
}

const handleLogin = async () => {
  // 先做表单校验
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
.login-page {
  display: flex;
  min-height: 100vh;
}

/* 左侧装饰区 */
.login-left {
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

/* 右侧登录区 */
.login-right {
  width: 45%;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.login-card {
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

/* 登录方式切换 */
.login-tabs {
  display: flex;
  margin-bottom: 24px;
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
  color: #1a1a2e;
  font-weight: 500;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
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

/* 表单底部 */
.form-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
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
  accent-color: #1a1a2e;
}

.forgot-link {
  font-size: 13px;
  color: #1a1a2e;
  text-decoration: none;
}

.forgot-link:hover {
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

/* 注册链接 */
.register-link {
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
  .login-page {
    flex-direction: column;
  }

  .login-left {
    width: 100%;
    min-height: 200px;
  }

  .login-right {
    width: 100%;
    padding: 24px;
  }

  .brand-title {
    font-size: 28px;
  }
}
</style>
