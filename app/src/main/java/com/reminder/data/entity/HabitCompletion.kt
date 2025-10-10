package com.reminder.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import java.time.LocalDate

/**
 * v1.44.0: Habit Completion Entity
 *
 * 습관 완료 기록을 저장하는 엔티티
 * Primary Key: habitId + completedDate (복합키)
 */
@Entity(
    tableName = "habit_completions",
    primaryKeys = ["habitId", "completedDate"],
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["habitId"]),
        Index(value = ["completedDate"]),
        Index(value = ["habitId", "completedDate"])
    ]
)
data class HabitCompletion(
    // 습관 ID
    val habitId: Long,

    // 완료한 날짜
    val completedDate: LocalDate
)
