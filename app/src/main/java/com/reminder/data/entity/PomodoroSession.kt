package com.reminder.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * v1.45.0: 포모도로 세션 엔티티
 *
 * 포모도로 타이머 세션을 기록합니다.
 * - FOCUS: 25분 집중 세션
 * - SHORT_BREAK: 5분 짧은 휴식
 * - LONG_BREAK: 15분 긴 휴식
 */
@Entity(
    tableName = "pomodoro_sessions",
    foreignKeys = [
        ForeignKey(
            entity = ReminderEntity::class,
            parentColumns = ["id"],
            childColumns = ["reminderId"],
            onDelete = ForeignKey.SET_NULL // 리마인더 삭제 시 연결 해제
        )
    ],
    indices = [
        Index(value = ["reminderId"]),
        Index(value = ["startedAt"]),
        Index(value = ["isCompleted"])
    ]
)
data class PomodoroSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /**
     * 연결된 리마인더 ID (선택사항)
     * null이면 독립 세션
     */
    val reminderId: Long? = null,

    /**
     * 세션 타입 (FOCUS, SHORT_BREAK, LONG_BREAK)
     */
    val sessionType: SessionType,

    /**
     * 세션 길이 (분 단위)
     * FOCUS: 25분
     * SHORT_BREAK: 5분
     * LONG_BREAK: 15분
     */
    val duration: Int,

    /**
     * 세션 시작 시간
     */
    val startedAt: LocalDateTime,

    /**
     * 세션 완료 시간 (완료되지 않으면 null)
     */
    val completedAt: LocalDateTime? = null,

    /**
     * 세션 완료 여부
     */
    val isCompleted: Boolean = false,

    /**
     * 생성 시간
     */
    val createdAt: LocalDateTime = LocalDateTime.now()
)

/**
 * 포모도로 세션 타입
 */
enum class SessionType {
    FOCUS,        // 집중 세션 (25분)
    SHORT_BREAK,  // 짧은 휴식 (5분)
    LONG_BREAK    // 긴 휴식 (15분)
}
