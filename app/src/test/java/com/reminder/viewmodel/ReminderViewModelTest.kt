package com.reminder.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.reminder.data.entity.Priority
import com.reminder.data.entity.ReminderEntity
import com.reminder.data.repository.ReminderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.*
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class ReminderViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: ReminderRepository
    private lateinit var alarmScheduler: com.reminder.notification.AlarmScheduler
    private lateinit var database: com.reminder.data.database.ReminderDatabase
    private lateinit var analyticsHelper: com.reminder.analytics.AnalyticsHelper
    private lateinit var viewModel: ReminderViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mock()
        alarmScheduler = mock()
        database = mock()
        analyticsHelper = mock()

        // Mock repository flows
        whenever(repository.allReminders).thenReturn(flowOf(emptyList()))
        whenever(repository.activeReminders).thenReturn(flowOf(emptyList()))
        whenever(repository.completedReminders).thenReturn(flowOf(emptyList()))

        viewModel = ReminderViewModel(repository, alarmScheduler, database, analyticsHelper)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `addReminder 호출 시 repository에 리마인더가 추가된다`() = runTest {
        // Given
        val title = "테스트 할일"
        val description = "테스트 설명"
        val priority = Priority.HIGH
        val category = "업무"

        // When
        viewModel.addReminder(title, description, priority = priority, category = category)
        advanceUntilIdle()

        // Then
        verify(repository).insertReminder(argThat { reminder ->
            reminder.title == title &&
            reminder.description == description &&
            reminder.priority == priority &&
            reminder.category == category
        })
    }

    @Test
    fun `updateReminder 호출 시 repository에 업데이트 요청이 전달된다`() = runTest {
        // Given
        val reminder = ReminderEntity(
            id = 1,
            title = "원본 할일",
            description = "원본 설명"
        )

        // When
        viewModel.updateReminder(reminder)
        advanceUntilIdle()

        // Then
        verify(repository).updateReminder(argThat { r -> r.id == reminder.id })
    }

    @Test
    fun `deleteReminder 호출 시 repository에 삭제 요청이 전달된다`() = runTest {
        // Given
        val reminder = ReminderEntity(id = 1, title = "삭제할 할일")

        // When
        viewModel.deleteReminder(reminder)
        advanceUntilIdle()

        // Then
        verify(repository).deleteReminder(reminder)
    }

    @Test
    fun `toggleReminderCompletion 호출 시 repository에 토글 요청이 전달된다`() = runTest {
        // Given
        val reminder = ReminderEntity(id = 1, title = "완료할 할일", isCompleted = false)

        // When
        viewModel.toggleReminderCompletion(reminder)
        advanceUntilIdle()

        // Then
        verify(repository).toggleReminderCompletion(reminder)
    }

    @Test
    fun `deleteAllCompletedReminders 호출 시 repository에 요청이 전달된다`() = runTest {
        // When
        viewModel.deleteAllCompletedReminders()
        advanceUntilIdle()

        // Then
        verify(repository).deleteAllCompletedReminders()
    }

    @Test
    fun `selectReminder 호출 시 selectedReminder 상태가 업데이트된다`() = runTest {
        // Given
        val reminder = ReminderEntity(id = 1, title = "선택할 할일")

        // When
        viewModel.selectReminder(reminder)
        advanceUntilIdle()

        // Then
        assertEquals(reminder, viewModel.selectedReminder.value)
    }

    @Test
    fun `selectReminder에 null 전달 시 selectedReminder가 null이 된다`() = runTest {
        // Given
        viewModel.selectReminder(ReminderEntity(id = 1, title = "할일"))

        // When
        viewModel.selectReminder(null)
        advanceUntilIdle()

        // Then
        assertNull(viewModel.selectedReminder.value)
    }

    @Test
    fun `updateSearchQuery 호출 시 searchQuery 상태가 업데이트된다`() = runTest {
        // Given
        val query = "검색어"

        // When
        viewModel.updateSearchQuery(query)
        advanceUntilIdle()

        // Then
        assertEquals(query, viewModel.searchQuery.value)
    }

    @Test
    fun `getFilteredReminders는 빈 쿼리일 때 모든 리마인더를 반환한다`() {
        // Given
        val reminders = listOf(
            ReminderEntity(id = 1, title = "할일 1"),
            ReminderEntity(id = 2, title = "할일 2")
        )

        // When
        val result = viewModel.getFilteredReminders(reminders, "")

        // Then
        assertEquals(reminders, result)
    }

    @Test
    fun `getFilteredReminders는 제목으로 필터링한다`() {
        // Given
        val reminders = listOf(
            ReminderEntity(id = 1, title = "업무 회의"),
            ReminderEntity(id = 2, title = "개인 운동")
        )

        // When
        val result = viewModel.getFilteredReminders(reminders, "업무")

        // Then
        assertEquals(1, result.size)
        assertEquals("업무 회의", result[0].title)
    }

    @Test
    fun `getFilteredReminders는 설명으로 필터링한다`() {
        // Given
        val reminders = listOf(
            ReminderEntity(id = 1, title = "회의", description = "중요한 프로젝트 논의"),
            ReminderEntity(id = 2, title = "운동", description = "헬스장 가기")
        )

        // When
        val result = viewModel.getFilteredReminders(reminders, "프로젝트")

        // Then
        assertEquals(1, result.size)
        assertEquals("회의", result[0].title)
    }

    @Test
    fun `getFilteredReminders는 카테고리로 필터링한다`() {
        // Given
        val reminders = listOf(
            ReminderEntity(id = 1, title = "회의", category = "업무"),
            ReminderEntity(id = 2, title = "운동", category = "건강")
        )

        // When
        val result = viewModel.getFilteredReminders(reminders, "건강")

        // Then
        assertEquals(1, result.size)
        assertEquals("운동", result[0].title)
    }

    @Test
    fun `getFilteredReminders는 대소문자를 구분하지 않는다`() {
        // Given
        val reminders = listOf(
            ReminderEntity(id = 1, title = "Important Meeting")
        )

        // When
        val result = viewModel.getFilteredReminders(reminders, "important")

        // Then
        assertEquals(1, result.size)
    }

    @Test
    fun `filterByPriority는 우선순위별로 필터링한다`() {
        // Given
        val reminders = listOf(
            ReminderEntity(id = 1, title = "할일1", priority = Priority.HIGH),
            ReminderEntity(id = 2, title = "할일2", priority = Priority.MEDIUM),
            ReminderEntity(id = 3, title = "할일3", priority = Priority.LOW)
        )

        // When
        val highOnly = viewModel.filterByPriority(reminders, com.reminder.data.entity.FilterPriority.HIGH)
        val all = viewModel.filterByPriority(reminders, com.reminder.data.entity.FilterPriority.ALL)

        // Then
        assertEquals(1, highOnly.size)
        assertEquals(Priority.HIGH, highOnly[0].priority)
        assertEquals(3, all.size)
    }

    @Test
    fun `filterByDate는 날짜별로 필터링한다`() {
        // Given
        val today = LocalDateTime.now()
        val tomorrow = today.plusDays(1)
        val nextWeek = today.plusDays(8)
        val reminders = listOf(
            ReminderEntity(id = 1, title = "오늘", dueDateTime = today),
            ReminderEntity(id = 2, title = "내일", dueDateTime = tomorrow),
            ReminderEntity(id = 3, title = "다음주", dueDateTime = nextWeek)
        )

        // When
        val todayOnly = viewModel.filterByDate(reminders, com.reminder.data.entity.FilterDate.TODAY)
        val thisWeek = viewModel.filterByDate(reminders, com.reminder.data.entity.FilterDate.THIS_WEEK)
        val all = viewModel.filterByDate(reminders, com.reminder.data.entity.FilterDate.ALL)

        // Then
        assertEquals(1, todayOnly.size)
        assertEquals("오늘", todayOnly[0].title)
        assertEquals(2, thisWeek.size)
        assertEquals(3, all.size)
    }

    @Test
    fun `filterByDate는 만료된 리마인더를 필터링한다`() {
        // Given
        val now = LocalDateTime.now()
        val yesterday = now.minusDays(1)
        val tomorrow = now.plusDays(1)
        val reminders = listOf(
            ReminderEntity(id = 1, title = "만료됨", dueDateTime = yesterday, isCompleted = false),
            ReminderEntity(id = 2, title = "내일", dueDateTime = tomorrow, isCompleted = false)
        )

        // When
        val overdue = viewModel.filterByDate(reminders, com.reminder.data.entity.FilterDate.OVERDUE)

        // Then
        assertEquals(1, overdue.size)
        assertEquals("만료됨", overdue[0].title)
    }

    @Test
    fun `sortReminders는 날짜 오름차순으로 정렬한다`() {
        // Given
        val date1 = LocalDateTime.now().plusDays(3)
        val date2 = LocalDateTime.now().plusDays(1)
        val date3 = LocalDateTime.now().plusDays(2)
        val reminders = listOf(
            ReminderEntity(id = 1, title = "셋째", dueDateTime = date1),
            ReminderEntity(id = 2, title = "첫째", dueDateTime = date2),
            ReminderEntity(id = 3, title = "둘째", dueDateTime = date3)
        )

        // When
        val sorted = viewModel.sortReminders(reminders, com.reminder.data.entity.SortOption.BY_DATE_ASC)

        // Then
        assertEquals("첫째", sorted[0].title)
        assertEquals("둘째", sorted[1].title)
        assertEquals("셋째", sorted[2].title)
    }

    @Test
    fun `sortReminders는 날짜 내림차순으로 정렬한다`() {
        // Given
        val date1 = LocalDateTime.now().plusDays(1)
        val date2 = LocalDateTime.now().plusDays(3)
        val date3 = LocalDateTime.now().plusDays(2)
        val reminders = listOf(
            ReminderEntity(id = 1, title = "첫째", dueDateTime = date1),
            ReminderEntity(id = 2, title = "셋째", dueDateTime = date2),
            ReminderEntity(id = 3, title = "둘째", dueDateTime = date3)
        )

        // When
        val sorted = viewModel.sortReminders(reminders, com.reminder.data.entity.SortOption.BY_DATE_DESC)

        // Then
        assertEquals("셋째", sorted[0].title)
        assertEquals("둘째", sorted[1].title)
        assertEquals("첫째", sorted[2].title)
    }

    @Test
    fun `sortReminders는 우선순위 높은 순으로 정렬한다`() {
        // Given
        val reminders = listOf(
            ReminderEntity(id = 1, title = "중간", priority = Priority.MEDIUM),
            ReminderEntity(id = 2, title = "높음", priority = Priority.HIGH),
            ReminderEntity(id = 3, title = "낮음", priority = Priority.LOW)
        )

        // When
        val sorted = viewModel.sortReminders(reminders, com.reminder.data.entity.SortOption.BY_PRIORITY_HIGH_FIRST)

        // Then
        assertEquals("높음", sorted[0].title)
        assertEquals("중간", sorted[1].title)
        assertEquals("낮음", sorted[2].title)
    }

    @Test
    fun `sortReminders는 우선순위 낮은 순으로 정렬한다`() {
        // Given
        val reminders = listOf(
            ReminderEntity(id = 1, title = "중간", priority = Priority.MEDIUM),
            ReminderEntity(id = 2, title = "높음", priority = Priority.HIGH),
            ReminderEntity(id = 3, title = "낮음", priority = Priority.LOW)
        )

        // When
        val sorted = viewModel.sortReminders(reminders, com.reminder.data.entity.SortOption.BY_PRIORITY_LOW_FIRST)

        // Then
        assertEquals("낮음", sorted[0].title)
        assertEquals("중간", sorted[1].title)
        assertEquals("높음", sorted[2].title)
    }

    @Test
    fun `sortReminders는 제목 오름차순으로 정렬한다`() {
        // Given
        val reminders = listOf(
            ReminderEntity(id = 1, title = "C 할일"),
            ReminderEntity(id = 2, title = "A 할일"),
            ReminderEntity(id = 3, title = "B 할일")
        )

        // When
        val sorted = viewModel.sortReminders(reminders, com.reminder.data.entity.SortOption.BY_TITLE_ASC)

        // Then
        assertEquals("A 할일", sorted[0].title)
        assertEquals("B 할일", sorted[1].title)
        assertEquals("C 할일", sorted[2].title)
    }

    @Test
    fun `sortReminders는 제목 내림차순으로 정렬한다`() {
        // Given
        val reminders = listOf(
            ReminderEntity(id = 1, title = "A 할일"),
            ReminderEntity(id = 2, title = "C 할일"),
            ReminderEntity(id = 3, title = "B 할일")
        )

        // When
        val sorted = viewModel.sortReminders(reminders, com.reminder.data.entity.SortOption.BY_TITLE_DESC)

        // Then
        assertEquals("C 할일", sorted[0].title)
        assertEquals("B 할일", sorted[1].title)
        assertEquals("A 할일", sorted[2].title)
    }

    @Test
    fun `sortReminders는 생성일 오름차순으로 정렬한다`() {
        // Given
        val date1 = LocalDateTime.now().minusDays(3)
        val date2 = LocalDateTime.now().minusDays(1)
        val date3 = LocalDateTime.now().minusDays(2)
        val reminders = listOf(
            ReminderEntity(id = 1, title = "첫째", createdAt = date1),
            ReminderEntity(id = 2, title = "셋째", createdAt = date2),
            ReminderEntity(id = 3, title = "둘째", createdAt = date3)
        )

        // When
        val sorted = viewModel.sortReminders(reminders, com.reminder.data.entity.SortOption.BY_CREATED_ASC)

        // Then
        assertEquals("첫째", sorted[0].title)
        assertEquals("둘째", sorted[1].title)
        assertEquals("셋째", sorted[2].title)
    }

    @Test
    fun `sortReminders는 생성일 내림차순으로 정렬한다`() {
        // Given
        val date1 = LocalDateTime.now().minusDays(3)
        val date2 = LocalDateTime.now().minusDays(1)
        val date3 = LocalDateTime.now().minusDays(2)
        val reminders = listOf(
            ReminderEntity(id = 1, title = "첫째", createdAt = date1),
            ReminderEntity(id = 2, title = "셋째", createdAt = date2),
            ReminderEntity(id = 3, title = "둘째", createdAt = date3)
        )

        // When
        val sorted = viewModel.sortReminders(reminders, com.reminder.data.entity.SortOption.BY_CREATED_DESC)

        // Then
        assertEquals("셋째", sorted[0].title)
        assertEquals("둘째", sorted[1].title)
        assertEquals("첫째", sorted[2].title)
    }
}
