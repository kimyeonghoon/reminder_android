package com.reminder.ui.screen

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.reminder.data.entity.Priority
import com.reminder.data.entity.ReminderEntity
import com.reminder.viewmodel.ReminderViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import java.time.LocalDateTime

@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var mockViewModel: ReminderViewModel
    private lateinit var activeRemindersFlow: MutableStateFlow<List<ReminderEntity>>
    private lateinit var searchQueryFlow: MutableStateFlow<String>

    private var onAddClickCalled = false
    private var onReminderClickCalled = false
    private var clickedReminder: ReminderEntity? = null

    @Before
    fun setup() {
        mockViewModel = mock(ReminderViewModel::class.java)
        activeRemindersFlow = MutableStateFlow(emptyList())
        searchQueryFlow = MutableStateFlow("")

        `when`(mockViewModel.activeReminders).thenReturn(activeRemindersFlow)
        `when`(mockViewModel.searchQuery).thenReturn(searchQueryFlow)

        // Mock filter and sort methods
        `when`(mockViewModel.getFilteredReminders(any(), any())).thenAnswer { invocation ->
            invocation.getArgument<List<ReminderEntity>>(0)
        }
        `when`(mockViewModel.filterByPriority(any(), any())).thenAnswer { invocation ->
            invocation.getArgument<List<ReminderEntity>>(0)
        }
        `when`(mockViewModel.filterByDate(any(), any())).thenAnswer { invocation ->
            invocation.getArgument<List<ReminderEntity>>(0)
        }
        `when`(mockViewModel.sortReminders(any(), any())).thenAnswer { invocation ->
            invocation.getArgument<List<ReminderEntity>>(0)
        }

        onAddClickCalled = false
        onReminderClickCalled = false
        clickedReminder = null
    }

    @Test
    fun fab_클릭_시_onAddClick_콜백이_호출된다() {
        // Given
        composeTestRule.setContent {
            HomeScreen(
                viewModel = mockViewModel,
                onAddClick = { onAddClickCalled = true },
                onReminderClick = {}
            )
        }

        // When
        composeTestRule.onNodeWithContentDescription("Add Reminder").performClick()

        // Then
        assert(onAddClickCalled)
    }

    @Test
    fun 리마인더가_없을_때_빈_상태_메시지를_표시한다() {
        // Given
        activeRemindersFlow.value = emptyList()

        // When
        composeTestRule.setContent {
            HomeScreen(
                viewModel = mockViewModel,
                onAddClick = {},
                onReminderClick = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("No active reminders").assertIsDisplayed()
    }

    @Test
    fun 리마인더_목록을_표시한다() {
        // Given
        val reminders = listOf(
            ReminderEntity(
                id = 1,
                title = "Test Reminder 1",
                description = "Description 1",
                priority = Priority.HIGH
            ),
            ReminderEntity(
                id = 2,
                title = "Test Reminder 2",
                description = "Description 2",
                priority = Priority.LOW
            )
        )
        activeRemindersFlow.value = reminders

        // When
        composeTestRule.setContent {
            HomeScreen(
                viewModel = mockViewModel,
                onAddClick = {},
                onReminderClick = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("Test Reminder 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Test Reminder 2").assertIsDisplayed()
    }

    @Test
    fun 검색_아이콘_클릭_시_검색바가_표시된다() {
        // Given
        composeTestRule.setContent {
            HomeScreen(
                viewModel = mockViewModel,
                onAddClick = {},
                onReminderClick = {}
            )
        }

        // When
        composeTestRule.onNodeWithContentDescription("Search").performClick()

        // Then
        composeTestRule.onNodeWithText("Search reminders...").assertIsDisplayed()
    }

    @Test
    fun 검색_쿼리_입력_시_updateSearchQuery가_호출된다() {
        // Given
        composeTestRule.setContent {
            HomeScreen(
                viewModel = mockViewModel,
                onAddClick = {},
                onReminderClick = {}
            )
        }

        // 검색바 열기
        composeTestRule.onNodeWithContentDescription("Search").performClick()

        // When
        composeTestRule.onNodeWithText("Search reminders...").performTextInput("test query")

        // Then
        verify(mockViewModel, atLeastOnce()).updateSearchQuery(contains("test"))
    }

    @Test
    fun 검색_결과가_없을_때_No_reminders_found_메시지를_표시한다() {
        // Given
        activeRemindersFlow.value = emptyList()
        searchQueryFlow.value = "nonexistent"

        // When
        composeTestRule.setContent {
            HomeScreen(
                viewModel = mockViewModel,
                onAddClick = {},
                onReminderClick = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("No reminders found").assertIsDisplayed()
    }

    @Test
    fun 리마인더_카드_클릭_시_onReminderClick_콜백이_호출된다() {
        // Given
        val reminder = ReminderEntity(
            id = 1,
            title = "Clickable Reminder",
            description = "Click me",
            priority = Priority.MEDIUM
        )
        activeRemindersFlow.value = listOf(reminder)

        composeTestRule.setContent {
            HomeScreen(
                viewModel = mockViewModel,
                onAddClick = {},
                onReminderClick = {
                    onReminderClickCalled = true
                    clickedReminder = it
                }
            )
        }

        // When
        composeTestRule.onNodeWithText("Clickable Reminder").performClick()

        // Then
        assert(onReminderClickCalled)
        assert(clickedReminder?.id == 1L)
    }

    @Test
    fun 통계_아이콘_클릭_시_onStatisticsClick_콜백이_호출된다() {
        // Given
        var onStatisticsClickCalled = false

        composeTestRule.setContent {
            HomeScreen(
                viewModel = mockViewModel,
                onAddClick = {},
                onReminderClick = {},
                onStatisticsClick = { onStatisticsClickCalled = true }
            )
        }

        // When
        composeTestRule.onNodeWithContentDescription("Statistics").performClick()

        // Then
        assert(onStatisticsClickCalled)
    }

    @Test
    fun 설정_아이콘_클릭_시_onSettingsClick_콜백이_호출된다() {
        // Given
        var onSettingsClickCalled = false

        composeTestRule.setContent {
            HomeScreen(
                viewModel = mockViewModel,
                onAddClick = {},
                onReminderClick = {},
                onSettingsClick = { onSettingsClickCalled = true }
            )
        }

        // When
        composeTestRule.onNodeWithContentDescription("Settings").performClick()

        // Then
        assert(onSettingsClickCalled)
    }

    @Test
    fun 필터_칩이_표시된다() {
        // Given
        composeTestRule.setContent {
            HomeScreen(
                viewModel = mockViewModel,
                onAddClick = {},
                onReminderClick = {}
            )
        }

        // Then - FilterChips 컴포넌트가 렌더링되는지 확인
        // Priority filters
        composeTestRule.onNodeWithText("All").assertIsDisplayed()
        composeTestRule.onNodeWithText("High").assertExists()
        composeTestRule.onNodeWithText("Medium").assertExists()
        composeTestRule.onNodeWithText("Low").assertExists()
    }

    @Test
    fun 정렬_옵션이_표시된다() {
        // Given
        composeTestRule.setContent {
            HomeScreen(
                viewModel = mockViewModel,
                onAddClick = {},
                onReminderClick = {}
            )
        }

        // Then - SortDropdown이 렌더링되는지 확인
        composeTestRule.onNodeWithText("Sort by: Due Date ↑").assertIsDisplayed()
    }
}
