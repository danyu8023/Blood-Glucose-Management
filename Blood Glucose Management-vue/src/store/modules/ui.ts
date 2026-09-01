import type { Module } from 'vuex'
import type { RootState } from '../../main'

export interface UiState { toast: string }
export const ui: Module<UiState, RootState> = {
  namespaced: true,
  state: () => ({ toast: '' }),
  // Toasts are intentionally transient and are cleared by App.vue after a short delay.
  mutations: { showToast(state, message: string) { state.toast = message }, clearToast(state) { state.toast = '' } }
}
