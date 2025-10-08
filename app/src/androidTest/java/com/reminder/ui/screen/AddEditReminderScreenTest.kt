package com.reminder.ui.screen

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.reminder.data.entity.Priority
import com.reminder.data.entity.RecurrencePattern
import com.reminder.data.entity.ReminderEntity
import com.reminder.viewmodel.ReminderViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*
import java.time.LocalDateTime

@RunWith(AndroidJUnit4::class)
class AddEditReminderScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var mockViewModel: ReminderViewModel
    private var onNavigateBackCalled = false

    @Before
    fun setup() {
        mockViewModel = mock(ReminderViewModel::class.java)
        onNavigateBackCalled = false
    }

    @Test
    fun 타이틀이_비어있으면_저장_버튼이_비활성화된다() {
        // Given
        composeTestRule.setContent {
            AddEditReminderScreen(
                viewModel = mockViewModel,
                reminder = null,
                onNavigateBack = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("Save").assertIsNotEnabled()
    }

    @Test
    fun 타이틀_입력_시_저장_버튼이_활성화된다() {
        // Given
        composeTestRule.setContent {
            AddEditReminderScreen(
                viewModel = mockViewModel,
                reminder = null,
                onNavigateBack = {}
            )
        }

        // When
        composeTestRule.onNodeWithText("Title*").performTextInput("New Reminder")

        // Then
        composeTestRule.onNodeWithText("Save").assertIsEnabled()
    }

    @Test
    fun 모든_필드_입력이_가능하다() {
        // Given
        composeTestRule.setContent {
            AddEditReminderScreen(
                viewModel = mockViewModel,
                reminder = null,
                onNavigateBack = {}
            )
        }

        // When
        composeTestRule.onNodeWithText("Title*").performTextInput("Test Title")
        composeTestRule.onNodeWithText("Description").performTextInput("Test Description")
        composeTestRule.onNodeWithText("Category").performTextInput("Test Category")

        // Then
        composeTestRule.onNodeWithText("Test Title").assertIsDisplayed()
        composeTestRule.onNodeWithText("Test Description").assertIsDisplayed()
        composeTestRule.onNodeWithText("Test Category").assertIsDisplayed()
    }

    @Test
    fun 우선순위_선택_버튼이_표시된다() {
        // Given
        composeTestRule.setContent {
            AddEditReminderScreen(
                viewModel = mockViewModel,
                reminder = null,
                onNavigateBack = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("High").assertIsDisplayed()
        composeTestRule.onNodeWithText("Medium").assertIsDisplayed()
        composeTestRule.onNodeWithText("Low").assertIsDisplayed()
    }

    @Test
    fun 날짜_선택_버튼이_표시된다() {
        // Given
        composeTestRule.setContent {
            AddEditReminderScreen(
                viewModel = mockViewModel,
                reminder = null,
                onNavigateBack = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("Select Date").assertIsDisplayed()
    }

    @Test
    fun 시간_선택_버튼이_표시된다() {
        // Given
        composeTestRule.setContent {
            AddEditReminderScreen(
                viewModel = mockViewModel,
                reminder = null,
                onNavigateBack = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("Select Time").assertIsDisplayed()
    }

    @Test
    fun 반복_설정_UI가_표시된다() {
        // Given
        composeTestRule.setContent {
            AddEditReminderScreen(
                viewModel = mockViewModel,
                reminder = null,
                onNavigateBack = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("Recurrence").assertIsDisplayed()
        composeTestRule.onNodeWithText("None").assertIsDisplayed() // 기본값
    }

    @Test
    fun 저장_버튼_클릭_시_addReminder가_호출된다() {
        // Given
        composeTestRule.setContent {
            AddEditReminderScreen(
                viewModel = mockViewModel,
                reminder = null,
                onNavigateBack = { onNavigateBackCalled = true }
            )
        }

        // When
        composeTestRule.onNodeWithText("Title*").performTextInput("New Reminder")
        composeTestRule.onNodeWithText("Save").performClick()

        // Then
        verify(mockViewModel, times(1)).addReminder(
            title = eq("New Reminder"),
            description = eq(""),
            priority = any(),
            category = eq(""),
            dueDateTime = any(),
            recurrencePattern = any(),
            recurrenceInterval = any(),
            recurrenceDaysOfWeek = any(),
            recurrenceEndDate = any()
        )
        assert(onNavigateBackCalled)
    }

    @Test
    fun 리마인더_수정_시_updateReminder가_호출된다() {
        // Given
        val existingReminder = ReminderEntity(
            id = 1,
            title = "Existing Reminder",
            description = "Description",
            priority = Priority.MEDIUM
        )

        composeTestRule.setContent {
            AddEditReminderScreen(
                viewModel = mockViewModel,
                reminder = existingReminder,
                onNavigateBack = { onNavigateBackCalled = true }
            )
        }

        // When
        composeTestRule.onNodeWithText("Existing Reminder").performTextClearance()
        composeTestRule.onNodeWithText("Title*").performTextInput("Updated Reminder")
        composeTestRule.onNodeWithText("Save").performClick()

        // Then
        val captor = ArgumentCaptor.forClass(ReminderEntity::class.java)
        verify(mockViewModel, times(1)).updateReminder(captor.capture())
        assert(captor.value.title == "Updated Reminder")
        assert(onNavigateBackCalled)
    }

    @Test
    fun 기존_리마인더_데이터가_표시된다() {
        // Given
        val existingReminder = ReminderEntity(
            id = 1,
            title = "Existing Title",
            description = "Existing Description",
            priority = Priority.HIGH,
            category = "Work"
        )

        // When
        composeTestRule.setContent {
            AddEditReminderScreen(
                viewModel = mockViewModel,
                reminder = existingReminder,
                onNavigateBack = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("Existing Title").assertIsDisplayed()
        composeTestRule.onNodeWithText("Existing Description").assertIsDisplayed()
        composeTestRule.onNodeWithText("Work").assertIsDisplayed()
    }

    @Test
    fun 취소_버튼_클릭_시_onBack이_호출된다() {
        // Given
        composeTestRule.setContent {
            AddEditReminderScreen(
                viewModel = mockViewModel,
                reminder = null,
                onNavigateBack = { onNavigateBackCalled = true }
            )
        }

        // When
        composeTestRule.onNodeWithContentDescription("Navigate back").performClick()

        // Then
        assert(onNavigateBackCalled)
    }

    @Test
    fun 반복_패턴_드롭다운이_동작한다() {
        // Given
        composeTestRule.setContent {
            AddEditReminderScreen(
                viewModel = mockViewModel,
                reminder = null,
                onNavigateBack = {}
            )
        }

        // When - 반복 드롭다운 클릭
        composeTestRule.onNodeWithText("None").performClick()

        // Then - 드롭다운 옵션들이 표시됨
        composeTestRule.onAllNodesWithText("Daily").onFirst().assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Weekly").onFirst().assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Monthly").onFirst().assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Yearly").onFirst().assertIsDisplayed()
    }

    @Test
    fun Daily_반복_선택_시_간격_입력이_표시된다() {
        // Given
        composeTestRule.setContent {
            AddEditReminderScreen(
                viewModel = mockViewModel,
                reminder = null,
                onNavigateBack = {}
            )
        }

        // When
        composeTestRule.onNodeWithText("None").performClick()
        composeTestRule.onAllNodesWithText("Daily").onFirst().performClick()

        // Then
        composeTestRule.onNodeWithText("Repeat every").assertIsDisplayed()
        composeTestRule.onNodeWithText("day(s)").assertIsDisplayed()
    }

    @Test
    fun Weekly_반복_선택_시_요일_선택이_표시된다() {
        // Given
        composeTestRule.setContent {
            AddEditReminderScreen(
                viewModel = mockViewModel,
                reminder = null,
                onNavigateBack = {}
            )
        }

        // When
        composeTestRule.onNodeWithText("None").performClick()
        composeTestRule.onAllNodesWithText("Weekly").onFirst().performClick()

        // Then
        composeTestRule.onNodeWithText("Select days").assertIsDisplayed()
        composeTestRule.onNodeWithText("Mon").assertExists()
        composeTestRule.onNodeWithText("Tue").assertExists()
        composeTestRule.onNodeWithText("Wed").assertExists()
    }

    @Test
    fun 종료_날짜_선택_버튼이_표시된다() {
        // Given
        composeTestRule.setContent {
            AddEditReminderScreen(
                viewModel = mockViewModel,
                reminder = null,
                onNavigateBack = {}
            )
        }

        // When - 반복 패턴 선택
        composeTestRule.onNodeWithText("None").performClick()
        composeTestRule.onAllNodesWithText("Daily").onFirst().performClick()

        // Then
        composeTestRule.onNodeWithText("End Date").assertIsDisplayed()
    }

    @Test
    fun 기존_리마인더의_반복_설정이_표시된다() {
        // Given
        val existingReminder = ReminderEntity(
            id = 1,
            title = "Recurring Reminder",
            recurrencePattern = RecurrencePattern.DAILY,
            recurrenceInterval = 2,
            recurrenceDaysOfWeek = null,
            recurrenceEndDate = null
        )

        // When
        composeTestRule.setContent {
            AddEditReminderScreen(
                viewModel = mockViewModel,
                reminder = existingReminder,
                onNavigateBack = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("Daily").assertIsDisplayed()
    }

    @Test
    fun 우선순위_변경이_가능하다() {
        // Given
        composeTestRule.setContent {
            AddEditReminderScreen(
                viewModel = mockViewModel,
                reminder = null,
                onNavigateBack = {}
            )
        }

        // When - Low 우선순위 선택
        composeTestRule.onNodeWithText("Low").performClick()
        composeTestRule.onNodeWithText("Title*").performTextInput("Test")
        composeTestRule.onNodeWithText("Save").performClick()

        // Then
        verify(mockViewModel, times(1)).addReminder(
            title = eq("Test"),
            description = any(),
            priority = eq(Priority.LOW),
            category = any(),
            dueDateTime = any(),
            recurrencePattern = any(),
            recurrenceInterval = any(),
            recurrenceDaysOfWeek = any(),
            recurrenceEndDate = any()
        )
    }
}
