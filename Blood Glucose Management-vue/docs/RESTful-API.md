# 糖安血糖管理 RESTful API 文档

版本：`v1`  
适用前端：`Blood Glucose Management-vue`  
文档状态：原型阶段接口契约，可直接作为后端实现和联调依据。

## 1. 设计约定

### 1.1 基础信息

| 项目 | 约定 |
| --- | --- |
| Base URL | `https://api.tangan.example.com/api/v1` |
| 协议 | HTTPS only |
| 数据格式 | `application/json; charset=utf-8` |
| 时间格式 | ISO 8601，例如 `2026-08-30T07:42:00+08:00` |
| 时区 | 用户资料中的 `timezone`，默认 `Asia/Shanghai` |
| 血糖单位 | `mmol/L`，接口字段统一使用 `value` |
| 认证 | `Authorization: Bearer <accessToken>` |
| 幂等 | 所有创建接口建议传 `Idempotency-Key`，服务端保存 24 小时 |
| ID | UUID v4 字符串 |

所有个人数据接口只允许访问当前登录用户的数据。未登录用户只能访问 `/public/*` 内容和注册、登录接口。

### 1.2 统一成功响应

```json
{
  "data": {},
  "meta": {
    "requestId": "req_01J6Y8C0WJ9M",
    "timestamp": "2026-08-30T07:42:03+08:00"
  }
}
```

列表接口在 `meta` 中增加分页信息：

```json
{
  "data": [],
  "meta": {
    "requestId": "req_01J6Y8C0WJ9M",
    "page": 1,
    "pageSize": 20,
    "total": 48,
    "hasNext": true
  }
}
```

### 1.3 统一错误响应

```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "血糖值必须在 1.0 至 40.0 mmol/L 之间",
    "details": [
      { "field": "value", "reason": "out_of_range" }
    ]
  },
  "meta": { "requestId": "req_01J6Y8C0WJ9M" }
}
```

常用状态码：

| HTTP | code | 说明 |
| --- | --- | --- |
| 400 | `BAD_REQUEST` | JSON 格式或参数类型错误 |
| 401 | `UNAUTHENTICATED` | 缺少、过期或无效 Token |
| 403 | `FORBIDDEN` | 已登录但无权访问资源 |
| 404 | `NOT_FOUND` | 资源不存在或不属于当前用户 |
| 409 | `CONFLICT` | 幂等键重复、手机号已注册等冲突 |
| 422 | `VALIDATION_ERROR` | 业务字段校验失败 |
| 429 | `RATE_LIMITED` | 超过频率限制 |
| 500 | `INTERNAL_ERROR` | 服务端异常 |

### 1.4 资源与前端模块

| REST 资源 | 前端模块/页面 | 说明 |
| --- | --- | --- |
| `users`、`sessions` | Auth 模块、登录、注册 | 账户和会话 |
| `me`、`settings` | Profile 模块、我的、设置 | 个人资料、医嘱、提醒 |
| `dashboard` | Insights 模块、首页 | 最近读数、打卡、提醒 |
| `glucose-records` | Records 模块、血糖记录 | 血糖测量 |
| `meal-records` | Records 模块、饮食记录 | 餐次、食物、碳水 |
| `medication-records` | Records 模块、用药记录 | 药名、剂量、依从性 |
| `exercise-records` | Records 模块、运动记录 | 运动类型、时长、强度 |
| `glucose-trends` | Insights 模块、趋势 | 7 天/30 天统计 |
| `advice` | Insights 模块、智能建议 | 基于记录的日常建议 |
| `reports`、`family-connections` | Profile 模块 | 报告和家属共享 |
| `public-articles`、`public-guides` | Public Content 模块 | 登录前可访问的公开内容 |

## 2. 接口总览

### 2.1 账户与个人设置

| 方法 | 路径 | 登录 | 说明 |
| --- | --- | --- | --- |
| `POST` | `/users` | 否 | 注册并创建首个会话 |
| `POST` | `/sessions` | 否 | 登录创建会话 |
| `POST` | `/sessions/refresh` | 否 | 轮换 refresh token |
| `DELETE` | `/sessions/current` | 是 | 退出当前设备 |
| `GET` | `/me` | 是 | 获取当前用户资料 |
| `PATCH` | `/me` | 是 | 修改姓名、医嘱和目标范围 |
| `GET` | `/me/settings` | 是 | 获取提醒和隐私设置 |
| `PATCH` | `/me/settings` | 是 | 更新提醒和隐私设置 |

### 2.2 健康数据与洞察

