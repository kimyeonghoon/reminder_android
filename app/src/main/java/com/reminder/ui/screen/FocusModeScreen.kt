package com.reminder.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.reminder.ReminderApplication
import com.reminder.ui.components.*
import com.reminder.viewmodel.FocusModeViewModel
import com.reminder.viewmodel.FocusModeViewModelFactory

/**
 * v1.51.0: 포커스 모드 화면
 * v1.54.0: 방해 금지 모드 통합
 * v1.68.3: 컴포넌트 분리 (FocusTimerCard, FocusDndSettingsCard, FocusStatsCard, FocusSessionHistoryItem)
 *
 * 집중 모드 타이머 및 세션 관리 화면
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusModeScreen(
    application: ReminderApplication
) {
    val viewModel: FocusModeViewModel = viewModel(
        factory = FocusModeViewModelFactory(
            application.focusSessionRepository,
            application.dndRepository // v1.54.0: DND 지원
        )
    )

    val focusState by viewModel.focusState.collectAsState()
    val currentSession by viewModel.currentSession.collectAsState()
    val todayFocusMinutes by viewModel.getTodayFocusMinutes().collectAsState(initial = 0)
    val currentStreak by viewModel.getCurrentStreak().collectAsState(initial = 0)
    val completedSessions by viewModel.completedSessions.collectAsState()

    // v1.54.0: DND 설정
    val dndSettings by viewModel.dndSettings.collectAsState()
    var showDndPermissionDialog by remember { mutableStateOf(false) }

    // v1.63.1: 타이머 업데이트 (ViewModel의 remainingSeconds StateFlow 사용)
    val totalRemainingSeconds by viewModel.remainingSeconds.collectAsState()
    val remainingMinutes = totalRemainingSeconds / 60
    val remainingSeconds = totalRemainingSeconds % 60

    // 진행률 계산
    val progress = currentSession?.let { session ->
        val totalSeconds = session.targetDurationMinutes * 60
        if (totalSeconds > 0) {
            1f - (totalRemainingSeconds.toFloat() / totalSeconds)
        } else 0f
    } ?: 0f

    // Content
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 통계 카드
            item {
                FocusStatsCard(
                    todayMinutes = todayFocusMinutes,
                    streak = currentStreak
                )
            }

            // v1.54.0: DND 설정 카드
            item {
                FocusDndSettingsCard(
                    settings = dndSettings,
                    onSettingsChange = { viewModel.updateDndSettings(it) },
                    onRequestPermission = { showDndPermissionDialog = true }
                )
            }

            // 타이머 카드
            item {
                FocusTimerCard(
                    focusState = focusState,
                    currentSession = currentSession,
                    remainingMinutes = remainingMinutes,
                    remainingSeconds = remainingSeconds,
                    progress = progress,
                    onStart = { minutes, type ->
                        viewModel.startFocusSession(minutes, type)
                    },
                    onComplete = { viewModel.completeSession() },
                    onInterrupt = { viewModel.interruptSession() },
                    onReset = { viewModel.resetSession() }
                )
            }

            // 세션 히스토리
            item {
                Text(
                    "최근 세션",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            }

            items(completedSessions.take(10)) { session ->
                FocusSessionHistoryItem(session)
            }
        }
    }

    // v1.54.0: DND 권한 요청 다이얼로그
    if (showDndPermissionDialog) {
        val context = androidx.compose.ui.platform.LocalContext.current
        AlertDialog(
            onDismissRequest = { showDndPermissionDialog = false },
            title = { Text("방해 금지 모드 권한 필요") },
            text = {
                Text(
                    "포커스 세션 중 알림을 차단하려면 방해 금지 모드 권한이 필요합니다. " +
                            "설정에서 권한을 허용해주세요."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val intent = viewModel.getDndPermissionIntent()
                        if (intent != null) {
                            context.startActivity(intent)
                        }
                        showDndPermissionDialog = false
                    }
                ) {
                    Text("설정으로 이동")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDndPermissionDialog = false }) {
                    Text("취소")
                }
            }
        )
    }
}
