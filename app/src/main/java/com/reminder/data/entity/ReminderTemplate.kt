package com.reminder.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * 리마인더 템플릿 엔티티
 *
 * 자주 사용하는 리마인더의 구조를 템플릿으로 저장
 */
@Entity(tableName = "reminder_templates")
data class ReminderTemplate(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /**
     * 템플릿 이름
     */
    val name: String,

    /**
     * 리마인더 제목 템플릿
     */
    val titleTemplate: String,

    /**
     * 리마인더 설명 템플릿
     */
    val descriptionTemplate: String = "",

    /**
     * 기본 우선순위
     */
    val defaultPriority: Priority = Priority.MEDIUM,

    /**
     * 기본 카테고리
     */
    val defaultCategory: String = "",

    /**
     * 기본 반복 패턴
     */
    val defaultRecurrencePattern: RecurrencePattern = RecurrencePattern.NONE,

    /**
     * 기본 반복 간격
     */
    val defaultRecurrenceInterval: Int = 1,

    /**
     * 생성 일시
     */
    val createdAt: LocalDateTime = LocalDateTime.now(),

    /**
     * 수정 일시
     */
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
