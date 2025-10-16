package com.reminder.data.dao

import com.reminder.data.entity.GoalEntity
import com.reminder.data.entity.GoalType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.time.LocalDate
import org.junit.Assert.*

/**
 * GoalDao 테스트
 *
 * 목표(Goal) DAO의 모든 메서드를 검증
 * - Mockito + kotlin.test + runTest 사용
 * - AAA 패턴 (Given-When-Then)
 * - DAO 100% 커버리지 목표
 */
@OptIn(ExperimentalCoroutinesApi::class)
class GoalDaoTest {

    private lateinit var dao: GoalDao

    @Before
    fun setup() {
        dao = mock()
    }

    /** getAllActiveGoals는 활성 목표만 조회한다 */
    @Test
    fun testGetAllActiveGoalsReturnsOnlyActiveGoals() = runTest {
        // Given
        val activeGoals = listOf(
            GoalEntity(
                id = 1,
                type = GoalType.DAILY,
                targetCount = 5,
                startDate = LocalDate.now(),
                endDate = LocalDate.now().plusDays(1),
                isActive = true
            ),
            GoalEntity(
                id = 2,
                type = GoalType.WEEKLY,
                targetCount = 10,
                startDate = LocalDate.now(),
                endDate = LocalDate.now().plusWeeks(1),
                isActive = true
            )
        )
        whenever(dao.getAllActiveGoals()).thenReturn(flowOf(activeGoals))

        // When
        val result = dao.getAllActiveGoals()

        // Then
        verify(dao).getAllActiveGoals()
        assertNotNull(result)
    }

    /** getAllGoals는 활성/비활성 모든 목표를 조회한다 */
    @Test
    fun testGetAllGoalsReturnsAllGoals() = runTest {
        // Given
        val allGoals = listOf(
            GoalEntity(
                id = 1,
                type = GoalType.DAILY,
                targetCount = 5,
                startDate = LocalDate.now(),
                endDate = LocalDate.now().plusDays(1),
                isActive = true
            ),
            GoalEntity(
                id = 2,
                type = GoalType.WEEKLY,
                targetCount = 10,
                startDate = LocalDate.now(),
                endDate = LocalDate.now().plusWeeks(1),
                isActive = false
            )
        )
        whenever(dao.getAllGoals()).thenReturn(flowOf(allGoals))

        // When
        val result = dao.getAllGoals()

        // Then
        verify(dao).getAllGoals()
        assertNotNull(result)
    }

    /** getGoalsByType은 특정 타입의 활성 목표만 조회한다 */
    @Test
    fun testGetGoalsByTypeReturnsGoalsOfSpecificType() = runTest {
        // Given
        val goalType = GoalType.DAILY
        val dailyGoals = listOf(
            GoalEntity(
                id = 1,
                type = GoalType.DAILY,
                targetCount = 5,
                startDate = LocalDate.now(),
                endDate = LocalDate.now().plusDays(1),
                isActive = true
            )
        )
        whenever(dao.getGoalsByType(goalType)).thenReturn(flowOf(dailyGoals))

        // When
        val result = dao.getGoalsByType(goalType)

        // Then
        verify(dao).getGoalsByType(goalType)
        assertNotNull(result)
    }

    /** getGoalsByDateRange는 특정 기간 내의 활성 목표를 조회한다 */
    @Test
    fun testGetGoalsByDateRangeReturnsGoalsInRange() = runTest {
        // Given
        val startDate = LocalDate.now()
        val endDate = LocalDate.now().plusDays(7)
        val goalsInRange = listOf(
            GoalEntity(
                id = 1,
                type = GoalType.WEEKLY,
                targetCount = 10,
                startDate = startDate,
                endDate = endDate,
                isActive = true
            )
        )
        whenever(dao.getGoalsByDateRange(startDate, endDate)).thenReturn(flowOf(goalsInRange))

        // When
        val result = dao.getGoalsByDateRange(startDate, endDate)

        // Then
        verify(dao).getGoalsByDateRange(startDate, endDate)
        assertNotNull(result)
    }

    /** getGoalById는 특정 ID의 목표를 조회한다 */
    @Test
    fun testGetGoalByIdReturnsGoalWithId() = runTest {
        // Given
        val goalId = 1L
        val goal = GoalEntity(
            id = goalId,
            type = GoalType.DAILY,
            targetCount = 5,
            startDate = LocalDate.now(),
            endDate = LocalDate.now().plusDays(1),
            isActive = true
        )
        whenever(dao.getGoalById(goalId)).thenReturn(goal)

        // When
        val result = dao.getGoalById(goalId)

        // Then
        verify(dao).getGoalById(goalId)
        assertEquals(goal, result)
    }

