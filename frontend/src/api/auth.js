import request from '../utils/request'

// 注册
export function register(data) {
    return request.post('/auth/register', data)
}

// 用户名登录
export function loginByUsername(username, password) {
    return request.post('/auth/login/username', { username, password })
}

// 邮箱登录
export function loginByEmail(email, password) {
    return request.post('/auth/login/email', { email, password })
}