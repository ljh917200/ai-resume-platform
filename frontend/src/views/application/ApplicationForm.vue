<template>
  <el-dialog
      :model-value="visible"
      @update:model-value="$emit('update:visible', $event)"
      :title="isEdit ? '编辑投递记录' : '新增投递记录'"
      width="900px"
      :close-on-click-modal="false"
      :class="['dialog-container']"
      header-class="dialog-header"
      title-class="dialog-title"
      body-class="dialog-body"
      footer-class="dialog-footer"
      align-center="true"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="80px" label-position="top" class="form-wrapper">
      <!-- 基本信息 -->
      <div class="form-section">
        <div class="section-title">基本信息</div>
        <div class="form-row">
          <el-form-item prop="companyName" class="form-item">
            <span class="label-text"><span class="required">*</span>公司名称</span>
            <el-input v-model="form.companyName" placeholder="请输入公司名称" class="form-input" />
          </el-form-item>
          <el-form-item prop="jobTitle" class="form-item">
            <span class="label-text"><span class="required">*</span>职位名称</span>
            <el-input v-model="form.jobTitle" placeholder="请输入职位名称" class="form-input" />
          </el-form-item>
          <el-form-item class="form-item">
            <span class="label-text">公司行业</span>
            <el-select v-model="form.companyIndustry" placeholder="选择行业" clearable class="form-select">
              <el-option v-for="item in industryOptions" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
        </div>
        <div class="form-row">
          <el-form-item class="form-item">
            <span class="label-text">公司规模</span>
            <el-select v-model="form.companySize" placeholder="选择规模" clearable class="form-select">
              <el-option label="初创(0-50人)" value="初创" />
              <el-option label="中小型(50-500人)" value="中小型" />
              <el-option label="大型(500-10000人)" value="大型" />
              <el-option label="上市公司(10000+人)" value="上市公司" />
            </el-select>
          </el-form-item>
          <el-form-item class="form-item">
            <span class="label-text">公司地址</span>
            <el-input v-model="form.companyLocation" placeholder="如：广州市天河区" class="form-input" />
          </el-form-item>
        </div>
      </div>

      <!-- 职位信息 -->
      <div class="form-section">
        <div class="section-title">职位信息</div>
        <div class="form-row">
          <el-form-item class="form-item">
            <span class="label-text">职位类别</span>
            <el-select v-model="form.jobCategory" placeholder="选择类别" clearable class="form-select">
              <el-option label="技术" value="技术" />
              <el-option label="产品" value="产品" />
              <el-option label="运营" value="运营" />
              <el-option label="市场" value="市场" />
              <el-option label="设计" value="设计" />
              <el-option label="职能" value="职能" />
            </el-select>
          </el-form-item>
          <el-form-item class="form-item">
            <span class="label-text">职级</span>
            <el-select v-model="form.jobLevel" placeholder="选择职级" clearable class="form-select">
              <el-option label="初级" value="初级" />
              <el-option label="中级" value="中级" />
              <el-option label="高级" value="高级" />
              <el-option label="专家" value="专家" />
            </el-select>
          </el-form-item>
          <el-form-item class="form-item">
            <span class="label-text">薪资范围</span>
            <div class="salary-input">
              <el-input-number v-model="form.salaryMin" :min="0" :step="1000" placeholder="最低" class="form-number" />
              <span class="salary-separator">-</span>
              <el-input-number v-model="form.salaryMax" :min="0" :step="1000" placeholder="最高" class="form-number" />
            </div>
          </el-form-item>
        </div>
      </div>

      <!-- 投递信息 -->
      <div class="form-section">
        <div class="section-title">投递信息</div>
        <div class="form-row">
          <el-form-item prop="source" class="form-item">
            <span class="label-text"><span class="required">*</span>投递渠道</span>
            <el-select v-model="form.source" placeholder="选择渠道" class="form-select">
              <el-option label="Boss直聘" value="boss直聘" />
              <el-option label="猎聘" value="猎聘" />
              <el-option label="拉勾" value="拉勾" />
              <el-option label="官网" value="官网" />
              <el-option label="内推" value="内推" />
              <el-option label="其他" value="其他" />
            </el-select>
          </el-form-item>
          <el-form-item prop="status" class="form-item">
            <span class="label-text"><span class="required">*</span>投递状态</span>
            <el-select v-model="form.status" placeholder="选择状态" class="form-select">
              <el-option
                  v-for="item in statusOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item class="form-item">
            <span class="label-text">投递日期</span>
            <el-date-picker
                v-model="form.applyDate"
                type="date"
                placeholder="选择日期"
                value-format="YYYY-MM-DD"
                class="form-date"
            />
          </el-form-item>
        </div>
        <div class="form-row">
          <el-form-item v-if="showInterviewTime" class="form-item">
            <span class="label-text">面试时间</span>
            <el-date-picker
                v-model="form.interviewDate"
                type="datetime"
                placeholder="选择面试时间"
                value-format="YYYY-MM-DDTHH:mm:ss"
                class="form-date"
            />
          </el-form-item>
          <el-form-item class="form-item">
            <span class="label-text">HR姓名</span>
            <el-input v-model="form.hrName" placeholder="HR姓名（选填）" class="form-input" />
          </el-form-item>
          <el-form-item class="form-item">
            <span class="label-text">来源链接</span>
            <el-input v-model="form.sourceUrl" placeholder="职位链接（选填）" class="form-input" />
          </el-form-item>
        </div>
      </div>

      <!-- 补充信息 -->
      <div class="form-section">
        <div class="section-title">补充信息</div>
        <el-form-item class="form-item full-width">
          <span class="label-text">备注</span>
          <el-input v-model="form.notes" type="textarea" :rows="2" placeholder="备注信息（选填）" class="form-textarea" />
        </el-form-item>
        <el-form-item class="form-item full-width">
          <span class="label-text">职位描述</span>
          <el-input v-model="form.jobDescription" type="textarea" :rows="3" placeholder="粘贴JD原文，后续可用于AI分析" class="form-textarea" />
        </el-form-item>
      </div>
    </el-form>

    <template #footer>
      <el-button class="btn-cancel-ink" @click="$emit('update:visible', false)">取消</el-button>
      <el-button class="btn-primary-ink" :loading="submitting" @click="handleSubmit">
        {{ isEdit ? '保存修改' : '立即创建' }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { createApplication, updateApplication } from '@/api/application'

const props = defineProps({
  visible: Boolean,
  editData: Object,
  defaultStatus: String
})
const emit = defineEmits(['update:visible', 'success'])

const formRef = ref(null)
const submitting = ref(false)

const isEdit = computed(() => !!props.editData?.id)

const interviewStatuses = ['screening', 'test', 'first_interview', 'second_interview', 'hr_interview']
const showInterviewTime = computed(() => interviewStatuses.includes(form.value.status))

const statusOptions = [
  { value: 'interested', label: '感兴趣' },
  { value: 'applied', label: '已投递' },
  { value: 'screening', label: '筛选中' },
  { value: 'test', label: '笔试/测评' },
  { value: 'first_interview', label: '一面' },
  { value: 'second_interview', label: '二面' },
  { value: 'hr_interview', label: 'HR面' },
  { value: 'offer_received', label: '已Offer' },
  { value: 'rejected', label: '已拒绝' },
  { value: 'withdrawn', label: '已撤回' }
]

const industryOptions = [
  '互联网', '软件开发', '电子商务', '金融', '教育', '医疗健康',
  '制造业', '房地产', '物流', '新能源', '人工智能', '游戏',
  '传媒', '餐饮', '零售', '其他'
]

const form = ref(getDefaultForm())

function getDefaultForm() {
  return {
    companyName: '',
    companyIndustry: '',
    companySize: '',
    companyLocation: '',
    jobTitle: '',
    jobCategory: '',
    jobLevel: '',
    salaryMin: null,
    salaryMax: null,
    source: '其他',
    sourceUrl: '',
    status: props.defaultStatus || 'interested',
    applyDate: '',
    interviewDate: '',
    hrName: '',
    notes: '',
    jobDescription: ''
  }
}

watch(() => props.visible, (val) => {
  if (val) {
    if (props.editData) {
      form.value = { ...getDefaultForm(), ...props.editData }
    } else {
      form.value = getDefaultForm()
    }
  }
})

const rules = {
  companyName: [{ required: true, message: '请输入公司名称', trigger: 'blur' }],
  jobTitle: [{ required: true, message: '请输入职位名称', trigger: 'blur' }],
  source: [{ required: true, message: '请选择投递渠道', trigger: 'change' }],
  status: [{ required: true, message: '请选择投递状态', trigger: 'change' }]
}

async function handleSubmit() {
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  submitting.value = true
  try {
    let res
    if (isEdit.value) {
      res = await updateApplication(props.editData.id, form.value)
    } else {
      res = await createApplication(form.value)
    }
    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '修改成功' : '创建成功')
      emit('update:visible', false)
      emit('success')
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (e) {
    ElMessage.error('操作失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.dialog-container {
  border-radius: 16px !important;
  overflow: hidden;
  animation: dialogEnter 0.3s var(--ink-ease) both;
}

.dialog-container.el-dialog--closing {
  animation: dialogLeave 0.15s var(--ink-ease) both;
}

@keyframes dialogEnter {
  from {
    opacity: 0;
    transform: scale(0.95);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

@keyframes dialogLeave {
  from {
    opacity: 1;
    transform: scale(1);
  }
  to {
    opacity: 0;
    transform: scale(0.95);
  }
}

.dialog-container::v-deep .el-dialog__wrapper {
  background: rgba(26, 26, 46, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
}

.dialog-container::v-deep .el-dialog {
  margin: 0;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
}

.dialog-container::v-deep .el-dialog__body {
  flex: 1;
  overflow-y: auto;
  max-height: calc(90vh - 120px);
}

.dialog-header {
  position: relative;
  border-bottom: 1px solid #f0f0f0 !important;
  padding: 0 !important;
}

.dialog-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 24px;
  right: 24px;
  height: 2px;
  background: var(--ink-text-title);
}

.dialog-title {
  color: var(--ink-text-title) !important;
  font-family: var(--ink-font-serif);
  font-weight: 600 !important;
  font-size: 18px !important;
  padding: 24px 24px 16px !important;
}

.dialog-container::v-deep .el-dialog__headerbtn {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  margin-right: 12px;
  transition: background 0.2s var(--ink-ease);
}

.dialog-container::v-deep .el-dialog__headerbtn:hover {
  background: rgba(26, 26, 46, 0.08);
}

.dialog-container::v-deep .el-dialog__close {
  font-size: 16px;
  color: var(--ink-text-secondary);
}

.dialog-body {
  padding: 24px 32px !important;
}

.form-wrapper {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.form-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--ink-text-title);
  padding-left: 10px;
  border-left: 3px solid var(--ink-text-title);
  margin-bottom: 8px;
}

.form-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-item.full-width {
  grid-column: span 3;
}

.form-item.half-width {
  grid-column: span 1.5;
}

.label-text {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.required {
  color: var(--ink-text-title);
  margin-right: 4px;
}

.form-input .el-input__wrapper {
  background: #f7f8fa !important;
  border: 1px solid #e8e8e8 !important;
  border-radius: 8px !important;
  height: 40px;
  transition: border-color 0.3s var(--ink-ease), box-shadow 0.3s var(--ink-ease);
}

.form-input .el-input__wrapper.is-focus {
  border-color: var(--ink-text-title) !important;
  box-shadow: 0 0 0 3px rgba(26, 26, 46, 0.06) !important;
}

.form-input .el-input__inner {
  color: var(--ink-text-primary) !important;
  font-size: 14px;
}

.form-input .el-input__placeholder {
  color: #ccc !important;
}

.form-select .el-input__wrapper {
  background: #f7f8fa !important;
  border: 1px solid #e8e8e8 !important;
  border-radius: 8px !important;
  height: 40px;
  transition: border-color 0.3s var(--ink-ease), box-shadow 0.3s var(--ink-ease);
}

.form-select .el-input__wrapper.is-focus {
  border-color: var(--ink-text-title) !important;
  box-shadow: 0 0 0 3px rgba(26, 26, 46, 0.06) !important;
}

.form-select::v-deep .el-select-dropdown {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e8e8e8;
  box-shadow: 0 4px 16px rgba(26, 26, 46, 0.08);
}

.form-select::v-deep .el-select-dropdown__item {
  padding: 10px 16px;
  font-size: 14px;
  color: var(--ink-text-primary);
}

.form-select::v-deep .el-select-dropdown__item:hover {
  background: #fafafa;
}

.form-date .el-input__wrapper {
  background: #f7f8fa !important;
  border: 1px solid #e8e8e8 !important;
  border-radius: 8px !important;
  height: 40px;
  transition: border-color 0.3s var(--ink-ease), box-shadow 0.3s var(--ink-ease);
}

.form-date .el-input__wrapper.is-focus {
  border-color: var(--ink-text-title) !important;
  box-shadow: 0 0 0 3px rgba(26, 26, 46, 0.06) !important;
}

.salary-input {
  display: flex;
  align-items: center;
  gap: 8px;
}

.salary-separator {
  color: #999;
  font-size: 14px;
}

.form-number {
  flex: 1;
  background: #f7f8fa !important;
  border: 1px solid #e8e8e8 !important;
  border-radius: 8px !important;
  height: 40px;
  transition: border-color 0.3s var(--ink-ease), box-shadow 0.3s var(--ink-ease);
}

.form-number.is-focus {
  border-color: var(--ink-text-title) !important;
  box-shadow: 0 0 0 3px rgba(26, 26, 46, 0.06) !important;
}

.form-textarea .el-textarea__inner {
  background: #f7f8fa !important;
  border: 1px solid #e8e8e8 !important;
  border-radius: 8px !important;
  min-height: 80px;
  transition: border-color 0.3s var(--ink-ease), box-shadow 0.3s var(--ink-ease);
}

.form-textarea .el-textarea__inner:focus {
  border-color: var(--ink-text-title) !important;
  box-shadow: 0 0 0 3px rgba(26, 26, 46, 0.06) !important;
}

.form-textarea .el-textarea__inner::placeholder {
  color: #ccc;
}

.form-wrapper::v-deep .el-form-item__error {
  color: #666 !important;
  font-size: 12px;
  margin-top: 4px;
  border-bottom: 1px dashed #666;
  padding-bottom: 2px;
}

.dialog-footer {
  border-top: 1px solid #f0f0f0 !important;
  padding: 16px 32px !important;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.btn-cancel-ink {
  background: transparent !important;
  border: 1px solid var(--ink-text-title) !important;
  color: var(--ink-text-title) !important;
  border-radius: 8px !important;
  height: 40px;
  padding: 0 20px;
  font-size: 14px;
  transition: background 0.2s var(--ink-ease);
}

.btn-cancel-ink:hover {
  background: rgba(26, 26, 46, 0.05) !important;
}

.btn-primary-ink {
  background: var(--ink-text-title) !important;
  border-color: var(--ink-text-title) !important;
  color: #fff !important;
  border-radius: 8px !important;
  height: 40px;
  padding: 0 24px;
  font-size: 14px;
  font-weight: 500;
  transition: background 0.2s var(--ink-ease);
}

.btn-primary-ink:hover {
  background: #151525 !important;
}

.btn-primary-ink.is-loading {
  pointer-events: none;
}

.btn-primary-ink.is-loading::v-deep .el-button__loading-icon {
  color: #fff;
}

@media (max-width: 600px) {
  .form-row {
    grid-template-columns: 1fr;
  }
  
  .form-item.full-width {
    grid-column: span 1;
  }
  
  .dialog-body {
    padding: 16px 20px !important;
  }
  
  .dialog-footer {
    padding: 12px 20px !important;
  }
}
</style>
