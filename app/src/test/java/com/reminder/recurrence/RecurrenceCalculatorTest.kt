package com.reminder.recurrence

import org.junit.Assert.*
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * RecurrenceCalculator 테스트
 *
 * v1.65.0: 다음 발생 시간 계산 로직 검증
 */
class RecurrenceCalculatorTest {

    /** 매일 반복 시 다음 날로 계산된다 */
    @Test
    fun calculatesNextOccurrenceForDailyRecurrence() {
        // Given
        val startTime = LocalDateTime.of(2025, 10, 13, 10, 0)
        val rule = RecurrenceRule(type = RecurrenceType.DAILY, interval = 1)

        // When
        val next = RecurrenceCalculator.calculateNextOccurrence(
            currentTime = startTime,
            recurrenceRule = rule,
            recurrenceEnd = RecurrenceEnd.Never
        )

        // Then
        assertNotNull(next)
        assertEquals(LocalDateTime.of(2025, 10, 14, 10, 0), next)
    }

    /** 2일마다 반복 시 2일 후로 계산된다 */
    @Test
    fun calculatesNextOccurrenceForEveryTwoDays() {
        // Given
        val startTime = LocalDateTime.of(2025, 10, 13, 10, 0)
        val rule = RecurrenceRule(type = RecurrenceType.DAILY, interval = 2)

        // When
        val next = RecurrenceCalculator.calculateNextOccurrence(
            currentTime = startTime,
            recurrenceRule = rule,
            recurrenceEnd = RecurrenceEnd.Never
        )

        // Then
        assertNotNull(next)
        assertEquals(LocalDateTime.of(2025, 10, 15, 10, 0), next)
    }

    /** 주간 반복 시 지정된 요일로 계산된다 */
    @Test
    fun calculatesNextOccurrenceForWeeklyRecurrence() {
        // Given: 월요일에 시작, 매주 월/수/금
        val startTime = LocalDateTime.of(2025, 10, 13, 10, 0) // Monday
        val rule = RecurrenceRule(
            type = RecurrenceType.WEEKLY,
            interval = 1,
            daysOfWeek = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
        )

        // When: 다음 발생 시간은 수요일
        val next = RecurrenceCalculator.calculateNextOccurrence(
            currentTime = startTime,
            recurrenceRule = rule,
            recurrenceEnd = RecurrenceEnd.Never
        )

        // Then
        assertNotNull(next)
        assertEquals(LocalDateTime.of(2025, 10, 15, 10, 0), next) // Wednesday
    }

    /** 주간 반복에서 마지막 요일 이후 다음 주로 넘어간다 */
    @Test
    fun calculatesNextOccurrenceForWeeklyRecurrenceRollsOverToNextWeek() {
        // Given: 금요일에 시작, 매주 월/수/금
        val startTime = LocalDateTime.of(2025, 10, 17, 10, 0) // Friday
        val rule = RecurrenceRule(
            type = RecurrenceType.WEEKLY,
            interval = 1,
            daysOfWeek = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
        )

        // When: 다음 발생 시간은 다음 주 월요일
        val next = RecurrenceCalculator.calculateNextOccurrence(
            currentTime = startTime,
            recurrenceRule = rule,
            recurrenceEnd = RecurrenceEnd.Never
        )

        // Then
        assertNotNull(next)
        assertEquals(LocalDateTime.of(2025, 10, 20, 10, 0), next) // Next Monday
    }

    /** 2주마다 반복 시 2주 후로 계산된다 */
    @Test
    fun calculatesNextOccurrenceForEveryTwoWeeks() {
        // Given
        val startTime = LocalDateTime.of(2025, 10, 13, 10, 0) // Monday
        val rule = RecurrenceRule(
            type = RecurrenceType.WEEKLY,
            interval = 2,
            daysOfWeek = setOf(DayOfWeek.MONDAY)
        )

        // When
        val next = RecurrenceCalculator.calculateNextOccurrence(
            currentTime = startTime,
            recurrenceRule = rule,
            recurrenceEnd = RecurrenceEnd.Never
        )

        // Then
        assertNotNull(next)
        assertEquals(LocalDateTime.of(2025, 10, 27, 10, 0), next) // 2 weeks later
    }

    /** 월간 반복 시 지정된 날짜로 계산된다 */
    @Test
    fun calculatesNextOccurrenceForMonthlyRecurrenceByDay() {
        // Given: 매월 15일
        val startTime = LocalDateTime.of(2025, 10, 15, 10, 0)
        val rule = RecurrenceRule(
            type = RecurrenceType.MONTHLY,
            interval = 1,
            dayOfMonth = 15
        )

        // When
        val next = RecurrenceCalculator.calculateNextOccurrence(
            currentTime = startTime,
            recurrenceRule = rule,
            recurrenceEnd = RecurrenceEnd.Never
        )

        // Then
        assertNotNull(next)
        assertEquals(LocalDateTime.of(2025, 11, 15, 10, 0), next)
    }

    /** 월간 반복에서 날짜가 없는 달은 건너뛴다 */
    @Test
    fun calculatesNextOccurrenceForMonthlyRecurrenceSkipsInvalidDay() {
        // Given: 매월 31일
        val startTime = LocalDateTime.of(2025, 10, 31, 10, 0)
        val rule = RecurrenceRule(
            type = RecurrenceType.MONTHLY,
            interval = 1,
            dayOfMonth = 31
        )

        // When: 11월은 30일까지이므로 12월 31일로 계산
        val next = RecurrenceCalculator.calculateNextOccurrence(
            currentTime = startTime,
            recurrenceRule = rule,
            recurrenceEnd = RecurrenceEnd.Never
        )

        // Then
        assertNotNull(next)
        assertEquals(LocalDateTime.of(2025, 12, 31, 10, 0), next)
    }

