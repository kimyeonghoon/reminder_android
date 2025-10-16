package com.reminder.data.repository

import com.reminder.data.dao.ReminderDao
import com.reminder.data.entity.ReminderEntity
import com.reminder.network.NetworkMonitor
import com.reminder.sync.OfflineQueue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import org.junit.Assert.*

/**
 * OfflineFirstRepository 테스트
 *
 * 오프라인 우선 전략의 모든 메서드를 검증합니다.
 * - AAA 패턴 (Given-When-Then)
 * - Mockito를 사용한 단위 테스트
 */
@OptIn(ExperimentalCoroutinesApi::class)
class OfflineFirstRepositoryTest {

    private lateinit var reminderDao: ReminderDao
    private lateinit var offlineQueue: OfflineQueue
    private lateinit var networkMonitor: NetworkMonitor
    private lateinit var repository: OfflineFirstRepository

    @Before
    fun setup() {
        reminderDao = mock()
        offlineQueue = mock()
        networkMonitor = mock()
        repository = OfflineFirstRepository(reminderDao, offlineQueue, networkMonitor)
    }

    /** insertReminderOfflineFirst는 로컬 DB에 저장하고 오프라인 큐에 추가한다 */
    @Test
    fun testInsertReminderOfflineFirstSavesToLocalAndEnqueues() = runTest {
        // Given
        val reminder = ReminderEntity(id = 0, title = "테스트 리마인더")
        val insertedId = 1L
        whenever(reminderDao.insertReminder(reminder)).thenReturn(insertedId)

        // When
        val result = repository.insertReminderOfflineFirst(reminder)

        // Then
        verify(reminderDao).insertReminder(reminder)
        verify(offlineQueue).enqueueInsert(insertedId)
        assertEquals(insertedId, result)
    }

    /** insertReminderOfflineFirst는 로컬 DB 저장 후 큐에 추가하는 순서를 지킨다 */
    @Test
    fun testInsertReminderOfflineFirstMaintainsCorrectOrder() = runTest {
        // Given
        val reminder = ReminderEntity(id = 0, title = "테스트 리마인더")
        val insertedId = 2L
        whenever(reminderDao.insertReminder(reminder)).thenReturn(insertedId)
        val inOrder = inOrder(reminderDao, offlineQueue)

        // When
        repository.insertReminderOfflineFirst(reminder)

        // Then
        inOrder.verify(reminderDao).insertReminder(reminder)
        inOrder.verify(offlineQueue).enqueueInsert(insertedId)
    }

    /** insertReminderOfflineFirst는 반환된 ID를 큐에 전달한다 */
    @Test
    fun testInsertReminderOfflineFirstPassesCorrectIdToQueue() = runTest {
        // Given
        val reminder = ReminderEntity(id = 0, title = "테스트 리마인더")
        val insertedId = 100L
        whenever(reminderDao.insertReminder(reminder)).thenReturn(insertedId)

        // When
        repository.insertReminderOfflineFirst(reminder)

        // Then
        verify(offlineQueue).enqueueInsert(eq(100L))
    }

    /** updateReminderOfflineFirst는 로컬 DB를 업데이트하고 오프라인 큐에 추가한다 */
    @Test
    fun testUpdateReminderOfflineFirstUpdatesLocalAndEnqueues() = runTest {
        // Given
        val reminder = ReminderEntity(id = 1, title = "수정된 리마인더")

        // When
        repository.updateReminderOfflineFirst(reminder)

        // Then
        verify(reminderDao).updateReminder(reminder)
        verify(offlineQueue).enqueueUpdate(reminder.id)
    }

    /** updateReminderOfflineFirst는 로컬 DB 업데이트 후 큐에 추가하는 순서를 지킨다 */
    @Test
    fun testUpdateReminderOfflineFirstMaintainsCorrectOrder() = runTest {
        // Given
        val reminder = ReminderEntity(id = 1, title = "수정된 리마인더")
        val inOrder = inOrder(reminderDao, offlineQueue)

        // When
        repository.updateReminderOfflineFirst(reminder)

        // Then
        inOrder.verify(reminderDao).updateReminder(reminder)
        inOrder.verify(offlineQueue).enqueueUpdate(reminder.id)
    }

    /** updateReminderOfflineFirst는 올바른 리마인더 ID를 큐에 전달한다 */
    @Test
    fun testUpdateReminderOfflineFirstPassesCorrectIdToQueue() = runTest {
        // Given
        val reminderId = 42L
        val reminder = ReminderEntity(id = reminderId, title = "수정된 리마인더")

        // When
        repository.updateReminderOfflineFirst(reminder)

        // Then
        verify(offlineQueue).enqueueUpdate(eq(42L))
    }

