<template>
  <section class="page">
    <PageHeader title="趋势" :eyebrow="loggedIn ? `近 ${range === '7d' ? '7' : '30'} 天` : '公开健康知识'" />

    <template v-if="loggedIn">
      <div class="tabs">
        <button :class="{ active: range === '7d' }" @click="changeRange('7d')">近 7 天</button>
        <button :class="{ active: range === '30d' }" @click="changeRange('30d')">近 30 天</button>
      </div>

      <div v-if="error" class="glass rounded-2xl p-4 mb-4 text-sm text-[#ff3b30]">{{ error }}</div>

      <div class="glass trend-panel rounded-[26px] p-4 mb-4">
        <div class="flex justify-between">
          <div>
            <p class="muted text-sm">每日平均血糖</p>
            <p class="text-3xl font-semibold mt-1">{{ average }} <span class="text-sm font-normal muted">mmol/L</span></p>
          </div>
          <span class="chip bg-[#e3f8ea] text-[#248a3d]">范围内 {{ timeInRange }}%</span>
        </div>

        <div class="chart-wrap">
          <svg class="trend-chart w-full h-44 mt-4" viewBox="0 0 350 150" preserveAspectRatio="none" role="img" aria-label="每日平均血糖折线图">
            <line x1="0" y1="30" x2="350" y2="30" stroke="#e5e5ea" />
            <line x1="0" y1="75" x2="350" y2="75" stroke="#e5e5ea" />
            <line x1="0" y1="120" x2="350" y2="120" stroke="#e5e5ea" />
            <polyline :points="chartPoints" fill="none" stroke="#1677ff" stroke-width="4" stroke-linecap="round" stroke-linejoin="round" />
            <circle v-for="point in chartDots" :key="point.x" :cx="point.x" :cy="point.y" r="4" fill="#fff" stroke="#1677ff" stroke-width="2" />
          </svg>
          <div v-if="series.length" class="chart-labels">
            <span v-for="item in series" :key="String(item.date)">{{ formatDate(item.date) }}</span>
          </div>
        </div>

        <p v-if="!series.length" class="empty-chart text-xs muted text-center">该周期暂无血糖记录</p>
        <div v-else class="daily-values">
          <div v-for="item in series" :key="`daily-${item.date}`">
            <span>{{ formatDate(item.date) }}</span>
            <strong>{{ Number(item.average || 0).toFixed(1) }} <small>mmol/L</small></strong>
          </div>
        </div>
      </div>

      <div class="grid grid-cols-3 gap-2 mb-5">
        <div v-for="item in periods" :key="item.label" class="glass rounded-2xl p-3">
          <p class="text-xs muted">{{ item.label }}</p>
          <p class="text-xl font-semibold mt-2">{{ item.value }}</p>
          <span class="text-[10px]" :class="item.ok ? 'text-[#34c759]' : 'text-[#ff9f0a]'">{{ item.ok ? '正常' : '接近上限' }}</span>
        </div>
      </div>

      <div class="glass rounded-2xl p-4">
        <h2 class="font-semibold mb-3">目标达成率</h2>
        <div class="h-3 bg-[#e5e5ea] rounded-full overflow-hidden"><div class="h-full bg-[#34c759] rounded-full" :style="{ width: `${timeInRange}%` }" /></div>
        <p class="text-xs muted mt-3">过去 {{ range === '7d' ? '7' : '30' }} 天共有 {{ recordCount }} 次记录，其中 {{ inRangeCount }} 次在目标范围内</p>
      </div>
    </template>

    <template v-else>
      <div class="glass rounded-[26px] p-5 mb-4">
        <div class="w-14 h-14 rounded-2xl bg-[#eaf4ff] text-[#0a84ff] grid place-items-center text-2xl"><i class="fa-solid fa-chart-line" /></div>
        <p class="text-xs muted mt-5">公开健康知识</p><h2 class="text-2xl font-bold mt-1">趋势怎么看？</h2>
        <p class="muted text-sm leading-relaxed mt-3">登录后查看个人趋势，现在可以先了解通用控糖指标。</p>
        <RouterLink to="/login" class="btn-primary mt-5">登录账号</RouterLink><RouterLink to="/register" class="btn-secondary mt-3">注册新账号</RouterLink>
      </div>
      <p class="section-label">指标说明</p>
      <RouterLink to="/public/trend-guide" class="glass rounded-2xl p-4 mb-3 flex items-center gap-3 no-underline text-[#1c1c1e]"><i class="fa-solid fa-chart-simple text-[#0a84ff]" /><span class="flex-1 text-sm">范围内时间 · 了解长期趋势</span><i class="fa-solid fa-chevron-right text-xs text-[#c7c7cc]" /></RouterLink>
      <RouterLink to="/public/glucose-guide" class="glass rounded-2xl p-4 flex items-center gap-3 no-underline text-[#1c1c1e]"><i class="fa-solid fa-calendar-check text-[#ff9f0a]" /><span class="flex-1 text-sm">固定时段记录建议</span><i class="fa-solid fa-chevron-right text-xs text-[#c7c7cc]" /></RouterLink>
    </template>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useStore } from 'vuex'
import PageHeader from '../components/PageHeader.vue'
import type { RootState } from '../main'
import { apiClient } from '../services/api'

const store = useStore<RootState>()
const loggedIn = computed(() => store.getters['auth/isLoggedIn'])
const range = ref<'7d' | '30d'>('7d')
const periods = ref([{ label: '空腹', value: '--', ok: true }, { label: '餐后', value: '--', ok: true }, { label: '睡前', value: '--', ok: true }])
const average = ref('--')
const timeInRange = ref(0)
const recordCount = ref(0)
const inRangeCount = computed(() => Math.round(recordCount.value * timeInRange.value / 100))
const series = ref<Array<{ date?: string; average?: number }>>([])
const error = ref('')
const formatDate = (date?: string) => { if (!date) return '--'; const parts = String(date).split('-'); return parts.length >= 3 ? `${Number(parts[1])}/${Number(parts[2])}` : String(date) }
const chartDots = computed(() => { if (!series.value.length) return []; const values = series.value.map(x => Number(x.average || 0)); const min = Math.min(...values, 3.9); const max = Math.max(...values, 10); const span = Math.max(max - min, 1); return values.map((value, index) => ({ x: series.value.length === 1 ? 175 : index * 350 / (series.value.length - 1), y: 130 - ((value - min) / span) * 105 })) })
const chartPoints = computed(() => chartDots.value.map(point => `${point.x},${point.y}`).join(' '))
const periodMap: Record<string, string> = { fasting: '空腹', pre_meal: '餐前', post_meal: '餐后', bedtime: '睡前' }

async function load() {
  if (!loggedIn.value) return
  error.value = ''
  try {
    const data = await apiClient.trends(range.value) as any
    average.value = data.recordCount ? Number(data.average || 0).toFixed(1) : '--'
    timeInRange.value = Number(data.timeInRange || 0)
    recordCount.value = Number(data.recordCount || 0)
    series.value = Array.isArray(data.series) ? data.series : []
    const averages = data.periodAverages as Record<string, number> || {}
    periods.value = ['fasting', 'post_meal', 'bedtime'].map(key => { const value = Number(averages[key] || 0); return { label: periodMap[key], value: value ? value.toFixed(1) : '--', ok: !value || value <= 7.8 } })
  } catch (e) { error.value = e instanceof Error ? e.message : '趋势加载失败' }
}

function changeRange(next: '7d' | '30d') { if (range.value !== next) { range.value = next; load() } }
onMounted(load)
</script>
