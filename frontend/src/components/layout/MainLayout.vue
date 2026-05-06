<template>
  <div class="main-layout">
    <!-- 顶部导航栏 -->
    <header class="top-nav">
      <!-- Logo区域 -->
      <div class="nav-left" @click="$router.push('/dashboard')">
        <div class="logo-icon">
          <span class="logo-text-inner">墨</span>
        </div>
        <span class="logo-text">AI求职助手</span>
      </div>

      <!-- 模块Tab导航 -->
      <nav class="nav-tabs">
        <div
          v-for="tab in tabs"
          :key="tab.path"
          :class="['nav-tab', { active: isTabActive(tab) }]"
          @click="handleTabClick(tab)"
          :ref="el => setTabRef(tab.path, el)"
        >
          <el-icon :size="16"><component :is="tab.icon" /></el-icon>
          <span>{{ tab.label }}</span>
          <el-tag v-if="tab.dev" size="small" type="warning" effect="plain" class="dev-tag">开发中</el-tag>
        </div>
        <!-- 滑动指示条 -->
        <div 
          class="nav-indicator"
          :style="indicatorStyle"
        ></div>
      </nav>

      <!-- 移动端菜单按钮 -->
      <button class="mobile-menu-btn" @click="mobileMenuOpen = !mobileMenuOpen">
        <el-icon><Menu /></el-icon>
      </button>

      <!-- 用户操作区 -->
      <div class="nav-right">
        <el-dropdown @visible-change="handleDropdownVisible" @command="handleDropdownCommand">
          <div class="user-avatar">
            <el-avatar v-if="userAvatarUrl" :size="36" :src="userAvatarUrl" />
            <el-avatar v-else :size="36">
              {{ userName.charAt(0).toUpperCase() }}
            </el-avatar>
          </div>
          <template #dropdown>
            <Transition name="dropdown">
              <el-dropdown-menu>
                <el-dropdown-item command="dashboard">
                  <el-icon><HomeFilled /></el-icon> 首页
                </el-dropdown-item>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon> 个人中心
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon> 退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </Transition>
          </template>
        </el-dropdown>
      </div>
    </header>

    <!-- 移动端菜单 -->
    <Transition name="slide-down">
      <div v-if="mobileMenuOpen" class="mobile-menu-overlay" @click="mobileMenuOpen = false">
        <div class="mobile-menu" @click.stop>
          <div
            v-for="tab in tabs"
            :key="tab.path"
            :class="['mobile-tab', { active: isTabActive(tab) }]"
            @click="handleMobileTabClick(tab)"
          >
            <el-icon :size="18"><component :is="tab.icon" /></el-icon>
            <span>{{ tab.label }}</span>
            <el-tag v-if="tab.dev" size="small" type="warning" effect="plain">开发中</el-tag>
          </div>
        </div>
      </div>
    </Transition>

    <!-- 页面内容区域 -->
    <main class="main-content">
      <router-view v-slot="{ Component }">
        <Transition name="fade-slide" mode="out-in">
          <component :is="Component" :key="$route.fullPath" />
        </Transition>
      </router-view>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  Collection, Aim, ChatDotRound, Document, EditPen,
  HomeFilled, User, SwitchButton, Menu
} from '@element-plus/icons-vue'
import { getUserProfile } from '@/api/user'

const router = useRouter()
const route = useRoute()

// ====== 用户信息 ======
const userName = ref('用户')
const userAvatarUrl = ref('')

// ====== 移动端菜单 ======
const mobileMenuOpen = ref(false)

// ====== Tab引用 ======
const tabRefs = ref({})

function setTabRef(path, el) {
  if (el) {
    tabRefs.value[path] = el
  }
}

// ====== 指示条样式 ======
const indicatorStyle = ref({
  left: '0px',
  width: '0px',
  opacity: '0'
})

// ====== 模块Tab配置 ======
const tabs = [
  {
    label: '投递看板',
    icon: Collection,
    path: '/application/board',
    matchPaths: ['/application/list', '/application/board'],
    dev: false
  },
  {
    label: '岗位匹配',
    icon: Aim,
    path: '/job-match',
    matchPaths: ['/job-match'],
    dev: true
  },
  {
    label: '面试助手',
    icon: ChatDotRound,
    path: '/interview',
    matchPaths: ['/interview'],
    dev: true
  },
  {
    label: '简历优化',
    icon: Document,
    path: '/home',
    matchPaths: ['/home', '/preview', '/history'],
    dev: false
  },
  {
    label: '求职信',
    icon: EditPen,
    path: '/cover-letter',
    matchPaths: ['/cover-letter'],
    dev: true
  }
]

