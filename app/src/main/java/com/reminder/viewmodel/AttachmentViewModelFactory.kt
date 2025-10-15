package com.reminder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.reminder.analytics.AnalyticsHelper
import com.reminder.data.dao.ReminderImageDao

/**
 * v1.68.1: AttachmentViewModel Factory
 */
class AttachmentViewModelFactory(
    private val reminderImageDao: ReminderImageDao,
    private val analyticsHelper: AnalyticsHelper
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AttachmentViewModel::class.java)) {
            return AttachmentViewModel(reminderImageDao, analyticsHelper) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
