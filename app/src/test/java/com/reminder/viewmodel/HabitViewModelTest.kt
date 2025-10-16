package com.reminder.viewmodel

import com.reminder.data.entity.HabitEntity
import com.reminder.habit.HabitManager
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
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * HabitViewModel 테스트
 *
 * AAA 패턴 (Given-When-Then)으로 핵심 메서드 검증
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HabitViewModelTest {

    private lateinit var habitManager: HabitManager
    private lateinit var viewModel: HabitViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        habitManager = mock(HabitManager::class.java)
        // 기본 빈 리스트로 초기화
        `when`(habitManager.getAllHabits()).thenReturn(flowOf(emptyList()))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /** 초기 상태는 빈 리스트와 로딩 중 아님이다 */
    @Test
    fun initialStateIsEmptyListAndNotLoading() {
        // When
        viewModel = HabitViewModel(habitManager)

        // Then
        assertEquals(emptyList<HabitEntity>(), viewModel.allHabits.value)
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.errorMessage.value)
        assertNull(viewModel.successMessage.value)
    }

    /** allHabits는 HabitManager의 데이터를 반영한다 */
    @Test
    fun allHabitsReflectsHabitManagerData() = runTest {
        // Given
        val habits = listOf(
            createHabit(id = 1, name = "운동"),
            createHabit(id = 2, name = "독서")
        )
        val testManager = mock(HabitManager::class.java)
        `when`(testManager.getAllHabits()).thenReturn(flowOf(habits))

        // When
        val testViewModel = HabitViewModel(testManager)
        advanceUntilIdle()

        // Then
        assertEquals(2, testViewModel.allHabits.value.size)
        assertEquals("운동", testViewModel.allHabits.value[0].name)
        assertEquals("독서", testViewModel.allHabits.value[1].name)
    }

    /** createHabit는 습관을 생성하고 성공 메시지를 표시한다 */
    @Test
    fun createHabitCreatesHabitAndShowsSuccessMessage() = runTest {
        // Given
        viewModel = HabitViewModel(habitManager)
        whenever(habitManager.createHabit(any())).thenReturn(1L)

        // When
        viewModel.createHabit("운동", "매일 운동하기", 7)
        advanceUntilIdle()

        // Then
        verify(habitManager).createHabit(any())
        assertEquals("습관이 생성되었습니다", viewModel.successMessage.value)
        assertFalse(viewModel.isLoading.value)
    }

    /** createHabit는 실행 중 로딩 상태를 true로 설정한다 */
    @Test
    fun createHabitSetsLoadingToTrueDuringExecution() = runTest {
        // Given
        viewModel = HabitViewModel(habitManager)
        var loadingDuringExecution = false
        whenever(habitManager.createHabit(any())).then {
            loadingDuringExecution = viewModel.isLoading.value
            Unit
        }

        // When
        viewModel.createHabit("운동")
        advanceUntilIdle()

        // Then
        assertTrue(loadingDuringExecution)
        assertFalse(viewModel.isLoading.value)
    }

    /** createHabit는 오류 발생 시 에러 메시지를 표시한다 */
    @Test
    fun createHabitShowsErrorMessageOnFailure() = runTest {
        // Given
        viewModel = HabitViewModel(habitManager)
        val errorMsg = "생성 실패"
        whenever(habitManager.createHabit(any())).thenThrow(RuntimeException(errorMsg))

        // When
        viewModel.createHabit("운동")
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.errorMessage.value?.contains(errorMsg) == true)
        assertFalse(viewModel.isLoading.value)
    }

    /** toggleHabitCompletion은 습관 완료 상태를 토글한다 */
    @Test
    fun toggleHabitCompletionTogglesCompletionState() = runTest {
        // Given
        viewModel = HabitViewModel(habitManager)
        val habitId = 1L
        whenever(habitManager.isHabitCompletedToday(habitId, LocalDate.now())).thenReturn(false)
        whenever(habitManager.completeHabit(any(), any())).thenReturn(Unit)

        // When
        viewModel.toggleHabitCompletion(habitId)
        advanceUntilIdle()

        // Then
        verify(habitManager).completeHabit(eq(habitId), any())
    }

    /** toggleHabitCompletion은 완료된 습관을 미완료로 변경한다 */
    @Test
    fun toggleHabitCompletionUncompletesCompletedHabit() = runTest {
        // Given
        viewModel = HabitViewModel(habitManager)
        val habitId = 1L
        whenever(habitManager.isHabitCompletedToday(habitId, LocalDate.now())).thenReturn(true)
        whenever(habitManager.uncompleteHabit(any(), any())).thenReturn(Unit)

        // When
        viewModel.toggleHabitCompletion(habitId)
        advanceUntilIdle()

        // Then
        verify(habitManager).uncompleteHabit(eq(habitId), any())
    }

    /** toggleHabitCompletion은 오류 발생 시 에러 메시지를 표시한다 */
    @Test
    fun toggleHabitCompletionShowsErrorMessageOnFailure() = runTest {
        // Given
        viewModel = HabitViewModel(habitManager)
        val habitId = 1L
        val errorMsg = "토글 실패"
        whenever(habitManager.isHabitCompletedToday(any(), any()))
            .thenThrow(RuntimeException(errorMsg))

        // When
        viewModel.toggleHabitCompletion(habitId)
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.errorMessage.value?.contains(errorMsg) == true)
    }

    /** deleteHabit는 습관을 삭제하고 성공 메시지를 표시한다 */
    @Test
    fun deleteHabitDeletesHabitAndShowsSuccessMessage() = runTest {
        // Given
        viewModel = HabitViewModel(habitManager)
        val habitId = 1L
        whenever(habitManager.deleteHabit(habitId)).thenReturn(Unit)

        // When
        viewModel.deleteHabit(habitId)
        advanceUntilIdle()

        // Then
        verify(habitManager).deleteHabit(habitId)
        assertEquals("습관이 삭제되었습니다", viewModel.successMessage.value)
        assertFalse(viewModel.isLoading.value)
    }

    /** deleteHabit는 오류 발생 시 에러 메시지를 표시한다 */
    @Test
    fun deleteHabitShowsErrorMessageOnFailure() = runTest {
        // Given
        viewModel = HabitViewModel(habitManager)
        val habitId = 1L
        val errorMsg = "삭제 실패"
        whenever(habitManager.deleteHabit(habitId)).thenThrow(RuntimeException(errorMsg))

        // When
        viewModel.deleteHabit(habitId)
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.errorMessage.value?.contains(errorMsg) == true)
        assertFalse(viewModel.isLoading.value)
    }

    /** updateHabit는 습관을 수정하고 성공 메시지를 표시한다 */
    @Test
    fun updateHabitUpdatesHabitAndShowsSuccessMessage() = runTest {
        // Given
        viewModel = HabitViewModel(habitManager)
        val habit = createHabit(id = 1, name = "운동")
        whenever(habitManager.updateHabit(any())).thenReturn(Unit)

        // When
        viewModel.updateHabit(habit)
        advanceUntilIdle()

        // Then
        verify(habitManager).updateHabit(any())
        assertEquals("습관이 수정되었습니다", viewModel.successMessage.value)
        assertFalse(viewModel.isLoading.value)
    }

    /** updateHabit는 오류 발생 시 에러 메시지를 표시한다 */
    @Test
    fun updateHabitShowsErrorMessageOnFailure() = runTest {
        // Given
        viewModel = HabitViewModel(habitManager)
        val habit = createHabit(id = 1, name = "운동")
        val errorMsg = "수정 실패"
        whenever(habitManager.updateHabit(any())).thenThrow(RuntimeException(errorMsg))

        // When
        viewModel.updateHabit(habit)
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.errorMessage.value?.contains(errorMsg) == true)
        assertFalse(viewModel.isLoading.value)
    }

    /** getCompletionRate는 완료율을 조회한다 */
    @Test
    fun getCompletionRateRetrievesCompletionRate() = runTest {
        // Given
        viewModel = HabitViewModel(habitManager)
        val habitId = 1L
        val startDate = LocalDate.now().minusDays(7)
        val endDate = LocalDate.now()
        val completionRate = 0.75
        whenever(habitManager.getCompletionRate(habitId, startDate, endDate))
            .thenReturn(completionRate)

        // When
        val result = viewModel.getCompletionRate(habitId, startDate, endDate)

        // Then
        assertEquals(completionRate, result, 0.001)
        verify(habitManager).getCompletionRate(habitId, startDate, endDate)
    }

    /** clearMessages는 에러와 성공 메시지를 초기화한다 */
    @Test
    fun clearMessagesClearsErrorAndSuccessMessages() = runTest {
        // Given
        viewModel = HabitViewModel(habitManager)
        whenever(habitManager.createHabit(any())).thenReturn(1L)
        viewModel.createHabit("운동")
        advanceUntilIdle()
        assertNotNull(viewModel.successMessage.value)

        // When
        viewModel.clearMessages()

        // Then
        assertNull(viewModel.errorMessage.value)
        assertNull(viewModel.successMessage.value)
    }

    // Helper function
    private fun createHabit(
        id: Long,
        name: String = "Test Habit $id",
        description: String = "",
        frequency: Int = 7
    ) = HabitEntity(
        id = id,
        name = name,
        description = description,
        frequency = frequency,
        isActive = true,
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now()
    )
}
