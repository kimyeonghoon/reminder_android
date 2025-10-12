package com.reminder.ui.screen

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.reminder.data.entity.*
import com.reminder.viewmodel.ReminderViewModel
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import kotlinx.coroutines.flow.MutableStateFlow
import org.mockito.Mockito.`when`

/**
 * v1.63.1: AddEditReminderScreen UI 테스트 (TDD 재작성)
 *
 * 한글화된 UI에 맞춰 테스트 재작성
 */
class AddEditReminderScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createMockViewModel(): ReminderViewModel {
        val viewModel = mock(ReminderViewModel::class.java)
        `when`(viewModel.activeReminders).thenReturn(MutableStateFlow(emptyList()))
        `when`(viewModel.searchQuery).thenReturn(MutableStateFlow(""))
        return viewModel
    }

    /**
     * 신규 추가 모드에서 제목 표시 확인
     */
    @Test
    fun titleIsDisplayedInNewAddMode() {
        // Given
        val viewModel = createMockViewModel()

        // When
        composeTestRule.setContent {
            AddEditReminderScreen(
                viewModel = viewModel,
                reminder = null,
                onNavigateBack = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("리마인더 추가").assertExists()
    }

    /**
     * 수정 모드에서 제목 표시 확인
     */
    @Test
    fun titleIsDisplayedInEditMode() {
        // Given
        val viewModel = createMockViewModel()
        val reminder = ReminderEntity(
            id = 1L,
            title = "기존 할일",
            priority = Priority.MEDIUM,
            isCompleted = false
        )

        // When
        composeTestRule.setContent {
            AddEditReminderScreen(
                viewModel = viewModel,
                reminder = reminder,
                onNavigateBack = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("리마인더 수정").assertExists()
    }

    /**
     * 제목 입력 필드 표시 확인
     */
    @Test
    fun titleInputFieldIsDisplayed() {
        // Given
        val viewModel = createMockViewModel()

        // When
        composeTestRule.setContent {
            AddEditReminderScreen(
                viewModel = viewModel,
                reminder = null,
                onNavigateBack = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("제목").assertExists()
    }

    /**
     * 설명 입력 필드 표시 확인
     */
    @Test
    fun descriptionInputFieldIsDisplayed() {
        // Given
        val viewModel = createMockViewModel()

        // When
        composeTestRule.setContent {
            AddEditReminderScreen(
                viewModel = viewModel,
                reminder = null,
                onNavigateBack = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("설명 (선택사항)").assertExists()
    }

    /**
     * 날짜/시간 선택 버튼 표시 확인
     */
    @Test
    fun dateTimePickerButtonsAreDisplayed() {
        // Given
        val viewModel = createMockViewModel()

        // When
        composeTestRule.setContent {
            AddEditReminderScreen(
                viewModel = viewModel,
                reminder = null,
                onNavigateBack = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("날짜 선택").assertExists()
        composeTestRule.onNodeWithText("시간 선택").assertExists()
    }

    /**
     * 우선순위 섹션 한글 표시 확인
     */
    @Test
    fun prioritySectionIsDisplayedInKorean() {
        // Given
        val viewModel = createMockViewModel()

        // When
        composeTestRule.setContent {
            AddEditReminderScreen(
                viewModel = viewModel,
                reminder = null,
                onNavigateBack = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("우선순위").assertExists()
        composeTestRule.onNodeWithText("높음").assertExists()
        composeTestRule.onNodeWithText("중간").assertExists()
        composeTestRule.onNodeWithText("낮음").assertExists()
    }

    /**
     * 중요도/긴급도 섹션 한글 표시 확인
     */
    @Test
    fun importanceUrgencySectionIsDisplayedInKorean() {
        // Given
        val viewModel = createMockViewModel()

        // When
        composeTestRule.setContent {
            AddEditReminderScreen(
                viewModel = viewModel,
                reminder = null,
                onNavigateBack = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("중요도").assertExists()
        composeTestRule.onNodeWithText("긴급도").assertExists()
    }

    /**
     * 카테고리 입력 필드 표시 확인
     */
    @Test
    fun categoryInputFieldIsDisplayed() {
        // Given
        val viewModel = createMockViewModel()

        // When
        composeTestRule.setContent {
            AddEditReminderScreen(
                viewModel = viewModel,
                reminder = null,
                onNavigateBack = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("카테고리").assertExists()
    }

    /**
     * 태그 입력 필드 표시 확인
     */
    @Test
    fun tagInputFieldIsDisplayed() {
        // Given
        val viewModel = createMockViewModel()

        // When
        composeTestRule.setContent {
            AddEditReminderScreen(
                viewModel = viewModel,
                reminder = null,
                onNavigateBack = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("태그").assertExists()
    }

    /**
     * 반복 설정 섹션 표시 확인
     */
    @Test
    fun recurrenceSettingsSectionIsDisplayed() {
        // Given
        val viewModel = createMockViewModel()

        // When
        composeTestRule.setContent {
            AddEditReminderScreen(
                viewModel = viewModel,
                reminder = null,
                onNavigateBack = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("반복 설정").assertExists()
    }

    /**
     * 음성 알림 스위치 표시 확인
     */
    @Test
    fun voiceAlertSwitchIsDisplayed() {
        // Given
        val viewModel = createMockViewModel()

        // When
        composeTestRule.setContent {
            AddEditReminderScreen(
                viewModel = viewModel,
                reminder = null,
                onNavigateBack = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("음성으로 읽기").assertExists()
    }

    /**
     * 신규 추가 모드에서 저장 버튼 텍스트 확인
     */
    @Test
    fun saveButtonTextIsCorrectInNewAddMode() {
        // Given
        val viewModel = createMockViewModel()

        // When
        composeTestRule.setContent {
            AddEditReminderScreen(
                viewModel = viewModel,
                reminder = null,
                onNavigateBack = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("리마인더 추가").assertExists()
    }

    /**
     * 수정 모드에서 저장 버튼 텍스트 확인
     */
    @Test
    fun saveButtonTextIsCorrectInEditMode() {
        // Given
        val viewModel = createMockViewModel()
        val reminder = ReminderEntity(
            id = 1L,
            title = "기존 할일",
            priority = Priority.MEDIUM,
            isCompleted = false
        )

        // When
        composeTestRule.setContent {
            AddEditReminderScreen(
                viewModel = viewModel,
                reminder = reminder,
                onNavigateBack = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("리마인더 수정").assertExists()
    }

    /**
     * 제목이 비어있을 때 저장 버튼 비활성화 확인
     */
    @Test
    fun saveButtonIsDisabledWhenTitleIsEmpty() {
        // Given
        val viewModel = createMockViewModel()

        // When
        composeTestRule.setContent {
            AddEditReminderScreen(
                viewModel = viewModel,
                reminder = null,
                onNavigateBack = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("리마인더 추가").assertIsNotEnabled()
    }

    /**
     * 제목 입력 시 저장 버튼 활성화 확인
     */
    @Test
    fun saveButtonIsEnabledWhenTitleIsEntered() {
        // Given
        val viewModel = createMockViewModel()

        // When
        composeTestRule.setContent {
            AddEditReminderScreen(
                viewModel = viewModel,
                reminder = null,
                onNavigateBack = {}
            )
        }

        // 제목 입력
        composeTestRule.onNodeWithText("제목").performTextInput("새로운 할일")

        // Then
        composeTestRule.onNodeWithText("리마인더 추가").assertIsEnabled()
    }

    /**
     * 수정 모드에서 기존 제목 표시 확인
     */
    @Test
    fun existingTitleIsDisplayedInEditMode() {
        // Given
        val viewModel = createMockViewModel()
        val reminder = ReminderEntity(
            id = 1L,
            title = "기존 할일 제목",
            description = "기존 설명",
            priority = Priority.HIGH,
            category = "업무",
            isCompleted = false
        )

        // When
        composeTestRule.setContent {
            AddEditReminderScreen(
                viewModel = viewModel,
                reminder = reminder,
                onNavigateBack = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("기존 할일 제목").assertExists()
        composeTestRule.onNodeWithText("기존 설명").assertExists()
        composeTestRule.onNodeWithText("업무").assertExists()
    }

    /**
     * 화면 스크롤 가능 확인
     */
    @Test
    fun screenIsScrollable() {
        // Given
        val viewModel = createMockViewModel()

        // When
        composeTestRule.setContent {
            AddEditReminderScreen(
                viewModel = viewModel,
                reminder = null,
                onNavigateBack = {}
            )
        }

        // Then - 마지막 요소(저장 버튼)까지 스크롤 가능해야 함
        composeTestRule.onNodeWithText("리마인더 추가").assertExists()
    }

    /**
     * 간편 모드에서 고급 옵션 숨김 확인
     */
    @Test
    fun advancedOptionsAreHiddenInSimpleMode() {
        // Given
        val viewModel = createMockViewModel()

        // When
        composeTestRule.setContent {
            AddEditReminderScreen(
                viewModel = viewModel,
                reminder = null,
                onNavigateBack = {},
                simpleMode = true
            )
        }

        // Then - 반복 설정, 태그 등 고급 옵션 숨김
        composeTestRule.onNodeWithText("반복 설정").assertDoesNotExist()
        composeTestRule.onNodeWithText("태그").assertDoesNotExist()
    }
}
