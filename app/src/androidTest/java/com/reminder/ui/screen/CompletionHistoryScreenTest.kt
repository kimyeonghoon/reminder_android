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
import org.mockito.kotlin.any
import java.time.LocalDateTime
import java.time.YearMonth

@RunWith(AndroidJUnit4::class)
class CompletionHistoryScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var mockViewModel: ReminderViewModel
    private var onNavigateBackCalled = false

    @Before
    fun setup() {
        mockViewModel = mock(ReminderViewModel::class.java)

        // Mock getCompletionCountByDay to return empty map
        `when`(mockViewModel.getCompletionCountByDay(any(), any())).thenReturn(emptyMap())

        // Mock getCompletedRemindersByDate to return empty list
        `when`(mockViewModel.getCompletedRemindersByDate(any())).thenReturn(emptyList())

        onNavigateBackCalled = false
    }

    @Test
    fun 완료_이력_화면_제목이_표시된다() {
        // Given
        composeTestRule.setContent {
            CompletionHistoryScreen(
                viewModel = mockViewModel,
                onNavigateBack = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("완료 이력").assertIsDisplayed()
    }

    @Test
    fun 뒤로가기_버튼_클릭_시_onNavigateBack_콜백이_호출된다() {
        // Given
        composeTestRule.setContent {
            CompletionHistoryScreen(
                viewModel = mockViewModel,
                onNavigateBack = { onNavigateBackCalled = true }
            )
        }

        // When
        composeTestRule.onNodeWithContentDescription("뒤로가기").performClick()

        // Then
        assert(onNavigateBackCalled)
    }

    @Test
    fun 월_선택기가_표시된다() {
        // Given
        val currentYearMonth = YearMonth.now()

        composeTestRule.setContent {
            CompletionHistoryScreen(
                viewModel = mockViewModel,
                onNavigateBack = {}
            )
        }

        // Then - 현재 년월이 표시되는지 확인
        composeTestRule.onNodeWithText(
            currentYearMonth.year.toString() + "년",
            substring = true
        ).assertIsDisplayed()
    }

    @Test
    fun 이전_달_버튼이_표시된다() {
        // Given
        composeTestRule.setContent {
            CompletionHistoryScreen(
                viewModel = mockViewModel,
                onNavigateBack = {}
            )
        }

        // Then
        composeTestRule.onNodeWithContentDescription("이전 달").assertIsDisplayed()
    }

    @Test
    fun 다음_달_버튼이_표시된다() {
        // Given
        composeTestRule.setContent {
            CompletionHistoryScreen(
                viewModel = mockViewModel,
                onNavigateBack = {}
            )
        }

        // Then
        composeTestRule.onNodeWithContentDescription("다음 달").assertIsDisplayed()
    }

    @Test
    fun 달력_요일_헤더가_표시된다() {
        // Given
        composeTestRule.setContent {
            CompletionHistoryScreen(
                viewModel = mockViewModel,
                onNavigateBack = {}
            )
        }

        // Then - 요일이 표시되는지 확인
        composeTestRule.onNodeWithText("일").assertIsDisplayed()
        composeTestRule.onNodeWithText("월").assertIsDisplayed()
        composeTestRule.onNodeWithText("화").assertIsDisplayed()
        composeTestRule.onNodeWithText("수").assertIsDisplayed()
        composeTestRule.onNodeWithText("목").assertIsDisplayed()
        composeTestRule.onNodeWithText("금").assertIsDisplayed()
        composeTestRule.onNodeWithText("토").assertIsDisplayed()
    }

    @Test
    fun 달력에_날짜들이_표시된다() {
        // Given
        composeTestRule.setContent {
            CompletionHistoryScreen(
                viewModel = mockViewModel,
                onNavigateBack = {}
            )
        }

        // Then - 1일이 표시되는지 확인
        composeTestRule.onNodeWithText("1").assertExists()
    }

    @Test
    fun 완료_개수가_있는_날짜에_숫자가_표시된다() {
        // Given
        val testDate = LocalDateTime.now().withDayOfMonth(15).withHour(0).withMinute(0).withSecond(0)
        val completionCounts = mapOf(testDate to 3)

        `when`(mockViewModel.getCompletionCountByDay(any(), any())).thenReturn(completionCounts)

        composeTestRule.setContent {
            CompletionHistoryScreen(
                viewModel = mockViewModel,
                onNavigateBack = {}
            )
        }

        // Then - 완료 개수 3이 표시되는지 확인
        composeTestRule.onNodeWithText("3").assertExists()
    }
}
