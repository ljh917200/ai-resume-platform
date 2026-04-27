<template>
  <div class="optimize-container">
    <!-- 顶部导航栏 -->
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
      <!-- 加载中 -->
      <div v-if="loadingResume" class="loading-container">
        <el-icon class="is-loading" :size="40"><Loading /></el-icon>
        <p>加载中...</p>
      </div>

      <!-- 简历内容 -->
      <div v-else-if="resume" class="optimize-content">
        <!-- 操作栏 -->
        <div class="action-bar">
          <div class="file-info">
            <el-tag :type="resume.fileFormat === 'PDF' ? 'danger' : 'primary'">
              {{ resume.fileFormat }}
            </el-tag>
            <span class="file-name">{{ resume.fileName }}</span>
          </div>
          <div class="optimize-section">
            <el-input
                v-model="targetRole"
                placeholder="目标岗位（可选）"
                style="width: 200px; margin-right: 12px;"
            />
            <el-button
                type="primary"
                @click="handleOptimize"
                :loading="optimizing"
            >
              <el-icon v-if="!optimizing"><i class="el-icon-magic-stick"></i></el-icon>
              {{ optimizing ? 'AI优化中...' : '开始优化' }}
            </el-button>
          </div>
        </div>

        <!-- 简历对比区域 -->
        <div class="resume-comparison">
          <!-- 左侧：原始简历 -->
          <div class="resume-panel original">
            <div class="panel-header">
              <h3>原始简历</h3>
              <el-button size="small" @click="copyOriginal">复制原文</el-button>
            </div>

            <!-- 结构化展示 -->
            <div v-if="structuredData && hasContent" class="structured-resume">
              <!-- 基本信息 -->
              <div class="section basic-info" v-if="structuredData.name || structuredData.phone || structuredData.email">
                <div class="avatar">{{ structuredData.name ? structuredData.name.charAt(0) : 'U' }}</div>
                <div class="info">
                  <h2 class="name">{{ structuredData.name || '未填写姓名' }}</h2>
                  <div class="contact">
                    <span v-if="structuredData.phone">{{ structuredData.phone }}</span>
                    <span v-if="structuredData.email">{{ structuredData.email }}</span>
                  </div>
                </div>
              </div>

              <!-- 教育经历 -->
              <div class="section" v-if="structuredData.education && structuredData.education.length > 0">
                <h4 class="section-title">教育经历</h4>
                <div class="section-content">
                  <div v-for="(edu, index) in structuredData.education" :key="index" class="item">
                    <div class="item-header">
                      <span class="item-title">{{ edu.school }}</span>
                      <span class="item-period">{{ edu.period }}</span>
                    </div>
                    <div class="item-subtitle">{{ edu.major }} · {{ edu.degree }}</div>
                    <div class="item-extra" v-if="edu.gpa">GPA: {{ edu.gpa }}</div>
                  </div>
                </div>
              </div>

              <!-- 工作经历 -->
              <div class="section" v-if="structuredData.experience && structuredData.experience.length > 0">
                <h4 class="section-title">工作/实习经历</h4>
                <div class="section-content">
                  <div v-for="(exp, index) in structuredData.experience" :key="index" class="item">
                    <div class="item-header">
                      <span class="item-title">{{ exp.company }}</span>
                      <span class="item-period">{{ exp.period }}</span>
                    </div>
                    <div class="item-subtitle">{{ exp.position }}</div>
                    <div class="item-desc">{{ exp.description }}</div>
                  </div>
                </div>
              </div>

              <!-- 项目经历 -->
              <div class="section" v-if="structuredData.projects && structuredData.projects.length > 0">
                <h4 class="section-title">项目经历</h4>
                <div class="section-content">
                  <div v-for="(proj, index) in structuredData.projects" :key="index" class="item">
                    <div class="item-header">
                      <span class="item-title">{{ proj.name }}</span>
                      <span class="item-period">{{ proj.period }}</span>
                    </div>
                    <div class="item-subtitle">{{ proj.role }}</div>
                    <div class="item-desc">{{ proj.description }}</div>
                  </div>
                </div>
              </div>

              <!-- 技能 -->
              <div class="section" v-if="structuredData.skills && structuredData.skills.length > 0">
                <h4 class="section-title">专业技能</h4>
                <div class="skill-tags">
                  <el-tag v-for="skill in structuredData.skills" :key="skill" size="small">
                    {{ skill }}
                  </el-tag>
                </div>
              </div>

              <!-- 获奖经历 -->
              <div class="section" v-if="structuredData.awards && structuredData.awards.length > 0">
                <h4 class="section-title">获奖经历</h4>
                <div class="section-content">
                  <div v-for="(award, index) in structuredData.awards" :key="index" class="item">
                    <div class="item-header">
                      <span class="item-title">{{ award.name }}</span>
                      <span class="item-period">{{ award.year }}</span>
                    </div>
                    <div class="item-subtitle">{{ award.level }}</div>
                  </div>
                </div>
              </div>

              <!-- 比赛经历 -->
              <div class="section" v-if="structuredData.competitions && structuredData.competitions.length > 0">
                <h4 class="section-title">比赛经历</h4>
                <div class="section-content">
                  <div v-for="(comp, index) in structuredData.competitions" :key="index" class="item">
                    <div class="item-header">
                      <span class="item-title">{{ comp.name }}</span>
                      <span class="item-period">{{ comp.year }}</span>
                    </div>
                    <div class="item-subtitle">{{ comp.result }}</div>
                  </div>
                </div>
              </div>

              <!-- 证书资质 -->
              <div class="section" v-if="structuredData.certifications && structuredData.certifications.length > 0">
                <h4 class="section-title">证书资质</h4>
                <div class="skill-tags">
                  <el-tag v-for="cert in structuredData.certifications" :key="cert" size="small" type="success">
                    {{ cert }}
                  </el-tag>
                </div>
              </div>

              <!-- 自我评价 -->
              <div class="section" v-if="structuredData.selfEvaluation">
                <h4 class="section-title">自我评价</h4>
                <div class="item-desc">{{ structuredData.selfEvaluation }}</div>
              </div>
            </div>

            <!-- 无结构化数据时显示原文 -->
            <div v-else class="raw-text">
              {{ resume.originalText }}
            </div>
          </div>

          <!-- 中间：优化箭头 -->
          <div class="comparison-arrow">
            <el-icon :size="32"><i class="el-icon-d-arrow-right"></i></el-icon>
            <div class="arrow-label">AI优化</div>
          </div>

          <!-- 右侧：优化结果 -->
          <div class="resume-panel optimized">
            <div class="panel-header">
              <h3>优化结果</h3>
              <el-button
                  size="small"
                  type="primary"
                  @click="copyOptimized"
                  :disabled="!optimizedText"
              >
                复制结果
              </el-button>
            </div>

            <!-- 结构化展示优化后内容 -->
            <div v-if="optimizedStructuredData && hasOptimizedContent" class="structured-resume optimized">
              <!-- 基本信息 -->
              <div class="section basic-info" v-if="optimizedStructuredData.name || optimizedStructuredData.phone || optimizedStructuredData.email">
                <div class="avatar">{{ optimizedStructuredData.name ? optimizedStructuredData.name.charAt(0) : 'U' }}</div>
                <div class="info">
                  <h2 class="name">{{ optimizedStructuredData.name || '未填写姓名' }}</h2>
                  <div class="contact">
                    <span v-if="optimizedStructuredData.phone">{{ optimizedStructuredData.phone }}</span>
                    <span v-if="optimizedStructuredData.email">{{ optimizedStructuredData.email }}</span>
                  </div>
                </div>
              </div>

              <!-- 教育经历 -->
              <div class="section" v-if="optimizedStructuredData.education && optimizedStructuredData.education.length > 0">
                <h4 class="section-title">教育经历</h4>
                <div class="section-content">
                  <div v-for="(edu, index) in optimizedStructuredData.education" :key="index" class="item">
                    <div class="item-header">
                      <span class="item-title">{{ edu.school }}</span>
                      <span class="item-period">{{ edu.period }}</span>
                    </div>
                    <div class="item-subtitle">{{ edu.major }} · {{ edu.degree }}</div>
                    <div class="item-extra" v-if="edu.gpa">GPA: {{ edu.gpa }}</div>
                  </div>
                </div>
              </div>

              <!-- 工作经历 -->
              <div class="section" v-if="optimizedStructuredData.experience && optimizedStructuredData.experience.length > 0">
                <h4 class="section-title">工作/实习经历</h4>
                <div class="section-content">
                  <div v-for="(exp, index) in optimizedStructuredData.experience" :key="index" class="item">
                    <div class="item-header">
                      <span class="item-title">{{ exp.company }}</span>
                      <span class="item-period">{{ exp.period }}</span>
                    </div>
                    <div class="item-subtitle">{{ exp.position }}</div>
                    <div class="item-desc">{{ exp.description }}</div>
                  </div>
                </div>
              </div>

              <!-- 项目经历 -->
              <div class="section" v-if="optimizedStructuredData.projects && optimizedStructuredData.projects.length > 0">
                <h4 class="section-title">项目经历</h4>
                <div class="section-content">
                  <div v-for="(proj, index) in optimizedStructuredData.projects" :key="index" class="item">
                    <div class="item-header">
                      <span class="item-title">{{ proj.name }}</span>
                      <span class="item-period">{{ proj.period }}</span>
                    </div>
                    <div class="item-subtitle">{{ proj.role }}</div>
                    <div class="item-desc">{{ proj.description }}</div>
                  </div>
                </div>
              </div>

              <!-- 技能 -->
              <div class="section" v-if="optimizedStructuredData.skills && optimizedStructuredData.skills.length > 0">
                <h4 class="section-title">专业技能</h4>
                <div class="skill-tags">
                  <el-tag v-for="skill in optimizedStructuredData.skills" :key="skill" size="small">
                    {{ skill }}
                  </el-tag>
                </div>
              </div>

              <!-- 获奖经历 -->
              <div class="section" v-if="optimizedStructuredData.awards && optimizedStructuredData.awards.length > 0">
                <h4 class="section-title">获奖经历</h4>
                <div class="section-content">
                  <div v-for="(award, index) in optimizedStructuredData.awards" :key="index" class="item">
                    <div class="item-header">
                      <span class="item-title">{{ award.name }}</span>
                      <span class="item-period">{{ award.year }}</span>
                    </div>
                    <div class="item-subtitle">{{ award.level }}</div>
                  </div>
                </div>
              </div>

              <!-- 比赛经历 -->
              <div class="section" v-if="optimizedStructuredData.competitions && optimizedStructuredData.competitions.length > 0">
                <h4 class="section-title">比赛经历</h4>
                <div class="section-content">
                  <div v-for="(comp, index) in optimizedStructuredData.competitions" :key="index" class="item">
                    <div class="item-header">
                      <span class="item-title">{{ comp.name }}</span>
                      <span class="item-period">{{ comp.year }}</span>
                    </div>
                    <div class="item-subtitle">{{ comp.result }}</div>
                  </div>
                </div>
              </div>

              <!-- 证书资质 -->
              <div class="section" v-if="optimizedStructuredData.certifications && optimizedStructuredData.certifications.length > 0">
                <h4 class="section-title">证书资质</h4>
                <div class="skill-tags">
                  <el-tag v-for="cert in optimizedStructuredData.certifications" :key="cert" size="small" type="success">
                    {{ cert }}
                  </el-tag>
                </div>
              </div>

              <!-- 自我评价 -->
              <div class="section" v-if="optimizedStructuredData.selfEvaluation">
                <h4 class="section-title">自我评价</h4>
                <div class="item-desc">{{ optimizedStructuredData.selfEvaluation }}</div>
              </div>
            </div>

            <!-- 未优化时的提示 -->
            <div v-else class="empty-optimized">
              <el-icon :size="48"><i class="el-icon-edit-outline"></i></el-icon>
              <p>点击"开始优化"按钮</p>
              <p class="sub-text">AI将为您优化简历内容</p>
            </div>
          </div>
        </div>

        <!-- 底部操作按钮 -->
        <div v-if="optimizedText" class="bottom-actions">
          <el-button size="large" @click="handleExport('original')">
            <el-icon><i class="el-icon-download"></i></el-icon>
            导出原始PDF
          </el-button>
          <el-button type="primary" size="large" @click="handleExport('optimized')">
            <el-icon><i class="el-icon-download"></i></el-icon>
            导出优化版PDF
          </el-button>
          <el-button size="large" @click="handleOptimize" :loading="optimizing">
            <el-icon v-if="!optimizing"><i class="el-icon-refresh"></i></el-icon>
            重新优化
          </el-button>
        </div>
      </div>
    </el-main>
  </div>
