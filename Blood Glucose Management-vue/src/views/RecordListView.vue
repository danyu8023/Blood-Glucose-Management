<template>
  <section class="page">
    <PageHeader :title="config.title" :eyebrow="`${config.subtitle} · 共 ${items.length} 条`" :back="true" />

    <div class="flex gap-3 mb-4">
      <RouterLink :to="config.addPath" class="btn-primary flex-1"><i class="fa-solid fa-plus" />新增{{ config.shortTitle }}</RouterLink>
      <button class="icon-btn" type="button" title="刷新记录" aria-label="刷新记录" @click="load"><i class="fa-solid fa-rotate-right" /></button>
    </div>

    <div v-if="error" class="glass rounded-2xl p-4 mb-4 border-l-4 border-[#ff3b30]">
      <p class="text-sm text-[#ff3b30]">{{ error }}</p>
      <button class="text-sm text-[#0a84ff] mt-2" type="button" @click="load">重新加载</button>
    </div>

    <div v-if="loading" class="glass rounded-2xl p-6 text-center muted text-sm"><i class="fa-solid fa-spinner fa-spin mr-2" />正在加载记录…</div>
    <div v-else-if="!items.length && !error" class="glass rounded-2xl p-7 text-center">
      <div class="w-14 h-14 mx-auto rounded-2xl grid place-items-center text-2xl" :class="config.accent.bg"><i :class="['fa-solid', config.accent.icon, config.accent.text]" /></div>
      <h2 class="font-semibold mt-4">还没有{{ config.shortTitle }}记录</h2>
      <p class="muted text-sm mt-2">保存第一条记录后，这里会显示你的真实数据。</p>
      <RouterLink :to="config.addPath" class="btn-primary mt-5">开始记录</RouterLink>
    </div>

    <div v-else class="space-y-3">
      <article v-for="item in items" :key="String(item.id)" class="glass rounded-2xl p-4">
        <div class="flex items-start gap-3">
          <span class="w-10 h-10 rounded-xl grid place-items-center shrink-0" :class="config.accent.bg"><i :class="['fa-solid', config.accent.icon, config.accent.text]" /></span>
          <div class="flex-1 min-w-0">
            <div class="flex justify-between items-start gap-2">
              <h2 class="font-semibold text-[15px] truncate">{{ config.primary(item) }}</h2>
              <span v-if="config.badge" class="chip shrink-0" :class="config.badge(item).class">{{ config.badge(item).text }}</span>
            </div>
            <p class="text-xs muted mt-1">{{ config.secondary(item) }}</p>
            <p v-if="config.detail(item)" class="text-sm mt-3 leading-relaxed">{{ config.detail(item) }}</p>
          </div>
        </div>
      </article>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import PageHeader from '../components/PageHeader.vue'
import { apiClient } from '../services/api'

type Item = Record<string, unknown>
type Badge = { text: string; class: string }
type ModuleConfig = {
  title: string; shortTitle: string; subtitle: string; addPath: string
  accent: { icon: string; text: string; bg: string }
  load: () => Promise<{ items: Item[] }>
  primary: (item: Item) => string
  secondary: (item: Item) => string
  detail: (item: Item) => string
  badge?: (item: Item) => Badge
}

const formatTime = (value: unknown) => value ? new Date(String(value)).toLocaleString('zh-CN', { month: 'numeric', day: 'numeric', hour: '2-digit', minute: '2-digit' }) : '时间未填写'
const periodNames: Record<string, string> = { fasting: '空腹', pre_meal: '餐前', post_meal: '餐后', bedtime: '睡前' }
const mealNames: Record<string, string> = { breakfast: '早餐', lunch: '午餐', dinner: '晚餐', snack: '加餐' }
const statusNames: Record<string, string> = { taken: '已服用', missed: '漏服', skipped: '跳过' }
const intensityNames: Record<string, string> = { light: '轻度', moderate: '中度', vigorous: '高强度' }

const configs: Record<string, ModuleConfig> = {
  glucose: {
    title: '血糖记录', shortTitle: '血糖', subtitle: '按时间倒序', addPath: '/glucose', accent: { icon: 'fa-droplet', text: 'text-[#0a84ff]', bg: 'bg-[#eaf4ff]' }, load: apiClient.glucose,
    primary: item => `${Number(item.value).toFixed(1)} mmol/L`, secondary: item => `${periodNames[String(item.period)] || String(item.period)} · ${formatTime(item.measuredAt)}`,
    detail: item => String(item.note || ''), badge: item => ({ text: item.status === 'low' || item.status === 'critical_low' ? '偏低' : item.status === 'high' ? '偏高' : '正常', class: item.status === 'low' || item.status === 'critical_low' ? 'bg-[#fff1f0] text-[#ff3b30]' : item.status === 'high' ? 'bg-[#fff4df] text-[#b86f00]' : 'bg-[#e3f8ea] text-[#248a3d]' })
  },
  medication: {
    title: '用药记录', shortTitle: '用药', subtitle: '按服用时间倒序', addPath: '/medication', accent: { icon: 'fa-pills', text: 'text-[#5856d6]', bg: 'bg-[#efefff]' }, load: apiClient.medications,
    primary: item => `${String(item.medicationName)} ${item.dose} ${String(item.doseUnit)}`, secondary: item => `${statusNames[String(item.status)] || String(item.status)} · ${formatTime(item.takenAt || item.scheduledAt)}`,
    detail: item => String(item.note || ''), badge: item => ({ text: statusNames[String(item.status)] || '待记录', class: item.status === 'taken' ? 'bg-[#e3f8ea] text-[#248a3d]' : 'bg-[#fff4df] text-[#b86f00]' })
  },
  meals: {
    title: '饮食记录', shortTitle: '饮食', subtitle: '按进餐时间倒序', addPath: '/meals', accent: { icon: 'fa-utensils', text: 'text-[#ff9f0a]', bg: 'bg-[#fff4df]' }, load: apiClient.meals,
    primary: item => mealNames[String(item.mealType)] || String(item.mealType), secondary: item => `${formatTime(item.eatenAt)}${item.carbohydrateGrams ? ` · 碳水 ${item.carbohydrateGrams} g` : ''}`,
    detail: item => { const foods = Array.isArray(item.foods) ? item.foods.map((food: any) => `${food.name} ${food.amount}${food.unit}`).join('、') : ''; return foods || String(item.note || '') }
  },
  exercise: {
    title: '运动记录', shortTitle: '运动', subtitle: '按开始时间倒序', addPath: '/exercise', accent: { icon: 'fa-person-walking', text: 'text-[#34c759]', bg: 'bg-[#e3f8ea]' }, load: apiClient.exercises,
    primary: item => `${String(item.exerciseType)} · ${item.durationMinutes} 分钟`, secondary: item => `${intensityNames[String(item.intensity)] || String(item.intensity)} · ${formatTime(item.startedAt)}`,
    detail: item => { const glucose = item.beforeGlucose && item.afterGlucose ? `运动前 ${item.beforeGlucose} · 运动后 ${item.afterGlucose} mmol/L` : ''; return glucose || String(item.note || '') }
  }
}

const props = defineProps<{ type: string }>()
const config = computed(() => configs[props.type] || configs.glucose)
const items = ref<Item[]>([])
const loading = ref(false)
const error = ref('')

async function load() {
  loading.value = true; error.value = ''
  try { items.value = (await config.value.load()).items || [] } catch (e) { error.value = e instanceof Error ? e.message : '记录加载失败，请稍后重试' } finally { loading.value = false }
}
onMounted(load)
</script>
