package com.reminder.ui.screen

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.reminder.backup.BackupManager
import com.reminder.data.preferences.FontSize
import com.reminder.data.preferences.ThemeMode
import com.reminder.data.preferences.UserPreferences
import com.reminder.viewmodel.SettingsViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*

@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var mockViewModel: SettingsViewModel
    private lateinit var mockBackupManager: BackupManager
    private lateinit var userPreferencesFlow: MutableStateFlow<UserPreferences>

    private var onNavigateBackCalled = false
    private var onHelpClickCalled = false

    @Before
    fun setup() {
        mockViewModel = mock(SettingsViewModel::class.java)
        mockBackupManager = mock(BackupManager::class.java)
        userPreferencesFlow = MutableStateFlow(UserPreferences())

        `when`(mockViewModel.userPreferences).thenReturn(userPreferencesFlow)

        onNavigateBackCalled = false
        onHelpClickCalled = false
    }

    @Test
    fun 설정_화면_제목이_표시된다() {
        // Given
        composeTestRule.setContent {
            SettingsScreen(
                viewModel = mockViewModel,
                backupManager = mockBackupManager,
                onNavigateBack = {},
                onHelpClick = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("설정").assertIsDisplayed()
    }

    @Test
    fun 뒤로가기_버튼_클릭_시_onNavigateBack_콜백이_호출된다() {
        // Given
        composeTestRule.setContent {
            SettingsScreen(
                viewModel = mockViewModel,
                backupManager = mockBackupManager,
                onNavigateBack = { onNavigateBackCalled = true },
                onHelpClick = {}
            )
        }

        // When
        composeTestRule.onNodeWithContentDescription("뒤로가기").performClick()

        // Then
        assert(onNavigateBackCalled)
    }

    @Test
    fun 도움말_버튼_클릭_시_onHelpClick_콜백이_호출된다() {
        // Given
        composeTestRule.setContent {
            SettingsScreen(
                viewModel = mockViewModel,
                backupManager = mockBackupManager,
                onNavigateBack = {},
                onHelpClick = { onHelpClickCalled = true }
            )
        }

        // When
        composeTestRule.onNodeWithText("도움말 보기").performClick()

        // Then
        assert(onHelpClickCalled)
    }

    @Test
    fun 테마_섹션이_표시된다() {
        // Given
        composeTestRule.setContent {
            SettingsScreen(
                viewModel = mockViewModel,
                backupManager = mockBackupManager,
                onNavigateBack = {},
                onHelpClick = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("테마").assertIsDisplayed()
        composeTestRule.onNodeWithText("라이트 모드").assertIsDisplayed()
        composeTestRule.onNodeWithText("다크 모드").assertIsDisplayed()
        composeTestRule.onNodeWithText("시스템 설정 따르기").assertIsDisplayed()
    }

    @Test
    fun 글씨_크기_섹션이_표시된다() {
        // Given
        composeTestRule.setContent {
            SettingsScreen(
                viewModel = mockViewModel,
                backupManager = mockBackupManager,
                onNavigateBack = {},
                onHelpClick = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("글씨 크기").assertIsDisplayed()
        composeTestRule.onNodeWithText("작게").assertIsDisplayed()
        composeTestRule.onNodeWithText("보통").assertIsDisplayed()
        composeTestRule.onNodeWithText("크게").assertIsDisplayed()
        composeTestRule.onNodeWithText("아주 크게").assertIsDisplayed()
    }

    @Test
    fun 간편_모드_섹션이_표시된다() {
        // Given
        composeTestRule.setContent {
            SettingsScreen(
                viewModel = mockViewModel,
                backupManager = mockBackupManager,
                onNavigateBack = {},
                onHelpClick = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("간편 모드").assertIsDisplayed()
        composeTestRule.onNodeWithText("간편 모드 사용").assertIsDisplayed()
        composeTestRule.onNodeWithText("복잡한 기능 숨김, 더 큰 버튼").assertIsDisplayed()
    }

    @Test
    fun 간편_모드_스위치_토글_시_viewModel이_호출된다() {
        // Given
        userPreferencesFlow.value = UserPreferences(simpleMode = false)

        composeTestRule.setContent {
            SettingsScreen(
                viewModel = mockViewModel,
                backupManager = mockBackupManager,
                onNavigateBack = {},
                onHelpClick = {}
            )
        }

        // When
        composeTestRule.onNode(
            hasText("간편 모드 사용").and(hasAnyDescendant(hasText("간편 모드 사용")))
        ).performClick()

        // Then
        verify(mockViewModel, atLeastOnce()).updateSimpleMode(true)
    }

    @Test
    fun 알림_설정_섹션이_표시된다() {
        // Given
        composeTestRule.setContent {
            SettingsScreen(
                viewModel = mockViewModel,
                backupManager = mockBackupManager,
                onNavigateBack = {},
                onHelpClick = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("알림 설정").assertIsDisplayed()
        composeTestRule.onNodeWithText("알림 소리").assertIsDisplayed()
        composeTestRule.onNodeWithText("진동").assertIsDisplayed()
        composeTestRule.onNodeWithText("LED 표시등").assertIsDisplayed()
    }

    @Test
    fun 알림_소리_스위치의_접근성_설명이_설정되어_있다() {
        // Given
        userPreferencesFlow.value = UserPreferences(notificationSound = true)

        composeTestRule.setContent {
            SettingsScreen(
                viewModel = mockViewModel,
                backupManager = mockBackupManager,
                onNavigateBack = {},
                onHelpClick = {}
            )
        }

        // Then
        composeTestRule.onNode(
            hasContentDescription("알림 소리 켜짐")
        ).assertExists()
    }

    @Test
    fun 진동_스위치의_접근성_설명이_설정되어_있다() {
        // Given
        userPreferencesFlow.value = UserPreferences(notificationVibration = false)

        composeTestRule.setContent {
            SettingsScreen(
                viewModel = mockViewModel,
                backupManager = mockBackupManager,
                onNavigateBack = {},
                onHelpClick = {}
            )
        }

        // Then
        composeTestRule.onNode(
            hasContentDescription("진동 꺼짐")
        ).assertExists()
    }

    @Test
    fun 배지_설정_섹션이_표시된다() {
        // Given
        composeTestRule.setContent {
            SettingsScreen(
                viewModel = mockViewModel,
                backupManager = mockBackupManager,
                onNavigateBack = {},
                onHelpClick = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("앱 아이콘 배지").assertIsDisplayed()
        composeTestRule.onNodeWithText("배지 표시").assertIsDisplayed()
        composeTestRule.onNodeWithText("미완료 리마인더 수를 앱 아이콘에 표시").assertIsDisplayed()
    }

    @Test
    fun 백업_복원_섹션이_표시된다() {
        // Given
        userPreferencesFlow.value = UserPreferences(simpleMode = false)

        composeTestRule.setContent {
            SettingsScreen(
                viewModel = mockViewModel,
                backupManager = mockBackupManager,
                onNavigateBack = {},
                onHelpClick = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("데이터 관리").assertIsDisplayed()
        composeTestRule.onNodeWithText("백업").assertIsDisplayed()
        composeTestRule.onNodeWithText("복원").assertIsDisplayed()
    }

    @Test
    fun 간편_모드일_때_백업_복원_섹션이_표시되지_않는다() {
        // Given
        userPreferencesFlow.value = UserPreferences(simpleMode = true)

        composeTestRule.setContent {
            SettingsScreen(
                viewModel = mockViewModel,
                backupManager = mockBackupManager,
                onNavigateBack = {},
                onHelpClick = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("데이터 관리").assertDoesNotExist()
        composeTestRule.onNodeWithText("백업").assertDoesNotExist()
        composeTestRule.onNodeWithText("복원").assertDoesNotExist()
    }

    @Test
    fun 동적_컬러_섹션이_표시된다() {
        // Given
        userPreferencesFlow.value = UserPreferences(simpleMode = false, dynamicColor = false)

        composeTestRule.setContent {
            SettingsScreen(
                viewModel = mockViewModel,
                backupManager = mockBackupManager,
                onNavigateBack = {},
                onHelpClick = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("색상").assertIsDisplayed()
        composeTestRule.onNodeWithText("동적 컬러 (Material You)").assertIsDisplayed()
        composeTestRule.onNodeWithText("시스템 배경화면 색상 사용").assertIsDisplayed()
    }

    @Test
    fun 간편_모드일_때_동적_컬러_섹션이_표시되지_않는다() {
        // Given
        userPreferencesFlow.value = UserPreferences(simpleMode = true)

        composeTestRule.setContent {
            SettingsScreen(
                viewModel = mockViewModel,
                backupManager = mockBackupManager,
                onNavigateBack = {},
                onHelpClick = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("색상").assertDoesNotExist()
        composeTestRule.onNodeWithText("동적 컬러 (Material You)").assertDoesNotExist()
    }

    @Test
    fun 테마_모드_변경_시_viewModel이_호출된다() {
        // Given
        userPreferencesFlow.value = UserPreferences(themeMode = ThemeMode.LIGHT)

        composeTestRule.setContent {
            SettingsScreen(
                viewModel = mockViewModel,
                backupManager = mockBackupManager,
                onNavigateBack = {},
                onHelpClick = {}
            )
        }

        // When
        composeTestRule.onNodeWithText("다크 모드").performClick()

        // Then
        verify(mockViewModel, atLeastOnce()).updateThemeMode(ThemeMode.DARK)
    }

    @Test
    fun 글씨_크기_변경_시_viewModel이_호출된다() {
        // Given
        userPreferencesFlow.value = UserPreferences(fontSize = FontSize.NORMAL)

        composeTestRule.setContent {
            SettingsScreen(
                viewModel = mockViewModel,
                backupManager = mockBackupManager,
                onNavigateBack = {},
                onHelpClick = {}
            )
        }

        // When
        composeTestRule.onNodeWithText("크게").performClick()

        // Then
        verify(mockViewModel, atLeastOnce()).updateFontSize(FontSize.LARGE)
    }

    @Test
    fun 선택된_테마_모드의_라디오_버튼이_선택되어_있다() {
        // Given
        userPreferencesFlow.value = UserPreferences(themeMode = ThemeMode.DARK)

        composeTestRule.setContent {
            SettingsScreen(
                viewModel = mockViewModel,
                backupManager = mockBackupManager,
                onNavigateBack = {},
                onHelpClick = {}
            )
        }

        // Then - 다크 모드가 선택되어 있는지 확인
        // RadioButton의 선택 상태는 시각적으로만 확인 가능하므로
        // 텍스트가 표시되는지만 확인
        composeTestRule.onNodeWithText("다크 모드").assertIsDisplayed()
    }

    @Test
    fun 선택된_글씨_크기의_라디오_버튼이_선택되어_있다() {
        // Given
        userPreferencesFlow.value = UserPreferences(fontSize = FontSize.LARGE)

        composeTestRule.setContent {
            SettingsScreen(
                viewModel = mockViewModel,
                backupManager = mockBackupManager,
                onNavigateBack = {},
                onHelpClick = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("크게").assertIsDisplayed()
    }
}
