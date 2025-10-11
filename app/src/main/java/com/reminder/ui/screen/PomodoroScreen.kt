package com.reminder.ui.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reminder.data.entity.PomodoroSession
import com.reminder.data.entity.SessionType
import com.reminder.viewmodel.PomodoroState
import com.reminder.viewmodel.PomodoroViewModel
import java.time.format.DateTimeFormatter

/**
 * v1.46.0: Pomodoro 타이머 화면
 *
 * 포모도로 기법을 사용한 집중 시간 관리 화면
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroScreen(
    viewModel: PomodoroViewModel,
    modifier: Modifier = Modifier
) {
    // 상태 수집
    val currentState by viewModel.currentState.collectAsState()
    val remainingSeconds by viewModel.remainingSeconds.collectAsState()
    val todaySessions by viewModel.todaySessions.collectAsState()
    val todayCompletedSessions by viewModel.todayCompletedSessions.collectAsState()
    val totalCompletedSessions by viewModel.totalCompletedSessions.collectAsState()
    val streakDays by viewModel.streakDays.collectAsState()
    val totalFocusMinutes by viewModel.totalFocusMinutes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    // Snackbar
    val snackbarHostState = remember { SnackbarHostState() }

    // 메시지 표시
    LaunchedEffect(errorMessage, successMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
        successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("포모도로 타이머") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 타이머 표시
            item {
                TimerCard(
                    currentState = currentState,
                    remainingSeconds = remainingSeconds,
                    onStartFocus = { viewModel.startFocusSession() },
                    onStartShortBreak = { viewModel.startShortBreak() },
                    onStartLongBreak = { viewModel.startLongBreak() },
                    onComplete = { viewModel.completeSession() },
                    onCancel = { viewModel.cancelSession() },
                    isLoading = isLoading
                )
            }

            // 통계 카드
            item {
                StatisticsCard(
                    todayCompletedSessions = todayCompletedSessions,
                    totalCompletedSessions = totalCompletedSessions,
                    streakDays = streakDays,
                    totalFocusMinutes = totalFocusMinutes
                )
            }

            // 오늘의 세션 목록
            item {
                Text(
                    text = "오늘의 세션",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (todaySessions.isEmpty()) {
                item {
                    EmptySessionsMessage()
                }
            } else {
                items(todaySessions) { session ->
                    SessionCard(session = session)
                }
            }
        }
    }
}

/**
 * 타이머 카드
 */
@Composable
private fun TimerCard(
    currentState: PomodoroState,
    remainingSeconds: Int,
    onStartFocus: () -> Unit,
    onStartShortBreak: () -> Unit,
    onStartLongBreak: () -> Unit,
    onComplete: () -> Unit,
    onCancel: () -> Unit,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (currentState) {
                PomodoroState.FOCUS -> MaterialTheme.colorScheme.primaryContainer
                PomodoroState.SHORT_BREAK -> MaterialTheme.colorScheme.secondaryContainer
                PomodoroState.LONG_BREAK -> MaterialTheme.colorScheme.tertiaryContainer
                PomodoroState.IDLE -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 상태 표시
            Text(
                text = when (currentState) {
                    PomodoroState.IDLE -> "대기 중"
                    PomodoroState.FOCUS -> "집중 시간"
                    PomodoroState.SHORT_BREAK -> "짧은 휴식"
                    PomodoroState.LONG_BREAK -> "긴 휴식"
                },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            // 타이머 표시
            Text(
                text = formatTime(remainingSeconds),
                style = MaterialTheme.typography.displayLarge,
                fontSize = 72.sp,
                fontWeight = FontWeight.Bold
            )

            HorizontalDivider()

            // 버튼들
            if (currentState == PomodoroState.IDLE) {
                // 대기 중일 때: 세션 시작 버튼들
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = onStartFocus,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("집중 시작 (25분)")
                    }

                    OutlinedButton(
                        onClick = onStartShortBreak,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    ) {
                        Icon(Icons.Default.Coffee, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("짧은 휴식 (5분)")
                    }

                    OutlinedButton(
                        onClick = onStartLongBreak,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    ) {
                        Icon(Icons.Default.Weekend, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("긴 휴식 (15분)")
                    }
                }
            } else {
                // 세션 진행 중일 때: 완료/취소 버튼
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("취소")
                    }

                    Button(
                        onClick = onComplete,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("완료")
                    }
                }
            }

            // 로딩 인디케이터
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            }
        }
    }
}

/**
 * 통계 카드
 */
@Composable
private fun StatisticsCard(
    todayCompletedSessions: Int,
    totalCompletedSessions: Int,
    streakDays: Int,
    totalFocusMinutes: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "통계",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            HorizontalDivider()

            // 통계 항목들
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "오늘",
                    value = "$todayCompletedSessions",
                    icon = Icons.Default.Today
                )
                StatItem(
                    label = "전체",
                    value = "$totalCompletedSessions",
                    icon = Icons.Default.AllInclusive
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "연속",
                    value = "${streakDays}일",
                    icon = Icons.Default.LocalFireDepartment
                )
                StatItem(
                    label = "집중 시간",
                    value = "${totalFocusMinutes}분",
                    icon = Icons.Default.Timer
                )
            }
        }
    }
}

/**
 * 통계 항목
 */
@Composable
private fun StatItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 세션 카드
 */
@Composable
private fun SessionCard(session: PomodoroSession) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when (session.sessionType) {
                            SessionType.FOCUS -> Icons.Default.WorkHistory
                            SessionType.SHORT_BREAK -> Icons.Default.Coffee
                            SessionType.LONG_BREAK -> Icons.Default.Weekend
                        },
                        contentDescription = null,
                        tint = when (session.sessionType) {
                            SessionType.FOCUS -> MaterialTheme.colorScheme.primary
                            SessionType.SHORT_BREAK -> MaterialTheme.colorScheme.secondary
                            SessionType.LONG_BREAK -> MaterialTheme.colorScheme.tertiary
                        }
                    )
                    Text(
                        text = when (session.sessionType) {
                            SessionType.FOCUS -> "집중 세션"
                            SessionType.SHORT_BREAK -> "짧은 휴식"
                            SessionType.LONG_BREAK -> "긴 휴식"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                Text(
                    text = "${session.duration}분",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = session.startedAt.format(DateTimeFormatter.ofPattern("HH:mm")),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 완료 상태 표시
            if (session.isCompleted) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "완료",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Cancel,
                    contentDescription = "취소됨",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

/**
 * 빈 세션 메시지
 */
@Composable
private fun EmptySessionsMessage() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.History,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "아직 완료한 세션이 없습니다",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Text(
            text = "집중 세션을 시작해보세요!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * 시간 포맷팅 (MM:SS)
 */
private fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}
