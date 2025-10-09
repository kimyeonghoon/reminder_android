package com.reminder.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.reminder.data.entity.FilterDate
import com.reminder.data.entity.FilterPriority
import com.reminder.data.entity.ReminderEntity
import com.reminder.data.entity.SortOption
import com.reminder.ui.components.FilterChips
import com.reminder.ui.components.ReminderCard
import com.reminder.ui.components.SortDropdown
import com.reminder.util.rememberHapticFeedback
import com.reminder.viewmodel.ReminderViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: ReminderViewModel,
    onAddClick: () -> Unit,
    onReminderClick: (ReminderEntity) -> Unit,
    onStatisticsClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    simpleMode: Boolean = false
) {
    val context = LocalContext.current
    val haptic = rememberHapticFeedback()
    val activeReminders by viewModel.activeReminders.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var showSearchBar by remember { mutableStateOf(false) }
    var selectedPriorityFilter by remember { mutableStateOf(FilterPriority.ALL) }
    var selectedDateFilter by remember { mutableStateOf(FilterDate.ALL) }
    var selectedSortOption by remember { mutableStateOf(SortOption.BY_DATE_ASC) }

    // 선택 모드 상태
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedReminders by remember { mutableStateOf<Set<Long>>(emptySet()) }

    // LazyColumn 스크롤 상태
    val listState = rememberLazyListState()

    // FAB 확장 상태 - 첫 번째 아이템이 보이면 확장, 아니면 축소
    val expandedFab by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0
        }
    }

    // Apply filters and sorting with derivedStateOf to avoid unnecessary recompositions
    val sortedReminders by remember {
        derivedStateOf {
            val searchFiltered = viewModel.getFilteredReminders(activeReminders, searchQuery)
            val priorityFiltered = viewModel.filterByPriority(searchFiltered, selectedPriorityFilter)
            val dateFiltered = viewModel.filterByDate(priorityFiltered, selectedDateFilter)
            viewModel.sortReminders(dateFiltered, selectedSortOption)
        }
    }

    // 리마인더 공유 함수
    val shareReminder: (ReminderEntity) -> Unit = { reminder ->
        val shareText = buildString {
            append("📋 ${reminder.title}\n\n")
            if (reminder.description.isNotBlank()) {
                append("${reminder.description}\n\n")
            }
            reminder.dueDateTime?.let {
                append("📅 마감: ${it.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))}\n")
            }
            if (reminder.category.isNotBlank()) {
                append("🏷️ 카테고리: ${reminder.category}\n")
            }
            val priority = when (reminder.priority) {
                com.reminder.data.entity.Priority.HIGH -> "높음"
                com.reminder.data.entity.Priority.MEDIUM -> "중간"
                com.reminder.data.entity.Priority.LOW -> "낮음"
            }
            append("⭐ 우선순위: $priority\n")
        }

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "리마인더 공유")
        context.startActivity(shareIntent)
    }

    Scaffold(
        topBar = {
            if (showSearchBar) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { viewModel.updateSearchQuery(it) },
                    onSearch = { },
                    active = true,
                    onActiveChange = { if (!it) showSearchBar = false },
                    placeholder = { Text("Search reminders...") },
                    modifier = Modifier.fillMaxWidth()
                ) {}
            } else if (isSelectionMode) {
                TopAppBar(
                    title = { Text("${selectedReminders.size}개 선택됨") },
                    navigationIcon = {
                        IconButton(onClick = {
                            isSelectionMode = false
                            selectedReminders = emptySet()
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "선택 취소")
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            // 모두 선택
                            haptic.click()
                            selectedReminders = sortedReminders.map { it.id }.toSet()
                        }) {
                            Icon(Icons.Default.SelectAll, contentDescription = "모두 선택")
                        }
                        IconButton(onClick = {
                            // 선택된 항목 완료
                            haptic.confirm()
                            val toComplete = sortedReminders.filter { it.id in selectedReminders }
                            viewModel.completeReminders(toComplete)
                            isSelectionMode = false
                            selectedReminders = emptySet()
                        }) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "완료")
                        }
                        IconButton(onClick = {
                            // 선택된 항목 삭제
                            haptic.reject()
                            val toDelete = sortedReminders.filter { it.id in selectedReminders }
                            viewModel.deleteReminders(toDelete)
                            isSelectionMode = false
                            selectedReminders = emptySet()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "삭제")
                        }
                    }
                )
            } else {
                TopAppBar(
                    title = { Text("Reminder") },
                    actions = {
                        // 간편 모드에서는 통계와 검색 숨기기
                        if (!simpleMode) {
                            IconButton(onClick = onStatisticsClick) {
                                Icon(Icons.Default.BarChart, contentDescription = "통계")
                            }
                        }
                        IconButton(onClick = onSettingsClick) {
                            Icon(Icons.Default.Settings, contentDescription = "설정")
                        }
                        if (!simpleMode) {
                            IconButton(onClick = { showSearchBar = true }) {
                                Icon(Icons.Default.Search, contentDescription = "검색")
                            }
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            if (!isSelectionMode) {
                if (simpleMode) {
                    // 간편 모드에서는 큰 일반 FAB
                    FloatingActionButton(
                        onClick = {
                            haptic.click()
                            onAddClick()
                        },
                        modifier = Modifier.size(72.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "리마인더 추가",
                            modifier = Modifier.size(36.dp)
                        )
                    }
                } else {
                    // 일반 모드에서는 Extended FAB (스크롤에 반응)
                    ExtendedFloatingActionButton(
                        onClick = {
                            haptic.click()
                            onAddClick()
                        },
                        expanded = expandedFab,
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        text = { Text("리마인더 추가") }
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 간편 모드에서는 필터와 정렬 숨기기
            if (!simpleMode) {
                // Filter and Sort UI
                FilterChips(
                    selectedPriorityFilter = selectedPriorityFilter,
                    selectedDateFilter = selectedDateFilter,
                    onPriorityFilterChange = { selectedPriorityFilter = it },
                    onDateFilterChange = { selectedDateFilter = it },
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Divider()

                SortDropdown(
                    selectedSortOption = selectedSortOption,
                    onSortOptionChange = { selectedSortOption = it }
                )

                Divider()
            }

            // Reminders list
            if (sortedReminders.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isNotBlank()) "No reminders found" else "No active reminders",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Load subtask progress for all visible reminders
                val subTaskProgressMap by produceState(
                    initialValue = emptyMap<Long, Pair<Int, Int>>(),
                    key1 = sortedReminders
                ) {
                    val progressMap = mutableMapOf<Long, Pair<Int, Int>>()
                    sortedReminders.forEach { reminder ->
                        val progress = viewModel.getSubTaskProgress(reminder.id)
                        if (progress.second > 0) { // Only include reminders with subtasks
                            progressMap[reminder.id] = progress
                        }
                    }
                    value = progressMap
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = sortedReminders,
                        key = { it.id }
                    ) { reminder ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(animationSpec = tween(300)) + scaleIn(
                                initialScale = 0.8f,
                                animationSpec = tween(300)
                            ),
                            exit = fadeOut(animationSpec = tween(300)) + scaleOut(
                                targetScale = 0.8f,
                                animationSpec = tween(300)
                            ),
                            modifier = Modifier.animateItem(
                                fadeInSpec = tween(300),
                                fadeOutSpec = tween(300),
                                placementSpec = tween(300)
                            )
                        ) {
                            ReminderCard(
                                reminder = reminder,
                                onCheckedChange = { viewModel.toggleReminderCompletion(reminder) },
                                onDelete = { viewModel.deleteReminder(reminder) },
                                onClick = {
                                    if (isSelectionMode) {
                                        // 선택 모드에서는 선택 토글
                                        selectedReminders = if (reminder.id in selectedReminders) {
                                            selectedReminders - reminder.id
                                        } else {
                                            selectedReminders + reminder.id
                                        }
                                    } else {
                                        onReminderClick(reminder)
                                    }
                                },
                                modifier = Modifier
                                    .then(
                                        if (!simpleMode) {
                                            Modifier.combinedClickable(
                                                onClick = {},
                                                onLongClick = {
                                                    haptic.longPress()
                                                    isSelectionMode = true
                                                    selectedReminders = setOf(reminder.id)
                                                }
                                            )
                                        } else {
                                            Modifier
                                        }
                                    ),
                                subTaskProgress = subTaskProgressMap[reminder.id],
                                onShare = { shareReminder(reminder) },
                                onDuplicate = { viewModel.duplicateReminder(reminder) },
                                isSelected = reminder.id in selectedReminders,
                                isSelectionMode = isSelectionMode,
                                onSelectionToggle = {
                                    selectedReminders = if (reminder.id in selectedReminders) {
                                        selectedReminders - reminder.id
                                    } else {
                                        selectedReminders + reminder.id
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