| 方法 | 路径 | 登录 | 说明 |
| --- | --- | --- | --- |
| `GET` | `/dashboard` | 是 | 首页聚合数据 |
| `GET` | `/glucose-records` | 是 | 分页查询血糖记录 |
| `POST` | `/glucose-records` | 是 | 新增血糖记录 |
| `GET` | `/glucose-records/{id}` | 是 | 查看单条血糖记录 |
| `PATCH` | `/glucose-records/{id}` | 是 | 修改血糖记录 |
| `DELETE` | `/glucose-records/{id}` | 是 | 删除血糖记录 |
| `GET` | `/meal-records` | 是 | 查询饮食记录 |
| `POST` | `/meal-records` | 是 | 新增饮食记录 |
| `PATCH` | `/meal-records/{id}` | 是 | 修改饮食记录 |
| `DELETE` | `/meal-records/{id}` | 是 | 删除饮食记录 |
| `GET` | `/medication-records` | 是 | 查询用药记录 |
| `POST` | `/medication-records` | 是 | 新增用药记录 |
| `PATCH` | `/medication-records/{id}` | 是 | 修改用药记录 |
| `DELETE` | `/medication-records/{id}` | 是 | 删除用药记录 |
| `GET` | `/exercise-records` | 是 | 查询运动记录 |
| `POST` | `/exercise-records` | 是 | 新增运动记录 |
| `PATCH` | `/exercise-records/{id}` | 是 | 修改运动记录 |
| `DELETE` | `/exercise-records/{id}` | 是 | 删除运动记录 |
| `GET` | `/glucose-trends` | 是 | 获取 7 天或 30 天趋势 |
| `POST` | `/recommendations` | 是 | 根据读数生成建议 |
| `GET` | `/reports/{period}` | 是 | 获取健康报告 |
| `GET` | `/family-connections` | 是 | 查询家属共享关系 |
| `POST` | `/family-connections` | 是 | 创建家属共享邀请 |
| `DELETE` | `/family-connections/{id}` | 是 | 解除家属共享 |

### 2.3 公开内容

| 方法 | 路径 | 登录 | 说明 |
| --- | --- | --- | --- |
| `GET` | `/public/articles` | 否 | 资讯列表 |
| `GET` | `/public/articles/{slug}` | 否 | 资讯详情 |
| `GET` | `/public/guides/{slug}` | 否 | 血糖、饮食、用药、趋势指南 |

## 3. 认证与账户接口

### 3.1 注册用户

`POST /users`

业务逻辑：

1. 校验姓名、手机号、密码和用户协议同意状态。
2. 手机号必须唯一，密码使用 Argon2id 或 bcrypt 存储，禁止明文落库。
3. 创建用户、默认目标范围和默认通知设置。
4. 返回短期 `accessToken` 与长期 `refreshToken`，前端注册成功后进入首页。

请求：

```json
{
  "name": "张明",
  "phone": "13800000000",
  "password": "S3cure!123456",
  "consent": true,
  "timezone": "Asia/Shanghai"
}
```

响应 `201 Created`：

```json
{
  "data": {
    "user": {
      "id": "usr_2b7b1c7b-3e43-4b57-8f3a-1d6a71c8c041",
      "name": "张明",
      "account": "13800000000",
      "diabetesType": "type2",
      "targetRange": { "min": 4.4, "max": 7.8, "unit": "mmol/L" }
    },
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "rft_01J6Y8C0WJ9M",
    "expiresIn": 3600
  },
  "meta": { "requestId": "req_01J6Y8C0WJ9M" }
}
```

curl：

```bash
curl -X POST 'https://api.tangan.example.com/api/v1/users' \
  -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: register-13800000000-20260830' \
  -d '{
    "name":"张明",
    "phone":"13800000000",
    "password":"S3cure!123456",
    "consent":true,
    "timezone":"Asia/Shanghai"
  }'
```

### 3.2 创建登录会话

`POST /sessions`

请求：

```json
{ "account": "13800000000", "password": "S3cure!123456", "deviceName": "iPhone 16 Pro Max" }
```

响应 `201 Created`：

```json
{
  "data": {
    "sessionId": "ses_3f0e2a9c-0e08-4d69-a6d5-1a2e1f3a5f00",
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "rft_01J6Y8D2Q7V2",
    "expiresIn": 3600,
    "user": { "id": "usr_2b7b1c7b-3e43-4b57-8f3a-1d6a71c8c041", "name": "张明" }
  },
  "meta": { "requestId": "req_01J6Y8D2Q7V2" }
}
```

curl：

```bash
curl -X POST 'https://api.tangan.example.com/api/v1/sessions' \
  -H 'Content-Type: application/json' \
  -d '{"account":"13800000000","password":"S3cure!123456","deviceName":"iPhone 16 Pro Max"}'
```

### 3.3 退出当前会话

`DELETE /sessions/current`

服务端撤销当前 Token 的 `jti`，并使 refresh token 失效。响应 `204 No Content`。

