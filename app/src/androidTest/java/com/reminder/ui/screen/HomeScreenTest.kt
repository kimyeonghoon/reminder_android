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
import java.time.LocalDateTime

/**
 * v1.63.1: HomeScreen UI 테스트 (TDD 재작성)
 *
 * 한글화된 UI에 맞춰 테스트 재작성
 */
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createMockViewModel(): ReminderViewModel {
        val viewModel = mock(ReminderViewModel::class.java)
        `when`(viewModel.activeReminders).thenReturn(MutableStateFlow(emptyList()))
        `when`(viewModel.searchQuery).thenReturn(MutableStateFlow(""))
        `when`(viewModel.currentFilter).thenReturn(MutableStateFlow(null))
        return viewModel
    }

    /**
     * 기본 UI 요소 검증 - 한글
     */
    @Test
    fun 홈_화면_제목이_표시된다() {
        // Given
        val viewModel = createMockViewModel()

        // When
        composeTestRule.setContent {
            HomeScreen(
                viewModel = viewModel,
                onAddClick = {},
                onReminderClick = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("리마인더").assertExists()
    }

    /**
     * 빈 상태 메시지 - 한글
     */
    @Test
    fun 리마인더가_없을_때_빈_상태_메시지가_표시된다() {
        // Given
        val viewModel = createMockViewModel()

        // When
        composeTestRule.setContent {
            HomeScreen(
                viewModel = viewModel,
                onAddClick = {},
                onReminderClick = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("할 일이 없습니다").assertExists()
    }

    /**
     * FAB 버튼 - 한글
     */
    @Test
    fun 리마인더_추가_버튼이_표시된다() {
        // Given
        val viewModel = createMockViewModel()

        // When
        composeTestRule.setContent {
            HomeScreen(
                viewModel = viewModel,
                onAddClick = {},
                onReminderClick = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("리마인더 추가").assertExists()
    }

    /**
     * 필터 버튼 - 아이콘으로 검증
     */
    @Test
    fun 필터_버튼이_표시된다() {
        // Given
        val viewModel = createMockViewModel()

        // When
        composeTestRule.setContent {
            HomeScreen(
                viewModel = viewModel,
                onAddClick = {},
                onReminderClick = {}
            )
        }

        // Then
        composeTestRule.onNodeWithContentDescription("필터").assertExists()
    }

    /**
     * 검색 버튼 - 아이콘으로 검증
     */
    @Test
    fun 검색_버튼이_표시된다() {
        // Given
        val viewModel = createMockViewModel()

        // When
        composeTestRule.setContent {
            HomeScreen(
                viewModel = viewModel,
                onAddClick = {},
                onReminderClick = {}
            )
        }

        // Then
        composeTestRule.onNodeWithContentDescription("검색").assertExists()
    }

    /**
     * Eisenhower Matrix 버튼
     */
    @Test
    fun 아이젠하워_매트릭스_버튼이_표시된다() {
        // Given
        val viewModel = createMockViewModel()

        // When
        composeTestRule.setContent {
            HomeScreen(
                viewModel = viewModel,
                onAddClick = {},
                onReminderClick = {}
            )
        }

        // Then
        composeTestRule.onNodeWithContentDescription("Eisenhower Matrix").assertExists()
    }

    /**
     * 설정 버튼
     */
    @Test
    fun 설정_버튼이_표시된다() {
        // Given
        val viewModel = createMockViewModel()

        // When
        composeTestRule.setContent {
            HomeScreen(
                viewModel = viewModel,
                onAddClick = {},
                onReminderClick = {}
            )
        }

        // Then
        composeTestRule.onNodeWithContentDescription("설정").assertExists()
    }

    /**
     * 리마인더 목록 표시
     */
    @Test
    fun 리마인더가_있을_때_목록이_표시된다() {
        // Given
        val viewModel = mock(ReminderViewModel::class.java)
        val reminder = ReminderEntity(
            id = 1L,
            title = "테스트 할일",
            description = "테스트 설명",
            priority = Priority.HIGH,
            category = "업무",
            isCompleted = false
        )
        `when`(viewModel.activeReminders).thenReturn(MutableStateFlow(listOf(reminder)))
        `when`(viewModel.searchQuery).thenReturn(MutableStateFlow(""))
        `when`(viewModel.currentFilter).thenReturn(MutableStateFlow(null))

        // When
        composeTestRule.setContent {
            HomeScreen(
                viewModel = viewModel,
                onAddClick = {},
                onReminderClick = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("테스트 할일").assertExists()
        composeTestRule.onNodeWithText("테스트 설명").assertExists()
    }

    /**
     * 검색 기능
     */
    @Test
    fun 검색_버튼_클릭_시_검색바가_표시된다() {
        // Given
        val viewModel = createMockViewModel()

        // When
        composeTestRule.setContent {
            HomeScreen(
                viewModel = viewModel,
                onAddClick = {},
                onReminderClick = {}
            )
        }

        composeTestRule.onNodeWithContentDescription("검색").performClick()

        // Then
        composeTestRule.onNodeWithText("리마인더 검색...").assertExists()
    }

    /**
     * 간편 모드 - FAB 크기
     */
    @Test
    fun 간편_모드에서_큰_FAB이_표시된다() {
        // Given
        val viewModel = createMockViewModel()

        // When
        composeTestRule.setContent {
            HomeScreen(
                viewModel = viewModel,
                onAddClick = {},
                onReminderClick = {},
                simpleMode = true
            )
        }

        // Then
        composeTestRule.onNodeWithContentDescription("리마인더 추가").assertExists()
    }

    /**
     * 간편 모드 - 필터/검색 숨김
     */
    @Test
    fun 간편_모드에서_필터와_검색이_숨겨진다() {
        // Given
        val viewModel = createMockViewModel()

        // When
        composeTestRule.setContent {
            HomeScreen(
                viewModel = viewModel,
                onAddClick = {},
                onReminderClick = {},
                simpleMode = true
            )
        }

        // Then
        composeTestRule.onNodeWithContentDescription("필터").assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription("검색").assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription("Eisenhower Matrix").assertDoesNotExist()
    }

    /**
     * 정렬 드롭다운 - 한글
     */
    @Test
    fun 정렬_옵션이_한글로_표시된다() {
        // Given
        val viewModel = createMockViewModel()

        // When
        composeTestRule.setContent {
            HomeScreen(
                viewModel = viewModel,
                onAddClick = {},
                onReminderClick = {}
            )
        }

        // Then
        // 기본 정렬 옵션 중 하나가 표시되어야 함
        composeTestRule.onNodeWithText("마감일 빠른 순", substring = true).assertExists()
    }

    /**
     * 우선순위 필터 칩 - 한글
     */
    @Test
    fun 우선순위_필터_칩이_한글로_표시된다() {
        // Given
        val viewModel = createMockViewModel()

        // When
        composeTestRule.setContent {
            HomeScreen(
                viewModel = viewModel,
                onAddClick = {},
                onReminderClick = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("전체").assertExists()
        composeTestRule.onNodeWithText("높음").assertExists()
        composeTestRule.onNodeWithText("중간").assertExists()
        composeTestRule.onNodeWithText("낮음").assertExists()
    }

    /**
     * 날짜 필터 칩 - 한글
     */
    @Test
    fun 날짜_필터_칩이_한글로_표시된다() {
        // Given
        val viewModel = createMockViewModel()

        // When
        composeTestRule.setContent {
            HomeScreen(
                viewModel = viewModel,
                onAddClick = {},
                onReminderClick = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("오늘").assertExists()
        composeTestRule.onNodeWithText("이번 주").assertExists()
    }

    /**
     * 검색 결과 없음 메시지
     */
    @Test
    fun 검색_결과가_없을_때_메시지가_표시된다() {
        // Given
        val viewModel = mock(ReminderViewModel::class.java)
        `when`(viewModel.activeReminders).thenReturn(MutableStateFlow(emptyList()))
        `when`(viewModel.searchQuery).thenReturn(MutableStateFlow("존재하지않는검색어"))
        `when`(viewModel.currentFilter).thenReturn(MutableStateFlow(null))

        // When
        composeTestRule.setContent {
            HomeScreen(
                viewModel = viewModel,
                onAddClick = {},
                onReminderClick = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("리마인더를 찾을 수 없습니다").assertExists()
    }
}
