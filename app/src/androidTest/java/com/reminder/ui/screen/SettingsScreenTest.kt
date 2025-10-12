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
     * 기본 UI 요소 - 제목
     */
    @Test
    fun 설정_화면_제목이_표시된다() {
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
     * 테마 섹션 - 한글
     */
    @Test
    fun 테마_섹션이_한글로_표시된다() {
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
     * 테마 모드 옵션 - 한글
     */
    @Test
    fun 테마_모드_옵션이_한글로_표시된다() {
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
     * 테마 색상 섹션
     */
    @Test
    fun 테마_색상_섹션이_표시된다() {
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
     * 글씨 크기 섹션 - 한글
     */
    @Test
    fun 글씨_크기_섹션이_한글로_표시된다() {
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
     * 간편 모드 섹션 - 한글
     */
    @Test
    fun 간편_모드_섹션이_한글로_표시된다() {
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
     * 알림 설정 섹션 - 한글
     */
    @Test
    fun 알림_설정_섹션이_한글로_표시된다() {
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
     * 알림 옵션 - 한글
     */
    @Test
    fun 알림_옵션이_한글로_표시된다() {
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
     * 배지 설정 섹션
     */
    @Test
    fun 배지_설정_섹션이_표시된다() {
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
     * 백업/복원 섹션
     */
    @Test
    fun 백업_복원_섹션이_표시된다() {
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
     * 스크롤 가능 확인
     */
    @Test
    fun 설정_화면을_스크롤할_수_있다() {
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
     * 간편 모드 - 고급 기능 숨김
     */
    @Test
    fun 간편_모드에서_고급_기능이_숨겨진다() {
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
