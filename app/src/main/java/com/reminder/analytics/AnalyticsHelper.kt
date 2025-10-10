package com.reminder.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.reminder.data.entity.Priority

/**
 * Firebase Analytics 이벤트 로깅을 담당하는 헬퍼 클래스
 *
 * 사용자 행동 추적 및 앱 사용 패턴 분석을 위한 이벤트 로깅 제공
 */
class AnalyticsHelper(private val firebaseAnalytics: FirebaseAnalytics) {

    /**
     * 리마인더 생성 이벤트 로깅
     *
     * @param priority 우선순위
     * @param category 카테고리
     * @param hasRecurrence 반복 여부
     */
    fun logReminderCreated(priority: Priority, category: String, hasRecurrence: Boolean) {
        val bundle = Bundle().apply {
            putString("priority", priority.name)
            putString("category", category)
            putBoolean("has_recurrence", hasRecurrence)
        }
        firebaseAnalytics.logEvent("reminder_created", bundle)
    }

    /**
     * 리마인더 완료 이벤트 로깅
     *
     * @param daysUntilDue 마감일까지 남은 일수 (null이면 마감일 없음)
     */
    fun logReminderCompleted(daysUntilDue: Int?) {
        val bundle = Bundle().apply {
            if (daysUntilDue != null) {
                putInt("days_until_due", daysUntilDue)
            } else {
                putInt("days_until_due", -1)
            }
        }
        firebaseAnalytics.logEvent("reminder_completed", bundle)
    }

    /**
     * 리마인더 삭제 이벤트 로깅
     */
    fun logReminderDeleted() {
        firebaseAnalytics.logEvent("reminder_deleted", Bundle())
    }

    /**
     * 리마인더 수정 이벤트 로깅
     */
    fun logReminderEdited() {
        firebaseAnalytics.logEvent("reminder_edited", Bundle())
    }

    /**
     * 서브태스크 추가 이벤트 로깅
     */
    fun logSubtaskAdded() {
        firebaseAnalytics.logEvent("subtask_added", Bundle())
    }

    /**
     * 이미지 첨부 이벤트 로깅
     */
    fun logImageAttached() {
        firebaseAnalytics.logEvent("image_attached", Bundle())
    }

    /**
     * 템플릿 생성 이벤트 로깅
     */
    fun logTemplateCreated() {
        firebaseAnalytics.logEvent("template_created", Bundle())
    }

    /**
     * 템플릿 사용 이벤트 로깅
     *
     * @param templateName 사용한 템플릿 이름
     */
    fun logTemplateUsed(templateName: String) {
        val bundle = Bundle().apply {
            putString("template_name", templateName)
        }
        firebaseAnalytics.logEvent("template_used", bundle)
    }

    /**
     * 배치 작업 이벤트 로깅
     *
     * @param operationType 작업 유형 (delete, complete, etc.)
     * @param count 작업한 항목 수
     */
    fun logBatchOperation(operationType: String, count: Int) {
        val bundle = Bundle().apply {
            putString("operation_type", operationType)
            putInt("count", count)
        }
        firebaseAnalytics.logEvent("batch_operation", bundle)
    }

    /**
     * 검색 수행 이벤트 로깅
     *
     * @param queryLength 검색어 길이
     */
    fun logSearchPerformed(queryLength: Int) {
        val bundle = Bundle().apply {
            putInt("query_length", queryLength)
        }
        firebaseAnalytics.logEvent("search_performed", bundle)
    }

    /**
     * 필터 적용 이벤트 로깅
     *
     * @param filterType 필터 유형 (priority, category, etc.)
     */
    fun logFilterApplied(filterType: String = "custom") {
        val bundle = Bundle().apply {
            putString("filter_type", filterType)
        }
        firebaseAnalytics.logEvent("filter_applied", bundle)
    }

    /**
     * v1.32.0: 필터 초기화 이벤트 로깅
     */
    fun logFilterCleared() {
        firebaseAnalytics.logEvent("filter_cleared", Bundle())
    }

