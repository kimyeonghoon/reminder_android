package com.reminder.goal

import com.reminder.data.entity.GoalEntity
import com.reminder.data.entity.GoalType
import com.reminder.data.entity.ReminderEntity
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * v1.33.0: 목표 추적기
 *
 * 목표 진행률 계산 및 달성 여부 확인
 */

// Goal typealias (테스트에서 사용)
typealias Goal = GoalEntity

/**
 * 목표 진행 상황
 */
data class GoalProgress(
    /**
     * 현재 완료 개수
     */
    val currentCount: Int,

    /**
     * 목표 개수
     */
    val targetCount: Int,

    /**
     * 달성률 (0.0 ~ 100.0+)
     */
    val percentage: Double,

    /**
     * 목표 달성 여부
     */
    val isAchieved: Boolean,

    /**
     * 남은 일수 (음수면 기간 만료)
     */
    val remainingDays: Int
)

/**
 * 목표 추적기 클래스
 */
class GoalTracker {

    /**
     * 목표 진행률 계산
     *
     * @param goal 목표 정보
     * @param reminders 모든 리마인더 목록
     * @return 목표 진행 상황
     */
    fun calculateProgress(goal: Goal, reminders: List<ReminderEntity>): GoalProgress {
        // 목표 기간 내에 완료된 리마인더 필터링
        val completedInPeriod = reminders.filter { reminder ->
            reminder.isCompleted &&
                    reminder.completedAt != null &&
                    isWithinPeriod(reminder.completedAt!!, goal.startDate, goal.endDate) &&
                    matchesCategory(reminder, goal.category)
        }

        val currentCount = completedInPeriod.size
        val targetCount = goal.targetCount

        val percentage = if (targetCount > 0) {
            (currentCount.toDouble() / targetCount) * 100.0
        } else {
            0.0
        }

        val isAchieved = currentCount >= targetCount

        val remainingDays = calculateRemainingDays(goal.endDate)

        return GoalProgress(
            currentCount = currentCount,
            targetCount = targetCount,
            percentage = percentage,
            isAchieved = isAchieved,
            remainingDays = remainingDays
        )
    }

    /**
     * 날짜가 목표 기간 내에 있는지 확인
     */
    private fun isWithinPeriod(
        dateTime: java.time.LocalDateTime,
        startDate: LocalDate,
        endDate: LocalDate
    ): Boolean {
        val date = dateTime.toLocalDate()
        return !date.isBefore(startDate) && !date.isAfter(endDate)
    }

    /**
     * 리마인더가 목표 카테고리와 일치하는지 확인
     */
    private fun matchesCategory(reminder: ReminderEntity, goalCategory: String?): Boolean {
        // goalCategory가 null이면 모든 카테고리 허용
        return goalCategory == null || reminder.category == goalCategory
    }

    /**
     * 남은 일수 계산 (오늘부터 종료일까지)
     */
    private fun calculateRemainingDays(endDate: LocalDate): Int {
        val today = LocalDate.now()
        val days = ChronoUnit.DAYS.between(today, endDate).toInt()
        return if (days < 0) 0 else days
    }
}
