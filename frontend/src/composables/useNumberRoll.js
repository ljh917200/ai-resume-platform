/**
 * 数字滚动动画 composable
 * 使用方式: const { displayValue, animateTo } = useNumberRoll(100)
 */

import { ref } from 'vue'

export function useNumberRoll(initialValue = 0) {
  const displayValue = ref(initialValue)
  const targetValue = ref(initialValue)
  let animationFrame = null

  const animateTo = (newValue, duration = 500) => {
    targetValue.value = newValue
    
    if (animationFrame) {
      cancelAnimationFrame(animationFrame)
    }

    const startValue = displayValue.value
    const start = performance.now()

    const update = (currentTime) => {
      const elapsed = currentTime - start
      const progress = Math.min(elapsed / duration, 1)
      
      // 使用缓动函数
      const easeProgress = 1 - Math.pow(1 - progress, 3)
      
      // 计算当前值
      const currentValue = startValue + (newValue - startValue) * easeProgress
      
      // 根据增减决定滚动方向（这里简化为直接设置值）
      displayValue.value = Math.round(currentValue)
      
      if (progress < 1) {
        animationFrame = requestAnimationFrame(update)
      }
    }

    animationFrame = requestAnimationFrame(update)
  }

  const increment = (amount = 1, duration = 300) => {
    animateTo(targetValue.value + amount, duration)
  }

  const decrement = (amount = 1, duration = 300) => {
    animateTo(targetValue.value - amount, duration)
  }

  const reset = (value = 0, duration = 300) => {
    animateTo(value, duration)
  }

  return {
    displayValue,
    targetValue,
    animateTo,
    increment,
    decrement,
    reset
  }
}

export default useNumberRoll
