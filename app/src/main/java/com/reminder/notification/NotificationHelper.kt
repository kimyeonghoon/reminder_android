package com.reminder.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.net.Uri
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
import java.io.InputStream

class NotificationHelper(private val context: Context) {

    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val preferencesRepository = PreferencesRepository.create(context)

    companion object {
        const val CHANNEL_ID = "reminder_channel"
        const val CHANNEL_NAME = "Reminder Notifications"
        private const val REQUEST_CODE = 100

        // v1.29.0: 액션 요청 코드
        private const val ACTION_COMPLETE = 101
        private const val ACTION_SNOOZE = 102
        private const val ACTION_VIEW = 103
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
     * v1.29.0: 모든 알림 채널 생성 (우선순위별)
     * Android 8.0+ 에서 우선순위별로 다른 채널 사용
     */
    fun createAllNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val userPreferences = runBlocking {
                preferencesRepository.userPreferences.first()
            }

            ReminderNotificationChannel.getAllChannels().forEach { channelType ->
                val channel = NotificationChannel(
                    channelType.channelId,
                    channelType.channelName,
                    channelType.importance
                ).apply {
                    description = channelType.description

                    // LED 설정
                    if (userPreferences.notificationLed) {
                        enableLights(true)
                        lightColor = android.graphics.Color.BLUE
                    } else {
                        enableLights(false)
                    }

                    // 진동 설정 (낮은 우선순위는 진동 없음)
                    if (userPreferences.notificationVibration &&
                        channelType != ReminderNotificationChannel.LOW_PRIORITY
                    ) {
                        enableVibration(true)
                        vibrationPattern = longArrayOf(0, 250, 250, 250)
                    } else {
                        enableVibration(false)
                    }

                    // 소리 설정 (낮은 우선순위는 소리 없음)
                    if (!userPreferences.notificationSound ||
                        channelType == ReminderNotificationChannel.LOW_PRIORITY
                    ) {
                        setSound(null, null)
                    }
                }
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    /**
     * Notification 객체 생성
     * v1.29.0: 우선순위별로 다른 채널 사용
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

        // v1.29.0: 우선순위별 채널 ID 선택
        val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ReminderNotificationChannel.fromPriority(reminder.priority).channelId
        } else {
            CHANNEL_ID
        }

        // 사용자 설정 읽기
        val userPreferences = runBlocking {
            preferencesRepository.userPreferences.first()
        }

        val builder = NotificationCompat.Builder(context, channelId)
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
     * v1.29.0: 리치 알림 생성 (BigPictureStyle)
     * 이미지가 첨부된 경우 큰 이미지로 표시
     */
    fun buildRichNotification(reminder: ReminderEntity): Notification {
        val baseNotification = buildNotification(reminder)

        // 이미지 URI가 있는 경우 BigPictureStyle 적용
        val imageUri = reminder.imageUri
        if (imageUri.isNullOrBlank()) {
            return baseNotification
        }

        val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ReminderNotificationChannel.fromPriority(reminder.priority).channelId
        } else {
            CHANNEL_ID
        }

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

        // 이미지 로드
        val bitmap = try {
            loadBitmapFromUri(imageUri)
        } catch (e: Exception) {
            null
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(getNotificationIcon(reminder.priority))
            .setContentTitle(reminder.title)
            .setContentText(reminder.description)
            .setPriority(priority)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        // BigPictureStyle 적용
        if (bitmap != null) {
            builder.setStyle(
                NotificationCompat.BigPictureStyle()
                    .bigPicture(bitmap)
                    .bigLargeIcon(null as Bitmap?) // 확장 시 large icon 숨김
            )
            builder.setLargeIcon(bitmap)
        }

        return builder.build()
    }

    /**
     * v1.29.0: 액션 버튼이 포함된 알림 생성
     * "완료", "1시간 후", "보기" 버튼 추가
     */
    fun buildNotificationWithActions(reminder: ReminderEntity): Notification {
        val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ReminderNotificationChannel.fromPriority(reminder.priority).channelId
        } else {
            CHANNEL_ID
        }

        val viewIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val viewPendingIntent = PendingIntent.getActivity(
            context,
            ACTION_VIEW,
            viewIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val priority = when (reminder.priority) {
            Priority.HIGH -> NotificationCompat.PRIORITY_HIGH
            Priority.MEDIUM -> NotificationCompat.PRIORITY_DEFAULT
            Priority.LOW -> NotificationCompat.PRIORITY_LOW
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(getNotificationIcon(reminder.priority))
            .setContentTitle(reminder.title)
            .setContentText(reminder.description)
            .setPriority(priority)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(viewPendingIntent)

        // 액션 버튼 추가
        // TODO: 실제 BroadcastReceiver 구현 필요
        builder.addAction(
            android.R.drawable.ic_menu_delete,
            "완료",
            createActionPendingIntent(ACTION_COMPLETE, reminder.id)
        )

        builder.addAction(
            android.R.drawable.ic_menu_recent_history,
            "1시간 후",
            createActionPendingIntent(ACTION_SNOOZE, reminder.id)
        )

        return builder.build()
    }

    /**
     * 액션 버튼용 PendingIntent 생성
     */
    private fun createActionPendingIntent(action: Int, reminderId: Long): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("action", action)
            putExtra("reminderId", reminderId)
        }
        return PendingIntent.getActivity(
            context,
            action,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    /**
     * URI로부터 Bitmap 로드
     */
    private fun loadBitmapFromUri(uriString: String): Bitmap? {
        return try {
            val uri = Uri.parse(uriString)
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            null
        }
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
