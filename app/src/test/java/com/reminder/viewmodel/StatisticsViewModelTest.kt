package com.reminder.viewmodel

import com.reminder.data.dao.GoalDao
import com.reminder.data.entity.Priority
import com.reminder.data.entity.ReminderEntity
import com.reminder.data.repository.ReminderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class StatisticsViewModelTest {

    private lateinit var repository: ReminderRepository
    private lateinit var goalDao: GoalDao
    private lateinit var viewModel: StatisticsViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mock(ReminderRepository::class.java)
        goalDao = mock(GoalDao::class.java)
        // 기본 빈 리스트로 초기화
        `when`(repository.allReminders).thenReturn(flowOf(emptyList()))
        `when`(goalDao.getAllActiveGoals()).thenReturn(flowOf(emptyList()))
        viewModel = StatisticsViewModel(repository, goalDao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /** 초기 상태는 모든 통계가 0이다 */
    @Test
    fun initialStateAllStatisticsAreZero() {
        val statistics = viewModel.statistics.value

        assertEquals(0, statistics.totalReminders)
        assertEquals(0, statistics.completedReminders)
        assertEquals(0, statistics.pendingReminders)
        assertEquals(0f, statistics.completionRate)
    }

    /** 전체 리마인더 개수를 계산한다 */
    @Test
    fun calculateTotalRemindersCount() = runTest {
        // Given
        val reminders = listOf(
            createReminder(id = 1, isCompleted = true),
            createReminder(id = 2, isCompleted = false),
            createReminder(id = 3, isCompleted = false)
        )
        val testRepository = mock(ReminderRepository::class.java)
        `when`(testRepository.allReminders).thenReturn(flowOf(reminders))

        // When
        val testGoalDao = mock(GoalDao::class.java)
        `when`(testGoalDao.getAllActiveGoals()).thenReturn(flowOf(emptyList()))
        val testViewModel = StatisticsViewModel(testRepository, testGoalDao)

        // Then
        assertEquals(3, testViewModel.statistics.value.totalReminders)
    }

    /** 완료된 리마인더 개수를 계산한다 */
    @Test
    fun calculateCompletedRemindersCount() = runTest {
        // Given
        val reminders = listOf(
            createReminder(id = 1, isCompleted = true),
            createReminder(id = 2, isCompleted = true),
            createReminder(id = 3, isCompleted = false)
        )
        val testRepository = mock(ReminderRepository::class.java)
        `when`(testRepository.allReminders).thenReturn(flowOf(reminders))

        // When
        val testGoalDao = mock(GoalDao::class.java)
        `when`(testGoalDao.getAllActiveGoals()).thenReturn(flowOf(emptyList()))
        val testViewModel = StatisticsViewModel(testRepository, testGoalDao)

        // Then
        assertEquals(2, testViewModel.statistics.value.completedReminders)
    }

    /** 미완료 리마인더 개수를 계산한다 */
    @Test
    fun calculatePendingRemindersCount() = runTest {
        // Given
        val reminders = listOf(
            createReminder(id = 1, isCompleted = true),
            createReminder(id = 2, isCompleted = false),
            createReminder(id = 3, isCompleted = false)
        )
        val testRepository = mock(ReminderRepository::class.java)
        `when`(testRepository.allReminders).thenReturn(flowOf(reminders))

        // When
        val testGoalDao = mock(GoalDao::class.java)
        `when`(testGoalDao.getAllActiveGoals()).thenReturn(flowOf(emptyList()))
        val testViewModel = StatisticsViewModel(testRepository, testGoalDao)

        // Then
        assertEquals(2, testViewModel.statistics.value.pendingReminders)
    }

    /** 완료율을 계산한다 */
    @Test
    fun calculateCompletionRate() = runTest {
        // Given
        val reminders = listOf(
            createReminder(id = 1, isCompleted = true),
            createReminder(id = 2, isCompleted = true),
            createReminder(id = 3, isCompleted = false),
            createReminder(id = 4, isCompleted = false)
        )
        val testRepository = mock(ReminderRepository::class.java)
        `when`(testRepository.allReminders).thenReturn(flowOf(reminders))

        // When
        val testGoalDao = mock(GoalDao::class.java)
        `when`(testGoalDao.getAllActiveGoals()).thenReturn(flowOf(emptyList()))
        val testViewModel = StatisticsViewModel(testRepository, testGoalDao)

        // Then
        assertEquals(0.5f, testViewModel.statistics.value.completionRate, 0.001f)
    }

    /** 리마인더가 없을 때 완료율은 0이다 */
    @Test
    fun completionRateIsZeroWhenNoReminders() = runTest {
        // Given
        val testRepository = mock(ReminderRepository::class.java)
        `when`(testRepository.allReminders).thenReturn(flowOf(emptyList()))

        // When
        val testGoalDao = mock(GoalDao::class.java)
        `when`(testGoalDao.getAllActiveGoals()).thenReturn(flowOf(emptyList()))
        val testViewModel = StatisticsViewModel(testRepository, testGoalDao)

        // Then
        assertEquals(0f, testViewModel.statistics.value.completionRate)
    }

    /** 우선순위별 개수를 계산한다 */
    @Test
    fun calculateCountByPriority() = runTest {
        // Given
        val reminders = listOf(
            createReminder(id = 1, priority = Priority.HIGH),
            createReminder(id = 2, priority = Priority.HIGH),
            createReminder(id = 3, priority = Priority.MEDIUM),
            createReminder(id = 4, priority = Priority.LOW)
        )
        val testRepository = mock(ReminderRepository::class.java)
        `when`(testRepository.allReminders).thenReturn(flowOf(reminders))

        // When
        val testGoalDao = mock(GoalDao::class.java)
        `when`(testGoalDao.getAllActiveGoals()).thenReturn(flowOf(emptyList()))
        val testViewModel = StatisticsViewModel(testRepository, testGoalDao)

        // Then
        val statistics = testViewModel.statistics.value
        assertEquals(2, statistics.highPriorityCount)
        assertEquals(1, statistics.mediumPriorityCount)
        assertEquals(1, statistics.lowPriorityCount)
    }

    /** 카테고리별 분포를 계산한다 */
    @Test
    fun calculateDistributionByCategory() = runTest {
        // Given
        val reminders = listOf(
            createReminder(id = 1, category = "Work"),
            createReminder(id = 2, category = "Work"),
            createReminder(id = 3, category = "Personal"),
            createReminder(id = 4, category = "Shopping"),
            createReminder(id = 5, category = "Shopping"),
            createReminder(id = 6, category = "Shopping")
        )
        val testRepository = mock(ReminderRepository::class.java)
        `when`(testRepository.allReminders).thenReturn(flowOf(reminders))

        // When
        val testGoalDao = mock(GoalDao::class.java)
        `when`(testGoalDao.getAllActiveGoals()).thenReturn(flowOf(emptyList()))
        val testViewModel = StatisticsViewModel(testRepository, testGoalDao)

        // Then
        val distribution = testViewModel.statistics.value.categoryDistribution
        assertEquals(2, distribution["Work"])
        assertEquals(1, distribution["Personal"])
        assertEquals(3, distribution["Shopping"])
    }

    /** 빈 카테고리는 분포에서 제외한다 */
    @Test
    fun excludeEmptyCategoriesFromDistribution() = runTest {
        // Given
        val reminders = listOf(
            createReminder(id = 1, category = "Work"),
            createReminder(id = 2, category = ""),
            createReminder(id = 3, category = "")
        )
        val testRepository = mock(ReminderRepository::class.java)
        `when`(testRepository.allReminders).thenReturn(flowOf(reminders))

        // When
        val testGoalDao = mock(GoalDao::class.java)
        `when`(testGoalDao.getAllActiveGoals()).thenReturn(flowOf(emptyList()))
        val testViewModel = StatisticsViewModel(testRepository, testGoalDao)

        // Then
        val distribution = testViewModel.statistics.value.categoryDistribution
        assertEquals(1, distribution.size)
        assertEquals(1, distribution["Work"])
        assertNull(distribution[""])
    }

    /** 최근 7일간 완료된 리마인더 개수를 계산한다 */
    @Test
    fun calculateCompletedRemindersForLast7Days() = runTest {
        // Given
        val now = LocalDateTime.now()
        val reminders = listOf(
            // 오늘 완료된 리마인더 2개
            createReminder(id = 1, isCompleted = true, updatedAt = now),
            createReminder(id = 2, isCompleted = true, updatedAt = now.minusHours(2)),
            // 2일 전 완료된 리마인더 1개
            createReminder(id = 3, isCompleted = true, updatedAt = now.minusDays(2)),
            // 5일 전 완료된 리마인더 3개 (같은 날짜가 되도록 시간 조정)
            createReminder(id = 4, isCompleted = true, updatedAt = now.minusDays(5)),
            createReminder(id = 5, isCompleted = true, updatedAt = now.minusDays(5).plusHours(3)),
            createReminder(id = 6, isCompleted = true, updatedAt = now.minusDays(5).minusHours(2)),
            // 미완료 리마인더 (카운트 안됨)
            createReminder(id = 7, isCompleted = false, updatedAt = now),
            // 8일 전 완료 (범위 밖)
            createReminder(id = 8, isCompleted = true, updatedAt = now.minusDays(8))
        )
        val testRepository = mock(ReminderRepository::class.java)
        `when`(testRepository.allReminders).thenReturn(flowOf(reminders))

        // When
        val testGoalDao = mock(GoalDao::class.java)
        `when`(testGoalDao.getAllActiveGoals()).thenReturn(flowOf(emptyList()))
        val testViewModel = StatisticsViewModel(testRepository, testGoalDao, currentTimeProvider = { now })

        // Then
        val weeklyCompleted = testViewModel.statistics.value.weeklyCompleted
        assertEquals(7, weeklyCompleted.size)
        assertEquals(2, weeklyCompleted[0]) // 오늘
        assertEquals(0, weeklyCompleted[1]) // 어제
        assertEquals(1, weeklyCompleted[2]) // 2일 전
        assertEquals(0, weeklyCompleted[3]) // 3일 전
        assertEquals(0, weeklyCompleted[4]) // 4일 전
        assertEquals(3, weeklyCompleted[5]) // 5일 전
        assertEquals(0, weeklyCompleted[6]) // 6일 전
    }

    /** 최근 30일간 완료된 리마인더 개수를 계산한다 */
    @Test
    fun calculateCompletedRemindersForLast30Days() = runTest {
        // Given
        val now = LocalDateTime.now()
        val reminders = listOf(
            // 오늘 완료 1개
            createReminder(id = 1, isCompleted = true, updatedAt = now),
            // 10일 전 완료 2개
            createReminder(id = 2, isCompleted = true, updatedAt = now.minusDays(10)),
            createReminder(id = 3, isCompleted = true, updatedAt = now.minusDays(10).minusHours(5)),
            // 29일 전 완료 1개 (범위 내 마지막 날)
            createReminder(id = 4, isCompleted = true, updatedAt = now.minusDays(29)),
            // 31일 전 완료 (범위 밖)
            createReminder(id = 5, isCompleted = true, updatedAt = now.minusDays(31))
        )
        val testRepository = mock(ReminderRepository::class.java)
        `when`(testRepository.allReminders).thenReturn(flowOf(reminders))

        // When
        val testGoalDao = mock(GoalDao::class.java)
        `when`(testGoalDao.getAllActiveGoals()).thenReturn(flowOf(emptyList()))
        val testViewModel = StatisticsViewModel(testRepository, testGoalDao)

        // Then
        val monthlyCompleted = testViewModel.statistics.value.monthlyCompleted
        assertEquals(30, monthlyCompleted.size)
        assertEquals(1, monthlyCompleted[0])  // 오늘
        assertEquals(2, monthlyCompleted[10]) // 10일 전
        assertEquals(1, monthlyCompleted[29]) // 29일 전
        // 31일 전은 범위 밖이므로 포함 안됨
        assertEquals(4, monthlyCompleted.sum()) // 총 4개만 포함
    }

    // Helper function
    private fun createReminder(
        id: Long,
        title: String = "Test Reminder $id",
        description: String = "",
        dueDateTime: LocalDateTime? = null,
        priority: Priority = Priority.MEDIUM,
        category: String = "",
        isCompleted: Boolean = false,
        createdAt: LocalDateTime = LocalDateTime.now(),
        updatedAt: LocalDateTime = LocalDateTime.now()
    ) = ReminderEntity(
        id = id,
        title = title,
        description = description,
        dueDateTime = dueDateTime,
        priority = priority,
        category = category,
        isCompleted = isCompleted,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
