package com.reminder.notification

import com.reminder.data.entity.RecurrencePattern
import org.junit.Assert.*
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDateTime

/**
 * AlarmScheduler의 calculateNextOccurrence 메서드에 대한 유닛 테스트
 */
class AlarmSchedulerCalculationTest {

    /** NONE 패턴은 null을 반환한다 */
    @Test
    fun nonePatternReturnsNull() {
        // Given
        val currentDateTime = LocalDateTime.of(2025, 10, 8, 10, 0)

        // When
        val result = AlarmScheduler.calculateNextOccurrence(
            currentDateTime = currentDateTime,
            pattern = RecurrencePattern.NONE,
            interval = 1,
            daysOfWeek = null,
            endDate = null
        )

        // Then
        assertNull(result)
    }

    /** DAILY 패턴은 interval 일 후를 반환한다 */
    @Test
    fun dailyPatternReturnsIntervalDaysLater() {
        // Given
        val currentDateTime = LocalDateTime.of(2025, 10, 8, 10, 0)

        // When
        val result = AlarmScheduler.calculateNextOccurrence(
            currentDateTime = currentDateTime,
            pattern = RecurrencePattern.DAILY,
            interval = 1,
            daysOfWeek = null,
            endDate = null
        )

        // Then
        assertNotNull(result)
        assertEquals(LocalDateTime.of(2025, 10, 9, 10, 0), result)
    }

    /** DAILY 패턴 interval 3은 3일 후를 반환한다 */
    @Test
    fun dailyPatternInterval3ReturnsThreeDaysLater() {
        // Given
        val currentDateTime = LocalDateTime.of(2025, 10, 8, 10, 0)

        // When
        val result = AlarmScheduler.calculateNextOccurrence(
            currentDateTime = currentDateTime,
            pattern = RecurrencePattern.DAILY,
            interval = 3,
            daysOfWeek = null,
            endDate = null
        )

        // Then
        assertNotNull(result)
        assertEquals(LocalDateTime.of(2025, 10, 11, 10, 0), result)
    }

    /** WEEKLY 패턴 요일 지정 없이는 interval 주 후를 반환한다 */
    @Test
    fun weeklyPatternWithoutDaysReturnsIntervalWeeksLater() {
        // Given
        val currentDateTime = LocalDateTime.of(2025, 10, 8, 10, 0) // 수요일

        // When
        val result = AlarmScheduler.calculateNextOccurrence(
            currentDateTime = currentDateTime,
            pattern = RecurrencePattern.WEEKLY,
            interval = 1,
            daysOfWeek = null,
            endDate = null
        )

        // Then
        assertNotNull(result)
        assertEquals(LocalDateTime.of(2025, 10, 15, 10, 0), result)
    }

    /** WEEKLY 패턴 요일 지정 시 다음 요일을 반환한다 */
    @Test
    fun weeklyPatternWithDaysReturnsNextSpecifiedDay() {
        // Given
        val currentDateTime = LocalDateTime.of(2025, 10, 8, 10, 0) // 수요일
        val daysOfWeek = "FRIDAY" // 금요일

        // When
        val result = AlarmScheduler.calculateNextOccurrence(
            currentDateTime = currentDateTime,
            pattern = RecurrencePattern.WEEKLY,
            interval = 1,
            daysOfWeek = daysOfWeek,
            endDate = null
        )

        // Then
        assertNotNull(result)
        assertEquals(DayOfWeek.FRIDAY, result!!.dayOfWeek)
        assertEquals(LocalDateTime.of(2025, 10, 10, 10, 0), result)
    }

    /** WEEKLY 패턴 여러 요일 지정 시 가장 가까운 요일을 반환한다 */
    @Test
    fun weeklyPatternWithMultipleDaysReturnsClosestDay() {
        // Given
        val currentDateTime = LocalDateTime.of(2025, 10, 8, 10, 0) // 수요일
        val daysOfWeek = "MONDAY,FRIDAY" // 월요일, 금요일

        // When
        val result = AlarmScheduler.calculateNextOccurrence(
            currentDateTime = currentDateTime,
            pattern = RecurrencePattern.WEEKLY,
            interval = 1,
            daysOfWeek = daysOfWeek,
            endDate = null
        )

        // Then
        assertNotNull(result)
        assertEquals(DayOfWeek.FRIDAY, result!!.dayOfWeek) // 수요일 다음은 금요일
        assertEquals(LocalDateTime.of(2025, 10, 10, 10, 0), result)
    }

