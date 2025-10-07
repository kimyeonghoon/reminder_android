package com.reminder.ui.screen

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
import androidx.compose.ui.unit.dp
import com.reminder.data.entity.FilterDate
import com.reminder.data.entity.FilterPriority
import com.reminder.data.entity.ReminderEntity
import com.reminder.data.entity.SortOption
import com.reminder.ui.components.FilterChips
import com.reminder.ui.components.ReminderCard
import com.reminder.ui.components.SortDropdown
import com.reminder.viewmodel.ReminderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: ReminderViewModel,
    onAddClick: () -> Unit,
    onReminderClick: (ReminderEntity) -> Unit,
    onStatisticsClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    val activeReminders by viewModel.activeReminders.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var showSearchBar by remember { mutableStateOf(false) }
    var selectedPriorityFilter by remember { mutableStateOf(FilterPriority.ALL) }
    var selectedDateFilter by remember { mutableStateOf(FilterDate.ALL) }
    var selectedSortOption by remember { mutableStateOf(SortOption.BY_DATE_ASC) }

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
                        IconButton(onClick = onStatisticsClick) {
                            Icon(Icons.Default.BarChart, "Statistics")
                        }
                        IconButton(onClick = onSettingsClick) {
                            Icon(Icons.Default.Settings, "Settings")
                        }
                        IconButton(onClick = { showSearchBar = true }) {
                            Icon(Icons.Default.Search, "Search")
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, "Add Reminder")
            }
        }
    ) { paddingValues ->
        // Apply filters and sorting
        val searchFiltered = viewModel.getFilteredReminders(activeReminders, searchQuery)
        val priorityFiltered = viewModel.filterByPriority(searchFiltered, selectedPriorityFilter)
        val dateFiltered = viewModel.filterByDate(priorityFiltered, selectedDateFilter)
        val sortedReminders = viewModel.sortReminders(dateFiltered, selectedSortOption)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
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
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(sortedReminders, key = { it.id }) { reminder ->
                        ReminderCard(
                            reminder = reminder,
                            onCheckedChange = { viewModel.toggleReminderCompletion(reminder) },
                            onDelete = { viewModel.deleteReminder(reminder) },
                            onClick = { onReminderClick(reminder) }
                        )
                    }
                }
            }
        }
    }
}
