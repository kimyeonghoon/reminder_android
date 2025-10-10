package com.reminder.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * ML 학습 데이터 엔티티
 * 사용자의 리마인더 사용 패턴을 학습하여 스마트 제안에 활용
 */
@Entity(
    tableName = "ml_training_data",
    indices = [
        Index(value = ["dataType"]),
        Index(value = ["dataType", "inputText"]),
        Index(value = ["createdAt"])
    ]
)
data class MLTrainingDataEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /**
     * 학습 데이터 타입
     * PRIORITY: 우선순위 예측 데이터
     * CATEGORY: 카테고리 분류 데이터
     * DUE_DATE: 마감일 제안 데이터
     * NOTIFICATION_TIME: 알림 시간 제안 데이터
     */
    val dataType: MLDataType,

    /**
     * 입력 텍스트 (제목 + 설명)
     * 키워드 추출 및 패턴 분석에 사용
     */
    val inputText: String,

    /**
     * 출력 레이블 (학습할 값)
     * PRIORITY: "LOW", "MEDIUM", "HIGH"
     * CATEGORY: 카테고리명
     * DUE_DATE: 완료까지 걸린 일수 (정수)
     * NOTIFICATION_TIME: 알림 시간 (HH:mm 형식)
     */
    val outputLabel: String,

    /**
     * 카테고리 (CATEGORY, DUE_DATE, NOTIFICATION_TIME 타입에서 사용)
     */
    val category: String? = null,

    /**
     * 요일 (NOTIFICATION_TIME 타입에서 사용)
     * 0=일요일, 1=월요일, ..., 6=토요일
     */
    val dayOfWeek: Int? = null,

    /**
     * 신뢰도 (0.0 ~ 1.0)
     * 해당 패턴이 얼마나 신뢰할 수 있는지
     */
    val confidence: Float = 1.0f,

    /**
     * 사용 횟수
     * 동일한 패턴이 반복될수록 증가
     */
    val usageCount: Int = 1,

    /**
     * 생성 시각
     */
    val createdAt: LocalDateTime = LocalDateTime.now(),

    /**
     * 마지막 사용 시각
     * 최근 패턴에 더 높은 가중치 부여
     */
    val lastUsedAt: LocalDateTime = LocalDateTime.now()
)

/**
 * ML 학습 데이터 타입
 */
enum class MLDataType {
    /** 우선순위 예측 */
    PRIORITY,

    /** 카테고리 분류 */
    CATEGORY,

    /** 마감일 제안 */
    DUE_DATE,

    /** 알림 시간 제안 */
    NOTIFICATION_TIME
}
