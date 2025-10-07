package com.reminder.viewmodel

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
    private lateinit var viewModel: StatisticsViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mock(ReminderRepository::class.java)
        // 기본 빈 리스트로 초기화
        `when`(repository.allReminders).thenReturn(flowOf(emptyList()))
        viewModel = StatisticsViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `초기 상태는 모든 통계가 0이다`() {
        val statistics = viewModel.statistics.value

        assertEquals(0, statistics.totalReminders)
        assertEquals(0, statistics.completedReminders)
        assertEquals(0, statistics.pendingReminders)
        assertEquals(0f, statistics.completionRate)
    }

    @Test
    fun `전체 리마인더 개수를 계산한다`() = runTest {
        // Given
        val reminders = listOf(
            createReminder(id = 1, isCompleted = true),
            createReminder(id = 2, isCompleted = false),
            createReminder(id = 3, isCompleted = false)
        )
        val testRepository = mock(ReminderRepository::class.java)
        `when`(testRepository.allReminders).thenReturn(flowOf(reminders))

        // When
        val testViewModel = StatisticsViewModel(testRepository)

        // Then
        assertEquals(3, testViewModel.statistics.value.totalReminders)
    }

    @Test
    fun `완료된 리마인더 개수를 계산한다`() = runTest {
        // Given
        val reminders = listOf(
            createReminder(id = 1, isCompleted = true),
            createReminder(id = 2, isCompleted = true),
            createReminder(id = 3, isCompleted = false)
        )
        val testRepository = mock(ReminderRepository::class.java)
        `when`(testRepository.allReminders).thenReturn(flowOf(reminders))

        // When
        val testViewModel = StatisticsViewModel(testRepository)

        // Then
        assertEquals(2, testViewModel.statistics.value.completedReminders)
    }

    @Test
    fun `미완료 리마인더 개수를 계산한다`() = runTest {
        // Given
        val reminders = listOf(
            createReminder(id = 1, isCompleted = true),
            createReminder(id = 2, isCompleted = false),
            createReminder(id = 3, isCompleted = false)
        )
        val testRepository = mock(ReminderRepository::class.java)
        `when`(testRepository.allReminders).thenReturn(flowOf(reminders))

        // When
        val testViewModel = StatisticsViewModel(testRepository)

        // Then
        assertEquals(2, testViewModel.statistics.value.pendingReminders)
    }

    @Test
    fun `완료율을 계산한다`() = runTest {
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
        val testViewModel = StatisticsViewModel(testRepository)

        // Then
        assertEquals(0.5f, testViewModel.statistics.value.completionRate, 0.001f)
    }

    @Test
    fun `리마인더가 없을 때 완료율은 0이다`() = runTest {
        // Given
        val testRepository = mock(ReminderRepository::class.java)
        `when`(testRepository.allReminders).thenReturn(flowOf(emptyList()))

        // When
        val testViewModel = StatisticsViewModel(testRepository)

        // Then
        assertEquals(0f, testViewModel.statistics.value.completionRate)
    }

    @Test
    fun `우선순위별 개수를 계산한다`() = runTest {
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
        val testViewModel = StatisticsViewModel(testRepository)

        // Then
        val statistics = testViewModel.statistics.value
        assertEquals(2, statistics.highPriorityCount)
        assertEquals(1, statistics.mediumPriorityCount)
        assertEquals(1, statistics.lowPriorityCount)
    }

    @Test
    fun `카테고리별 분포를 계산한다`() = runTest {
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
        val testViewModel = StatisticsViewModel(testRepository)

        // Then
        val distribution = testViewModel.statistics.value.categoryDistribution
        assertEquals(2, distribution["Work"])
        assertEquals(1, distribution["Personal"])
        assertEquals(3, distribution["Shopping"])
    }

    @Test
    fun `빈 카테고리는 분포에서 제외한다`() = runTest {
        // Given
        val reminders = listOf(
            createReminder(id = 1, category = "Work"),
            createReminder(id = 2, category = ""),
            createReminder(id = 3, category = "")
        )
        val testRepository = mock(ReminderRepository::class.java)
        `when`(testRepository.allReminders).thenReturn(flowOf(reminders))

        // When
        val testViewModel = StatisticsViewModel(testRepository)

        // Then
        val distribution = testViewModel.statistics.value.categoryDistribution
        assertEquals(1, distribution.size)
        assertEquals(1, distribution["Work"])
        assertNull(distribution[""])
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
