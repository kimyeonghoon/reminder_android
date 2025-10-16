package com.reminder.ml

import com.reminder.data.entity.Priority
import com.reminder.data.entity.ReminderEntity
import com.reminder.data.entity.Urgency
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

/**
 * CategorySuggestionHelper 단위 테스트
 *
 * 테스트 범위:
 * - 키워드 기반 카테고리 제안 (suggestCategories를 통해 간접 테스트)
 * - 패턴 기반 카테고리 제안 (suggestCategories를 통해 간접 테스트)
 * - 빈도 기반 카테고리 제안 (suggestCategories를 통해 간접 테스트)
 * - 전체 카테고리 제안 통합 로직
 * - 카테고리 빈도 계산
 * - 카테고리 목록 조회
 * - 엣지 케이스 처리
 */
class CategorySuggestionHelperTest {

    private lateinit var helper: CategorySuggestionHelper

    @Before
    fun setup() {
        helper = CategorySuggestionHelper()
    }

    // ===== suggestCategories 테스트 (키워드 기반) =====

    @Test
    fun suggestCategoriesReturnsWorkForMeetingTitle() {
        // Given: "회의" 키워드가 포함된 제목
        val title = "팀 회의 준비"
        val description = ""

        // When: 카테고리 제안 요청
        val suggestions = helper.suggestCategories(title, description)

        // Then: "업무" 카테고리 제안
        assertTrue(suggestions.contains("업무"))
    }

    @Test
    fun suggestCategoriesReturnsHealthForExerciseTitle() {
        // Given: "운동" 키워드가 포함된 제목
        val title = "헬스장 가기"
        val description = "러닝머신 30분"

        // When: 카테고리 제안 요청
        val suggestions = helper.suggestCategories(title, description)

        // Then: "건강" 카테고리 제안
        assertTrue(suggestions.contains("건강"))
    }

    @Test
    fun suggestCategoriesReturnsShoppingForPurchaseTitle() {
        // Given: "구매" 키워드가 포함된 제목
        val title = "노트북 구매"
        val description = ""

        // When: 카테고리 제안 요청
        val suggestions = helper.suggestCategories(title, description)

        // Then: "쇼핑" 카테고리 제안
        assertTrue(suggestions.contains("쇼핑"))
    }

    @Test
    fun suggestCategoriesChecksDescriptionForKeywords() {
        // Given: 설명에 "병원" 키워드 포함
        val title = "주말 계획"
        val description = "병원에서 건강검진 받기"

        // When: 카테고리 제안 요청
        val suggestions = helper.suggestCategories(title, description)

        // Then: "건강" 카테고리 제안
        assertTrue(suggestions.contains("건강"))
    }

    @Test
    fun suggestCategoriesReturnsEmptyForNoMatchingKeywords() {
        // Given: 매칭되는 키워드가 없는 제목
        val title = "xyz123"
        val description = "abc456"

        // When: 카테고리 제안 요청 (기존 리마인더 없음)
        val suggestions = helper.suggestCategories(title, description, emptyList())

        // Then: 빈 리스트 반환
        assertTrue(suggestions.isEmpty())
    }

    @Test
    fun suggestCategoriesHandlesEmptyStrings() {
        // Given: 빈 문자열
        val title = ""
        val description = ""

        // When: 카테고리 제안 요청
        val suggestions = helper.suggestCategories(title, description)

        // Then: 빈 리스트 반환
        assertTrue(suggestions.isEmpty())
    }

    // ===== suggestCategories 테스트 (패턴 기반) =====

    @Test
    fun suggestCategoriesUsesPatternFromExistingReminders() {
        // Given: "회의" 제목의 리마인더가 많은 목록
        val existingReminders = listOf(
            createReminder(1, "팀 회의", "업무"),
            createReminder(2, "클라이언트 회의", "업무"),
            createReminder(3, "주간 회의", "업무")
        )
        val title = "월간 회의"
        val description = ""

        // When: 카테고리 제안 요청
        val suggestions = helper.suggestCategories(title, description, existingReminders)

        // Then: 패턴 기반으로 "업무" 카테고리 제안
        assertTrue(suggestions.contains("업무"))
    }

