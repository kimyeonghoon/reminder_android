package com.reminder.viewmodel

import com.reminder.analytics.AnalyticsHelper
import com.reminder.analytics.CompletionPatternAnalyzer
import com.reminder.data.entity.ReminderEntity
import com.reminder.data.repository.ReminderRepository
import com.reminder.ml.CategorySuggestionHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * ReminderAnalyticsViewModel 테스트
 *
 * AAA 패턴 (Given-When-Then)으로 Analytics 메서드 검증
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ReminderAnalyticsViewModelTest {

    private lateinit var repository: ReminderRepository
    private lateinit var analyticsHelper: AnalyticsHelper
    private lateinit var categorySuggestionHelper: CategorySuggestionHelper
    private lateinit var completionPatternAnalyzer: CompletionPatternAnalyzer
    private lateinit var viewModel: ReminderAnalyticsViewModel

    @Before
    fun setup() {
        repository = mock(ReminderRepository::class.java)
        analyticsHelper = mock(AnalyticsHelper::class.java)
        categorySuggestionHelper = mock(CategorySuggestionHelper::class.java)
        completionPatternAnalyzer = mock(CompletionPatternAnalyzer::class.java)

        viewModel = ReminderAnalyticsViewModel(
            repository,
            analyticsHelper,
            categorySuggestionHelper,
            completionPatternAnalyzer
        )
    }

    // ==================== 완료 이력 테스트 ====================

    /** getCompletedRemindersByDate는 특정 날짜에 완료된 리마인더를 반환한다 */
    @Test
    fun getCompletedRemindersByDateReturnsCompletedReminders() = runTest {
        // Given
        val date = LocalDateTime.of(2025, 1, 1, 0, 0)
        val reminders = listOf(
            createReminder(id = 1, title = "완료1", isCompleted = true),
            createReminder(id = 2, title = "완료2", isCompleted = true)
        )
        whenever(repository.getCompletedRemindersByDate(date)).thenReturn(reminders)

        // When
        val result = viewModel.getCompletedRemindersByDate(date)

        // Then
        assertEquals(2, result.size)
        verify(repository).getCompletedRemindersByDate(date)
    }

    /** getCompletedRemindersInRange는 날짜 범위 내 완료된 리마인더를 반환한다 */
    @Test
    fun getCompletedRemindersInRangeReturnsRemindersInRange() = runTest {
        // Given
        val startDate = LocalDateTime.of(2025, 1, 1, 0, 0)
        val endDate = LocalDateTime.of(2025, 1, 31, 23, 59)
        val reminders = listOf(
            createReminder(id = 1, isCompleted = true),
            createReminder(id = 2, isCompleted = true),
            createReminder(id = 3, isCompleted = true)
        )
        whenever(repository.getCompletedRemindersInRange(startDate, endDate)).thenReturn(reminders)

        // When
        val result = viewModel.getCompletedRemindersInRange(startDate, endDate)

        // Then
        assertEquals(3, result.size)
        verify(repository).getCompletedRemindersInRange(startDate, endDate)
    }

    /** getCompletionCountByDay는 날짜별 완료 개수 맵을 생성한다 */
    @Test
    fun getCompletionCountByDayCreatesCompletionCountMap() = runTest {
        // Given
        val startDate = LocalDateTime.of(2025, 1, 1, 0, 0)
        val endDate = LocalDateTime.of(2025, 1, 3, 23, 59)
        val date1 = LocalDateTime.of(2025, 1, 1, 10, 0)
        val date2 = LocalDateTime.of(2025, 1, 1, 15, 0)
        val date3 = LocalDateTime.of(2025, 1, 2, 10, 0)

        val reminders = listOf(
            createReminder(id = 1, isCompleted = true, updatedAt = date1),
            createReminder(id = 2, isCompleted = true, updatedAt = date2),
            createReminder(id = 3, isCompleted = true, updatedAt = date3)
        )
        whenever(repository.getCompletedRemindersInRange(startDate, endDate)).thenReturn(reminders)

        // When
        val result = viewModel.getCompletionCountByDay(startDate, endDate)

        // Then
        assertEquals(2, result.size) // 2개 날짜
        assertEquals(2, result[date1.toLocalDate().atStartOfDay()]) // 1월 1일: 2개
        assertEquals(1, result[date3.toLocalDate().atStartOfDay()]) // 1월 2일: 1개
    }

    // ==================== 카테고리 제안 테스트 ====================

    /** suggestCategories는 카테고리를 제안한다 */
    @Test
    fun suggestCategoriesSuggestsCategories() = runTest {
        // Given
        val title = "운동하기"
        val description = "매일 운동"
        val allReminders = listOf(createReminder(id = 1))
        val suggestions = listOf("건강", "운동", "습관")
        whenever(repository.getAllRemindersList()).thenReturn(allReminders)
        whenever(categorySuggestionHelper.suggestCategories(title, description, allReminders))
            .thenReturn(suggestions)

        // When
        val result = viewModel.suggestCategories(title, description)

        // Then
        assertEquals(3, result.size)
        assertEquals("건강", result[0])
        verify(analyticsHelper).logCategorySuggested(3)
    }

    /** suggestCategories는 제안이 없으면 analytics를 로깅하지 않는다 */
    @Test
    fun suggestCategoriesDoesNotLogAnalyticsWhenNoSuggestions() = runTest {
        // Given
        val title = "테스트"
        val allReminders = listOf(createReminder(id = 1))
        whenever(repository.getAllRemindersList()).thenReturn(allReminders)
        whenever(categorySuggestionHelper.suggestCategories(any(), any(), any())).thenReturn(emptyList())

        // When
        val result = viewModel.suggestCategories(title)

        // Then
        assertTrue(result.isEmpty())
        verify(analyticsHelper, never()).logCategorySuggested(any())
    }

    /** getAllCategories는 모든 고유 카테고리를 반환한다 */
    @Test
    fun getAllCategoriesReturnsAllUniqueCategories() = runTest {
        // Given
        val allReminders = listOf(createReminder(id = 1))
        val categories = listOf("업무", "개인", "건강")
        whenever(repository.getAllRemindersList()).thenReturn(allReminders)
        whenever(categorySuggestionHelper.getAllCategories(allReminders)).thenReturn(categories)

        // When
        val result = viewModel.getAllCategories()

        // Then
        assertEquals(3, result.size)
        assertEquals("업무", result[0])
    }

    /** getCategoryFrequency는 카테고리 사용 빈도를 반환한다 */
    @Test
    fun getCategoryFrequencyReturnsCategoryFrequency() = runTest {
        // Given
        val allReminders = listOf(createReminder(id = 1))
        val frequency = mapOf("업무" to 5, "개인" to 3, "건강" to 2)
        whenever(repository.getAllRemindersList()).thenReturn(allReminders)
        whenever(categorySuggestionHelper.getCategoryFrequency(allReminders)).thenReturn(frequency)

        // When
        val result = viewModel.getCategoryFrequency()

        // Then
        assertEquals(3, result.size)
        assertEquals(5, result["업무"])
        assertEquals(3, result["개인"])
    }

    /** getDefaultCategories는 기본 카테고리 목록을 반환한다 */
    @Test
    fun getDefaultCategoriesReturnsDefaultCategories() {
        // When
        val result = viewModel.getDefaultCategories()

        // Then
        assertNotNull(result)
        assertTrue(result.isNotEmpty())
    }

    // ==================== 완료 패턴 분석 테스트 ====================

    /** analyzeCompletionPattern는 완료 패턴을 분석한다 */
    @Test
    fun analyzeCompletionPatternAnalyzesPattern() = runTest {
        // Given
        val allReminders = listOf(createReminder(id = 1, isCompleted = true))
        val pattern = CompletionPatternAnalyzer.CompletionPattern(
            mostProductiveHour = 10,
            mostProductiveDay = DayOfWeek.MONDAY,
            averageCompletionTime = 3.0,
            completionRate = 0.75,
            hourlyCompletionRate = mapOf(9 to 0.8, 10 to 0.9),
            dailyCompletionRate = mapOf(DayOfWeek.MONDAY to 0.85)
        )
        whenever(repository.getAllRemindersList()).thenReturn(allReminders)
        whenever(completionPatternAnalyzer.analyzeCompletionPattern(allReminders)).thenReturn(pattern)

        // When
        val result = viewModel.analyzeCompletionPattern()

        // Then
        assertNotNull(result)
        assertEquals(0.75, result!!.completionRate, 0.01)
        verify(analyticsHelper).logPatternAnalyzed(0.75)
    }

    /** analyzeCompletionPattern는 패턴이 null일 때 analytics를 로깅하지 않는다 */
    @Test
    fun analyzeCompletionPatternDoesNotLogAnalyticsWhenPatternIsNull() = runTest {
        // Given
        val allReminders = listOf(createReminder(id = 1))
        whenever(repository.getAllRemindersList()).thenReturn(allReminders)
        whenever(completionPatternAnalyzer.analyzeCompletionPattern(any())).thenReturn(null)

        // When
        val result = viewModel.analyzeCompletionPattern()

        // Then
        assertNull(result)
        verify(analyticsHelper, never()).logPatternAnalyzed(any())
    }

    /** suggestOptimalTime는 최적의 리마인더 시간을 제안한다 */
    @Test
    fun suggestOptimalTimeSuggestsOptimalTime() = runTest {
        // Given
        val dueDate = LocalDate.of(2025, 1, 15)
        val pattern = CompletionPatternAnalyzer.CompletionPattern(
            mostProductiveHour = 10,
            mostProductiveDay = DayOfWeek.MONDAY,
            averageCompletionTime = 3.0,
            completionRate = 0.75,
            hourlyCompletionRate = mapOf(9 to 0.8, 10 to 0.9),
            dailyCompletionRate = mapOf(DayOfWeek.MONDAY to 0.85)
        )
        val optimalTime = LocalDateTime.of(2025, 1, 15, 9, 0)
        val allReminders = listOf(createReminder(id = 1))
        whenever(repository.getAllRemindersList()).thenReturn(allReminders)
        whenever(completionPatternAnalyzer.analyzeCompletionPattern(any())).thenReturn(pattern)
        whenever(completionPatternAnalyzer.suggestOptimalTime(pattern, dueDate)).thenReturn(optimalTime)

        // When
        val result = viewModel.suggestOptimalTime(dueDate)

        // Then
        assertEquals(optimalTime, result)
    }

    /** getBestCompletionHours는 완료하기 좋은 시간대 목록을 반환한다 */
    @Test
    fun getBestCompletionHoursReturnsBestHours() = runTest {
        // Given
        val pattern = CompletionPatternAnalyzer.CompletionPattern(
            mostProductiveHour = 10,
            mostProductiveDay = DayOfWeek.MONDAY,
            averageCompletionTime = 3.0,
            completionRate = 0.75,
            hourlyCompletionRate = mapOf(9 to 0.8, 10 to 0.9, 14 to 0.85, 15 to 0.82),
            dailyCompletionRate = mapOf(DayOfWeek.MONDAY to 0.85)
        )
        val bestHours = listOf(9, 10, 14, 15)
        val allReminders = listOf(createReminder(id = 1))
        whenever(repository.getAllRemindersList()).thenReturn(allReminders)
        whenever(completionPatternAnalyzer.analyzeCompletionPattern(any())).thenReturn(pattern)
        whenever(completionPatternAnalyzer.getBestCompletionHours(pattern)).thenReturn(bestHours)

        // When
        val result = viewModel.getBestCompletionHours()

        // Then
        assertEquals(4, result.size)
        assertTrue(result.contains(9))
        assertTrue(result.contains(10))
    }

    /** getBestCompletionDays는 완료하기 좋은 요일 목록을 반환한다 */
    @Test
    fun getBestCompletionDaysReturnsBestDays() = runTest {
        // Given
        val pattern = CompletionPatternAnalyzer.CompletionPattern(
            mostProductiveHour = 10,
            mostProductiveDay = DayOfWeek.MONDAY,
            averageCompletionTime = 3.0,
            completionRate = 0.75,
            hourlyCompletionRate = mapOf(9 to 0.8, 10 to 0.9),
            dailyCompletionRate = mapOf(
                DayOfWeek.MONDAY to 0.90,
                DayOfWeek.WEDNESDAY to 0.85,
                DayOfWeek.FRIDAY to 0.80
            )
        )
        val bestDays = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
        val allReminders = listOf(createReminder(id = 1))
        whenever(repository.getAllRemindersList()).thenReturn(allReminders)
        whenever(completionPatternAnalyzer.analyzeCompletionPattern(any())).thenReturn(pattern)
        whenever(completionPatternAnalyzer.getBestCompletionDays(pattern)).thenReturn(bestDays)

        // When
        val result = viewModel.getBestCompletionDays()

        // Then
        assertEquals(3, result.size)
        assertTrue(result.contains(DayOfWeek.MONDAY))
        assertTrue(result.contains(DayOfWeek.WEDNESDAY))
    }

    /** getPatternSummary는 완료 패턴 요약 텍스트를 반환한다 */
    @Test
    fun getPatternSummaryReturnsPatternSummary() = runTest {
        // Given
        val pattern = CompletionPatternAnalyzer.CompletionPattern(
            mostProductiveHour = 10,
            mostProductiveDay = DayOfWeek.MONDAY,
            averageCompletionTime = 3.0,
            completionRate = 0.75,
            hourlyCompletionRate = mapOf(9 to 0.8, 10 to 0.9),
            dailyCompletionRate = mapOf(DayOfWeek.MONDAY to 0.85)
        )
        val summary = "완료율 75%, 평균 완료 시간 3.0시간"
        val allReminders = listOf(createReminder(id = 1))
        whenever(repository.getAllRemindersList()).thenReturn(allReminders)
        whenever(completionPatternAnalyzer.analyzeCompletionPattern(any())).thenReturn(pattern)
        whenever(completionPatternAnalyzer.getPatternSummary(pattern)).thenReturn(summary)

        // When
        val result = viewModel.getPatternSummary()

        // Then
        assertEquals(summary, result)
    }

    /** getCompletionProbabilityByHour는 특정 시간대의 완료 확률을 반환한다 */
    @Test
    fun getCompletionProbabilityByHourReturnsProbability() = runTest {
        // Given
        val hour = 10
        val probability = 0.85
        val pattern = CompletionPatternAnalyzer.CompletionPattern(
            mostProductiveHour = 10,
            mostProductiveDay = DayOfWeek.MONDAY,
            averageCompletionTime = 3.0,
            completionRate = 0.75,
            hourlyCompletionRate = mapOf(9 to 0.8, 10 to 0.9),
            dailyCompletionRate = mapOf(DayOfWeek.MONDAY to 0.85)
        )
        val allReminders = listOf(createReminder(id = 1))
        whenever(repository.getAllRemindersList()).thenReturn(allReminders)
        whenever(completionPatternAnalyzer.analyzeCompletionPattern(any())).thenReturn(pattern)
        whenever(completionPatternAnalyzer.getCompletionProbability(pattern, hour)).thenReturn(probability)

        // When
        val result = viewModel.getCompletionProbabilityByHour(hour)

        // Then
        assertEquals(0.85, result, 0.01)
    }

    /** getCompletionProbabilityByDay는 특정 요일의 완료 확률을 반환한다 */
    @Test
    fun getCompletionProbabilityByDayReturnsProbability() = runTest {
        // Given
        val day = DayOfWeek.MONDAY
        val probability = 0.90
        val pattern = CompletionPatternAnalyzer.CompletionPattern(
            mostProductiveHour = 10,
            mostProductiveDay = DayOfWeek.MONDAY,
            averageCompletionTime = 3.0,
            completionRate = 0.75,
            hourlyCompletionRate = mapOf(9 to 0.8, 10 to 0.9),
            dailyCompletionRate = mapOf(DayOfWeek.MONDAY to 0.85)
        )
        val allReminders = listOf(createReminder(id = 1))
        whenever(repository.getAllRemindersList()).thenReturn(allReminders)
        whenever(completionPatternAnalyzer.analyzeCompletionPattern(any())).thenReturn(pattern)
        whenever(completionPatternAnalyzer.getCompletionProbability(pattern, day)).thenReturn(probability)

        // When
        val result = viewModel.getCompletionProbabilityByDay(day)

        // Then
        assertEquals(0.90, result, 0.01)
    }

    // Helper function
    private fun createReminder(
        id: Long,
        title: String = "Test Reminder $id",
        isCompleted: Boolean = false,
        updatedAt: LocalDateTime = LocalDateTime.now()
    ) = ReminderEntity(
        id = id,
        title = title,
        isCompleted = isCompleted,
        createdAt = LocalDateTime.now(),
        updatedAt = updatedAt
    )
}
