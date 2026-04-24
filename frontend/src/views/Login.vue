<template>
  <div class="login-container">
    <el-card class="login-card">
      <div class="title-container">
        <el-icon class="title-icon"><i class="el-icon-s-operation"></i></el-icon>
        <h2>AI 简历优化平台</h2>
      </div>
      <p class="subtitle">让简历更具竞争力</p>
      <el-tabs v-model="loginType">
        <el-tab-pane label="用户名登录" name="username">
          <el-form :model="form" :rules="rules" ref="formRef">
            <el-form-item prop="username">
              <el-input v-model="form.username" placeholder="用户名" prefix-icon="User" />
            </el-form-item>
            <el-form-item prop="password">
              <el-input v-model="form.password" type="password" placeholder="密码" prefix-icon="Lock" />
            </el-form-item>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="邮箱登录" name="email">
          <el-form :model="form" :rules="rules" ref="formRef">
            <el-form-item prop="email">
              <el-input v-model="form.email" placeholder="邮箱" prefix-icon="Message" />
            </el-form-item>
            <el-form-item prop="password">
              <el-input v-model="form.password" type="password" placeholder="密码" prefix-icon="Lock" />
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
      <el-button type="primary" @click="handleLogin" :loading="loading" style="width: 100%">登录</el-button>
      <p class="register-link">
        还没有账号？<router-link to="/register">立即注册</router-link>
      </p>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { loginByUsername, loginByEmail } from '../api/auth'

const router = useRouter()
const formRef = ref()
const loginType = ref('username')
const loading = ref(false)

const form = reactive({
  username: '',
  email: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  email: [{ required: true, message: '请输入邮箱', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  if (!form.password) {
    ElMessage.error('请输入密码')
    return
  }

  loading.value = true
  try {
    let res
    if (loginType.value === 'username') {
      if (!form.username) {
        ElMessage.error('请输入用户名')
        return
      }
      res = await loginByUsername(form.username, form.password)
    } else {
      if (!form.email) {
        ElMessage.error('请输入邮箱')
        return
      }
      res = await loginByEmail(form.email, form.password)
    }

    if (res.code === 200) {
      localStorage.setItem('token', res.data.token)
      ElMessage.success('登录成功')
      await router.push('/home')
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    ElMessage.error('登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
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

.login-container::before {
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

.login-container::after {
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

.login-card {
  width: 400px;
  padding: 30px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
  z-index: 1;
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

.login-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

.title-container {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 10px;
}

.title-icon {
  font-size: 28px;
  color: #667eea;
  margin-right: 12px;
}

.login-card h2 {
  text-align: center;
  margin-bottom: 10px;
  color: #303133;
  font-weight: 700;
  font-size: 24px;
  margin: 0;
}

.login-card .subtitle {
  text-align: center;
  margin-bottom: 30px;
  color: #909399;
  font-size: 14px;
}

.register-link {
  text-align: center;
  margin-top: 20px;
  color: #666;
}

.register-link a {
  color: #667eea;
  transition: color 0.3s ease;
}

.register-link a:hover {
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
</style>