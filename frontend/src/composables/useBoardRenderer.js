import { computed } from 'vue'

export function useBoardRenderer(props) {
  const cellSize = 32
  const pieceSize = 26
  const halfCell = cellSize / 2
  const boardWidth = cellSize * 15

  const boardStyle = computed(() => ({
    width: `${boardWidth}px`,
    height: `${boardWidth}px`
  }))

  const starPoints = [
    { x: 3, y: 3 }, { x: 3, y: 7 }, { x: 3, y: 11 },
    { x: 7, y: 3 }, { x: 7, y: 7 }, { x: 7, y: 11 },
    { x: 11, y: 3 }, { x: 11, y: 7 }, { x: 11, y: 11 }
  ]

  const canPlace = (x, y) => {
    return !props.gameOver && !props.disabled && props.board[x][y] === 0
  }

  const isLastMove = (x, y) => {
    if (props.moves.length === 0) return false
    const lastMove = props.moves[props.moves.length - 1]
    return lastMove.x === x && lastMove.y === y
  }

  const getMoveOrder = (x, y) => {
    const move = props.moves.find(m => m.x === x && m.y === y)
    return move ? move.step : ''
  }

  const getCellStyle = (x, y) => ({
    width: cellSize + 'px',
    height: cellSize + 'px'
  })

  const getHorizontalLineStyle = (i) => ({
    left: halfCell + 'px',
    top: ((i - 1) * cellSize + halfCell) + 'px',
    width: (cellSize * 14) + 'px',
    height: '1px'
  })

  const getVerticalLineStyle = (i) => ({
    left: ((i - 1) * cellSize + halfCell) + 'px',
    top: halfCell + 'px',
    width: '1px',
    height: (cellSize * 14) + 'px'
  })

  const getStarPointStyle = (point) => ({
    left: (point.x * cellSize + halfCell) + 'px',
    top: (point.y * cellSize + halfCell) + 'px'
  })

  const getPieceStyle = () => ({
    width: pieceSize + 'px',
    height: pieceSize + 'px'
  })

  return {
    cellSize,
    pieceSize,
    halfCell,
    boardWidth,
    boardStyle,
    starPoints,
    canPlace,
    isLastMove,
    getMoveOrder,
    getCellStyle,
    getHorizontalLineStyle,
    getVerticalLineStyle,
    getStarPointStyle,
    getPieceStyle
  }
}
