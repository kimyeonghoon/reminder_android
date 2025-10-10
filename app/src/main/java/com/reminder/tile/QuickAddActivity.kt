package com.reminder.tile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.lifecycleScope
import com.reminder.ReminderApplication
import com.reminder.data.entity.Priority
import com.reminder.data.entity.ReminderEntity
import com.reminder.ui.theme.ReminderTheme
import kotlinx.coroutines.launch
import java.time.LocalDateTime

/**
 * v1.42.0: Quick Settings Tile에서 빠른 리마인더 추가 Activity
 *
 * 다이얼로그 형식의 투명 Activity로, 최소한의 입력으로 리마인더 생성
 */
class QuickAddActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ReminderTheme {
                QuickAddDialog(
                    onDismiss = { finish() },
                    onAdd = { title, priority ->
                        addReminder(title, priority)
                        finish()
                    }
                )
            }
        }
    }

    /**
     * 리마인더 추가
     */
    private fun addReminder(title: String, priority: Priority) {
        val app = application as ReminderApplication
        val reminder = ReminderEntity(
            title = title,
            description = "",
            priority = priority,
            category = "",
            isCompleted = false,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        lifecycleScope.launch {
            app.repository.insertReminder(reminder)
        }
    }
}

/**
 * 빠른 추가 다이얼로그 UI
 */
@Composable
fun QuickAddDialog(
    onDismiss: () -> Unit,
    onAdd: (String, Priority) -> Unit,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf(Priority.MEDIUM) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 제목
                Text(
                    text = "빠른 리마인더 추가",
                    style = MaterialTheme.typography.headlineSmall
                )

                // 제목 입력
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("할 일") },
                    placeholder = { Text("예: 우유 사기") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // 우선순위 선택
                Text(
                    text = "우선순위",
                    style = MaterialTheme.typography.labelMedium
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PriorityChip(
                        priority = Priority.LOW,
                        isSelected = selectedPriority == Priority.LOW,
                        onClick = { selectedPriority = Priority.LOW },
                        modifier = Modifier.weight(1f)
                    )
                    PriorityChip(
                        priority = Priority.MEDIUM,
                        isSelected = selectedPriority == Priority.MEDIUM,
                        onClick = { selectedPriority = Priority.MEDIUM },
                        modifier = Modifier.weight(1f)
                    )
                    PriorityChip(
                        priority = Priority.HIGH,
                        isSelected = selectedPriority == Priority.HIGH,
                        onClick = { selectedPriority = Priority.HIGH },
                        modifier = Modifier.weight(1f)
                    )
                }

                // 버튼
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("취소")
                    }
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onAdd(title.trim(), selectedPriority)
                            }
                        },
                        enabled = title.isNotBlank()
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("추가")
                    }
                }
            }
        }
    }
}

/**
 * 우선순위 선택 칩
 */
@Composable
fun PriorityChip(
    priority: Priority,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val label = when (priority) {
        Priority.LOW -> "낮음"
        Priority.MEDIUM -> "중간"
        Priority.HIGH -> "높음"
    }

    val containerColor = when {
        isSelected -> when (priority) {
            Priority.LOW -> MaterialTheme.colorScheme.tertiary
            Priority.MEDIUM -> MaterialTheme.colorScheme.secondary
            Priority.HIGH -> MaterialTheme.colorScheme.error
        }
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val contentColor = when {
        isSelected -> when (priority) {
            Priority.LOW -> MaterialTheme.colorScheme.onTertiary
            Priority.MEDIUM -> MaterialTheme.colorScheme.onSecondary
            Priority.HIGH -> MaterialTheme.colorScheme.onError
        }
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(label) },
        modifier = modifier,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            selectedContainerColor = containerColor,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedLabelColor = contentColor
        )
    )
}
