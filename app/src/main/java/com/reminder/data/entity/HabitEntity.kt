package com.reminder.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * v1.44.0: Habit Entity
 *
 * 습관 추적 정보를 저장하는 엔티티
 */
@Entity(
    tableName = "habits",
    indices = [
        Index(value = ["isActive"]),
        Index(value = ["createdAt"])
    ]
)
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // 습관 이름 (필수)
    val name: String,

    // 습관 설명
    val description: String = "",

    // 주당 목표 횟수 (기본값: 7 = 매일)
    val frequency: Int = 7,

    // 활성 상태 (비활성화된 습관은 목록에서 숨김)
    val isActive: Boolean = true,

    // 생성 일시
    val createdAt: LocalDateTime = LocalDateTime.now(),

    // 수정 일시
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
