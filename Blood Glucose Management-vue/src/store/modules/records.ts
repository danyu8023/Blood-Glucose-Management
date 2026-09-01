import type { Module } from 'vuex'
import type { RootState } from '../../main'

export interface GlucoseRecord { id?: string; value: number; period: string; time: string; note: string; measuredAt?: string; status?: string }
export interface RecordsState { glucose: GlucoseRecord[]; medicationDone: number; mealsDone: number; exerciseMinutes: number }

// Start empty and populate from the authenticated account. Health data must never be fabricated.
const seed: RecordsState = {
  glucose: [],
  medicationDone: 0, mealsDone: 0, exerciseMinutes: 0
}
const initial = seed

// The API is the source of truth. Do not persist health records in a global browser key.
function persist(_state: RecordsState) { /* state is refreshed from the authenticated API */ }

export const records: Module<RecordsState, RootState> = {
  namespaced: true,
  state: () => initial,
  getters: {
    latest: (state) => state.glucose[0],
    average: (state) => state.glucose.reduce((sum, item) => sum + item.value, 0) / state.glucose.length
  },
  mutations: {
    setGlucose(state, values: GlucoseRecord[]) { state.glucose = values; persist(state) },
    addGlucose(state, record: GlucoseRecord) { state.glucose.unshift(record); persist(state) },
    toggleMedication(state) { state.medicationDone = state.medicationDone ? 0 : 1; persist(state) },
    setMedicationDone(state, value: number) { state.medicationDone = value; persist(state) },
    setMealDone(state, value: number) { state.mealsDone = value; persist(state) },
    setExerciseMinutes(state, value: number) { state.exerciseMinutes = value; persist(state) }
  }
}
