# 糖安血糖管理 Vue 原型

这是基于 Vue 3 + TypeScript + Vite + Vuex 的高保真移动端原型，页面按 iOS 风格手机壳布局呈现。

## 模块划分

- **AppShell 模块**：`src/App.vue`、`src/styles.css`，负责手机壳、动态岛状态栏、Toast 和全局布局。
- **Navigation 模块**：`src/components/PageHeader.vue`、`src/components/BottomNav.vue`，负责页面标题、返回和四个主 Tab。
- **Auth 模块**：`src/store/modules/auth.ts`、`LoginView.vue`、`RegisterView.vue`，负责登录、注册、退出和会话持久化。
- **Records 模块**：`src/store/modules/records.ts`、`RecordDetailView.vue`、`RecordsView.vue`，负责血糖、饮食、用药和运动记录。
- **Insights 模块**：`HomeView.vue`、`TrendsView.vue`、`AdviceView.vue`，负责仪表盘、趋势统计和基于读数的日常建议。
- **Profile 模块**：`ProfileView.vue`、`SettingsView.vue`，负责个人资料、医嘱、提醒和隐私设置。
- **Public Content 模块**：`PublicDetailView.vue`，负责登录前可访问的资讯、课堂和记录指南。

## 路由与权限

`/home`、`/records`、`/trends`、`/profile` 是四个主 Tab。登录前显示公共内容，登录后显示个人数据。`/settings`、`/advice`、`/glucose`、`/medication`、`/meals`、`/exercise` 由路由守卫保护，未登录会跳转 `/login`。`/public/:slug` 始终公开。

## 本地运行

```bash
npm install
npm run dev
```

生产构建：`npm run build`。

RESTful API 契约见：[docs/RESTful-API.md](docs/RESTful-API.md)。
