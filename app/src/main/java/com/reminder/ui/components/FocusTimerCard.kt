package com.reminder.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.reminder.data.entity.FocusSessionEntity
import com.reminder.data.entity.FocusType
import com.reminder.viewmodel.FocusState

/**
 * v1.68.3: FocusTimerCard 컴포넌트 (FocusModeScreen에서 분리)
 *
 * 타이머 카드, 원형 타이머, 포커스 타입 관련 컴포넌트
 */

/**
 * 타이머 카드
 */
@Composable
fun FocusTimerCard(
    focusState: FocusState,
    currentSession: FocusSessionEntity?,
    remainingMinutes: Int,
    remainingSeconds: Int,
    progress: Float,
    onStart: (Int, FocusType) -> Unit,
    onComplete: () -> Unit,
    onInterrupt: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedMinutes by remember { mutableIntStateOf(25) }
    var selectedType by remember { mutableStateOf(FocusType.DEEP_WORK) }
    var showTypeDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (focusState) {
                FocusState.IDLE -> {
                    // 세션 시작 전
                    Text(
                        "집중 세션 시작",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    // 타입 선택
                    OutlinedButton(
                        onClick = { showTypeDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Category, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(getFocusTypeLabel(selectedType))
                    }

                    // 시간 선택
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(25, 50, 90).forEach { minutes ->
                            FilterChip(
                                selected = selectedMinutes == minutes,
                                onClick = { selectedMinutes = minutes },
                                label = { Text("${minutes}분") }
                            )
                        }
                    }

                    // 시작 버튼
                    Button(
                        onClick = { onStart(selectedMinutes, selectedType) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("시작")
                    }
                }

                FocusState.ACTIVE -> {
                    // 진행 중
                    Text(
                        getFocusTypeLabel(currentSession?.focusType ?: FocusType.DEEP_WORK),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // 원형 프로그레스
                    CircularTimer(
                        progress = progress,
                        remainingMinutes = remainingMinutes,
                        remainingSeconds = remainingSeconds
                    )

                    // 중단 버튼
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onInterrupt,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("중단")
                        }
                        Button(
                            onClick = onComplete,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("완료")
                        }
                    }
                }

                FocusState.COMPLETED -> {
                    // 완료
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        "세션 완료!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${currentSession?.actualDurationMinutes ?: 0}분 집중했습니다",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Button(
                        onClick = onReset,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("새 세션 시작")
                    }
                }

                FocusState.INTERRUPTED -> {
                    // 중단됨
                    Icon(
                        Icons.Default.Cancel,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        "세션 중단됨",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${currentSession?.actualDurationMinutes ?: 0}분 집중했습니다",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Button(
                        onClick = onReset,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("새 세션 시작")
                    }
                }
            }
        }
    }

    // 타입 선택 다이얼로그
    if (showTypeDialog) {
        AlertDialog(
            onDismissRequest = { showTypeDialog = false },
            title = { Text("집중 타입 선택") },
            text = {
                Column {
                    FocusType.values().forEach { type ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedType == type,
                                onClick = {
                                    selectedType = type
                                    showTypeDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(getFocusTypeLabel(type))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTypeDialog = false }) {
                    Text("닫기")
                }
            }
        )
    }
}

/**
 * 원형 타이머
 */
@Composable
fun CircularTimer(
    progress: Float,
    remainingMinutes: Int,
    remainingSeconds: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(240.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 20.dp.toPx()

            // 배경 원
            drawCircle(
                color = androidx.compose.ui.graphics.Color.LightGray.copy(alpha = 0.3f),
                style = Stroke(width = strokeWidth)
            )

            // 진행 원
            drawArc(
                color = androidx.compose.ui.graphics.Color(0xFF6200EE),
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        // 남은 시간 텍스트
        Text(
            text = String.format("%02d:%02d", remainingMinutes, remainingSeconds),
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * FocusType 한글 라벨
 */
fun getFocusTypeLabel(type: FocusType): String {
    return when (type) {
        FocusType.DO_FIRST -> "긴급 중요 작업"
        FocusType.DEEP_WORK -> "깊은 작업"
        FocusType.POMODORO -> "포모도로"
        FocusType.BREAK -> "휴식"
    }
}
