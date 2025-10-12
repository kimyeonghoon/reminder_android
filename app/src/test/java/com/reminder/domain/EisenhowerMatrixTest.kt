package com.reminder.domain

import com.reminder.data.entity.Priority
import com.reminder.data.entity.ReminderEntity
import com.reminder.data.entity.Urgency
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime

/**
 * v1.47.0: Eisenhower Matrix 계산 로직 테스트 (TDD)
 *
 * Eisenhower Matrix (아이젠하워 매트릭스):
 * - DO_FIRST (Q1): 중요하고 긴급함 - 즉시 처리
 * - SCHEDULE (Q2): 중요하지만 긴급하지 않음 - 계획 수립
 * - DELEGATE (Q3): 긴급하지만 중요하지 않음 - 위임
 * - DELETE (Q4): 중요하지도 긴급하지도 않음 - 제거
 */
class EisenhowerMatrixTest {

    /**
     * Quadrant 1 (DO_FIRST) 테스트
     * - Priority.HIGH + Urgency.HIGH
     * - Priority.HIGH + Urgency.MEDIUM
     * - Priority.MEDIUM + Urgency.HIGH
     */
    /** HIGH 우선순위와 HIGH 긴급도는 DO_FIRST 쿼드런트 */
    @Test
    fun highPriorityAndHighUrgency_shouldBeDoFirstQuadrant() {
        val reminder = createReminder(Priority.HIGH, Urgency.HIGH)
        assertEquals(Quadrant.DO_FIRST, reminder.getQuadrant())
    }

    /** HIGH 우선순위와 MEDIUM 긴급도는 DO_FIRST 쿼드런트 */
    @Test
    fun highPriorityAndMediumUrgency_shouldBeDoFirstQuadrant() {
        val reminder = createReminder(Priority.HIGH, Urgency.MEDIUM)
        assertEquals(Quadrant.DO_FIRST, reminder.getQuadrant())
    }

    /** MEDIUM 우선순위와 HIGH 긴급도는 DO_FIRST 쿼드런트 */
    @Test
    fun mediumPriorityAndHighUrgency_shouldBeDoFirstQuadrant() {
        val reminder = createReminder(Priority.MEDIUM, Urgency.HIGH)
        assertEquals(Quadrant.DO_FIRST, reminder.getQuadrant())
    }

    /**
     * Quadrant 2 (SCHEDULE) 테스트
     * - Priority.HIGH + Urgency.LOW
     * - Priority.MEDIUM + Urgency.MEDIUM
     * - Priority.MEDIUM + Urgency.LOW
     */
    /** HIGH 우선순위와 LOW 긴급도는 SCHEDULE 쿼드런트 */
    @Test
    fun highPriorityAndLowUrgency_shouldBeScheduleQuadrant() {
        val reminder = createReminder(Priority.HIGH, Urgency.LOW)
        assertEquals(Quadrant.SCHEDULE, reminder.getQuadrant())
    }

    /** MEDIUM 우선순위와 MEDIUM 긴급도는 SCHEDULE 쿼드런트 */
    @Test
    fun mediumPriorityAndMediumUrgency_shouldBeScheduleQuadrant() {
        val reminder = createReminder(Priority.MEDIUM, Urgency.MEDIUM)
        assertEquals(Quadrant.SCHEDULE, reminder.getQuadrant())
    }

    /** MEDIUM 우선순위와 LOW 긴급도는 SCHEDULE 쿼드런트 */
    @Test
    fun mediumPriorityAndLowUrgency_shouldBeScheduleQuadrant() {
        val reminder = createReminder(Priority.MEDIUM, Urgency.LOW)
        assertEquals(Quadrant.SCHEDULE, reminder.getQuadrant())
    }

    /**
     * Quadrant 3 (DELEGATE) 테스트
     * - Priority.LOW + Urgency.HIGH
     * - Priority.LOW + Urgency.MEDIUM
     */
    /** LOW 우선순위와 HIGH 긴급도는 DELEGATE 쿼드런트 */
    @Test
    fun lowPriorityAndHighUrgency_shouldBeDelegateQuadrant() {
        val reminder = createReminder(Priority.LOW, Urgency.HIGH)
        assertEquals(Quadrant.DELEGATE, reminder.getQuadrant())
    }

    /** LOW 우선순위와 MEDIUM 긴급도는 DELEGATE 쿼드런트 */
    @Test
    fun lowPriorityAndMediumUrgency_shouldBeDelegateQuadrant() {
        val reminder = createReminder(Priority.LOW, Urgency.MEDIUM)
        assertEquals(Quadrant.DELEGATE, reminder.getQuadrant())
    }

