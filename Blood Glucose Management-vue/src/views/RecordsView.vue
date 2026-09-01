<template>
  <section class="page">
    <PageHeader title="记录中心" :eyebrow="loggedIn ? `今天 · ${displayDate}` : '公开记录指南'" />

    <template v-if="loggedIn">
      <div class="glass completion-card rounded-[26px] p-5 mb-4 flex items-center justify-between" :aria-busy="completionLoading">
        <div>
          <p class="muted text-sm">今日完成</p>
          <p class="text-3xl font-semibold mt-1">{{ completedCount }} <span class="text-base font-normal muted">/ 4 项</span></p>
          <p class="text-xs muted mt-2">血糖、用药、饮食、运动共 4 类记录<span v-if="completionLoading"> · 正在同步</span></p>
          <p v-if="completionError" class="text-xs text-[#ff3b30] mt-2">{{ completionError }}</p>
        </div>
        <div class="w-16 h-16 rounded-full grid place-items-center text-sm font-semibold text-[#0a84ff]" :style="{ background: `conic-gradient(#0a84ff ${completionPercent}%, #dbeafe 0)` }">
          <div class="w-12 h-12 rounded-full bg-[#f2f2f7] grid place-items-center">{{ completionPercent }}%</div>
        </div>
      </div>

      <p class="section-label">记录管理</p>
      <div class="glass record-actions rounded-2xl px-4 mb-4">
        <div v-for="item in actions" :key="item.addPath" class="action-row">
          <RouterLink :to="item.addPath" class="flex items-center gap-3 flex-1 min-w-0 no-underline text-[#1c1c1e]">
            <span class="w-11 h-11 rounded-xl grid place-items-center shrink-0" :class="item.bg"><i :class="['fa-solid', item.icon, item.color]" /></span>
            <span class="flex-1 min-w-0"><b class="block text-sm">新增{{ item.label }}</b><small class="block muted mt-1">{{ item.desc }}</small></span>
          </RouterLink>
          <RouterLink :to="item.listPath" class="text-xs text-[#0a84ff] shrink-0 px-2">查看</RouterLink>
        </div>
      </div>

      <div class="glass recent-card rounded-2xl p-4">
        <div class="flex items-start justify-between"><div><h2 class="font-semibold">最近血糖</h2><p class="text-xs muted mt-1">最近 {{ recentRecords.length }} 条记录 · 点击查看详情</p></div><RouterLink to="/glucose-records" class="text-xs text-[#0a84ff]">查看全部</RouterLink></div>
        <div class="grid grid-cols-3 gap-2 mt-4 mb-3">
          <div class="rounded-xl bg-[#f7f7f9] p-2.5"><p class="text-[10px] muted">平均值</p><p class="text-base font-semibold mt-1">{{ recentAverage }}<small class="text-[10px] font-normal muted"> mmol/L</small></p></div>
          <div class="rounded-xl bg-[#f7f7f9] p-2.5"><p class="text-[10px] muted">正常</p><p class="text-base font-semibold mt-1 text-[#34c759]">{{ normalCount }}<small class="text-[10px] font-normal muted"> 条</small></p></div>
          <div class="rounded-xl bg-[#f7f7f9] p-2.5"><p class="text-[10px] muted">需留意</p><p class="text-base font-semibold mt-1" :class="attentionCount ? 'text-[#ff9f0a]' : 'text-[#34c759]'">{{ attentionCount }}<small class="text-[10px] font-normal muted"> 条</small></p></div>
        </div>
        <div v-if="recentRecords.length" class="divide-y divide-[#e5e5ea]">
          <RouterLink v-for="record in recentRecords" :key="record.id || record.time" to="/glucose-records" class="flex items-center gap-3 py-3 first:pt-2 last:pb-1 no-underline text-[#1c1c1e]">
            <span class="w-9 h-9 rounded-full grid place-items-center shrink-0" :class="statusMeta(record.value, record.status).bg"><i class="fa-solid fa-droplet text-sm" :class="statusMeta(record.value, record.status).icon" /></span>
            <div class="flex-1 min-w-0"><div class="flex items-center gap-2"><p class="text-sm font-semibold">{{ Number(record.value).toFixed(1) }} mmol/L</p><span class="text-[10px] font-medium" :class="statusMeta(record.value, record.status).text">{{ statusMeta(record.value, record.status).label }}</span></div><p class="text-xs muted mt-1">{{ record.period }} · {{ record.time }}</p><p v-if="record.note" class="text-xs text-[#636366] mt-1 truncate">{{ record.note }}</p></div><i class="fa-solid fa-chevron-right text-xs text-[#c7c7cc]" />
          </RouterLink>
        </div>
        <div v-else class="py-5 text-center"><span class="w-10 h-10 mx-auto rounded-full bg-[#eaf4ff] text-[#0a84ff] grid place-items-center"><i class="fa-solid fa-droplet" /></span><p class="text-sm font-medium mt-3">还没有血糖记录</p><p class="text-xs muted mt-1">记录第一条数据后，这里会显示趋势摘要</p></div>
        <RouterLink to="/glucose" class="btn-secondary mt-3"><i class="fa-solid fa-plus mr-1" />新增血糖</RouterLink>
      </div>
    </template>

    <template v-else>
      <div class="glass rounded-[26px] p-5 mb-4">
        <div class="w-14 h-14 rounded-2xl bg-[#eaf4ff] text-[#0a84ff] grid place-items-center text-2xl"><i class="fa-solid fa-book-medical" /></div>
        <p class="text-xs muted mt-5">公开健康指南</p><h2 class="text-2xl font-bold mt-1">先了解，再开始记录</h2>
        <p class="muted text-sm leading-relaxed mt-3">登录后可保存个人数据。现在可以浏览记录方法和控糖建议。</p>
        <RouterLink to="/login" class="btn-primary mt-5">登录账号</RouterLink><RouterLink to="/register" class="btn-secondary mt-3">注册新账号</RouterLink>
      </div>
      <p class="section-label">公开记录指南</p>
      <RouterLink v-for="guide in guides" :key="guide.path" :to="guide.path" class="glass rounded-2xl p-4 mb-3 flex items-center gap-3 no-underline text-[#1c1c1e]">
        <i :class="['fa-solid', guide.icon, guide.color]" /><div class="flex-1"><p class="font-medium text-sm">{{ guide.title }}</p><p class="text-xs muted mt-1">{{ guide.desc }}</p></div><i class="fa-solid fa-chevron-right text-xs text-[#c7c7cc]" />
      </RouterLink>
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
const dateKey = (() => {
  const date = new Date()
  const pad = (value: number) => String(value).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
})()
const displayDate = new Date(`${dateKey}T00:00:00`).toLocaleDateString('zh-CN', { month: 'numeric', day: 'numeric' })
const latest = computed(() => store.getters['records/latest'])
const latestStatus = computed(() => latest.value.value < 4.4 ? '偏低' : latest.value.value <= 7.8 ? '正常' : '偏高')
const latestStatusClass = computed(() => latest.value.value < 4.4 ? 'text-[#ff3b30]' : latest.value.value <= 7.8 ? 'text-[#34c759]' : 'text-[#ff9f0a]')
const recentRecords = computed(() => store.state.records.glucose.slice(0, 3))
const recentAverage = computed(() => recentRecords.value.length ? (recentRecords.value.reduce((sum, record) => sum + Number(record.value), 0) / recentRecords.value.length).toFixed(1) : '--')
const normalCount = computed(() => recentRecords.value.filter(record => statusMeta(Number(record.value), record.status).label === '正常').length)
const attentionCount = computed(() => recentRecords.value.length - normalCount.value)
const completedCount = ref(0)
const completionLoading = ref(false)
const completionError = ref('')
const completionPercent = computed(() => completedCount.value * 25)
const statusMeta = (value: number, status?: string) => { const resolved = status || (value < 3.9 ? 'critical_low' : value < 4.4 ? 'low' : value <= 7.8 ? 'normal' : 'high'); return resolved === 'critical_low' ? { label: '紧急偏低', text: 'text-[#ff3b30]', bg: 'bg-[#fff1f0]', icon: 'text-[#ff3b30]' } : resolved === 'low' ? { label: '偏低', text: 'text-[#ff3b30]', bg: 'bg-[#fff1f0]', icon: 'text-[#ff3b30]' } : resolved === 'high' ? { label: '偏高', text: 'text-[#b86f00]', bg: 'bg-[#fff4df]', icon: 'text-[#ff9f0a]' } : { label: '正常', text: 'text-[#34c759]', bg: 'bg-[#e3f8ea]', icon: 'text-[#0a84ff]' } }

