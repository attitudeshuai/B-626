<template>
  <div class="min-h-screen bg-zen-bg py-12 px-4">
    <div class="max-w-4xl mx-auto space-y-12">
      <!-- Header -->
      <header class="text-center space-y-4">
        <h1 class="text-4xl font-light tracking-[0.3em] text-zen-text">联机对战</h1>
        <p class="text-xs text-zen-muted uppercase tracking-[0.5em]">Online Battle</p>
      </header>

      <!-- 连接状态 -->
      <div class="flex justify-center">
        <span class="flex items-center space-x-2 text-xs tracking-widest">
          <span class="w-2 h-2 rounded-full" :class="isConnected ? 'bg-green-500' : 'bg-red-500'"></span>
          <span class="text-zen-muted">{{ isConnected ? '已连接' : '未连接' }}</span>
        </span>
      </div>

      <!-- 匹配中状态 -->
      <div v-if="isMatching" class="card-zen p-8 text-center space-y-6">
        <div class="animate-pulse">
          <div class="text-2xl font-light tracking-[0.2em] text-zen-text">匹配中...</div>
          <p class="text-xs text-zen-muted mt-2 tracking-widest">正在寻找对手</p>
        </div>
        <button @click="cancelMatch" class="btn-zen px-8">取消匹配</button>
      </div>

      <!-- 房间中状态 -->
      <div v-else-if="currentRoom" class="card-zen p-8 space-y-6">
        <div class="text-center space-y-2">
          <h2 class="text-xl font-light tracking-[0.2em] text-zen-text">{{ currentRoom.roomName }}</h2>
          <p class="text-xs text-zen-muted tracking-widest">房间ID: {{ currentRoom.roomId }}</p>
        </div>

        <div class="grid grid-cols-2 gap-4">
          <div class="p-4 border border-zen-border text-center">
            <div class="text-xs text-zen-muted tracking-widest mb-2">房主</div>
            <div class="text-lg font-light">{{ currentRoom.hostNickname }}</div>
          </div>
          <div class="p-4 border border-zen-border text-center">
            <div class="text-xs text-zen-muted tracking-widest mb-2">玩家</div>
            <div class="text-lg font-light">{{ currentRoom.guestNickname || '等待中...' }}</div>
          </div>
        </div>

        <div class="flex justify-center space-x-4">
          <button v-if="currentRoom.guestNickname && isHost" @click="startGame" class="btn-zen px-8">
            开始游戏
          </button>
          <button @click="leaveRoom" class="btn-zen px-8">离开房间</button>
        </div>
      </div>

      <!-- 大厅 -->
      <div v-else class="space-y-8">
        <!-- 快速匹配 -->
        <section class="space-y-4">
          <h2 class="text-sm text-zen-muted uppercase tracking-[0.2em] text-center">快速匹配</h2>
          <button @click="startMatch" :disabled="!isConnected" class="btn-zen w-full py-4">
            开始匹配
          </button>
        </section>

        <!-- 创建房间 -->
        <section class="space-y-4">
          <h2 class="text-sm text-zen-muted uppercase tracking-[0.2em] text-center">创建房间</h2>
          <div class="flex space-x-4">
            <input
              v-model="newRoomName"
              type="text"
              placeholder="房间名称"
              class="input-zen flex-1"
            />
            <button @click="createRoom" :disabled="!isConnected" class="btn-zen px-8">
              创建
            </button>
          </div>
        </section>

        <!-- 房间列表 -->
        <section class="space-y-4">
          <div class="flex justify-between items-center">
            <h2 class="text-sm text-zen-muted uppercase tracking-[0.2em]">房间列表</h2>
            <button @click="refreshRooms" :disabled="!isConnected" class="text-xs text-zen-muted hover:text-zen-text tracking-widest">
              刷新
            </button>
          </div>

          <div v-if="rooms.length === 0" class="text-center py-8 text-zen-muted text-xs tracking-widest">
            暂无可用房间
          </div>

          <div v-else class="space-y-2">
            <div
              v-for="room in rooms"
              :key="room.roomId"
              class="p-4 border border-zen-border hover:border-zen-text transition-colors cursor-pointer flex justify-between items-center"
              @click="joinRoom(room.roomId)"
            >
              <div>
                <div class="text-sm font-light">{{ room.roomName }}</div>
                <div class="text-xs text-zen-muted tracking-widest">房主: {{ room.hostNickname }}</div>
              </div>
              <span class="text-xs text-zen-muted tracking-widest">加入</span>
            </div>
          </div>
        </section>
      </div>

      <!-- 返回按钮 -->
      <div class="text-center">
        <button @click="goHome" class="text-xs text-zen-muted hover:text-zen-text tracking-[0.2em] transition-colors uppercase">
          返回首页
        </button>
      </div>

      <!-- Toast 提示 -->
      <Toast :visible="showToast" :message="toastMessage" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { wsService } from '../services/websocket'
