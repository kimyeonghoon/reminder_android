package com.reminder.ml

import com.reminder.data.entity.ReminderEntity

/**
 * 카테고리 자동 제안 헬퍼 클래스
 *
 * 사용자의 과거 리마인더 데이터를 분석하여
 * 제목과 설명을 기반으로 적절한 카테고리를 제안합니다.
 */
class CategorySuggestionHelper {

    /**
     * 키워드와 카테고리 매핑
     */
    private val keywordCategoryMap = mapOf(
        // 업무 관련
        "회의" to "업무",
        "미팅" to "업무",
        "프로젝트" to "업무",
        "업무" to "업무",
        "보고서" to "업무",
        "발표" to "업무",
        "프레젠테이션" to "업무",
        "PT" to "업무",
        "출장" to "업무",

        // 개인 관련
        "개인" to "개인",
        "일기" to "개인",
        "독서" to "개인",
        "취미" to "개인",

        // 건강 관련
        "운동" to "건강",
        "헬스" to "건강",
        "요가" to "건강",
        "병원" to "건강",
        "약" to "건강",
        "건강검진" to "건강",
        "다이어트" to "건강",

        // 쇼핑 관련
        "쇼핑" to "쇼핑",
        "구매" to "쇼핑",
        "주문" to "쇼핑",
        "장보기" to "쇼핑",
        "마트" to "쇼핑",

        // 가족 관련
        "가족" to "가족",
        "부모님" to "가족",
        "엄마" to "가족",
        "아빠" to "가족",
        "자녀" to "가족",

        // 공부 관련
        "공부" to "공부",
        "학습" to "공부",
        "시험" to "공부",
        "수업" to "공부",
        "강의" to "공부",
        "과제" to "공부",

        // 금융 관련
        "세금" to "금융",
        "납부" to "금융",
        "카드" to "금융",
        "대출" to "금융",
        "보험" to "금융",
        "은행" to "금융"
    )

    /**
     * 텍스트를 분석하여 카테고리 제안
     *
     * @param title 리마인더 제목
     * @param description 리마인더 설명
     * @param existingReminders 기존 리마인더 목록 (학습용)
     * @return 제안된 카테고리 목록 (최대 3개)
     */
    fun suggestCategories(
        title: String,
        description: String = "",
        existingReminders: List<ReminderEntity> = emptyList()
    ): List<String> {
        val suggestions = mutableSetOf<String>()
        val combinedText = "$title $description".lowercase()

        // 1. 키워드 기반 제안
        val keywordSuggestions = suggestByKeywords(combinedText)
        suggestions.addAll(keywordSuggestions)

        // 2. 과거 패턴 기반 제안 (유사한 제목의 리마인더에서 학습)
        val patternSuggestions = suggestByPattern(title, existingReminders)
        suggestions.addAll(patternSuggestions)

        // 3. 자주 사용된 카테고리 제안
        val frequentSuggestions = suggestByFrequency(existingReminders)
        suggestions.addAll(frequentSuggestions)

        // 최대 3개 반환
        return suggestions.take(3)
    }

    /**
     * 키워드 기반 카테고리 제안
     */
    private fun suggestByKeywords(text: String): List<String> {
        val suggestions = mutableListOf<String>()

        for ((keyword, category) in keywordCategoryMap) {
            if (text.contains(keyword)) {
                suggestions.add(category)
            }
        }

        return suggestions.distinct()
    }

    /**
     * 과거 패턴 기반 카테고리 제안
     *
     * 제목이 유사한 리마인더의 카테고리를 제안
     */
    private fun suggestByPattern(title: String, existingReminders: List<ReminderEntity>): List<String> {
        if (existingReminders.isEmpty()) return emptyList()

        val titleLower = title.lowercase()
        val suggestions = mutableMapOf<String, Int>()

        // 유사한 제목을 가진 리마인더 찾기
        for (reminder in existingReminders) {
            if (reminder.category.isBlank()) continue

            val reminderTitleLower = reminder.title.lowercase()

            // 제목에 공통 단어가 있으면 점수 증가
            val commonWords = titleLower.split(" ")
                .filter { it.length >= 2 }
                .count { reminderTitleLower.contains(it) }

            if (commonWords > 0) {
                suggestions[reminder.category] = suggestions.getOrDefault(reminder.category, 0) + commonWords
            }
        }

        // 점수 순으로 정렬
        return suggestions.entries
            .sortedByDescending { it.value }
            .map { it.key }
            .take(2)
    }

    /**
     * 자주 사용된 카테고리 제안
     */
    private fun suggestByFrequency(existingReminders: List<ReminderEntity>): List<String> {
        if (existingReminders.isEmpty()) return emptyList()

        // 카테고리별 빈도 계산
        val categoryFrequency = existingReminders
            .filter { it.category.isNotBlank() }
            .groupingBy { it.category }
            .eachCount()

        // 빈도 순으로 정렬하여 상위 2개 반환
        return categoryFrequency.entries
            .sortedByDescending { it.value }
            .map { it.key }
            .take(2)
    }

    /**
     * 모든 고유 카테고리 목록 반환
     */
    fun getAllCategories(existingReminders: List<ReminderEntity>): List<String> {
        return existingReminders
            .map { it.category }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
    }

    /**
     * 카테고리 사용 빈도 계산
     */
    fun getCategoryFrequency(existingReminders: List<ReminderEntity>): Map<String, Int> {
        return existingReminders
            .filter { it.category.isNotBlank() }
            .groupingBy { it.category }
            .eachCount()
    }

    companion object {
        /**
         * 기본 카테고리 목록
         */
        val DEFAULT_CATEGORIES = listOf(
            "업무",
            "개인",
            "건강",
            "쇼핑",
            "가족",
            "공부",
            "금융"
        )
    }
}
