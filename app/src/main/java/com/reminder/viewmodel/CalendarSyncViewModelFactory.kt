package com.reminder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.reminder.calendar.CalendarSyncManager
import com.reminder.calendar.DeviceCalendarProvider
import com.reminder.data.dao.ReminderDao

/**
 * v1.40.1: CalendarSyncViewModel Factory
 */
class CalendarSyncViewModelFactory(
    private val calendarSyncManager: CalendarSyncManager,
    private val deviceCalendarProvider: DeviceCalendarProvider,
    private val reminderDao: ReminderDao
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalendarSyncViewModel::class.java)) {
            return CalendarSyncViewModel(
                calendarSyncManager,
                deviceCalendarProvider,
                reminderDao
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
