package com.reminder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.reminder.analytics.AnalyticsHelper
import com.reminder.data.repository.ReminderRepository
import com.reminder.location.LocationManager

/**
 * v1.68.1: LocationViewModel Factory
 */
class LocationViewModelFactory(
    private val locationManager: LocationManager,
    private val repository: ReminderRepository,
    private val analyticsHelper: AnalyticsHelper
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LocationViewModel::class.java)) {
            return LocationViewModel(locationManager, repository, analyticsHelper) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
