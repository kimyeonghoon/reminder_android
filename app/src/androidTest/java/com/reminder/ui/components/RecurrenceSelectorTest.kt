package com.reminder.ui.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.reminder.data.entity.RecurrencePattern
import org.junit.Rule
import org.junit.Test
import java.time.DayOfWeek

/**
 * v1.63.1: RecurrenceSelector UI 테스트
 *
 * 한글화 검증 및 UI 동작 테스트
 */
class RecurrenceSelectorTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    /**
     * 반복 설정 제목 한글 표시 확인
     */
    @Test
    fun recurrenceSettingsTitleIsDisplayedInKorean() {
        // Given
        composeTestRule.setContent {
            RecurrenceSelector(
                recurrencePattern = RecurrencePattern.NONE,
                onPatternChange = {},
                recurrenceInterval = 1,
                onIntervalChange = {},
                recurrenceDaysOfWeek = null,
                onDaysOfWeekChange = {},
                recurrenceEndDate = null,
                onEndDateChange = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("반복 설정").assertExists()
    }

    /**
     * 반복 드롭다운 라벨 한글 표시 확인
     */
    @Test
    fun recurrenceDropdownLabelIsDisplayedInKorean() {
        // Given
        composeTestRule.setContent {
            RecurrenceSelector(
                recurrencePattern = RecurrencePattern.NONE,
                onPatternChange = {},
                recurrenceInterval = 1,
                onIntervalChange = {},
                recurrenceDaysOfWeek = null,
                onDaysOfWeekChange = {},
                recurrenceEndDate = null,
                onEndDateChange = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("반복").assertExists()
    }

    /**
     * 반복 안 함이 기본값으로 표시되는지 확인
     */
    @Test
    fun nonePatternIsDisplayedAsDefault() {
        // Given
        composeTestRule.setContent {
            RecurrenceSelector(
                recurrencePattern = RecurrencePattern.NONE,
                onPatternChange = {},
                recurrenceInterval = 1,
                onIntervalChange = {},
                recurrenceDaysOfWeek = null,
                onDaysOfWeekChange = {},
                recurrenceEndDate = null,
                onEndDateChange = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("반복 안 함").assertExists()
    }

    /**
     * 매일 패턴 한글 표시 확인
     */
    @Test
    fun dailyPatternIsDisplayedInKorean() {
        // Given
        composeTestRule.setContent {
            RecurrenceSelector(
                recurrencePattern = RecurrencePattern.DAILY,
                onPatternChange = {},
                recurrenceInterval = 1,
                onIntervalChange = {},
                recurrenceDaysOfWeek = null,
                onDaysOfWeekChange = {},
                recurrenceEndDate = null,
                onEndDateChange = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("매일").assertExists()
    }

    /**
     * 매주 패턴 한글 표시 확인
     */
    @Test
    fun weeklyPatternIsDisplayedInKorean() {
        // Given
        composeTestRule.setContent {
            RecurrenceSelector(
                recurrencePattern = RecurrencePattern.WEEKLY,
                onPatternChange = {},
                recurrenceInterval = 1,
                onIntervalChange = {},
                recurrenceDaysOfWeek = null,
                onDaysOfWeekChange = {},
                recurrenceEndDate = null,
                onEndDateChange = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("매주").assertExists()
    }

    /**
     * 매월 패턴 한글 표시 확인
     */
    @Test
    fun monthlyPatternIsDisplayedInKorean() {
        // Given
        composeTestRule.setContent {
            RecurrenceSelector(
                recurrencePattern = RecurrencePattern.MONTHLY,
                onPatternChange = {},
                recurrenceInterval = 1,
                onIntervalChange = {},
                recurrenceDaysOfWeek = null,
                onDaysOfWeekChange = {},
                recurrenceEndDate = null,
                onEndDateChange = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("매월").assertExists()
    }

    /**
     * 매년 패턴 한글 표시 확인
     */
    @Test
    fun yearlyPatternIsDisplayedInKorean() {
        // Given
        composeTestRule.setContent {
            RecurrenceSelector(
                recurrencePattern = RecurrencePattern.YEARLY,
                onPatternChange = {},
                recurrenceInterval = 1,
                onIntervalChange = {},
                recurrenceDaysOfWeek = null,
                onDaysOfWeekChange = {},
                recurrenceEndDate = null,
                onEndDateChange = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("매년").assertExists()
    }

    /**
     * 매일 간격 단위 한글 표시 확인
     */
    @Test
    fun dailyIntervalUnitIsDisplayedInKorean() {
        // Given
        composeTestRule.setContent {
            RecurrenceSelector(
                recurrencePattern = RecurrencePattern.DAILY,
                onPatternChange = {},
                recurrenceInterval = 1,
                onIntervalChange = {},
                recurrenceDaysOfWeek = null,
                onDaysOfWeekChange = {},
                recurrenceEndDate = null,
                onEndDateChange = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("매").assertExists()
        composeTestRule.onNodeWithText("일").assertExists()
    }

    /**
     * 매주 간격 단위 한글 표시 확인
     */
    @Test
    fun weeklyIntervalUnitIsDisplayedInKorean() {
        // Given
        composeTestRule.setContent {
            RecurrenceSelector(
                recurrencePattern = RecurrencePattern.WEEKLY,
                onPatternChange = {},
                recurrenceInterval = 2,
                onIntervalChange = {},
                recurrenceDaysOfWeek = null,
                onDaysOfWeekChange = {},
                recurrenceEndDate = null,
                onEndDateChange = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("매").assertExists()
        composeTestRule.onNodeWithText("주").assertExists()
    }

    /**
     * 매월 간격 단위 한글 표시 확인
     */
    @Test
    fun monthlyIntervalUnitIsDisplayedInKorean() {
        // Given
        composeTestRule.setContent {
            RecurrenceSelector(
                recurrencePattern = RecurrencePattern.MONTHLY,
                onPatternChange = {},
                recurrenceInterval = 3,
                onIntervalChange = {},
                recurrenceDaysOfWeek = null,
                onDaysOfWeekChange = {},
                recurrenceEndDate = null,
                onEndDateChange = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("매").assertExists()
        composeTestRule.onNodeWithText("개월").assertExists()
    }

    /**
     * 매년 간격 단위 한글 표시 확인
     */
    @Test
    fun yearlyIntervalUnitIsDisplayedInKorean() {
        // Given
        composeTestRule.setContent {
            RecurrenceSelector(
                recurrencePattern = RecurrencePattern.YEARLY,
                onPatternChange = {},
                recurrenceInterval = 1,
                onIntervalChange = {},
                recurrenceDaysOfWeek = null,
                onDaysOfWeekChange = {},
                recurrenceEndDate = null,
                onEndDateChange = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("매").assertExists()
        composeTestRule.onNodeWithText("년").assertExists()
    }

    /**
     * 매주 선택 시 반복 요일 라벨 한글 표시 확인
     */
    @Test
    fun recurrenceDaysLabelIsDisplayedInKoreanWhenWeeklySelected() {
        // Given
        composeTestRule.setContent {
            RecurrenceSelector(
                recurrencePattern = RecurrencePattern.WEEKLY,
                onPatternChange = {},
                recurrenceInterval = 1,
                onIntervalChange = {},
                recurrenceDaysOfWeek = setOf(DayOfWeek.MONDAY),
                onDaysOfWeekChange = {},
                recurrenceEndDate = null,
                onEndDateChange = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("반복 요일").assertExists()
    }

    /**
     * 종료일 라벨 한글 표시 확인
     */
    @Test
    fun endDateLabelIsDisplayedInKorean() {
        // Given
        composeTestRule.setContent {
            RecurrenceSelector(
                recurrencePattern = RecurrencePattern.DAILY,
                onPatternChange = {},
                recurrenceInterval = 1,
                onIntervalChange = {},
                recurrenceDaysOfWeek = null,
                onDaysOfWeekChange = {},
                recurrenceEndDate = null,
                onEndDateChange = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("종료일").assertExists()
    }

    /**
     * 종료일 설정 버튼 한글 표시 확인
     */
    @Test
    fun setEndDateButtonIsDisplayedInKorean() {
        // Given
        composeTestRule.setContent {
            RecurrenceSelector(
                recurrencePattern = RecurrencePattern.DAILY,
                onPatternChange = {},
                recurrenceInterval = 1,
                onIntervalChange = {},
                recurrenceDaysOfWeek = null,
                onDaysOfWeekChange = {},
                recurrenceEndDate = null,
                onEndDateChange = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("종료일 설정").assertExists()
    }

    /**
     * 종료일 설정 후 지우기 버튼 한글 표시 확인
     */
    @Test
    fun clearEndDateButtonIsDisplayedInKoreanAfterSettingEndDate() {
        // Given
        val endDate = java.time.LocalDateTime.now().plusDays(7)
        composeTestRule.setContent {
            RecurrenceSelector(
                recurrencePattern = RecurrencePattern.DAILY,
                onPatternChange = {},
                recurrenceInterval = 1,
                onIntervalChange = {},
                recurrenceDaysOfWeek = null,
                onDaysOfWeekChange = {},
                recurrenceEndDate = endDate,
                onEndDateChange = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("지우기").assertExists()
    }

    /**
     * 반복 안 함 선택 시 간격 설정이 표시되지 않는지 확인
     */
    @Test
    fun intervalSettingsAreHiddenWhenNonePatternIsSelected() {
        // Given
        composeTestRule.setContent {
            RecurrenceSelector(
                recurrencePattern = RecurrencePattern.NONE,
                onPatternChange = {},
                recurrenceInterval = 1,
                onIntervalChange = {},
                recurrenceDaysOfWeek = null,
                onDaysOfWeekChange = {},
                recurrenceEndDate = null,
                onEndDateChange = {}
            )
        }

        // Then - "매" 텍스트가 없어야 함
        composeTestRule.onNodeWithText("매").assertDoesNotExist()
    }

    /**
     * 반복 안 함 선택 시 종료일 설정이 표시되지 않는지 확인
     */
    @Test
    fun endDateSettingsAreHiddenWhenNonePatternIsSelected() {
        // Given
        composeTestRule.setContent {
            RecurrenceSelector(
                recurrencePattern = RecurrencePattern.NONE,
                onPatternChange = {},
                recurrenceInterval = 1,
                onIntervalChange = {},
                recurrenceDaysOfWeek = null,
                onDaysOfWeekChange = {},
                recurrenceEndDate = null,
                onEndDateChange = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("종료일").assertDoesNotExist()
    }
}
