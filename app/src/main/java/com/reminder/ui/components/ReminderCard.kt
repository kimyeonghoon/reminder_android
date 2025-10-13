package com.reminder.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.reminder.data.entity.Priority
import com.reminder.data.entity.ReminderEntity
import com.reminder.ui.theme.HighPriority
import com.reminder.ui.theme.LowPriority
import com.reminder.ui.theme.MediumPriority
import com.reminder.util.rememberHapticFeedback
import java.time.format.DateTimeFormatter

private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderCard(
    reminder: ReminderEntity,
    onCheckedChange: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subTaskProgress: Pair<Int, Int>? = null, // (완료, 전체)
    onShare: (() -> Unit)? = null,
    onDuplicate: (() -> Unit)? = null,
    isSelected: Boolean = false,
    isSelectionMode: Boolean = false,
    onSelectionToggle: (() -> Unit)? = null,
    simpleMode: Boolean = false // 간편 모드
) {
    val haptic = rememberHapticFeedback()

    // 불필요한 재구성 방지를 위해 람다를 remember로 캐싱
    val onCheckChange = remember(reminder.id) {
        { checked: Boolean ->
            haptic.confirm()
            onCheckedChange(checked)
        }
    }
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = if (isSelectionMode) { onSelectionToggle ?: {} } else onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = reminder.isCompleted,
                onCheckedChange = onCheckChange,
                modifier = Modifier.semantics {
                    contentDescription = "완료 여부 체크박스"
                }
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 간편 모드에서는 우선순위 인디케이터 숨김
                    if (!simpleMode) {
                        PriorityIndicator(priority = reminder.priority)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = reminder.title,
                        style = MaterialTheme.typography.titleMedium,
                        textDecoration = if (reminder.isCompleted) TextDecoration.LineThrough else null,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (reminder.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = reminder.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                reminder.dueDateTime?.let { dueDate ->
                    Spacer(modifier = Modifier.height(4.dp))
                    // v1.66.0: hasTime 필드로 시간 표시 여부 결정
                    val displayText = if (reminder.hasTime) {
                        dueDate.format(dateTimeFormatter)  // 날짜 + 시간
                    } else {
                        dueDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))  // 날짜만
                    }
                    Text(
                        text = displayText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // 간편 모드에서는 카테고리, 위치, 링크, TTS, 서브태스크 숨김
                if (!simpleMode) {
                    if (reminder.category.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        SuggestionChip(
                            onClick = { },
                            label = { Text(reminder.category) }
                        )
                    }

                    // v1.22.0: 위치 표시
                    if (!reminder.locationName.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "📍 ${reminder.locationName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    // v1.23.0: 웹 링크 표시
                    if (!reminder.webLink.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "🔗 ${reminder.webLink}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.tertiary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // v1.24.0: TTS 활성화 표시
                    if (reminder.readAloud) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "🔊 자동 읽기",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // 서브태스크 진행률 표시
                    subTaskProgress?.let { (completed, total) ->
                        if (total > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                LinearProgressIndicator(
                                    progress = { completed.toFloat() / total.toFloat() },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(6.dp),
                                    color = MaterialTheme.colorScheme.tertiary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                                Text(
                                    text = "$completed/$total",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            if (!isSelectionMode) {
                // 간편 모드에서는 복제/공유 버튼 숨김
                if (!simpleMode) {
                    onDuplicate?.let { duplicateAction ->
                        IconButton(onClick = {
                            haptic.click()
                            duplicateAction()
                        }) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "복제",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    onShare?.let { shareAction ->
                        IconButton(onClick = {
                            haptic.click()
                            shareAction()
                        }) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "공유",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                IconButton(onClick = {
                    haptic.reject()
                    onDelete()
                }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "삭제",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun PriorityIndicator(priority: Priority) {
    val color = when (priority) {
        Priority.HIGH -> HighPriority
        Priority.MEDIUM -> MediumPriority
        Priority.LOW -> LowPriority
    }

    val priorityText = when (priority) {
        Priority.HIGH -> "높음"
        Priority.MEDIUM -> "중간"
        Priority.LOW -> "낮음"
    }

    Surface(
        modifier = Modifier
            .size(12.dp)
            .semantics {
                contentDescription = "우선순위: $priorityText"
            },
        shape = MaterialTheme.shapes.small,
        color = color
    ) {}
}
