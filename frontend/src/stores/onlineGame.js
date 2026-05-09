import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useOnlineGameStore = defineStore('onlineGame', () => {
  const roomId = ref('')
  const board = ref(Array(15).fill(null).map(() => Array(15).fill(0)))
  const currentPlayer = ref('BLACK')
  const playerColor = ref('BLACK')
  const gameOver = ref(false)
  const winner = ref('')
  const moves = ref([])
  const undoRemaining = ref(3)
  const hostNickname = ref('')
  const guestNickname = ref('')
  const isHost = ref(false)
  const opponentNickname = ref('')
  const chatMessages = ref([])
  const undoRequested = ref(false)
  const undoRequestFrom = ref(null)

  function setGameData(data) {
    roomId.value = data.roomId
    board.value = data.board
    currentPlayer.value = data.currentPlayer
    playerColor.value = data.playerColor
    hostNickname.value = data.hostNickname
    guestNickname.value = data.guestNickname
    isHost.value = data.isHost
    undoRemaining.value = data.undoRemaining || 3
    gameOver.value = false
    winner.value = ''
    moves.value = []
    chatMessages.value = []
    undoRequested.value = false
    undoRequestFrom.value = null
    opponentNickname.value = data.isHost ? data.guestNickname : data.hostNickname
  }

  function updateBoard(newBoard) {
    board.value = newBoard
  }

  function addMove(move) {
    moves.value.push(move)
  }

  function setCurrentPlayer(player) {
    currentPlayer.value = player
  }

  function setGameOver(winnerColor) {
    gameOver.value = true
    winner.value = winnerColor
  }

  function setUndoRemaining(remaining) {
    undoRemaining.value = remaining
  }

  function addChatMessage(message) {
    chatMessages.value.push(message)
    if (chatMessages.value.length > 50) {
      chatMessages.value.shift()
    }
  }

  function setUndoRequest(requested, fromUserId = null) {
    undoRequested.value = requested
    undoRequestFrom.value = fromUserId
  }

  function reset() {
    roomId.value = ''
    board.value = Array(15).fill(null).map(() => Array(15).fill(0))
    currentPlayer.value = 'BLACK'
    playerColor.value = 'BLACK'
    gameOver.value = false
    winner.value = ''
    moves.value = []
    undoRemaining.value = 3
    hostNickname.value = ''
    guestNickname.value = ''
    isHost.value = false
    opponentNickname.value = ''
    chatMessages.value = []
    undoRequested.value = false
    undoRequestFrom.value = null
  }

  return {
    roomId,
    board,
    currentPlayer,
    playerColor,
    gameOver,
    winner,
    moves,
    undoRemaining,
    hostNickname,
    guestNickname,
    isHost,
    opponentNickname,
    chatMessages,
    undoRequested,
    undoRequestFrom,
    setGameData,
    updateBoard,
    addMove,
    setCurrentPlayer,
    setGameOver,
    setUndoRemaining,
    addChatMessage,
    setUndoRequest,
    reset
  }
})
