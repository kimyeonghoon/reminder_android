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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.reminder.ai.UrgencyPredictor
import com.reminder.api.kakao.KakaoPlace
import com.reminder.data.entity.Priority
// import com.reminder.data.entity.RecurrencePattern  // v1.64.0: Deprecated, TODO v1.65.0 RecurrenceRule UI
import com.reminder.data.entity.ReminderEntity
import com.reminder.data.entity.SubTask
import com.reminder.data.entity.Urgency
import com.reminder.ReminderApplication
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import com.reminder.ui.components.DatePickerField
import com.reminder.ui.components.ExposedDropdownField
import com.reminder.ui.components.ImageAttachmentSection
import com.reminder.ui.components.LocationSearchSection
import com.reminder.ui.components.RecurrenceSelector
import com.reminder.ui.components.SubTaskItem
import com.reminder.ui.components.SubTaskSection
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
    subTaskViewModel: com.reminder.viewmodel.SubTaskViewModel, // v1.68.1: SubTask 기능 분리
    attachmentViewModel: com.reminder.viewmodel.AttachmentViewModel, // v1.68.1: 첨부 기능 분리
    reminder: ReminderEntity?,
    onNavigateBack: () -> Unit,
    onNavigateToMap: (Double, Double, String) -> Unit = { _, _, _ -> }, // v1.68.0: 지도 화면 이동
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

    // v1.22.0: 위치 관련 (v1.68.1: rememberSaveable로 변경 - 화면 전환 시에도 유지)
    var locationLatitude by rememberSaveable { mutableStateOf(reminder?.locationLatitude?.toString() ?: "") }
    var locationLongitude by rememberSaveable { mutableStateOf(reminder?.locationLongitude?.toString() ?: "") }
    var locationName by rememberSaveable { mutableStateOf(reminder?.locationName ?: "") }
    var locationRadius by rememberSaveable { mutableStateOf(reminder?.locationRadius?.toString() ?: "100") }

    // v1.67.0: 카카오 장소 검색 결과
    var locationSearchResults by remember { mutableStateOf<List<KakaoPlace>>(emptyList()) }
    var skipLocationSearch by remember { mutableStateOf(false) } // 선택 후 검색 스킵

    // v1.23.0: 웹 링크
    var webLink by remember { mutableStateOf(reminder?.webLink ?: "") }

    // v1.24.0: TTS 자동 읽기
    var readAloud by remember { mutableStateOf(reminder?.readAloud ?: false) }

    // v1.66.0: 미리 알림
    var advanceNotificationMinutes by remember { mutableStateOf(reminder?.advanceNotificationMinutes) }
    var advanceNotificationExpanded by remember { mutableStateOf(false) }

    // v1.25.0: 카테고리 제안
    var categorySuggestions by remember { mutableStateOf<List<String>>(emptyList()) }

    // 카테고리 자동 제안 (title이나 description 변경 시)
    LaunchedEffect(title, description) {
        if (title.isNotBlank() || description.isNotBlank()) {
            categorySuggestions = viewModel.suggestCategories(title, description)
        }
    }

    // v1.67.0: 카카오 장소 검색 (debounced)
    LaunchedEffect(locationName, skipLocationSearch) {
        if (skipLocationSearch) {
            skipLocationSearch = false // 플래그 리셋
        } else if (locationName.length >= 2) {
            delay(500) // 500ms 디바운스
            val app = context.applicationContext as? ReminderApplication
            if (app != null) {
                locationSearchResults = app.locationSearchRepository.searchPlaces(locationName)
            }
        } else {
            locationSearchResults = emptyList()
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

    // v1.65.0: RecurrenceRule UI 복원
    var recurrenceRule by remember { mutableStateOf(reminder?.recurrenceRule) }
    var recurrenceEnd by remember { mutableStateOf(reminder?.recurrenceEnd ?: com.reminder.recurrence.RecurrenceEnd.Never) }

    // 서브태스크 관련 (편집 모드일 때만)
    val subTasksFlow = if (reminder != null) {
        subTaskViewModel.getSubTasks(reminder.id).collectAsState(initial = emptyList())
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
        attachmentViewModel.getImages(reminder.id).collectAsState(initial = emptyList())
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
            attachmentViewModel.addImage(reminder.id, uri.toString())
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
                title = { Text(if (reminder == null) "새 리마인더" else "리마인더 수정") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
                    .padding(bottom = 80.dp), // 버튼 공간 확보
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("제목") },
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
                label = { Text("설명") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            // 간편 모드에서는 카테고리 숨기기
            if (!simpleMode) {
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("카테고리") },
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

            // 간편 모드에서는 중요도와 긴급도 숨김
            if (!simpleMode) {
                ExposedDropdownField(
                    label = "중요도",
                    value = when (priority) {
                        Priority.HIGH -> "높음"
                        Priority.MEDIUM -> "중간"
                        Priority.LOW -> "낮음"
                    },
                    options = Priority.entries.map { p ->
                        p to when (p) {
                            Priority.HIGH -> "높음"
                            Priority.MEDIUM -> "중간"
                            Priority.LOW -> "낮음"
                        }
                    },
                    expanded = priorityExpanded,
                    onExpandedChange = { priorityExpanded = it },
                    onSelected = { priority = it }
                )

                // v1.47.0: Urgency (긴급도) 선택
                ExposedDropdownField(
                    label = "긴급도",
                    value = when (urgency) {
                        Urgency.HIGH -> "높음"
                        Urgency.MEDIUM -> "중간"
                        Urgency.LOW -> "낮음"
                    },
                    options = Urgency.entries.map { u ->
                        u to when (u) {
                            Urgency.HIGH -> "높음"
                            Urgency.MEDIUM -> "중간"
                            Urgency.LOW -> "낮음"
                        }
                    },
                    expanded = urgencyExpanded,
                    onExpandedChange = { urgencyExpanded = it },
                    onSelected = { urgency = it }
                )
            }

            // v1.48.0: AI 긴급도 예측
            if (!simpleMode && (title.isNotBlank() || description.isNotBlank())) {
                val predictor = remember { UrgencyPredictor() }
                val predicted = remember(title, description) {
                    predictor.predictUrgency(title, description)
                }
                val reason = remember(title, description) {
                    predictor.getReasonForPrediction(title, description)
                }

                if (predicted != urgency) {
                    OutlinedButton(
                        onClick = { urgency = predicted },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                        ) {
                            val urgencyText = when (predicted) {
                                Urgency.HIGH -> "높음"
                                Urgency.MEDIUM -> "중간"
                                Urgency.LOW -> "낮음"
                            }
                            Text("🤖 AI 제안: $urgencyText 긴급도")
                            Text(
                                text = reason,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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

            // v1.66.0: 미리 알림 (간편 모드에서도 표시 - 70대에게 유용)
            // 날짜와 시간을 모두 선택해야만 미리 알림 UI 표시
            if (selectedDate != null && selectedTime != null) {
                ExposedDropdownField(
                    label = "⏰ 미리 알림",
                    value = when (advanceNotificationMinutes) {
                        null -> "없음"
                        5 -> "5분 전"
                        10 -> "10분 전"
                        15 -> "15분 전"
                        30 -> "30분 전"
                        60 -> "1시간 전"
                        120 -> "2시간 전"
                        1440 -> "1일 전"
                        else -> "${advanceNotificationMinutes}분 전"
                    },
                    options = listOf(
                        null to "없음",
                        5 to "5분 전",
                        10 to "10분 전",
                        15 to "15분 전",
                        30 to "30분 전",
                        60 to "1시간 전",
                        120 to "2시간 전",
                        1440 to "1일 전"
                    ),
                    expanded = advanceNotificationExpanded,
                    onExpandedChange = { advanceNotificationExpanded = it },
                    onSelected = { advanceNotificationMinutes = it }
                )
            }

            // v1.26.0: 최적 시간 제안 (간편 모드 제외)
            selectedDate?.let { date ->
                if (!simpleMode) {
                    var optimalTime by remember { mutableStateOf<LocalTime?>(null) }

                        LaunchedEffect(date) {
                        val optimalDateTime = viewModel.suggestOptimalTime(date)
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

            // v1.68.2: 위치 검색 (카카오 로컬 API) - 간편 모드에서도 표시
            LocationSearchSection(
                locationName = locationName,
                onLocationNameChange = { locationName = it },
                locationLatitude = locationLatitude,
                locationLongitude = locationLongitude,
                locationRadius = locationRadius,
                onLocationRadiusChange = { locationRadius = it },
                locationSearchResults = locationSearchResults,
                onPlaceSelected = { place ->
                    skipLocationSearch = true
                    locationName = place.placeName
                    locationLatitude = place.latitude
                    locationLongitude = place.longitude
                },
                onClearSearchResults = { locationSearchResults = emptyList() },
                onNavigateToMap = {
                    val lat = locationLatitude.toDoubleOrNull()
                    val lon = locationLongitude.toDoubleOrNull()
                    if (lat != null && lon != null) {
                        onNavigateToMap(lat, lon, locationName.ifBlank { "선택한 위치" })
                    }
                }
            )

            // v1.65.0: RecurrenceRule UI 복원
            if (!simpleMode) {
                HorizontalDivider()
                Text(
                    text = "🔄 반복 설정",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                RecurrenceSelector(
                    recurrenceRule = recurrenceRule,
                    onRecurrenceRuleChange = { recurrenceRule = it },
                    recurrenceEnd = recurrenceEnd,
                    onRecurrenceEndChange = { recurrenceEnd = it }
                )
            }

            // v1.68.2: 서브태스크 섹션 (편집 모드일 때만, 간편 모드 제외)
            if (reminder != null && !simpleMode) {
                SubTaskSection(
                    subTasks = subTasks,
                    onSubTasksReordered = { reorderedSubTasks ->
                        subTasks = reorderedSubTasks
                        subTaskViewModel.reorderSubTasks(reorderedSubTasks)
                    },
                    onSubTaskToggle = { subTaskViewModel.toggleSubTaskCompletion(it) },
                    onSubTaskDelete = { subTaskViewModel.deleteSubTask(it) },
                    newSubTaskTitle = newSubTaskTitle,
                    onNewSubTaskTitleChange = { newSubTaskTitle = it },
                    onAddSubTask = {
                        if (newSubTaskTitle.isNotBlank()) {
                            subTaskViewModel.addSubTask(reminder.id, newSubTaskTitle)
                            newSubTaskTitle = ""
                        }
                    }
                )
            }

            // v1.68.2: 이미지 첨부 섹션 (편집 모드일 때만, 간편 모드 제외)
            if (reminder != null && !simpleMode) {
                ImageAttachmentSection(
                    images = images.value,
                    onAddImageClick = { imagePickerLauncher.launch("image/*") },
                    onImageClick = { uri -> selectedImageUri = uri },
                    onImageDelete = { attachmentViewModel.deleteImage(it) }
                )
            }
        }

            // 버튼을 Box 하단에 고정
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        // v1.66.0: hasTime 계산 (시간이 명시적으로 선택되었는지 여부)
                        val hasTime = selectedTime != null

                        val dueDateTime = if (selectedDate != null && selectedTime != null) {
                            LocalDateTime.of(selectedDate, selectedTime)
                        } else if (selectedDate != null) {
                            LocalDateTime.of(selectedDate, LocalTime.of(0, 0))
                        } else {
                            null
                        }

                        // v1.22.0: 위치 데이터 파싱
                        val lat = locationLatitude.toDoubleOrNull()
                        val lon = locationLongitude.toDoubleOrNull()
                        val radius = locationRadius.toFloatOrNull()

                        if (reminder == null) {
                            // v1.66.0: 미리 알림 및 hasTime 파라미터 추가
                            viewModel.addReminder(
                                title = title,
                                description = description,
                                priority = priority,
                                category = category,
                                dueDateTime = dueDateTime,
                                recurrenceRule = recurrenceRule,
                                recurrenceEnd = recurrenceEnd,
                                advanceNotificationMinutes = advanceNotificationMinutes,
                                hasTime = hasTime,
                                // v1.67.0: 위치 파라미터 추가
                                locationLatitude = lat,
                                locationLongitude = lon,
                                locationName = locationName.ifBlank { null },
                                locationRadius = radius
                            )
                        } else {
                            // v1.65.0: RecurrenceRule 필드 추가
                            var updatedReminder = reminder.copy(
                                title = title,
                                description = description,
                                priority = priority,
                                urgency = urgency,  // v1.47.0
                                category = category,
                                dueDateTime = dueDateTime,
                                updatedAt = LocalDateTime.now(),
                                // v1.22.0: 위치 필드
                                locationLatitude = lat,
                                locationLongitude = lon,
                                locationName = locationName.ifBlank { null },
                                locationRadius = radius,
                                // v1.23.0: 웹 링크
                                webLink = webLink.ifBlank { null },
                                // v1.24.0: TTS 자동 읽기
                                readAloud = readAloud,
                                // v1.65.0: RecurrenceRule
                                recurrenceRule = recurrenceRule,
                                recurrenceEnd = recurrenceEnd,
                                // v1.66.0: 미리 알림 및 hasTime
                                advanceNotificationMinutes = advanceNotificationMinutes,
                                hasTime = hasTime
                            )
                            viewModel.updateReminder(updatedReminder)
                        }
                        onNavigateBack()
                    }
                },
                modifier = Modifier
                    .align(androidx.compose.ui.Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                enabled = title.isNotBlank()
            ) {
                Text(if (reminder == null) "리마인더 추가" else "리마인더 수정")
            }
        }
    }
}
