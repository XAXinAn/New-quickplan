package com.example.quickplan.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.quickplan.data.local.UserPreferences
import com.example.quickplan.data.repository.ScheduleRepository
import com.example.quickplan.model.Schedule
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.launch

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scheduleRepository = remember { ScheduleRepository.getInstance(context) }
    
    // 从 repository 的 StateFlow 获取缓存的日程数据
    val allSchedules by scheduleRepository.schedules.collectAsState()
    
    val today = remember { LocalDate.now() }
    var selectedDate by remember { mutableStateOf(today) }
    var showAddDialog by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var dialogError by remember { mutableStateOf<String?>(null) }
    var isLoadingSchedules by remember { mutableStateOf(false) }
    val startMonth = remember { YearMonth.now().minusMonths(12) }
    val endMonth = remember { YearMonth.now().plusMonths(12) }
    val firstDayOfWeek = remember { DayOfWeek.MONDAY }
    val coroutineScope = rememberCoroutineScope()
    val calendarState = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = YearMonth.now(),
        firstDayOfWeek = firstDayOfWeek
    )
    
    // 初始加载
    LaunchedEffect(Unit) {
        calendarState.scrollToMonth(YearMonth.now())
        isLoadingSchedules = true
        scheduleRepository.refreshSchedules()
            .onSuccess { isLoadingSchedules = false }
            .onFailure { 
                isLoadingSchedules = false
                dialogError = it.message 
            }
    }
    
    // 监听生命周期，当页面重新可见时刷新数据
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // 页面重新可见时刷新
                coroutineScope.launch {
                    isLoadingSchedules = true
                    scheduleRepository.refreshSchedules()
                        .onSuccess { isLoadingSchedules = false }
                        .onFailure { 
                            isLoadingSchedules = false
                            dialogError = it.message 
                        }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    // 从缓存中过滤当前月份的日程（不再重复请求后端）
    val currentMonth = calendarState.firstVisibleMonth.yearMonth
    val monthSchedules = remember(allSchedules, currentMonth) {
        val startDate = currentMonth.atDay(1)
        val endDate = currentMonth.atEndOfMonth()
        val filtered = allSchedules.filter { it.date in startDate..endDate }
        android.util.Log.d("HomeScreen", "当前月份: $currentMonth, 总日程数: ${allSchedules.size}, 当月日程数: ${filtered.size}")
        filtered
    }
    
    // 日志输出所有日程
    LaunchedEffect(allSchedules) {
        android.util.Log.d("HomeScreen", "日程更新，总数: ${allSchedules.size}")
        allSchedules.forEach {
            android.util.Log.d("HomeScreen", "  - ${it.title} (${it.date})")
        }
    }
    
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "添加日程")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
            MonthSwitcher(currentMonth = currentMonth, onPrevious = { coroutineScope.launch { calendarState.animateScrollToMonth(currentMonth.minusMonths(1)) } }, onNext = { coroutineScope.launch { calendarState.animateScrollToMonth(currentMonth.plusMonths(1)) } })
            Spacer(modifier = Modifier.height(8.dp))
            WeekHeader()
            HorizontalCalendar(state = calendarState, dayContent = { day -> DayCell(day = day, isSelected = day.date == selectedDate, isToday = day.date == today, onClick = { selectedDate = day.date }) })
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "${selectedDate.year}年${selectedDate.monthValue}月${selectedDate.dayOfMonth}日的日程", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            DayScheduleList(schedules = monthSchedules, selectedDate = selectedDate)
        }
    }
    if (showAddDialog) {
        AddScheduleDialog(
            selectedDate = selectedDate,
            isSaving = isSaving,
            errorMessage = dialogError,
            onDismiss = {
                if (!isSaving) {
                    dialogError = null
                    showAddDialog = false
                }
            },
            onConfirm = { title, time, location, description ->
                if (title.isBlank()) {
                    dialogError = "标题不能为空"
                    return@AddScheduleDialog
                }
                coroutineScope.launch {
                    isSaving = true
                    dialogError = null
                    scheduleRepository.addSchedule(
                        title = title,
                        date = selectedDate,
                        time = time,
                        location = location.ifBlank { null },
                        description = description.ifBlank { null }
                    ).onSuccess {
                        // addSchedule 已经更新了缓存,不需要手动更新
                        isSaving = false
                        showAddDialog = false
                    }.onFailure {
                        isSaving = false
                        dialogError = it.message ?: "创建失败"
                    }
                }
            }
        )
    }
}

