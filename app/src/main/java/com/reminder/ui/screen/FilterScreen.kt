package com.reminder.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.reminder.data.entity.Priority
import com.reminder.filter.FilterPresets
import com.reminder.filter.ReminderFilter
import java.time.LocalDateTime

/**
 * v1.32.0: 고급 필터 BottomSheet
 *
 * 복합 필터 조건을 설정할 수 있는 화면
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    currentFilter: ReminderFilter?,
    onApplyFilter: (ReminderFilter) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 필터 상태 관리
    var selectedPriorities by remember { mutableStateOf(currentFilter?.priorities?.toSet() ?: emptySet()) }
    var selectedCategories by remember { mutableStateOf(currentFilter?.categories?.toSet() ?: emptySet()) }
    var filterCompleted by remember { mutableStateOf(currentFilter?.isCompleted) }
    var filterWithLocation by remember { mutableStateOf(currentFilter?.hasLocation ?: false) }
    var filterWithWebLink by remember { mutableStateOf(currentFilter?.hasWebLink ?: false) }
    var filterWithTts by remember { mutableStateOf(currentFilter?.hasTts ?: false) }
    var filterWithRecurrence by remember { mutableStateOf(currentFilter?.hasRecurrence ?: false) }

    // 사용 가능한 카테고리 목록 (실제로는 ViewModel에서 가져와야 함)
    val availableCategories = remember {
        listOf("업무", "개인", "학습", "운동", "쇼핑", "건강", "집안일", "기타")
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // 제목
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "고급 필터",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "닫기")
                    }
                }
            }

            // 우선순위 필터
            item {
                FilterSection(title = "우선순위") {
                    Priority.entries.forEach { priority ->
                        val isSelected = selectedPriorities.contains(priority)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedPriorities = if (isSelected) {
                                    selectedPriorities - priority
                                } else {
                                    selectedPriorities + priority
                                }
                            },
                            label = {
                                Text(
                                    when (priority) {
                                        Priority.HIGH -> "높음"
                                        Priority.MEDIUM -> "중간"
                                        Priority.LOW -> "낮음"
                                    }
                                )
                            },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize)) }
                            } else null
                        )
                    }
                }
            }

            // 카테고리 필터
            item {
                FilterSection(title = "카테고리") {
                    availableCategories.forEach { category ->
                        val isSelected = selectedCategories.contains(category)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedCategories = if (isSelected) {
                                    selectedCategories - category
                                } else {
                                    selectedCategories + category
                                }
                            },
                            label = { Text(category) },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize)) }
                            } else null
                        )
                    }
                }
            }

            // 완료 상태 필터
            item {
                FilterSection(title = "완료 상태") {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = filterCompleted == false,
                            onClick = { filterCompleted = if (filterCompleted == false) null else false },
                            label = { Text("미완료만") },
                            leadingIcon = if (filterCompleted == false) {
                                { Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize)) }
                            } else null
                        )
                        FilterChip(
                            selected = filterCompleted == true,
                            onClick = { filterCompleted = if (filterCompleted == true) null else true },
                            label = { Text("완료만") },
                            leadingIcon = if (filterCompleted == true) {
                                { Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize)) }
                            } else null
                        )
                    }
                }
            }

            // 추가 필터
            item {
                FilterSection(title = "추가 조건") {
                    FilterChip(
                        selected = filterWithLocation,
                        onClick = { filterWithLocation = !filterWithLocation },
                        label = { Text("위치 설정됨") },
                        leadingIcon = {
                            Icon(
                                if (filterWithLocation) Icons.Default.Done else Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                            )
                        }
                    )
                    FilterChip(
                        selected = filterWithWebLink,
                        onClick = { filterWithWebLink = !filterWithWebLink },
                        label = { Text("웹 링크") },
                        leadingIcon = {
                            Icon(
                                if (filterWithWebLink) Icons.Default.Done else Icons.Default.Link,
                                contentDescription = null,
                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                            )
                        }
                    )
                    FilterChip(
                        selected = filterWithTts,
                        onClick = { filterWithTts = !filterWithTts },
                        label = { Text("음성 알림") },
                        leadingIcon = {
                            Icon(
                                if (filterWithTts) Icons.Default.Done else Icons.Default.VolumeUp,
                                contentDescription = null,
                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                            )
                        }
                    )
                    FilterChip(
                        selected = filterWithRecurrence,
                        onClick = { filterWithRecurrence = !filterWithRecurrence },
                        label = { Text("반복 작업") },
                        leadingIcon = {
                            Icon(
                                if (filterWithRecurrence) Icons.Default.Done else Icons.Default.Repeat,
                                contentDescription = null,
                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                            )
                        }
                    )
                }
            }

            // 액션 버튼
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 초기화 버튼
                    OutlinedButton(
                        onClick = {
                            selectedPriorities = emptySet()
                            selectedCategories = emptySet()
                            filterCompleted = null
                            filterWithLocation = false
                            filterWithWebLink = false
                            filterWithTts = false
                            filterWithRecurrence = false
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("초기화")
                    }

                    // 적용 버튼
                    Button(
                        onClick = {
                            val filter = ReminderFilter(
                                priorities = selectedPriorities.takeIf { it.isNotEmpty() }?.toList(),
                                categories = selectedCategories.takeIf { it.isNotEmpty() }?.toList(),
                                isCompleted = filterCompleted,
                                hasLocation = if (filterWithLocation) true else null,
                                hasWebLink = if (filterWithWebLink) true else null,
                                hasTts = if (filterWithTts) true else null,
                                hasRecurrence = if (filterWithRecurrence) true else null
                            )
                            onApplyFilter(filter)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("적용")
                    }
                }
            }
        }
    }
}

/**
 * 필터 섹션 컴포넌트
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable FlowRowScope.() -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = content
        )
    }
}
