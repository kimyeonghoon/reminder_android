package com.reminder.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.reminder.data.entity.Priority
import com.reminder.data.entity.RecurrencePattern
import com.reminder.data.entity.ReminderEntity
import com.reminder.ui.components.DatePickerField
import com.reminder.ui.components.RecurrenceSelector
import com.reminder.ui.components.TimePickerField
import com.reminder.viewmodel.ReminderViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditReminderScreen(
    viewModel: ReminderViewModel,
    reminder: ReminderEntity?,
    onNavigateBack: () -> Unit
) {
    var title by remember { mutableStateOf(reminder?.title ?: "") }
    var description by remember { mutableStateOf(reminder?.description ?: "") }
    var category by remember { mutableStateOf(reminder?.category ?: "") }
    var priority by remember { mutableStateOf(reminder?.priority ?: Priority.MEDIUM) }
    var selectedDate by remember { mutableStateOf(reminder?.dueDateTime?.toLocalDate()) }
    var selectedTime by remember { mutableStateOf(reminder?.dueDateTime?.toLocalTime()) }
    var expanded by remember { mutableStateOf(false) }

    // 반복 설정
    var recurrencePattern by remember { mutableStateOf(reminder?.recurrencePattern ?: RecurrencePattern.NONE) }
    var recurrenceInterval by remember { mutableStateOf(reminder?.recurrenceInterval ?: 1) }
    var recurrenceDaysOfWeek by remember {
        mutableStateOf(
            reminder?.recurrenceDaysOfWeek?.split(",")
                ?.mapNotNull { dayName ->
                    try {
                        DayOfWeek.valueOf(dayName.trim())
                    } catch (e: Exception) {
                        null
                    }
                }?.toSet()
        )
    }
    var recurrenceEndDate by remember { mutableStateOf(reminder?.recurrenceEndDate) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (reminder == null) "New Reminder" else "Edit Reminder") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Category") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = priority.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Priority") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    Priority.entries.forEach { p ->
                        DropdownMenuItem(
                            text = { Text(p.name) },
                            onClick = {
                                priority = p
                                expanded = false
                            }
                        )
                    }
                }
            }

            DatePickerField(
                selectedDate = selectedDate,
                onDateSelected = { selectedDate = it }
            )

            TimePickerField(
                selectedTime = selectedTime,
                onTimeSelected = { selectedTime = it }
            )

            Divider()

            RecurrenceSelector(
                recurrencePattern = recurrencePattern,
                onPatternChange = { recurrencePattern = it },
                recurrenceInterval = recurrenceInterval,
                onIntervalChange = { recurrenceInterval = it },
                recurrenceDaysOfWeek = recurrenceDaysOfWeek,
                onDaysOfWeekChange = { recurrenceDaysOfWeek = it },
                recurrenceEndDate = recurrenceEndDate,
                onEndDateChange = { recurrenceEndDate = it },
                startDateTime = if (selectedDate != null && selectedTime != null) {
                    LocalDateTime.of(selectedDate, selectedTime)
                } else {
                    null
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val dueDateTime = if (selectedDate != null && selectedTime != null) {
                            LocalDateTime.of(selectedDate, selectedTime)
                        } else if (selectedDate != null) {
                            LocalDateTime.of(selectedDate, LocalTime.of(0, 0))
                        } else {
                            null
                        }

                        val daysOfWeekString = recurrenceDaysOfWeek
                            ?.joinToString(",") { it.name }

                        if (reminder == null) {
                            viewModel.addReminder(
                                title = title,
                                description = description,
                                priority = priority,
                                category = category,
                                dueDateTime = dueDateTime,
                                recurrencePattern = recurrencePattern,
                                recurrenceInterval = recurrenceInterval,
                                recurrenceDaysOfWeek = daysOfWeekString,
                                recurrenceEndDate = recurrenceEndDate
                            )
                        } else {
                            viewModel.updateReminder(
                                reminder.copy(
                                    title = title,
                                    description = description,
                                    priority = priority,
                                    category = category,
                                    dueDateTime = dueDateTime,
                                    updatedAt = LocalDateTime.now(),
                                    recurrencePattern = recurrencePattern,
                                    recurrenceInterval = recurrenceInterval,
                                    recurrenceDaysOfWeek = daysOfWeekString,
                                    recurrenceEndDate = recurrenceEndDate
                                )
                            )
                        }
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotBlank()
            ) {
                Text(if (reminder == null) "Add Reminder" else "Update Reminder")
            }
        }
    }
}
