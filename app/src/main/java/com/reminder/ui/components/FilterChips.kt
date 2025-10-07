package com.reminder.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.reminder.data.entity.FilterDate
import com.reminder.data.entity.FilterPriority

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
