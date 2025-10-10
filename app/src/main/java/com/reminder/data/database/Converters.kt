package com.reminder.data.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.reminder.data.entity.ActionType
import com.reminder.data.entity.ChosenDataSource
import com.reminder.data.entity.FileType
import com.reminder.data.entity.MLDataType
import com.reminder.data.entity.Priority
import com.reminder.data.entity.RecurrencePattern
import com.reminder.data.entity.ResolutionStrategy
import com.reminder.data.entity.SyncDirection
import com.reminder.recurrence.RecurrenceEnd
import com.reminder.recurrence.RecurrenceRule
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Converters {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val gson = Gson()

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

    // v1.35.0: LocalDate converters
    @TypeConverter
    fun fromLocalDate(value: LocalDate?): String? {
        return value?.format(dateFormatter)
    }

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it, dateFormatter) }
    }

    // v1.35.0: RecurrenceRule converters
    @TypeConverter
    fun fromRecurrenceRule(value: RecurrenceRule?): String? {
        return value?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toRecurrenceRule(value: String?): RecurrenceRule? {
        return value?.let {
            try {
                gson.fromJson(it, RecurrenceRule::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }

    // v1.35.0: RecurrenceEnd converters (sealed class)
    @TypeConverter
    fun fromRecurrenceEnd(value: RecurrenceEnd?): String? {
        return value?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toRecurrenceEnd(value: String?): RecurrenceEnd? {
        return value?.let {
            try {
                // Gson needs RuntimeTypeAdapterFactory for sealed classes
                // For simplicity, we'll handle the three types manually
                when {
                    it.contains("\"count\"") -> gson.fromJson(it, RecurrenceEnd.AfterOccurrences::class.java)
                    it.contains("\"date\"") -> gson.fromJson(it, RecurrenceEnd.OnDate::class.java)
                    else -> RecurrenceEnd.Never
                }
            } catch (e: Exception) {
                RecurrenceEnd.Never
            }
        }
    }

    // v1.37.0: MLDataType converters
    @TypeConverter
    fun fromMLDataType(value: MLDataType): String {
        return value.name
    }

    @TypeConverter
    fun toMLDataType(value: String): MLDataType {
        return try {
            MLDataType.valueOf(value)
        } catch (e: Exception) {
            MLDataType.PRIORITY
        }
    }

    // v1.38.0: ActionType converters
    @TypeConverter
    fun fromActionType(value: ActionType): String {
        return value.name
    }

    @TypeConverter
    fun toActionType(value: String): ActionType {
        return try {
            ActionType.valueOf(value)
        } catch (e: Exception) {
            ActionType.UPDATE
        }
    }

    // v1.38.0: ResolutionStrategy converters
    @TypeConverter
    fun fromResolutionStrategy(value: ResolutionStrategy): String {
        return value.name
    }

    @TypeConverter
    fun toResolutionStrategy(value: String): ResolutionStrategy {
        return try {
            ResolutionStrategy.valueOf(value)
        } catch (e: Exception) {
            ResolutionStrategy.LAST_WRITE_WINS
        }
    }

    // v1.38.0: ChosenDataSource converters
    @TypeConverter
    fun fromChosenDataSource(value: ChosenDataSource): String {
        return value.name
    }

    @TypeConverter
    fun toChosenDataSource(value: String): ChosenDataSource {
        return try {
            ChosenDataSource.valueOf(value)
        } catch (e: Exception) {
            ChosenDataSource.REMOTE
        }
    }

    // v1.39.0: FileType converters
    @TypeConverter
    fun fromFileType(value: FileType): String {
        return value.name
    }

    @TypeConverter
    fun toFileType(value: String): FileType {
        return try {
            FileType.valueOf(value)
        } catch (e: Exception) {
            FileType.OTHER
        }
    }

    // v1.40.0: SyncDirection converters
    @TypeConverter
    fun fromSyncDirection(value: SyncDirection): String {
        return value.name
    }

    @TypeConverter
    fun toSyncDirection(value: String): SyncDirection {
        return try {
            SyncDirection.valueOf(value)
        } catch (e: Exception) {
            SyncDirection.TWO_WAY
        }
    }
}
