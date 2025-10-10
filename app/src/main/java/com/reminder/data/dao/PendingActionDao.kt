package com.reminder.data.dao

import androidx.room.*
import com.reminder.data.entity.PendingActionEntity
import kotlinx.coroutines.flow.Flow

/**
 * 오프라인 작업 큐 DAO
 */
@Dao
interface PendingActionDao {

    /**
     * 모든 대기 중인 작업 조회 (Flow)
     */
    @Query("SELECT * FROM pending_actions ORDER BY createdAt ASC")
    fun getAllPendingActions(): Flow<List<PendingActionEntity>>

    /**
     * 특정 리마인더의 대기 중인 작업 조회
     */
    @Query("SELECT * FROM pending_actions WHERE reminderId = :reminderId")
    suspend fun getPendingActionsByReminderId(reminderId: Long): List<PendingActionEntity>

    /**
     * 재시도 횟수가 특정 값 이하인 작업 조회
     */
    @Query("SELECT * FROM pending_actions WHERE retryCount <= :maxRetryCount ORDER BY createdAt ASC")
    suspend fun getPendingActionsForRetry(maxRetryCount: Int = 3): List<PendingActionEntity>

    /**
     * 작업 추가
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPendingAction(action: PendingActionEntity): Long

    /**
     * 작업 업데이트 (재시도 횟수 증가 등)
     */
    @Update
    suspend fun updatePendingAction(action: PendingActionEntity)

    /**
     * 작업 삭제 (동기화 완료 시)
     */
    @Delete
    suspend fun deletePendingAction(action: PendingActionEntity)

    /**
     * 모든 작업 삭제
     */
    @Query("DELETE FROM pending_actions")
    suspend fun deleteAllPendingActions()

    /**
     * 특정 리마인더의 모든 대기 작업 삭제
     */
    @Query("DELETE FROM pending_actions WHERE reminderId = :reminderId")
    suspend fun deletePendingActionsByReminderId(reminderId: Long)

    /**
     * 대기 중인 작업 개수
     */
    @Query("SELECT COUNT(*) FROM pending_actions")
    fun getPendingActionsCount(): Flow<Int>
}
