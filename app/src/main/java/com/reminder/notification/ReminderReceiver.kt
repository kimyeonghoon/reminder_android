package com.reminder.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.reminder.data.entity.Priority
import com.reminder.data.entity.ReminderEntity

/**
 * 알람 시간이 되면 호출되는 BroadcastReceiver
 */
class ReminderReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "ReminderReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Alarm received")

        val reminderId = intent.getLongExtra(AlarmScheduler.EXTRA_REMINDER_ID, -1L)
        val title = intent.getStringExtra(AlarmScheduler.EXTRA_REMINDER_TITLE) ?: "Reminder"
        val description = intent.getStringExtra(AlarmScheduler.EXTRA_REMINDER_DESCRIPTION) ?: ""
        val priorityName = intent.getStringExtra(AlarmScheduler.EXTRA_REMINDER_PRIORITY) ?: Priority.MEDIUM.name

        if (reminderId == -1L) {
            Log.e(TAG, "Invalid reminder ID")
            return
        }

        // Priority enum으로 변환
        val priority = try {
            Priority.valueOf(priorityName)
        } catch (e: IllegalArgumentException) {
            Priority.MEDIUM
        }

        // ReminderEntity 재구성 (간소화된 버전)
        val reminder = ReminderEntity(
            id = reminderId,
            title = title,
            description = description,
            priority = priority
        )

        // NotificationHelper를 사용해서 알림 표시
        val notificationHelper = NotificationHelper(context)
        notificationHelper.createNotificationChannel()
        notificationHelper.showNotification(reminder)

        Log.d(TAG, "Notification shown for reminder: $title")
    }
}
