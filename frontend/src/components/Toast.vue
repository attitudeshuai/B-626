<template>
  <teleport to="body">
    <transition name="slide">
      <div
        v-if="visible"
        class="fixed top-20 left-1/2 -translate-x-1/2 z-50 bg-zen-text text-white px-6 py-3 shadow-lg max-w-sm"
      >
        <p class="text-sm tracking-widest text-center">{{ message }}</p>
      </div>
    </transition>
  </teleport>
</template>

<script setup>
defineProps({
  visible: { type: Boolean, default: false },
  message: { type: String, default: '' }
})
</script>

<style scoped>
.slide-enter-active,
.slide-leave-active {
  transition: all 0.3s ease;
}

.slide-enter-from,
.slide-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(-20px);
}
</style>
