package com.reminder.data.dao

import androidx.room.*
import com.reminder.data.entity.ConflictLogEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

/**
 * 충돌 로그 DAO
 */
@Dao
interface ConflictLogDao {

    /**
     * 모든 충돌 로그 조회 (최신순)
     */
    @Query("SELECT * FROM conflict_logs ORDER BY conflictedAt DESC")
    fun getAllConflictLogs(): Flow<List<ConflictLogEntity>>

    /**
     * 미해결 충돌 로그 조회
     */
    @Query("SELECT * FROM conflict_logs WHERE isResolved = 0 ORDER BY conflictedAt DESC")
    fun getUnresolvedConflicts(): Flow<List<ConflictLogEntity>>

    /**
     * 특정 리마인더의 충돌 로그 조회
     */
    @Query("SELECT * FROM conflict_logs WHERE reminderId = :reminderId ORDER BY conflictedAt DESC")
    suspend fun getConflictLogsByReminderId(reminderId: Long): List<ConflictLogEntity>

    /**
     * 충돌 로그 추가
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConflictLog(log: ConflictLogEntity): Long

    /**
     * 충돌 로그 업데이트 (해결 시)
     */
    @Update
    suspend fun updateConflictLog(log: ConflictLogEntity)

    /**
     * 충돌 로그 삭제
     */
    @Delete
    suspend fun deleteConflictLog(log: ConflictLogEntity)

    /**
     * 오래된 충돌 로그 삭제 (30일 이상)
     */
    @Query("DELETE FROM conflict_logs WHERE conflictedAt < :cutoffDate")
    suspend fun deleteOldConflictLogs(cutoffDate: LocalDateTime)

    /**
     * 모든 충돌 로그 삭제
     */
    @Query("DELETE FROM conflict_logs")
    suspend fun deleteAllConflictLogs()

    /**
     * 미해결 충돌 개수
     */
    @Query("SELECT COUNT(*) FROM conflict_logs WHERE isResolved = 0")
    fun getUnresolvedConflictCount(): Flow<Int>
}