```bash
curl -i -X DELETE 'https://api.tangan.example.com/api/v1/sessions/current' \
  -H 'Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...'
```

### 3.4 刷新访问令牌

`POST /sessions/refresh`

refresh token 采用一次性轮换策略：成功后旧 refresh token 立即失效，返回新的 access/refresh token；检测到重复使用时，服务端撤销该用户的全部会话并返回 `401 TOKEN_REUSE_DETECTED`。

请求：

```json
{ "refreshToken": "rft_01J6Y8D2Q7V2" }
```

响应 `200 OK`：

```json
{
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.new...",
    "refreshToken": "rft_01J6Y8E8K3Q1",
    "expiresIn": 3600
  }
}
```

```bash
curl -X POST 'https://api.tangan.example.com/api/v1/sessions/refresh' \
  -H 'Content-Type: application/json' \
  -d '{"refreshToken":"rft_01J6Y8D2Q7V2"}'
```

### 3.5 获取当前用户

`GET /me`

响应 `200 OK`：

```json
{
  "data": {
    "id": "usr_2b7b1c7b-3e43-4b57-8f3a-1d6a71c8c041",
    "name": "张明",
    "account": "13800000000",
    "diabetesType": "type2",
    "doctor": { "name": "李医生", "clinic": "上海市第一人民医院" },
    "targetRange": { "min": 4.4, "max": 7.8, "unit": "mmol/L" },
    "timezone": "Asia/Shanghai"
  }
}
```

```bash
curl 'https://api.tangan.example.com/api/v1/me' \
  -H 'Authorization: Bearer <ACCESS_TOKEN>'
```

### 3.6 修改当前用户资料

`PATCH /me`

只更新请求中出现的字段。目标范围变更必须记录审计日志，并影响后续状态计算和趋势统计。

请求：

```json
{
  "name": "张明",
  "diabetesType": "type2",
  "targetRange": { "min": 4.4, "max": 7.8, "unit": "mmol/L" },
  "doctor": { "name": "李医生", "clinic": "上海市第一人民医院" }
}
```

响应 `200 OK` 返回更新后的完整用户资源。

```bash
curl -X PATCH 'https://api.tangan.example.com/api/v1/me' \
  -H 'Authorization: Bearer <ACCESS_TOKEN>' \
  -H 'Content-Type: application/json' \
  -d '{"name":"张明","targetRange":{"min":4.4,"max":7.8,"unit":"mmol/L"}}'
```

### 3.7 获取与更新设置

`GET /me/settings`、`PATCH /me/settings`

设置字段对应前端“提醒与通知”和“数据与隐私”开关。

响应示例：

```json
{
  "data": {
    "glucoseReminder": true,
    "medicationReminder": true,
    "familyAlert": true,
    "autoSync": true,
    "faceIdUnlock": false
  }
}
```

更新请求：

```json
{ "glucoseReminder": false, "familyAlert": true }
```

curl：

```bash
curl 'https://api.tangan.example.com/api/v1/me/settings' \
  -H 'Authorization: Bearer <ACCESS_TOKEN>'

curl -X PATCH 'https://api.tangan.example.com/api/v1/me/settings' \
  -H 'Authorization: Bearer <ACCESS_TOKEN>' \
  -H 'Content-Type: application/json' \
  -d '{"glucoseReminder":false,"familyAlert":true}'
```

## 4. 首页与洞察接口

### 4.1 首页聚合数据

`GET /dashboard?date=2026-08-30`

服务端一次性返回首页所需数据，避免前端串行请求。`latestGlucose`、`mealCheckIn`、`medicationCheckIn` 和 `alerts` 均按用户时区计算。

响应 `200 OK`：

```json
{
  "data": {
    "date": "2026-08-30",
    "latestGlucose": {
      "id": "glu_7f7c2d6a-0b2e-46b4-a2b1-54fa74cd0b10",
      "value": 6.8,
      "unit": "mmol/L",
      "period": "post_meal",
      "measuredAt": "2026-08-30T07:42:00+08:00",
      "status": "normal"
    },
    "timeInRange": 86,
    "streakDays": 6,
    "variabilityIndex": 1.2,
    "mealCheckIn": { "completed": 2, "total": 3 },
    "medicationCheckIn": { "completed": 1, "total": 2 },
    "alerts": [{ "type": "near_upper_bound", "message": "昨天晚餐后 7.2 mmol/L，接近上限" }],
    "chart": { "labels": ["06:55", "07:42"], "values": [5.6, 6.8] }
  }
}
```

```bash
curl 'https://api.tangan.example.com/api/v1/dashboard?date=2026-08-30' \
  -H 'Authorization: Bearer <ACCESS_TOKEN>'
```

### 4.2 血糖趋势统计