@Composable
private fun MonthSwitcher(currentMonth: YearMonth, onPrevious: () -> Unit, onNext: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Button(onClick = onPrevious, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) { Text("上一月") }
        Text(text = "${currentMonth.year}年 ${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.CHINA)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Button(onClick = onNext, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) { Text("下一月") }
    }
}

@Composable
private fun WeekHeader() {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        val weekLabels = listOf("一", "二", "三", "四", "五", "六", "日")
        weekLabels.forEachIndexed { index, label ->
            Text(text = label, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.SemiBold, color = if (index >= 5) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground)
        }
    }
}

@Composable
private fun DayScheduleList(schedules: List<Schedule>, selectedDate: LocalDate) {
    val daySchedules = remember(schedules, selectedDate) {
        schedules.filter { it.date == selectedDate }.sortedBy { it.time }
    }
    if (daySchedules.isEmpty()) {
        Text(text = "暂无日程", color = MaterialTheme.colorScheme.outline)
    } else {
        Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            daySchedules.forEach { schedule -> ScheduleCard(schedule = schedule) }
        }
    }
}

@Composable
private fun ScheduleCard(schedule: Schedule) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = schedule.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "时间：${schedule.time}")
            schedule.location?.let { Spacer(modifier = Modifier.height(4.dp)); Text(text = "地点：$it") }
            schedule.description?.let { Spacer(modifier = Modifier.height(4.dp)); Text(text = "备注：$it") }
        }
    }
}

@Composable
private fun AddScheduleDialog(selectedDate: LocalDate, isSaving: Boolean, errorMessage: String?, onDismiss: () -> Unit, onConfirm: (String, LocalTime, String, String) -> Unit) {
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    var title by remember { mutableStateOf("") }
    var timeText by remember { mutableStateOf(LocalTime.now().withSecond(0).withNano(0).format(timeFormatter)) }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }
    AlertDialog(onDismissRequest = { if (!isSaving) onDismiss() }, title = { Text(text = "添加日程") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = "日期：${selectedDate.year}-${selectedDate.monthValue}-${selectedDate.dayOfMonth}")
            OutlinedTextField(value = title, onValueChange = { title = it; localError = null }, label = { Text("标题") }, singleLine = true, enabled = !isSaving)
            OutlinedTextField(value = timeText, onValueChange = { timeText = it; localError = null }, label = { Text("时间 (HH:mm)") }, singleLine = true, enabled = !isSaving)
            OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("地点 (可选)") }, singleLine = true, enabled = !isSaving)
            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("备注 (可选)") }, enabled = !isSaving)
            val combinedError = localError ?: errorMessage
            if (!combinedError.isNullOrBlank()) { Text(text = combinedError, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
        }
    }, confirmButton = {
        TextButton(onClick = { 
            val cleanedTime = timeText.trim().substringBefore(':').padStart(2, '0') + ":" + 
                             timeText.trim().substringAfter(':').substringBefore(':').padStart(2, '0')
            runCatching { 
                LocalTime.parse(cleanedTime, timeFormatter) 
            }.onSuccess { parsedTime -> 
                localError = null
                timeText = cleanedTime // 更新为清理后的时间
                onConfirm(title, parsedTime, location, description) 
            }.onFailure { e -> 
                localError = "时间格式错误，请输入 HH:mm 格式（例如：15:30）"
            } 
        }, enabled = !isSaving && title.isNotBlank()) {
            Text(if (isSaving) "保存中..." else "保存")
        }
    }, dismissButton = { TextButton(onClick = onDismiss, enabled = !isSaving) { Text("取消") } })
}

@Composable
private fun DayCell(day: CalendarDay, isSelected: Boolean, isToday: Boolean, onClick: () -> Unit) {
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        day.position != DayPosition.MonthDate -> Color.Transparent
        else -> Color(0xFFF3F3F3)
    }
    Box(modifier = Modifier.aspectRatio(1f).padding(2.dp).background(backgroundColor, RoundedCornerShape(8.dp)).clickable(enabled = day.position == DayPosition.MonthDate, onClick = onClick), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(text = day.date.dayOfMonth.toString(), fontSize = 16.sp, color = when {
                isSelected -> Color.White
                day.position != DayPosition.MonthDate -> Color.Gray
                else -> Color.Black
            })
            if (isToday) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(if (isSelected) Color.White else MaterialTheme.colorScheme.primary))
            }
        }
    }
}
