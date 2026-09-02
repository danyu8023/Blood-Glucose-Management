<template>
  <section class="page report-page">
    <PageHeader title="健康报告" eyebrow="个人数据总结" :back="true" />

    <div class="tabs">
      <button :disabled="loading" :class="{ active: period === '7d' }" @click="changePeriod('7d')">近 7 天</button>
      <button :disabled="loading" :class="{ active: period === '30d' }" @click="changePeriod('30d')">近 30 天</button>
    </div>

    <div v-if="error" class="glass report-error rounded-2xl p-4">
      <div class="flex items-center gap-3">
        <span class="report-icon report-icon-danger"><i class="fa-solid fa-triangle-exclamation" /></span>
        <div class="flex-1 min-w-0"><p class="font-semibold text-sm">报告生成失败</p><p class="text-xs muted mt-1">{{ error }}</p></div>
      </div>
      <button class="btn-secondary mt-3" :disabled="loading" @click="load">重新加载</button>
    </div>

    <div v-else-if="loading && !report" class="report-loading" aria-live="polite">
      <div class="glass rounded-2xl p-4 report-skeleton report-skeleton-wide" />
      <div class="grid grid-cols-2 gap-3 mt-3"><div class="glass rounded-2xl report-skeleton" /><div class="glass rounded-2xl report-skeleton" /></div>
      <p class="text-xs muted text-center mt-4">正在汇总本期记录…</p>
    </div>

    <div v-else-if="report" class="space-y-3" :class="{ 'report-refreshing': loading }" aria-live="polite">
      <div class="glass rounded-2xl p-4 report-period">
        <div class="flex items-center justify-between gap-3">
          <div><p class="muted text-xs">报告周期</p><p class="font-semibold mt-1">{{ report.from }} 至 {{ report.to }}</p></div>
          <span class="chip bg-[#eaf4ff] text-[#0a84ff]">{{ report.days }} 天</span>
        </div>
        <p class="text-xs muted mt-3">生成于 {{ formatGenerated(report.generatedAt) }}</p>
      </div>

      <div class="glass rounded-2xl p-4 report-glucose">
        <div class="flex items-start justify-between gap-3">
          <div>
            <p class="muted text-xs">平均血糖</p>
            <p class="report-value mt-1">{{ hasGlucose ? formatNumber(report.glucose.average) : '--' }}<small> mmol/L</small></p>
            <p class="text-xs muted mt-1">{{ report.glucose.recordCount }} 次记录 · 覆盖 {{ report.glucose.daysRecorded }} 天</p>
          </div>
          <div class="report-range text-center">
            <strong>{{ hasGlucose ? `${report.glucose.timeInRange}%` : '--' }}</strong>
            <span>目标范围内</span>
          </div>
        </div>

        <template v-if="hasGlucose">
          <div class="report-chart mt-4">
            <svg viewBox="0 0 350 130" preserveAspectRatio="none" role="img" aria-label="报告周期内每日平均血糖折线图">
              <rect x="0" :y="targetBand.y" width="350" :height="targetBand.height" fill="#e7f7ed" rx="5" />
              <line x1="0" y1="18" x2="350" y2="18" stroke="#e5eaf0" />
              <line x1="0" y1="65" x2="350" y2="65" stroke="#e5eaf0" />
              <line x1="0" y1="112" x2="350" y2="112" stroke="#e5eaf0" />
              <polyline v-if="chartPoints" :points="chartPoints" fill="none" stroke="#1677ff" stroke-width="4" stroke-linecap="round" stroke-linejoin="round" />
              <circle v-for="point in chartDots" :key="`${point.x}-${point.y}`" :cx="point.x" :cy="point.y" r="4" fill="#fff" stroke="#1677ff" stroke-width="2" />
            </svg>
            <div v-if="report.glucose.series.length" class="report-chart-labels"><span>{{ formatDate(report.glucose.series[0].date) }}</span><span>每日平均</span><span>{{ formatDate(report.glucose.series[report.glucose.series.length - 1]?.date) }}</span></div>
          </div>
          <div class="report-minmax mt-3">
            <span>最低 <b>{{ formatNumber(report.glucose.minimum) }}</b></span>
            <span>目标 {{ formatNumber(report.glucose.targetMin) }}–{{ formatNumber(report.glucose.targetMax) }}</span>
            <span>最高 <b>{{ formatNumber(report.glucose.maximum) }}</b></span>
          </div>
        </template>
        <div v-else class="report-empty mt-4"><i class="fa-solid fa-droplet" /><p>本期暂无血糖记录</p></div>
      </div>

      <div class="grid grid-cols-2 gap-3">
        <div class="glass rounded-2xl p-4 report-metric">
          <span class="report-icon report-icon-purple"><i class="fa-solid fa-pills" /></span>
          <p class="muted text-xs mt-3">用药依从性</p>
          <p class="report-value mt-1">{{ report.medication.hasData ? report.medication.adherence : '--' }}<small v-if="report.medication.hasData">%</small></p>
          <p class="text-xs muted mt-1">已服 {{ report.medication.taken }} / {{ report.medication.scheduled }} 次</p>
        </div>
        <div class="glass rounded-2xl p-4 report-metric">
          <span class="report-icon report-icon-orange"><i class="fa-solid fa-utensils" /></span>
          <p class="muted text-xs mt-3">饮食记录</p>
          <p class="report-value mt-1">{{ report.meals.completed }}<small> 次</small></p>
          <p class="text-xs muted mt-1">覆盖 {{ report.meals.daysRecorded }} 天</p>
        </div>
        <div class="glass rounded-2xl p-4 report-metric">
          <span class="report-icon report-icon-green"><i class="fa-solid fa-person-walking" /></span>
          <p class="muted text-xs mt-3">运动时长</p>
          <p class="report-value mt-1">{{ report.exercise.minutes }}<small> 分钟</small></p>
          <p class="text-xs muted mt-1">{{ report.exercise.sessions }} 次 · {{ report.exercise.days }} 天</p>
        </div>
        <div class="glass rounded-2xl p-4 report-metric">
          <span class="report-icon report-icon-blue"><i class="fa-solid fa-calendar-check" /></span>
          <p class="muted text-xs mt-3">记录覆盖</p>
          <p class="report-value mt-1">{{ report.glucose.daysRecorded }}<small> 天</small></p>
          <p class="text-xs muted mt-1">占周期 {{ glucoseCoverage }}%</p>
        </div>
      </div>

      <div class="glass rounded-2xl p-4">
        <div class="flex items-center justify-between"><h2 class="font-semibold">血糖分布</h2><span class="text-xs muted">共 {{ report.glucose.recordCount }} 次</span></div>
        <div class="report-distribution mt-4" :aria-label="distributionLabel">
          <span class="normal" :style="{ width: distributionWidth(report.glucose.normalCount) }" />
          <span class="high" :style="{ width: distributionWidth(report.glucose.highCount) }" />
          <span class="low" :style="{ width: distributionWidth(report.glucose.lowCount) }" />
        </div>
        <div class="report-legend mt-3">
          <span><i class="normal" />范围内 {{ report.glucose.normalCount }}</span>
          <span><i class="high" />偏高 {{ report.glucose.highCount }}</span>
          <span><i class="low" />偏低 {{ report.glucose.lowCount }}</span>
        </div>
      </div>

      <div v-if="report.medication.hasData || report.meals.completed || report.exercise.sessions" class="glass rounded-2xl p-4">
        <h2 class="font-semibold">行为记录明细</h2>
        <div v-if="report.medication.hasData" class="report-detail-row"><span>用药</span><b>已服 {{ report.medication.taken }} · 漏服 {{ report.medication.missed }} · 跳过 {{ report.medication.skipped }}</b></div>
        <div v-if="report.meals.completed" class="report-detail-row"><span>饮食</span><b>平均碳水 {{ formatNumber(report.meals.averageCarbohydrate) }} g / 次</b></div>
        <div v-if="report.exercise.sessions" class="report-detail-row"><span>运动</span><b>平均 {{ formatNumber(report.exercise.averageMinutes) }} 分钟 / 次</b></div>
      </div>

      <div class="glass rounded-2xl p-4 report-highlights">
        <h2 class="font-semibold">本期总结</h2>
        <div v-for="(item, index) in report.highlights" :key="item" class="report-highlight">
          <span>{{ index + 1 }}</span><p>{{ item }}</p>
        </div>
      </div>
      <p class="report-disclaimer">报告依据当前记录自动生成，仅用于日常管理参考，不能替代医生诊断。</p>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import PageHeader from '../components/PageHeader.vue'
