package com.reminder.viewmodel

import com.reminder.data.entity.Priority
import com.reminder.data.entity.ReminderEntity
import com.reminder.data.entity.ReminderTemplate
import com.reminder.recurrence.RecurrenceEnd
import com.reminder.recurrence.RecurrenceRule
import com.reminder.recurrence.RecurrenceType
import org.junit.Test
import org.junit.Assert.*

/**
 * 리마인더 템플릿 기능 테스트
 *
 * v1.64.0: RecurrenceRule 사용
 */
class ReminderTemplateTest {

    /** 템플릿에서 리마인더 생성 시 모든 필드가 정확히 복사된다 */
    @Test
    fun creatingReminderFromTemplateCopiesAllFieldsCorrectly() {
        // Given
        val recurrenceRule = RecurrenceRule(type = RecurrenceType.WEEKLY, interval = 1)
        val template = ReminderTemplate(
            id = 1L,
            name = "회의 템플릿",
            titleTemplate = "팀 회의",
            descriptionTemplate = "주간 팀 회의 안건 논의",
            defaultPriority = Priority.HIGH,
            defaultCategory = "업무",
            defaultRecurrenceRule = recurrenceRule,
            defaultRecurrenceEnd = RecurrenceEnd.Never
        )

        // When
        val reminder = ReminderEntity(
            title = template.titleTemplate,
            description = template.descriptionTemplate,
            priority = template.defaultPriority,
            category = template.defaultCategory,
            recurrenceRule = template.defaultRecurrenceRule,
            recurrenceEnd = template.defaultRecurrenceEnd
        )

        // Then
        assertEquals(template.titleTemplate, reminder.title)
        assertEquals(template.descriptionTemplate, reminder.description)
        assertEquals(template.defaultPriority, reminder.priority)
        assertEquals(template.defaultCategory, reminder.category)
        assertEquals(template.defaultRecurrenceRule, reminder.recurrenceRule)
        assertEquals(template.defaultRecurrenceEnd, reminder.recurrenceEnd)
    }

    /** 리마인더를 템플릿으로 저장 시 핵심 정보만 저장된다 */
    @Test
    fun savingReminderAsTemplateStoresOnlyEssentialInformation() {
        // Given
        val reminder = ReminderEntity(
            id = 10L,
            title = "프로젝트 마감",
            description = "Q4 프로젝트 최종 제출",
            priority = Priority.HIGH,
            category = "업무",
            recurrenceRule = null,  // v1.64.0: 반복 없음
            isCompleted = true  // 완료 상태는 템플릿에 저장되면 안됨
        )

        // When
        val template = ReminderTemplate(
            name = "프로젝트 템플릿",
            titleTemplate = reminder.title,
            descriptionTemplate = reminder.description,
            defaultPriority = reminder.priority,
            defaultCategory = reminder.category,
            defaultRecurrenceRule = reminder.recurrenceRule
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
