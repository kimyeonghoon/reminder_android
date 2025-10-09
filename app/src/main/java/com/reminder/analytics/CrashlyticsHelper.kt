package com.reminder.analytics

import com.google.firebase.crashlytics.FirebaseCrashlytics

/**
 * Firebase Crashlytics 커스터마이징을 담당하는 헬퍼 클래스
 *
 * 사용자 속성, breadcrumb, 커스텀 키 설정 제공
 */
class CrashlyticsHelper(private val crashlytics: FirebaseCrashlytics) {

    /**
     * 사용자 속성 설정
     */
    fun setUserProperties(
        themeMode: String,
        simpleMode: Boolean,
        totalReminders: Int
    ) {
        crashlytics.setCustomKey("theme_mode", themeMode)
        crashlytics.setCustomKey("simple_mode", simpleMode)
        crashlytics.setCustomKey("total_reminders", totalReminders)
    }

    /**
     * 마지막 액션 기록 (Breadcrumb)
     */
    fun logBreadcrumb(action: String, details: String = "") {
        val message = if (details.isNotEmpty()) {
            "$action: $details"
        } else {
            action
        }
        crashlytics.log(message)
    }

    /**
     * 현재 화면 설정
     */
    fun setCurrentScreen(screenName: String) {
        crashlytics.setCustomKey("current_screen", screenName)
        logBreadcrumb("Screen", screenName)
    }

    /**
     * 마지막 액션 설정
     */
    fun setLastAction(action: String) {
        crashlytics.setCustomKey("last_action", action)
        logBreadcrumb("Action", action)
    }

    /**
     * 에러 로깅
     */
    fun recordException(exception: Throwable, context: String = "") {
        if (context.isNotEmpty()) {
            crashlytics.log("Exception context: $context")
        }
        crashlytics.recordException(exception)
    }
}
