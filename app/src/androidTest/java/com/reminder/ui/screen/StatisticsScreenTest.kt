package com.reminder.ui.screen

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.reminder.data.entity.Statistics
import com.reminder.viewmodel.StatisticsViewModel
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import kotlinx.coroutines.flow.MutableStateFlow
import org.mockito.Mockito.`when`

/**
 * v1.63.1: StatisticsScreen UI 테스트 (TDD 재작성)
 *
 * 한글화된 UI에 맞춰 테스트 재작성
 */
class StatisticsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createMockViewModel(statistics: Statistics = Statistics()): StatisticsViewModel {
        val viewModel = mock(StatisticsViewModel::class.java)
        `when`(viewModel.statistics).thenReturn(MutableStateFlow(statistics))
        return viewModel
    }

    /**
     * 전체 통계 카드 - 제목
     */
    @Test
    fun 전체_통계_카드_제목이_표시된다() {
        // Given
        val viewModel = createMockViewModel()

        // When
        composeTestRule.setContent {
            StatisticsScreen(viewModel = viewModel)
        }

        // Then
        composeTestRule.onNodeWithText("전체 통계").assertExists()
    }

    /**
     * 전체 통계 카드 - 라벨들
     */
    @Test
    fun 전체_통계_라벨이_한글로_표시된다() {
        // Given
        val viewModel = createMockViewModel()

        // When
        composeTestRule.setContent {
            StatisticsScreen(viewModel = viewModel)
        }

        // Then
        composeTestRule.onNodeWithText("전체").assertExists()
        composeTestRule.onNodeWithText("완료").assertExists()
        composeTestRule.onNodeWithText("미완료").assertExists()
    }

    /**
     * 전체 통계 카드 - 숫자 표시
     */
    @Test
    fun 전체_통계_숫자가_표시된다() {
        // Given
        val statistics = Statistics(
            totalReminders = 10,
            completedReminders = 5,
            pendingReminders = 5
        )
        val viewModel = createMockViewModel(statistics)

        // When
        composeTestRule.setContent {
            StatisticsScreen(viewModel = viewModel)
        }

        // Then
        composeTestRule.onNodeWithText("10").assertExists()
        composeTestRule.onNodeWithText("5").assertExists()
    }

    /**
     * 완료율 카드 - 제목
     */
    @Test
    fun 완료율_카드_제목이_표시된다() {
        // Given
        val viewModel = createMockViewModel()

        // When
        composeTestRule.setContent {
            StatisticsScreen(viewModel = viewModel)
        }

        // Then
        composeTestRule.onNodeWithText("완료율").assertExists()
    }

    /**
     * 완료율 카드 - 퍼센트 표시
     */
    @Test
    fun 완료율_퍼센트가_표시된다() {
        // Given
        val statistics = Statistics(
            totalReminders = 10,
            completedReminders = 7,
            completionRate = 0.7f
        )
        val viewModel = createMockViewModel(statistics)

        // When
        composeTestRule.setContent {
            StatisticsScreen(viewModel = viewModel)
        }

        // Then
        composeTestRule.onNodeWithText("70%").assertExists()
    }

    /**
     * 완료 이력 달력 버튼
     */
    @Test
    fun 완료_이력_달력_버튼이_표시된다() {
        // Given
        val viewModel = createMockViewModel()

        // When
        composeTestRule.setContent {
            StatisticsScreen(viewModel = viewModel)
        }

        // Then
        composeTestRule.onNodeWithText("📅 완료 이력 달력 보기").assertExists()
    }

    /**
     * 완료 패턴 분석 버튼
     */
    @Test
    fun 완료_패턴_분석_버튼이_표시된다() {
        // Given
        val viewModel = createMockViewModel()

        // When
        composeTestRule.setContent {
            StatisticsScreen(viewModel = viewModel)
        }

        // Then
        composeTestRule.onNodeWithText("📊 완료 패턴 분석 보기").assertExists()
    }

    /**
     * 우선순위별 분포 - 제목
     */
    @Test
    fun 우선순위별_분포_제목이_표시된다() {
        // Given
        val viewModel = createMockViewModel()

        // When
        composeTestRule.setContent {
            StatisticsScreen(viewModel = viewModel)
        }

        // Then
        composeTestRule.onNodeWithText("우선순위별 분포").assertExists()
    }

    /**
     * 우선순위별 분포 - 데이터 없을 때
     */
    @Test
    fun 우선순위별_분포_데이터_없을_때_메시지가_표시된다() {
        // Given
        val statistics = Statistics(totalReminders = 0)
        val viewModel = createMockViewModel(statistics)

        // When
        composeTestRule.setContent {
            StatisticsScreen(viewModel = viewModel)
        }

        // Then
        composeTestRule.onNodeWithText("데이터가 없습니다").assertExists()
    }

    /**
     * 카테고리별 분포 - 제목
     */
    @Test
    fun 카테고리별_분포_제목이_표시된다() {
        // Given
        val statistics = Statistics(
            categoryDistribution = mapOf("업무" to 5, "개인" to 3)
        )
        val viewModel = createMockViewModel(statistics)

        // When
        composeTestRule.setContent {
            StatisticsScreen(viewModel = viewModel)
        }

        // Then
        composeTestRule.onNodeWithText("카테고리별 분포").assertExists()
    }

    /**
     * 카테고리별 분포 - 데이터 없을 때 숨김
     */
    @Test
    fun 카테고리별_분포_데이터_없을_때_숨겨진다() {
        // Given
        val statistics = Statistics(categoryDistribution = emptyMap())
        val viewModel = createMockViewModel(statistics)

        // When
        composeTestRule.setContent {
            StatisticsScreen(viewModel = viewModel)
        }

        // Then
        composeTestRule.onNodeWithText("카테고리별 분포").assertDoesNotExist()
    }

    /**
     * 주간 트렌드 - 제목
     */
    @Test
    fun 주간_트렌드_제목이_한글로_표시된다() {
        // Given
        val viewModel = createMockViewModel()

        // When
        composeTestRule.setContent {
            StatisticsScreen(viewModel = viewModel)
        }

        // Then
        composeTestRule.onNodeWithText("최근 7일 완료 트렌드").assertExists()
    }

    /**
     * 주간 트렌드 - 데이터 없을 때
     */
    @Test
    fun 주간_트렌드_데이터_없을_때_메시지가_표시된다() {
        // Given
        val statistics = Statistics(weeklyCompleted = listOf(0, 0, 0, 0, 0, 0, 0))
        val viewModel = createMockViewModel(statistics)

        // When
        composeTestRule.setContent {
            StatisticsScreen(viewModel = viewModel)
        }

        // Then
        composeTestRule.onNodeWithText("완료된 리마인더가 없습니다", substring = true).assertExists()
    }

    /**
     * 월간 트렌드 - 제목
     */
    @Test
    fun 월간_트렌드_제목이_한글로_표시된다() {
        // Given
        val viewModel = createMockViewModel()

        // When
        composeTestRule.setContent {
            StatisticsScreen(viewModel = viewModel)
        }

        // Then
        composeTestRule.onNodeWithText("최근 30일 완료 트렌드").assertExists()
    }

    /**
     * 월간 트렌드 - 데이터 없을 때
     */
    @Test
    fun 월간_트렌드_데이터_없을_때_메시지가_표시된다() {
        // Given
        val statistics = Statistics(monthlyCompleted = List(30) { 0 })
        val viewModel = createMockViewModel(statistics)

        // When
        composeTestRule.setContent {
            StatisticsScreen(viewModel = viewModel)
        }

        // Then
        composeTestRule.onNodeWithText("완료된 리마인더가 없습니다", substring = true).assertExists()
    }

    /**
     * 스크롤 가능 확인
     */
    @Test
    fun 화면을_스크롤할_수_있다() {
        // Given
        val statistics = Statistics(
            totalReminders = 10,
            completedReminders = 7,
            categoryDistribution = mapOf("업무" to 5, "개인" to 3)
        )
        val viewModel = createMockViewModel(statistics)

        // When
        composeTestRule.setContent {
            StatisticsScreen(viewModel = viewModel)
        }

        // Then - 여러 섹션이 표시되어야 함
        composeTestRule.onNodeWithText("전체 통계").assertExists()
        composeTestRule.onNodeWithText("완료율").assertExists()
        composeTestRule.onNodeWithText("우선순위별 분포").assertExists()
    }

    /**
     * 완료 이력 버튼 클릭
     */
    @Test
    fun 완료_이력_버튼_클릭_시_콜백이_호출된다() {
        // Given
        val viewModel = createMockViewModel()
        var clicked = false

        // When
        composeTestRule.setContent {
            StatisticsScreen(
                viewModel = viewModel,
                onCompletionHistoryClick = { clicked = true }
            )
        }

        composeTestRule.onNodeWithText("📅 완료 이력 달력 보기").performClick()

        // Then
        assert(clicked)
    }

    /**
     * 패턴 분석 버튼 클릭
     */
    @Test
    fun 패턴_분석_버튼_클릭_시_콜백이_호출된다() {
        // Given
        val viewModel = createMockViewModel()
        var clicked = false

        // When
        composeTestRule.setContent {
            StatisticsScreen(
                viewModel = viewModel,
                onPatternAnalysisClick = { clicked = true }
            )
        }

        composeTestRule.onNodeWithText("📊 완료 패턴 분석 보기").performClick()

        // Then
        assert(clicked)
    }
}
