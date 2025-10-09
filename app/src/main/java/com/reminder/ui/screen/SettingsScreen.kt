package com.reminder.ui.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.reminder.backup.BackupManager
import com.reminder.data.preferences.FontSize
import com.reminder.data.preferences.ThemeMode
import com.reminder.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    backupManager: BackupManager,
    onNavigateBack: () -> Unit,
    onHelpClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userPreferences by viewModel.userPreferences.collectAsState()

    var showBackupSuccessDialog by remember { mutableStateOf(false) }
    var showBackupErrorDialog by remember { mutableStateOf(false) }
    var showRestoreSuccessDialog by remember { mutableStateOf(false) }
    var showRestoreErrorDialog by remember { mutableStateOf(false) }

    // 백업 파일 생성 launcher
    val backupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val success = backupManager.exportToJson(uri)
                if (success) {
                    showBackupSuccessDialog = true
                } else {
                    showBackupErrorDialog = true
                }
            }
        }
    }

    // 복원 파일 선택 launcher
    val restoreLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val success = backupManager.importFromJson(uri)
                if (success) {
                    showRestoreSuccessDialog = true
                } else {
                    showRestoreErrorDialog = true
                }
            }
        }
    }

    // 성공/실패 다이얼로그
    if (showBackupSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showBackupSuccessDialog = false },
            title = { Text("백업 완료") },
            text = { Text("데이터가 성공적으로 백업되었습니다.") },
            confirmButton = {
                TextButton(onClick = { showBackupSuccessDialog = false }) {
                    Text("확인")
                }
            }
        )
    }

    if (showBackupErrorDialog) {
        AlertDialog(
            onDismissRequest = { showBackupErrorDialog = false },
            title = { Text("백업 실패") },
            text = { Text("데이터 백업 중 오류가 발생했습니다.") },
            confirmButton = {
                TextButton(onClick = { showBackupErrorDialog = false }) {
                    Text("확인")
                }
            }
        )
    }

    if (showRestoreSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreSuccessDialog = false },
            title = { Text("복원 완료") },
            text = { Text("데이터가 성공적으로 복원되었습니다.") },
            confirmButton = {
                TextButton(onClick = { showRestoreSuccessDialog = false }) {
                    Text("확인")
                }
            }
        )
    }

    if (showRestoreErrorDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreErrorDialog = false },
            title = { Text("복원 실패") },
            text = { Text("데이터 복원 중 오류가 발생했습니다.") },
            confirmButton = {
                TextButton(onClick = { showRestoreErrorDialog = false }) {
                    Text("확인")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("설정") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
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
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 테마 설정 섹션
            ThemeSection(
                selectedThemeMode = userPreferences.themeMode,
                onThemeModeChange = { viewModel.updateThemeMode(it) }
            )

            // 간편 모드가 아닐 때만 동적 컬러 설정 표시
            if (!userPreferences.simpleMode) {
                Divider()

                // 동적 컬러 설정
                DynamicColorSection(
                    dynamicColorEnabled = userPreferences.dynamicColor,
                    onDynamicColorChange = { viewModel.updateDynamicColor(it) }
                )
            }

            Divider()

            // 글씨 크기 설정
            FontSizeSection(
                selectedFontSize = userPreferences.fontSize,
                onFontSizeChange = { viewModel.updateFontSize(it) }
            )

            Divider()

            // 간편 모드 설정
            SimpleModeSection(
                simpleModeEnabled = userPreferences.simpleMode,
                onSimpleModeChange = { viewModel.updateSimpleMode(it) }
            )

            Divider()

            // 알림 설정 섹션
            NotificationSection(
                soundEnabled = userPreferences.notificationSound,
                vibrationEnabled = userPreferences.notificationVibration,
                ledEnabled = userPreferences.notificationLed,
                onSoundChange = { viewModel.updateNotificationSound(it) },
                onVibrationChange = { viewModel.updateNotificationVibration(it) },
                onLedChange = { viewModel.updateNotificationLed(it) }
            )

            Divider()

            // 백업/복원 섹션 (간편 모드에서는 숨기기)
            if (!userPreferences.simpleMode) {
                BackupRestoreSection(
                    onBackupClick = {
                        val timestamp = LocalDateTime.now().format(
                            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
                        )
                        backupLauncher.launch("reminder_backup_$timestamp.json")
                    },
                    onRestoreClick = {
                        restoreLauncher.launch("application/json")
                    }
                )

                Divider()
            }

            // 도움말
            Button(
                onClick = onHelpClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("도움말 보기")
            }
        }
    }
}

