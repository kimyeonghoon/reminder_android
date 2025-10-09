package com.reminder.analytics

import com.reminder.data.entity.ReminderEntity
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * 완료 패턴 분석 클래스
 *
 * 사용자의 리마인더 완료 패턴을 분석하여
 * 최적의 시간대와 요일을 제안합니다.
 */
class CompletionPatternAnalyzer {

    /**
     * 완료 패턴 분석 결과
     */
    data class CompletionPattern(
        val mostProductiveHour: Int,               // 가장 생산적인 시간 (0-23)
        val mostProductiveDay: DayOfWeek,          // 가장 생산적인 요일
        val averageCompletionTime: Double,         // 평균 완료 시간 (분)
        val completionRate: Double,                // 완료율 (0.0-1.0)
        val hourlyCompletionRate: Map<Int, Double>,  // 시간대별 완료율
        val dailyCompletionRate: Map<DayOfWeek, Double>  // 요일별 완료율
    )

    /**
     * 완료 패턴 분석
     *
     * @param allReminders 모든 리마인더 목록
     * @return 완료 패턴 분석 결과
     */
    fun analyzeCompletionPattern(allReminders: List<ReminderEntity>): CompletionPattern? {
        if (allReminders.isEmpty()) return null

        val completedReminders = allReminders.filter { it.isCompleted }
        if (completedReminders.isEmpty()) return null

        // 시간대별 완료 통계
        val hourlyCompletion = analyzeHourlyCompletion(completedReminders)
        val mostProductiveHour = hourlyCompletion.maxByOrNull { it.value }?.key ?: 9

        // 요일별 완료 통계
        val dailyCompletion = analyzeDailyCompletion(completedReminders)
        val mostProductiveDay = dailyCompletion.maxByOrNull { it.value }?.key ?: DayOfWeek.MONDAY

        // 평균 완료 시간 계산 (생성 시간 ~ 완료 시간)
        val averageCompletionTime = calculateAverageCompletionTime(completedReminders)

        // 전체 완료율
        val completionRate = completedReminders.size.toDouble() / allReminders.size

        return CompletionPattern(
            mostProductiveHour = mostProductiveHour,
            mostProductiveDay = mostProductiveDay,
            averageCompletionTime = averageCompletionTime,
            completionRate = completionRate,
            hourlyCompletionRate = hourlyCompletion,
            dailyCompletionRate = dailyCompletion
        )
    }

    /**
     * 시간대별 완료율 분석
     */
    private fun analyzeHourlyCompletion(completedReminders: List<ReminderEntity>): Map<Int, Double> {
        val hourlyCount = completedReminders
            .groupBy { it.updatedAt.hour }
            .mapValues { it.value.size.toDouble() }

        val totalCompleted = completedReminders.size.toDouble()

        return (0..23).associateWith { hour ->
            hourlyCount[hour]?.div(totalCompleted) ?: 0.0
        }
    }

    /**
     * 요일별 완료율 분석
     */
    private fun analyzeDailyCompletion(completedReminders: List<ReminderEntity>): Map<DayOfWeek, Double> {
        val dailyCount = completedReminders
            .groupBy { it.updatedAt.dayOfWeek }
            .mapValues { it.value.size.toDouble() }

        val totalCompleted = completedReminders.size.toDouble()

        return DayOfWeek.values().associateWith { day ->
            dailyCount[day]?.div(totalCompleted) ?: 0.0
        }
    }

    /**
     * 평균 완료 시간 계산 (분 단위)
     */
    private fun calculateAverageCompletionTime(completedReminders: List<ReminderEntity>): Double {
        if (completedReminders.isEmpty()) return 0.0

        val completionTimes = completedReminders.mapNotNull { reminder ->
            val created = reminder.createdAt
            val completed = reminder.updatedAt

            // 생성 시간과 완료 시간 차이 (분 단위)
            val durationInMinutes = java.time.Duration.between(created, completed).toMinutes()
            if (durationInMinutes >= 0) durationInMinutes.toDouble() else null
        }

        return if (completionTimes.isNotEmpty()) {
            completionTimes.average()
        } else {
            0.0
        }
    }

    /**
     * 최적의 리마인더 시간 제안
     *
     * @param pattern 완료 패턴
     * @param dueDate 원하는 마감일
     * @return 제안된 마감 시간
     */
    fun suggestOptimalTime(pattern: CompletionPattern?, dueDate: java.time.LocalDate): LocalDateTime {
        if (pattern == null) {
            // 패턴이 없으면 기본값 (오전 9시)
            return dueDate.atTime(9, 0)
        }

        // 가장 생산적인 시간대를 기준으로 제안
        return dueDate.atTime(pattern.mostProductiveHour, 0)
    }

    /**
     * 특정 시간대의 완료 확률 계산
     */
    fun getCompletionProbability(pattern: CompletionPattern?, hour: Int): Double {
        if (pattern == null || hour !in 0..23) return 0.0
        return pattern.hourlyCompletionRate[hour] ?: 0.0
    }

    /**
     * 특정 요일의 완료 확률 계산
     */
    fun getCompletionProbability(pattern: CompletionPattern?, day: DayOfWeek): Double {
        if (pattern == null) return 0.0
        return pattern.dailyCompletionRate[day] ?: 0.0
    }

    /**
     * 완료하기 좋은 시간대 목록 (상위 3개)
     */
    fun getBestCompletionHours(pattern: CompletionPattern?): List<Int> {
        if (pattern == null) return listOf(9, 14, 18)  // 기본값

        return pattern.hourlyCompletionRate.entries
            .sortedByDescending { it.value }
            .take(3)
            .map { it.key }
    }

    /**
     * 완료하기 좋은 요일 목록 (상위 3개)
     */
    fun getBestCompletionDays(pattern: CompletionPattern?): List<DayOfWeek> {
        if (pattern == null) {
            return listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
        }

        return pattern.dailyCompletionRate.entries
            .sortedByDescending { it.value }
            .take(3)
            .map { it.key }
    }

    /**
     * 완료 패턴 요약 텍스트 생성
     */
    fun getPatternSummary(pattern: CompletionPattern?): String {
        if (pattern == null) {
            return "충분한 데이터가 없습니다. 더 많은 리마인더를 완료해보세요!"
        }

        val hourText = when (pattern.mostProductiveHour) {
            in 0..5 -> "새벽"
            in 6..11 -> "오전"
            in 12..17 -> "오후"
            else -> "저녁"
        }

        val dayText = when (pattern.mostProductiveDay) {
            DayOfWeek.MONDAY -> "월요일"
            DayOfWeek.TUESDAY -> "화요일"
            DayOfWeek.WEDNESDAY -> "수요일"
            DayOfWeek.THURSDAY -> "목요일"
            DayOfWeek.FRIDAY -> "금요일"
            DayOfWeek.SATURDAY -> "토요일"
            DayOfWeek.SUNDAY -> "일요일"
        }

        val completionRatePercent = (pattern.completionRate * 100).toInt()
        val avgHours = (pattern.averageCompletionTime / 60).toInt()

        return buildString {
            append("당신은 주로 $dayText $hourText ${pattern.mostProductiveHour}시경에 가장 생산적입니다. ")
            append("전체 완료율은 ${completionRatePercent}%이며, ")
            append("평균 ${avgHours}시간 만에 리마인더를 완료합니다.")
        }
    }
}
