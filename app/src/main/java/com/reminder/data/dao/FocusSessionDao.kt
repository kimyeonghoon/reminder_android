package com.reminder.data.dao

import androidx.room.*
import com.reminder.data.entity.FocusSessionEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

/**
 * v1.51.0: 포커스 세션 DAO
 *
 * Room Database를 통한 포커스 세션 데이터 접근
 */
@Dao
interface FocusSessionDao {

    /**
     * 세션 삽입
     */
    @Insert
    suspend fun insertSession(session: FocusSessionEntity): Long

    /**
     * 세션 업데이트
     */
    @Update
    suspend fun updateSession(session: FocusSessionEntity)

    /**
     * 세션 삭제
     */
    @Delete
    suspend fun deleteSession(session: FocusSessionEntity)

    /**
     * ID로 세션 조회
     */
    @Query("SELECT * FROM focus_sessions WHERE id = :id")
    suspend fun getSessionById(id: Long): FocusSessionEntity?

    /**
     * 모든 세션 조회 (최신순)
     */
    @Query("SELECT * FROM focus_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<FocusSessionEntity>>

    /**
     * 활성 세션 조회 (진행 중인 세션들)
     */
    @Query("SELECT * FROM focus_sessions WHERE endTime IS NULL AND isCompleted = 0 AND isInterrupted = 0 ORDER BY startTime DESC")
    fun getActiveSessions(): Flow<List<FocusSessionEntity>>

    /**
     * 완료된 세션만 조회
     */
    @Query("SELECT * FROM focus_sessions WHERE isCompleted = 1 ORDER BY startTime DESC")
    fun getCompletedSessions(): Flow<List<FocusSessionEntity>>

    /**
     * 특정 리마인더의 세션들 조회
     */
    @Query("SELECT * FROM focus_sessions WHERE reminderId = :reminderId ORDER BY startTime DESC")
    fun getSessionsByReminderId(reminderId: Long): Flow<List<FocusSessionEntity>>

    /**
     * 날짜 범위로 세션 조회
     */
    @Query("SELECT * FROM focus_sessions WHERE startTime BETWEEN :startDate AND :endDate ORDER BY startTime DESC")
    fun getSessionsBetweenDates(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<FocusSessionEntity>>

    /**
     * 총 집중 시간 계산 (완료된 세션만)
     */
    @Query("SELECT SUM(actualDurationMinutes) FROM focus_sessions WHERE isCompleted = 1")
    suspend fun getTotalFocusMinutes(): Int

    /**
     * 오래된 세션 삭제 (데이터 정리용)
     */
    @Query("DELETE FROM focus_sessions WHERE startTime < :cutoffDate")
    suspend fun deleteOldSessions(cutoffDate: LocalDateTime)

    /**
     * 모든 세션 삭제 (테스트용)
     */
    @Query("DELETE FROM focus_sessions")
    suspend fun deleteAllSessions()
}
