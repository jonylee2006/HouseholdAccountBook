# 懒人账本（Lazy Ledger）

面向「懒得手动记账」人群的一站式自动记账方案，前端采用微信小程序，后端基于 Spring Boot + MySQL + Redis + RabbitMQ。当前仓库已经完成后台账单导入模块的基础实现，支持：

- 账单 CSV 上传后写入对象存储，再通过消息队列异步导入
- 微信/支付宝授权同步（示例实现使用 Mock 客户端）
- 成员角色/权限校验，仅管理员及以上可以发起导入
- 导入完成后自动发布“分类任务”“预算刷新”事件，便于后续扩展

## 目录结构

- `lazyledger-backend/`：Spring Boot 3.2 项目，内含导入接口、解析器、领域模型等代码

## 环境要求

- JDK 17、Maven 3.9+
- MySQL 8（建议创建 `lazyledger` 数据库）
- Redis 6
- RabbitMQ 3.x（默认使用 guest/guest，可在 `application.yml` 覆盖）
- 对象存储：示例实现为本地磁盘 `${user.home}/lazyledger/storage`，生产可替换为 OSS/COS 等实现

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
| `/api/v1/import/jobs` | `POST` | 上传 CSV（`multipart/form-data`，字段 `metadata` + `file`），后台将文件落地对象存储并投递异步任务 |
| `/api/v1/import/jobs/authorization` | `POST` | 基于已保存的授权记录触发远程拉取导入 |
| `/api/v1/import/jobs/{jobId}` | `GET` | 查询导入任务状态 |
| `/api/v1/import/jobs/{jobId}/transactions` | `GET` | 查看该任务入库的流水（仅展示预览字段） |
| `/api/v1/import/authorizations` | `POST / GET` | 管理微信/支付宝授权凭证，用于“授权拉取”场景 |

`metadata` JSON 示例：

```json
{
  "ledgerId": 1,
  "sourceType": "WECHAT"
}
```

授权触发示例：

```json
POST /api/v1/import/jobs/authorization
{
  "ledgerId": 1,
  "authorizationId": 10
}
```

## 下一步计划

- 接入对象存储获取临时上传凭证
- 结合家庭账本/成员权限体系
- 增加导入队列与异步通知 ✅（已初步完成）
- 完善分类模型与预算统计落地
