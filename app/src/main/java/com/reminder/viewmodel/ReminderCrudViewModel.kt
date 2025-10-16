package com.reminder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reminder.analytics.AnalyticsHelper
import com.reminder.data.entity.Priority
import com.reminder.data.entity.ReminderEntity
import com.reminder.data.repository.ReminderRepository
import com.reminder.domain.moveToQuadrant
import com.reminder.notification.AlarmScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime

/**
 * v1.68.3: CRUD 전용 ViewModel
 *
 * ReminderViewModel에서 분리됨 (God Class 리팩토링)
 * CRUD 핵심 기능, 배치 작업, 복제, 템플릿, Eisenhower Matrix 이동 담당
 */
open class ReminderCrudViewModel(
    private val repository: ReminderRepository,
    private val alarmScheduler: AlarmScheduler,
    private val locationManager: com.reminder.location.LocationManager,
    private val analyticsHelper: AnalyticsHelper
) : ViewModel() {

    val allReminders: StateFlow<List<ReminderEntity>> = repository.allReminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeReminders: StateFlow<List<ReminderEntity>> = repository.activeReminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completedReminders: StateFlow<List<ReminderEntity>> = repository.completedReminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedReminder = MutableStateFlow<ReminderEntity?>(null)
    val selectedReminder: StateFlow<ReminderEntity?> = _selectedReminder.asStateFlow()

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
                    java.time.temporal.ChronoUnit.DAYS.between(LocalDateTime.now(), it).toInt()
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

    // ==================== 템플릿 관련 함수 ====================

    /**
     * 템플릿에서 리마인더 생성 (핵심 CRUD이므로 유지)
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
