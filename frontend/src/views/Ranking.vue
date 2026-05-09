<template>
  <div class="min-h-screen bg-zen-bg py-12 px-4">
    <div class="max-w-2xl mx-auto space-y-12">
      <header class="text-center space-y-4">
        <h1 class="text-4xl font-light tracking-[0.3em] text-zen-text">排行榜</h1>
        <p class="text-[10px] text-zen-muted uppercase tracking-[0.5em]">Hall of Fame</p>
      </header>

      <div class="card-zen p-0">
        <!-- Tab切换 -->
        <div class="flex border-b border-zen-border">
          <button
            @click="activeTab = 'winrate'"
            class="flex-1 py-4 text-[10px] uppercase tracking-[0.3em] transition-all duration-300"
            :class="activeTab === 'winrate' ? 'text-zen-text bg-white' : 'text-zen-muted bg-zen-bg hover:text-zen-text'"
          >
            胜率排行
          </button>
          <button
            @click="activeTab = 'wins'"
            class="flex-1 py-4 text-[10px] uppercase tracking-[0.3em] transition-all duration-300"
            :class="activeTab === 'wins' ? 'text-zen-text bg-white' : 'text-zen-muted bg-zen-bg hover:text-zen-text'"
          >
            胜场排行
          </button>
        </div>

        <div v-if="loading" class="text-center py-20 animate-pulse text-[10px] uppercase tracking-widest text-zen-muted">
          寻觅中...
        </div>

        <div v-else-if="currentList.length === 0" class="text-center py-20 space-y-4">
          <div class="text-2xl grayscale opacity-50">🏆</div>
          <p class="text-[10px] text-zen-muted uppercase tracking-widest">虚位以待</p>
        </div>

        <div v-else class="divide-y divide-zen-bg">
          <div
            v-for="(item, index) in currentList"
            :key="item.userId"
            class="flex items-center p-6 transition-colors hover:bg-zen-bg"
          >
            <!-- 排名 -->
            <div class="w-12 text-[10px] font-light tracking-widest text-zen-muted">
              {{ (index + 1).toString().padStart(2, '0') }}
            </div>

            <!-- 头像和昵称 -->
            <div class="flex items-center space-x-4 flex-1">
              <img
                :src="item.avatar || 'https://via.placeholder.com/40'"
                class="w-8 h-8 rounded-full object-cover border border-zen-border grayscale p-0.5"
                alt="avatar"
              />
              <span class="text-sm font-light tracking-widest text-zen-text">{{ item.nickname }}</span>
            </div>

            <!-- 数据 -->
            <div class="text-right space-y-1">
              <div v-if="activeTab === 'winrate'" class="text-sm font-light tracking-widest text-zen-text">
                {{ item.winRate?.toFixed(1) }}%
              </div>
              <div v-else class="text-sm font-light tracking-widest text-zen-text">
                {{ item.wins }} WINS
              </div>
              <div class="text-[10px] text-zen-muted uppercase tracking-widest">
                {{ item.wins }}W / {{ item.totalGames }}T
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { getWinRateRanking, getWinsRanking } from '../api/game'

const activeTab = ref('winrate')
const winrateList = ref([])
const winsList = ref([])
const loading = ref(true)

const currentList = computed(() => {
  return activeTab.value === 'winrate' ? winrateList.value : winsList.value
})

const fetchData = async () => {
  loading.value = true
  try {
    const [winrateRes, winsRes] = await Promise.all([
      getWinRateRanking(),
      getWinsRanking()
    ])

    if (winrateRes.code === 200) {
      winrateList.value = winrateRes.data
    }
    if (winsRes.code === 200) {
      winsList.value = winsRes.data
    }
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)
</script>
