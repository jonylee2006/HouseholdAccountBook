# 懒人账本（Lazy Ledger）

面向「懒得手动记账」人群的一站式自动记账方案，前端采用微信小程序，后端基于 Spring Boot + MySQL + Redis + RabbitMQ。当前仓库已经完成后台账单导入模块的基础实现，支持：

- OSS/COS 直传：小程序通过 `/v1/storage/signatures` 获取预签名 URL，直接把 CSV 上传至云存储
- 上传完成后仅需携带 `objectKey` 调用导入接口，系统会落入队列并异步解析
- 微信/支付宝授权同步（真实 OAuth/Bearer Token 流程，可配置官方开放平台 API 地址）
- 成员角色/权限校验，仅管理员及以上可以发起导入
- 导入完成后调用分类微服务，同时将预算事件推送到 RabbitMQ，触发后续清算

## 目录结构

- `lazyledger-backend/`：Spring Boot 3.2 项目，内含导入接口、解析器、领域模型等代码

## 环境要求

- JDK 17、Maven 3.9+
- MySQL 8（建议创建 `lazyledger` 数据库）
- Redis 6
- RabbitMQ 3.x（默认使用 guest/guest，可在 `application.yml` 覆盖）
- 对象存储：原生支持阿里云 OSS 与腾讯 COS，需在 `lazyledger.storage.*` 中配置凭证

## 后端快速启动

```bash
cd lazyledger-backend
mvn spring-boot:run
```

首次运行前可在 `src/main/resources/application.yml` 中调整数据库/Redis 连接信息。

> 调试阶段可在请求头添加 `X-User-Id: 1` 来模拟已登录用户。

## 核心导入接口

| 接口 | 方法 | 说明 |
| --- | --- | --- |
| `/api/v1/storage/signatures` | `POST` | 生成 OSS/COS 直传签名，返回 `url/objectKey/headers`，用于小程序直传 |
| `/api/v1/import/jobs` | `POST` | 提交导入任务（JSON），仅需提供 `ledgerId/sourceType/objectKey` 即可 |
| `/api/v1/import/jobs/authorization` | `POST` | 基于已保存的授权记录触发远程拉取导入 |
| `/api/v1/import/jobs/{jobId}` | `GET` | 查询导入任务状态 |
| `/api/v1/import/jobs/{jobId}/transactions` | `GET` | 查看该任务入库的流水（仅展示预览字段） |
| `/api/v1/import/authorizations` | `POST / GET` | 管理微信/支付宝授权凭证，用于“授权拉取”场景 |

直传上传后提交导入示例：

```json
POST /api/v1/import/jobs
{
  "ledgerId": 1,
  "sourceType": "WECHAT",
  "objectKey": "statements/6f6c8b5c.csv",
  "statementDate": "2024-05-01"
}
```

授权触发示例：

```json
POST /api/v1/import/jobs/authorization
{
  "ledgerId": 1,
  "authorizationId": 10,
  "statementDate": "2024-05-01"
}
```

## 下一步计划

- 更细粒度的账单分类模型接入与策略管理
- 家庭共享账本的多维度权限控制
- 导入全链路压测 & Trace/指标输出
