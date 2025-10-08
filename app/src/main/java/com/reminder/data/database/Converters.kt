package com.reminder.data.database

import androidx.room.TypeConverter
import com.reminder.data.entity.Priority
import com.reminder.data.entity.RecurrencePattern
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Converters {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): String? {
        return value?.format(formatter)
    }

    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it, formatter) }
    }

    @TypeConverter
    fun fromPriority(value: Priority): Int {
        return when (value) {
            Priority.LOW -> 1
            Priority.MEDIUM -> 2
            Priority.HIGH -> 3
        }
    }

    @TypeConverter
    fun toPriority(value: Int): Priority {
        return when (value) {
            1 -> Priority.LOW
            2 -> Priority.MEDIUM
            3 -> Priority.HIGH
            else -> Priority.MEDIUM
        }
    }

    @TypeConverter
    fun fromRecurrencePattern(value: RecurrencePattern): String {
        return value.name
    }

    @TypeConverter
    fun toRecurrencePattern(value: String): RecurrencePattern {
        return try {
            RecurrencePattern.valueOf(value)
        } catch (e: Exception) {
            RecurrencePattern.NONE
        }
    }
}
