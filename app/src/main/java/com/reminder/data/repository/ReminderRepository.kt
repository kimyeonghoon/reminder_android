package com.reminder.data.repository

import com.reminder.data.dao.ReminderDao
import com.reminder.data.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

class ReminderRepository(private val reminderDao: ReminderDao) {

    val allReminders: Flow<List<ReminderEntity>> = reminderDao.getAllReminders()
    val activeReminders: Flow<List<ReminderEntity>> = reminderDao.getActiveReminders()
    val completedReminders: Flow<List<ReminderEntity>> = reminderDao.getCompletedReminders()

    suspend fun getReminderById(id: Long): ReminderEntity? {
        return reminderDao.getReminderById(id)
    }

    fun getRemindersByCategory(category: String): Flow<List<ReminderEntity>> {
        return reminderDao.getRemindersByCategory(category)
    }

    suspend fun insertReminder(reminder: ReminderEntity): Long {
        return reminderDao.insertReminder(reminder)
    }

    suspend fun updateReminder(reminder: ReminderEntity) {
        reminderDao.updateReminder(reminder)
    }

    suspend fun deleteReminder(reminder: ReminderEntity) {
        reminderDao.deleteReminder(reminder)
    }

    suspend fun deleteReminderById(id: Long) {
        reminderDao.deleteReminderById(id)
    }

    suspend fun deleteAllCompletedReminders() {
        reminderDao.deleteAllCompletedReminders()
    }

    suspend fun toggleReminderCompletion(reminder: ReminderEntity) {
        val updatedReminder = reminder.copy(
            isCompleted = !reminder.isCompleted,
            updatedAt = java.time.LocalDateTime.now()
        )
        reminderDao.updateReminder(updatedReminder)
    }
}
