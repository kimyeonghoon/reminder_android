package com.reminder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reminder.analytics.AnalyticsHelper
import com.reminder.data.preferences.FontSize
import com.reminder.data.preferences.PreferencesRepository
import com.reminder.data.preferences.ThemeMode
import com.reminder.data.preferences.ThemePreset
import com.reminder.data.preferences.UserPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val preferencesRepository: PreferencesRepository,
    private val analyticsHelper: AnalyticsHelper
) : ViewModel() {

    val userPreferences: StateFlow<UserPreferences> =
        preferencesRepository.userPreferences
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = UserPreferences()
            )

    fun updateThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            preferencesRepository.updateThemeMode(themeMode)

            // Analytics 이벤트 로깅
            analyticsHelper.logThemeChanged(themeMode.name)
        }
    }

    fun updateThemePreset(themePreset: ThemePreset) {
        viewModelScope.launch {
            preferencesRepository.updateThemePreset(themePreset)

            // Analytics 이벤트 로깅
            analyticsHelper.logThemeChanged("preset_${themePreset.name}")
        }
    }

    fun updateDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateDynamicColor(enabled)
        }
    }

    fun setOnboardingCompleted() {
        viewModelScope.launch {
            preferencesRepository.updateOnboardingCompleted(true)
        }
    }

    fun updateFontSize(fontSize: FontSize) {
        viewModelScope.launch {
            preferencesRepository.updateFontSize(fontSize)
        }
    }

    fun updateSimpleMode(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateSimpleMode(enabled)

            // Analytics 이벤트 로깅
            analyticsHelper.logSimpleModeToggled(enabled)
        }
    }

    fun updateNotificationSound(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateNotificationSound(enabled)

            // Analytics 이벤트 로깅
            analyticsHelper.logNotificationSettingsChanged("sound", enabled.toString())
        }
    }

    fun updateNotificationVibration(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateNotificationVibration(enabled)

            // Analytics 이벤트 로깅
            analyticsHelper.logNotificationSettingsChanged("vibration", enabled.toString())
        }
    }

    fun updateNotificationLed(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateNotificationLed(enabled)

            // Analytics 이벤트 로깅
            analyticsHelper.logNotificationSettingsChanged("led", enabled.toString())
        }
    }
}
