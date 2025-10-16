package com.reminder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.reminder.analytics.AnalyticsHelper
import com.reminder.analytics.CompletionPatternAnalyzer
import com.reminder.data.repository.ReminderRepository
import com.reminder.ml.CategorySuggestionHelper

/**
 * v1.68.3: ReminderAnalyticsViewModel용 Factory
 */
class ReminderAnalyticsViewModelFactory(
    private val repository: ReminderRepository,
    private val analyticsHelper: AnalyticsHelper,
    private val categorySuggestionHelper: CategorySuggestionHelper,
    private val completionPatternAnalyzer: CompletionPatternAnalyzer
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReminderAnalyticsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReminderAnalyticsViewModel(repository, analyticsHelper, categorySuggestionHelper, completionPatternAnalyzer) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
