package com.reminder.data.preferences

import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class PreferencesRepositoryTest {

    @Test
    fun `UserPreferences 초기값은 시스템 테마와 동적 컬러 활성화이다`() {
        // Given & When
        val preferences = UserPreferences()

        // Then
        assertEquals(ThemeMode.SYSTEM, preferences.themeMode)
        assertTrue(preferences.dynamicColor)
    }

    @Test
    fun `ThemeMode의 모든 값이 정의되어 있다`() {
        // When
        val themeModes = ThemeMode.values()

        // Then
        assertEquals(3, themeModes.size)
        assertTrue(themeModes.contains(ThemeMode.LIGHT))
        assertTrue(themeModes.contains(ThemeMode.DARK))
        assertTrue(themeModes.contains(ThemeMode.SYSTEM))
    }

    @Test
    fun `UserPreferences copy로 themeMode를 변경할 수 있다`() {
        // Given
        val preferences = UserPreferences()

        // When
        val updated = preferences.copy(themeMode = ThemeMode.DARK)

        // Then
        assertEquals(ThemeMode.DARK, updated.themeMode)
        assertTrue(updated.dynamicColor)
    }

    @Test
    fun `UserPreferences copy로 dynamicColor를 변경할 수 있다`() {
        // Given
        val preferences = UserPreferences()

        // When
        val updated = preferences.copy(dynamicColor = false)

        // Then
        assertEquals(ThemeMode.SYSTEM, updated.themeMode)
        assertFalse(updated.dynamicColor)
    }

    @Test
    fun `FontSize의 모든 값이 정의되어 있다`() {
        // When
        val fontSizes = FontSize.values()

        // Then
        assertEquals(4, fontSizes.size)
        assertTrue(fontSizes.contains(FontSize.SMALL))
        assertTrue(fontSizes.contains(FontSize.NORMAL))
        assertTrue(fontSizes.contains(FontSize.LARGE))
        assertTrue(fontSizes.contains(FontSize.EXTRA_LARGE))
    }

    @Test
    fun `UserPreferences 초기 fontSize는 NORMAL이다`() {
        // Given & When
        val preferences = UserPreferences()

        // Then
        assertEquals(FontSize.NORMAL, preferences.fontSize)
    }

    @Test
    fun `UserPreferences copy로 fontSize를 변경할 수 있다`() {
        // Given
        val preferences = UserPreferences()

        // When
        val updated = preferences.copy(fontSize = FontSize.LARGE)

        // Then
        assertEquals(FontSize.LARGE, updated.fontSize)
        assertEquals(ThemeMode.SYSTEM, updated.themeMode)
    }
}
