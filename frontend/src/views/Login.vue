<template>
  <div class="min-h-screen flex items-center justify-center bg-zen-bg py-12 px-4">
    <div class="max-w-md w-full space-y-12">
      <header class="text-center space-y-4">
        <h1 class="text-5xl font-light tracking-[0.3em] text-zen-text">登录</h1>
        <p class="text-xs text-zen-muted uppercase tracking-[0.5em]">Gomoku Zen</p>
      </header>

      <form @submit.prevent="handleLogin" class="space-y-8">
        <div class="space-y-6">
          <div class="relative">
            <input
              v-model="username"
              type="text"
              required
              class="input-zen"
              placeholder="用户名"
            />
          </div>

          <div class="relative">
            <input
              v-model="password"
              type="password"
              required
              class="input-zen"
              placeholder="密码"
            />
          </div>
        </div>

        <div v-if="error" class="text-xs text-zen-text text-center tracking-widest bg-red-50 py-2 border border-red-100">
          {{ error }}
        </div>

        <button
          type="submit"
          :disabled="loading"
          class="btn-zen w-full"
        >
          <span v-if="loading" class="animate-pulse">正在进入...</span>
          <span v-else>确认登录</span>
        </button>
      </form>

      <div class="text-center">
        <router-link to="/register" class="text-xs text-zen-muted hover:text-zen-text tracking-[0.2em] transition-colors">
          还没有账户？前往注册
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const username = ref('')
const password = ref('')
const loading = ref(false)
const error = ref('')

const handleLogin = async () => {
  loading.value = true
  error.value = ''

  try {
    const result = await authStore.login(username.value, password.value)
    if (result.success) {
      router.push('/')
    } else {
      error.value = result.message || '登录失败'
    }
  } catch (e) {
    error.value = e.message || '登录失败，请重试'
  } finally {
    loading.value = false
  }
}
</script>