// ====== Dashboard匹配路径 ======
const dashboardMatchPaths = ['/dashboard', '/profile']

// ====== 判断当前Tab是否激活 ======
function isTabActive(tab) {
  const currentPath = route.path
  // 特殊处理Dashboard路径
  if (dashboardMatchPaths.some(p => currentPath.startsWith(p))) {
    return false
  }
  if (currentPath === tab.path) return true
  return tab.matchPaths.some(p => currentPath.startsWith(p))
}

// ====== 获取当前激活的Tab路径 ======
const activeTabPath = computed(() => {
  const currentPath = route.path
  // Dashboard页面时指示条隐藏
  if (dashboardMatchPaths.some(p => currentPath.startsWith(p))) {
    return null
  }
  return tabs.find(tab => isTabActive(tab))?.path || tabs[0]?.path
})

// ====== 更新指示条位置 ======
function updateIndicator() {
  nextTick(() => {
    const activePath = activeTabPath.value
    
    // Dashboard页面时隐藏指示条
    if (!activePath) {
      indicatorStyle.value = {
        left: '0px',
        width: '0px',
        opacity: '0'
      }
      return
    }
    
    const activeTabEl = tabRefs.value[activePath]
    
    if (activeTabEl) {
      const rect = activeTabEl.getBoundingClientRect()
      const tabsRect = activeTabEl.parentElement.getBoundingClientRect()
      
      indicatorStyle.value = {
        left: `${rect.left - tabsRect.left}px`,
        width: `${rect.width}px`,
        opacity: '1'
      }
    } else {
      indicatorStyle.value = {
        left: '0px',
        width: '0px',
        opacity: '0'
      }
    }
  })
}

// ====== Tab点击处理 ======
function handleTabClick(tab) {
  if (!isTabActive(tab)) {
    router.push(tab.path)
  }
}

// ====== 移动端Tab点击处理 ======
function handleMobileTabClick(tab) {
  router.push(tab.path)
  mobileMenuOpen.value = false
}

// ====== 下拉菜单可见性变化 ======
function handleDropdownVisible(visible) {
  if (!visible) {
    // 下拉菜单关闭时的处理
  }
}

// ====== 下拉菜单命令处理 ======
function handleDropdownCommand(command) {
  switch (command) {
    case 'dashboard':
      router.push('/dashboard')
      break
    case 'profile':
      router.push('/profile')
      break
    case 'logout':
      logout()
      break
  }
}

// ====== 加载用户信息 ======
async function loadUserProfile() {
  try {
    const res = await getUserProfile()
    if (res.code === 200) {
      const profile = res.data
      userName.value = profile.username || '用户'
      userAvatarUrl.value = profile.avatarUrl || ''
    }
  } catch (e) {
    console.error('加载用户信息失败', e)
  }
}

// ====== 退出登录 ======
function logout() {
  localStorage.removeItem('token')
  router.push('/login')
}

// ====== 监听路由变化 ======
watch(() => route.path, () => {
  updateIndicator()
})

onMounted(() => {
  loadUserProfile()
  updateIndicator()
  
  // 监听窗口resize
  window.addEventListener('resize', updateIndicator)
})
</script>

<style scoped>
/* 主布局容器 */
.main-layout {
  min-height: 100vh;
  background: var(--ink-bg-page);
  display: flex;
  flex-direction: column;
}

/* 顶部导航栏 */
.top-nav {
  display: flex;
  align-items: center;
  height: 60px;
  background: #ffffff;
  border-bottom: 1px solid #e8e8e8;
  padding: 0 24px;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
}

/* Logo区域 */
.nav-left {
  display: flex;
  align-items: center;
  cursor: pointer;
  width: 200px;
  flex-shrink: 0;
}

.logo-icon {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--ink-text-title);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 10px;
  flex-shrink: 0;
}

.logo-text-inner {
  color: #ffffff;
  font-family: var(--ink-font-serif);
  font-size: 16px;
  font-weight: 600;
}

.logo-text {
  font-size: 18px;
  font-weight: 600;
  color: var(--ink-text-title);
  font-family: var(--ink-font-serif);
  white-space: nowrap;
}

/* Tab导航区域 */
.nav-tabs {
  display: flex;
  align-items: center;
  gap: 24px;
  flex: 1;
  position: relative;
  padding: 0 16px;
}

/* 单个Tab */
.nav-tab {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  cursor: pointer;
  font-size: 14px;
  color: #999999;
  white-space: nowrap;
  transition: color 0.3s var(--ink-ease);
  position: relative;
  border-radius: var(--ink-radius-xs);
}

