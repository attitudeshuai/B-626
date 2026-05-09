<template>
  <div class="flex justify-center p-4">
    <!-- 外层包装，添加边框 -->
    <div class="border-2 border-amber-800 shadow-lg">
      <!-- 棋盘主体 -->
      <div class="relative bg-amber-200" :style="boardStyle">
        <!-- 网格线容器 -->
        <div class="absolute inset-0 pointer-events-none">
          <!-- 横线 -->
          <div
            v-for="i in 15"
            :key="'h' + i"
            class="absolute bg-amber-700"
            :style="{
              left: halfCell + 'px',
              top: ((i - 1) * cellSize + halfCell) + 'px',
              width: (cellSize * 14) + 'px',
              height: '1px'
            }"
          ></div>
          <!-- 竖线 -->
          <div
            v-for="i in 15"
            :key="'v' + i"
            class="absolute bg-amber-700"
            :style="{
              left: ((i - 1) * cellSize + halfCell) + 'px',
              top: halfCell + 'px',
              width: '1px',
              height: (cellSize * 14) + 'px'
            }"
          ></div>
          <!-- 星位点 -->
          <div
            v-for="point in starPoints"
            :key="'star' + point.x + point.y"
            class="absolute w-2 h-2 bg-amber-800 rounded-full -translate-x-1 -translate-y-1"
            :style="{
              left: (point.x * cellSize + halfCell) + 'px',
              top: (point.y * cellSize + halfCell) + 'px'
            }"
          ></div>
        </div>

        <!-- 棋子和点击区域 -->
        <div class="relative z-10">
          <div
            v-for="(row, x) in board"
            :key="'row' + x"
            class="flex"
          >
            <div
              v-for="(cell, y) in row"
              :key="'cell' + x + '-' + y"
              class="relative flex items-center justify-center"
              :style="{ width: cellSize + 'px', height: cellSize + 'px' }"
              :class="{
                'cursor-pointer': canPlace(x, y)
              }"
              @click="handleClick(x, y)"
            >
              <!-- Hover 效果 -->
              <div
                v-if="canPlace(x, y)"
                class="absolute inset-1 opacity-0 hover:opacity-100 transition-opacity"
              >
                <div class="w-full h-full rounded-full bg-amber-400/50"></div>
              </div>

              <!-- 棋子 -->
              <div
                v-if="cell !== 0"
                class="rounded-full flex items-center justify-center shadow-md relative z-10"
                :style="{ width: pieceSize + 'px', height: pieceSize + 'px' }"
                :class="cell === 1 ? 'bg-gray-900' : 'bg-gray-100 border-2 border-gray-400'"
              >
                <span v-if="showOrder" class="text-[8px] tracking-tighter font-bold" :class="cell === 1 ? 'text-white' : 'text-gray-800'">
                  {{ getMoveOrder(x, y) }}
                </span>
              </div>

              <!-- 最近一步标记 -->
              <div
                v-if="isLastMove(x, y) && cell !== 0"
                class="absolute w-2 h-2 rounded-full bg-red-500 z-20"
              ></div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  board: { type: Array, required: true },
  currentPlayer: { type: String, default: 'BLACK' },
  playerColor: { type: String, default: 'BLACK' },
  gameOver: { type: Boolean, default: false },
  moves: { type: Array, default: () => [] },
  showOrder: { type: Boolean, default: false },
  disabled: { type: Boolean, default: false }
})

const emit = defineEmits(['move'])

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

const handleClick = (x, y) => {
  if (canPlace(x, y)) {
    emit('move', x, y)
  }
}
</script>
