package com.reminder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.reminder.analytics.AnalyticsHelper

/**
 * v1.68.3: ReminderSearchViewModel용 Factory
 */
class ReminderSearchViewModelFactory(
    private val analyticsHelper: AnalyticsHelper
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReminderSearchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReminderSearchViewModel(analyticsHelper) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
