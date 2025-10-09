package com.reminder.ui.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.reminder.data.entity.Priority
import com.reminder.data.entity.RecurrencePattern
import com.reminder.data.entity.ReminderEntity
import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime

/**
 * ReminderCard 컴포넌트 UI 테스트
 */
class ReminderCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun reminderCard_공유버튼_표시됨() {
        // Given
        val testReminder = ReminderEntity(
            id = 1L,
            title = "테스트 리마인더",
            description = "테스트 설명",
            dueDateTime = LocalDateTime.of(2025, 10, 10, 10, 0),
            priority = Priority.HIGH,
            category = "업무",
            isCompleted = false,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            recurrencePattern = RecurrencePattern.NONE,
            recurrenceInterval = 1,
            recurrenceDaysOfWeek = null,
            recurrenceEndDate = null
        )

        var shareClicked = false

        // When
        composeTestRule.setContent {
            ReminderCard(
                reminder = testReminder,
                onCheckedChange = {},
                onDelete = {},
                onClick = {},
                onShare = { shareClicked = true }
            )
        }

        // Then
        composeTestRule.onNodeWithContentDescription("공유").assertIsDisplayed()
    }

    @Test
    fun reminderCard_공유버튼_클릭_동작() {
        // Given
        val testReminder = ReminderEntity(
            id = 1L,
            title = "공유 테스트",
            description = "",
            dueDateTime = null,
            priority = Priority.MEDIUM,
            category = "",
            isCompleted = false,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            recurrencePattern = RecurrencePattern.NONE,
            recurrenceInterval = 1,
            recurrenceDaysOfWeek = null,
            recurrenceEndDate = null
        )

        var shareClicked = false

        // When
        composeTestRule.setContent {
            ReminderCard(
                reminder = testReminder,
                onCheckedChange = {},
                onDelete = {},
                onClick = {},
                onShare = { shareClicked = true }
            )
        }

        // 공유 버튼 클릭
        composeTestRule.onNodeWithContentDescription("공유").performClick()

        // Then
        assert(shareClicked) { "공유 버튼이 클릭되지 않았습니다" }
    }

    @Test
    fun reminderCard_공유버튼_없음_표시안됨() {
        // Given
        val testReminder = ReminderEntity(
            id = 1L,
            title = "공유 없음 테스트",
            description = "",
            dueDateTime = null,
            priority = Priority.LOW,
            category = "",
            isCompleted = false,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            recurrencePattern = RecurrencePattern.NONE,
            recurrenceInterval = 1,
            recurrenceDaysOfWeek = null,
            recurrenceEndDate = null
        )

        // When - onShare를 null로 전달
        composeTestRule.setContent {
            ReminderCard(
                reminder = testReminder,
                onCheckedChange = {},
                onDelete = {},
                onClick = {}
            )
        }

        // Then - 공유 버튼이 표시되지 않음
        composeTestRule.onNodeWithContentDescription("공유").assertDoesNotExist()
    }

    @Test
    fun reminderCard_서브태스크_진행률_표시() {
        // Given
        val testReminder = ReminderEntity(
            id = 1L,
            title = "서브태스크 테스트",
            description = "",
            dueDateTime = null,
            priority = Priority.MEDIUM,
            category = "",
            isCompleted = false,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            recurrencePattern = RecurrencePattern.NONE,
            recurrenceInterval = 1,
            recurrenceDaysOfWeek = null,
            recurrenceEndDate = null
        )

        val subTaskProgress = Pair(2, 5) // 2/5 완료

        // When
        composeTestRule.setContent {
            ReminderCard(
                reminder = testReminder,
                onCheckedChange = {},
                onDelete = {},
                onClick = {},
                subTaskProgress = subTaskProgress
            )
        }

        // Then
        composeTestRule.onNodeWithText("2/5").assertIsDisplayed()
    }
}
