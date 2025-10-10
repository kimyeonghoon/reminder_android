package com.reminder.recurrence

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * v1.35.0: RecurrenceScheduler 테스트
 *
 * TDD Red Phase - 테스트 먼저 작성
 */
class RecurrenceSchedulerTest {

    private lateinit var scheduler: RecurrenceScheduler

    @Before
    fun setup() {
        scheduler = RecurrenceScheduler()
    }

    @Test
    fun `매일 반복 - 다음 5회 계산`() {
        // Given
        val rule = RecurrenceRule(type = RecurrenceType.DAILY, interval = 1)
        val start = LocalDate.of(2025, 1, 1)

        // When
        val occurrences = scheduler.calculateNextOccurrences(rule, start, RecurrenceEnd.Never, limit = 5)

        // Then
        assertEquals(5, occurrences.size)
        assertEquals(LocalDate.of(2025, 1, 1), occurrences[0])
        assertEquals(LocalDate.of(2025, 1, 2), occurrences[1])
        assertEquals(LocalDate.of(2025, 1, 3), occurrences[2])
        assertEquals(LocalDate.of(2025, 1, 4), occurrences[3])
        assertEquals(LocalDate.of(2025, 1, 5), occurrences[4])
    }

    @Test
    fun `2일마다 반복`() {
        // Given
        val rule = RecurrenceRule(type = RecurrenceType.DAILY, interval = 2)
        val start = LocalDate.of(2025, 1, 1)

        // When
        val occurrences = scheduler.calculateNextOccurrences(rule, start, RecurrenceEnd.Never, limit = 5)

        // Then
        assertEquals(5, occurrences.size)
        assertEquals(LocalDate.of(2025, 1, 1), occurrences[0])
        assertEquals(LocalDate.of(2025, 1, 3), occurrences[1])
        assertEquals(LocalDate.of(2025, 1, 5), occurrences[2])
        assertEquals(LocalDate.of(2025, 1, 7), occurrences[3])
        assertEquals(LocalDate.of(2025, 1, 9), occurrences[4])
    }

    @Test
    fun `매주 월, 수, 금 반복`() {
        // Given
        val rule = RecurrenceRule(
            type = RecurrenceType.WEEKLY,
            interval = 1,
            daysOfWeek = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
        )
        val start = LocalDate.of(2025, 1, 6) // 월요일

        // When
        val occurrences = scheduler.calculateNextOccurrences(rule, start, RecurrenceEnd.Never, limit = 6)

        // Then
        assertEquals(6, occurrences.size)
        assertEquals(DayOfWeek.MONDAY, occurrences[0].dayOfWeek)
        assertEquals(DayOfWeek.WEDNESDAY, occurrences[1].dayOfWeek)
        assertEquals(DayOfWeek.FRIDAY, occurrences[2].dayOfWeek)
        assertEquals(DayOfWeek.MONDAY, occurrences[3].dayOfWeek)
    }

    @Test
    fun `2주마다 화요일 반복`() {
        // Given
        val rule = RecurrenceRule(
            type = RecurrenceType.WEEKLY,
            interval = 2,
            daysOfWeek = setOf(DayOfWeek.TUESDAY)
        )
        val start = LocalDate.of(2025, 1, 7) // 화요일

        // When
        val occurrences = scheduler.calculateNextOccurrences(rule, start, RecurrenceEnd.Never, limit = 4)

        // Then
        assertEquals(4, occurrences.size)
        assertEquals(LocalDate.of(2025, 1, 7), occurrences[0])
        assertEquals(LocalDate.of(2025, 1, 21), occurrences[1]) // 2주 후
        assertEquals(LocalDate.of(2025, 2, 4), occurrences[2])  // 4주 후
    }

    @Test
    fun `매월 15일 반복`() {
        // Given
        val rule = RecurrenceRule(
            type = RecurrenceType.MONTHLY,
            interval = 1,
            dayOfMonth = 15
        )
        val start = LocalDate.of(2025, 1, 15)

        // When
        val occurrences = scheduler.calculateNextOccurrences(rule, start, RecurrenceEnd.Never, limit = 3)

        // Then
        assertEquals(3, occurrences.size)
        assertEquals(LocalDate.of(2025, 1, 15), occurrences[0])
        assertEquals(LocalDate.of(2025, 2, 15), occurrences[1])
        assertEquals(LocalDate.of(2025, 3, 15), occurrences[2])
    }

    @Test
    fun `2개월마다 1일 반복`() {
        // Given
        val rule = RecurrenceRule(
            type = RecurrenceType.MONTHLY,
            interval = 2,
            dayOfMonth = 1
        )
        val start = LocalDate.of(2025, 1, 1)

        // When
        val occurrences = scheduler.calculateNextOccurrences(rule, start, RecurrenceEnd.Never, limit = 4)

        // Then
        assertEquals(4, occurrences.size)
        assertEquals(LocalDate.of(2025, 1, 1), occurrences[0])
        assertEquals(LocalDate.of(2025, 3, 1), occurrences[1])
        assertEquals(LocalDate.of(2025, 5, 1), occurrences[2])
        assertEquals(LocalDate.of(2025, 7, 1), occurrences[3])
    }

