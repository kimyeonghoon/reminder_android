package com.reminder.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.reminder.recurrence.RecurrenceEnd
import com.reminder.recurrence.RecurrenceRule
import com.reminder.recurrence.RecurrenceType
import com.reminder.ui.components.RecurrencePreview
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * v1.35.0: 반복 설정 화면
 *
 * 반복 규칙과 종료 조건을 설정하는 전체 화면
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurrenceSettingScreen(
    initialRule: RecurrenceRule? = null,
    initialEnd: RecurrenceEnd? = null,
    onSave: (RecurrenceRule?, RecurrenceEnd?) -> Unit,
    onCancel: () -> Unit
) {
    var recurrenceType by remember { mutableStateOf(initialRule?.type ?: RecurrenceType.DAILY) }
    var interval by remember { mutableStateOf(initialRule?.interval ?: 1) }
    var selectedDaysOfWeek by remember { mutableStateOf(initialRule?.daysOfWeek ?: emptySet()) }
    var dayOfMonth by remember { mutableStateOf(initialRule?.dayOfMonth ?: 1) }
    var endType by remember { mutableStateOf(
        when (initialEnd) {
            is RecurrenceEnd.Never -> "never"
            is RecurrenceEnd.AfterOccurrences -> "after"
            is RecurrenceEnd.OnDate -> "on_date"
            else -> "never"
        }
    ) }
    var afterCount by remember { mutableStateOf((initialEnd as? RecurrenceEnd.AfterOccurrences)?.count ?: 10) }
    var endDate by remember { mutableStateOf((initialEnd as? RecurrenceEnd.OnDate)?.date ?: LocalDate.now().plusMonths(1)) }

    var isEnabled by remember { mutableStateOf(initialRule != null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("반복 설정") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.ArrowBack, "뒤로")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (isEnabled) {
                                val rule = when (recurrenceType) {
                                    RecurrenceType.DAILY -> RecurrenceRule(
                                        type = RecurrenceType.DAILY,
                                        interval = interval
                                    )
                                    RecurrenceType.WEEKLY -> RecurrenceRule(
                                        type = RecurrenceType.WEEKLY,
                                        interval = interval,
                                        daysOfWeek = selectedDaysOfWeek.takeIf { it.isNotEmpty() }
                                    )
                                    RecurrenceType.MONTHLY -> RecurrenceRule(
                                        type = RecurrenceType.MONTHLY,
                                        interval = interval,
                                        dayOfMonth = dayOfMonth
                                    )
                                    RecurrenceType.YEARLY -> RecurrenceRule(
                                        type = RecurrenceType.YEARLY,
                                        interval = interval
                                    )
                                    RecurrenceType.CUSTOM -> RecurrenceRule(
                                        type = RecurrenceType.CUSTOM,
                                        interval = interval
                                    )
                                }

                                val end = when (endType) {
                                    "never" -> RecurrenceEnd.Never
                                    "after" -> RecurrenceEnd.AfterOccurrences(afterCount)
                                    "on_date" -> RecurrenceEnd.OnDate(endDate)
                                    else -> RecurrenceEnd.Never
                                }

                                onSave(rule, end)
                            } else {
                                onSave(null, null)
                            }
                        }
                    ) {
                        Icon(Icons.Default.Check, "저장")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 반복 활성화/비활성화
            item {
                Card {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "반복 사용",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Switch(
                            checked = isEnabled,
                            onCheckedChange = { isEnabled = it }
                        )
                    }
                }
            }

            if (isEnabled) {
                // 반복 타입 선택
                item {
                    Text("반복 패턴", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "매일" to RecurrenceType.DAILY,
                            "매주" to RecurrenceType.WEEKLY,
                            "매월" to RecurrenceType.MONTHLY,
                            "매년" to RecurrenceType.YEARLY
                        ).forEach { (label, type) ->
                            FilterChip(
                                selected = recurrenceType == type,
                                onClick = { recurrenceType = type },
                                label = { Text(label) }
                            )
                        }
                    }
                }

                // 간격 설정
                item {
                    OutlinedTextField(
                        value = interval.toString(),
                        onValueChange = {
                            interval = it.toIntOrNull()?.coerceAtLeast(1) ?: 1
                        },
                        label = {
                            Text(
                                when (recurrenceType) {
                                    RecurrenceType.DAILY -> "일 간격"
                                    RecurrenceType.WEEKLY -> "주 간격"
                                    RecurrenceType.MONTHLY -> "개월 간격"
                                    RecurrenceType.YEARLY -> "년 간격"
                                    else -> "간격"
                                }
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // 주간 반복 - 요일 선택
                if (recurrenceType == RecurrenceType.WEEKLY) {
                    item {
                        Text("요일 선택", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            DayOfWeek.values().forEach { day ->
                                val dayName = when (day) {
                                    DayOfWeek.MONDAY -> "월요일"
                                    DayOfWeek.TUESDAY -> "화요일"
                                    DayOfWeek.WEDNESDAY -> "수요일"
                                    DayOfWeek.THURSDAY -> "목요일"
                                    DayOfWeek.FRIDAY -> "금요일"
                                    DayOfWeek.SATURDAY -> "토요일"
                                    DayOfWeek.SUNDAY -> "일요일"
                                }
                                FilterChip(
                                    selected = selectedDaysOfWeek.contains(day),
                                    onClick = {
                                        selectedDaysOfWeek = if (selectedDaysOfWeek.contains(day)) {
                                            selectedDaysOfWeek - day
                                        } else {
                                            selectedDaysOfWeek + day
                                        }
                                    },
                                    label = { Text(dayName) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                // 월간 반복 - 날짜 선택
                if (recurrenceType == RecurrenceType.MONTHLY) {
                    item {
                        OutlinedTextField(
                            value = dayOfMonth.toString(),
                            onValueChange = {
                                dayOfMonth = it.toIntOrNull()?.coerceIn(1, 31) ?: 1
                            },
                            label = { Text("매월 날짜") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            supportingText = { Text("1~31 사이의 숫자를 입력하세요") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // 종료 조건
                item {
                    Text("종료 조건", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            "종료 없음" to "never",
                            "N회 후 종료" to "after",
                            "날짜 지정" to "on_date"
                        ).forEach { (label, type) ->
                            FilterChip(
                                selected = endType == type,
                                onClick = { endType = type },
                                label = { Text(label) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // N회 후 종료 - 횟수 입력
                if (endType == "after") {
                    item {
                        OutlinedTextField(
                            value = afterCount.toString(),
                            onValueChange = {
                                afterCount = it.toIntOrNull()?.coerceAtLeast(1) ?: 10
                            },
                            label = { Text("반복 횟수") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // 날짜 지정 - 종료 날짜 선택
                if (endType == "on_date") {
                    item {
                        // TODO: DatePicker integration
                        OutlinedTextField(
                            value = endDate.toString(),
                            onValueChange = { },
                            label = { Text("종료 날짜") },
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "날짜 선택 기능은 추후 추가 예정",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // 미리보기
                item {
                    val previewRule = when (recurrenceType) {
                        RecurrenceType.DAILY -> RecurrenceRule(RecurrenceType.DAILY, interval)
                        RecurrenceType.WEEKLY -> RecurrenceRule(
                            type = RecurrenceType.WEEKLY,
                            interval = interval,
                            daysOfWeek = selectedDaysOfWeek.takeIf { it.isNotEmpty() }
                        )
                        RecurrenceType.MONTHLY -> RecurrenceRule(
                            type = RecurrenceType.MONTHLY,
                            interval = interval,
                            dayOfMonth = dayOfMonth
                        )
                        RecurrenceType.YEARLY -> RecurrenceRule(RecurrenceType.YEARLY, interval)
                        else -> RecurrenceRule(RecurrenceType.CUSTOM, interval)
                    }

                    val previewEnd = when (endType) {
                        "never" -> RecurrenceEnd.Never
                        "after" -> RecurrenceEnd.AfterOccurrences(afterCount)
                        "on_date" -> RecurrenceEnd.OnDate(endDate)
                        else -> RecurrenceEnd.Never
                    }

                    RecurrencePreview(
                        recurrenceRule = previewRule,
                        recurrenceEnd = previewEnd,
                        startDate = LocalDate.now()
                    )
                }
            }
        }
    }
}
