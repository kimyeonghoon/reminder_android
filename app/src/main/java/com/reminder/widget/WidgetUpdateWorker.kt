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
        // v1.67.1: 업데이트 주기를 15분에서 30분으로 변경 (배터리 절약)
        private const val UPDATE_INTERVAL_MINUTES = 30L

        /**
         * 주기적 위젯 업데이트 작업 예약
         *
         * v1.67.1: 배터리 제약 제거 - 위젯은 항상 최신 상태를 유지해야 함
         */
        fun schedulePeriodicUpdate(context: Context) {
            val updateRequest = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(
                UPDATE_INTERVAL_MINUTES, TimeUnit.MINUTES
            )
                // v1.67.1: 배터리 제약 제거 - 위젯은 사용자 경험에 중요
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE, // v1.67.1: KEEP → UPDATE로 변경 (설정 업데이트 적용)
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
