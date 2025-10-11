package com.reminder.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * v1.51.0: 포커스 세션 엔티티
 *
 * 집중 모드 세션 정보를 저장합니다.
 */
@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // 연결된 리마인더 ID (nullable - 특정 작업에 연결되지 않을 수 있음)
    val reminderId: Long? = null,

    // 집중 타입
    val focusType: FocusType = FocusType.DEEP_WORK,

    // 세션 시작 시간
    val startTime: LocalDateTime = LocalDateTime.now(),

    // 세션 종료 시간 (진행 중이면 null)
    val endTime: LocalDateTime? = null,

    // 목표 집중 시간 (분)
    val targetDurationMinutes: Int = 25,

    // 실제 집중 시간 (분)
    val actualDurationMinutes: Int = 0,

    // 완료 여부
    val isCompleted: Boolean = false,

    // 중단 여부 (사용자가 중간에 취소)
    val isInterrupted: Boolean = false,

    // 생성 시간
    val createdAt: LocalDateTime = LocalDateTime.now()
)

/**
 * 포커스 타입
 */
enum class FocusType {
    /** Eisenhower Matrix의 DO_FIRST 작업 */
    DO_FIRST,

    /** 일반 깊은 작업 */
    DEEP_WORK,

    /** Pomodoro 타이머 */
    POMODORO,

    /** 휴식 시간 */
    BREAK
}
