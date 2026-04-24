import axios from 'axios'

// 创建 axios 实例
// VITE_API_BASE_URL 是环境变量，会根据开发/生产环境自动切换
const request = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL,
    timeout: 30000
})

// 请求拦截器：自动带上 Token
request.interceptors.request.use(
    config => {
        const token = localStorage.getItem('token')
        if (token) {
            config.headers.Authorization = `Bearer ${token}`
        }
        return config
    },
    error => {
        return Promise.reject(error)
    }
)

// 响应拦截器：处理 401 错误
request.interceptors.response.use(
    response => {
        return response.data
    },
    error => {
        if (error.response && error.response.status === 401) {
            // Token 过期，清除本地存储，跳转登录页
            localStorage.removeItem('token')
            localStorage.removeItem('user')
            window.location.href = '/login'
        }
        return Promise.reject(error)
    }
)

export default request