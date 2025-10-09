package com.reminder.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "reminders",
    indices = [
        Index(value = ["isCompleted"]),
        Index(value = ["dueDateTime"]),
        Index(value = ["priority"]),
        Index(value = ["category"]),
        Index(value = ["updatedAt"]),
        Index(value = ["isCompleted", "dueDateTime"])  // 복합 인덱스 (가장 자주 사용되는 쿼리)
    ]
)
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val dueDateTime: LocalDateTime? = null,
    val priority: Priority = Priority.MEDIUM,
    val category: String = "",
    val tags: String = "", // 콤마로 구분된 태그 (예: "work,urgent,meeting")
    val isCompleted: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),

    // 스누즈 기능
    val snoozeUntil: LocalDateTime? = null,  // 스누즈된 시간 (null이면 스누즈되지 않음)

    // 반복 리마인더
    val recurrencePattern: RecurrencePattern = RecurrencePattern.NONE,
    val recurrenceInterval: Int = 1,
    val recurrenceDaysOfWeek: String? = null,  // "MONDAY,WEDNESDAY,FRIDAY"
    val recurrenceEndDate: LocalDateTime? = null
) {
    /**
     * RecurrenceRule 생성
     */
    fun toRecurrenceRule(): RecurrenceRule {
        val daysOfWeek = recurrenceDaysOfWeek?.split(",")
            ?.mapNotNull { dayName ->
                try {
                    java.time.DayOfWeek.valueOf(dayName.trim())
                } catch (e: Exception) {
                    null
                }
            }?.toSet()

        return RecurrenceRule(
            pattern = recurrencePattern,
            interval = recurrenceInterval,
            daysOfWeek = daysOfWeek,
            endDate = recurrenceEndDate
        )
    }
}

enum class Priority {
    LOW,
    MEDIUM,
    HIGH
}
