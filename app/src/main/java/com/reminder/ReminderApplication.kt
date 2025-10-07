package com.reminder

import android.app.Application
import com.reminder.data.database.ReminderDatabase
import com.reminder.data.repository.ReminderRepository

class ReminderApplication : Application() {
    val database by lazy { ReminderDatabase.getDatabase(this) }
    val repository by lazy { ReminderRepository(database.reminderDao()) }
}
