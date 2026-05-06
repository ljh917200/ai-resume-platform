/**
 * 墨色涟漪效果指令
 * 使用方式: <button v-ripple>按钮</button>
 */

export default {
  mounted(el, binding) {
    const options = {
      color: binding.value?.color || 'rgba(26, 26, 46, 0.3)',
      duration: binding.value?.duration || 600
    }

    el.addEventListener('click', function(e) {
      // 获取点击位置相对于元素的坐标
      const rect = el.getBoundingClientRect()
      const x = e.clientX - rect.left
      const y = e.clientY - rect.top

      // 创建涟漪元素
      const ripple = document.createElement('span')
      ripple.style.cssText = `
        position: absolute;
        border-radius: 50%;
        background: ${options.color};
        width: 10px;
        height: 10px;
        left: ${x}px;
        top: ${y}px;
        transform: translate(-50%, -50%) scale(0);
        pointer-events: none;
        z-index: 9999;
      `

      // 确保父元素有定位
      if (getComputedStyle(el).position === 'static') {
        el.style.position = 'relative'
      }

      // 添加overflow hidden
      if (getComputedStyle(el).overflow !== 'hidden') {
        el.style.overflow = 'hidden'
      }

      el.appendChild(ripple)

      // 获取最大尺寸用于涟漪扩散
      const maxDim = Math.max(rect.width, rect.height)
      const scale = maxDim / 10

      // 动画
      ripple.animate([
        { transform: 'translate(-50%, -50%) scale(0)', opacity: '1' },
        { transform: `translate(-50%, -50%) scale(${scale})`, opacity: '0' }
      ], {
        duration: options.duration,
        easing: 'cubic-bezier(0.4, 0, 0.2, 1)'
      })

      // 动画结束后移除元素
      setTimeout(() => {
        ripple.remove()
      }, options.duration)
    })
  }
}
