import request from '@/utils/request'

/**
 * 生成求职信
 * @param {Object} data - { resumeId, applicationId, jobTitle, companyName, jobDescription, letterStyle, language }
 */
export function generateLetter(data) {
    return request({
        url: '/cover-letter/generate',
        method: 'post',
        data
    })
}

/**
 * 查询求职信详情
 * @param {Number} id - 记录ID
 */
export function getLetter(id) {
    return request({
        url: `/cover-letter/${id}`,
        method: 'get'
    })
}

/**
 * 查询求职信历史列表
 */
export function getLetterHistory() {
    return request({
        url: '/cover-letter/history',
        method: 'get'
    })
}

/**
 * 重新生成求职信（换风格或换语言）
 * @param {Number} id - 记录ID
 * @param {String} style - 新风格（可选）
 * @param {String} lang - 新语言（可选）
 */
export function regenerateLetter(id, style, lang) {
    return request({
        url: `/cover-letter/${id}/regenerate`,
        method: 'post',
        params: { style, lang }
    })
}

/**
 * 保存/取消保存求职信
 * @param {Number} id - 记录ID
 * @param {Number} isSaved - 0取消保存 1保存
 */
export function toggleSave(id, isSaved) {
    return request({
        url: `/cover-letter/${id}/save`,
        method: 'put',
        params: { isSaved }
    })
}

/**
 * 删除求职信
 * @param {Number} id - 记录ID
 */
export function deleteLetter(id) {
    return request({
        url: `/cover-letter/${id}`,
        method: 'delete'
    })
}
