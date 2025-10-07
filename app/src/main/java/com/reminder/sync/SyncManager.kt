package com.reminder.sync

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.reminder.data.dao.ReminderDao
import com.reminder.data.entity.ReminderEntity
import com.reminder.data.remote.RemoteDataSource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sync_prefs")

class SyncManager(
    private val context: Context,
    private val localDao: ReminderDao,
    private val remoteDataSource: RemoteDataSource
) {
    private val TAG = "SyncManager"

    companion object {
        private val LAST_SYNC_TIME = longPreferencesKey("last_sync_time")
    }

    /**
     * 양방향 동기화 수행
     * 1. 로컬 → 원격: 로컬에서 변경된 항목 업로드
     * 2. 원격 → 로컬: 원격에서 변경된 항목 다운로드
     */
    suspend fun sync(): Result<Unit> {
        return try {
            Log.d(TAG, "동기화 시작")

            // 마지막 동기화 시간 가져오기
            val lastSyncTimeMillis = getLastSyncTime()
            val lastSyncTime = timestampToLocalDateTime(lastSyncTimeMillis)
            Log.d(TAG, "마지막 동기화: $lastSyncTime")

            // 1단계: 로컬 → 원격 (로컬에서 수정된 항목 업로드)
            val localModified = localDao.getRemindersModifiedAfter(lastSyncTime)
            Log.d(TAG, "로컬 수정 항목: ${localModified.size}개")

            localModified.forEach { reminder ->
                remoteDataSource.upsertReminder(reminder)
                    .onFailure { e ->
                        Log.e(TAG, "업로드 실패: ${reminder.id}", e)
                    }
            }

            // 2단계: 원격 → 로컬 (원격에서 수정된 항목 다운로드)
            val remoteModified = remoteDataSource.getRemindersModifiedAfter(lastSyncTimeMillis)
            Log.d(TAG, "원격 수정 항목: ${remoteModified.size}개")

            remoteModified.forEach { remoteReminder ->
                // 충돌 해결: 마지막 수정 시간 비교
                val localReminder = localDao.getReminderById(remoteReminder.id)
                if (localReminder == null) {
                    // 로컬에 없으면 추가
                    localDao.insertReminder(remoteReminder)
                    Log.d(TAG, "로컬에 추가: ${remoteReminder.id}")
                } else {
                    // 충돌 해결: 최신 것 유지
                    if (remoteReminder.updatedAt.isAfter(localReminder.updatedAt)) {
                        localDao.updateReminder(remoteReminder)
                        Log.d(TAG, "로컬 업데이트: ${remoteReminder.id}")
                    } else {
                        Log.d(TAG, "로컬이 최신: ${remoteReminder.id}")
                    }
                }
            }

            // 동기화 시간 업데이트
            updateLastSyncTime(System.currentTimeMillis())
            Log.d(TAG, "동기화 완료")

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "동기화 실패", e)
            Result.failure(e)
        }
    }

    /**
     * 초기 동기화: 모든 로컬 데이터를 원격에 업로드
     */
    suspend fun initialSync(): Result<Unit> {
        return try {
            Log.d(TAG, "초기 동기화 시작")

            // 모든 로컬 데이터 가져오기
            val allLocal = localDao.getAllRemindersList()
            Log.d(TAG, "로컬 항목: ${allLocal.size}개")

            // 원격에 업로드
            remoteDataSource.uploadAll(allLocal)
                .onSuccess {
                    updateLastSyncTime(System.currentTimeMillis())
                    Log.d(TAG, "초기 동기화 완료")
                }
                .onFailure { e ->
                    Log.e(TAG, "초기 동기화 실패", e)
                }
        } catch (e: Exception) {
            Log.e(TAG, "초기 동기화 오류", e)
            Result.failure(e)
        }
    }

    private suspend fun getLastSyncTime(): Long {
        return context.dataStore.data
            .map { preferences ->
                preferences[LAST_SYNC_TIME] ?: 0L
            }
            .first()
    }

    private suspend fun updateLastSyncTime(time: Long) {
        context.dataStore.edit { preferences ->
            preferences[LAST_SYNC_TIME] = time
        }
    }

    private fun timestampToLocalDateTime(timestamp: Long): LocalDateTime {
        return if (timestamp == 0L) {
            LocalDateTime.MIN
        } else {
            LocalDateTime.ofInstant(
                Instant.ofEpochMilli(timestamp),
                ZoneId.systemDefault()
            )
        }
    }
}
