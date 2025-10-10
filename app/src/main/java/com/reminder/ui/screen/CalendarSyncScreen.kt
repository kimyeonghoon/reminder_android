package com.reminder.ui.screen

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.reminder.calendar.DeviceCalendar
import com.reminder.data.entity.SyncDirection
import com.reminder.viewmodel.CalendarSyncViewModel

/**
 * v1.40.1: 캘린더 동기화 화면
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarSyncScreen(
    viewModel: CalendarSyncViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasPermission by viewModel.hasCalendarPermission.collectAsState()
    val deviceCalendars by viewModel.deviceCalendars.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val syncError by viewModel.syncError.collectAsState()

    // 캘린더 권한 요청 launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            viewModel.checkCalendarPermission()
        }
    }

    // 에러 다이얼로그
    if (syncError != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("오류") },
            text = { Text(syncError ?: "") },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("확인")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("캘린더 동기화") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
                    }
                },
                actions = {
                    // 동기화 버튼
                    if (hasPermission && deviceCalendars.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.syncAllReminders() },
                            enabled = !isSyncing
                        ) {
                            Icon(
                                Icons.Default.Sync,
                                contentDescription = "지금 동기화"
                            )
                        }
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 동기화 중 표시
            if (isSyncing) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "동기화 중...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // 권한 요청 UI
            if (!hasPermission) {
                PermissionRequestCard(
                    onRequestPermission = {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.READ_CALENDAR,
                                Manifest.permission.WRITE_CALENDAR
                            )
                        )
                    }
                )
            } else {
                // 설명 텍스트
                Text(
                    text = "기기 캘린더와 리마인더를 동기화할 수 있습니다. 동기화하려는 캘린더를 선택하세요.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // 캘린더 목록
                if (deviceCalendars.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "캘린더가 없습니다",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    Text(
                        text = "캘린더 목록",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    deviceCalendars.forEach { calendar ->
                        CalendarCard(
                            calendar = calendar,
                            onAddCalendar = { syncDirection ->
                                viewModel.addCalendarSync(calendar, syncDirection)
                            }
                        )
                    }
                }

                // "지금 동기화" 버튼
                if (deviceCalendars.isNotEmpty()) {
                    Button(
                        onClick = { viewModel.syncAllReminders() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSyncing
                    ) {
                        Icon(
                            Icons.Default.Sync,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("전체 리마인더 동기화")
                    }
                }
            }
        }
    }
}

/**
 * 권한 요청 카드
 */
@Composable
private fun PermissionRequestCard(
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "캘린더 권한 필요",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                text = "기기 캘린더와 동기화하려면 캘린더 읽기/쓰기 권한이 필요합니다.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Button(
                onClick = onRequestPermission,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text("권한 요청")
            }
        }
    }
}

/**
 * 캘린더 카드
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalendarCard(
    calendar: DeviceCalendar,
    onAddCalendar: (SyncDirection) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSyncDirectionDialog by remember { mutableStateOf(false) }

    if (showSyncDirectionDialog) {
        SyncDirectionDialog(
            calendarName = calendar.name,
            onDismiss = { showSyncDirectionDialog = false },
            onConfirm = { syncDirection ->
                onAddCalendar(syncDirection)
                showSyncDirectionDialog = false
            }
        )
    }

    Card(
        onClick = { showSyncDirectionDialog = true },
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 캘린더 색상
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(calendar.color))
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        CircleShape
                    )
            )

            // 캘린더 정보
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = calendar.name,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = calendar.accountName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * 동기화 방향 선택 다이얼로그
 */
@Composable
private fun SyncDirectionDialog(
    calendarName: String,
    onDismiss: () -> Unit,
    onConfirm: (SyncDirection) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedDirection by remember { mutableStateOf(SyncDirection.TWO_WAY) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("동기화 방향 선택") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "\"$calendarName\" 캘린더의 동기화 방향을 선택하세요.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 단방향 옵션
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedDirection == SyncDirection.ONE_WAY,
                        onClick = { selectedDirection = SyncDirection.ONE_WAY }
                    )
                    Column(
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            text = "단방향",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "리마인더 → 캘린더만",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // 양방향 옵션
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedDirection == SyncDirection.TWO_WAY,
                        onClick = { selectedDirection = SyncDirection.TWO_WAY }
                    )
                    Column(
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            text = "양방향 (권장)",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "리마인더 ↔ 캘린더",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedDirection) }) {
                Text("추가")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        },
        modifier = modifier
    )
}
