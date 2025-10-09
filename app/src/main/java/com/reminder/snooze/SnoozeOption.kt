package com.reminder.snooze

import java.time.LocalDateTime

/**
 * 스누즈 옵션
 *
 * 리마인더를 일시적으로 연기할 수 있는 시간 옵션
 */
enum class SnoozeOption(
    val label: String,
    val minutes: Int
) {
    FIVE_MINUTES("5분", 5),
    TEN_MINUTES("10분", 10),
    THIRTY_MINUTES("30분", 30),
    ONE_HOUR("1시간", 60),
    TWO_HOURS("2시간", 120),
    TOMORROW("내일 오전 9시", -1);  // 특수 케이스

    /**
     * 현재 시간으로부터 스누즈 시간 계산
     */
    fun calculateSnoozeTime(from: LocalDateTime = LocalDateTime.now()): LocalDateTime {
        return when (this) {
            TOMORROW -> {
                // 내일 오전 9시
                val tomorrow = from.plusDays(1)
                tomorrow.withHour(9).withMinute(0).withSecond(0).withNano(0)
            }
            else -> {
                // 분 단위 추가
                from.plusMinutes(minutes.toLong())
            }
        }
    }

    companion object {
        /**
         * 모든 스누즈 옵션 목록
         */
        fun all(): List<SnoozeOption> = values().toList()
    }
}
