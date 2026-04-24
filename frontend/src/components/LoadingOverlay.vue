<template>
  <Teleport to="body">
    <div v-if="isLoading" class="loading-overlay">
      <div class="loading-content">
        <div class="loading-spinner">
          <div class="spinner-ring"></div>
          <div class="spinner-icon">
            <el-icon><i class="el-icon-magic-stick"></i></el-icon>
          </div>
        </div>
        <div class="loading-text">{{ loadingText }}</div>
        <div class="loading-hint">{{ loadingHint }}</div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { ref, provide } from 'vue'

const isLoading = ref(false)
const loadingText = ref('AI正在努力优化中...')
const loadingHint = ref('预计需要 5-10 秒')

const showLoading = (text, hint) => {
  loadingText.value = text || 'AI正在努力优化中...'
  loadingHint.value = hint || '预计需要 5-10 秒'
  isLoading.value = true
}

const hideLoading = () => {
  isLoading.value = false
}

defineExpose({ showLoading, hideLoading })

provide('loading', { showLoading, hideLoading })
</script>

<style scoped>
.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.loading-content {
  text-align: center;
}

.loading-spinner {
  position: relative;
  width: 100px;
  height: 100px;
  margin: 0 auto 30px;
}

.spinner-ring {
  position: absolute;
  width: 100%;
  height: 100%;
  border: 4px solid rgba(102, 126, 234, 0.1);
  border-top-color: #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.spinner-icon {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 50px;
  height: 50px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% {
    transform: translate(-50%, -50%) scale(1);
  }
  50% {
    transform: translate(-50%, -50%) scale(1.1);
  }
}

.loading-text {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
  animation: fadeInOut 2s ease-in-out infinite;
}

@keyframes fadeInOut {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

.loading-hint {
  font-size: 14px;
  color: #909399;
}
</style>