<template>
  <div style="display: flex; justify-content: center; align-items: center; height: 60vh">
    <el-empty description="AI岗位匹配功能开发中，敬请期待...">
      <template #image>
        <el-icon :size="80" color="#C0C4CC"><Aim /></el-icon>
      </template>
    </el-empty>
  </div>
</template>

<script setup>
import { Aim } from '@element-plus/icons-vue'
</script>