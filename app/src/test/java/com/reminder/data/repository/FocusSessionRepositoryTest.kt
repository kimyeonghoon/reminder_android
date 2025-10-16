package com.reminder.data.repository

import com.reminder.data.dao.FocusSessionDao
import com.reminder.data.entity.FocusSessionEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.time.LocalDateTime
import org.junit.Assert.*

/**
 * FocusSessionRepository 테스트
 *
 * 포커스 세션 저장소의 모든 메서드를 검증합니다.
 * - AAA 패턴 (Given-When-Then)
 * - Mockito를 사용한 단위 테스트
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FocusSessionRepositoryTest {

    private lateinit var focusSessionDao: FocusSessionDao
    private lateinit var repository: FocusSessionRepository

    @Before
    fun setup() {
        focusSessionDao = mock()
        repository = FocusSessionRepository(focusSessionDao)
    }

    /** insertSession은 세션을 삽입하고 ID를 반환한다 */
    @Test
    fun testInsertSessionInsertsAndReturnsId() = runTest {
        // Given
        val session = FocusSessionEntity(
            id = 0,
            reminderId = 1,
            startTime = LocalDateTime.now(),
            targetDurationMinutes = 25
        )
        val insertedId = 10L
        whenever(focusSessionDao.insertSession(session)).thenReturn(insertedId)

        // When
        val result = repository.insertSession(session)

        // Then
        verify(focusSessionDao).insertSession(session)
        assertEquals(insertedId, result)
    }

    /** insertSession은 올바른 세션 정보를 DAO에 전달한다 */
    @Test
    fun testInsertSessionPassesCorrectSessionToDao() = runTest {
        // Given
        val session = FocusSessionEntity(
            id = 0,
            reminderId = 5,
            startTime = LocalDateTime.now(),
            targetDurationMinutes = 30
        )
        whenever(focusSessionDao.insertSession(session)).thenReturn(1L)

        // When
        repository.insertSession(session)

        // Then
        verify(focusSessionDao).insertSession(argThat { s ->
            s.reminderId == 5L && s.targetDurationMinutes == 30
        })
    }

    /** updateSession은 세션을 업데이트한다 */
    @Test
    fun testUpdateSessionUpdatesSession() = runTest {
        // Given
        val session = FocusSessionEntity(
            id = 1,
            reminderId = 1,
            startTime = LocalDateTime.now(),
            endTime = LocalDateTime.now().plusMinutes(25),
            targetDurationMinutes = 25,
            actualDurationMinutes = 25,
            isCompleted = true
        )

        // When
        repository.updateSession(session)

        // Then
        verify(focusSessionDao).updateSession(session)
    }

    /** updateSession은 완료된 세션의 정보를 업데이트할 수 있다 */
    @Test
    fun testUpdateSessionCanUpdateCompletedSession() = runTest {
        // Given
        val session = FocusSessionEntity(
            id = 1,
            reminderId = 1,
            startTime = LocalDateTime.now().minusMinutes(25),
            endTime = LocalDateTime.now(),
            targetDurationMinutes = 25,
            actualDurationMinutes = 25,
            isCompleted = true
        )

        // When
        repository.updateSession(session)

        // Then
        verify(focusSessionDao).updateSession(argThat { s ->
            s.isCompleted == true && s.actualDurationMinutes == 25
        })
    }

    /** deleteSession은 세션을 삭제한다 */
    @Test
    fun testDeleteSessionDeletesSession() = runTest {
        // Given
        val session = FocusSessionEntity(
            id = 1,
            reminderId = 1,
            startTime = LocalDateTime.now(),
            targetDurationMinutes = 25
        )

        // When
        repository.deleteSession(session)

        // Then
        verify(focusSessionDao).deleteSession(session)
    }

    /** getSessionById는 ID로 세션을 조회한다 */
    @Test
    fun testGetSessionByIdReturnsSessionWithMatchingId() = runTest {
        // Given
        val sessionId = 1L
        val session = FocusSessionEntity(
            id = sessionId,
            reminderId = 1,
            startTime = LocalDateTime.now(),
            targetDurationMinutes = 25
        )
        whenever(focusSessionDao.getSessionById(sessionId)).thenReturn(session)

        // When
        val result = repository.getSessionById(sessionId)

        // Then
        verify(focusSessionDao).getSessionById(sessionId)
        assertEquals(sessionId, result?.id)
    }

    /** getSessionById는 존재하지 않는 ID로 조회 시 null을 반환한다 */
    @Test
    fun testGetSessionByIdReturnsNullForNonExistentId() = runTest {
        // Given
        val nonExistentId = 999L
        whenever(focusSessionDao.getSessionById(nonExistentId)).thenReturn(null)

        // When
        val result = repository.getSessionById(nonExistentId)

        // Then
        verify(focusSessionDao).getSessionById(nonExistentId)
        assertNull(result)
    }

    /** getAllSessions는 모든 세션을 Flow로 반환한다 */
    @Test
    fun testGetAllSessionsReturnsAllSessionsAsFlow() = runTest {
        // Given
        val sessions = listOf(
            FocusSessionEntity(id = 1, reminderId = 1, startTime = LocalDateTime.now(), targetDurationMinutes = 25),
            FocusSessionEntity(id = 2, reminderId = 2, startTime = LocalDateTime.now(), targetDurationMinutes = 30)
        )
        whenever(focusSessionDao.getAllSessions()).thenReturn(flowOf(sessions))

        // When
        val flow = repository.getAllSessions()

        // Then
        verify(focusSessionDao).getAllSessions()
        flow.collect { result ->
            assertEquals(2, result.size)
        }
    }

    /** getAllSessions는 세션이 없을 때 빈 리스트를 반환한다 */
    @Test
    fun testGetAllSessionsReturnsEmptyListWhenNoSessions() = runTest {
        // Given
        whenever(focusSessionDao.getAllSessions()).thenReturn(flowOf(emptyList()))

        // When
        val flow = repository.getAllSessions()

        // Then
        verify(focusSessionDao).getAllSessions()
        flow.collect { result ->
            assertTrue(result.isEmpty())
        }
    }

    /** getActiveSessions는 활성 세션만 반환한다 */
    @Test
    fun testGetActiveSessionsReturnsOnlyActiveSessions() = runTest {
        // Given
        val activeSessions = listOf(
            FocusSessionEntity(
                id = 1,
                reminderId = 1,
                startTime = LocalDateTime.now(),
                targetDurationMinutes = 25,
                isCompleted = false
            )
        )
        whenever(focusSessionDao.getActiveSessions()).thenReturn(flowOf(activeSessions))

        // When
        val flow = repository.getActiveSessions()

        // Then
        verify(focusSessionDao).getActiveSessions()
        flow.collect { result ->
            assertEquals(1, result.size)
            assertFalse(result[0].isCompleted)
        }
    }

    /** getCompletedSessions는 완료된 세션만 반환한다 */
    @Test
    fun testGetCompletedSessionsReturnsOnlyCompletedSessions() = runTest {
        // Given
        val completedSessions = listOf(
            FocusSessionEntity(
                id = 1,
                reminderId = 1,
                startTime = LocalDateTime.now().minusMinutes(25),
                endTime = LocalDateTime.now(),
                targetDurationMinutes = 25,
                actualDurationMinutes = 25,
                isCompleted = true
            )
        )
        whenever(focusSessionDao.getCompletedSessions()).thenReturn(flowOf(completedSessions))

        // When
        val flow = repository.getCompletedSessions()

        // Then
        verify(focusSessionDao).getCompletedSessions()
        flow.collect { result ->
            assertEquals(1, result.size)
            assertTrue(result[0].isCompleted)
        }
    }

    /** getSessionsByReminderId는 특정 리마인더의 세션만 반환한다 */
    @Test
    fun testGetSessionsByReminderIdReturnsSessionsForSpecificReminder() = runTest {
        // Given
        val reminderId = 5L
        val sessions = listOf(
            FocusSessionEntity(id = 1, reminderId = reminderId, startTime = LocalDateTime.now(), targetDurationMinutes = 25),
            FocusSessionEntity(id = 2, reminderId = reminderId, startTime = LocalDateTime.now(), targetDurationMinutes = 30)
        )
        whenever(focusSessionDao.getSessionsByReminderId(reminderId)).thenReturn(flowOf(sessions))

        // When
        val flow = repository.getSessionsByReminderId(reminderId)

        // Then
        verify(focusSessionDao).getSessionsByReminderId(reminderId)
        flow.collect { result ->
            assertEquals(2, result.size)
            assertTrue(result.all { it.reminderId == reminderId })
        }
    }

    /** getSessionsByReminderId는 해당 리마인더에 세션이 없으면 빈 리스트를 반환한다 */
    @Test
    fun testGetSessionsByReminderIdReturnsEmptyListWhenNoSessions() = runTest {
        // Given
        val reminderId = 999L
        whenever(focusSessionDao.getSessionsByReminderId(reminderId)).thenReturn(flowOf(emptyList()))

        // When
        val flow = repository.getSessionsByReminderId(reminderId)

        // Then
        verify(focusSessionDao).getSessionsByReminderId(reminderId)
        flow.collect { result ->
            assertTrue(result.isEmpty())
        }
    }

    /** getSessionsBetweenDates는 날짜 범위 내의 세션을 반환한다 */
    @Test
    fun testGetSessionsBetweenDatesReturnsSessionsInDateRange() = runTest {
        // Given
        val startDate = LocalDateTime.of(2025, 10, 1, 0, 0)
        val endDate = LocalDateTime.of(2025, 10, 31, 23, 59)
        val sessions = listOf(
            FocusSessionEntity(
                id = 1,
                reminderId = 1,
                startTime = LocalDateTime.of(2025, 10, 15, 10, 0),
                targetDurationMinutes = 25
            )
        )
        whenever(focusSessionDao.getSessionsBetweenDates(startDate, endDate))
            .thenReturn(flowOf(sessions))

        // When
        val flow = repository.getSessionsBetweenDates(startDate, endDate)

        // Then
        verify(focusSessionDao).getSessionsBetweenDates(startDate, endDate)
        flow.collect { result ->
            assertEquals(1, result.size)
        }
    }

    /** getSessionsBetweenDates는 범위를 벗어난 세션을 제외한다 */
    @Test
    fun testGetSessionsBetweenDatesExcludesSessionsOutsideRange() = runTest {
        // Given
        val startDate = LocalDateTime.of(2025, 10, 1, 0, 0)
        val endDate = LocalDateTime.of(2025, 10, 31, 23, 59)
        whenever(focusSessionDao.getSessionsBetweenDates(startDate, endDate))
            .thenReturn(flowOf(emptyList()))

        // When
        val flow = repository.getSessionsBetweenDates(startDate, endDate)

        // Then
        verify(focusSessionDao).getSessionsBetweenDates(startDate, endDate)
        flow.collect { result ->
            assertTrue(result.isEmpty())
        }
    }

    /** getTotalFocusMinutes는 총 집중 시간을 분 단위로 반환한다 */
    @Test
    fun testGetTotalFocusMinutesReturnsTotalMinutes() = runTest {
        // Given
        val totalMinutes = 150
        whenever(focusSessionDao.getTotalFocusMinutes()).thenReturn(totalMinutes)

        // When
        val result = repository.getTotalFocusMinutes()

        // Then
        verify(focusSessionDao).getTotalFocusMinutes()
        assertEquals(totalMinutes, result)
    }

    /** getTotalFocusMinutes는 세션이 없을 때 0을 반환한다 */
    @Test
    fun testGetTotalFocusMinutesReturnsZeroWhenNoSessions() = runTest {
        // Given
        whenever(focusSessionDao.getTotalFocusMinutes()).thenReturn(0)

        // When
        val result = repository.getTotalFocusMinutes()

        // Then
        verify(focusSessionDao).getTotalFocusMinutes()
        assertEquals(0, result)
    }

    /** deleteOldSessions는 기준 날짜 이전의 세션을 삭제한다 */
    @Test
    fun testDeleteOldSessionsDeletesSessionsBeforeCutoffDate() = runTest {
        // Given
        val cutoffDate = LocalDateTime.now().minusDays(90)

        // When
        repository.deleteOldSessions(cutoffDate)

        // Then
        verify(focusSessionDao).deleteOldSessions(cutoffDate)
    }

    /** deleteOldSessions는 90일 이상 된 세션을 정리할 수 있다 */
    @Test
    fun testDeleteOldSessionsCanCleanup90DaysOldSessions() = runTest {
        // Given
        val ninetyDaysAgo = LocalDateTime.now().minusDays(90)

        // When
        repository.deleteOldSessions(ninetyDaysAgo)

        // Then
        verify(focusSessionDao).deleteOldSessions(argThat { date ->
            date.isBefore(LocalDateTime.now().minusDays(89))
        })
    }

    /** deleteAllSessions는 모든 세션을 삭제한다 */
    @Test
    fun testDeleteAllSessionsDeletesAllSessions() = runTest {
        // When
        repository.deleteAllSessions()

        // Then
        verify(focusSessionDao).deleteAllSessions()
    }

    /** deleteAllSessions는 테스트용으로 사용할 수 있다 */
    @Test
    fun testDeleteAllSessionsCanBeUsedForTesting() = runTest {
        // When
        repository.deleteAllSessions()

        // Then
        verify(focusSessionDao, times(1)).deleteAllSessions()
    }

    /** 여러 세션을 연속으로 추가할 수 있다 */
    @Test
    fun testCanInsertMultipleSessionsSequentially() = runTest {
        // Given
        val session1 = FocusSessionEntity(id = 0, reminderId = 1, startTime = LocalDateTime.now(), targetDurationMinutes = 25)
        val session2 = FocusSessionEntity(id = 0, reminderId = 1, startTime = LocalDateTime.now(), targetDurationMinutes = 30)
        val session3 = FocusSessionEntity(id = 0, reminderId = 1, startTime = LocalDateTime.now(), targetDurationMinutes = 45)
        whenever(focusSessionDao.insertSession(any())).thenReturn(1L, 2L, 3L)

        // When
        repository.insertSession(session1)
        repository.insertSession(session2)
        repository.insertSession(session3)

        // Then
        verify(focusSessionDao, times(3)).insertSession(any())
    }

    /** 세션을 삽입하고 업데이트할 수 있다 */
    @Test
    fun testCanInsertAndUpdateSession() = runTest {
        // Given
        val session = FocusSessionEntity(
            id = 1,
            reminderId = 1,
            startTime = LocalDateTime.now(),
            targetDurationMinutes = 25
        )
        whenever(focusSessionDao.insertSession(session)).thenReturn(1L)

        // When
        repository.insertSession(session)
        val updatedSession = session.copy(isCompleted = true, actualDurationMinutes = 25)
        repository.updateSession(updatedSession)

        // Then
        verify(focusSessionDao).insertSession(session)
        verify(focusSessionDao).updateSession(updatedSession)
    }

    /** Repository는 DAO 호출을 정확히 위임한다 */
    @Test
    fun testRepositoryDelegatesToDaoCorrectly() = runTest {
        // Given
        val session = FocusSessionEntity(id = 1, reminderId = 1, startTime = LocalDateTime.now(), targetDurationMinutes = 25)
        val reminderId = 1L
        val startDate = LocalDateTime.now().minusDays(7)
        val endDate = LocalDateTime.now()
        val cutoffDate = LocalDateTime.now().minusDays(90)

        whenever(focusSessionDao.insertSession(any())).thenReturn(1L)
        whenever(focusSessionDao.getSessionById(any())).thenReturn(session)
        whenever(focusSessionDao.getAllSessions()).thenReturn(flowOf(listOf(session)))
        whenever(focusSessionDao.getActiveSessions()).thenReturn(flowOf(listOf(session)))
        whenever(focusSessionDao.getCompletedSessions()).thenReturn(flowOf(emptyList()))
        whenever(focusSessionDao.getSessionsByReminderId(any())).thenReturn(flowOf(listOf(session)))
        whenever(focusSessionDao.getSessionsBetweenDates(any(), any())).thenReturn(flowOf(listOf(session)))
        whenever(focusSessionDao.getTotalFocusMinutes()).thenReturn(100)

        // When
        repository.insertSession(session)
        repository.updateSession(session)
        repository.deleteSession(session)
        repository.getSessionById(1L)
        repository.getAllSessions()
        repository.getActiveSessions()
        repository.getCompletedSessions()
        repository.getSessionsByReminderId(reminderId)
        repository.getSessionsBetweenDates(startDate, endDate)
        repository.getTotalFocusMinutes()
        repository.deleteOldSessions(cutoffDate)
        repository.deleteAllSessions()

        // Then (모든 DAO 메서드가 정확히 한 번씩 호출됨)
        verify(focusSessionDao).insertSession(session)
        verify(focusSessionDao).updateSession(session)
        verify(focusSessionDao).deleteSession(session)
        verify(focusSessionDao).getSessionById(1L)
        verify(focusSessionDao).getAllSessions()
        verify(focusSessionDao).getActiveSessions()
        verify(focusSessionDao).getCompletedSessions()
        verify(focusSessionDao).getSessionsByReminderId(reminderId)
        verify(focusSessionDao).getSessionsBetweenDates(startDate, endDate)
        verify(focusSessionDao).getTotalFocusMinutes()
        verify(focusSessionDao).deleteOldSessions(cutoffDate)
        verify(focusSessionDao).deleteAllSessions()
    }
}
