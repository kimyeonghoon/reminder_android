package com.reminder.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.reminder.data.entity.SubTask
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

/**
 * v1.68.2: 서브태스크 섹션 컴포넌트
 * AddEditReminderScreen에서 추출됨
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SubTaskSection(
    subTasks: List<SubTask>,
    onSubTasksReordered: (List<SubTask>) -> Unit,
    onSubTaskToggle: (SubTask) -> Unit,
    onSubTaskDelete: (SubTask) -> Unit,
    newSubTaskTitle: String,
    onNewSubTaskTitleChange: (String) -> Unit,
    onAddSubTask: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        HorizontalDivider()

        Text(
            text = "서브태스크",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        // 서브태스크 리스트 (드래그 앤 드롭 가능)
        if (subTasks.isNotEmpty()) {
            var mutableSubTasks by remember(subTasks) { mutableStateOf(subTasks) }

            val reorderableState = rememberReorderableLazyListState(
                onMove = { from, to ->
                    mutableSubTasks = mutableSubTasks.toMutableList().apply {
                        add(to.index, removeAt(from.index))
                    }
                    onSubTasksReordered(mutableSubTasks)
                }
            )

            LazyColumn(
                state = reorderableState.listState,
                modifier = Modifier
                    .heightIn(max = 300.dp)
                    .reorderable(reorderableState)
            ) {
                items(
                    count = mutableSubTasks.size,
                    key = { index -> mutableSubTasks[index].id }
                ) { index ->
                    ReorderableItem(reorderableState, key = mutableSubTasks[index].id) { isDragging ->
                        val subTask = mutableSubTasks[index]
                        SubTaskItem(
                            subTask = subTask,
                            onCheckedChange = { onSubTaskToggle(subTask) },
                            onDelete = { onSubTaskDelete(subTask) },
                            modifier = if (isDragging) {
                                Modifier.border(
                                    width = 2.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = MaterialTheme.shapes.medium
                                )
                            } else {
                                Modifier
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        // 새 서브태스크 추가
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = newSubTaskTitle,
                onValueChange = onNewSubTaskTitleChange,
                label = { Text("새 서브태스크") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )

            FilledIconButton(
                onClick = onAddSubTask,
                enabled = newSubTaskTitle.isNotBlank(),
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "서브태스크 추가"
                )
            }
        }
    }
}
