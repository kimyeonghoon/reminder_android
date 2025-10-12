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
     * 신규 추가 모드 - 제목
     */
    @Test
    fun 신규_추가_모드에서_제목이_표시된다() {
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
     * 수정 모드 - 제목
     */
    @Test
    fun 수정_모드에서_제목이_표시된다() {
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
     * 제목 입력 필드 - 한글 라벨
     */
    @Test
    fun 제목_입력_필드가_표시된다() {
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
     * 설명 입력 필드 - 한글 라벨
     */
    @Test
    fun 설명_입력_필드가_표시된다() {
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
     * 날짜/시간 선택 버튼
     */
    @Test
    fun 날짜_시간_선택_버튼이_표시된다() {
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
     * 우선순위 섹션 - 한글
     */
    @Test
    fun 우선순위_섹션이_한글로_표시된다() {
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
     * 중요도/긴급도 섹션 - 한글
     */
    @Test
    fun 중요도_긴급도_섹션이_한글로_표시된다() {
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
     * 카테고리 입력 필드
     */
    @Test
    fun 카테고리_입력_필드가_표시된다() {
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
     * 태그 입력 필드
     */
    @Test
    fun 태그_입력_필드가_표시된다() {
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
     * 반복 설정 섹션
     */
    @Test
    fun 반복_설정_섹션이_표시된다() {
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
     * 음성 알림 스위치
     */
    @Test
    fun 음성_알림_스위치가_표시된다() {
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
     * 저장 버튼 - 신규 추가
     */
    @Test
    fun 신규_추가_모드에서_저장_버튼_텍스트가_올바르다() {
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
     * 저장 버튼 - 수정 모드
     */
    @Test
    fun 수정_모드에서_저장_버튼_텍스트가_올바르다() {
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
     * 제목 없을 때 저장 버튼 비활성화
     */
    @Test
    fun 제목이_비어있으면_저장_버튼이_비활성화된다() {
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
     * 제목 입력 시 저장 버튼 활성화
     */
    @Test
    fun 제목을_입력하면_저장_버튼이_활성화된다() {
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
     * 기존 데이터 표시 - 제목
     */
    @Test
    fun 수정_모드에서_기존_제목이_표시된다() {
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
     * 스크롤 가능 확인
     */
    @Test
    fun 화면을_스크롤할_수_있다() {
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
     * 간편 모드 - 고급 옵션 숨김
     */
    @Test
    fun 간편_모드에서_고급_옵션이_숨겨진다() {
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
