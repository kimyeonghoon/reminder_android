package com.reminder.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * v1.38.0: 동기화 상태 배너 UI 컴포넌트
 *
 * 오프라인 상태, 동기화 진행률, 충돌 등을 표시합니다.
 */

/**
 * 동기화 상태
 */
enum class SyncStatus {
    ONLINE,         // 온라인, 동기화 완료
    OFFLINE,        // 오프라인
    SYNCING,        // 동기화 중
    CONFLICT,       // 충돌 발생
    ERROR           // 동기화 오류
}

/**
 * 동기화 상태 배너 컴포넌트
 */
@Composable
fun SyncStatusBanner(
    syncStatus: SyncStatus,
    pendingActionsCount: Int = 0,
    conflictCount: Int = 0,
    onSyncNowClick: (() -> Unit)? = null,
    onResolveConflictsClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = syncStatus != SyncStatus.ONLINE,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
        modifier = modifier
    ) {
        Surface(
            color = when (syncStatus) {
                SyncStatus.OFFLINE -> MaterialTheme.colorScheme.errorContainer
                SyncStatus.SYNCING -> MaterialTheme.colorScheme.primaryContainer
                SyncStatus.CONFLICT -> MaterialTheme.colorScheme.tertiaryContainer
                SyncStatus.ERROR -> MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = when (syncStatus) {
                            SyncStatus.OFFLINE -> Icons.Default.CloudOff
                            SyncStatus.SYNCING -> Icons.Default.Sync
                            SyncStatus.CONFLICT -> Icons.Default.Warning
                            SyncStatus.ERROR -> Icons.Default.Error
                            else -> Icons.Default.CheckCircle
                        },
                        contentDescription = null,
                        tint = when (syncStatus) {
                            SyncStatus.OFFLINE -> MaterialTheme.colorScheme.onErrorContainer
                            SyncStatus.SYNCING -> MaterialTheme.colorScheme.onPrimaryContainer
                            SyncStatus.CONFLICT -> MaterialTheme.colorScheme.onTertiaryContainer
                            SyncStatus.ERROR -> MaterialTheme.colorScheme.onErrorContainer
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = when (syncStatus) {
                                SyncStatus.OFFLINE -> "오프라인"
                                SyncStatus.SYNCING -> "동기화 중..."
                                SyncStatus.CONFLICT -> "충돌 발생"
                                SyncStatus.ERROR -> "동기화 오류"
                                else -> "온라인"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            color = when (syncStatus) {
                                SyncStatus.OFFLINE -> MaterialTheme.colorScheme.onErrorContainer
                                SyncStatus.SYNCING -> MaterialTheme.colorScheme.onPrimaryContainer
                                SyncStatus.CONFLICT -> MaterialTheme.colorScheme.onTertiaryContainer
                                SyncStatus.ERROR -> MaterialTheme.colorScheme.onErrorContainer
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )

                        if (pendingActionsCount > 0) {
                            Text(
                                text = "$pendingActionsCount 개 작업 대기 중",
                                style = MaterialTheme.typography.bodySmall,
                                color = when (syncStatus) {
                                    SyncStatus.OFFLINE -> MaterialTheme.colorScheme.onErrorContainer
                                    SyncStatus.SYNCING -> MaterialTheme.colorScheme.onPrimaryContainer
                                    SyncStatus.CONFLICT -> MaterialTheme.colorScheme.onTertiaryContainer
                                    SyncStatus.ERROR -> MaterialTheme.colorScheme.onErrorContainer
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }.copy(alpha = 0.8f)
                            )
                        }

                        if (conflictCount > 0) {
                            Text(
                                text = "$conflictCount 개 충돌",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                // 액션 버튼
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (syncStatus == SyncStatus.OFFLINE && onSyncNowClick != null) {
                        TextButton(onClick = onSyncNowClick) {
                            Text("지금 동기화")
                        }
                    }

                    if (syncStatus == SyncStatus.CONFLICT && onResolveConflictsClick != null) {
                        TextButton(onClick = onResolveConflictsClick) {
                            Text("해결")
                        }
                    }
                }
            }
        }
    }
}

/**
 * 동기화 진행률 표시
 */
@Composable
fun SyncProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "${(progress * 100).toInt()}% 완료",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