    /**
     * Quadrant 4 (DELETE) 테스트
     * - Priority.LOW + Urgency.LOW
     */
    /** LOW 우선순위와 LOW 긴급도는 DELETE 쿼드런트 */
    @Test
    fun lowPriorityAndLowUrgency_shouldBeDeleteQuadrant() {
        val reminder = createReminder(Priority.LOW, Urgency.LOW)
        assertEquals(Quadrant.DELETE, reminder.getQuadrant())
    }

    /**
     * 쿼드런트별 필터링 테스트
     */
    /** 리마인더 리스트를 쿼드런트별로 필터링할 수 있다 */
    @Test
    fun filterRemindersByQuadrant() {
        val reminders = listOf(
            createReminder(Priority.HIGH, Urgency.HIGH),    // DO_FIRST
            createReminder(Priority.HIGH, Urgency.LOW),     // SCHEDULE
            createReminder(Priority.LOW, Urgency.HIGH),     // DELEGATE
            createReminder(Priority.LOW, Urgency.LOW),      // DELETE
            createReminder(Priority.MEDIUM, Urgency.HIGH),  // DO_FIRST
        )

        val doFirst = reminders.filterByQuadrant(Quadrant.DO_FIRST)
        val schedule = reminders.filterByQuadrant(Quadrant.SCHEDULE)
        val delegate = reminders.filterByQuadrant(Quadrant.DELEGATE)
        val delete = reminders.filterByQuadrant(Quadrant.DELETE)

        assertEquals(2, doFirst.size)
        assertEquals(1, schedule.size)
        assertEquals(1, delegate.size)
        assertEquals(1, delete.size)
    }

    /**
     * 쿼드런트별 카운트 테스트
     */
    /** 리마인더 리스트의 쿼드런트별 개수를 계산할 수 있다 */
    @Test
    fun calculateCountByQuadrant() {
        val reminders = listOf(
            createReminder(Priority.HIGH, Urgency.HIGH),
            createReminder(Priority.HIGH, Urgency.HIGH),
            createReminder(Priority.HIGH, Urgency.LOW),
            createReminder(Priority.LOW, Urgency.LOW)
        )

        val counts = reminders.countByQuadrant()

        assertEquals(2, counts[Quadrant.DO_FIRST])
        assertEquals(1, counts[Quadrant.SCHEDULE])
        assertEquals(0, counts[Quadrant.DELEGATE])
        assertEquals(1, counts[Quadrant.DELETE])
    }

    /**
     * v1.49.0: 쿼드런트 통계 테스트
     */
    /** 쿼드런트 통계를 계산할 수 있다 */
    @Test
    fun calculateQuadrantStats() {
        val now = LocalDateTime.now()
        val reminders = listOf(
            createReminder(Priority.HIGH, Urgency.HIGH, isCompleted = true, createdAt = now.minusHours(2), updatedAt = now),
            createReminder(Priority.HIGH, Urgency.HIGH, isCompleted = false, createdAt = now.minusHours(1)),
            createReminder(Priority.HIGH, Urgency.HIGH, isCompleted = true, createdAt = now.minusHours(4), updatedAt = now)
        )

        val stats = reminders.calculateQuadrantStats(Quadrant.DO_FIRST)

        assertEquals(3, stats.totalCount)
        assertEquals(2, stats.completedCount)
        assertEquals(66, stats.completionRate.toInt()) // 2/3 * 100 = 66%
        // 평균 처리 시간: (2시간 + 4시간) / 2 = 3시간 = 180분
        assertEquals(180, stats.averageCompletionMinutes.toLong())
    }

    /** 완료된 리마인더가 없으면 평균 처리 시간은 0 */
    @Test
    fun averageCompletionTime_withNoCompletedReminders_shouldBeZero() {
        val reminders = listOf(
            createReminder(Priority.HIGH, Urgency.HIGH, isCompleted = false),
            createReminder(Priority.HIGH, Urgency.HIGH, isCompleted = false)
        )

        val stats = reminders.calculateQuadrantStats(Quadrant.DO_FIRST)

        assertEquals(2, stats.totalCount)
        assertEquals(0, stats.completedCount)
        assertEquals(0, stats.completionRate.toInt())
        assertEquals(0, stats.averageCompletionMinutes.toLong())
    }

    /** 쿼드런트에 리마인더가 없으면 모든 통계는 0 */
    @Test
    fun quadrantStats_withNoReminders_shouldAllBeZero() {
        val reminders = listOf(
            createReminder(Priority.LOW, Urgency.LOW) // DELETE 쿼드런트
        )

        val stats = reminders.calculateQuadrantStats(Quadrant.DO_FIRST)

        assertEquals(0, stats.totalCount)
        assertEquals(0, stats.completedCount)
        assertEquals(0, stats.completionRate.toInt())
        assertEquals(0, stats.averageCompletionMinutes.toLong())
    }

