package com.reminder.widget

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

/**
 * v1.34.0: 위젯 주기적 업데이트 Worker
 *
 * WorkManager를 사용하여 15분마다 위젯 업데이트
 */
class WidgetUpdateWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    companion object {
        private const val WORK_NAME = "widget_update_work"
        private const val UPDATE_INTERVAL_MINUTES = 15L

        /**
         * 주기적 위젯 업데이트 작업 예약
         */
        fun schedulePeriodicUpdate(context: Context) {
            val updateRequest = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(
                UPDATE_INTERVAL_MINUTES, TimeUnit.MINUTES
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(true) // 배터리가 낮지 않을 때만
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP, // 이미 예약된 작업이 있으면 유지
                updateRequest
            )
        }

        /**
         * 주기적 업데이트 작업 취소
         */
        fun cancelPeriodicUpdate(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }

        /**
         * 즉시 위젯 업데이트 (일회성)
         */
        fun updateNow(context: Context) {
            val updateRequest = OneTimeWorkRequestBuilder<WidgetUpdateWorker>()
                .build()

            WorkManager.getInstance(context).enqueue(updateRequest)
        }
    }

    override fun doWork(): Result {
        return try {
            // 모든 위젯 업데이트
            ReminderWidgetProvider.updateAllWidgets(applicationContext)
            Result.success()
        } catch (e: Exception) {
            // 에러 발생 시 재시도
            Result.retry()
        }
    }
}
