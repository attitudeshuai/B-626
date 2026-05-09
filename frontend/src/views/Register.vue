<template>
  <div class="min-h-screen flex items-center justify-center bg-zen-bg py-12 px-4">
    <div class="max-w-md w-full space-y-12">
      <header class="text-center space-y-4">
        <h1 class="text-5xl font-light tracking-[0.3em] text-zen-text">注册</h1>
        <p class="text-xs text-zen-muted uppercase tracking-[0.5em]">Gomoku Zen</p>
      </header>

      <form @submit.prevent="handleRegister" class="space-y-8">
        <div class="space-y-6">
          <input
            v-model="username"
            type="text"
            required
            class="input-zen"
            placeholder="账号 (2-50字符)"
          />

          <input
            v-model="nickname"
            type="text"
            class="input-zen"
            placeholder="昵称 (选填)"
          />

          <input
            v-model="password"
            type="password"
            required
            class="input-zen"
            placeholder="密码 (至少6位)"
          />

          <input
            v-model="confirmPassword"
            type="password"
            required
            class="input-zen"
            placeholder="确认密码"
          />
        </div>

        <div v-if="error" class="text-xs text-zen-text text-center tracking-widest bg-red-50 py-2 border border-red-100">
          {{ error }}
        </div>

        <div v-if="success" class="text-xs text-zen-text text-center tracking-widest bg-green-50 py-2 border border-green-100">
          {{ success }}
        </div>

        <button
          type="submit"
          :disabled="loading"
          class="btn-zen w-full"
        >
          <span v-if="loading" class="animate-pulse">正在登记...</span>
          <span v-else>完成注册</span>
        </button>
      </form>

      <div class="text-center">
        <router-link to="/login" class="text-xs text-zen-muted hover:text-zen-text tracking-[0.2em] transition-colors">
          已有账户？返回登录
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
const nickname = ref('')
const password = ref('')
const confirmPassword = ref('')
const loading = ref(false)
const error = ref('')
const success = ref('')

const handleRegister = async () => {
  error.value = ''
  success.value = ''

  if (password.value !== confirmPassword.value) {
    error.value = '两次输入的密码不一致'
    return
  }

  if (password.value.length < 6) {
    error.value = '密码长度至少6位'
    return
  }

  // 密码强度验证：至少包含字母和数字
  const hasLetter = /[a-zA-Z]/.test(password.value)
  const hasDigit = /[0-9]/.test(password.value)
  if (!hasLetter || !hasDigit) {
    error.value = '密码必须包含字母和数字'
    return
  }

  loading.value = true

  try {
    const result = await authStore.register(
      username.value,
      password.value,
      nickname.value || username.value
    )
    if (result.success) {
      success.value = '注册成功，正在跳转到登录页...'
      setTimeout(() => {
        router.push('/login')
      }, 1500)
    } else {
      error.value = result.message || '注册失败'
    }
  } catch (e) {
    error.value = e.message || '注册失败，请重试'
  } finally {
    loading.value = false
  }
}
</script>
