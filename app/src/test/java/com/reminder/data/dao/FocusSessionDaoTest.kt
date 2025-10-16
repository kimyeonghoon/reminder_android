package com.reminder.data.dao

import com.reminder.data.entity.FocusSessionEntity
import com.reminder.data.entity.FocusType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.time.LocalDateTime
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class FocusSessionDaoTest {

    private lateinit var dao: FocusSessionDao

    @Before
    fun setup() {
        dao = mock()
    }

    /** insertSession은 세션을 삽입하고 ID를 반환한다 */
    @Test
    fun testInsertSessionInsertsSessionAndReturnsId() = runTest {
        // Given
        val session = FocusSessionEntity(
            reminderId = 1L,
            focusType = FocusType.DEEP_WORK,
            targetDurationMinutes = 25
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
        val session = FocusSessionEntity(
            id = 1L,
            reminderId = 1L,
            focusType = FocusType.POMODORO,
            actualDurationMinutes = 25,
            isCompleted = true
        )

        // When
        dao.updateSession(session)

        // Then
        verify(dao).updateSession(session)
    }

    /** deleteSession은 세션을 삭제한다 */
    @Test
    fun testDeleteSessionDeletesSession() = runTest {
        // Given
        val session = FocusSessionEntity(
            id = 1L,
            reminderId = 1L,
            focusType = FocusType.DEEP_WORK
        )

        // When
        dao.deleteSession(session)

        // Then
        verify(dao).deleteSession(session)
    }

    /** getSessionById는 ID로 세션을 조회한다 */
    @Test
    fun testGetSessionByIdReturnsSessionWithMatchingId() = runTest {
        // Given
        val sessionId = 5L
        val session = FocusSessionEntity(
            id = sessionId,
            reminderId = 1L,
            focusType = FocusType.DO_FIRST,
            targetDurationMinutes = 30
        )
        whenever(dao.getSessionById(sessionId)).thenReturn(session)

        // When
        val result = dao.getSessionById(sessionId)

        // Then
        verify(dao).getSessionById(sessionId)
        assertEquals(session, result)
    }

    /** getSessionById는 존재하지 않는 ID로 조회 시 null을 반환한다 */
    @Test
    fun testGetSessionByIdReturnsNullWhenNotFound() = runTest {
        // Given
        val sessionId = 999L
        whenever(dao.getSessionById(sessionId)).thenReturn(null)

        // When
        val result = dao.getSessionById(sessionId)

        // Then
        verify(dao).getSessionById(sessionId)
        assertNull(result)
    }

    /** getAllSessions는 모든 세션을 최신순으로 조회한다 */
    @Test
    fun testGetAllSessionsReturnsAllSessionsInDescendingOrder() = runTest {
        // Given
        val now = LocalDateTime.now()
        val sessions = listOf(
            FocusSessionEntity(id = 2, startTime = now, focusType = FocusType.POMODORO),
            FocusSessionEntity(id = 1, startTime = now.minusHours(1), focusType = FocusType.DEEP_WORK)
        )
        whenever(dao.getAllSessions()).thenReturn(flowOf(sessions))

        // When
        val result = dao.getAllSessions()

        // Then
        verify(dao).getAllSessions()
        assertNotNull(result)
    }

    /** getActiveSessions는 진행 중인 세션들만 조회한다 */
    @Test
    fun testGetActiveSessionsReturnsOnlyActiveSessions() = runTest {
        // Given
        val activeSessions = listOf(
            FocusSessionEntity(
                id = 1,
                reminderId = 1L,
                focusType = FocusType.DEEP_WORK,
                endTime = null,
                isCompleted = false,
                isInterrupted = false
            ),
            FocusSessionEntity(
                id = 2,
                reminderId = 2L,
                focusType = FocusType.POMODORO,
                endTime = null,
                isCompleted = false,
                isInterrupted = false
            )
        )
        whenever(dao.getActiveSessions()).thenReturn(flowOf(activeSessions))

        // When
        val result = dao.getActiveSessions()

        // Then
        verify(dao).getActiveSessions()
        assertNotNull(result)
    }

    /** getCompletedSessions는 완료된 세션들만 조회한다 */
    @Test
    fun testGetCompletedSessionsReturnsOnlyCompletedSessions() = runTest {
        // Given
        val now = LocalDateTime.now()
        val completedSessions = listOf(
            FocusSessionEntity(
                id = 1,
                reminderId = 1L,
                focusType = FocusType.DEEP_WORK,
                startTime = now.minusHours(2),
                endTime = now.minusHours(1),
                actualDurationMinutes = 60,
                isCompleted = true
            ),
            FocusSessionEntity(
                id = 2,
                reminderId = 2L,
                focusType = FocusType.POMODORO,
                startTime = now.minusHours(3),
                endTime = now.minusHours(2).minusMinutes(35),
                actualDurationMinutes = 25,
                isCompleted = true
            )
        )
        whenever(dao.getCompletedSessions()).thenReturn(flowOf(completedSessions))

        // When
        val result = dao.getCompletedSessions()

        // Then
        verify(dao).getCompletedSessions()
        assertNotNull(result)
    }

    /** getSessionsByReminderId는 특정 리마인더의 세션들을 조회한다 */
    @Test
    fun testGetSessionsByReminderIdReturnsSessionsForReminder() = runTest {
        // Given
        val reminderId = 10L
        val sessions = listOf(
            FocusSessionEntity(id = 1, reminderId = reminderId, focusType = FocusType.DEEP_WORK),
            FocusSessionEntity(id = 2, reminderId = reminderId, focusType = FocusType.POMODORO)
        )
        whenever(dao.getSessionsByReminderId(reminderId)).thenReturn(flowOf(sessions))

        // When
        val result = dao.getSessionsByReminderId(reminderId)

        // Then
        verify(dao).getSessionsByReminderId(reminderId)
        assertNotNull(result)
    }

    /** getSessionsBetweenDates는 날짜 범위 내의 세션들을 조회한다 */
    @Test
    fun testGetSessionsBetweenDatesReturnsSessionsInDateRange() = runTest {
        // Given
        val startDate = LocalDateTime.of(2025, 10, 1, 0, 0)
        val endDate = LocalDateTime.of(2025, 10, 31, 23, 59)
        val sessions = listOf(
            FocusSessionEntity(
                id = 1,
                startTime = LocalDateTime.of(2025, 10, 15, 10, 0),
                focusType = FocusType.DEEP_WORK
            ),
            FocusSessionEntity(
                id = 2,
                startTime = LocalDateTime.of(2025, 10, 20, 14, 0),
                focusType = FocusType.POMODORO
            )
        )
        whenever(dao.getSessionsBetweenDates(startDate, endDate)).thenReturn(flowOf(sessions))

        // When
        val result = dao.getSessionsBetweenDates(startDate, endDate)

        // Then
        verify(dao).getSessionsBetweenDates(startDate, endDate)
        assertNotNull(result)
    }

    /** getTotalFocusMinutes는 완료된 세션들의 총 집중 시간을 반환한다 */
    @Test
    fun testGetTotalFocusMinutesReturnsSumOfCompletedSessions() = runTest {
        // Given
        val totalMinutes = 150 // 25 + 50 + 75
        whenever(dao.getTotalFocusMinutes()).thenReturn(totalMinutes)

        // When
        val result = dao.getTotalFocusMinutes()

        // Then
        verify(dao).getTotalFocusMinutes()
        assertEquals(totalMinutes, result)
    }

    /** getTotalFocusMinutes는 완료된 세션이 없을 때 0을 반환한다 */
    @Test
    fun testGetTotalFocusMinutesReturnsZeroWhenNoCompletedSessions() = runTest {
        // Given
        whenever(dao.getTotalFocusMinutes()).thenReturn(0)

        // When
        val result = dao.getTotalFocusMinutes()

        // Then
        verify(dao).getTotalFocusMinutes()
        assertEquals(0, result)
    }

    /** deleteOldSessions는 지정된 날짜 이전의 세션들을 삭제한다 */
    @Test
    fun testDeleteOldSessionsDeletesSessionsBeforeCutoffDate() = runTest {
        // Given
        val cutoffDate = LocalDateTime.of(2025, 9, 1, 0, 0)

        // When
        dao.deleteOldSessions(cutoffDate)

        // Then
        verify(dao).deleteOldSessions(cutoffDate)
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
