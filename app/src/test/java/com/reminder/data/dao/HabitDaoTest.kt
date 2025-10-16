package com.reminder.data.dao

import com.reminder.data.entity.HabitCompletion
import com.reminder.data.entity.HabitEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.time.LocalDate
import java.time.LocalDateTime
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class HabitDaoTest {

    private lateinit var dao: HabitDao

    @Before
    fun setup() {
        dao = mock()
    }

    // === Habit CRUD 테스트 ===

    /** insertHabit은 습관을 삽입하고 ID를 반환한다 */
    @Test
    fun testInsertHabitInsertsHabitAndReturnsId() = runTest {
        // Given
        val habit = HabitEntity(
            name = "운동하기",
            description = "매일 30분 운동",
            frequency = 7
        )
        val insertedId = 1L
        whenever(dao.insertHabit(habit)).thenReturn(insertedId)

        // When
        val result = dao.insertHabit(habit)

        // Then
        verify(dao).insertHabit(habit)
        assertEquals(insertedId, result)
    }

    /** updateHabit은 습관 정보를 업데이트한다 */
    @Test
    fun testUpdateHabitUpdatesHabitInformation() = runTest {
        // Given
        val habit = HabitEntity(
            id = 1L,
            name = "독서하기",
            description = "하루 30페이지",
            frequency = 5
        )

        // When
        dao.updateHabit(habit)

        // Then
        verify(dao).updateHabit(habit)
    }

    /** deleteHabit은 ID로 습관을 삭제한다 */
    @Test
    fun testDeleteHabitDeletesHabitById() = runTest {
        // Given
        val habitId = 1L

        // When
        dao.deleteHabit(habitId)

        // Then
        verify(dao).deleteHabit(habitId)
    }

    /** getHabitById는 ID로 습관을 조회한다 */
    @Test
    fun testGetHabitByIdReturnsHabitWithMatchingId() = runTest {
        // Given
        val habitId = 1L
        val habit = HabitEntity(
            id = habitId,
            name = "명상하기",
            description = "10분 명상",
            frequency = 7
        )
        whenever(dao.getHabitById(habitId)).thenReturn(habit)

        // When
        val result = dao.getHabitById(habitId)

        // Then
        verify(dao).getHabitById(habitId)
        assertEquals(habit, result)
    }

    /** getHabitById는 존재하지 않는 ID인 경우 null을 반환한다 */
    @Test
    fun testGetHabitByIdReturnsNullWhenNotFound() = runTest {
        // Given
        val habitId = 999L
        whenever(dao.getHabitById(habitId)).thenReturn(null)

        // When
        val result = dao.getHabitById(habitId)

        // Then
        verify(dao).getHabitById(habitId)
        assertNull(result)
    }

    /** getAllHabits는 활성화된 습관만 반환한다 */
    @Test
    fun testGetAllHabitsReturnsOnlyActiveHabits() = runTest {
        // Given
        val activeHabits = listOf(
            HabitEntity(id = 1L, name = "운동", isActive = true),
            HabitEntity(id = 2L, name = "독서", isActive = true)
        )
        whenever(dao.getAllHabits()).thenReturn(flowOf(activeHabits))

        // When
        val result = dao.getAllHabits()

        // Then
        verify(dao).getAllHabits()
        assertNotNull(result)
    }

    /** getAllHabitsIncludingInactive는 모든 습관을 반환한다 */
    @Test
    fun testGetAllHabitsIncludingInactiveReturnsAllHabits() = runTest {
        // Given
        val allHabits = listOf(
            HabitEntity(id = 1L, name = "운동", isActive = true),
            HabitEntity(id = 2L, name = "독서", isActive = false)
        )
        whenever(dao.getAllHabitsIncludingInactive()).thenReturn(flowOf(allHabits))

        // When
        val result = dao.getAllHabitsIncludingInactive()

        // Then
        verify(dao).getAllHabitsIncludingInactive()
        assertNotNull(result)
    }

    // === Habit Completion CRUD 테스트 ===

    /** insertCompletion은 습관 완료 기록을 삽입한다 */
    @Test
    fun testInsertCompletionInsertsHabitCompletion() = runTest {
        // Given
        val completion = HabitCompletion(
            habitId = 1L,
            completedDate = LocalDate.now()
        )

        // When
        dao.insertCompletion(completion)

        // Then
        verify(dao).insertCompletion(completion)
    }

    /** deleteCompletion은 특정 날짜의 완료 기록을 삭제한다 */
    @Test
    fun testDeleteCompletionDeletesCompletionForSpecificDate() = runTest {
        // Given
        val habitId = 1L
        val date = LocalDate.now()

        // When
        dao.deleteCompletion(habitId, date)

        // Then
        verify(dao).deleteCompletion(habitId, date)
    }

    /** deleteAllCompletionsForHabit은 습관의 모든 완료 기록을 삭제한다 */
    @Test
    fun testDeleteAllCompletionsForHabitDeletesAllCompletions() = runTest {
        // Given
        val habitId = 1L

        // When
        dao.deleteAllCompletionsForHabit(habitId)

        // Then
        verify(dao).deleteAllCompletionsForHabit(habitId)
    }

    /** getCompletion은 특정 날짜의 완료 기록을 반환한다 */
    @Test
    fun testGetCompletionReturnsCompletionForSpecificDate() = runTest {
        // Given
        val habitId = 1L
        val date = LocalDate.now()
        val completion = HabitCompletion(habitId = habitId, completedDate = date)
        whenever(dao.getCompletion(habitId, date)).thenReturn(completion)

        // When
        val result = dao.getCompletion(habitId, date)

        // Then
        verify(dao).getCompletion(habitId, date)
        assertEquals(completion, result)
    }

    /** getCompletion은 기록이 없는 경우 null을 반환한다 */
    @Test
    fun testGetCompletionReturnsNullWhenNotFound() = runTest {
        // Given
        val habitId = 1L
        val date = LocalDate.now()
        whenever(dao.getCompletion(habitId, date)).thenReturn(null)

        // When
        val result = dao.getCompletion(habitId, date)

        // Then
        verify(dao).getCompletion(habitId, date)
        assertNull(result)
    }

    // === Streak Calculation 테스트 ===

    /** getCompletionDates는 습관의 완료 날짜 목록을 반환한다 */
    @Test
    fun testGetCompletionDatesReturnsListOfCompletionDates() = runTest {
        // Given
        val habitId = 1L
        val dates = listOf(
            LocalDate.now(),
            LocalDate.now().minusDays(1),
            LocalDate.now().minusDays(2)
        )
        whenever(dao.getCompletionDates(habitId)).thenReturn(dates)

        // When
        val result = dao.getCompletionDates(habitId)

        // Then
        verify(dao).getCompletionDates(habitId)
        assertEquals(3, result.size)
        assertEquals(dates, result)
    }

    // === Statistics 테스트 ===

    /** getCompletionCountInPeriod는 기간 내 완료 횟수를 반환한다 */
    @Test
    fun testGetCompletionCountInPeriodReturnsCountInDateRange() = runTest {
        // Given
        val habitId = 1L
        val startDate = LocalDate.now().minusDays(7)
        val endDate = LocalDate.now()
        val count = 5
        whenever(dao.getCompletionCountInPeriod(habitId, startDate, endDate)).thenReturn(count)

        // When
        val result = dao.getCompletionCountInPeriod(habitId, startDate, endDate)

        // Then
        verify(dao).getCompletionCountInPeriod(habitId, startDate, endDate)
        assertEquals(count, result)
    }

    /** getTotalCompletionCount는 전체 완료 횟수를 반환한다 */
    @Test
    fun testGetTotalCompletionCountReturnsTotalCount() = runTest {
        // Given
        val habitId = 1L
        val totalCount = 42
        whenever(dao.getTotalCompletionCount(habitId)).thenReturn(totalCount)

        // When
        val result = dao.getTotalCompletionCount(habitId)

        // Then
        verify(dao).getTotalCompletionCount(habitId)
        assertEquals(totalCount, result)
    }

    /** getCompletionsInPeriod는 기간 내 완료 날짜 목록을 반환한다 */
    @Test
    fun testGetCompletionsInPeriodReturnsCompletionDatesInRange() = runTest {
        // Given
        val habitId = 1L
        val startDate = LocalDate.now().minusDays(7)
        val endDate = LocalDate.now()
        val completions = listOf(
            LocalDate.now().minusDays(1),
            LocalDate.now().minusDays(3),
            LocalDate.now().minusDays(5)
        )
        whenever(dao.getCompletionsInPeriod(habitId, startDate, endDate)).thenReturn(completions)

        // When
        val result = dao.getCompletionsInPeriod(habitId, startDate, endDate)

        // Then
        verify(dao).getCompletionsInPeriod(habitId, startDate, endDate)
        assertEquals(3, result.size)
        assertEquals(completions, result)
    }
}