`GET /glucose-trends?range=7d&to=2026-08-30`

`range` 只能为 `7d` 或 `30d`。服务端按用户目标范围计算 `timeInRange`，按 `period` 计算平均值，并返回折线图点位。没有足够数据时返回 `insufficient_data: true`，不伪造个人统计。

响应：

```json
{
  "data": {
    "range": "7d",
    "from": "2026-08-24",
    "to": "2026-08-30",
    "average": 6.4,
    "timeInRange": 86,
    "recordCount": 24,
    "periodAverages": {
      "fasting": 5.8,
      "pre_meal": 6.0,
      "post_meal": 7.1,
      "bedtime": 6.3
    },
    "series": [
      { "date": "2026-08-24", "average": 6.2, "min": 5.1, "max": 7.4 },
      { "date": "2026-08-30", "average": 6.4, "min": 5.6, "max": 7.2 }
    ],
    "insufficientData": false
  }
}
```

```bash
curl 'https://api.tangan.example.com/api/v1/glucose-trends?range=30d&to=2026-08-30' \
  -H 'Authorization: Bearer <ACCESS_TOKEN>'
```

### 4.3 智能建议

`POST /recommendations`

说明：前端页面路由仍为 `/advice`，后端使用名词资源 `/recommendations`，避免把动作名直接放进 API 路径。

请求：

```json
{
  "glucoseValue": 7.2,
  "period": "post_meal",
  "recordId": "glu_7f7c2d6a-0b2e-46b4-a2b1-54fa74cd0b10"
}
```

业务逻辑：

1. 优先使用 `recordId` 对应的读数，`glucoseValue` 仅用于未保存的即时预览。
2. 对照用户目标范围和低血糖阈值生成风险等级。
3. 结合最近 7 天饮食、用药和运动记录生成建议。
4. 返回的内容是日常管理提示，必须包含“不能替代医生诊断”的免责声明。
5. 低血糖或持续明显偏高时返回 `urgent: true`，前端需要高亮并建议联系医生；服务端不得自动改药量。

响应：

```json
{
  "data": {
    "risk": "in_range",
    "title": "当前处于目标范围",
    "summary": "保持规律饮食，餐后步行 15–20 分钟。",
    "diet": "优先蔬菜、优质蛋白和适量低 GI 主食，避免含糖饮料。",
    "medication": "按处方时间用药，不因单次读数自行加药或停药。",
  "actions": ["餐后步行 15 分钟", "2 小时后按计划复测"],
    "urgent": false,
    "disclaimer": "建议仅供日常管理参考，不能替代医生诊断，不要自行调整药量。"
  }
}
```

```bash
curl -X POST 'https://api.tangan.example.com/api/v1/recommendations' \
  -H 'Authorization: Bearer <ACCESS_TOKEN>' \
  -H 'Content-Type: application/json' \
  -d '{"glucoseValue":7.2,"period":"post_meal","recordId":"glu_7f7c2d6a-0b2e-46b4-a2b1-54fa74cd0b10"}'
```

## 5. 血糖记录接口

### 5.1 字段和状态规则

| 字段 | 类型 | 必填 | 规则 |
| --- | --- | --- | --- |
| `value` | number | 是 | `1.0 <= value <= 40.0`，最多 1 位小数 |
| `unit` | string | 否 | 只允许 `mmol/L`，默认 `mmol/L` |
| `period` | enum | 是 | `fasting`、`pre_meal`、`post_meal`、`bedtime` |
| `measuredAt` | datetime | 是 | 不得晚于当前时间 5 分钟以上，不能早于 2 年 |
| `note` | string | 否 | 最长 500 字，记录饮食或身体感受 |

状态由用户目标范围计算：低于 `min` 为 `low`，高于 `max` 为 `high`，其余为 `normal`。低于 `3.9 mmol/L` 额外标记 `critical_low`。

### 5.2 查询血糖记录

`GET /glucose-records?from=2026-08-24&to=2026-08-30&period=post_meal&status=all&page=1&pageSize=20`

响应：

```json
{
  "data": [{
    "id": "glu_7f7c2d6a-0b2e-46b4-a2b1-54fa74cd0b10",
    "value": 6.8,
    "unit": "mmol/L",
    "period": "post_meal",
    "measuredAt": "2026-08-30T07:42:00+08:00",
    "status": "normal",
    "note": "燕麦粥、鸡蛋、无糖豆浆",
    "createdAt": "2026-08-30T07:43:12+08:00"
  }],
  "meta": { "page": 1, "pageSize": 20, "total": 1, "hasNext": false }
}
```

```bash
curl 'https://api.tangan.example.com/api/v1/glucose-records?from=2026-08-24&to=2026-08-30&page=1&pageSize=20' \
  -H 'Authorization: Bearer <ACCESS_TOKEN>'
```

