import request from '@/utils/request'

/**
 * 发起岗位匹配分析
 * @param {Object} data - { resumeId, jobTitle, jobDescription, companyName }
 */
export function analyzeMatch(data) {
    return request({
        url: '/job-match/analyze',
        method: 'post',
        data
    })
}

/**
 * 查询单条分析结果
 * @param {Number} id - 分析记录ID
 */
export function getResult(id) {
    return request({
        url: `/job-match/result/${id}`,
        method: 'get'
    })
}

/**
 * 查询当前用户的匹配分析历史
 */
export function getHistory() {
    return request({
        url: '/job-match/history',
        method: 'get'
    })
}

/**
 * 保存/取消保存分析记录
 * @param {Number} id - 分析记录ID
 * @param {Number} isSaved - 0取消保存 1保存
 */
export function toggleSave(id, isSaved) {
    return request({
        url: `/job-match/${id}/save`,
        method: 'put',
        params: { isSaved }
    })
}

/**
 * 删除分析记录
 * @param {Number} id - 分析记录ID
 */
export function deleteAnalysis(id) {
    return request({
        url: `/job-match/${id}`,
        method: 'delete'
    })
}
