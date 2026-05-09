import { ref } from 'vue'

class WebSocketService {
  constructor() {
    this.socket = null
    this.listeners = new Map()
    this.isConnected = ref(false)
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 5
  }

  connect() {
    return new Promise((resolve, reject) => {
      const token = localStorage.getItem('token')
      if (!token) {
        reject(new Error('未登录'))
        return
      }

      const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
      const wsHost = window.location.host
      const wsUrl = `${wsProtocol}//${wsHost}/ws/game?token=${token}`

      this.socket = new WebSocket(wsUrl)

      this.socket.onopen = () => {
        this.isConnected.value = true
        this.reconnectAttempts = 0
        resolve()
      }

      this.socket.onclose = () => {
        this.isConnected.value = false
        this.handleReconnect()
      }

      this.socket.onerror = (error) => {
        reject(error)
      }

      this.socket.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data)
          const type = data.type
          if (this.listeners.has(type)) {
            this.listeners.get(type).forEach(callback => callback(data))
          }
          if (this.listeners.has('*')) {
            this.listeners.get('*').forEach(callback => callback(data))
          }
        } catch (e) {
          console.error('WebSocket message parse error:', e)
        }
      }
    })
  }

  handleReconnect() {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++
      setTimeout(() => {
        this.connect().catch(() => {})
      }, 2000 * this.reconnectAttempts)
    }
  }

  disconnect() {
    if (this.socket) {
      this.socket.close()
      this.socket = null
    }
    this.isConnected.value = false
  }

  send(type, data = {}) {
    if (this.socket && this.socket.readyState === WebSocket.OPEN) {
      this.socket.send(JSON.stringify({ type, ...data }))
    }
  }

  on(type, callback) {
    if (!this.listeners.has(type)) {
      this.listeners.set(type, [])
    }
    this.listeners.get(type).push(callback)
  }

  off(type, callback) {
    if (this.listeners.has(type)) {
      const callbacks = this.listeners.get(type)
      const index = callbacks.indexOf(callback)
      if (index > -1) {
        callbacks.splice(index, 1)
      }
    }
  }

  removeAllListeners() {
    this.listeners.clear()
  }

  getRooms() {
    this.send('getRooms')
  }

  createRoom(roomName) {
    this.send('createRoom', { roomName })
  }

  joinRoom(roomId) {
    this.send('joinRoom', { roomId })
  }

  leaveRoom() {
    this.send('leaveRoom')
  }

  startMatch() {
    this.send('startMatch')
  }

  cancelMatch() {
    this.send('cancelMatch')
  }

  ready() {
    this.send('ready')
  }

  makeMove(x, y) {
    this.send('move', { x, y })
  }

  requestUndo() {
    this.send('requestUndo')
  }

  respondUndo(accept) {
    this.send('respondUndo', { accept })
  }

  surrender() {
    this.send('surrender')
  }

  sendChat(message) {
    this.send('chat', { message })
  }
}

export const wsService = new WebSocketService()
