package com.reminder.data.repository

import com.reminder.data.dao.ReminderDao
import com.reminder.data.entity.ReminderEntity
import com.reminder.data.remote.RemoteDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * Firebase 동기화 기능을 포함한 Repository
 * 로컬 Room Database와 원격 Firestore를 동기화
 */
class FirebaseSyncRepository(
    private val reminderDao: ReminderDao,
    private val remoteDataSource: RemoteDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val syncScope = CoroutineScope(ioDispatcher)

    // 로컬 데이터 Flow
    val allReminders: Flow<List<ReminderEntity>> = reminderDao.getAllReminders()
    val activeReminders: Flow<List<ReminderEntity>> = reminderDao.getActiveReminders()
    val completedReminders: Flow<List<ReminderEntity>> = reminderDao.getCompletedReminders()

    /**
     * 원격 데이터를 Flow로 가져오기
     */
    fun syncFromRemote(): Flow<List<ReminderEntity>> {
        return remoteDataSource.getAllReminders()
    }

    /**
     * 로컬 데이터를 원격에 동기화
     */
    suspend fun syncToRemote(): Result<Unit> {
        return try {
            val localReminders = reminderDao.getAllRemindersList()
            remoteDataSource.uploadAll(localReminders)
        } catch (e: Exception) {
            // 로깅은 프로덕션에서 처리
            Result.failure(e)
        }
    }

    // 읽기 작업
    suspend fun getReminderById(id: Long): ReminderEntity? {
        return reminderDao.getReminderById(id)
    }

    fun getRemindersByCategory(category: String): Flow<List<ReminderEntity>> {
        return reminderDao.getRemindersByCategory(category)
    }

    // 쓰기 작업 - 로컬 우선, 백그라운드에서 원격 동기화
    suspend fun insertReminder(reminder: ReminderEntity): Long {
        val id = reminderDao.insertReminder(reminder)

        // 백그라운드에서 원격 동기화
        syncScope.launch {
            remoteDataSource.upsertReminder(reminder.copy(id = id))
        }

        return id
    }

    suspend fun updateReminder(reminder: ReminderEntity) {
        reminderDao.updateReminder(reminder)

        // 백그라운드에서 원격 동기화
        syncScope.launch {
            remoteDataSource.upsertReminder(reminder)
        }
    }

    suspend fun deleteReminder(reminder: ReminderEntity) {
        reminderDao.deleteReminder(reminder)

        // 백그라운드에서 원격 동기화
        syncScope.launch {
            remoteDataSource.deleteReminder(reminder.id)
        }
    }

    suspend fun deleteReminderById(id: Long) {
        reminderDao.deleteReminderById(id)

        // 백그라운드에서 원격 동기화
        syncScope.launch {
            remoteDataSource.deleteReminder(id)
        }
    }

    suspend fun deleteAllCompletedReminders() {
        reminderDao.deleteAllCompletedReminders()

        // TODO: 완료된 항목들을 원격에서도 삭제하는 로직 필요
    }

    suspend fun toggleReminderCompletion(reminder: ReminderEntity) {
        val updatedReminder = reminder.copy(
            isCompleted = !reminder.isCompleted,
            updatedAt = java.time.LocalDateTime.now()
        )
        reminderDao.updateReminder(updatedReminder)

        // 백그라운드에서 원격 동기화
        syncScope.launch {
            remoteDataSource.upsertReminder(updatedReminder)
        }
    }
}
