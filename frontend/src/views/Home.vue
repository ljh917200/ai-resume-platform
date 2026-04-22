<template>
  <div class="home-container">
    <el-header>
      <h1>AI 简历优化平台</h1>
      <el-button @click="logout" type="danger" plain>退出登录</el-button>
    </el-header>

    <el-main>
      <!-- 上传简历 -->
      <el-card class="upload-card">
        <h3>上传简历</h3>
        <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :on-change="handleFileChange"
            :limit="1"
            accept=".pdf,.docx"
            drag
        >
          <el-icon class="el-icon--upload"><upload-filled /></el-icon>
          <div class="el-upload__text">
            拖拽文件到此处，或<em>点击上传</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">支持 PDF、DOCX 格式，最大 5MB</div>
          </template>
        </el-upload>
        <el-button type="primary" @click="handleUpload" :loading="uploading" style="margin-top: 15px">
          上传简历
        </el-button>
      </el-card>

      <!-- 快速优化文本 -->
      <el-card class="quick-optimize-card">
        <h3>快速优化文本</h3>
        <el-input
            v-model="quickText"
            type="textarea"
            :rows="4"
            placeholder="输入要优化的简历内容..."
        />
        <el-input v-model="targetRole" placeholder="目标岗位（可选）" style="margin-top: 10px" />
        <el-button type="primary" @click="handleQuickOptimize" :loading="optimizing" style="margin-top: 10px">
          优化文本
        </el-button>
      </el-card>

      <!-- 我的简历列表 -->
      <el-card class="resume-list-card">
        <h3>我的简历</h3>
        <el-table :data="resumeList" style="width: 100%">
          <el-table-column prop="fileName" label="文件名" />
          <el-table-column prop="fileFormat" label="格式" width="80" />
          <el-table-column prop="createdAt" label="上传时间" width="180" />
          <el-table-column label="操作" width="200">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="goOptimize(row.id)">优化</el-button>
              <el-button type="danger" size="small" @click="handleDelete(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </el-main>

    <!-- 优化结果弹窗 -->
    <el-dialog v-model="resultDialogVisible" title="优化结果" width="60%">
      <div class="result-container">
        <h4>原始内容：</h4>
        <div class="original-text">{{ resultData.originalText }}</div>
        <h4>优化结果：</h4>
        <div class="optimized-text">{{ resultData.optimizedText }}</div>
      </div>
      <template #footer>
        <el-button @click="copyResult">复制结果</el-button>
        <el-button type="primary" @click="resultDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { uploadResume, getResumeList, deleteResume, optimizeResume } from '../api/resume'
import { optimizeText } from '../api/ai'

const router = useRouter()
const uploadRef = ref()
const resumeList = ref([])
const selectedFile = ref(null)
const uploading = ref(false)
const optimizing = ref(false)
const quickText = ref('')
const targetRole = ref('')
const resultDialogVisible = ref(false)
const resultData = ref({
  originalText: '',
  optimizedText: ''
})

// 获取简历列表
const fetchResumeList = async () => {
  try {
    const res = await getResumeList()
    if (res.code === 200) {
      resumeList.value = res.data
    }
  } catch (error) {
    console.error(error)
  }
}

onMounted(() => {
  fetchResumeList()
})

// 文件选择
const handleFileChange = (file) => {
  selectedFile.value = file.raw
}

// 上传简历
const handleUpload = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择文件')
    return
  }

  uploading.value = true
  try {
    const res = await uploadResume(selectedFile.value)
    if (res.code === 200) {
      ElMessage.success('上传成功')
      fetchResumeList()
      uploadRef.value.clearFiles()
      selectedFile.value = null
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    ElMessage.error('上传失败')
  } finally {
    uploading.value = false
  }
}

// 快速优化
const handleQuickOptimize = async () => {
  if (!quickText.value.trim()) {
    ElMessage.warning('请输入要优化的内容')
    return
  }

  optimizing.value = true
  try {
    const res = await optimizeText(quickText.value, targetRole.value)
    if (res.code === 200) {
      resultData.value = res.data
      resultDialogVisible.value = true
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    ElMessage.error('优化失败')
  } finally {
    optimizing.value = false
  }
}

// 跳转优化页
const goOptimize = (id) => {
  router.push(`/optimize?id=${id}`)
}

// 删除简历
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除该简历？', '提示', { type: 'warning' })
    const res = await deleteResume(id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchResumeList()
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    // 取消删除
  }
}

// 复制结果
const copyResult = () => {
  navigator.clipboard.writeText(resultData.value.optimizedText)
  ElMessage.success('已复制到剪贴板')
}

// 退出登录
const logout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  router.push('/login')
}
</script>

<style scoped>
.home-container {
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
.el-header h1 {
  font-size: 20px;
  color: #333;
}
.el-main {
  max-width: 1000px;
  margin: 20px auto;
}
.upload-card, .quick-optimize-card, .resume-list-card {
  margin-bottom: 20px;
}
.result-container {
  max-height: 400px;
  overflow-y: auto;
}
.original-text {
  background: #f5f5f5;
  padding: 10px;
  border-radius: 4px;
  white-space: pre-wrap;
  margin-bottom: 15px;
}
.optimized-text {
  background: #e8f4ff;
  padding: 10px;
  border-radius: 4px;
  white-space: pre-wrap;
}
</style>