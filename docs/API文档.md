# HerLoop API 接口文档

> Base URL: `http://localhost:8080/api`
>
> 认证方式: 请求 Header 中携带 `Authorization: Bearer <token>`

---

## 通用说明

### 统一响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

### 错误码

| code | 说明 |
|------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未登录或 token 过期 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

### 分页格式

```json
{
  "code": 200,
  "data": {
    "list": [...],
    "total": 42,
    "page": 1,
    "pageSize": 10
  }
}
```

---

## 1. 认证模块 (Auth)

### POST /auth/register

注册新用户。

**请求体：**
```json
{
  "nickname": "小叶",
  "password": "123456",
  "avatar": "🦢",
  "wechat": "xiaoye_wx"
}
```

**响应：**
```json
{
  "code": 200,
  "message": "注册成功",
  "data": null
}
```

---

### POST /auth/login

用户登录，返回 JWT token。

**请求体：**
```json
{
  "nickname": "小叶",
  "password": "123456"
}
```

**响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "user": {
      "id": 1,
      "nickname": "小叶",
      "avatar": "🦢",
      "points": 186,
      "verified": true
    }
  }
}
```

---

## 2. 用户模块 (User)

### GET /user/profile

获取当前登录用户信息。需要认证。

**响应：**
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "nickname": "小叶",
    "avatar": "🦢",
    "wechat": "xiaoye_wx",
    "points": 186,
    "verified": true,
    "productCount": 4,
    "tradeCount": 9,
    "createdAt": "2026-05-01T10:00:00"
  }
}
```

---

### PUT /user/profile

更新个人资料。需要认证。

**请求体：**
```json
{
  "nickname": "新昵称",
  "avatar": "🌸",
  "wechat": "new_wx_id"
}
```

---

## 3. 商品模块 (Product)

### GET /products

获取商品列表，支持筛选和分页。

**Query 参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|:----:|------|
| category | string | 否 | 分类：全部/服饰/图书/美妆/运动/生活 |
| tradeMode | string | 否 | 交易方式：cash/points/both |
| status | string | 否 | 状态：on/reserved/sold |
| keyword | string | 否 | 关键词搜索 |
| page | int | 否 | 页码，默认 1 |
| pageSize | int | 否 | 每页数量，默认 10 |

**响应 data 结构：**
```json
{
  "list": [
    {
      "id": 1,
      "title": "真丝睡裙 米白色",
      "category": "服饰",
      "condition": "95新",
      "description": "澳洲带回的真丝睡裙...",
      "cashPrice": 120,
      "pointsPrice": 0,
      "tradeMode": "cash",
      "status": "on",
      "icon": "dress",
      "createdAt": "2026-06-20T10:00:00",
      "seller": {
        "id": 2,
        "nickname": "小鹿",
        "avatar": "🦌",
        "verified": true
      }
    }
  ],
  "total": 8,
  "page": 1,
  "pageSize": 10
}
```

---

### GET /products/{id}

获取商品详情。

**路径参数：** `id` - 商品 ID

**响应 data：** 同上述单个商品对象

---

### POST /products

发布商品。需要认证。

**请求体：**
```json
{
  "title": "真丝睡裙 米白色",
  "category": "服饰",
  "condition": "95新",
  "description": "澳洲带回的真丝睡裙，只穿过两次...",
  "cashPrice": 120,
  "pointsPrice": 0,
  "tradeMode": "cash",
  "icon": "dress",
  "wechat": "xiaolu_wx"
}
```

---

### PUT /products/{id}/status

更新商品状态。需要认证（仅商品发布者可操作）。

**请求体：**
```json
{
  "status": "reserved"
}
```

status 可选值：`on`（重新上架）、`reserved`（已预定）、`sold`（已成交）

---

### GET /user/products

获取当前用户发布的商品列表。需要认证。

**Query 参数：** `status`（可选）、`page`、`pageSize`

---

## 4. 求购模块 (Want)

### GET /wants

获取求购列表。

**Query 参数：** `page`、`pageSize`

**响应 data 结构：**
```json
{
  "list": [
    {
      "id": 1,
      "title": "求购 Kindle 电子书阅读器",
      "budget": "¥300以内",
      "description": "看书用，二手九成新即可...",
      "icon": "kindle",
      "status": "open",
      "createdAt": "2026-06-20T10:00:00",
      "user": {
        "id": 2,
        "nickname": "阿茶",
        "avatar": "🍵",
        "verified": true
      }
    }
  ],
  "total": 3,
  "page": 1,
  "pageSize": 10
}
```

---

### GET /wants/{id}

获取求购详情。

---

### POST /wants

发布求购。需要认证。

**请求体：**
```json
{
  "title": "求购 Kindle 电子书阅读器",
  "budget": "¥300以内",
  "description": "看书用，二手九成新即可...",
  "icon": "kindle"
}
```

---

### GET /user/wants

获取当前用户的求购列表。需要认证。

---

## 5. 交易模块 (Trade)

### GET /user/trades

获取我的交易记录。需要认证。

**Query 参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|:----:|------|
| type | string | 否 | buy（我买的）/ sell（我卖的） |
| page | int | 否 | 页码 |
| pageSize | int | 否 | 每页数量 |

**响应 data 结构：**
```json
{
  "list": [
    {
      "id": 1,
      "productId": 2,
      "product": { "id": 2, "title": "Herstory 课程笔记本", "icon": "book", ... },
      "type": "buy",
      "status": "已完成",
      "tradeDate": "2026-06-15T14:00:00"
    }
  ],
  "total": 1,
  "page": 1,
  "pageSize": 10
}
```

---

## 6. 积分模块 (Points)

### GET /user/points

获取积分余额。需要认证。

**响应：**
```json
{
  "code": 200,
  "data": {
    "points": 186
  }
}
```

---

### GET /user/points/log

获取积分流水。需要认证。

**Query 参数：** `page`、`pageSize`

**响应 data 结构：**
```json
{
  "list": [
    {
      "id": 1,
      "description": "出售「化妆刷套装」获得积分",
      "amount": "+60",
      "createdAt": "2026-06-16T10:00:00"
    }
  ],
  "total": 3,
  "page": 1,
  "pageSize": 20
}
```

---

## 7. 收藏模块 (Favorite)

### GET /favorites

获取我的收藏列表。需要认证。

**Query 参数：** `page`、`pageSize`

**响应 data：** 分页的商品列表（同 Product 格式）

---

### POST /favorites/{productId}

添加收藏。需要认证。

---

### DELETE /favorites/{productId}

取消收藏。需要认证。

---

## 字段枚举值速查

| 字段 | 可选值 |
|------|--------|
| category | 全部、服饰、图书、美妆、运动、生活 |
| tradeMode | cash、points、both |
| status (商品) | on（在售）、reserved（预定中）、sold（已成交） |
| status (求购) | open（求购中）、closed（已关闭） |
| type (交易) | buy、sell |

---

## 前端对接要点

1. 登录后将 `token` 存入 `localStorage`
2. 每次请求添加 Header: `Authorization: Bearer <token>`
3. 原来硬编码的 `products`、`wants`、`pointsLog`、`myTrades` 数组替换为 API 调用
4. 商品对象字段变化：`cond` → `condition`，`desc` → `description`，`cash` → `cashPrice`，`points` → `pointsPrice`，`mode` → `tradeMode`，`date` → `createdAt`
5. 积分流水字段变化：`t` → `description`，`v` → `amount`，`d` → `createdAt`
