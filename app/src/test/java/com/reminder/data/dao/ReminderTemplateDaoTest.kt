package com.reminder.data.dao

import com.reminder.data.entity.Priority
import com.reminder.data.entity.ReminderTemplate
import com.reminder.recurrence.RecurrenceEnd
import com.reminder.recurrence.RecurrenceRule
import com.reminder.recurrence.RecurrenceType
import java.time.DayOfWeek
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.time.LocalDateTime
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class ReminderTemplateDaoTest {

    private lateinit var dao: ReminderTemplateDao

    @Before
    fun setup() {
        dao = mock()
    }

    /** getAllTemplates는 모든 템플릿을 이름 순으로 정렬하여 Flow로 반환한다 */
    @Test
    fun testGetAllTemplatesReturnsAllTemplatesSortedByName() = runTest {
        // Given
        val templates = listOf(
            ReminderTemplate(id = 1, name = "회의 템플릿", titleTemplate = "회의"),
            ReminderTemplate(id = 2, name = "운동 템플릿", titleTemplate = "운동")
        )
        whenever(dao.getAllTemplates()).thenReturn(flowOf(templates))

        // When
        dao.getAllTemplates()

        // Then
        verify(dao).getAllTemplates()
    }

    /** getAllTemplates는 빈 리스트를 Flow로 반환할 수 있다 */
    @Test
    fun testGetAllTemplatesReturnsEmptyFlowWhenNoTemplates() = runTest {
        // Given
        whenever(dao.getAllTemplates()).thenReturn(flowOf(emptyList()))

        // When
        dao.getAllTemplates()

        // Then
        verify(dao).getAllTemplates()
    }

    /** getTemplateById는 ID로 템플릿을 조회한다 */
    @Test
    fun testGetTemplateByIdReturnsTemplateWhenExists() = runTest {
        // Given
        val templateId = 1L
        val template = ReminderTemplate(
            id = templateId,
            name = "업무 템플릿",
            titleTemplate = "업무 처리",
            descriptionTemplate = "업무 관련 일정",
            defaultPriority = Priority.HIGH,
            defaultCategory = "업무"
        )
        whenever(dao.getTemplateById(templateId)).thenReturn(template)

        // When
        val result = dao.getTemplateById(templateId)

        // Then
        verify(dao).getTemplateById(templateId)
        assertEquals(template, result)
    }

    /** getTemplateById는 존재하지 않는 ID로 조회 시 null을 반환한다 */
    @Test
    fun testGetTemplateByIdReturnsNullWhenNotExists() = runTest {
        // Given
        val templateId = 999L
        whenever(dao.getTemplateById(templateId)).thenReturn(null)

        // When
        val result = dao.getTemplateById(templateId)

        // Then
        verify(dao).getTemplateById(templateId)
        assertNull(result)
    }

    /** insert는 템플릿을 추가하고 생성된 ID를 반환한다 */
    @Test
    fun testInsertAddsTemplateAndReturnsId() = runTest {
        // Given
        val template = ReminderTemplate(
            name = "새 템플릿",
            titleTemplate = "새로운 작업",
            descriptionTemplate = "템플릿 설명",
            defaultPriority = Priority.MEDIUM,
            defaultCategory = "개인"
        )
        val insertedId = 5L
        whenever(dao.insert(template)).thenReturn(insertedId)

        // When
        val result = dao.insert(template)

        // Then
        verify(dao).insert(template)
        assertEquals(insertedId, result)
    }

    /** insert는 반복 규칙을 포함한 템플릿을 추가할 수 있다 */
    @Test
    fun testInsertAddsTemplateWithRecurrenceRule() = runTest {
        // Given
        val recurrenceRule = RecurrenceRule(
            type = RecurrenceType.DAILY,
            interval = 1
        )
        val recurrenceEnd = RecurrenceEnd.AfterOccurrences(count = 10)
        val template = ReminderTemplate(
            name = "반복 템플릿",
            titleTemplate = "일일 작업",
            defaultRecurrenceRule = recurrenceRule,
            defaultRecurrenceEnd = recurrenceEnd
        )
        val insertedId = 3L
        whenever(dao.insert(template)).thenReturn(insertedId)

        // When
        val result = dao.insert(template)

        // Then
        verify(dao).insert(template)
        assertEquals(insertedId, result)
    }

    /** update는 템플릿을 업데이트한다 */
    @Test
    fun testUpdateModifiesExistingTemplate() = runTest {
        // Given
        val template = ReminderTemplate(
            id = 1,
            name = "수정된 템플릿",
            titleTemplate = "수정된 제목",
            descriptionTemplate = "수정된 설명",
            defaultPriority = Priority.LOW,
            updatedAt = LocalDateTime.now()
        )

        // When
        dao.update(template)

        // Then
        verify(dao).update(template)
    }

    /** update는 템플릿의 반복 규칙을 수정할 수 있다 */
    @Test
    fun testUpdateModifiesTemplateRecurrenceRule() = runTest {
        // Given
        val updatedRule = RecurrenceRule(
            type = RecurrenceType.WEEKLY,
            interval = 2,
            daysOfWeek = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
        )
        val template = ReminderTemplate(
            id = 1,
            name = "템플릿",
            titleTemplate = "주간 작업",
            defaultRecurrenceRule = updatedRule,
            updatedAt = LocalDateTime.now()
        )

        // When
        dao.update(template)

        // Then
        verify(dao).update(template)
    }

    /** delete는 템플릿을 삭제한다 */
    @Test
    fun testDeleteRemovesTemplate() = runTest {
        // Given
        val template = ReminderTemplate(
            id = 1,
            name = "삭제할 템플릿",
            titleTemplate = "삭제"
        )

        // When
        dao.delete(template)

        // Then
        verify(dao).delete(template)
    }

    /** deleteAll은 모든 템플릿을 삭제한다 */
    @Test
    fun testDeleteAllRemovesAllTemplates() = runTest {
        // When
        dao.deleteAll()

        // Then
        verify(dao).deleteAll()
    }
}
