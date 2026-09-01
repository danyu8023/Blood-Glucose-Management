<template>
  <section class="page">
    <PageHeader :title="title" :back="true" />
    <form @submit.prevent="save">
      <div class="glass rounded-[26px] p-5 mb-4">
        <div class="w-14 h-14 rounded-2xl grid place-items-center text-2xl" :class="accent.bg"><i :class="['fa-solid', accent.icon, accent.text]" /></div>
        <h2 class="text-2xl font-bold mt-5">新增{{ title }}</h2>
        <p class="muted text-sm mt-2">填写本次记录，保存后会用于趋势和建议。</p>
      </div>

      <div class="glass rounded-2xl p-4 space-y-4">
        <template v-if="props.type === 'glucose'">
          <label class="block text-sm font-medium">血糖值（mmol/L）<input v-model.number="form.glucose" required min="1" max="40" step="0.1" type="number" class="w-full mt-2 p-3 rounded-xl border-0 bg-[#f7f7f9] text-xl" placeholder="例如 6.8" /></label>
          <label class="block text-sm font-medium">测量时段<select v-model="form.period" class="w-full mt-2 p-3 rounded-xl border-0 bg-[#f7f7f9]"><option value="空腹">空腹</option><option value="餐前">餐前</option><option value="餐后">餐后</option><option value="睡前">睡前</option></select></label>
          <label class="block text-sm font-medium">饮食备注<textarea v-model="form.note" rows="3" class="w-full mt-2 p-3 rounded-xl border-0 bg-[#f7f7f9]" placeholder="记录本次餐食或身体感受（可选）" /></label>
        </template>

        <template v-else-if="props.type === 'medication'">
          <label class="block text-sm font-medium">药物名称<input v-model="form.medicationName" required maxlength="120" class="w-full mt-2 p-3 rounded-xl border-0 bg-[#f7f7f9]" placeholder="例如：二甲双胍" /></label>
          <div class="grid grid-cols-2 gap-3"><label class="block text-sm font-medium">剂量<input v-model.number="form.dose" required min="0.01" type="number" step="0.01" class="w-full mt-2 p-3 rounded-xl border-0 bg-[#f7f7f9]" placeholder="500" /></label><label class="block text-sm font-medium">单位<select v-model="form.doseUnit" class="w-full mt-2 p-3 rounded-xl border-0 bg-[#f7f7f9]"><option value="mg">mg</option><option value="片">片</option><option value="U">U</option></select></label></div>
          <label class="block text-sm font-medium">服用时间<input v-model="form.takenAt" required type="datetime-local" class="w-full mt-2 p-3 rounded-xl border-0 bg-[#f7f7f9]" /></label>
          <label class="block text-sm font-medium">服用状态<select v-model="form.medicationStatus" class="w-full mt-2 p-3 rounded-xl border-0 bg-[#f7f7f9]"><option value="taken">已服用</option><option value="missed">漏服</option><option value="skipped">跳过</option></select></label>
          <label class="block text-sm font-medium">备注<textarea v-model="form.note" rows="2" class="w-full mt-2 p-3 rounded-xl border-0 bg-[#f7f7f9]" placeholder="可选" /></label>
        </template>

        <template v-else-if="props.type === 'meals'">
          <label class="block text-sm font-medium">餐次<select v-model="form.mealType" class="w-full mt-2 p-3 rounded-xl border-0 bg-[#f7f7f9]"><option value="breakfast">早餐</option><option value="lunch">午餐</option><option value="dinner">晚餐</option><option value="snack">加餐</option></select></label>
          <label class="block text-sm font-medium">进餐时间<input v-model="form.eatenAt" required type="datetime-local" class="w-full mt-2 p-3 rounded-xl border-0 bg-[#f7f7f9]" /></label>
          <label class="block text-sm font-medium">主要食物<input v-model="form.foodName" required class="w-full mt-2 p-3 rounded-xl border-0 bg-[#f7f7f9]" placeholder="例如：糙米饭、西兰花、鸡胸肉" /></label>
          <div class="grid grid-cols-2 gap-3"><label class="block text-sm font-medium">份量<input v-model.number="form.foodAmount" required min="0.01" type="number" step="0.1" class="w-full mt-2 p-3 rounded-xl border-0 bg-[#f7f7f9]" placeholder="100" /></label><label class="block text-sm font-medium">单位<select v-model="form.foodUnit" class="w-full mt-2 p-3 rounded-xl border-0 bg-[#f7f7f9]"><option value="g">克</option><option value="份">份</option><option value="ml">毫升</option></select></label></div>
          <label class="block text-sm font-medium">碳水估算（g）<input v-model.number="form.carbohydrateGrams" min="0" type="number" step="0.1" class="w-full mt-2 p-3 rounded-xl border-0 bg-[#f7f7f9]" placeholder="可选" /></label>
          <label class="block text-sm font-medium">备注<textarea v-model="form.note" rows="2" class="w-full mt-2 p-3 rounded-xl border-0 bg-[#f7f7f9]" placeholder="可选" /></label>
        </template>

        <template v-else>
          <label class="block text-sm font-medium">运动类型<input v-model="form.exerciseType" required maxlength="30" class="w-full mt-2 p-3 rounded-xl border-0 bg-[#f7f7f9]" placeholder="例如：快走" /></label>
          <div class="grid grid-cols-2 gap-3"><label class="block text-sm font-medium">开始时间<input v-model="form.startedAt" required type="datetime-local" class="w-full mt-2 p-3 rounded-xl border-0 bg-[#f7f7f9]" /></label><label class="block text-sm font-medium">时长（分钟）<input v-model.number="form.durationMinutes" required min="1" max="600" type="number" class="w-full mt-2 p-3 rounded-xl border-0 bg-[#f7f7f9]" placeholder="30" /></label></div>
          <label class="block text-sm font-medium">运动强度<select v-model="form.intensity" class="w-full mt-2 p-3 rounded-xl border-0 bg-[#f7f7f9]"><option value="light">轻度</option><option value="moderate">中度</option><option value="vigorous">高强度</option></select></label>
          <div class="grid grid-cols-2 gap-3"><label class="block text-sm font-medium">运动前血糖<input v-model.number="form.beforeGlucose" min="0" max="40" step="0.1" type="number" class="w-full mt-2 p-3 rounded-xl border-0 bg-[#f7f7f9]" placeholder="可选" /></label><label class="block text-sm font-medium">运动后血糖<input v-model.number="form.afterGlucose" min="0" max="40" step="0.1" type="number" class="w-full mt-2 p-3 rounded-xl border-0 bg-[#f7f7f9]" placeholder="可选" /></label></div>
          <label class="block text-sm font-medium">备注<textarea v-model="form.note" rows="2" class="w-full mt-2 p-3 rounded-xl border-0 bg-[#f7f7f9]" placeholder="可选" /></label>
        </template>
      </div>

      <p v-if="error" class="text-sm text-[#ff3b30] mt-3">{{ error }}</p>
      <button class="btn-primary mt-4" type="submit" :disabled="saving"><i class="fa-solid fa-check" />{{ saving ? '保存中…' : '保存记录' }}</button>
    </form>
  </section>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import PageHeader from '../components/PageHeader.vue'
