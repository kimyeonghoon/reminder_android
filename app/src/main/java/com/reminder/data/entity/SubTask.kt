package com.reminder.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * 서브태스크 엔티티
 *
 * 리마인더의 하위 작업 항목을 나타냅니다.
 * 각 리마인더는 여러 개의 서브태스크를 가질 수 있습니다.
 *
 * @property id 서브태스크 고유 ID
 * @property reminderId 상위 리마인더 ID (외래키)
 * @property title 서브태스크 제목
 * @property isCompleted 완료 여부
 * @property position 서브태스크 순서 (0부터 시작)
 * @property createdAt 생성 일시
 */
@Entity(
    tableName = "subtasks",
    foreignKeys = [
        ForeignKey(
            entity = ReminderEntity::class,
            parentColumns = ["id"],
            childColumns = ["reminderId"],
            onDelete = ForeignKey.CASCADE // 리마인더 삭제 시 서브태스크도 함께 삭제
        )
    ],
    indices = [
        Index(value = ["reminderId"]), // 리마인더별 조회 최적화
        Index(value = ["reminderId", "position"]) // 정렬된 조회 최적화
    ]
)
data class SubTask(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val reminderId: Long,

    val title: String,

    val isCompleted: Boolean = false,

    val position: Int = 0,

    val createdAt: LocalDateTime = LocalDateTime.now()
)