    @Test
    fun `매년 1월 1일 반복`() {
        // Given
        val rule = RecurrenceRule(
            type = RecurrenceType.YEARLY,
            interval = 1
        )
        val start = LocalDate.of(2025, 1, 1)

        // When
        val occurrences = scheduler.calculateNextOccurrences(rule, start, RecurrenceEnd.Never, limit = 3)

        // Then
        assertEquals(3, occurrences.size)
        assertEquals(LocalDate.of(2025, 1, 1), occurrences[0])
        assertEquals(LocalDate.of(2026, 1, 1), occurrences[1])
        assertEquals(LocalDate.of(2027, 1, 1), occurrences[2])
    }

    @Test
    fun `N회 후 종료 - 3회 반복`() {
        // Given
        val rule = RecurrenceRule(type = RecurrenceType.DAILY, interval = 1)
        val start = LocalDate.of(2025, 1, 1)
        val end = RecurrenceEnd.AfterOccurrences(3)

        // When
        val occurrences = scheduler.calculateNextOccurrences(rule, start, end, limit = 10)

        // Then
        assertEquals(3, occurrences.size)
        assertEquals(LocalDate.of(2025, 1, 1), occurrences[0])
        assertEquals(LocalDate.of(2025, 1, 2), occurrences[1])
        assertEquals(LocalDate.of(2025, 1, 3), occurrences[2])
    }

    @Test
    fun `특정 날짜에 종료`() {
        // Given
        val rule = RecurrenceRule(type = RecurrenceType.DAILY, interval = 1)
        val start = LocalDate.of(2025, 1, 1)
        val end = RecurrenceEnd.OnDate(LocalDate.of(2025, 1, 5))

        // When
        val occurrences = scheduler.calculateNextOccurrences(rule, start, end, limit = 10)

        // Then
        assertEquals(5, occurrences.size)
        assertEquals(LocalDate.of(2025, 1, 5), occurrences.last())
    }

    @Test
    fun `예외 날짜 스킵 - 매일 반복에서 특정 날짜 제외`() {
        // Given
        val rule = RecurrenceRule(type = RecurrenceType.DAILY, interval = 1)
        val start = LocalDate.of(2025, 1, 1)
        val exceptions = setOf(
            LocalDate.of(2025, 1, 2),
            LocalDate.of(2025, 1, 4)
        )

        // When
        val occurrences = scheduler.calculateNextOccurrences(rule, start, RecurrenceEnd.Never, limit = 5, exceptions = exceptions)

        // Then
        assertEquals(5, occurrences.size)
        assertEquals(LocalDate.of(2025, 1, 1), occurrences[0])
        assertEquals(LocalDate.of(2025, 1, 3), occurrences[1]) // 1/2 스킵
        assertEquals(LocalDate.of(2025, 1, 5), occurrences[2]) // 1/4 스킵
        assertEquals(LocalDate.of(2025, 1, 6), occurrences[3])
    }

    @Test
    fun `특정 날짜가 반복 발생 날짜인지 확인 - 매일`() {
        // Given
        val rule = RecurrenceRule(type = RecurrenceType.DAILY, interval = 1)
        val start = LocalDate.of(2025, 1, 1)
        val checkDate = LocalDate.of(2025, 1, 10)

        // When
        val isOccurrence = scheduler.isOccurrenceDate(rule, start, checkDate)

        // Then
        assertTrue(isOccurrence)
    }

    @Test
    fun `특정 날짜가 반복 발생 날짜인지 확인 - 2일마다 (해당)`() {
        // Given
        val rule = RecurrenceRule(type = RecurrenceType.DAILY, interval = 2)
        val start = LocalDate.of(2025, 1, 1)
        val checkDate = LocalDate.of(2025, 1, 5) // 1, 3, 5 -> 해당

        // When
        val isOccurrence = scheduler.isOccurrenceDate(rule, start, checkDate)

        // Then
        assertTrue(isOccurrence)
    }

    @Test
    fun `특정 날짜가 반복 발생 날짜인지 확인 - 2일마다 (해당 안 됨)`() {
        // Given
        val rule = RecurrenceRule(type = RecurrenceType.DAILY, interval = 2)
        val start = LocalDate.of(2025, 1, 1)
        val checkDate = LocalDate.of(2025, 1, 4) // 1, 3, 5 -> 4는 해당 안 됨

        // When
        val isOccurrence = scheduler.isOccurrenceDate(rule, start, checkDate)

        // Then
        assertFalse(isOccurrence)
    }

    @Test
    fun `자연어 설명 - 매일`() {
        // Given
        val rule = RecurrenceRule(type = RecurrenceType.DAILY, interval = 1)

        // When
        val description = rule.toNaturalLanguage()

        // Then
        assertEquals("매일", description)
    }

    @Test
    fun `자연어 설명 - 매주 월, 수, 금`() {
        // Given
        val rule = RecurrenceRule(
            type = RecurrenceType.WEEKLY,
            interval = 1,
            daysOfWeek = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
        )

        // When
        val description = rule.toNaturalLanguage()

        // Then
        assertTrue(description.contains("월요일"))
        assertTrue(description.contains("수요일"))
        assertTrue(description.contains("금요일"))
    }
}
