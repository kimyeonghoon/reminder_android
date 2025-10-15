package com.reminder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reminder.analytics.AnalyticsHelper
import com.reminder.data.dao.ReminderTemplateDao
import com.reminder.data.entity.Priority
import com.reminder.data.entity.ReminderEntity
import com.reminder.data.entity.ReminderTemplate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * v1.68.1: Template 전용 ViewModel
 *
 * ReminderViewModel에서 분리됨 (God Class 리팩토링)
 * 템플릿 CRUD 기능 담당
 */
class TemplateViewModel(
    private val reminderTemplateDao: ReminderTemplateDao,
    private val analyticsHelper: AnalyticsHelper
) : ViewModel() {

    /**
     * 모든 템플릿 조회
     */
    fun getAllTemplates(): Flow<List<ReminderTemplate>> {
        return reminderTemplateDao.getAllTemplates()
    }

    /**
     * 템플릿 추가
     *
     * v1.64.0: RecurrenceRule 사용
     */
    fun addTemplate(
        name: String,
        titleTemplate: String,
        descriptionTemplate: String = "",
        defaultPriority: Priority = Priority.MEDIUM,
        defaultCategory: String = "",
        defaultRecurrenceRule: com.reminder.recurrence.RecurrenceRule? = null,
        defaultRecurrenceEnd: com.reminder.recurrence.RecurrenceEnd? = null
    ) {
        viewModelScope.launch {
            val template = ReminderTemplate(
                name = name,
                titleTemplate = titleTemplate,
                descriptionTemplate = descriptionTemplate,
                defaultPriority = defaultPriority,
                defaultCategory = defaultCategory,
                defaultRecurrenceRule = defaultRecurrenceRule,
                defaultRecurrenceEnd = defaultRecurrenceEnd
            )
            reminderTemplateDao.insert(template)

            // Analytics 이벤트 로깅
            analyticsHelper.logTemplateCreated()
        }
    }

    /**
     * 템플릿 삭제
     */
    fun deleteTemplate(template: ReminderTemplate) {
        viewModelScope.launch {
            reminderTemplateDao.delete(template)
        }
    }

    /**
     * 현재 리마인더를 템플릿으로 저장
     *
     * v1.64.0: RecurrenceRule 사용
     */
    fun saveAsTemplate(
        reminder: ReminderEntity,
        templateName: String
    ) {
        viewModelScope.launch {
            val template = ReminderTemplate(
                name = templateName,
                titleTemplate = reminder.title,
                descriptionTemplate = reminder.description,
                defaultPriority = reminder.priority,
                defaultCategory = reminder.category,
                defaultRecurrenceRule = reminder.recurrenceRule,
                defaultRecurrenceEnd = reminder.recurrenceEnd
            )
            reminderTemplateDao.insert(template)
        }
    }
}
