package com.reminder.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reminder.R
import com.reminder.data.entity.SessionType
import com.reminder.viewmodel.PomodoroViewModel

/**
 * v1.45.0: 포모도로 타이머 화면
 *
 * 간단한 포모도로 타이머 UI
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroTimerScreen(
    viewModel: PomodoroViewModel,
    onBackClick: () -> Unit
) {
    val remainingSeconds by viewModel.remainingSeconds.collectAsState()
    val isRunning by viewModel.isRunning.collectAsState()
    val currentSessionType by viewModel.currentSessionType.collectAsState()
    val todayCompletedSessions by viewModel.todayCompletedSessions.collectAsState()
    val totalFocusMinutes by viewModel.totalFocusMinutes.collectAsState()
    val streakDays by viewModel.streakDays.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // 에러 다이얼로그
    errorMessage?.let { error ->
        AlertDialog(
            onDismissRequest = { viewModel.clearErrorMessage() },
            title = { Text(stringResource(R.string.pomodoro_error)) },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearErrorMessage() }) {
                    Text(stringResource(R.string.pomodoro_ok))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.pomodoro_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 타이머 디스플레이
            item {
                TimerDisplay(
                    remainingSeconds = remainingSeconds,
                    sessionType = currentSessionType
                )
            }

            // 컨트롤 버튼
            item {
                ControlButtons(
                    isRunning = isRunning,
                    onStartClick = { viewModel.startSession() },
                    onPauseClick = { viewModel.togglePause() },
                    onStopClick = { viewModel.stopSession() }
                )
            }

            // 통계 카드
            item {
                StatisticsCard(
                    todayCompletedSessions = todayCompletedSessions,
                    totalFocusMinutes = totalFocusMinutes,
                    streakDays = streakDays
                )
            }
        }
    }
}

/**
 * 타이머 디스플레이
 */
@Composable
fun TimerDisplay(
    remainingSeconds: Int,
    sessionType: SessionType
) {
    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (sessionType) {
                SessionType.FOCUS -> MaterialTheme.colorScheme.primaryContainer
                SessionType.SHORT_BREAK -> MaterialTheme.colorScheme.secondaryContainer
                SessionType.LONG_BREAK -> MaterialTheme.colorScheme.tertiaryContainer
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 세션 타입 표시
            Text(
                text = when (sessionType) {
                    SessionType.FOCUS -> stringResource(R.string.pomodoro_focus)
                    SessionType.SHORT_BREAK -> stringResource(R.string.pomodoro_short_break)
                    SessionType.LONG_BREAK -> stringResource(R.string.pomodoro_long_break)
                },
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 타이머 표시
            Text(
                text = "%02d:%02d".format(minutes, seconds),
                fontSize = 72.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * 컨트롤 버튼
 */
@Composable
fun ControlButtons(
    isRunning: Boolean,
    onStartClick: () -> Unit,
    onPauseClick: () -> Unit,
    onStopClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!isRunning) {
            // 시작 버튼
            Button(
                onClick = onStartClick,
                modifier = Modifier.size(80.dp)
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Start",
                    modifier = Modifier.size(40.dp)
                )
            }
        } else {
            // 일시정지 버튼
            Button(
                onClick = onPauseClick,
                modifier = Modifier.size(80.dp)
            ) {
                Icon(
                    Icons.Default.Pause,
                    contentDescription = "Pause",
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 중지 버튼
            OutlinedButton(
                onClick = onStopClick,
                modifier = Modifier.size(80.dp)
            ) {
                Icon(
                    Icons.Default.Stop,
                    contentDescription = "Stop",
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}

/**
 * 통계 카드
 */
@Composable
fun StatisticsCard(
    todayCompletedSessions: Int,
    totalFocusMinutes: Int,
    streakDays: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.pomodoro_statistics),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 오늘 완료한 세션
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(R.string.pomodoro_today_sessions))
                Text(
                    text = "$todayCompletedSessions",
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 전체 집중 시간
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(R.string.pomodoro_total_focus_minutes))
                Text(
                    text = stringResource(R.string.pomodoro_minutes, totalFocusMinutes),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 연속 완료 일수
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.pomodoro_streak))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (streakDays > 0) {
                        Icon(
                            Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = stringResource(R.string.pomodoro_days, streakDays),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
