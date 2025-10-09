package com.reminder.ui.screen

import androidx.compose.animation.core.tween
import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.BarChart
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
    val activeReminders by viewModel.activeReminders.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var showSearchBar by remember { mutableStateOf(false) }
    var selectedPriorityFilter by remember { mutableStateOf(FilterPriority.ALL) }
    var selectedDateFilter by remember { mutableStateOf(FilterDate.ALL) }
    var selectedSortOption by remember { mutableStateOf(SortOption.BY_DATE_ASC) }

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
            FloatingActionButton(
                onClick = onAddClick,
                // 간편 모드에서는 버튼을 더 크게
                modifier = if (simpleMode) Modifier.size(72.dp) else Modifier
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "리마인더 추가",
                    // 간편 모드에서는 아이콘도 더 크게
                    modifier = if (simpleMode) Modifier.size(36.dp) else Modifier
                )
            }
        }
    ) { paddingValues ->
        // Apply filters and sorting with derivedStateOf to avoid unnecessary recompositions
        val sortedReminders by remember {
            derivedStateOf {
                val searchFiltered = viewModel.getFilteredReminders(activeReminders, searchQuery)
                val priorityFiltered = viewModel.filterByPriority(searchFiltered, selectedPriorityFilter)
                val dateFiltered = viewModel.filterByDate(priorityFiltered, selectedDateFilter)
                viewModel.sortReminders(dateFiltered, selectedSortOption)
            }
        }

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
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = sortedReminders,
                        key = { it.id }
                    ) { reminder ->
                        ReminderCard(
                            reminder = reminder,
                            onCheckedChange = { viewModel.toggleReminderCompletion(reminder) },
                            onDelete = { viewModel.deleteReminder(reminder) },
                            onClick = { onReminderClick(reminder) },
                            modifier = Modifier.animateItemPlacement(
                                animationSpec = tween(durationMillis = 300)
                            ),
                            subTaskProgress = subTaskProgressMap[reminder.id],
                            onShare = { shareReminder(reminder) }
                        )
                    }
                }
            }
        }
    }
}
