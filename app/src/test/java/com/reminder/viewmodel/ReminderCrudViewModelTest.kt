package com.reminder.viewmodel

import com.reminder.analytics.AnalyticsHelper
import com.reminder.data.entity.Priority
import com.reminder.data.entity.ReminderEntity
import com.reminder.data.repository.ReminderRepository
import com.reminder.location.LocationManager
import com.reminder.notification.AlarmScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import java.time.LocalDateTime

/**
 * ReminderCrudViewModel 테스트
 *
 * AAA 패턴 (Given-When-Then)으로 핵심 CRUD 메서드 검증
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ReminderCrudViewModelTest {

    private lateinit var repository: ReminderRepository
    private lateinit var alarmScheduler: AlarmScheduler
    private lateinit var locationManager: LocationManager
    private lateinit var analyticsHelper: AnalyticsHelper
    private lateinit var viewModel: ReminderCrudViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mock(ReminderRepository::class.java)
        alarmScheduler = mock(AlarmScheduler::class.java)
        locationManager = mock(LocationManager::class.java)
        analyticsHelper = mock(AnalyticsHelper::class.java)

        // 기본 빈 리스트로 초기화
        `when`(repository.allReminders).thenReturn(flowOf(emptyList()))
        `when`(repository.activeReminders).thenReturn(flowOf(emptyList()))
        `when`(repository.completedReminders).thenReturn(flowOf(emptyList()))

        viewModel = ReminderCrudViewModel(repository, alarmScheduler, locationManager, analyticsHelper)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /** 초기 상태는 빈 리스트이다 */
    @Test
    fun initialStateIsEmptyList() {
        assertEquals(emptyList<ReminderEntity>(), viewModel.allReminders.value)
        assertEquals(emptyList<ReminderEntity>(), viewModel.activeReminders.value)
        assertEquals(emptyList<ReminderEntity>(), viewModel.completedReminders.value)
        assertNull(viewModel.selectedReminder.value)
    }

    /** addReminder는 리마인더를 추가한다 */
    @Test
    fun addReminderAddsReminder() = runTest {
        // Given
        whenever(repository.insertReminder(any())).thenReturn(1L)

        // When
        viewModel.addReminder(
            title = "테스트 리마인더",
            description = "설명",
            priority = Priority.HIGH
        )
        advanceUntilIdle()

        // Then
        verify(repository).insertReminder(any())
        verify(analyticsHelper).logReminderCreated(any(), any(), any())
    }

    /** updateReminder는 리마인더를 수정한다 */
    @Test
    fun updateReminderUpdatesReminder() = runTest {
        // Given
        val reminder = createReminder(id = 1, title = "테스트")
        whenever(repository.updateReminder(any())).thenReturn(Unit)

        // When
        viewModel.updateReminder(reminder)
        advanceUntilIdle()

        // Then
        verify(repository).updateReminder(any())
        verify(analyticsHelper).logReminderEdited()
    }

    /** deleteReminder는 리마인더를 삭제한다 */
    @Test
    fun deleteReminderDeletesReminder() = runTest {
        // Given
        val reminder = createReminder(id = 1, title = "테스트")
        whenever(repository.deleteReminder(any())).thenReturn(Unit)

        // When
        viewModel.deleteReminder(reminder)
        advanceUntilIdle()

        // Then
        verify(repository).deleteReminder(reminder)
        verify(alarmScheduler).cancel(reminder.id)
        verify(analyticsHelper).logReminderDeleted()
    }

    /** toggleReminderCompletion은 완료 상태를 토글한다 */
    @Test
    fun toggleReminderCompletionTogglesCompletion() = runTest {
        // Given
        val reminder = createReminder(id = 1, isCompleted = false)
        whenever(repository.toggleReminderCompletion(any())).thenReturn(Unit)

        // When
        viewModel.toggleReminderCompletion(reminder)
        advanceUntilIdle()

        // Then
        verify(repository).toggleReminderCompletion(reminder)
        verify(alarmScheduler).cancel(reminder.id)
    }

    /** selectReminder는 선택된 리마인더를 설정한다 */
    @Test
    fun selectReminderSetsSelectedReminder() {
        // Given
        val reminder = createReminder(id = 1, title = "테스트")

        // When
        viewModel.selectReminder(reminder)

        // Then
        assertEquals(reminder, viewModel.selectedReminder.value)
    }

    /** duplicateReminder는 리마인더를 복제한다 */
    @Test
    fun duplicateReminderDuplicatesReminder() = runTest {
        // Given
        val reminder = createReminder(id = 1, title = "원본")
        whenever(repository.insertReminder(any())).thenReturn(2L)

        // When
        viewModel.duplicateReminder(reminder)
        advanceUntilIdle()

        // Then
        verify(repository).insertReminder(any())
    }

    // Helper function
    private fun createReminder(
        id: Long,
        title: String = "Test Reminder $id",
        isCompleted: Boolean = false
    ) = ReminderEntity(
        id = id,
        title = title,
        isCompleted = isCompleted,
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now()
    )
}