    @Test
    fun suggestCategoriesUsesFrequencyFromExistingReminders() {
        // Given: "건강" 카테고리가 가장 빈번한 리마인더 목록
        val existingReminders = listOf(
            createReminder(1, "운동", "건강"),
            createReminder(2, "조깅", "건강"),
            createReminder(3, "요가", "건강"),
            createReminder(4, "회의", "업무")
        )
        val title = "새로운 할일"
        val description = ""

        // When: 카테고리 제안 요청
        val suggestions = helper.suggestCategories(title, description, existingReminders)

        // Then: 빈도 기반으로 "건강" 카테고리 제안
        assertTrue(suggestions.contains("건강"))
    }

    @Test
    fun suggestCategoriesReturnsMaxThreeSuggestions() {
        // Given: 많은 키워드가 포함된 제목과 많은 기존 리마인더
        val existingReminders = listOf(
            createReminder(1, "a", "카테고리1"),
            createReminder(2, "b", "카테고리2"),
            createReminder(3, "c", "카테고리3"),
            createReminder(4, "d", "카테고리4")
        )
        val title = "회의 운동 쇼핑 공부"
        val description = ""

        // When: 카테고리 제안 요청
        val suggestions = helper.suggestCategories(title, description, existingReminders)

        // Then: 최대 3개 카테고리 반환
        assertTrue(suggestions.size <= 3)
    }

    @Test
    fun suggestCategoriesReturnsUniqueCategories() {
        // Given: 중복 제안이 발생할 수 있는 입력
        val existingReminders = listOf(
            createReminder(1, "미팅", "업무"),
            createReminder(2, "회의", "업무"),
            createReminder(3, "발표", "업무")
        )
        val title = "회의"
        val description = "업무 회의"

        // When: 카테고리 제안 요청
        val suggestions = helper.suggestCategories(title, description, existingReminders)

        // Then: 중복 없이 고유한 카테고리만 반환
        assertEquals(suggestions.size, suggestions.toSet().size)
    }

    @Test
    fun suggestCategoriesWorksWithoutExistingReminders() {
        // Given: 기존 리마인더 없이 제목만 제공
        val title = "운동하기"
        val description = ""

        // When: 카테고리 제안 요청 (existingReminders 생략)
        val suggestions = helper.suggestCategories(title, description)

        // Then: 키워드 기반 제안만 반환
        assertTrue(suggestions.contains("건강"))
    }

    @Test
    fun suggestCategoriesIgnoresBlankCategoriesInExistingReminders() {
        // Given: 빈 카테고리를 포함한 리마인더 목록
        val existingReminders = listOf(
            createReminder(1, "회의", "업무"),
            createReminder(2, "할일", ""),
            createReminder(3, "운동", "건강")
        )
        val title = "새로운 할일"
        val description = ""

        // When: 카테고리 제안 요청
        val suggestions = helper.suggestCategories(title, description, existingReminders)

        // Then: 빈 카테고리는 제안되지 않음
        assertFalse(suggestions.contains(""))
        assertTrue(suggestions.all { it.isNotBlank() })
    }

    // ===== getAllCategories 테스트 =====

    @Test
    fun getAllCategoriesReturnsUniqueCategories() {
        // Given: 다양한 카테고리의 리마인더 목록
        val reminders = listOf(
            createReminder(1, "회의", "업무"),
            createReminder(2, "운동", "건강"),
            createReminder(3, "발표", "업무"),
            createReminder(4, "조깅", "건강")
        )

        // When: 모든 카테고리 목록 요청
        val categories = helper.getAllCategories(reminders)

        // Then: 고유한 카테고리만 반환
        assertEquals(2, categories.size)
        assertTrue(categories.contains("업무"))
        assertTrue(categories.contains("건강"))
    }

    @Test
    fun getAllCategoriesReturnsSortedList() {
        // Given: 정렬되지 않은 카테고리의 리마인더 목록
        val reminders = listOf(
            createReminder(1, "a", "쇼핑"),
            createReminder(2, "b", "건강"),
            createReminder(3, "c", "업무")
        )

        // When: 모든 카테고리 목록 요청
        val categories = helper.getAllCategories(reminders)

        // Then: 정렬된 리스트 반환
        assertEquals(listOf("건강", "쇼핑", "업무"), categories)
    }

