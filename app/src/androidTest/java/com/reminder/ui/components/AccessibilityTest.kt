package com.reminder.ui.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.reminder.data.entity.Priority
import com.reminder.data.entity.ReminderEntity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime

@RunWith(AndroidJUnit4::class)
class AccessibilityTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun reminderCard_checkbox_hasContentDescription() {
        // Given
        val reminder = ReminderEntity(
            id = 1L,
            title = "Test Reminder",
            description = "Test Description",
            priority = Priority.HIGH,
            category = "Work",
            isCompleted = false,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        // When
        composeTestRule.setContent {
            ReminderCard(
                reminder = reminder,
                onCheckedChange = {},
                onDelete = {},
                onClick = {}
            )
        }

        // Then - Checkbox should have content description
        composeTestRule
            .onNode(hasContentDescription("완료 여부 체크박스"))
            .assertExists()
    }

    @Test
    fun reminderCard_deleteButton_hasContentDescription() {
        // Given
        val reminder = ReminderEntity(
            id = 1L,
            title = "Test Reminder",
            description = "",
            priority = Priority.MEDIUM,
            category = "",
            isCompleted = false,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        // When
        composeTestRule.setContent {
            ReminderCard(
                reminder = reminder,
                onCheckedChange = {},
                onDelete = {},
                onClick = {}
            )
        }

        // Then - Delete button should have content description
        composeTestRule
            .onNodeWithContentDescription("삭제")
            .assertExists()
    }

    @Test
    fun reminderCard_priorityIndicator_hasSemanticDescription() {
        // Given
        val reminder = ReminderEntity(
            id = 1L,
            title = "High Priority Task",
            description = "",
            priority = Priority.HIGH,
            category = "",
            isCompleted = false,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        // When
        composeTestRule.setContent {
            ReminderCard(
                reminder = reminder,
                onCheckedChange = {},
                onDelete = {},
                onClick = {}
            )
        }

        // Then - Priority indicator should have semantic description
        composeTestRule
            .onNode(hasContentDescription("우선순위: 높음"))
            .assertExists()
    }

    @Test
    fun reminderCard_card_hasSemanticInformation() {
        // Given
        val reminder = ReminderEntity(
            id = 1L,
            title = "Test Task",
            description = "Test Description",
            priority = Priority.LOW,
            category = "Personal",
            isCompleted = false,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        // When
        composeTestRule.setContent {
            ReminderCard(
                reminder = reminder,
                onCheckedChange = {},
                onDelete = {},
                onClick = {}
            )
        }

        // Then - Card should be clickable with proper semantics
        composeTestRule
            .onNodeWithText("Test Task")
            .assertHasClickAction()
    }

    @Test
    fun priorityIndicator_highPriority_hasCorrectDescription() {
        // Given & When
        composeTestRule.setContent {
            PriorityIndicator(priority = Priority.HIGH)
        }

        // Then
        composeTestRule
            .onNode(hasContentDescription("우선순위: 높음"))
            .assertExists()
    }

    @Test
    fun priorityIndicator_mediumPriority_hasCorrectDescription() {
        // Given & When
        composeTestRule.setContent {
            PriorityIndicator(priority = Priority.MEDIUM)
        }

        // Then
        composeTestRule
            .onNode(hasContentDescription("우선순위: 중간"))
            .assertExists()
    }

    @Test
    fun priorityIndicator_lowPriority_hasCorrectDescription() {
        // Given & When
        composeTestRule.setContent {
            PriorityIndicator(priority = Priority.LOW)
        }

        // Then
        composeTestRule
            .onNode(hasContentDescription("우선순위: 낮음"))
            .assertExists()
    }
}
