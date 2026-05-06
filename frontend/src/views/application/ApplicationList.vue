<template>
  <div class="application-list">
    <div class="content-wrapper">
      <!-- 页面标题和操作栏 -->
      <div class="page-header">
        <h2>投递列表</h2>
        <div class="header-actions">
          <el-button type="primary" class="btn-add" @click="handleAdd">
            <el-icon><Plus /></el-icon> 新增投递
          </el-button>
          <el-button class="btn-board" @click="$router.push('/application/board')">
            <el-icon><Grid /></el-icon> 看板视图
          </el-button>
        </div>
      </div>

      <!-- 搜索和筛选栏 -->
      <div class="filter-card">
        <div class="filter-row">
          <div class="search-wrapper">
            <el-icon class="search-icon"><Search /></el-icon>
            <el-input
                v-model="searchKey"
                placeholder="搜索公司或职位"
                clearable
                @clear="handleSearch"
                @keyup.enter="handleSearch"
            />
          </div>
          <el-select 
            v-model="filterStatus" 
            placeholder="投递状态" 
            clearable 
            class="filter-select"
            @change="handleSearch"
          >
            <el-option
                v-for="item in statusOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
            />
          </el-select>
          <el-select 
            v-model="filterSource" 
            placeholder="投递渠道" 
            clearable 
            class="filter-select"
            @change="handleSearch"
          >
            <el-option
                v-for="item in sourceOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
            />
          </el-select>
          <el-button class="btn-search" @click="handleSearch">
            <el-icon><Search /></el-icon> 搜索
          </el-button>
          <el-button class="btn-reset" @click="handleReset">
            重置
          </el-button>
        </div>
      </div>

      <!-- 批量操作提示条 -->
      <Transition name="slide-down">
        <div v-if="multipleSelection.length > 0" class="batch-bar">
          <div class="batch-info">
            <span>已选择 <span class="batch-count">{{ multipleSelection.length }}</span> 项</span>
          </div>
          <div class="batch-actions">
            <el-button size="small" class="btn-cancel" @click="multipleSelection = []">取消选择</el-button>
            <el-button size="small" class="btn-batch-delete" @click="handleBatchDelete">批量删除</el-button>
          </div>
        </div>
      </Transition>

      <!-- 数据表格 -->
      <div class="table-card">
        <Transition name="table-fade">
          <el-table
              v-if="!loading"
              :data="tableData"
              class="application-table"
              :row-class-name="getRowClassName"
              @selection-change="handleSelectionChange"
          >
            <el-table-column type="selection" width="55" />
            <el-table-column prop="companyName" label="公司名称" width="220" show-overflow-tooltip />
            <el-table-column prop="jobTitle" label="职位名称" width="200" show-overflow-tooltip />
            <el-table-column label="薪资范围" width="140" align="center">
              <template #default="{ row }">
                <span v-if="row.salaryMin || row.salaryMax">
                  {{ row.salaryMin ? (row.salaryMin / 1000).toFixed(0) + 'k' : '?' }} -
                  {{ row.salaryMax ? (row.salaryMax / 1000).toFixed(0) + 'k' : '?' }}
                </span>
                <span v-else class="text-muted">未填写</span>
              </template>
            </el-table-column>
            <el-table-column prop="source" label="投递渠道" width="120" align="center">
              <template #default="{ row }">
                {{ sourceLabel(row.source) }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="投递状态" width="120" align="center">
              <template #default="{ row }">
                <span :class="['status-tag', statusTagClass(row.status)]">
                  {{ statusLabel(row.status) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="applyDate" label="投递日期" width="130" align="center" />
            <el-table-column label="操作" width="240" align="center">
              <template #default="{ row }">
                <el-button link class="btn-edit" size="small" @click="handleEdit(row)">编辑</el-button>
                <el-popconfirm 
                  title="确定删除这条投递记录？" 
                  @confirm="handleDelete(row)"
                  confirm-button-text="删除"
                  cancel-button-text="取消"
                >
                  <template #reference>
                    <el-button link class="btn-delete" size="small">删除</el-button>
                  </template>
                </el-popconfirm>
              </template>
            </el-table-column>
          </el-table>
        </Transition>

        <!-- 加载状态 -->
        <div v-if="loading" class="loading-overlay">
          <el-loading-spinner />
          <span>加载中...</span>
        </div>

        <!-- 空状态 -->
        <div v-if="tableData.length === 0 && !loading" class="empty-state">
          <div class="empty-icon">
            <svg viewBox="0 0 64 64" fill="none">
              <path d="M12 16h40v36H12z" stroke="#ccc" stroke-width="1" fill="none"/>
              <path d="M16 24h12M16 32h20M16 40h16M16 48h24" stroke="#ccc" stroke-width="0.5"/>
              <circle cx="52" cy="20" r="6" stroke="#ccc" stroke-width="1" fill="none"/>
            </svg>
          </div>
          <div class="empty-title">暂无投递记录</div>
          <div class="empty-desc">点击右上角"新增投递"添加您的第一条投递记录</div>
        </div>
      </div>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <span class="total-count">共 {{ total }} 条</span>
        <el-pagination
            v-model:current-page="page"
            v-model:page-size="size"
            :total="total"
            :page-sizes="[10, 20, 50]"
            layout="prev, pager, next, jumper"
            @size-change="loadData"
            @current-change="loadData"
        />
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <ApplicationForm
        v-model:visible="formVisible"
        :edit-data="editData"
        @success="loadData"
    />

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="投递详情" width="600px">
      <el-descriptions :column="2" border v-if="detailData">
        <el-descriptions-item label="公司名称">{{ detailData.companyName }}</el-descriptions-item>
        <el-descriptions-item label="公司行业">{{ detailData.companyIndustry || '-' }}</el-descriptions-item>
        <el-descriptions-item label="公司规模">{{ detailData.companySize || '-' }}</el-descriptions-item>
        <el-descriptions-item label="公司地址">{{ detailData.companyLocation || '-' }}</el-descriptions-item>
        <el-descriptions-item label="职位名称">{{ detailData.jobTitle }}</el-descriptions-item>
        <el-descriptions-item label="职位类别">{{ detailData.jobCategory || '-' }}</el-descriptions-item>
        <el-descriptions-item label="职级">{{ detailData.jobLevel || '-' }}</el-descriptions-item>
        <el-descriptions-item label="薪资范围">
          <span v-if="detailData.salaryMin || detailData.salaryMax">
            {{ detailData.salaryMin || '?' }} - {{ detailData.salaryMax || '?' }}
          </span>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="投递渠道">{{ sourceLabel(detailData.source) }}</el-descriptions-item>
        <el-descriptions-item label="投递状态">
          <span :class="['status-tag', statusTagClass(detailData.status)]">
            {{ statusLabel(detailData.status) }}
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="投递日期">{{ detailData.applyDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="面试时间">{{ detailData.interviewDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="HR姓名">{{ detailData.hrName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="来源链接">
          <a v-if="detailData.sourceUrl" :href="detailData.sourceUrl" target="_blank">查看</a>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detailData.notes || '-' }}</el-descriptions-item>
        <el-descriptions-item label="职位描述" :span="2">
          <div style="white-space: pre-wrap; max-height: 200px; overflow-y: auto">
            {{ detailData.jobDescription || '-' }}
          </div>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Grid } from '@element-plus/icons-vue'
import {
  getApplications,
  deleteApplication,
  batchDeleteApplications,
  updateApplicationStatus
} from '@/api/application'
import ApplicationForm from './ApplicationForm.vue'

// ====== 数据 ======
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)

// 筛选条件
const searchKey = ref('')
const filterStatus = ref('')
const filterSource = ref('')

// 多选
const multipleSelection = ref([])

// 弹窗控制
const formVisible = ref(false)
const editData = ref(null)
const detailVisible = ref(false)
const detailData = ref(null)

// ====== 状态配置 ======
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

const sourceOptions = [
  { value: 'boss直聘', label: 'Boss直聘' },
  { value: '猎聘', label: '猎聘' },
  { value: '拉勾', label: '拉勾' },
  { value: '官网', label: '官网' },
  { value: '内推', label: '内推' },
  { value: '其他', label: '其他' }
]

function statusTagClass(status) {
  const interviewStatuses = ['screening', 'test', 'first_interview', 'second_interview', 'hr_interview']
  const endedStatuses = ['rejected', 'withdrawn']
  
  if (status === 'interested') return 'tag-interested'
  if (status === 'applied') return 'tag-applied'
  if (interviewStatuses.includes(status)) return 'tag-interview'
  if (status === 'offer_received') return 'tag-offer'
  if (endedStatuses.includes(status)) return 'tag-ended'
  return 'tag-interested'
}

function statusLabel(status) {
  const found = statusOptions.find(o => o.value === status)
  return found ? found.label : status
}

function sourceLabel(source) {
  const found = sourceOptions.find(o => o.value === source)
  return found ? found.label : source || '其他'
}

// ====== 获取行样式类 ======
function getRowClassName({ row }) {
  const selected = multipleSelection.value.some(item => item.id === row.id)
  return selected ? 'row-selected' : ''
}

// ====== 搜索触发 ======
function handleSearch() {
  page.value = 1
  loadData()
}

// ====== 重置筛选 ======
function handleReset() {
  searchKey.value = ''
  filterStatus.value = ''
  filterSource.value = ''
  page.value = 1
  loadData()
}

// ====== 数据加载 ======
async function loadData() {
  loading.value = true
  try {
    const params = {
      page: page.value,
      size: size.value
    }
    if (filterStatus.value) {
      params.status = filterStatus.value
    }
    if (searchKey.value) {
      params.keyword = searchKey.value
    }
    if (filterSource.value) {
      params.source = filterSource.value
    }
    const res = await getApplications(params)
    if (res.code === 200) {
      tableData.value = res.data.list || []
      total.value = res.data.total || 0
    }
  } catch (e) {
    console.error('加载投递列表失败', e)
  } finally {
    loading.value = false
  }
}

// ====== 多选 ======
function handleSelectionChange(val) {
  multipleSelection.value = val
}

// ====== 批量删除 ======
async function handleBatchDelete() {
  const ids = multipleSelection.value.map(item => item.id)
  if (ids.length === 0) return

  try {
    await ElMessageBox.confirm(
        `确定删除选中的 ${ids.length} 条投递记录？`,
        '批量删除',
        { type: 'warning' }
    )
    const res = await batchDeleteApplications(ids)
    if (res.code === 200) {
      ElMessage.success(`已删除 ${ids.length} 条记录`)
      multipleSelection.value = []
      await loadData()
    } else {
      ElMessage.error(res.message || '批量删除失败')
    }
  } catch {
    // 用户取消
  }
}

// ====== 操作 ======
function handleAdd() {
  editData.value = null
  formVisible.value = true
}

function handleEdit(row) {
  editData.value = { ...row }
  formVisible.value = true
}

function handleView(row) {
  detailData.value = row
  detailVisible.value = true
}

async function handleDelete(row) {
  try {
    const res = await deleteApplication(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      await loadData()
    }
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.application-list {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 32px;
}

.content-wrapper {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 22px;
  color: var(--ink-text-title);
  font-family: var(--ink-font-serif);
  font-weight: 500;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.btn-add {
  border-radius: 8px !important;
  padding: 8px 16px;
  font-size: 14px;
}

/* 筛选卡片 */
.filter-card {
  background: #fff;
  border-radius: var(--ink-radius-md);
  padding: 16px 20px;
  box-shadow: 0 2px 8px rgba(26, 26, 46, 0.04);
  margin-bottom: 16px;
}

.filter-row {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.search-wrapper {
  position: relative;
  flex: 1;
  max-width: 320px;
}

.search-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #999;
  font-size: 14px;
}

.search-wrapper .el-input__wrapper {
  padding-left: 36px;
  border-radius: 8px;
  background: #f7f8fa;
  border: 1px solid #e8e8e8;
}

.search-wrapper .el-input__wrapper:focus-within {
  border-color: var(--ink-text-title);
  box-shadow: 0 0 0 3px rgba(26, 26, 46, 0.05);
}

.filter-select {
  width: 160px;
}

.filter-select .el-input__wrapper {
  border-radius: 8px;
  background: #f7f8fa;
  border: 1px solid #e8e8e8;
}

.filter-select .el-input__wrapper:focus-within {
  border-color: var(--ink-text-title);
}

.btn-search {
  background: var(--ink-text-title) !important;
  border-color: var(--ink-text-title) !important;
  color: #fff !important;
  border-radius: 8px !important;
  padding: 8px 16px;
}

.btn-reset {
  background: transparent !important;
  border: 1px solid var(--ink-text-title) !important;
  color: var(--ink-text-title) !important;
  border-radius: 8px !important;
  padding: 8px 16px;
}

.btn-reset:hover {
  background: rgba(26, 26, 46, 0.05) !important;
}

/* 批量操作提示条 */
.batch-bar {
  background: var(--ink-text-title);
  color: #fff;
  padding: 14px 20px;
  border-radius: var(--ink-radius-md);
  margin-bottom: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.batch-info {
  font-size: 14px;
}

.batch-count {
  font-weight: 600;
  font-size: 16px;
}

.batch-actions {
  display: flex;
  gap: 12px;
}

.btn-cancel {
  background: transparent !important;
  border: none !important;
  color: rgba(255, 255, 255, 0.8) !important;
  padding: 6px 12px;
  font-size: 13px;
}

.btn-cancel:hover {
  color: #fff !important;
}

.btn-batch-delete {
  background: #fff !important;
  border: 1px solid #fff !important;
  color: var(--ink-text-title) !important;
  border-radius: 6px !important;
  padding: 6px 12px;
  font-size: 13px;
}

/* 表格卡片 */
.table-card {
  background: #fff;
  border-radius: var(--ink-radius-md);
  box-shadow: 0 2px 8px rgba(26, 26, 46, 0.04);
  overflow: hidden;
  min-height: 300px;
  position: relative;
}

/* 表格样式 */
.application-table {
  --el-table-border-color: transparent;
  --el-table-row-hover-bg-color: #fafafa;
  --el-table-header-text-color: #666;
}

.application-table thead th {
  background: #fafafa !important;
  font-weight: 500;
  font-size: 14px;
  color: #666;
  border-bottom: 1px solid #f0f0f0 !important;
  padding: 14px 12px;
}

.application-table tbody tr {
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.3s var(--ink-ease);
}

.application-table tbody tr:last-child {
  border-bottom: none;
}

.application-table tbody tr.row-selected {
  background: #f5f5f5;
  position: relative;
}

.application-table tbody tr.row-selected::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background: var(--ink-text-title);
}

.application-table .el-table__row {
  height: 56px;
}

/* 状态标签 */
.status-tag {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
}

.tag-interested {
  background: transparent;
  border: 1px solid #999;
  color: #666;
}

.tag-applied {
  background: transparent;
  border: 1px solid #666;
  color: #333;
}

.tag-interview {
  background: #333;
  color: #fff;
}

.tag-offer {
  background: var(--ink-text-title);
  color: #fff;
}

.tag-ended {
  background: #f0f0f0;
  color: #999;
}

/* 操作按钮 */
.btn-edit {
  color: #999 !important;
  font-size: 13px;
}

.btn-edit:hover {
  color: var(--ink-text-title) !important;
}

.btn-delete {
  color: #999 !important;
  font-size: 13px;
}

.btn-delete:hover {
  color: #c75b5b !important;
}

.text-muted {
  color: #999;
}

/* 加载状态 */
.loading-overlay {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  color: #999;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60px 20px;
}

.empty-icon {
  width: 64px;
  height: 64px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-title {
  font-size: 16px;
  color: var(--ink-text-title);
  margin-bottom: 8px;
}

.empty-desc {
  font-size: 14px;
  color: #999;
}

/* 分页 */
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 16px;
  margin-top: 20px;
  padding: 16px;
  background: #fff;
  border-radius: var(--ink-radius-md);
}

.total-count {
  font-size: 13px;
  color: #999;
}

.pagination-wrapper .el-pagination {
  --el-pagination-button-bg-color: transparent;
  --el-pagination-button-active-bg-color: var(--ink-text-title);
  --el-pagination-button-active-text-color: #fff;
  --el-pagination-button-text-color: #666;
}

.pagination-wrapper .el-pager li {
  border-radius: 6px;
  min-width: 32px;
  height: 32px;
  line-height: 32px;
}

.pagination-wrapper .el-pager li.active {
  background: var(--ink-text-title);
  color: #fff;
}

.pagination-wrapper .el-pager li:hover:not(.active) {
  color: var(--ink-text-title);
}

/* 过渡动画 */
.slide-down-enter-active,
.slide-down-leave-active {
  animation: slideDown 0.3s var(--ink-ease) both;
}

.slide-down-enter-active {
  animation-name: slideDownEnter;
}

.slide-down-leave-active {
  animation-name: slideDownLeave;
}

@keyframes slideDownEnter {
  from {
    opacity: 0;
    transform: translateY(-10px);
    max-height: 0;
  }
  to {
    opacity: 1;
    transform: translateY(0);
    max-height: 100px;
  }
}

@keyframes slideDownLeave {
  from {
    opacity: 1;
    transform: translateY(0);
    max-height: 100px;
  }
  to {
    opacity: 0;
    transform: translateY(-10px);
    max-height: 0;
  }
}

.table-fade-enter-active,
.table-fade-leave-active {
  transition: opacity 0.2s var(--ink-ease);
}

.table-fade-enter-from,
.table-fade-leave-to {
  opacity: 0.5;
}

@media (max-width: 768px) {
  .application-list {
    padding: 16px;
  }
  
  .filter-row {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-wrapper {
    max-width: 100%;
  }
  
  .filter-select {
    width: 100%;
  }
  
  .pagination-wrapper {
    flex-direction: column;
    gap: 12px;
  }
  
  .total-count {
    align-self: flex-start;
  }
}
</style>
