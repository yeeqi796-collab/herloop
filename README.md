# HerLoop 她循环

> 校园女性闲置交易平台 — Campus secondhand marketplace for female users

[English](#english) | [中文](#中文)

---

## English

### About

HerLoop is a campus-focused secondhand marketplace designed for female university students. Users can list idle items, post wanted ads, trade with each other, and earn points through daily check-ins, completing trades, and inviting friends.

### Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Spring Boot 3.3.0, Java 17, MyBatis-Plus, MySQL 8.x |
| Auth | JWT (jjwt 0.12.6), Spring Security, BCrypt |
| API Docs | Springdoc OpenAPI (Swagger UI) |
| Frontend | Single `index.html` (vanilla HTML/CSS/JS) |

### Project Structure

```
herloop/
├── index.html                  # Frontend (390px mobile UI, no build step)
├── docs/
│   └── api-spec.yaml           # OpenAPI 3.0 spec (44 endpoints)
└── herloop-backend/
    ├── pom.xml
    └── src/
        ├── main/java/com/herloop/
        │   ├── auth/           # Registration, login, JWT
        │   ├── user/           # Profile CRUD
        │   ├── product/        # Product listings
        │   ├── want/           # Wanted posts
        │   ├── trade/          # Trade lifecycle
        │   ├── points/         # Points system
        │   ├── favorite/       # Favorites
        │   ├── notification/   # Notifications
        │   ├── messaging/      # In-app chat
        │   ├── report/         # Reporting
        │   ├── admin/          # Admin panel
        │   ├── config/         # Security, CORS, MyBatis-Plus
        │   └── common/         # Shared: Result, exceptions, CurrentUser
        ├── main/resources/
        │   ├── application-example.yml
        │   └── db/schema.sql   # 12 tables
        └── test/java/com/herloop/
            ├── points/PointsServiceTest
            ├── product/ProductServiceTest
            └── trade/TradeServiceTest
```

### Quick Start

**Prerequisites:** Java 17+, MySQL 8.x, Maven 3.8+

```bash
# 1. Initialize database
mysql -u root -p < herloop-backend/src/main/resources/db/schema.sql

# 2. Configure application
cd herloop-backend
cp src/main/resources/application-example.yml src/main/resources/application.yml
# Edit application.yml: fill in MySQL credentials and JWT secret

# 3. Build and run
mvn clean package
mvn spring-boot:run
```

The server starts at `http://localhost:8080/api`. Swagger UI is available at `/api/swagger-ui.html`.

### Common Commands

```bash
cd herloop-backend

mvn clean package              # Build
mvn spring-boot:run            # Run
mvn test                       # Run all tests
mvn test -Dtest=ClassName      # Run a single test class
```

### Features

- **Auth** — Email registration, JWT login, Herstory verification
- **Products** — List, search (fulltext), filter by category/trade mode, image upload
- **Wanted Posts** — Post items you're looking for
- **Trading** — Full lifecycle: create → complete/cancel, with automatic point settlement
- **Points** — Register (+100), daily check-in (+5), trade (+50), invite friends (+100). 12-month expiry, 5000 cap, FIFO deduction
- **Favorites** — Save products to your collection
- **Messaging** — In-app chat between buyers and sellers
- **Notifications** — Trade updates, verification results, system messages
- **Reporting** — Report users/products; confirmed reports deduct -30 points
- **Admin** — Verification review, user management, platform statistics

### API

All endpoints are prefixed with `/api` and return:

```json
{ "code": 200, "message": "success", "data": { ... } }
```

See `docs/api-spec.yaml` for the complete OpenAPI 3.0 spec (44 endpoints across 11 controllers).

### Deployment

Intended setup is Nginx reverse proxy:
- Serve `index.html` at `/`
- Proxy `/api` to `localhost:8080`

File uploads are stored locally under `uploads/products/` and `uploads/proofs/`.

---

## 中文

### 项目简介

HerLoop（她循环）是一款面向高校女生的校园闲置交易平台。用户可以发布闲置物品、发布求购信息、互相交易，并通过每日签到、完成交易、邀请好友等方式赚取积分。

### 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Spring Boot 3.3.0, Java 17, MyBatis-Plus, MySQL 8.x |
| 认证 | JWT (jjwt 0.12.6), Spring Security, BCrypt |
| 接口文档 | Springdoc OpenAPI (Swagger UI) |
| 前端 | 单个 `index.html`（原生 HTML/CSS/JS） |

### 项目结构

```
herloop/
├── index.html                  # 前端（390px 手机 UI，无构建步骤）
├── docs/
│   └── api-spec.yaml           # OpenAPI 3.0 规范（44 个接口）
└── herloop-backend/
    ├── pom.xml
    └── src/
        ├── main/java/com/herloop/
        │   ├── auth/           # 注册、登录、JWT
        │   ├── user/           # 用户资料
        │   ├── product/        # 商品
        │   ├── want/           # 求购
        │   ├── trade/          # 交易
        │   ├── points/         # 积分
        │   ├── favorite/       # 收藏
        │   ├── notification/   # 通知
        │   ├── messaging/      # 私信
        │   ├── report/         # 举报
        │   ├── admin/          # 管理后台
        │   ├── config/         # 安全、CORS、MyBatis-Plus
        │   └── common/         # 通用：Result、异常、CurrentUser
        ├── main/resources/
        │   ├── application-example.yml
        │   └── db/schema.sql   # 12 张表
        └── test/java/com/herloop/
            ├── points/PointsServiceTest
            ├── product/ProductServiceTest
            └── trade/TradeServiceTest
```

### 快速开始

**环境要求：** Java 17+、MySQL 8.x、Maven 3.8+

```bash
# 1. 初始化数据库
mysql -u root -p < herloop-backend/src/main/resources/db/schema.sql

# 2. 配置应用
cd herloop-backend
cp src/main/resources/application-example.yml src/main/resources/application.yml
# 编辑 application.yml：填写 MySQL 连接信息和 JWT 密钥

# 3. 构建并运行
mvn clean package
mvn spring-boot:run
```

服务启动后访问 `http://localhost:8080/api`，Swagger UI 地址为 `/api/swagger-ui.html`。

### 常用命令

```bash
cd herloop-backend

mvn clean package              # 构建
mvn spring-boot:run            # 运行
mvn test                       # 运行全部测试
mvn test -Dtest=ClassName      # 运行单个测试类
```

### 功能模块

- **认证** — 邮箱注册、JWT 登录、Herstory 实名认证
- **商品** — 发布、搜索（全文检索）、按分类/交易方式筛选、图片上传
- **求购** — 发布想买的物品
- **交易** — 完整生命周期：创建 → 完成/取消，自动结算积分
- **积分** — 注册 (+100)、每日签到 (+5)、交易完成 (+50)、邀请好友 (+100)。12 个月过期、5000 上限、先进先出扣除
- **收藏** — 收藏感兴趣的商品
- **私信** — 买卖双方在线聊天
- **通知** — 交易动态、认证结果、系统消息
- **举报** — 举报用户/商品，确认后扣 30 积分
- **管理后台** — 认证审核、用户管理、平台统计

### 接口说明

所有接口以 `/api` 为前缀，统一返回格式：

```json
{ "code": 200, "message": "success", "data": { ... } }
```

完整接口文档见 `docs/api-spec.yaml`（OpenAPI 3.0 规范，11 个控制器共 44 个接口）。

### 部署方式

推荐使用 Nginx 反向代理：
- `/` 指向 `index.html`
- `/api` 代理到 `localhost:8080`

文件上传存储在项目根目录的 `uploads/products/` 和 `uploads/proofs/`。
