package com.reminder.domain.focus

import com.reminder.data.entity.FocusSessionEntity
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * v1.51.0: 포커스 세션 도메인 로직
 *
 * FocusSessionEntity의 비즈니스 로직을 확장 함수로 구현
 */

/**
 * 세션이 진행 중인지 확인
 */
fun FocusSessionEntity.isActive(): Boolean {
    return endTime == null && !isCompleted && !isInterrupted
}

/**
 * 세션을 완료로 표시
 */
fun FocusSessionEntity.complete(): FocusSessionEntity {
    val now = LocalDateTime.now()
    val actualMinutes = ChronoUnit.MINUTES.between(startTime, now).toInt()

    return copy(
        endTime = now,
        actualDurationMinutes = actualMinutes,
        isCompleted = true,
        isInterrupted = false
    )
}

/**
 * 세션을 중단으로 표시
 */
fun FocusSessionEntity.interrupt(): FocusSessionEntity {
    val now = LocalDateTime.now()
    val actualMinutes = ChronoUnit.MINUTES.between(startTime, now).toInt()

    return copy(
        endTime = now,
        actualDurationMinutes = actualMinutes,
        isCompleted = false,
        isInterrupted = true
    )
}

/**
 * 남은 시간 계산 (분)
 */
fun FocusSessionEntity.getRemainingMinutes(): Int {
    val now = LocalDateTime.now()
    val elapsedMinutes = ChronoUnit.MINUTES.between(startTime, now).toInt()
    val remaining = targetDurationMinutes - elapsedMinutes

    return maxOf(0, remaining)
}

/**
 * 진행률 계산 (0-100%)
 */
fun FocusSessionEntity.getProgress(): Int {
    val now = LocalDateTime.now()
    val elapsedMinutes = ChronoUnit.MINUTES.between(startTime, now).toInt()
    val progress = (elapsedMinutes.toFloat() / targetDurationMinutes * 100).toInt()

    return minOf(100, progress)
}

/**
 * 완료된 세션들의 총 집중 시간 계산 (분)
 */
fun List<FocusSessionEntity>.calculateTotalFocusMinutes(): Int {
    return filter { it.isCompleted }
        .sumOf { it.actualDurationMinutes }
}

/**
 * 연속 집중 기록(Streak) 계산
 *
 * 오늘부터 과거로 거슬러 올라가며 매일 완료된 세션이 있는지 확인
 * 하루라도 빠지면 Streak 끊김
 */
fun List<FocusSessionEntity>.calculateStreak(): Int {
    val completedSessions = filter { it.isCompleted }
    if (completedSessions.isEmpty()) return 0

    // 날짜별로 그룹화 (시간 무시, 날짜만)
    val sessionsByDate = completedSessions
        .groupBy { it.startTime.toLocalDate() }
        .toSortedMap(reverseOrder()) // 최신 날짜부터

    val today = LocalDateTime.now().toLocalDate()
    var currentDate = today
    var streak = 0

    // 오늘부터 과거로 거슬러 올라가며 확인
    while (sessionsByDate.containsKey(currentDate)) {
        streak++
        currentDate = currentDate.minusDays(1)
    }

    // 오늘 기록이 없으면 Streak는 0
    if (!sessionsByDate.containsKey(today)) {
        return 0
    }

    return streak
}
