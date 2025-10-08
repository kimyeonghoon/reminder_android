package com.reminder.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.reminder.data.entity.Priority
import com.reminder.data.entity.RecurrencePattern
import com.reminder.data.entity.ReminderEntity
import java.time.LocalDateTime

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

        // 반복 알람 처리
        val patternName = intent.getStringExtra(AlarmScheduler.EXTRA_RECURRENCE_PATTERN) ?: RecurrencePattern.NONE.name
        val pattern = try {
            RecurrencePattern.valueOf(patternName)
        } catch (e: IllegalArgumentException) {
            RecurrencePattern.NONE
        }

        if (pattern != RecurrencePattern.NONE) {
            val interval = intent.getIntExtra(AlarmScheduler.EXTRA_RECURRENCE_INTERVAL, 1)
            val daysOfWeek = intent.getStringExtra(AlarmScheduler.EXTRA_RECURRENCE_DAYS_OF_WEEK)
            val endDateString = intent.getStringExtra(AlarmScheduler.EXTRA_RECURRENCE_END_DATE)
            val endDate = endDateString?.let {
                try {
                    LocalDateTime.parse(it)
                } catch (e: Exception) {
                    null
                }
            }

            // 다음 반복 알람 계산
            val nextOccurrence = AlarmScheduler.calculateNextOccurrence(
                currentDateTime = LocalDateTime.now(),
                pattern = pattern,
                interval = interval,
                daysOfWeek = daysOfWeek,
                endDate = endDate
            )

            // 다음 알람 스케줄링
            if (nextOccurrence != null) {
                val nextReminder = ReminderEntity(
                    id = reminderId,
                    title = title,
                    description = description,
                    priority = priority,
                    dueDateTime = nextOccurrence,
                    recurrencePattern = pattern,
                    recurrenceInterval = interval,
                    recurrenceDaysOfWeek = daysOfWeek,
                    recurrenceEndDate = endDate
                )
                val alarmScheduler = AlarmScheduler(context)
                alarmScheduler.schedule(nextReminder)
                Log.d(TAG, "Next recurrence scheduled for: $nextOccurrence")
            } else {
                Log.d(TAG, "No more recurrences for reminder: $reminderId")
            }
        }
    }
}
