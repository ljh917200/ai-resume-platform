<template>
  <div class="detail-container">
    <!-- 顶部导航栏 -->
    <el-header class="top-nav">
      <div class="logo" @click="goHome">
        <el-icon class="logo-icon"><i class="el-icon-s-operation"></i></el-icon>
        <span>AI简历优化</span>
      </div>
      <el-button type="primary" @click="goHome">返回首页</el-button>
    </el-header>

    <el-main class="main-content">
      <!-- 加载中 -->
      <div v-if="loading" class="loading-container">
        <el-icon class="is-loading" :size="40"><Loading /></el-icon>
        <p>加载中...</p>
      </div>

      <!-- 简历内容 -->
      <div v-else-if="resumeData" class="resume-wrapper">
        <!-- 操作栏 -->
        <div class="action-bar">
          <div class="file-info">
            <el-tag :type="resumeData.fileFormat === 'PDF' ? 'danger' : 'primary'">
              {{ resumeData.fileFormat }}
            </el-tag>
            <span class="file-name">{{ resumeData.fileName }}</span>
          </div>
          <div class="action-buttons">
            <el-button type="primary" @click="goOptimize">优化简历</el-button>
            <el-button type="info" @click="exportPDF('original')">导出原始PDF</el-button>
            <el-button type="success" @click="exportPDF('optimized')" :disabled="!resumeData.optimizedText">
              导出优化版PDF
            </el-button>
          </div>
        </div>

        <!-- 结构化简历展示 -->
        <div v-if="structuredData" class="resume-preview">
          <!-- 左侧栏：个人信息 -->
          <div class="left-sidebar">
            <div class="avatar-section">
              <el-avatar :size="100" class="avatar">
                {{ structuredData.name ? structuredData.name.charAt(0).toUpperCase() : 'U' }}
              </el-avatar>
              <h2 class="name">{{ structuredData.name || '未填写姓名' }}</h2>
            </div>

            <!-- 联系方式 -->
            <div class="info-section" v-if="structuredData.phone || structuredData.email">
              <h3 class="section-title">联系方式</h3>
              <div class="info-item" v-if="structuredData.phone">
                <el-icon><Phone /></el-icon>
                <span>{{ structuredData.phone }}</span>
              </div>
              <div class="info-item" v-if="structuredData.email">
                <el-icon><Message /></el-icon>
                <span>{{ structuredData.email }}</span>
              </div>
            </div>

            <!-- 技能 -->
            <div class="info-section" v-if="structuredData.skills && structuredData.skills.length > 0">
              <h3 class="section-title">专业技能</h3>
              <div class="skill-tags">
                <el-tag v-for="skill in structuredData.skills" :key="skill" class="skill-tag">
                  {{ skill }}
                </el-tag>
              </div>
            </div>
          </div>

          <!-- 右侧主体内容 -->
          <div class="right-content">
            <!-- 教育经历 -->
            <div class="content-section" v-if="structuredData.education && structuredData.education.length > 0">
              <h3 class="section-title">
                <el-icon><School /></el-icon>
                教育经历
              </h3>
              <div class="section-content">
                <div v-for="(edu, index) in structuredData.education" :key="index" class="item-card">
                  <div class="item-header">
                    <h4 class="item-title">{{ edu.school }}</h4>
                    <span class="item-period">{{ edu.period }}</span>
                  </div>
                  <div class="item-subtitle">{{ edu.major }} · {{ edu.degree }}</div>
                </div>
              </div>
            </div>

            <!-- 工作/实习经历 -->
            <div class="content-section" v-if="structuredData.experience && structuredData.experience.length > 0">
              <h3 class="section-title">
                <el-icon><Briefcase /></el-icon>
                工作/实习经历
              </h3>
              <div class="section-content">
                <div v-for="(exp, index) in structuredData.experience" :key="index" class="item-card">
                  <div class="item-header">
                    <h4 class="item-title">{{ exp.company }}</h4>
                    <span class="item-period">{{ exp.period }}</span>
                  </div>
                  <div class="item-subtitle">{{ exp.position }}</div>
                  <div class="item-description">{{ exp.description }}</div>
                </div>
              </div>
            </div>

            <!-- 项目经历 -->
            <div class="content-section" v-if="structuredData.projects && structuredData.projects.length > 0">
              <h3 class="section-title">
                <el-icon><Folder /></el-icon>
                项目经历
              </h3>
              <div class="section-content">
                <div v-for="(proj, index) in structuredData.projects" :key="index" class="item-card">
                  <div class="item-header">
                    <h4 class="item-title">{{ proj.name }}</h4>
                    <span class="item-period">{{ proj.period || '' }}</span>
                  </div>
                  <div class="item-subtitle">{{ proj.role }}</div>
                  <div class="item-description">{{ proj.description }}</div>
                </div>
              </div>
            </div>

            <!-- 原始文本（无结构化数据时显示） -->
            <div class="content-section" v-if="!hasStructuredData && resumeData.originalText">
              <h3 class="section-title">
                <el-icon><Document /></el-icon>
                简历内容
              </h3>
              <div class="raw-text">{{ resumeData.originalText }}</div>
            </div>
          </div>
        </div>

        <!-- 无结构化数据时的提示 -->
        <div v-else class="no-structured-data">
          <el-empty description="暂无结构化数据">
            <el-button type="primary" @click="restructure">重新解析</el-button>
          </el-empty>
          <div class="raw-text-section" v-if="resumeData.originalText">
            <h4>原始文本</h4>
            <div class="raw-text">{{ resumeData.originalText }}</div>
          </div>
        </div>
      </div>

      <!-- 错误状态 -->
      <div v-else class="error-container">
        <el-empty description="简历不存在或加载失败">
          <el-button type="primary" @click="goHome">返回首页</el-button>
        </el-empty>
      </div>
    </el-main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading, Phone, Message, School, Briefcase, Folder, Document } from '@element-plus/icons-vue'
