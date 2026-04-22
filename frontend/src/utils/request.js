import axios from 'axios'

// 创建 axios 实例
const request = axios.create({
    baseURL: 'http://localhost:8080/api',  // 后端地址
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