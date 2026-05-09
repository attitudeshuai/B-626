<template>
  <div class="min-h-screen">
    <Navbar v-if="authStore.isLoggedIn" />
    <router-view />
  </div>
</template>

<script setup>
import { useAuthStore } from './stores/auth'
import Navbar from './components/Navbar.vue'

const authStore = useAuthStore()
</script>
