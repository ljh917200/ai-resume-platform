<template>
  <div class="register-container">
    <div class="brand-section">
      <div class="brand-content">
        <h1>开启智能简历优化之旅</h1>
        <p class="brand-subtitle">AI赋能，让每一份简历都脱颖而出</p>
        <div class="features">
          <div class="feature-item">
            <span class="feature-icon">🤖</span>
            <span>AI智能优化，提升简历竞争力</span>
          </div>
          <div class="feature-item">
            <span class="feature-icon">📄</span>
            <span>支持PDF/Word格式，一键上传</span>
          </div>
          <div class="feature-item">
            <span class="feature-icon">💾</span>
            <span>历史记录保存，随时查看对比</span>
          </div>
        </div>
      </div>
    </div>
    <div class="form-section">
      <el-card class="register-card">
        <h2>注册账号</h2>
        <el-form :model="form" :rules="rules" ref="formRef">
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="用户名" prefix-icon="User" />
          </el-form-item>
          <el-form-item prop="email">
            <el-input v-model="form.email" placeholder="邮箱" prefix-icon="Message" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="form.password" type="password" placeholder="密码" prefix-icon="Lock" />
          </el-form-item>
          <el-form-item prop="confirmPassword">
            <el-input v-model="form.confirmPassword" type="password" placeholder="确认密码" prefix-icon="Lock" />
          </el-form-item>
        </el-form>
        <el-button type="primary" @click="handleRegister" :loading="loading" style="width: 100%">注册</el-button>
        <p class="login-link">
          已有账号？<router-link to="/login">立即登录</router-link>
        </p>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '../api/auth'

const router = useRouter()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ],
  confirmPassword: [{ required: true, message: '请确认密码', trigger: 'blur' }]
}

const handleRegister = async () => {
  if (form.password !== form.confirmPassword) {
    ElMessage.error('两次密码不一致')
    return
  }

  loading.value = true
  try {
    const res = await register({
      username: form.username,
      email: form.email,
      password: form.password
    })

    if (res.code === 200) {
      ElMessage.success('注册成功，请登录')
      router.push('/login')
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    ElMessage.error('注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-container {
  height: 100vh;
  display: flex;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  background-size: 200% 200%;
  animation: gradientAnimation 15s ease infinite;
  position: relative;
  overflow: hidden;
}

@keyframes gradientAnimation {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

.register-container::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.1) 1px, transparent 1px);
  background-size: 50px 50px;
  animation: patternMove 20s linear infinite;
}

@keyframes patternMove {
  0% { transform: translate(0, 0); }
  100% { transform: translate(50px, 50px); }
}

.register-container::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: 
    radial-gradient(circle at 20% 30%, rgba(255, 255, 255, 0.15) 0%, transparent 50%),
    radial-gradient(circle at 80% 70%, rgba(255, 255, 255, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 40% 80%, rgba(255, 255, 255, 0.08) 0%, transparent 50%);
  animation: float 15s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.brand-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 40px;
  color: white;
  z-index: 1;
}

.brand-content {
  max-width: 500px;
  animation: brandEnter 1s ease-out 0.3s both;
}

@keyframes brandEnter {
  from {
    opacity: 0;
    transform: translateX(-30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.brand-content h1 {
  font-size: 36px;
  font-weight: 700;
  margin-bottom: 16px;
  line-height: 1.2;
}

.brand-subtitle {
  font-size: 18px;
  margin-bottom: 40px;
  opacity: 0.9;
  line-height: 1.5;
}

.features {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 16px;
  animation: featureEnter 1s ease-out both;
}

.feature-item:nth-child(1) {
  animation-delay: 0.5s;
}

.feature-item:nth-child(2) {
  animation-delay: 0.7s;
}

.feature-item:nth-child(3) {
  animation-delay: 0.9s;
}

@keyframes featureEnter {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.feature-icon {
  font-size: 24px;
}

.form-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  z-index: 1;
}

.register-card {
  width: 400px;
  padding: 30px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
  animation: cardEnter 0.8s ease-out 0.15s both;
}

@keyframes cardEnter {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.register-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

.register-card h2 {
  text-align: center;
  margin-bottom: 30px;
  color: #303133;
  font-weight: 700;
  font-size: 24px;
}

.login-link {
  text-align: center;
  margin-top: 20px;
  color: #666;
}

.login-link a {
  color: #667eea;
  transition: color 0.3s ease;
}

.login-link a:hover {
  color: #764ba2;
}

/* 表单样式 */
.el-form {
  margin-bottom: 20px;
}

.el-form-item {
  margin-bottom: 20px;
}

.el-input {
  border-radius: 8px;
  background: rgba(240, 242, 245, 0.8);
  transition: all 0.3s ease;
}

.el-input:focus-within {
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.4);
  border-color: #667eea;
}

.el-input__wrapper {
  border-radius: 8px;
}

/* 按钮样式 */
.el-button--primary {
  width: 100%;
  height: 44px;
  border-radius: 8px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
  font-size: 16px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.el-button--primary:hover {
  background: linear-gradient(135deg, #764ba2, #667eea);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.el-button--primary:active {
  transform: scale(0.98);
}

/* 输入框聚焦样式 */
.el-input__wrapper.is-focus {
  box-shadow: 0 0 0 1px #667eea inset;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .register-container {
    flex-direction: column;
  }
  
  .brand-section {
    display: none;
  }
  
  .form-section {
    flex: 1;
    width: 100%;
    padding: 20px;
  }
  
  .register-card {
    width: 100%;
    max-width: 400px;
  }
}
</style>