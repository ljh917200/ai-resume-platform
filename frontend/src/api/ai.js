import request from '../utils/request'

// 文本优化
export function optimizeText(text, targetRole) {
    const params = new URLSearchParams()
    params.append('text', text)
    if (targetRole) {
        params.append('targetRole', targetRole)
    }
    return request.post('/ai/optimize', params)
}