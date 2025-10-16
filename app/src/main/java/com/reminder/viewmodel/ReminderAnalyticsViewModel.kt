package com.reminder.viewmodel

import androidx.lifecycle.ViewModel
import com.reminder.analytics.AnalyticsHelper
import com.reminder.analytics.CompletionPatternAnalyzer
import com.reminder.data.entity.ReminderEntity
import com.reminder.data.repository.ReminderRepository
import com.reminder.ml.CategorySuggestionHelper
import java.time.LocalDateTime

/**
 * v1.68.3: Analytics 전용 ViewModel
 *
 * ReminderViewModel에서 분리됨 (God Class 리팩토링)
 * ML 카테고리 제안, 완료 패턴 분석, 완료 이력 조회 담당
 */
class ReminderAnalyticsViewModel(
    private val repository: ReminderRepository,
    private val analyticsHelper: AnalyticsHelper,
    private val categorySuggestionHelper: CategorySuggestionHelper,
    private val completionPatternAnalyzer: CompletionPatternAnalyzer
) : ViewModel() {

    // ==================== 완료 이력 관련 함수 ====================

    /**
     * 특정 날짜에 완료된 리마인더 조회
     */
    suspend fun getCompletedRemindersByDate(date: LocalDateTime): List<ReminderEntity> {
        return repository.getCompletedRemindersByDate(date)
    }

    /**
     * 날짜 범위 내 완료된 리마인더 조회
     */
    suspend fun getCompletedRemindersInRange(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): List<ReminderEntity> {
        return repository.getCompletedRemindersInRange(startDate, endDate)
    }

    /**
     * 월별 완료 개수 맵 생성 (날짜 -> 완료 개수)
     */
    suspend fun getCompletionCountByDay(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Map<LocalDateTime, Int> {
        val reminders = getCompletedRemindersInRange(startDate, endDate)
        return reminders
            .groupBy { it.updatedAt.toLocalDate().atStartOfDay() }
            .mapValues { it.value.size }
    }

    // ==================== 카테고리 제안 (ML) 관련 함수 ====================

    /**
     * 제목과 설명을 기반으로 카테고리 제안
     */
    suspend fun suggestCategories(title: String, description: String = ""): List<String> {
        val allReminders = repository.getAllRemindersList()
        val suggestions = categorySuggestionHelper.suggestCategories(title, description, allReminders)

        // Analytics 이벤트 로깅
        if (suggestions.isNotEmpty()) {
            analyticsHelper.logCategorySuggested(suggestions.size)
        }

        return suggestions
    }

    /**
     * 모든 고유 카테고리 목록 조회
     */
    suspend fun getAllCategories(): List<String> {
        val allReminders = repository.getAllRemindersList()
        return categorySuggestionHelper.getAllCategories(allReminders)
    }

    /**
     * 카테고리 사용 빈도 조회
     */
    suspend fun getCategoryFrequency(): Map<String, Int> {
        val allReminders = repository.getAllRemindersList()
        return categorySuggestionHelper.getCategoryFrequency(allReminders)
    }

    /**
     * 기본 카테고리 목록 반환
     */
    fun getDefaultCategories(): List<String> {
        return CategorySuggestionHelper.DEFAULT_CATEGORIES
    }

    // ==================== 완료 패턴 분석 관련 함수 ====================

    /**
     * 완료 패턴 분석
     */
    suspend fun analyzeCompletionPattern(): CompletionPatternAnalyzer.CompletionPattern? {
        val allReminders = repository.getAllRemindersList()
        val pattern = completionPatternAnalyzer.analyzeCompletionPattern(allReminders)

        // Analytics 이벤트 로깅
        if (pattern != null) {
            analyticsHelper.logPatternAnalyzed(pattern.completionRate)
        }

        return pattern
    }

    /**
     * 최적의 리마인더 시간 제안
     */
    suspend fun suggestOptimalTime(dueDate: java.time.LocalDate): LocalDateTime {
        val pattern = analyzeCompletionPattern()
        return completionPatternAnalyzer.suggestOptimalTime(pattern, dueDate)
    }

    /**
     * 완료하기 좋은 시간대 목록
     */
    suspend fun getBestCompletionHours(): List<Int> {
        val pattern = analyzeCompletionPattern()
        return completionPatternAnalyzer.getBestCompletionHours(pattern)
    }

    /**
     * 완료하기 좋은 요일 목록
     */
    suspend fun getBestCompletionDays(): List<java.time.DayOfWeek> {
        val pattern = analyzeCompletionPattern()
        return completionPatternAnalyzer.getBestCompletionDays(pattern)
    }

    /**
     * 완료 패턴 요약 텍스트
     */
    suspend fun getPatternSummary(): String {
        val pattern = analyzeCompletionPattern()
        return completionPatternAnalyzer.getPatternSummary(pattern)
    }

    /**
     * 특정 시간대의 완료 확률
     */
    suspend fun getCompletionProbabilityByHour(hour: Int): Double {
        val pattern = analyzeCompletionPattern()
        return completionPatternAnalyzer.getCompletionProbability(pattern, hour)
    }

    /**
     * 특정 요일의 완료 확률
     */
    suspend fun getCompletionProbabilityByDay(day: java.time.DayOfWeek): Double {
        val pattern = analyzeCompletionPattern()
        return completionPatternAnalyzer.getCompletionProbability(pattern, day)
    }
}
