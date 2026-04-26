import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/home'
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('../views/Login.vue')
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('../views/Register.vue')
    },
    {
      path: '/home',
      name: 'Home',
      component: () => import('../views/Home.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/optimize',
      name: 'Optimize',
      component: () => import('../views/Optimize.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/profile',
      name: 'Profile',
      component: () => import('../views/Profile.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/resume/:id',           // 新增：简历详情页路由
      name: 'ResumeDetail',
      component: () => import('../views/Detail.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/history/:resumeId',
      name: 'OptimizeHistory',
      component: () => import('../views/History.vue'),
      meta: { requiresAuth: true }
    }
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')

  // 访问登录/注册页时，如果已登录则跳首页
  if ((to.path === '/login' || to.path === '/register') && token) {
    next('/home')
    return
  }

  // 需要登录的页面，没token则跳登录页
  if (to.meta.requiresAuth && !token) {
    next('/login')
    return
  }

  next()
})

export default router