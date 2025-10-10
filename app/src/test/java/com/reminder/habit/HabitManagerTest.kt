package com.reminder.habit

import com.reminder.data.dao.HabitDao
import com.reminder.data.entity.HabitCompletion
import com.reminder.data.entity.HabitEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.whenever
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * v1.44.0: HabitManager TDD Tests
 *
 * Red Phase - 테스트 먼저 작성
 */
class HabitManagerTest {

    private lateinit var habitDao: HabitDao
    private lateinit var habitManager: HabitManager

    @Before
    fun setup() {
        habitDao = mock(HabitDao::class.java)
        habitManager = HabitManager(habitDao)
    }

    @Test
    fun `습관을 생성할 수 있다`() = runTest {
        // Given
        val habit = createTestHabit(id = 1, name = "물 마시기")
        whenever(habitDao.insertHabit(habit)).thenReturn(1L)

        // When
        val habitId = habitManager.createHabit(habit)

        // Then
        assertEquals(1L, habitId)
        verify(habitDao).insertHabit(habit)
    }

    @Test
    fun `오늘 습관을 완료 처리할 수 있다`() = runTest {
        // Given
        val habitId = 1L
        val today = LocalDate.now()
        val completion = HabitCompletion(habitId = habitId, completedDate = today)

        // When
        habitManager.completeHabit(habitId, today)

        // Then
        verify(habitDao).insertCompletion(completion)
    }

    @Test
    fun `오늘 습관 완료를 취소할 수 있다`() = runTest {
        // Given
        val habitId = 1L
        val today = LocalDate.now()

        // When
        habitManager.uncompleteHabit(habitId, today)

        // Then
        verify(habitDao).deleteCompletion(habitId, today)
    }

    @Test
    fun `완료한 습관은 true를 반환한다`() = runTest {
        // Given
        val habitId = 1L
        val today = LocalDate.now()
        val completion = HabitCompletion(habitId = habitId, completedDate = today)
        whenever(habitDao.getCompletion(habitId, today)).thenReturn(completion)

        // When
        val isCompleted = habitManager.isHabitCompletedToday(habitId, today)

        // Then
        assertTrue(isCompleted)
    }

    @Test
    fun `완료하지 않은 습관은 false를 반환한다`() = runTest {
        // Given
        val habitId = 1L
        val today = LocalDate.now()
        whenever(habitDao.getCompletion(habitId, today)).thenReturn(null)

        // When
        val isCompleted = habitManager.isHabitCompletedToday(habitId, today)

        // Then
        assertFalse(isCompleted)
    }

    @Test
    fun `완료 기록이 없으면 연속 일수는 0이다`() = runTest {
        // Given
        val habitId = 1L
        whenever(habitDao.getCompletionDates(habitId)).thenReturn(emptyList())

        // When
        val streak = habitManager.calculateStreak(habitId)

        // Then
        assertEquals(0, streak)
    }

    @Test
    fun `연속 완료 일수를 계산한다`() = runTest {
        // Given
        val habitId = 1L
        val today = LocalDate.now()
        val completions = listOf(
            today,
            today.minusDays(1),
            today.minusDays(2),
            today.minusDays(5) // 연속 끊김
        )
        whenever(habitDao.getCompletionDates(habitId)).thenReturn(completions)

        // When
        val streak = habitManager.calculateStreak(habitId)

        // Then
        assertEquals(3, streak) // 오늘 포함 3일 연속
    }

    @Test
    fun `오늘 완료하지 않으면 연속 일수에 포함하지 않는다`() = runTest {
        // Given
        val habitId = 1L
        val today = LocalDate.now()
        val completions = listOf(
            today.minusDays(1),
            today.minusDays(2),
            today.minusDays(3)
        )
        whenever(habitDao.getCompletionDates(habitId)).thenReturn(completions)

        // When
        val streak = habitManager.calculateStreak(habitId)

        // Then
        assertEquals(0, streak) // 오늘 완료 안 했으면 0
    }

    @Test
    fun `기간별 완료율을 계산한다`() = runTest {
        // Given
        val habitId = 1L
        val startDate = LocalDate.now().minusDays(9) // 10일 기간
        val endDate = LocalDate.now()
        val completedDays = 7 // 7일 완료

        whenever(habitDao.getCompletionCountInPeriod(habitId, startDate, endDate))
            .thenReturn(completedDays)

        // When
        val rate = habitManager.getCompletionRate(habitId, startDate, endDate)

        // Then
        assertEquals(70.0, rate, 0.01) // 7/10 * 100 = 70%
    }

    @Test
    fun `기간이 0일이면 완료율은 0이다`() = runTest {
        // Given
        val habitId = 1L
        val date = LocalDate.now()
        whenever(habitDao.getCompletionCountInPeriod(habitId, date, date))
            .thenReturn(0)

        // When
        val rate = habitManager.getCompletionRate(habitId, date, date)

        // Then
        assertEquals(0.0, rate, 0.01)
    }

    @Test
    fun `습관과 모든 완료 기록을 삭제한다`() = runTest {
        // Given
        val habitId = 1L

        // When
        habitManager.deleteHabit(habitId)

        // Then
        verify(habitDao).deleteHabit(habitId)
        verify(habitDao).deleteAllCompletionsForHabit(habitId)
    }

    @Test
    fun `모든 습관을 조회할 수 있다`() = runTest {
        // Given
        val habits = listOf(
            createTestHabit(id = 1, name = "운동"),
            createTestHabit(id = 2, name = "독서")
        )
        whenever(habitDao.getAllHabits()).thenReturn(flowOf(habits))

        // When
        val result = habitManager.getAllHabits().first()

        // Then
        assertEquals(2, result.size)
        assertEquals("운동", result[0].name)
        assertEquals("독서", result[1].name)
    }

    // Helper function
    private fun createTestHabit(
        id: Long = 0,
        name: String = "Test Habit",
        description: String = "",
        frequency: Int = 7, // 주 7회 (매일)
        isActive: Boolean = true
    ) = HabitEntity(
        id = id,
        name = name,
        description = description,
        frequency = frequency,
        isActive = isActive
    )
}