async function loadRecords() {
  if (!loggedIn.value) return
  completionLoading.value = true
  completionError.value = ''
  const results = await Promise.allSettled([
    apiClient.glucose(),
    apiClient.glucose({ from: dateKey, to: dateKey }),
    apiClient.meals({ date: dateKey }),
    apiClient.medications({ date: dateKey }),
    apiClient.exercises({ from: dateKey, to: dateKey })
  ])
  try {
    const [recentResult, glucoseToday, mealsToday, medicationsToday, exerciseToday] = results
    if (recentResult.status === 'fulfilled') {
      const result = recentResult.value
      const periodNames: Record<string, string> = { fasting: '空腹', pre_meal: '餐前', post_meal: '餐后', bedtime: '睡前' }
      store.commit('records/setGlucose', result.items.map(item => ({ id: String(item.id), value: Number(item.value), period: periodNames[String(item.period)] || String(item.period), measuredAt: String(item.measuredAt), time: new Date(String(item.measuredAt)).toLocaleString('zh-CN', { month: 'numeric', day: 'numeric', hour: '2-digit', minute: '2-digit' }), note: String(item.note || ''), status: String(item.status || '') })))
    }
    const hasItems = (result: PromiseSettledResult<{ items: Array<Record<string, unknown>> }>) => result.status === 'fulfilled' && result.value.items.length > 0
    completedCount.value = [glucoseToday, mealsToday, medicationsToday, exerciseToday].filter(hasItems).length
    if (results.some(result => result.status === 'rejected')) completionError.value = '部分记录暂时无法同步'
  } finally {
    completionLoading.value = false
  }
}

