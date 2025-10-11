package com.reminder.ui.screen

import android.Manifest
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.reminder.data.entity.Priority
import com.reminder.data.entity.RecurrencePattern
import com.reminder.data.entity.ReminderEntity
import com.reminder.data.entity.SubTask
import com.reminder.data.entity.Urgency
import com.reminder.ui.components.DatePickerField
import com.reminder.ui.components.RecurrenceSelector
import com.reminder.ui.components.SubTaskItem
import com.reminder.ui.components.TimePickerField
import com.reminder.viewmodel.ReminderViewModel
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
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
    var urgency by remember { mutableStateOf(reminder?.urgency ?: Urgency.MEDIUM) }  // v1.47.0
    var selectedDate by remember { mutableStateOf(reminder?.dueDateTime?.toLocalDate()) }
    var selectedTime by remember { mutableStateOf(reminder?.dueDateTime?.toLocalTime()) }
    var priorityExpanded by remember { mutableStateOf(false) }
    var urgencyExpanded by remember { mutableStateOf(false) }  // v1.47.0

    // v1.22.0: 위치 관련
    var locationLatitude by remember { mutableStateOf(reminder?.locationLatitude?.toString() ?: "") }
    var locationLongitude by remember { mutableStateOf(reminder?.locationLongitude?.toString() ?: "") }
    var locationName by remember { mutableStateOf(reminder?.locationName ?: "") }
    var locationRadius by remember { mutableStateOf(reminder?.locationRadius?.toString() ?: "100") }

    // v1.23.0: 웹 링크
    var webLink by remember { mutableStateOf(reminder?.webLink ?: "") }

    // v1.24.0: TTS 자동 읽기
    var readAloud by remember { mutableStateOf(reminder?.readAloud ?: false) }

    // v1.25.0: 카테고리 제안
    var categorySuggestions by remember { mutableStateOf<List<String>>(emptyList()) }

    // v1.26.0: 최적 시간 제안
    var showOptimalTimeSuggestion by remember { mutableStateOf(false) }

    // 카테고리 자동 제안 (title이나 description 변경 시)
    LaunchedEffect(title, description) {
        if (title.isNotBlank() || description.isNotBlank()) {
            categorySuggestions = viewModel.suggestCategories(title, description)
        }
    }

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

    // 서브태스크 관련 (편집 모드일 때만)
    val subTasksFlow = if (reminder != null) {
        viewModel.getSubTasks(reminder.id).collectAsState(initial = emptyList())
    } else {
        remember { mutableStateOf(emptyList<SubTask>()) }
    }

    // 드래그 앤 드롭을 위한 mutable list
    var subTasks by remember { mutableStateOf(emptyList<SubTask>()) }

    // Flow의 변경사항을 subTasks에 동기화
    LaunchedEffect(subTasksFlow.value) {
        subTasks = subTasksFlow.value
    }

    var newSubTaskTitle by remember { mutableStateOf("") }

    // 이미지 관련 (편집 모드일 때만)
    val images = if (reminder != null) {
        viewModel.getImages(reminder.id).collectAsState(initial = emptyList())
    } else {
        remember { mutableStateOf(emptyList<com.reminder.data.entity.ReminderImage>()) }
    }

    // 이미지 선택 launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null && reminder != null) {
            // URI를 영구적으로 사용할 수 있도록 권한 요청
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            viewModel.addImage(reminder.id, uri.toString())
        }
    }

    // 이미지 확대 보기
    var selectedImageUri by remember { mutableStateOf<String?>(null) }

    // 이미지 확대 다이얼로그
    selectedImageUri?.let { uri ->
        AlertDialog(
            onDismissRequest = { selectedImageUri = null },
            confirmButton = {
                TextButton(onClick = { selectedImageUri = null }) {
                    Text("닫기")
                }
            },
            text = {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(uri.toUri())
                        .crossfade(true)
                        .build(),
                    contentDescription = "확대된 이미지",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    contentScale = ContentScale.Fit
                )
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (reminder == null) "New Reminder" else "Edit Reminder") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
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

                // v1.25.0: 카테고리 자동 제안 칩
                if (categorySuggestions.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categorySuggestions) { suggestion ->
                            SuggestionChip(
                                onClick = { category = suggestion },
                                label = { Text(suggestion) }
                            )
                        }
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = priorityExpanded,
                onExpandedChange = { priorityExpanded = !priorityExpanded }
            ) {
                OutlinedTextField(
                    value = priority.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Priority (중요도)") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = priorityExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = priorityExpanded,
                    onDismissRequest = { priorityExpanded = false }
                ) {
                    Priority.entries.forEach { p ->
                        DropdownMenuItem(
                            text = { Text(p.name) },
                            onClick = {
                                priority = p
                                priorityExpanded = false
                            }
                        )
                    }
                }
            }

            // v1.47.0: Urgency (긴급도) 선택
            ExposedDropdownMenuBox(
                expanded = urgencyExpanded,
                onExpandedChange = { urgencyExpanded = !urgencyExpanded }
            ) {
                OutlinedTextField(
                    value = urgency.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Urgency (긴급도)") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = urgencyExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = urgencyExpanded,
                    onDismissRequest = { urgencyExpanded = false }
                ) {
                    Urgency.entries.forEach { u ->
                        DropdownMenuItem(
                            text = { Text(u.name) },
                            onClick = {
                                urgency = u
                                urgencyExpanded = false
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

            // v1.26.0: 최적 시간 제안 (간편 모드 제외)
            if (!simpleMode && selectedDate != null) {
                var optimalTime by remember { mutableStateOf<LocalTime?>(null) }

                LaunchedEffect(selectedDate) {
                    val optimalDateTime = viewModel.suggestOptimalTime(selectedDate!!)
                    optimalTime = optimalDateTime.toLocalTime()
                }

                if (optimalTime != null) {
                    OutlinedButton(
                        onClick = { selectedTime = optimalTime },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("💡 추천 시간: ${optimalTime?.hour}:${optimalTime?.minute?.toString()?.padStart(2, '0')}")
                    }
                }
            }

            // v1.23.0: 웹 링크 입력 (간편 모드 제외)
            if (!simpleMode) {
                OutlinedTextField(
                    value = webLink,
                    onValueChange = { webLink = it },
                    label = { Text("🔗 웹 링크 (선택사항)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("https://example.com") }
                )
            }

            // v1.24.0: TTS 자동 읽기 토글 (간편 모드 제외)
            if (!simpleMode) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text("🔊 알림 시 자동 읽기")
                    Switch(
                        checked = readAloud,
                        onCheckedChange = { readAloud = it }
                    )
                }
            }

            // v1.22.0: 위치 입력 (간편 모드 제외)
            if (!simpleMode) {
                HorizontalDivider()
                Text(
                    text = "📍 위치 기반 알림 (선택사항)",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = locationName,
                    onValueChange = { locationName = it },
                    label = { Text("위치 이름") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("예: 집, 회사") }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = locationLatitude,
                        onValueChange = { locationLatitude = it },
                        label = { Text("위도") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        placeholder = { Text("37.5665") }
                    )

                    OutlinedTextField(
                        value = locationLongitude,
                        onValueChange = { locationLongitude = it },
                        label = { Text("경도") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        placeholder = { Text("126.9780") }
                    )
                }

                OutlinedTextField(
                    value = locationRadius,
                    onValueChange = { locationRadius = it },
                    label = { Text("반경 (미터)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("100") }
                )
            }

            // 간편 모드에서는 반복 일정 숨기기
            if (!simpleMode) {
                HorizontalDivider()

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

            // 서브태스크 섹션 (편집 모드일 때만, 간편 모드 제외)
            if (reminder != null && !simpleMode) {
                HorizontalDivider()

                Text(
                    text = "서브태스크",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                // 서브태스크 리스트 (드래그 앤 드롭 가능)
                if (subTasks.isNotEmpty()) {
                    val reorderableState = rememberReorderableLazyListState(
                        onMove = { from, to ->
                            subTasks = subTasks.toMutableList().apply {
                                add(to.index, removeAt(from.index))
                            }
                            // 드래그 중 즉시 데이터베이스 업데이트
                            viewModel.reorderSubTasks(subTasks)
                        }
                    )

                    LazyColumn(
                        state = reorderableState.listState,
                        modifier = Modifier
                            .heightIn(max = 300.dp)
                            .reorderable(reorderableState)
                    ) {
                        items(
                            count = subTasks.size,
                            key = { index -> subTasks[index].id }
                        ) { index ->
                            ReorderableItem(reorderableState, key = subTasks[index].id) { isDragging ->
                                val subTask = subTasks[index]
                                SubTaskItem(
                                    subTask = subTask,
                                    onCheckedChange = { viewModel.toggleSubTaskCompletion(subTask) },
                                    onDelete = { viewModel.deleteSubTask(subTask) },
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
                        onValueChange = { newSubTaskTitle = it },
                        label = { Text("새 서브태스크") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    FilledIconButton(
                        onClick = {
                            if (newSubTaskTitle.isNotBlank()) {
                                viewModel.addSubTask(reminder.id, newSubTaskTitle)
                                newSubTaskTitle = ""
                            }
                        },
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

            // 이미지 첨부 섹션 (편집 모드일 때만, 간편 모드 제외)
            if (reminder != null && !simpleMode) {
                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "이미지",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    FilledIconButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "이미지 추가",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // 이미지 목록 (가로 스크롤)
                if (images.value.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(images.value) { image ->
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outline,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                            ) {
                                // Coil AsyncImage로 실제 이미지 표시
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(image.imageUri.toUri())
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "첨부된 이미지",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable { selectedImageUri = image.imageUri },
                                    contentScale = ContentScale.Crop,
                                    placeholder = androidx.compose.ui.graphics.painter.ColorPainter(
                                        MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    error = androidx.compose.ui.graphics.painter.ColorPainter(
                                        MaterialTheme.colorScheme.errorContainer
                                    )
                                )

                                // 삭제 버튼
                                IconButton(
                                    onClick = { viewModel.deleteImage(image) },
                                    modifier = Modifier
                                        .align(androidx.compose.ui.Alignment.TopEnd)
                                        .size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "이미지 삭제",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Text(
                        text = "첨부된 이미지가 없습니다",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
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

                        // v1.22.0: 위치 데이터 파싱
                        val lat = locationLatitude.toDoubleOrNull()
                        val lon = locationLongitude.toDoubleOrNull()
                        val radius = locationRadius.toFloatOrNull()

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
                            var updatedReminder = reminder.copy(
                                title = title,
                                description = description,
                                priority = priority,
                                urgency = urgency,  // v1.47.0
                                category = category,
                                dueDateTime = dueDateTime,
                                updatedAt = LocalDateTime.now(),
                                recurrencePattern = recurrencePattern,
                                recurrenceInterval = recurrenceInterval,
                                recurrenceDaysOfWeek = daysOfWeekString,
                                recurrenceEndDate = recurrenceEndDate,
                                // v1.22.0: 위치 필드
                                locationLatitude = lat,
                                locationLongitude = lon,
                                locationName = locationName.ifBlank { null },
                                locationRadius = radius,
                                // v1.23.0: 웹 링크
                                webLink = webLink.ifBlank { null },
                                // v1.24.0: TTS 자동 읽기
                                readAloud = readAloud
                            )
                            viewModel.updateReminder(updatedReminder)
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
