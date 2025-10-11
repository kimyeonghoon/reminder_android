package com.reminder.ai

import com.reminder.data.entity.Urgency

/**
 * v1.48.0: AI 긴급도 예측기
 *
 * 제목과 설명을 분석하여 긴급도를 자동으로 예측합니다.
 * 키워드 기반 NLP 분석을 사용합니다.
 */
class UrgencyPredictor {

    private val highUrgencyKeywords = setOf(
        // 한글
        "긴급", "지금", "바로", "오늘", "당장", "즉시", "빨리", "급해",
        // 영어
        "urgent", "asap", "now", "immediately", "today", "critical", "emergency"
    )

    private val mediumUrgencyKeywords = setOf(
        // 한글
        "이번 주", "이번주", "곧", "빨리", "조만간",
        // 영어
        "this week", "soon", "upcoming", "shortly"
    )

    private val lowUrgencyKeywords = setOf(
        // 한글
        "나중에", "언젠가", "여유", "천천히",
        // 영어
        "later", "someday", "eventually", "whenever", "low priority"
    )

    /**
     * 제목과 설명을 분석하여 긴급도를 예측합니다.
     *
     * @param title 리마인더 제목
     * @param description 리마인더 설명
     * @return 예측된 긴급도 (HIGH, MEDIUM, LOW)
     */
    fun predictUrgency(title: String, description: String): Urgency {
        val combined = "$title $description".lowercase()

        // HIGH 우선순위가 가장 높음
        if (highUrgencyKeywords.any { combined.contains(it) }) {
            return Urgency.HIGH
        }

        // MEDIUM이 두 번째 우선순위
        if (mediumUrgencyKeywords.any { combined.contains(it) }) {
            return Urgency.MEDIUM
        }

        // LOW가 세 번째 우선순위
        if (lowUrgencyKeywords.any { combined.contains(it) }) {
            return Urgency.LOW
        }

        // 키워드가 없으면 기본값 MEDIUM
        return Urgency.MEDIUM
    }

    /**
     * 긴급도 예측에 대한 설명을 반환합니다.
     *
     * @param title 리마인더 제목
     * @param description 리마인더 설명
     * @return 예측 근거 설명
     */
    fun getReasonForPrediction(title: String, description: String): String {
        val combined = "$title $description".lowercase()
        val urgency = predictUrgency(title, description)

        return when (urgency) {
            Urgency.HIGH -> {
                val found = highUrgencyKeywords.find { combined.contains(it) }
                "\"$found\" 키워드를 감지하여 높은 긴급도로 예측했습니다"
            }
            Urgency.MEDIUM -> {
                val found = mediumUrgencyKeywords.find { combined.contains(it) }
                if (found != null) {
                    "\"$found\" 키워드를 감지하여 보통 긴급도로 예측했습니다"
                } else {
                    "특별한 긴급도 키워드가 없어 보통 긴급도로 예측했습니다"
                }
            }
            Urgency.LOW -> {
                val found = lowUrgencyKeywords.find { combined.contains(it) }
                "\"$found\" 키워드를 감지하여 낮은 긴급도로 예측했습니다"
            }
        }
    }
}
