package com.reminder.notification

import android.app.NotificationManager
import com.reminder.data.entity.Priority

/**
 * 알림 채널 정의
 * Android 8.0+ 에서 우선순위별로 다른 채널 사용
 */
enum class ReminderNotificationChannel(
    val channelId: String,
    val channelName: String,
    val description: String,
    val importance: Int
) {
    /**
     * 높은 우선순위 채널
     * - 알림음, 진동, 헤드업 알림
     * - 중요한 리마인더
     */
    HIGH_PRIORITY(
        channelId = "reminder_high_priority",
        channelName = "높은 우선순위",
        description = "중요한 리마인더 알림",
        importance = NotificationManager.IMPORTANCE_HIGH
    ),

    /**
     * 중간 우선순위 채널
     * - 알림음만 표시
     * - 일반 리마인더
     */
    MEDIUM_PRIORITY(
        channelId = "reminder_medium_priority",
        channelName = "중간 우선순위",
        description = "일반 리마인더 알림",
        importance = NotificationManager.IMPORTANCE_DEFAULT
    ),

    /**
     * 낮은 우선순위 채널
     * - 소리/진동 없음
     * - 상태바에만 표시
     */
    LOW_PRIORITY(
        channelId = "reminder_low_priority",
        channelName = "낮은 우선순위",
        description = "덜 중요한 리마인더 알림",
        importance = NotificationManager.IMPORTANCE_LOW
    );

    companion object {
        /**
         * Priority enum을 NotificationChannel로 변환
         */
        fun fromPriority(priority: Priority): ReminderNotificationChannel {
            return when (priority) {
                Priority.HIGH -> HIGH_PRIORITY
                Priority.MEDIUM -> MEDIUM_PRIORITY
                Priority.LOW -> LOW_PRIORITY
            }
        }

        /**
         * 모든 채널 목록 반환
         */
        fun getAllChannels(): List<ReminderNotificationChannel> {
            return values().toList()
        }
    }
}