import { getResume, exportResume } from '../api/resume'

const route = useRoute()
const router = useRouter()

// ============================================
// 数据定义
// ============================================
const loading = ref(true)
const resumeData = ref(null)
const structuredData = ref(null)

// 计算属性：是否有结构化数据
const hasStructuredData = computed(() => {
  return structuredData.value &&
      (structuredData.value.name ||
          structuredData.value.education?.length > 0 ||
          structuredData.value.experience?.length > 0 ||
          structuredData.value.projects?.length > 0 ||
          structuredData.value.skills?.length > 0)
})

// ============================================
// 生命周期
// ============================================
onMounted(() => {
  loadResumeDetail()
})

// ============================================
// 方法定义
// ============================================

/**
 * 加载简历详情
 */
const loadResumeDetail = async () => {
  try {
    loading.value = true
    const res = await getResume(route.params.id)

    // 判断返回格式：可能是 { code, data } 或直接是数据
    if (res.code === 200 && res.data) {
      resumeData.value = res.data
    } else {
      resumeData.value = res
    }

    // 解析结构化数据
    if (resumeData.value.structuredData) {
      try {
        structuredData.value = typeof resumeData.value.structuredData === 'string'
            ? JSON.parse(resumeData.value.structuredData)
            : resumeData.value.structuredData
      } catch (e) {
        console.error('解析结构化数据失败:', e)
        structuredData.value = null
      }
    }
  } catch (error) {
    ElMessage.error('加载简历失败：' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

/**
 * 跳转首页
 */
const goHome = () => {
  router.push('/home')
}

/**
 * 跳转优化页面
 */
const goOptimize = () => {
  router.push(`/optimize?id=${resumeData.value.id}`)
}

/**
 * 导出PDF
 * @param {string} type - 导出类型：original/optimized
 */
const exportPDF = async (type) => {
  try {
    const res = await exportResume(resumeData.value.id, type)
    // 创建下载链接
    const url = window.URL.createObjectURL(new Blob([res]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `简历_${type === 'original' ? '原始' : '优化'}.pdf`)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败：' + (error.message || '未知错误'))
  }
}

/**
 * 重新解析（提示用户重新上传）
 */
const restructure = () => {
  ElMessage.warning('请重新上传简历以获取结构化数据')
}
</script>

<style scoped>
/* ============================================
   整体布局
   ============================================ */
.detail-container {
  min-height: 100vh;
  background: #f5f7fa;
}

.top-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 0 24px;
  height: 60px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  color: white;
  font-size: 18px;
  font-weight: 600;
  cursor: pointer;
}

.logo-icon {
  font-size: 24px;
}

.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

/* ============================================
   操作栏
   ============================================ */
.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: white;
  padding: 16px 24px;
  border-radius: 8px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.file-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.file-name {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.action-buttons {
  display: flex;
  gap: 12px;
}

/* ============================================
   简历预览区域
   ============================================ */
.resume-preview {
  display: flex;
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

/* 左侧栏 */
.left-sidebar {
  width: 280px;
  background: linear-gradient(180deg, #667eea 0%, #764ba2 100%);
  padding: 32px 24px;
  color: white;
}

.avatar-section {
  text-align: center;
  margin-bottom: 32px;
}

.avatar {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  font-size: 36px;
  font-weight: 600;
  margin-bottom: 16px;
}

.name {
  font-size: 24px;
  font-weight: 600;
  margin: 0;
}

.info-section {
  margin-bottom: 24px;
}

.info-section .section-title {
  font-size: 14px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  font-size: 14px;
}

.skill-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.skill-tag {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
}

/* 右侧内容 */
.right-content {
  flex: 1;
  padding: 32px;
}

.content-section {
  margin-bottom: 32px;
}

.content-section .section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #667eea;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 2px solid #667eea;
}

.section-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.item-card {
  background: #f8f9fa;
  padding: 16px;
  border-radius: 8px;
  border-left: 3px solid #667eea;
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
}

.item-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.item-period {
  font-size: 14px;
  color: #909399;
}

.item-subtitle {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

.item-description {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  white-space: pre-wrap;
}

/* ============================================
   原始文本显示
   ============================================ */
.raw-text-section {
  margin-top: 24px;
  padding: 24px;
  background: white;
  border-radius: 8px;
}

.raw-text-section h4 {
  margin-bottom: 16px;
  color: #303133;
}

.raw-text {
  white-space: pre-wrap;
  line-height: 1.8;
  color: #606266;
  background: #f8f9fa;
  padding: 16px;
  border-radius: 8px;
  font-size: 14px;
}

/* ============================================
   状态提示
   ============================================ */
.loading-container,
.error-container,
.no-structured-data {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  background: white;
  border-radius: 8px;
}

.loading-container p {
  margin-top: 16px;
  color: #909399;
}
</style>