.nav-tab:hover {
  color: var(--ink-text-title);
  background: rgba(26, 26, 46, 0.03);
}

.nav-tab.active {
  color: var(--ink-text-title);
  font-weight: 500;
}

/* 滑动指示条 */
.nav-indicator {
  position: absolute;
  bottom: -1px;
  height: 2px;
  background: var(--ink-text-title);
  border-radius: 1px;
  transition: left 0.3s var(--ink-ease), width 0.3s var(--ink-ease), opacity 0.2s var(--ink-ease);
  pointer-events: none;
}

/* 开发中标签 */
.dev-tag {
  font-size: 10px;
  padding: 0 4px;
  height: 16px;
  line-height: 16px;
  border-radius: 4px;
  background: rgba(201, 166, 90, 0.1);
  border: 1px solid var(--ink-warning);
  color: var(--ink-warning);
}

/* 移动端菜单按钮 */
.mobile-menu-btn {
  display: none;
  background: none;
  border: none;
  cursor: pointer;
  padding: 8px;
  color: var(--ink-text-secondary);
  font-size: 20px;
}

.mobile-menu-btn:hover {
  color: var(--ink-text-title);
}

/* 右侧用户区 */
.nav-right {
  margin-left: auto;
  flex-shrink: 0;
}

.user-avatar {
  cursor: pointer;
  transition: transform 0.2s var(--ink-ease);
}

.user-avatar .el-avatar {
  border: 1px solid #e8e8e8;
  transition: border-color 0.2s var(--ink-ease);
}

.user-avatar:hover {
  transform: scale(1.05);
}

.user-avatar:hover .el-avatar {
  border-color: var(--ink-text-title);
}

/* 下拉菜单过渡 */
.dropdown-enter-active,
.dropdown-leave-active {
  animation: dropdownSlide 0.2s var(--ink-ease) both;
}

.dropdown-enter-active {
  animation-name: dropdownEnter;
}

.dropdown-leave-active {
  animation-name: dropdownLeave;
}

@keyframes dropdownEnter {
  from {
    opacity: 0;
    transform: translateY(-8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes dropdownLeave {
  from {
    opacity: 1;
    transform: translateY(0);
  }
  to {
    opacity: 0;
    transform: translateY(-8px);
  }
}

/* 移动端菜单 */
.mobile-menu-overlay {
  position: fixed;
  top: 60px;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.3);
  z-index: 99;
}

.mobile-menu {
  background: #ffffff;
  border-bottom: 1px solid #e8e8e8;
  padding: 8px 0;
}

.mobile-tab {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 24px;
  cursor: pointer;
  font-size: 15px;
  color: var(--ink-text-secondary);
  transition: background 0.2s var(--ink-ease), color 0.2s var(--ink-ease);
}

.mobile-tab:hover {
  background: var(--ink-bg-section);
}

.mobile-tab.active {
  color: var(--ink-text-title);
  font-weight: 500;
  background: rgba(26, 26, 46, 0.03);
}

/* 页面内容区域 */
.main-content {
  flex: 1;
  overflow-y: auto;
  padding-top: 60px;
}

/* 页面过渡动画 */
.fade-slide-enter-active {
  animation: fadeSlideIn 400ms var(--ink-ease) both;
}

.fade-slide-leave-active {
  animation: fadeSlideOut 200ms var(--ink-ease) both;
}

@keyframes fadeSlideIn {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fadeSlideOut {
  from {
    opacity: 1;
    transform: translateY(0);
  }
  to {
    opacity: 0;
    transform: translateY(-8px);
  }
}

/* 移动端菜单滑动动画 */
.slide-down-enter-active,
.slide-down-leave-active {
  animation: slideDown 0.25s var(--ink-ease) both;
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
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes slideDownLeave {
  from {
    opacity: 1;
    transform: translateY(0);
  }
  to {
    opacity: 0;
    transform: translateY(-10px);
  }
}

/* 响应式 */
@media (max-width: 992px) {
  .nav-tab span:not(.dev-tag) {
    font-size: 13px;
  }
  
  .nav-tabs {
    gap: 16px;
  }
  
  .nav-tab {
    padding: 6px 8px;
    gap: 4px;
  }
}

@media (max-width: 768px) {
  .nav-left {
    width: auto;
  }
  
  .logo-text {
    display: none;
  }
  
  .logo-icon {
    margin-right: 0;
  }
  
  .nav-tabs {
    display: none;
  }
  
  .mobile-menu-btn {
    display: flex;
    align-items: center;
    justify-content: center;
  }
  
  .nav-right {
    margin-left: 12px;
  }
  
  .top-nav {
    padding: 0 16px;
  }
}
</style>
