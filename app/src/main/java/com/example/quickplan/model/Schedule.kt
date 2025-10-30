package com.example.quickplan.model

import java.time.LocalDate
import java.time.LocalTime

/**
 * 表示一个日程的UI模型
 */
data class Schedule(
    // 本地唯一ID，用于在UI中进行识别
    val id: String,
    // 后端数据库中的ID，对于新建的日程可能为空
    val serverId: String?,
    val title: String,
    val date: LocalDate,
    val time: LocalTime,
    val location: String?,
    val description: String?
)
