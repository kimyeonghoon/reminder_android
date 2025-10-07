package com.reminder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.reminder.data.preferences.PreferencesRepository

class SettingsViewModelFactory(
    private val preferencesRepository: PreferencesRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            return SettingsViewModel(preferencesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
