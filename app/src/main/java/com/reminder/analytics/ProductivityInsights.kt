package com.reminder.analytics

/**
 * v1.33.0: 생산성 인사이트 생성기
 *
 * 통계 데이터를 기반으로 유용한 인사이트 메시지를 생성
 */

/**
 * 통계 데이터
 */
data class Statistics(
    /**
     * 이번 주 완료율 (0.0 ~ 1.0)
     */
    val thisWeekCompletionRate: Double,

    /**
     * 지난 주 완료율 (0.0 ~ 1.0)
     */
    val lastWeekCompletionRate: Double,

    /**
     * 가장 생산적인 시간대 (0 ~ 23)
     */
    val mostProductiveHour: Int?,

    /**
     * 카테고리별 통계
     */
    val categoryStats: Map<String, CategoryStats>,

    /**
     * 연속 목표 달성 일수
     */
    val consecutiveGoalAchievementDays: Int
)

/**
 * 카테고리별 통계
 */
data class CategoryStats(
    /**
     * 완료된 개수
     */
    val completed: Int,

    /**
     * 전체 개수
     */
    val total: Int,

    /**
     * 완료율 (0.0 ~ 1.0)
     */
    val completionRate: Double
)

/**
 * 인사이트
 */
data class Insight(
    /**
     * 인사이트 타입
     */
    val type: InsightType,

    /**
     * 인사이트 메시지
     */
    val message: String,

    /**
     * 우선순위 (높을수록 중요)
     */
    val priority: Int = 0
)

/**
 * 인사이트 타입
 */
enum class InsightType {
    WEEKLY_IMPROVEMENT,     // 주간 향상
    WEEKLY_DECLINE,         // 주간 하락
    PRODUCTIVE_HOUR,        // 생산적인 시간대
    LOW_CATEGORY_COMPLETION, // 낮은 카테고리 완료율
    STREAK,                 // 연속 달성
    GENERAL                 // 일반
}

/**
 * 생산성 인사이트 생성기
 */
class ProductivityInsights {

    /**
     * 통계 데이터를 기반으로 인사이트 생성
     *
     * @param stats 통계 데이터
     * @return 인사이트 목록 (우선순위순으로 정렬)
     */
    fun generateInsights(stats: Statistics): List<Insight> {
        val insights = mutableListOf<Insight>()

        // 1. 주간 완료율 변화 인사이트
        val weeklyChange = stats.thisWeekCompletionRate - stats.lastWeekCompletionRate
        if (weeklyChange > 0.05) { // 5% 이상 상승
            val percentChange = (weeklyChange * 100).toInt()
            insights.add(
                Insight(
                    type = InsightType.WEEKLY_IMPROVEMENT,
                    message = "이번 주 완료율이 지난주보다 ${percentChange}% 상승했어요! 🎉",
                    priority = 10
                )
            )
        } else if (weeklyChange < -0.05) { // 5% 이상 하락
            val percentChange = (Math.abs(weeklyChange) * 100).toInt()
            insights.add(
                Insight(
                    type = InsightType.WEEKLY_DECLINE,
                    message = "이번 주 완료율이 지난주보다 ${percentChange}% 하락했어요. 다시 힘내봐요! 💪",
                    priority = 8
                )
            )
        }

        // 2. 가장 생산적인 시간대 인사이트
        stats.mostProductiveHour?.let { hour ->
            val timeDescription = when (hour) {
                in 0..5 -> "새벽 ${hour}시"
                in 6..11 -> "오전 ${hour}시"
                12 -> "정오"
                in 13..17 -> "오후 ${hour - 12}시"
                in 18..21 -> "저녁 ${hour - 12}시"
                else -> "밤 ${hour - 12}시"
            }
            insights.add(
                Insight(
                    type = InsightType.PRODUCTIVE_HOUR,
                    message = "가장 생산적인 시간대는 $timeDescription 입니다 ⏰",
                    priority = 6
                )
            )
        }

        // 3. 카테고리별 낮은 완료율 인사이트
        val lowCompletionCategories = stats.categoryStats.filter { (_, categoryStats) ->
            categoryStats.total >= 3 && categoryStats.completionRate < 0.5 // 완료율 50% 미만
        }
        if (lowCompletionCategories.isNotEmpty()) {
            val (category, categoryStats) = lowCompletionCategories.minByOrNull { it.value.completionRate }!!
            val completionPercent = (categoryStats.completionRate * 100).toInt()
            insights.add(
                Insight(
                    type = InsightType.LOW_CATEGORY_COMPLETION,
                    message = "'${category}' 카테고리 완료율이 낮습니다 (${completionPercent}%) 📝",
                    priority = 7
                )
            )
        }

        // 4. 연속 목표 달성 인사이트
        if (stats.consecutiveGoalAchievementDays >= 3) {
            insights.add(
                Insight(
                    type = InsightType.STREAK,
                    message = "${stats.consecutiveGoalAchievementDays}일 연속 목표 달성! 대단해요! 🔥",
                    priority = 9
                )
            )
        }

        // 우선순위순으로 정렬
        return insights.sortedByDescending { it.priority }
    }
}
