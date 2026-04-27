<template>
  <div class="preview-container">
    <el-header class="top-nav">
      <div class="logo" @click="goBack">
        <el-icon class="logo-icon"><i class="el-icon-s-operation"></i></el-icon>
        <span>AI简历优化</span>
      </div>
      <div class="nav-actions">
        <el-button type="primary" @click="saveTemplate" :loading="saving">保存模板</el-button>
        <el-button type="success" @click="exportPDF" :loading="exporting">导出PDF</el-button>
      </div>
    </el-header>

    <el-main class="main-content">
      <div class="preview-wrapper">
        <div class="template-selector">
          <h3>选择模板</h3>
          <div class="templates">
            <div v-for="tpl in templates" :key="tpl.id" class="template-card" :class="{ active: currentTemplate === tpl.id }" @click="currentTemplate = tpl.id">
              <div class="preview-thumb" :class="`tpl-${tpl.id}`">
                <div class="mock-name"></div>
                <div class="mock-line"></div>
                <div class="mock-line short"></div>
              </div>
              <p class="tpl-name">{{ tpl.name }}</p>
              <span class="tpl-desc">{{ tpl.desc }}</span>
            </div>
          </div>
        </div>

        <div class="resume-preview" :class="`template-${currentTemplate}`">
          <h1 class="name">{{ structuredData?.name || '未填写姓名' }}</h1>
          <p class="contact-info">
            <span v-if="structuredData?.phone">{{ structuredData.phone }}</span>
            <span v-if="structuredData?.email"> | {{ structuredData.email }}</span>
            <span v-if="structuredData?.location"> | {{ structuredData.location }}</span>
          </p>
          <div class="separator"></div>

          <div v-if="structuredData?.selfEvaluation" class="section">
            <h3 class="section-title">自我评价</h3>
            <p class="section-content">{{ structuredData.selfEvaluation }}</p>
          </div>

          <div v-if="structuredData?.education?.length" class="section">
            <h3 class="section-title">教育经历</h3>
            <div v-for="edu in structuredData.education" :key="edu.school" class="item">
              <h4 class="item-title">{{ edu.school }}</h4>
              <p class="item-subtitle">{{ edu.major }} | {{ edu.degree }}</p>
              <p class="item-period">{{ edu.period }}</p>
            </div>
          </div>

          <div v-if="structuredData?.experience?.length" class="section">
            <h3 class="section-title">工作经历</h3>
            <div v-for="exp in structuredData.experience" :key="exp.company" class="item">
              <h4 class="item-title">{{ exp.company }}</h4>
              <p class="item-subtitle">{{ exp.position }}</p>
              <p class="item-period">{{ exp.period }}</p>
              <p class="item-desc">{{ exp.description }}</p>
            </div>
          </div>

          <div v-if="structuredData?.projects?.length" class="section">
            <h3 class="section-title">项目经历</h3>
            <div v-for="proj in structuredData.projects" :key="proj.name" class="item">
              <h4 class="item-title">{{ proj.name }}</h4>
              <p class="item-subtitle">{{ proj.role }}</p>
              <p class="item-period">{{ proj.period }}</p>
              <p class="item-desc">{{ proj.description }}</p>
            </div>
          </div>

          <div v-if="structuredData?.skills?.length" class="section">
            <h3 class="section-title">技能特长</h3>
            <p class="skills-text">{{ structuredData.skills.join(' | ') }}</p>
          </div>

          <div v-if="structuredData?.awards?.length" class="section">
            <h3 class="section-title">获奖情况</h3>
            <p v-for="award in structuredData.awards" :key="award.name" class="list-item">- {{ award.name }} | {{ award.level }} | {{ award.year }}</p>
          </div>

          <div v-if="structuredData?.competitions?.length" class="section">
            <h3 class="section-title">比赛经历</h3>
            <p v-for="comp in structuredData.competitions" :key="comp.name" class="list-item">- {{ comp.name }} | {{ comp.result }} | {{ comp.year }}</p>
          </div>

          <div v-if="structuredData?.certifications?.length" class="section">
            <h3 class="section-title">证书资质</h3>
            <p v-for="cert in structuredData.certifications" :key="cert.name" class="list-item">- {{ cert.name || cert }} | {{ cert.year || '' }}</p>
          </div>

          <div v-if="structuredData?.campusActivities?.length" class="section">
            <h3 class="section-title">校园活动</h3>
            <p v-for="activity in structuredData.campusActivities" :key="activity.name" class="list-item">- {{ activity.name }} | {{ activity.role }} | {{ activity.period }}</p>
          </div>
        </div>
      </div>
    </el-main>
  </div>
</template>

<script setup>
import {ref, computed, onMounted} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {getResume, switchTemplate, exportResume} from '@/api/resume'
import {ElMessage} from 'element-plus'

const route = useRoute()
const router = useRouter()
const resumeId = route.params.id
const resumeData = ref(null)
const currentTemplate = ref(1)
const saving = ref(false)
const exporting = ref(false)

const templates = [
  {id: 1, name: '简约蓝', desc: '清新简洁，适合技术岗'},
  {id: 2, name: '商务灰', desc: '稳重专业，适合国企/大厂'},
  {id: 3, name: '创意橙', desc: '活力醒目，适合互联网'}
]

