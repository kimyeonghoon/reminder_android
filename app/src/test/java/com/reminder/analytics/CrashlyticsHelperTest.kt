package com.reminder.analytics

import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

/**
 * CrashlyticsHelper 단위 테스트
 *
 * 테스트 범위:
 * - 사용자 속성 설정 (setUserProperties)
 * - Breadcrumb 로깅 (logBreadcrumb)
 * - 현재 화면 설정 (setCurrentScreen)
 * - 마지막 액션 설정 (setLastAction)
 * - 예외 기록 (recordException)
 *
 * FirebaseCrashlytics는 Mockito로 모킹하여 테스트합니다.
 */
class CrashlyticsHelperTest {

    private lateinit var mockCrashlytics: FirebaseCrashlytics
    private lateinit var helper: CrashlyticsHelper

    @Before
    fun setup() {
        mockCrashlytics = mock()
        helper = CrashlyticsHelper(mockCrashlytics)
    }

    // ===== setUserProperties 테스트 =====

    @Test
    fun setUserPropertiesSetsThemeMode() {
        // Given: 테마 모드, 심플 모드, 총 리마인더 수
        val themeMode = "dark"
        val simpleMode = true
        val totalReminders = 10

        // When: 사용자 속성 설정
        helper.setUserProperties(themeMode, simpleMode, totalReminders)

        // Then: FirebaseCrashlytics.setCustomKey 호출 확인
        verify(mockCrashlytics).setCustomKey("theme_mode", themeMode)
        verify(mockCrashlytics).setCustomKey("simple_mode", simpleMode)
        verify(mockCrashlytics).setCustomKey("total_reminders", totalReminders)
    }

    @Test
    fun setUserPropertiesSetsSimpleMode() {
        // Given: 사용자 속성
        val themeMode = "light"
        val simpleMode = false
        val totalReminders = 5

        // When: 사용자 속성 설정
        helper.setUserProperties(themeMode, simpleMode, totalReminders)

        // Then: simple_mode 키 설정 확인
        verify(mockCrashlytics).setCustomKey("simple_mode", simpleMode)
    }

    @Test
    fun setUserPropertiesSetsTotalReminders() {
        // Given: 사용자 속성
        val themeMode = "auto"
        val simpleMode = true
        val totalReminders = 100

        // When: 사용자 속성 설정
        helper.setUserProperties(themeMode, simpleMode, totalReminders)

        // Then: total_reminders 키 설정 확인
        verify(mockCrashlytics).setCustomKey("total_reminders", totalReminders)
    }

    // ===== logBreadcrumb 테스트 =====

    @Test
    fun logBreadcrumbLogsActionOnly() {
        // Given: 액션만 제공
        val action = "Button clicked"

        // When: Breadcrumb 로깅
        helper.logBreadcrumb(action)

        // Then: 액션만 로그에 기록
        verify(mockCrashlytics).log(action)
    }

    @Test
    fun logBreadcrumbLogsActionWithDetails() {
        // Given: 액션과 상세 정보
        val action = "API call"
        val details = "getUserData"

        // When: Breadcrumb 로깅
        helper.logBreadcrumb(action, details)

        // Then: "액션: 상세정보" 형식으로 로그 기록
        verify(mockCrashlytics).log("$action: $details")
    }

    @Test
    fun logBreadcrumbHandlesEmptyDetails() {
        // Given: 빈 상세 정보
        val action = "Screen viewed"
        val details = ""

        // When: Breadcrumb 로깅
        helper.logBreadcrumb(action, details)

        // Then: 액션만 로그에 기록
        verify(mockCrashlytics).log(action)
    }

    // ===== setCurrentScreen 테스트 =====

    @Test
    fun setCurrentScreenSetsCustomKey() {
        // Given: 화면 이름
        val screenName = "HomeScreen"

        // When: 현재 화면 설정
        helper.setCurrentScreen(screenName)

        // Then: current_screen 키 설정
        verify(mockCrashlytics).setCustomKey("current_screen", screenName)
    }

    @Test
    fun setCurrentScreenLogsBreadcrumb() {
        // Given: 화면 이름
        val screenName = "SettingsScreen"

        // When: 현재 화면 설정
        helper.setCurrentScreen(screenName)

        // Then: Breadcrumb에 화면 이름 기록
        verify(mockCrashlytics).log("Screen: $screenName")
    }

    @Test
    fun setCurrentScreenCallsBothMethods() {
        // Given: 화면 이름
        val screenName = "ProfileScreen"

        // When: 현재 화면 설정
        helper.setCurrentScreen(screenName)

        // Then: setCustomKey와 log 모두 호출
        verify(mockCrashlytics).setCustomKey("current_screen", screenName)
        verify(mockCrashlytics).log("Screen: $screenName")
    }

    // ===== setLastAction 테스트 =====

    @Test
    fun setLastActionSetsCustomKey() {
        // Given: 액션
        val action = "Add reminder"

        // When: 마지막 액션 설정
        helper.setLastAction(action)

        // Then: last_action 키 설정
        verify(mockCrashlytics).setCustomKey("last_action", action)
    }

    @Test
    fun setLastActionLogsBreadcrumb() {
        // Given: 액션
        val action = "Delete reminder"

        // When: 마지막 액션 설정
        helper.setLastAction(action)

        // Then: Breadcrumb에 액션 기록
        verify(mockCrashlytics).log("Action: $action")
    }

    @Test
    fun setLastActionCallsBothMethods() {
        // Given: 액션
        val action = "Edit reminder"

        // When: 마지막 액션 설정
        helper.setLastAction(action)

        // Then: setCustomKey와 log 모두 호출
        verify(mockCrashlytics).setCustomKey("last_action", action)
        verify(mockCrashlytics).log("Action: $action")
    }

    // ===== recordException 테스트 =====

    @Test
    fun recordExceptionRecordsExceptionOnly() {
        // Given: 예외
        val exception = RuntimeException("Test error")

        // When: 예외 기록
        helper.recordException(exception)

        // Then: recordException 호출 확인
        verify(mockCrashlytics).recordException(exception)
    }

    @Test
    fun recordExceptionRecordsExceptionWithContext() {
        // Given: 예외와 컨텍스트
        val exception = NullPointerException("Null value")
        val context = "Loading user data"

        // When: 예외 기록
        helper.recordException(exception, context)

        // Then: 컨텍스트 로그와 예외 기록 모두 호출
        verify(mockCrashlytics).log("Exception context: $context")
        verify(mockCrashlytics).recordException(exception)
    }

    @Test
    fun recordExceptionHandlesEmptyContext() {
        // Given: 빈 컨텍스트
        val exception = IllegalStateException("Invalid state")
        val context = ""

        // When: 예외 기록
        helper.recordException(exception, context)

        // Then: 컨텍스트 로그 없이 예외만 기록
        verify(mockCrashlytics).recordException(exception)
    }

    @Test
    fun recordExceptionWithContextLogsFirst() {
        // Given: 예외와 컨텍스트
        val exception = Exception("Error")
        val context = "Saving data"

        // When: 예외 기록
        helper.recordException(exception, context)

        // Then: 컨텍스트를 먼저 로그에 기록
        verify(mockCrashlytics).log("Exception context: $context")
        // 그 다음 예외 기록
        verify(mockCrashlytics).recordException(exception)
    }
}