### 5.3 新增血糖记录

`POST /glucose-records`

请求：

```json
{
  "value": 6.8,
  "unit": "mmol/L",
  "period": "post_meal",
  "measuredAt": "2026-08-30T07:42:00+08:00",
  "note": "燕麦粥、鸡蛋、无糖豆浆"
}
```

响应 `201 Created`：返回完整记录，并立即计算 `status`。

```bash
curl -X POST 'https://api.tangan.example.com/api/v1/glucose-records' \
  -H 'Authorization: Bearer <ACCESS_TOKEN>' \
  -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: glucose-20260830-0742' \
  -d '{"value":6.8,"unit":"mmol/L","period":"post_meal","measuredAt":"2026-08-30T07:42:00+08:00","note":"燕麦粥、鸡蛋、无糖豆浆"}'
```

### 5.4 查看、修改、删除单条血糖记录

- `GET /glucose-records/{id}`：返回单条完整记录。
- `PATCH /glucose-records/{id}`：部分更新 `value`、`period`、`measuredAt`、`note`，重新计算状态并刷新趋势缓存。
- `DELETE /glucose-records/{id}`：软删除记录，成功返回 `204 No Content`；已用于报告的数据仍保留审计版本。

修改请求示例：

```json
{ "value": 7.1, "note": "补充备注：餐后步行 15 分钟" }
```

修改成功响应 `200 OK`：

```json
{
  "data": {
    "id": "glu_7f7c2d6a-0b2e-46b4-a2b1-54fa74cd0b10",
    "value": 7.1,
    "unit": "mmol/L",
    "period": "post_meal",
    "measuredAt": "2026-08-30T07:42:00+08:00",
    "status": "normal",
    "note": "补充备注：餐后步行 15 分钟"
  }
}
```

`GET /glucose-records/{id}` 使用相同的成员资源响应结构；删除成功只返回 `204 No Content`，响应体为空。

```bash
curl 'https://api.tangan.example.com/api/v1/glucose-records/glu_7f7c2d6a-0b2e-46b4-a2b1-54fa74cd0b10' \
  -H 'Authorization: Bearer <ACCESS_TOKEN>'

curl -X PATCH 'https://api.tangan.example.com/api/v1/glucose-records/glu_7f7c2d6a-0b2e-46b4-a2b1-54fa74cd0b10' \
  -H 'Authorization: Bearer <ACCESS_TOKEN>' \
  -H 'Content-Type: application/json' \
  -d '{"value":7.1,"note":"补充备注：餐后步行 15 分钟"}'

curl -X DELETE 'https://api.tangan.example.com/api/v1/glucose-records/glu_7f7c2d6a-0b2e-46b4-a2b1-54fa74cd0b10' \
  -H 'Authorization: Bearer <ACCESS_TOKEN>'
```

## 6. 饮食、用药、运动记录接口

三个资源遵循相同的集合/成员 REST 结构：`GET` 查询、`POST` 新增、`PATCH` 修改、`DELETE` 软删除。响应均返回完整资源；删除成功返回 `204 No Content`。所有创建接口都应带 `Idempotency-Key`。

通用接口输入/输出约定（以下示例使用饮食资源，其他两个资源仅替换资源名和字段）：

```http
GET /meal-records?date=2026-08-30&page=1&pageSize=20
```

```json
{
  "data": [{
    "id": "meal_1c0d2c51-645c-4d6f-8ab4-5f9e7ce4a211",
    "mealType": "breakfast",
    "eatenAt": "2026-08-30T07:10:00+08:00",
    "foods": [{ "name": "燕麦粥", "amount": 1, "unit": "碗" }],
    "carbohydrateGrams": 38
  }],
  "meta": { "page": 1, "pageSize": 20, "total": 1, "hasNext": false }
}
```

```http
PATCH /meal-records/meal_1c0d2c51-645c-4d6f-8ab4-5f9e7ce4a211
Content-Type: application/json

{ "carbohydrateGrams": 32 }
```

```json
{
  "data": {
    "id": "meal_1c0d2c51-645c-4d6f-8ab4-5f9e7ce4a211",
    "mealType": "breakfast",
    "eatenAt": "2026-08-30T07:10:00+08:00",
    "carbohydrateGrams": 32,
    "updatedAt": "2026-08-30T08:02:11+08:00"
  }
}
```

`DELETE /meal-records/{id}`、`DELETE /medication-records/{id}` 和 `DELETE /exercise-records/{id}` 成功均返回 `204 No Content`；不存在或不属于当前用户时返回 `404 NOT_FOUND`。

### 6.1 饮食记录

资源：`/meal-records`

请求字段：

