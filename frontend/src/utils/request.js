import axios from 'axios'
import { ElMessage, ElLoading } from 'element-plus'

// 创建axios实例
// VITE_API_BASE_URL 是环境变量，会根据开发/生产环境自动切换
const request = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL,
    timeout: 30000
})

// ========== 全局Loading控制 ==========
let loadingInstance = null
let requestCount = 0

/**
 * 显示全局Loading
 */
const showLoading = () => {
    if (requestCount === 0) {
        loadingInstance = ElLoading.service({
            lock: true,
            text: '加载中...',
            background: 'rgba(255, 255, 255, 0.7)'
        })
    }
    requestCount++
}

/**
 * 隐藏全局Loading
 */
const hideLoading = () => {
    requestCount--
    if (requestCount <= 0) {
        requestCount = 0
        loadingInstance?.close()
    }
}

// ========== 请求拦截器 ==========
request.interceptors.request.use(
    config => {
        // 添加token
        const token = localStorage.getItem('token')
        if (token) {
            config.headers.Authorization = `Bearer ${token}`
        }

        // 显示loading（除非明确指定hideLoading: true）
        if (!config.hideLoading) {
            showLoading()
        }

        return config
    },
    error => {
        hideLoading()
        return Promise.reject(error)
    }
)

// ========== 响应拦截器 ==========
request.interceptors.response.use(
    response => {
        hideLoading()
        return response.data
    },
    error => {
        hideLoading()

        // 处理不同类型的错误
        if (error.code === 'ECONNABORTED' || error.message.includes('timeout')) {
            ElMessage.error('请求超时，请检查网络连接')
        } else if (error.message === 'Network Error') {
            ElMessage.error('网络连接失败，请检查网络')
        } else if (error.response) {
            // HTTP状态码错误
            const status = error.response.status
            switch (status) {
                case 401:
                    ElMessage.error('登录已过期，请重新登录')
                    localStorage.removeItem('token')
                    localStorage.removeItem('user')
                    window.location.href = '/login'
                    break
                case 403:
                    ElMessage.error('没有权限访问该资源')
                    break
                case 404:
                    ElMessage.error('请求的资源不存在')
                    break
                case 500:
                    ElMessage.error('服务器内部错误，请稍后重试')
                    break
                default:
                    ElMessage.error(`请求失败(${status})`)
            }
        } else {
            ElMessage.error('请求失败，请稍后重试')
        }

        return Promise.reject(error)
    }
)

export default request

















