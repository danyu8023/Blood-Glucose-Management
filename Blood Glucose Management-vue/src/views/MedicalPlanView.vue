<template>
  <section class="page">
    <PageHeader title="医嘱与目标" eyebrow="健康管理" :back="true" />
    <div v-if="loading" class="glass rounded-2xl p-6 text-center muted text-sm"><i class="fa-solid fa-spinner fa-spin mr-2" />正在加载健康档案…</div>
    <template v-else>
      <div class="glass rounded-2xl p-5 mb-4">
        <div class="flex items-center gap-3"><span class="setting-icon bg-[#eaf4ff]"><i class="fa-solid fa-user-doctor text-[#0a84ff]" /></span><div><p class="font-semibold">当前医生</p><p class="text-sm muted mt-1">{{ form.doctorName || '尚未填写医生' }}</p></div></div>
        <label class="block text-sm font-medium mt-5">医生姓名<input v-model.trim="form.doctorName" maxlength="60" class="w-full mt-2 p-3 rounded-xl border-0 bg-[#f7f7f9]" placeholder="例如：李医生" /></label>
        <label class="block text-sm font-medium mt-3">就诊医院<input v-model.trim="form.doctorClinic" maxlength="120" class="w-full mt-2 p-3 rounded-xl border-0 bg-[#f7f7f9]" placeholder="例如：上海市第一人民医院" /></label>
      </div>

      <div id="target" class="glass rounded-2xl p-5 mb-4">
        <div class="flex items-center gap-3"><span class="setting-icon bg-[#e3f8ea]"><i class="fa-solid fa-bullseye text-[#34c759]" /></span><div><p class="font-semibold">目标血糖范围</p><p class="text-sm muted mt-1">用于首页状态、趋势达成率和异常建议</p></div></div>
        <div class="grid grid-cols-2 gap-3 mt-5"><label class="block text-sm font-medium">下限（mmol/L）<input v-model.number="form.targetMin" required min="1" max="39" step="0.1" type="number" class="w-full mt-2 p-3 rounded-xl border-0 bg-[#f7f7f9] text-lg" /></label><label class="block text-sm font-medium">上限（mmol/L）<input v-model.number="form.targetMax" required min="2" max="40" step="0.1" type="number" class="w-full mt-2 p-3 rounded-xl border-0 bg-[#f7f7f9] text-lg" /></label></div>
        <p class="text-xs muted mt-3"><i class="fa-solid fa-circle-info text-[#0a84ff] mr-1" />目标范围应根据医生建议设置，通常需要结合测量时段分别评估。</p>
      </div>

      <div class="glass rounded-2xl p-4 mb-4"><h2 class="font-semibold">当前管理说明</h2><p class="text-sm muted leading-relaxed mt-2">{{ adviceText }}</p></div>
      <p v-if="error" class="glass rounded-xl p-3 mb-3 text-sm text-[#ff3b30]">{{ error }}</p>
      <button class="btn-primary" type="button" :disabled="saving" @click="save"><i :class="['fa-solid', saving ? 'fa-spinner fa-spin' : 'fa-check']" />{{ saving ? '保存中…' : '保存医嘱与目标' }}</button>
    </template>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useStore } from 'vuex'
import PageHeader from '../components/PageHeader.vue'
import type { RootState } from '../main'
import { apiClient } from '../services/api'

const store = useStore<RootState>()
const loading = ref(true); const saving = ref(false); const error = ref('')
const form = reactive({ doctorName: '', doctorClinic: '', targetMin: 4.4, targetMax: 7.8 })
const adviceText = computed(() => form.doctorName ? `${form.doctorName}${form.doctorClinic ? ` · ${form.doctorClinic}` : ''}。如需调整药物或目标范围，请先与医生确认。` : '请补充医生信息，并在复诊时与医生确认目标范围。')
onMounted(async () => { try { const data = await apiClient.me() as any; form.doctorName = String(data.doctor?.name || ''); form.doctorClinic = String(data.doctor?.clinic || ''); form.targetMin = Number(data.targetRange?.min ?? 4.4); form.targetMax = Number(data.targetRange?.max ?? 7.8) } catch (e) { error.value = e instanceof Error ? e.message : '健康档案加载失败' } finally { loading.value = false } })
async function save() { error.value = ''; if (!Number.isFinite(form.targetMin) || !Number.isFinite(form.targetMax) || form.targetMin >= form.targetMax) { error.value = '目标下限必须小于上限'; return } saving.value = true; try { await apiClient.updateMe({ doctor: { name: form.doctorName, clinic: form.doctorClinic }, targetRange: { min: form.targetMin, max: form.targetMax, unit: 'mmol/L' } }); store.commit('ui/showToast', '医嘱与目标已保存'); } catch (e) { error.value = e instanceof Error ? e.message : '保存失败，请稍后重试' } finally { saving.value = false } }
</script>
