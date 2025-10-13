package com.reminder.recurrence

import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

/**
 * v1.65.0: 반복 작업의 다음 발생 시간을 계산하는 유틸리티
 *
 * RecurrenceRule과 RecurrenceEnd를 기반으로 다음 발생 시간을 계산합니다.
 */
object RecurrenceCalculator {

    /**
     * 다음 발생 시간 계산
     *
     * @param currentTime 현재 시간 (또는 마지막 발생 시간)
     * @param recurrenceRule 반복 규칙
     * @param recurrenceEnd 반복 종료 조건
     * @param occurrenceCount 현재까지 발생 횟수 (AfterOccurrences 종료 조건용)
     * @return 다음 발생 시간 (종료 조건 도달 시 null)
     */
    fun calculateNextOccurrence(
        currentTime: LocalDateTime,
        recurrenceRule: RecurrenceRule,
        recurrenceEnd: RecurrenceEnd,
        occurrenceCount: Int = 0
    ): LocalDateTime? {
        // 1. 종료 조건 체크
        if (!shouldContinue(currentTime, recurrenceEnd, occurrenceCount)) {
            return null
        }

        // 2. RecurrenceType에 따라 다음 시간 계산
        val nextTime = when (recurrenceRule.type) {
            RecurrenceType.DAILY -> calculateDailyNext(currentTime, recurrenceRule)
            RecurrenceType.WEEKLY -> calculateWeeklyNext(currentTime, recurrenceRule)
            RecurrenceType.MONTHLY -> calculateMonthlyNext(currentTime, recurrenceRule)
            RecurrenceType.YEARLY -> calculateYearlyNext(currentTime, recurrenceRule)
            RecurrenceType.CUSTOM -> null // 현재 미지원
        }

        // 3. 계산된 시간이 종료 조건을 초과하면 null 반환
        if (nextTime != null && recurrenceEnd is RecurrenceEnd.OnDate) {
            if (nextTime.toLocalDate().isAfter(recurrenceEnd.date)) {
                return null
            }
        }

        return nextTime
    }

    /**
     * 반복을 계속해야 하는지 확인
     */
    private fun shouldContinue(
        currentTime: LocalDateTime,
        recurrenceEnd: RecurrenceEnd,
        occurrenceCount: Int
    ): Boolean {
        return when (recurrenceEnd) {
            is RecurrenceEnd.Never -> true
            is RecurrenceEnd.AfterOccurrences -> occurrenceCount < recurrenceEnd.count
            is RecurrenceEnd.OnDate -> !currentTime.toLocalDate().isAfter(recurrenceEnd.date)
        }
    }

    /**
     * 일간 반복의 다음 발생 시간 계산
     */
    private fun calculateDailyNext(
        currentTime: LocalDateTime,
        rule: RecurrenceRule
    ): LocalDateTime {
        return currentTime.plusDays(rule.interval.toLong())
    }

    /**
     * 주간 반복의 다음 발생 시간 계산
     */
    private fun calculateWeeklyNext(
        currentTime: LocalDateTime,
        rule: RecurrenceRule
    ): LocalDateTime {
        val daysOfWeek = rule.daysOfWeek ?: return currentTime.plusWeeks(rule.interval.toLong())

        // 현재 요일
        val currentDayOfWeek = currentTime.dayOfWeek

        // daysOfWeek를 요일 순서대로 정렬
        val sortedDays = daysOfWeek.sortedBy { it.value }

        // 현재 요일 이후의 다음 요일 찾기
        val nextDayInSameWeek = sortedDays.firstOrNull { it.value > currentDayOfWeek.value }

        return if (nextDayInSameWeek != null) {
            // 같은 주 내에 다음 요일이 있음
            val daysUntilNext = nextDayInSameWeek.value - currentDayOfWeek.value
            currentTime.plusDays(daysUntilNext.toLong())
        } else {
            // 다음 주로 넘어감
            val firstDay = sortedDays.first()
            currentTime.plusWeeks(rule.interval.toLong())
                .minusDays(currentDayOfWeek.value.toLong())
                .plusDays(firstDay.value.toLong())
        }
    }

    /**
     * 월간 반복의 다음 발생 시간 계산
     */
    private fun calculateMonthlyNext(
        currentTime: LocalDateTime,
        rule: RecurrenceRule
    ): LocalDateTime {
        val dayOfMonth = rule.dayOfMonth ?: return currentTime.plusMonths(rule.interval.toLong())

        var nextMonth = currentTime.plusMonths(rule.interval.toLong())

        // 해당 월에 dayOfMonth가 없으면 다음 달로 넘어감
        while (dayOfMonth > nextMonth.month.length(nextMonth.toLocalDate().isLeapYear)) {
            nextMonth = nextMonth.plusMonths(1)
        }

        return nextMonth.withDayOfMonth(dayOfMonth)
    }

    /**
     * 연간 반복의 다음 발생 시간 계산
     */
    private fun calculateYearlyNext(
        currentTime: LocalDateTime,
        rule: RecurrenceRule
    ): LocalDateTime {
        return currentTime.plusYears(rule.interval.toLong())
    }
}
