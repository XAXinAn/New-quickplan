# QuickPlan 用户认证接口文档

> **版本**: v1.0  
> **最后更新**: 2025-10-29  
> **Base URL**: `http://your-server:8080`

---

## 目录

1. [接口概览](#接口概览)
2. [通用说明](#通用说明)
3. [手机号注册](#手机号注册)
4. [邮箱注册](#邮箱注册)
5. [手机号登录](#手机号登录)
6. [邮箱登录](#邮箱登录)
7. [第三方登录](#第三方登录)
8. [Token管理](#token管理)
9. [用户信息](#用户信息)
10. [错误码说明](#错误码说明)

---

## 接口概览

| 接口名称 | 请求方法 | 路径 | 说明 |
|---------|---------|------|------|
| 发送验证码 | POST | `/api/auth/phone/send-code` | 向手机号发送验证码(登录/注册) |
| 手机号注册 | POST | `/api/auth/phone/register` | 使用手机号验证码注册 |
| 邮箱注册 | POST | `/api/auth/email/register` | 使用邮箱和密码注册 |
| 手机号登录 | POST | `/api/auth/phone/login` | 使用手机号和验证码登录 |
| 邮箱登录 | POST | `/api/auth/email/login` | 使用邮箱和密码登录 |
| 微信登录 | POST | `/api/auth/wechat/login` | 使用微信授权登录 |
| QQ登录 | POST | `/api/auth/qq/login` | 使用QQ授权登录 |
| 刷新Token | POST | `/api/auth/refresh-token` | 刷新访问令牌 |
| 登出 | POST | `/api/auth/logout` | 退出登录 |
| 获取用户信息 | GET | `/api/user/info` | 获取当前登录用户信息 |

---

## 通用说明

### 请求头

```http
Content-Type: application/json
Authorization: Bearer {token}  # 需要认证的接口
```

### 统一响应格式

**成功响应**:
```json
{
  "success": true,
  "message": "操作成功",
  "data": {
    // 具体数据
  }
}
```

**失败响应**:
```json
{
  "success": false,
  "message": "错误信息描述",
  "data": null
}
```

---

## 手机号注册

### 1. 手机号验证码注册

**接口**: `POST /api/auth/phone/register`

**描述**: 使用手机号+短信验证码创建新账号，成功后直接返回登录态。

**请求体**:
```json
{
  "phone": "13800138000",
  "code": "123456",
  "password": "your_password_here",
  "nickname": "用户昵称"
}
```

**字段说明**:
| 字段 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| phone | String | 是 | 11位手机号码 |
| code | String | 是 | 6位短信验证码 |
| password | String | 否 | 首次设置的登录密码，至少6位 |
| nickname | String | 否 | 用户昵称，不提供则后端生成默认昵称 |

**响应示例**:
```json
{
  "success": true,
  "message": "注册成功",
  "data": {
    "userId": "user_123456",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "refresh_token_here",
    "expiresIn": 7200,
    "userInfo": {
      "userId": "user_123456",
      "phone": "13800138000",
      "nickname": "用户昵称",
      "avatar": "https://example.com/default-avatar.png",
      "createdAt": "2025-10-29T10:30:00",
      "loginType": "phone"
    }
  }
}
```

**错误码**:
- `400` - 手机号或验证码格式不正确
- `401` - 验证码错误或已过期
- `409` - 手机号已注册
- `500` - 服务器内部错误

---

## 邮箱注册

### 2. 邮箱注册

**接口**: `POST /api/auth/email/register`

**描述**: 使用邮箱和密码创建新账号，成功后直接返回登录态。

**请求体**:
```json
{
  "email": "user@example.com",
  "password": "your_password_here",
  "nickname": "用户昵称"
}
```

**字段说明**:
| 字段 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| email | String | 是 | 邮箱地址 |
| password | String | 是 | 登录密码(至少6位) |
| nickname | String | 否 | 用户昵称，不提供则后端生成默认昵称 |

**响应示例**:
```json
{
  "success": true,
  "message": "注册成功",
  "data": {
    "userId": "user_123456",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "refresh_token_here",
    "expiresIn": 7200,
    "userInfo": {
      "userId": "user_123456",
      "email": "user@example.com",
      "nickname": "用户昵称",
      "avatar": "https://example.com/avatar.png",
      "createdAt": "2025-10-29T10:30:00",
      "loginType": "email"
    }
  }
}
```

**错误码**:
- `400` - 邮箱或密码格式不正确
- `409` - 邮箱已注册
- `500` - 服务器内部错误

---

## 手机号登录

### 1. 发送验证码

**接口**: `POST /api/auth/phone/send-code`

**描述**: 向指定手机号发送6位数字验证码，有效期5分钟

**请求体**:
```json
{
  "phone": "13800138000",
  "type": "login"
}
```

**字段说明**:
| 字段 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| phone | String | 是 | 11位手机号码 |
| type | String | 否 | 验证码类型: `login`(登录) 或 `register`(注册)，默认`login` |

**响应示例**:
```json
{
  "success": true,
  "message": "验证码已发送",
  "data": {
    "expiresIn": 300
  }
}
```

**字段说明**:
| 字段 | 类型 | 说明 |
|-----|------|------|
| expiresIn | Integer | 验证码有效期(秒) |

**错误码**:
- `400` - 手机号格式不正确
- `429` - 发送过于频繁，请稍后再试
- `500` - 短信服务异常

---

### 2. 手机号验证码登录

**接口**: `POST /api/auth/phone/login`

**描述**: 使用手机号和验证码登录，如果用户不存在则自动注册

**请求体**:
```json
{
  "phone": "13800138000",
  "code": "123456"
}
```

**字段说明**:
| 字段 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| phone | String | 是 | 11位手机号码 |
| code | String | 是 | 6位验证码 |

**响应示例**:
```json
{
  "success": true,
  "message": "登录成功",
  "data": {
    "userId": "user_123456",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "refresh_token_here",
    "expiresIn": 7200,
    "userInfo": {
      "userId": "user_123456",
      "phone": "13800138000",
      "nickname": "用户138****8000",
      "avatar": "https://example.com/default-avatar.png",
      "createdAt": "2025-10-29T10:30:00",
      "loginType": "phone"
    }
  }
}
```

**字段说明**:
| 字段 | 类型 | 说明 |
|-----|------|------|
| userId | String | 用户唯一ID |
| token | String | 访问令牌 (JWT) |
| refreshToken | String | 刷新令牌 |
| expiresIn | Long | Token有效期(秒)，默认7200秒(2小时) |
| userInfo | Object | 用户信息对象 |

**错误码**:
- `400` - 手机号或验证码格式不正确
- `401` - 验证码错误或已过期
- `500` - 服务器内部错误

---

## 邮箱登录

### 3. 邮箱密码登录

**接口**: `POST /api/auth/email/login`

**描述**: 使用邮箱和密码登录

**请求体**:
```json
{
  "email": "user@example.com",
  "password": "your_password_here"
}
```

**字段说明**:
| 字段 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| email | String | 是 | 邮箱地址 |
| password | String | 是 | 登录密码 (最少6位) |

**响应示例**:
```json
{
  "success": true,
  "message": "登录成功",
  "data": {
    "userId": "user_123456",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "refresh_token_here",
    "expiresIn": 7200,
    "userInfo": {
      "userId": "user_123456",
      "email": "user@example.com",
      "nickname": "用户昵称",
      "avatar": "https://example.com/avatar.png",
      "createdAt": "2025-10-29T10:30:00",
      "loginType": "email"
    }
  }
}
```

**错误码**:
- `400` - 邮箱格式不正确
- `401` - 邮箱或密码错误
- `404` - 用户不存在
- `500` - 服务器内部错误

---

## 第三方登录

### 4. 微信登录

**接口**: `POST /api/auth/wechat/login`

**描述**: 使用微信授权码登录

**请求体**:
```json
{
  "code": "wechat_auth_code_here",
  "userInfo": {
    "nickname": "微信用户",
    "avatarUrl": "https://wx.qlogo.cn/avatar.png",
    "openId": "wechat_open_id"
  }
}
```

**字段说明**:
| 字段 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| code | String | 是 | 微信授权码 |
| userInfo | Object | 否 | 微信用户信息 (首次登录时提供) |
| userInfo.nickname | String | 否 | 微信昵称 |
| userInfo.avatarUrl | String | 否 | 微信头像URL |
| userInfo.openId | String | 否 | 微信OpenID |

**响应示例**:
```json
{
  "success": true,
  "message": "登录成功",
  "data": {
    "userId": "user_123456",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "refresh_token_here",
    "expiresIn": 7200,
    "userInfo": {
      "userId": "user_123456",
      "nickname": "微信用户",
      "avatar": "https://wx.qlogo.cn/avatar.png",
      "createdAt": "2025-10-29T10:30:00",
      "loginType": "wechat"
    }
  }
}
```

**错误码**:
- `400` - 授权码无效
- `401` - 微信授权失败
- `500` - 服务器内部错误

---

### 5. QQ登录

**接口**: `POST /api/auth/qq/login`

**描述**: 使用QQ授权登录

**请求体**:
```json
{
  "accessToken": "qq_access_token_here",
  "openId": "qq_open_id",
  "userInfo": {
    "nickname": "QQ用户",
    "avatarUrl": "https://qlogo.cn/avatar.png"
  }
}
```

**字段说明**:
| 字段 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| accessToken | String | 是 | QQ访问令牌 |
| openId | String | 是 | QQ OpenID |
| userInfo | Object | 否 | QQ用户信息 (首次登录时提供) |
| userInfo.nickname | String | 否 | QQ昵称 |
| userInfo.avatarUrl | String | 否 | QQ头像URL |

**响应示例**:
```json
{
  "success": true,
  "message": "登录成功",
  "data": {
    "userId": "user_123456",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "refresh_token_here",
    "expiresIn": 7200,
    "userInfo": {
      "userId": "user_123456",
      "nickname": "QQ用户",
      "avatar": "https://qlogo.cn/avatar.png",
      "createdAt": "2025-10-29T10:30:00",
      "loginType": "qq"
    }
  }
}
```

**错误码**:
- `400` - AccessToken或OpenID无效
- `401` - QQ授权失败
- `500` - 服务器内部错误

---

## Token管理

### 6. 刷新Token

**接口**: `POST /api/auth/refresh-token`

**描述**: 使用刷新令牌获取新的访问令牌

**请求体**:
```json
{
  "refreshToken": "refresh_token_here"
}
```

**字段说明**:
| 字段 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| refreshToken | String | 是 | 刷新令牌 |

**响应示例**:
```json
{
  "success": true,
  "message": "Token刷新成功",
  "data": {
    "userId": "user_123456",
    "token": "new_access_token_here",
    "refreshToken": "new_refresh_token_here",
    "expiresIn": 7200,
    "userInfo": {
      "userId": "user_123456",
      "nickname": "用户昵称",
      "avatar": "https://example.com/avatar.png",
      "createdAt": "2025-10-29T10:30:00",
      "loginType": "phone"
    }
  }
}
```

**错误码**:
- `401` - RefreshToken无效或已过期
- `500` - 服务器内部错误

---

### 7. 登出

**接口**: `POST /api/auth/logout`

**描述**: 退出登录，使当前Token失效

**请求头**:
```http
Authorization: Bearer {token}
```

**响应示例**:
```json
{
  "success": true,
  "message": "登出成功"
}
```

**错误码**:
- `401` - Token无效或已过期
- `500` - 服务器内部错误

---

## 用户信息

### 8. 获取用户信息

**接口**: `GET /api/user/info`

**描述**: 获取当前登录用户的详细信息

**请求头**:
```http
Authorization: Bearer {token}
```

**响应示例**:
```json
{
  "success": true,
  "message": "获取成功",
  "data": {
    "userId": "user_123456",
    "phone": "13800138000",
    "email": "user@example.com",
    "nickname": "用户昵称",
    "avatar": "https://example.com/avatar.png",
    "createdAt": "2025-10-29T10:30:00",
    "loginType": "phone"
  }
}
```

**字段说明**:
| 字段 | 类型 | 说明 |
|-----|------|------|
| userId | String | 用户唯一ID |
| phone | String | 手机号 (可能为null) |
| email | String | 邮箱 (可能为null) |
| nickname | String | 用户昵称 |
| avatar | String | 头像URL |
| createdAt | String | 注册时间 (ISO 8601格式) |
| loginType | String | 登录方式: phone/email/wechat/qq |

**错误码**:
- `401` - Token无效或已过期
- `404` - 用户不存在
- `500` - 服务器内部错误

---

## 错误码说明

### HTTP状态码

| 状态码 | 说明 |
|--------|------|
| 200 | 请求成功 |
| 400 | 请求参数错误 |
| 401 | 未授权或Token无效 |
| 403 | 禁止访问 |
| 404 | 资源不存在 |
| 429 | 请求过于频繁 |
| 500 | 服务器内部错误 |

### 业务错误码

| 错误码 | 说明 |
|--------|------|
| 1001 | 手机号格式不正确 |
| 1002 | 验证码错误或已过期 |
| 1003 | 邮箱格式不正确 |
| 1004 | 密码格式不正确 |
| 1005 | 用户不存在 |
| 1006 | 密码错误 |
| 1007 | Token无效或已过期 |
| 1008 | RefreshToken无效或已过期 |
| 1009 | 第三方授权失败 |
| 2001 | 短信发送失败 |
| 2002 | 短信发送过于频繁 |

---

## 安全建议

### Token使用

1. **访问令牌(Access Token)**
   - 有效期: 2小时
   - 用途: 访问受保护的API
   - 存储: 内存或安全存储 (不建议LocalStorage)

2. **刷新令牌(Refresh Token)**
   - 有效期: 30天
   - 用途: 刷新访问令牌
   - 存储: 安全存储 (加密)

3. **Token格式**
   ```
   Authorization: Bearer <token>
   ```

### 密码要求

- 最少6位字符
- 建议包含大小写字母、数字和特殊字符
- 不建议使用常见密码

### 验证码

- 6位数字
- 有效期5分钟
- 每个手机号每天最多发送10次
- 同一手机号60秒内只能发送一次

---

## 测试示例

### 使用cURL测试

**发送验证码**:
```bash
curl -X POST http://localhost:8080/api/auth/phone/send-code \
  -H "Content-Type: application/json" \
  -d '{
    "phone": "13800138000",
    "type": "login"
  }'
```

**手机号登录**:
```bash
curl -X POST http://localhost:8080/api/auth/phone/login \
  -H "Content-Type: application/json" \
  -d '{
    "phone": "13800138000",
    "code": "123456"
  }'
```

**获取用户信息**:
```bash
curl -X GET http://localhost:8080/api/user/info \
  -H "Authorization: Bearer your_token_here"
```

---

## 更新日志

### v1.0 (2025-10-29)
- ✅ 实现手机号验证码登录
- ✅ 实现邮箱密码登录
- ✅ 实现微信登录接口
- ✅ 实现QQ登录接口
- ✅ 实现Token刷新机制
- ✅ 实现用户信息查询

---

## 联系方式

如有问题，请联系:
- **邮箱**: support@quickplan.example
- **电话**: 010-12345678
