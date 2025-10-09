package com.reminder.widget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.reminder.ReminderApplication
import com.reminder.ui.theme.ReminderTheme
import kotlinx.coroutines.launch

/**
 * 빠른 메모 입력을 위한 다이얼로그 스타일 Activity
 *
 * 위젯에서 호출되어 빠르게 리마인더를 추가할 수 있습니다.
 */
class QuickNoteActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ReminderTheme {
                QuickNoteDialog(
                    onDismiss = { finish() },
                    onSave = { title ->
                        saveQuickNote(title)
                        finish()
                    }
                )
            }
        }
    }

    private fun saveQuickNote(title: String) {
        val app = application as ReminderApplication

        // ViewModel을 사용하지 않고 직접 Repository에 접근
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try {
                app.repository.insertReminder(
                    com.reminder.data.entity.ReminderEntity(
                        title = title,
                        description = "",
                        priority = com.reminder.data.entity.Priority.MEDIUM
                    )
                )

                // 위젯 업데이트
                ReminderWidgetProvider.updateAllWidgets(this@QuickNoteActivity)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

@Composable
fun QuickNoteDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var title by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "빠른 메모",
                    style = MaterialTheme.typography.headlineSmall
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("할 일") },
                    placeholder = { Text("할 일을 입력하세요") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("취소")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onSave(title.trim())
                            }
                        },
                        enabled = title.isNotBlank()
                    ) {
                        Text("추가")
                    }
                }
            }
        }
    }
}