import { apiClient, type HealthReport } from '../services/api'

const period = ref<'7d' | '30d'>('30d')
const report = ref<HealthReport | null>(null)
const error = ref('')
const loading = ref(false)
let requestId = 0

const hasGlucose = computed(() => Boolean(report.value?.glucose.recordCount))
const glucoseCoverage = computed(() => report.value ? Math.round(report.value.glucose.daysRecorded * 100 / Math.max(report.value.days, 1)) : 0)
const chartBounds = computed(() => {
  const glucose = report.value?.glucose
  const values = glucose?.series.map(item => Number(item.average)) || []
  const minimum = Math.min(...values, Number(glucose?.targetMin || 4.4)) - 0.8
  const maximum = Math.max(...values, Number(glucose?.targetMax || 7.8)) + 0.8
  return { minimum, maximum, span: Math.max(maximum - minimum, 1) }
})
const chartY = (value: number) => 112 - ((value - chartBounds.value.minimum) / chartBounds.value.span) * 94
const chartDots = computed(() => {
  const series = report.value?.glucose.series || []
  return series.map((item, index) => ({ x: series.length === 1 ? 175 : index * 350 / (series.length - 1), y: chartY(Number(item.average)) }))
})
const chartPoints = computed(() => chartDots.value.map(point => `${point.x},${point.y}`).join(' '))
const targetBand = computed(() => {
  const glucose = report.value?.glucose
  const top = chartY(Number(glucose?.targetMax || 7.8))
  const bottom = chartY(Number(glucose?.targetMin || 4.4))
  return { y: top, height: Math.max(bottom - top, 2) }
})
const distributionLabel = computed(() => report.value ? `范围内 ${report.value.glucose.normalCount} 次，偏高 ${report.value.glucose.highCount} 次，偏低 ${report.value.glucose.lowCount} 次` : '')

