package com.reminder.pomodoro

import com.reminder.data.dao.PomodoroSessionDao
import com.reminder.data.entity.PomodoroSession
import com.reminder.data.entity.SessionType
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * v1.45.0: 포모도로 타이머 매니저 TDD 테스트
 *
 * Red Phase: 테스트 먼저 작성
 */
class PomodoroManagerTest {

    private lateinit var pomodoroSessionDao: PomodoroSessionDao
    private lateinit var pomodoroManager: PomodoroManager

    @Before
    fun setup() {
        pomodoroSessionDao = mock()
        pomodoroManager = PomodoroManager(pomodoroSessionDao)
    }

    /** 집중 세션을 시작할 수 있다 */
    @Test
    fun startFocusSession() = runTest {
        // Given
        val reminderId = 1L
        val sessionType = SessionType.FOCUS
        whenever(pomodoroSessionDao.insertSession(any())).thenReturn(1L)

        // When
        val sessionId = pomodoroManager.startSession(reminderId, sessionType)

        // Then
        assertTrue(sessionId > 0)
        verify(pomodoroSessionDao, times(1)).insertSession(any())
    }

    /** 리마인더 없이 독립 세션을 시작할 수 있다 */
    @Test
    fun startIndependentSessionWithoutReminder() = runTest {
        // Given
        val sessionType = SessionType.FOCUS
        whenever(pomodoroSessionDao.insertSession(any())).thenReturn(1L)

        // When
        val sessionId = pomodoroManager.startSession(null, sessionType)

        // Then
        assertTrue(sessionId > 0)
        verify(pomodoroSessionDao, times(1)).insertSession(any())
    }

    /** 세션을 완료 처리할 수 있다 */
    @Test
    fun completeSession() = runTest {
        // Given
        val sessionId = 1L
        val session = PomodoroSession(
            id = sessionId,
            reminderId = null,
            sessionType = SessionType.FOCUS,
            duration = 25,
            startedAt = LocalDateTime.now(),
            completedAt = null,
            isCompleted = false,
            createdAt = LocalDateTime.now()
        )
        whenever(pomodoroSessionDao.getSessionById(sessionId)).thenReturn(session)

        // When
        pomodoroManager.completeSession(sessionId)

        // Then
        verify(pomodoroSessionDao, times(1)).updateSession(
            check { require(it.id == sessionId && it.isCompleted) }
        )
    }

    /** 세션을 취소할 수 있다 */
    @Test
    fun cancelSession() = runTest {
        // Given
        val sessionId = 1L

        // When
        pomodoroManager.cancelSession(sessionId)

        // Then
        verify(pomodoroSessionDao, times(1)).deleteSessionById(sessionId)
    }

    /** 전체 완료 세션 수를 조회할 수 있다 */
    @Test
    fun getTotalCompletedSessionsCount() = runTest {
        // Given
        val expectedCount = 42
        whenever(pomodoroSessionDao.getCompletedSessionsCount()).thenReturn(expectedCount)

        // When
        val count = pomodoroManager.getTotalCompletedSessions()

        // Then
        assertEquals(expectedCount, count)
    }

    /** 오늘 완료한 세션 수를 조회할 수 있다 */
    @Test
    fun getTodayCompletedSessionsCount() = runTest {
        // Given
        val today = LocalDate.now()
        val expectedCount = 5
        whenever(pomodoroSessionDao.getCompletedSessionsCountByDate(today)).thenReturn(expectedCount)

        // When
        val count = pomodoroManager.getTodayCompletedSessions()

        // Then
        assertEquals(expectedCount, count)
    }

    /** 리마인더의 모든 세션을 조회할 수 있다 */
    @Test
    fun getAllSessionsForReminder() = runTest {
        // Given
        val reminderId = 1L
        val sessions = listOf(
            PomodoroSession(
                id = 1L,
                reminderId = reminderId,
                sessionType = SessionType.FOCUS,
                duration = 25,
                startedAt = LocalDateTime.now(),
                completedAt = LocalDateTime.now(),
                isCompleted = true,
                createdAt = LocalDateTime.now()
            )
        )
        whenever(pomodoroSessionDao.getSessionsByReminder(reminderId)).thenReturn(flowOf(sessions))

        // When
        pomodoroManager.getSessionsForReminder(reminderId).collect { result ->
            // Then
            assertEquals(1, result.size)
            assertEquals(reminderId, result[0].reminderId)
        }
    }

    /** 집중 세션 시간이 25분이다 */
    @Test
    fun focusSessionDurationIs25Minutes() {
        // When
        val duration = pomodoroManager.getFocusSessionDuration()

        // Then
        assertEquals(25, duration)
    }

    /** 짧은 휴식 시간이 5분이다 */
    @Test
    fun shortBreakDurationIs5Minutes() {
        // When
        val duration = pomodoroManager.getShortBreakDuration()

        // Then
        assertEquals(5, duration)
    }

    /** 긴 휴식 시간이 15분이다 */
    @Test
    fun longBreakDurationIs15Minutes() {
        // When
        val duration = pomodoroManager.getLongBreakDuration()

        // Then
        assertEquals(15, duration)
    }

    /** 전체 집중 시간을 올바르게 계산한다 */
    @Test
    fun calculateTotalFocusMinutesCorrectly() = runTest {
        // Given
        val completedSessions = 10
        whenever(pomodoroSessionDao.getCompletedFocusSessionsCount()).thenReturn(completedSessions)

        // When
        val totalMinutes = pomodoroManager.getTotalFocusMinutes()

        // Then
        assertEquals(250, totalMinutes) // 10 * 25 = 250분
    }

    /** 연속 완료 일수를 계산한다 */
    @Test
    fun calculateStreakDays() = runTest {
        // Given
        val dates = listOf(
            LocalDate.now(),
            LocalDate.now().minusDays(1),
            LocalDate.now().minusDays(2)
        )
        whenever(pomodoroSessionDao.getDistinctCompletionDates()).thenReturn(dates)

        // When
        val streak = pomodoroManager.getStreakDays()

        // Then
        assertEquals(3, streak)
    }
}
