package com.reminder.data.entity

import java.time.DayOfWeek
import java.time.LocalDateTime

/**
 * 반복 리마인더 규칙
 */
data class RecurrenceRule(
    val pattern: RecurrencePattern = RecurrencePattern.NONE,
    val interval: Int = 1,                    // 간격 (예: 2주마다 = interval:2, pattern:WEEKLY)
    val daysOfWeek: Set<DayOfWeek>? = null,   // 매주 반복 시 요일 선택
    val endDate: LocalDateTime? = null        // 반복 종료일 (null이면 무한)
) {
    /**
     * 다음 반복 날짜 계산
     */
    fun getNextOccurrence(from: LocalDateTime): LocalDateTime? {
        if (pattern == RecurrencePattern.NONE) return null
        if (endDate != null && from.isAfter(endDate)) return null

        return when (pattern) {
            RecurrencePattern.NONE -> null
            RecurrencePattern.DAILY -> from.plusDays(interval.toLong())
            RecurrencePattern.WEEKLY -> {
                if (daysOfWeek.isNullOrEmpty()) {
                    from.plusWeeks(interval.toLong())
                } else {
                    findNextDayOfWeek(from, daysOfWeek)
                }
            }
            RecurrencePattern.MONTHLY -> from.plusMonths(interval.toLong())
            RecurrencePattern.YEARLY -> from.plusYears(interval.toLong())
        }?.let { next ->
            if (endDate != null && next.isAfter(endDate)) null else next
        }
    }

    private fun findNextDayOfWeek(from: LocalDateTime, daysOfWeek: Set<DayOfWeek>): LocalDateTime {
        var current = from.plusDays(1)
        val maxDays = 7 * interval
        var daysChecked = 0

        while (daysChecked < maxDays) {
            if (daysOfWeek.contains(current.dayOfWeek)) {
                return current
            }
            current = current.plusDays(1)
            daysChecked++
        }

        return from.plusWeeks(interval.toLong())
    }
}

/**
 * 반복 패턴
 */
enum class RecurrencePattern {
    NONE,        // 반복 없음
    DAILY,       // 매일
    WEEKLY,      // 매주
    MONTHLY,     // 매월
    YEARLY       // 매년
}
