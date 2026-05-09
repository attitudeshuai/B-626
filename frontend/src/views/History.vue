<template>
  <div class="min-h-screen bg-zen-bg py-12 px-4">
    <div class="max-w-4xl mx-auto space-y-12">
      <header class="text-center space-y-4">
        <h1 class="text-4xl font-light tracking-[0.3em] text-zen-text">对局记录</h1>
        <p class="text-[10px] text-zen-muted uppercase tracking-[0.5em]">Game Archives</p>
      </header>

      <div v-if="loading" class="text-center py-20 animate-pulse text-[10px] uppercase tracking-widest text-zen-muted">
        溯源中...
      </div>

      <div v-else-if="records.length === 0" class="card-zen text-center py-20 space-y-8">
        <div class="text-2xl grayscale opacity-30">📜</div>
        <p class="text-[10px] text-zen-muted uppercase tracking-widest">尚无尘缘</p>
        <router-link to="/" class="btn-zen inline-block px-12">开启新局</router-link>
      </div>

      <div v-else class="space-y-4">
        <div
          v-for="record in records"
          :key="record.id"
          class="card-zen p-6 flex items-center justify-between hover:border-zen-text transition-all duration-500 cursor-pointer group"
          @click="viewDetail(record)"
        >
          <div class="flex items-center space-x-8">
            <div class="text-[10px] text-zen-muted tracking-widest uppercase">
              {{ record.result }}
            </div>
            <div>
              <div class="text-sm font-light tracking-widest text-zen-text">
                {{ record.gameMode === 'AI' ? '人机对弈' : '同机对弈' }}
                <span v-if="record.difficulty" class="text-[10px] text-zen-muted lowercase ml-2">
                  ({{ record.difficulty.toLowerCase() }})
                </span>
              </div>
              <div class="text-[10px] text-zen-muted uppercase tracking-widest mt-1">
                {{ formatDate(record.createdAt) }}
              </div>
            </div>
          </div>

          <div class="flex items-center space-x-8">
            <div class="text-right">
              <div
                class="text-xs tracking-[0.2em] font-light"
                :class="{
                  'text-zen-text': record.result === 'WIN',
                  'text-zen-muted': record.result !== 'WIN'
                }"
              >
                {{ resultLabel(record.result) }}
              </div>
              <div class="text-[10px] text-zen-muted uppercase tracking-widest mt-1">
                {{ formatDuration(record.duration) }}
              </div>
            </div>
            <div class="text-zen-muted group-hover:text-zen-text transition-colors">
              <i class="fa-solid fa-chevron-right text-[10px]"></i>
            </div>
          </div>
        </div>
      </div>

      <!-- 棋谱详情弹窗 -->
      <transition name="fade">
        <div v-if="selectedRecord" class="fixed inset-0 bg-white/95 backdrop-blur-sm flex items-center justify-center z-50 p-4">
          <div class="max-w-4xl w-full max-h-[90vh] overflow-y-auto space-y-10 py-10 px-6">
            <header class="flex justify-between items-center border-b border-zen-border pb-4">
              <div class="space-y-1">
                <h2 class="text-2xl font-light tracking-[0.2em] text-zen-text">对局复盘</h2>
                <p class="text-[10px] text-zen-muted uppercase tracking-widest">
                  {{ formatDate(selectedRecord.createdAt) }} · {{ formatDuration(selectedRecord.duration) }}
                </p>
              </div>
              <button @click="selectedRecord = null" class="text-zen-muted hover:text-zen-text transition-colors">
                <i class="fa-solid fa-xmark text-lg"></i>
              </button>
            </header>

            <div class="flex flex-col lg:flex-row gap-12 items-center lg:items-start">
              <div class="card-zen p-2">
                <GameBoard
                  :board="replayBoard"
                  :moves="replayMoves"
                  :show-order="true"
                  :game-over="true"
                  :disabled="true"
                />
              </div>

              <div class="lg:w-64 space-y-10 flex-shrink-0">
                <section class="space-y-4">
                  <h3 class="text-[10px] text-zen-muted uppercase tracking-[0.3em] border-b border-zen-bg pb-2">落子同步</h3>
                  <div class="flex items-center justify-between">
                    <button @click="replayStep = Math.max(0, replayStep - 1)" :disabled="replayStep === 0" class="text-zen-muted hover:text-zen-text disabled:opacity-30">
                      <i class="fa-solid fa-backward-step"></i>
                    </button>
                    <span class="text-xs font-light tracking-widest text-zen-text">{{ replayStep }} / {{ JSON.parse(selectedRecord.moves || '[]').length }}</span>
                    <button @click="replayStep = Math.min(JSON.parse(selectedRecord.moves || '[]').length, replayStep + 1)" :disabled="replayStep >= JSON.parse(selectedRecord.moves || '[]').length" class="text-zen-muted hover:text-zen-text disabled:opacity-30">
                      <i class="fa-solid fa-forward-step"></i>
                    </button>
                  </div>
                  <input type="range" v-model="replayStep" :min="0" :max="JSON.parse(selectedRecord.moves || '[]').length" class="w-full h-1 bg-zen-bg appearance-none cursor-pointer accent-zen-text">
                </section>

                <section class="space-y-4">
                  <h3 class="text-[10px] text-zen-muted uppercase tracking-[0.3em] border-b border-zen-bg pb-2">对局摘要</h3>
                  <div class="space-y-3 text-[10px] tracking-widest uppercase text-zen-muted">
                    <div class="flex justify-between"><span>结果</span><span class="text-zen-text">{{ resultLabel(selectedRecord.result) }}</span></div>
                    <div class="flex justify-between"><span>模式</span><span class="text-zen-text">{{ selectedRecord.gameMode }}</span></div>
                    <div v-if="selectedRecord.difficulty" class="flex justify-between"><span>难度</span><span class="text-zen-text">{{ selectedRecord.difficulty }}</span></div>
                  </div>
                </section>
              </div>
            </div>
          </div>
        </div>
      </transition>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { getHistory, getGameDetail } from '../api/game'
import { createEmptyBoard, colorToValue } from '../utils/boardRules'
import GameBoard from '../components/GameBoard.vue'

const records = ref([])
const loading = ref(true)
const selectedRecord = ref(null)
const replayStep = ref(0)

const replayMoves = computed(() => {
  if (!selectedRecord.value?.moves) return []
  try {
    const moves = JSON.parse(selectedRecord.value.moves)
    return moves.slice(0, replayStep.value)
  } catch {
    return []
  }
})

const replayBoard = computed(() => {
  const board = createEmptyBoard()
  replayMoves.value.forEach(move => {
    board[move.x][move.y] = colorToValue(move.color)
  })
  return board
})

onMounted(async () => {
  try {
    const res = await getHistory()
    if (res.code === 200) {
      records.value = res.data
    }
  } finally {
    loading.value = false
  }
})

const viewDetail = async (record) => {
  try {
    const res = await getGameDetail(record.id)
    if (res.code === 200) {
      selectedRecord.value = res.data
      const moves = JSON.parse(res.data.moves || '[]')
      replayStep.value = moves.length
    }
  } catch (e) {
    console.error('获取详情失败', e)
  }
}

const difficultyLabel = (d) => {
  const map = { EASY: '简单', MEDIUM: '中等', HARD: '困难' }
  return map[d] || d
}

const resultLabel = (r) => {
  const map = { WIN: '胜利', LOSE: '失败', DRAW: '平局' }
  return map[r] || r
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

const formatDuration = (seconds) => {
  if (!seconds) return '0秒'
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return mins > 0 ? `${mins}分${secs}秒` : `${secs}秒`
}
</script>
