package com.reminder.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.reminder.MainActivity
import com.reminder.R
import com.reminder.data.entity.Priority
import com.reminder.data.entity.ReminderEntity
import com.reminder.data.preferences.PreferencesRepository
import com.reminder.data.preferences.UserPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class NotificationHelper(private val context: Context) {

    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val preferencesRepository = PreferencesRepository.create(context)

    companion object {
        const val CHANNEL_ID = "reminder_channel"
        const val CHANNEL_NAME = "Reminder Notifications"
        private const val REQUEST_CODE = 100
    }

    /**
     * Android 8.0 이상에서 NotificationChannel 생성
     * 사용자 설정에 따라 소리, 진동, LED 설정
     */
    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 사용자 설정 읽기
            val userPreferences = runBlocking {
                preferencesRepository.userPreferences.first()
            }

            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for your tasks"

                // LED 설정
                if (userPreferences.notificationLed) {
                    enableLights(true)
                    lightColor = android.graphics.Color.BLUE
                } else {
                    enableLights(false)
                }

                // 진동 설정
                if (userPreferences.notificationVibration) {
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 250, 250, 250)
                } else {
                    enableVibration(false)
                }

                // 소리 설정 (채널에서는 기본 소리만 사용)
                if (!userPreferences.notificationSound) {
                    setSound(null, null)
                }
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Notification 객체 생성
     * 사용자 설정에 따라 소리, 진동 적용
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

        // 사용자 설정 읽기
        val userPreferences = runBlocking {
            preferencesRepository.userPreferences.first()
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(getNotificationIcon(reminder.priority))
            .setContentTitle(reminder.title)
            .setContentText(reminder.description)
            .setPriority(priority)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        // Android O 미만에서는 개별 알림에 설정 적용
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            // 소리 설정
            if (userPreferences.notificationSound) {
                val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                builder.setSound(defaultSoundUri)
            }

            // 진동 설정
            if (userPreferences.notificationVibration) {
                builder.setVibrate(longArrayOf(0, 250, 250, 250))
            }

            // LED 설정
            if (userPreferences.notificationLed) {
                builder.setLights(android.graphics.Color.BLUE, 1000, 1000)
            }
        }

        return builder.build()
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
