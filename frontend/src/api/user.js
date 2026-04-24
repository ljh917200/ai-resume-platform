/**
 * 用户相关API封装
 * 提供用户个人中心、用户信息管理等接口调用
 */

import request from '../utils/request'

/**
 * 获取当前登录用户的基本信息
 * @returns Promise 返回用户基本信息
 */
export function getUserProfile() {
    return request.get('/user/profile')
}

/**
 * 修改用户显示名称
 * @param {string} username - 新的用户名
 * @returns Promise 返回修改结果
 */
export function updateUsername(username) {
    return request.put('/user/username', { username })
}

/**
 * 绑定或修改用户邮箱
 * @param {string} email - 新的邮箱地址
 * @returns Promise 返回修改结果
 */
export function updateEmail(email) {
    return request.put('/user/email', { email })
}

/**
 * 修改用户密码
 * 需要验证原密码才能修改
 * @param {string} oldPassword - 原密码
 * @param {string} newPassword - 新密码
 * @returns Promise 返回修改结果
 */
export function updatePassword(oldPassword, newPassword) {
    return request.put('/user/password', { oldPassword, newPassword })
}

/**
 * 获取用户统计数据
 * 包括：简历数量、优化次数、已用额度、加入天数
 * @returns Promise 返回统计数据
 */
export function getUserStatistics() {
    return request.get('/user/statistics')
}