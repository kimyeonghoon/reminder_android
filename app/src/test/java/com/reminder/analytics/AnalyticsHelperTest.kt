package com.reminder.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.reminder.data.entity.Priority
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class AnalyticsHelperTest {

    @Mock
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    private lateinit var analyticsHelper: AnalyticsHelper

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        analyticsHelper = AnalyticsHelper(firebaseAnalytics)
    }

    @Test
    fun `리마인더 생성 이벤트 로깅`() {
        // Given
        val priority = Priority.HIGH
        val category = "Work"
        val hasRecurrence = true

        // When
        analyticsHelper.logReminderCreated(priority, category, hasRecurrence)

        // Then
        val bundleCaptor = ArgumentCaptor.forClass(Bundle::class.java)
        verify(firebaseAnalytics).logEvent(eq("reminder_created"), bundleCaptor.capture())

        val bundle = bundleCaptor.value
        assert(bundle.getString("priority") == priority.name)
        assert(bundle.getString("category") == category)
        assert(bundle.getBoolean("has_recurrence") == hasRecurrence)
    }

    @Test
    fun `리마인더 완료 이벤트 로깅`() {
        // Given
        val daysUntilDue = 3

        // When
        analyticsHelper.logReminderCompleted(daysUntilDue)

        // Then
        val bundleCaptor = ArgumentCaptor.forClass(Bundle::class.java)
        verify(firebaseAnalytics).logEvent(eq("reminder_completed"), bundleCaptor.capture())

        val bundle = bundleCaptor.value
        assert(bundle.getInt("days_until_due") == daysUntilDue)
    }

    @Test
    fun `리마인더 완료 이벤트 로깅 - dueDateTime null`() {
        // Given
        val daysUntilDue: Int? = null

        // When
        analyticsHelper.logReminderCompleted(daysUntilDue)

        // Then
        val bundleCaptor = ArgumentCaptor.forClass(Bundle::class.java)
        verify(firebaseAnalytics).logEvent(eq("reminder_completed"), bundleCaptor.capture())

        val bundle = bundleCaptor.value
        assert(bundle.getInt("days_until_due", -1) == -1)
    }

    @Test
    fun `리마인더 삭제 이벤트 로깅`() {
        // When
        analyticsHelper.logReminderDeleted()

        // Then
        verify(firebaseAnalytics).logEvent(eq("reminder_deleted"), any())
    }

    @Test
    fun `리마인더 수정 이벤트 로깅`() {
        // When
        analyticsHelper.logReminderEdited()

        // Then
        verify(firebaseAnalytics).logEvent(eq("reminder_edited"), any())
    }

    @Test
    fun `서브태스크 추가 이벤트 로깅`() {
        // When
        analyticsHelper.logSubtaskAdded()

        // Then
        verify(firebaseAnalytics).logEvent(eq("subtask_added"), any())
    }

    @Test
    fun `이미지 첨부 이벤트 로깅`() {
        // When
        analyticsHelper.logImageAttached()

        // Then
        verify(firebaseAnalytics).logEvent(eq("image_attached"), any())
    }

    @Test
    fun `템플릿 생성 이벤트 로깅`() {
        // When
        analyticsHelper.logTemplateCreated()

        // Then
        verify(firebaseAnalytics).logEvent(eq("template_created"), any())
    }

    @Test
    fun `템플릿 사용 이벤트 로깅`() {
        // Given
        val templateName = "Daily Task"

        // When
        analyticsHelper.logTemplateUsed(templateName)

        // Then
        val bundleCaptor = ArgumentCaptor.forClass(Bundle::class.java)
        verify(firebaseAnalytics).logEvent(eq("template_used"), bundleCaptor.capture())

        val bundle = bundleCaptor.value
        assert(bundle.getString("template_name") == templateName)
    }

    @Test
    fun `배치 작업 이벤트 로깅`() {
        // Given
        val operationType = "delete"
        val count = 5

        // When
        analyticsHelper.logBatchOperation(operationType, count)

        // Then
        val bundleCaptor = ArgumentCaptor.forClass(Bundle::class.java)
        verify(firebaseAnalytics).logEvent(eq("batch_operation"), bundleCaptor.capture())

        val bundle = bundleCaptor.value
        assert(bundle.getString("operation_type") == operationType)
        assert(bundle.getInt("count") == count)
    }

    @Test
    fun `검색 수행 이벤트 로깅`() {
        // Given
        val queryLength = 10

        // When
        analyticsHelper.logSearchPerformed(queryLength)

        // Then
        val bundleCaptor = ArgumentCaptor.forClass(Bundle::class.java)
        verify(firebaseAnalytics).logEvent(eq("search_performed"), bundleCaptor.capture())

        val bundle = bundleCaptor.value
        assert(bundle.getInt("query_length") == queryLength)
    }

    @Test
    fun `필터 적용 이벤트 로깅`() {
        // Given
        val filterType = "priority"

        // When
        analyticsHelper.logFilterApplied(filterType)

        // Then
        val bundleCaptor = ArgumentCaptor.forClass(Bundle::class.java)
        verify(firebaseAnalytics).logEvent(eq("filter_applied"), bundleCaptor.capture())

        val bundle = bundleCaptor.value
        assert(bundle.getString("filter_type") == filterType)
    }

    @Test
    fun `정렬 변경 이벤트 로깅`() {
        // Given
        val sortOption = "dueDate"

        // When
        analyticsHelper.logSortChanged(sortOption)

        // Then
        val bundleCaptor = ArgumentCaptor.forClass(Bundle::class.java)
        verify(firebaseAnalytics).logEvent(eq("sort_changed"), bundleCaptor.capture())

        val bundle = bundleCaptor.value
        assert(bundle.getString("sort_option") == sortOption)
    }

    @Test
    fun `테마 변경 이벤트 로깅`() {
        // Given
        val themeName = "DARK"

        // When
        analyticsHelper.logThemeChanged(themeName)

        // Then
        val bundleCaptor = ArgumentCaptor.forClass(Bundle::class.java)
        verify(firebaseAnalytics).logEvent(eq("theme_changed"), bundleCaptor.capture())

        val bundle = bundleCaptor.value
        assert(bundle.getString("theme_name") == themeName)
    }

    @Test
    fun `알림 설정 변경 이벤트 로깅`() {
        // Given
        val settingKey = "notification_sound"
        val value = "enabled"

        // When
        analyticsHelper.logNotificationSettingsChanged(settingKey, value)

        // Then
        val bundleCaptor = ArgumentCaptor.forClass(Bundle::class.java)
        verify(firebaseAnalytics).logEvent(eq("notification_settings_changed"), bundleCaptor.capture())

        val bundle = bundleCaptor.value
        assert(bundle.getString("setting_key") == settingKey)
        assert(bundle.getString("value") == value)
    }

    @Test
    fun `간편 모드 전환 이벤트 로깅`() {
        // Given
        val enabled = true

        // When
        analyticsHelper.logSimpleModeToggled(enabled)

        // Then
        val bundleCaptor = ArgumentCaptor.forClass(Bundle::class.java)
        verify(firebaseAnalytics).logEvent(eq("simple_mode_toggled"), bundleCaptor.capture())

        val bundle = bundleCaptor.value
        assert(bundle.getBoolean("enabled") == enabled)
    }
}
