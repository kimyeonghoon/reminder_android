package com.reminder.data.dao

import com.reminder.data.entity.ChosenDataSource
import com.reminder.data.entity.ConflictLogEntity
import com.reminder.data.entity.ResolutionStrategy
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.time.LocalDateTime
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class ConflictLogDaoTest {

    private lateinit var dao: ConflictLogDao

    @Before
    fun setup() {
        dao = mock()
    }

    /** getAllConflictLogs는 모든 충돌 로그를 최신순으로 반환한다 */
    @Test
    fun testGetAllConflictLogsReturnsAllLogsInDescendingOrder() = runTest {
        // Given
        val now = LocalDateTime.now()
        val log1 = ConflictLogEntity(
            id = 1,
            reminderId = 100,
            conflictedAt = now.minusHours(2),
            resolutionStrategy = ResolutionStrategy.LAST_WRITE_WINS,
            localData = "{\"title\":\"로컬\"}",
            remoteData = "{\"title\":\"원격\"}",
            chosenData = ChosenDataSource.REMOTE,
            isResolved = true
        )
        val log2 = ConflictLogEntity(
            id = 2,
            reminderId = 200,
            conflictedAt = now.minusHours(1),
            resolutionStrategy = ResolutionStrategy.MANUAL,
            localData = "{\"title\":\"로컬2\"}",
            remoteData = "{\"title\":\"원격2\"}",
            chosenData = ChosenDataSource.LOCAL,
            isResolved = false
        )
        whenever(dao.getAllConflictLogs()).thenReturn(flowOf(listOf(log2, log1)))

        // When
        val flow = dao.getAllConflictLogs()

        // Then
        verify(dao).getAllConflictLogs()
        flow.collect { logs ->
            assertEquals(2, logs.size)
            assertEquals(log2.id, logs[0].id) // 최신순
            assertEquals(log1.id, logs[1].id)
        }
    }

    /** getUnresolvedConflicts는 미해결 충돌만 반환한다 */
    @Test
    fun testGetUnresolvedConflictsReturnsOnlyUnresolvedLogs() = runTest {
        // Given
        val now = LocalDateTime.now()
        val unresolvedLog = ConflictLogEntity(
            id = 1,
            reminderId = 100,
            conflictedAt = now,
            resolutionStrategy = ResolutionStrategy.MANUAL,
            localData = "{\"title\":\"로컬\"}",
            remoteData = "{\"title\":\"원격\"}",
            chosenData = ChosenDataSource.LOCAL,
            isResolved = false
        )
        whenever(dao.getUnresolvedConflicts()).thenReturn(flowOf(listOf(unresolvedLog)))

        // When
        val flow = dao.getUnresolvedConflicts()

        // Then
        verify(dao).getUnresolvedConflicts()
        flow.collect { logs ->
            assertEquals(1, logs.size)
            assertEquals(false, logs[0].isResolved)
        }
    }

    /** getConflictLogsByReminderId는 특정 리마인더의 충돌 로그만 반환한다 */
    @Test
    fun testGetConflictLogsByReminderIdReturnsLogsForSpecificReminder() = runTest {
        // Given
        val reminderId = 100L
        val now = LocalDateTime.now()
        val log1 = ConflictLogEntity(
            id = 1,
            reminderId = reminderId,
            conflictedAt = now,
            resolutionStrategy = ResolutionStrategy.LAST_WRITE_WINS,
            localData = "{\"title\":\"로컬1\"}",
            remoteData = "{\"title\":\"원격1\"}",
            chosenData = ChosenDataSource.REMOTE,
            isResolved = true
        )
        val log2 = ConflictLogEntity(
            id = 2,
            reminderId = reminderId,
            conflictedAt = now.minusHours(1),
            resolutionStrategy = ResolutionStrategy.MANUAL,
            localData = "{\"title\":\"로컬2\"}",
            remoteData = "{\"title\":\"원격2\"}",
            chosenData = ChosenDataSource.LOCAL,
            isResolved = false
        )
        whenever(dao.getConflictLogsByReminderId(reminderId)).thenReturn(listOf(log1, log2))

        // When
        val result = dao.getConflictLogsByReminderId(reminderId)

        // Then
        verify(dao).getConflictLogsByReminderId(reminderId)
        assertEquals(2, result.size)
        assertEquals(reminderId, result[0].reminderId)
        assertEquals(reminderId, result[1].reminderId)
    }

    /** insertConflictLog는 충돌 로그를 삽입하고 ID를 반환한다 */
    @Test
    fun testInsertConflictLogInsertsLogAndReturnsId() = runTest {
        // Given
        val log = ConflictLogEntity(
            reminderId = 100,
            resolutionStrategy = ResolutionStrategy.MANUAL,
            localData = "{\"title\":\"로컬\"}",
            remoteData = "{\"title\":\"원격\"}",
            chosenData = ChosenDataSource.LOCAL,
            isResolved = false
        )
        val insertedId = 5L
        whenever(dao.insertConflictLog(log)).thenReturn(insertedId)

        // When
        val result = dao.insertConflictLog(log)

        // Then
        verify(dao).insertConflictLog(log)
        assertEquals(insertedId, result)
    }

    /** updateConflictLog는 충돌 로그를 업데이트한다 */
    @Test
    fun testUpdateConflictLogUpdatesLog() = runTest {
        // Given
        val log = ConflictLogEntity(
            id = 1,
            reminderId = 100,
            resolutionStrategy = ResolutionStrategy.MANUAL,
            localData = "{\"title\":\"로컬\"}",
            remoteData = "{\"title\":\"원격\"}",
            chosenData = ChosenDataSource.LOCAL,
            isResolved = true,
            resolvedAt = LocalDateTime.now()
        )

        // When
        dao.updateConflictLog(log)

        // Then
        verify(dao).updateConflictLog(log)
    }

    /** deleteConflictLog는 충돌 로그를 삭제한다 */
    @Test
    fun testDeleteConflictLogDeletesLog() = runTest {
        // Given
        val log = ConflictLogEntity(
            id = 1,
            reminderId = 100,
            resolutionStrategy = ResolutionStrategy.MANUAL,
            localData = "{\"title\":\"로컬\"}",
            remoteData = "{\"title\":\"원격\"}",
            chosenData = ChosenDataSource.LOCAL,
            isResolved = true
        )

        // When
        dao.deleteConflictLog(log)

        // Then
        verify(dao).deleteConflictLog(log)
    }

    /** deleteOldConflictLogs는 기준 날짜 이전의 로그를 삭제한다 */
    @Test
    fun testDeleteOldConflictLogsDeletesLogsBeforeCutoffDate() = runTest {
        // Given
        val cutoffDate = LocalDateTime.now().minusDays(30)

        // When
        dao.deleteOldConflictLogs(cutoffDate)

        // Then
        verify(dao).deleteOldConflictLogs(cutoffDate)
    }

    /** deleteAllConflictLogs는 모든 충돌 로그를 삭제한다 */
    @Test
    fun testDeleteAllConflictLogsDeletesAllLogs() = runTest {
        // When
        dao.deleteAllConflictLogs()

        // Then
        verify(dao).deleteAllConflictLogs()
    }

    /** getUnresolvedConflictCount는 미해결 충돌 개수를 반환한다 */
    @Test
    fun testGetUnresolvedConflictCountReturnsUnresolvedCount() = runTest {
        // Given
        val unresolvedCount = 3
        whenever(dao.getUnresolvedConflictCount()).thenReturn(flowOf(unresolvedCount))

        // When
        val flow = dao.getUnresolvedConflictCount()

        // Then
        verify(dao).getUnresolvedConflictCount()
        flow.collect { count ->
            assertEquals(unresolvedCount, count)
        }
    }

    /** insertConflictLog는 REPLACE 전략으로 동일 ID 로그를 덮어쓴다 */
    @Test
    fun testInsertConflictLogWithReplaceStrategyOverwritesExistingLog() = runTest {
        // Given
        val existingLog = ConflictLogEntity(
            id = 1,
            reminderId = 100,
            resolutionStrategy = ResolutionStrategy.MANUAL,
            localData = "{\"title\":\"기존\"}",
            remoteData = "{\"title\":\"원격\"}",
            chosenData = ChosenDataSource.LOCAL,
            isResolved = false
        )
        whenever(dao.insertConflictLog(existingLog)).thenReturn(1L)

        // When
        val result = dao.insertConflictLog(existingLog)

        // Then
        verify(dao).insertConflictLog(existingLog)
        assertEquals(1L, result)
    }

    /** getConflictLogsByReminderId는 존재하지 않는 리마인더 ID로 조회 시 빈 리스트를 반환한다 */
    @Test
    fun testGetConflictLogsByReminderIdReturnsEmptyListForNonExistentReminder() = runTest {
        // Given
        val nonExistentReminderId = 999L
        whenever(dao.getConflictLogsByReminderId(nonExistentReminderId)).thenReturn(emptyList())

        // When
        val result = dao.getConflictLogsByReminderId(nonExistentReminderId)

        // Then
        verify(dao).getConflictLogsByReminderId(nonExistentReminderId)
        assertEquals(0, result.size)
    }

    /** getAllConflictLogs는 로그가 없을 때 빈 리스트를 반환한다 */
    @Test
    fun testGetAllConflictLogsReturnsEmptyListWhenNoLogs() = runTest {
        // Given
        whenever(dao.getAllConflictLogs()).thenReturn(flowOf(emptyList()))

        // When
        val flow = dao.getAllConflictLogs()

        // Then
        verify(dao).getAllConflictLogs()
        flow.collect { logs ->
            assertEquals(0, logs.size)
        }
    }

    /** getUnresolvedConflicts는 모든 충돌이 해결되었을 때 빈 리스트를 반환한다 */
    @Test
    fun testGetUnresolvedConflictsReturnsEmptyListWhenAllResolved() = runTest {
        // Given
        whenever(dao.getUnresolvedConflicts()).thenReturn(flowOf(emptyList()))

        // When
        val flow = dao.getUnresolvedConflicts()

        // Then
        verify(dao).getUnresolvedConflicts()
        flow.collect { logs ->
            assertEquals(0, logs.size)
        }
    }

    /** getUnresolvedConflictCount는 미해결 충돌이 없을 때 0을 반환한다 */
    @Test
    fun testGetUnresolvedConflictCountReturnsZeroWhenNoUnresolvedConflicts() = runTest {
        // Given
        whenever(dao.getUnresolvedConflictCount()).thenReturn(flowOf(0))

        // When
        val flow = dao.getUnresolvedConflictCount()

        // Then
        verify(dao).getUnresolvedConflictCount()
        flow.collect { count ->
            assertEquals(0, count)
        }
    }

    /** insertConflictLog는 모든 ResolutionStrategy 타입을 지원한다 */
    @Test
    fun testInsertConflictLogSupportsAllResolutionStrategies() = runTest {
        // Given
        val strategies = listOf(
            ResolutionStrategy.LAST_WRITE_WINS,
            ResolutionStrategy.MANUAL,
            ResolutionStrategy.FIELD_LEVEL_MERGE
        )

        strategies.forEachIndexed { index, strategy ->
            val log = ConflictLogEntity(
                reminderId = 100L + index,
                resolutionStrategy = strategy,
                localData = "{\"title\":\"로컬\"}",
                remoteData = "{\"title\":\"원격\"}",
                chosenData = ChosenDataSource.MERGED,
                isResolved = false
            )
            whenever(dao.insertConflictLog(log)).thenReturn((index + 1).toLong())

            // When
            val result = dao.insertConflictLog(log)

            // Then
            verify(dao).insertConflictLog(log)
            assertEquals((index + 1).toLong(), result)
        }
    }

    /** insertConflictLog는 모든 ChosenDataSource 타입을 지원한다 */
    @Test
    fun testInsertConflictLogSupportsAllChosenDataSources() = runTest {
        // Given
        val dataSources = listOf(
            ChosenDataSource.LOCAL,
            ChosenDataSource.REMOTE,
            ChosenDataSource.MERGED
        )

        dataSources.forEachIndexed { index, dataSource ->
            val log = ConflictLogEntity(
                reminderId = 200L + index,
                resolutionStrategy = ResolutionStrategy.MANUAL,
                localData = "{\"title\":\"로컬\"}",
                remoteData = "{\"title\":\"원격\"}",
                chosenData = dataSource,
                isResolved = false
            )
            whenever(dao.insertConflictLog(log)).thenReturn((index + 1).toLong())

            // When
            val result = dao.insertConflictLog(log)

            // Then
            verify(dao).insertConflictLog(log)
            assertEquals((index + 1).toLong(), result)
        }
    }

    /** updateConflictLog는 해결 시 resolvedAt 시간을 포함하여 업데이트한다 */
    @Test
    fun testUpdateConflictLogUpdatesResolvedAtWhenResolved() = runTest {
        // Given
        val resolvedAt = LocalDateTime.now()
        val log = ConflictLogEntity(
            id = 1,
            reminderId = 100,
            resolutionStrategy = ResolutionStrategy.MANUAL,
            localData = "{\"title\":\"로컬\"}",
            remoteData = "{\"title\":\"원격\"}",
            chosenData = ChosenDataSource.LOCAL,
            isResolved = true,
            resolvedAt = resolvedAt
        )

        // When
        dao.updateConflictLog(log)

        // Then
        verify(dao).updateConflictLog(argThat { l ->
            l.id == log.id &&
            l.isResolved == true &&
            l.resolvedAt == resolvedAt
        })
    }

    /** deleteOldConflictLogs는 30일 이상 된 로그만 삭제한다 */
    @Test
    fun testDeleteOldConflictLogsDeletesOnlyLogsOlderThan30Days() = runTest {
        // Given
        val thirtyDaysAgo = LocalDateTime.now().minusDays(30)

        // When
        dao.deleteOldConflictLogs(thirtyDaysAgo)

        // Then
        verify(dao).deleteOldConflictLogs(thirtyDaysAgo)
    }
}
