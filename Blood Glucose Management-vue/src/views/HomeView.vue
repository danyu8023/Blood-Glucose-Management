<template><section class="page"><PageHeader title="我的首页" :eyebrow="loggedIn ? `星期三，8 月 30 日` : '糖安血糖管理'"/><template v-if="loggedIn"><div class="welcome-row flex items-center justify-between mb-4"><div><p class="muted text-sm">早上好，{{ name }}</p><p class="text-xs muted mt-1">今天继续保持稳定节奏</p></div><RouterLink to="/profile" class="avatar avatar-sm" aria-label="打开个人资料">{{ name.slice(0, 1) }}</RouterLink></div><div class="glass dashboard-hero rounded-[26px] p-5 mb-4"><div class="flex items-center justify-between"><div><p class="muted text-sm">最近一次血糖</p><p class="text-5xl font-semibold mt-1">{{ latest?.value ?? '--' }} <span class="text-sm font-normal muted">mmol/L</span></p></div><span class="chip" :class="statusClass">{{ statusText }}</span></div><p class="text-xs muted mt-3">{{ latest?.period || '暂无记录' }} · {{ latest?.time || '等待同步' }} · 目标 4.4–7.8</p><RouterLink to="/records" class="btn-primary mt-4"><i class="fa-solid fa-plus"/>记录血糖</RouterLink></div><div class="stat-grid grid grid-cols-3 gap-2 mb-5"><div class="glass stat-card rounded-2xl p-3"><p class="text-xs muted">范围内</p><p class="text-xl font-semibold mt-1">{{ timeInRange }}<span class="text-xs font-normal">%</span></p><p class="text-[10px] text-[#34c759] mt-1">近 7 天</p></div><div class="glass stat-card rounded-2xl p-3"><p class="text-xs muted">连续记录</p><p class="text-xl font-semibold mt-1">{{ streakDays }}<span class="text-xs font-normal">天</span></p><p class="text-[10px] text-[#0a84ff] mt-1">保持得很好</p></div><div class="glass stat-card rounded-2xl p-3"><p class="text-xs muted">波动指数</p><p class="text-xl font-semibold mt-1">{{ variabilityIndex.toFixed(1) }}</p><p class="text-[10px] text-[#34c759] mt-1">低波动</p></div></div><h2 class="font-semibold text-lg mb-3">今日管理</h2><div class="quick-grid grid grid-cols-2 gap-3 mb-5"><RouterLink to="/records" class="glass quick-card rounded-2xl p-4 no-underline text-[#1c1c1e]"><div class="flex justify-between"><span class="text-sm muted">饮食打卡</span><i class="fa-solid fa-utensils text-[#ff9f0a]"/></div><p class="text-2xl font-semibold mt-3">{{ mealsDone }} <span class="text-sm font-normal muted">/ 3 餐</span></p><p class="text-xs muted mt-2">今日已完成</p></RouterLink><RouterLink to="/records" class="glass quick-card rounded-2xl p-4 no-underline text-[#1c1c1e]"><div class="flex justify-between"><span class="text-sm muted">用药打卡</span><i class="fa-solid fa-pills text-[#5856d6]"/></div><p class="text-2xl font-semibold mt-3">{{ medicationDone }} <span class="text-sm font-normal muted">/ 2 次</span></p><p class="text-xs muted mt-2">今日已完成</p></RouterLink></div><div v-if="alertText" class="glass rounded-2xl p-4 mb-4 flex items-center gap-3"><span class="w-9 h-9 rounded-full bg-[#fff4df] text-[#ff9f0a] grid place-items-center"><i class="fa-solid fa-triangle-exclamation text-xs"/></span><div class="flex-1"><p class="font-medium text-sm">需要留意</p><p class="text-xs muted mt-1">{{ alertText }}</p></div><RouterLink to="/advice" class="text-xs text-[#0a84ff]">查看</RouterLink></div><div class="flex justify-between mb-3"><h2 class="font-semibold text-lg">今日趋势</h2><RouterLink to="/trends" class="text-sm text-[#0a84ff]">详情 ›</RouterLink></div><div class="glass trend-card rounded-2xl p-4"><div class="flex items-end gap-2 h-28"><span v-for="(v,i) in bars" :key="i" class="flex-1 rounded-t-lg" :class="i===3?'bg-[#0a84ff]':'bg-[#bfe0ff]'" :style="{height:`${v}%`}"/></div></div></template><template v-else><div class="glass dashboard-empty rounded-[26px] p-5 mb-4"><div class="w-14 h-14 rounded-2xl bg-[#eaf4ff] text-[#0a84ff] grid place-items-center text-2xl"><i class="fa-solid fa-lock"/></div><h2 class="text-2xl font-bold mt-5">登录后查看你的健康首页</h2><p class="muted text-sm leading-relaxed mt-3">个人血糖、饮食、用药和趋势数据仅在登录后展示，保障你的健康隐私。</p><RouterLink to="/login" class="btn-primary mt-5">登录账号</RouterLink><RouterLink to="/register" class="btn-secondary mt-3">注册新账号</RouterLink></div><p class="section-label">公开健康内容</p><RouterLink to="/public/news" class="glass rounded-2xl p-4 mb-3 flex items-center gap-3 no-underline text-[#1c1c1e]"><span class="w-10 h-10 rounded-xl bg-[#fff4df] text-[#ff9f0a] grid place-items-center"><i class="fa-solid fa-newspaper"/></span><div><p class="font-medium text-sm">本周健康资讯</p><p class="text-xs muted mt-1">餐后步行 15 分钟，有助于平稳餐后血糖</p></div><i class="fa-solid fa-chevron-right text-xs text-[#c7c7cc] ml-auto"/></RouterLink><RouterLink to="/public/class" class="glass rounded-2xl p-4 mb-3 flex items-center gap-3 no-underline text-[#1c1c1e]"><span class="w-10 h-10 rounded-xl bg-[#e3f8ea] text-[#34c759] grid place-items-center"><i class="fa-solid fa-book-medical"/></span><div><p class="font-medium text-sm">控糖小课堂</p><p class="text-xs muted mt-1">一餐先吃蔬菜和蛋白质，再吃主食</p></div><i class="fa-solid fa-chevron-right text-xs text-[#c7c7cc] ml-auto"/></RouterLink><div class="glass rounded-2xl p-4 flex items-center gap-3 opacity-60"><i class="fa-solid fa-chart-line text-[#0a84ff]"/><div class="flex-1"><p class="font-medium text-sm">个人趋势分析</p><p class="text-xs muted mt-1">登录后解锁 7 天 / 30 天趋势</p></div><i class="fa-solid fa-lock text-xs muted"/></div></template></section></template>
<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useStore } from 'vuex'
import PageHeader from '../components/PageHeader.vue'
import type { RootState } from '../main'
import { apiClient } from '../services/api'

