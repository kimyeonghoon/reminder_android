package com.reminder.ui.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.reminder.ReminderApplication
import com.reminder.data.entity.FocusSessionEntity
import com.reminder.data.entity.FocusType
import com.reminder.viewmodel.FocusModeViewModel
import com.reminder.viewmodel.FocusModeViewModelFactory
import com.reminder.viewmodel.FocusState
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * v1.51.0: 포커스 모드 화면
 * v1.54.0: 방해 금지 모드 통합
 *
 * 집중 모드 타이머 및 세션 관리 화면
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusModeScreen(
    application: ReminderApplication,
    onNavigateBack: () -> Unit
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

    // 타이머 업데이트 (매 초)
    var remainingMinutes by remember { mutableStateOf(0) }
    var remainingSeconds by remember { mutableStateOf(0) }
    var progress by remember { mutableStateOf(0f) }

    LaunchedEffect(currentSession, focusState) {
        if (focusState == FocusState.ACTIVE) {
            while (true) {
                val remaining = viewModel.getRemainingMinutes()
                val progressValue = viewModel.getProgress()

                remainingMinutes = remaining
                remainingSeconds = ((currentSession?.let {
                    java.time.Duration.between(it.startTime, LocalDateTime.now()).seconds
                } ?: 0) % 60).toInt()
                progress = progressValue / 100f

                // 목표 시간 도달 시 자동 완료
                if (remaining <= 0) {
                    viewModel.completeSession()
                    break
                }

                delay(1000)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("집중 모드") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "뒤로 가기")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 통계 카드
            item {
                StatsCard(
                    todayMinutes = todayFocusMinutes,
                    streak = currentStreak
                )
            }

            // v1.54.0: DND 설정 카드 (API 23+)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                item {
                    DndSettingsCard(
                        settings = dndSettings,
                        onSettingsChange = { viewModel.updateDndSettings(it) },
                        onRequestPermission = { showDndPermissionDialog = true }
                    )
                }
            }

            // 타이머 카드
            item {
                TimerCard(
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
                    fontWeight = FontWeight.Bold
                )
            }

            items(completedSessions.take(10)) { session ->
                SessionHistoryItem(session)
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

/**
 * 통계 카드
 */
@Composable
private fun StatsCard(
    todayMinutes: Int,
    streak: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            StatItem(
                icon = Icons.Default.Timer,
                label = "오늘 집중 시간",
                value = "${todayMinutes}분"
            )
            StatItem(
                icon = Icons.Default.LocalFireDepartment,
                label = "연속 기록",
                value = "${streak}일"
            )
        }
    }
}

@Composable
private fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * 타이머 카드
 */
@Composable
private fun TimerCard(
    focusState: FocusState,
    currentSession: FocusSessionEntity?,
    remainingMinutes: Int,
    remainingSeconds: Int,
    progress: Float,
    onStart: (Int, FocusType) -> Unit,
    onComplete: () -> Unit,
    onInterrupt: () -> Unit,
    onReset: () -> Unit
) {
    var selectedMinutes by remember { mutableStateOf(25) }
    var selectedType by remember { mutableStateOf(FocusType.DEEP_WORK) }
    var showTypeDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth()
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
private fun CircularTimer(
    progress: Float,
    remainingMinutes: Int,
    remainingSeconds: Int
) {
    Box(
        modifier = Modifier.size(240.dp),
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
 * 세션 히스토리 아이템
 */
@Composable
private fun SessionHistoryItem(session: FocusSessionEntity) {
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    getFocusTypeLabel(session.focusType),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    session.startTime.format(DateTimeFormatter.ofPattern("MM/dd HH:mm")),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    if (session.isCompleted) Icons.Default.CheckCircle else Icons.Default.Cancel,
                    contentDescription = null,
                    tint = if (session.isCompleted)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    "${session.actualDurationMinutes}분",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * FocusType 한글 라벨
 */
private fun getFocusTypeLabel(type: FocusType): String {
    return when (type) {
        FocusType.DO_FIRST -> "긴급 중요 작업"
        FocusType.DEEP_WORK -> "깊은 작업"
        FocusType.POMODORO -> "포모도로"
        FocusType.BREAK -> "휴식"
    }
}

/**
 * v1.54.0: DND 설정 카드
 */
@androidx.annotation.RequiresApi(android.os.Build.VERSION_CODES.M)
@Composable
private fun DndSettingsCard(
    settings: com.reminder.data.DndSettings,
    onSettingsChange: (com.reminder.data.DndSettings) -> Unit,
    onRequestPermission: () -> Unit
) {
    val viewModel: FocusModeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val hasPermission = remember { viewModel.hasDndPermission() }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "방해 금지 모드",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "포커스 세션 중 알림 차단",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = settings.enabled,
                    onCheckedChange = { enabled ->
                        if (enabled && !hasPermission) {
                            onRequestPermission()
                        } else {
                            onSettingsChange(settings.copy(enabled = enabled))
                        }
                    }
                )
            }

            if (settings.enabled) {
                HorizontalDivider()

                // 긴급 전화 허용
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "긴급 전화 허용",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(
                        checked = settings.allowCalls,
                        onCheckedChange = { onSettingsChange(settings.copy(allowCalls = it)) }
                    )
                }

                // 알람 허용
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "알람 허용",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(
                        checked = settings.allowAlarms,
                        onCheckedChange = { onSettingsChange(settings.copy(allowAlarms = it)) }
                    )
                }

                // 자동 활성화
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "자동 활성화",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            "세션 시작 시 자동으로 DND 활성화",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = settings.autoEnable,
                        onCheckedChange = { onSettingsChange(settings.copy(autoEnable = it)) }
                    )
                }
            }
        }
    }
}
