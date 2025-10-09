package com.reminder.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class PreferencesRepository(
    private val dataStore: DataStore<Preferences>
) {
    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val THEME_PRESET = stringPreferencesKey("theme_preset")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val HIGH_CONTRAST_MODE = booleanPreferencesKey("high_contrast_mode")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val FONT_SIZE = stringPreferencesKey("font_size")
        val SIMPLE_MODE = booleanPreferencesKey("simple_mode")

        // 알림 설정
        val NOTIFICATION_SOUND = booleanPreferencesKey("notification_sound")
        val NOTIFICATION_VIBRATION = booleanPreferencesKey("notification_vibration")
        val NOTIFICATION_LED = booleanPreferencesKey("notification_led")
    }

    val userPreferences: Flow<UserPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(androidx.datastore.preferences.core.emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val themeModeString = preferences[PreferencesKeys.THEME_MODE] ?: ThemeMode.SYSTEM.name
            val themeMode = try {
                ThemeMode.valueOf(themeModeString)
            } catch (e: IllegalArgumentException) {
                ThemeMode.SYSTEM
            }

            val themePresetString = preferences[PreferencesKeys.THEME_PRESET] ?: ThemePreset.PURPLE.name
            val themePreset = try {
                ThemePreset.valueOf(themePresetString)
            } catch (e: IllegalArgumentException) {
                ThemePreset.PURPLE
            }

            val fontSizeString = preferences[PreferencesKeys.FONT_SIZE] ?: FontSize.NORMAL.name
            val fontSize = try {
                FontSize.valueOf(fontSizeString)
            } catch (e: IllegalArgumentException) {
                FontSize.NORMAL
            }

            val dynamicColor = preferences[PreferencesKeys.DYNAMIC_COLOR] ?: true
            val highContrastMode = preferences[PreferencesKeys.HIGH_CONTRAST_MODE] ?: false
            val onboardingCompleted = preferences[PreferencesKeys.ONBOARDING_COMPLETED] ?: false
            val simpleMode = preferences[PreferencesKeys.SIMPLE_MODE] ?: false

            // 알림 설정 (기본값: 모두 활성화)
            val notificationSound = preferences[PreferencesKeys.NOTIFICATION_SOUND] ?: true
            val notificationVibration = preferences[PreferencesKeys.NOTIFICATION_VIBRATION] ?: true
            val notificationLed = preferences[PreferencesKeys.NOTIFICATION_LED] ?: true

            UserPreferences(
                themeMode = themeMode,
                themePreset = themePreset,
                dynamicColor = dynamicColor,
                highContrastMode = highContrastMode,
                onboardingCompleted = onboardingCompleted,
                fontSize = fontSize,
                simpleMode = simpleMode,
                notificationSound = notificationSound,
                notificationVibration = notificationVibration,
                notificationLed = notificationLed
            )
        }

    suspend fun updateThemeMode(themeMode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = themeMode.name
        }
    }

    suspend fun updateThemePreset(themePreset: ThemePreset) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_PRESET] = themePreset.name
        }
    }

    suspend fun updateDynamicColor(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DYNAMIC_COLOR] = enabled
        }
    }

    suspend fun updateHighContrastMode(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.HIGH_CONTRAST_MODE] = enabled
        }
    }

    suspend fun updateOnboardingCompleted(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun updateFontSize(fontSize: FontSize) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.FONT_SIZE] = fontSize.name
        }
    }

    suspend fun updateSimpleMode(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SIMPLE_MODE] = enabled
        }
    }

    suspend fun updateNotificationSound(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATION_SOUND] = enabled
        }
    }

    suspend fun updateNotificationVibration(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATION_VIBRATION] = enabled
        }
    }

    suspend fun updateNotificationLed(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATION_LED] = enabled
        }
    }

    companion object {
        fun create(context: Context): PreferencesRepository {
            return PreferencesRepository(context.dataStore)
        }
    }
}