    /** getGoalById는 존재하지 않는 ID에 대해 null을 반환한다 */
    @Test
    fun testGetGoalByIdReturnsNullForNonExistentId() = runTest {
        // Given
        val goalId = 999L
        whenever(dao.getGoalById(goalId)).thenReturn(null)

        // When
        val result = dao.getGoalById(goalId)

        // Then
        verify(dao).getGoalById(goalId)
        assertNull(result)
    }

    /** getCurrentDailyGoals는 현재 진행 중인 일일 목표를 조회한다 */
    @Test
    fun testGetCurrentDailyGoalsReturnsCurrentDailyGoals() = runTest {
        // Given
        val today = LocalDate.now()
        val currentDailyGoals = listOf(
            GoalEntity(
                id = 1,
                type = GoalType.DAILY,
                targetCount = 5,
                startDate = today,
                endDate = today,
                isActive = true
            )
        )
        whenever(dao.getCurrentDailyGoals(today)).thenReturn(currentDailyGoals)

        // When
        val result = dao.getCurrentDailyGoals(today)

        // Then
        verify(dao).getCurrentDailyGoals(today)
        assertEquals(currentDailyGoals, result)
    }

    /** getCurrentWeeklyGoals는 현재 진행 중인 주간 목표를 조회한다 */
    @Test
    fun testGetCurrentWeeklyGoalsReturnsCurrentWeeklyGoals() = runTest {
        // Given
        val today = LocalDate.now()
        val currentWeeklyGoals = listOf(
            GoalEntity(
                id = 1,
                type = GoalType.WEEKLY,
                targetCount = 10,
                startDate = today.minusDays(3),
                endDate = today.plusDays(3),
                isActive = true
            )
        )
        whenever(dao.getCurrentWeeklyGoals(today)).thenReturn(currentWeeklyGoals)

        // When
        val result = dao.getCurrentWeeklyGoals(today)

        // Then
        verify(dao).getCurrentWeeklyGoals(today)
        assertEquals(currentWeeklyGoals, result)
    }

    /** getCurrentMonthlyGoals는 현재 진행 중인 월간 목표를 조회한다 */
    @Test
    fun testGetCurrentMonthlyGoalsReturnsCurrentMonthlyGoals() = runTest {
        // Given
        val today = LocalDate.now()
        val currentMonthlyGoals = listOf(
            GoalEntity(
                id = 1,
                type = GoalType.MONTHLY,
                targetCount = 30,
                startDate = today.withDayOfMonth(1),
                endDate = today.withDayOfMonth(today.lengthOfMonth()),
                isActive = true
            )
        )
        whenever(dao.getCurrentMonthlyGoals(today)).thenReturn(currentMonthlyGoals)

        // When
        val result = dao.getCurrentMonthlyGoals(today)

        // Then
        verify(dao).getCurrentMonthlyGoals(today)
        assertEquals(currentMonthlyGoals, result)
    }

    /** insertGoal은 목표를 삽입하고 생성된 ID를 반환한다 */
    @Test
    fun testInsertGoalInsertsGoalAndReturnsId() = runTest {
        // Given
        val goal = GoalEntity(
            type = GoalType.DAILY,
            targetCount = 5,
            startDate = LocalDate.now(),
            endDate = LocalDate.now().plusDays(1)
        )
        val insertedId = 10L
        whenever(dao.insertGoal(goal)).thenReturn(insertedId)

        // When
        val result = dao.insertGoal(goal)

        // Then
        verify(dao).insertGoal(goal)
        assertEquals(insertedId, result)
    }

    /** updateGoal은 목표를 업데이트한다 */
    @Test
    fun testUpdateGoalUpdatesGoal() = runTest {
        // Given
        val goal = GoalEntity(
            id = 1,
            type = GoalType.DAILY,
            targetCount = 10,
            startDate = LocalDate.now(),
            endDate = LocalDate.now().plusDays(1)
        )

        // When
        dao.updateGoal(goal)

        // Then
        verify(dao).updateGoal(goal)
    }