</template>

<script setup>
import {ref, computed, onMounted} from 'vue'
import {useRouter, useRoute} from 'vue-router'
import {ElMessage} from 'element-plus'
import {Loading} from '@element-plus/icons-vue'
import {getResume, optimizeResume, exportResume} from '../api/resume'

const router = useRouter()
const route = useRoute()

// ============================================
// 数据定义
// ============================================
const resume = ref(null)
const structuredData = ref(null)
const optimizedStructuredData = ref(null)
const targetRole = ref('')
const optimizedText = ref('')
const optimizing = ref(false)
const loadingResume = ref(true)

// 计算属性：是否有结构化内容
const hasContent = computed(() => {
  if (!structuredData.value) return false
  const data = structuredData.value
  return data.name ||
      data.education?.length > 0 ||
      data.experience?.length > 0 ||
      data.projects?.length > 0 ||
      data.skills?.length > 0 ||
      data.awards?.length > 0 ||
      data.competitions?.length > 0 ||
      data.certifications?.length > 0 ||
      data.selfEvaluation
})

// 计算属性：优化后是否有结构化内容
const hasOptimizedContent = computed(() => {
  if (!optimizedStructuredData.value) return false
  const data = optimizedStructuredData.value
  return data.name ||
      data.education?.length > 0 ||
      data.experience?.length > 0 ||
      data.projects?.length > 0 ||
      data.skills?.length > 0 ||
      data.awards?.length > 0 ||
      data.competitions?.length > 0 ||
      data.certifications?.length > 0 ||
      data.selfEvaluation
})

