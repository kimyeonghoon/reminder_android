package com.reminder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.reminder.ReminderApplication

class ReminderViewModelFactory(private val application: ReminderApplication) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReminderViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReminderViewModel(
                application.repository,
                application.alarmScheduler,
                application.database
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
