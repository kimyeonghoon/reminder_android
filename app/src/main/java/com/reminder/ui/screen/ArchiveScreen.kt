package com.reminder.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.reminder.R
import com.reminder.data.entity.Priority
import com.reminder.data.entity.ReminderEntity
import com.reminder.viewmodel.ArchiveViewModel
import java.time.format.DateTimeFormatter

/**
 * v1.43.0: Archive Screen
 *
 * 아카이브된 리마인더 목록 및 관리 화면
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveScreen(
    viewModel: ArchiveViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val archivedReminders by viewModel.archivedReminders.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    var showAutoArchiveDialog by remember { mutableStateOf(false) }
    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var reminderToDelete by remember { mutableStateOf<ReminderEntity?>(null) }

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
                title = { Text(stringResource(R.string.archive_title)) },
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
            if (archivedReminders.isNotEmpty()) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 자동 아카이브 실행 버튼
                    FloatingActionButton(
                        onClick = { showAutoArchiveDialog = true },
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    ) {
                        Icon(Icons.Default.Archive, contentDescription = stringResource(R.string.archive_auto_run))
                    }

                    // 모두 삭제 버튼
                    FloatingActionButton(
                        onClick = { showDeleteAllDialog = true },
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = stringResource(R.string.archive_delete_all))
                    }
                }
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
                archivedReminders.isEmpty() -> {
                    EmptyArchiveMessage(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(archivedReminders) { reminder ->
                            ArchivedReminderCard(
                                reminder = reminder,
                                onRestore = { viewModel.unarchiveReminder(it) },
                                onDelete = { reminderToDelete = it }
                            )
                        }
                    }
                }
            }
        }
    }

    // 자동 아카이브 확인 다이얼로그
    if (showAutoArchiveDialog) {
        AlertDialog(
            onDismissRequest = { showAutoArchiveDialog = false },
            title = { Text(stringResource(R.string.archive_auto_run)) },
            text = { Text(stringResource(R.string.archive_auto_run_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.autoArchiveOldCompleted(30)
                        showAutoArchiveDialog = false
                    }
                ) {
                    Text(stringResource(R.string.common_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAutoArchiveDialog = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    // 모두 삭제 확인 다이얼로그
    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            icon = { Icon(Icons.Default.DeleteForever, contentDescription = null) },
            title = { Text(stringResource(R.string.archive_delete_all)) },
            text = { Text(stringResource(R.string.archive_delete_all_confirm)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAllArchived()
                        showDeleteAllDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.common_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllDialog = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    // 개별 삭제 확인 다이얼로그
    reminderToDelete?.let { reminder ->
        AlertDialog(
            onDismissRequest = { reminderToDelete = null },
            icon = { Icon(Icons.Default.Delete, contentDescription = null) },
            title = { Text(stringResource(R.string.archive_delete)) },
            text = { Text(stringResource(R.string.archive_delete_confirm)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteArchivedReminder(reminder)
                        reminderToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.common_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { reminderToDelete = null }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }
}

@Composable
fun ArchivedReminderCard(
    reminder: ReminderEntity,
    onRestore: (ReminderEntity) -> Unit,
    onDelete: (ReminderEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 제목 (취소선)
            Text(
                text = reminder.title,
                style = MaterialTheme.typography.bodyLarge,
                textDecoration = TextDecoration.LineThrough,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // 설명
            if (reminder.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = reminder.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            // 우선순위
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = when (reminder.priority) {
                    Priority.HIGH -> stringResource(R.string.priority_high)
                    Priority.MEDIUM -> stringResource(R.string.priority_medium)
                    Priority.LOW -> stringResource(R.string.priority_low)
                },
                style = MaterialTheme.typography.labelSmall,
                color = when (reminder.priority) {
                    Priority.HIGH -> MaterialTheme.colorScheme.error
                    Priority.MEDIUM -> MaterialTheme.colorScheme.tertiary
                    Priority.LOW -> MaterialTheme.colorScheme.primary
                }
            )

            // 아카이브 날짜
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "아카이브: ${reminder.updatedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )

            // 액션 버튼
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 복원 버튼
                TextButton(
                    onClick = { onRestore(reminder) },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        Icons.Default.Restore,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.archive_restore))
                }

                Spacer(modifier = Modifier.width(8.dp))

                // 삭제 버튼
                TextButton(
                    onClick = { onDelete(reminder) },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.archive_delete))
                }
            }
        }
    }
}

@Composable
fun EmptyArchiveMessage(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Archive,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.archive_no_items),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