    /**
     * v1.49.0: 리마인더를 다른 쿼드런트로 이동 테스트
     */
    /** 리마인더를 DO_FIRST 쿼드런트로 이동할 수 있다 */
    @Test
    fun moveReminderToDoFirstQuadrant() {
        val reminder = createReminder(Priority.LOW, Urgency.LOW)
        assertEquals(Quadrant.DELETE, reminder.getQuadrant())

        val moved = reminder.moveToQuadrant(Quadrant.DO_FIRST)

        assertEquals(Quadrant.DO_FIRST, moved.getQuadrant())
        assertEquals(Priority.HIGH, moved.priority)
        assertEquals(Urgency.HIGH, moved.urgency)
    }

    /** 리마인더를 SCHEDULE 쿼드런트로 이동할 수 있다 */
    @Test
    fun moveReminderToScheduleQuadrant() {
        val reminder = createReminder(Priority.LOW, Urgency.HIGH)
        assertEquals(Quadrant.DELEGATE, reminder.getQuadrant())

        val moved = reminder.moveToQuadrant(Quadrant.SCHEDULE)

        assertEquals(Quadrant.SCHEDULE, moved.getQuadrant())
        assertEquals(Priority.HIGH, moved.priority)
        assertEquals(Urgency.LOW, moved.urgency)
    }

    /** 리마인더를 DELEGATE 쿼드런트로 이동할 수 있다 */
    @Test
    fun moveReminderToDelegateQuadrant() {
        val reminder = createReminder(Priority.HIGH, Urgency.LOW)
        assertEquals(Quadrant.SCHEDULE, reminder.getQuadrant())

        val moved = reminder.moveToQuadrant(Quadrant.DELEGATE)

        assertEquals(Quadrant.DELEGATE, moved.getQuadrant())
        assertEquals(Priority.LOW, moved.priority)
        assertEquals(Urgency.HIGH, moved.urgency)
    }

    /** 리마인더를 DELETE 쿼드런트로 이동할 수 있다 */
    @Test
    fun moveReminderToDeleteQuadrant() {
        val reminder = createReminder(Priority.HIGH, Urgency.HIGH)
        assertEquals(Quadrant.DO_FIRST, reminder.getQuadrant())

        val moved = reminder.moveToQuadrant(Quadrant.DELETE)

        assertEquals(Quadrant.DELETE, moved.getQuadrant())
        assertEquals(Priority.LOW, moved.priority)
        assertEquals(Urgency.LOW, moved.urgency)
    }

    /**
     * v1.50.0: 쿼드런트 트렌드 분석 테스트
     */
    /** 주간 트렌드를 계산할 수 있다 */
    @Test
    fun calculateWeeklyTrend() {
        val now = LocalDateTime.now()
        val reminders = listOf(
            // 오늘 완료 2개
            createReminder(Priority.HIGH, Urgency.HIGH, isCompleted = true, updatedAt = now),
            createReminder(Priority.HIGH, Urgency.HIGH, isCompleted = true, updatedAt = now),
            // 2일 전 완료 1개
            createReminder(Priority.HIGH, Urgency.HIGH, isCompleted = true, updatedAt = now.minusDays(2)),
            // 7일 전 완료 1개
            createReminder(Priority.HIGH, Urgency.HIGH, isCompleted = true, updatedAt = now.minusDays(7)),
            // 8일 전 완료 (범위 밖)
            createReminder(Priority.HIGH, Urgency.HIGH, isCompleted = true, updatedAt = now.minusDays(8)),
            // 미완료 (제외)
            createReminder(Priority.HIGH, Urgency.HIGH, isCompleted = false)
        )

        val trend = reminders.calculateQuadrantTrend(Quadrant.DO_FIRST, TrendPeriod.WEEKLY)

        assertEquals(8, trend.dataPoints.size) // 오늘부터 7일 전까지 = 8개
        assertEquals(2, trend.dataPoints[0].count) // 오늘
        assertEquals(0, trend.dataPoints[1].count) // 어제
        assertEquals(1, trend.dataPoints[2].count) // 2일 전
        assertEquals(1, trend.dataPoints[7].count) // 7일 전
        assertEquals(4, trend.totalCompleted) // 8일 전 제외
    }

