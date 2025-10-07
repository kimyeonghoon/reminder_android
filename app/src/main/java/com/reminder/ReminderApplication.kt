package com.reminder

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.reminder.auth.AuthManager
import com.reminder.data.database.ReminderDatabase
import com.reminder.data.remote.FirestoreDataSource
import com.reminder.data.repository.ReminderRepository
import com.reminder.notification.AlarmScheduler
import com.reminder.notification.NotificationHelper
import com.reminder.sync.SyncManager
import com.reminder.sync.SyncWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class ReminderApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    val database by lazy { ReminderDatabase.getDatabase(this) }
    val authManager by lazy { AuthManager() }
    val remoteDataSource by lazy { FirestoreDataSource(authManager) }
    val syncManager by lazy { SyncManager(this, database.reminderDao(), remoteDataSource) }
    val repository by lazy { ReminderRepository(database.reminderDao()) }
    val alarmScheduler by lazy { AlarmScheduler(this) }
    val notificationHelper by lazy { NotificationHelper(this) }

    override fun onCreate() {
        super.onCreate()

        // NotificationChannel 생성
        notificationHelper.createNotificationChannel()

        // Firebase 익명 로그인
        applicationScope.launch {
            authManager.ensureSignedIn()
        }

        // 주기적 동기화 워커 설정
        setupSyncWorker()
    }

    private fun setupSyncWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWorkRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            15, TimeUnit.MINUTES // 15분마다 동기화
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "sync_reminders",
            ExistingPeriodicWorkPolicy.KEEP,
            syncWorkRequest
        )
    }
}