// ============================================
// 生命周期
// ============================================
onMounted(() => {
  fetchResume()
})

// ============================================
// 方法定义
// ============================================

/**
 * 获取简历详情
 */
const fetchResume = async () => {
  const id = route.query.id
  if (!id) {
    ElMessage.error('缺少简历ID')
    router.push('/home')
    return
  }

  try {
    loadingResume.value = true
    const res = await getResume(id)

    // 判断返回格式
    if (res.code === 200 && res.data) {
      resume.value = res.data
    } else {
      resume.value = res
    }

    // 解析结构化数据
    if (resume.value.structuredData) {
      try {
        structuredData.value = typeof resume.value.structuredData === 'string'
            ? JSON.parse(resume.value.structuredData)
            : resume.value.structuredData
      } catch (e) {
        console.error('解析结构化数据失败:', e)
        structuredData.value = null
      }
    }

    // 解析优化后的结构化数据
    if (resume.value.optimizedStructuredData) {
      try {
        optimizedStructuredData.value = typeof resume.value.optimizedStructuredData === 'string'
            ? JSON.parse(resume.value.optimizedStructuredData)
            : resume.value.optimizedStructuredData
      } catch (e) {
        console.error('解析优化后结构化数据失败:', e)
        optimizedStructuredData.value = null
      }
    }

    // 如果已有优化结果，显示
    if (resume.value.optimizedText) {
      optimizedText.value = resume.value.optimizedText
    }

  } catch (error) {
    ElMessage.error('获取简历失败')
    router.push('/home')
  } finally {
    loadingResume.value = false
  }
}

