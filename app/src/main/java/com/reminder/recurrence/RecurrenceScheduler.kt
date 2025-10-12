package com.reminder.recurrence

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * v1.35.0: 반복 일정 계산 엔진
 *
 * TDD Green Phase - 테스트를 통과하는 구현
 */
class RecurrenceScheduler {

    /**
     * 다음 N회의 반복 발생 날짜 계산
     *
     * @param rule 반복 규칙
     * @param start 시작 날짜
     * @param end 종료 조건
     * @param limit 최대 계산 횟수
     * @param exceptions 제외할 날짜들
     * @return 반복 발생 날짜 리스트
     */
    fun calculateNextOccurrences(
        rule: RecurrenceRule,
        start: LocalDate,
        end: RecurrenceEnd,
        limit: Int,
        exceptions: Set<LocalDate> = emptySet()
    ): List<LocalDate> {
        val occurrences = mutableListOf<LocalDate>()
        var currentDate = start
        var count = 0

        // 최대 1000회까지만 계산 (무한 루프 방지)
        val maxIterations = 1000

        while (occurrences.size < limit && count < maxIterations) {
            // 종료 조건 확인
            when (end) {
                is RecurrenceEnd.AfterOccurrences -> {
                    if (occurrences.size >= end.count) break
                }
                is RecurrenceEnd.OnDate -> {
                    if (currentDate.isAfter(end.date)) break
                }
                is RecurrenceEnd.Never -> {
                    // 계속 진행
                }
            }

            // 예외 날짜가 아니면 추가
            if (!exceptions.contains(currentDate)) {
                occurrences.add(currentDate)
            }

            // 다음 날짜 계산
            currentDate = calculateNextDate(rule, start, currentDate)
            count++
        }

        return occurrences
    }

    /**
     * 특정 날짜가 반복 발생 날짜인지 확인
     *
     * @param rule 반복 규칙
     * @param start 시작 날짜
     * @param checkDate 확인할 날짜
     * @return 반복 발생 날짜 여부
     */
    fun isOccurrenceDate(
        rule: RecurrenceRule,
        start: LocalDate,
        checkDate: LocalDate
    ): Boolean {
        // 시작 날짜 이전은 발생하지 않음
        if (checkDate.isBefore(start)) return false

        return when (rule.type) {
            RecurrenceType.DAILY -> {
                val daysBetween = ChronoUnit.DAYS.between(start, checkDate)
                daysBetween % rule.interval == 0L
            }
            RecurrenceType.WEEKLY -> {
                val weeksBetween = ChronoUnit.WEEKS.between(start, checkDate)
                if (weeksBetween % rule.interval != 0L) return false

                // 요일 확인
                rule.daysOfWeek?.contains(checkDate.dayOfWeek) ?: true
            }
            RecurrenceType.MONTHLY -> {
                val monthsBetween = ChronoUnit.MONTHS.between(start, checkDate)
                if (monthsBetween % rule.interval != 0L) return false

                // 날짜 확인
                rule.dayOfMonth?.let { checkDate.dayOfMonth == it } ?: true
            }
            RecurrenceType.YEARLY -> {
                val yearsBetween = ChronoUnit.YEARS.between(start, checkDate)
                if (yearsBetween % rule.interval != 0L) return false

                // 월/일 확인
                checkDate.month == start.month && checkDate.dayOfMonth == start.dayOfMonth
            }
            RecurrenceType.CUSTOM -> {
                // 사용자 정의는 기본적으로 false
                false
            }
        }
    }

    /**
     * 다음 반복 날짜 계산
     */
    private fun calculateNextDate(
        rule: RecurrenceRule,
        @Suppress("UNUSED_PARAMETER") start: LocalDate,
        current: LocalDate
    ): LocalDate {
        return when (rule.type) {
            RecurrenceType.DAILY -> {
                current.plusDays(rule.interval.toLong())
            }
            RecurrenceType.WEEKLY -> {
                calculateNextWeeklyDate(rule, current)
            }
            RecurrenceType.MONTHLY -> {
                calculateNextMonthlyDate(rule, current)
            }
            RecurrenceType.YEARLY -> {
                current.plusYears(rule.interval.toLong())
            }
            RecurrenceType.CUSTOM -> {
                current.plusDays(1) // 기본값
            }
        }
    }

    /**
     * 주간 반복 다음 날짜 계산
     */
    private fun calculateNextWeeklyDate(
        rule: RecurrenceRule,
        current: LocalDate
    ): LocalDate {
        val daysOfWeek = rule.daysOfWeek ?: return current.plusWeeks(rule.interval.toLong())

        // 정렬된 요일 목록
        val sortedDays = daysOfWeek.sortedBy { it.value }

        // 현재 요일 다음 요일 찾기
        val currentDayOfWeek = current.dayOfWeek
        val nextDayInWeek = sortedDays.firstOrNull { it.value > currentDayOfWeek.value }

        return if (nextDayInWeek != null) {
            // 같은 주 내 다음 요일
            val daysToAdd = nextDayInWeek.value - currentDayOfWeek.value
            current.plusDays(daysToAdd.toLong())
        } else {
            // 다음 주의 첫 번째 요일
            val firstDayOfWeek = sortedDays.first()
            val daysToAdd = 7 - currentDayOfWeek.value + firstDayOfWeek.value +
                           (rule.interval - 1) * 7
            current.plusDays(daysToAdd.toLong())
        }
    }

    /**
     * 월간 반복 다음 날짜 계산
     */
    private fun calculateNextMonthlyDate(
        rule: RecurrenceRule,
        current: LocalDate
    ): LocalDate {
        val dayOfMonth = rule.dayOfMonth ?: current.dayOfMonth

        // interval 개월 후의 같은 날짜
        var nextDate = current.plusMonths(rule.interval.toLong())

        // 해당 월에 그 날짜가 없으면 마지막 날로 조정
        val lastDayOfMonth = nextDate.lengthOfMonth()
        if (dayOfMonth > lastDayOfMonth) {
            nextDate = nextDate.withDayOfMonth(lastDayOfMonth)
        } else {
            nextDate = nextDate.withDayOfMonth(dayOfMonth)
        }

        return nextDate
    }
}
