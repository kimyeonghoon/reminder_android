package com.reminder

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.reminder.BuildConfig
import com.reminder.analytics.AnalyticsHelper
import com.reminder.analytics.CrashlyticsHelper
import com.reminder.auth.AuthManager
import com.reminder.backup.BackupManager
import com.reminder.badge.BadgeManager
import com.reminder.data.database.ReminderDatabase
import com.reminder.snooze.SnoozeManager
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

class ReminderApplication : Application(), ImageLoaderFactory {
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
    val backupManager by lazy { BackupManager(this, database) }
    val analyticsHelper by lazy { AnalyticsHelper(FirebaseAnalytics.getInstance(this)) }
    val crashlyticsHelper by lazy { CrashlyticsHelper(FirebaseCrashlytics.getInstance()) }
    val badgeManager by lazy { BadgeManager(this) }
    val snoozeManager by lazy { SnoozeManager(database.reminderDao()) }

    override fun onCreate() {
        super.onCreate()

        // Firebase Crashlytics 초기화
        setupCrashlytics()

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

        // 배지 업데이트 관찰
        setupBadgeUpdates()

        // 초기 사용자 속성 설정
        setupInitialUserProperties()
    }

    /**
     * Firebase Crashlytics 초기화 및 설정
     */
    private fun setupCrashlytics() {
        val crashlytics = FirebaseCrashlytics.getInstance()

        // 앱 버전 정보 설정
        crashlytics.setCustomKey("app_version", BuildConfig.VERSION_NAME)
        crashlytics.setCustomKey("version_code", BuildConfig.VERSION_CODE)

        // Debug 모드에서는 Crashlytics 비활성화 (선택사항)
        crashlytics.setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)

        // 전역 에러 핸들러 설정 (추가 로깅용)
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            crashlytics.log("Uncaught exception in thread: ${thread.name}")
            crashlytics.recordException(throwable)
            // 기본 핸들러 호출 (앱 크래시 처리)
            defaultHandler?.uncaughtException(thread, throwable)
        }
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

    /**
     * Repository 변경사항을 관찰하여 배지 카운트 자동 업데이트
     */
    private fun setupBadgeUpdates() {
        repository.allReminders
            .onEach { reminders ->
                // 미완료 리마인더 수를 배지에 표시
                val incompleteCount = reminders.count { !it.isCompleted }
                applicationScope.launch(Dispatchers.IO) {
                    badgeManager.updateBadgeCount(incompleteCount)
                }
            }
            .launchIn(applicationScope)
    }

    /**
     * 초기 사용자 속성 설정
     */
    private fun setupInitialUserProperties() {
        applicationScope.launch {
            preferencesRepository.userPreferences.collect { preferences ->
                // Crashlytics 사용자 속성 설정
                val totalReminders = database.reminderDao().getAllRemindersList().size
                crashlyticsHelper.setUserProperties(
                    themeMode = preferences.themeMode.name,
                    simpleMode = preferences.simpleMode,
                    totalReminders = totalReminders
                )

                // Firebase Analytics 사용자 속성 설정
                FirebaseAnalytics.getInstance(this@ReminderApplication).apply {
                    setUserProperty("theme_mode", preferences.themeMode.name)
                    setUserProperty("simple_mode", preferences.simpleMode.toString())
                    setUserProperty("font_size", preferences.fontSize.name)
                }
            }
        }
    }

    /**
     * Coil ImageLoader 설정 (메모리 최적화)
     */
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25) // 메모리의 25% 사용
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(50 * 1024 * 1024) // 50MB
                    .build()
            }
            .respectCacheHeaders(false) // 캐시 헤더 무시 (로컬 이미지용)
            .build()
    }
}
