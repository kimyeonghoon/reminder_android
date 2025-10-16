package com.reminder.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.reminder.viewmodel.FocusModeViewModel

/**
 * v1.68.3: FocusDndSettingsCard 컴포넌트 (FocusModeScreen에서 분리)
 * v1.54.0: 방해 금지 모드 통합
 *
 * DND 설정 카드
 */
@Composable
fun FocusDndSettingsCard(
    settings: com.reminder.data.DndSettings,
    onSettingsChange: (com.reminder.data.DndSettings) -> Unit,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: FocusModeViewModel = viewModel()
    val hasPermission = remember { viewModel.hasDndPermission() }

    Card(
        modifier = modifier.fillMaxWidth()
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
