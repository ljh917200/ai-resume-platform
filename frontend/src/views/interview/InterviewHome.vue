<template>
  <div style="display: flex; justify-content: center; align-items: center; height: 60vh">
    <el-empty description="AI面试助手功能开发中，敬请期待...">
      <template #image>
        <el-icon :size="80" color="#C0C4CC"><ChatDotRound /></el-icon>
      </template>
    </el-empty>
  </div>
</template>

<script setup>
import { ChatDotRound } from '@element-plus/icons-vue'
</script>