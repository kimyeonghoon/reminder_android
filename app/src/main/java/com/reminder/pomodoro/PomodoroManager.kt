package com.reminder.pomodoro

import com.reminder.data.dao.PomodoroSessionDao
import com.reminder.data.entity.PomodoroSession
import com.reminder.data.entity.SessionType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * v1.45.0: 포모도로 타이머 매니저
 *
 * 포모도로 세션 관리 비즈니스 로직
 * - 세션 시작/완료/취소
 * - 통계 조회
 * - Streak 계산
 */
class PomodoroManager(
    private val pomodoroSessionDao: PomodoroSessionDao
) {
    companion object {
        const val FOCUS_DURATION = 25      // 집중 세션: 25분
        const val SHORT_BREAK_DURATION = 5  // 짧은 휴식: 5분
        const val LONG_BREAK_DURATION = 15  // 긴 휴식: 15분
    }

    /**
     * 세션 시작
     *
     * @param reminderId 연결된 리마인더 ID (선택사항)
     * @param sessionType 세션 타입
     * @return 생성된 세션 ID
     */
    suspend fun startSession(reminderId: Long?, sessionType: SessionType): Long {
        val duration = when (sessionType) {
            SessionType.FOCUS -> FOCUS_DURATION
            SessionType.SHORT_BREAK -> SHORT_BREAK_DURATION
            SessionType.LONG_BREAK -> LONG_BREAK_DURATION
        }

        val session = PomodoroSession(
            reminderId = reminderId,
            sessionType = sessionType,
            duration = duration,
            startedAt = LocalDateTime.now(),
            completedAt = null,
            isCompleted = false,
            createdAt = LocalDateTime.now()
        )

        return pomodoroSessionDao.insertSession(session)
    }

    /**
     * 세션 완료
     *
     * @param sessionId 세션 ID
     */
    suspend fun completeSession(sessionId: Long) {
        val session = pomodoroSessionDao.getSessionById(sessionId) ?: return

        val completedSession = session.copy(
            completedAt = LocalDateTime.now(),
            isCompleted = true
        )

        pomodoroSessionDao.updateSession(completedSession)
    }

    /**
     * 세션 취소 (삭제)
     *
     * @param sessionId 세션 ID
     */
    suspend fun cancelSession(sessionId: Long) {
        pomodoroSessionDao.deleteSessionById(sessionId)
    }

    /**
     * 전체 완료된 세션 개수
     */
    suspend fun getTotalCompletedSessions(): Int {
        return pomodoroSessionDao.getCompletedSessionsCount()
    }

    /**
     * 오늘 완료된 세션 개수
     */
    suspend fun getTodayCompletedSessions(): Int {
        return pomodoroSessionDao.getCompletedSessionsCountByDate(LocalDate.now())
    }

    /**
     * 리마인더의 모든 세션 조회
     *
     * @param reminderId 리마인더 ID
     * @return Flow<List<PomodoroSession>>
     */
    fun getSessionsForReminder(reminderId: Long): Flow<List<PomodoroSession>> {
        return pomodoroSessionDao.getSessionsByReminder(reminderId)
    }

    /**
     * 집중 세션 길이 (분)
     */
    fun getFocusSessionDuration(): Int = FOCUS_DURATION

    /**
     * 짧은 휴식 길이 (분)
     */
    fun getShortBreakDuration(): Int = SHORT_BREAK_DURATION

    /**
     * 긴 휴식 길이 (분)
     */
    fun getLongBreakDuration(): Int = LONG_BREAK_DURATION

    /**
     * 전체 집중 시간 (분)
     */
    suspend fun getTotalFocusMinutes(): Int {
        val completedFocusSessions = pomodoroSessionDao.getCompletedFocusSessionsCount()
        return completedFocusSessions * FOCUS_DURATION
    }

    /**
     * 연속 완료 일수 (Streak)
     *
     * 오늘부터 역순으로 세션을 완료한 날짜를 확인하여
     * 연속된 일수를 반환합니다.
     *
     * @return 연속 완료 일수
     */
    suspend fun getStreakDays(): Int {
        val completionDates = pomodoroSessionDao.getDistinctCompletionDates()

        if (completionDates.isEmpty()) return 0

        val today = LocalDate.now()
        if (!completionDates.contains(today)) return 0 // 오늘 완료 안 했으면 0

        var streak = 0
        var currentDate = today

        for (date in completionDates) {
            if (date == currentDate) {
                streak++
                currentDate = currentDate.minusDays(1)
            } else if (date.isBefore(currentDate)) {
                break // 연속 끊김
            }
        }

        return streak
    }

    /**
     * 모든 세션 조회
     */
    fun getAllSessions(): Flow<List<PomodoroSession>> {
        return pomodoroSessionDao.getAllSessions()
    }

    /**
     * 오늘의 모든 세션 조회
     */
    fun getTodaySessions(): Flow<List<PomodoroSession>> {
        return pomodoroSessionDao.getTodaySessions(LocalDate.now())
    }

    /**
     * 기간별 세션 조회
     */
    fun getSessionsByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<PomodoroSession>> {
        return pomodoroSessionDao.getSessionsByDateRange(startDate, endDate)
    }
}
