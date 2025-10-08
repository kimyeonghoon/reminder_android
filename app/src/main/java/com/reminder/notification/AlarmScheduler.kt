package com.reminder.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.reminder.data.entity.RecurrencePattern
import com.reminder.data.entity.ReminderEntity
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

class AlarmScheduler(private val context: Context) {

    private val alarmManager: AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    companion object {
        private const val TAG = "AlarmScheduler"
        const val EXTRA_REMINDER_ID = "reminder_id"
        const val EXTRA_REMINDER_TITLE = "reminder_title"
        const val EXTRA_REMINDER_DESCRIPTION = "reminder_description"
        const val EXTRA_REMINDER_PRIORITY = "reminder_priority"
        const val EXTRA_RECURRENCE_PATTERN = "recurrence_pattern"
        const val EXTRA_RECURRENCE_INTERVAL = "recurrence_interval"
        const val EXTRA_RECURRENCE_DAYS_OF_WEEK = "recurrence_days_of_week"
        const val EXTRA_RECURRENCE_END_DATE = "recurrence_end_date"

        /**
         * 다음 N개의 반복 일정 계산 (미리보기용)
         */
        fun calculateNextOccurrences(
            startDateTime: LocalDateTime,
            pattern: RecurrencePattern,
            interval: Int,
            daysOfWeek: String?,
            endDate: LocalDateTime?,
            count: Int = 5
        ): List<LocalDateTime> {
            if (pattern == RecurrencePattern.NONE) return emptyList()

            val occurrences = mutableListOf<LocalDateTime>()
            var current = startDateTime

            repeat(count) {
                val next = calculateNextOccurrence(current, pattern, interval, daysOfWeek, endDate)
                if (next != null) {
                    occurrences.add(next)
                    current = next
                } else {
                    return occurrences // 종료 날짜 도달 또는 더 이상 없음
                }
            }

            return occurrences
        }

        /**
         * 다음 반복 알람 시간 계산
         */
        fun calculateNextOccurrence(
            currentDateTime: LocalDateTime,
            pattern: RecurrencePattern,
            interval: Int,
            daysOfWeek: String?,
            endDate: LocalDateTime?
        ): LocalDateTime? {
            var nextDateTime = when (pattern) {
                RecurrencePattern.NONE -> return null

                RecurrencePattern.DAILY -> {
                    currentDateTime.plusDays(interval.toLong())
                }

                RecurrencePattern.WEEKLY -> {
                    if (daysOfWeek.isNullOrEmpty()) {
                        // 요일 지정 없으면 interval 주 후
                        currentDateTime.plusWeeks(interval.toLong())
                    } else {
                        // 지정된 요일 중 다음 요일 찾기
                        val selectedDays = daysOfWeek.split(",")
                            .mapNotNull { dayName ->
                                try {
                                    DayOfWeek.valueOf(dayName.trim())
                                } catch (e: Exception) {
                                    null
                                }
                            }
                            .sortedBy { it.value }

                        if (selectedDays.isEmpty()) {
                            currentDateTime.plusWeeks(interval.toLong())
                        } else {
                            findNextDayOfWeek(currentDateTime, selectedDays, interval)
                        }
                    }
                }

                RecurrencePattern.MONTHLY -> {
                    currentDateTime.plusMonths(interval.toLong())
                }

                RecurrencePattern.YEARLY -> {
                    currentDateTime.plusYears(interval.toLong())
                }
            }

            // 종료 날짜 확인
            if (endDate != null && nextDateTime.isAfter(endDate)) {
                return null
            }

            return nextDateTime
        }

        /**
         * 지정된 요일 중 다음 발생 요일 찾기
         */
        private fun findNextDayOfWeek(
            currentDateTime: LocalDateTime,
            daysOfWeek: List<DayOfWeek>,
            interval: Int
        ): LocalDateTime {
            val currentDayOfWeek = currentDateTime.dayOfWeek

            // 이번 주에서 다음 요일 찾기
            val nextDayInWeek = daysOfWeek.firstOrNull { it.value > currentDayOfWeek.value }

            return if (nextDayInWeek != null) {
                // 이번 주에 다음 요일이 있음
                currentDateTime.with(TemporalAdjusters.next(nextDayInWeek))
            } else {
                // 다음 주기로 이동
                val firstDay = daysOfWeek.first()
                currentDateTime.plusWeeks(interval.toLong())
                    .with(TemporalAdjusters.nextOrSame(firstDay))
            }
        }
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
            // 반복 정보 추가
            putExtra(EXTRA_RECURRENCE_PATTERN, reminder.recurrencePattern.name)
            putExtra(EXTRA_RECURRENCE_INTERVAL, reminder.recurrenceInterval)
            putExtra(EXTRA_RECURRENCE_DAYS_OF_WEEK, reminder.recurrenceDaysOfWeek)
            reminder.recurrenceEndDate?.let {
                putExtra(EXTRA_RECURRENCE_END_DATE, it.toString())
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
