import type { Module } from 'vuex'
import type { RootState } from '../../main'
import { apiClient } from '../../services/api'

export interface AuthState { loggedIn: boolean; name: string; account: string; accessToken: string; refreshToken: string }

// Persist only the small session profile needed by the prototype; clinical data stays in its own module.
const saved = localStorage.getItem('tangan_vue_auth')
const legacyLoggedIn = localStorage.getItem('tangan_logged_in') === '1'
let parsed: AuthState | null = null
try { parsed = saved ? JSON.parse(saved) as AuthState : null } catch { parsed = null }
const savedAccessToken = localStorage.getItem('tangan_access_token') || ''
const savedRefreshToken = localStorage.getItem('tangan_refresh_token') || ''
const initial: AuthState = parsed ? { ...parsed, loggedIn: Boolean(parsed.loggedIn && savedAccessToken), accessToken: savedAccessToken, refreshToken: savedRefreshToken } : { loggedIn: Boolean(legacyLoggedIn && savedAccessToken), name: legacyLoggedIn ? (localStorage.getItem('tangan_name') || '张明') : '', account: '', accessToken: savedAccessToken, refreshToken: savedRefreshToken }

export const auth: Module<AuthState, RootState> = {
  namespaced: true,
  state: () => initial,
  getters: { isLoggedIn: (state) => state.loggedIn },
  actions: {
    async login({ commit }, payload: { account: string; password: string }) {
      const session = await apiClient.login(payload.account, payload.password)
      commit('login', session)
      return session
    },
    async register({ commit }, payload: { name: string; phone: string; password: string; consent?: boolean }) {
      const session = await apiClient.register(payload.name, payload.phone, payload.password, payload.consent !== false)
      commit('login', session)
      return session
    },
    async logout({ commit }) { try { await apiClient.logout() } finally { commit('logout') } }
  },
  mutations: {
    login(state, payload: { user: { name: string; account: string }; accessToken: string; refreshToken: string }) { state.loggedIn = true; state.name = payload.user.name; state.account = payload.user.account; state.accessToken = payload.accessToken; state.refreshToken = payload.refreshToken; localStorage.setItem('tangan_access_token', payload.accessToken); localStorage.setItem('tangan_refresh_token', payload.refreshToken); localStorage.setItem('tangan_vue_auth', JSON.stringify(state)) },
    logout(state) { state.loggedIn = false; state.name = ''; state.account = ''; state.accessToken = ''; state.refreshToken = ''; localStorage.removeItem('tangan_vue_auth'); localStorage.removeItem('tangan_access_token'); localStorage.removeItem('tangan_refresh_token') }
  }
}
