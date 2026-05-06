<template>
  <div class="page-progress-ink">
    <div 
      class="progress-fill" 
      :style="{ width: `${progress}%` }"
    ></div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  autoStart: {
    type: Boolean,
    default: true
  },
  initialProgress: {
    type: Number,
    default: 0
  }
})

const emit = defineEmits(['complete'])

const progress = ref(props.initialProgress)
let interval = null

const start = () => {
  progress.value = 0
  
  if (interval) {
    clearInterval(interval)
  }

  interval = setInterval(() => {
    // 随机增加进度，模拟加载效果
    const increment = Math.random() * 15 + 5
    progress.value = Math.min(progress.value + increment, 90)
  }, 150)
}

const finish = () => {
  if (interval) {
    clearInterval(interval)
    interval = null
  }
  
  // 平滑过渡到100%
  const startProgress = progress.value
  const start = performance.now()
  const duration = 300
  
  const animate = (currentTime) => {
    const elapsed = currentTime - start
    const progressRatio = Math.min(elapsed / duration, 1)
    const eased = 1 - Math.pow(1 - progressRatio, 3)
    
    progress.value = startProgress + (100 - startProgress) * eased
    
    if (progressRatio < 1) {
      requestAnimationFrame(animate)
    } else {
      emit('complete')
      // 完成后延迟隐藏
      setTimeout(() => {
        progress.value = 0
      }, 200)
    }
  }
  
  requestAnimationFrame(animate)
}

const setProgress = (value) => {
  progress.value = Math.max(0, Math.min(100, value))
}

defineExpose({
  start,
  finish,
  setProgress
})

onMounted(() => {
  if (props.autoStart) {
    start()
  }
})

onUnmounted(() => {
  if (interval) {
    clearInterval(interval)
  }
})
</script>

<style scoped>
.page-progress-ink {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  background-color: #e8e8e8;
  z-index: 9999;
}

.progress-fill {
  height: 100%;
  background-color: #1a1a2e;
  transition: width 200ms cubic-bezier(0.4, 0, 0.2, 1);
}
</style>
