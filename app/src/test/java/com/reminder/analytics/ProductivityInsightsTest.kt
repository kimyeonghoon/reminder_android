package com.reminder.analytics

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * v1.33.0: ProductivityInsights 테스트
 *
 * TDD Red Phase - 테스트 먼저 작성
 */
class ProductivityInsightsTest {

    private lateinit var productivityInsights: ProductivityInsights

    @Before
    fun setup() {
        productivityInsights = ProductivityInsights()
    }

    @Test
    fun `주간 완료율 상승 인사이트 생성`() {
        // Given
        val stats = Statistics(
            thisWeekCompletionRate = 0.75, // 75%
            lastWeekCompletionRate = 0.60, // 60%
            mostProductiveHour = 10,
            categoryStats = emptyMap(),
            consecutiveGoalAchievementDays = 0
        )

        // When
        val insights = productivityInsights.generateInsights(stats)

        // Then
        assertTrue(insights.any { it.type == InsightType.WEEKLY_IMPROVEMENT })
        val weeklyInsight = insights.find { it.type == InsightType.WEEKLY_IMPROVEMENT }
        assertTrue(weeklyInsight?.message?.contains("15%") == true)
    }

    @Test
    fun `주간 완료율 하락 인사이트 생성`() {
        // Given
        val stats = Statistics(
            thisWeekCompletionRate = 0.50,
            lastWeekCompletionRate = 0.75,
            mostProductiveHour = 10,
            categoryStats = emptyMap(),
            consecutiveGoalAchievementDays = 0
        )

        // When
        val insights = productivityInsights.generateInsights(stats)

        // Then
        assertTrue(insights.any { it.type == InsightType.WEEKLY_DECLINE })
        val weeklyInsight = insights.find { it.type == InsightType.WEEKLY_DECLINE }
        assertTrue(weeklyInsight?.message?.contains("25%") == true)
    }

    @Test
    fun `가장 생산적인 시간대 인사이트 생성`() {
        // Given
        val stats = Statistics(
            thisWeekCompletionRate = 0.70,
            lastWeekCompletionRate = 0.70,
            mostProductiveHour = 14, // 오후 2시
            categoryStats = emptyMap(),
            consecutiveGoalAchievementDays = 0
        )

        // When
        val insights = productivityInsights.generateInsights(stats)

        // Then
        assertTrue(insights.any { it.type == InsightType.PRODUCTIVE_HOUR })
        val hourInsight = insights.find { it.type == InsightType.PRODUCTIVE_HOUR }
        assertTrue(hourInsight?.message?.contains("14") == true || hourInsight?.message?.contains("오후 2시") == true)
    }

    @Test
    fun `카테고리별 낮은 완료율 인사이트 생성`() {
        // Given
        val stats = Statistics(
            thisWeekCompletionRate = 0.70,
            lastWeekCompletionRate = 0.70,
            mostProductiveHour = 10,
            categoryStats = mapOf(
                "업무" to CategoryStats(completed = 3, total = 10, completionRate = 0.30),
                "개인" to CategoryStats(completed = 8, total = 10, completionRate = 0.80)
            ),
            consecutiveGoalAchievementDays = 0
        )

        // When
        val insights = productivityInsights.generateInsights(stats)

        // Then
        assertTrue(insights.any { it.type == InsightType.LOW_CATEGORY_COMPLETION })
        val categoryInsight = insights.find { it.type == InsightType.LOW_CATEGORY_COMPLETION }
        assertTrue(categoryInsight?.message?.contains("업무") == true)
        assertTrue(categoryInsight?.message?.contains("30%") == true)
    }

    @Test
    fun `연속 목표 달성 인사이트 생성`() {
        // Given
        val stats = Statistics(
            thisWeekCompletionRate = 0.70,
            lastWeekCompletionRate = 0.70,
            mostProductiveHour = 10,
            categoryStats = emptyMap(),
            consecutiveGoalAchievementDays = 5
        )

        // When
        val insights = productivityInsights.generateInsights(stats)

        // Then
        assertTrue(insights.any { it.type == InsightType.STREAK })
        val streakInsight = insights.find { it.type == InsightType.STREAK }
        assertTrue(streakInsight?.message?.contains("5") == true)
    }

    @Test
    fun `인사이트 없음 - 변화가 없는 경우`() {
        // Given
        val stats = Statistics(
            thisWeekCompletionRate = 0.70,
            lastWeekCompletionRate = 0.70, // 변화 없음
            mostProductiveHour = null,
            categoryStats = emptyMap(),
            consecutiveGoalAchievementDays = 0
        )

        // When
        val insights = productivityInsights.generateInsights(stats)

        // Then
        // 변화가 없고 특별한 패턴이 없으므로 인사이트가 적거나 없음
        assertTrue(insights.size <= 1) // 기본 메시지만 있을 수 있음
    }
}
