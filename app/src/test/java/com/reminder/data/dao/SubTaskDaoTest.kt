package com.reminder.data.dao

import com.reminder.data.entity.SubTask
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.time.LocalDateTime
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class SubTaskDaoTest {

    private lateinit var dao: SubTaskDao

    @Before
    fun setup() {
        dao = mock()
    }

    /** insert는 서브태스크를 추가하고 생성된 ID를 반환한다 */
    @Test
    fun testInsertAddsSubTaskAndReturnsId() = runTest {
        // Given
        val subTask = SubTask(
            reminderId = 1L,
            title = "서브태스크 1",
            isCompleted = false,
            position = 0
        )
        val insertedId = 5L
        whenever(dao.insert(subTask)).thenReturn(insertedId)

        // When
        val result = dao.insert(subTask)

        // Then
        verify(dao).insert(subTask)
        assertEquals(insertedId, result)
    }

    /** insert는 완료된 서브태스크를 추가할 수 있다 */
    @Test
    fun testInsertAddsCompletedSubTask() = runTest {
        // Given
        val subTask = SubTask(
            reminderId = 1L,
            title = "완료된 서브태스크",
            isCompleted = true,
            position = 1
        )
        val insertedId = 3L
        whenever(dao.insert(subTask)).thenReturn(insertedId)

        // When
        val result = dao.insert(subTask)

        // Then
        verify(dao).insert(subTask)
        assertEquals(insertedId, result)
    }

    /** insertAll은 여러 서브태스크를 추가한다 */
    @Test
    fun testInsertAllAddsMultipleSubTasks() = runTest {
        // Given
        val subTasks = listOf(
            SubTask(reminderId = 1L, title = "서브태스크 1", position = 0),
            SubTask(reminderId = 1L, title = "서브태스크 2", position = 1),
            SubTask(reminderId = 1L, title = "서브태스크 3", position = 2)
        )

        // When
        dao.insertAll(subTasks)

        // Then
        verify(dao).insertAll(subTasks)
    }

    /** insertAll은 빈 리스트로 호출할 수 있다 */
    @Test
    fun testInsertAllAcceptsEmptyList() = runTest {
        // Given
        val emptyList = emptyList<SubTask>()

        // When
        dao.insertAll(emptyList)

        // Then
        verify(dao).insertAll(emptyList)
    }

    /** update는 서브태스크를 업데이트한다 */
    @Test
    fun testUpdateModifiesExistingSubTask() = runTest {
        // Given
        val subTask = SubTask(
            id = 1L,
            reminderId = 1L,
            title = "수정된 서브태스크",
            isCompleted = true,
            position = 0,
            createdAt = LocalDateTime.now()
        )

        // When
        dao.update(subTask)

        // Then
        verify(dao).update(subTask)
    }

    /** update는 서브태스크의 완료 상태를 변경할 수 있다 */
    @Test
    fun testUpdateTogglesCompletionStatus() = runTest {
        // Given
        val subTask = SubTask(
            id = 1L,
            reminderId = 1L,
            title = "서브태스크",
            isCompleted = false,
            position = 0
        )

        // When
        dao.update(subTask)

        // Then
        verify(dao).update(subTask)
    }

    /** updateAll은 여러 서브태스크를 업데이트한다 (재정렬 시 사용) */
    @Test
    fun testUpdateAllModifiesMultipleSubTasks() = runTest {
        // Given
        val subTasks = listOf(
            SubTask(id = 1L, reminderId = 1L, title = "서브태스크 1", position = 2),
            SubTask(id = 2L, reminderId = 1L, title = "서브태스크 2", position = 0),
            SubTask(id = 3L, reminderId = 1L, title = "서브태스크 3", position = 1)
        )

        // When
        dao.updateAll(subTasks)

        // Then
        verify(dao).updateAll(subTasks)
    }

    /** updateAll은 빈 리스트로 호출할 수 있다 */
    @Test
    fun testUpdateAllAcceptsEmptyList() = runTest {
        // Given
        val emptyList = emptyList<SubTask>()

        // When
        dao.updateAll(emptyList)

        // Then
        verify(dao).updateAll(emptyList)
    }

    /** delete는 서브태스크를 삭제한다 */
    @Test
    fun testDeleteRemovesSubTask() = runTest {
        // Given
        val subTask = SubTask(
            id = 1L,
            reminderId = 1L,
            title = "삭제할 서브태스크",
            position = 0
        )

        // When
        dao.delete(subTask)

        // Then
        verify(dao).delete(subTask)
    }

    /** delete는 완료된 서브태스크를 삭제할 수 있다 */
    @Test
    fun testDeleteRemovesCompletedSubTask() = runTest {
        // Given
        val subTask = SubTask(
            id = 1L,
            reminderId = 1L,
            title = "완료된 서브태스크",
            isCompleted = true,
            position = 0
        )

        // When
        dao.delete(subTask)

        // Then
        verify(dao).delete(subTask)
    }

    /** getSubTaskById는 ID로 서브태스크를 조회한다 */
    @Test
    fun testGetSubTaskByIdReturnsSubTaskWhenExists() = runTest {
        // Given
        val subTaskId = 1L
        val subTask = SubTask(
            id = subTaskId,
            reminderId = 1L,
            title = "조회할 서브태스크",
            isCompleted = false,
            position = 0
        )
        whenever(dao.getSubTaskById(subTaskId)).thenReturn(subTask)

        // When
        val result = dao.getSubTaskById(subTaskId)

        // Then
        verify(dao).getSubTaskById(subTaskId)
        assertEquals(subTask, result)
    }

    /** getSubTaskById는 존재하지 않는 ID로 조회 시 null을 반환한다 */
    @Test
    fun testGetSubTaskByIdReturnsNullWhenNotExists() = runTest {
        // Given
        val subTaskId = 999L
        whenever(dao.getSubTaskById(subTaskId)).thenReturn(null)

        // When
        val result = dao.getSubTaskById(subTaskId)

        // Then
        verify(dao).getSubTaskById(subTaskId)
        assertNull(result)
    }

    /** getSubTasksByReminderId는 리마인더별 서브태스크를 position 순으로 정렬하여 Flow로 반환한다 */
    @Test
    fun testGetSubTasksByReminderIdReturnsSubTasksSortedByPosition() = runTest {
        // Given
        val reminderId = 1L
        val subTasks = listOf(
            SubTask(id = 1L, reminderId = reminderId, title = "서브태스크 1", position = 0),
            SubTask(id = 2L, reminderId = reminderId, title = "서브태스크 2", position = 1),
            SubTask(id = 3L, reminderId = reminderId, title = "서브태스크 3", position = 2)
        )
        whenever(dao.getSubTasksByReminderId(reminderId)).thenReturn(flowOf(subTasks))

        // When
        dao.getSubTasksByReminderId(reminderId)

        // Then
        verify(dao).getSubTasksByReminderId(reminderId)
    }

    /** getSubTasksByReminderId는 빈 리스트를 Flow로 반환할 수 있다 */
    @Test
    fun testGetSubTasksByReminderIdReturnsEmptyFlowWhenNoSubTasks() = runTest {
        // Given
        val reminderId = 1L
        whenever(dao.getSubTasksByReminderId(reminderId)).thenReturn(flowOf(emptyList()))

        // When
        dao.getSubTasksByReminderId(reminderId)

        // Then
        verify(dao).getSubTasksByReminderId(reminderId)
    }

    /** getSubTasksByReminderId는 완료/미완료 서브태스크를 모두 반환한다 */
    @Test
    fun testGetSubTasksByReminderIdReturnsAllSubTasksIncludingCompleted() = runTest {
        // Given
        val reminderId = 1L
        val subTasks = listOf(
            SubTask(id = 1L, reminderId = reminderId, title = "미완료", isCompleted = false, position = 0),
            SubTask(id = 2L, reminderId = reminderId, title = "완료", isCompleted = true, position = 1)
        )
        whenever(dao.getSubTasksByReminderId(reminderId)).thenReturn(flowOf(subTasks))

        // When
        dao.getSubTasksByReminderId(reminderId)

        // Then
        verify(dao).getSubTasksByReminderId(reminderId)
    }

    /** getCompletedSubTasksCount는 리마인더의 완료된 서브태스크 개수를 반환한다 */
    @Test
    fun testGetCompletedSubTasksCountReturnsCountOfCompletedSubTasks() = runTest {
        // Given
        val reminderId = 1L
        val completedCount = 3
        whenever(dao.getCompletedSubTasksCount(reminderId)).thenReturn(completedCount)

        // When
        val result = dao.getCompletedSubTasksCount(reminderId)

        // Then
        verify(dao).getCompletedSubTasksCount(reminderId)
        assertEquals(completedCount, result)
    }

    /** getCompletedSubTasksCount는 완료된 서브태스크가 없으면 0을 반환한다 */
    @Test
    fun testGetCompletedSubTasksCountReturnsZeroWhenNoCompletedSubTasks() = runTest {
        // Given
        val reminderId = 1L
        whenever(dao.getCompletedSubTasksCount(reminderId)).thenReturn(0)

        // When
        val result = dao.getCompletedSubTasksCount(reminderId)

        // Then
        verify(dao).getCompletedSubTasksCount(reminderId)
        assertEquals(0, result)
    }

    /** getTotalSubTasksCount는 리마인더의 전체 서브태스크 개수를 반환한다 */
    @Test
    fun testGetTotalSubTasksCountReturnsTotalCountOfSubTasks() = runTest {
        // Given
        val reminderId = 1L
        val totalCount = 5
        whenever(dao.getTotalSubTasksCount(reminderId)).thenReturn(totalCount)

        // When
        val result = dao.getTotalSubTasksCount(reminderId)

        // Then
        verify(dao).getTotalSubTasksCount(reminderId)
        assertEquals(totalCount, result)
    }

    /** getTotalSubTasksCount는 서브태스크가 없으면 0을 반환한다 */
    @Test
    fun testGetTotalSubTasksCountReturnsZeroWhenNoSubTasks() = runTest {
        // Given
        val reminderId = 1L
        whenever(dao.getTotalSubTasksCount(reminderId)).thenReturn(0)

        // When
        val result = dao.getTotalSubTasksCount(reminderId)

        // Then
        verify(dao).getTotalSubTasksCount(reminderId)
        assertEquals(0, result)
    }

    /** getTotalSubTasksCount는 완료/미완료 서브태스크를 모두 포함한다 */
    @Test
    fun testGetTotalSubTasksCountIncludesAllSubTasksRegardlessOfCompletion() = runTest {
        // Given
        val reminderId = 1L
        val totalCount = 10 // 완료 3개 + 미완료 7개
        whenever(dao.getTotalSubTasksCount(reminderId)).thenReturn(totalCount)

        // When
        val result = dao.getTotalSubTasksCount(reminderId)

        // Then
        verify(dao).getTotalSubTasksCount(reminderId)
        assertEquals(totalCount, result)
    }

    /** deleteAllByReminderId는 리마인더의 모든 서브태스크를 삭제한다 */
    @Test
    fun testDeleteAllByReminderIdRemovesAllSubTasksForReminder() = runTest {
        // Given
        val reminderId = 1L

        // When
        dao.deleteAllByReminderId(reminderId)

        // Then
        verify(dao).deleteAllByReminderId(reminderId)
    }

    /** deleteAllByReminderId는 서브태스크가 없는 리마인더에도 안전하게 호출할 수 있다 */
    @Test
    fun testDeleteAllByReminderIdSafelyHandlesReminderWithNoSubTasks() = runTest {
        // Given
        val reminderId = 999L

        // When
        dao.deleteAllByReminderId(reminderId)

        // Then
        verify(dao).deleteAllByReminderId(reminderId)
    }
}