```json
{
  "mealType": "breakfast",
  "eatenAt": "2026-08-30T07:10:00+08:00",
  "foods": [
    { "name": "燕麦粥", "amount": 1, "unit": "碗" },
    { "name": "鸡蛋", "amount": 1, "unit": "个" }
  ],
  "carbohydrateGrams": 38,
  "note": "无糖豆浆"
}
```

响应示例：

```json
{
  "data": {
    "id": "meal_1c0d2c51-645c-4d6f-8ab4-5f9e7ce4a211",
    "mealType": "breakfast",
    "eatenAt": "2026-08-30T07:10:00+08:00",
    "foods": [{ "name": "燕麦粥", "amount": 1, "unit": "碗" }, { "name": "鸡蛋", "amount": 1, "unit": "个" }],
    "carbohydrateGrams": 38,
    "linkedGlucoseRecordId": "glu_7f7c2d6a-0b2e-46b4-a2b1-54fa74cd0b10"
  }
}
```

业务逻辑：同一天同一 `mealType` 重复提交时返回 `409 DUPLICATE_MEAL`，除非使用 PATCH；保存后首页餐饮打卡数加一，并尝试关联前后 3 小时内的血糖记录。

```bash
curl -X POST 'https://api.tangan.example.com/api/v1/meal-records' \
  -H 'Authorization: Bearer <ACCESS_TOKEN>' -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: meal-breakfast-20260830' \
  -d '{"mealType":"breakfast","eatenAt":"2026-08-30T07:10:00+08:00","foods":[{"name":"燕麦粥","amount":1,"unit":"碗"},{"name":"鸡蛋","amount":1,"unit":"个"}],"carbohydrateGrams":38,"note":"无糖豆浆"}'

curl 'https://api.tangan.example.com/api/v1/meal-records?from=2026-08-30&to=2026-08-30' \
  -H 'Authorization: Bearer <ACCESS_TOKEN>'

curl -X PATCH 'https://api.tangan.example.com/api/v1/meal-records/meal_1c0d2c51-645c-4d6f-8ab4-5f9e7ce4a211' \
  -H 'Authorization: Bearer <ACCESS_TOKEN>' -H 'Content-Type: application/json' \
  -d '{"carbohydrateGrams":32}'

curl -X DELETE 'https://api.tangan.example.com/api/v1/meal-records/meal_1c0d2c51-645c-4d6f-8ab4-5f9e7ce4a211' \
  -H 'Authorization: Bearer <ACCESS_TOKEN>'
```

### 6.2 用药记录

资源：`/medication-records`

请求：

```json
{
  "medicationName": "二甲双胍缓释片",
  "dose": 500,
  "doseUnit": "mg",
  "takenAt": "2026-08-30T07:00:00+08:00",
  "scheduledAt": "2026-08-30T07:00:00+08:00",
  "status": "taken",
  "note": "早餐前"
}
```

响应：

```json
{
  "data": {
    "id": "med_5d1d3e8b-e490-4f4d-8b8d-2d19f0f7c900",
    "medicationName": "二甲双胍缓释片",
    "dose": 500,
    "doseUnit": "mg",
    "takenAt": "2026-08-30T07:00:00+08:00",
    "scheduledAt": "2026-08-30T07:00:00+08:00",
    "status": "taken",
    "adherence": "on_time"
  }
}
```

业务逻辑：`status` 可为 `taken`、`missed`、`skipped`；服务器根据 `takenAt - scheduledAt` 计算依从性。接口绝不根据血糖自动调整剂量，漏服或异常时只产生提醒。

```bash
curl -X POST 'https://api.tangan.example.com/api/v1/medication-records' \
  -H 'Authorization: Bearer <ACCESS_TOKEN>' -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: med-20260830-0700' \
  -d '{"medicationName":"二甲双胍缓释片","dose":500,"doseUnit":"mg","takenAt":"2026-08-30T07:00:00+08:00","scheduledAt":"2026-08-30T07:00:00+08:00","status":"taken","note":"早餐前"}'

curl 'https://api.tangan.example.com/api/v1/medication-records?date=2026-08-30' \
  -H 'Authorization: Bearer <ACCESS_TOKEN>'

curl -X PATCH 'https://api.tangan.example.com/api/v1/medication-records/med_5d1d3e8b-e490-4f4d-8b8d-2d19f0f7c900' \
  -H 'Authorization: Bearer <ACCESS_TOKEN>' -H 'Content-Type: application/json' \
  -d '{"status":"missed","note":"外出忘记服用"}'

curl -X DELETE 'https://api.tangan.example.com/api/v1/medication-records/med_5d1d3e8b-e490-4f4d-8b8d-2d19f0f7c900' \
  -H 'Authorization: Bearer <ACCESS_TOKEN>'
```

### 6.3 运动记录

