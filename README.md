# Security-Defense 安全防御系统

基于 Spring Boot + Vue 3 的安全防御与态势感知系统，包含资产与风险管理、攻击事件监控、威胁评分、合规报告与安全态势大屏等模块。

## 技术栈

**后端**
- Spring Boot 3 + Spring Security
- MyBatis-Plus（MySQL）
- Druid 连接池
- HTTPS / TLS（PKCS12）

**前端**
- Vue 3 + TypeScript + Vite
- ECharts 态势可视化

## 目录结构

```
├── src/main/java/com/example/securitydefense/   后端源码
│   ├── situation/     安全态势（检测器、评分、大屏接口）
│   ├── security/      攻击事件与安全管理
│   ├── report/        合规与威胁报告
│   └── ...
├── src/main/resources/
│   ├── application.properties   应用配置
│   ├── mapper/                  MyBatis XML
│   └── sql/                     建库脚本
├── frontend/          Vue 3 前端
└── pom.xml            Maven 构建
```

## 本地运行

后端（默认 8443，启用 HTTPS）：

```bash
./mvnw spring-boot:run
```

前端：

```bash
cd frontend
npm install
npm run dev
```

数据库配置见 `src/main/resources/application.properties`，建表脚本见 `src/main/resources/sql/`。

## 配置说明

本仓库为公开仓库，敏感配置不入库，运行前请设置环境变量：

| 变量 | 说明 | 示例 |
|---|---|---|
| `DB_PASSWORD` | MySQL 密码 | `你的数据库密码` |
| `KEYSTORE_PASSWORD` | HTTPS 证书库口令 | `你的证书口令` |

IDEA 中可在 Run Configuration → Environment variables 设置；命令行可用：

```bash
DB_PASSWORD=你的数据库密码 KEYSTORE_PASSWORD=你的证书口令 ./mvnw spring-boot:run
```

前端 `frontend/.env` 中的 `VITE_AES_KEY` 也请替换为自己的值。

## 注意事项

- 证书文件（`*.p12`）未入库，运行前需自行放到 `src/main/resources/`。
- 运行日志（`*.log`）、`uploads/` 与 `node_modules/` 已在 `.gitignore` 中排除，不会入库。
