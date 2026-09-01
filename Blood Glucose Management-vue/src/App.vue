<template>
  <main class="phone-shell">
    <div class="status-bar"><span><i class="fa-solid fa-signal"/><i class="fa-solid fa-wifi ml-1"/></span><span class="island">9:41</span><span>100% <i class="fa-solid fa-battery-full"/></span></div>
    <RouterView />
    <BottomNav v-if="showNav" />
    <div v-if="toast" class="toast">{{ toast }}</div>
  </main>
</template>
<script setup lang="ts">

import { computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useStore } from 'vuex'
import BottomNav from './components/BottomNav.vue'
import type { RootState } from './main'
const route = useRoute(); const store = useStore<RootState>()
const showNav = computed(() => ['/home','/records','/trends','/profile'].includes(route.path))
const toast = computed(() => store.state.ui.toast)
watch(toast, (value) => { if (value) window.setTimeout(() => store.commit('ui/clearToast'), 1800) })
</script>


