<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2>AI 简历优化平台</h2>
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
      router.push('/home')
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
}
.login-card {
  width: 400px;
  padding: 20px;
}
.login-card h2 {
  text-align: center;
  margin-bottom: 20px;
  color: #333;
}
.register-link {
  text-align: center;
  margin-top: 15px;
  color: #666;
}
.register-link a {
  color: #409eff;
}
</style>