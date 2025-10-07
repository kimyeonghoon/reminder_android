package com.reminder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reminder.data.entity.Priority
import com.reminder.data.entity.ReminderEntity
import com.reminder.data.repository.ReminderRepository
import com.reminder.notification.AlarmScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class ReminderViewModel(
    private val repository: ReminderRepository,
    private val alarmScheduler: AlarmScheduler
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
        category: String = ""
    ) {
        viewModelScope.launch {
            val reminder = ReminderEntity(
                title = title,
                description = description,
                dueDateTime = dueDateTime,
                priority = priority,
                category = category
            )
            repository.insertReminder(reminder)

            // 알람 스케줄링
            if (dueDateTime != null) {
                alarmScheduler.schedule(reminder)
            }
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
        }
    }

    fun deleteReminder(reminder: ReminderEntity) {
        viewModelScope.launch {
            repository.deleteReminder(reminder)
            // 알람 취소
            alarmScheduler.cancel(reminder.id)
        }
    }

    fun toggleReminderCompletion(reminder: ReminderEntity) {
        viewModelScope.launch {
            repository.toggleReminderCompletion(reminder)

            // 완료되면 알람 취소, 완료 취소되면 알람 재스케줄링
            if (!reminder.isCompleted) {
                // 완료로 변경되면 알람 취소
                alarmScheduler.cancel(reminder.id)
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
    }

    fun getFilteredReminders(reminders: List<ReminderEntity>, query: String): List<ReminderEntity> {
        if (query.isBlank()) return reminders
        return reminders.filter {
            it.title.contains(query, ignoreCase = true) ||
                    it.description.contains(query, ignoreCase = true) ||
                    it.category.contains(query, ignoreCase = true)
        }
    }
}
