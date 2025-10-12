package com.reminder.fcm

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.reminder.data.entity.Priority
import com.reminder.data.entity.ReminderEntity
import com.reminder.notification.NotificationHelper

/**
 * v1.29.0: Firebase Cloud Messaging 서비스
 * FCM으로부터 푸시 알림을 수신하고 리치 알림으로 표시
 */
class ReminderMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "ReminderMessagingService"

        // FCM 메시지 데이터 키
        private const val KEY_REMINDER_ID = "reminderId"
        private const val KEY_TITLE = "title"
        private const val KEY_DESCRIPTION = "description"
        private const val KEY_PRIORITY = "priority"
        private const val KEY_CATEGORY = "category"
        private const val KEY_IMAGE_URI = "imageUri"
    }

    /**
     * FCM 메시지 수신 시 호출
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d(TAG, "FCM 메시지 수신: from=${remoteMessage.from}")

        // 알림 채널 생성 (앱이 백그라운드일 수도 있으므로)
        val notificationHelper = NotificationHelper(applicationContext)
        notificationHelper.createAllNotificationChannels()

        // 데이터 메시지 처리
        remoteMessage.data.let { data ->
            if (data.isNotEmpty()) {
                Log.d(TAG, "메시지 데이터: $data")
                handleDataMessage(data, notificationHelper)
            }
        }

        // 알림 메시지 처리 (선택사항)
        remoteMessage.notification?.let { notification ->
            Log.d(TAG, "알림 메시지: title=${notification.title}, body=${notification.body}")
            handleNotificationMessage(notification, notificationHelper)
        }
    }

    /**
     * 데이터 메시지 처리
     * FCM data payload로 전송된 리마인더 정보를 파싱하여 알림 표시
     */
    private fun handleDataMessage(data: Map<String, String>, notificationHelper: NotificationHelper) {
        try {
            val reminderId = data[KEY_REMINDER_ID]?.toLongOrNull() ?: 0L
            val title = data[KEY_TITLE] ?: "리마인더"
            val description = data[KEY_DESCRIPTION] ?: ""
            val priority = parsePriority(data[KEY_PRIORITY])
            val category = data[KEY_CATEGORY] ?: ""
            val imageUri = data[KEY_IMAGE_URI]

            val reminder = ReminderEntity(
                id = reminderId,
                title = title,
                description = description,
                priority = priority,
                category = category,
                imageUri = imageUri
            )

            // 이미지가 있으면 리치 알림, 없으면 액션 버튼 알림 표시
            if (!imageUri.isNullOrBlank()) {
                notificationHelper.buildRichNotification(reminder)
            } else {
                notificationHelper.buildNotificationWithActions(reminder)
            }

            notificationHelper.showNotification(reminder)

            Log.d(TAG, "알림 표시 완료: reminderId=$reminderId, title=$title")

        } catch (e: Exception) {
            Log.e(TAG, "데이터 메시지 처리 실패", e)
        }
    }

    /**
     * 알림 메시지 처리
     * FCM notification payload로 전송된 경우 (간단한 알림)
     */
    private fun handleNotificationMessage(
        notification: RemoteMessage.Notification,
        notificationHelper: NotificationHelper
    ) {
        val title = notification.title ?: "리마인더"
        val body = notification.body ?: ""

        val reminder = ReminderEntity(
            id = System.currentTimeMillis(),
            title = title,
            description = body,
            priority = Priority.MEDIUM
        )

        notificationHelper.showNotification(reminder)
        Log.d(TAG, "간단 알림 표시 완료: title=$title")
    }

    /**
     * 우선순위 문자열을 Priority enum으로 변환
     */
    private fun parsePriority(priorityString: String?): Priority {
        return when (priorityString?.uppercase()) {
            "HIGH" -> Priority.HIGH
            "MEDIUM" -> Priority.MEDIUM
            "LOW" -> Priority.LOW
            else -> Priority.MEDIUM
        }
    }

    /**
     * 새로운 FCM 토큰 수신 시 호출
     * 서버에 토큰을 전송하여 저장해야 함 (향후 구현 가능)
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "새로운 FCM 토큰 수신: $token")

        // TODO: 서버에 토큰 전송하여 저장
        // sendTokenToServer(token)
    }

    /**
     * 메시지 삭제 시 호출 (메시지가 서버에서 삭제된 경우)
     */
    override fun onDeletedMessages() {
        super.onDeletedMessages()
        Log.d(TAG, "FCM 메시지 삭제됨")
    }
}
