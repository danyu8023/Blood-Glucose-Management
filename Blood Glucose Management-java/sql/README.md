# 数据库字符集说明

本项目统一使用 `utf8mb4` 保存中文内容，JDBC 连接参数已设置为 `characterEncoding=UTF-8` 和 `connectionCollation=utf8mb4_unicode_ci`。

## 检查数据库字符集

```sql
SELECT @@character_set_database, @@collation_database,
       @@character_set_connection, @@character_set_client;
```

结果应为 `utf8mb4`（排序规则可以是 `utf8mb4_unicode_ci` 或 MySQL 8 默认的 `utf8mb4_0900_ai_ci`）。

## 检查表字符集和 UUID 字段

```sql
SELECT TABLE_NAME, TABLE_COLLATION
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'bloodmanage';

SELECT TABLE_NAME, COLUMN_NAME, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'bloodmanage'
  AND COLUMN_NAME IN ('id', 'user_id');
```

`id` 和 `user_id` 应为 `char(36)`。旧版本 Hibernate 可能创建为 `binary(16)`，数据库客户端会将其显示成类似乱码的二进制字符；当前开发库已经完成转换。

如果中文在客户端仍显示为乱码，请在数据库工具的连接属性中选择 `UTF-8/utf8mb4`，重新打开查询结果。不要使用 GBK 或 Latin1 连接打开 utf8mb4 数据。