onMounted(async () => {
  try {
    await loadRecords()
  } catch {
    completionError.value = '记录同步失败，请稍后重试'
    completionLoading.value = false
  }
})

const actions = [
  { addPath: '/glucose', listPath: '/glucose-records', label: '血糖记录', desc: '空腹、餐前、餐后、睡前', icon: 'fa-droplet', color: 'text-[#0a84ff]', bg: 'bg-[#eaf4ff]' },
  { addPath: '/medication', listPath: '/medication-records', label: '用药记录', desc: '药物、剂量、时间与依从性', icon: 'fa-pills', color: 'text-[#5856d6]', bg: 'bg-[#efefff]' },
  { addPath: '/meals', listPath: '/meal-records', label: '饮食记录', desc: '餐次、食物、份量与碳水', icon: 'fa-utensils', color: 'text-[#ff9f0a]', bg: 'bg-[#fff4df]' },
  { addPath: '/exercise', listPath: '/exercise-records', label: '运动记录', desc: '类型、时长、强度与前后血糖', icon: 'fa-person-walking', color: 'text-[#34c759]', bg: 'bg-[#e3f8ea]' }
]
const guides = [
  { path: '/public/glucose-guide', title: '血糖怎么测', desc: '空腹、餐前、餐后 2 小时和睡前', icon: 'fa-droplet', color: 'text-[#0a84ff]' },
  { path: '/public/meals-guide', title: '记录饮食有什么用', desc: '食物和份量帮助识别血糖波动来源', icon: 'fa-utensils', color: 'text-[#ff9f0a]' },
  { path: '/public/medication-guide', title: '用药记录要点', desc: '记下药名、剂量和时间', icon: 'fa-pills', color: 'text-[#5856d6]' }
]
</script>