资源：`/exercise-records`

请求和响应示例：

```json
{
  "exerciseType": "walking",
  "startedAt": "2026-08-30T18:30:00+08:00",
  "durationMinutes": 20,
  "intensity": "moderate",
  "beforeGlucose": 6.9,
  "afterGlucose": 6.3,
  "note": "晚餐后步行"
}
```

```json
{
  "data": {
    "id": "exr_3e0a3e0f-04f2-4d5c-aee1-9db2a4e9f901",
    "exerciseType": "walking",
    "startedAt": "2026-08-30T18:30:00+08:00",
    "durationMinutes": 20,
    "intensity": "moderate",
    "beforeGlucose": 6.9,
    "afterGlucose": 6.3,
    "note": "晚餐后步行"
  }
}
```

业务逻辑：`durationMinutes` 必须为 1–600；运动总时长用于首页统计。运动前血糖低于 `3.9` 或出现低血糖症状时，接口返回 `warnings`，但仍不替代医疗判断。

```bash
curl -X POST 'https://api.tangan.example.com/api/v1/exercise-records' \
  -H 'Authorization: Bearer <ACCESS_TOKEN>' -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: exercise-20260830-1830' \
  -d '{"exerciseType":"walking","startedAt":"2026-08-30T18:30:00+08:00","durationMinutes":20,"intensity":"moderate","beforeGlucose":6.9,"afterGlucose":6.3,"note":"晚餐后步行"}'

curl 'https://api.tangan.example.com/api/v1/exercise-records?from=2026-08-24&to=2026-08-30' \
  -H 'Authorization: Bearer <ACCESS_TOKEN>'

curl -X PATCH 'https://api.tangan.example.com/api/v1/exercise-records/exr_3e0a3e0f-04f2-4d5c-aee1-9db2a4e9f901' \
  -H 'Authorization: Bearer <ACCESS_TOKEN>' -H 'Content-Type: application/json' \
  -d '{"durationMinutes":25}'

curl -X DELETE 'https://api.tangan.example.com/api/v1/exercise-records/exr_3e0a3e0f-04f2-4d5c-aee1-9db2a4e9f901' \
  -H 'Authorization: Bearer <ACCESS_TOKEN>'
```

## 7. 报告与家属共享

### 7.1 获取健康报告

`GET /reports/{period}`

`period` 支持 `7d`、`30d`、`monthly`。服务端以当前用户时区聚合血糖、饮食、用药和运动数据，返回可供前端展示或导出的结构化结果。

```json
{
  "data": {
    "period": "monthly",
    "from": "2026-08-01",
    "to": "2026-08-30",
    "glucose": { "average": 6.4, "timeInRange": 86, "recordCount": 96, "highCount": 7, "lowCount": 1 },
    "medication": { "scheduled": 60, "taken": 57, "adherence": 95 },
    "meals": { "completed": 72, "total": 90 },
    "exercise": { "minutes": 420, "days": 18 },
    "highlights": ["餐后平均值较上月下降 0.3 mmol/L", "用药依从性达到 95%"]
  }
}
```

```bash
curl 'https://api.tangan.example.com/api/v1/reports/monthly' \
  -H 'Authorization: Bearer <ACCESS_TOKEN>'
```

### 7.2 家属共享关系

`GET /family-connections` 返回已关联家属及权限；`POST` 创建邀请；`DELETE /family-connections/{id}` 立即解除关系并撤销后续异常通知。

创建邀请请求：

```json
{ "contact": "13900000000", "relationship": "女儿", "permissions": ["critical_alerts", "weekly_report"] }
```

响应：

```json
{
  "data": {
    "id": "fam_7c7d3af9-1a52-4d9b-b7f1-efc001a46f32",
    "status": "pending",
    "relationship": "女儿",
    "permissions": ["critical_alerts", "weekly_report"],
    "expiresAt": "2026-09-06T08:00:00+08:00"
  }
}
```

```bash
curl -X POST 'https://api.tangan.example.com/api/v1/family-connections' \
  -H 'Authorization: Bearer <ACCESS_TOKEN>' -H 'Content-Type: application/json' \
  -d '{"contact":"13900000000","relationship":"女儿","permissions":["critical_alerts","weekly_report"]}'

curl 'https://api.tangan.example.com/api/v1/family-connections' \
  -H 'Authorization: Bearer <ACCESS_TOKEN>'

curl -X DELETE 'https://api.tangan.example.com/api/v1/family-connections/fam_7c7d3af9-1a52-4d9b-b7f1-efc001a46f32' \
  -H 'Authorization: Bearer <ACCESS_TOKEN>'
```

## 8. 公开内容接口

### 8.1 资讯列表与详情

