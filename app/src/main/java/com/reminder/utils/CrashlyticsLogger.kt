package com.reminder.utils

import com.google.firebase.crashlytics.FirebaseCrashlytics

/**
 * Firebase Crashlytics 로깅 유틸리티
 *
 * 앱 전체에서 에러와 이벤트를 Crashlytics에 기록하는 헬퍼 클래스
 */
object CrashlyticsLogger {
    private val crashlytics = FirebaseCrashlytics.getInstance()

    /**
     * 사용자 ID 설정 (익명 또는 로그인 사용자)
     */
    fun setUserId(userId: String) {
        crashlytics.setUserId(userId)
    }

    /**
     * 커스텀 키-값 설정
     */
    fun setCustomKey(key: String, value: String) {
        crashlytics.setCustomKey(key, value)
    }

    fun setCustomKey(key: String, value: Boolean) {
        crashlytics.setCustomKey(key, value)
    }

    fun setCustomKey(key: String, value: Int) {
        crashlytics.setCustomKey(key, value)
    }

    fun setCustomKey(key: String, value: Long) {
        crashlytics.setCustomKey(key, value)
    }

    /**
     * 일반 로그 메시지 기록
     */
    fun log(message: String) {
        crashlytics.log(message)
    }

    /**
     * 치명적이지 않은 예외 기록
     */
    fun recordException(throwable: Throwable) {
        crashlytics.recordException(throwable)
    }

    /**
     * 컨텍스트와 함께 예외 기록
     */
    fun recordException(throwable: Throwable, context: Map<String, String>) {
        context.forEach { (key, value) ->
            crashlytics.setCustomKey(key, value)
        }
        crashlytics.recordException(throwable)
    }

    /**
     * 데이터베이스 에러 기록
     */
    fun logDatabaseError(operation: String, throwable: Throwable) {
        crashlytics.log("Database operation failed: $operation")
        crashlytics.setCustomKey("db_operation", operation)
        crashlytics.recordException(throwable)
    }

    /**
     * 네트워크 에러 기록
     */
    fun logNetworkError(endpoint: String, throwable: Throwable) {
        crashlytics.log("Network request failed: $endpoint")
        crashlytics.setCustomKey("network_endpoint", endpoint)
        crashlytics.recordException(throwable)
    }

    /**
     * 알림 에러 기록
     */
    fun logNotificationError(reminderId: Long, throwable: Throwable) {
        crashlytics.log("Notification scheduling failed for reminder: $reminderId")
        crashlytics.setCustomKey("reminder_id", reminderId)
        crashlytics.recordException(throwable)
    }

    /**
     * 위젯 에러 기록
     */
    fun logWidgetError(throwable: Throwable) {
        crashlytics.log("Widget update failed")
        crashlytics.setCustomKey("component", "widget")
        crashlytics.recordException(throwable)
    }

    /**
     * Firebase 동기화 에러 기록
     */
    fun logSyncError(operation: String, throwable: Throwable) {
        crashlytics.log("Firebase sync failed: $operation")
        crashlytics.setCustomKey("sync_operation", operation)
        crashlytics.recordException(throwable)
    }
}
