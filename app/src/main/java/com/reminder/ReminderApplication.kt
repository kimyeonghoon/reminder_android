package com.reminder

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.reminder.auth.AuthManager
import com.reminder.data.database.ReminderDatabase
import com.reminder.data.preferences.PreferencesRepository
import com.reminder.data.remote.FirestoreDataSource
import com.reminder.data.repository.FirebaseSyncRepository
import com.reminder.data.repository.ReminderRepository
import com.reminder.notification.AlarmScheduler
import com.reminder.notification.NotificationHelper
import com.reminder.sync.SyncManager
import com.reminder.sync.SyncWorker
import com.reminder.widget.ReminderWidgetProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class ReminderApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    val database by lazy { ReminderDatabase.getDatabase(this) }
    val authManager by lazy { AuthManager() }
    val remoteDataSource by lazy { FirestoreDataSource(authManager) }
    val syncManager by lazy { SyncManager(this, database.reminderDao(), remoteDataSource) }

    // Firebase 동기화가 통합된 Repository
    val repository by lazy {
        ReminderRepository(database.reminderDao(), remoteDataSource)
    }

    // Firebase 동기화 전용 Repository (직접 접근이 필요한 경우)
    val firebaseSyncRepository by lazy {
        FirebaseSyncRepository(database.reminderDao(), remoteDataSource)
    }

    val alarmScheduler by lazy { AlarmScheduler(this) }
    val notificationHelper by lazy { NotificationHelper(this) }
    val preferencesRepository by lazy { PreferencesRepository.create(this) }

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

        // 위젯 업데이트 관찰
        setupWidgetUpdates()
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

    /**
     * Repository 변경사항을 관찰하여 위젯 자동 업데이트
     */
    private fun setupWidgetUpdates() {
        repository.allReminders
            .onEach {
                // 리마인더 목록이 변경될 때마다 위젯 업데이트
                ReminderWidgetProvider.updateAllWidgets(this)
            }
            .launchIn(applicationScope)
    }
}
