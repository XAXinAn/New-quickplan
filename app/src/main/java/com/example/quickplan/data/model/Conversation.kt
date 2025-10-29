package com.example.quickplan.data.model

import java.util.UUID

/**
 * 对话数据模型
 */
data class Conversation(
    val id: String = UUID.randomUUID().toString(),
    val title: String, // 对话标题
    val messages: List<Message> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
