package com.reminder.recurrence

import java.time.DayOfWeek
import java.time.Month

/**
 * v1.35.0: 반복 규칙 정의
 *
 * 다양한 반복 패턴을 지원하는 유연한 반복 규칙
 */
data class RecurrenceRule(
    /**
     * 반복 타입
     */
    val type: RecurrenceType,

    /**
     * 반복 간격 (예: 2일마다, 3주마다)
     */
    val interval: Int = 1,

    /**
     * 주간 반복 시 요일 (월, 수, 금 등)
     */
    val daysOfWeek: Set<DayOfWeek>? = null,

    /**
     * 월간 반복 시 날짜 (매월 15일 등)
     */
    val dayOfMonth: Int? = null,

    /**
     * 월간 반복 시 주 (매월 첫째 주 등)
     */
    val weekOfMonth: Int? = null,

    /**
     * 연간 반복 시 월 (1월, 7월만 등)
     */
    val monthsOfYear: Set<Month>? = null
) {
    /**
     * 자연어 설명 생성
     */
    fun toNaturalLanguage(): String {
        return when (type) {
            RecurrenceType.DAILY -> {
                if (interval == 1) "매일" else "${interval}일마다"
            }
            RecurrenceType.WEEKLY -> {
                val days = daysOfWeek?.joinToString(", ") { dayOfWeekToKorean(it) } ?: "매주"
                if (interval == 1) "매주 $days" else "${interval}주마다 $days"
            }
            RecurrenceType.MONTHLY -> {
                when {
                    dayOfMonth != null -> {
                        if (interval == 1) "매월 ${dayOfMonth}일" else "${interval}개월마다 ${dayOfMonth}일"
                    }
                    weekOfMonth != null && daysOfWeek != null -> {
                        val week = weekOfMonthToKorean(weekOfMonth)
                        val day = daysOfWeek.firstOrNull()?.let { dayOfWeekToKorean(it) } ?: ""
                        if (interval == 1) "매월 $week $day" else "${interval}개월마다 $week $day"
                    }
                    else -> if (interval == 1) "매월" else "${interval}개월마다"
                }
            }
            RecurrenceType.YEARLY -> {
                if (interval == 1) "매년" else "${interval}년마다"
            }
            RecurrenceType.CUSTOM -> "사용자 정의"
        }
    }

    private fun dayOfWeekToKorean(dayOfWeek: DayOfWeek): String {
        return when (dayOfWeek) {
            DayOfWeek.MONDAY -> "월요일"
            DayOfWeek.TUESDAY -> "화요일"
            DayOfWeek.WEDNESDAY -> "수요일"
            DayOfWeek.THURSDAY -> "목요일"
            DayOfWeek.FRIDAY -> "금요일"
            DayOfWeek.SATURDAY -> "토요일"
            DayOfWeek.SUNDAY -> "일요일"
        }
    }

    private fun weekOfMonthToKorean(week: Int): String {
        return when (week) {
            1 -> "첫째 주"
            2 -> "둘째 주"
            3 -> "셋째 주"
            4 -> "넷째 주"
            5 -> "다섯째 주"
            -1 -> "마지막 주"
            else -> "${week}째 주"
        }
    }
}

/**
 * 반복 타입
 */
enum class RecurrenceType {
    DAILY,      // 일간 반복
    WEEKLY,     // 주간 반복
    MONTHLY,    // 월간 반복
    YEARLY,     // 연간 반복
    CUSTOM      // 사용자 정의
}

/**
 * v1.35.0: 반복 종료 조건
 */
sealed class RecurrenceEnd {
    /**
     * 종료 없음 (무한 반복)
     */
    object Never : RecurrenceEnd() {
        override fun toString() = "종료 없음"
    }

    /**
     * N회 후 종료
     */
    data class AfterOccurrences(val count: Int) : RecurrenceEnd() {
        override fun toString() = "${count}회 후 종료"
    }

    /**
     * 특정 날짜에 종료
     */
    data class OnDate(val date: java.time.LocalDate) : RecurrenceEnd() {
        override fun toString() = "${date}에 종료"
    }
}