const store = useStore<RootState>()
const loggedIn = computed(() => store.getters['auth/isLoggedIn'])
const name = computed(() => store.state.auth.name || '张明')
const latest = computed(() => store.getters['records/latest'])
const mealsDone = computed(() => store.state.records.mealsDone)
const medicationDone = computed(() => store.state.records.medicationDone)
const bars = [44, 62, 53, 72, 49, 58, 42]
const timeInRange = ref(0); const streakDays = ref(0); const variabilityIndex = ref(0); const alertText = ref('')
onMounted(async () => {
  if (!loggedIn.value) return
  try {
    const dashboard = await apiClient.dashboard() as any
    let latestGlucose = dashboard.latestGlucose
    if (!latestGlucose) {
      const recent = await apiClient.glucose()
      latestGlucose = recent.items?.[0]
    }
    if (latestGlucose) store.commit('records/setGlucose', [{ value: Number(latestGlucose.value), period: ({ fasting: '空腹', pre_meal: '餐前', post_meal: '餐后', bedtime: '睡前' } as Record<string, string>)[latestGlucose.period] || latestGlucose.period, time: new Date(String(latestGlucose.measuredAt || Date.now())).toLocaleString('zh-CN', { month: 'numeric', day: 'numeric', hour: '2-digit', minute: '2-digit' }), note: String(latestGlucose.note || '') }])
    if (dashboard.mealCheckIn) store.commit('records/setMealDone', Number(dashboard.mealCheckIn.completed))
    if (dashboard.medicationCheckIn) store.commit('records/setMedicationDone', Number(dashboard.medicationCheckIn.completed))
    timeInRange.value = Number(dashboard.timeInRange || 0); streakDays.value = Number(dashboard.streakDays || 0); variabilityIndex.value = Number(dashboard.variabilityIndex || 0); alertText.value = Array.isArray(dashboard.alerts) && dashboard.alerts.length ? String(dashboard.alerts[0].message || dashboard.alerts[0].text || '') : ''
    if (!dashboard.latestGlucose || !dashboard.timeInRange) {
      const trends = await apiClient.trends('7d') as any
      timeInRange.value = Number(trends.timeInRange || timeInRange.value)
      variabilityIndex.value = Number(trends.variabilityIndex || variabilityIndex.value)
    }
  } catch { /* 保留本地演示数据，支持离线预览 */ }
})

// Keep the status thresholds in one place so the dashboard label and color stay consistent.
const statusText = computed(() => {
  const glucose = latest.value?.value
  if (glucose == null) return '暂无数据'
  return glucose < 4.4 ? '偏低' : glucose <= 7.8 ? '正常' : '偏高'
})
const statusClass = computed(() => {
  const glucose = latest.value?.value
  if (glucose == null) return 'bg-[#e5e5ea] text-[#6e6e73]'
  return glucose < 4.4
    ? 'bg-[#fff1f0] text-[#ff3b30]'
    : glucose <= 7.8
      ? 'bg-[#e3f8ea] text-[#248a3d]'
      : 'bg-[#fff4df] text-[#b86f00]'
})
</script>


