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
            text = "Recurrence",
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
                label = { Text("Repeat") },
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
                Text("Every")

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
            Text("Repeat on", style = MaterialTheme.typography.bodyMedium)

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
                Text("End date")

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (recurrenceEndDate != null) {
                        Text(
                            text = "${recurrenceEndDate.toLocalDate()}",
                            modifier = Modifier.padding(8.dp)
                        )
                        TextButton(onClick = { onEndDateChange(null) }) {
                            Text("Clear")
                        }
                    } else {
                        TextButton(onClick = { showEndDatePicker = true }) {
                            Text("Set end date")
                        }
                    }
                }
            }
        }

        // 미리보기
        if (previewOccurrences.isNotEmpty()) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                text = "Next occurrences:",
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
        RecurrencePattern.NONE -> "Does not repeat"
        RecurrencePattern.DAILY -> "Daily"
        RecurrencePattern.WEEKLY -> "Weekly"
        RecurrencePattern.MONTHLY -> "Monthly"
        RecurrencePattern.YEARLY -> "Yearly"
    }
}

private fun getIntervalUnit(pattern: RecurrencePattern, interval: Int): String {
    val unit = when (pattern) {
        RecurrencePattern.DAILY -> "day"
        RecurrencePattern.WEEKLY -> "week"
        RecurrencePattern.MONTHLY -> "month"
        RecurrencePattern.YEARLY -> "year"
        else -> ""
    }
    return if (interval > 1) "${unit}s" else unit
}