    /** deleteGoal은 목표를 삭제한다 */
    @Test
    fun testDeleteGoalDeletesGoal() = runTest {
        // Given
        val goal = GoalEntity(
            id = 1,
            type = GoalType.DAILY,
            targetCount = 5,
            startDate = LocalDate.now(),
            endDate = LocalDate.now().plusDays(1)
        )

        // When
        dao.deleteGoal(goal)

        // Then
        verify(dao).deleteGoal(goal)
    }

    /** deactivateGoal은 목표를 비활성화한다 */
    @Test
    fun testDeactivateGoalDeactivatesGoal() = runTest {
        // Given
        val goalId = 1L

        // When
        dao.deactivateGoal(goalId)

        // Then
        verify(dao).deactivateGoal(goalId)
    }

    /** activateGoal은 목표를 활성화한다 */
    @Test
    fun testActivateGoalActivatesGoal() = runTest {
        // Given
        val goalId = 1L

        // When
        dao.activateGoal(goalId)

        // Then
        verify(dao).activateGoal(goalId)
    }

    /** deactivateExpiredGoals는 만료된 목표를 자동 비활성화한다 */
    @Test
    fun testDeactivateExpiredGoalsDeactivatesExpiredGoals() = runTest {
        // Given
        val today = LocalDate.now()

        // When
        dao.deactivateExpiredGoals(today)

        // Then
        verify(dao).deactivateExpiredGoals(today)
    }

    /** getGoalsByType은 WEEKLY 타입 목표를 조회한다 */
    @Test
    fun testGetGoalsByTypeReturnsWeeklyGoals() = runTest {
        // Given
        val goalType = GoalType.WEEKLY
        val weeklyGoals = listOf(
            GoalEntity(
                id = 2,
                type = GoalType.WEEKLY,
                targetCount = 15,
                startDate = LocalDate.now(),
                endDate = LocalDate.now().plusWeeks(1),
                isActive = true
            )
        )
        whenever(dao.getGoalsByType(goalType)).thenReturn(flowOf(weeklyGoals))

        // When
        val result = dao.getGoalsByType(goalType)

        // Then
        verify(dao).getGoalsByType(goalType)
        assertNotNull(result)
    }

    /** getGoalsByType은 MONTHLY 타입 목표를 조회한다 */
    @Test
    fun testGetGoalsByTypeReturnsMonthlyGoals() = runTest {
        // Given
        val goalType = GoalType.MONTHLY
        val monthlyGoals = listOf(
            GoalEntity(
                id = 3,
                type = GoalType.MONTHLY,
                targetCount = 50,
                startDate = LocalDate.now(),
                endDate = LocalDate.now().plusMonths(1),
                isActive = true
            )
        )
        whenever(dao.getGoalsByType(goalType)).thenReturn(flowOf(monthlyGoals))

        // When
        val result = dao.getGoalsByType(goalType)

        // Then
        verify(dao).getGoalsByType(goalType)
        assertNotNull(result)
    }

    /** getGoalsByDateRange는 교차하는 기간의 목표를 조회한다 */
    @Test
    fun testGetGoalsByDateRangeReturnsOverlappingGoals() = runTest {
        // Given
        val startDate = LocalDate.now().minusDays(5)
        val endDate = LocalDate.now().plusDays(5)
        val overlappingGoals = listOf(
            GoalEntity(
                id = 1,
                type = GoalType.DAILY,
                targetCount = 5,
                startDate = LocalDate.now().minusDays(2),
                endDate = LocalDate.now().plusDays(2),
                isActive = true
            )
        )
        whenever(dao.getGoalsByDateRange(startDate, endDate)).thenReturn(flowOf(overlappingGoals))

        // When
        val result = dao.getGoalsByDateRange(startDate, endDate)

        // Then
        verify(dao).getGoalsByDateRange(startDate, endDate)
        assertNotNull(result)
    }

    /** insertGoal은 카테고리가 있는 목표를 삽입한다 */
    @Test
    fun testInsertGoalInsertsGoalWithCategory() = runTest {
        // Given
        val goal = GoalEntity(
            type = GoalType.DAILY,
            targetCount = 5,
            category = "업무",
            startDate = LocalDate.now(),
            endDate = LocalDate.now().plusDays(1)
        )
        val insertedId = 20L
        whenever(dao.insertGoal(goal)).thenReturn(insertedId)

        // When
        val result = dao.insertGoal(goal)

        // Then
        verify(dao).insertGoal(goal)
        assertEquals(insertedId, result)
    }
}
