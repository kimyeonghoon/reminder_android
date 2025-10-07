package com.reminder.ui.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.reminder.ui.theme.ReminderTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.LocalTime

@RunWith(AndroidJUnit4::class)
class DateTimePickerComponentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun datePickerField_표시된다() {
        // Given
        var selectedDate: LocalDate? = null

        // When
        composeTestRule.setContent {
            ReminderTheme {
                DatePickerField(
                    selectedDate = null,
                    onDateSelected = { selectedDate = it }
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Due Date").assertExists()
        composeTestRule.onNodeWithText("Select Date").assertExists()
    }

    @Test
    fun datePickerField_날짜선택버튼_클릭가능() {
        // Given
        var selectedDate: LocalDate? = null

        composeTestRule.setContent {
            ReminderTheme {
                DatePickerField(
                    selectedDate = null,
                    onDateSelected = { selectedDate = it }
                )
            }
        }

        // When
        composeTestRule.onNodeWithText("Select Date").performClick()

        // Then - Dialog가 열렸는지 확인 (DatePicker Dialog 타이틀)
        composeTestRule.waitUntil(timeoutMillis = 1000) {
            composeTestRule.onAllNodesWithText("Select date")
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun datePickerField_선택된날짜_표시된다() {
        // Given
        val testDate = LocalDate.of(2025, 12, 25)

        // When
        composeTestRule.setContent {
            ReminderTheme {
                DatePickerField(
                    selectedDate = testDate,
                    onDateSelected = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("2025-12-25").assertExists()
    }

    @Test
    fun timePickerField_표시된다() {
        // Given
        var selectedTime: LocalTime? = null

        // When
        composeTestRule.setContent {
            ReminderTheme {
                TimePickerField(
                    selectedTime = null,
                    onTimeSelected = { selectedTime = it }
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Due Time").assertExists()
        composeTestRule.onNodeWithText("Select Time").assertExists()
    }

    @Test
    fun timePickerField_시간선택버튼_클릭가능() {
        // Given
        var selectedTime: LocalTime? = null

        composeTestRule.setContent {
            ReminderTheme {
                TimePickerField(
                    selectedTime = null,
                    onTimeSelected = { selectedTime = it }
                )
            }
        }

        // When
        composeTestRule.onNodeWithText("Select Time").performClick()

        // Then - Dialog가 열렸는지 확인
        composeTestRule.waitUntil(timeoutMillis = 1000) {
            composeTestRule.onAllNodesWithText("Select time")
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun timePickerField_선택된시간_표시된다() {
        // Given
        val testTime = LocalTime.of(14, 30)

        // When
        composeTestRule.setContent {
            ReminderTheme {
                TimePickerField(
                    selectedTime = testTime,
                    onTimeSelected = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("14:30").assertExists()
    }

    @Test
    fun datePickerField_날짜지우기_동작한다() {
        // Given
        val testDate = LocalDate.of(2025, 12, 25)
        var selectedDate: LocalDate? = testDate

        composeTestRule.setContent {
            ReminderTheme {
                DatePickerField(
                    selectedDate = selectedDate,
                    onDateSelected = { selectedDate = it }
                )
            }
        }

        // When - Clear 버튼 클릭
        composeTestRule.onNodeWithContentDescription("Clear date").performClick()

        // Then
        composeTestRule.onNodeWithText("Select Date").assertExists()
    }

    @Test
    fun timePickerField_시간지우기_동작한다() {
        // Given
        val testTime = LocalTime.of(14, 30)
        var selectedTime: LocalTime? = testTime

        composeTestRule.setContent {
            ReminderTheme {
                TimePickerField(
                    selectedTime = selectedTime,
                    onTimeSelected = { selectedTime = it }
                )
            }
        }

        // When - Clear 버튼 클릭
        composeTestRule.onNodeWithContentDescription("Clear time").performClick()

        // Then
        composeTestRule.onNodeWithText("Select Time").assertExists()
    }
}
