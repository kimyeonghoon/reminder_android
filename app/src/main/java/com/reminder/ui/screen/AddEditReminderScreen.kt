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

    // v1.22.0: 위치 관련
    var locationLatitude by remember { mutableStateOf(reminder?.locationLatitude?.toString() ?: "") }
    var locationLongitude by remember { mutableStateOf(reminder?.locationLongitude?.toString() ?: "") }
    var locationName by remember { mutableStateOf(reminder?.locationName ?: "") }
    var locationRadius by remember { mutableStateOf(reminder?.locationRadius?.toString() ?: "100") }

    // v1.67.0: 카카오 장소 검색 결과
    var locationSearchResults by remember { mutableStateOf<List<KakaoPlace>>(emptyList()) }

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
    LaunchedEffect(locationName) {
        if (locationName.length >= 2) {
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
                ExposedDropdownMenuBox(
                    expanded = priorityExpanded,
                    onExpandedChange = { priorityExpanded = !priorityExpanded }
                ) {
                    OutlinedTextField(
                        value = when (priority) {
                            Priority.HIGH -> "높음"
                            Priority.MEDIUM -> "중간"
                            Priority.LOW -> "낮음"
                        },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("중요도") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = priorityExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )

                    ExposedDropdownMenu(
                        expanded = priorityExpanded,
                        onDismissRequest = { priorityExpanded = false }
                    ) {
                        Priority.entries.forEach { p ->
                            DropdownMenuItem(
                                text = { Text(when (p) {
                                    Priority.HIGH -> "높음"
                                    Priority.MEDIUM -> "중간"
                                    Priority.LOW -> "낮음"
                                }) },
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
                        value = when (urgency) {
                            Urgency.HIGH -> "높음"
                            Urgency.MEDIUM -> "중간"
                            Urgency.LOW -> "낮음"
                        },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("긴급도") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = urgencyExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )

                    ExposedDropdownMenu(
                        expanded = urgencyExpanded,
                        onDismissRequest = { urgencyExpanded = false }
                    ) {
                        Urgency.entries.forEach { u ->
                            DropdownMenuItem(
                                text = { Text(when (u) {
                                    Urgency.HIGH -> "높음"
                                    Urgency.MEDIUM -> "중간"
                                    Urgency.LOW -> "낮음"
                                }) },
                                onClick = {
                                    urgency = u
                                    urgencyExpanded = false
                                }
                            )
                        }
                    }
                }
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
                ExposedDropdownMenuBox(
                    expanded = advanceNotificationExpanded,
                    onExpandedChange = { advanceNotificationExpanded = !advanceNotificationExpanded }
                ) {
                    OutlinedTextField(
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
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("⏰ 미리 알림") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = advanceNotificationExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )

                    ExposedDropdownMenu(
                        expanded = advanceNotificationExpanded,
                        onDismissRequest = { advanceNotificationExpanded = false }
                    ) {
                        val options = listOf(
                            null to "없음",
                            5 to "5분 전",
                            10 to "10분 전",
                            15 to "15분 전",
                            30 to "30분 전",
                            60 to "1시간 전",
                            120 to "2시간 전",
                            1440 to "1일 전"
                        )
                        options.forEach { (minutes, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    advanceNotificationMinutes = minutes
                                    advanceNotificationExpanded = false
                                }
                            )
                        }
                    }
                }
            }

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

            // v1.67.0: 위치 검색 (카카오 로컬 API) - 간편 모드에서도 표시
            HorizontalDivider()
            Text(
                text = "📍 위치 기반 알림 (선택사항)",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )

            // 위치 이름 검색 TextField
            OutlinedTextField(
                value = locationName,
                onValueChange = {
                    locationName = it
                    // 검색은 LaunchedEffect에서 자동으로 실행됨 (debounced)
                },
                label = { Text("위치 이름") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("예: 스타벅스 강남점") },
                supportingText = {
                    Text("2글자 이상 입력하면 자동으로 검색됩니다")
                }
            )

            // 검색 결과 목록 (있을 때만 표시)
            if (locationSearchResults.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                    ) {
                        locationSearchResults.forEach { place ->
                            ListItem(
                                headlineContent = { Text(place.placeName) },
                                supportingContent = { Text(place.addressName) },
                                modifier = Modifier.clickable {
                                    // 선택 시 위치 정보 자동 입력
                                    locationName = place.placeName
                                    locationLatitude = place.latitude
                                    locationLongitude = place.longitude
                                    locationSearchResults = emptyList() // 결과 목록 닫기
                                },
                                colors = ListItemDefaults.colors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                            if (place != locationSearchResults.last()) {
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }

            // 위치 상태 표시
            if (locationLatitude.isNotBlank() && locationLongitude.isNotBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Geofencing 활성화됨 (좌표: ${locationLatitude.take(8)}, ${locationLongitude.take(9)})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                }

                // v1.68.0: 지도에서 보기 버튼
                OutlinedButton(
                    onClick = {
                        val lat = locationLatitude.toDoubleOrNull()
                        val lon = locationLongitude.toDoubleOrNull()
                        if (lat != null && lon != null) {
                            onNavigateToMap(lat, lon, locationName.ifBlank { "선택한 위치" })
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("🗺️ 지도에서 위치 확인")
                }
            } else if (locationName.isNotBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "위치 메모만 저장됨 (알림 없음)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 반경 설정 (Geofencing이 활성화된 경우에만 표시)
            if (locationLatitude.isNotBlank() && locationLongitude.isNotBlank()) {
                OutlinedTextField(
                    value = locationRadius,
                    onValueChange = { locationRadius = it },
                    label = { Text("반경 (미터)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("100") }
                )
            }

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
                    onRecurrenceEndChange = { recurrenceEnd = it },
                    startDateTime = if (selectedDate != null && selectedTime != null) {
                        LocalDateTime.of(selectedDate, selectedTime)
                    } else null
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
