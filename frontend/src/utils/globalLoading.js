import { reactive, readonly } from 'vue'

const state = reactive({
  isLoading: false,
  loadingText: 'AI正在努力优化中...',
  loadingHint: '预计需要 5-10 秒',
  isError: false,
  errorType: 'error',
  errorTitle: '操作失败',
  errorMessage: ''
})

let loadingCallback = null
let errorCallback = null

export const setLoadingCallback = (callback) => {
  loadingCallback = callback
}

export const setErrorCallback = (callback) => {
  errorCallback = callback
}

export const showLoading = (text, hint) => {
  state.isLoading = true
  state.loadingText = text || 'AI正在努力优化中...'
  state.loadingHint = hint || '预计需要 5-10 秒'
  if (loadingCallback) {
    loadingCallback(true, state.loadingText, state.loadingHint)
  }
}

export const hideLoading = () => {
  state.isLoading = false
  if (loadingCallback) {
    loadingCallback(false)
  }
}

export const showError = (message, type = 'error', title = null) => {
  state.isError = true
  state.errorType = type
  state.errorMessage = message
  
  if (type === 'error') {
    state.errorTitle = title || '操作失败'
  } else if (type === 'warning') {
    state.errorTitle = title || '警告'
  } else {
    state.errorTitle = title || '提示'
  }
  
  if (errorCallback) {
    errorCallback(true, state.errorMessage, state.errorType, state.errorTitle)
  }
  
  setTimeout(() => {
    hideError()
  }, 5000)
}

export const hideError = () => {
  state.isError = false
  if (errorCallback) {
    errorCallback(false)
  }
}

export const loadingState = readonly(state)