import type { RootState } from '../main'
import { apiClient } from '../services/api'

const props = defineProps<{ type: string }>()
const router = useRouter(); const store = useStore<RootState>()
const config: Record<string, { title: string; accent: { bg: string; text: string; icon: string } }> = {
  glucose: { title: '血糖记录', accent: { bg: 'bg-[#eaf4ff]', text: 'text-[#0a84ff]', icon: 'fa-droplet' } }, medication: { title: '用药记录', accent: { bg: 'bg-[#efefff]', text: 'text-[#5856d6]', icon: 'fa-pills' } }, meals: { title: '饮食记录', accent: { bg: 'bg-[#fff4df]', text: 'text-[#ff9f0a]', icon: 'fa-utensils' } }, exercise: { title: '运动记录', accent: { bg: 'bg-[#e3f8ea]', text: 'text-[#34c759]', icon: 'fa-person-walking' } }
}
const current = computed(() => config[props.type] || config.glucose); const title = computed(() => current.value.title); const accent = computed(() => current.value.accent)
const localDateTime = () => { const d = new Date(Date.now() - new Date().getTimezoneOffset() * 60000); return d.toISOString().slice(0, 16) }
const form = reactive<Record<string, string | number>>({ glucose: 6.8, period: '餐后', note: '', medicationName: '', dose: 500, doseUnit: 'mg', takenAt: localDateTime(), medicationStatus: 'taken', mealType: 'lunch', eatenAt: localDateTime(), foodName: '', foodAmount: 100, foodUnit: 'g', carbohydrateGrams: 0, exerciseType: '快走', startedAt: localDateTime(), durationMinutes: 30, intensity: 'light', beforeGlucose: '', afterGlucose: '' })
const saving = ref(false); const error = ref('')
const iso = (value: string | number) => new Date(String(value)).toISOString()

async function save() {
  saving.value = true; error.value = ''
  try {
    if (props.type === 'glucose') {
      const periods: Record<string, string> = { 空腹: 'fasting', 餐前: 'pre_meal', 餐后: 'post_meal', 睡前: 'bedtime' }
      const item = await apiClient.createGlucose({ value: Number(form.glucose), unit: 'mmol/L', period: periods[String(form.period)] || 'post_meal', measuredAt: new Date().toISOString(), note: String(form.note) })
      store.commit('records/addGlucose', { id: String(item.id), value: Number(item.value), period: String(form.period), time: '刚刚', note: String(form.note) })
    } else if (props.type === 'medication') {
      await apiClient.createMedication({ medicationName: String(form.medicationName), dose: Number(form.dose), doseUnit: String(form.doseUnit), takenAt: iso(form.takenAt), status: String(form.medicationStatus), note: String(form.note) })
      store.commit('records/setMedicationDone', Math.min(2, store.state.records.medicationDone + 1))
    } else if (props.type === 'meals') {
      await apiClient.createMeal({ mealType: String(form.mealType), eatenAt: iso(form.eatenAt), foods: [{ name: String(form.foodName), amount: Number(form.foodAmount), unit: String(form.foodUnit) }], carbohydrateGrams: Number(form.carbohydrateGrams) || 0, note: String(form.note) })
      store.commit('records/setMealDone', Math.min(3, store.state.records.mealsDone + 1))
    } else {
      await apiClient.createExercise({ exerciseType: String(form.exerciseType), startedAt: iso(form.startedAt), durationMinutes: Number(form.durationMinutes), intensity: String(form.intensity), beforeGlucose: form.beforeGlucose === '' ? null : Number(form.beforeGlucose), afterGlucose: form.afterGlucose === '' ? null : Number(form.afterGlucose), note: String(form.note) })
      store.commit('records/setExerciseMinutes', store.state.records.exerciseMinutes + Number(form.durationMinutes))
    }
    store.commit('ui/showToast', '记录已保存')
    router.push({ glucose: '/glucose-records', medication: '/medication-records', meals: '/meal-records', exercise: '/exercise-records' }[props.type] || '/records')
  } catch (e) { error.value = e instanceof Error ? e.message : '保存失败，请稍后重试' } finally { saving.value = false }
}
</script>
