package com.reminder.viewmodel

import com.reminder.analytics.AnalyticsHelper
import com.reminder.data.dao.SubTaskDao
import com.reminder.data.entity.SubTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.whenever

/**
 * SubTaskViewModel 테스트
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SubTaskViewModelTest {

    @get:Rule
    val mockitoRule: MockitoRule = MockitoJUnit.rule()

    private val testDispatcher = UnconfinedTestDispatcher()

    @Mock
    private lateinit var subTaskDao: SubTaskDao

    @Mock
    private lateinit var analyticsHelper: AnalyticsHelper

    private lateinit var viewModel: SubTaskViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = SubTaskViewModel(subTaskDao, analyticsHelper)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getSubTasks returns flow from dao`() {
        // given: 서브태스크 리스트
        val reminderId = 1L
        val subTasks = listOf(
            SubTask(id = 1, reminderId = reminderId, title = "Task 1", position = 0),
            SubTask(id = 2, reminderId = reminderId, title = "Task 2", position = 1)
        )
        whenever(subTaskDao.getSubTasksByReminderId(reminderId)).thenReturn(flowOf(subTasks))

        // when: getSubTasks 호출
        viewModel.getSubTasks(reminderId)

        // then: dao 메서드가 호출되고 Flow가 반환됨
        verify(subTaskDao).getSubTasksByReminderId(reminderId)
    }

    @Test
    fun `addSubTask inserts new subtask with correct position`() = runTest(testDispatcher) {
        // given: reminderId와 title
        val reminderId = 1L
        val title = "New Task"
        val currentCount = 2
        whenever(subTaskDao.getTotalSubTasksCount(reminderId)).thenReturn(currentCount)

        // when: addSubTask 호출
        viewModel.addSubTask(reminderId, title)
        testScheduler.advanceUntilIdle()

        // then: dao insert 메서드 호출 및 analytics 로깅
        val captor = argumentCaptor<SubTask>()
        verify(subTaskDao).insert(captor.capture())
        val insertedTask = captor.firstValue

        assert(insertedTask.reminderId == reminderId)
        assert(insertedTask.title == title)
        assert(insertedTask.position == currentCount) // position은 현재 개수와 같음
        verify(analyticsHelper).logSubtaskAdded()
    }

    @Test
    fun `toggleSubTaskCompletion updates subtask completion state`() = runTest(testDispatcher) {
        // given: 완료되지 않은 서브태스크
        val subTask = SubTask(
            id = 1,
            reminderId = 1L,
            title = "Task",
            isCompleted = false,
            position = 0
        )

        // when: toggleSubTaskCompletion 호출
        viewModel.toggleSubTaskCompletion(subTask)
        testScheduler.advanceUntilIdle()

        // then: isCompleted가 반전된 서브태스크가 업데이트됨
        val captor = argumentCaptor<SubTask>()
        verify(subTaskDao).update(captor.capture())
        val updatedTask = captor.firstValue

        assert(updatedTask.isCompleted == true)
    }

    @Test
    fun `deleteSubTask deletes subtask from dao`() = runTest(testDispatcher) {
        // given: 서브태스크
        val subTask = SubTask(id = 1, reminderId = 1L, title = "Task", position = 0)

        // when: deleteSubTask 호출
        viewModel.deleteSubTask(subTask)
        testScheduler.advanceUntilIdle()

        // then: dao delete 메서드 호출
        verify(subTaskDao).delete(subTask)
    }

    @Test
    fun `reorderSubTasks updates positions correctly`() = runTest(testDispatcher) {
        // given: 재정렬할 서브태스크 리스트
        val subTasks = listOf(
            SubTask(id = 1, reminderId = 1L, title = "Task 1", position = 2), // 원래 position: 2
            SubTask(id = 2, reminderId = 1L, title = "Task 2", position = 0), // 원래 position: 0
            SubTask(id = 3, reminderId = 1L, title = "Task 3", position = 1)  // 원래 position: 1
        )

        // when: reorderSubTasks 호출
        viewModel.reorderSubTasks(subTasks)
        testScheduler.advanceUntilIdle()

        // then: 새로운 순서대로 position이 업데이트됨
        val captor = argumentCaptor<List<SubTask>>()
        verify(subTaskDao).updateAll(captor.capture())
        val reorderedTasks = captor.firstValue

        assert(reorderedTasks[0].id == 1L && reorderedTasks[0].position == 0)
        assert(reorderedTasks[1].id == 2L && reorderedTasks[1].position == 1)
        assert(reorderedTasks[2].id == 3L && reorderedTasks[2].position == 2)
    }

    @Test
    fun `getSubTaskProgress returns completed and total count`() = runTest {
        // given: reminderId
        val reminderId = 1L
        val completedCount = 3
        val totalCount = 5
        whenever(subTaskDao.getCompletedSubTasksCount(reminderId)).thenReturn(completedCount)
        whenever(subTaskDao.getTotalSubTasksCount(reminderId)).thenReturn(totalCount)

        // when: getSubTaskProgress 호출
        val result = viewModel.getSubTaskProgress(reminderId)

        // then: (completed, total) Pair 반환
        assert(result.first == completedCount)
        assert(result.second == totalCount)
    }
}
