package com.reminder.viewmodel

import com.reminder.analytics.AnalyticsHelper
import com.reminder.data.entity.Priority
import com.reminder.data.entity.ReminderEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import java.time.LocalDateTime

/**
 * ReminderSearchViewModel 테스트
 *
 * AAA 패턴 (Given-When-Then)으로 검색/필터/정렬 메서드 검증
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ReminderSearchViewModelTest {

    private lateinit var analyticsHelper: AnalyticsHelper
    private lateinit var viewModel: ReminderSearchViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        analyticsHelper = mock(AnalyticsHelper::class.java)
        viewModel = ReminderSearchViewModel(analyticsHelper)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /** 초기 검색 쿼리는 빈 문자열이다 */
    @Test
    fun initialSearchQueryIsEmpty() {
        assertEquals("", viewModel.searchQuery.value)
    }

    /** updateSearchQuery는 검색 쿼리를 업데이트한다 */
    @Test
    fun updateSearchQueryUpdatesQuery() {
        // When
        viewModel.updateSearchQuery("테스트")

        // Then
        assertEquals("테스트", viewModel.searchQuery.value)
        verify(analyticsHelper).logSearchPerformed(3)
    }

    /** updateSearchQuery는 빈 쿼리일 때 analytics를 호출하지 않는다 */
    @Test
    fun updateSearchQueryDoesNotLogForEmptyQuery() {
        // When
        viewModel.updateSearchQuery("")

        // Then
        verify(analyticsHelper, never()).logSearchPerformed(anyInt())
    }

    /** getFilteredReminders는 제목으로 필터링한다 */
    @Test
    fun getFilteredRemindersFiltersByTitle() {
        // Given
        val reminders = listOf(
            createReminder(id = 1, title = "운동하기"),
            createReminder(id = 2, title = "공부하기"),
            createReminder(id = 3, title = "독서하기")
        )

        // When
        val result = viewModel.getFilteredReminders(reminders, "운동")

        // Then
        assertEquals(1, result.size)
        assertEquals("운동하기", result[0].title)
    }

    /** getFilteredReminders는 빈 쿼리일 때 모든 리마인더를 반환한다 */
    @Test
    fun getFilteredRemindersReturnsAllForEmptyQuery() {
        // Given
        val reminders = listOf(
            createReminder(id = 1, title = "운동하기"),
            createReminder(id = 2, title = "공부하기")
        )

        // When
        val result = viewModel.getFilteredReminders(reminders, "")

        // Then
        assertEquals(2, result.size)
    }

    /** filterByPriority는 우선순위로 필터링한다 */
    @Test
    fun filterByPriorityFiltersByPriority() {
        // Given
        val reminders = listOf(
            createReminder(id = 1, priority = Priority.HIGH),
            createReminder(id = 2, priority = Priority.MEDIUM),
            createReminder(id = 3, priority = Priority.LOW)
        )

        // When
        val result = viewModel.filterByPriority(reminders, com.reminder.data.entity.FilterPriority.HIGH)

        // Then
        assertEquals(1, result.size)
        assertEquals(Priority.HIGH, result[0].priority)
    }

    /** sortReminders는 제목으로 정렬한다 */
    @Test
    fun sortRemindersSortsByTitle() {
        // Given
        val reminders = listOf(
            createReminder(id = 1, title = "C"),
            createReminder(id = 2, title = "A"),
            createReminder(id = 3, title = "B")
        )

        // When
        val result = viewModel.sortReminders(reminders, com.reminder.data.entity.SortOption.BY_TITLE_ASC)

        // Then
        assertEquals(3, result.size)
        assertEquals("A", result[0].title)
        assertEquals("B", result[1].title)
        assertEquals("C", result[2].title)
    }

    // Helper function
    private fun createReminder(
        id: Long,
        title: String = "Test Reminder $id",
        priority: Priority = Priority.MEDIUM
    ) = ReminderEntity(
        id = id,
        title = title,
        priority = priority,
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now()
    )
}
