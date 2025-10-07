package com.reminder.data.remote

import com.google.firebase.Timestamp
import com.reminder.data.entity.Priority
import com.reminder.data.entity.ReminderEntity
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Firestore 문서 형식의 리마인더 DTO
 * LocalDateTime을 Timestamp로 변환하여 저장
 */
data class FirestoreReminder(
    val id: Long = 0,
    val title: String = "",
    val description: String = "",
    val dueDateTime: Timestamp? = null,
    val priority: String = Priority.MEDIUM.name,
    val category: String = "",
    val isCompleted: Boolean = false,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
) {
    companion object {
        fun fromEntity(entity: ReminderEntity): FirestoreReminder {
            return FirestoreReminder(
                id = entity.id,
                title = entity.title,
                description = entity.description,
                dueDateTime = entity.dueDateTime?.toTimestamp(),
                priority = entity.priority.name,
                category = entity.category,
                isCompleted = entity.isCompleted,
                createdAt = entity.createdAt.toTimestamp(),
                updatedAt = entity.updatedAt.toTimestamp()
            )
        }

        private fun LocalDateTime.toTimestamp(): Timestamp {
            val instant = this.atZone(ZoneId.systemDefault()).toInstant()
            return Timestamp(instant.epochSecond, instant.nano)
        }

        private fun Timestamp.toLocalDateTime(): LocalDateTime {
            val instant = Instant.ofEpochSecond(this.seconds, this.nanoseconds.toLong())
            return LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        }
    }

    fun toEntity(): ReminderEntity {
        return ReminderEntity(
            id = id,
            title = title,
            description = description,
            dueDateTime = dueDateTime?.toLocalDateTime(),
            priority = Priority.valueOf(priority),
            category = category,
            isCompleted = isCompleted,
            createdAt = createdAt.toLocalDateTime(),
            updatedAt = updatedAt.toLocalDateTime()
        )
    }
}
