package com.reminder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.reminder.analytics.AnalyticsHelper
import com.reminder.data.dao.SavedFilterDao

/**
 * v1.68.1: FilterViewModel Factory
 */
class FilterViewModelFactory(
    private val savedFilterDao: SavedFilterDao,
    private val analyticsHelper: AnalyticsHelper
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FilterViewModel::class.java)) {
            return FilterViewModel(savedFilterDao, analyticsHelper) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
