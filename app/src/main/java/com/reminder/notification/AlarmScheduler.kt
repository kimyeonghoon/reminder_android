package com.reminder.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.google.gson.Gson
import com.reminder.data.entity.ReminderEntity
import java.time.LocalDateTime
import java.time.ZoneId

class AlarmScheduler(private val context: Context) {

    private val alarmManager: AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val gson = Gson()

    companion object {
        private const val TAG = "AlarmScheduler"
        const val EXTRA_REMINDER_ID = "reminder_id"
        const val EXTRA_REMINDER_TITLE = "reminder_title"
        const val EXTRA_REMINDER_DESCRIPTION = "reminder_description"
        const val EXTRA_REMINDER_PRIORITY = "reminder_priority"
        const val EXTRA_RECURRENCE_RULE = "recurrence_rule"  // v1.64.0
        const val EXTRA_RECURRENCE_END = "recurrence_end"    // v1.64.0
    }

    /**
     * 알람 스케줄링
     */
    fun schedule(reminder: ReminderEntity) {
        // dueDateTime이 null이면 스케줄하지 않음
        val dueDateTime = reminder.dueDateTime ?: return

        // 과거 시간이면 스케줄하지 않음
        if (dueDateTime.isBefore(LocalDateTime.now())) {
            Log.w(TAG, "Cannot schedule alarm for past time: ${reminder.id}")
            return
        }

        // LocalDateTime을 milliseconds로 변환
        val triggerAtMillis = dueDateTime
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(EXTRA_REMINDER_ID, reminder.id)
            putExtra(EXTRA_REMINDER_TITLE, reminder.title)
            putExtra(EXTRA_REMINDER_DESCRIPTION, reminder.description)
            putExtra(EXTRA_REMINDER_PRIORITY, reminder.priority.name)
            // v1.64.0: RecurrenceRule 직렬화
            reminder.recurrenceRule?.let {
                putExtra(EXTRA_RECURRENCE_RULE, gson.toJson(it))
            }
            reminder.recurrenceEnd?.let {
                putExtra(EXTRA_RECURRENCE_END, gson.toJson(it))
            }
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Android 12+ 정확한 알람 권한 확인
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
                Log.d(TAG, "Exact alarm scheduled for reminder ${reminder.id} at $dueDateTime")
            } else {
                // 권한이 없으면 일반 알람으로 스케줄
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
                Log.w(TAG, "Scheduled approximate alarm for reminder ${reminder.id}")
            }
        } else {
            // Android 12 미만
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
            Log.d(TAG, "Exact alarm scheduled for reminder ${reminder.id} at $dueDateTime")
        }
    }

    /**
     * 알람 취소
     */
    fun cancel(reminderId: Long) {
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
        )

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d(TAG, "Alarm cancelled for reminder $reminderId")
        }
    }

    /**
     * 정확한 알람 스케줄링 권한 확인 (Android 12+)
     */
    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

}
