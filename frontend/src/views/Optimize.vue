<template>
  <div class="optimize-container">
    <el-header class="top-nav">
      <div class="logo" @click="goBack">
        <el-icon class="logo-icon"><i class="el-icon-arrow-left"></i></el-icon>
        <span>简历优化</span>
      </div>
      <div class="nav-actions">
        <el-button @click="goBack" plain>返回首页</el-button>
      </div>
    </el-header>

    <el-main>
      <div v-if="resume" class="optimize-content">
        <div class="resume-header-info">
          <h2>{{ resume.fileName }}</h2>
          <div v-if="targetRole" class="target-role-tag">
            <el-icon><i class="el-icon-briefcase"></i></el-icon>
            <span>目标岗位：{{ targetRole }}</span>
          </div>
        </div>

        <div class="section">
          <h4>目标岗位（可选）</h4>
          <el-input v-model="targetRole" placeholder="如：前端开发工程师、Java开发工程师" />
        </div>

        <el-button type="primary" @click="handleOptimize" :loading="loading" size="large" class="optimize-button">
          <el-icon v-if="!loading"><i class="el-icon-magic-stick"></i></el-icon>
          {{ loading ? 'AI优化中...' : '开始优化' }}
        </el-button>

        <div v-if="optimizedText" class="comparison-section">
          <div class="comparison-header">
            <h3>优化对比</h3>
          </div>
          <div class="comparison-container">
            <div class="comparison-panel original">
              <div class="panel-header">
                <h4>原文内容</h4>
                <el-button size="small" @click="copyOriginal">
                  <el-icon><i class="el-icon-document-copy"></i></el-icon>
                  复制
                </el-button>
              </div>
              <div class="panel-content">{{ resume.originalText }}</div>
            </div>

            <div class="comparison-arrow">
              <div class="arrow-icon">
                <el-icon><i class="el-icon-d-arrow-right"></i></el-icon>
              </div>
              <div class="arrow-text">AI优化</div>
            </div>

            <div class="comparison-panel optimized">
              <div class="panel-header">
                <h4>优化结果</h4>
                <el-button type="primary" size="small" @click="copyResult">
                  <el-icon><i class="el-icon-document-copy"></i></el-icon>
                  复制
                </el-button>
              </div>
              <div class="panel-content">{{ optimizedText }}</div>
            </div>
          </div>
        </div>

        <div v-if="optimizedText" class="action-buttons">
          <el-button @click="goBack" size="large">
            <el-icon><i class="el-icon-house"></i></el-icon>
            返回首页
          </el-button>
          <el-button @click="handleExport('original')" size="large">
            <el-icon><i class="el-icon-download"></i></el-icon>
            导出原始
          </el-button>
          <el-button type="primary" @click="handleExport('optimized')" size="large">
            <el-icon><i class="el-icon-download"></i></el-icon>
            导出优化版
          </el-button>
          <el-button @click="handleOptimize" :loading="loading" size="large">
            <el-icon v-if="!loading"><i class="el-icon-refresh"></i></el-icon>
            重新优化
          </el-button>
        </div>

      </div>
    </el-main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getResume, optimizeResume, exportResume } from '../api/resume'

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

// 复制原文
const copyOriginal = () => {
  navigator.clipboard.writeText(resume.value.originalText)
  ElMessage.success('已复制到剪贴板')
}