import { useOnlineGameStore } from '../stores/onlineGame'
import Toast from '../components/Toast.vue'

const router = useRouter()
const onlineStore = useOnlineGameStore()

const newRoomName = ref('')
const rooms = ref([])
const isMatching = ref(false)
const currentRoom = ref(null)
const isHost = ref(false)
const toastMessage = ref('')
const showToast = ref(false)

const isConnected = computed(() => wsService.isConnected.value)

onMounted(async () => {
  try {
    await wsService.connect()
    setupListeners()
    refreshRooms()
  } catch (e) {
    console.error('WebSocket连接失败:', e)
  }
})

onUnmounted(() => {
  wsService.removeAllListeners()
})

function setupListeners() {
  wsService.on('roomList', (data) => {
    rooms.value = data.rooms || []
  })

  wsService.on('roomCreated', (data) => {
    currentRoom.value = {
      roomId: data.roomId,
      roomName: data.roomName,
      hostNickname: '我'
    }
    isHost.value = true
  })

  wsService.on('roomJoined', (data) => {
    currentRoom.value = {
      roomId: data.roomId,
      roomName: data.roomName,
      hostNickname: data.hostNickname
    }
    isHost.value = false
  })

  wsService.on('playerJoined', (data) => {
    if (currentRoom.value) {
      currentRoom.value.guestNickname = data.guestNickname
    }
  })

  wsService.on('playerLeft', () => {
    if (currentRoom.value) {
      currentRoom.value.guestNickname = null
    }
  })

  wsService.on('roomClosed', () => {
    currentRoom.value = null
    isHost.value = false
    showToastMessage('房间已关闭')
  })

  wsService.on('leftRoom', () => {
    currentRoom.value = null
    isHost.value = false
    refreshRooms()
  })

  wsService.on('matchStarted', () => {
    isMatching.value = true
  })

  wsService.on('matchCancelled', () => {
    isMatching.value = false
  })

  wsService.on('matchFound', (data) => {
    isMatching.value = false
    onlineStore.setGameData(data)
    router.push('/online-game')
  })

  wsService.on('gameStart', (data) => {
    onlineStore.setGameData(data)
    router.push('/online-game')
  })

  wsService.on('error', (data) => {
    showToastMessage(data.message)
  })
}

function showToastMessage(msg) {
  toastMessage.value = msg
  showToast.value = true
  setTimeout(() => {
    showToast.value = false
  }, 3000)
}

function refreshRooms() {
  wsService.getRooms()
}

function createRoom() {
  const name = newRoomName.value.trim() || '新房间'
  wsService.createRoom(name)
  newRoomName.value = ''
}

function joinRoom(roomId) {
  wsService.joinRoom(roomId)
}

function leaveRoom() {
  wsService.leaveRoom()
}

function startMatch() {
  wsService.startMatch()
}

function cancelMatch() {
  wsService.cancelMatch()
  isMatching.value = false
}

function startGame() {
  wsService.ready()
}

function goHome() {
  if (currentRoom.value) {
    wsService.leaveRoom()
  }
  if (isMatching.value) {
    wsService.cancelMatch()
  }
  router.push('/')
}
</script>
