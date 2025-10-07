package com.reminder.data.remote

import com.reminder.data.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

interface RemoteDataSource {
    /**
     * 모든 리마인더를 Flow로 관찰
     */
    fun getAllReminders(): Flow<List<ReminderEntity>>

    /**
     * 특정 리마인더 가져오기
     */
    suspend fun getReminderById(id: Long): ReminderEntity?

    /**
     * 리마인더 추가 또는 업데이트
     */
    suspend fun upsertReminder(reminder: ReminderEntity): Result<Unit>

    /**
     * 리마인더 삭제
     */
    suspend fun deleteReminder(id: Long): Result<Unit>

    /**
     * 특정 시간 이후 수정된 리마인더 가져오기 (동기화용)
     */
    suspend fun getRemindersModifiedAfter(timestamp: Long): List<ReminderEntity>

    /**
     * 모든 리마인더 일괄 업로드 (초기 동기화)
     */
    suspend fun uploadAll(reminders: List<ReminderEntity>): Result<Unit>
}
