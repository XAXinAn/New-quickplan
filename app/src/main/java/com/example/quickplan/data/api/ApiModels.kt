package com.example.quickplan.data.api

/**
 * API 请求和响应数据模型
 * 这些类对应后端接口的JSON格式
 */

// ==================== 发送消息相关 ====================

/**
 * 发送消息请求体
 * POST /api/ai/chat
 */
data class ChatRequest(
    val memoryId: String, // 对话ID（必填）
    val message: String, // 用户输入的消息内容
    val userId: String? = null, // 用户ID（可选）
    val ocrText: String? = null // OCR 识别的文本（可选）
)

/**
 * 发送消息响应体（非流式）
 * POST /api/ai/chat 返回
 */
data class ChatResponse(
    val success: Boolean,      // 是否成功
    val message: String,       // AI回复的完整文本或错误信息
    val data: Any? = null      // 预留扩展字段
)

// ==================== OCR 相关 ====================

/**
 * OCR 识别请求
 * POST /api/ai/ocr/reminder
 */
data class OCRReminderRequest(
    val memoryId: String,
    val userId: String,
    val ocrText: String // OCR 识别出的文本
)

/**
 * OCR 识别响应
 */
data class OCRReminderResponse(
    val success: Boolean,
    val message: String,
    val data: ReminderData? = null
)

data class ReminderData(
    val reminderId: String?,
    val title: String,
    val time: String?,
    val description: String?
)

// ==================== 对话历史相关 ====================

/**
 * 获取对话列表响应体
 * GET /api/conversation/list/{userId}
 */
data class ConversationsResponse(
    val success: Boolean,
    val data: List<ConversationSummary>,
    val total: Int
)

/**
 * 对话摘要（用于列表显示）
 */
data class ConversationSummary(
    val id: String,
    val userId: String,
    val title: String,
    val createdAt: String, // LocalDateTime 格式
    val updatedAt: String,
    val isDeleted: Int
)

// ==================== 单个对话详情相关 ====================

/**
 * 获取对话详情响应体
 * GET /api/conversation/detail/{conversationId}
 */
data class ConversationDetailResponse(
    val success: Boolean,
    val data: ConversationDetail
)

data class ConversationDetail(
    val id: String,
    val userId: String,
    val title: String,
    val createdAt: String,
    val updatedAt: String,
    val isDeleted: Int
)

/**
 * 获取对话消息列表响应体
 * GET /api/conversation/messages/{conversationId}
 */
data class ConversationMessagesResponse(
    val success: Boolean,
    val data: List<MessageDto>,
    val total: Int
)

/**
 * 消息DTO（用于网络传输）
 */
data class MessageDto(
    val id: Long,
    val conversationId: String,
    val role: String, // "user" 或 "assistant"
    val content: String,
    val createdAt: String, // LocalDateTime 格式
    val isDeleted: Int
)

// ==================== 创建新对话相关 ====================

/**
 * 创建新对话请求体
 * POST /api/ai/chat/new 或 POST /api/conversation/create
 */
data class CreateConversationRequest(
    val userId: String, // 用户ID
    val title: String? = "新对话", // 对话标题
    val message: String? = null // 首条消息（可选）
)

/**
 * 创建新对话响应体
 */
data class CreateConversationResponse(
    val success: Boolean,
    val data: ConversationData,
    val message: String? = null
)

data class ConversationData(
    val id: String,
    val userId: String,
    val title: String,
    val createdAt: String,
    val updatedAt: String,
    val isDeleted: Int
)

// ==================== 删除对话相关 ====================

/**
 * 删除对话响应体
 * DELETE /api/ai/conversations/{memoryId}
 */
data class DeleteConversationResponse(
    val success: Boolean,
    val message: String
)

// ==================== 日程管理相关 ====================

/**
 * 日程列表响应
 * GET /api/schedule/list/{userId}
 */
data class ScheduleListResponse(
    val success: Boolean,
    val data: List<ScheduleDto>,
    val total: Int,
    val message: String? = null
)

/**
 * 日程数据传输对象
 */
data class ScheduleDto(
    val id: String,
    val userId: String,
    val title: String,
    val location: String?,
    val date: String, // 格式: yyyy-MM-dd
    val time: String, // 格式: HH:mm
    val description: String?,
    val createdAt: String,
    val updatedAt: String,
    val isDeleted: Int
)

/**
 * 创建日程请求
 * POST /api/schedule/create
 */
data class CreateScheduleRequest(
    val userId: String,
    val title: String,
    val location: String?,
    val date: String, // 格式: yyyy-MM-dd
    val time: String, // 格式: HH:mm
    val description: String?
)

/**
 * 更新日程请求
 * PUT /api/schedule/update
 */
data class UpdateScheduleRequest(
    val id: String,
    val userId: String,
    val title: String,
    val location: String?,
    val date: String, // 格式: yyyy-MM-dd
    val time: String, // 格式: HH:mm
    val description: String?
)

/**
 * 日程操作响应
 */
data class ScheduleResponse(
    val success: Boolean,
    val data: ScheduleDto?,
    val message: String
)

/**
 * 删除日程响应
 * DELETE /api/schedule/delete/{scheduleId}
 */
data class DeleteScheduleResponse(
    val success: Boolean,
    val message: String
)

// ==================== 通用错误响应 ====================

/**
 * API错误响应
 */
data class ErrorResponse(
    val error: String,
    val message: String,
    val timestamp: Long
)
