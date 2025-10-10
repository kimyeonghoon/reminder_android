package com.reminder.data.dao

import androidx.room.*
import com.reminder.data.entity.PomodoroSession
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * v1.45.0: 포모도로 세션 DAO
 */
@Dao
interface PomodoroSessionDao {

    /**
     * 세션 삽입
     */
    @Insert
    suspend fun insertSession(session: PomodoroSession): Long

    /**
     * 세션 업데이트
     */
    @Update
    suspend fun updateSession(session: PomodoroSession)

    /**
     * 세션 ID로 조회
     */
    @Query("SELECT * FROM pomodoro_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: Long): PomodoroSession?

    /**
     * 세션 삭제
     */
    @Query("DELETE FROM pomodoro_sessions WHERE id = :sessionId")
    suspend fun deleteSessionById(sessionId: Long)

    /**
     * 리마인더의 모든 세션 조회
     */
    @Query("SELECT * FROM pomodoro_sessions WHERE reminderId = :reminderId ORDER BY startedAt DESC")
    fun getSessionsByReminder(reminderId: Long): Flow<List<PomodoroSession>>

    /**
     * 완료된 세션 개수
     */
    @Query("SELECT COUNT(*) FROM pomodoro_sessions WHERE isCompleted = 1")
    suspend fun getCompletedSessionsCount(): Int

    /**
     * 특정 날짜에 완료된 세션 개수
     */
    @Query("""
        SELECT COUNT(*) FROM pomodoro_sessions
        WHERE isCompleted = 1
        AND DATE(startedAt) = :date
    """)
    suspend fun getCompletedSessionsCountByDate(date: LocalDate): Int

    /**
     * 완료된 집중 세션 개수
     */
    @Query("SELECT COUNT(*) FROM pomodoro_sessions WHERE isCompleted = 1 AND sessionType = 'FOCUS'")
    suspend fun getCompletedFocusSessionsCount(): Int

    /**
     * 세션 완료한 날짜 목록 (중복 제거, 최신순)
     */
    @Query("""
        SELECT DISTINCT DATE(startedAt) as date
        FROM pomodoro_sessions
        WHERE isCompleted = 1
        ORDER BY date DESC
    """)
    suspend fun getDistinctCompletionDates(): List<LocalDate>

    /**
     * 모든 세션 조회 (최신순)
     */
    @Query("SELECT * FROM pomodoro_sessions ORDER BY startedAt DESC")
    fun getAllSessions(): Flow<List<PomodoroSession>>

    /**
     * 오늘의 모든 세션 조회
     */
    @Query("""
        SELECT * FROM pomodoro_sessions
        WHERE DATE(startedAt) = :today
        ORDER BY startedAt DESC
    """)
    fun getTodaySessions(today: LocalDate): Flow<List<PomodoroSession>>

    /**
     * 기간별 세션 조회
     */
    @Query("""
        SELECT * FROM pomodoro_sessions
        WHERE DATE(startedAt) BETWEEN :startDate AND :endDate
        ORDER BY startedAt DESC
    """)
    fun getSessionsByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<PomodoroSession>>

    /**
     * 모든 세션 삭제 (테스트용)
     */
    @Query("DELETE FROM pomodoro_sessions")
    suspend fun deleteAllSessions()
}
