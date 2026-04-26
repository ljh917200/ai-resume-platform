import request from '../utils/request'

/**
 * 获取某份简历的优化历史列表
 * @param {number} resumeId - 简历ID
 * @returns {Promise}
 */
export function getHistoryList(resumeId) {
    return request.get(`/history/list/${resumeId}`)
}

/**
 * 获取优化历史详情
 * @param {number} id - 历史记录ID
 * @returns {Promise}
 */
export function getHistoryDetail(id) {
    return request.get(`/history/${id}`)
}