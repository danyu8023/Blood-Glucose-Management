const API_BASE = import.meta.env.VITE_API_BASE || 'http://127.0.0.1:8080/api/v1'

export interface ApiResult<T> { code: number; message: string; data: T }
type ListQuery = { date?: string; from?: string; to?: string }

function withQuery(path: string, query: ListQuery = {}) {
  const params = new URLSearchParams()
  if (query.date) params.set('date', query.date)
  if (query.from) params.set('from', query.from)
  if (query.to) params.set('to', query.to)
  const value = params.toString()
  return value ? `${path}?${value}&page=1&pageSize=50` : `${path}?page=1&pageSize=50`
}

const publicAuthPaths = ['/sessions', '/users', '/sessions/refresh']
function isSessionExpiredResponse(path: string, response: Response, payload: Partial<ApiResult<unknown>> & { error?: { message?: string } }) {
  const hadToken = Boolean(localStorage.getItem('tangan_access_token'))
  if (!hadToken || publicAuthPaths.some(publicPath => path === publicPath)) return false
  const message = `${payload.message || ''} ${payload.error?.message || ''}`.toLowerCase()
  return response.status === 401 || (typeof payload.code === 'number' && payload.code >= 40100 && payload.code < 40200) || /token|jwt|session|登录过期|未授权/.test(message)
}

function clearExpiredSession() {
  localStorage.removeItem('tangan_vue_auth')
  localStorage.removeItem('tangan_access_token')
  localStorage.removeItem('tangan_refresh_token')
  window.dispatchEvent(new CustomEvent('tangan:auth-expired'))
  if (!window.location.hash.endsWith('/login')) window.location.hash = '#/login'
}

export async function api<T>(path: string, options: RequestInit = {}): Promise<T> {
  const token = localStorage.getItem('tangan_access_token')
  const headers = new Headers(options.headers)
  headers.set('Content-Type', 'application/json')
  if (token) headers.set('Authorization', `Bearer ${token}`)
  let response: Response
  try {
    response = await fetch(`${API_BASE}${path}`, { ...options, headers })
  } catch {
    throw new Error('无法连接服务器，请确认后端已启动（http://127.0.0.1:8080）')
  }
  if (response.status === 204) return undefined as T
  const payload = await response.json().catch(() => ({})) as ApiResult<T> & { error?: { message?: string } }
  if (isSessionExpiredResponse(path, response, payload)) {
    clearExpiredSession()
    // The app is already redirecting to login; do not leak the backend token message into page-level error cards.
    throw new Error('')
  }
  if (!response.ok || payload.code && payload.code !== 0) {
    const detail = payload.data && typeof payload.data === 'object' ? Object.values(payload.data as Record<string, unknown>).filter(Boolean).join('；') : ''
    throw new Error(detail ? `${payload.message || '请求失败'}：${detail}` : (payload.message || payload.error?.message || `请求失败 (${response.status})`))
  }
  return payload.data
}

export const apiClient = {
  publicArticles: (category?: string) => api<{ items: Array<Record<string, unknown>> }>(`/public/articles${category ? `?category=${encodeURIComponent(category)}` : ''}`),
  publicArticle: (slug: string) => api<Record<string, unknown>>(`/public/articles/${encodeURIComponent(slug)}`),
  publicGuide: (slug: string) => api<Record<string, unknown>>(`/public/guides/${encodeURIComponent(slug)}`),
  login: (account: string, password: string) => api<{ accessToken: string; refreshToken: string; user: { name: string; account: string } }>('/sessions', { method: 'POST', body: JSON.stringify({ account, password }) }),
  register: (name: string, phone: string, password: string, consent = true) => api<{ accessToken: string; refreshToken: string; user: { name: string; account: string } }>('/users', { method: 'POST', body: JSON.stringify({ name, phone, password, consent, timezone: 'Asia/Shanghai' }) }),
  logout: () => api<void>('/sessions/current', { method: 'DELETE' }),
  me: () => api<Record<string, unknown>>('/me'),
  updateMe: (payload: Record<string, unknown>) => api<Record<string, unknown>>('/me', { method: 'PATCH', body: JSON.stringify(payload) }),
  dashboard: () => api<Record<string, unknown>>('/dashboard'),
  trends: (range = '7d') => api<Record<string, unknown>>(`/glucose-trends?range=${range}`),
  report: (period = '30d') => api<Record<string, unknown>>(`/reports/${period}`),
  recommendations: (glucoseValue: number, period: string) => api<Record<string, unknown>>('/recommendations', { method: 'POST', body: JSON.stringify({ glucoseValue, period }) }),
  glucose: (query: ListQuery = {}) => api<{ items: Array<Record<string, unknown>> }>(withQuery('/glucose-records', query)),
  createGlucose: (payload: Record<string, unknown>) => api<Record<string, unknown>>('/glucose-records', { method: 'POST', body: JSON.stringify(payload) }),
  meals: (query: ListQuery = {}) => api<{ items: Array<Record<string, unknown>> }>(withQuery('/meal-records', query)),
  createMeal: (payload: Record<string, unknown>) => api<Record<string, unknown>>('/meal-records', { method: 'POST', body: JSON.stringify(payload) }),
  medications: (query: ListQuery = {}) => api<{ items: Array<Record<string, unknown>> }>(withQuery('/medication-records', query)),
  createMedication: (payload: Record<string, unknown>) => api<Record<string, unknown>>('/medication-records', { method: 'POST', body: JSON.stringify(payload) }),
  exercises: (query: ListQuery = {}) => api<{ items: Array<Record<string, unknown>> }>(withQuery('/exercise-records', query)),
  createExercise: (payload: Record<string, unknown>) => api<Record<string, unknown>>('/exercise-records', { method: 'POST', body: JSON.stringify(payload) }),
  settings: () => api<Record<string, boolean>>('/me/settings'),
  updateSettings: (payload: Record<string, boolean>) => api<Record<string, boolean>>('/me/settings', { method: 'PATCH', body: JSON.stringify(payload) }),
  family: () => api<Array<Record<string, unknown>>>('/family-connections'),
  createFamily: (payload: Record<string, unknown>) => api<Record<string, unknown>>('/family-connections', { method: 'POST', body: JSON.stringify(payload) }),
  deleteFamily: (id: string) => api<void>(`/family-connections/${id}`, { method: 'DELETE' })
}
