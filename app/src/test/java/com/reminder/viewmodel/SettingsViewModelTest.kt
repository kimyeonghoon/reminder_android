package com.reminder.viewmodel

import com.reminder.analytics.AnalyticsHelper
import com.reminder.data.preferences.FontSize
import com.reminder.data.preferences.Language
import com.reminder.data.preferences.PreferencesRepository
import com.reminder.data.preferences.ThemeMode
import com.reminder.data.preferences.ThemePreset
import com.reminder.data.preferences.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

/**
 * SettingsViewModel 테스트
 *
 * AAA 패턴 (Given-When-Then)으로 설정 메서드 검증
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private lateinit var preferencesRepository: PreferencesRepository
    private lateinit var analyticsHelper: AnalyticsHelper
    private lateinit var viewModel: SettingsViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        preferencesRepository = mock(PreferencesRepository::class.java)
        analyticsHelper = mock(AnalyticsHelper::class.java)

        // 기본 설정으로 초기화
        `when`(preferencesRepository.userPreferences).thenReturn(flowOf(UserPreferences()))

        viewModel = SettingsViewModel(preferencesRepository, analyticsHelper)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /** 초기 userPreferences는 기본값이다 */
    @Test
    fun initialUserPreferencesIsDefault() {
        assertEquals(UserPreferences(), viewModel.userPreferences.value)
    }

    /** updateLanguage는 언어를 업데이트한다 */
    @Test
    fun updateLanguageUpdatesLanguage() = runTest {
        // Given
        val language = Language.ENGLISH
        whenever(preferencesRepository.updateLanguage(any())).thenReturn(Unit)

        // When
        viewModel.updateLanguage(language)
        advanceUntilIdle()

        // Then
        verify(preferencesRepository).updateLanguage(language)
        verify(analyticsHelper).logLanguageChanged(language.code)
    }

    /** updateThemeMode는 테마 모드를 업데이트한다 */
    @Test
    fun updateThemeModeUpdatesThemeMode() = runTest {
        // Given
        val themeMode = ThemeMode.DARK
        whenever(preferencesRepository.updateThemeMode(any())).thenReturn(Unit)

        // When
        viewModel.updateThemeMode(themeMode)
        advanceUntilIdle()

        // Then
        verify(preferencesRepository).updateThemeMode(themeMode)
        verify(analyticsHelper).logThemeChanged(themeMode.name)
    }

    /** updateThemePreset는 테마 프리셋을 업데이트한다 */
    @Test
    fun updateThemePresetUpdatesThemePreset() = runTest {
        // Given
        val themePreset = ThemePreset.BLUE
        whenever(preferencesRepository.updateThemePreset(any())).thenReturn(Unit)

        // When
        viewModel.updateThemePreset(themePreset)
        advanceUntilIdle()

        // Then
        verify(preferencesRepository).updateThemePreset(themePreset)
        verify(analyticsHelper).logThemeChanged("preset_${themePreset.name}")
    }

    /** updateDynamicColor는 다이나믹 컬러를 업데이트한다 */
    @Test
    fun updateDynamicColorUpdatesDynamicColor() = runTest {
        // Given
        whenever(preferencesRepository.updateDynamicColor(any())).thenReturn(Unit)

        // When
        viewModel.updateDynamicColor(true)
        advanceUntilIdle()

        // Then
        verify(preferencesRepository).updateDynamicColor(true)
    }

    /** setOnboardingCompleted는 온보딩 완료를 설정한다 */
    @Test
    fun setOnboardingCompletedSetsOnboardingCompleted() = runTest {
        // Given
        whenever(preferencesRepository.updateOnboardingCompleted(any())).thenReturn(Unit)

        // When
        viewModel.setOnboardingCompleted()
        advanceUntilIdle()

        // Then
        verify(preferencesRepository).updateOnboardingCompleted(true)
    }

    /** updateFontSize는 폰트 크기를 업데이트한다 */
    @Test
    fun updateFontSizeUpdatesFontSize() = runTest {
        // Given
        val fontSize = FontSize.LARGE
        whenever(preferencesRepository.updateFontSize(any())).thenReturn(Unit)

        // When
        viewModel.updateFontSize(fontSize)
        advanceUntilIdle()

        // Then
        verify(preferencesRepository).updateFontSize(fontSize)
    }

    /** updateSimpleMode는 간단한 모드를 업데이트한다 */
    @Test
    fun updateSimpleModeUpdatesSimpleMode() = runTest {
        // Given
        whenever(preferencesRepository.updateSimpleMode(any())).thenReturn(Unit)

        // When
        viewModel.updateSimpleMode(true)
        advanceUntilIdle()

        // Then
        verify(preferencesRepository).updateSimpleMode(true)
        verify(analyticsHelper).logSimpleModeToggled(true)
    }

    /** updateNotificationSound는 알림 사운드를 업데이트한다 */
    @Test
    fun updateNotificationSoundUpdatesNotificationSound() = runTest {
        // Given
        whenever(preferencesRepository.updateNotificationSound(any())).thenReturn(Unit)

        // When
        viewModel.updateNotificationSound(true)
        advanceUntilIdle()

        // Then
        verify(preferencesRepository).updateNotificationSound(true)
        verify(analyticsHelper).logNotificationSettingsChanged("sound", "true")
    }

    /** updateNotificationVibration는 알림 진동을 업데이트한다 */
    @Test
    fun updateNotificationVibrationUpdatesNotificationVibration() = runTest {
        // Given
        whenever(preferencesRepository.updateNotificationVibration(any())).thenReturn(Unit)

        // When
        viewModel.updateNotificationVibration(false)
        advanceUntilIdle()

        // Then
        verify(preferencesRepository).updateNotificationVibration(false)
        verify(analyticsHelper).logNotificationSettingsChanged("vibration", "false")
    }

    /** updateBadgeEnabled는 배지를 업데이트한다 */
    @Test
    fun updateBadgeEnabledUpdatesBadgeEnabled() = runTest {
        // Given
        whenever(preferencesRepository.updateBadgeEnabled(any())).thenReturn(Unit)

        // When
        viewModel.updateBadgeEnabled(true)
        advanceUntilIdle()

        // Then
        verify(preferencesRepository).updateBadgeEnabled(true)
    }
}
