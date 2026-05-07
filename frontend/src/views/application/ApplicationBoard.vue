<template>
  <div class="application-board">
    <div class="page-header">
      <h2>投递看板</h2>
      <div class="header-actions">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon> 新增投递
        </el-button>
        <el-button @click="$router.push('/application/list')">
          <el-icon><List /></el-icon> 列表视图
        </el-button>
      </div>
    </div>

    <div class="board-container">
      <div 
        v-for="column in columns" 
        :key="column.key"
        class="board-column"
        :class="column.key"
      >
        <div class="column-header">
          <div class="column-bar"></div>
          <span class="column-title">{{ column.title }}</span>
          <span class="column-count">{{ getColumnData(column.key).length }}</span>
        </div>
        <div class="column-body">
          <div
              v-for="(item, index) in getColumnData(column.key)"
              :key="item.id"
              class="board-card"
              :style="{ '--stagger-delay': `${index * 50}ms` }"
              @click="handleView(item)"
          >
            <div class="card-bar"></div>
            <div class="card-content">
              <div class="card-top">
                <span class="card-company">{{ item.companyName }}</span>
                <span :class="['card-status', getStatusClass(item.status)]">{{ statusLabel(item.status) }}</span>
              </div>
              <div class="card-job">{{ item.jobTitle }}</div>
              <div class="card-date">{{ formatDate(item.applyDate) }}</div>
              
              <div v-if="isInterviewStatus(item.status)" class="interview-progress">
                <div 
                  v-for="(step, stepIndex) in interviewSteps" 
                  :key="step.key"
                  :class="['progress-step', getStepClass(item.status, step.key)]"
                >
                  <div class="step-dot"></div>
                  <span class="step-label">{{ step.label }}</span>
                  <div v-if="stepIndex < interviewSteps.length - 1" class="step-line"></div>
                </div>
              </div>
              
              <div class="card-actions">
                <el-button 
                  v-if="getNextStatus(item.status)" 
                  size="small" 
                  class="btn-next" 
                  text 
                  @click.stop="moveToStatus(item, getNextStatus(item.status))"
                >
                  {{ getNextLabel(item.status) }} →
                </el-button>
                <el-button v-if="column.key !== 'ended'" size="small" class="btn-offer" text @click.stop="moveToStatus(item, 'offer_received')">
                  Offer ✓
                </el-button>
                <el-button v-if="column.key !== 'ended'" size="small" class="btn-reject" text @click.stop="moveToStatus(item, 'rejected')">
                  拒绝 ✗
                </el-button>
                <el-button size="small" class="btn-delete" text @click.stop="handleDelete(item)">删除</el-button>
              </div>
            </div>
          </div>
          
          <div v-if="getColumnData(column.key).length === 0" class="empty-state">
            <div class="empty-icon">
              <svg viewBox="0 0 64 64" fill="none">
                <path d="M8 20h48v32H8z" stroke="#ccc" stroke-width="1" fill="none"/>
                <path d="M12 24h8M12 32h16M12 40h12M12 48h20" stroke="#ccc" stroke-width="0.5"/>
              </svg>
            </div>
            <p>暂无投递记录</p>
          </div>
        </div>
        
        <div class="column-footer">
          <button class="btn-add" @click="handleAddWithStatus(column.key)">
            <el-icon><Plus /></el-icon> 新增
          </button>
        </div>
      </div>
    </div>

    <el-dialog v-model="detailVisible" title="投递详情" width="600px">
      <el-descriptions :column="2" border v-if="detailData">
        <el-descriptions-item label="公司名称">{{ detailData.companyName }}</el-descriptions-item>
        <el-descriptions-item label="职位名称">{{ detailData.jobTitle }}</el-descriptions-item>
        <el-descriptions-item label="投递状态">
          <span :class="['status-tag', getStatusClass(detailData.status)]">
            {{ statusLabel(detailData.status) }}
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="投递渠道">{{ detailData.source || '其他' }}</el-descriptions-item>
        <el-descriptions-item label="投递日期">{{ detailData.applyDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="面试时间">{{ detailData.interviewDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detailData.notes || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleEditFromDetail">编辑</el-button>
        <el-button type="danger" @click="handleDeleteFromDetail">删除</el-button>
      </template>
    </el-dialog>

    <ApplicationForm
        v-model:visible="formVisible"
        :edit-data="editData"
        :default-status="defaultStatus"
        @success="loadBoardData"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, List } from '@element-plus/icons-vue';
import { getApplications, updateApplicationStatus, deleteApplication } from '@/api/application';
import ApplicationForm from './ApplicationForm.vue';

const allData = ref([]);
const loading = ref(false);

const formVisible = ref(false);
const editData = ref(null);
const detailVisible = ref(false);
const detailData = ref(null);
const defaultStatus = ref('interested');

const interviewStatuses = ['screening', 'test', 'first_interview', 'second_interview', 'hr_interview'];
const endedStatuses = ['rejected', 'withdrawn'];

const statusMap = {
 interested: '感兴趣',
 applied: '已投递',
 screening: '筛选中',
 test: '笔试',
 first_interview: '一面',
 second_interview: '二面',
 hr_interview: 'HR面',
 offer_received: '已Offer',
 rejected: '已拒绝',
 withdrawn: '已撤回'
};

const interviewSteps = [
 { key: 'screening', label: '筛选' },
 { key: 'test', label: '笔试' },
 { key: 'first_interview', label: '一面' },
 { key: 'second_interview', label: '二面' },
 { key: 'hr_interview', label: 'HR面' }
];

const columns = [
 { key: 'interested', title: '感兴趣' },
 { key: 'applied', title: '已投递' },
 { key: 'interviewing', title: '面试中' },
 { key: 'offer', title: '已Offer' },
 { key: 'ended', title: '已结束' }
];

function statusLabel(status) { return statusMap[status] || status; }

function isInterviewStatus(status) {
 return interviewStatuses.includes(status);
}

function getStatusClass(status) {
 if (status === 'interested') return 'status-interested';
 if (status === 'applied') return 'status-applied';
 if (interviewStatuses.includes(status)) return 'status-interview';
 if (status === 'offer_received') return 'status-offer';
 return 'status-ended';
}

function getStepClass(currentStatus, stepKey) {
 const currentIndex = interviewSteps.findIndex(s => s.key === currentStatus);
 const stepIndex = interviewSteps.findIndex(s => s.key === stepKey);
 if (stepIndex < currentIndex) return 'step-done';
 if (stepIndex === currentIndex) return 'step-current';
 return 'step-pending';
}

function getNextStatus(status) {
 const order = ['interested', 'applied', 'screening', 'test', 'first_interview', 'second_interview', 'hr_interview'];
 const index = order.indexOf(status);
 if (index >= 0 && index < order.length - 1) {
 return order[index + 1];
 }
 return null;
}

function getNextLabel(status) {
 const nextStatus = getNextStatus(status);
 if (nextStatus) {
 return statusLabel(nextStatus);
 }
 return '下一步';
}

function formatDate(dt) {
 if (!dt) return '';
 return dt.substring(0, 10);
}

function getColumnData(columnKey) {
 switch (columnKey) {
 case 'interested':
 return allData.value.filter(item => item.status === 'interested');
 case 'applied':
 return allData.value.filter(item => item.status === 'applied');
 case 'interviewing':
 return allData.value.filter(item => interviewStatuses.includes(item.status));
 case 'offer':
 return allData.value.filter(item => item.status === 'offer_received');
 case 'ended':
 return allData.value.filter(item => endedStatuses.includes(item.status));
 default:
 return [];
 }
}

async function loadBoardData() {
 loading.value = true;
 try {
 const res = await getApplications({ page: 1, size: 999 });
 if (res.code === 200) {
 allData.value = res.data.list || [];
 }
 } catch (e) {
 console.error('加载看板数据失败', e);
 } finally {
 loading.value = false;
 }
}

function handleAdd() {
 defaultStatus.value = 'interested';
 editData.value = null;
 formVisible.value = true;
}

function handleAddWithStatus(status) {
 defaultStatus.value = status === 'interviewing' ? 'screening' : status === 'offer' ? 'offer_received' : status;
 editData.value = null;
 formVisible.value = true;
}

function handleView(item) {
 detailData.value = item;
 detailVisible.value = true;
}

function handleEditFromDetail() {
 detailVisible.value = false;
 editData.value = { ...detailData.value };
 formVisible.value = true;
}

async function handleDeleteFromDetail() {
 try {
 await ElMessageBox.confirm('确定删除这条投递记录？', '提示', { type: 'warning' });
 detailVisible.value = false;
 await doDelete(detailData.value.id);
 } catch {
 }
}

async function moveToStatus(item, newStatus) {
 try {
 const res = await updateApplicationStatus(item.id, newStatus);
 if (res.code === 200) {
 ElMessage.success(`已移至「${statusLabel(newStatus)}」`);
 await loadBoardData();
 }
 } catch (e) {
 ElMessage.error('状态更新失败');
 }
}

async function handleDelete(item) {
 try {
 await ElMessageBox.confirm('确定删除这条投递记录？', '提示', { type: 'warning' });
 await doDelete(item.id);
 } catch {
 }
}

async function doDelete(id) {
 try {
 const res = await deleteApplication(id);
 if (res.code === 200) {
 ElMessage.success('已删除');
 await loadBoardData();
 }
 } catch (e) {
 ElMessage.error('删除失败');
 }
}

onMounted(() => {
 loadBoardData();
});
</script>

<style scoped>
.application-board {
  padding: 32px;
  background: #f5f5f5;
  min-height: calc(100vh - 60px);
  padding-top: 88px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  position: fixed;
  top: 60px;
  left: 0;
  right: 0;
  padding: 16px 32px;
  background: #f5f5f5;
  z-index: 50;
}

.page-header h2 {
  margin: 0;
  font-size: 22px;
  color: var(--ink-text-title);
  font-family: var(--ink-font-serif);
  font-weight: 600;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.board-container {
  display: flex;
  gap: 16px;
  overflow-x: auto;
  padding-bottom: 24px;
  height: calc(100vh - 196px);
}

.board-column {
  flex: 1;
  min-width: 260px;
  max-width: 320px;
  border-radius: var(--ink-radius-md);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: #fafafa;
}

.board-column.interviewing {
  background: #f7f7f7;
}

.board-column.ended {
  background: #fafafa;
}

.column-header {
  padding: 14px 16px;
  display: flex;
  align-items: center;
  position: relative;
  border-bottom: 1px solid #e8e8e8;
}

.column-bar {
  width: 2px;
  height: 20px;
  background: #ccc;
  margin-right: 10px;
}

.board-column.interviewing .column-bar {
  background: var(--ink-text-title);
}

.board-column.offer .column-bar {
  background: var(--ink-text-title);
}

.column-title {
  font-weight: 500;
  font-size: 16px;
  color: var(--ink-text-title);
  flex: 1;
}

.column-count {
  font-size: 12px;
  color: #666;
  background: #f0f0f0;
  padding: 2px 8px;
  border-radius: 6px;
}

.column-body {
  padding: 12px;
  flex: 1;
  overflow-y: auto;
}

.column-footer {
  padding: 12px;
  border-top: 1px solid #e8e8e8;
}

.btn-add {
  width: 100%;
  padding: 10px;
  border: 1px dashed #ccc;
  background: transparent;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 13px;
  color: #999;
  cursor: pointer;
  transition: all 0.2s var(--ink-ease);
}

.btn-add:hover {
  background: var(--ink-text-title);
  border-color: var(--ink-text-title);
  color: #fff;
}

.board-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 8px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  opacity: 0;
  animation: cardEnter 300ms var(--ink-ease) both;
  animation-delay: var(--stagger-delay);
  transition: all var(--ink-transition-normal);
}

.board-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(26, 26, 46, 0.08);
}

@keyframes cardEnter {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.card-bar {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background: var(--ink-text-title);
}

.card-content {
  padding-left: 12px;
}

.card-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.card-company {
  font-weight: 500;
  font-size: 14px;
  color: var(--ink-text-title);
}

.card-status {
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 4px;
}

.status-interested {
  background: transparent;
  border: 1px solid #999;
  color: #999;
}

.status-applied {
  background: transparent;
  border: 1px solid #666;
  color: #666;
}

.status-interview {
  background: #333;
  color: #fff;
}

.status-offer {
  background: var(--ink-text-title);
  color: #fff;
}

.status-ended {
  background: #f0f0f0;
  color: #999;
}

.card-job {
  font-size: 13px;
  color: #666;
  margin-bottom: 8px;
}

.card-date {
  font-size: 12px;
  color: #999;
  margin-bottom: 12px;
}

.interview-progress {
  display: flex;
  align-items: center;
  padding: 8px 0;
  margin-bottom: 12px;
  border-top: 1px solid #f5f5f5;
}

.progress-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
  position: relative;
}

.step-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #ddd;
  margin-bottom: 4px;
}

.step-pending .step-dot {
  border: 1px solid #ddd;
  background: transparent;
}

.step-done .step-dot {
  background: var(--ink-text-title);
}

.step-current .step-dot {
  background: var(--ink-text-title);
  box-shadow: 0 0 0 4px rgba(26, 26, 46, 0.1);
}

.step-label {
  font-size: 10px;
  color: #999;
}

.step-done .step-label,
.step-current .step-label {
  color: var(--ink-text-title);
}

.step-line {
  position: absolute;
  top: 4px;
  left: calc(50% + 8px);
  right: calc(50% - 8px);
  height: 1px;
  background: #ddd;
}

.step-done .step-line {
  background: var(--ink-text-title);
}

.card-actions {
  display: flex;
  gap: 4px;
  padding-top: 12px;
  border-top: 1px solid #f5f5f5;
  justify-content: flex-end;
  white-space: nowrap;
}

.btn-next {
  border-color: var(--ink-text-title) !important;
  color: var(--ink-text-title) !important;
  font-size: 12px;
  height: 28px;
  padding: 0 10px;
  border-radius: 8px !important;
}

.btn-next:hover {
  background: rgba(26, 26, 46, 0.05) !important;
}

.btn-offer {
  border-color: #52c41a !important;
  color: #52c41a !important;
  font-size: 12px;
  height: 28px;
  padding: 0 10px;
  border-radius: 8px !important;
}

.btn-offer:hover {
  background: rgba(82, 196, 26, 0.1) !important;
}

.btn-reject {
  border-color: #999 !important;
  color: #666 !important;
  font-size: 12px;
  height: 28px;
  padding: 0 10px;
  border-radius: 8px !important;
}

.btn-delete {
  color: #ff4d4f !important;
  font-size: 12px;
}

.btn-delete:hover {
  color: #d93636 !important;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 32px 16px;
}

.empty-icon {
  width: 48px;
  height: 48px;
  margin-bottom: 12px;
  opacity: 0.5;
}

.empty-state p {
  font-size: 13px;
  color: #999;
  margin: 0;
}

.status-tag {
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 4px;
}

@media (max-width: 1024px) {
  .board-container {
    height: calc(100vh - 180px);
  }
  
  .board-column {
    min-width: 220px;
  }
}

@media (max-width: 768px) {
  .page-header {
    padding: 12px 16px;
  }
  
  .page-header h2 {
    font-size: 18px;
  }
  
  .application-board {
    padding: 16px;
    padding-top: 100px;
  }
  
  .board-container {
    height: calc(100vh - 180px);
  }
}
</style>