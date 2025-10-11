package com.reminder.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.reminder.recurrence.RecurrenceEnd
import com.reminder.recurrence.RecurrenceRule
import java.time.LocalDateTime

@Entity(
    tableName = "reminders",
    indices = [
        Index(value = ["isCompleted"]),
        Index(value = ["isArchived"]),  // v1.43.0: 아카이브 인덱스
        Index(value = ["dueDateTime"]),
        Index(value = ["priority"]),
        Index(value = ["urgency"]),     // v1.47.0: 긴급도 인덱스
        Index(value = ["category"]),
        Index(value = ["updatedAt"]),
        Index(value = ["isCompleted", "dueDateTime"]),  // 복합 인덱스 (가장 자주 사용되는 쿼리)
        Index(value = ["isCompleted", "isArchived"]),    // v1.43.0: 완료+아카이브 복합 인덱스
        Index(value = ["priority", "urgency"])           // v1.47.0: Eisenhower Matrix용 복합 인덱스
    ]
)
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val dueDateTime: LocalDateTime? = null,
    val priority: Priority = Priority.MEDIUM,
    val urgency: Urgency = Urgency.MEDIUM,  // v1.47.0: 긴급도 (Eisenhower Matrix용)
    val category: String = "",
    val tags: String = "", // 콤마로 구분된 태그 (예: "work,urgent,meeting")
    val isCompleted: Boolean = false,
    val completedAt: LocalDateTime? = null,  // 완료 시간 (통계 및 분석용)
    val isArchived: Boolean = false,     // v1.43.0: 아카이브 여부 (완료 후 일정 기간 지나면 자동 아카이브)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),

    // 이미지 첨부
    val imageUri: String? = null,            // 첨부 이미지 URI (v1.39.0 확장)

    // 스누즈 기능
    val snoozeUntil: LocalDateTime? = null,  // 스누즈된 시간 (null이면 스누즈되지 않음)

    // 위치 기반 리마인더
    val locationLatitude: Double? = null,    // 위도
    val locationLongitude: Double? = null,   // 경도
    val locationName: String? = null,        // 위치 이름 (예: "집", "회사")
    val locationRadius: Float? = null,       // 반경 (미터, 기본 100m)

    // 웹 링크 첨부
    val webLink: String? = null,             // 관련 웹 링크 (URL)

    // 음성 알림 (TTS)
    val readAloud: Boolean = false,          // 알림 시 음성으로 읽기

    // v1.35.0: 반복 작업 고급 옵션
    val recurrenceRule: RecurrenceRule? = null,  // 반복 규칙 (DAILY, WEEKLY, MONTHLY, YEARLY, CUSTOM)
    val recurrenceEnd: RecurrenceEnd? = null,    // 반복 종료 조건 (Never, AfterOccurrences, OnDate)

    // 반복 리마인더 (레거시 - 하위 호환성 유지)
    @Deprecated("Use recurrenceRule instead")
    val recurrencePattern: RecurrencePattern = RecurrencePattern.NONE,
    @Deprecated("Use recurrenceRule instead")
    val recurrenceInterval: Int = 1,
    @Deprecated("Use recurrenceRule instead")
    val recurrenceDaysOfWeek: String? = null,  // "MONDAY,WEDNESDAY,FRIDAY"
    @Deprecated("Use recurrenceEnd instead")
    val recurrenceEndDate: LocalDateTime? = null
)

enum class Priority {
    LOW,
    MEDIUM,
    HIGH
}

/**
 * v1.47.0: 긴급도 (Urgency) - Eisenhower Matrix용
 */
enum class Urgency {
    LOW,      // 긴급하지 않음
    MEDIUM,   // 보통 긴급
    HIGH      // 매우 긴급
}
