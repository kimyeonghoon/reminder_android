package com.reminder.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * 리마인더 이미지 엔티티
 *
 * 리마인더에 첨부된 이미지를 나타냅니다.
 * 각 리마인더는 여러 개의 이미지를 가질 수 있습니다.
 *
 * @property id 이미지 고유 ID
 * @property reminderId 상위 리마인더 ID (외래키)
 * @property imageUri 이미지 URI (content:// 또는 file://)
 * @property createdAt 생성 일시
 */
@Entity(
    tableName = "reminder_images",
    foreignKeys = [
        ForeignKey(
            entity = ReminderEntity::class,
            parentColumns = ["id"],
            childColumns = ["reminderId"],
            onDelete = ForeignKey.CASCADE // 리마인더 삭제 시 이미지도 함께 삭제
        )
    ],
    indices = [
        Index(value = ["reminderId"]) // 리마인더별 조회 최적화
    ]
)
data class ReminderImage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val reminderId: Long,

    val imageUri: String,

    val createdAt: LocalDateTime = LocalDateTime.now()
)
