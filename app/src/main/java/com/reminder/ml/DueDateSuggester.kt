package com.reminder.ml

import com.reminder.data.dao.MLTrainingDataDao
import com.reminder.data.entity.MLDataType
import com.reminder.data.entity.MLTrainingDataEntity
import java.time.LocalDateTime
import kotlin.math.roundToInt

/**
 * 마감일 스마트 제안기
 * 과거 유사한 작업의 소요 시간을 분석하여 적절한 마감일을 제안
 */
class DueDateSuggester(
    private val mlDao: MLTrainingDataDao
) {

    /**
     * 마감일 제안
     * @param title 리마인더 제목
     * @param description 리마인더 설명
     * @param category 카테고리 (선택)
     * @return 제안된 마감일 및 예상 소요 일수
     */
    suspend fun suggestDueDate(
        title: String,
        description: String,
        category: String? = null
    ): DueDateSuggestion {
        val inputText = "$title $description".trim()

        // 카테고리별 학습 데이터 우선 조회
        val categoryData = if (category != null) {
            mlDao.getDataByTypeAndCategory(MLDataType.DUE_DATE, category)
        } else {
            emptyList()
        }

        // 유사한 텍스트 기반 학습 데이터 조회
        val similarData = mlDao.findSimilarData(
            dataType = MLDataType.DUE_DATE,
            searchText = inputText,
            limit = 10
        )

        // 통합 데이터 (카테고리 + 유사 텍스트)
        val allData = (categoryData + similarData).distinctBy { it.id }

        if (allData.isEmpty()) {
            // 기본값: 3일 후
            val suggestedDate = LocalDateTime.now().plusDays(3)
            return DueDateSuggestion(
                dueDate = suggestedDate,
                estimatedDays = 3,
                confidence = 0.0f,
                reason = "기본 설정 (학습 데이터 없음)"
            )
        }

        // 평균 소요 일수 계산
        val avgDays = allData.map { it.outputLabel.toInt() }
            .average()
            .roundToInt()

        // 신뢰도 계산
        val confidence = calculateConfidence(allData)

        val suggestedDate = LocalDateTime.now().plusDays(avgDays.toLong())

        return DueDateSuggestion(
            dueDate = suggestedDate,
            estimatedDays = avgDays,
            confidence = confidence,
            reason = "유사한 작업 ${allData.size}개 분석 (평균 ${avgDays}일 소요)"
        )
    }

    /**
     * 신뢰도 계산
     */
    private fun calculateConfidence(data: List<MLTrainingDataEntity>): Float {
        if (data.isEmpty()) return 0.0f

        // 데이터 개수에 따른 신뢰도
        val countConfidence = kotlin.math.min(data.size / 10f, 1.0f)

        // 평균 신뢰도
        val avgConfidence = data.map { it.confidence }.average().toFloat()

        // 데이터 분산도 (일수 편차가 작을수록 높은 신뢰도)
        val days = data.map { it.outputLabel.toInt() }
        val avgDays = days.average()
        val variance = days.map { (it - avgDays) * (it - avgDays) }.average()
        val dispersionConfidence = when {
            variance < 1.0 -> 1.0f  // 거의 일정
            variance < 4.0 -> 0.8f  // 약간의 편차
            variance < 9.0 -> 0.6f  // 중간 편차
            else -> 0.4f            // 큰 편차
        }

        return (countConfidence * 0.4f) + (avgConfidence * 0.3f) + (dispersionConfidence * 0.3f)
    }

    /**
     * 학습 데이터 저장
     * @param title 리마인더 제목
     * @param description 리마인더 설명
     * @param category 카테고리
     * @param daysToComplete 완료까지 걸린 일수
     */
    suspend fun learn(
        title: String,
        description: String,
        category: String,
        daysToComplete: Int
    ) {
        val inputText = "$title $description".trim()

        val newData = MLTrainingDataEntity(
            dataType = MLDataType.DUE_DATE,
            inputText = inputText,
            outputLabel = daysToComplete.toString(),
            category = category,
            confidence = 1.0f,
            usageCount = 1,
            createdAt = LocalDateTime.now(),
            lastUsedAt = LocalDateTime.now()
        )

        mlDao.insert(newData)
    }
}

/**
 * 마감일 제안 결과
 */
data class DueDateSuggestion(
    val dueDate: LocalDateTime,
    val estimatedDays: Int,
    val confidence: Float,
    val reason: String
)
