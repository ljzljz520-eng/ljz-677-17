<template>
  <div v-if="hasError" class="min-h-screen flex items-center justify-center bg-gray-100">
    <div class="card text-center max-w-md">
      <div class="w-16 h-16 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-4">
        <svg class="w-8 h-8 text-red-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
            d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
        </svg>
      </div>
      <h2 class="text-xl font-semibold text-gray-800 mb-2">页面出错了</h2>
      <p class="text-gray-500 mb-6">抱歉，页面加载过程中发生了错误</p>
      <el-button type="primary" @click="handleReload">刷新页面</el-button>
    </div>
  </div>
  <slot v-else />
</template>

<script setup>
import { ref, onErrorCaptured } from 'vue'

const hasError = ref(false)

onErrorCaptured((error, instance, info) => {
  hasError.value = true
  return false
})

const handleReload = () => {
  window.location.reload()
}
</script>
