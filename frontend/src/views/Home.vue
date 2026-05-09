<template>
  <div class="min-h-screen bg-zen-bg py-12 px-4">
    <div class="max-w-2xl mx-auto space-y-20">
      <!-- Header -->
      <header class="text-center space-y-4">
        <h1 class="text-5xl font-light tracking-[0.3em] text-zen-text">五子棋</h1>
        <p class="text-xs text-zen-muted uppercase tracking-[0.5em]">Gomoku Zen</p>
      </header>

      <!-- Game Modes -->
      <div class="space-y-12">
        <!-- AI Game -->
        <section class="space-y-6">
          <h2 class="text-sm text-zen-muted uppercase tracking-[0.2em] text-center">人机对战</h2>
          <div class="space-y-4">
            <div class="flex justify-center space-x-4">
              <button
                v-for="d in difficulties"
                :key="d.value"
                @click="selectedDifficulty = d.value"
                class="px-6 py-2 text-sm tracking-widest transition-all duration-300"
                :class="selectedDifficulty === d.value
                  ? 'border-b border-zen-text text-zen-text'
                  : 'text-zen-muted hover:text-zen-text'"
              >
                {{ d.label }}
              </button>
            </div>

            <div class="flex justify-center space-x-8">
              <label class="flex items-center space-x-2 cursor-pointer group">
                <input type="radio" v-model="playerFirst" :value="true" class="hidden" />
                <span class="text-xs tracking-widest transition-colors duration-300"
                  :class="playerFirst ? 'text-zen-text' : 'text-zen-muted group-hover:text-zen-text'">
                  先手 / 黑
                </span>
              </label>
              <label class="flex items-center space-x-2 cursor-pointer group">
                <input type="radio" v-model="playerFirst" :value="false" class="hidden" />
                <span class="text-xs tracking-widest transition-colors duration-300"
                  :class="!playerFirst ? 'text-zen-text' : 'text-zen-muted group-hover:text-zen-text'">
                  后手 / 白
                </span>
              </label>
            </div>

            <button @click="startAIGame" class="btn-zen w-full">
              开始对弈
            </button>
          </div>
        </section>

        <!-- Local Game -->
        <section class="space-y-6">
          <h2 class="text-sm text-zen-muted uppercase tracking-[0.2em] text-center">双人对弈</h2>
          <button @click="startLocalGame" class="btn-zen w-full">
            本地对战
          </button>
        </section>

        <!-- Online Game -->
        <section class="space-y-6">
          <h2 class="text-sm text-zen-muted uppercase tracking-[0.2em] text-center">联机对战</h2>
          <button @click="goOnline" class="btn-zen w-full">
            进入大厅
          </button>
        </section>
      </div>

      <!-- Stats -->
      <footer class="pt-10 border-t border-zen-border">
        <div class="grid grid-cols-4 gap-4 text-center">
          <div class="space-y-1">
            <div class="text-xs text-zen-muted tracking-widest">胜场</div>
            <div class="text-lg font-light">{{ user?.wins || 0 }}</div>
          </div>
          <div class="space-y-1">
            <div class="text-xs text-zen-muted tracking-widest">败场</div>
            <div class="text-lg font-light">{{ user?.losses || 0 }}</div>
          </div>
          <div class="space-y-1">
            <div class="text-xs text-zen-muted tracking-widest">总计</div>
            <div class="text-lg font-light">{{ user?.totalGames || 0 }}</div>
          </div>
          <div class="space-y-1">
            <div class="text-xs text-zen-muted tracking-widest">胜率</div>
            <div class="text-lg font-light">{{ user?.winRate?.toFixed(0) || 0 }}%</div>
          </div>
        </div>
      </footer>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useGameStore } from '../stores/game'

const router = useRouter()
const authStore = useAuthStore()
const gameStore = useGameStore()

const user = computed(() => authStore.user)

const difficulties = [
  { value: 'EASY', label: '简单' },
  { value: 'MEDIUM', label: '中等' },
  { value: 'HARD', label: '困难' }
]

const selectedDifficulty = ref('EASY')
const playerFirst = ref(true)

onMounted(() => {
  authStore.fetchUser()
})

const startAIGame = async () => {
  const result = await gameStore.startGame('AI', selectedDifficulty.value, playerFirst.value)
  if (result.success) {
    router.push('/game')
  }
}

const startLocalGame = async () => {
  const result = await gameStore.startGame('LOCAL', null, true)
  if (result.success) {
    router.push('/game')
  }
}

const goOnline = () => {
  router.push('/online')
}
</script>
