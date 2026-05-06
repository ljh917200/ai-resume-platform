import { createRouter, createWebHistory } from 'vue-router'

/**
 * 路由配置（v2.0 AI求职助手平台）
 *
 * 结构：
 * - 登录/注册页面：独立，无导航栏
 * - 其他所有页面：嵌套在 MainLayout 下，共享顶部Tab导航
 * - 根路径重定向到 /dashboard
 */

const routes = [
  {
    path: '/',
    redirect: '/dashboard'
  },
  // ====== 独立页面（无导航栏） ======
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue'),
    meta: { requiresAuth: false }
  },
  // ====== 主页面（共享Layout导航） ======
  {
    path: '/',
    component: () => import('../components/layout/MainLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      // 首页导航中心
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue'),
        meta: { requiresAuth: true, title: '首页' }
      },
      // 投递看板
      {
        path: 'application/list',
        name: 'ApplicationList',
        component: () => import('../views/application/ApplicationList.vue'),
        meta: { requiresAuth: true, title: '投递列表' }
      },
      {
        path: 'application/board',
        name: 'ApplicationBoard',
        component: () => import('../views/application/ApplicationBoard.vue'),
        meta: { requiresAuth: true, title: '投递看板' }
      },
      // AI岗位匹配（占位）
      {
        path: 'job-match',
        name: 'JobMatch',
        component: () => import('../views/jobMatch/JobMatch.vue'),
        meta: { requiresAuth: true, title: 'AI岗位匹配' }
      },
      // AI面试助手（占位）
      {
        path: 'interview',
        name: 'InterviewHome',
        component: () => import('../views/interview/InterviewHome.vue'),
        meta: { requiresAuth: true, title: 'AI面试助手' }
      },
      // AI求职信（占位）
      {
        path: 'cover-letter',
        name: 'CoverLetterList',
        component: () => import('../views/coverLetter/CoverLetterList.vue'),
        meta: { requiresAuth: true, title: 'AI求职信' }
      },
      // 简历相关页面（原有）
      {
        path: 'home',
        name: 'Home',
        component: () => import('../views/Home.vue'),
        meta: { requiresAuth: true, title: '简历管理' }
      },
      {
        path: 'preview/:id',
        name: 'Preview',
        component: () => import('../views/Preview.vue'),
        meta: { requiresAuth: true, title: '简历预览' }
      },
      {
        path: 'history/:resumeId',
        name: 'OptimizeHistory',
        component: () => import('../views/History.vue'),
        meta: { requiresAuth: true, title: '优化历史' }
      },
      // 个人中心
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/Profile.vue'),
        meta: { requiresAuth: true, title: '个人中心' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫：未登录跳转登录页
router.beforeEach((to, from) => {
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - AI求职助手`
  }

  // 权限校验
  if (to.meta.requiresAuth) {
    const token = localStorage.getItem('token')
    if (!token) {
      return { name: 'Login', query: { redirect: to.fullPath } }
    }
  }
})

export default router