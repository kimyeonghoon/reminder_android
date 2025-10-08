package com.reminder.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val dueDateTime: LocalDateTime? = null,
    val priority: Priority = Priority.MEDIUM,
    val category: String = "",
    val isCompleted: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),

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
