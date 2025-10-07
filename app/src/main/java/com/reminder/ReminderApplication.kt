package com.reminder

import android.app.Application
import com.reminder.data.database.ReminderDatabase
import com.reminder.data.repository.ReminderRepository
import com.reminder.notification.AlarmScheduler
import com.reminder.notification.NotificationHelper

class ReminderApplication : Application() {
    val database by lazy { ReminderDatabase.getDatabase(this) }
    val repository by lazy { ReminderRepository(database.reminderDao()) }
    val alarmScheduler by lazy { AlarmScheduler(this) }
    val notificationHelper by lazy { NotificationHelper(this) }

    override fun onCreate() {
        super.onCreate()
        // NotificationChannel 생성
        notificationHelper.createNotificationChannel()
    }
}
