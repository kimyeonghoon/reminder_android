package com.reminder.viewmodel

import androidx.lifecycle.ViewModel
import com.reminder.analytics.AnalyticsHelper
import com.reminder.data.entity.Priority
import com.reminder.data.entity.ReminderEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime

/**
 * v1.68.3: 검색/필터/정렬 전용 ViewModel
 *
 * ReminderViewModel에서 분리됨 (God Class 리팩토링)
 * 검색, 필터링, 정렬 기능 담당
 */
class ReminderSearchViewModel(
    private val analyticsHelper: AnalyticsHelper
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

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
}