@Composable
fun ThemeSection(
    selectedThemeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "테마",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Column(
            modifier = Modifier.selectableGroup()
        ) {
            ThemeMode.values().forEach { themeMode ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .selectable(
                            selected = selectedThemeMode == themeMode,
                            onClick = { onThemeModeChange(themeMode) },
                            role = Role.RadioButton
                        )
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedThemeMode == themeMode,
                        onClick = null
                    )
                    Text(
                        text = getThemeModeLabel(themeMode),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DynamicColorSection(
    dynamicColorEnabled: Boolean,
    onDynamicColorChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "색상",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "동적 컬러 (Material You)",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "시스템 배경화면 색상 사용",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Switch(
                checked = dynamicColorEnabled,
                onCheckedChange = onDynamicColorChange,
                modifier = Modifier.semantics {
                    contentDescription = "동적 컬러 사용 ${if (dynamicColorEnabled) "켜짐" else "꺼짐"}"
                }
            )
        }

        if (!dynamicColorEnabled) {
            Text(
                text = "Android 12+ 기기에서만 사용 가능",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
fun FontSizeSection(
    selectedFontSize: FontSize,
    onFontSizeChange: (FontSize) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "글씨 크기",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Column(
            modifier = Modifier.selectableGroup()
        ) {
            FontSize.values().forEach { fontSize ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .selectable(
                            selected = selectedFontSize == fontSize,
                            onClick = { onFontSizeChange(fontSize) },
                            role = Role.RadioButton
                        )
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedFontSize == fontSize,
                        onClick = null
                    )
                    Text(
                        text = getFontSizeLabel(fontSize),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun getThemeModeLabel(themeMode: ThemeMode): String {
    return when (themeMode) {
        ThemeMode.LIGHT -> "라이트 모드"
        ThemeMode.DARK -> "다크 모드"
        ThemeMode.SYSTEM -> "시스템 설정 따르기"
    }
}

@Composable
fun SimpleModeSection(
    simpleModeEnabled: Boolean,
    onSimpleModeChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "간편 모드",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "간편 모드 사용",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "복잡한 기능 숨김, 더 큰 버튼",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Switch(
                checked = simpleModeEnabled,
                onCheckedChange = onSimpleModeChange,
                modifier = Modifier.semantics {
                    contentDescription = "간편 모드 ${if (simpleModeEnabled) "켜짐" else "꺼짐"}"
                }
            )
        }
    }
}

@Composable
private fun getFontSizeLabel(fontSize: FontSize): String {
    return when (fontSize) {
        FontSize.SMALL -> "작게"
        FontSize.NORMAL -> "보통"
        FontSize.LARGE -> "크게"
        FontSize.EXTRA_LARGE -> "아주 크게"
    }
}

@Composable
fun NotificationSection(
    soundEnabled: Boolean,
    vibrationEnabled: Boolean,
    ledEnabled: Boolean,
    onSoundChange: (Boolean) -> Unit,
    onVibrationChange: (Boolean) -> Unit,
    onLedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "알림 설정",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        // 소리 설정
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "알림 소리",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "알림 수신 시 소리 재생",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Switch(
                checked = soundEnabled,
                onCheckedChange = onSoundChange,
                modifier = Modifier.semantics {
                    contentDescription = "알림 소리 ${if (soundEnabled) "켜짐" else "꺼짐"}"
                }
            )
        }

        // 진동 설정
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "진동",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "알림 수신 시 진동",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Switch(
                checked = vibrationEnabled,
                onCheckedChange = onVibrationChange,
                modifier = Modifier.semantics {
                    contentDescription = "진동 ${if (vibrationEnabled) "켜짐" else "꺼짐"}"
                }
            )
        }

        // LED 설정
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "LED 표시등",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "알림 수신 시 LED 깜박임",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Switch(
                checked = ledEnabled,
                onCheckedChange = onLedChange,
                modifier = Modifier.semantics {
                    contentDescription = "LED 표시등 ${if (ledEnabled) "켜짐" else "꺼짐"}"
                }
            )
        }
    }
}

@Composable
fun BackupRestoreSection(
    onBackupClick: () -> Unit,
    onRestoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "데이터 관리",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "리마인더와 서브태스크를 JSON 파일로 백업하거나 복원할 수 있습니다.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onBackupClick,
                modifier = Modifier.weight(1f)
            ) {
                Text("백업")
            }

            OutlinedButton(
                onClick = onRestoreClick,
                modifier = Modifier.weight(1f)
            ) {
                Text("복원")
            }
        }

        Text(
            text = "⚠️ 복원 시 기존 데이터와 병합됩니다.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}
