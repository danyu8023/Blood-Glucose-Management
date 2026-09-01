# 糖安血糖管理：本地运行与联调教程

本文面向第一次运行项目的开发者，环境为 Windows、JDK 17、MySQL 8、Node.js 18+。

## 1. 数据库准备

确认 MySQL 服务已启动，并准备以下实例和账号：

```text
数据库：bloodmanage
用户名：bloodManage
密码：123456
端口：3306
```

`bloodmanage` 数据库需要允许用户创建或修改表。后端使用 Hibernate `ddl-auto=update` 自动建表，也可以先执行后端目录下的 `sql/schema.sql`。

项目已固定 `hibernate.type.preferred_uuid_jdbc_type: CHAR`，UUID 主键和外键使用 `CHAR(36)` 保存，避免在数据库客户端中把二进制 UUID 显示成乱码。现有开发库已经完成迁移；新环境执行 `schema.sql` 后也会直接得到可读的 UUID。

## 2. 启动后端

打开 PowerShell：

```powershell
cd 'D:\codexWork\Blood Glucose Management\Blood Glucose Management-java'
& 'D:\codexWork\PM14-0\store-java\.tools\maven-full\apache-maven-3.9.9\bin\mvn.cmd' '-Dmaven.repo.local=D:\codexWork\PM14-0\store-java\.m2repo' clean package
java -jar target\blood-glucose-management-api-1.0.0.jar
```

看到 `Started GlucoseManagementApplication` 后，接口地址为 `http://127.0.0.1:8080/api/v1`。首次启动会写入演示账号和公开文章。

演示账号：`13800000000`，密码：`123456`。

## 3. 启动前端

另开一个 PowerShell：

```powershell
cd 'D:\codexWork\Blood Glucose Management\Blood Glucose Management-vue'
npm install
npm run dev
```

浏览器打开 Vite 输出的地址（通常是 `http://localhost:5173`）。前端默认请求 `http://127.0.0.1:8080/api/v1`；如需修改，可设置 `VITE_API_BASE` 环境变量后重新启动。

## 4. 快速联调

```powershell
$base='http://127.0.0.1:8080/api/v1'
$login=Invoke-RestMethod -Uri "$base/sessions" -Method Post -ContentType 'application/json' -Body (@{account='13800000000';password='123456'}|ConvertTo-Json)
$headers=@{Authorization="Bearer $($login.data.accessToken)"}
Invoke-RestMethod -Uri "$base/dashboard" -Headers $headers
Invoke-RestMethod -Uri "$base/glucose-trends?range=7d" -Headers $headers
Invoke-RestMethod -Uri "$base/public/articles" -Method Get
```

新增血糖记录示例：

```powershell
$body=@{value=6.4;unit='mmol/L';period='fasting';measuredAt='2026-08-30T07:30:00+08:00';note='早餐前'}|ConvertTo-Json
Invoke-RestMethod -Uri "$base/glucose-records" -Method Post -Headers $headers -ContentType 'application/json' -Body $body
```

## 5. 权限与页面行为

未登录只能访问首页、记录、趋势中的公开健康内容，以及登录和注册页面；个人血糖、饮食、用药、运动、趋势、建议、设置和家属共享接口均需要 Bearer Token。退出登录会撤销当前用户的刷新会话并清理浏览器本地 Token。

## 6. 常见问题

- `Access denied for user`：检查 MySQL 用户名大小写，必须使用 `bloodManage`，并确认该用户拥有 `bloodmanage.*` 权限。
- 前端请求跨域：确认后端 `tangan.allowed-origins` 包含 Vite 地址，默认允许本机 `localhost:*` 和 `127.0.0.1:*`，包括 Vite 自动切换到的 5174 端口。
- 端口被占用：修改 `server.port`，同时设置前端 `VITE_API_BASE` 指向新端口。
- 登录后页面仍显示默认数据：打开浏览器开发者工具确认 `/sessions`、`/dashboard` 返回 2xx，并清除旧的 `tangan_vue_auth`、`tangan_access_token` 后重新登录。

完整字段、状态码、curl 用例和业务泳道图请参考同目录的 `RESTful-API.md`。
