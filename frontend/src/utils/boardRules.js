export const BOARD_SIZE = 15

export const BLACK = 1
export const WHITE = 2

export function createEmptyBoard() {
  return Array(BOARD_SIZE).fill(null).map(() => Array(BOARD_SIZE).fill(0))
}

export function isValidPosition(x, y) {
  return x >= 0 && x < BOARD_SIZE && y >= 0 && y < BOARD_SIZE
}

export function isEmptyCell(board, x, y) {
  return isValidPosition(x, y) && board[x][y] === 0
}

export function canPlace(board, x, y, gameOver, disabled) {
  return !gameOver && !disabled && isEmptyCell(board, x, y)
}

export function colorToValue(color) {
  return color === 'BLACK' ? BLACK : WHITE
}

export function valueToColor(value) {
  return value === BLACK ? 'BLACK' : 'WHITE'
}

export function oppositeColor(color) {
  return color === 'BLACK' ? 'WHITE' : 'BLACK'
}

export function isMyTurn(currentPlayer, playerColor) {
  return currentPlayer === playerColor
}
