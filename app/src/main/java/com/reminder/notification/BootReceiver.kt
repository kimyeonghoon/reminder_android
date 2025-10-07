package com.reminder.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.reminder.ReminderApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime

/**
 * 기기 재부팅 시 알람을 재설정하는 BroadcastReceiver
 */
class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Boot completed, rescheduling alarms")

            // Application에서 Repository 가져오기
            val application = context.applicationContext as ReminderApplication
            val repository = application.repository

            val alarmScheduler = AlarmScheduler(context)

            // 코루틴으로 비동기 처리
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // 완료되지 않은 모든 리마인더 가져오기
                    repository.activeReminders.collect { reminders ->
                        reminders
                            .filter { it.dueDateTime != null && it.dueDateTime!!.isAfter(LocalDateTime.now()) }
                            .forEach { reminder ->
                                alarmScheduler.schedule(reminder)
                                Log.d(TAG, "Rescheduled alarm for reminder: ${reminder.title}")
                            }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error rescheduling alarms", e)
                }
            }
        }
    }
}