    /** deleteReminderOfflineFirst는 로컬 DB에서 삭제하고 오프라인 큐에 추가한다 */
    @Test
    fun testDeleteReminderOfflineFirstDeletesFromLocalAndEnqueues() = runTest {
        // Given
        val reminder = ReminderEntity(id = 1, title = "삭제할 리마인더")

        // When
        repository.deleteReminderOfflineFirst(reminder)

        // Then
        verify(reminderDao).deleteReminder(reminder)
        verify(offlineQueue).enqueueDelete(reminder.id)
    }

    /** deleteReminderOfflineFirst는 로컬 DB 삭제 후 큐에 추가하는 순서를 지킨다 */
    @Test
    fun testDeleteReminderOfflineFirstMaintainsCorrectOrder() = runTest {
        // Given
        val reminder = ReminderEntity(id = 1, title = "삭제할 리마인더")
        val inOrder = inOrder(reminderDao, offlineQueue)

        // When
        repository.deleteReminderOfflineFirst(reminder)

        // Then
        inOrder.verify(reminderDao).deleteReminder(reminder)
        inOrder.verify(offlineQueue).enqueueDelete(reminder.id)
    }

    /** deleteReminderOfflineFirst는 올바른 리마인더 ID를 큐에 전달한다 */
    @Test
    fun testDeleteReminderOfflineFirstPassesCorrectIdToQueue() = runTest {
        // Given
        val reminderId = 99L
        val reminder = ReminderEntity(id = reminderId, title = "삭제할 리마인더")

        // When
        repository.deleteReminderOfflineFirst(reminder)

        // Then
        verify(offlineQueue).enqueueDelete(eq(99L))
    }

    /** toggleReminderCompletionOfflineFirst는 완료 상태를 토글하고 오프라인 큐에 추가한다 */
    @Test
    fun testToggleReminderCompletionOfflineFirstTogglesAndEnqueues() = runTest {
        // Given
        val reminderId = 1L

        // When
        repository.toggleReminderCompletionOfflineFirst(reminderId)

        // Then
        verify(reminderDao).toggleReminderCompletion(eq(reminderId), any(), any())
        verify(offlineQueue).enqueueUpdate(reminderId)
    }

    /** toggleReminderCompletionOfflineFirst는 토글 후 큐에 추가하는 순서를 지킨다 */
    @Test
    fun testToggleReminderCompletionOfflineFirstMaintainsCorrectOrder() = runTest {
        // Given
        val reminderId = 1L
        val inOrder = inOrder(reminderDao, offlineQueue)

        // When
        repository.toggleReminderCompletionOfflineFirst(reminderId)

        // Then
        inOrder.verify(reminderDao).toggleReminderCompletion(eq(reminderId), any(), any())
        inOrder.verify(offlineQueue).enqueueUpdate(reminderId)
    }

    /** toggleReminderCompletionOfflineFirst는 올바른 리마인더 ID를 전달한다 */
    @Test
    fun testToggleReminderCompletionOfflineFirstPassesCorrectId() = runTest {
        // Given
        val reminderId = 55L

        // When
        repository.toggleReminderCompletionOfflineFirst(reminderId)

        // Then
        verify(reminderDao).toggleReminderCompletion(eq(55L), any(), any())
        verify(offlineQueue).enqueueUpdate(eq(55L))
    }

    /** getPendingActionsCount는 대기 중인 작업 개수를 반환한다 */
    @Test
    fun testGetPendingActionsCountReturnsCorrectCount() = runTest {
        // Given
        val pendingCount = 5
        whenever(offlineQueue.pendingActionsCount).thenReturn(flowOf(pendingCount))

        // When
        val result = repository.getPendingActionsCount()

        // Then
        assertEquals(pendingCount, result)
    }

    /** getPendingActionsCount는 대기 작업이 없을 때 0을 반환한다 */
    @Test
    fun testGetPendingActionsCountReturnsZeroWhenNoPendingActions() = runTest {
        // Given
        whenever(offlineQueue.pendingActionsCount).thenReturn(flowOf(0))

        // When
        val result = repository.getPendingActionsCount()

        // Then
        assertEquals(0, result)
    }

    /** getPendingActionsCount는 Flow의 첫 번째 값을 사용한다 */
    @Test
    fun testGetPendingActionsCountUsesFirstFlowValue() = runTest {
        // Given
        val pendingCount = 10
        whenever(offlineQueue.pendingActionsCount).thenReturn(flowOf(pendingCount, 20, 30))

        // When
        val result = repository.getPendingActionsCount()

        // Then
        assertEquals(pendingCount, result)
    }