`GET /public/articles?category=news&page=1&pageSize=10` 无需 Token，返回登录前首页展示的新闻卡片。

```json
{
  "data": [{
    "slug": "post-meal-walk-15-minutes",
    "title": "餐后步行 15 分钟",
    "summary": "轻松步行有助于平稳餐后血糖。",
    "coverUrl": "https://images.unsplash.com/photo-1551632811-561732d1e306",
    "publishedAt": "2026-08-28T09:00:00+08:00"
  }],
  "meta": { "page": 1, "pageSize": 10, "total": 1, "hasNext": false }
}
```

`GET /public/articles/{slug}` 返回 `lead`、`body`、免责声明和相关推荐。`slug` 只允许服务端已发布内容，未发布内容返回 `404`。

```bash
curl 'https://api.tangan.example.com/api/v1/public/articles?category=news&page=1&pageSize=10'
curl 'https://api.tangan.example.com/api/v1/public/articles/post-meal-walk-15-minutes'
```

### 8.2 指南详情

`GET /public/guides/{slug}` 支持：`glucose-guide`、`meals-guide`、`medication-guide`、`trend-guide`。

响应示例：

```json
{
  "data": {
    "slug": "glucose-guide",
    "title": "血糖怎么测",
    "eyebrow": "公开记录指南",
    "lead": "固定时段记录，趋势才有可比性。",
    "sections": [
      { "heading": "空腹", "body": "起床后、进食前测量。" },
      { "heading": "餐后 2 小时", "body": "从第一口饭开始计时。" }
    ],
    "disclaimer": "本文为公共健康科普，不替代医生针对个人情况的建议。"
  }
}
```

```bash
curl 'https://api.tangan.example.com/api/v1/public/guides/glucose-guide'
```

## 9. 关键业务流程泳道图

### 9.1 登录与权限控制

```mermaid
sequenceDiagram
    participant U as 用户
    participant V as Vue 前端
    participant G as API 网关
    participant A as Auth 服务
    participant DB as 用户库
    U->>V: 输入账号和密码
    V->>G: POST /sessions
    G->>A: 校验凭证
    A->>DB: 查询用户与密码哈希
    DB-->>A: 用户记录
    A-->>G: accessToken + refreshToken
    G-->>V: 201 Session
    V->>V: Vuex auth.login()
    V->>G: GET /dashboard (Bearer Token)
    G->>A: 校验 Token 与用户状态
    A-->>G: authenticated
    G-->>V: 首页聚合数据
    Note over V,G: 未登录访问个人接口时返回 401，前端跳转 /login
```

### 9.2 新增血糖并生成建议

```mermaid
sequenceDiagram
    participant U as 用户
    participant R as 记录页面
    participant API as API 服务
    participant D as 记录数据库
    participant I as 洞察服务
    U->>R: 填写读数、时段、饮食备注
    R->>API: POST /glucose-records
    API->>API: 校验范围、时区、Idempotency-Key
    API->>D: 保存记录
    D-->>API: recordId
    API->>I: 刷新 dashboard/trend 缓存
    API-->>R: 201 记录(status=normal/high/low)
    R->>API: POST /recommendations (recordId)
    API->>D: 读取近 7 天血糖、饮食、用药、运动
    D-->>API: 上下文数据
    API->>I: 执行规则与风险分级
    I-->>API: 建议、行动项、免责声明
    API-->>R: 200 Advice
    R-->>U: 展示状态、饮食建议和用药提醒
```

### 9.3 登录前公共内容

```mermaid
flowchart LR
    A[未登录用户] --> B{访问路径}
    B -->|/public/*| C[读取已发布资讯/指南]
    B -->|/home /records /trends| D[展示公共卡片与登录入口]
    B -->|/glucose /advice /settings| E[401 或路由跳转 /login]
    C --> F[不返回任何个人健康数据]
    D --> F
```

## 10. 实现与安全要求

1. 所有 SQL 查询必须带 `user_id` 条件，禁止通过 URL 中的 ID 越权读取其他用户数据。
2. 密码、Token、医疗备注不得写入普通日志；日志仅保留 `requestId`、用户内部 ID 和耗时。
3. 建议使用短期 access token（1 小时）和轮换 refresh token；退出时撤销当前会话。
4. 创建记录使用 `Idempotency-Key` 防止移动端重复点击造成重复数据。
5. 血糖值、剂量、运动时长等数值必须服务端再次校验，不能只依赖 Vue 表单校验。
6. 所有删除采用软删除并保留审计记录；报告生成只读取未删除版本。
7. 建议对登录接口、建议接口和家属邀请接口限流，并对连续登录失败启用验证码或临时锁定。
8. 建议为接口生成 OpenAPI 3.1 文件；本 Markdown 文档中的字段命名可直接映射到 `components.schemas`。
