package com.example.quickplan.data.repository

import android.content.Context
import com.example.quickplan.data.api.CreateScheduleRequest
import com.example.quickplan.data.api.ScheduleDto
import com.example.quickplan.data.api.ScheduleListResponse
import com.example.quickplan.data.api.ScheduleResponse
import com.example.quickplan.data.api.UpdateScheduleRequest
import com.example.quickplan.data.api.DeleteScheduleResponse
import com.example.quickplan.data.api.AiApiService
import com.example.quickplan.data.api.RetrofitClient
import com.example.quickplan.data.local.UserPreferences
import com.example.quickplan.model.Schedule
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import retrofit2.Response

/**
 * 日程数据仓库：负责与后端同步日程信息，并在内存中缓存。
 * 使用单例模式确保整个应用共享同一个缓存
 */
class ScheduleRepository private constructor(
    private val apiService: AiApiService,
    private val userPreferences: UserPreferences
) {

    private val _schedules = MutableStateFlow<List<Schedule>>(emptyList())
    val schedules: StateFlow<List<Schedule>> = _schedules.asStateFlow()

    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    // 后端期望的时间格式是 HH:mm:ss，但前端显示和输入使用 HH:mm
    private val timeFormatterWithSeconds: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    private val timeFormatterWithoutSeconds: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    
    private fun getCurrentUserId(): String {
        return userPreferences.getUserInfo()?.userId ?: DEFAULT_USER_ID
    }
    
    /**
     * 清除本地缓存（用于调试或强制刷新）
     */
    fun clearCache() {
        _schedules.value = emptyList()
    }

    suspend fun refreshSchedules(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val userId = getCurrentUserId()
            android.util.Log.d("ScheduleRepository", "开始刷新日程，userId: $userId")
            val response = apiService.getSchedules(userId)
            android.util.Log.d("ScheduleRepository", "API响应: ${response.isSuccessful}, code: ${response.code()}")
            handleScheduleListResponse(response)
            android.util.Log.d("ScheduleRepository", "刷新完成，当前日程数: ${_schedules.value.size}")
            Unit
        }
    }

    suspend fun addSchedule(
        title: String,
        date: LocalDate,
        time: LocalTime,
        location: String?,
        description: String?
    ): Result<Schedule> = withContext(Dispatchers.IO) {
        runCatching {
            val request = CreateScheduleRequest(
                userId = getCurrentUserId(),
                title = title,
                location = location,
                date = date.format(dateFormatter),
                time = time.format(timeFormatterWithSeconds), // 使用带秒的格式发送给后端
                description = description
            )

            val response = apiService.createSchedule(request)
            handleScheduleResponse(response)
        }
    }

    suspend fun updateSchedule(schedule: Schedule): Result<Schedule> = withContext(Dispatchers.IO) {
        runCatching {
            val request = UpdateScheduleRequest(
                id = schedule.serverId ?: schedule.id,
                userId = getCurrentUserId(),
                title = schedule.title,
                location = schedule.location,
                date = schedule.date.format(dateFormatter),
                time = schedule.time.format(timeFormatterWithSeconds), // 使用带秒的格式发送给后端
                description = schedule.description
            )

            val response = apiService.updateSchedule(request)
            val updated = handleScheduleResponse(response)
            updateLocalCache(updated)
            updated
        }
    }

    suspend fun deleteSchedule(schedule: Schedule): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val response = apiService.deleteSchedule(schedule.serverId ?: schedule.id)
            handleDeleteResponse(response)
            _schedules.value = _schedules.value.filterNot { it.id == schedule.id }
        }
    }

    /**
     * 获取指定日期的日程
     * @param date 日期
     * @return 该日期的所有日程列表
     */
    suspend fun getSchedulesByDate(date: LocalDate): Result<List<Schedule>> = withContext(Dispatchers.IO) {
        runCatching {
            val response = apiService.getSchedulesByDate(
                userId = getCurrentUserId(),
                date = date.format(dateFormatter)
            )
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true) {
                    body.data.map { it.toModel() }
                } else {
                    throw IllegalStateException(body?.message ?: "获取日程失败")
                }
            } else {
                throw IllegalStateException("HTTP ${response.code()} ${response.message()}")
            }
        }
    }

    /**
     * 获取日期范围内的日程
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 该日期范围内的所有日程列表
     */
    suspend fun getSchedulesByDateRange(
        startDate: LocalDate,
        endDate: LocalDate
    ): Result<List<Schedule>> = withContext(Dispatchers.IO) {
        runCatching {
            val response = apiService.getSchedulesByDateRange(
                userId = getCurrentUserId(),
                startDate = startDate.format(dateFormatter),
                endDate = endDate.format(dateFormatter)
            )
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true) {
                    body.data.map { it.toModel() }
                } else {
                    throw IllegalStateException(body?.message ?: "获取日程失败")
                }
            } else {
                throw IllegalStateException("HTTP ${response.code()} ${response.message()}")
            }
        }
    }

    /**
     * 获取单个日程的详细信息
     * @param scheduleId 日程ID
     * @return 日程详情
     */
    suspend fun getScheduleDetail(scheduleId: String): Result<Schedule> = withContext(Dispatchers.IO) {
        runCatching {
            val response = apiService.getScheduleDetail(scheduleId)
            handleScheduleResponse(response)
        }
    }

    private fun updateLocalCache(schedule: Schedule) {
        val current = _schedules.value.toMutableList()
        val existingIndex = current.indexOfFirst { it.id == schedule.id }
        if (existingIndex >= 0) {
            current[existingIndex] = schedule
        } else {
            current.add(schedule)
        }
        _schedules.value = current.sortedBy { it.date }
    }

    private fun handleScheduleListResponse(response: Response<ScheduleListResponse>) {
        if (response.isSuccessful) {
            val body = response.body()
            if (body?.success == true) {
                _schedules.value = body.data.map { it.toModel() }.sortedBy { it.date }
            } else {
                throw IllegalStateException(body?.message ?: "加载日程失败")
            }
        } else {
            throw IllegalStateException("HTTP ${response.code()} ${response.message()}")
        }
    }

    private fun handleScheduleResponse(response: Response<ScheduleResponse>): Schedule {
        if (response.isSuccessful) {
            val body = response.body()
            if (body?.success == true && body.data != null) {
                return body.data.toModel()
            } else {
                throw IllegalStateException(body?.message ?: "日程操作失败")
            }
        } else {
            throw IllegalStateException("HTTP ${response.code()} ${response.message()}")
        }
    }

    private fun handleDeleteResponse(response: Response<DeleteScheduleResponse>) {
        if (response.isSuccessful) {
            val body = response.body()
            if (body?.success != true) {
                throw IllegalStateException(body?.message ?: "删除失败")
            }
        } else {
            throw IllegalStateException("HTTP ${response.code()} ${response.message()}")
        }
    }

    private fun ScheduleDto.toModel(): Schedule {
        if (id.isBlank()) {
            throw IllegalStateException("服务器返回的日程ID为空，无法创建日程。")
        }
        // 后端返回的时间可能是 HH:mm:ss 或 HH:mm 格式，需要兼容处理
        val parsedTime = try {
            LocalTime.parse(time, timeFormatterWithSeconds)
        } catch (e: Exception) {
            try {
                LocalTime.parse(time, timeFormatterWithoutSeconds)
            } catch (e: Exception) {
                LocalTime.parse(time) // 使用默认解析器
            }
        }
        
        return Schedule(
            id = id, // 直接使用服务器返回的ID
            serverId = id,
            title = title,
            date = LocalDate.parse(date, dateFormatter),
            time = parsedTime,
            location = location,
            description = description
        )
    }

    companion object {
        private const val DEFAULT_USER_ID = "default_user_001"
        
        @Volatile
        private var INSTANCE: ScheduleRepository? = null
        
        /**
         * 获取单例实例
         */
        fun getInstance(context: Context): ScheduleRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ScheduleRepository(
                    apiService = RetrofitClient.aiApiService,
                    userPreferences = UserPreferences(context.applicationContext)
                ).also { INSTANCE = it }
            }
        }
    }
}
