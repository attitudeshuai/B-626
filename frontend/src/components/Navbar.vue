<template>
  <nav class="bg-white border-b border-zen-border sticky top-0 z-40">
    <div class="max-w-7xl mx-auto px-6">
      <div class="flex justify-between h-20">
        <div class="flex items-center space-x-12">
          <router-link to="/" class="group">
            <h1 class="text-2xl font-light tracking-[0.3em] text-zen-text group-hover:opacity-70 transition-opacity">五子棋</h1>
          </router-link>

          <div class="hidden md:flex items-center space-x-8">
            <router-link
              v-for="link in navLinks"
              :key="link.to"
              :to="link.to"
              class="text-[10px] uppercase tracking-[0.3em] transition-all duration-300"
              :class="$route.path === link.to ? 'text-zen-text font-medium' : 'text-zen-muted hover:text-zen-text'"
            >
              {{ link.label }}
            </router-link>
          </div>
        </div>

        <div class="flex items-center space-x-8">
          <router-link
            to="/profile"
            class="flex items-center space-x-3 group"
          >
            <img
              v-if="authStore.user?.avatar"
              :src="authStore.user.avatar"
              class="w-6 h-6 rounded-full object-cover border border-zen-border grayscale group-hover:grayscale-0 transition-all duration-500"
              alt="avatar"
              @error="$event.target.style.display='none'; $event.target.nextElementSibling.style.display='flex'"
            />
            <div
              :class="{'hidden': authStore.user?.avatar}"
              class="w-6 h-6 rounded-full border border-zen-border bg-zen-bg flex items-center justify-center text-zen-muted grayscale group-hover:grayscale-0 transition-all duration-500"
            >
              <svg xmlns="http://www.w3.org/2000/svg" class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
              </svg>
            </div>
            <span class="hidden md:inline text-[10px] uppercase tracking-[0.2em] text-zen-muted group-hover:text-zen-text transition-colors">
              {{ authStore.user?.nickname || authStore.user?.username }}
            </span>
          </router-link>

          <button
            @click="handleLogout"
            class="text-[10px] uppercase tracking-[0.2em] text-zen-muted hover:text-red-400 transition-colors"
          >
            登出
          </button>
        </div>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { useAuthStore } from '../stores/auth'
import { useRouter } from 'vue-router'

const authStore = useAuthStore()
const router = useRouter()

const navLinks = [
  { to: '/', label: '首页' },
  { to: '/online', label: '联机' },
  { to: '/history', label: '档案' },
  { to: '/ranking', label: '排行' }
]

const handleLogout = () => {
  authStore.logout()
  router.push('/login')
}
</script>
