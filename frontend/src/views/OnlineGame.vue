<template>
  <div class="min-h-screen bg-zen-bg py-12 px-4">
    <div class="max-w-5xl mx-auto">
      <div class="flex flex-col lg:flex-row items-start gap-12">
        <!-- 棋盘区域 -->
        <div class="flex-1 w-full lg:w-auto flex justify-center">
          <div class="card-zen p-2 inline-block">
            <GameBoard
              :board="onlineStore.board"
              :current-player="onlineStore.currentPlayer"
              :player-color="onlineStore.playerColor"
              :game-over="onlineStore.gameOver"
              :moves="onlineStore.moves"
              :show-order="showOrder"
              :disabled="onlineStore.gameOver || onlineStore.currentPlayer !== onlineStore.playerColor"
              @move="handleMove"
            />
          </div>
        </div>

        <!-- 信息面板 -->
        <div class="w-full lg:w-80 space-y-8">
          <!-- 对战信息 -->
          <section class="space-y-4">
            <header class="border-b border-zen-border pb-2">
              <h3 class="text-xs text-zen-muted uppercase tracking-[0.3em]">对战信息</h3>
            </header>

            <div class="grid grid-cols-2 gap-4 text-center">
              <div class="p-3 border border-zen-border" :class="isMyTurn ? '' : 'opacity-50'">
                <div class="flex items-center justify-center space-x-2 mb-1">
                  <span class="w-3 h-3 rounded-full" :class="onlineStore.playerColor === 'BLACK' ? 'bg-zen-text' : 'bg-white border border-zen-border'"></span>
                  <span class="text-xs text-zen-muted">我</span>
                </div>
                <div class="text-sm font-light truncate">{{ myNickname }}</div>
              </div>
              <div class="p-3 border border-zen-border" :class="!isMyTurn ? '' : 'opacity-50'">
                <div class="flex items-center justify-center space-x-2 mb-1">
                  <span class="w-3 h-3 rounded-full" :class="onlineStore.playerColor === 'BLACK' ? 'bg-white border border-zen-border' : 'bg-zen-text'"></span>
                  <span class="text-xs text-zen-muted">对手</span>
                </div>
                <div class="text-sm font-light truncate">{{ onlineStore.opponentNickname }}</div>
              </div>
            </div>

            <div class="text-center">
              <span v-if="isMyTurn && !onlineStore.gameOver" class="text-sm text-zen-text animate-pulse tracking-widest">
                你的回合
              </span>
              <span v-else-if="!onlineStore.gameOver" class="text-sm text-zen-muted tracking-widest">
                对手思考中...
              </span>
            </div>
          </section>

          <!-- 悔棋请求弹窗 -->
          <div v-if="onlineStore.undoRequested" class="p-4 border border-zen-border bg-white space-y-3">
            <p class="text-sm text-center">对手请求悔棋</p>
            <div class="flex space-x-2">
              <button @click="respondUndo(true)" class="btn-zen flex-1 text-xs py-2">同意</button>
              <button @click="respondUndo(false)" class="btn-zen flex-1 text-xs py-2">拒绝</button>
            </div>
          </div>

          <!-- 操作按钮 -->
          <section class="space-y-3">
            <button
              @click="requestUndo"
              :disabled="onlineStore.gameOver || onlineStore.moves.length === 0 || onlineStore.undoRemaining <= 0"
              class="btn-zen w-full text-sm py-3"
            >
              请求悔棋 ({{ onlineStore.undoRemaining }})
            </button>

            <button
              @click="handleSurrender"
              :disabled="onlineStore.gameOver"
              class="btn-zen w-full text-sm py-3"
            >
              认输
            </button>

            <label class="flex items-center justify-center space-x-3 cursor-pointer group pt-2">
              <input v-model="showOrder" type="checkbox" class="hidden">
              <div class="w-4 h-4 border border-zen-border flex items-center justify-center transition-colors" :class="showOrder ? 'bg-zen-text' : 'bg-white'">
                <div v-if="showOrder" class="w-1.5 h-1.5 bg-white"></div>
              </div>
              <span class="text-[10px] text-zen-muted uppercase tracking-[0.2em] group-hover:text-zen-text transition-colors">显示手数</span>
            </label>
          </section>

          <!-- 聊天 -->
          <section class="space-y-3">
            <header class="border-b border-zen-border pb-2">
              <h3 class="text-xs text-zen-muted uppercase tracking-[0.3em]">聊天</h3>
            </header>
            <div class="h-32 overflow-y-auto space-y-1 pr-2 custom-scrollbar border border-zen-border p-2">
              <div
                v-for="(msg, idx) in onlineStore.chatMessages"
                :key="idx"
                class="text-[10px]"
              >
                <span class="text-zen-muted">{{ msg.nickname }}:</span>
                <span class="text-zen-text ml-1">{{ msg.message }}</span>
              </div>
              <div v-if="onlineStore.chatMessages.length === 0" class="text-[10px] text-zen-muted text-center py-4">
                暂无消息
              </div>
            </div>
            <div class="flex space-x-2">
              <input
                v-model="chatInput"
                type="text"
                placeholder="发送消息..."
                class="input-zen flex-1 text-xs py-2"
                @keyup.enter="sendChat"
              />
              <button @click="sendChat" class="btn-zen px-4 text-xs">发送</button>
            </div>
          </section>

          <!-- 最近手数 -->
          <section class="space-y-3">
            <header class="border-b border-zen-border pb-2">
              <h3 class="text-xs text-zen-muted uppercase tracking-[0.3em]">最近手数</h3>
            </header>
            <div class="h-32 overflow-y-auto space-y-1 pr-2 custom-scrollbar">
              <div
                v-for="move in [...onlineStore.moves].reverse().slice(0, 10)"
                :key="move.step"
                class="flex items-center justify-between text-[10px] tracking-widest py-1 border-b border-zen-bg"
              >
                <div class="flex items-center space-x-2">
                  <span class="w-2 h-2 rounded-full" :class="move.color === 'BLACK' ? 'bg-zen-text' : 'bg-white border border-zen-border'"></span>
                  <span class="text-zen-muted">第 {{ move.step }} 手</span>
                </div>
                <span class="text-zen-text">({{ move.x }}, {{ move.y }})</span>
              </div>
              <div v-if="onlineStore.moves.length === 0" class="text-[10px] text-zen-muted italic text-center py-4 tracking-widest uppercase">
                虚位以待
              </div>
            </div>
          </section>
        </div>
      </div>

      <!-- 胜负弹窗 -->
      <transition name="fade">
        <div v-if="onlineStore.gameOver" class="fixed inset-0 bg-white/90 backdrop-blur-sm flex items-center justify-center z-50">
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
              <button @click="handleGoLobby" class="btn-zen w-full">
                返回大厅
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

      <!-- Toast 提示 -->
      <Toast :visible="showToast" :message="toastMessage" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useOnlineGameStore } from '../stores/onlineGame'
