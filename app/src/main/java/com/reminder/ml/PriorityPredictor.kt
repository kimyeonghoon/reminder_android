package com.reminder.ml

import com.reminder.data.dao.MLTrainingDataDao
import com.reminder.data.entity.MLDataType
import com.reminder.data.entity.MLTrainingDataEntity
import com.reminder.data.entity.Priority
import java.time.LocalDateTime
import kotlin.math.min

/**
 * 우선순위 자동 예측기
 * 제목과 설명을 분석하여 적절한 우선순위를 제안
 */
class PriorityPredictor(
    private val mlDao: MLTrainingDataDao
) {

    /**
     * 우선순위 예측
     * @param title 리마인더 제목
     * @param description 리마인더 설명
     * @return 예측된 우선순위 및 신뢰도
     */
    suspend fun predictPriority(title: String, description: String): PriorityPrediction {
        // 입력 텍스트 조합
        val inputText = "$title $description".trim()

        // 유사한 학습 데이터 조회
        val similarData = mlDao.findSimilarData(
            dataType = MLDataType.PRIORITY,
            searchText = inputText,
            limit = 10
        )

        // 학습 데이터가 없으면 기본 우선순위 반환
        if (similarData.isEmpty()) {
            return PriorityPrediction(
                priority = Priority.MEDIUM,
                confidence = 0.0f,
                reason = "학습 데이터 없음"
            )
        }

        // 우선순위별 가중치 계산
        val priorityScores = mutableMapOf<Priority, Float>()

        for (data in similarData) {
            val priority = Priority.valueOf(data.outputLabel)
            val weight = calculateWeight(data)
            priorityScores[priority] = (priorityScores[priority] ?: 0f) + weight
        }

        // 가장 높은 점수를 가진 우선순위 선택
        val predictedPriority = priorityScores.maxByOrNull { it.value }?.key ?: Priority.MEDIUM

        // 신뢰도 계산
        val confidence = calculateConfidence(similarData, predictedPriority)

        return PriorityPrediction(
            priority = predictedPriority,
            confidence = confidence,
            reason = "유사한 작업 ${similarData.size}개 발견"
        )
    }

    /**
     * 학습 데이터의 가중치 계산
     * 사용 횟수와 신뢰도를 고려
     */
    private fun calculateWeight(data: MLTrainingDataEntity): Float {
        // 사용 횟수 가중치 (로그 스케일, 1~10)
        val usageWeight = min(1f + (Math.log10(data.usageCount.toDouble()).toFloat()), 10f)

        // 신뢰도 가중치 (0.0 ~ 1.0)
        val confidenceWeight = data.confidence

        // 최근성 가중치 (최근 데이터일수록 높은 가중치)
        val daysSinceLastUse = java.time.Duration.between(
            data.lastUsedAt,
            LocalDateTime.now()
        ).toDays()
        val recencyWeight = when {
            daysSinceLastUse <= 7 -> 1.0f      // 1주일 이내
            daysSinceLastUse <= 30 -> 0.8f     // 1개월 이내
            daysSinceLastUse <= 90 -> 0.6f     // 3개월 이내
            else -> 0.4f                        // 3개월 이상
        }

        return usageWeight * confidenceWeight * recencyWeight
    }

    /**
     * 예측 신뢰도 계산
     * @param similarData 유사한 학습 데이터 리스트
     * @param predictedPriority 예측된 우선순위
     * @return 신뢰도 (0.0 ~ 1.0)
     */
    private fun calculateConfidence(
        similarData: List<MLTrainingDataEntity>,
        predictedPriority: Priority
    ): Float {
        if (similarData.isEmpty()) return 0.0f

        // 예측된 우선순위와 일치하는 데이터 개수
        val matchingCount = similarData.count {
            Priority.valueOf(it.outputLabel) == predictedPriority
        }

        // 일치율
        val matchRatio = matchingCount.toFloat() / similarData.size

        // 평균 사용 횟수 (정규화)
        val avgUsageCount = similarData.map { it.usageCount }.average().toFloat()
        val usageConfidence = min(avgUsageCount / 10f, 1.0f)

        // 평균 신뢰도
        val avgConfidence = similarData.map { it.confidence }.average().toFloat()

        // 최종 신뢰도 = (일치율 50% + 사용 신뢰도 25% + 평균 신뢰도 25%)
        return (matchRatio * 0.5f) + (usageConfidence * 0.25f) + (avgConfidence * 0.25f)
    }

    /**
     * 학습 데이터 저장
     * 사용자가 우선순위를 설정할 때마다 호출
     * @param title 리마인더 제목
     * @param description 리마인더 설명
     * @param priority 사용자가 설정한 우선순위
     */
    suspend fun learn(title: String, description: String, priority: Priority) {
        val inputText = "$title $description".trim()

        // 동일한 입력 텍스트가 있는지 확인
        val existingData = mlDao.findSimilarData(
            dataType = MLDataType.PRIORITY,
            searchText = inputText,
            limit = 1
        ).firstOrNull()

        if (existingData != null && existingData.inputText == inputText &&
            existingData.outputLabel == priority.name) {
            // 기존 데이터의 사용 횟수 증가
            mlDao.incrementUsageCount(
                id = existingData.id,
                currentTime = LocalDateTime.now().toString()
            )
        } else {
            // 새 학습 데이터 삽입
            val newData = MLTrainingDataEntity(
                dataType = MLDataType.PRIORITY,
                inputText = inputText,
                outputLabel = priority.name,
                confidence = 1.0f,  // 사용자 입력이므로 100% 신뢰
                usageCount = 1,
                createdAt = LocalDateTime.now(),
                lastUsedAt = LocalDateTime.now()
            )
            mlDao.insert(newData)
        }
    }

    /**
     * 키워드 추출 (향후 개선용)
     * 제목과 설명에서 중요한 키워드를 추출
     */
    private fun extractKeywords(text: String): List<String> {
        // 간단한 구현: 공백으로 분리
        // TODO: 한국어 형태소 분석기 적용 (KoNLPy, Komoran 등)
        return text.lowercase()
            .split(Regex("\\s+"))
            .filter { it.length > 1 }  // 1글자 단어 제외
            .take(5)  // 상위 5개 키워드
    }
}

/**
 * 우선순위 예측 결과 데이터 클래스
 */
data class PriorityPrediction(
    val priority: Priority,
    val confidence: Float,  // 0.0 ~ 1.0
    val reason: String = "" // 예측 이유 (디버깅/UI 표시용)
)