    @Test
    fun getAllCategoriesIgnoresBlankCategories() {
        // Given: 빈 카테고리를 포함한 리마인더 목록
        val reminders = listOf(
            createReminder(1, "회의", "업무"),
            createReminder(2, "할일", ""),
            createReminder(3, "운동", "건강")
        )

        // When: 모든 카테고리 목록 요청
        val categories = helper.getAllCategories(reminders)

        // Then: 빈 카테고리는 제외
        assertEquals(2, categories.size)
        assertFalse(categories.contains(""))
    }

    @Test
    fun getAllCategoriesReturnsEmptyForEmptyList() {
        // Given: 빈 리마인더 목록
        val reminders = emptyList<ReminderEntity>()

        // When: 모든 카테고리 목록 요청
        val categories = helper.getAllCategories(reminders)

        // Then: 빈 리스트 반환
        assertTrue(categories.isEmpty())
    }

    // ===== getCategoryFrequency 테스트 =====

    @Test
    fun getCategoryFrequencyCountsCorrectly() {
        // Given: 카테고리 빈도가 다른 리마인더 목록
        val reminders = listOf(
            createReminder(1, "회의", "업무"),
            createReminder(2, "운동", "건강"),
            createReminder(3, "발표", "업무"),
            createReminder(4, "조깅", "건강"),
            createReminder(5, "요가", "건강")
        )

        // When: 카테고리 빈도 계산 요청
        val frequency = helper.getCategoryFrequency(reminders)

        // Then: 정확한 빈도 반환
        assertEquals(2, frequency["업무"])
        assertEquals(3, frequency["건강"])
    }

    @Test
    fun getCategoryFrequencyIgnoresBlankCategories() {
        // Given: 빈 카테고리를 포함한 리마인더 목록
        val reminders = listOf(
            createReminder(1, "회의", "업무"),
            createReminder(2, "할일", ""),
            createReminder(3, "운동", "건강")
        )

        // When: 카테고리 빈도 계산 요청
        val frequency = helper.getCategoryFrequency(reminders)

        // Then: 빈 카테고리는 제외
        assertFalse(frequency.containsKey(""))
        assertEquals(1, frequency["업무"])
        assertEquals(1, frequency["건강"])
    }

    @Test
    fun getCategoryFrequencyReturnsEmptyMapForEmptyList() {
        // Given: 빈 리마인더 목록
        val reminders = emptyList<ReminderEntity>()

        // When: 카테고리 빈도 계산 요청
        val frequency = helper.getCategoryFrequency(reminders)

        // Then: 빈 맵 반환
        assertTrue(frequency.isEmpty())
    }

    @Test
    fun getCategoryFrequencyCombinesSameCategories() {
        // Given: 동일한 카테고리의 리마인더 목록
        val reminders = listOf(
            createReminder(1, "회의", "업무"),
            createReminder(2, "미팅", "업무"),
            createReminder(3, "발표", "업무")
        )

        // When: 카테고리 빈도 계산 요청
        val frequency = helper.getCategoryFrequency(reminders)

        // Then: 동일 카테고리는 합산
        assertEquals(1, frequency.size)
        assertEquals(3, frequency["업무"])
    }

    // ===== DEFAULT_CATEGORIES 테스트 =====

    @Test
    fun defaultCategoriesContainsExpectedCategories() {
        // When: 기본 카테고리 목록 확인
        val defaultCategories = CategorySuggestionHelper.DEFAULT_CATEGORIES

        // Then: 예상된 카테고리 포함
        assertTrue(defaultCategories.contains("업무"))
        assertTrue(defaultCategories.contains("개인"))
        assertTrue(defaultCategories.contains("건강"))
        assertTrue(defaultCategories.contains("쇼핑"))
        assertTrue(defaultCategories.contains("가족"))
        assertTrue(defaultCategories.contains("공부"))
        assertTrue(defaultCategories.contains("금융"))
    }

    @Test
    fun defaultCategoriesIsNotEmpty() {
        // When: 기본 카테고리 목록 확인
        val defaultCategories = CategorySuggestionHelper.DEFAULT_CATEGORIES

        // Then: 비어있지 않음
        assertTrue(defaultCategories.isNotEmpty())
    }

    // ===== 헬퍼 메서드 =====

    private fun createReminder(
        id: Long,
        title: String,
        category: String
    ): ReminderEntity {
        return ReminderEntity(
            id = id,
            title = title,
            description = "",
            dueDateTime = LocalDateTime.now(),
            priority = Priority.MEDIUM,
            urgency = Urgency.MEDIUM,
            category = category,
            isCompleted = false,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }
}
