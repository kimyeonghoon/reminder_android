package com.reminder.goal

import com.reminder.data.entity.GoalType
import com.reminder.data.entity.Priority
import com.reminder.data.entity.ReminderEntity
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * v1.33.0: GoalTracker 테스트
 *
 * TDD Red Phase - 테스트 먼저 작성
 */
class GoalTrackerTest {

    private lateinit var goalTracker: GoalTracker
    private lateinit var testReminders: List<ReminderEntity>

    @Before
    fun setup() {
        goalTracker = GoalTracker()

        // 테스트용 리마인더 데이터 생성
        val today = LocalDate.now()
        val thisWeek = today.minusDays(3)
        // 이번 달 범위 내에서 날짜 설정 (flaky test 방지)
        val thisMonthStart = today.withDayOfMonth(1)
        val thisMonthMid = today.withDayOfMonth(15) // 이번 달 중순

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

    @Test
    fun `일일 목표 진행률 계산 - 목표 달성`() {
        // Given
        val goal = Goal(
            id = 1,
            type = GoalType.DAILY,
            targetCount = 2,
            startDate = LocalDate.now(),
            endDate = LocalDate.now(),
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

    @Test
    fun `일일 목표 진행률 계산 - 목표 미달성`() {
        // Given
        val goal = Goal(
            id = 1,
            type = GoalType.DAILY,
            targetCount = 5,
            startDate = LocalDate.now(),
            endDate = LocalDate.now(),
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

    @Test
    fun `주간 목표 진행률 계산`() {
        // Given
        val startOfWeek = LocalDate.now().minusDays(6)
        val endOfWeek = LocalDate.now()
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

    @Test
    fun `월간 목표 진행률 계산`() {
        // Given
        val startOfMonth = LocalDate.now().withDayOfMonth(1)
        val endOfMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth())
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

    @Test
    fun `카테고리별 목표 진행률 - 업무 카테고리`() {
        // Given
        val goal = Goal(
            id = 4,
            type = GoalType.WEEKLY,
            targetCount = 2,
            category = "업무",
            startDate = LocalDate.now().minusDays(6),
            endDate = LocalDate.now(),
            isActive = true
        )

        // When
        val progress = goalTracker.calculateProgress(goal, testReminders)

        // Then
        assertEquals(2, progress.currentCount) // 업무 카테고리 완료 2개
        assertEquals(2, progress.targetCount)
        assertTrue(progress.isAchieved)
    }

    @Test
    fun `목표 초과 달성 케이스`() {
        // Given
        val goal = Goal(
            id = 5,
            type = GoalType.DAILY,
            targetCount = 1,
            startDate = LocalDate.now(),
            endDate = LocalDate.now(),
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

    @Test
    fun `남은 일수 계산`() {
        // Given
        val goal = Goal(
            id = 6,
            type = GoalType.WEEKLY,
            targetCount = 5,
            startDate = LocalDate.now(),
            endDate = LocalDate.now().plusDays(3),
            isActive = true
        )

        // When
        val progress = goalTracker.calculateProgress(goal, testReminders)

        // Then
        assertEquals(3, progress.remainingDays)
    }

    @Test
    fun `목표 기간 만료 케이스`() {
        // Given
        val goal = Goal(
            id = 7,
            type = GoalType.WEEKLY,
            targetCount = 5,
            startDate = LocalDate.now().minusDays(14),
            endDate = LocalDate.now().minusDays(7), // 1주일 전 만료
            isActive = true
        )

        // When
        val progress = goalTracker.calculateProgress(goal, testReminders)

        // Then
        assertEquals(0, progress.remainingDays)
        assertFalse(progress.isAchieved) // 기간 내 목표 미달성
    }
}
