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

/**
 * v1.68.3: 리팩토링된 ReminderViewModel (Facade Pattern)
 *
 * CRUD, 검색/필터/정렬, Analytics 기능을 별도 ViewModel로 분리 완료
 * ReminderViewModel은 하위 호환성을 위해 유지하며, 내부적으로 3개의 전문 ViewModel에 위임합니다.
 *
 * 내부 위임 ViewModel:
 * - crudViewModel: CRUD 핵심 기능
 * - searchViewModel: 검색/필터/정렬
 * - analyticsViewModel: ML 카테고리 제안, 완료 패턴 분석
 *
 * 직접 담당 기능:
 * - 스누즈, 웹링크, TTS
 */
// v1.68.1: open class for mockito testing
open class ReminderViewModel(
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

    // ==================== v1.68.3: 내부 위임 ViewModel ====================
    private val crudViewModel = ReminderCrudViewModel(repository, alarmScheduler, locationManager, analyticsHelper)
    private val searchViewModel = ReminderSearchViewModel(analyticsHelper)
    private val analyticsViewModel = ReminderAnalyticsViewModel(repository, analyticsHelper, categorySuggestionHelper, completionPatternAnalyzer)

    // ==================== v1.68.3: CRUD 함수 (crudViewModel에 위임) ====================
    val allReminders: StateFlow<List<ReminderEntity>> = crudViewModel.allReminders
    val activeReminders: StateFlow<List<ReminderEntity>> = crudViewModel.activeReminders
    val completedReminders: StateFlow<List<ReminderEntity>> = crudViewModel.completedReminders
    val selectedReminder: StateFlow<ReminderEntity?> = crudViewModel.selectedReminder

    fun addReminder(
        title: String,
        description: String = "",
        dueDateTime: LocalDateTime? = null,
        priority: Priority = Priority.MEDIUM,
        category: String = "",
        recurrenceRule: com.reminder.recurrence.RecurrenceRule? = null,
        recurrenceEnd: com.reminder.recurrence.RecurrenceEnd? = null,
        advanceNotificationMinutes: Int? = null,
        hasTime: Boolean = true,
        locationLatitude: Double? = null,
        locationLongitude: Double? = null,
        locationName: String? = null,
        locationRadius: Float? = null
    ) = crudViewModel.addReminder(title, description, dueDateTime, priority, category, recurrenceRule, recurrenceEnd, advanceNotificationMinutes, hasTime, locationLatitude, locationLongitude, locationName, locationRadius)

    fun updateReminder(reminder: ReminderEntity) = crudViewModel.updateReminder(reminder)
    fun deleteReminder(reminder: ReminderEntity) = crudViewModel.deleteReminder(reminder)
    fun toggleReminderCompletion(reminder: ReminderEntity) = crudViewModel.toggleReminderCompletion(reminder)
    fun deleteAllCompletedReminders() = crudViewModel.deleteAllCompletedReminders()
    fun selectReminder(reminder: ReminderEntity?) = crudViewModel.selectReminder(reminder)
    fun deleteReminders(reminders: List<ReminderEntity>) = crudViewModel.deleteReminders(reminders)
    fun completeReminders(reminders: List<ReminderEntity>) = crudViewModel.completeReminders(reminders)
    fun duplicateReminder(reminder: ReminderEntity) = crudViewModel.duplicateReminder(reminder)
    fun createReminderFromTemplate(template: com.reminder.data.entity.ReminderTemplate, dueDateTime: LocalDateTime? = null) = crudViewModel.createReminderFromTemplate(template, dueDateTime)
    fun moveReminderToQuadrant(reminder: ReminderEntity, targetQuadrant: com.reminder.domain.Quadrant) = crudViewModel.moveReminderToQuadrant(reminder, targetQuadrant)

    // ==================== v1.68.3: 검색/필터/정렬 함수 (searchViewModel에 위임) ====================
    val searchQuery: StateFlow<String> = searchViewModel.searchQuery

    fun updateSearchQuery(query: String) = searchViewModel.updateSearchQuery(query)
    fun getFilteredReminders(reminders: List<ReminderEntity>, query: String) = searchViewModel.getFilteredReminders(reminders, query)
    fun filterByTag(reminders: List<ReminderEntity>, tag: String) = searchViewModel.filterByTag(reminders, tag)
    fun filterByPriority(reminders: List<ReminderEntity>, filter: com.reminder.data.entity.FilterPriority) = searchViewModel.filterByPriority(reminders, filter)
    fun filterByDate(reminders: List<ReminderEntity>, filter: com.reminder.data.entity.FilterDate) = searchViewModel.filterByDate(reminders, filter)
    fun sortReminders(reminders: List<ReminderEntity>, sortOption: com.reminder.data.entity.SortOption) = searchViewModel.sortReminders(reminders, sortOption)

    // ==================== v1.68.3: Analytics 함수 (analyticsViewModel에 위임) ====================
    suspend fun getCompletedRemindersByDate(date: LocalDateTime) = analyticsViewModel.getCompletedRemindersByDate(date)
    suspend fun getCompletedRemindersInRange(startDate: LocalDateTime, endDate: LocalDateTime) = analyticsViewModel.getCompletedRemindersInRange(startDate, endDate)
    suspend fun getCompletionCountByDay(startDate: LocalDateTime, endDate: LocalDateTime) = analyticsViewModel.getCompletionCountByDay(startDate, endDate)
    suspend fun suggestCategories(title: String, description: String = "") = analyticsViewModel.suggestCategories(title, description)
    suspend fun getAllCategories() = analyticsViewModel.getAllCategories()
    suspend fun getCategoryFrequency() = analyticsViewModel.getCategoryFrequency()
    fun getDefaultCategories() = analyticsViewModel.getDefaultCategories()
    suspend fun analyzeCompletionPattern() = analyticsViewModel.analyzeCompletionPattern()
    suspend fun suggestOptimalTime(dueDate: java.time.LocalDate) = analyticsViewModel.suggestOptimalTime(dueDate)
    suspend fun getBestCompletionHours() = analyticsViewModel.getBestCompletionHours()
    suspend fun getBestCompletionDays() = analyticsViewModel.getBestCompletionDays()
    suspend fun getPatternSummary() = analyticsViewModel.getPatternSummary()
    suspend fun getCompletionProbabilityByHour(hour: Int) = analyticsViewModel.getCompletionProbabilityByHour(hour)
    suspend fun getCompletionProbabilityByDay(day: java.time.DayOfWeek) = analyticsViewModel.getCompletionProbabilityByDay(day)

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

    // ==================== 웹 링크 관련 함수 ====================

    /**
     * URL이 유효한지 확인
     */
    fun isValidUrl(url: String?): Boolean {
        return com.reminder.utils.UrlValidator.isValidUrl(url)
    }

    /**
     * URL 정규화 (http:// 또는 https:// 접두사 추가)
     */
    fun normalizeUrl(url: String): String {
        return com.reminder.utils.UrlValidator.normalizeUrl(url)
    }

    /**
     * 리마인더에 웹 링크 추가
     */
    fun addWebLinkToReminder(reminder: ReminderEntity, webLink: String) {
        viewModelScope.launch {
            val normalizedUrl = normalizeUrl(webLink)
            val updated = reminder.copy(
                webLink = normalizedUrl,
                updatedAt = java.time.LocalDateTime.now()
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
                updatedAt = java.time.LocalDateTime.now()
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
                updatedAt = java.time.LocalDateTime.now()
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
}
