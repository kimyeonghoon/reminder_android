package com.reminder.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.reminder.data.entity.SortOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortDropdown(
    selectedSortOption: SortOption,
    onSortOptionChange: (SortOption) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Sort by",
            style = MaterialTheme.typography.labelMedium
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable)
            ) {
                Text(text = getSortOptionLabel(selectedSortOption))
                Spacer(modifier = Modifier.width(4.dp))
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            }

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                SortOption.entries.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val icon = when {
                                    option.name.contains("ASC") -> Icons.Default.ArrowUpward
                                    option.name.contains("DESC") -> Icons.Default.ArrowDownward
                                    else -> null
                                }
                                icon?.let {
                                    Icon(
                                        imageVector = it,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(text = getSortOptionLabel(option))
                            }
                        },
                        onClick = {
                            onSortOptionChange(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

private fun getSortOptionLabel(option: SortOption): String {
    return when (option) {
        SortOption.BY_DATE_ASC -> "Date (Earliest)"
        SortOption.BY_DATE_DESC -> "Date (Latest)"
        SortOption.BY_PRIORITY_HIGH_FIRST -> "Priority (High First)"
        SortOption.BY_PRIORITY_LOW_FIRST -> "Priority (Low First)"
        SortOption.BY_TITLE_ASC -> "Title (A-Z)"
        SortOption.BY_TITLE_DESC -> "Title (Z-A)"
        SortOption.BY_CREATED_ASC -> "Created (Oldest)"
        SortOption.BY_CREATED_DESC -> "Created (Newest)"
    }
}
