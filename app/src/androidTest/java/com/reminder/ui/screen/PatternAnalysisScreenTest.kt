package com.reminder.ui.screen

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.reminder.analytics.CompletionPatternAnalyzer
import com.reminder.viewmodel.ReminderViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import java.time.DayOfWeek

/**
 * PatternAnalysisScreen UI 테스트
 *
 * v1.26.0 완료 패턴 분석 화면의 UI 동작을 검증합니다.
 */
@RunWith(AndroidJUnit4::class)
class PatternAnalysisScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var mockViewModel: ReminderViewModel
    private var onNavigateBackCalled = false

    @Before
    fun setup() {
        mockViewModel = mock(ReminderViewModel::class.java)
        onNavigateBackCalled = false
    }

    @Test
    fun 화면_제목이_표시된다() {
        // Given
        `when`(mockViewModel.analyzeCompletionPattern()).thenReturn(null)
        `when`(mockViewModel.getPatternSummary()).thenReturn("데이터가 부족합니다")

        // When
        composeTestRule.setContent {
            PatternAnalysisScreen(
                viewModel = mockViewModel,
                onNavigateBack = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("📊 완료 패턴 분석").assertIsDisplayed()
    }

    @Test
    fun 뒤로가기_버튼_클릭_시_onNavigateBack이_호출된다() {
        // Given
        `when`(mockViewModel.analyzeCompletionPattern()).thenReturn(null)
        `when`(mockViewModel.getPatternSummary()).thenReturn("데이터가 부족합니다")

        composeTestRule.setContent {
            PatternAnalysisScreen(
                viewModel = mockViewModel,
                onNavigateBack = { onNavigateBackCalled = true }
            )
        }

        // When
        composeTestRule.onNodeWithContentDescription("뒤로가기").performClick()

        // Then
        assert(onNavigateBackCalled)
    }

    @Test
    fun 데이터_없을_때_로딩_상태가_표시된다() {
        // Given
        `when`(mockViewModel.analyzeCompletionPattern()).thenReturn(null)
        `when`(mockViewModel.getPatternSummary()).thenReturn("")

        // When
        composeTestRule.setContent {
            PatternAnalysisScreen(
                viewModel = mockViewModel,
                onNavigateBack = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("패턴을 분석하는 중...").assertIsDisplayed()
    }

    @Test
    fun 데이터_부족_메시지가_표시된다() {
        // Given
        `when`(mockViewModel.analyzeCompletionPattern()).thenReturn(null)
        `when`(mockViewModel.getPatternSummary()).thenReturn("데이터가 부족합니다")

        // When
        composeTestRule.setContent {
            PatternAnalysisScreen(
                viewModel = mockViewModel,
                onNavigateBack = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("데이터가 부족합니다").assertIsDisplayed()
    }

    @Test
    fun 패턴_데이터가_있을_때_요약_카드가_표시된다() {
        // Given
        val mockPattern = CompletionPatternAnalyzer.CompletionPattern(
            completionRate = 0.75,
            mostProductiveHour = 14,
            mostProductiveDay = DayOfWeek.MONDAY,
            hourlyCompletionRate = mapOf(14 to 0.9, 15 to 0.8, 16 to 0.7, 17 to 0.6, 18 to 0.5),
            dailyCompletionRate = mapOf(
                DayOfWeek.MONDAY to 0.8,
                DayOfWeek.TUESDAY to 0.7,
                DayOfWeek.WEDNESDAY to 0.6,
                DayOfWeek.THURSDAY to 0.5,
                DayOfWeek.FRIDAY to 0.4,
                DayOfWeek.SATURDAY to 0.3,
                DayOfWeek.SUNDAY to 0.2
            ),
            averageCompletionTime = 120.0
        )
        `when`(mockViewModel.analyzeCompletionPattern()).thenReturn(mockPattern)
        `when`(mockViewModel.getPatternSummary()).thenReturn("월요일 14:00에 가장 생산적입니다")

        // When
        composeTestRule.setContent {
            PatternAnalysisScreen(
                viewModel = mockViewModel,
                onNavigateBack = {}
            )
        }

        // Then
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("📝 요약").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("📝 요약").assertIsDisplayed()
        composeTestRule.onNodeWithText("월요일 14:00에 가장 생산적입니다").assertIsDisplayed()
    }

    @Test
    fun 전체_완료율이_표시된다() {
        // Given
        val mockPattern = CompletionPatternAnalyzer.CompletionPattern(
            completionRate = 0.75,
            mostProductiveHour = 14,
            mostProductiveDay = DayOfWeek.MONDAY,
            hourlyCompletionRate = mapOf(14 to 0.9, 15 to 0.8, 16 to 0.7, 17 to 0.6, 18 to 0.5),
            dailyCompletionRate = mapOf(
                DayOfWeek.MONDAY to 0.8,
                DayOfWeek.TUESDAY to 0.7,
                DayOfWeek.WEDNESDAY to 0.6,
                DayOfWeek.THURSDAY to 0.5,
                DayOfWeek.FRIDAY to 0.4,
                DayOfWeek.SATURDAY to 0.3,
                DayOfWeek.SUNDAY to 0.2
            ),
            averageCompletionTime = 120.0
        )
        `when`(mockViewModel.analyzeCompletionPattern()).thenReturn(mockPattern)
        `when`(mockViewModel.getPatternSummary()).thenReturn("테스트 요약")

        // When
        composeTestRule.setContent {
            PatternAnalysisScreen(
                viewModel = mockViewModel,
                onNavigateBack = {}
            )
        }

        // Then
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("✅ 전체 완료율").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("✅ 전체 완료율").assertIsDisplayed()
        composeTestRule.onNodeWithText("75%").assertIsDisplayed()
    }

    @Test
    fun 가장_생산적인_시간대가_표시된다() {
        // Given
        val mockPattern = CompletionPatternAnalyzer.CompletionPattern(
            completionRate = 0.75,
            mostProductiveHour = 14,
            mostProductiveDay = DayOfWeek.MONDAY,
            hourlyCompletionRate = mapOf(14 to 0.9, 15 to 0.8, 16 to 0.7, 17 to 0.6, 18 to 0.5),
            dailyCompletionRate = mapOf(
                DayOfWeek.MONDAY to 0.8,
                DayOfWeek.TUESDAY to 0.7,
                DayOfWeek.WEDNESDAY to 0.6,
                DayOfWeek.THURSDAY to 0.5,
                DayOfWeek.FRIDAY to 0.4,
                DayOfWeek.SATURDAY to 0.3,
                DayOfWeek.SUNDAY to 0.2
            ),
            averageCompletionTime = 120.0
        )
        `when`(mockViewModel.analyzeCompletionPattern()).thenReturn(mockPattern)
        `when`(mockViewModel.getPatternSummary()).thenReturn("테스트 요약")

        // When
        composeTestRule.setContent {
            PatternAnalysisScreen(
                viewModel = mockViewModel,
                onNavigateBack = {}
            )
        }

        // Then
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("⏰ 가장 생산적인 시간대").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("⏰ 가장 생산적인 시간대").assertIsDisplayed()
        composeTestRule.onNodeWithText("14:00").assertIsDisplayed()
    }

    @Test
    fun 시간대별_완료율이_표시된다() {
        // Given
        val mockPattern = CompletionPatternAnalyzer.CompletionPattern(
            completionRate = 0.75,
            mostProductiveHour = 14,
            mostProductiveDay = DayOfWeek.MONDAY,
            hourlyCompletionRate = mapOf(14 to 0.9, 15 to 0.8, 16 to 0.7, 17 to 0.6, 18 to 0.5),
            dailyCompletionRate = mapOf(
                DayOfWeek.MONDAY to 0.8,
                DayOfWeek.TUESDAY to 0.7,
                DayOfWeek.WEDNESDAY to 0.6,
                DayOfWeek.THURSDAY to 0.5,
                DayOfWeek.FRIDAY to 0.4,
                DayOfWeek.SATURDAY to 0.3,
                DayOfWeek.SUNDAY to 0.2
            ),
            averageCompletionTime = 120.0
        )
        `when`(mockViewModel.analyzeCompletionPattern()).thenReturn(mockPattern)
        `when`(mockViewModel.getPatternSummary()).thenReturn("테스트 요약")

        // When
        composeTestRule.setContent {
            PatternAnalysisScreen(
                viewModel = mockViewModel,
                onNavigateBack = {}
            )
        }

        // Then - 상위 5개 시간대가 표시됨
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("14:00").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("15:00").assertIsDisplayed()
        composeTestRule.onNodeWithText("16:00").assertIsDisplayed()
        composeTestRule.onNodeWithText("17:00").assertIsDisplayed()
        composeTestRule.onNodeWithText("18:00").assertIsDisplayed()
    }

    @Test
    fun 가장_생산적인_요일이_표시된다() {
        // Given
        val mockPattern = CompletionPatternAnalyzer.CompletionPattern(
            completionRate = 0.75,
            mostProductiveHour = 14,
            mostProductiveDay = DayOfWeek.MONDAY,
            hourlyCompletionRate = mapOf(14 to 0.9, 15 to 0.8, 16 to 0.7, 17 to 0.6, 18 to 0.5),
            dailyCompletionRate = mapOf(
                DayOfWeek.MONDAY to 0.8,
                DayOfWeek.TUESDAY to 0.7,
                DayOfWeek.WEDNESDAY to 0.6,
                DayOfWeek.THURSDAY to 0.5,
                DayOfWeek.FRIDAY to 0.4,
                DayOfWeek.SATURDAY to 0.3,
                DayOfWeek.SUNDAY to 0.2
            ),
            averageCompletionTime = 120.0
        )
        `when`(mockViewModel.analyzeCompletionPattern()).thenReturn(mockPattern)
        `when`(mockViewModel.getPatternSummary()).thenReturn("테스트 요약")

        // When
        composeTestRule.setContent {
            PatternAnalysisScreen(
                viewModel = mockViewModel,
                onNavigateBack = {}
            )
        }

        // Then
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("📅 가장 생산적인 요일").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("📅 가장 생산적인 요일").assertIsDisplayed()
        composeTestRule.onNodeWithText("월요일").assertIsDisplayed()
    }

    @Test
    fun 요일별_완료율이_표시된다() {
        // Given
        val mockPattern = CompletionPatternAnalyzer.CompletionPattern(
            completionRate = 0.75,
            mostProductiveHour = 14,
            mostProductiveDay = DayOfWeek.MONDAY,
            hourlyCompletionRate = mapOf(14 to 0.9, 15 to 0.8, 16 to 0.7, 17 to 0.6, 18 to 0.5),
            dailyCompletionRate = mapOf(
                DayOfWeek.MONDAY to 0.8,
                DayOfWeek.TUESDAY to 0.7,
                DayOfWeek.WEDNESDAY to 0.6,
                DayOfWeek.THURSDAY to 0.5,
                DayOfWeek.FRIDAY to 0.4,
                DayOfWeek.SATURDAY to 0.3,
                DayOfWeek.SUNDAY to 0.2
            ),
            averageCompletionTime = 120.0
        )
        `when`(mockViewModel.analyzeCompletionPattern()).thenReturn(mockPattern)
        `when`(mockViewModel.getPatternSummary()).thenReturn("테스트 요약")

        // When
        composeTestRule.setContent {
            PatternAnalysisScreen(
                viewModel = mockViewModel,
                onNavigateBack = {}
            )
        }

        // Then - 모든 요일이 표시됨
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("월요일").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("화요일").assertIsDisplayed()
        composeTestRule.onNodeWithText("수요일").assertIsDisplayed()
        composeTestRule.onNodeWithText("목요일").assertIsDisplayed()
        composeTestRule.onNodeWithText("금요일").assertIsDisplayed()
        composeTestRule.onNodeWithText("토요일").assertIsDisplayed()
        composeTestRule.onNodeWithText("일요일").assertIsDisplayed()
    }

    @Test
    fun 평균_완료_시간이_표시된다() {
        // Given
        val mockPattern = CompletionPatternAnalyzer.CompletionPattern(
            completionRate = 0.75,
            mostProductiveHour = 14,
            mostProductiveDay = DayOfWeek.MONDAY,
            hourlyCompletionRate = mapOf(14 to 0.9, 15 to 0.8, 16 to 0.7, 17 to 0.6, 18 to 0.5),
            dailyCompletionRate = mapOf(
                DayOfWeek.MONDAY to 0.8,
                DayOfWeek.TUESDAY to 0.7,
                DayOfWeek.WEDNESDAY to 0.6,
                DayOfWeek.THURSDAY to 0.5,
                DayOfWeek.FRIDAY to 0.4,
                DayOfWeek.SATURDAY to 0.3,
                DayOfWeek.SUNDAY to 0.2
            ),
            averageCompletionTime = 120.0
        )
        `when`(mockViewModel.analyzeCompletionPattern()).thenReturn(mockPattern)
        `when`(mockViewModel.getPatternSummary()).thenReturn("테스트 요약")

        // When
        composeTestRule.setContent {
            PatternAnalysisScreen(
                viewModel = mockViewModel,
                onNavigateBack = {}
            )
        }

        // Then
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("⏱️ 평균 완료 시간").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("⏱️ 평균 완료 시간").assertIsDisplayed()
        composeTestRule.onNodeWithText("2시간 0분").assertIsDisplayed()
    }

    @Test
    fun 평균_완료_시간이_1시간_미만일_때_분만_표시된다() {
        // Given
        val mockPattern = CompletionPatternAnalyzer.CompletionPattern(
            completionRate = 0.75,
            mostProductiveHour = 14,
            mostProductiveDay = DayOfWeek.MONDAY,
            hourlyCompletionRate = mapOf(14 to 0.9, 15 to 0.8, 16 to 0.7, 17 to 0.6, 18 to 0.5),
            dailyCompletionRate = mapOf(
                DayOfWeek.MONDAY to 0.8,
                DayOfWeek.TUESDAY to 0.7,
                DayOfWeek.WEDNESDAY to 0.6,
                DayOfWeek.THURSDAY to 0.5,
                DayOfWeek.FRIDAY to 0.4,
                DayOfWeek.SATURDAY to 0.3,
                DayOfWeek.SUNDAY to 0.2
            ),
            averageCompletionTime = 45.0 // 45분
        )
        `when`(mockViewModel.analyzeCompletionPattern()).thenReturn(mockPattern)
        `when`(mockViewModel.getPatternSummary()).thenReturn("테스트 요약")

        // When
        composeTestRule.setContent {
            PatternAnalysisScreen(
                viewModel = mockViewModel,
                onNavigateBack = {}
            )
        }

        // Then
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("⏱️ 평균 완료 시간").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("45분").assertIsDisplayed()
    }
}
