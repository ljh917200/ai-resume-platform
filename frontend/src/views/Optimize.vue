<template>
  <div class="optimize-container">
    <el-header>
      <h1>简历优化</h1>
      <el-button @click="goBack" plain>返回首页</el-button>
    </el-header>

    <el-main>
      <el-card v-if="resume">
        <h3>{{ resume.fileName }}</h3>

        <div class="section">
          <h4>原始内容</h4>
          <div class="content-box">{{ resume.originalText }}</div>
        </div>

        <div class="section">
          <h4>目标岗位（可选）</h4>
          <el-input v-model="targetRole" placeholder="如：前端开发工程师、Java开发工程师" />
        </div>

        <el-button type="primary" @click="handleOptimize" :loading="loading" size="large">
          开始优化
        </el-button>

        <div v-if="optimizedText" class="section">
          <h4>优化结果</h4>
          <div class="optimized-box">{{ optimizedText }}</div>
          <el-button type="success" @click="copyResult" style="margin-top: 10px">
            复制结果
          </el-button>
        </div>
      </el-card>
    </el-main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getResume, optimizeResume } from '../api/resume'

const router = useRouter()
const route = useRoute()
const resume = ref(null)
const targetRole = ref('')
const optimizedText = ref('')
const loading = ref(false)

// 获取简历详情
const fetchResume = async () => {
  const id = route.query.id
  if (!id) {
    ElMessage.error('缺少简历ID')
    router.push('/home')
    return
  }

  try {
    const res = await getResume(id)
    if (res.code === 200) {
      resume.value = res.data
    } else {
      ElMessage.error(res.message)
      router.push('/home')
    }
  } catch (error) {
    ElMessage.error('获取简历失败')
    router.push('/home')
  }
}

// 优化简历
const handleOptimize = async () => {
  loading.value = true
  try {
    const res = await optimizeResume(resume.value.id, targetRole.value)
    if (res.code === 200) {
      optimizedText.value = res.data.optimizedText
      ElMessage.success('优化成功')
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    ElMessage.error('优化失败')
  } finally {
    loading.value = false
  }
}

// 复制结果
const copyResult = () => {
  navigator.clipboard.writeText(optimizedText.value)
  ElMessage.success('已复制到剪贴板')
}

// 返回首页
const goBack = () => {
  router.push('/home')
}

onMounted(() => {
  fetchResume()
})
</script>

<style scoped>
.optimize-container {
  min-height: 100vh;
  background: #f5f5f5;
}
.el-header {
  background: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 30px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}
.el-main {
  max-width: 900px;
  margin: 20px auto;
}
.section {
  margin: 20px 0;
}
.content-box {
  background: #f5f5f5;
  padding: 15px;
  border-radius: 8px;
  white-space: pre-wrap;
  max-height: 300px;
  overflow-y: auto;
}
.optimized-box {
  background: #e8f4ff;
  padding: 15px;
  border-radius: 8px;
  white-space: pre-wrap;
  max-height: 400px;
  overflow-y: auto;
}
</style>