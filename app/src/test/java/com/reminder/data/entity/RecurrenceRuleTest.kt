package com.reminder.data.entity

import org.junit.Assert.*
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDateTime

/**
 * RecurrenceRule 단위 테스트
 */
class RecurrenceRuleTest {

    @Test
    fun `반복 없음 시 다음 발생 없음`() {
        // Given
        val rule = RecurrenceRule(pattern = RecurrencePattern.NONE)
        val from = LocalDateTime.of(2025, 10, 8, 10, 0)

        // When
        val next = rule.getNextOccurrence(from)

        // Then
        assertNull(next)
    }

    @Test
    fun `매일 반복 - 다음 날짜 계산`() {
        // Given
        val rule = RecurrenceRule(pattern = RecurrencePattern.DAILY, interval = 1)
        val from = LocalDateTime.of(2025, 10, 8, 10, 0)

        // When
        val next = rule.getNextOccurrence(from)

        // Then
        assertEquals(LocalDateTime.of(2025, 10, 9, 10, 0), next)
    }

    @Test
    fun `2일마다 반복`() {
        // Given
        val rule = RecurrenceRule(pattern = RecurrencePattern.DAILY, interval = 2)
        val from = LocalDateTime.of(2025, 10, 8, 10, 0)

        // When
        val next = rule.getNextOccurrence(from)

        // Then
        assertEquals(LocalDateTime.of(2025, 10, 10, 10, 0), next)
    }

    @Test
    fun `매주 반복 - 요일 지정 없음`() {
        // Given
        val rule = RecurrenceRule(pattern = RecurrencePattern.WEEKLY, interval = 1)
        val from = LocalDateTime.of(2025, 10, 8, 10, 0) // 수요일

        // When
        val next = rule.getNextOccurrence(from)

        // Then
        assertEquals(LocalDateTime.of(2025, 10, 15, 10, 0), next)
    }

    @Test
    fun `매주 월수금 반복`() {
        // Given
        val rule = RecurrenceRule(
            pattern = RecurrencePattern.WEEKLY,
            interval = 1,
            daysOfWeek = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
        )
        val from = LocalDateTime.of(2025, 10, 8, 10, 0) // 수요일

        // When
        val next = rule.getNextOccurrence(from)

        // Then
        // 다음은 금요일 (10월 10일)
        assertEquals(LocalDateTime.of(2025, 10, 10, 10, 0), next)
    }

    @Test
    fun `매주 월수금 반복 - 금요일에서 다음은 월요일`() {
        // Given
        val rule = RecurrenceRule(
            pattern = RecurrencePattern.WEEKLY,
            interval = 1,
            daysOfWeek = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
        )
        val from = LocalDateTime.of(2025, 10, 10, 10, 0) // 금요일

        // When
        val next = rule.getNextOccurrence(from)

        // Then
        // 다음은 월요일 (10월 13일)
        assertEquals(LocalDateTime.of(2025, 10, 13, 10, 0), next)
    }

    @Test
    fun `매월 반복`() {
        // Given
        val rule = RecurrenceRule(pattern = RecurrencePattern.MONTHLY, interval = 1)
        val from = LocalDateTime.of(2025, 10, 8, 10, 0)

        // When
        val next = rule.getNextOccurrence(from)

        // Then
        assertEquals(LocalDateTime.of(2025, 11, 8, 10, 0), next)
    }

    @Test
    fun `매년 반복`() {
        // Given
        val rule = RecurrenceRule(pattern = RecurrencePattern.YEARLY, interval = 1)
        val from = LocalDateTime.of(2025, 10, 8, 10, 0)

        // When
        val next = rule.getNextOccurrence(from)

        // Then
        assertEquals(LocalDateTime.of(2026, 10, 8, 10, 0), next)
    }

    @Test
    fun `종료일 이후에는 다음 발생 없음`() {
        // Given
        val endDate = LocalDateTime.of(2025, 10, 10, 0, 0)
        val rule = RecurrenceRule(
            pattern = RecurrencePattern.DAILY,
            interval = 1,
            endDate = endDate
        )
        val from = LocalDateTime.of(2025, 10, 11, 10, 0) // 종료일 이후

        // When
        val next = rule.getNextOccurrence(from)

        // Then
        assertNull(next)
    }

    @Test
    fun `종료일 이전에는 다음 발생 계산`() {
        // Given
        val endDate = LocalDateTime.of(2025, 10, 15, 0, 0)
        val rule = RecurrenceRule(
            pattern = RecurrencePattern.DAILY,
            interval = 1,
            endDate = endDate
        )
        val from = LocalDateTime.of(2025, 10, 8, 10, 0)

        // When
        val next = rule.getNextOccurrence(from)

        // Then
        assertEquals(LocalDateTime.of(2025, 10, 9, 10, 0), next)
    }

    @Test
    fun `종료일 직전 다음 발생이 종료일 이후면 null 반환`() {
        // Given
        val endDate = LocalDateTime.of(2025, 10, 9, 0, 0)
        val rule = RecurrenceRule(
            pattern = RecurrencePattern.DAILY,
            interval = 1,
            endDate = endDate
        )
        val from = LocalDateTime.of(2025, 10, 8, 10, 0)

        // When
        val next = rule.getNextOccurrence(from)

        // Then
        // 다음 발생은 10/9 10:00인데, 종료일이 10/9 00:00이므로 이후임 -> null
        assertNull(next)
    }
}
