package com.reminder.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.reminder.data.entity.FilterDate
import com.reminder.data.entity.FilterPriority
import com.reminder.filter.FilterPreset
import com.reminder.filter.FilterPresets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterChips(
    selectedPriorityFilter: FilterPriority,
    selectedDateFilter: FilterDate,
    onPriorityFilterChange: (FilterPriority) -> Unit,
    onDateFilterChange: (FilterDate) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Priority filters
        Text(
            text = "Priority",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(start = 16.dp)
        )
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(FilterPriority.entries) { filter ->
                FilterChip(
                    selected = selectedPriorityFilter == filter,
                    onClick = { onPriorityFilterChange(filter) },
                    label = {
                        Text(
                            text = when (filter) {
                                FilterPriority.ALL -> "All"
                                FilterPriority.HIGH -> "High"
                                FilterPriority.MEDIUM -> "Medium"
                                FilterPriority.LOW -> "Low"
                            }
                        )
                    }
                )
            }
        }

        // Date filters
        Text(
            text = "Date",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(start = 16.dp, top = 8.dp)
        )
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(FilterDate.entries) { filter ->
                FilterChip(
                    selected = selectedDateFilter == filter,
                    onClick = { onDateFilterChange(filter) },
                    label = {
                        Text(
                            text = when (filter) {
                                FilterDate.ALL -> "All"
                                FilterDate.TODAY -> "Today"
                                FilterDate.THIS_WEEK -> "This Week"
                                FilterDate.THIS_MONTH -> "This Month"
                                FilterDate.OVERDUE -> "Overdue"
                            }
                        )
                    }
                )
            }
        }
    }
}

/**
 * v1.32.0: 필터 프리셋 칩 (빠른 필터 선택)
 *
 * 가로 스크롤 가능한 프리셋 칩 목록
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterPresetChips(
    currentFilter: com.reminder.filter.ReminderFilter?,
    onPresetClick: (String) -> Unit,
    onClearFilter: () -> Unit,
    modifier: Modifier = Modifier
) {
    val presets = remember { FilterPresets.getAllPresets() }

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // "모두 보기" 칩 (필터 초기화)
        item {
            FilterChip(
                selected = currentFilter == null,
                onClick = onClearFilter,
                label = { Text("모두 보기") },
                leadingIcon = if (currentFilter == null) {
                    { Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize)) }
                } else null
            )
        }

        // 프리셋 칩들
        items(presets) { preset ->
            FilterChip(
                selected = false, // 프리셋은 선택 상태를 직접 표시하지 않음
                onClick = { onPresetClick(preset.id) },
                label = { Text(preset.name) },
                leadingIcon = {
                    Icon(
                        imageVector = getIconForPreset(preset.icon),
                        contentDescription = null,
                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                    )
                }
            )
        }
    }
}

/**
 * 프리셋 아이콘 이름을 ImageVector로 변환
 */
@Composable
private fun getIconForPreset(iconName: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (iconName) {
        "today" -> Icons.Default.Today
        "date_range" -> Icons.Default.DateRange
        "priority_high" -> Icons.Default.PriorityHigh
        "error" -> Icons.Default.Error
        "radio_button_unchecked" -> Icons.Default.RadioButtonUnchecked
        "check_circle" -> Icons.Default.CheckCircle
        "location_on" -> Icons.Default.LocationOn
        "repeat" -> Icons.Default.Repeat
        "link" -> Icons.Default.Link
        "volume_up" -> Icons.AutoMirrored.Filled.VolumeUp
        else -> Icons.Default.FilterList
    }
}

/**
 * v1.32.0: 활성 필터 표시 칩 (제거 가능)
 *
 * 현재 적용된 필터를 표시하고 제거 버튼 제공
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveFilterChip(
    filterName: String,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    InputChip(
        selected = true,
        onClick = onRemove,
        label = { Text(filterName) },
        trailingIcon = {
            Icon(
                Icons.Default.Close,
                contentDescription = "필터 제거",
                modifier = Modifier.size(InputChipDefaults.IconSize)
            )
        },
        modifier = modifier
    )
}

/**
 * v1.32.0: 저장된 필터 칩 목록
 *
 * 사용자가 저장한 커스텀 필터 목록을 표시
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedFilterChips(
    savedFilters: List<com.reminder.data.entity.SavedFilterEntity>,
    onFilterClick: (com.reminder.data.entity.SavedFilterEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    if (savedFilters.isEmpty()) {
        Text(
            text = "저장된 필터가 없습니다",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier.padding(16.dp)
        )
    } else {
        LazyRow(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(savedFilters) { savedFilter ->
                FilterChip(
                    selected = false,
                    onClick = { onFilterClick(savedFilter) },
                    label = { Text(savedFilter.name) },
                    leadingIcon = {
                        Icon(
                            imageVector = getIconForPreset(savedFilter.icon),
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                )
            }
        }
    }
}
