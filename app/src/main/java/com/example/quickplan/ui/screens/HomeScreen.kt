package com.example.quickplan.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.quickplan.data.repository.ScheduleRepository
import com.example.quickplan.model.Schedule
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

val localDateSaver = Saver<LocalDate, String>(
    save = { it.toString() },
    restore = { LocalDate.parse(it) }
)

val yearMonthSaver = Saver<YearMonth, String>(
    save = { it.toString() },
    restore = { YearMonth.parse(it) }
)

@Composable
fun CalendarGrid(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val firstDayOfMonth = currentMonth.atDay(1)
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfWeek = (firstDayOfMonth.dayOfWeek.value % 7)
    val totalCells = daysInMonth + firstDayOfWeek

    Column {
        for (week in 0 until (totalCells + 6) / 7) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (dayOfWeek in 0..6) {
                    val index = week * 7 + dayOfWeek
                    val day = index - firstDayOfWeek + 1
                    if (day in 1..daysInMonth) {
                        val date = currentMonth.atDay(day)
                        val isToday = date == LocalDate.now()
                        val isSelected = date == selectedDate

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(4.dp)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) { onDateSelected(date) },
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                isSelected -> Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = day.toString(), fontWeight = FontWeight.Bold)
                                }
                                isToday -> Text(
                                    text = day.toString(),
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                else -> Text(text = day.toString())
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, initialDate: String?) {
    val context = LocalContext.current
    val repository = remember { ScheduleRepository.getInstance(context) }
    val scope = rememberCoroutineScope()
    val schedules by repository.schedules.collectAsState()

    var selectedDate by rememberSaveable(stateSaver = localDateSaver) {
        mutableStateOf(LocalDate.now())
    }
    var currentMonth by rememberSaveable(stateSaver = yearMonthSaver) {
        mutableStateOf(YearMonth.from(LocalDate.now()))
    }

    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val scheduleUpdated by savedStateHandle?.getStateFlow("schedule_updated", false)?.collectAsState() ?: mutableStateOf(false)
    val newSelectedDateStr by savedStateHandle?.getStateFlow<String?>("selectedDate", null)?.collectAsState() ?: mutableStateOf(null)

    LaunchedEffect(scheduleUpdated) {
        if (scheduleUpdated) {
            scope.launch {
                repository.refreshSchedules()
            }
            savedStateHandle?.set("schedule_updated", false)
        }
    }

    LaunchedEffect(newSelectedDateStr) {
        newSelectedDateStr?.let { dateString ->
            val newDate = LocalDate.parse(dateString)
            selectedDate = newDate
            currentMonth = YearMonth.from(newDate)
            savedStateHandle?.set<String?>("selectedDate", null)
        }
    }

    LaunchedEffect(initialDate) {
        initialDate?.let { dateString ->
            val newDate = LocalDate.parse(dateString)
            selectedDate = newDate
            currentMonth = YearMonth.from(newDate)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate("addSchedule/${selectedDate}")
                },
                containerColor = MaterialTheme.colorScheme.primary,
                shape = CircleShape,
                modifier = Modifier.size(64.dp),
                elevation = FloatingActionButtonDefaults.elevation(6.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "添加日程", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(32.dp))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) { Icon(Icons.Filled.ArrowBack, contentDescription = "上个月") }
                Text("${currentMonth.year}年${currentMonth.monthValue}月", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) { Icon(Icons.Filled.ArrowForward, contentDescription = "下个月") }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val weekDays = listOf("一","二","三","四","五","六","日")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                weekDays.forEach { day ->
                    Text(day, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            CalendarGrid(currentMonth = currentMonth, selectedDate = selectedDate, onDateSelected = { 
                selectedDate = it
                currentMonth = YearMonth.from(it)
             })

            Spacer(modifier = Modifier.height(16.dp))

            val todaySchedules = schedules.filter { it.date == selectedDate }
            Text("${selectedDate.monthValue}月${selectedDate.dayOfMonth}日 的日程", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))

            if (todaySchedules.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("暂无日程", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(todaySchedules) { schedule ->
                        Surface(
                            tonalElevation = 2.dp,
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    navController.navigate("editSchedule/${schedule.id}")
                                }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(schedule.title, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                                    var showDialog by remember { mutableStateOf(false) }
                                    IconButton(onClick = { showDialog = true }) { Icon(Icons.Filled.Delete, contentDescription = "删除日程", tint = MaterialTheme.colorScheme.error) }

                                    if (showDialog) {
                                        AlertDialog(
                                            onDismissRequest = { showDialog = false },
                                            title = { Text("确认删除该日程？") },
                                            text = { Text("删除后将无法恢复。") },
                                            confirmButton = {
                                                TextButton(onClick = {
                                                    scope.launch { repository.deleteSchedule(schedule) }
                                                    showDialog = false
                                                }) { Text("确认", color = MaterialTheme.colorScheme.error) }
                                            },
                                            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("取消") } }
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Place, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(schedule.location ?: "未填写地点")
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(schedule.time.format(DateTimeFormatter.ofPattern("HH:mm")))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
