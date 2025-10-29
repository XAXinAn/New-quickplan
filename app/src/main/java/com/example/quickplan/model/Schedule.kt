package com.example.quickplan.model

import java.time.LocalDate
import java.time.LocalTime

/**
 * 应用内使用的日程模型。
 * - id: 本地唯一标识，用于界面列表操作。
 * - serverId: 后端返回的唯一标识，用于接口调用。
 */
data class Schedule(
    val id: String,
    val serverId: String?,
    val title: String,
    val date: LocalDate,
    val time: LocalTime,
    val location: String?,
    val description: String?
)
