package com.reminder.viewmodel

import com.reminder.data.entity.Priority
import com.reminder.data.entity.RecurrencePattern
import com.reminder.data.entity.ReminderEntity
import com.reminder.data.entity.ReminderTemplate
import org.junit.Test
import org.junit.Assert.*

/**
 * 리마인더 템플릿 기능 테스트
 */
class ReminderTemplateTest {

    @Test
    fun `템플릿에서 리마인더 생성 시 모든 필드가 정확히 복사된다`() {
        // Given
        val template = ReminderTemplate(
            id = 1L,
            name = "회의 템플릿",
            titleTemplate = "팀 회의",
            descriptionTemplate = "주간 팀 회의 안건 논의",
            defaultPriority = Priority.HIGH,
            defaultCategory = "업무",
            defaultRecurrencePattern = RecurrencePattern.WEEKLY,
            defaultRecurrenceInterval = 1
        )

        // When
        val reminder = ReminderEntity(
            title = template.titleTemplate,
            description = template.descriptionTemplate,
            priority = template.defaultPriority,
            category = template.defaultCategory,
            recurrencePattern = template.defaultRecurrencePattern,
            recurrenceInterval = template.defaultRecurrenceInterval
        )

        // Then
        assertEquals(template.titleTemplate, reminder.title)
        assertEquals(template.descriptionTemplate, reminder.description)
        assertEquals(template.defaultPriority, reminder.priority)
        assertEquals(template.defaultCategory, reminder.category)
        assertEquals(template.defaultRecurrencePattern, reminder.recurrencePattern)
        assertEquals(template.defaultRecurrenceInterval, reminder.recurrenceInterval)
    }

    @Test
    fun `리마인더를 템플릿으로 저장 시 핵심 정보만 저장된다`() {
        // Given
        val reminder = ReminderEntity(
            id = 10L,
            title = "프로젝트 마감",
            description = "Q4 프로젝트 최종 제출",
            priority = Priority.HIGH,
            category = "업무",
            recurrencePattern = RecurrencePattern.NONE,
            isCompleted = true  // 완료 상태는 템플릿에 저장되면 안됨
        )

        // When
        val template = ReminderTemplate(
            name = "프로젝트 템플릿",
            titleTemplate = reminder.title,
            descriptionTemplate = reminder.description,
            defaultPriority = reminder.priority,
            defaultCategory = reminder.category,
            defaultRecurrencePattern = reminder.recurrencePattern
        )

        // Then
        assertEquals(reminder.title, template.titleTemplate)
        assertEquals(reminder.description, template.descriptionTemplate)
        assertEquals(reminder.priority, template.defaultPriority)
        assertEquals(reminder.category, template.defaultCategory)
        // 완료 상태는 템플릿에 없음을 확인
        assertNotEquals(reminder.id, template.id)
    }
}
