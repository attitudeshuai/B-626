<template>
  <div class="min-h-screen bg-zen-bg py-12 px-4">
    <div class="max-w-5xl mx-auto">
      <div class="flex flex-col lg:flex-row items-start gap-12">
        <!-- 棋盘区域 -->
        <div class="flex-1 w-full lg:w-auto flex justify-center">
          <div class="card-zen p-2 inline-block">
            <GameBoard
              :board="gameStore.board"
              :current-player="gameStore.currentPlayer"
              :player-color="gameStore.playerColor"
              :game-over="gameStore.gameOver"
              :moves="gameStore.moves"
              :show-order="showOrder"
              :disabled="gameStore.isLoading || (gameStore.gameMode === 'AI' && gameStore.currentPlayer !== gameStore.playerColor)"
              @move="handleMove"
            />
          </div>
        </div>

        <!-- 信息面板 -->
        <div class="w-full lg:w-72 space-y-10">
          <!-- 游戏状态 -->
          <section class="space-y-6">
            <header class="border-b border-zen-border pb-2">
              <h3 class="text-xs text-zen-muted uppercase tracking-[0.3em]">对局信息</h3>
            </header>

            <div class="space-y-4 text-sm tracking-widest">
              <div class="flex justify-between items-center">
                <span class="text-zen-muted">模式</span>
                <span class="text-zen-text font-light">{{ gameStore.gameMode === 'AI' ? '人机' : '双人' }}</span>
              </div>

              <div v-if="gameStore.gameMode === 'AI'" class="flex justify-between items-center">
                <span class="text-zen-muted">难度</span>
                <span class="text-zen-text font-light">{{ difficultyLabel }}</span>
              </div>

              <div class="flex justify-between items-center">
                <span class="text-zen-muted">当前落子</span>
                <span class="flex items-center space-x-2">
                  <span class="w-3 h-3 rounded-full" :class="gameStore.currentPlayer === 'BLACK' ? 'bg-zen-text' : 'bg-white border border-zen-border'"></span>
                  <span class="text-zen-text font-light">{{ gameStore.currentPlayer === 'BLACK' ? '黑方' : '白方' }}</span>
                </span>
              </div>

            </div>
          </section>

          <!-- 操作 -->
          <section class="space-y-4">
            <button
              @click="handleUndo"
              :disabled="gameStore.gameOver || gameStore.moves.length === 0"
              class="btn-zen w-full text-sm py-3"
            >
              悔棋 ({{ gameStore.undoRemaining }})
            </button>

            <button
              @click="handleSurrender"
              :disabled="gameStore.gameOver"
              class="btn-zen w-full text-sm py-3"
            >
              认输
            </button>

            <label class="flex items-center justify-center space-x-3 cursor-pointer group pt-4">
              <input v-model="showOrder" type="checkbox" class="hidden">
              <div class="w-4 h-4 border border-zen-border flex items-center justify-center transition-colors" :class="showOrder ? 'bg-zen-text' : 'bg-white'">
                <div v-if="showOrder" class="w-1.5 h-1.5 bg-white"></div>
              </div>
              <span class="text-[10px] text-zen-muted uppercase tracking-[0.2em] group-hover:text-zen-text transition-colors">显示手数</span>
            </label>
          </section>

          <!-- 历史记录 -->
          <section class="space-y-4">
            <header class="border-b border-zen-border pb-2">
              <h3 class="text-xs text-zen-muted uppercase tracking-[0.3em]">最近手数</h3>
            </header>
            <div class="h-40 overflow-y-auto space-y-2 pr-2 custom-scrollbar">
              <div
                v-for="move in [...gameStore.moves].reverse().slice(0, 10)"
                :key="move.step"
                class="flex items-center justify-between text-[10px] tracking-widest py-1 border-b border-zen-bg"
              >
                <div class="flex items-center space-x-2">
                  <span class="w-2 h-2 rounded-full" :class="move.color === 'BLACK' ? 'bg-zen-text' : 'bg-white border border-zen-border'"></span>
                  <span class="text-zen-muted">第 {{ move.step }} 手</span>
                </div>
                <span class="text-zen-text">({{ move.x }}, {{ move.y }})</span>
              </div>
              <div v-if="gameStore.moves.length === 0" class="text-[10px] text-zen-muted italic text-center py-4 tracking-widest uppercase">
                虚位以待
              </div>
            </div>
          </section>
        </div>
      </div>

      <!-- 胜负弹窗 -->
      <transition name="fade">
        <div v-if="gameStore.gameOver" class="fixed inset-0 bg-white/90 backdrop-blur-sm flex items-center justify-center z-50">
          <div class="max-w-sm w-full p-12 text-center space-y-12">
            <header class="space-y-4">
              <h2 class="text-4xl font-light tracking-[0.4em] text-zen-text">
                {{ getResultTitle }}
              </h2>
              <p class="text-xs text-zen-muted uppercase tracking-[0.3em]">
                {{ getResultMessage }}
              </p>
            </header>

            <div class="space-y-4">
              <button @click="handlePlayAgain" class="btn-zen w-full">
                重开一局
              </button>
              <button @click="handleGoHome" class="text-xs text-zen-muted hover:text-zen-text tracking-[0.2em] transition-colors uppercase">
                返回首页
              </button>
            </div>
          </div>
        </div>
      </transition>

      <!-- 认输确认对话框 -->
      <ConfirmDialog
        :visible="showSurrenderDialog"
        title="确认认输"
        message="确定要认输吗？"
        confirm-text="确定"
        cancel-text="取消"
        @confirm="confirmSurrender"
        @cancel="cancelSurrender"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useGameStore } from '../stores/game'
