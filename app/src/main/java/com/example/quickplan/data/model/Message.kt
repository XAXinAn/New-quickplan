package com.example.quickplan.data.model

import java.util.UUID

/**
 * 消息数据模型
 */
data class Message(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val isUser: Boolean, // true: 用户消息, false: AI回复
    val timestamp: Long = System.currentTimeMillis()
)
