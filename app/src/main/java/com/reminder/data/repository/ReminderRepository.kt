package com.reminder.data.repository

import com.reminder.data.dao.ReminderDao
import com.reminder.data.entity.ReminderEntity
import com.reminder.data.remote.RemoteDataSource
import kotlinx.coroutines.flow.Flow

/**
 * Firebase 동기화 기능이 통합된 Repository
 * FirebaseSyncRepository를 내부적으로 사용하여 Firebase 동기화를 지원하면서
 * 기존 인터페이스를 유지하여 하위 호환성 보장
 */
class ReminderRepository(
    private val reminderDao: ReminderDao,
    private val remoteDataSource: RemoteDataSource? = null
) {
    // Firebase 동기화가 활성화된 경우 FirebaseSyncRepository 사용
    private val syncRepository: FirebaseSyncRepository? = remoteDataSource?.let {
        FirebaseSyncRepository(reminderDao, it)
    }

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
        return syncRepository?.insertReminder(reminder) ?: reminderDao.insertReminder(reminder)
    }

    suspend fun updateReminder(reminder: ReminderEntity) {
        syncRepository?.updateReminder(reminder) ?: reminderDao.updateReminder(reminder)
    }

    suspend fun deleteReminder(reminder: ReminderEntity) {
        syncRepository?.deleteReminder(reminder) ?: reminderDao.deleteReminder(reminder)
    }

    suspend fun deleteReminderById(id: Long) {
        syncRepository?.deleteReminderById(id) ?: reminderDao.deleteReminderById(id)
    }

    suspend fun deleteAllCompletedReminders() {
        syncRepository?.deleteAllCompletedReminders() ?: reminderDao.deleteAllCompletedReminders()
    }

    suspend fun toggleReminderCompletion(reminder: ReminderEntity) {
        if (syncRepository != null) {
            syncRepository.toggleReminderCompletion(reminder)
        } else {
            val updatedReminder = reminder.copy(
                isCompleted = !reminder.isCompleted,
                updatedAt = java.time.LocalDateTime.now()
            )
            reminderDao.updateReminder(updatedReminder)
        }
    }

    // Firebase 동기화 메서드 (선택적)
    suspend fun syncToRemote(): Result<Unit> {
        return syncRepository?.syncToRemote() ?: Result.failure(Exception("Firebase sync not enabled"))
    }

    fun syncFromRemote(): Flow<List<ReminderEntity>>? {
        return syncRepository?.syncFromRemote()
    }

    // 완료 이력 메서드
    suspend fun getCompletedRemindersByDate(date: java.time.LocalDateTime): List<ReminderEntity> {
        return reminderDao.getCompletedRemindersByDate(date)
    }

    suspend fun getCompletedRemindersInRange(
        startDate: java.time.LocalDateTime,
        endDate: java.time.LocalDateTime
    ): List<ReminderEntity> {
        return reminderDao.getCompletedRemindersInRange(startDate, endDate)
    }
}
