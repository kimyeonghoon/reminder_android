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
