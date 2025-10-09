package com.reminder.ui.screen

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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.reminder.data.preferences.FontSize
import com.reminder.data.preferences.ThemeMode
import com.reminder.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val userPreferences by viewModel.userPreferences.collectAsState()

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

            Divider()

            // 동적 컬러 설정
            DynamicColorSection(
                dynamicColorEnabled = userPreferences.dynamicColor,
                onDynamicColorChange = { viewModel.updateDynamicColor(it) }
            )

            Divider()

            // 글씨 크기 설정
            FontSizeSection(
                selectedFontSize = userPreferences.fontSize,
                onFontSizeChange = { viewModel.updateFontSize(it) }
            )
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
private fun getFontSizeLabel(fontSize: FontSize): String {
    return when (fontSize) {
        FontSize.SMALL -> "작게"
        FontSize.NORMAL -> "보통"
        FontSize.LARGE -> "크게"
        FontSize.EXTRA_LARGE -> "아주 크게"
    }
}
