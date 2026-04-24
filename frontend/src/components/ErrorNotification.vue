<template>
  <Teleport to="body">
    <Transition name="slide-fade">
      <div v-if="isVisible" class="error-notification" :class="errorType">
        <div class="error-icon">
          <el-icon v-if="errorType === 'error'"><i class="el-icon-circle-close"></i></el-icon>
          <el-icon v-else-if="errorType === 'warning'"><i class="el-icon-warning"></i></el-icon>
          <el-icon v-else><i class="el-icon-info"></i></el-icon>
        </div>
        <div class="error-content">
          <div class="error-title">{{ title }}</div>
          <div class="error-message">{{ message }}</div>
        </div>
        <button class="error-close" @click="close">
          <el-icon><i class="el-icon-close"></i></el-icon>
        </button>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref, provide } from 'vue'

const isVisible = ref(false)
const errorType = ref('error')
const title = ref('错误')
const message = ref('')

let timeoutId = null

const showError = (msg, type = 'error', duration = 5000) => {
  message.value = msg
  errorType.value = type
  
  if (type === 'error') {
    title.value = '操作失败'
  } else if (type === 'warning') {
    title.value = '警告'
  } else {
    title.value = '提示'
  }
  
  isVisible.value = true
  
  if (timeoutId) {
    clearTimeout(timeoutId)
  }
  
  if (duration > 0) {
    timeoutId = setTimeout(() => {
      isVisible.value = false
    }, duration)
  }
}

const close = () => {
  isVisible.value = false
  if (timeoutId) {
    clearTimeout(timeoutId)
  }
}

defineExpose({ showError, close })

provide('error', { showError, close })
</script>

<style scoped>
.error-notification {
  position: fixed;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: flex-start;
  padding: 16px 20px;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  z-index: 10000;
  max-width: 500px;
  min-width: 300px;
}

.error-notification.error {
  background: linear-gradient(135deg, rgba(245, 108, 108, 0.95), rgba(245, 108, 108, 0.85));
  color: white;
}

.error-notification.warning {
  background: linear-gradient(135deg, rgba(230, 162, 60, 0.95), rgba(230, 162, 60, 0.85));
  color: white;
}

.error-notification.info {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.95), rgba(102, 126, 234, 0.85));
  color: white;
}

.error-icon {
  font-size: 24px;
  margin-right: 12px;
  flex-shrink: 0;
}

.error-content {
  flex: 1;
}

.error-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 4px;
}

.error-message {
  font-size: 14px;
  opacity: 0.9;
}

.error-close {
  background: transparent;
  border: none;
  color: white;
  cursor: pointer;
  font-size: 16px;
  padding: 4px;
  margin-left: 12px;
  opacity: 0.8;
  transition: opacity 0.3s ease;
}

.error-close:hover {
  opacity: 1;
}

.slide-fade-enter-active {
  animation: slideIn 0.3s ease-out;
}

.slide-fade-leave-active {
  animation: slideOut 0.3s ease-in;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(-50%) translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(-50%) translateY(0);
  }
}

@keyframes slideOut {
  from {
    opacity: 1;
    transform: translateX(-50%) translateY(0);
  }
  to {
    opacity: 0;
    transform: translateX(-50%) translateY(-20px);
  }
}
</style>