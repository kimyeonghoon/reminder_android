package com.reminder.pomodoro

import com.reminder.data.dao.PomodoroSessionDao
import com.reminder.data.entity.PomodoroSession
import com.reminder.data.entity.SessionType
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.whenever
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
        pomodoroSessionDao = mock(PomodoroSessionDao::class.java)
        pomodoroManager = PomodoroManager(pomodoroSessionDao)
    }

    @Test
    fun `startSession should create new focus session`() = runTest {
        // Given
        val reminderId = 1L
        val sessionType = SessionType.FOCUS

        // When
        val sessionId = pomodoroManager.startSession(reminderId, sessionType)

        // Then
        assertTrue(sessionId > 0)
        verify(pomodoroSessionDao, times(1)).insertSession(any())
    }

    @Test
    fun `startSession without reminderId should create standalone session`() = runTest {
        // Given
        val sessionType = SessionType.FOCUS

        // When
        val sessionId = pomodoroManager.startSession(null, sessionType)

        // Then
        assertTrue(sessionId > 0)
        verify(pomodoroSessionDao, times(1)).insertSession(any())
    }

    @Test
    fun `completeSession should mark session as completed`() = runTest {
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
        verify(pomodoroSessionDao, times(1)).updateSession(argThat {
            it.id == sessionId && it.isCompleted
        })
    }

    @Test
    fun `cancelSession should delete session`() = runTest {
        // Given
        val sessionId = 1L

        // When
        pomodoroManager.cancelSession(sessionId)

        // Then
        verify(pomodoroSessionDao, times(1)).deleteSessionById(sessionId)
    }

    @Test
    fun `getTotalCompletedSessions should return count`() = runTest {
        // Given
        val expectedCount = 42
        whenever(pomodoroSessionDao.getCompletedSessionsCount()).thenReturn(expectedCount)

        // When
        val count = pomodoroManager.getTotalCompletedSessions()

        // Then
        assertEquals(expectedCount, count)
    }

    @Test
    fun `getTodayCompletedSessions should return today count`() = runTest {
        // Given
        val today = LocalDate.now()
        val expectedCount = 5
        whenever(pomodoroSessionDao.getCompletedSessionsCountByDate(today)).thenReturn(expectedCount)

        // When
        val count = pomodoroManager.getTodayCompletedSessions()

        // Then
        assertEquals(expectedCount, count)
    }

    @Test
    fun `getSessionsForReminder should return all sessions`() = runTest {
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

    @Test
    fun `getFocusSessionDuration should return 25 minutes`() {
        // When
        val duration = pomodoroManager.getFocusSessionDuration()

        // Then
        assertEquals(25, duration)
    }

    @Test
    fun `getShortBreakDuration should return 5 minutes`() {
        // When
        val duration = pomodoroManager.getShortBreakDuration()

        // Then
        assertEquals(5, duration)
    }

    @Test
    fun `getLongBreakDuration should return 15 minutes`() {
        // When
        val duration = pomodoroManager.getLongBreakDuration()

        // Then
        assertEquals(15, duration)
    }

    @Test
    fun `getTotalFocusMinutes should calculate correctly`() = runTest {
        // Given
        val completedSessions = 10
        whenever(pomodoroSessionDao.getCompletedFocusSessionsCount()).thenReturn(completedSessions)

        // When
        val totalMinutes = pomodoroManager.getTotalFocusMinutes()

        // Then
        assertEquals(250, totalMinutes) // 10 * 25 = 250분
    }

    @Test
    fun `getStreakDays should calculate consecutive days`() = runTest {
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