import GameBoard from '../components/GameBoard.vue'
import ConfirmDialog from '../components/ConfirmDialog.vue'

const router = useRouter()
const gameStore = useGameStore()

const showOrder = ref(false)
const showSurrenderDialog = ref(false)

const difficultyLabel = computed(() => {
  const map = { EASY: '简单', MEDIUM: '中等', HARD: '困难' }
  return map[gameStore.difficulty] || '简单'
})

const getResultEmoji = computed(() => {
  if (gameStore.winner === 'DRAW') return '🤝'
  if (gameStore.winner === gameStore.playerColor) return '🎉'
  return '😢'
})

const getResultTitle = computed(() => {
  if (gameStore.winner === 'DRAW') return '平局'
  if (gameStore.winner === gameStore.playerColor) return '你赢了！'
  return '你输了'
})

const getResultMessage = computed(() => {
  if (gameStore.winner === 'DRAW') return '棋盘已满，双方平局'
  if (gameStore.winner === gameStore.playerColor) return '恭喜你取得胜利！'
  return '再接再厉，下次一定能赢！'
})

onMounted(() => {
  if (!gameStore.gameId) {
    router.push('/')
  }
})

const handleMove = async (x, y) => {
  await gameStore.makeMove(x, y)
}

const handleUndo = async () => {
  await gameStore.undo()
}

const handleSurrender = async () => {
  showSurrenderDialog.value = true
}

const confirmSurrender = async () => {
  showSurrenderDialog.value = false
  await gameStore.giveUp()
}

const cancelSurrender = () => {
  showSurrenderDialog.value = false
}

const handlePlayAgain = async () => {
  // 保存游戏记录
  let result = 'DRAW'
  if (gameStore.winner !== 'DRAW') {
    result = gameStore.winner === gameStore.playerColor ? 'WIN' : 'LOSE'
  }
  await gameStore.save(result)

  // 重新开始游戏
  await gameStore.startGame(gameStore.gameMode, gameStore.difficulty, gameStore.playerColor === 'BLACK')
}

const handleGoHome = async () => {
  // 保存游戏记录
  let result = 'DRAW'
  if (gameStore.winner !== 'DRAW') {
    result = gameStore.winner === gameStore.playerColor ? 'WIN' : 'LOSE'
  }
  await gameStore.save(result)

  gameStore.reset()
  router.push('/')
}
</script>
