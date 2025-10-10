package com.reminder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reminder.analytics.ProductivityInsights
import com.reminder.data.dao.GoalDao
import com.reminder.data.entity.GoalEntity
import com.reminder.data.entity.Priority
import com.reminder.data.entity.Statistics
import com.reminder.data.repository.ReminderRepository
import com.reminder.goal.GoalProgress
import com.reminder.goal.GoalTracker
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class StatisticsViewModel(
    private val repository: ReminderRepository,
    private val goalDao: GoalDao
) : ViewModel() {

    // v1.33.0: 목표 추적 및 인사이트
    private val goalTracker = GoalTracker()
    private val productivityInsights = ProductivityInsights()

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

    // ========== v1.33.0: 목표 설정 및 추적 기능 ==========

    /**
     * 모든 활성 목표
     */
    val activeGoals: StateFlow<List<GoalEntity>> = goalDao.getAllActiveGoals()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * 목표 진행률 계산
     */
    fun getGoalProgress(goal: GoalEntity): StateFlow<GoalProgress> {
        return repository.allReminders.map { reminders ->
            goalTracker.calculateProgress(goal, reminders)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = GoalProgress(0, goal.targetCount, 0.0, false, 0)
        )
    }

    /**
     * 목표 추가
     */
    fun addGoal(goal: GoalEntity) {
        viewModelScope.launch {
            goalDao.insertGoal(goal)
        }
    }

    /**
     * 목표 수정
     */
    fun updateGoal(goal: GoalEntity) {
        viewModelScope.launch {
            goalDao.updateGoal(goal)
        }
    }

    /**
     * 목표 삭제
     */
    fun deleteGoal(goal: GoalEntity) {
        viewModelScope.launch {
            goalDao.deleteGoal(goal)
        }
    }

    /**
     * 목표 비활성화
     */
    fun deactivateGoal(goalId: Long) {
        viewModelScope.launch {
            goalDao.deactivateGoal(goalId)
        }
    }

    /**
     * 만료된 목표 자동 비활성화
     */
    fun deactivateExpiredGoals() {
        viewModelScope.launch {
            goalDao.deactivateExpiredGoals()
        }
    }

    // ========== v1.33.0: 생산성 인사이트 ==========

    /**
     * 생산성 인사이트 (실시간 생성)
     */
    val insights = combine(
        statistics,
        repository.allReminders
    ) { stats, reminders ->
        // Statistics 객체를 ProductivityInsights용 Statistics로 변환
        val completedThisWeek = reminders.count {
            it.isCompleted && it.completedAt != null &&
                    ChronoUnit.DAYS.between(it.completedAt?.toLocalDate(), LocalDate.now()) < 7
        }
        val totalThisWeek = reminders.count {
            ChronoUnit.DAYS.between(it.createdAt.toLocalDate(), LocalDate.now()) < 7
        }
        val thisWeekRate = if (totalThisWeek > 0) completedThisWeek.toDouble() / totalThisWeek else 0.0

        val completedLastWeek = reminders.count {
            it.isCompleted && it.completedAt != null &&
                    ChronoUnit.DAYS.between(it.completedAt?.toLocalDate(), LocalDate.now()) in 7..13
        }
        val totalLastWeek = reminders.count {
            val daysSince = ChronoUnit.DAYS.between(it.createdAt.toLocalDate(), LocalDate.now())
            daysSince in 7..13
        }
        val lastWeekRate = if (totalLastWeek > 0) completedLastWeek.toDouble() / totalLastWeek else 0.0

        // 가장 생산적인 시간대 계산
        val productiveHour = reminders
            .filter { it.isCompleted && it.completedAt != null }
            .groupBy { it.completedAt!!.hour }
            .maxByOrNull { it.value.size }
            ?.key

        // 카테고리별 통계
        val categoryStats = reminders
            .filter { it.category.isNotBlank() }
            .groupBy { it.category }
            .mapValues { (_, categoryReminders) ->
                val completed = categoryReminders.count { it.isCompleted }
                val total = categoryReminders.size
                com.reminder.analytics.CategoryStats(
                    completed = completed,
                    total = total,
                    completionRate = if (total > 0) completed.toDouble() / total else 0.0
                )
            }

        val insightStats = com.reminder.analytics.Statistics(
            thisWeekCompletionRate = thisWeekRate,
            lastWeekCompletionRate = lastWeekRate,
            mostProductiveHour = productiveHour,
            categoryStats = categoryStats,
            consecutiveGoalAchievementDays = 0 // TODO: 연속 달성 일수 계산
        )

        productivityInsights.generateInsights(insightStats)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
}