    /** 월간 트렌드를 계산할 수 있다 */
    @Test
    fun calculateMonthlyTrend() {
        val now = LocalDateTime.now()
        val reminders = listOf(
            createReminder(Priority.HIGH, Urgency.LOW, isCompleted = true, updatedAt = now),
            createReminder(Priority.HIGH, Urgency.LOW, isCompleted = true, updatedAt = now.minusDays(15)),
            createReminder(Priority.HIGH, Urgency.LOW, isCompleted = true, updatedAt = now.minusDays(29)),
            createReminder(Priority.HIGH, Urgency.LOW, isCompleted = true, updatedAt = now.minusDays(30)),
            createReminder(Priority.HIGH, Urgency.LOW, isCompleted = true, updatedAt = now.minusDays(31)) // 범위 밖
        )

        val trend = reminders.calculateQuadrantTrend(Quadrant.SCHEDULE, TrendPeriod.MONTHLY)

        assertEquals(31, trend.dataPoints.size) // 오늘부터 30일 전까지 = 31개
        assertEquals(1, trend.dataPoints[0].count) // 오늘
        assertEquals(1, trend.dataPoints[15].count) // 15일 전
        assertEquals(1, trend.dataPoints[29].count) // 29일 전
        assertEquals(1, trend.dataPoints[30].count) // 30일 전
        assertEquals(4, trend.totalCompleted) // 31일 전 제외
    }

    /** 완료된 리마인더가 없으면 트렌드는 모두 0 */
    @Test
    fun trend_withNoCompletedReminders_shouldAllBeZero() {
        val reminders = listOf(
            createReminder(Priority.HIGH, Urgency.HIGH, isCompleted = false),
            createReminder(Priority.HIGH, Urgency.HIGH, isCompleted = false)
        )

        val trend = reminders.calculateQuadrantTrend(Quadrant.DO_FIRST, TrendPeriod.WEEKLY)

        assertEquals(8, trend.dataPoints.size) // 오늘부터 7일 전까지 = 8개
        assertEquals(0, trend.totalCompleted)
        trend.dataPoints.forEach { dataPoint ->
            assertEquals(0, dataPoint.count)
        }
    }

    /** 다른 쿼드런트의 리마인더는 트렌드에 포함되지 않는다 */
    @Test
    fun trend_shouldExcludeRemindersFromOtherQuadrants() {
        val now = LocalDateTime.now()
        val reminders = listOf(
            // DO_FIRST 2개
            createReminder(Priority.HIGH, Urgency.HIGH, isCompleted = true, updatedAt = now),
            createReminder(Priority.HIGH, Urgency.HIGH, isCompleted = true, updatedAt = now),
            // SCHEDULE 1개
            createReminder(Priority.HIGH, Urgency.LOW, isCompleted = true, updatedAt = now)
        )

        val doFirstTrend = reminders.calculateQuadrantTrend(Quadrant.DO_FIRST, TrendPeriod.WEEKLY)
        val scheduleTrend = reminders.calculateQuadrantTrend(Quadrant.SCHEDULE, TrendPeriod.WEEKLY)

        assertEquals(2, doFirstTrend.totalCompleted)
        assertEquals(1, scheduleTrend.totalCompleted)
    }

    /** 시간대별 쿼드런트 분포를 계산할 수 있다 */
    @Test
    fun calculateTimeDistributionByQuadrant() {
        val now = LocalDateTime.now()
        val reminders = listOf(
            // 오전 (0-11시): 2개
            createReminder(Priority.HIGH, Urgency.HIGH, isCompleted = true,
                updatedAt = now.withHour(9).withMinute(0)),
            createReminder(Priority.HIGH, Urgency.HIGH, isCompleted = true,
                updatedAt = now.withHour(11).withMinute(30)),
            // 오후 (12-17시): 3개
            createReminder(Priority.HIGH, Urgency.HIGH, isCompleted = true,
                updatedAt = now.withHour(14).withMinute(0)),
            createReminder(Priority.HIGH, Urgency.HIGH, isCompleted = true,
                updatedAt = now.withHour(16).withMinute(30)),
            createReminder(Priority.HIGH, Urgency.HIGH, isCompleted = true,
                updatedAt = now.withHour(17).withMinute(0)),
            // 저녁 (18-23시): 1개
            createReminder(Priority.HIGH, Urgency.HIGH, isCompleted = true,
                updatedAt = now.withHour(20).withMinute(0))
        )

        val distribution = reminders.calculateTimeDistribution(Quadrant.DO_FIRST)

        assertEquals(2, distribution.morning) // 0-11시
        assertEquals(3, distribution.afternoon) // 12-17시
        assertEquals(1, distribution.evening) // 18-23시
        assertEquals(0, distribution.night) // 24-5시
    }

    // Helper 함수
    private fun createReminder(
        priority: Priority,
        urgency: Urgency,
        title: String = "테스트 리마인더",
        isCompleted: Boolean = false,
        createdAt: LocalDateTime = LocalDateTime.now(),
        updatedAt: LocalDateTime = LocalDateTime.now()
    ): ReminderEntity {
        return ReminderEntity(
            id = 0,
            title = title,
            priority = priority,
            urgency = urgency,
            isCompleted = isCompleted,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}
