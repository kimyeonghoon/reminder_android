package com.reminder.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * 저장된 필터 (스마트 컬렉션)
 *
 * 사용자가 자주 사용하는 필터 조합을 저장하여 빠르게 접근 가능
 */
@Entity(tableName = "saved_filters")
data class SavedFilterEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /**
     * 필터 이름 (예: "오늘의 긴급 업무", "이번 주 할 일")
     */
    val name: String,

    /**
     * 아이콘 이름 (Material Icons)
     */
    val icon: String = "filter_list",

    /**
     * ReminderFilter를 JSON으로 직렬화한 문자열
     */
    val filterJson: String,

    /**
     * 생성 시간
     */
    val createdAt: LocalDateTime = LocalDateTime.now(),

    /**
     * 표시 순서 (낮을수록 먼저 표시)
     */
    val order: Int = 0
)
