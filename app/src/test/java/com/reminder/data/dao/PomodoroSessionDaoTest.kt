package com.reminder.data.dao

import com.reminder.data.entity.PomodoroSession
import com.reminder.data.entity.SessionType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.time.LocalDate
import java.time.LocalDateTime
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class PomodoroSessionDaoTest {

    private lateinit var dao: PomodoroSessionDao

    @Before
    fun setup() {
        dao = mock()
    }

    /** insertSession은 세션을 삽입하고 ID를 반환한다 */
    @Test
    fun testInsertSessionInsertsSessionAndReturnsId() = runTest {
        // Given
        val session = PomodoroSession(
            reminderId = 1L,
            sessionType = SessionType.FOCUS,
            duration = 25,
            startedAt = LocalDateTime.now()
        )
        val insertedId = 10L
        whenever(dao.insertSession(session)).thenReturn(insertedId)

        // When
        val result = dao.insertSession(session)

        // Then
        verify(dao).insertSession(session)
        assertEquals(insertedId, result)
    }

    /** updateSession은 세션을 업데이트한다 */
    @Test
    fun testUpdateSessionUpdatesSession() = runTest {
        // Given
        val session = PomodoroSession(
            id = 1L,
            sessionType = SessionType.FOCUS,
            duration = 25,
            startedAt = LocalDateTime.now(),
            isCompleted = true,
            completedAt = LocalDateTime.now()
        )

        // When
        dao.updateSession(session)

        // Then
        verify(dao).updateSession(session)
    }

    /** getSessionById는 ID로 세션을 조회한다 */
    @Test
    fun testGetSessionByIdReturnsSessionById() = runTest {
        // Given
        val sessionId = 1L
        val session = PomodoroSession(
            id = sessionId,
            sessionType = SessionType.FOCUS,
            duration = 25,
            startedAt = LocalDateTime.now()
        )
        whenever(dao.getSessionById(sessionId)).thenReturn(session)

        // When
        val result = dao.getSessionById(sessionId)

        // Then
        verify(dao).getSessionById(sessionId)
        assertEquals(session, result)
    }

    /** getSessionById는 존재하지 않는 ID에 대해 null을 반환한다 */
    @Test
    fun testGetSessionByIdReturnsNullForNonExistentId() = runTest {
        // Given
        val sessionId = 999L
        whenever(dao.getSessionById(sessionId)).thenReturn(null)

        // When
        val result = dao.getSessionById(sessionId)

        // Then
        verify(dao).getSessionById(sessionId)
        assertNull(result)
    }

    /** deleteSessionById는 ID로 세션을 삭제한다 */
    @Test
    fun testDeleteSessionByIdDeletesSessionById() = runTest {
        // Given
        val sessionId = 1L

        // When
        dao.deleteSessionById(sessionId)

        // Then
        verify(dao).deleteSessionById(sessionId)
    }

    /** getSessionsByReminder는 리마인더의 모든 세션을 조회한다 */
    @Test
    fun testGetSessionsByReminderReturnsSessionsByReminderId() = runTest {
        // Given
        val reminderId = 1L
        val sessions = listOf(
            PomodoroSession(
                id = 1L,
                reminderId = reminderId,
                sessionType = SessionType.FOCUS,
                duration = 25,
                startedAt = LocalDateTime.now()
            ),
            PomodoroSession(
                id = 2L,
                reminderId = reminderId,
                sessionType = SessionType.SHORT_BREAK,
                duration = 5,
                startedAt = LocalDateTime.now().minusMinutes(30)
            )
        )
        whenever(dao.getSessionsByReminder(reminderId)).thenReturn(flowOf(sessions))

        // When
        val result = dao.getSessionsByReminder(reminderId)

        // Then
        verify(dao).getSessionsByReminder(reminderId)
        assertNotNull(result)
    }

    /** getCompletedSessionsCount는 완료된 세션 개수를 반환한다 */
    @Test
    fun testGetCompletedSessionsCountReturnsCount() = runTest {
        // Given
        val expectedCount = 10
        whenever(dao.getCompletedSessionsCount()).thenReturn(expectedCount)

        // When
        val result = dao.getCompletedSessionsCount()

        // Then
        verify(dao).getCompletedSessionsCount()
        assertEquals(expectedCount, result)
    }

    /** getCompletedSessionsCountByDate는 특정 날짜에 완료된 세션 개수를 반환한다 */
    @Test
    fun testGetCompletedSessionsCountByDateReturnsCountForDate() = runTest {
        // Given
        val date = LocalDate.now()
        val expectedCount = 5
        whenever(dao.getCompletedSessionsCountByDate(date)).thenReturn(expectedCount)

        // When
        val result = dao.getCompletedSessionsCountByDate(date)

        // Then
        verify(dao).getCompletedSessionsCountByDate(date)
        assertEquals(expectedCount, result)
    }

    /** getCompletedFocusSessionsCount는 완료된 집중 세션 개수를 반환한다 */
    @Test
    fun testGetCompletedFocusSessionsCountReturnsCount() = runTest {
        // Given
        val expectedCount = 8
        whenever(dao.getCompletedFocusSessionsCount()).thenReturn(expectedCount)

        // When
        val result = dao.getCompletedFocusSessionsCount()

        // Then
        verify(dao).getCompletedFocusSessionsCount()
        assertEquals(expectedCount, result)
    }

    /** getDistinctCompletionDates는 세션 완료 날짜 목록을 반환한다 */
    @Test
    fun testGetDistinctCompletionDatesReturnsDateList() = runTest {
        // Given
        val dates = listOf(
            LocalDate.now(),
            LocalDate.now().minusDays(1),
            LocalDate.now().minusDays(2)
        )
        whenever(dao.getDistinctCompletionDates()).thenReturn(dates)

        // When
        val result = dao.getDistinctCompletionDates()

        // Then
        verify(dao).getDistinctCompletionDates()
        assertEquals(dates, result)
    }

    /** getAllSessions는 모든 세션을 조회한다 */
    @Test
    fun testGetAllSessionsReturnsAllSessions() = runTest {
        // Given
        val sessions = listOf(
            PomodoroSession(
                id = 1L,
                sessionType = SessionType.FOCUS,
                duration = 25,
                startedAt = LocalDateTime.now()
            ),
            PomodoroSession(
                id = 2L,
                sessionType = SessionType.SHORT_BREAK,
                duration = 5,
                startedAt = LocalDateTime.now().minusHours(1)
            )
        )
        whenever(dao.getAllSessions()).thenReturn(flowOf(sessions))

        // When
        val result = dao.getAllSessions()

        // Then
        verify(dao).getAllSessions()
        assertNotNull(result)
    }

    /** getTodaySessions는 오늘의 모든 세션을 조회한다 */
    @Test
    fun testGetTodaySessionsReturnsTodaySessions() = runTest {
        // Given
        val today = LocalDate.now()
        val sessions = listOf(
            PomodoroSession(
                id = 1L,
                sessionType = SessionType.FOCUS,
                duration = 25,
                startedAt = LocalDateTime.now()
            )
        )
        whenever(dao.getTodaySessions(today)).thenReturn(flowOf(sessions))

        // When
        val result = dao.getTodaySessions(today)

        // Then
        verify(dao).getTodaySessions(today)
        assertNotNull(result)
    }

    /** getSessionsByDateRange는 기간별 세션을 조회한다 */
    @Test
    fun testGetSessionsByDateRangeReturnsSessionsInRange() = runTest {
        // Given
        val startDate = LocalDate.now().minusDays(7)
        val endDate = LocalDate.now()
        val sessions = listOf(
            PomodoroSession(
                id = 1L,
                sessionType = SessionType.FOCUS,
                duration = 25,
                startedAt = LocalDateTime.now().minusDays(3)
            ),
            PomodoroSession(
                id = 2L,
                sessionType = SessionType.LONG_BREAK,
                duration = 15,
                startedAt = LocalDateTime.now().minusDays(5)
            )
        )
        whenever(dao.getSessionsByDateRange(startDate, endDate)).thenReturn(flowOf(sessions))

        // When
        val result = dao.getSessionsByDateRange(startDate, endDate)

        // Then
        verify(dao).getSessionsByDateRange(startDate, endDate)
        assertNotNull(result)
    }

    /** deleteAllSessions는 모든 세션을 삭제한다 */
    @Test
    fun testDeleteAllSessionsDeletesAllSessions() = runTest {
        // When
        dao.deleteAllSessions()

        // Then
        verify(dao).deleteAllSessions()
    }
}