    /** 연간 반복 시 다음 년도로 계산된다 */
    @Test
    fun calculatesNextOccurrenceForYearlyRecurrence() {
        // Given
        val startTime = LocalDateTime.of(2025, 10, 13, 10, 0)
        val rule = RecurrenceRule(type = RecurrenceType.YEARLY, interval = 1)

        // When
        val next = RecurrenceCalculator.calculateNextOccurrence(
            currentTime = startTime,
            recurrenceRule = rule,
            recurrenceEnd = RecurrenceEnd.Never
        )

        // Then
        assertNotNull(next)
        assertEquals(LocalDateTime.of(2026, 10, 13, 10, 0), next)
    }

    /** 종료 조건이 Never면 항상 다음 시간이 계산된다 */
    @Test
    fun calculatesNextOccurrenceForNeverEndingRecurrence() {
        // Given
        val startTime = LocalDateTime.of(2025, 10, 13, 10, 0)
        val rule = RecurrenceRule(type = RecurrenceType.DAILY, interval = 1)

        // When
        val next = RecurrenceCalculator.calculateNextOccurrence(
            currentTime = startTime,
            recurrenceRule = rule,
            recurrenceEnd = RecurrenceEnd.Never
        )

        // Then
        assertNotNull(next)
    }

    /** 종료 조건이 AfterOccurrences면 횟수 초과 시 null을 반환한다 */
    @Test
    fun returnsNullWhenAfterOccurrencesLimitReached() {
        // Given
        val startTime = LocalDateTime.of(2025, 10, 13, 10, 0)
        val rule = RecurrenceRule(type = RecurrenceType.DAILY, interval = 1)
        val recurrenceEnd = RecurrenceEnd.AfterOccurrences(count = 3)

        // When: 3번 발생 후
        val next = RecurrenceCalculator.calculateNextOccurrence(
            currentTime = startTime,
            recurrenceRule = rule,
            recurrenceEnd = recurrenceEnd,
            occurrenceCount = 3
        )

        // Then
        assertNull(next)
    }

    /** 종료 조건이 AfterOccurrences면 횟수 이내일 때 다음 시간이 계산된다 */
    @Test
    fun calculatesNextOccurrenceWhenAfterOccurrencesNotReached() {
        // Given
        val startTime = LocalDateTime.of(2025, 10, 13, 10, 0)
        val rule = RecurrenceRule(type = RecurrenceType.DAILY, interval = 1)
        val recurrenceEnd = RecurrenceEnd.AfterOccurrences(count = 3)

        // When: 2번 발생 후
        val next = RecurrenceCalculator.calculateNextOccurrence(
            currentTime = startTime,
            recurrenceRule = rule,
            recurrenceEnd = recurrenceEnd,
            occurrenceCount = 2
        )

        // Then
        assertNotNull(next)
        assertEquals(LocalDateTime.of(2025, 10, 14, 10, 0), next)
    }

    /** 종료 조건이 OnDate면 날짜 초과 시 null을 반환한다 */
    @Test
    fun returnsNullWhenOnDateLimitReached() {
        // Given
        val rule = RecurrenceRule(type = RecurrenceType.DAILY, interval = 1)
        val recurrenceEnd = RecurrenceEnd.OnDate(date = LocalDate.of(2025, 10, 15))

        // When: 10월 16일 계산 시 종료 날짜 초과
        val next = RecurrenceCalculator.calculateNextOccurrence(
            currentTime = LocalDateTime.of(2025, 10, 15, 10, 0),
            recurrenceRule = rule,
            recurrenceEnd = recurrenceEnd
        )

        // Then
        assertNull(next)
    }

    /** 종료 조건이 OnDate면 날짜 이내일 때 다음 시간이 계산된다 */
    @Test
    fun calculatesNextOccurrenceWhenOnDateNotReached() {
        // Given
        val startTime = LocalDateTime.of(2025, 10, 13, 10, 0)
        val rule = RecurrenceRule(type = RecurrenceType.DAILY, interval = 1)
        val recurrenceEnd = RecurrenceEnd.OnDate(date = LocalDate.of(2025, 10, 20))

        // When
        val next = RecurrenceCalculator.calculateNextOccurrence(
            currentTime = startTime,
            recurrenceRule = rule,
            recurrenceEnd = recurrenceEnd
        )

        // Then
        assertNotNull(next)
        assertEquals(LocalDateTime.of(2025, 10, 14, 10, 0), next)
    }

    /** CUSTOM 타입은 현재 미지원이므로 null을 반환한다 */
    @Test
    fun returnsNullForCustomRecurrenceType() {
        // Given
        val startTime = LocalDateTime.of(2025, 10, 13, 10, 0)
        val rule = RecurrenceRule(type = RecurrenceType.CUSTOM, interval = 1)

        // When
        val next = RecurrenceCalculator.calculateNextOccurrence(
            currentTime = startTime,
            recurrenceRule = rule,
            recurrenceEnd = RecurrenceEnd.Never
        )

        // Then
        assertNull(next)
    }
}
