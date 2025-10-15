package com.reminder.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.reminder.api.kakao.KakaoPlace

/**
 * v1.68.2: 위치 검색 UI 컴포넌트
 * AddEditReminderScreen에서 추출됨
 */
@Composable
fun LocationSearchSection(
    locationName: String,
    onLocationNameChange: (String) -> Unit,
    locationLatitude: String,
    locationLongitude: String,
    locationRadius: String,
    onLocationRadiusChange: (String) -> Unit,
    locationSearchResults: List<KakaoPlace>,
    onPlaceSelected: (KakaoPlace) -> Unit,
    onClearSearchResults: () -> Unit,
    onNavigateToMap: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        HorizontalDivider()
        Text(
            text = "📍 위치 기반 알림 (선택사항)",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )

        // 위치 이름 검색 TextField
        OutlinedTextField(
            value = locationName,
            onValueChange = onLocationNameChange,
            label = { Text("위치 이름") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("예: 스타벅스 강남점") },
            supportingText = {
                Text("2글자 이상 입력하면 자동으로 검색됩니다")
            }
        )

        // 검색 결과 목록 (있을 때만 표시)
        if (locationSearchResults.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                ) {
                    locationSearchResults.forEach { place ->
                        ListItem(
                            headlineContent = { Text(place.placeName) },
                            supportingContent = { Text(place.addressName) },
                            modifier = Modifier.clickable {
                                onPlaceSelected(place)
                                onClearSearchResults()
                            },
                            colors = ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                        if (place != locationSearchResults.last()) {
                            HorizontalDivider()
                        }
                    }
                }
            }
        }

        // 위치 상태 표시
        if (locationLatitude.isNotBlank() && locationLongitude.isNotBlank()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Geofencing 활성화됨 (좌표: ${locationLatitude.take(8)}, ${locationLongitude.take(9)})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
            }

            // 지도에서 보기 버튼
            OutlinedButton(
                onClick = onNavigateToMap,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("🗺️ 지도에서 위치 확인")
            }
        } else if (locationName.isNotBlank()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "위치 메모만 저장됨 (알림 없음)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 반경 설정 (Geofencing이 활성화된 경우에만 표시)
        if (locationLatitude.isNotBlank() && locationLongitude.isNotBlank()) {
            OutlinedTextField(
                value = locationRadius,
                onValueChange = onLocationRadiusChange,
                label = { Text("반경 (미터)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("100") }
            )
        }
    }
}
