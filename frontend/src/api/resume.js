import request from '../utils/request'


// 上传简历
export function uploadResume(file) {
    const formData = new FormData()
    formData.append('file', file)
    return request.post('/resume/upload', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
    })
}

// 获取简历列表
export function getResumeList() {
    return request.get('/resume/list')
}

// 获取简历详情
export function getResume(id) {
    return request.get(`/resume/${id}`)
}

// 删除简历
export function deleteResume(id) {
    return request.delete(`/resume/${id}`)
}

// 一键优化简历
export function optimizeResume(id, targetRole) {
    return request.post(`/resume/optimize/${id}?targetRole=${encodeURIComponent(targetRole || '')}`)
}

// 导出简历PDF
export function exportResume(id, type = 'optimized', templateId=1) {
    return request.get(`/resume/export/${id}`, {
        params: { type, templateId },
        responseType: 'blob'  // 告诉 axios 返回二进制数据
    })
}

// ========== 新增功能 ==========

/**
 * 重命名简历
 * @param {number} id - 简历ID
 * @param {string} displayName - 新的显示名称
 * @returns {Promise}
 */
export function renameResume(id, displayName) {
    return request.put(`/resume/rename/${id}`, {
        displayName: displayName
    })
}

/**
 * 批量删除简历
 * @param {Array<number>} ids - 简历ID列表
 * @returns {Promise}
 */
export function batchDeleteResume(ids) {
    return request.delete('/resume/batch', {
        data: {ids: ids}
    })
}

///////////////////////////////////////////////////////////////////////////////

// 切换简历模板
export function switchTemplate(resumeId, templateId) {
    return request({
        url: `/resume/${resumeId}/template`,
        method: 'put',
        params: { templateId }
    })
}

/**
 * 生成简历HTML（v1.7.0新增）
 *
 * 功能说明：
 * - 调用后端接口，让 DeepSeek 生成 XHTML 格式的简历
 * - 返回 HTML 内容供前端 iframe 预览
 *
 * @param {number} id - 简历ID
 * @param {string} type - 类型：'original'-原始版，'optimized'-优化版
 * @param {number} templateId - 模板ID：1-简约蓝 2-商务灰 3-创意橙
 * @returns {Promise} 返回HTML内容
 */
export const generateHtml = (id, type, templateId) => {
    return request({
        url: '/resume/generate-html',
        method: 'post',
        params: {id, type, templateId}
    })
}

/**
 * 从HTML导出PDF（v1.7.0新增）
 *
 * 功能说明：
 * - 调用后端接口，将已生成的 HTML 转换为 PDF 并下载
 * - 后端使用 Flying Saucer 处理转换
 *
 * @param {number} id - 简历ID
 * @param {string} type - 类型：'original'-原始版，'optimized'-优化版
 * @param {number} templateId - 模板ID：1-简约蓝 2-商务灰 3-创意橙
 * @returns {Promise} 返回Blob格式的PDF文件
 */
export const exportFromHtml = (id, type, templateId) => {
    return request({
        url: '/resume/export-from-html',
        method: 'post',
        params: {id, type, templateId},
        // 指定响应类型为 blob，用于处理文件下载
        responseType: 'blob'
    })
}