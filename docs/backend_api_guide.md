# QuickPlan 后端 API 接口文档

> **版本**: v1.0  
> **最后更新**: 2025-10-26  
> **客户端**: Android Kotlin + Jetpack Compose  
> **后端要求**: Java Spring Boot (推荐) 或其他支持 REST/SSE 的框架

---

## 📋 目录

1. [技术栈与约定](#1-技术栈与约定)
2. [数据库设计建议](#2-数据库设计建议)
3. [接口清单](#3-接口清单)
4. [AI 对话模块](#4-ai-对话模块)
5. [OCR 识别模块](#5-ocr-识别模块)
6. [日程管理模块](#6-日程管理模块)
7. [错误处理](#7-错误处理)
8. [部署与测试](#8-部署与测试)

---

## 1. 技术栈与约定

### 1.1 推荐技术栈

```
框架: Spring Boot 3.x
语言: Java 17+
数据库: MySQL 8.0 / PostgreSQL 14+
AI集成: OpenAI API / 讯飞星火 / 通义千问
构建工具: Maven / Gradle
```

### 1.2 基础约定

| 项目 | 说明 |
|------|------|
| **Base URL** | `http://localhost:8080/` (开发) / `https://yourdomain.com/` (生产) |
| **字符编码** | UTF-8 |
| **Content-Type** | `application/json` (除 SSE 外) |
| **日期格式** | `yyyy-MM-dd` (例: `2025-10-26`) |
| **时间格式** | `HH:mm` (例: `14:30`) |
| **日期时间格式** | `yyyy-MM-dd'T'HH:mm:ss` (例: `2025-10-26T14:30:00`) |
| **用户标识** | 当前使用固定 `userId = "default_user_001"` |

### 1.3 统一响应结构

**成功响应**:
```json
{
  "success": true,
  "message": "操作成功",
  "data": { ... }
}
```

---

## 2. 数据库设计建议

> 以下为示例字段，可根据实际业务调整。所有表建议添加 `created_at`、`updated_at`、`is_deleted` 软删字段。

### 2.1 用户对话与消息

| 表名 | 字段 | 类型 | 说明 |
|------|------|------|------|
| `conversation` | `id` | varchar(64) | 会话唯一 ID (UUID) |
| | `user_id` | varchar(64) | 用户 ID |
| | `title` | varchar(128) | 会话标题 |
| | `status` | tinyint | 0=正常,1=归档 |

| 表名 | 字段 | 类型 | 说明 |
|------|------|------|------|
| `conversation_message` | `id` | bigint | 自增 |
| | `conversation_id` | varchar(64) | 所属会话 ID |
| | `role` | varchar(16) | `user` / `assistant` |
| | `content` | text | 消息内容 |

### 2.2 OCR 提醒

| 表名 | 字段 | 类型 | 说明 |
|------|------|------|------|
| `ocr_reminder` | `id` | varchar(64) | 提醒 ID |
| | `conversation_id` | varchar(64) | 来源会话 |
| | `title` | varchar(128) | 提醒标题 |
| | `description` | text | 备注 |
| | `remind_time` | datetime | 提醒时间 (可选) |

### 2.3 日程安排

| 表名 | 字段 | 类型 | 说明 |
|------|------|------|------|
| `schedule` | `id` | varchar(64) | 日程 ID |
| | `user_id` | varchar(64) | 用户 ID |
| | `title` | varchar(128) | 日程标题 |
| | `location` | varchar(128) | 地点 |
| | `date` | date | 日期 |
| | `time` | time | 时间 |
| | `description` | text | 备注 |

---

**失败响应**:
```json
{
  "success": false,
  "message": "错误描述",
  "data": null
}
```

## 3. 核心接口一览

| 模块 | 方法/路径 | 说明 |
|------|-----------|------|
| AI 对话 | `POST /api/ai/chat` | 流式返回 AI 回复 (SSE) |
| | `POST /api/ai/chat/new` | 新建对话 |
| | `GET /api/conversation/list/{userId}` | 用户对话列表 |
| | `GET /api/conversation/messages/{conversationId}` | 单个对话消息 |
| | `DELETE /api/conversation/delete/{conversationId}` | 删除对话 |
| OCR | `POST /api/ai/ocr/reminder` | OCR 结果创建提醒 |
| 日程 | `GET /api/schedule/list/{userId}` | 用户全部日程 |
| | `GET /api/schedule/range?userId=&startDate=&endDate=` | 日期范围内日程 |
| | `POST /api/schedule/create` | **新增日程** (新增功能) |
| | `PUT /api/schedule/update` | 更新日程 |
| | `DELETE /api/schedule/delete/{scheduleId}` | 删除日程 |

## 4. 接口详情

### 4.1 AI 对话

#### 🔌 SSE 连接要求
- 响应头需要包含 `Content-Type: text/event-stream`、`Cache-Control: no-cache`、`Connection: keep-alive`
- 每个 chunk 以 `data: xxx\n\n` 形式推送；最后一帧可发送 `data: [DONE]`

#### `POST /api/ai/chat`
- **描述**：收集用户问题并以 `text/event-stream` 推送 AI 回复
- **请求体**
```json
{
  "memoryId": "conversation-uuid",
  "message": "帮我规划明天的行程",
  "userId": "default_user_001",
  "ocrText": null
}
```
- **响应**：SSE，每条消息形如 `data: <chunk>`，客户端负责拼接
- **HTTP 状态**：200 (成功建立连接)，4xx/5xx 为错误
- **失败示例**
```json
{
  "success": false,
  "message": "会话不存在",
  "data": null
}
```

#### `POST /api/ai/chat/new`
```json
// Request
{
  "userId": "default_user_001",
  "title": "新对话",
  "message": null
}

// Response
{
  "success": true,
  "data": {
    "id": "conversation-uuid",
    "userId": "default_user_001",
    "title": "新对话",
    "createdAt": "2025-10-26T12:00:00",
    "updatedAt": "2025-10-26T12:00:00",
    "isDeleted": 0
  },
  "message": "创建成功"
}
```

其余对话列表、消息详情、删除接口均遵循同样的 `success / message / data` 模式。

#### `GET /api/conversation/list/{userId}`
- **说明**：返回用户全部会话，按照更新时间倒序
- **样例响应**：
```json
{
  "success": true,
  "data": [
    {
      "id": "conversation-uuid",
      "userId": "default_user_001",
      "title": "周计划",
      "createdAt": "2025-10-24T09:20:00",
      "updatedAt": "2025-10-25T20:11:00",
      "isDeleted": 0
    }
  ],
  "total": 1
}
```

#### `GET /api/conversation/messages/{conversationId}`
- **说明**：返回指定会话全部消息，按时间升序
- **样例响应**：
```json
{
  "success": true,
  "data": [
    {"id":1,"role":"user","content":"帮我安排明天"},
    {"id":2,"role":"assistant","content":"好的，以下是建议..."}
  ],
  "total": 2
}
```

### 4.2 OCR → 提醒

#### `POST /api/ai/ocr/reminder`
- **描述**：接收 ML Kit 识别的文本，解析并创建提醒
- **请求体**
```json
{
  "memoryId": "conversation-uuid",
  "userId": "default_user_001",
  "ocrText": "1. 明早9点开会\n2. 下午写周报"
}
```
- **响应体**
```json
{
  "success": true,
  "message": "成功创建 2 条提醒",
  "data": {
    "reminderId": "reminder-uuid",
    "title": "下午写周报",
    "time": "2025-10-26T15:00:00",
    "description": "由 OCR 自动生成"
  }
}
```
- **说明**：`data` 部分仅返回最新创建的一条提醒，或根据业务需要返回全部提醒数组。
- **业务建议**：
  - 若同一图片解析出多条提醒，可返回 `data` 数组并附加 `createdCount`
  - 可将 OCR 文本存入 `ocr_text` 表备份，便于追溯

### 4.3 日程管理 (新接口)

客户端现在支持新增日程并展示每日安排，对应的后端接口如下。

#### 数据结构
```json
// ScheduleDto
{
  "id": "schedule-uuid",
  "userId": "default_user_001",
  "title": "团队周会",
  "location": "会议室A",
  "date": "2025-10-27",
  "time": "09:30",
  "description": "带上最新日报",
  "createdAt": "2025-10-25T10:00:00",
  "updatedAt": "2025-10-25T10:00:00",
  "isDeleted": 0
}
```

#### `GET /api/schedule/list/{userId}`
- **说明**：返回用户全部日程，客户端会自行按日期过滤
- **响应**
```json
{
  "success": true,
  "data": [ { ...ScheduleDto }, { ... } ],
  "total": 2,
  "message": null
}
```

#### `GET /api/schedule/range`
- **查询参数**：`userId`、`startDate`、`endDate`
- **用途**：可选，如果需要按月份分页下发日程可实现该接口

#### `POST /api/schedule/create`
- **请求体**
```json
{
  "userId": "default_user_001",
  "title": "和客户开会",
  "location": "腾讯会议",
  "date": "2025-10-28",
  "time": "14:00",
  "description": "准备报价单"
}
```
- **响应体**
```json
{
  "success": true,
  "message": "创建成功",
  "data": { ...ScheduleDto }
}
```
- **注意**：客户端会将 `data.id` 保存为 `serverId`，后续更新/删除需使用该值
- **校验建议**：
  - `title` 非空、`title.length <= 50`
  - `date` 不得早于 1970-01-01；`time` 符合 `HH:mm`

#### `PUT /api/schedule/update`
- **请求体**
```json
{
  "id": "schedule-uuid",
  "userId": "default_user_001",
  "title": "和客户开会",
  "location": "线下会议室",
  "date": "2025-10-28",
  "time": "14:30",
  "description": "改为线下，提前准备设备"
}
```
- **响应体** 与创建接口一致

#### `DELETE /api/schedule/delete/{scheduleId}`
- **响应体**
```json
{
  "success": true,
  "message": "删除成功"
}
```
- **实现建议**：执行软删除 (`is_deleted = 1`)，客户端无需等待 job 同步

## 5. 错误码建议

统一返回 HTTP 200 + `success=false` 表示业务错误，或直接使用对应该情况的 HTTP 状态码。建议保留以下场景：

| 场景 | HTTP | message 示例 |
|------|------|---------------|
| 参数校验失败 | 400 | `时间格式非法` |
| 未找到资源 | 404 | `会话不存在` |
| 权限不足 | 403 | `无权访问该会话` |
| 服务器异常 | 500 | `内部错误，请稍后再试` |

## 6. 开发流程建议

1. 根据本文档实现所有 REST/SSE 接口
2. 使用 Postman 或 curl 验证接口返回结构
3. 将服务部署到可达地址 (内网可用局域网 IP)
4. 修改 Android 项目 `RetrofitClient.BASE_URL`
5. 重新构建并验证：
   - AI 对话能收到流式回复
   - OCR 上传图片后可返回提醒
   - 首页新增日程成功，且刷新后仍可看到

如需扩展身份认证、多人协作、提示词管理等功能，可在以上接口基础上继续迭代。

---

## 7. 部署与测试

1. **本地运行**：`./mvnw spring-boot:run` 或 `./gradlew bootRun`
2. **接口测试**：
  - Postman Collection: 建议创建文件夹 `AI / OCR / Schedule`
  - `curl --no-buffer http://localhost:8080/api/ai/chat` 验证 SSE
3. **生产部署**：
  - 建议使用 Docker + Nginx 反向代理
  - 开启 HTTPS，配置 CORS 允许 Android 客户端域名
4. **日志监控**：
  - 使用 Spring Boot Actuator 暴露健康检查 `/actuator/health`
  - 记录每次 OCR 解析与 AI 回复的异常便于排查
