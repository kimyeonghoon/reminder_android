package com.reminder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reminder.data.preferences.FontSize
import com.reminder.data.preferences.PreferencesRepository
import com.reminder.data.preferences.ThemeMode
import com.reminder.data.preferences.UserPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val preferencesRepository: PreferencesRepository
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
        }
    }
}
