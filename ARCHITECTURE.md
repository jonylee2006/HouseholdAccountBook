## 懒人账本系统说明（v1）

### 1. 总体架构
- **技术栈**
  - 前端：微信小程序原生（WXML/WXSS/JS）；REST + SSE 与后端交互。
  - 后端：Spring Boot 3.x、Spring Security、JPA + MySQL、Redis（缓存/任务）、RabbitMQ（导入链路）。
  - 对象存储：阿里云 OSS / 腾讯 COS，用于 CSV 直传与授权拉取。
- **部署拓扑**
  ```
  小程序 → Nginx → Spring Boot 服务
                         ├── MySQL（结构化数据）
                         ├── Redis（缓存/消息）
                         ├── RabbitMQ（导入/预算事件）
                         └── OSS/COS（账单文件）
  ```
- **事件/任务**
  - 导入：上传 → MQ → 解析 → 分类 → 写库 → SSE/MQ 通知。
  - 定时任务：`MaintenanceScheduler` 预留 nightly Hook，可扩展预算提醒/报表刷新。

### 2. 模块拆分
| 模块 | 说明 | 关键类 |
| --- | --- | --- |
| auth | 微信登录示例、用户创建 | `AuthController`, `AuthService` |
| ledger | 账本、成员、邀请、实时推送 | `LedgerController`, `LedgerInviteController`, `LedgerStreamService` |
| importer | 上传签名、CSV 解析、授权拉取、任务管理 | `ImportBridgeController`, `StatementImportService`, `ImportJobProcessor` |
| classifier | 词典 + 规则 + LLM 预留 | `TransactionClassificationService`, `ClassificationDictionary`, `ClassificationServiceClient` |
| dashboard/report | 首页概览、报表快照 | `DashboardController`, `DashboardService`, `ReportController`, `ReportService` |
| billing/subscription | 套餐等级/到期控制 | `SubscriptionController`, `SubscriptionService` |
| storage | OSS/COS 直传签名、文件读取 | `StorageService`, `AliyunOssStorageService`, `TencentCosStorageService` |
| budget/category | 预算、分类配置 | `Budget`, `Category`, `CategorySummary` |

### 3. 核心流程
1. **用户 & 账本**
   - `POST /api/auth/wx-login`：通过 code/phone/nickname 生成示例 token，真实环境需接入微信服务端。
   - `POST /api/ledger`：创建账本并自动写入 OWNER 成员。

2. **账单导入**
   - 直传：`/api/import/upload-url` 获取签名 → 小程序上传 → `POST /api/import/parse-csv` 提交 `objectKey/statementDate`。
   - 授权：`POST /api/import/parse-authorization` 通过授权 ID 拉取账单。
   - 后台：`ImportJobProcessor` 解析 CSV → `Transaction` + `ImportJob` 入库 → 触发分类/预算/SSE。

3. **智能分类**
   - 规则优先：用户自定义规则 (`classification_rule`)。
   - 词典回退：`ClassificationDictionary` 匹配常见商户。
   - 外部模型：`ClassificationServiceClient` 预留调用（可配置关闭）。
   - 人工纠偏：`PUT /v1/transactions/{id}/category` 修改分类并可保存规则。

4. **家庭共享 & 实时推送**
   - 邀请：`POST /api/ledger/{id}/invite` 生成 token；`POST /api/ledger/invite/{token}/accept` 加入账本。
   - SSE：`GET /api/ledger/{id}/stream` 订阅导入完成、成员加入等事件。

5. **超简首页 & 报表**
   - `GET /api/dashboard/summary?ledgerId=`：今日支出、月预算、剩余额、TOP3 分类（JPA 聚合 + `budget`）。
   - `GET /v1/report/monthly`：生成/返回报表快照 (`report_snapshot`)。

6. **订阅/权益**
   - `POST /v1/subscription`：写入 `subscription_level`、到期时间；用于控制自动导入、多成员、报表导出等付费点。

### 4. 数据库关键表
| 表 | 说明 | 主要字段 |
| --- | --- | --- |
| `user_account` | 用户信息 | `phone`, `nickname`, `created_at` |
| `ledger` / `ledger_member` | 账本与成员 | `owner_id`, `currency`, `role`, `status` |
| `ledger_invite` | 邀请 token | `token`, `inviter_id`, `status`, `expired_at` |
| `transaction` | 流水 (wechat/alipay/manual) | `source_type`, `direction`, `category`, `raw_payload` |
| `import_job` | 导入任务 | `source_type`, `object_key`, `statement_date`, `status` |
| `classification_rule` | 用户规则 | `ledger_id`, `keyword`, `category` |
| `budget` | 月度预算 | `ledger_id`, `year_month`, `amount` |
| `subscription` | 套餐/权益 | `ledger_id`, `level`, `expires_at` |
| `category` | 分类配置（系统/自定义） | `ledger_id`, `name`, `type` |
| `report_snapshot` | 报表缓存 | `ledger_id`, `period`, `payload` |

### 5. API & 前端交互
| 接口 | 用途 |
| --- | --- |
| `POST /api/auth/wx-login` | 微信登录（示例） |
| `POST /api/ledger` | 新建账本 |
| `POST /api/import/upload-url` | 获取 OSS/COS 直传签名 |
| `POST /api/import/parse-csv` / `parse-authorization` | 发起账单导入 |
| `GET /api/dashboard/summary` | 首页概览（今日支出、月预算、TOP3） |
| `PUT /v1/transactions/{id}/category` | 单笔改分类/保存规则 |
| `POST /api/ledger/{id}/invite` / `POST /api/ledger/invite/{token}/accept` | 家庭共享 |
| `GET /api/ledger/{id}/stream` | SSE 实时消息 |
| `GET /v1/report/monthly` | 月度报表 |
| `POST /v1/subscription` | 订阅/权益 |

前端页面建议：
- `pages/index/index`：使用 `/api/dashboard/summary` + SSE，展示今日支出、预算、TOP3。
- `pages/import/index`：上传流程（签名 → 直传 → 发起导入）。
- `pages/ledger/detail`：流水列表，可接入分页 API。
- `pages/ledger/member`：成员管理、邀请 token。
- `pages/report/index`：月/年报图表，调用 `/v1/report/monthly`。
- `pages/profile/vip`：订阅信息、权益说明、支付入口。

### 6. 运维与扩展
- **运行**：`mvn clean package -DskipTests && java -jar target/lazyledger-backend-0.0.1-SNAPSHOT.jar`。
- **测试**：`mvn clean verify`（执行分类、邀请、Dashboard 等单元/契约测试）。
- **配置**：在 `application.yml` 或环境变量中设置 MySQL/Redis/RabbitMQ、OSS/COS、外部分类服务地址。
- **监控**：建议启用 Spring Actuator、RabbitMQ/Redis/数据库告警。
- **拓展方向**：  
  - 接入真实微信/支付宝 OAuth、支付回调。  
  - 增强订阅计费、广告投放、A/B 测试。  
  - 引入更细粒度的分类模型和 BI 报表。  
  - 拆分微服务或接入链路追踪（SkyWalking/Zipkin）以支撑更大规模。

该文档可作为后续开发、运维、版本规划的参考手册。
