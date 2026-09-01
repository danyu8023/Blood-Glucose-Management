<template>
  <section class="page">
    <PageHeader title="设置" :back="true" />
    <div v-if="loading" class="glass rounded-2xl p-5 mb-5 text-center muted text-sm"><i class="fa-solid fa-spinner fa-spin mr-2" />正在加载设置…</div>
    <div v-else>
      <p class="section-label">提醒与通知</p>
      <div class="glass rounded-2xl px-4 mb-5">
        <button v-for="item in notificationSettings" :key="item.key" class="setting-row" type="button" :aria-label="`${item.label}，${item.value ? '已开启' : '已关闭'}`" @click="toggle(item)">
          <span class="setting-icon" :class="item.iconBg"><i :class="['fa-solid', item.icon, item.color]" /></span>
          <span class="flex-1 min-w-0 text-left"><strong class="block text-sm font-medium">{{ item.label }}</strong><small class="block muted mt-1">{{ item.description }}</small></span>
          <span class="setting-status mr-2" :class="item.value ? 'text-[#34c759]' : 'muted'">{{ item.value ? '开启' : '关闭' }}</span>
          <span class="settings-switch" :class="{ on: item.value }" role="switch" :aria-checked="item.value"><span /></span>
        </button>
      </div>

      <p class="section-label">数据与隐私</p>
      <div class="glass rounded-2xl px-4 mb-5">
        <button v-for="item in privacySettings" :key="item.key" class="setting-row" type="button" :aria-label="`${item.label}，${item.value ? '已开启' : '已关闭'}`" @click="toggle(item)">
          <span class="setting-icon" :class="item.iconBg"><i :class="['fa-solid', item.icon, item.color]" /></span>
          <span class="flex-1 min-w-0 text-left"><strong class="block text-sm font-medium">{{ item.label }}</strong><small class="block muted mt-1">{{ item.description }}</small></span>
          <span class="setting-status mr-2" :class="item.value ? 'text-[#34c759]' : 'muted'">{{ item.value ? '开启' : '关闭' }}</span>
          <span class="settings-switch" :class="{ on: item.value }" role="switch" :aria-checked="item.value"><span /></span>
        </button>
      </div>

      <div class="flex items-center justify-between mb-3"><p class="text-xs muted">{{ dirty ? '有未保存的更改' : '设置已同步' }}</p><button v-if="dirty" class="text-xs text-[#0a84ff]" type="button" @click="resetDraft">恢复上次保存</button></div>
      <p v-if="error" class="glass rounded-xl p-3 mb-3 text-sm text-[#ff3b30]">{{ error }}</p>
      <button class="btn-primary" type="button" :disabled="saving || loading || !dirty" @click="save"><i :class="['fa-solid', saving ? 'fa-spinner fa-spin' : 'fa-check']" />{{ saving ? '保存中…' : dirty ? '保存设置' : '已保存' }}</button>
      <button class="btn-secondary mt-3" type="button" :disabled="saving || loading" @click="restoreDefaults"><i class="fa-solid fa-rotate-left mr-1" />恢复默认设置</button>
      <p class="text-center text-xs text-[#34c759] mt-3 min-h-4">{{ saved ? '设置已保存并同步' : '' }}</p>
      <RouterLink to="/login" class="block text-center text-sm text-[#ff3b30] mt-8" @click="logout">切换账号</RouterLink>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import PageHeader from '../components/PageHeader.vue'
import type { RootState } from '../main'
import { apiClient } from '../services/api'

type SettingItem = { key: string; label: string; description: string; icon: string; color: string; iconBg: string; value: boolean }
type SettingsSnapshot = Record<string, boolean>
const store = useStore<RootState>(); const router = useRouter(); const loading = ref(true); const saving = ref(false); const saved = ref(false); const error = ref(''); const lastSaved = ref<SettingsSnapshot | null>(null)
const notificationSettings = ref<SettingItem[]>([
  { key: 'glucoseReminder', label: '血糖测量提醒', description: '按计划提醒记录空腹与餐后血糖', icon: 'fa-bell', color: 'text-[#ff9f0a]', iconBg: 'bg-[#fff4df]', value: true },
  { key: 'medicationReminder', label: '用药提醒', description: '在处方时间提醒确认是否已服药', icon: 'fa-pills', color: 'text-[#5856d6]', iconBg: 'bg-[#efefff]', value: true },
  { key: 'familyAlert', label: '家属异常通知', description: '出现异常血糖时通知已关联家属', icon: 'fa-user-group', color: 'text-[#0a84ff]', iconBg: 'bg-[#eaf4ff]', value: true }
])
const privacySettings = ref<SettingItem[]>([
  { key: 'autoSync', label: '自动同步健康数据', description: '登录后自动同步记录、趋势与报告', icon: 'fa-cloud-arrow-up', color: 'text-[#0a84ff]', iconBg: 'bg-[#eaf4ff]', value: true },
  { key: 'faceIdUnlock', label: 'Face ID 解锁', description: '支持的设备可使用系统生物识别解锁', icon: 'fa-lock', color: 'text-[#34c759]', iconBg: 'bg-[#e3f8ea]', value: false }
])
const allItems = computed(() => [...notificationSettings.value, ...privacySettings.value])
const snapshot = (): SettingsSnapshot => Object.fromEntries(allItems.value.map(item => [item.key, item.value]))
const dirty = computed(() => JSON.stringify(snapshot()) !== JSON.stringify(lastSaved.value || {}))
function applySnapshot(values: SettingsSnapshot) { allItems.value.forEach(item => { if (typeof values[item.key] === 'boolean') item.value = values[item.key] }) }
onMounted(async () => { try { const values = await apiClient.settings(); applySnapshot(values); lastSaved.value = snapshot() } catch (e) { error.value = e instanceof Error ? e.message : '设置加载失败，请稍后重试' } finally { loading.value = false } })
function toggle(item: SettingItem) { if (saving.value || loading.value) return; item.value = !item.value; saved.value = false; error.value = '' }
function resetDraft() { if (lastSaved.value) applySnapshot(lastSaved.value); saved.value = false; error.value = '' }
function restoreDefaults() { applySnapshot({ glucoseReminder: true, medicationReminder: true, familyAlert: true, autoSync: true, faceIdUnlock: false }); saved.value = false; error.value = '' }
async function save() { saving.value = true; saved.value = false; error.value = ''; try { const values = await apiClient.updateSettings(snapshot()); applySnapshot(values); lastSaved.value = snapshot(); saved.value = true; store.commit('ui/showToast', '设置已保存并同步'); window.setTimeout(() => { saved.value = false }, 1800) } catch (e) { error.value = e instanceof Error ? e.message : '保存失败，请检查网络后重试'; store.commit('ui/showToast', error.value) } finally { saving.value = false } }
async function logout() { await store.dispatch('auth/logout'); router.push('/login') }
</script>
