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
import com.reminder.util.LocaleHelper
import com.reminder.widget.ReminderWidgetProvider
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit
import android.util.Log
import android.content.Context

class ReminderApplication : Application(), ImageLoaderFactory {
    /**
     * 전역 Coroutine Exception Handler
     * 모든 처리되지 않은 코루틴 예외를 캐치하여 로깅
     */
    private val coroutineExceptionHandler = CoroutineExceptionHandler { context, throwable ->
        Log.e("ReminderApp", "Uncaught coroutine exception in context: $context", throwable)

        // Firebase Crashlytics에 예외 기록
        try {
            val crashlytics = FirebaseCrashlytics.getInstance()
            crashlytics.log("Coroutine context: $context")
            crashlytics.recordException(throwable)
        } catch (e: Exception) {
            // Crashlytics 초기화 전에 예외가 발생한 경우
            Log.e("ReminderApp", "Failed to record exception to Crashlytics", e)
        }
    }

    private val applicationScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main + coroutineExceptionHandler
    )

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
    val locationManager by lazy { com.reminder.location.LocationManager(this) }
    val ttsHelper by lazy { com.reminder.tts.TtsHelper(this) }
    val categorySuggestionHelper by lazy { com.reminder.ml.CategorySuggestionHelper() }
    val completionPatternAnalyzer by lazy { com.reminder.analytics.CompletionPatternAnalyzer() }

    // v1.40.1: 캘린더 동기화
    val deviceCalendarProvider by lazy { com.reminder.calendar.DeviceCalendarProvider(this) }
    val calendarSyncManager by lazy {
        com.reminder.calendar.CalendarSyncManager(
            deviceCalendarProvider,
            database.calendarSyncConfigDao(),
            database.reminderDao()
        )
    }

    // v1.43.0: 아카이브 관리
    val archiveManager by lazy { com.reminder.archive.ArchiveManager(database.reminderDao()) }

    /**
     * v1.30.0: 애플리케이션 시작 시 저장된 언어 설정 적용
     */
    override fun attachBaseContext(base: Context?) {
        if (base == null) {
            super.attachBaseContext(base)
            return
        }

        // DataStore에서 언어 설정 읽기 (동기적으로)
        val preferences = runBlocking {
            PreferencesRepository.create(base).userPreferences.first()
        }

        // 저장된 언어로 Context 업데이트
        val updatedContext = LocaleHelper.updateLocale(base, preferences.language)
        super.attachBaseContext(updatedContext)
    }

    override fun onCreate() {
        super.onCreate()

        // Firebase Crashlytics 초기화
        setupCrashlytics()

        // v1.29.0: 모든 알림 채널 생성 (우선순위별 세분화)
        notificationHelper.createAllNotificationChannels()

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
                    setUserProperty("language", preferences.language.code)  // v1.30.0
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

    /**
     * ViewModel에서 사용할 수 있는 전역 CoroutineExceptionHandler를 반환
     *
     * ViewModel의 viewModelScope에서 발생하는 예외는 자동으로 처리되지만,
     * 추가적인 로깅이 필요한 경우 이 핸들러를 사용할 수 있습니다.
     */
    fun getCoroutineExceptionHandler(): CoroutineExceptionHandler {
        return coroutineExceptionHandler
    }
}
