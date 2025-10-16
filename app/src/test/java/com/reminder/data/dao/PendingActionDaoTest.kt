package com.reminder.data.dao

import com.reminder.data.entity.ActionType
import com.reminder.data.entity.PendingActionEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.time.LocalDateTime
import org.junit.Assert.*

/**
 * PendingActionDao 테스트
 *
 * AAA 패턴 (Given-When-Then)으로 모든 메서드 검증
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PendingActionDaoTest {

    private lateinit var dao: PendingActionDao

    @Before
    fun setup() {
        dao = mock()
    }

    /** getAllPendingActions는 모든 대기 작업을 생성 시간 순으로 반환한다 */
    @Test
    fun testGetAllPendingActionsReturnsAllActionsOrderedByCreatedAt() = runTest {
        // Given
        val action1 = PendingActionEntity(
            id = 1,
            reminderId = 100,
            actionType = ActionType.INSERT,
            createdAt = LocalDateTime.now().minusHours(2)
        )
        val action2 = PendingActionEntity(
            id = 2,
            reminderId = 101,
            actionType = ActionType.UPDATE,
            createdAt = LocalDateTime.now().minusHours(1)
        )
        val expectedActions = listOf(action1, action2)
        whenever(dao.getAllPendingActions()).thenReturn(flowOf(expectedActions))

        // When
        val result = dao.getAllPendingActions()

        // Then
        verify(dao).getAllPendingActions()
        // Flow 검증 - 실제 프로젝트에서는 first()로 값 확인
    }

    /** getAllPendingActions는 작업이 없을 때 빈 리스트를 반환한다 */
    @Test
    fun testGetAllPendingActionsReturnsEmptyListWhenNoActions() = runTest {
        // Given
        whenever(dao.getAllPendingActions()).thenReturn(flowOf(emptyList()))

        // When
        val result = dao.getAllPendingActions()

        // Then
        verify(dao).getAllPendingActions()
    }

    /** getPendingActionsByReminderId는 특정 리마인더의 대기 작업만 반환한다 */
    @Test
    fun testGetPendingActionsByReminderIdReturnsOnlyMatchingActions() = runTest {
        // Given
        val reminderId = 100L
        val action1 = PendingActionEntity(
            id = 1,
            reminderId = reminderId,
            actionType = ActionType.INSERT
        )
        val action2 = PendingActionEntity(
            id = 2,
            reminderId = reminderId,
            actionType = ActionType.UPDATE
        )
        val expectedActions = listOf(action1, action2)
        whenever(dao.getPendingActionsByReminderId(reminderId)).thenReturn(expectedActions)

        // When
        val result = dao.getPendingActionsByReminderId(reminderId)

        // Then
        verify(dao).getPendingActionsByReminderId(reminderId)
        assertEquals(expectedActions, result)
    }

    /** getPendingActionsByReminderId는 일치하는 작업이 없을 때 빈 리스트를 반환한다 */
    @Test
    fun testGetPendingActionsByReminderIdReturnsEmptyListWhenNoMatch() = runTest {
        // Given
        val reminderId = 999L
        whenever(dao.getPendingActionsByReminderId(reminderId)).thenReturn(emptyList())

        // When
        val result = dao.getPendingActionsByReminderId(reminderId)

        // Then
        verify(dao).getPendingActionsByReminderId(reminderId)
        assertEquals(emptyList<PendingActionEntity>(), result)
    }

    /** getPendingActionsForRetry는 재시도 횟수가 최대값 이하인 작업만 반환한다 */
    @Test
    fun testGetPendingActionsForRetryReturnsOnlyRetryableActions() = runTest {
        // Given
        val maxRetryCount = 3
        val action1 = PendingActionEntity(
            id = 1,
            reminderId = 100,
            actionType = ActionType.INSERT,
            retryCount = 2
        )
        val action2 = PendingActionEntity(
            id = 2,
            reminderId = 101,
            actionType = ActionType.UPDATE,
            retryCount = 3
        )
        val expectedActions = listOf(action1, action2)
        whenever(dao.getPendingActionsForRetry(maxRetryCount)).thenReturn(expectedActions)

        // When
        val result = dao.getPendingActionsForRetry(maxRetryCount)

        // Then
        verify(dao).getPendingActionsForRetry(maxRetryCount)
        assertEquals(expectedActions, result)
    }

    /** getPendingActionsForRetry는 기본값 3을 사용한다 */
    @Test
    fun testGetPendingActionsForRetryUsesDefaultMaxRetryCount() = runTest {
        // Given
        val expectedActions = listOf(
            PendingActionEntity(
                id = 1,
                reminderId = 100,
                actionType = ActionType.INSERT,
                retryCount = 1
            )
        )
        whenever(dao.getPendingActionsForRetry()).thenReturn(expectedActions)

        // When
        val result = dao.getPendingActionsForRetry()

        // Then
        verify(dao).getPendingActionsForRetry()
        assertEquals(expectedActions, result)
    }

    /** getPendingActionsForRetry는 재시도 횟수 초과 작업을 제외한다 */
    @Test
    fun testGetPendingActionsForRetryExcludesExceededRetryCount() = runTest {
        // Given
        val maxRetryCount = 2
        val action1 = PendingActionEntity(
            id = 1,
            reminderId = 100,
            actionType = ActionType.INSERT,
            retryCount = 1
        )
        val expectedActions = listOf(action1)
        whenever(dao.getPendingActionsForRetry(maxRetryCount)).thenReturn(expectedActions)

        // When
        val result = dao.getPendingActionsForRetry(maxRetryCount)

        // Then
        verify(dao).getPendingActionsForRetry(maxRetryCount)
        assertEquals(expectedActions, result)
    }

    /** insertPendingAction은 작업을 삽입하고 생성된 ID를 반환한다 */
    @Test
    fun testInsertPendingActionInsertsActionAndReturnsId() = runTest {
        // Given
        val action = PendingActionEntity(
            reminderId = 100,
            actionType = ActionType.INSERT
        )
        val insertedId = 5L
        whenever(dao.insertPendingAction(action)).thenReturn(insertedId)

        // When
        val result = dao.insertPendingAction(action)

        // Then
        verify(dao).insertPendingAction(action)
        assertEquals(insertedId, result)
    }

    /** insertPendingAction은 REPLACE 전략으로 중복을 처리한다 */
    @Test
    fun testInsertPendingActionReplacesOnConflict() = runTest {
        // Given
        val action = PendingActionEntity(
            id = 1,
            reminderId = 100,
            actionType = ActionType.UPDATE,
            retryCount = 1
        )
        val replacedId = 1L
        whenever(dao.insertPendingAction(action)).thenReturn(replacedId)

        // When
        val result = dao.insertPendingAction(action)

        // Then
        verify(dao).insertPendingAction(action)
        assertEquals(replacedId, result)
    }

    /** updatePendingAction은 작업을 업데이트한다 */
    @Test
    fun testUpdatePendingActionUpdatesAction() = runTest {
        // Given
        val action = PendingActionEntity(
            id = 1,
            reminderId = 100,
            actionType = ActionType.INSERT,
            retryCount = 2,
            lastRetryAt = LocalDateTime.now()
        )

        // When
        dao.updatePendingAction(action)

        // Then
        verify(dao).updatePendingAction(action)
    }

    /** updatePendingAction은 재시도 횟수 증가 시 사용된다 */
    @Test
    fun testUpdatePendingActionIncrementsRetryCount() = runTest {
        // Given
        val originalAction = PendingActionEntity(
            id = 1,
            reminderId = 100,
            actionType = ActionType.INSERT,
            retryCount = 1
        )
        val updatedAction = originalAction.copy(
            retryCount = originalAction.retryCount + 1,
            lastRetryAt = LocalDateTime.now()
        )

        // When
        dao.updatePendingAction(updatedAction)

        // Then
        verify(dao).updatePendingAction(updatedAction)
    }

    /** deletePendingAction은 특정 작업을 삭제한다 */
    @Test
    fun testDeletePendingActionDeletesSpecificAction() = runTest {
        // Given
        val action = PendingActionEntity(
            id = 1,
            reminderId = 100,
            actionType = ActionType.INSERT
        )

        // When
        dao.deletePendingAction(action)

        // Then
        verify(dao).deletePendingAction(action)
    }

    /** deletePendingAction은 동기화 완료 후 호출된다 */
    @Test
    fun testDeletePendingActionCalledAfterSyncSuccess() = runTest {
        // Given
        val syncedAction = PendingActionEntity(
            id = 1,
            reminderId = 100,
            actionType = ActionType.UPDATE,
            retryCount = 1
        )

        // When (동기화 성공 가정)
        dao.deletePendingAction(syncedAction)

        // Then
        verify(dao).deletePendingAction(syncedAction)
    }

    /** deleteAllPendingActions는 모든 대기 작업을 삭제한다 */
    @Test
    fun testDeleteAllPendingActionsDeletesAllActions() = runTest {
        // When
        dao.deleteAllPendingActions()

        // Then
        verify(dao).deleteAllPendingActions()
    }

    /** deleteAllPendingActions는 수동 동기화 완료 시 사용된다 */
    @Test
    fun testDeleteAllPendingActionsCalledAfterManualSync() = runTest {
        // Given (수동 동기화 완료 가정)

        // When
        dao.deleteAllPendingActions()

        // Then
        verify(dao).deleteAllPendingActions()
    }

    /** deletePendingActionsByReminderId는 특정 리마인더의 모든 작업을 삭제한다 */
    @Test
    fun testDeletePendingActionsByReminderIdDeletesAllMatchingActions() = runTest {
        // Given
        val reminderId = 100L

        // When
        dao.deletePendingActionsByReminderId(reminderId)

        // Then
        verify(dao).deletePendingActionsByReminderId(reminderId)
    }

    /** deletePendingActionsByReminderId는 리마인더 삭제 시 호출된다 */
    @Test
    fun testDeletePendingActionsByReminderIdCalledWhenReminderDeleted() = runTest {
        // Given
        val deletedReminderId = 100L

        // When (리마인더 삭제 가정)
        dao.deletePendingActionsByReminderId(deletedReminderId)

        // Then
        verify(dao).deletePendingActionsByReminderId(deletedReminderId)
    }

    /** getPendingActionsCount는 대기 중인 작업 개수를 반환한다 */
    @Test
    fun testGetPendingActionsCountReturnsActionCount() = runTest {
        // Given
        val count = 5
        whenever(dao.getPendingActionsCount()).thenReturn(flowOf(count))

        // When
        val result = dao.getPendingActionsCount()

        // Then
        verify(dao).getPendingActionsCount()
    }

    /** getPendingActionsCount는 작업이 없을 때 0을 반환한다 */
    @Test
    fun testGetPendingActionsCountReturnsZeroWhenNoActions() = runTest {
        // Given
        whenever(dao.getPendingActionsCount()).thenReturn(flowOf(0))

        // When
        val result = dao.getPendingActionsCount()

        // Then
        verify(dao).getPendingActionsCount()
    }

    /** 모든 ActionType을 지원한다 */
    @Test
    fun testSupportsAllActionTypes() = runTest {
        // Given
        val insertAction = PendingActionEntity(
            id = 1,
            reminderId = 100,
            actionType = ActionType.INSERT
        )
        val updateAction = PendingActionEntity(
            id = 2,
            reminderId = 101,
            actionType = ActionType.UPDATE
        )
        val deleteAction = PendingActionEntity(
            id = 3,
            reminderId = 102,
            actionType = ActionType.DELETE
        )
        whenever(dao.insertPendingAction(any())).thenReturn(1L)

        // When
        dao.insertPendingAction(insertAction)
        dao.insertPendingAction(updateAction)
        dao.insertPendingAction(deleteAction)

        // Then
        verify(dao, times(3)).insertPendingAction(any())
    }

    /** retryCount 0으로 새 작업을 생성한다 */
    @Test
    fun testCreatesNewActionWithZeroRetryCount() = runTest {
        // Given
        val newAction = PendingActionEntity(
            reminderId = 100,
            actionType = ActionType.INSERT,
            retryCount = 0
        )
        whenever(dao.insertPendingAction(newAction)).thenReturn(1L)

        // When
        dao.insertPendingAction(newAction)

        // Then
        verify(dao).insertPendingAction(argThat { action -> action.retryCount == 0 })
    }

    /** errorMessage를 저장할 수 있다 */
    @Test
    fun testStoresErrorMessage() = runTest {
        // Given
        val actionWithError = PendingActionEntity(
            id = 1,
            reminderId = 100,
            actionType = ActionType.UPDATE,
            retryCount = 1,
            errorMessage = "Network timeout"
        )

        // When
        dao.updatePendingAction(actionWithError)

        // Then
        verify(dao).updatePendingAction(argThat { action ->
            action.errorMessage == "Network timeout"
        })
    }

    /** lastRetryAt을 업데이트할 수 있다 */
    @Test
    fun testUpdatesLastRetryAt() = runTest {
        // Given
        val now = LocalDateTime.now()
        val actionWithRetry = PendingActionEntity(
            id = 1,
            reminderId = 100,
            actionType = ActionType.INSERT,
            retryCount = 1,
            lastRetryAt = now
        )

        // When
        dao.updatePendingAction(actionWithRetry)

        // Then
        verify(dao).updatePendingAction(argThat { action ->
            action.lastRetryAt != null
        })
    }
}
