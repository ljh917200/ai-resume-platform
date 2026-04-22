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