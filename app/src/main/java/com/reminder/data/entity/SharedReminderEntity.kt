package com.reminder.data.entity

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName
import com.reminder.sharing.Permission

/**
 * v1.36.0: 공유 리마인더 메타데이터
 *
 * Firestore 컬렉션: shared_reminders/{reminderId}
 *
 * 실제 리마인더 데이터는 로컬 Room DB에 저장되고,
 * 이 엔티티는 공유 메타데이터만 Firestore에 저장
 */
data class SharedReminderEntity(
    @DocumentId
    val id: String = "",

    /**
     * 원본 리마인더 ID (로컬 Room DB의 ID)
     */
    @PropertyName("reminder_id")
    val reminderId: Long = 0,

    /**
     * 소유자 사용자 ID
     */
    @PropertyName("owner_id")
    val ownerId: String = "",

    /**
     * 공유 생성 시간
     */
    @PropertyName("created_at")
    val createdAt: Long = System.currentTimeMillis(),

    /**
     * 마지막 수정 시간
     */
    @PropertyName("updated_at")
    val updatedAt: Long = System.currentTimeMillis(),

    /**
     * 마지막 수정한 사용자 ID
     */
    @PropertyName("last_modified_by")
    val lastModifiedBy: String? = null,

    /**
     * 공유 제목 (검색용)
     */
    @PropertyName("title")
    val title: String = "",

    /**
     * 공유 상태 (활성/비활성)
     */
    @PropertyName("is_active")
    val isActive: Boolean = true
) {
    /**
     * Firestore용 빈 생성자
     */
    constructor() : this(
        id = "",
        reminderId = 0,
        ownerId = "",
        createdAt = 0,
        updatedAt = 0,
        lastModifiedBy = null,
        title = "",
        isActive = true
    )

    /**
     * Map으로 변환 (Firestore 저장용)
     */
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "reminder_id" to reminderId,
            "owner_id" to ownerId,
            "created_at" to createdAt,
            "updated_at" to updatedAt,
            "last_modified_by" to lastModifiedBy,
            "title" to title,
            "is_active" to isActive
        )
    }
}

/**
 * 공유 리마인더 요약 정보 (UI 표시용)
 */
data class SharedReminderSummary(
    val reminderId: Long,
    val title: String,
    val ownerName: String,
    val permission: Permission,
    val collaboratorCount: Int,
    val lastModified: Long
)
