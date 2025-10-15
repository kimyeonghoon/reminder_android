package com.reminder.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.reminder.recurrence.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * v1.65.0: RecurrenceRule 기반 반복 설정 UI
 *
 * RecurrenceRule과 RecurrenceEnd를 설정할 수 있는 UI 컴포넌트
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurrenceSelector(
    recurrenceRule: RecurrenceRule?,
    onRecurrenceRuleChange: (RecurrenceRule?) -> Unit,
    recurrenceEnd: RecurrenceEnd,
    onRecurrenceEndChange: (RecurrenceEnd) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var showEndDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        // 1. 반복 타입 선택
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = recurrenceRule?.type?.toKorean() ?: "반복 없음",
                onValueChange = {},
                readOnly = true,
                label = { Text("반복") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("반복 없음") },
                    onClick = {
                        onRecurrenceRuleChange(null)
                        expanded = false
                    }
                )
                RecurrenceType.entries.filter { it != RecurrenceType.CUSTOM }.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type.toKorean()) },
                        onClick = {
                            onRecurrenceRuleChange(RecurrenceRule(type = type))
                            expanded = false
                        }
                    )
                }
            }
        }

        // 2. 반복 규칙이 설정되면 추가 옵션 표시
        recurrenceRule?.let { rule ->
            Spacer(modifier = Modifier.height(16.dp))

            // interval 설정
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("${rule.interval}${rule.type.toIntervalUnit()}마다")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = {
                            if (rule.interval > 1) {
                                onRecurrenceRuleChange(rule.copy(interval = rule.interval - 1))
                            }
                        },
                        enabled = rule.interval > 1
                    ) {
                        Text("-")
                    }
                    IconButton(
                        onClick = {
                            onRecurrenceRuleChange(rule.copy(interval = rule.interval + 1))
                        }
                    ) {
                        Text("+")
                    }
                }
            }

            // WEEKLY: 요일 선택
            if (rule.type == RecurrenceType.WEEKLY) {
                Spacer(modifier = Modifier.height(16.dp))
                DaysOfWeekSelector(
                    selectedDays = rule.daysOfWeek ?: emptySet(),
                    onDaysChange = { days ->
                        onRecurrenceRuleChange(rule.copy(daysOfWeek = days))
                    }
                )
            }

            // MONTHLY: 날짜 선택
            if (rule.type == RecurrenceType.MONTHLY) {
                Spacer(modifier = Modifier.height(16.dp))
                var showDayPicker by remember { mutableStateOf(false) }

                OutlinedButton(
                    onClick = { showDayPicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("매월 ${rule.dayOfMonth ?: 1}일")
                }

                if (showDayPicker) {
                    AlertDialog(
                        onDismissRequest = { showDayPicker = false },
                        title = { Text("날짜 선택") },
                        text = {
                            Column {
                                (1..31).chunked(7).forEach { week ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        week.forEach { day ->
                                            FilterChip(
                                                selected = rule.dayOfMonth == day,
                                                onClick = {
                                                    onRecurrenceRuleChange(rule.copy(dayOfMonth = day))
                                                    showDayPicker = false
                                                },
                                                label = { Text(day.toString()) }
                                            )
                                        }
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showDayPicker = false }) {
                                Text("닫기")
                            }
                        }
                    )
                }
            }

            // 3. 반복 종료 조건 설정
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = { showEndDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.CalendarToday, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(recurrenceEnd.toString())
            }
        }
    }

    // 반복 종료 조건 설정 다이얼로그
    if (showEndDialog) {
        RecurrenceEndDialog(
            currentEnd = recurrenceEnd,
            onEndChange = {
                onRecurrenceEndChange(it)
                showEndDialog = false
            },
            onDismiss = { showEndDialog = false }
        )
    }
}

/**
 * 요일 선택 UI
 */
@Composable
private fun DaysOfWeekSelector(
    selectedDays: Set<DayOfWeek>,
    onDaysChange: (Set<DayOfWeek>) -> Unit
) {
    Column {
        Text("반복 요일", style = MaterialTheme.typography.labelMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            DayOfWeek.entries.forEach { day ->
                FilterChip(
                    selected = day in selectedDays,
                    onClick = {
                        val newDays = if (day in selectedDays) {
                            selectedDays - day
                        } else {
                            selectedDays + day
                        }
                        onDaysChange(newDays)
                    },
                    label = { Text(day.toKoreanShort()) }
                )
            }
        }
    }
}