import { useAuthStore } from '../stores/auth'
import { wsService } from '../services/websocket'
import GameBoard from '../components/GameBoard.vue'
import ConfirmDialog from '../components/ConfirmDialog.vue'
import Toast from '../components/Toast.vue'

const router = useRouter()
const onlineStore = useOnlineGameStore()
const authStore = useAuthStore()

const showOrder = ref(false)
const chatInput = ref('')
const showSurrenderDialog = ref(false)
const toastMessage = ref('')
const showToast = ref(false)

const myNickname = computed(() => authStore.user?.nickname || '我')

const isMyTurn = computed(() => {
  return onlineStore.currentPlayer === onlineStore.playerColor
})

const getResultTitle = computed(() => {
  if (onlineStore.winner === 'DRAW') return '平局'
  if (onlineStore.winner === onlineStore.playerColor) return '你赢了！'
  return '你输了'
})

const getResultMessage = computed(() => {
  if (onlineStore.winner === 'DRAW') return '棋盘已满，双方平局'
  if (onlineStore.winner === onlineStore.playerColor) return '恭喜你取得胜利！'
  return '再接再厉，下次一定能赢！'
})

onMounted(() => {
  if (!onlineStore.roomId) {
    router.push('/online')
    return
  }
  setupListeners()
})

onUnmounted(() => {
  wsService.removeAllListeners()
})

function setupListeners() {
  wsService.on('moveMade', (data) => {
    onlineStore.updateBoard(data.move.board)
    onlineStore.addMove({
      x: data.move.x,
      y: data.move.y,
      color: data.move.color,
      step: data.move.step
    })
    onlineStore.setCurrentPlayer(data.currentPlayer)
  })

  wsService.on('gameOver', (data) => {
    if (data.move) {
      onlineStore.updateBoard(data.move.board)
      onlineStore.addMove({
        x: data.move.x,
        y: data.move.y,
        color: data.move.color,
        step: data.move.step
      })
    }
    onlineStore.setGameOver(data.winner)
  })

  wsService.on('undoRequest', () => {
    onlineStore.setUndoRequest(true)
  })

  wsService.on('undoAccepted', (data) => {
    onlineStore.updateBoard(data.board)
    onlineStore.setCurrentPlayer(data.currentPlayer)
    onlineStore.setUndoRemaining(data.undoRemaining)
    onlineStore.moves.splice(-2)
    onlineStore.setUndoRequest(false)
  })

  wsService.on('undoRejected', () => {
    showToastMessage('对手拒绝了悔棋请求')
    onlineStore.setUndoRequest(false)
  })

  wsService.on('chat', (data) => {
    onlineStore.addChatMessage({
      nickname: data.nickname,
      message: data.message,
      timestamp: data.timestamp
    })
  })

  wsService.on('error', (data) => {
    showToastMessage(data.message)
  })
}

function handleMove(x, y) {
  if (isMyTurn.value && !onlineStore.gameOver) {
    wsService.makeMove(x, y)
  }
}

function requestUndo() {
  wsService.requestUndo()
}

function respondUndo(accept) {
  wsService.respondUndo(accept)
  onlineStore.setUndoRequest(false)
}

function handleSurrender() {
  showSurrenderDialog.value = true
}

function confirmSurrender() {
  showSurrenderDialog.value = false
  wsService.surrender()
}

function cancelSurrender() {
  showSurrenderDialog.value = false
}

function showToastMessage(msg) {
  toastMessage.value = msg
  showToast.value = true
  setTimeout(() => {
    showToast.value = false
  }, 3000)
}

function sendChat() {
  if (chatInput.value.trim()) {
    wsService.sendChat(chatInput.value.trim())
    chatInput.value = ''
  }
}

function handleGoLobby() {
  wsService.leaveRoom()
  onlineStore.reset()
  router.push('/online')
}

function handleGoHome() {
  wsService.leaveRoom()
  onlineStore.reset()
  router.push('/')
}
</script>
