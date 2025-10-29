package com.example.quickplan.data.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

/**
 * AI 对话后端 API 接口定义
 * 
 * ⚠️ 所有后端API调用位置 - 需要修改baseUrl请在 RetrofitClient.kt 中修改
 */
interface AiApiService {
    
    /**
     * 发送聊天消息（非流式响应 - JSON）
     * POST http://localhost:8080/api/ai/chat
     * 
     * 📍 API调用位置 #1: 发送用户消息并获取AI完整回复
     * 
     * 注意：后端返回 application/json 格式，包含完整的AI回复
     */
    @POST("api/ai/chat")
    suspend fun sendMessage(@Body request: ChatRequest): Response<ChatResponse>
    
    /**
     * 获取用户的所有对话列表
     * GET http://localhost:8080/api/conversation/list/{userId}
     * 
     * 📍 API调用位置 #2: 加载对话历史列表
     */
    @GET("api/conversation/list/{userId}")
    suspend fun getConversations(@Path("userId") userId: String): Response<ConversationsResponse>
    
    /**
     * 获取指定对话的详细信息
     * GET http://localhost:8080/api/conversation/detail/{conversationId}
     * 
     * 📍 API调用位置 #3: 加载某个对话的基本信息
     */
    @GET("api/conversation/detail/{conversationId}")
    suspend fun getConversationDetail(@Path("conversationId") conversationId: String): Response<ConversationDetailResponse>
    
    /**
     * 获取对话的消息列表
     * GET http://localhost:8080/api/conversation/messages/{conversationId}?userId={userId}
     * 
     * 📍 API调用位置 #3-2: 加载某个对话的完整消息历史
     */
    @GET("api/conversation/messages/{conversationId}")
    suspend fun getConversationMessages(
        @Path("conversationId") conversationId: String,
        @Query("userId") userId: String
    ): Response<ConversationMessagesResponse>
    
    /**
     * 创建新对话
     * POST http://localhost:8080/api/ai/chat/new
     * 
     * 📍 API调用位置 #4: 用户点击"新建对话"时
     */
    @POST("api/ai/chat/new")
    suspend fun createConversation(@Body request: CreateConversationRequest): Response<CreateConversationResponse>
    
    /**
     * 删除对话
     * DELETE http://localhost:8080/api/conversation/delete/{conversationId}
     * 
     * 📍 API调用位置 #5: 用户删除某个对话时
     */
    @DELETE("api/conversation/delete/{conversationId}")
    suspend fun deleteConversation(@Path("conversationId") conversationId: String): Response<DeleteConversationResponse>
    
    /**
     * OCR 识别并创建提醒
     * POST http://localhost:8080/api/ai/ocr/reminder
     * 
     * 📍 API调用位置 #6: 用户上传图片后,OCR识别文字并自动创建提醒
     */
    @POST("api/ai/ocr/reminder")
    suspend fun createReminderFromOCR(@Body request: OCRReminderRequest): Response<OCRReminderResponse>
    
    // ==================== 日程管理相关 ====================
    
    /**
     * 获取用户的所有日程
     * GET http://localhost:8080/api/schedule/list/{userId}
     * 
     * 📍 API调用位置 #7: 加载用户的日程列表
     */
    @GET("api/schedule/list/{userId}")
    suspend fun getSchedules(@Path("userId") userId: String): Response<ScheduleListResponse>
    
    /**
     * 创建新日程
     * POST http://localhost:8080/api/schedule/create
     * 
     * 📍 API调用位置 #8: 用户创建新日程
     */
    @POST("api/schedule/create")
    suspend fun createSchedule(@Body request: CreateScheduleRequest): Response<ScheduleResponse>
    
    /**
     * 更新日程
     * PUT http://localhost:8080/api/schedule/update
     * 
     * 📍 API调用位置 #9: 用户编辑日程
     */
    @PUT("api/schedule/update")
    suspend fun updateSchedule(@Body request: UpdateScheduleRequest): Response<ScheduleResponse>
    
    /**
     * 删除日程
     * DELETE http://localhost:8080/api/schedule/delete/{scheduleId}
     * 
     * 📍 API调用位置 #10: 用户删除日程
     */
    @DELETE("api/schedule/delete/{scheduleId}")
    suspend fun deleteSchedule(@Path("scheduleId") scheduleId: String): Response<DeleteScheduleResponse>
    
    /**
     * 获取指定日期范围的日程
     * GET http://localhost:8080/api/schedule/range
     * 
     * 📍 API调用位置 #11: 加载日历视图中某个月份的日程
     */
    @GET("api/schedule/range")
    suspend fun getSchedulesByDateRange(
        @Query("userId") userId: String,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String
    ): Response<ScheduleListResponse>
    
    /**
     * 获取指定日期的日程
     * GET http://localhost:8080/api/schedule/date
     * 
     * 📍 API调用位置 #12: 加载某一天的日程
     */
    @GET("api/schedule/date")
    suspend fun getSchedulesByDate(
        @Query("userId") userId: String,
        @Query("date") date: String
    ): Response<ScheduleListResponse>
    
    /**
     * 获取日程详情
     * GET http://localhost:8080/api/schedule/detail/{scheduleId}
     * 
     * 📍 API调用位置 #13: 查看单个日程的详细信息
     */
    @GET("api/schedule/detail/{scheduleId}")
    suspend fun getScheduleDetail(@Path("scheduleId") scheduleId: String): Response<ScheduleResponse>
}
