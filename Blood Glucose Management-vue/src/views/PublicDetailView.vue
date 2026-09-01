<template><section class="page"><PageHeader :title="content.title" :eyebrow="content.eyebrow" :back="true"/><div class="glass rounded-2xl p-5 mt-3"><div class="w-14 h-14 rounded-2xl grid place-items-center text-2xl" :class="content.bg"><i :class="['fa-solid',content.icon,content.color]"/></div><h1 class="text-2xl font-bold mt-5">{{ content.title }}</h1><p class="muted text-sm leading-relaxed mt-3">{{ content.lead }}</p></div><article class="glass rounded-2xl p-5 mt-4 text-sm muted leading-7"><p v-for="paragraph in content.body" :key="paragraph" class="mb-3 last:mb-0">{{ paragraph }}</p></article><RouterLink v-if="content.cta" to="/register" class="btn-primary mt-5">{{ content.cta }}</RouterLink></section></template>
<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import PageHeader from '../components/PageHeader.vue'
import { apiClient } from '../services/api'

interface PublicContent {
  title: string
  eyebrow: string
  lead: string
  body: string[]
  icon: string
  color: string
  bg: string
  cta?: string
}

const route = useRoute()
const remote = ref<Partial<PublicContent> | null>(null)
const map: Record<string, PublicContent> = {
  news: { title: '餐后步行 15 分钟', eyebrow: '糖安健康资讯', lead: '轻松步行有助于平稳餐后血糖。', body: ['餐后 20–30 分钟开始，持续 15–20 分钟，以能正常交谈的强度为宜。', '若运动前血糖偏低、出现头晕或冷汗，应先暂停并复测。', '本文为公共健康科普，不替代医生针对个人情况的建议。'], icon: 'fa-newspaper', color: 'text-[#ff9f0a]', bg: 'bg-[#fff4df]' },
  class: { title: '一餐先吃蔬菜和蛋白质', eyebrow: '控糖小课堂', lead: '调整进餐顺序，帮助增加饱腹感并减缓餐后上升。', body: ['先吃非淀粉蔬菜，再吃鱼、蛋、豆制品等蛋白质，最后吃糙米、全麦面等主食。', '建议餐盘分为 1/2 蔬菜、1/4 蛋白质、1/4 主食。'], icon: 'fa-book-medical', color: 'text-[#34c759]', bg: 'bg-[#e3f8ea]' },
  about: { title: '糖安健康管理', eyebrow: '产品介绍', lead: '用简单、连续的记录，帮助你更了解血糖变化。', body: ['糖安支持记录血糖、饮食、用药和运动，并根据记录生成日常建议。', '登录后可同步数据、查看趋势、导出报告，并与家属共享异常提醒。'], icon: 'fa-book-open', color: 'text-[#0a84ff]', bg: 'bg-[#eaf4ff]', cta: '注册后开始记录' },
  'glucose-guide': { title: '血糖怎么测', eyebrow: '公开记录指南', lead: '固定时段记录，趋势才有可比性。', body: ['空腹：起床后、进食前测量。', '餐前：进餐前立即测量并标记餐次。', '餐后 2 小时：从第一口饭开始计时。', '睡前：睡前测量，观察夜间风险。'], icon: 'fa-droplet', color: 'text-[#0a84ff]', bg: 'bg-[#eaf4ff]' },
  'meals-guide': { title: '记录饮食有什么用', eyebrow: '公开记录指南', lead: '把血糖变化和具体食物对应起来。', body: ['建议记录餐次与时间、食物名称、主食份量、甜饮和零食。', '连续记录 3–7 天，比只记录某一餐更容易发现规律。'], icon: 'fa-utensils', color: 'text-[#ff9f0a]', bg: 'bg-[#fff4df]' },
  'medication-guide': { title: '用药记录要点', eyebrow: '公开记录指南', lead: '记录依从性，复诊时更容易核对。', body: ['记下药物名称、剂量、服用时间，以及是否漏服或出现不适。', '不要根据单次血糖读数自行加药、减药或停药。'], icon: 'fa-pills', color: 'text-[#5856d6]', bg: 'bg-[#efefff]' },
  'trend-guide': { title: '趋势指标怎么看', eyebrow: '公开健康知识', lead: '连续 7–30 天的变化更适合复盘管理效果。', body: ['范围内时间反映血糖处于目标范围的比例。', '平均值用于观察整体水平，但不能代替对高低血糖事件的关注。', '复诊时可携带趋势报告，与医生一起确认目标范围。'], icon: 'fa-chart-line', color: 'text-[#0a84ff]', bg: 'bg-[#eaf4ff]' },
  history: { title: '血糖记录示例', eyebrow: '公开记录指南', lead: '连续记录让每一次变化都有上下文。', body: ['建议同时记录测量时段、餐饮备注和当时感受，方便复盘影响因素。', '登录后，糖安会按日期、时段和异常状态筛选你的个人记录。', '公开示例仅用于演示，不包含任何真实个人健康数据。'], icon: 'fa-clock-rotate-left', color: 'text-[#0a84ff]', bg: 'bg-[#eaf4ff]', cta: '注册后开始记录' }
}

const content = computed(() => ({ ...(map[String(route.params.slug)] || map.about), ...(remote.value || {}) }))
const apiSlug: Record<string, { type: 'article' | 'guide'; slug: string }> = {
  news: { type: 'article', slug: 'post-meal-walk-15-minutes' },
  class: { type: 'article', slug: 'balanced-plate-order' },
  'glucose-guide': { type: 'guide', slug: 'glucose-guide' },
  'meals-guide': { type: 'guide', slug: 'meals-guide' },
  'medication-guide': { type: 'guide', slug: 'medication-guide' },
  'trend-guide': { type: 'guide', slug: 'trend-guide' }
}
async function loadRemote(slugValue: string) {
  remote.value = null
  const target = apiSlug[slugValue]
  if (!target) return
  try {
    const data = target.type === 'article' ? await apiClient.publicArticle(target.slug) : await apiClient.publicGuide(target.slug)
    if (target.type === 'guide' && Array.isArray(data.sections)) remote.value = { title: String(data.title || ''), lead: String(data.lead || ''), body: (data.sections as Array<{ heading?: string; body?: string }>).map(section => `${section.heading || ''}：${section.body || ''}`) }
    else remote.value = { title: String(data.title || ''), lead: String(data.lead || ''), body: [String(data.body || ''), String(data.disclaimer || '')].filter(Boolean) }
  } catch { /* keep the bundled public copy when the API is unavailable */ }
}
watch(() => String(route.params.slug), loadRemote, { immediate: true })
</script>