/**
 * 优化简历
 */
const handleOptimize = async () => {
  optimizing.value = true
  try {
    const res = await optimizeResume(resume.value.id, targetRole.value)
    console.log('优化API返回数据:', res)
    if (res.code === 200) {
      optimizedText.value = res.data.optimizedText

      // 解析优化后的结构化数据
      console.log('检查structuredData:', res.data.structuredData)
      if (res.data.structuredData) {
        try {
          optimizedStructuredData.value = typeof res.data.structuredData === 'string'
              ? JSON.parse(res.data.structuredData)
              : res.data.structuredData
          console.log('优化后结构化数据:', optimizedStructuredData.value)
        } catch (e) {
          console.error('解析优化后结构化数据失败:', e)
          optimizedStructuredData.value = null
        }
      } else {
        console.log('没有structuredData字段，尝试其他字段')
        // 尝试其他可能的字段名
        if (res.data.optimizedStructuredData) {
          optimizedStructuredData.value = typeof res.data.optimizedStructuredData === 'string'
              ? JSON.parse(res.data.optimizedStructuredData)
              : res.data.optimizedStructuredData
          console.log('从optimizedStructuredData获取:', optimizedStructuredData.value)
        }
      }

      ElMessage.success('优化成功')
    } else {
      ElMessage.error(res.message || '优化失败')
    }
  } catch (error) {
    ElMessage.error('优化失败')
  } finally {
    optimizing.value = false
  }
}

