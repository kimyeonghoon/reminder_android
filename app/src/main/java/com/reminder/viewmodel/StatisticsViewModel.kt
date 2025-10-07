package com.reminder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reminder.data.entity.Priority
import com.reminder.data.entity.Statistics
import com.reminder.data.repository.ReminderRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class StatisticsViewModel(
    private val repository: ReminderRepository
) : ViewModel() {

    /**
     * 통계 데이터 StateFlow
     */
    val statistics: StateFlow<Statistics> = repository.allReminders
        .map { reminders ->
            val total = reminders.size
            val completed = reminders.count { it.isCompleted }
            val pending = total - completed
            val completionRate = if (total > 0) completed.toFloat() / total else 0f

            // 우선순위별 개수
            val highPriority = reminders.count { it.priority == Priority.HIGH }
            val mediumPriority = reminders.count { it.priority == Priority.MEDIUM }
            val lowPriority = reminders.count { it.priority == Priority.LOW }

            // 카테고리별 분포 (빈 카테고리 제외)
            val categoryDistribution = reminders
                .filter { it.category.isNotBlank() }
                .groupBy { it.category }
                .mapValues { it.value.size }

            Statistics(
                totalReminders = total,
                completedReminders = completed,
                pendingReminders = pending,
                completionRate = completionRate,
                highPriorityCount = highPriority,
                mediumPriorityCount = mediumPriority,
                lowPriorityCount = lowPriority,
                categoryDistribution = categoryDistribution
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = Statistics()
        )
}
