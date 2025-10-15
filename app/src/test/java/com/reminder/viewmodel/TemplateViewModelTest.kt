package com.reminder.viewmodel

import com.reminder.analytics.AnalyticsHelper
import com.reminder.data.dao.ReminderTemplateDao
import com.reminder.data.entity.Priority
import com.reminder.data.entity.ReminderEntity
import com.reminder.data.entity.ReminderTemplate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.whenever

/**
 * TemplateViewModel 테스트
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TemplateViewModelTest {

    @get:Rule
    val mockitoRule: MockitoRule = MockitoJUnit.rule()

    private val testDispatcher = UnconfinedTestDispatcher()

    @Mock
    private lateinit var reminderTemplateDao: ReminderTemplateDao

    @Mock
    private lateinit var analyticsHelper: AnalyticsHelper

    private lateinit var viewModel: TemplateViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = TemplateViewModel(reminderTemplateDao, analyticsHelper)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getAllTemplates returns flow from dao`() {
        // given: 템플릿 리스트
        val templates = listOf(
            ReminderTemplate(id = 1, name = "Template 1", titleTemplate = "Title 1"),
            ReminderTemplate(id = 2, name = "Template 2", titleTemplate = "Title 2")
        )
        whenever(reminderTemplateDao.getAllTemplates()).thenReturn(flowOf(templates))

        // when: getAllTemplates 호출
        viewModel.getAllTemplates()

        // then: dao 메서드가 호출됨
        verify(reminderTemplateDao).getAllTemplates()
    }

    @Test
    fun `addTemplate inserts new template`() = runTest(testDispatcher) {
        // given: 템플릿 정보
        val name = "Daily Task"
        val titleTemplate = "Complete {{task}}"
        val descriptionTemplate = "Don't forget to {{action}}"
        val defaultPriority = Priority.HIGH
        val defaultCategory = "Work"

        // when: addTemplate 호출
        viewModel.addTemplate(
            name = name,
            titleTemplate = titleTemplate,
            descriptionTemplate = descriptionTemplate,
            defaultPriority = defaultPriority,
            defaultCategory = defaultCategory
        )
        testScheduler.advanceUntilIdle()

        // then: dao insert 메서드 호출 및 analytics 로깅
        val captor = argumentCaptor<ReminderTemplate>()
        verify(reminderTemplateDao).insert(captor.capture())
        val insertedTemplate = captor.firstValue

        assert(insertedTemplate.name == name)
        assert(insertedTemplate.titleTemplate == titleTemplate)
        assert(insertedTemplate.descriptionTemplate == descriptionTemplate)
        assert(insertedTemplate.defaultPriority == defaultPriority)
        assert(insertedTemplate.defaultCategory == defaultCategory)
        verify(analyticsHelper).logTemplateCreated()
    }

    @Test
    fun `deleteTemplate deletes template from dao`() = runTest(testDispatcher) {
        // given: 템플릿
        val template = ReminderTemplate(
            id = 1,
            name = "Template 1",
            titleTemplate = "Title 1"
        )

        // when: deleteTemplate 호출
        viewModel.deleteTemplate(template)
        testScheduler.advanceUntilIdle()

        // then: dao delete 메서드 호출
        verify(reminderTemplateDao).delete(template)
    }

    @Test
    fun `saveAsTemplate creates template from reminder`() = runTest(testDispatcher) {
        // given: 리마인더와 템플릿 이름
        val reminder = ReminderEntity(
            id = 1,
            title = "Example Reminder",
            description = "Example Description",
            priority = Priority.MEDIUM,
            category = "Personal",
            recurrenceRule = null,
            recurrenceEnd = null
        )
        val templateName = "My Template"

        // when: saveAsTemplate 호출
        viewModel.saveAsTemplate(reminder, templateName)
        testScheduler.advanceUntilIdle()

        // then: dao insert 메서드 호출
        val captor = argumentCaptor<ReminderTemplate>()
        verify(reminderTemplateDao).insert(captor.capture())
        val createdTemplate = captor.firstValue

        assert(createdTemplate.name == templateName)
        assert(createdTemplate.titleTemplate == reminder.title)
        assert(createdTemplate.descriptionTemplate == reminder.description)
        assert(createdTemplate.defaultPriority == reminder.priority)
        assert(createdTemplate.defaultCategory == reminder.category)
    }
}
