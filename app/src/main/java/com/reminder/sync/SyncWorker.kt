package com.reminder.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.reminder.ReminderApplication

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val TAG = "SyncWorker"

    override suspend fun doWork(): Result {
        Log.d(TAG, "동기화 워커 시작")

        val app = applicationContext as ReminderApplication
        val syncManager = app.syncManager

        return try {
            // 동기화 수행
            syncManager.sync()
                .onSuccess {
                    Log.d(TAG, "동기화 성공")
                }
                .onFailure { e ->
                    Log.e(TAG, "동기화 실패", e)
                }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "동기화 워커 오류", e)
            // 재시도
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}
