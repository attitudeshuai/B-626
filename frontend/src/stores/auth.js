import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, register as registerApi } from '../api/auth'
import { getCurrentUser } from '../api/user'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))

  const isLoggedIn = computed(() => !!token.value)

  async function login(username, password) {
    const res = await loginApi({ username, password })
    if (res.code === 200) {
      token.value = res.data.token
      user.value = res.data.user
      localStorage.setItem('token', res.data.token)
      localStorage.setItem('user', JSON.stringify(res.data.user))
      return { success: true }
    }
    return { success: false, message: res.message }
  }

  async function register(username, password, nickname) {
    const res = await registerApi({ username, password, nickname })
    if (res.code === 200) {
      return { success: true }
    }
    return { success: false, message: res.message }
  }

  async function fetchUser() {
    if (!token.value) return
    try {
      const res = await getCurrentUser()
      if (res.code === 200) {
        user.value = res.data
        localStorage.setItem('user', JSON.stringify(res.data))
      }
    } catch (e) {
      console.error('获取用户信息失败', e)
    }
  }

  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  return {
    token,
    user,
    isLoggedIn,
    login,
    register,
    fetchUser,
    logout
  }
})
