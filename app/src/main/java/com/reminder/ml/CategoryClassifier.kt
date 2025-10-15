package com.reminder.ml

import com.reminder.data.dao.MLTrainingDataDao
import com.reminder.data.entity.MLDataType
import com.reminder.data.entity.MLTrainingDataEntity
import java.time.LocalDateTime

/**
 * 카테고리 자동 분류기
 * 제목과 설명을 분석하여 적절한 카테고리를 제안
 * v1.25.0의 통계 기반 카테고리 제안을 ML 기반으로 강화
 */
class CategoryClassifier(
    private val mlDao: MLTrainingDataDao
) {

    /**
     * 카테고리 분류
     * @param title 리마인더 제목
     * @param description 리마인더 설명
     * @return 제안된 카테고리 목록 (최대 3개, 신뢰도 순)
     */
    suspend fun classifyCategory(
        title: String,
        description: String
    ): List<CategorySuggestion> {
        val inputText = "$title $description".trim()

        // 유사한 학습 데이터 조회
        val similarData = mlDao.findSimilarData(
            dataType = MLDataType.CATEGORY,
            searchText = inputText,
            limit = 20
        )

        if (similarData.isEmpty()) {
            return listOf(
                CategorySuggestion(
                    category = "기타",
                    confidence = 0.0f,
                    reason = "학습 데이터 없음"
                )
            )
        }

        // 카테고리별 점수 계산
        val categoryScores = mutableMapOf<String, MutableList<Float>>()

        for (data in similarData) {
            val category = data.outputLabel
            val score = calculateScore(data)

            categoryScores.getOrPut(category) { mutableListOf() }.add(score)
        }

        // 카테고리별 평균 점수 계산 및 정렬
        val suggestions = categoryScores.map { (category, scores) ->
            val avgScore = scores.average().toFloat()
            val confidence = calculateConfidence(scores.size, avgScore)

            CategorySuggestion(
                category = category,
                confidence = confidence,
                reason = "${scores.size}개 유사 패턴 발견"
            )
        }.sortedByDescending { it.confidence }
            .take(3)  // 상위 3개

        return suggestions
    }

    /**
     * 학습 데이터 점수 계산
     */
    private fun calculateScore(data: MLTrainingDataEntity): Float {
        // 사용 횟수 점수
        val usageScore = kotlin.math.min(data.usageCount / 5f, 1.0f)

        // 신뢰도 점수
        val confidenceScore = data.confidence

        // 최근성 점수
        val daysSinceLastUse = java.time.Duration.between(
            data.lastUsedAt,
            LocalDateTime.now()
        ).toDays()
        val recencyScore = when {
            daysSinceLastUse <= 7 -> 1.0f
            daysSinceLastUse <= 30 -> 0.8f
            daysSinceLastUse <= 90 -> 0.6f
            else -> 0.4f
        }

        return (usageScore * 0.4f) + (confidenceScore * 0.4f) + (recencyScore * 0.2f)
    }

    /**
     * 신뢰도 계산
     */
    private fun calculateConfidence(count: Int, avgScore: Float): Float {
        // 데이터 개수에 따른 신뢰도
        val countConfidence = kotlin.math.min(count / 5f, 1.0f)

        // 평균 점수 신뢰도
        val scoreConfidence = avgScore

        return (countConfidence * 0.5f) + (scoreConfidence * 0.5f)
    }

    /**
     * 학습 데이터 저장
     */
    suspend fun learn(title: String, description: String, category: String) {
        val inputText = "$title $description".trim()

        // 동일한 입력-출력 쌍이 있는지 확인
        val existingData = mlDao.findSimilarData(
            dataType = MLDataType.CATEGORY,
            searchText = inputText,
            limit = 1
        ).firstOrNull { it.inputText == inputText && it.outputLabel == category }

        if (existingData != null) {
            // 사용 횟수 증가
            mlDao.incrementUsageCount(
                id = existingData.id,
                currentTime = LocalDateTime.now().toString()
            )
        } else {
            // 새 학습 데이터 삽입
            val newData = MLTrainingDataEntity(
                dataType = MLDataType.CATEGORY,
                inputText = inputText,
                outputLabel = category,
                confidence = 1.0f,
                usageCount = 1,
                createdAt = LocalDateTime.now(),
                lastUsedAt = LocalDateTime.now()
            )
            mlDao.insert(newData)
        }
    }
}

/**
 * 카테고리 제안 결과
 */
data class CategorySuggestion(
    val category: String,
    val confidence: Float,
    val reason: String
)
