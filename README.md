# 懒人账本（Lazy Ledger）

面向「懒得手动记账」人群的一站式自动记账方案，前端采用微信小程序，后端基于 Spring Boot + MySQL + Redis。当前仓库已经完成后台账单导入模块的基础实现，支持上传微信/支付宝官方导出的 CSV 文件，完成解析、入库与结果查询。

## 目录结构

- `lazyledger-backend/`：Spring Boot 3.2 项目，内含导入接口、解析器、领域模型等代码

## 环境要求

- JDK 17
- Maven 3.9+
- MySQL 8（建议创建 `lazyledger` 数据库）
- Redis 6

## 后端快速启动

```bash
cd lazyledger-backend
mvn spring-boot:run
```

首次运行前可在 `src/main/resources/application.yml` 中调整数据库/Redis 连接信息。

## 核心导入接口

| 接口 | 方法 | 说明 |
| --- | --- | --- |
| `/api/v1/import/jobs` | `POST` | 上传 CSV（`multipart/form-data`，字段 `metadata` + `file`），完成导入或预览（`dryRun=true`） |
| `/api/v1/import/jobs/{jobId}` | `GET` | 查询导入任务状态 |
| `/api/v1/import/jobs/{jobId}/transactions` | `GET` | 查看该任务解析出的流水（仅展示预览字段） |

`metadata` JSON 示例：

```json
{
  "ledgerId": 1,
  "sourceType": "WECHAT",
  "dryRun": false
}
```

## 下一步计划

- 接入对象存储获取临时上传凭证
- 结合家庭账本/成员权限体系
- 增加导入队列与异步通知
