package com.reminder.ui.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.reminder.data.preferences.Language
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
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
            // v1.30.0: 언어 설정 섹션
            LanguageSection(
                selectedLanguage = userPreferences.language,
                onLanguageChange = { viewModel.updateLanguage(it) }
            )

            HorizontalDivider()

            // 테마 설정 섹션
            ThemeSection(
                selectedThemeMode = userPreferences.themeMode,
                onThemeModeChange = { viewModel.updateThemeMode(it) }
            )

            // 간편 모드가 아닐 때만 동적 컬러 설정 표시
            if (!userPreferences.simpleMode) {
                HorizontalDivider()

                // 동적 컬러 설정
                DynamicColorSection(
                    dynamicColorEnabled = userPreferences.dynamicColor,
                    onDynamicColorChange = { viewModel.updateDynamicColor(it) }
                )
            }

            HorizontalDivider()

            // 글씨 크기 설정
            FontSizeSection(
                selectedFontSize = userPreferences.fontSize,
                onFontSizeChange = { viewModel.updateFontSize(it) }
            )

            HorizontalDivider()

            // 간편 모드 설정
            SimpleModeSection(
                simpleModeEnabled = userPreferences.simpleMode,
                onSimpleModeChange = { viewModel.updateSimpleMode(it) }
            )

            HorizontalDivider()

            // 알림 설정 섹션
            NotificationSection(
                soundEnabled = userPreferences.notificationSound,
                vibrationEnabled = userPreferences.notificationVibration,
                ledEnabled = userPreferences.notificationLed,
                onSoundChange = { viewModel.updateNotificationSound(it) },
                onVibrationChange = { viewModel.updateNotificationVibration(it) },
                onLedChange = { viewModel.updateNotificationLed(it) }
            )

            HorizontalDivider()

            // 배지 설정 섹션
            BadgeSection(
                badgeEnabled = userPreferences.badgeEnabled,
                onBadgeChange = { viewModel.updateBadgeEnabled(it) }
            )

            HorizontalDivider()

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

                HorizontalDivider()
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

/**
 * v1.30.0: 언어 선택 섹션
 */
@Composable
fun LanguageSection(
    selectedLanguage: Language,
    onLanguageChange: (Language) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "언어 / Language",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Column(
            modifier = Modifier.selectableGroup()
        ) {
            Language.values().forEach { language ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .selectable(
                            selected = selectedLanguage == language,
                            onClick = { onLanguageChange(language) },
                            role = Role.RadioButton
                        )
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedLanguage == language,
                        onClick = null
                    )
                    Text(
                        text = getLanguageLabel(language),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
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

/**
 * 재사용 가능한 설정 스위치 아이템
 */
@Composable
private fun SettingsSwitchItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.semantics {
                contentDescription = "$title ${if (checked) "켜짐" else "꺼짐"}"
            }
        )
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

        SettingsSwitchItem(
            title = "동적 컬러 (Material You)",
            description = "시스템 배경화면 색상 사용",
            checked = dynamicColorEnabled,
            onCheckedChange = onDynamicColorChange
        )

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
private fun getLanguageLabel(language: Language): String {
    return when (language) {
        Language.SYSTEM -> "시스템 기본값 (System Default)"
        Language.KOREAN -> "한국어 (Korean)"
        Language.ENGLISH -> "English"
        Language.CHINESE -> "中文 (Chinese)"
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

        SettingsSwitchItem(
            title = "간편 모드 사용",
            description = "복잡한 기능 숨김, 더 큰 버튼",
            checked = simpleModeEnabled,
            onCheckedChange = onSimpleModeChange
        )
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
        SettingsSwitchItem(
            title = "알림 소리",
            description = "알림 수신 시 소리 재생",
            checked = soundEnabled,
            onCheckedChange = onSoundChange
        )

        // 진동 설정
        SettingsSwitchItem(
            title = "진동",
            description = "알림 수신 시 진동",
            checked = vibrationEnabled,
            onCheckedChange = onVibrationChange
        )

        // LED 설정
        SettingsSwitchItem(
            title = "LED 표시등",
            description = "알림 수신 시 LED 깜박임",
            checked = ledEnabled,
            onCheckedChange = onLedChange
        )
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

@Composable
fun BadgeSection(
    badgeEnabled: Boolean,
    onBadgeChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "앱 아이콘 배지",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        SettingsSwitchItem(
            title = "배지 표시",
            description = "미완료 리마인더 수를 앱 아이콘에 표시",
            checked = badgeEnabled,
            onCheckedChange = onBadgeChange
        )
    }
}
