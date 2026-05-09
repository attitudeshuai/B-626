import { defineStore } from 'pinia'
import { ref } from 'vue'
import { startGame as startGameApi, makeMove as makeMoveApi, undoMove as undoMoveApi, surrender as surrenderApi, saveGame as saveGameApi } from '../api/game'
import { createEmptyBoard, oppositeColor } from '../utils/boardRules'

export const useGameStore = defineStore('game', () => {
  const gameId = ref('')
  const board = ref(createEmptyBoard())
  const currentPlayer = ref('BLACK')
  const playerColor = ref('BLACK')
  const gameMode = ref('AI')
  const difficulty = ref('EASY')
  const gameOver = ref(false)
  const winner = ref('')
  const moves = ref([])
  const undoRemaining = ref(3)
  const startTime = ref(0)
  const isLoading = ref(false)

  async function startGame(mode, diff, playerFirst) {
    isLoading.value = true
    try {
      const res = await startGameApi({
        mode,
        difficulty: diff,
        playerFirst
      })
      if (res.code === 200) {
        gameId.value = res.data.gameId
        board.value = res.data.board
        currentPlayer.value = res.data.currentPlayer
        playerColor.value = res.data.playerColor
        gameMode.value = mode
        difficulty.value = diff
        gameOver.value = false
        winner.value = ''
        moves.value = []
        undoRemaining.value = 3
        startTime.value = Date.now()

        if (res.data.aiMove) {
          moves.value.push({
            x: res.data.aiMove[0],
            y: res.data.aiMove[1],
            color: 'BLACK',
            step: 1
          })
        }
        return { success: true }
      }
      return { success: false, message: res.message }
    } finally {
      isLoading.value = false
    }
  }

  async function makeMove(x, y) {
    if (gameOver.value || isLoading.value) return { success: false }

    isLoading.value = true
    try {
      const res = await makeMoveApi({ gameId: gameId.value, x, y })
      if (res.code === 200) {
        board.value = res.data.board

        moves.value.push({
          x,
          y,
          color: currentPlayer.value,
          step: moves.value.length + 1
        })

        if (res.data.gameOver) {
          gameOver.value = true
          winner.value = res.data.winner
        } else {
          currentPlayer.value = res.data.currentPlayer

          if (res.data.aiMove) {
            moves.value.push({
              x: res.data.aiMove.x,
              y: res.data.aiMove.y,
              color: oppositeColor(playerColor.value),
              step: moves.value.length + 1
            })
          }
        }
        return { success: true, data: res.data }
      }
      return { success: false, message: res.message }
    } finally {
      isLoading.value = false
    }
  }

  async function undo() {
    if (undoRemaining.value <= 0) return { success: false, message: '悔棋次数已用完' }

    const res = await undoMoveApi(gameId.value)
    if (res.code === 200) {
      board.value = res.data.board
      currentPlayer.value = res.data.currentPlayer
      undoRemaining.value = res.data.undoRemaining

      if (gameMode.value === 'AI') {
        moves.value = moves.value.slice(0, -2)
      } else {
        moves.value = moves.value.slice(0, -1)
      }
      return { success: true }
    }
    return { success: false, message: res.message }
  }

  async function giveUp() {
    const res = await surrenderApi(gameId.value)
    if (res.code === 200) {
      gameOver.value = true
      winner.value = res.data.winner
      return { success: true }
    }
    return { success: false }
  }

  async function save(result) {
    const duration = Math.floor((Date.now() - startTime.value) / 1000)
    await saveGameApi({
      gameId: gameId.value,
      mode: gameMode.value,
      difficulty: difficulty.value,
      result,
      playerColor: playerColor.value,
      moves: JSON.stringify(moves.value),
      duration
    })
  }

  function reset() {
    gameId.value = ''
    board.value = createEmptyBoard()
    currentPlayer.value = 'BLACK'
    gameOver.value = false
    winner.value = ''
    moves.value = []
    undoRemaining.value = 3
  }

  return {
    gameId,
    board,
    currentPlayer,
    playerColor,
    gameMode,
    difficulty,
    gameOver,
    winner,
    moves,
    undoRemaining,
    isLoading,
    startGame,
    makeMove,
    undo,
    giveUp,
    save,
    reset
  }
})
