package com.reminder.ui.screen

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.reminder.data.entity.Statistics
import com.reminder.viewmodel.StatisticsViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*

@RunWith(AndroidJUnit4::class)
class StatisticsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var mockViewModel: StatisticsViewModel
    private lateinit var statisticsFlow: MutableStateFlow<Statistics>

    private var onNavigateBackCalled = false
    private var onCompletionHistoryClickCalled = false
    private var onPatternAnalysisClickCalled = false

    @Before
    fun setup() {
        mockViewModel = mock(StatisticsViewModel::class.java)
        statisticsFlow = MutableStateFlow(Statistics())

        `when`(mockViewModel.statistics).thenReturn(statisticsFlow)

        onNavigateBackCalled = false
        onCompletionHistoryClickCalled = false
        onPatternAnalysisClickCalled = false
    }

    @Test
    fun 통계_화면_제목이_표시된다() {
        // Given
        composeTestRule.setContent {
            StatisticsScreen(
                viewModel = mockViewModel,
                onNavigateBack = {},
                onCompletionHistoryClick = {},
                onPatternAnalysisClick = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("통계").assertIsDisplayed()
    }

    @Test
    fun 뒤로가기_버튼_클릭_시_onNavigateBack_콜백이_호출된다() {
        // Given
        composeTestRule.setContent {
            StatisticsScreen(
                viewModel = mockViewModel,
                onNavigateBack = { onNavigateBackCalled = true },
                onCompletionHistoryClick = {},
                onPatternAnalysisClick = {}
            )
        }

        // When
        composeTestRule.onNodeWithContentDescription("뒤로가기").performClick()

        // Then
        assert(onNavigateBackCalled)
    }

    @Test
    fun 전체_통계_카드가_올바른_데이터를_표시한다() {
        // Given
        val testStatistics = Statistics(
            totalReminders = 10,
            completedReminders = 7,
            pendingReminders = 3,
            completionRate = 0.7f
        )
        statisticsFlow.value = testStatistics

        // When
        composeTestRule.setContent {
            StatisticsScreen(
                viewModel = mockViewModel,
                onNavigateBack = {},
                onCompletionHistoryClick = {},
                onPatternAnalysisClick = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("전체 통계").assertIsDisplayed()
        composeTestRule.onNodeWithText("10").assertIsDisplayed() // 전체
        composeTestRule.onNodeWithText("7").assertIsDisplayed()  // 완료
        composeTestRule.onNodeWithText("3").assertIsDisplayed()  // 미완료
    }

    @Test
    fun 완료율이_올바르게_표시된다() {
        // Given
        val testStatistics = Statistics(
            totalReminders = 10,
            completedReminders = 7,
            pendingReminders = 3,
            completionRate = 0.7f
        )
        statisticsFlow.value = testStatistics

        // When
        composeTestRule.setContent {
            StatisticsScreen(
                viewModel = mockViewModel,
                onNavigateBack = {},
                onCompletionHistoryClick = {},
                onPatternAnalysisClick = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("완료율").assertIsDisplayed()
        composeTestRule.onNodeWithText("70%").assertIsDisplayed()
    }

    @Test
    fun 완료율이_0퍼센트일_때_올바르게_표시된다() {
        // Given
        val testStatistics = Statistics(
            totalReminders = 5,
            completedReminders = 0,
            pendingReminders = 5,
            completionRate = 0f
        )
        statisticsFlow.value = testStatistics

        // When
        composeTestRule.setContent {
            StatisticsScreen(
                viewModel = mockViewModel,
                onNavigateBack = {},
                onCompletionHistoryClick = {},
                onPatternAnalysisClick = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("0%").assertIsDisplayed()
    }

    @Test
    fun 완료율이_100퍼센트일_때_올바르게_표시된다() {
        // Given
        val testStatistics = Statistics(
            totalReminders = 5,
            completedReminders = 5,
            pendingReminders = 0,
            completionRate = 1.0f
        )
        statisticsFlow.value = testStatistics

        // When
        composeTestRule.setContent {
            StatisticsScreen(
                viewModel = mockViewModel,
                onNavigateBack = {},
                onCompletionHistoryClick = {},
                onPatternAnalysisClick = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("100%").assertIsDisplayed()
    }

    @Test
    fun 완료_이력_달력_버튼_클릭_시_콜백이_호출된다() {
        // Given
        composeTestRule.setContent {
            StatisticsScreen(
                viewModel = mockViewModel,
                onNavigateBack = {},
                onCompletionHistoryClick = { onCompletionHistoryClickCalled = true },
                onPatternAnalysisClick = {}
            )
        }

        // When
        composeTestRule.onNodeWithText("📅 완료 이력 달력 보기").performClick()

        // Then
        assert(onCompletionHistoryClickCalled)
    }

    @Test
    fun 완료_패턴_분석_버튼_클릭_시_콜백이_호출된다() {
        // Given
        composeTestRule.setContent {
            StatisticsScreen(
                viewModel = mockViewModel,
                onNavigateBack = {},
                onCompletionHistoryClick = {},
                onPatternAnalysisClick = { onPatternAnalysisClickCalled = true }
            )
        }

        // When
        composeTestRule.onNodeWithText("📊 완료 패턴 분석 보기").performClick()

        // Then
        assert(onPatternAnalysisClickCalled)
    }

    @Test
    fun 우선순위_분포_차트_제목이_표시된다() {
        // Given
        val testStatistics = Statistics(
            totalReminders = 10,
            highPriorityCount = 3,
            mediumPriorityCount = 4,
            lowPriorityCount = 3
        )
        statisticsFlow.value = testStatistics

        // When
        composeTestRule.setContent {
            StatisticsScreen(
                viewModel = mockViewModel,
                onNavigateBack = {},
                onCompletionHistoryClick = {},
                onPatternAnalysisClick = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("우선순위별 분포").assertIsDisplayed()
    }

    @Test
    fun 리마인더가_없을_때_데이터가_없습니다_메시지가_표시된다() {
        // Given
        val emptyStatistics = Statistics(
            totalReminders = 0
        )
        statisticsFlow.value = emptyStatistics

        // When
        composeTestRule.setContent {
            StatisticsScreen(
                viewModel = mockViewModel,
                onNavigateBack = {},
                onCompletionHistoryClick = {},
                onPatternAnalysisClick = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("데이터가 없습니다").assertIsDisplayed()
    }

    @Test
    fun 카테고리가_있을_때_카테고리_분포_차트가_표시된다() {
        // Given
        val testStatistics = Statistics(
            totalReminders = 10,
            categoryDistribution = mapOf(
                "Work" to 5,
                "Personal" to 3,
                "Shopping" to 2
            )
        )
        statisticsFlow.value = testStatistics

        // When
        composeTestRule.setContent {
            StatisticsScreen(
                viewModel = mockViewModel,
                onNavigateBack = {},
                onCompletionHistoryClick = {},
                onPatternAnalysisClick = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("카테고리별 분포").assertIsDisplayed()
    }

    @Test
    fun 카테고리가_없을_때_카테고리_분포_차트가_표시되지_않는다() {
        // Given
        val testStatistics = Statistics(
            totalReminders = 5,
            categoryDistribution = emptyMap()
        )
        statisticsFlow.value = testStatistics

        // When
        composeTestRule.setContent {
            StatisticsScreen(
                viewModel = mockViewModel,
                onNavigateBack = {},
                onCompletionHistoryClick = {},
                onPatternAnalysisClick = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("카테고리별 분포").assertDoesNotExist()
    }

    @Test
    fun 완료율_프로그레스바의_접근성_설명이_설정되어_있다() {
        // Given
        val testStatistics = Statistics(
            totalReminders = 10,
            completedReminders = 7,
            pendingReminders = 3,
            completionRate = 0.7f
        )
        statisticsFlow.value = testStatistics

        // When
        composeTestRule.setContent {
            StatisticsScreen(
                viewModel = mockViewModel,
                onNavigateBack = {},
                onCompletionHistoryClick = {},
                onPatternAnalysisClick = {}
            )
        }

        // Then
        composeTestRule.onNode(
            hasContentDescription("완료율 70 퍼센트")
        ).assertExists()
    }

    @Test
    fun 우선순위_차트의_접근성_설명이_설정되어_있다() {
        // Given
        val testStatistics = Statistics(
            totalReminders = 10,
            highPriorityCount = 3,
            mediumPriorityCount = 4,
            lowPriorityCount = 3
        )
        statisticsFlow.value = testStatistics

        // When
        composeTestRule.setContent {
            StatisticsScreen(
                viewModel = mockViewModel,
                onNavigateBack = {},
                onCompletionHistoryClick = {},
                onPatternAnalysisClick = {}
            )
        }

        // Then
        composeTestRule.onNode(
            hasContentDescription("우선순위별 분포 차트. 높음 3개, 중간 4개, 낮음 3개")
        ).assertExists()
    }

    @Test
    fun 카테고리_차트의_접근성_설명이_설정되어_있다() {
        // Given
        val testStatistics = Statistics(
            totalReminders = 10,
            categoryDistribution = mapOf(
                "Work" to 5,
                "Personal" to 3
            )
        )
        statisticsFlow.value = testStatistics

        // When
        composeTestRule.setContent {
            StatisticsScreen(
                viewModel = mockViewModel,
                onNavigateBack = {},
                onCompletionHistoryClick = {},
                onPatternAnalysisClick = {}
            )
        }

        // Then
        composeTestRule.onNode(
            hasContentDescription(
                "카테고리별 분포 차트. Work 5 개, Personal 3 개",
                substring = true
            )
        ).assertExists()
    }

    @Test
    fun 전체_통계_라벨이_올바르게_표시된다() {
        // Given
        composeTestRule.setContent {
            StatisticsScreen(
                viewModel = mockViewModel,
                onNavigateBack = {},
                onCompletionHistoryClick = {},
                onPatternAnalysisClick = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("전체").assertIsDisplayed()
        composeTestRule.onNodeWithText("완료").assertIsDisplayed()
        composeTestRule.onNodeWithText("미완료").assertIsDisplayed()
    }

    @Test
    fun 우선순위_분포가_높음만_있을_때_올바르게_표시된다() {
        // Given
        val testStatistics = Statistics(
            totalReminders = 5,
            highPriorityCount = 5,
            mediumPriorityCount = 0,
            lowPriorityCount = 0
        )
        statisticsFlow.value = testStatistics

        // When
        composeTestRule.setContent {
            StatisticsScreen(
                viewModel = mockViewModel,
                onNavigateBack = {},
                onCompletionHistoryClick = {},
                onPatternAnalysisClick = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("우선순위별 분포").assertIsDisplayed()
        // 차트가 렌더링되는지 확인
        composeTestRule.onNode(
            hasContentDescription("우선순위별 분포 차트. 높음 5개, 중간 0개, 낮음 0개")
        ).assertExists()
    }

    @Test
    fun 통계_화면이_스크롤_가능하다() {
        // Given
        val testStatistics = Statistics(
            totalReminders = 10,
            completedReminders = 7,
            highPriorityCount = 3,
            mediumPriorityCount = 4,
            lowPriorityCount = 3,
            categoryDistribution = mapOf(
                "Work" to 5,
                "Personal" to 3,
                "Shopping" to 2
            )
        )
        statisticsFlow.value = testStatistics

        // When
        composeTestRule.setContent {
            StatisticsScreen(
                viewModel = mockViewModel,
                onNavigateBack = {},
                onCompletionHistoryClick = {},
                onPatternAnalysisClick = {}
            )
        }

        // Then - 스크롤 가능 확인 (모든 컴포넌트가 존재하는지)
        composeTestRule.onNodeWithText("전체 통계").assertIsDisplayed()
        composeTestRule.onNodeWithText("완료율").assertIsDisplayed()
        composeTestRule.onNodeWithText("우선순위별 분포").assertIsDisplayed()
        composeTestRule.onNodeWithText("카테고리별 분포").assertIsDisplayed()
    }
}
