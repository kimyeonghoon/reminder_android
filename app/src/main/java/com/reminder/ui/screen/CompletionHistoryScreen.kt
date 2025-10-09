package com.reminder.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.reminder.data.entity.ReminderEntity
import com.reminder.viewmodel.ReminderViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompletionHistoryScreen(
    viewModel: ReminderViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentYearMonth by remember { mutableStateOf(YearMonth.now()) }
    var completionCounts by remember { mutableStateOf<Map<LocalDateTime, Int>>(emptyMap()) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedDayReminders by remember { mutableStateOf<List<ReminderEntity>>(emptyList()) }

    // 현재 월의 완료 개수 로드
    LaunchedEffect(currentYearMonth) {
        val startOfMonth = currentYearMonth.atDay(1).atStartOfDay()
        val endOfMonth = currentYearMonth.atEndOfMonth().atTime(23, 59, 59)
        completionCounts = viewModel.getCompletionCountByDay(startOfMonth, endOfMonth)
    }

    // 선택한 날짜의 완료된 리마인더 로드
    LaunchedEffect(selectedDate) {
        selectedDate?.let { date ->
            selectedDayReminders = viewModel.getCompletedRemindersByDate(date.atStartOfDay())
        } ?: run {
            selectedDayReminders = emptyList()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("완료 이력") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 월 선택 헤더
            MonthSelector(
                yearMonth = currentYearMonth,
                onPreviousMonth = { currentYearMonth = currentYearMonth.minusMonths(1) },
                onNextMonth = { currentYearMonth = currentYearMonth.plusMonths(1) }
            )

            // 달력
            CalendarGrid(
                yearMonth = currentYearMonth,
                completionCounts = completionCounts,
                selectedDate = selectedDate,
                onDateClick = { date ->
                    selectedDate = if (selectedDate == date) null else date
                }
            )

            // 선택한 날짜의 리마인더 목록
            if (selectedDate != null && selectedDayReminders.isNotEmpty()) {
                Divider()

                Text(
                    text = "${selectedDate!!.format(DateTimeFormatter.ofPattern("M월 d일"))} 완료 목록",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(selectedDayReminders) { reminder ->
                        CompletedReminderItem(reminder)
                    }
                }
            }
        }
    }
}

@Composable
fun MonthSelector(
    yearMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "이전 달")
        }

        Text(
            text = yearMonth.format(DateTimeFormatter.ofPattern("yyyy년 M월")),
            style = MaterialTheme.typography.titleLarge
        )

        IconButton(onClick = onNextMonth) {
            Icon(Icons.Default.ChevronRight, contentDescription = "다음 달")
        }
    }
}

@Composable
fun CalendarGrid(
    yearMonth: YearMonth,
    completionCounts: Map<LocalDateTime, Int>,
    selectedDate: LocalDate?,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // 요일 헤더
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val daysOfWeek = listOf("일", "월", "화", "수", "목", "금", "토")
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 날짜 그리드
        val firstDayOfMonth = yearMonth.atDay(1)
        val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // 0 = Sunday
        val daysInMonth = yearMonth.lengthOfMonth()

        var currentDay = 1
        var hasMoreDays = true

        // 주 단위로 표시 (최대 6주)
        for (week in 0 until 6) {
            if (!hasMoreDays) break

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (dayOfWeek in 0 until 7) {
                    if (week == 0 && dayOfWeek < firstDayOfWeek) {
                        // 이전 달의 빈 칸
                        Spacer(modifier = Modifier.weight(1f))
                    } else if (currentDay > daysInMonth) {
                        // 다음 달의 빈 칸
                        Spacer(modifier = Modifier.weight(1f))
                        hasMoreDays = false
                    } else {
                        // 현재 달의 날짜
                        val date = yearMonth.atDay(currentDay)
                        val completionCount = completionCounts[date.atStartOfDay()] ?: 0

                        DayCell(
                            day = currentDay,
                            completionCount = completionCount,
                            isSelected = date == selectedDate,
                            isToday = date == LocalDate.now(),
                            onClick = { onDateClick(date) },
                            modifier = Modifier.weight(1f)
                        )

                        currentDay++
                    }
                }
            }
        }
    }
}

@Composable
fun DayCell(
    day: Int,
    completionCount: Int,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .background(
                color = when {
                    isSelected -> MaterialTheme.colorScheme.primaryContainer
                    isToday -> MaterialTheme.colorScheme.tertiaryContainer
                    else -> MaterialTheme.colorScheme.surface
                },
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = when {
                    isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                    isToday -> MaterialTheme.colorScheme.onTertiaryContainer
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )

            if (completionCount > 0) {
                Text(
                    text = completionCount.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun CompletedReminderItem(
    reminder: ReminderEntity,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = reminder.title,
                style = MaterialTheme.typography.bodyLarge,
                textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
            )

            if (reminder.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = reminder.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "완료: ${reminder.updatedAt.format(DateTimeFormatter.ofPattern("HH:mm"))}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
