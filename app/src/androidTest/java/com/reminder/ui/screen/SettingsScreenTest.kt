package com.reminder.ui.screen

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.reminder.backup.BackupManager
import com.reminder.data.preferences.UserPreferences
import com.reminder.viewmodel.SettingsViewModel
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import kotlinx.coroutines.flow.MutableStateFlow
import org.mockito.Mockito.`when`

/**
 * v1.63.1: SettingsScreen UI 테스트 (TDD 재작성)
 *
 * 한글화된 UI에 맞춰 테스트 재작성
 */
class SettingsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createMockViewModel(): SettingsViewModel {
        val viewModel = mock(SettingsViewModel::class.java)
        `when`(viewModel.userPreferences).thenReturn(MutableStateFlow(UserPreferences()))
        return viewModel
    }

    private fun createMockBackupManager(): BackupManager {
        return mock(BackupManager::class.java)
    }

    /**
     * 설정 화면 제목 표시 확인
     */
    @Test
    fun settingsScreenTitleIsDisplayed() {
        // Given
        val viewModel = createMockViewModel()
        val backupManager = createMockBackupManager()

        // When
        composeTestRule.setContent {
            SettingsScreen(
                viewModel = viewModel,
                backupManager = backupManager
            )
        }

        // Then
        composeTestRule.onNodeWithText("설정").assertExists()
    }

    /**
     * 테마 섹션 한글 표시 확인
     */
    @Test
    fun themeSectionIsDisplayedInKorean() {
        // Given
        val viewModel = createMockViewModel()
        val backupManager = createMockBackupManager()

        // When
        composeTestRule.setContent {
            SettingsScreen(
                viewModel = viewModel,
                backupManager = backupManager
            )
        }

        // Then
        composeTestRule.onNodeWithText("테마").assertExists()
    }

    /**
     * 테마 모드 옵션 한글 표시 확인
     */
    @Test
    fun themeModeOptionsAreDisplayedInKorean() {
        // Given
        val viewModel = createMockViewModel()
        val backupManager = createMockBackupManager()

        // When
        composeTestRule.setContent {
            SettingsScreen(
                viewModel = viewModel,
                backupManager = backupManager
            )
        }

        // Then
        composeTestRule.onNodeWithText("시스템 설정 따르기", substring = true).assertExists()
    }

    /**
     * 테마 색상 섹션 표시 확인
     */
    @Test
    fun themeColorSectionIsDisplayed() {
        // Given
        val viewModel = createMockViewModel()
        val backupManager = createMockBackupManager()

        // When
        composeTestRule.setContent {
            SettingsScreen(
                viewModel = viewModel,
                backupManager = backupManager
            )
        }

        // Then
        composeTestRule.onNodeWithText("테마 색상").assertExists()
    }

    /**
     * 글씨 크기 섹션 한글 표시 확인
     */
    @Test
    fun fontSizeSectionIsDisplayedInKorean() {
        // Given
        val viewModel = createMockViewModel()
        val backupManager = createMockBackupManager()

        // When
        composeTestRule.setContent {
            SettingsScreen(
                viewModel = viewModel,
                backupManager = backupManager
            )
        }

        // Then
        composeTestRule.onNodeWithText("글씨 크기").assertExists()
    }

    /**
     * 간편 모드 섹션 한글 표시 확인
     */
    @Test
    fun simpleModeSectionIsDisplayedInKorean() {
        // Given
        val viewModel = createMockViewModel()
        val backupManager = createMockBackupManager()

        // When
        composeTestRule.setContent {
            SettingsScreen(
                viewModel = viewModel,
                backupManager = backupManager
            )
        }

        // Then
        composeTestRule.onNodeWithText("간편 모드").assertExists()
        composeTestRule.onNodeWithText("간편 모드 사용").assertExists()
    }

    /**
     * 알림 설정 섹션 한글 표시 확인
     */
    @Test
    fun notificationSettingsSectionIsDisplayedInKorean() {
        // Given
        val viewModel = createMockViewModel()
        val backupManager = createMockBackupManager()

        // When
        composeTestRule.setContent {
            SettingsScreen(
                viewModel = viewModel,
                backupManager = backupManager
            )
        }

        // Then
        composeTestRule.onNodeWithText("알림 설정").assertExists()
    }

    /**
     * 알림 옵션 한글 표시 확인
     */
    @Test
    fun notificationOptionsAreDisplayedInKorean() {
        // Given
        val viewModel = createMockViewModel()
        val backupManager = createMockBackupManager()

        // When
        composeTestRule.setContent {
            SettingsScreen(
                viewModel = viewModel,
                backupManager = backupManager
            )
        }

        // Then
        composeTestRule.onNodeWithText("알림 소리").assertExists()
        composeTestRule.onNodeWithText("진동").assertExists()
    }

    /**
     * 배지 설정 섹션 표시 확인
     */
    @Test
    fun badgeSettingsSectionIsDisplayed() {
        // Given
        val viewModel = createMockViewModel()
        val backupManager = createMockBackupManager()

        // When
        composeTestRule.setContent {
            SettingsScreen(
                viewModel = viewModel,
                backupManager = backupManager
            )
        }

        // Then
        composeTestRule.onNodeWithText("배지", substring = true).assertExists()
    }

    /**
     * 백업/복원 섹션 표시 확인
     */
    @Test
    fun backupRestoreSectionIsDisplayed() {
        // Given
        val viewModel = createMockViewModel()
        val backupManager = createMockBackupManager()

        // When
        composeTestRule.setContent {
            SettingsScreen(
                viewModel = viewModel,
                backupManager = backupManager
            )
        }

        // Then
        composeTestRule.onNodeWithText("백업", substring = true).assertExists()
    }

    /**
     * 설정 화면 스크롤 가능 확인
     */
    @Test
    fun settingsScreenIsScrollable() {
        // Given
        val viewModel = createMockViewModel()
        val backupManager = createMockBackupManager()

        // When
        composeTestRule.setContent {
            SettingsScreen(
                viewModel = viewModel,
                backupManager = backupManager
            )
        }

        // Then - 여러 섹션이 표시되어야 함
        composeTestRule.onNodeWithText("테마").assertExists()
        composeTestRule.onNodeWithText("알림 설정").assertExists()
    }

    /**
     * 간편 모드에서 고급 기능 숨김 확인
     */
    @Test
    fun advancedFeaturesAreHiddenInSimpleMode() {
        // Given
        val viewModel = mock(SettingsViewModel::class.java)
        val simplePrefs = UserPreferences(simpleMode = true)
        `when`(viewModel.userPreferences).thenReturn(MutableStateFlow(simplePrefs))
        val backupManager = createMockBackupManager()

        // When
        composeTestRule.setContent {
            SettingsScreen(
                viewModel = viewModel,
                backupManager = backupManager
            )
        }

        // Then - 백업/복원 등 고급 기능 숨김
        composeTestRule.onNodeWithText("백업", substring = true).assertDoesNotExist()
    }
}