function formatNumber(value: number) { return Number(value || 0).toFixed(1) }
function formatDate(value?: unknown) { const text = value == null ? '' : String(value); if (!text) return '--'; const parts = text.split('-'); return parts.length >= 3 ? `${Number(parts[1])}/${Number(parts[2])}` : text }
function formatGenerated(value?: unknown) { const date = new Date(String(value || '')); return Number.isNaN(date.getTime()) ? '--' : date.toLocaleString('zh-CN', { month: 'numeric', day: 'numeric', hour: '2-digit', minute: '2-digit' }) }
function distributionWidth(count: number) { const total = report.value?.glucose.recordCount || 0; return total ? `${count * 100 / total}%` : '0%' }

async function load() {
  const currentRequest = ++requestId
  loading.value = true
  error.value = ''
  try {
    const data = await apiClient.report(period.value)
    if (currentRequest === requestId) report.value = data
  } catch (e) {
    if (currentRequest === requestId) error.value = e instanceof Error ? e.message : '报告加载失败'
  } finally {
    if (currentRequest === requestId) loading.value = false
  }
}

function changePeriod(next: '7d' | '30d') {
  if (period.value === next) return
  period.value = next
  load()
}

onMounted(load)
</script>

<style scoped>
.report-page { padding-bottom: 54px; }
.report-refreshing { opacity: .72; transition: opacity .18s ease; }
.report-error { border-color: rgba(230, 75, 67, .16); }
.report-period { border-color: rgba(22, 119, 255, .1); }
.report-glucose { border-color: rgba(22, 119, 255, .12); box-shadow: 0 16px 32px rgba(22,119,255,.08); }
.report-value { font-size: 27px; line-height: 1.1; font-weight: 760; color: var(--ink); letter-spacing: 0; }
.report-value small { font-size: 11px; font-weight: 500; color: var(--muted); }
.report-range { min-width: 82px; padding: 10px 8px; border-radius: 14px; background: var(--green-soft); color: var(--green); }
.report-range strong, .report-range span { display: block; }
.report-range strong { font-size: 20px; }
.report-range span { margin-top: 3px; font-size: 9px; }
.report-chart svg { display: block; width: 100%; height: 130px; overflow: visible; }
.report-chart-labels, .report-minmax { display: flex; align-items: center; justify-content: space-between; color: var(--muted); font-size: 10px; }
.report-chart-labels { margin-top: 4px; }
.report-chart-labels span:nth-child(2) { color: #99a3b1; }
.report-minmax { padding-top: 10px; border-top: 1px solid var(--line); }
.report-minmax b { color: var(--ink); }
.report-empty { min-height: 116px; display: grid; place-items: center; align-content: center; gap: 8px; border-radius: 14px; background: #f5f8fc; color: var(--muted); font-size: 12px; }
.report-empty i { color: var(--blue); font-size: 20px; }
.report-metric { min-height: 154px; }
.report-icon { width: 34px; height: 34px; border-radius: 11px; display: grid; place-items: center; font-size: 14px; }
.report-icon-blue { color: var(--blue); background: var(--blue-soft); }
.report-icon-green { color: var(--green); background: var(--green-soft); }
.report-icon-orange { color: var(--orange); background: var(--orange-soft); }
.report-icon-purple { color: var(--purple); background: var(--purple-soft); }
.report-icon-danger { color: #e64b43; background: #fff0ef; }
.report-distribution { height: 12px; display: flex; overflow: hidden; border-radius: 999px; background: #e7ebf1; }
.report-distribution span { min-width: 0; }
.report-distribution .normal, .report-legend i.normal { background: var(--green); }
.report-distribution .high, .report-legend i.high { background: var(--orange); }
.report-distribution .low, .report-legend i.low { background: #e64b43; }
.report-legend { display: flex; justify-content: space-between; gap: 8px; color: var(--muted); font-size: 10px; }
.report-legend span { white-space: nowrap; }
.report-legend i { width: 7px; height: 7px; display: inline-block; margin-right: 4px; border-radius: 50%; }
.report-detail-row { min-height: 43px; display: flex; align-items: center; justify-content: space-between; gap: 12px; border-bottom: 1px solid var(--line); font-size: 12px; }
.report-detail-row:last-child { border-bottom: 0; }
.report-detail-row span { color: var(--muted); }
.report-detail-row b { text-align: right; color: var(--ink); font-weight: 650; }
.report-highlight { display: grid; grid-template-columns: 24px 1fr; gap: 10px; align-items: start; margin-top: 13px; }
.report-highlight span { width: 24px; height: 24px; display: grid; place-items: center; border-radius: 8px; background: var(--blue-soft); color: var(--blue); font-size: 11px; font-weight: 750; }
.report-highlight p { margin: 2px 0 0; color: #5f6a78; font-size: 12px; line-height: 1.55; }
.report-disclaimer { margin: 16px 12px 0; color: #929dab; font-size: 10px; line-height: 1.5; text-align: center; }
.report-skeleton { min-height: 154px; background: linear-gradient(100deg, rgba(255,255,255,.75) 20%, rgba(235,240,247,.95) 40%, rgba(255,255,255,.75) 60%); background-size: 220% 100%; animation: report-shimmer 1.3s linear infinite; }
.report-skeleton-wide { min-height: 100px; }
@keyframes report-shimmer { to { background-position-x: -220%; } }
</style>
