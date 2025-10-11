package com.reminder.domain

import com.reminder.data.entity.Priority
import com.reminder.data.entity.ReminderEntity
import com.reminder.data.entity.Urgency

/**
 * v1.47.0: Eisenhower Matrix (아이젠하워 매트릭스)
 *
 * 생산성 향상을 위한 4개 쿼드런트:
 * - Q1 (DO_FIRST): 중요하고 긴급함 - 즉시 처리해야 할 일
 * - Q2 (SCHEDULE): 중요하지만 긴급하지 않음 - 계획을 세워 처리할 일
 * - Q3 (DELEGATE): 긴급하지만 중요하지 않음 - 위임하거나 빠르게 처리할 일
 * - Q4 (DELETE): 중요하지도 긴급하지도 않음 - 제거하거나 최소화할 일
 */
enum class Quadrant {
    DO_FIRST,   // Q1: 중요 + 긴급
    SCHEDULE,   // Q2: 중요 + 긴급하지 않음
    DELEGATE,   // Q3: 중요하지 않음 + 긴급
    DELETE      // Q4: 중요하지 않음 + 긴급하지 않음
}

/**
 * ReminderEntity의 Priority와 Urgency를 기반으로 Eisenhower Matrix 쿼드런트를 계산
 *
 * 계산 로직:
 * - DO_FIRST: (Priority >= MEDIUM AND Urgency >= MEDIUM) AND (Priority == HIGH OR Urgency == HIGH)
 * - SCHEDULE: Priority >= MEDIUM AND Urgency < MEDIUM
 * - DELEGATE: Priority == LOW AND Urgency >= MEDIUM
 * - DELETE: Priority == LOW AND Urgency == LOW
 */
fun ReminderEntity.getQuadrant(): Quadrant {
    return when {
        // Q1: 중요하고 긴급함
        // - HIGH 우선순위 + HIGH/MEDIUM 긴급도
        // - MEDIUM 우선순위 + HIGH 긴급도
        (priority == Priority.HIGH && urgency >= Urgency.MEDIUM) ||
        (priority == Priority.MEDIUM && urgency == Urgency.HIGH) -> {
            Quadrant.DO_FIRST
        }

        // Q2: 중요하지만 긴급하지 않음
        // - HIGH/MEDIUM 우선순위 + LOW 긴급도
        // - MEDIUM 우선순위 + MEDIUM 긴급도
        priority >= Priority.MEDIUM && urgency < Urgency.HIGH -> {
            Quadrant.SCHEDULE
        }

        // Q3: 긴급하지만 중요하지 않음
        // - LOW 우선순위 + HIGH/MEDIUM 긴급도
        priority == Priority.LOW && urgency >= Urgency.MEDIUM -> {
            Quadrant.DELEGATE
        }

        // Q4: 중요하지도 긴급하지도 않음
        // - LOW 우선순위 + LOW 긴급도
        else -> {
            Quadrant.DELETE
        }
    }
}

/**
 * 리마인더 리스트를 특정 쿼드런트로 필터링
 */
fun List<ReminderEntity>.filterByQuadrant(quadrant: Quadrant): List<ReminderEntity> {
    return this.filter { it.getQuadrant() == quadrant }
}

/**
 * 리마인더 리스트의 쿼드런트별 개수를 계산
 * @return Map<Quadrant, Int> - 각 쿼드런트별 리마인더 개수
 */
fun List<ReminderEntity>.countByQuadrant(): Map<Quadrant, Int> {
    return Quadrant.entries.associateWith { quadrant ->
        this.count { it.getQuadrant() == quadrant }
    }
}

/**
 * 쿼드런트 정보를 가져오는 헬퍼 클래스
 */
data class QuadrantInfo(
    val quadrant: Quadrant,
    val title: String,
    val description: String,
    val color: Long  // Compose Color 값 (0xFFRRGGBB 형식)
)

/**
 * 쿼드런트별 UI 정보
 */
fun Quadrant.getInfo(): QuadrantInfo {
    return when (this) {
        Quadrant.DO_FIRST -> QuadrantInfo(
            quadrant = this,
            title = "즉시 처리",
            description = "중요하고 긴급함",
            color = 0xFFEF5350  // Red
        )
        Quadrant.SCHEDULE -> QuadrantInfo(
            quadrant = this,
            title = "계획 수립",
            description = "중요하지만 긴급하지 않음",
            color = 0xFF42A5F5  // Blue
        )
        Quadrant.DELEGATE -> QuadrantInfo(
            quadrant = this,
            title = "위임",
            description = "긴급하지만 중요하지 않음",
            color = 0xFFFFCA28  // Amber
        )
        Quadrant.DELETE -> QuadrantInfo(
            quadrant = this,
            title = "제거/최소화",
            description = "중요하지도 긴급하지도 않음",
            color = 0xFF66BB6A  // Green
        )
    }
}

/**
 * v1.49.0: 쿼드런트 통계 데이터 클래스
 */
data class QuadrantStats(
    val quadrant: Quadrant,
    val totalCount: Int,
    val completedCount: Int,
    val completionRate: Double,  // 0.0 ~ 100.0
    val averageCompletionMinutes: Double  // 평균 처리 시간 (분)
)

/**
 * v1.49.0: 리마인더 리스트의 쿼드런트별 통계 계산
 *
 * @param quadrant 통계를 계산할 쿼드런트
 * @return QuadrantStats - 완료율, 평균 처리 시간 등
 */
