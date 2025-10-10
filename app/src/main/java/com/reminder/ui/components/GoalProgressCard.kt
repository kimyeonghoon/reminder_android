package com.reminder.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.reminder.data.entity.GoalEntity
import com.reminder.data.entity.GoalType
import com.reminder.goal.GoalProgress

/**
 * v1.33.0: 목표 진행률 카드 컴포넌트
 */
@Composable
fun GoalProgressCard(
    goal: GoalEntity,
    progress: GoalProgress,
    onDelete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 헤더 - 목표 타입 및 카테고리
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 목표 타입
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val typeText = when (goal.type) {
                        GoalType.DAILY -> "일일 목표"
                        GoalType.WEEKLY -> "주간 목표"
                        GoalType.MONTHLY -> "월간 목표"
                    }
                    Text(
                        text = typeText,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // 카테고리 (있는 경우)
                    goal.category?.let { category ->
                        AssistChip(
                            onClick = {},
                            label = { Text(category, style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.height(24.dp)
                        )
                    }
                }

                // 남은 일수
                if (progress.remainingDays > 0) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Timer,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${progress.remainingDays}일 남음",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 진행률 표시
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${progress.currentCount} / ${progress.targetCount}",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = "${progress.percentage.toInt()}%",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (progress.isAchieved) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }

                // 진행률 바
                LinearProgressIndicator(
                    progress = (progress.percentage / 100.0).toFloat().coerceIn(0f, 1f),
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = if (progress.isAchieved) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.secondary
                    },
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            // 달성 메시지
            if (progress.isAchieved) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "목표 달성! 축하합니다! 🎉",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
