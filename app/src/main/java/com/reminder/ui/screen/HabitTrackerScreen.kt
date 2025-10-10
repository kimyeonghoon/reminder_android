package com.reminder.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.reminder.R
import com.reminder.data.entity.HabitEntity
import com.reminder.viewmodel.HabitViewModel

/**
 * v1.44.0: Habit Tracker Screen
 *
 * 습관 추적 및 관리 화면
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitTrackerScreen(
    viewModel: HabitViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val habits by viewModel.allHabits.collectAsState()
    val completions by viewModel.habitCompletions.collectAsState()
    val streaks by viewModel.habitStreaks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var habitToDelete by remember { mutableStateOf<HabitEntity?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    // 메시지 표시
    LaunchedEffect(errorMessage, successMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
        successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.habit_tracker_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.habit_add))
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                habits.isEmpty() -> {
                    EmptyHabitMessage(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(habits) { habit ->
                            HabitCard(
                                habit = habit,
                                isCompleted = completions[habit.id] ?: false,
                                streak = streaks[habit.id] ?: 0,
                                onToggleCompletion = { viewModel.toggleHabitCompletion(habit.id) },
                                onDelete = {
                                    habitToDelete = habit
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // 습관 추가 다이얼로그
    if (showAddDialog) {
        AddHabitDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, description, frequency ->
                viewModel.createHabit(name, description, frequency)
                showAddDialog = false
            }
        )
    }

    // 습관 삭제 확인 다이얼로그
    if (showDeleteDialog && habitToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                habitToDelete = null
            },
            icon = { Icon(Icons.Default.Delete, contentDescription = null) },
            title = { Text(stringResource(R.string.habit_delete)) },
            text = { Text(stringResource(R.string.habit_delete_confirm, habitToDelete!!.name)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        habitToDelete?.let { viewModel.deleteHabit(it.id) }
                        showDeleteDialog = false
                        habitToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.common_delete))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        habitToDelete = null
                    }
                ) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }
}

@Composable
fun HabitCard(
    habit: HabitEntity,
    isCompleted: Boolean,
    streak: Int,
    onToggleCompletion: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 체크박스
            Checkbox(
                checked = isCompleted,
                onCheckedChange = { onToggleCompletion() }
            )

            Spacer(modifier = Modifier.width(12.dp))

            // 습관 정보
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isCompleted)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurface
                )

                if (habit.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = habit.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isCompleted)
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Streak 표시
                if (streak > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.habit_streak, streak),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }

                // 주당 빈도 표시
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.habit_frequency, habit.frequency),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isCompleted)
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            // 삭제 버튼
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(R.string.habit_delete),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun AddHabitDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, description: String, frequency: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf(7) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.habit_add)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.habit_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.habit_description)) },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                // 주당 빈도 선택
                Column {
                    Text(
                        text = stringResource(R.string.habit_frequency_label),
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(1, 3, 5, 7).forEach { freq ->
                            FilterChip(
                                selected = frequency == freq,
                                onClick = { frequency = freq },
                                label = { Text("$freq") }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name, description, frequency)
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text(stringResource(R.string.common_add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        },
        modifier = modifier
    )
}

@Composable
fun EmptyHabitMessage(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Task,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.habit_no_items),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.habit_no_items_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}
