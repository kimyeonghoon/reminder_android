package com.reminder.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.reminder.data.entity.Priority
import com.reminder.ml.CategorySuggestion
import com.reminder.ml.DueDateSuggestion
import com.reminder.ml.NotificationTimeSuggestion
import com.reminder.ml.PriorityPrediction
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * AI 스마트 제안 UI 컴포넌트
 * 우선순위, 카테고리, 마감일, 알림 시간 제안을 표시
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SmartSuggestionChips(
    prioritySuggestion: PriorityPrediction?,
    categorySuggestions: List<CategorySuggestion>,
    dueDateSuggestion: DueDateSuggestion?,
    notificationSuggestions: List<NotificationTimeSuggestion>,
    onPriorityClick: (Priority) -> Unit,
    onCategoryClick: (String) -> Unit,
    onDueDateClick: (LocalDateTime) -> Unit,
    onNotificationTimeClick: (LocalTime) -> Unit,
    modifier: Modifier = Modifier,
    showTitle: Boolean = true
) {
    // 제안이 하나라도 있는지 확인
    val hasSuggestions = prioritySuggestion != null ||
            categorySuggestions.isNotEmpty() ||
            dueDateSuggestion != null ||
            notificationSuggestions.isNotEmpty()

    AnimatedVisibility(
        visible = hasSuggestions,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                if (showTitle) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "AI 제안",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 우선순위 제안
                    prioritySuggestion?.let { prediction ->
                        if (prediction.confidence > 0.3f) {
                            PrioritySuggestionChip(
                                prediction = prediction,
                                onClick = { onPriorityClick(prediction.priority) }
                            )
                        }
                    }

                    // 카테고리 제안 (상위 2개)
                    categorySuggestions.take(2).forEach { suggestion ->
                        if (suggestion.confidence > 0.3f) {
                            CategorySuggestionChip(
                                suggestion = suggestion,
                                onClick = { onCategoryClick(suggestion.category) }
                            )
                        }
                    }

                    // 마감일 제안
                    dueDateSuggestion?.let { suggestion ->
                        if (suggestion.confidence > 0.3f) {
                            DueDateSuggestionChip(
                                suggestion = suggestion,
                                onClick = { onDueDateClick(suggestion.dueDate) }
                            )
                        }
                    }

                    // 알림 시간 제안 (상위 1개)
                    notificationSuggestions.firstOrNull()?.let { suggestion ->
                        if (suggestion.confidence > 0.3f) {
                            NotificationTimeSuggestionChip(
                                suggestion = suggestion,
                                onClick = { onNotificationTimeClick(suggestion.time) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PrioritySuggestionChip(
    prediction: PriorityPrediction,
    onClick: () -> Unit
) {
    AssistChip(
        onClick = onClick,
        label = {
            Text(
                text = when (prediction.priority) {
                    Priority.LOW -> "낮음"
                    Priority.MEDIUM -> "중간"
                    Priority.HIGH -> "높음"
                },
                style = MaterialTheme.typography.labelMedium
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.PriorityHigh,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            labelColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

@Composable
private fun CategorySuggestionChip(
    suggestion: CategorySuggestion,
    onClick: () -> Unit
) {
    AssistChip(
        onClick = onClick,
        label = {
            Text(
                text = suggestion.category,
                style = MaterialTheme.typography.labelMedium
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Category,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            labelColor = MaterialTheme.colorScheme.onTertiaryContainer
        )
    )
}

@Composable
private fun DueDateSuggestionChip(
    suggestion: DueDateSuggestion,
    onClick: () -> Unit
) {
    val dateFormatter = DateTimeFormatter.ofPattern("M월 d일")

    AssistChip(
        onClick = onClick,
        label = {
            Text(
                text = "${suggestion.estimatedDays}일 후 (${suggestion.dueDate.format(dateFormatter)})",
                style = MaterialTheme.typography.labelMedium
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            labelColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    )
}

@Composable
private fun NotificationTimeSuggestionChip(
    suggestion: NotificationTimeSuggestion,
    onClick: () -> Unit
) {
    val timeFormatter = DateTimeFormatter.ofPattern("a h시")

    AssistChip(
        onClick = onClick,
        label = {
            Text(
                text = suggestion.time.format(timeFormatter),
                style = MaterialTheme.typography.labelMedium
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}