/**
 * 반복 종료 조건 설정 다이얼로그
 */
@Composable
private fun RecurrenceEndDialog(
    currentEnd: RecurrenceEnd,
    onEndChange: (RecurrenceEnd) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedType by remember { mutableStateOf(currentEnd.toType()) }
    var occurrenceCount by remember { mutableStateOf(if (currentEnd is RecurrenceEnd.AfterOccurrences) currentEnd.count else 10) }
    var endDate by remember { mutableStateOf(if (currentEnd is RecurrenceEnd.OnDate) currentEnd.date else LocalDate.now().plusMonths(1)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("반복 종료") },
        text = {
            Column {
                // 종료 타입 선택
                RecurrenceEndType.entries.forEach { type ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedType == type,
                            onClick = { selectedType = type }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(type.toKorean())
                    }

                    // 추가 옵션
                    if (selectedType == type) {
                        when (type) {
                            RecurrenceEndType.AFTER_OCCURRENCES -> {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("${occurrenceCount}회 후 종료")
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        IconButton(
                                            onClick = {
                                                if (occurrenceCount > 1) occurrenceCount--
                                            },
                                            enabled = occurrenceCount > 1
                                        ) {
                                            Text("-")
                                        }
                                        IconButton(onClick = { occurrenceCount++ }) {
                                            Text("+")
                                        }
                                    }
                                }
                            }
                            RecurrenceEndType.ON_DATE -> {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("종료 날짜: $endDate")
                                // TODO: DatePicker 추가 (향후)
                            }
                            RecurrenceEndType.NEVER -> {}
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val newEnd = when (selectedType) {
                        RecurrenceEndType.NEVER -> RecurrenceEnd.Never
                        RecurrenceEndType.AFTER_OCCURRENCES -> RecurrenceEnd.AfterOccurrences(occurrenceCount)
                        RecurrenceEndType.ON_DATE -> RecurrenceEnd.OnDate(endDate)
                    }
                    onEndChange(newEnd)
                }
            ) {
                Text("확인")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}

/**
 * RecurrenceEndType enum
 */
private enum class RecurrenceEndType {
    NEVER,
    AFTER_OCCURRENCES,
    ON_DATE
}

/**
 * 확장 함수들
 */
private fun RecurrenceType.toKorean(): String = when (this) {
    RecurrenceType.DAILY -> "매일"
    RecurrenceType.WEEKLY -> "매주"
    RecurrenceType.MONTHLY -> "매월"
    RecurrenceType.YEARLY -> "매년"
    RecurrenceType.CUSTOM -> "사용자 정의"
}

private fun RecurrenceType.toIntervalUnit(): String = when (this) {
    RecurrenceType.DAILY -> "일"
    RecurrenceType.WEEKLY -> "주"
    RecurrenceType.MONTHLY -> "개월"
    RecurrenceType.YEARLY -> "년"
    RecurrenceType.CUSTOM -> ""
}

private fun DayOfWeek.toKoreanShort(): String = when (this) {
    DayOfWeek.MONDAY -> "월"
    DayOfWeek.TUESDAY -> "화"
    DayOfWeek.WEDNESDAY -> "수"
    DayOfWeek.THURSDAY -> "목"
    DayOfWeek.FRIDAY -> "금"
    DayOfWeek.SATURDAY -> "토"
    DayOfWeek.SUNDAY -> "일"
}

private fun RecurrenceEnd.toType(): RecurrenceEndType = when (this) {
    is RecurrenceEnd.Never -> RecurrenceEndType.NEVER
    is RecurrenceEnd.AfterOccurrences -> RecurrenceEndType.AFTER_OCCURRENCES
    is RecurrenceEnd.OnDate -> RecurrenceEndType.ON_DATE
}

private fun RecurrenceEndType.toKorean(): String = when (this) {
    RecurrenceEndType.NEVER -> "종료 없음"
    RecurrenceEndType.AFTER_OCCURRENCES -> "횟수 제한"
    RecurrenceEndType.ON_DATE -> "날짜 지정"
}
