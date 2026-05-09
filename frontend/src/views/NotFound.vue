<template>
  <div class="min-h-screen bg-zen-bg py-24 px-4">
    <div class="max-w-md mx-auto text-center space-y-8">
      <h1 class="text-8xl font-light tracking-[0.2em] text-zen-muted">404</h1>
      <h2 class="text-xl text-zen-text tracking-widest">页面不存在</h2>
      <p class="text-sm text-zen-muted tracking-wider">
        抱歉，您访问的页面不存在或已被移除。
      </p>
      <button @click="goHome" class="btn-zen">
        返回首页
      </button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/')
}
</script>