    /** WEEKLY 패턴 이번 주에 해당 요일이 없으면 다음 주를 반환한다 */
    @Test
    fun weeklyPatternReturnsNextWeekIfNoDayInCurrentWeek() {
        // Given
        val currentDateTime = LocalDateTime.of(2025, 10, 8, 10, 0) // 수요일
        val daysOfWeek = "MONDAY,TUESDAY" // 월요일, 화요일 (이미 지남)

        // When
        val result = AlarmScheduler.calculateNextOccurrence(
            currentDateTime = currentDateTime,
            pattern = RecurrencePattern.WEEKLY,
            interval = 1,
            daysOfWeek = daysOfWeek,
            endDate = null
        )

        // Then
        assertNotNull(result)
        assertEquals(DayOfWeek.MONDAY, result!!.dayOfWeek) // 다음 주 월요일
        assertTrue(result.isAfter(currentDateTime.plusDays(3)))
    }

    /** MONTHLY 패턴은 interval 개월 후를 반환한다 */
    @Test
    fun monthlyPatternReturnsIntervalMonthsLater() {
        // Given
        val currentDateTime = LocalDateTime.of(2025, 10, 8, 10, 0)

        // When
        val result = AlarmScheduler.calculateNextOccurrence(
            currentDateTime = currentDateTime,
            pattern = RecurrencePattern.MONTHLY,
            interval = 1,
            daysOfWeek = null,
            endDate = null
        )

        // Then
        assertNotNull(result)
        assertEquals(LocalDateTime.of(2025, 11, 8, 10, 0), result)
    }

    /** MONTHLY 패턴 interval 3은 3개월 후를 반환한다 */
    @Test
    fun monthlyPatternInterval3ReturnsThreeMonthsLater() {
        // Given
        val currentDateTime = LocalDateTime.of(2025, 10, 8, 10, 0)

        // When
        val result = AlarmScheduler.calculateNextOccurrence(
            currentDateTime = currentDateTime,
            pattern = RecurrencePattern.MONTHLY,
            interval = 3,
            daysOfWeek = null,
            endDate = null
        )

        // Then
        assertNotNull(result)
        assertEquals(LocalDateTime.of(2026, 1, 8, 10, 0), result)
    }

    /** YEARLY 패턴은 interval 년 후를 반환한다 */
    @Test
    fun yearlyPatternReturnsIntervalYearsLater() {
        // Given
        val currentDateTime = LocalDateTime.of(2025, 10, 8, 10, 0)

        // When
        val result = AlarmScheduler.calculateNextOccurrence(
            currentDateTime = currentDateTime,
            pattern = RecurrencePattern.YEARLY,
            interval = 1,
            daysOfWeek = null,
            endDate = null
        )

        // Then
        assertNotNull(result)
        assertEquals(LocalDateTime.of(2026, 10, 8, 10, 0), result)
    }

    /** 종료 날짜를 초과하면 null을 반환한다 */
    @Test
    fun returnsNullIfExceedsEndDate() {
        // Given
        val currentDateTime = LocalDateTime.of(2025, 10, 8, 10, 0)
        val endDate = LocalDateTime.of(2025, 10, 9, 10, 0)

        // When
        val result = AlarmScheduler.calculateNextOccurrence(
            currentDateTime = currentDateTime,
            pattern = RecurrencePattern.DAILY,
            interval = 2, // 2일 후 = 10/10
            daysOfWeek = null,
            endDate = endDate // 종료일은 10/9
        )

        // Then
        assertNull(result) // 10/10은 종료일(10/9)을 초과하므로 null
    }

    /** 종료 날짜와 같은 날짜는 반환한다 */
    @Test
    fun returnsDateEqualToEndDate() {
        // Given
        val currentDateTime = LocalDateTime.of(2025, 10, 8, 10, 0)
        val endDate = LocalDateTime.of(2025, 10, 9, 10, 0)

        // When
        val result = AlarmScheduler.calculateNextOccurrence(
            currentDateTime = currentDateTime,
            pattern = RecurrencePattern.DAILY,
            interval = 1, // 1일 후 = 10/9
            daysOfWeek = null,
            endDate = endDate // 종료일은 10/9
        )

        // Then
        assertNotNull(result)
        assertEquals(LocalDateTime.of(2025, 10, 9, 10, 0), result)
    }
}
