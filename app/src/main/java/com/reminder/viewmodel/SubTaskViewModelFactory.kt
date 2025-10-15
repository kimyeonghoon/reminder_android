package com.reminder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.reminder.analytics.AnalyticsHelper
import com.reminder.data.dao.SubTaskDao

/**
 * v1.68.1: SubTaskViewModel Factory
 */
class SubTaskViewModelFactory(
    private val subTaskDao: SubTaskDao,
    private val analyticsHelper: AnalyticsHelper
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SubTaskViewModel::class.java)) {
            return SubTaskViewModel(subTaskDao, analyticsHelper) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
