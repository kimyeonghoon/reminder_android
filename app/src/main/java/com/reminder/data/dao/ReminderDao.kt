package com.reminder.data.dao

import androidx.room.*
import com.reminder.data.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders ORDER BY createdAt DESC")
    fun getAllReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE isCompleted = 0 ORDER BY dueDateTime ASC, priority DESC")
    fun getActiveReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE isCompleted = 1 ORDER BY updatedAt DESC")
    fun getCompletedReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getReminderById(id: Long): ReminderEntity?

    @Query("SELECT * FROM reminders WHERE category = :category ORDER BY createdAt DESC")
    fun getRemindersByCategory(category: String): Flow<List<ReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity): Long

    @Update
    suspend fun updateReminder(reminder: ReminderEntity)

    @Delete
    suspend fun deleteReminder(reminder: ReminderEntity)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteReminderById(id: Long)

    @Query("DELETE FROM reminders WHERE isCompleted = 1")
    suspend fun deleteAllCompletedReminders()

    // Sync methods
    @Query("SELECT * FROM reminders")
    suspend fun getAllRemindersList(): List<ReminderEntity>

    @Query("SELECT * FROM reminders WHERE isCompleted = 1")
    suspend fun getCompletedRemindersList(): List<ReminderEntity>

    @Query("SELECT * FROM reminders WHERE updatedAt > :timestamp")
    suspend fun getRemindersModifiedAfter(timestamp: java.time.LocalDateTime): List<ReminderEntity>

    // Completion history methods
    @Query("""
        SELECT * FROM reminders
        WHERE isCompleted = 1
        AND date(updatedAt) = date(:date)
        ORDER BY updatedAt DESC
    """)
    suspend fun getCompletedRemindersByDate(date: java.time.LocalDateTime): List<ReminderEntity>

    @Query("""
        SELECT * FROM reminders
        WHERE isCompleted = 1
        AND date(updatedAt) >= date(:startDate)
        AND date(updatedAt) <= date(:endDate)
        ORDER BY updatedAt DESC
    """)
    suspend fun getCompletedRemindersInRange(
        startDate: java.time.LocalDateTime,
        endDate: java.time.LocalDateTime
    ): List<ReminderEntity>

    // Snooze methods
    @Query("UPDATE reminders SET snoozeUntil = :snoozeUntil, updatedAt = :updatedAt WHERE id = :id")
    suspend fun snoozeReminder(id: Long, snoozeUntil: java.time.LocalDateTime, updatedAt: java.time.LocalDateTime)

    @Query("UPDATE reminders SET snoozeUntil = NULL, updatedAt = :updatedAt WHERE id = :id")
    suspend fun cancelSnooze(id: Long, updatedAt: java.time.LocalDateTime)

    @Query("SELECT * FROM reminders WHERE snoozeUntil IS NOT NULL AND snoozeUntil <= :currentTime AND isCompleted = 0")
    suspend fun getSnoozedRemindersDue(currentTime: java.time.LocalDateTime): List<ReminderEntity>

    @Query("SELECT * FROM reminders WHERE snoozeUntil IS NOT NULL AND isCompleted = 0 ORDER BY snoozeUntil ASC")
    fun getSnoozedReminders(): Flow<List<ReminderEntity>>
}
