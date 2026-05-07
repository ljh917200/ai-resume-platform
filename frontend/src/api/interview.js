import request from '@/utils/request'

/**
 * 生成面试题
 * @param {Object} data - { resumeId, applicationId, jobTitle, companyName, jobDescription, questionType, countPerType }
 */
export function generateQuestions(data) {
    return request({
        url: '/interview/generate',
        method: 'post',
        data
    })
}

/**
 * 查询单条面试题详情
 * @param {Number} id - 题目ID
 */
export function getQuestion(id) {
    return request({
        url: `/interview/${id}`,
        method: 'get'
    })
}

/**
 * 查询面试题列表（支持按类型和状态筛选）
 * @param {String} questionType - 题目类型（可选）
 * @param {String} prepStatus - 准备状态（可选）
 */
export function getQuestionList(questionType, prepStatus) {
    return request({
        url: '/interview/list',
        method: 'get',
        params: { questionType, prepStatus }
    })
}

/**
 * 更新答题草稿
 * @param {Number} id - 题目ID
 * @param {String} answerDraft - 答题草稿内容
 */
export function updateDraft(id, answerDraft) {
    return request({
        url: `/interview/${id}/draft`,
        method: 'put',
        data: { answerDraft }
    })
}

/**
 * 更新准备状态
 * @param {Number} id - 题目ID
 * @param {String} prepStatus - 新状态
 */
export function updatePrepStatus(id, prepStatus) {
    return request({
        url: `/interview/${id}/status`,
        method: 'put',
        params: { prepStatus }
    })
}

/**
 * 保存/取消保存面试题
 * @param {Number} id - 题目ID
 * @param {Number} isSaved - 0取消保存 1保存
 */
export function toggleSave(id, isSaved) {
    return request({
        url: `/interview/${id}/save`,
        method: 'put',
        params: { isSaved }
    })
}

/**
 * 删除面试题
 * @param {Number} id - 题目ID
 */
export function deleteQuestion(id) {
    return request({
        url: `/interview/${id}`,
        method: 'delete'
    })
}

/**
 * 获取面试准备统计
 */
export function getInterviewStats() {
    return request({
        url: '/interview/stats',
        method: 'get'
    })
}
