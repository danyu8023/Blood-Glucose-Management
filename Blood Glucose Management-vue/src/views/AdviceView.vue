<template>
  <section class="page">
    <PageHeader title="智能建议" eyebrow="基于最近记录" :back="true" />

    <div class="glass rounded-2xl p-4 mb-4">
      <div class="flex items-center justify-between mb-3"><div><p class="font-semibold">选择一条血糖记录</p><p class="text-xs muted mt-1">建议会结合测量时段进行判断</p></div><span v-if="recordId" class="text-xs text-[#34c759]">已关联最近记录</span></div>
      <div class="grid grid-cols-2 gap-3">
        <label class="text-xs muted">血糖值（mmol/L）<input v-model.number="value" type="number" min="0" max="40" step="0.1" class="w-full mt-2 p-3 rounded-xl border-0 bg-[#f7f7f9] text-2xl font-semibold" placeholder="例如 6.8" @keyup.enter="generate" /></label>
        <label class="text-xs muted">测量时段<select v-model="period" class="w-full mt-2 p-3 rounded-xl border-0 bg-[#f7f7f9]"><option value="fasting">空腹</option><option value="pre_meal">餐前</option><option value="post_meal">餐后</option><option value="bedtime">睡前</option></select></label>
      </div>
      <p v-if="validationError" class="text-xs text-[#ff3b30] mt-3">{{ validationError }}</p>
      <button class="btn-primary mt-4" type="button" :disabled="loadingAdvice || !!validationError" @click="generate"><i :class="['fa-solid', loadingAdvice ? 'fa-spinner fa-spin' : 'fa-wand-magic-sparkles']" />{{ loadingAdvice ? '生成中…' : '生成最新建议' }}</button>
    </div>

    <div v-if="loadingLatest" class="glass rounded-2xl p-6 text-center muted text-sm"><i class="fa-solid fa-spinner fa-spin mr-2" />正在读取最近血糖…</div>
    <div v-else-if="error" class="glass rounded-2xl p-5 mb-4"><p class="text-sm text-[#ff3b30]">{{ error }}</p><button class="text-sm text-[#0a84ff] mt-3" type="button" @click="loadLatest">重新加载</button></div>
    <template v-else-if="advice">
      <div class="glass rounded-2xl p-4 mb-4" :class="tone"><div class="flex gap-3"><span class="w-10 h-10 rounded-xl bg-white/70 grid place-items-center shrink-0"><i class="fa-solid fa-heart-pulse" /></span><div class="flex-1"><div class="flex items-center justify-between gap-2"><h2 class="font-semibold">{{ advice.title }}</h2><span class="chip text-xs" :class="riskClass">{{ riskText }}</span></div><p class="text-sm muted mt-2 leading-relaxed">{{ advice.summary }}</p><p v-if="advice.urgent" class="text-xs text-[#ff3b30] mt-3"><i class="fa-solid fa-triangle-exclamation mr-1" />建议尽快复测，必要时联系医生或家属</p></div></div></div>
      <div class="glass rounded-2xl p-4 mb-4"><h2 class="font-semibold">饮食建议</h2><p class="text-sm muted mt-2 leading-relaxed">{{ advice.diet }}</p></div>
      <div class="glass rounded-2xl p-4 mb-4"><h2 class="font-semibold">用药与行动</h2><p class="text-sm muted mt-2 leading-relaxed">{{ advice.medication }}</p><ul v-if="advice.actions?.length" class="mt-3 space-y-2"><li v-for="action in advice.actions" :key="action" class="flex gap-2 text-sm"><i class="fa-solid fa-circle-check text-[#34c759] mt-1" /><span>{{ action }}</span></li></ul></div>
      <div class="glass rounded-2xl p-3 text-xs muted leading-relaxed"><i class="fa-solid fa-circle-info text-[#0a84ff] mr-1" />{{ advice.disclaimer || '建议仅供日常管理参考，不能替代医生诊断，不要自行调整药量。' }}</div>
    </template>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import PageHeader from '../components/PageHeader.vue'
import { apiClient } from '../services/api'

type Advice = { risk?: string; title: string; summary: string; diet: string; medication: string; actions?: string[]; urgent?: boolean; disclaimer?: string }
const value = ref<number | null>(null); const period = ref('post_meal'); const recordId = ref(''); const loadingLatest = ref(true); const loadingAdvice = ref(false); const error = ref(''); const advice = ref<Advice | null>(null); let requestId = 0; let debounceTimer: number | undefined
const periodNames: Record<string, string> = { fasting: '空腹', pre_meal: '餐前', post_meal: '餐后', bedtime: '睡前' }
const validationError = computed(() => value.value === null || !Number.isFinite(value.value) ? '请输入血糖值' : value.value < 0 || value.value > 40 ? '血糖值应在 0–40 mmol/L 之间' : '')
const riskText = computed(() => ({ critical_low: '紧急偏低', low: '偏低', high: '偏高', in_range: '目标范围' }[advice.value?.risk || ''] || '已评估'))
const riskClass = computed(() => advice.value?.risk === 'critical_low' || advice.value?.risk === 'low' ? 'bg-[#fff1f0] text-[#ff3b30]' : advice.value?.risk === 'high' ? 'bg-[#fff4df] text-[#b86f00]' : 'bg-[#e3f8ea] text-[#248a3d]')
const tone = computed(() => advice.value?.risk === 'critical_low' || advice.value?.risk === 'low' ? 'border-l-4 border-[#ff3b30]' : advice.value?.risk === 'high' ? 'border-l-4 border-[#ff9f0a]' : 'border-l-4 border-[#34c759]')
async function loadLatest() { loadingLatest.value = true; error.value = ''; try { const result = await apiClient.glucose(); const latest = result.items?.[0]; if (!latest) { error.value = '还没有血糖记录，请先添加一条记录后再生成建议'; advice.value = null; return } value.value = Number(latest.value); period.value = String(latest.period || 'post_meal'); recordId.value = String(latest.id || ''); await generate() } catch (e) { error.value = e instanceof Error ? e.message : '最近血糖加载失败，请稍后重试' } finally { loadingLatest.value = false } }
async function generate() { if (validationError.value) return; const id = ++requestId; loadingAdvice.value = true; error.value = ''; try { const result = await apiClient.recommendations(Number(value.value), period.value) as Advice; if (id === requestId) advice.value = result } catch (e) { if (id === requestId) error.value = e instanceof Error ? e.message : '建议生成失败，请重试' } finally { if (id === requestId) loadingAdvice.value = false } }
function scheduleGenerate() { window.clearTimeout(debounceTimer); if (validationError.value) { advice.value = null; return } debounceTimer = window.setTimeout(() => { if (!loadingLatest.value) generate() }, 400) }
watch([value, period], scheduleGenerate)
onMounted(loadLatest)
</script>
