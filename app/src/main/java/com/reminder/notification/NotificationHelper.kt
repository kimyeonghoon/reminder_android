package com.reminder.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.reminder.MainActivity
import com.reminder.R
import com.reminder.data.entity.Priority
import com.reminder.data.entity.ReminderEntity

class NotificationHelper(private val context: Context) {

    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val CHANNEL_ID = "reminder_channel"
        const val CHANNEL_NAME = "Reminder Notifications"
        private const val REQUEST_CODE = 100
    }

    /**
     * Android 8.0 이상에서 NotificationChannel 생성
     */
    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for your tasks"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Notification 객체 생성
     */
    fun buildNotification(reminder: ReminderEntity): Notification {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val priority = when (reminder.priority) {
            Priority.HIGH -> NotificationCompat.PRIORITY_HIGH
            Priority.MEDIUM -> NotificationCompat.PRIORITY_DEFAULT
            Priority.LOW -> NotificationCompat.PRIORITY_LOW
        }

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(getNotificationIcon(reminder.priority))
            .setContentTitle(reminder.title)
            .setContentText(reminder.description)
            .setPriority(priority)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    /**
     * 알림 표시
     */
    fun showNotification(reminder: ReminderEntity) {
        val notification = buildNotification(reminder)
        notificationManager.notify(reminder.id.toInt(), notification)
    }

    /**
     * 알림 취소
     */
    fun cancelNotification(reminderId: Long) {
        notificationManager.cancel(reminderId.toInt())
    }

    /**
     * 우선순위에 따른 아이콘 선택
     */
    private fun getNotificationIcon(priority: Priority): Int {
        return when (priority) {
            Priority.HIGH -> android.R.drawable.ic_dialog_alert
            Priority.MEDIUM -> android.R.drawable.ic_dialog_info
            Priority.LOW -> android.R.drawable.ic_dialog_email
        }
    }
}
