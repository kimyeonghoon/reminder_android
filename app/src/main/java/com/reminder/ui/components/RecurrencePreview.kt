package com.reminder.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.reminder.recurrence.RecurrenceEnd
import com.reminder.recurrence.RecurrenceRule
import com.reminder.recurrence.RecurrenceScheduler
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * v1.35.0: 반복 패턴 미리보기 컴포넌트
 *
 * 반복 규칙과 종료 조건을 시각적으로 표시하고,
 * 다음 발생 날짜들을 미리 보여줍니다.
 */
@Composable
fun RecurrencePreview(
    recurrenceRule: RecurrenceRule?,
    recurrenceEnd: RecurrenceEnd?,
    startDate: LocalDate = LocalDate.now(),
    modifier: Modifier = Modifier,
    exceptions: Set<LocalDate> = emptySet()
) {
    if (recurrenceRule == null) {
        // 반복 없음
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Repeat,
                    contentDescription = "No recurrence",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "반복 없음",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    val scheduler = RecurrenceScheduler()
    val nextOccurrences = scheduler.calculateNextOccurrences(
        rule = recurrenceRule,
        start = startDate,
        end = recurrenceEnd ?: RecurrenceEnd.Never,
        limit = 5,
        exceptions = exceptions
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // 반복 패턴 설명
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Repeat,
                    contentDescription = "Recurrence",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = recurrenceRule.toNaturalLanguage(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            // 종료 조건 표시
            if (recurrenceEnd != null && recurrenceEnd !is RecurrenceEnd.Never) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = recurrenceEnd.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }

            // 다음 발생 날짜들
            if (nextOccurrences.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "다음 일정",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                val dateFormatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일 (E)")
                nextOccurrences.take(5).forEach { date ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = date.format(dateFormatter),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // 예외 날짜 표시
            if (exceptions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "${exceptions.size}개의 제외된 날짜",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * 간단한 반복 요약 텍스트
 */
@Composable
fun RecurrenceSummary(
    recurrenceRule: RecurrenceRule?,
    recurrenceEnd: RecurrenceEnd?,
    modifier: Modifier = Modifier
) {
    if (recurrenceRule == null) {
        Text(
            text = "반복 없음",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier
        )
    } else {
        val summary = buildString {
            append(recurrenceRule.toNaturalLanguage())
            if (recurrenceEnd != null && recurrenceEnd !is RecurrenceEnd.Never) {
                append(" • ")
                append(recurrenceEnd.toString())
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
        ) {
            Icon(
                imageVector = Icons.Default.Repeat,
                contentDescription = "Recurrence",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = summary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
