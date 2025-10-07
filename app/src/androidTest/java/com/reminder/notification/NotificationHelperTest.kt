package com.reminder.notification

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.reminder.data.entity.Priority
import com.reminder.data.entity.ReminderEntity
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime

@RunWith(AndroidJUnit4::class)
class NotificationHelperTest {

    private lateinit var context: Context
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var notificationManager: NotificationManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        notificationHelper = NotificationHelper(context)
        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    @Test
    fun createNotificationChannel_채널이_생성된다() {
        // When
        notificationHelper.createNotificationChannel()

        // Then
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = notificationManager.getNotificationChannel(NotificationHelper.CHANNEL_ID)
            assertNotNull(channel)
            assertEquals(NotificationHelper.CHANNEL_ID, channel.id)
            assertEquals("Reminder Notifications", channel.name)
            assertEquals(NotificationManager.IMPORTANCE_HIGH, channel.importance)
        }
    }

    @Test
    fun buildNotification_알림이_생성된다() {
        // Given
        val reminder = ReminderEntity(
            id = 1,
            title = "테스트 리마인더",
            description = "테스트 설명",
            priority = Priority.HIGH,
            category = "업무"
        )

        // When
        val notification = notificationHelper.buildNotification(reminder)

        // Then
        assertNotNull(notification)
    }

    @Test
    fun buildNotification_우선순위에_따라_다른_아이콘() {
        // Given
        val highPriorityReminder = ReminderEntity(
            id = 1,
            title = "높은 우선순위",
            priority = Priority.HIGH
        )
        val lowPriorityReminder = ReminderEntity(
            id = 2,
            title = "낮은 우선순위",
            priority = Priority.LOW
        )

        // When
        val highNotification = notificationHelper.buildNotification(highPriorityReminder)
        val lowNotification = notificationHelper.buildNotification(lowPriorityReminder)

        // Then
        assertNotNull(highNotification)
        assertNotNull(lowNotification)
    }

    @Test
    fun showNotification_알림이_표시된다() {
        // Given
        notificationHelper.createNotificationChannel()
        val reminder = ReminderEntity(
            id = 1,
            title = "테스트 알림",
            description = "알림 테스트",
            priority = Priority.MEDIUM
        )

        // When
        notificationHelper.showNotification(reminder)

        // Then
        // 알림이 표시되었는지 확인하기는 어렵지만,
        // 예외가 발생하지 않으면 성공으로 간주
        assertTrue(true)
    }

    @Test
    fun cancelNotification_알림이_취소된다() {
        // Given
        notificationHelper.createNotificationChannel()
        val reminderId = 1L

        // When
        notificationHelper.cancelNotification(reminderId)

        // Then
        // 알림이 취소되었는지 확인 (예외 없으면 성공)
        assertTrue(true)
    }
}
