<template>
  <header class="page-header">
    <button v-if="back" class="icon-btn" @click="$router.back()"><i class="fa-solid fa-chevron-left" /></button>
    <div v-else class="w-9" />
    <div class="text-center"><p v-if="eyebrow" class="eyebrow">{{ eyebrow }}</p><h1>{{ title }}</h1></div>
    <div class="w-9"><slot name="action" /></div>
  </header>
</template>
<script setup lang="ts">

defineProps<{ title: string; eyebrow?: string; back?: boolean }>()
</script>


