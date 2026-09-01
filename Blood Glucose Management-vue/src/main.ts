import { createApp } from 'vue'
import { createStore } from 'vuex'
import { createRouter, createWebHashHistory } from 'vue-router'
import App from './App.vue'
import { auth, type AuthState } from './store/modules/auth'
import { records, type RecordsState } from './store/modules/records'
import { ui, type UiState } from './store/modules/ui'
import HomeView from './views/HomeView.vue'
import RecordsView from './views/RecordsView.vue'
import TrendsView from './views/TrendsView.vue'
import ProfileView from './views/ProfileView.vue'
import LoginView from './views/LoginView.vue'
import RegisterView from './views/RegisterView.vue'
import SettingsView from './views/SettingsView.vue'
import AdviceView from './views/AdviceView.vue'
import PublicDetailView from './views/PublicDetailView.vue'
import RecordDetailView from './views/RecordDetailView.vue'
import RecordListView from './views/RecordListView.vue'
import ReportView from './views/ReportView.vue'
import FamilyView from './views/FamilyView.vue'
import MedicalPlanView from './views/MedicalPlanView.vue'
import './styles.css'

export interface RootState { auth: AuthState; records: RecordsState; ui: UiState }

// Vuex modules keep authentication, health records, and transient UI feedback isolated.
const store = createStore<RootState>({ modules: { auth, records, ui } })
const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    { path: '/', redirect: '/home' },
    { path: '/home', component: HomeView },
    { path: '/records', component: RecordsView },
    { path: '/trends', component: TrendsView },
    { path: '/profile', component: ProfileView },
    { path: '/login', component: LoginView },
    { path: '/register', component: RegisterView },
    { path: '/settings', component: SettingsView, meta: { requiresAuth: true } },
    { path: '/advice', component: AdviceView, meta: { requiresAuth: true } },
    { path: '/report', component: ReportView, meta: { requiresAuth: true } },
    { path: '/family', component: FamilyView, meta: { requiresAuth: true } },
    { path: '/medical-plan', component: MedicalPlanView, meta: { requiresAuth: true } },
    { path: '/glucose', component: RecordDetailView, props: { type: 'glucose' }, meta: { requiresAuth: true } },
    { path: '/glucose-records', component: RecordListView, props: { type: 'glucose' }, meta: { requiresAuth: true } },
    { path: '/medication', component: RecordDetailView, props: { type: 'medication' }, meta: { requiresAuth: true } },
    { path: '/medication-records', component: RecordListView, props: { type: 'medication' }, meta: { requiresAuth: true } },
    { path: '/meals', component: RecordDetailView, props: { type: 'meals' }, meta: { requiresAuth: true } },
    { path: '/meal-records', component: RecordListView, props: { type: 'meals' }, meta: { requiresAuth: true } },
    { path: '/exercise', component: RecordDetailView, props: { type: 'exercise' }, meta: { requiresAuth: true } },
    { path: '/exercise-records', component: RecordListView, props: { type: 'exercise' }, meta: { requiresAuth: true } },
    { path: '/public/:slug', component: PublicDetailView }
  ]
})

// API requests notify the app when an access token expires; keep all pages consistent.
window.addEventListener('tangan:auth-expired', () => {
  store.commit('auth/logout')
  if (router.currentRoute.value.path !== '/login') router.push('/login')
})

// Protect personal routes while leaving public education pages available before sign-in.
router.beforeEach((to) => {
  if (to.meta.requiresAuth && !store.getters['auth/isLoggedIn']) return '/login'
  return true
})

createApp(App).use(store).use(router).mount('#app')
