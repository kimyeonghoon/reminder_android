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
     * 홈 화면 제목 표시 확인
     */
    @Test
    fun homeScreenTitleIsDisplayed() {
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
     * 리마인더가 없을 때 빈 상태 메시지 표시 확인
     */
    @Test
    fun emptyStateMessageIsDisplayedWhenNoReminders() {
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
     * 리마인더 추가 버튼 표시 확인
     */
    @Test
    fun addReminderButtonIsDisplayed() {
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
     * 필터 버튼 표시 확인
     */
    @Test
    fun filterButtonIsDisplayed() {
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
     * 검색 버튼 표시 확인
     */
    @Test
    fun searchButtonIsDisplayed() {
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
     * Eisenhower Matrix 버튼 표시 확인
     */
    @Test
    fun eisenhowerMatrixButtonIsDisplayed() {
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
     * 설정 버튼 표시 확인
     */
    @Test
    fun settingsButtonIsDisplayed() {
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
     * 리마인더가 있을 때 목록 표시 확인
     */
    @Test
    fun reminderListIsDisplayedWhenRemindersExist() {
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
     * 검색 버튼 클릭 시 검색바 표시 확인
     */
    @Test
    fun searchBarIsDisplayedWhenSearchButtonClicked() {
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
     * 간편 모드에서 큰 FAB 표시 확인
     */
    @Test
    fun largeFabIsDisplayedInSimpleMode() {
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
     * 간편 모드에서 필터와 검색 숨김 확인
     */
    @Test
    fun filterAndSearchAreHiddenInSimpleMode() {
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
     * 정렬 옵션 한글 표시 확인
     */
    @Test
    fun sortOptionsAreDisplayedInKorean() {
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
     * 우선순위 필터 칩 한글 표시 확인
     */
    @Test
    fun priorityFilterChipsAreDisplayedInKorean() {
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
     * 날짜 필터 칩 한글 표시 확인
     */
    @Test
    fun dateFilterChipsAreDisplayedInKorean() {
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
     * 검색 결과가 없을 때 메시지 표시 확인
     */
    @Test
    fun noSearchResultsMessageIsDisplayed() {
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
