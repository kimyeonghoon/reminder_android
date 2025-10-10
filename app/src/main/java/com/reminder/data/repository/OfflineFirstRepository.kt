package com.reminder.data.repository

import com.reminder.data.dao.ReminderDao
import com.reminder.data.entity.ReminderEntity
import com.reminder.network.NetworkMonitor
import com.reminder.sync.OfflineQueue
import kotlinx.coroutines.flow.first

/**
 * v1.38.0: 오프라인 우선 Repository
 *
 * 로컬 DB에 먼저 쓰고, 백그라운드에서 Firebase와 동기화합니다.
 * 낙관적 업데이트(Optimistic UI)를 지원합니다.
 */
class OfflineFirstRepository(
    private val reminderDao: ReminderDao,
    private val offlineQueue: OfflineQueue,
    private val networkMonitor: NetworkMonitor
) {

    /**
     * 리마인더 추가 (오프라인 우선)
     *
     * 1. 로컬 DB에 즉시 저장
     * 2. 오프라인 큐에 작업 추가
     * 3. 네트워크 연결 시 Firebase와 동기화
     */
    suspend fun insertReminderOfflineFirst(reminder: ReminderEntity): Long {
        // 1. 로컬 DB에 즉시 저장 (낙관적 업데이트)
        val localId = reminderDao.insertReminder(reminder)

        // 2. 오프라인 큐에 작업 추가
        offlineQueue.enqueueInsert(localId)

        return localId
    }

    /**
     * 리마인더 수정 (오프라인 우선)
     */
    suspend fun updateReminderOfflineFirst(reminder: ReminderEntity) {
        // 1. 로컬 DB에 즉시 수정
        reminderDao.updateReminder(reminder)

        // 2. 오프라인 큐에 작업 추가
        offlineQueue.enqueueUpdate(reminder.id)
    }

    /**
     * 리마인더 삭제 (오프라인 우선)
     */
    suspend fun deleteReminderOfflineFirst(reminder: ReminderEntity) {
        // 1. 로컬 DB에서 즉시 삭제
        reminderDao.deleteReminder(reminder)

        // 2. 오프라인 큐에 작업 추가
        offlineQueue.enqueueDelete(reminder.id)
    }

    /**
     * 리마인더 완료/미완료 토글 (오프라인 우선)
     */
    suspend fun toggleReminderCompletionOfflineFirst(reminderId: Long) {
        // 1. 로컬 DB에서 즉시 토글
        reminderDao.toggleReminderCompletion(reminderId)

        // 2. 오프라인 큐에 작업 추가
        offlineQueue.enqueueUpdate(reminderId)
    }

    /**
     * 대기 중인 작업 개수
     */
    suspend fun getPendingActionsCount(): Int {
        return offlineQueue.pendingActionsCount.first()
    }

    /**
     * 네트워크 연결 상태 확인
     */
    fun isNetworkAvailable(): Boolean {
        return networkMonitor.isCurrentlyConnected()
    }
}
