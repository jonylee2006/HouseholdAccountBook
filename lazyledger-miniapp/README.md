# 懒人账本微信小程序

## 结构
```
miniprogram/
  app.js / app.json / app.wxss
  pages/
    login/           # 登录示例
    index/           # 首页概览（今日支出/预算/TOP3）
    import/          # OSS 直传 + 授权导入
    ledgerDetail/    # 某次导入流水查询
    ledgerMember/    # 家庭共享邀请/加入
    report/          # 月度报表
    profile/         # 账号、账本、订阅入口
  utils/request.js   # API 封装
```

## 快速运行
1. 安装 [微信开发者工具](https://developers.weixin.qq.com/miniprogram/dev/devtools/download.html)，导入 `lazyledger-miniapp` 目录。
2. 在 `miniprogram/app.js` 中将 `apiBase` 改为实际后端地址，例如 `https://your-domain.com`。
3. 登录页使用示例手机号直接调用后端 `POST /api/auth/wx-login`，后台返回的 `userId` 会写入 `X-User-Id` 供所有 API 使用。
4. 在“我的”页创建或切换账本后，即可体验首页概览、账单导入、邀请共享、报表统计等功能。

> 提示：OSS 上传需使用真实签名，导入页会先调用 `/api/import/upload-url` 再使用 `wx.uploadFile` 直传，随后调用 `/api/import/parse-csv` 提交导入任务。