    /**
     * v1.32.0: 필터 저장 이벤트 로깅
     */
    fun logFilterSaved() {
        firebaseAnalytics.logEvent("filter_saved", Bundle())
    }

    /**
     * v1.32.0: 필터 프리셋 사용 이벤트 로깅
     *
     * @param presetId 프리셋 ID
     */
    fun logPresetUsed(presetId: String) {
        val bundle = Bundle().apply {
            putString("preset_id", presetId)
        }
        firebaseAnalytics.logEvent("preset_used", bundle)
    }

    /**
     * 정렬 변경 이벤트 로깅
     *
     * @param sortOption 정렬 옵션 (dueDate, priority, etc.)
     */
    fun logSortChanged(sortOption: String) {
        val bundle = Bundle().apply {
            putString("sort_option", sortOption)
        }
        firebaseAnalytics.logEvent("sort_changed", bundle)
    }

    /**
     * 테마 변경 이벤트 로깅
     *
     * @param themeName 테마 이름 (LIGHT, DARK, SYSTEM)
     */
    fun logThemeChanged(themeName: String) {
        val bundle = Bundle().apply {
            putString("theme_name", themeName)
        }
        firebaseAnalytics.logEvent("theme_changed", bundle)
    }

    /**
     * v1.30.0: 언어 변경 이벤트 로깅
     *
     * @param languageCode 언어 코드 (system, ko, en, zh)
     */
    fun logLanguageChanged(languageCode: String) {
        val bundle = Bundle().apply {
            putString("language_code", languageCode)
        }
        firebaseAnalytics.logEvent("language_changed", bundle)
    }

    /**
     * 알림 설정 변경 이벤트 로깅
     *
     * @param settingKey 설정 키
     * @param value 설정 값
     */
    fun logNotificationSettingsChanged(settingKey: String, value: String) {
        val bundle = Bundle().apply {
            putString("setting_key", settingKey)
            putString("value", value)
        }
        firebaseAnalytics.logEvent("notification_settings_changed", bundle)
    }

    /**
     * 간편 모드 전환 이벤트 로깅
     *
     * @param enabled 활성화 여부
     */
    fun logSimpleModeToggled(enabled: Boolean) {
        val bundle = Bundle().apply {
            putBoolean("enabled", enabled)
        }
        firebaseAnalytics.logEvent("simple_mode_toggled", bundle)
    }

    /**
     * 위치 추가 이벤트 로깅
     */
    fun logLocationAdded() {
        firebaseAnalytics.logEvent("location_added", Bundle())
    }

    /**
     * 웹 링크 추가 이벤트 로깅
     */
    fun logWebLinkAdded() {
        firebaseAnalytics.logEvent("web_link_added", Bundle())
    }

    /**
     * TTS 사용 이벤트 로깅
     */
    fun logTtsUsed() {
        firebaseAnalytics.logEvent("tts_used", Bundle())
    }

    /**
     * 자동 읽기 활성화 이벤트 로깅
     */
    fun logReadAloudEnabled() {
        firebaseAnalytics.logEvent("read_aloud_enabled", Bundle())
    }

    /**
     * 카테고리 제안 이벤트 로깅
     *
     * @param suggestionsCount 제안된 카테고리 수
     */
    fun logCategorySuggested(suggestionsCount: Int) {
        val bundle = Bundle().apply {
            putInt("suggestions_count", suggestionsCount)
        }
        firebaseAnalytics.logEvent("category_suggested", bundle)
    }

    /**
     * 완료 패턴 분석 이벤트 로깅
     *
     * @param completionRate 완료율
     */
    fun logPatternAnalyzed(completionRate: Double) {
        val bundle = Bundle().apply {
            putDouble("completion_rate", completionRate)
            putInt("completion_rate_percent", (completionRate * 100).toInt())
        }
        firebaseAnalytics.logEvent("pattern_analyzed", bundle)
    }
}