const structuredData = computed(() => {
  if (!resumeData.value?.structuredData) return null
  try {
    return JSON.parse(resumeData.value.structuredData)
  } catch {
    return null
  }
})

onMounted(async () => {
  try {
    const res = await getResume(resumeId)
    resumeData.value = res.data || res
    currentTemplate.value = resumeData.value.templateId || 1
  } catch (error) {
    ElMessage.error('加载简历失败')
  }
})

const saveTemplate = async () => {
  saving.value = true
  try {
    await switchTemplate(resumeId, currentTemplate.value);
    ElMessage.success('保存成功')
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const exportPDF = async () => {
  exporting.value = true
  try {
    const res = await exportResume(resumeId, 'optimized', currentTemplate.value)
    const blob = new Blob([res])
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    const date = new Date().toISOString().slice(0, 10).replace(/-/g, '')
    const tplNames = ['', '简约蓝', '商务灰', '创意橙']
    link.setAttribute('download', `简历_${tplNames[currentTemplate.value]}_${date}.pdf`)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
  } finally {
    exporting.value = false
  }
}

const goBack = () => {
  router.push(`/detail/${resumeId}`)
}
</script>

<style scoped>
.preview-container {
  min-height: 100vh;
  background: #f5f7fa;
}

.top-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 40px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  height: 60px;
}

.logo {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.logo-icon {
  font-size: 28px;
  margin-right: 10px;
  color: #409eff;
}

.nav-actions {
  display: flex;
  gap: 12px;
}

.main-content {
  padding: 20px;
}

.preview-wrapper {
  display: flex;
  gap: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.template-selector {
  width: 180px;
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  height: fit-content;
}

.template-selector h3 {
  margin: 0 0 15px;
  font-size: 16px;
  color: #333;
}

.template-card {
  padding: 12px;
  border: 2px solid #eee;
  border-radius: 8px;
  margin-bottom: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.template-card.active {
  border-color: #409eff;
  background: #ecf5ff;
}

.tpl-name {
  margin: 8px 0 4px;
  font-weight: 500;
  font-size: 14px;
}

.tpl-desc {
  font-size: 12px;
  color: #999;
}

.preview-thumb {
  height: 60px;
  border-radius: 4px;
  padding: 10px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.preview-thumb.tpl-1 {
  background: linear-gradient(135deg, #4A90E2 0%, #357ABD 100%);
}

.preview-thumb.tpl-2 {
  background: linear-gradient(135deg, #5A6A7A 0%, #3D4A5A 100%);
}

.preview-thumb.tpl-3 {
  background: linear-gradient(135deg, #F5A623 0%, #F76B1C 100%);
}

.mock-name {
  height: 10px;
  width: 50%;
  margin: 0 auto;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 2px;
}

.mock-line {
  height: 5px;
  background: rgba(255, 255, 255, 0.4);
  border-radius: 2px;
}

.mock-line.short {
  width: 60%;
}

.resume-preview {
  flex: 1;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 50px 60px;
  min-height: 800px;
}

.resume-preview .name {
  font-size: 28px;
  font-weight: bold;
  text-align: center;
  margin: 0 0 10px;
}

.resume-preview .contact-info {
  font-size: 12px;
  text-align: center;
  color: #666;
  margin-bottom: 15px;
}

.resume-preview .separator {
  height: 2px;
  margin: 20px 0;
}

.resume-preview .section-title {
  font-size: 16px;
  font-weight: bold;
  margin: 20px 0 10px;
  padding-bottom: 5px;
}

.resume-preview .section-content {
  font-size: 12px;
  line-height: 1.6;
  color: #333;
}

.resume-preview .item-title {
  font-size: 14px;
  font-weight: bold;
  margin: 10px 0 5px;
}

.resume-preview .item-subtitle {
  font-size: 12px;
  color: #666;
  margin: 0 0 3px;
}

.resume-preview .item-period {
  font-size: 11px;
  margin: 0 0 8px;
}

.resume-preview .item-desc {
  font-size: 11px;
  line-height: 1.5;
  color: #555;
  margin: 0 0 10px 15px;
}

.resume-preview .skills-text {
  font-size: 12px;
  line-height: 1.6;
}

.resume-preview .list-item {
  font-size: 12px;
  margin: 5px 0;
}

.template-1 .name {
  color: #4A90E2;
}

.template-1 .separator {
  background: linear-gradient(90deg, transparent, #4A90E2, transparent);
}

.template-1 .section-title {
  color: #4A90E2;
  border-bottom: 1px solid #e0e0e0;
}

.template-1 .item-period {
  color: #666;
}

.template-2 .name {
  color: #2C3E50;
}

.template-2 .separator {
  background: #5A6A7A;
}

.template-2 .section-title {
  color: #5A6A7A;
  border-bottom: 2px solid #5A6A7A;
}

.template-2 .item-period {
  color: #5A6A7A;
}

.template-3 .name {
  color: #F76B1C;
}

.template-3 .separator {
  background: linear-gradient(90deg, transparent, #F5A623, transparent);
}

.template-3 .section-title {
  color: #F76B1C;
  border-bottom: 1px solid #F5A623;
}

.template-3 .item-period {
  color: #F76B1C;
}
</style>