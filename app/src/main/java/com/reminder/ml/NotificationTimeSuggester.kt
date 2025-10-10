package com.reminder.ml

import com.reminder.data.dao.MLTrainingDataDao
import com.reminder.data.entity.MLDataType
import com.reminder.data.entity.MLTrainingDataEntity
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * 스마트 알림 시간 제안기
 * 사용자의 완료 시간대 패턴을 분석하여 최적의 알림 시간을 제안
 */
class NotificationTimeSuggester(
    private val mlDao: MLTrainingDataDao
) {

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    /**
     * 알림 시간 제안
     * @param category 카테고리 (선택)
     * @param dayOfWeek 요일 (선택, 없으면 오늘 요일)
     * @return 제안된 알림 시간 목록 (최대 3개)
     */
    suspend fun suggestNotificationTime(
        category: String? = null,
        dayOfWeek: DayOfWeek? = null
    ): List<NotificationTimeSuggestion> {
        val targetDayOfWeek = dayOfWeek ?: LocalDateTime.now().dayOfWeek
        val dayOfWeekValue = targetDayOfWeek.value % 7  // 0=일요일, 1=월요일, ..., 6=토요일

        // 요일별 학습 데이터 조회
        val dayData = mlDao.getNotificationTimeByDayOfWeek(
            dayOfWeek = dayOfWeekValue,
            limit = 20
        )

        // 카테고리 필터링 (있는 경우)
        val filteredData = if (category != null) {
            dayData.filter { it.category == category }
        } else {
            dayData
        }

        if (filteredData.isEmpty()) {
            // 기본 시간: 오전 9시, 오후 2시, 오후 7시
            return listOf(
                NotificationTimeSuggestion(
                    time = LocalTime.of(9, 0),
                    confidence = 0.0f,
                    reason = "기본 설정"
                ),
                NotificationTimeSuggestion(
                    time = LocalTime.of(14, 0),
                    confidence = 0.0f,
                    reason = "기본 설정"
                ),
                NotificationTimeSuggestion(
                    time = LocalTime.of(19, 0),
                    confidence = 0.0f,
                    reason = "기본 설정"
                )
            )
        }

        // 시간대별 완료 횟수 계산
        val timeSlots = mutableMapOf<Int, MutableList<MLTrainingDataEntity>>()

        for (data in filteredData) {
            val time = LocalTime.parse(data.outputLabel, timeFormatter)
            val hour = time.hour

            if (timeSlots.containsKey(hour)) {
                timeSlots[hour]!!.add(data)
            } else {
                timeSlots[hour] = mutableListOf(data)
            }
        }

        // 시간대별 점수 계산 및 정렬
        val suggestions = timeSlots.map { (hour, dataList) ->
            val totalUsage = dataList.sumOf { it.usageCount }
            val avgConfidence = dataList.map { it.confidence }.average().toFloat()

            // 최근성 고려
            val recentData = dataList.count {
                java.time.Duration.between(it.lastUsedAt, LocalDateTime.now()).toDays() <= 30
            }
            val recencyScore = (recentData.toFloat() / dataList.size)

            // 최종 신뢰도
            val confidence = (
                (totalUsage / 50f).coerceAtMost(1.0f) * 0.4f +
                avgConfidence * 0.3f +
                recencyScore * 0.3f
            )

            NotificationTimeSuggestion(
                time = LocalTime.of(hour, 0),
                confidence = confidence,
                reason = "${dataList.size}회 완료 패턴 발견"
            )
        }.sortedByDescending { it.confidence }
            .take(3)

        return suggestions
    }

    /**
     * 학습 데이터 저장
     * @param completedTime 작업 완료 시각
     * @param category 카테고리
     */
    suspend fun learn(completedTime: LocalDateTime, category: String) {
        val dayOfWeek = completedTime.dayOfWeek.value % 7
        val timeString = completedTime.toLocalTime().format(timeFormatter)

        // 동일한 시간대가 있는지 확인 (같은 요일, 카테고리, 시간)
        val existingData = mlDao.getNotificationTimeByDayOfWeek(dayOfWeek, 100)
            .firstOrNull {
                it.category == category &&
                it.outputLabel == timeString
            }

        if (existingData != null) {
            // 사용 횟수 증가
            mlDao.incrementUsageCount(
                id = existingData.id,
                currentTime = LocalDateTime.now().toString()
            )
        } else {
            // 새 학습 데이터 삽입
            val newData = MLTrainingDataEntity(
                dataType = MLDataType.NOTIFICATION_TIME,
                inputText = "$category $dayOfWeek",
                outputLabel = timeString,
                category = category,
                dayOfWeek = dayOfWeek,
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
 * 알림 시간 제안 결과
 */
data class NotificationTimeSuggestion(
    val time: LocalTime,
    val confidence: Float,
    val reason: String
)
