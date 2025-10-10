package com.reminder.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * v1.40.0: 캘린더 동기화 설정 엔티티
 *
 * 기기 캘린더와의 동기화 설정을 저장합니다.
 */
@Entity(tableName = "calendar_sync_config")
data class CalendarSyncConfig(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /**
     * 캘린더 ID (기기 캘린더의 고유 ID)
     */
    val calendarId: String,

    /**
     * 캘린더 이름
     */
    val calendarName: String,

    /**
     * 캘린더 계정 이름
     */
    val accountName: String,

    /**
     * 동기화 활성화 여부
     */
    val isSyncEnabled: Boolean = true,

    /**
     * 동기화 방향 (ONE_WAY, TWO_WAY)
     */
    val syncDirection: SyncDirection = SyncDirection.TWO_WAY,

    /**
     * 캘린더 색상 (ARGB)
     */
    val calendarColor: Int,

    /**
     * 마지막 동기화 시간
     */
    val lastSyncedAt: LocalDateTime? = null,

    /**
     * 생성 일시
     */
    val createdAt: LocalDateTime = LocalDateTime.now()
)

/**
 * 동기화 방향 Enum
 */
enum class SyncDirection {
    ONE_WAY,    // 단방향 (리마인더 → 캘린더만)
    TWO_WAY     // 양방향 (리마인더 ↔ 캘린더)
}