fun List<ReminderEntity>.calculateQuadrantStats(quadrant: Quadrant): QuadrantStats {
    val quadrantReminders = this.filterByQuadrant(quadrant)
    val totalCount = quadrantReminders.size
    val completedReminders = quadrantReminders.filter { it.isCompleted }
    val completedCount = completedReminders.size

    // 완료율 계산 (0.0 ~ 100.0)
    val completionRate = if (totalCount > 0) {
        (completedCount.toDouble() / totalCount) * 100.0
    } else {
        0.0
    }

    // 평균 처리 시간 계산 (분 단위)
    val averageCompletionMinutes = if (completedCount > 0) {
        val totalMinutes = completedReminders.sumOf { reminder ->
            java.time.Duration.between(reminder.createdAt, reminder.updatedAt).toMinutes()
        }
        totalMinutes.toDouble() / completedCount
    } else {
        0.0
    }

    return QuadrantStats(
        quadrant = quadrant,
        totalCount = totalCount,
        completedCount = completedCount,
        completionRate = completionRate,
        averageCompletionMinutes = averageCompletionMinutes
    )
}

/**
 * v1.49.0: 리마인더를 다른 쿼드런트로 이동
 *
 * 이동 시 Priority와 Urgency를 자동으로 조정하여
 * 해당 쿼드런트의 조건을 만족하도록 함
 *
 * @param targetQuadrant 이동할 쿼드런트
 * @return 이동된 ReminderEntity (Priority와 Urgency가 업데이트됨)
 */
fun ReminderEntity.moveToQuadrant(targetQuadrant: Quadrant): ReminderEntity {
    val (newPriority, newUrgency) = when (targetQuadrant) {
        Quadrant.DO_FIRST -> Pair(Priority.HIGH, Urgency.HIGH)
        Quadrant.SCHEDULE -> Pair(Priority.HIGH, Urgency.LOW)
        Quadrant.DELEGATE -> Pair(Priority.LOW, Urgency.HIGH)
        Quadrant.DELETE -> Pair(Priority.LOW, Urgency.LOW)
    }

    return this.copy(
        priority = newPriority,
        urgency = newUrgency,
        updatedAt = java.time.LocalDateTime.now()
    )
}

/**
 * v1.50.0: 트렌드 분석 기간
 */
enum class TrendPeriod(val days: Int) {
    WEEKLY(7),
    MONTHLY(30)
}

/**
 * v1.50.0: 트렌드 데이터 포인트
 */
data class TrendDataPoint(
    val date: java.time.LocalDate,
    val count: Int
)

/**
 * v1.50.0: 쿼드런트 트렌드 데이터
 */
data class QuadrantTrend(
    val quadrant: Quadrant,
    val period: TrendPeriod,
    val dataPoints: List<TrendDataPoint>,
    val totalCompleted: Int
)

/**
 * v1.50.0: 시간대별 분포
 */
data class TimeDistribution(
    val morning: Int,    // 0-11시
    val afternoon: Int,  // 12-17시
    val evening: Int,    // 18-23시
    val night: Int       // 0-5시
)

/**
 * v1.50.0: 쿼드런트별 트렌드 계산
 *
 * @param quadrant 분석할 쿼드런트
 * @param period 기간 (WEEKLY 또는 MONTHLY)
 * @return QuadrantTrend - 날짜별 완료 개수
 */
fun List<ReminderEntity>.calculateQuadrantTrend(
    quadrant: Quadrant,
    period: TrendPeriod
): QuadrantTrend {
    val now = java.time.LocalDateTime.now()
    val startDate = now.minusDays(period.days.toLong()).toLocalDate()

    // 해당 쿼드런트의 완료된 리마인더만 필터링
    val completedReminders = this
        .filterByQuadrant(quadrant)
        .filter { it.isCompleted }
        .filter { it.updatedAt.toLocalDate() >= startDate }

    // 날짜별 그룹화
    val dateGroups = completedReminders.groupBy { it.updatedAt.toLocalDate() }

    // 데이터 포인트 생성 (기간 내 모든 날짜)
    val dataPoints = (0..period.days).map { daysAgo ->
        val date = now.minusDays(daysAgo.toLong()).toLocalDate()
        val count = dateGroups[date]?.size ?: 0
        TrendDataPoint(date, count)
    }

    return QuadrantTrend(
        quadrant = quadrant,
        period = period,
        dataPoints = dataPoints,
        totalCompleted = completedReminders.size
    )
}

/**
 * v1.50.0: 시간대별 쿼드런트 분포 계산
 *
 * @param quadrant 분석할 쿼드런트
 * @return TimeDistribution - 시간대별 완료 개수
 */
fun List<ReminderEntity>.calculateTimeDistribution(quadrant: Quadrant): TimeDistribution {
    val completedReminders = this
        .filterByQuadrant(quadrant)
        .filter { it.isCompleted }

    val distribution = completedReminders.groupBy { reminder ->
        when (reminder.updatedAt.hour) {
            in 0..5 -> "night"
            in 6..11 -> "morning"
            in 12..17 -> "afternoon"
            else -> "evening"
        }
    }

    return TimeDistribution(
        morning = distribution["morning"]?.size ?: 0,
        afternoon = distribution["afternoon"]?.size ?: 0,
        evening = distribution["evening"]?.size ?: 0,
        night = distribution["night"]?.size ?: 0
    )
}