    /** isNetworkAvailable는 네트워크 연결 상태를 반환한다 */
    @Test
    fun testIsNetworkAvailableReturnsTrueWhenConnected() = runTest {
        // Given
        whenever(networkMonitor.isCurrentlyConnected()).thenReturn(true)

        // When
        val result = repository.isNetworkAvailable()

        // Then
        assertTrue(result)
        verify(networkMonitor).isCurrentlyConnected()
    }

    /** isNetworkAvailable는 네트워크 미연결 시 false를 반환한다 */
    @Test
    fun testIsNetworkAvailableReturnsFalseWhenDisconnected() = runTest {
        // Given
        whenever(networkMonitor.isCurrentlyConnected()).thenReturn(false)

        // When
        val result = repository.isNetworkAvailable()

        // Then
        assertFalse(result)
        verify(networkMonitor).isCurrentlyConnected()
    }

    /** 여러 리마인더를 연속으로 추가할 수 있다 */
    @Test
    fun testCanInsertMultipleRemindersSequentially() = runTest {
        // Given
        val reminder1 = ReminderEntity(id = 0, title = "리마인더 1")
        val reminder2 = ReminderEntity(id = 0, title = "리마인더 2")
        val reminder3 = ReminderEntity(id = 0, title = "리마인더 3")
        whenever(reminderDao.insertReminder(any())).thenReturn(1L, 2L, 3L)

        // When
        repository.insertReminderOfflineFirst(reminder1)
        repository.insertReminderOfflineFirst(reminder2)
        repository.insertReminderOfflineFirst(reminder3)

        // Then
        verify(reminderDao, times(3)).insertReminder(any())
        verify(offlineQueue, times(3)).enqueueInsert(any())
    }

    /** 추가, 수정, 삭제를 혼합하여 사용할 수 있다 */
    @Test
    fun testCanMixInsertUpdateDeleteOperations() = runTest {
        // Given
        val newReminder = ReminderEntity(id = 0, title = "신규 리마인더")
        val existingReminder = ReminderEntity(id = 1, title = "기존 리마인더")
        val reminderToDelete = ReminderEntity(id = 2, title = "삭제할 리마인더")
        whenever(reminderDao.insertReminder(newReminder)).thenReturn(1L)

        // When
        repository.insertReminderOfflineFirst(newReminder)
        repository.updateReminderOfflineFirst(existingReminder)
        repository.deleteReminderOfflineFirst(reminderToDelete)

        // Then
        verify(reminderDao).insertReminder(newReminder)
        verify(reminderDao).updateReminder(existingReminder)
        verify(reminderDao).deleteReminder(reminderToDelete)
        verify(offlineQueue).enqueueInsert(1L)
        verify(offlineQueue).enqueueUpdate(1L)
        verify(offlineQueue).enqueueDelete(2L)
    }

    /** 같은 리마인더를 여러 번 토글할 수 있다 */
    @Test
    fun testCanToggleSameReminderMultipleTimes() = runTest {
        // Given
        val reminderId = 1L

        // When
        repository.toggleReminderCompletionOfflineFirst(reminderId)
        repository.toggleReminderCompletionOfflineFirst(reminderId)
        repository.toggleReminderCompletionOfflineFirst(reminderId)

        // Then
        verify(reminderDao, times(3)).toggleReminderCompletion(eq(reminderId), any(), any())
        verify(offlineQueue, times(3)).enqueueUpdate(reminderId)
    }

    /** 네트워크 상태와 관계없이 로컬 DB에 저장된다 */
    @Test
    fun testInsertsToLocalDbRegardlessOfNetworkStatus() = runTest {
        // Given
        val reminder = ReminderEntity(id = 0, title = "테스트 리마인더")
        whenever(reminderDao.insertReminder(reminder)).thenReturn(1L)
        whenever(networkMonitor.isCurrentlyConnected()).thenReturn(false)

        // When
        repository.insertReminderOfflineFirst(reminder)

        // Then (네트워크 상태와 관계없이 로컬 DB 저장 및 큐 추가)
        verify(reminderDao).insertReminder(reminder)
        verify(offlineQueue).enqueueInsert(1L)
    }

    /** 대기 중인 작업 개수 조회는 큐의 상태를 반영한다 */
    @Test
    fun testPendingActionsCountReflectsQueueState() = runTest {
        // Given
        whenever(offlineQueue.pendingActionsCount).thenReturn(flowOf(3))

        // When
        val count = repository.getPendingActionsCount()

        // Then
        assertEquals(3, count)
    }
}
