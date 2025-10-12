package com.reminder.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.reminder.data.entity.RecurrencePattern
import com.reminder.notification.AlarmScheduler
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

/**
 * 반복 설정 UI 컴포넌트
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurrenceSelector(
    recurrencePattern: RecurrencePattern,
    onPatternChange: (RecurrencePattern) -> Unit,
    recurrenceInterval: Int,
    onIntervalChange: (Int) -> Unit,
    recurrenceDaysOfWeek: Set<DayOfWeek>?,
    onDaysOfWeekChange: (Set<DayOfWeek>?) -> Unit,
    recurrenceEndDate: LocalDateTime?,
    onEndDateChange: (LocalDateTime?) -> Unit,
    startDateTime: LocalDateTime? = null,
    modifier: Modifier = Modifier
) {
    var patternExpanded by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    // 미리보기 계산
    val previewOccurrences = remember(recurrencePattern, recurrenceInterval, recurrenceDaysOfWeek, recurrenceEndDate, startDateTime) {
        if (startDateTime != null && recurrencePattern != RecurrencePattern.NONE) {
            val daysOfWeekString = recurrenceDaysOfWeek?.joinToString(",") { it.name }
            AlarmScheduler.calculateNextOccurrences(
                startDateTime = startDateTime,
                pattern = recurrencePattern,
                interval = recurrenceInterval,
                daysOfWeek = daysOfWeekString,
                endDate = recurrenceEndDate,
                count = 5
            )
        } else {
            emptyList()
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "반복 설정",
            style = MaterialTheme.typography.titleMedium
        )

        // 반복 패턴 선택
        ExposedDropdownMenuBox(
            expanded = patternExpanded,
            onExpandedChange = { patternExpanded = !patternExpanded }
        ) {
            OutlinedTextField(
                value = getPatternDisplayName(recurrencePattern),
                onValueChange = {},
                readOnly = true,
                label = { Text("반복") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = patternExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
            )

            ExposedDropdownMenu(
                expanded = patternExpanded,
                onDismissRequest = { patternExpanded = false }
            ) {
                RecurrencePattern.entries.forEach { pattern ->
                    DropdownMenuItem(
                        text = { Text(getPatternDisplayName(pattern)) },
                        onClick = {
                            onPatternChange(pattern)
                            patternExpanded = false
                        }
                    )
                }
            }
        }

        // 간격 설정 (NONE이 아닐 때만)
        if (recurrencePattern != RecurrencePattern.NONE) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("매")

                OutlinedTextField(
                    value = recurrenceInterval.toString(),
                    onValueChange = {
                        it.toIntOrNull()?.let { interval ->
                            if (interval > 0 && interval <= 99) {
                                onIntervalChange(interval)
                            }
                        }
                    },
                    modifier = Modifier.width(80.dp),
                    singleLine = true
                )

                Text(getIntervalUnit(recurrencePattern, recurrenceInterval))
            }
        }

        // 요일 선택 (WEEKLY일 때만)
        if (recurrencePattern == RecurrencePattern.WEEKLY) {
            Text("반복 요일", style = MaterialTheme.typography.bodyMedium)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DayOfWeek.entries.forEach { day ->
                    val isSelected = recurrenceDaysOfWeek?.contains(day) == true
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            val currentDays = recurrenceDaysOfWeek ?: emptySet()
                            val newDays = if (isSelected) {
                                currentDays - day
                            } else {
                                currentDays + day
                            }
                            onDaysOfWeekChange(newDays.ifEmpty { null })
                        },
                        label = {
                            Text(
                                day.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                                    .take(1)
                            )
                        },
                        modifier = Modifier.width(48.dp)
                    )
                }
            }
        }

        // 종료일 설정
        if (recurrencePattern != RecurrencePattern.NONE) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("종료일")

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (recurrenceEndDate != null) {
                        Text(
                            text = "${recurrenceEndDate.toLocalDate()}",
                            modifier = Modifier.padding(8.dp)
                        )
                        TextButton(onClick = { onEndDateChange(null) }) {
                            Text("지우기")
                        }
                    } else {
                        TextButton(onClick = { showEndDatePicker = true }) {
                            Text("종료일 설정")
                        }
                    }
                }
            }
        }

        // 미리보기
        if (previewOccurrences.isNotEmpty()) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                text = "다음 반복 일정:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    previewOccurrences.forEach { occurrence ->
                        Text(
                            text = occurrence.format(DateTimeFormatter.ofPattern("MMM dd, yyyy - HH:mm")),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }

    // 종료일 DatePicker
    if (showEndDatePicker) {
        DatePickerField(
            selectedDate = recurrenceEndDate?.toLocalDate(),
            onDateSelected = { date ->
                onEndDateChange(date?.let { LocalDateTime.of(it, LocalTime.of(23, 59)) })
                showEndDatePicker = false
            }
        )
    }
}

private fun getPatternDisplayName(pattern: RecurrencePattern): String {
    return when (pattern) {
        RecurrencePattern.NONE -> "반복 안 함"
        RecurrencePattern.DAILY -> "매일"
        RecurrencePattern.WEEKLY -> "매주"
        RecurrencePattern.MONTHLY -> "매월"
        RecurrencePattern.YEARLY -> "매년"
    }
}

private fun getIntervalUnit(pattern: RecurrencePattern, @Suppress("UNUSED_PARAMETER") interval: Int): String {
    return when (pattern) {
        RecurrencePattern.DAILY -> "일"
        RecurrencePattern.WEEKLY -> "주"
        RecurrencePattern.MONTHLY -> "개월"
        RecurrencePattern.YEARLY -> "년"
        else -> ""
    }
}
