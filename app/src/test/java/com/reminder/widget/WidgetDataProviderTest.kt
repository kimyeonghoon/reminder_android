package com.reminder.widget

import com.reminder.data.entity.Priority
import com.reminder.data.entity.ReminderEntity
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDateTime

class WidgetDataProviderTest {

    /** 위젯 데이터 제공자는 리마인더 목록을 위젯용 데이터로 변환한다 */
    @Test
    fun widgetDataProviderConvertsRemindersToWidgetData() {
        // Given
        val reminders = listOf(
            ReminderEntity(
                id = 1,
                title = "할일 1",
                description = "설명 1",
                priority = Priority.HIGH,
                isCompleted = false,
                dueDateTime = LocalDateTime.of(2025, 10, 10, 10, 0)
            ),
            ReminderEntity(
                id = 2,
                title = "할일 2",
                description = "설명 2",
                priority = Priority.MEDIUM,
                isCompleted = false,
                dueDateTime = null
            )
        )
        val provider = WidgetDataProvider()

        // When
        val widgetData = provider.prepareWidgetData(reminders)

        // Then
        assertEquals(2, widgetData.size)
        assertEquals("할일 1", widgetData[0].title)
        assertEquals(Priority.HIGH, widgetData[0].priority)
        assertEquals("2025-10-10 10:00", widgetData[0].formattedDueDate)
    }

    /** 완료된 리마인더는 위젯에 표시하지 않는다 */
    @Test
    fun completedRemindersAreNotDisplayedInWidget() {
        // Given
        val reminders = listOf(
            ReminderEntity(
                id = 1,
                title = "할일 1",
                isCompleted = false
            ),
            ReminderEntity(
                id = 2,
                title = "할일 2",
                isCompleted = true
            )
        )
        val provider = WidgetDataProvider()

        // When
        val widgetData = provider.prepareWidgetData(reminders)

        // Then
        assertEquals(1, widgetData.size)
        assertEquals("할일 1", widgetData[0].title)
    }

    /** 마감일이 가까운 순서대로 정렬한다 */
    @Test
    fun sortsRemindersByClosestDueDate() {
        // Given
        val now = LocalDateTime.now()
        val reminders = listOf(
            ReminderEntity(
                id = 1,
                title = "할일 1",
                dueDateTime = now.plusDays(3),
                isCompleted = false
            ),
            ReminderEntity(
                id = 2,
                title = "할일 2",
                dueDateTime = now.plusDays(1),
                isCompleted = false
            ),
            ReminderEntity(
                id = 3,
                title = "할일 3",
                dueDateTime = null,
                isCompleted = false
            )
        )
        val provider = WidgetDataProvider()

        // When
        val widgetData = provider.prepareWidgetData(reminders)

        // Then
        assertEquals("할일 2", widgetData[0].title) // 1일 후
        assertEquals("할일 1", widgetData[1].title) // 3일 후
        assertEquals("할일 3", widgetData[2].title) // 마감일 없음
    }

    /** 최대 10개의 리마인더만 반환한다 */
    @Test
    fun returnsMaximumTenReminders() {
        // Given
        val reminders = (1..15).map { i ->
            ReminderEntity(
                id = i.toLong(),
                title = "할일 $i",
                isCompleted = false,
                dueDateTime = LocalDateTime.now().plusDays(i.toLong())
            )
        }
        val provider = WidgetDataProvider()

        // When
        val widgetData = provider.prepareWidgetData(reminders)

        // Then
        assertEquals(10, widgetData.size)
    }

    /** 마감일이 없는 경우 빈 문자열을 반환한다 */
    @Test
    fun returnsEmptyStringForNoDueDate() {
        // Given
        val reminders = listOf(
            ReminderEntity(
                id = 1,
                title = "할일 1",
                dueDateTime = null,
                isCompleted = false
            )
        )
        val provider = WidgetDataProvider()

        // When
        val widgetData = provider.prepareWidgetData(reminders)

        // Then
        assertEquals("", widgetData[0].formattedDueDate)
    }
}
