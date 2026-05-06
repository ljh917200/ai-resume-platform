/**
 * 投递记录相关API
 */
import request from '@/utils/request'

/**
 * 创建投递记录
 */
export function createApplication(data) {
    return request({
        url: '/applications',
        method: 'post',
        data
    })
}

/**
 * 查询投递记录列表（分页+搜索+过滤）
 * @param {Object} params - { status, keyword, source, page, size }
 */
export function getApplications(params) {
    return request({
        url: '/applications',
        method: 'get',
        params
    })
}

/**
 * 获取投递记录详情
 */
export function getApplication(id) {
    return request({
        url: `/applications/${id}`,
        method: 'get'
    })
}

/**
 * 更新投递记录
 */
export function updateApplication(id, data) {
    return request({
        url: `/applications/${id}`,
        method: 'put',
        data
    })
}

/**
 * 删除投递记录
 */
export function deleteApplication(id) {
    return request({
        url: `/applications/${id}`,
        method: 'delete'
    })
}

/**
 * 批量删除投递记录
 * @param {Array} ids - ID列表
 */
export function batchDeleteApplications(ids) {
    return request({
        url: '/applications/batch',
        method: 'delete',
        data: { ids }
    })
}

/**
 * 更新投递状态
 * @param {Number} id - 投递记录ID
 * @param {String} status - 新状态
 */
export function updateApplicationStatus(id, status) {
    return request({
        url: `/applications/${id}/status`,
        method: 'put',
        data: { status }
    })
}

/**
 * 获取看板数据（按状态分组统计）
 */
export function getBoardData() {
    return request({
        url: '/applications/board',
        method: 'get'
    })
}

/**
 * 获取统计数据
 */
export function getApplicationStats() {
    return request({
        url: '/applications/stats',
        method: 'get'
    })
}
