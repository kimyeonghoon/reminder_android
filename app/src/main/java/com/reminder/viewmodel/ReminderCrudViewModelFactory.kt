package com.reminder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.reminder.analytics.AnalyticsHelper
import com.reminder.data.repository.ReminderRepository
import com.reminder.location.LocationManager
import com.reminder.notification.AlarmScheduler

/**
 * v1.68.3: ReminderCrudViewModel용 Factory
 */
class ReminderCrudViewModelFactory(
    private val repository: ReminderRepository,
    private val alarmScheduler: AlarmScheduler,
    private val locationManager: LocationManager,
    private val analyticsHelper: AnalyticsHelper
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReminderCrudViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReminderCrudViewModel(repository, alarmScheduler, locationManager, analyticsHelper) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
