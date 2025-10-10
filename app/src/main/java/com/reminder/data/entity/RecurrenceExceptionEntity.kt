package com.reminder.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * v1.35.0: 반복 예외 날짜 엔티티
 *
 * 반복 일정에서 제외할 날짜를 저장
 */
@Entity(
    tableName = "recurrence_exceptions",
    foreignKeys = [
        ForeignKey(
            entity = ReminderEntity::class,
            parentColumns = ["id"],
            childColumns = ["reminderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["reminderId"]),
        Index(value = ["exceptionDate"])
    ]
)
data class RecurrenceExceptionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /**
     * 반복 리마인더 ID
     */
    val reminderId: Long,

    /**
     * 제외할 날짜
     */
    val exceptionDate: LocalDate
)
