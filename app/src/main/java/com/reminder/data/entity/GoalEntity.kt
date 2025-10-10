package com.reminder.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * v1.33.0: 목표 설정 엔티티
 *
 * 사용자가 설정한 일일/주간/월간 목표를 저장
 */
@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /**
     * 목표 타입 (DAILY, WEEKLY, MONTHLY)
     */
    val type: GoalType,

    /**
     * 목표 완료 개수
     */
    val targetCount: Int,

    /**
     * 특정 카테고리 목표 (null이면 전체 카테고리)
     */
    val category: String? = null,

    /**
     * 목표 시작일
     */
    val startDate: LocalDate,

    /**
     * 목표 종료일
     */
    val endDate: LocalDate,

    /**
     * 활성 상태
     */
    val isActive: Boolean = true,

    /**
     * 생성일시
     */
    val createdAt: LocalDate = LocalDate.now()
)

/**
 * 목표 타입
 */
enum class GoalType {
    DAILY,      // 일일 목표
    WEEKLY,     // 주간 목표
    MONTHLY     // 월간 목표
}