// 导出PDF
const handleExport = async (type) => {
  try {
    // 1. 调用导出接口，返回二进制数据（blob）
    const res = await exportResume(resume.value.id, type)

    // 2. 创建一个 Blob 对象（二进制大对象），用来存储文件数据
    // Blob 是浏览器处理二进制数据的方式
    const blob = new Blob([res])

    // 3. 为 Blob 创建一个临时的 URL 地址
    // 例如：blob:http://localhost:5173/xxxx-xxxx-xxxx
    const url = window.URL.createObjectURL(blob)

    // 4. 创建一个隐藏的 <a> 标签
    const link = document.createElement('a')

    // 5. 设置下载链接
    link.href = url

    // 6. 设置下载的文件名
    // 如果是 original 类型，文件名为 "简历_原始.pdf"
    // 如果是 optimized 类型，文件名为 "简历_优化.pdf"
    link.setAttribute('download', `简历_${type === 'original' ? '原始' : '优化'}.pdf`)

    // 7. 把 <a> 标签添加到页面中（必须添加才能触发点击）
    document.body.appendChild(link)

    // 8. 触发点击事件，开始下载
    link.click()

    // 9. 下载完成后，移除这个 <a> 标签
    document.body.removeChild(link)

    // 10. 释放临时 URL，释放内存
    window.URL.revokeObjectURL(url)

    // 11. 提示用户导出成功
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
  }
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
  background: linear-gradient(135deg, #f8f9ff 0%, #e8f0ff 100%);
}

.top-nav {
  background: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 30px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: opacity 0.3s ease;
}

.logo:hover {
  opacity: 0.7;
}

.logo-icon {
  font-size: 20px;
  color: #667eea;
}

.logo span {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.nav-actions {
  display: flex;
  gap: 12px;
}

.el-main {
  max-width: 1200px;
  margin: 0 auto;
  padding: 30px;
}

.optimize-content {
  animation: fadeIn 0.5s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.resume-header-info {
  background: #fff;
  border-radius: 16px;
  padding: 30px;
  margin-bottom: 30px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.resume-header-info h2 {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 16px;
}

.target-role-tag {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 14px;
}

.section {
  background: #fff;
  border-radius: 16px;
  padding: 30px;
  margin-bottom: 20px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.section h4 {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 16px;
}

.optimize-button {
  width: 100%;
  height: 56px;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
  font-size: 18px;
  font-weight: 600;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.optimize-button:hover {
  background: linear-gradient(135deg, #764ba2, #667eea);
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.4);
}

.optimize-button:active {
  transform: scale(0.98);
}

.comparison-section {
  margin-top: 30px;
}

.comparison-header {
  margin-bottom: 20px;
}

.comparison-header h3 {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.comparison-container {
  display: flex;
  align-items: stretch;
  gap: 20px;
}

.comparison-panel {
  flex: 1;
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  display: flex;
  flex-direction: column;
}

.comparison-panel.original {
  border: 2px solid #e4e7ed;
}

.comparison-panel.optimized {
  border: 2px solid #667eea;
  background: linear-gradient(135deg, #f8f9ff 0%, #fff 100%);
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.panel-header h4 {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.panel-content {
  flex: 1;
  background: #fafafa;
  padding: 16px;
  border-radius: 8px;
  white-space: pre-wrap;
  max-height: 350px;
  overflow-y: auto;
  line-height: 1.8;
  color: #606266;
}

.comparison-arrow {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 0 10px;
}

.arrow-icon {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 24px;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.4);
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
    box-shadow: 0 4px 16px rgba(102, 126, 234, 0.4);
  }
  50% {
    transform: scale(1.1);
    box-shadow: 0 8px 24px rgba(102, 126, 234, 0.6);
  }
}

.arrow-text {
  margin-top: 12px;
  font-size: 14px;
  font-weight: 600;
  color: #667eea;
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 30px;
}

.action-buttons .el-button {
  padding: 20px 40px;
  border-radius: 12px;
  font-size: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s ease;
}

.action-buttons .el-button:first-child {
  background: #fff;
  border: 2px solid #e4e7ed;
  color: #606266;
}

.action-buttons .el-button:first-child:hover {
  border-color: #667eea;
  color: #667eea;
}

.action-buttons .el-button:last-child {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
  color: #fff;
}

.action-buttons .el-button:last-child:hover {
  background: linear-gradient(135deg, #764ba2, #667eea);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .top-nav {
    padding: 0 20px;
  }
  
  .el-main {
    padding: 20px;
  }
  
  .comparison-container {
    flex-direction: column;
  }
  
  .comparison-arrow {
    flex-direction: row;
    padding: 10px 0;
  }
  
  .arrow-icon {
    width: 48px;
    height: 48px;
    font-size: 20px;
  }
  
  .arrow-text {
    margin-top: 0;
    margin-left: 12px;
  }
  
  .action-buttons {
    flex-direction: column;
  }
  
  .action-buttons .el-button {
    width: 100%;
  }
  
  .resume-header-info,
  .section {
    padding: 20px;
  }
}
</style>