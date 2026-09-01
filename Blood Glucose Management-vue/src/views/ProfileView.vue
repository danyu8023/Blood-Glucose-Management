<template>
  <section class="page">
    <PageHeader title="我的" />
    <template v-if="loggedIn">
      <div class="glass rounded-[26px] p-5 mb-4 flex items-center gap-4"><img class="w-16 h-16 rounded-full object-cover" src="https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=160&q=80" alt="头像" /><div class="flex-1"><h2 class="text-xl font-semibold">{{ name }}</h2><p class="muted text-sm mt-1">{{ diabetesType }} · 已登录</p><p class="text-xs text-[#34c759] mt-2">数据已同步</p></div></div>
      <p class="section-label">健康管理</p>
      <RouterLink to="/medical-plan" class="glass rounded-2xl p-4 mb-4 block no-underline text-[#1c1c1e]"><div class="flex items-center gap-3"><span class="setting-icon bg-[#eaf4ff]"><i class="fa-solid fa-user-doctor text-[#0a84ff]" /></span><div class="flex-1"><p class="font-semibold">我的医嘱</p><p class="muted text-xs mt-2">{{ doctorText }}</p></div><i class="fa-solid fa-chevron-right text-xs muted" /></div></RouterLink>
      <div class="glass rounded-2xl px-4 mb-4"><RouterLink to="/medical-plan#target" class="action-row no-underline text-[#1c1c1e]"><i class="fa-solid fa-bullseye text-[#0a84ff]" /><span class="flex-1 text-sm">目标血糖范围</span><span class="muted text-sm">{{ targetRange }}</span><i class="fa-solid fa-chevron-right text-xs muted" /></RouterLink><RouterLink to="/report" class="action-row no-underline text-[#1c1c1e]"><i class="fa-solid fa-file-medical text-[#5856d6]" /><span class="flex-1 text-sm">健康报告</span><span class="muted text-sm">查看</span><i class="fa-solid fa-chevron-right text-xs muted" /></RouterLink><RouterLink to="/family" class="action-row no-underline text-[#1c1c1e]"><i class="fa-solid fa-user-group text-[#0a84ff]" /><span class="flex-1 text-sm">家属共享</span><span class="muted text-sm">管理</span><i class="fa-solid fa-chevron-right text-xs muted" /></RouterLink></div>
      <p class="section-label">账户与安全</p>
      <div class="glass rounded-2xl px-4"><RouterLink to="/settings" class="account-row"><i class="fa-solid fa-gear text-[#5856d6]" /><span class="flex-1 text-sm">设置</span><i class="fa-solid fa-chevron-right text-xs muted" /></RouterLink><button class="account-row w-full text-left text-[#ff3b30]" @click="logout"><i class="fa-solid fa-arrow-right-from-bracket" /><span class="flex-1 text-sm">退出登录</span><i class="fa-solid fa-chevron-right text-xs muted" /></button></div>
    </template>
    <template v-else>
      <div class="glass rounded-[26px] p-5 mb-4"><div class="w-14 h-14 rounded-2xl bg-[#eaf4ff] text-[#0a84ff] grid place-items-center text-2xl"><i class="fa-solid fa-user-lock" /></div><h2 class="text-xl font-semibold mt-5">登录糖安，开始管理</h2><p class="muted text-sm leading-relaxed mt-2">登录后同步健康记录，并管理家属共享和提醒设置。</p><RouterLink to="/login" class="btn-primary mt-5">登录账号</RouterLink><RouterLink to="/register" class="btn-secondary mt-3">注册新账号</RouterLink></div>
      <p class="section-label">登录前可使用</p><div class="glass rounded-2xl px-4"><RouterLink to="/public/about" class="action-row no-underline"><i class="fa-solid fa-book-open text-[#0a84ff]" /><span class="flex-1 text-sm">浏览健康管理介绍</span><i class="fa-solid fa-chevron-right text-xs muted" /></RouterLink><div class="action-row locked"><i class="fa-solid fa-chart-line muted" /><span class="flex-1 text-sm">查看个人趋势</span><span class="text-xs muted">登录后可用</span><i class="fa-solid fa-lock text-xs muted" /></div><div class="action-row locked"><i class="fa-solid fa-file-medical muted" /><span class="flex-1 text-sm">导出健康报告</span><span class="text-xs muted">登录后可用</span><i class="fa-solid fa-lock text-xs muted" /></div></div>
    </template>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import PageHeader from '../components/PageHeader.vue'
import type { RootState } from '../main'
import { apiClient } from '../services/api'

const store = useStore<RootState>(); const router = useRouter(); const loggedIn = computed(() => store.getters['auth/isLoggedIn']); const name = computed(() => store.state.auth.name || '张明'); const targetRange = ref('4.4–7.8 mmol/L'); const doctorText = ref('请完善医生信息与医嘱'); const diabetesType = ref('2 型糖尿病')
onMounted(async () => { if (!loggedIn.value) return; try { const data = await apiClient.me() as any; store.commit('auth/login', { user: { name: String(data.name), account: String(data.account) }, accessToken: store.state.auth.accessToken, refreshToken: store.state.auth.refreshToken }); targetRange.value = `${data.targetRange?.min ?? 4.4}–${data.targetRange?.max ?? 7.8} ${data.targetRange?.unit || 'mmol/L'}`; diabetesType.value = data.diabetesType === 'type1' ? '1 型糖尿病' : '2 型糖尿病'; if (data.doctor?.name) doctorText.value = `${data.doctor.name}${data.doctor.clinic ? ` · ${data.doctor.clinic}` : ''}` } catch { /* 保留本地档案展示 */ } })
async function logout() { await store.dispatch('auth/logout'); router.push('/login') }
</script>
