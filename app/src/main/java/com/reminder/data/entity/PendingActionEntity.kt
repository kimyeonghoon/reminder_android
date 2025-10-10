package com.reminder.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * 오프라인 작업 큐 엔티티
 *
 * 네트워크가 없을 때 발생한 CRUD 작업을 저장하고,
 * 네트워크 복구 시 Firebase와 동기화합니다.
 */
@Entity(tableName = "pending_actions")
data class PendingActionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /**
     * 작업 대상 리마인더 ID
     */
    val reminderId: Long,

    /**
     * 작업 타입 (INSERT, UPDATE, DELETE)
     */
    val actionType: ActionType,

    /**
     * 작업 생성 시간
     */
    val createdAt: LocalDateTime = LocalDateTime.now(),

    /**
     * 재시도 횟수
     */
    val retryCount: Int = 0,

    /**
     * 마지막 재시도 시간
     */
    val lastRetryAt: LocalDateTime? = null,

    /**
     * 작업 실패 에러 메시지
     */
    val errorMessage: String? = null
)

/**
 * 작업 타입 Enum
 */
enum class ActionType {
    INSERT,    // 새 리마인더 생성
    UPDATE,    // 기존 리마인더 수정
    DELETE     // 리마인더 삭제
}
