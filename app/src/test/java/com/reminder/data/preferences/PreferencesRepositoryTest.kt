package com.reminder.data.preferences

import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class PreferencesRepositoryTest {

    /** UserPreferences 초기값은 시스템 테마와 동적 컬러 활성화이다 */
    @Test
    fun userPreferencesDefaultValuesAreSystemThemeAndDynamicColorEnabled() {
        // Given & When
        val preferences = UserPreferences()

        // Then
        assertEquals(ThemeMode.SYSTEM, preferences.themeMode)
        assertTrue(preferences.dynamicColor)
    }

    /** ThemeMode의 모든 값이 정의되어 있다 */
    @Test
    fun themeModeHasAllValuesDefined() {
        // When
        val themeModes = ThemeMode.values()

        // Then
        assertEquals(3, themeModes.size)
        assertTrue(themeModes.contains(ThemeMode.LIGHT))
        assertTrue(themeModes.contains(ThemeMode.DARK))
        assertTrue(themeModes.contains(ThemeMode.SYSTEM))
    }

    /** UserPreferences copy로 themeMode를 변경할 수 있다 */
    @Test
    fun userPreferencesCopyCanChangeThemeMode() {
        // Given
        val preferences = UserPreferences()

        // When
        val updated = preferences.copy(themeMode = ThemeMode.DARK)

        // Then
        assertEquals(ThemeMode.DARK, updated.themeMode)
        assertTrue(updated.dynamicColor)
    }

    /** UserPreferences copy로 dynamicColor를 변경할 수 있다 */
    @Test
    fun userPreferencesCopyCanChangeDynamicColor() {
        // Given
        val preferences = UserPreferences()

        // When
        val updated = preferences.copy(dynamicColor = false)

        // Then
        assertEquals(ThemeMode.SYSTEM, updated.themeMode)
        assertFalse(updated.dynamicColor)
    }

    /** FontSize의 모든 값이 정의되어 있다 */
    @Test
    fun fontSizeHasAllValuesDefined() {
        // When
        val fontSizes = FontSize.values()

        // Then
        assertEquals(4, fontSizes.size)
        assertTrue(fontSizes.contains(FontSize.SMALL))
        assertTrue(fontSizes.contains(FontSize.NORMAL))
        assertTrue(fontSizes.contains(FontSize.LARGE))
        assertTrue(fontSizes.contains(FontSize.EXTRA_LARGE))
    }

    /** UserPreferences 초기 fontSize는 NORMAL이다 */
    @Test
    fun userPreferencesDefaultFontSizeIsNormal() {
        // Given & When
        val preferences = UserPreferences()

        // Then
        assertEquals(FontSize.NORMAL, preferences.fontSize)
    }

    /** UserPreferences copy로 fontSize를 변경할 수 있다 */
    @Test
    fun userPreferencesCopyCanChangeFontSize() {
        // Given
        val preferences = UserPreferences()

        // When
        val updated = preferences.copy(fontSize = FontSize.LARGE)

        // Then
        assertEquals(FontSize.LARGE, updated.fontSize)
        assertEquals(ThemeMode.SYSTEM, updated.themeMode)
    }

    /** UserPreferences 초기 simpleMode는 true이다 (70대 사용자 기본값) */
    @Test
    fun userPreferencesDefaultSimpleModeIsTrue() {
        // Given & When
        val preferences = UserPreferences()

        // Then
        assertTrue(preferences.simpleMode)
    }

    /** UserPreferences copy로 simpleMode를 변경할 수 있다 */
    @Test
    fun userPreferencesCopyCanChangeSimpleMode() {
        // Given
        val preferences = UserPreferences()

        // When
        val updated = preferences.copy(simpleMode = true)

        // Then
        assertTrue(updated.simpleMode)
        assertEquals(ThemeMode.SYSTEM, updated.themeMode)
    }
}
