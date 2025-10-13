package com.reminder.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.gson.Gson
import com.reminder.data.entity.Priority
import com.reminder.data.entity.ReminderEntity
import com.reminder.recurrence.RecurrenceEnd
import com.reminder.recurrence.RecurrenceRule
import java.time.LocalDateTime

/**
 * 알람 시간이 되면 호출되는 BroadcastReceiver
 */
class ReminderReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "ReminderReceiver"
    }

    private val gson = Gson()

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

        // v1.64.0: RecurrenceRule 처리
        val recurrenceRuleJson = intent.getStringExtra(AlarmScheduler.EXTRA_RECURRENCE_RULE)
        val recurrenceEndJson = intent.getStringExtra(AlarmScheduler.EXTRA_RECURRENCE_END)

        if (recurrenceRuleJson != null) {
            try {
                val recurrenceRule = gson.fromJson(recurrenceRuleJson, RecurrenceRule::class.java)
                val recurrenceEnd = recurrenceEndJson?.let {
                    gson.fromJson(it, RecurrenceEnd::class.java)
                }

                // v1.65.0: RecurrenceCalculator를 사용하여 다음 발생 시간 계산
                val currentTime = LocalDateTime.now()

                val nextOccurrence = com.reminder.recurrence.RecurrenceCalculator.calculateNextOccurrence(
                    currentTime = currentTime,
                    recurrenceRule = recurrenceRule,
                    recurrenceEnd = recurrenceEnd ?: RecurrenceEnd.Never
                )

                if (nextOccurrence != null) {
                    // 다음 알람 스케줄링
                    val alarmScheduler = AlarmScheduler(context)
                    val nextReminder = reminder.copy(
                        dueDateTime = nextOccurrence,
                        recurrenceRule = recurrenceRule,
                        recurrenceEnd = recurrenceEnd
                    )
                    alarmScheduler.schedule(nextReminder)
                    Log.d(TAG, "Next occurrence scheduled: $nextOccurrence")
                } else {
                    Log.d(TAG, "Recurrence ended (no next occurrence)")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse recurrence rule: ${e.message}")
            }
        }
    }
}
