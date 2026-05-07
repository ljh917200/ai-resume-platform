<template>
  <div class="dashboard-container">
    <!-- 欢迎区域 -->
    <div class="welcome-card ink-slide-up">
      <div class="welcome-left">
        <h1>你好，{{ userName }}</h1>
        <p class="welcome-subtitle">今日宜投递，机会在前方</p>
      </div>
      <div class="welcome-right">
        <div class="current-date">{{ currentDate }}</div>
        <div class="current-time">{{ currentTime }}</div>
      </div>
    </div>

    <!-- 统计卡片区域 -->
    <div class="section">
      <div class="section-header">
        <h2 class="section-title">求职概览</h2>
      </div>
      <div class="stats-grid">
        <div 
          v-for="(stat, index) in statsList" 
          :key="stat.label"
          class="stat-card"
          :style="{ '--stagger-delay': `${index * 80}ms` }"
        >
          <div class="stat-bar"></div>
          <div class="stat-content">
            <span class="stat-label">{{ stat.label }}</span>
            <div class="stat-number" ref="statRefs">{{ animatedNumbers[index] || 0 }}</div>
            <div class="stat-trend" :class="stat.trend">
              <el-icon v-if="stat.trend === 'up'"><ArrowUp /></el-icon>
              <el-icon v-else-if="stat.trend === 'down'"><ArrowDown /></el-icon>
              <span>: {{ stat.change }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 功能入口区域 -->
    <div class="section">
      <div class="section-header">
        <h2 class="section-title">快捷入口</h2>
      </div>
      <div class="module-grid">
        <div 
          v-for="(module, index) in modules" 
          :key="module.path"
          class="module-card"
          :style="{ '--stagger-delay': `${index * 100}ms` }"
          @click="$router.push(module.path)"
        >
          <div class="module-icon">
            <el-icon :size="28"><component :is="module.icon" /></el-icon>
          </div>
          <h3 class="module-name">{{ module.name }}</h3>
          <p class="module-desc">{{ module.desc }}</p>
          <div class="module-arrow">
            <el-icon><ArrowRight /></el-icon>
          </div>
        </div>
      </div>
    </div>

    <!-- 最近动态区域 -->
    <div class="section">
      <div class="section-header">
        <h2 class="section-title">最近动态</h2>
        <a href="/application/list" class="view-all">查看全部 →</a>
      </div>
      <div class="timeline">
        <div 
          v-for="(item, index) in recentActivities" 
          :key="index"
          class="timeline-item"
          :style="{ '--stagger-delay': `${index * 60}ms` }"
        >
          <div class="timeline-node"></div>
          <div class="timeline-line"></div>
          <div class="timeline-content">
            <span class="timeline-time">{{ item.time }}</span>
            <p class="timeline-desc">{{ item.desc }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'

import { Collection, Aim, ChatDotRound, Document, ArrowRight, ArrowUp, ArrowDown } from '@element-plus/icons-vue'

import { getApplicationStats } from '@/api/application'
import { getResumeList } from '@/api/resume'
import { getUserProfile } from '@/api/user'

// ====== 用户信息 ======
const userName = ref('用户')

// ====== 日期时间 ======
const currentDate = ref('')
const currentTime = ref('')
let timeInterval = null

// ====== 统计数据 ======
const stats = ref({
  total: 0,
  interviewing: 0,
  offered: 0,
  thisWeekAdded: 0
})

const statsList = ref([
  { label: '简历总数', value: 0, trend: 'up', change: '+12%' },
  { label: '投递总数', value: 0, trend: 'up', change: '+8%' },
  { label: '面试中', value: 0, trend: 'up', change: '+3' },
  { label: '已Offer', value: 0, trend: 'down', change: '-1' }
])

const animatedNumbers = ref([0, 0, 0, 0])
const statRefs = ref([])

// ====== 功能模块 ======
const modules = [
  {
    name: '投递看板',
    desc: '管理投递记录，看板式追踪求职进度',
    icon: Collection,
    path: '/application/board'
  },
  {
    name: '岗位匹配',
    desc: '粘贴JD，智能分析匹配度与差距',
    icon: Aim,
    path: '/job-match'
  },
  {
    name: '面试助手',
    desc: '生成面试题，追踪准备状态',
    icon: ChatDotRound,
    path: '/interview'
  },
  {
    name: 'AI简历优化',
    desc: 'AI优化简历，多模板预览与PDF导出',
    icon: Document,
    path: '/home'
  }
]

// ====== 最近动态 ======
const recentActivities = ref([
  { time: '10分钟前', desc: '更新了简历「后端开发工程师.pdf」' },
  { time: '1小时前', desc: '投递了「阿里巴巴 - Java开发工程师」' },
  { time: '3小时前', desc: '收到「字节跳动」的面试邀请' },
  { time: '昨天', desc: 'AI优化了简历，匹配度提升至85%' },
  { time: '昨天', desc: '投递了「腾讯 - 后端开发」岗位' }
])

// ====== 更新时间 ======
function updateTime() {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  const weekDay = weekDays[now.getDay()]
  
  currentDate.value = `${year}年${month}月${day}日 ${weekDay}`
  currentTime.value = now.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

// ====== 数字滚动动画 ======
function animateNumber(index, target, duration = 1200) {
  const start = 0
  const startTime = performance.now()
  
  function update(currentTime) {
    const elapsed = currentTime - startTime
    const progress = Math.min(elapsed / duration, 1)
    
    // 使用缓动函数
    const easeProgress = 1 - Math.pow(1 - progress, 3)
    const current = Math.floor(start + (target - start) * easeProgress)
    
    animatedNumbers.value[index] = current
    
    if (progress < 1) {
      requestAnimationFrame(update)
    }
  }
  
  requestAnimationFrame(update)
}

// ====== 加载数据 ======
async function loadData() {
  try {
    const [userRes, statsRes, resumeRes] = await Promise.all([
      getUserProfile(),
      getApplicationStats(),
      getResumeList()
    ])
    
    // 用户信息
    if (userRes.data.code === 200) {
      userName.value = userRes.data.data.username || '用户'
    }
    
    // 统计数据
    if (statsRes.data.code === 200) {
      stats.value = statsRes.data.data
    }
    
    // 简历数量
    let resumeCount = 0
    if (resumeRes.data.code === 200) {
      resumeCount = (resumeRes.data.data || []).length
    }
    
    // 更新统计列表
    statsList.value = [
      { label: '简历总数', value: resumeCount, trend: resumeCount > 0 ? 'up' : '', change: resumeCount > 0 ? `+${resumeCount}` : '' },
      { label: '投递总数', value: stats.value.total || 0, trend: 'up', change: '+8%' },
      { label: '面试中', value: stats.value.interviewing || 0, trend: 'up', change: '+3' },
      { label: '已Offer', value: stats.value.offered || 0, trend: stats.value.offered > 0 ? 'up' : '', change: stats.value.offered > 0 ? `+${stats.value.offered}` : '' }
    ]
    
    // 延迟启动数字动画
    nextTick(() => {
      setTimeout(() => {
        statsList.value.forEach((stat, index) => {
          animateNumber(index, stat.value)
        })
      }, 500)
    })
    
  } catch (e) {
    console.error('加载数据失败', e)
  }
}

onMounted(() => {
  updateTime()
  timeInterval = setInterval(updateTime, 1000)
  loadData()
})

onUnmounted(() => {
  if (timeInterval) {
    clearInterval(timeInterval)
  }
})
</script>

<style scoped>
/* 整体容器 */
.dashboard-container {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 32px;
  max-width: 1200px;
  margin: 0 auto;
  animation: pageFadeIn 300ms var(--ink-ease) both;
}

@keyframes pageFadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

/* 区域间距 */
.section {
  margin-top: 32px;
}

/* 区域标题 */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--ink-border);
}

.section-title {
  font-size: 18px;
  font-family: var(--ink-font-serif);
  color: var(--ink-text-title);
  font-weight: 600;
  margin: 0;
}

.view-all {
  font-size: 13px;
  color: var(--ink-text-secondary);
  text-decoration: none;
  transition: color var(--ink-transition-fast);
}

.view-all:hover {
  color: var(--ink-text-title);
}

/* 欢迎卡片 */
.welcome-card {
  background: #ffffff;
  border-radius: var(--ink-radius-md);
  padding: 28px 32px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: var(--ink-shadow-sm);
}

.welcome-left h1 {
  font-size: 28px;
  font-family: var(--ink-font-serif);
  color: var(--ink-text-title);
  margin: 0;
  font-weight: 600;
}

.welcome-subtitle {
  font-size: 14px;
  color: var(--ink-text-secondary);
  margin: 8px 0 0 0;
}

.welcome-right {
  text-align: right;
}

.current-date {
  font-size: 14px;
  color: var(--ink-text-secondary);
  margin-bottom: 4px;
}

.current-time {
  font-size: 18px;
  font-family: var(--ink-font-mono);
  color: var(--ink-text-title);
  font-weight: 500;
}

/* 统计卡片网格 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.stat-card {
  background: #ffffff;
  border-radius: var(--ink-radius-md);
  padding: 24px;
  position: relative;
  overflow: hidden;
  opacity: 0;
  animation: statCardEnter 400ms var(--ink-ease) both;
  animation-delay: var(--stagger-delay);
  transition: all var(--ink-transition-normal);
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--ink-shadow-lg);
}

@keyframes statCardEnter {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.stat-bar {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background: var(--ink-primary);
}

.stat-content {
  padding-left: 12px;
}

.stat-label {
  font-size: 12px;
  color: var(--ink-text-secondary);
}

.stat-number {
  font-size: 36px;
  font-family: var(--ink-font-serif);
  color: var(--ink-text-title);
  font-weight: 600;
  margin: 8px 0;
  line-height: 1;
}

.stat-trend {
  font-size: 12px;
  color: var(--ink-text-secondary);
  display: flex;
  align-items: center;
  gap: 4px;
  justify-content: flex-end;
}

.stat-trend.up {
  color: var(--ink-text-secondary);
}

.stat-trend.down {
  color: var(--ink-text-secondary);
}

/* 功能模块网格 */
.module-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.module-card {
  background: #ffffff;
  border-radius: var(--ink-radius-md);
  padding: 24px;
  cursor: pointer;
  position: relative;
  opacity: 0;
  animation: moduleCardEnter 400ms var(--ink-ease) both;
  animation-delay: var(--stagger-delay);
  transition: all var(--ink-transition-normal);
}

.module-card:hover {
  transform: translateY(-6px);
  box-shadow: var(--ink-shadow-lg);
}

@keyframes moduleCardEnter {
  from {
    opacity: 0;
    transform: translateY(16px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.module-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: var(--ink-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  margin-bottom: 16px;
  transition: transform var(--ink-transition-normal);
}

.module-card:hover .module-icon {
  transform: scale(1.05);
}

.module-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--ink-text-title);
  margin: 0 0 8px 0;
}

.module-desc {
  font-size: 12px;
  color: #999999;
  margin: 0;
  line-height: 1.5;
}

.module-arrow {
  position: absolute;
  right: 24px;
  bottom: 24px;
  color: var(--ink-text-secondary);
  transition: all var(--ink-transition-normal);
}

.module-card:hover .module-arrow {
  transform: translateX(4px);
}

/* 时间线 */
.timeline {
  background: #ffffff;
  border-radius: var(--ink-radius-md);
  padding: 24px;
  box-shadow: var(--ink-shadow-sm);
}

.timeline-item {
  display: flex;
  position: relative;
  padding-bottom: 20px;
  opacity: 0;
  animation: timelineItemEnter 300ms var(--ink-ease) both;
  animation-delay: var(--stagger-delay);
}

.timeline-item:last-child {
  padding-bottom: 0;
}

.timeline-item:last-child .timeline-line {
  display: none;
}

@keyframes timelineItemEnter {
  from {
    opacity: 0;
    transform: translateX(-10px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.timeline-node {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--ink-primary);
  flex-shrink: 0;
  margin-right: 16px;
  z-index: 1;
}

.timeline-line {
  position: absolute;
  left: 4px;
  top: 14px;
  bottom: 0;
  width: 2px;
  background: var(--ink-border);
}

.timeline-content {
  flex: 1;
}

.timeline-time {
  font-size: 12px;
  color: var(--ink-text-secondary);
  margin-right: 12px;
}

.timeline-desc {
  font-size: 14px;
  color: var(--ink-text-title);
  margin: 0;
  display: inline;
}

/* 响应式 */
@media (max-width: 1024px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .module-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .dashboard-container {
    padding: 16px;
  }
  
  .welcome-card {
    flex-direction: column;
    text-align: center;
    gap: 16px;
  }
  
  .welcome-right {
    text-align: center;
  }
  
  .welcome-left h1 {
    font-size: 24px;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .module-grid {
    grid-template-columns: 1fr;
  }
  
  .stat-number {
    font-size: 32px;
  }
}

@media (max-width: 480px) {
  .dashboard-container {
    padding: 12px;
  }
  
  .welcome-card {
    padding: 20px;
  }
  
  .welcome-left h1 {
    font-size: 22px;
  }
  
  .stat-card,
  .module-card {
    padding: 16px;
  }
  
  .stat-number {
    font-size: 28px;
  }
}
</style>
