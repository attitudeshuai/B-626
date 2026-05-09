<template>
  <teleport to="body">
    <transition name="fade">
      <div v-if="visible" class="fixed inset-0 z-50 flex items-center justify-center">
        <!-- 遮罩 -->
        <div class="absolute inset-0 bg-black/40" @click="handleCancel"></div>

        <!-- 对话框 -->
        <div class="relative bg-white shadow-xl max-w-sm w-full mx-4 p-6 space-y-6">
          <div class="text-center space-y-2">
            <h3 class="text-lg font-light tracking-widest text-zen-text">{{ title }}</h3>
            <p v-if="message" class="text-sm text-zen-muted">{{ message }}</p>
          </div>

          <div class="flex space-x-3">
            <button
              @click="handleCancel"
              class="flex-1 py-3 border border-zen-border text-sm tracking-widest text-zen-muted hover:text-zen-text hover:border-zen-text transition-colors"
            >
              {{ cancelText }}
            </button>
            <button
              @click="handleConfirm"
              class="flex-1 py-3 bg-zen-text text-white text-sm tracking-widest hover:bg-gray-800 transition-colors"
            >
              {{ confirmText }}
            </button>
          </div>
        </div>
      </div>
    </transition>
  </teleport>
</template>

<script setup>
defineProps({
  visible: { type: Boolean, default: false },
  title: { type: String, default: '确认' },
  message: { type: String, default: '' },
  confirmText: { type: String, default: '确定' },
  cancelText: { type: String, default: '取消' }
})

const emit = defineEmits(['confirm', 'cancel'])

function handleConfirm() {
  emit('confirm')
}

function handleCancel() {
  emit('cancel')
}
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
