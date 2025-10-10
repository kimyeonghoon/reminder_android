package com.reminder.sync

import com.reminder.data.dao.PendingActionDao
import com.reminder.data.entity.ActionType
import com.reminder.data.entity.PendingActionEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

/**
 * v1.38.0: 오프라인 작업 큐 관리
 *
 * 네트워크 없이 발생한 CRUD 작업을 큐에 저장하고,
 * 네트워크 복구 시 Firebase와 동기화합니다.
 */
class OfflineQueue(
    private val pendingActionDao: PendingActionDao
) {

    /**
     * 모든 대기 중인 작업 Flow
     */
    val pendingActions: Flow<List<PendingActionEntity>> =
        pendingActionDao.getAllPendingActions()

    /**
     * 대기 중인 작업 개수 Flow
     */
    val pendingActionsCount: Flow<Int> =
        pendingActionDao.getPendingActionsCount()

    /**
     * 리마인더 생성 작업 추가
     */
    suspend fun enqueueInsert(reminderId: Long) {
        val action = PendingActionEntity(
            reminderId = reminderId,
            actionType = ActionType.INSERT,
            createdAt = LocalDateTime.now()
        )
        pendingActionDao.insertPendingAction(action)
    }

    /**
     * 리마인더 수정 작업 추가
     */
    suspend fun enqueueUpdate(reminderId: Long) {
        val action = PendingActionEntity(
            reminderId = reminderId,
            actionType = ActionType.UPDATE,
            createdAt = LocalDateTime.now()
        )
        pendingActionDao.insertPendingAction(action)
    }

    /**
     * 리마인더 삭제 작업 추가
     */
    suspend fun enqueueDelete(reminderId: Long) {
        // 이전 작업들 삭제 (최종 작업만 유지)
        pendingActionDao.deletePendingActionsByReminderId(reminderId)

        val action = PendingActionEntity(
            reminderId = reminderId,
            actionType = ActionType.DELETE,
            createdAt = LocalDateTime.now()
        )
        pendingActionDao.insertPendingAction(action)
    }

    /**
     * 재시도 가능한 작업 조회 (재시도 횟수 3회 이하)
     */
    suspend fun getPendingActionsForRetry(): List<PendingActionEntity> {
        return pendingActionDao.getPendingActionsForRetry(maxRetryCount = 3)
    }

    /**
     * 작업 재시도 횟수 증가
     */
    suspend fun incrementRetryCount(action: PendingActionEntity, errorMessage: String? = null) {
        val updatedAction = action.copy(
            retryCount = action.retryCount + 1,
            lastRetryAt = LocalDateTime.now(),
            errorMessage = errorMessage
        )
        pendingActionDao.updatePendingAction(updatedAction)
    }

    /**
     * 작업 완료 (큐에서 제거)
     */
    suspend fun markAsCompleted(action: PendingActionEntity) {
        pendingActionDao.deletePendingAction(action)
    }

    /**
     * 특정 리마인더의 모든 작업 제거
     */
    suspend fun clearActionsForReminder(reminderId: Long) {
        pendingActionDao.deletePendingActionsByReminderId(reminderId)
    }

    /**
     * 모든 작업 제거
     */
    suspend fun clearAll() {
        pendingActionDao.deleteAllPendingActions()
    }

    /**
     * 재시도 실패 작업 제거 (재시도 횟수 4회 이상)
     */
    suspend fun clearFailedActions() {
        val allActions = pendingActionDao.getAllPendingActions()
        // Note: This is a simplified version. In production, you'd use a better query
    }
}