/**
 * 复制原文
 */
const copyOriginal = () => {
  navigator.clipboard.writeText(resume.value.originalText)
  ElMessage.success('已复制到剪贴板')
}

/**
 * 复制优化结果
 */
const copyOptimized = () => {
  navigator.clipboard.writeText(optimizedText.value)
  ElMessage.success('已复制到剪贴板')
}

/**
 * 导出PDF
 */
const handleExport = async (type) => {
  try {
    const res = await exportResume(resume.value.id, type, resume.value.templateId)
    const blob = new Blob([res])
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `简历_${type === 'original' ? '原始' : '优化'}.pdf`)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

/**
 * 返回首页
 */
const goBack = () => {
  router.push('/home')
}
</script>

<style scoped>
/* ============================================
   整体布局
   ============================================ */
.optimize-container {
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
  max-width: 1400px;
  margin: 0 auto;
  padding: 24px;
}

/* ============================================
   加载状态
   ============================================ */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100px 20px;
}

.loading-container p {
  margin-top: 16px;
  color: #909399;
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

.optimize-section {
  display: flex;
  align-items: center;
}

/* ============================================
   简历对比区域
   ============================================ */
.resume-comparison {
  display: flex;
  gap: 24px;
  margin-bottom: 24px;
}

.resume-panel {
  flex: 1;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: #f8f9fa;
  border-bottom: 1px solid #ebeef5;
}

.panel-header h3 {
  margin: 0;
  font-size: 16px;
  color: #303133;
}

/* ============================================
   结构化简历样式
   ============================================ */
.structured-resume {
  padding: 20px;
  max-height: calc(100vh - 300px);
  overflow-y: auto;
}

.structured-resume.optimized {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
}

.structured-resume.optimized .section-title {
  color: #764ba2;
  border-bottom-color: #764ba2;
}

.structured-resume.optimized .item {
  border-left-color: #764ba2;
}

.section {
  margin-bottom: 24px;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #667eea;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 2px solid #667eea;
}

/* 基本信息 */
.basic-info {
  display: flex;
  align-items: center;
  gap: 16px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
}

.avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: 600;
}

.basic-info .info {
  flex: 1;
}

.basic-info .name {
  font-size: 20px;
  font-weight: 600;
  margin: 0 0 8px 0;
  color: #303133;
}

.basic-info .contact {
  display: flex;
  gap: 16px;
  font-size: 14px;
  color: #606266;
}

/* 列表项 */
.section-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.item {
  background: #f8f9fa;
  padding: 12px 16px;
  border-radius: 6px;
  border-left: 3px solid #667eea;
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 6px;
}

.item-title {
  font-weight: 600;
  color: #303133;
}

.item-period {
  font-size: 13px;
  color: #909399;
}

.item-subtitle {
  font-size: 14px;
  color: #606266;
  margin-bottom: 6px;
}

.item-desc {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  white-space: pre-wrap;
}

.item-extra {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

/* 技能标签 */
.skill-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

/* ============================================
   原始文本
   ============================================ */
.raw-text {
  padding: 20px;
  white-space: pre-wrap;
  line-height: 1.8;
  color: #606266;
  font-size: 14px;
  max-height: calc(100vh - 300px);
  overflow-y: auto;
}

/* ============================================
   优化结果
   ============================================ */
.optimized-content {
  padding: 20px;
  white-space: pre-wrap;
  line-height: 1.8;
  color: #606266;
  font-size: 14px;
  max-height: calc(100vh - 300px);
  overflow-y: auto;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
}

.empty-optimized {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #909399;
}

.empty-optimized p {
  margin: 8px 0 0 0;
}

.empty-optimized .sub-text {
  font-size: 13px;
}

/* ============================================
   中间箭头
   ============================================ */
.comparison-arrow {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #667eea;
}

.arrow-label {
  font-size: 13px;
  color: #667eea;
  margin-top: 8px;
}

/* ============================================
   底部操作按钮
   ============================================ */
.bottom-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  padding: 16px 0;
}
</style>