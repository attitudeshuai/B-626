<template>
  <div class="flex justify-center p-4">
    <div class="border-2 border-amber-800 shadow-lg">
      <div class="relative bg-amber-200" :style="boardStyle">
        <div class="absolute inset-0 pointer-events-none">
          <div
            v-for="i in 15"
            :key="'h' + i"
            class="absolute bg-amber-700"
            :style="getHorizontalLineStyle(i)"
          ></div>
          <div
            v-for="i in 15"
            :key="'v' + i"
            class="absolute bg-amber-700"
            :style="getVerticalLineStyle(i)"
          ></div>
          <div
            v-for="point in starPoints"
            :key="'star' + point.x + point.y"
            class="absolute w-2 h-2 bg-amber-800 rounded-full -translate-x-1 -translate-y-1"
            :style="getStarPointStyle(point)"
          ></div>
        </div>

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
              :style="getCellStyle(x, y)"
              :class="{
                'cursor-pointer': canPlace(x, y)
              }"
              @click="handleClick(x, y)"
            >
              <div
                v-if="canPlace(x, y)"
                class="absolute inset-1 opacity-0 hover:opacity-100 transition-opacity"
              >
                <div class="w-full h-full rounded-full bg-amber-400/50"></div>
              </div>

              <div
                v-if="cell !== 0"
                class="rounded-full flex items-center justify-center shadow-md relative z-10"
                :style="getPieceStyle()"
                :class="cell === 1 ? 'bg-gray-900' : 'bg-gray-100 border-2 border-gray-400'"
              >
                <span v-if="showOrder" class="text-[8px] tracking-tighter font-bold" :class="cell === 1 ? 'text-white' : 'text-gray-800'">
                  {{ getMoveOrder(x, y) }}
                </span>
              </div>

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
import { useBoardRenderer } from '../composables/useBoardRenderer'

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

const {
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
} = useBoardRenderer(props)

const handleClick = (x, y) => {
  if (canPlace(x, y)) {
    emit('move', x, y)
  }
}
</script>
