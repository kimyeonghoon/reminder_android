package com.reminder.ui.screen

import android.Manifest
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
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
    onNavigateBack: () -> Unit,
    simpleMode: Boolean = false
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(reminder?.title ?: "") }
    var description by remember { mutableStateOf(reminder?.description ?: "") }
    var category by remember { mutableStateOf(reminder?.category ?: "") }
    var priority by remember { mutableStateOf(reminder?.priority ?: Priority.MEDIUM) }
    var selectedDate by remember { mutableStateOf(reminder?.dueDateTime?.toLocalDate()) }
    var selectedTime by remember { mutableStateOf(reminder?.dueDateTime?.toLocalTime()) }
    var expanded by remember { mutableStateOf(false) }

    // 음성 인식 권한 요청
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // 권한이 부여되면 음성 인식 시작
        }
    }

    // 음성 인식 결과 처리
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val spokenText = result.data
            ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            ?.get(0) ?: ""
        if (spokenText.isNotBlank()) {
            title = spokenText
        }
    }

    // 음성 인식 시작 함수
    val startVoiceRecognition: () -> Unit = {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PermissionChecker.PERMISSION_GRANTED

        if (hasPermission) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR")
                putExtra(RecognizerIntent.EXTRA_PROMPT, "할 일을 말씀해주세요")
            }
            speechRecognizerLauncher.launch(intent)
        } else {
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

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
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                // 음성 입력 버튼 - 70대 사용자를 위해 큰 버튼
                FilledIconButton(
                    onClick = startVoiceRecognition,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "음성 입력",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            // 간편 모드에서는 카테고리 숨기기
            if (!simpleMode) {
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

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

            // 간편 모드에서는 반복 일정 숨기기
            if (!simpleMode) {
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
            }

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
