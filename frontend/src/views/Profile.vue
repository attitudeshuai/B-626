<template>
  <div class="min-h-screen bg-zen-bg py-12 px-4">
    <div class="max-w-2xl mx-auto space-y-12">
      <header class="text-center space-y-4">
        <h1 class="text-4xl font-light tracking-[0.3em] text-zen-text">个人中心</h1>
        <p class="text-[10px] text-zen-muted uppercase tracking-[0.5em]">User Profile</p>
      </header>

      <div class="card-zen space-y-10">
        <!-- 基础信息 -->
        <section class="flex flex-col items-center space-y-4">
          <div class="relative group">
            <img
              v-if="user?.avatar"
              :src="user.avatar"
              class="w-24 h-24 rounded-full object-cover border border-zen-border p-1 grayscale hover:grayscale-0 transition-all duration-500"
              alt="avatar"
              @error="$event.target.style.display='none'; $event.target.nextElementSibling.style.display='flex'"
            />
            <div
              :class="{'hidden': user?.avatar}"
              class="w-24 h-24 rounded-full border border-zen-border p-1 bg-zen-bg flex items-center justify-center text-zen-muted grayscale hover:grayscale-0 transition-all duration-500"
            >
              <svg xmlns="http://www.w3.org/2000/svg" class="w-12 h-12" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
              </svg>
            </div>
          </div>
          <div class="text-center">
            <h2 class="text-xl font-light tracking-widest text-zen-text">{{ user?.nickname || user?.username }}</h2>
            <p class="text-[10px] text-zen-muted uppercase tracking-widest mt-1">@{{ user?.username }}</p>
          </div>
        </section>

        <!-- 战绩 -->
        <section class="space-y-6 pt-6 border-t border-zen-bg">
          <h3 class="text-[10px] text-zen-muted uppercase tracking-[0.3em] text-center">战绩统计</h3>
          <div class="grid grid-cols-4 gap-4 text-center">
            <div class="space-y-1">
              <div class="text-[10px] text-zen-muted uppercase tracking-widest">总场次</div>
              <div class="text-lg font-light">{{ user?.totalGames || 0 }}</div>
            </div>
            <div class="space-y-1">
              <div class="text-[10px] text-zen-muted uppercase tracking-widest">胜利</div>
              <div class="text-lg font-light text-zen-text">{{ user?.wins || 0 }}</div>
            </div>
            <div class="space-y-1">
              <div class="text-[10px] text-zen-muted uppercase tracking-widest">失败</div>
              <div class="text-lg font-light">{{ user?.losses || 0 }}</div>
            </div>
            <div class="space-y-1">
              <div class="text-[10px] text-zen-muted uppercase tracking-widest">胜率</div>
              <div class="text-lg font-light">{{ (user?.winRate || 0).toFixed(0) }}%</div>
            </div>
          </div>
        </section>

        <!-- 修改资料 -->
        <section class="space-y-6 pt-10 border-t border-zen-bg">
          <h3 class="text-[10px] text-zen-muted uppercase tracking-[0.3em] text-center">修改资料</h3>
          <form @submit.prevent="handleUpdate" class="space-y-6">
            <div class="space-y-4">
              <input
                v-model="nickname"
                type="text"
                class="input-zen text-sm"
                placeholder="新昵称"
              />
              <input
                v-model="avatar"
                type="text"
                class="input-zen text-sm"
                placeholder="头像 URL"
              />
            </div>

            <div v-if="message" :class="messageType === 'success' ? 'text-zen-text' : 'text-red-400'" class="text-[10px] text-center tracking-widest uppercase">
              {{ message }}
            </div>

            <button type="submit" :disabled="loading" class="btn-zen w-full text-sm">
              {{ loading ? '同步中...' : '更新资料' }}
            </button>
          </form>
        </section>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '../stores/auth'
import { updateUser } from '../api/user'

const authStore = useAuthStore()
const user = computed(() => authStore.user)

const nickname = ref('')
const avatar = ref('')
const loading = ref(false)
const message = ref('')
const messageType = ref('')

onMounted(() => {
  authStore.fetchUser()
  if (user.value) {
    nickname.value = user.value.nickname || ''
    avatar.value = user.value.avatar || ''
  }
})

const handleUpdate = async () => {
  loading.value = true
  message.value = ''

  try {
    const res = await updateUser({
      nickname: nickname.value,
      avatar: avatar.value
    })

    if (res.code === 200) {
      message.value = '修改成功'
      messageType.value = 'success'
      await authStore.fetchUser()
    } else {
      message.value = res.message || '修改失败'
      messageType.value = 'error'
    }
  } catch (e) {
    message.value = '修改失败，请重试'
    messageType.value = 'error'
  } finally {
    loading.value = false
  }
}
</script>
