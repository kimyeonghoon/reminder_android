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
     * 기본 UI 요소 확인 - 한글 텍스트
     */
    @Test
    fun 반복_설정_제목이_한글로_표시된다() {
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
     * 반복 패턴 드롭다운 - 한글 라벨
     */
    @Test
    fun 반복_드롭다운_라벨이_한글로_표시된다() {
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
     * 반복 안 함 - 기본값
     */
    @Test
    fun 반복_안_함이_기본값으로_표시된다() {
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
     * 매일 패턴 선택 시 한글 표시
     */
    @Test
    fun 매일_패턴이_한글로_표시된다() {
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
     * 매주 패턴 선택 시 한글 표시
     */
    @Test
    fun 매주_패턴이_한글로_표시된다() {
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
     * 매월 패턴 선택 시 한글 표시
     */
    @Test
    fun 매월_패턴이_한글로_표시된다() {
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
     * 매년 패턴 선택 시 한글 표시
     */
    @Test
    fun 매년_패턴이_한글로_표시된다() {
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
     * 간격 설정 - 매일 단위 한글 표시
     */
    @Test
    fun 매일_간격_단위가_한글로_표시된다() {
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
     * 간격 설정 - 매주 단위 한글 표시
     */
    @Test
    fun 매주_간격_단위가_한글로_표시된다() {
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
     * 간격 설정 - 매월 단위 한글 표시
     */
    @Test
    fun 매월_간격_단위가_한글로_표시된다() {
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
     * 간격 설정 - 매년 단위 한글 표시
     */
    @Test
    fun 매년_간격_단위가_한글로_표시된다() {
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
     * 반복 요일 라벨 - 한글 표시
     */
    @Test
    fun 매주_선택_시_반복_요일_라벨이_한글로_표시된다() {
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
     * 종료일 라벨 - 한글 표시
     */
    @Test
    fun 종료일_라벨이_한글로_표시된다() {
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
     * 종료일 설정 버튼 - 한글 표시
     */
    @Test
    fun 종료일_설정_버튼이_한글로_표시된다() {
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
     * 종료일 지우기 버튼 - 한글 표시
     */
    @Test
    fun 종료일_설정_후_지우기_버튼이_한글로_표시된다() {
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
     * 반복 안 함 선택 시 간격 설정 숨김
     */
    @Test
    fun 반복_안_함_선택_시_간격_설정이_표시되지_않는다() {
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
     * 반복 안 함 선택 시 종료일 설정 숨김
     */
    @Test
    fun 반복_안_함_선택_시_종료일_설정이_표시되지_않는다() {
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
