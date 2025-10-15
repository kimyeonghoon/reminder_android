package com.reminder.goal

import com.reminder.data.entity.GoalType
import com.reminder.data.entity.Priority
import com.reminder.data.entity.ReminderEntity
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * v1.33.0: GoalTracker 테스트
 *
 * TDD Red Phase - 테스트 먼저 작성
 *
 * v1.68.1: Clock 주입으로 flaky test 해결
 */
class GoalTrackerTest {

    private lateinit var goalTracker: GoalTracker
    private lateinit var testReminders: List<ReminderEntity>
    private lateinit var fixedClock: Clock
    private lateinit var today: LocalDate

    @Before
    fun setup() {
        // 고정된 시간 사용 (2025-10-15 12:00:00 UTC)
        fixedClock = Clock.fixed(
            Instant.parse("2025-10-15T12:00:00Z"),
            ZoneId.of("UTC")
        )
        goalTracker = GoalTracker(fixedClock)

        // 고정된 today 값 사용
        today = LocalDate.now(fixedClock)  // 2025-10-15
        val thisWeek = today.minusDays(3)  // 2025-10-12
        val thisMonthMid = today.withDayOfMonth(5)  // 2025-10-05 (주간 범위 밖)

        testReminders = listOf(
            // 이번 주 완료 (3개)
            ReminderEntity(
                id = 1,
                title = "오늘 완료 1",
                isCompleted = true,
                completedAt = today.atTime(10, 0),
                category = "업무",
                priority = Priority.HIGH
            ),
            ReminderEntity(
                id = 2,
                title = "오늘 완료 2",
                isCompleted = true,
                completedAt = today.atTime(14, 0),
                category = "개인",
                priority = Priority.MEDIUM
            ),
            ReminderEntity(
                id = 3,
                title = "이번 주 완료",
                isCompleted = true,
                completedAt = thisWeek.atTime(15, 0),
                category = "업무",
                priority = Priority.LOW
            ),
            // 이번 달 초 완료 (1개)
            ReminderEntity(
                id = 4,
                title = "이번 달 초 완료",
                isCompleted = true,
                completedAt = thisMonthMid.atTime(9, 0),
                category = "개인",
                priority = Priority.HIGH
            ),
            // 미완료 (2개)
            ReminderEntity(
                id = 5,
                title = "미완료 1",
                isCompleted = false,
                category = "업무",
                priority = Priority.HIGH
            ),
            ReminderEntity(
                id = 6,
                title = "미완료 2",
                isCompleted = false,
                category = "개인",
                priority = Priority.LOW
            )
        )
    }

    /** 일일 목표 진행률 계산 - 목표 달성 */
    @Test
    fun calculateDailyGoalProgress_Achieved() {
        // Given
        val goal = Goal(
            id = 1,
            type = GoalType.DAILY,
            targetCount = 2,
            startDate = today,
            endDate = today,
            isActive = true
        )

        // When
        val progress = goalTracker.calculateProgress(goal, testReminders)

        // Then
        assertEquals(2, progress.currentCount)
        assertEquals(2, progress.targetCount)
        assertEquals(100.0, progress.percentage, 0.01)
        assertTrue(progress.isAchieved)
        assertEquals(0, progress.remainingDays)
    }

    /** 일일 목표 진행률 계산 - 목표 미달성 */
    @Test
    fun calculateDailyGoalProgress_NotAchieved() {
        // Given
        val goal = Goal(
            id = 1,
            type = GoalType.DAILY,
            targetCount = 5,
            startDate = today,
            endDate = today,
            isActive = true
        )

        // When
        val progress = goalTracker.calculateProgress(goal, testReminders)

        // Then
        assertEquals(2, progress.currentCount)
        assertEquals(5, progress.targetCount)
        assertEquals(40.0, progress.percentage, 0.01)
        assertFalse(progress.isAchieved)
    }

    /** 주간 목표 진행률 계산 */
    @Test
    fun calculateWeeklyGoalProgress() {
        // Given
        val startOfWeek = today.minusDays(6)
        val endOfWeek = today
        val goal = Goal(
            id = 2,
            type = GoalType.WEEKLY,
            targetCount = 3,
            startDate = startOfWeek,
            endDate = endOfWeek,
            isActive = true
        )

        // When
        val progress = goalTracker.calculateProgress(goal, testReminders)

        // Then
        assertEquals(3, progress.currentCount) // 이번 주 완료 3개
        assertEquals(3, progress.targetCount)
        assertEquals(100.0, progress.percentage, 0.01)
        assertTrue(progress.isAchieved)
    }

    /** 월간 목표 진행률 계산 */
    @Test
    fun calculateMonthlyGoalProgress() {
        // Given
        val startOfMonth = today.withDayOfMonth(1)
        val endOfMonth = today.withDayOfMonth(today.lengthOfMonth())
        val goal = Goal(
            id = 3,
            type = GoalType.MONTHLY,
            targetCount = 10,
            startDate = startOfMonth,
            endDate = endOfMonth,
            isActive = true
        )

        // When
        val progress = goalTracker.calculateProgress(goal, testReminders)

        // Then
        assertEquals(4, progress.currentCount) // 이번 달 완료 4개
        assertEquals(10, progress.targetCount)
        assertEquals(40.0, progress.percentage, 0.01)
        assertFalse(progress.isAchieved)
    }

    /** 카테고리별 목표 진행률 - 업무 카테고리 */
    @Test
    fun calculateGoalProgressByCategory_Work() {
        // Given
        val goal = Goal(
            id = 4,
            type = GoalType.WEEKLY,
            targetCount = 2,
            category = "업무",
            startDate = today.minusDays(6),
            endDate = today,
            isActive = true
        )

        // When
        val progress = goalTracker.calculateProgress(goal, testReminders)

        // Then
        assertEquals(2, progress.currentCount) // 업무 카테고리 완료 2개
        assertEquals(2, progress.targetCount)
        assertTrue(progress.isAchieved)
    }

    /** 목표 초과 달성 케이스 */
    @Test
    fun goalExceeded() {
        // Given
        val goal = Goal(
            id = 5,
            type = GoalType.DAILY,
            targetCount = 1,
            startDate = today,
            endDate = today,
            isActive = true
        )

        // When
        val progress = goalTracker.calculateProgress(goal, testReminders)

        // Then
        assertEquals(2, progress.currentCount)
        assertEquals(1, progress.targetCount)
        assertEquals(200.0, progress.percentage, 0.01) // 200% 달성
        assertTrue(progress.isAchieved)
    }

    /** 남은 일수 계산 */
    @Test
    fun calculateRemainingDays() {
        // Given
        val goal = Goal(
            id = 6,
            type = GoalType.WEEKLY,
            targetCount = 5,
            startDate = today,
            endDate = today.plusDays(3),
            isActive = true
        )

        // When
        val progress = goalTracker.calculateProgress(goal, testReminders)

        // Then
        assertEquals(3, progress.remainingDays)
    }

    /** 목표 기간 만료 케이스 */
    @Test
    fun goalPeriodExpired() {
        // Given
        val goal = Goal(
            id = 7,
            type = GoalType.WEEKLY,
            targetCount = 5,
            startDate = today.minusDays(14),
            endDate = today.minusDays(7), // 1주일 전 만료
            isActive = true
        )

        // When
        val progress = goalTracker.calculateProgress(goal, testReminders)

        // Then
        assertEquals(0, progress.remainingDays)
        assertFalse(progress.isAchieved) // 기간 내 목표 미달성
    }
}
