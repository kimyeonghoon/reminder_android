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
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

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

            // 주간/월간 트렌드 계산
            val weeklyCompleted = calculateDailyCompletions(reminders, 7)
            val monthlyCompleted = calculateDailyCompletions(reminders, 30)

            Statistics(
                totalReminders = total,
                completedReminders = completed,
                pendingReminders = pending,
                completionRate = completionRate,
                highPriorityCount = highPriority,
                mediumPriorityCount = mediumPriority,
                lowPriorityCount = lowPriority,
                categoryDistribution = categoryDistribution,
                weeklyCompleted = weeklyCompleted,
                monthlyCompleted = monthlyCompleted
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = Statistics()
        )

    /**
     * 최근 N일간 일별 완료된 리마인더 개수를 계산
     * @param reminders 전체 리마인더 목록
     * @param days 계산할 일수 (7 또는 30)
     * @return 일별 완료 개수 리스트 (0일 전 = 오늘, 1일 전 = 어제, ...)
     */
    private fun calculateDailyCompletions(
        reminders: List<com.reminder.data.entity.ReminderEntity>,
        days: Int
    ): List<Int> {
        val now = LocalDateTime.now()
        val today = now.toLocalDate()

        // 완료된 리마인더만 필터링
        val completedReminders = reminders.filter { it.isCompleted }

        // 각 날짜별 완료 개수를 저장할 배열 (0일 전 ~ (days-1)일 전)
        val completionCounts = MutableList(days) { 0 }

        // 각 완료된 리마인더의 updatedAt와 오늘 날짜의 차이 계산
        completedReminders.forEach { reminder ->
            val completedDate = reminder.updatedAt.toLocalDate()
            val daysDiff = ChronoUnit.DAYS.between(completedDate, today).toInt()

            // 범위 내(0 ~ days-1일 전)에 있는 경우에만 카운트
            if (daysDiff in 0 until days) {
                completionCounts[daysDiff]++
            }
        }

        return completionCounts
    }
}
