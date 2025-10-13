package com.reminder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reminder.analytics.AnalyticsHelper
import com.reminder.data.entity.Priority
import com.reminder.data.entity.ReminderEntity
import com.reminder.data.repository.ReminderRepository
import com.reminder.domain.moveToQuadrant
import com.reminder.notification.AlarmScheduler
import com.reminder.snooze.SnoozeManager
import com.reminder.snooze.SnoozeOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class ReminderViewModel(
    private val repository: ReminderRepository,
    private val alarmScheduler: AlarmScheduler,
    private val database: com.reminder.data.database.ReminderDatabase,
    private val analyticsHelper: AnalyticsHelper,
    private val snoozeManager: SnoozeManager,
    private val locationManager: com.reminder.location.LocationManager,
    private val ttsHelper: com.reminder.tts.TtsHelper,
    private val categorySuggestionHelper: com.reminder.ml.CategorySuggestionHelper,
    private val completionPatternAnalyzer: com.reminder.analytics.CompletionPatternAnalyzer
) : ViewModel() {

    val allReminders: StateFlow<List<ReminderEntity>> = repository.allReminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeReminders: StateFlow<List<ReminderEntity>> = repository.activeReminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completedReminders: StateFlow<List<ReminderEntity>> = repository.completedReminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedReminder = MutableStateFlow<ReminderEntity?>(null)
    val selectedReminder: StateFlow<ReminderEntity?> = _selectedReminder.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun addReminder(
        title: String,
        description: String = "",
        dueDateTime: LocalDateTime? = null,
        priority: Priority = Priority.MEDIUM,
        category: String = "",
        recurrenceRule: com.reminder.recurrence.RecurrenceRule? = null,
        recurrenceEnd: com.reminder.recurrence.RecurrenceEnd? = null,
        advanceNotificationMinutes: Int? = null,  // v1.66.0: 미리 알림
        hasTime: Boolean = true,  // v1.66.0: 시간 설정 여부
        // v1.67.0: 위치 파라미터 추가
        locationLatitude: Double? = null,
        locationLongitude: Double? = null,
        locationName: String? = null,
        locationRadius: Float? = null
    ) {
        viewModelScope.launch {
            val reminder = ReminderEntity(
                title = title,
                description = description,
                dueDateTime = dueDateTime,
                priority = priority,
                category = category,
                recurrenceRule = recurrenceRule,
                recurrenceEnd = recurrenceEnd,
                advanceNotificationMinutes = advanceNotificationMinutes,  // v1.66.0
                hasTime = hasTime,  // v1.66.0
                // v1.67.0: 위치 필드
                locationLatitude = locationLatitude,
                locationLongitude = locationLongitude,
                locationName = locationName,
                locationRadius = locationRadius
            )
            val reminderId = repository.insertReminder(reminder)

            // 알람 스케줄링
            if (dueDateTime != null) {
                alarmScheduler.schedule(reminder.copy(id = reminderId))
            }

            // v1.67.0: 지오펜스 등록 (위치가 있으면)
            if (locationLatitude != null && locationLongitude != null && locationRadius != null) {
                locationManager.setupGeofence(
                    reminderId = reminderId,
                    latitude = locationLatitude,
                    longitude = locationLongitude,
                    radius = locationRadius
                )
            }

            // Analytics 이벤트 로깅
            analyticsHelper.logReminderCreated(
                priority = priority,
                category = category,
                hasRecurrence = recurrenceRule != null
            )
        }
    }

    fun updateReminder(reminder: ReminderEntity) {
        viewModelScope.launch {
            val updatedReminder = reminder.copy(updatedAt = LocalDateTime.now())
            repository.updateReminder(updatedReminder)

            // 알람 재스케줄링
            alarmScheduler.cancel(updatedReminder.id)
            if (updatedReminder.dueDateTime != null && !updatedReminder.isCompleted) {
                alarmScheduler.schedule(updatedReminder)
            }

            // v1.67.0: 지오펜스 재등록 (위치가 있으면)
            val lat = updatedReminder.locationLatitude
            val lon = updatedReminder.locationLongitude
            val radius = updatedReminder.locationRadius

            if (lat != null && lon != null && radius != null) {
                // 기존 지오펜스 제거 후 재등록
                locationManager.removeGeofence(updatedReminder.id)
                locationManager.setupGeofence(
                    reminderId = updatedReminder.id,
                    latitude = lat,
                    longitude = lon,
                    radius = radius
                )
            } else {
                // 위치가 없으면 지오펜스 제거
                locationManager.removeGeofence(updatedReminder.id)
            }

            // Analytics 이벤트 로깅
            analyticsHelper.logReminderEdited()
        }
    }

    fun deleteReminder(reminder: ReminderEntity) {
        viewModelScope.launch {
            repository.deleteReminder(reminder)
            // 알람 취소
            alarmScheduler.cancel(reminder.id)
            // v1.67.0: 지오펜스 제거
            locationManager.removeGeofence(reminder.id)

            // Analytics 이벤트 로깅
            analyticsHelper.logReminderDeleted()
        }
    }

    fun toggleReminderCompletion(reminder: ReminderEntity) {
        viewModelScope.launch {
            repository.toggleReminderCompletion(reminder)

            // 완료되면 알람 취소, 완료 취소되면 알람 재스케줄링
            if (!reminder.isCompleted) {
                // 완료로 변경되면 알람 취소
                alarmScheduler.cancel(reminder.id)

                // Analytics 이벤트 로깅 (완료 시에만)
                val daysUntilDue = reminder.dueDateTime?.let {
                    ChronoUnit.DAYS.between(LocalDateTime.now(), it).toInt()
                }
                analyticsHelper.logReminderCompleted(daysUntilDue)
            } else {
                // 완료 취소되면 알람 재스케줄링
                if (reminder.dueDateTime != null) {
                    alarmScheduler.schedule(reminder)
                }
            }
        }
    }

    fun deleteAllCompletedReminders() {
        viewModelScope.launch {
            repository.deleteAllCompletedReminders()
        }
    }

    fun selectReminder(reminder: ReminderEntity?) {
        _selectedReminder.value = reminder
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query

        // Analytics 이벤트 로깅 (검색어가 비어있지 않을 때만)
        if (query.isNotBlank()) {
            analyticsHelper.logSearchPerformed(query.length)
        }
    }

    fun getFilteredReminders(reminders: List<ReminderEntity>, query: String): List<ReminderEntity> {
        if (query.isBlank()) return reminders
        return reminders.filter {
            it.title.contains(query, ignoreCase = true) ||
                    it.description.contains(query, ignoreCase = true) ||
                    it.category.contains(query, ignoreCase = true) ||
                    it.tags.contains(query, ignoreCase = true)
        }
    }

    fun filterByTag(reminders: List<ReminderEntity>, tag: String): List<ReminderEntity> {
        if (tag.isBlank()) return reminders
        return reminders.filter {
            it.tags.split(",").any { it.trim().equals(tag, ignoreCase = true) }
        }
    }

    fun filterByPriority(reminders: List<ReminderEntity>, filter: com.reminder.data.entity.FilterPriority): List<ReminderEntity> {
        return when (filter) {
            com.reminder.data.entity.FilterPriority.ALL -> reminders
            com.reminder.data.entity.FilterPriority.HIGH -> reminders.filter { it.priority == Priority.HIGH }
            com.reminder.data.entity.FilterPriority.MEDIUM -> reminders.filter { it.priority == Priority.MEDIUM }
            com.reminder.data.entity.FilterPriority.LOW -> reminders.filter { it.priority == Priority.LOW }
        }
    }

    fun filterByDate(reminders: List<ReminderEntity>, filter: com.reminder.data.entity.FilterDate): List<ReminderEntity> {
        val now = LocalDateTime.now()
        val today = now.toLocalDate()
        val startOfWeek = today.minusDays(today.dayOfWeek.value.toLong() - 1)
        val endOfWeek = startOfWeek.plusDays(6)
        val startOfMonth = today.withDayOfMonth(1)
        val endOfMonth = today.withDayOfMonth(today.lengthOfMonth())

        return when (filter) {
            com.reminder.data.entity.FilterDate.ALL -> reminders
            com.reminder.data.entity.FilterDate.TODAY -> reminders.filter {
                it.dueDateTime?.toLocalDate() == today
            }
            com.reminder.data.entity.FilterDate.THIS_WEEK -> reminders.filter {
                val dueDate = it.dueDateTime?.toLocalDate()
                dueDate != null && !dueDate.isBefore(startOfWeek) && !dueDate.isAfter(endOfWeek)
            }
            com.reminder.data.entity.FilterDate.THIS_MONTH -> reminders.filter {
                val dueDate = it.dueDateTime?.toLocalDate()
                dueDate != null && !dueDate.isBefore(startOfMonth) && !dueDate.isAfter(endOfMonth)
            }
            com.reminder.data.entity.FilterDate.OVERDUE -> reminders.filter {
                it.dueDateTime != null && it.dueDateTime.isBefore(now) && !it.isCompleted
            }
        }
    }

    fun sortReminders(reminders: List<ReminderEntity>, sortOption: com.reminder.data.entity.SortOption): List<ReminderEntity> {
        return when (sortOption) {
            com.reminder.data.entity.SortOption.BY_DATE_ASC -> reminders.sortedWith(
                compareBy(nullsLast()) { it.dueDateTime }
            )
            com.reminder.data.entity.SortOption.BY_DATE_DESC -> reminders.sortedWith(
                compareByDescending(nullsLast()) { it.dueDateTime }
            )
            com.reminder.data.entity.SortOption.BY_PRIORITY_HIGH_FIRST -> reminders.sortedBy {
                when (it.priority) {
                    Priority.HIGH -> 0
                    Priority.MEDIUM -> 1
                    Priority.LOW -> 2
                }
            }
            com.reminder.data.entity.SortOption.BY_PRIORITY_LOW_FIRST -> reminders.sortedBy {
                when (it.priority) {
                    Priority.LOW -> 0
                    Priority.MEDIUM -> 1
                    Priority.HIGH -> 2
                }
            }
            com.reminder.data.entity.SortOption.BY_TITLE_ASC -> reminders.sortedBy { it.title.lowercase() }
            com.reminder.data.entity.SortOption.BY_TITLE_DESC -> reminders.sortedByDescending { it.title.lowercase() }
            com.reminder.data.entity.SortOption.BY_CREATED_ASC -> reminders.sortedBy { it.createdAt }
            com.reminder.data.entity.SortOption.BY_CREATED_DESC -> reminders.sortedByDescending { it.createdAt }
        }
    }

    // ==================== 서브태스크 관련 함수 ====================

    /**
     * 리마인더의 서브태스크 목록 조회
     */
    fun getSubTasks(reminderId: Long) =
        database.subTaskDao().getSubTasksByReminderId(reminderId)

    /**
     * 서브태스크 추가
     */
    fun addSubTask(reminderId: Long, title: String) {
        viewModelScope.launch {
            val subTask = com.reminder.data.entity.SubTask(
                reminderId = reminderId,
                title = title,
                position = database.subTaskDao().getTotalSubTasksCount(reminderId)
            )
            database.subTaskDao().insert(subTask)

            // Analytics 이벤트 로깅
            analyticsHelper.logSubtaskAdded()
        }
    }

    /**
     * 서브태스크 완료 상태 토글
     */
    fun toggleSubTaskCompletion(subTask: com.reminder.data.entity.SubTask) {
        viewModelScope.launch {
            val updated = subTask.copy(isCompleted = !subTask.isCompleted)
            database.subTaskDao().update(updated)
        }
    }

    /**
     * 서브태스크 삭제
     */
    fun deleteSubTask(subTask: com.reminder.data.entity.SubTask) {
        viewModelScope.launch {
            database.subTaskDao().delete(subTask)
        }
    }

    /**
     * 서브태스크 재정렬 (드래그 앤 드롭)
     */
    fun reorderSubTasks(subTasks: List<com.reminder.data.entity.SubTask>) {
        viewModelScope.launch {
            // position 값을 새로운 순서로 업데이트
            val reorderedSubTasks = subTasks.mapIndexed { index, subTask ->
                subTask.copy(position = index)
            }
            database.subTaskDao().updateAll(reorderedSubTasks)
        }
    }

    /**
     * 서브태스크 진행률 계산 (완료/전체)
     */
    suspend fun getSubTaskProgress(reminderId: Long): Pair<Int, Int> {
        val completed = database.subTaskDao().getCompletedSubTasksCount(reminderId)
        val total = database.subTaskDao().getTotalSubTasksCount(reminderId)
        return Pair(completed, total)
    }

    // ==================== 이미지 첨부 관련 함수 ====================

    /**
     * 리마인더의 이미지 목록 조회
     */
    fun getImages(reminderId: Long) =
        database.reminderImageDao().getImagesByReminderId(reminderId)

    /**
     * 이미지 추가
     */
    fun addImage(reminderId: Long, imageUri: String) {
        viewModelScope.launch {
            val image = com.reminder.data.entity.ReminderImage(
                reminderId = reminderId,
                imageUri = imageUri
            )
            database.reminderImageDao().insert(image)

            // Analytics 이벤트 로깅
            analyticsHelper.logImageAttached()
        }
    }

    /**
     * 이미지 삭제
     */
    fun deleteImage(image: com.reminder.data.entity.ReminderImage) {
        viewModelScope.launch {
            database.reminderImageDao().delete(image)
        }
    }

    // ==================== 완료 이력 관련 함수 ====================

    /**
     * 특정 날짜에 완료된 리마인더 조회
     */
    suspend fun getCompletedRemindersByDate(date: LocalDateTime): List<ReminderEntity> {
        return repository.getCompletedRemindersByDate(date)
    }

    /**
     * 날짜 범위 내 완료된 리마인더 조회
     */
    suspend fun getCompletedRemindersInRange(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): List<ReminderEntity> {
        return repository.getCompletedRemindersInRange(startDate, endDate)
    }

    /**
     * 월별 완료 개수 맵 생성 (날짜 -> 완료 개수)
     */
    suspend fun getCompletionCountByDay(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Map<LocalDateTime, Int> {
        val reminders = getCompletedRemindersInRange(startDate, endDate)
        return reminders
            .groupBy { it.updatedAt.toLocalDate().atStartOfDay() }
            .mapValues { it.value.size }
    }

    // ==================== 템플릿 관련 함수 ====================

    /**
     * 모든 템플릿 조회
     */
    fun getAllTemplates() = database.reminderTemplateDao().getAllTemplates()

    /**
     * 템플릿 추가
     *
     * v1.64.0: RecurrenceRule 사용
     */
    fun addTemplate(
        name: String,
        titleTemplate: String,
        descriptionTemplate: String = "",
        defaultPriority: Priority = Priority.MEDIUM,
        defaultCategory: String = "",
        defaultRecurrenceRule: com.reminder.recurrence.RecurrenceRule? = null,
        defaultRecurrenceEnd: com.reminder.recurrence.RecurrenceEnd? = null
    ) {
        viewModelScope.launch {
            val template = com.reminder.data.entity.ReminderTemplate(
                name = name,
                titleTemplate = titleTemplate,
                descriptionTemplate = descriptionTemplate,
                defaultPriority = defaultPriority,
                defaultCategory = defaultCategory,
                defaultRecurrenceRule = defaultRecurrenceRule,
                defaultRecurrenceEnd = defaultRecurrenceEnd
            )
            database.reminderTemplateDao().insert(template)

            // Analytics 이벤트 로깅
            analyticsHelper.logTemplateCreated()
        }
    }

    /**
     * 템플릿에서 리마인더 생성
     *
     * v1.64.0: RecurrenceRule 사용
     */
    fun createReminderFromTemplate(template: com.reminder.data.entity.ReminderTemplate, dueDateTime: LocalDateTime? = null) {
        viewModelScope.launch {
            val reminder = ReminderEntity(
                title = template.titleTemplate,
                description = template.descriptionTemplate,
                dueDateTime = dueDateTime,
                priority = template.defaultPriority,
                category = template.defaultCategory,
                recurrenceRule = template.defaultRecurrenceRule,
                recurrenceEnd = template.defaultRecurrenceEnd
            )
            val reminderId = repository.insertReminder(reminder)

            // 알람 스케줄링
            if (dueDateTime != null) {
                alarmScheduler.schedule(reminder.copy(id = reminderId))
            }

            // Analytics 이벤트 로깅
            analyticsHelper.logTemplateUsed(template.name)
        }
    }

    /**
     * 템플릿 삭제
     */
    fun deleteTemplate(template: com.reminder.data.entity.ReminderTemplate) {
        viewModelScope.launch {
            database.reminderTemplateDao().delete(template)
        }
    }

    /**
     * 현재 리마인더를 템플릿으로 저장
     *
     * v1.64.0: RecurrenceRule 사용
     */
    fun saveAsTemplate(
        reminder: ReminderEntity,
        templateName: String
    ) {
        viewModelScope.launch {
            val template = com.reminder.data.entity.ReminderTemplate(
                name = templateName,
                titleTemplate = reminder.title,
                descriptionTemplate = reminder.description,
                defaultPriority = reminder.priority,
                defaultCategory = reminder.category,
                defaultRecurrenceRule = reminder.recurrenceRule,
                defaultRecurrenceEnd = reminder.recurrenceEnd
            )
            database.reminderTemplateDao().insert(template)
        }
    }

    // ==================== 배치 작업 관련 함수 ====================

    /**
     * 여러 리마인더 삭제
     */
    fun deleteReminders(reminders: List<ReminderEntity>) {
        viewModelScope.launch {
            reminders.forEach { reminder ->
                repository.deleteReminder(reminder)
                alarmScheduler.cancel(reminder.id)
            }

            // Analytics 이벤트 로깅
            analyticsHelper.logBatchOperation("delete", reminders.size)
        }
    }

    /**
     * 여러 리마인더 완료 처리
     */
    fun completeReminders(reminders: List<ReminderEntity>) {
        viewModelScope.launch {
            reminders.forEach { reminder ->
                if (!reminder.isCompleted) {
                    repository.toggleReminderCompletion(reminder)
                    alarmScheduler.cancel(reminder.id)
                }
            }

            // Analytics 이벤트 로깅
            analyticsHelper.logBatchOperation("complete", reminders.size)
        }
    }

    // ==================== 복제 관련 함수 ====================

    /**
     * 리마인더 복제
     */
    fun duplicateReminder(reminder: ReminderEntity) {
        viewModelScope.launch {
            val duplicated = reminder.copy(
                id = 0, // 새 ID 생성
                title = "${reminder.title} (복사본)",
                isCompleted = false,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
            val newId = repository.insertReminder(duplicated)

            // 알람 스케줄링
            if (duplicated.dueDateTime != null) {
                alarmScheduler.schedule(duplicated.copy(id = newId))
            }
        }
    }

    // ==================== 스누즈 관련 함수 ====================

    /**
     * 리마인더 스누즈
     */
    fun snoozeReminder(reminderId: Long, option: SnoozeOption) {
        viewModelScope.launch {
            snoozeManager.snoozeReminder(reminderId, option)
        }
    }

    /**
     * 스누즈 취소
     */
    fun cancelSnooze(reminderId: Long) {
        viewModelScope.launch {
            snoozeManager.cancelSnooze(reminderId)
        }
    }

    /**
     * 스누즈된 리마인더 목록 조회
     */
    val snoozedReminders = database.reminderDao().getSnoozedReminders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ==================== 위치 기반 리마인더 관련 함수 ====================

    /**
     * 위치 권한 확인
     */
    fun hasLocationPermission(): Boolean {
        return locationManager.hasLocationPermission()
    }

    /**
     * 백그라운드 위치 권한 확인
     */
    fun hasBackgroundLocationPermission(): Boolean {
        return locationManager.hasBackgroundLocationPermission()
    }

    /**
     * 현재 위치 가져오기
     */
    suspend fun getCurrentLocation(): Pair<Double, Double>? {
        return try {
            locationManager.getCurrentLocation()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 마지막으로 알려진 위치 가져오기
     */
    suspend fun getLastKnownLocation(): Pair<Double, Double>? {
        return locationManager.getLastKnownLocation()
    }

    /**
     * 리마인더에 위치 추가
     *
     * v1.67.0: 지오펜스 자동 등록
     */
    fun addLocationToReminder(
        reminder: ReminderEntity,
        latitude: Double,
        longitude: Double,
        locationName: String,
        radius: Float = com.reminder.location.LocationManager.DEFAULT_RADIUS
    ) {
        viewModelScope.launch {
            val updated = reminder.copy(
                locationLatitude = latitude,
                locationLongitude = longitude,
                locationName = locationName,
                locationRadius = radius,
                updatedAt = LocalDateTime.now()
            )
            repository.updateReminder(updated)

            // v1.67.0: 지오펜스 등록
            locationManager.setupGeofence(
                reminderId = updated.id,
                latitude = latitude,
                longitude = longitude,
                radius = radius
            )

            // Analytics 이벤트 로깅
            analyticsHelper.logLocationAdded()
        }
    }

    /**
     * 리마인더에서 위치 제거
     *
     * v1.67.0: 지오펜스 자동 제거
     */
    fun removeLocationFromReminder(reminder: ReminderEntity) {
        viewModelScope.launch {
            val updated = reminder.copy(
                locationLatitude = null,
                locationLongitude = null,
                locationName = null,
                locationRadius = null,
                updatedAt = LocalDateTime.now()
            )
            repository.updateReminder(updated)

            // v1.67.0: 지오펜스 제거
            locationManager.removeGeofence(reminder.id)
        }
    }

    /**
     * 현재 위치가 리마인더 위치 범위 내에 있는지 확인
     */
    suspend fun isWithinReminderRadius(reminder: ReminderEntity): Boolean {
        val lat = reminder.locationLatitude ?: return false
        val lon = reminder.locationLongitude ?: return false
        val radius = reminder.locationRadius ?: com.reminder.location.LocationManager.DEFAULT_RADIUS

        return locationManager.isWithinRadius(lat, lon, radius)
    }

    // ==================== 웹 링크 관련 함수 ====================

    /**
     * URL이 유효한지 확인
     */
    fun isValidUrl(url: String?): Boolean {
        return com.reminder.util.UrlValidator.isValidUrl(url)
    }

    /**
     * URL 정규화 (http:// 또는 https:// 접두사 추가)
     */
    fun normalizeUrl(url: String): String {
        return com.reminder.util.UrlValidator.normalizeUrl(url)
    }

    /**
     * 리마인더에 웹 링크 추가
     */
    fun addWebLinkToReminder(reminder: ReminderEntity, webLink: String) {
        viewModelScope.launch {
            val normalizedUrl = normalizeUrl(webLink)
            val updated = reminder.copy(
                webLink = normalizedUrl,
                updatedAt = LocalDateTime.now()
            )
            repository.updateReminder(updated)

            // Analytics 이벤트 로깅
            analyticsHelper.logWebLinkAdded()
        }
    }

    /**
     * 리마인더에서 웹 링크 제거
     */
    fun removeWebLinkFromReminder(reminder: ReminderEntity) {
        viewModelScope.launch {
            val updated = reminder.copy(
                webLink = null,
                updatedAt = LocalDateTime.now()
            )
            repository.updateReminder(updated)
        }
    }

    // ==================== TTS (음성 알림) 관련 함수 ====================

    /**
     * TTS 초기화
     */
    fun initializeTts(
        onSuccess: (() -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        ttsHelper.initialize(onSuccess, onError)
    }

    /**
     * 리마인더 내용을 음성으로 읽기
     */
    fun speakReminder(
        reminder: ReminderEntity,
        onStart: (() -> Unit)? = null,
        onDone: (() -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        ttsHelper.speakReminder(
            title = reminder.title,
            description = reminder.description,
            onStart = onStart,
            onDone = onDone,
            onError = onError
        )

        // Analytics 이벤트 로깅
        analyticsHelper.logTtsUsed()
    }

    /**
     * TTS 읽기 중지
     */
    fun stopTts() {
        ttsHelper.stop()
    }

    /**
     * TTS가 현재 말하고 있는지 확인
     */
    fun isTtsSpeaking(): Boolean {
        return ttsHelper.isSpeaking()
    }

    /**
     * 리마인더의 자동 읽기 설정 변경
     */
    fun toggleReadAloud(reminder: ReminderEntity) {
        viewModelScope.launch {
            val updated = reminder.copy(
                readAloud = !reminder.readAloud,
                updatedAt = LocalDateTime.now()
            )
            repository.updateReminder(updated)

            // Analytics 이벤트 로깅
            if (updated.readAloud) {
                analyticsHelper.logReadAloudEnabled()
            }
        }
    }

    /**
     * ViewModel 정리 시 TTS 리소스 해제
     */
    override fun onCleared() {
        super.onCleared()
        ttsHelper.shutdown()
    }

    // ==================== 카테고리 제안 (ML) 관련 함수 ====================

    /**
     * 제목과 설명을 기반으로 카테고리 제안
     */
    suspend fun suggestCategories(title: String, description: String = ""): List<String> {
        val allReminders = repository.getAllRemindersList()
        val suggestions = categorySuggestionHelper.suggestCategories(title, description, allReminders)

        // Analytics 이벤트 로깅
        if (suggestions.isNotEmpty()) {
            analyticsHelper.logCategorySuggested(suggestions.size)
        }

        return suggestions
    }

    /**
     * 모든 고유 카테고리 목록 조회
     */
    suspend fun getAllCategories(): List<String> {
        val allReminders = repository.getAllRemindersList()
        return categorySuggestionHelper.getAllCategories(allReminders)
    }

    /**
     * 카테고리 사용 빈도 조회
     */
    suspend fun getCategoryFrequency(): Map<String, Int> {
        val allReminders = repository.getAllRemindersList()
        return categorySuggestionHelper.getCategoryFrequency(allReminders)
    }

    /**
     * 기본 카테고리 목록 반환
     */
    fun getDefaultCategories(): List<String> {
        return com.reminder.ml.CategorySuggestionHelper.DEFAULT_CATEGORIES
    }

    // ==================== 완료 패턴 분석 관련 함수 ====================

    /**
     * 완료 패턴 분석
     */
    suspend fun analyzeCompletionPattern(): com.reminder.analytics.CompletionPatternAnalyzer.CompletionPattern? {
        val allReminders = repository.getAllRemindersList()
        val pattern = completionPatternAnalyzer.analyzeCompletionPattern(allReminders)

        // Analytics 이벤트 로깅
        if (pattern != null) {
            analyticsHelper.logPatternAnalyzed(pattern.completionRate)
        }

        return pattern
    }

    /**
     * 최적의 리마인더 시간 제안
     */
    suspend fun suggestOptimalTime(dueDate: java.time.LocalDate): LocalDateTime {
        val pattern = analyzeCompletionPattern()
        return completionPatternAnalyzer.suggestOptimalTime(pattern, dueDate)
    }

    /**
     * 완료하기 좋은 시간대 목록
     */
    suspend fun getBestCompletionHours(): List<Int> {
        val pattern = analyzeCompletionPattern()
        return completionPatternAnalyzer.getBestCompletionHours(pattern)
    }

    /**
     * 완료하기 좋은 요일 목록
     */
    suspend fun getBestCompletionDays(): List<java.time.DayOfWeek> {
        val pattern = analyzeCompletionPattern()
        return completionPatternAnalyzer.getBestCompletionDays(pattern)
    }

    /**
     * 완료 패턴 요약 텍스트
     */
    suspend fun getPatternSummary(): String {
        val pattern = analyzeCompletionPattern()
        return completionPatternAnalyzer.getPatternSummary(pattern)
    }

    /**
     * 특정 시간대의 완료 확률
     */
    suspend fun getCompletionProbabilityByHour(hour: Int): Double {
        val pattern = analyzeCompletionPattern()
        return completionPatternAnalyzer.getCompletionProbability(pattern, hour)
    }

    /**
     * 특정 요일의 완료 확률
     */
    suspend fun getCompletionProbabilityByDay(day: java.time.DayOfWeek): Double {
        val pattern = analyzeCompletionPattern()
        return completionPatternAnalyzer.getCompletionProbability(pattern, day)
    }

    // ==================== 고급 필터 시스템 (v1.32.0) ====================

    private val filterEngine = com.reminder.filter.FilterEngine()

    /**
     * 현재 적용된 필터
     */
    private val _currentFilter = MutableStateFlow<com.reminder.filter.ReminderFilter?>(null)
    val currentFilter: StateFlow<com.reminder.filter.ReminderFilter?> = _currentFilter.asStateFlow()

    /**
     * 저장된 필터 목록 조회
     */
    val savedFilters = database.savedFilterDao().getAllSavedFilters()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * 필터를 적용하여 필터링된 리마인더 목록 반환
     */
    fun applyFilter(filter: com.reminder.filter.ReminderFilter) {
        _currentFilter.value = filter

        // Analytics 이벤트 로깅
        analyticsHelper.logFilterApplied()
    }

    /**
     * 필터 초기화
     */
    fun clearFilter() {
        _currentFilter.value = null

        // Analytics 이벤트 로깅
        analyticsHelper.logFilterCleared()
    }

    /**
     * 현재 필터가 적용된 리마인더 목록 반환
     */
    fun getFilteredRemindersWithFilter(reminders: List<ReminderEntity>): List<ReminderEntity> {
        val filter = _currentFilter.value ?: return reminders
        return filterEngine.applyFilter(reminders, filter)
    }

    /**
     * 필터 프리셋 적용
     */
    fun applyFilterPreset(presetId: String) {
        val preset = com.reminder.filter.FilterPresets.getPresetById(presetId)
        if (preset != null) {
            applyFilter(preset.filter)

            // Analytics 이벤트 로깅
            analyticsHelper.logPresetUsed(presetId)
        }
    }

    /**
     * 필터를 저장된 필터로 저장
     */
    fun saveFilter(name: String, icon: String, filter: com.reminder.filter.ReminderFilter) {
        viewModelScope.launch {
            val filterJson = serializeFilter(filter)
            val savedFilter = com.reminder.data.entity.SavedFilterEntity(
                name = name,
                icon = icon,
                filterJson = filterJson
            )
            database.savedFilterDao().insertSavedFilter(savedFilter)

            // Analytics 이벤트 로깅
            analyticsHelper.logFilterSaved()
        }
    }

    /**
     * 저장된 필터 삭제
     */
    fun deleteSavedFilter(filter: com.reminder.data.entity.SavedFilterEntity) {
        viewModelScope.launch {
            database.savedFilterDao().deleteSavedFilter(filter)
        }
    }

    /**
     * 저장된 필터 적용
     */
    fun applySavedFilter(savedFilter: com.reminder.data.entity.SavedFilterEntity) {
        val filter = deserializeFilter(savedFilter.filterJson)
        if (filter != null) {
            applyFilter(filter)
        }
    }

    /**
     * ReminderFilter를 JSON으로 직렬화
     */
    private fun serializeFilter(filter: com.reminder.filter.ReminderFilter): String {
        return com.google.gson.Gson().toJson(filter)
    }

    /**
     * JSON을 ReminderFilter로 역직렬화
     */
    private fun deserializeFilter(json: String): com.reminder.filter.ReminderFilter? {
        return try {
            com.google.gson.Gson().fromJson(json, com.reminder.filter.ReminderFilter::class.java)
        } catch (e: Exception) {
            null
        }
    }

    // ==================== Eisenhower Matrix (v1.49.0) ====================

    /**
     * 리마인더를 다른 쿼드런트로 이동
     *
     * @param reminder 이동할 리마인더
     * @param targetQuadrant 목표 쿼드런트
     */
    fun moveReminderToQuadrant(reminder: ReminderEntity, targetQuadrant: com.reminder.domain.Quadrant) {
        viewModelScope.launch {
            val movedReminder = reminder.moveToQuadrant(targetQuadrant)
            repository.updateReminder(movedReminder)

            // 알람 재스케줄링 (필요시)
            alarmScheduler.cancel(movedReminder.id)
            if (movedReminder.dueDateTime != null && !movedReminder.isCompleted) {
                alarmScheduler.schedule(movedReminder)
            }

            // Analytics 이벤트 로깅
            analyticsHelper.logReminderMovedToQuadrant(targetQuadrant.name)
        }
    }
}
