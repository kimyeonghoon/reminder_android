package com.reminder.data.entity

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

/**
 * v1.36.0: 공유 리스트/프로젝트
 *
 * Firestore 컬렉션: shared_lists/{listId}
 *
 * 여러 리마인더를 그룹화하여 공유할 수 있는 리스트
 */
data class SharedListEntity(
    @DocumentId
    val id: String = "",

    /**
     * 리스트 이름
     */
    @PropertyName("name")
    val name: String = "",

    /**
     * 리스트 설명
     */
    @PropertyName("description")
    val description: String = "",

    /**
     * 소유자 사용자 ID
     */
    @PropertyName("owner_id")
    val ownerId: String = "",

    /**
     * 리스트 색상 (테마)
     */
    @PropertyName("color")
    val color: String = "#4CAF50",

    /**
     * 리스트 아이콘
     */
    @PropertyName("icon")
    val icon: String = "list",

    /**
     * 포함된 리마인더 ID 목록
     */
    @PropertyName("reminder_ids")
    val reminderIds: List<Long> = emptyList(),

    /**
     * 생성 시간
     */
    @PropertyName("created_at")
    val createdAt: Long = System.currentTimeMillis(),

    /**
     * 마지막 수정 시간
     */
    @PropertyName("updated_at")
    val updatedAt: Long = System.currentTimeMillis(),

    /**
     * 활성 상태
     */
    @PropertyName("is_active")
    val isActive: Boolean = true,

    /**
     * 정렬 순서
     */
    @PropertyName("order")
    val order: Int = 0
) {
    /**
     * Firestore용 빈 생성자
     */
    constructor() : this(
        id = "",
        name = "",
        description = "",
        ownerId = "",
        color = "#4CAF50",
        icon = "list",
        reminderIds = emptyList(),
        createdAt = 0,
        updatedAt = 0,
        isActive = true,
        order = 0
    )

    /**
     * Map으로 변환 (Firestore 저장용)
     */
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "name" to name,
            "description" to description,
            "owner_id" to ownerId,
            "color" to color,
            "icon" to icon,
            "reminder_ids" to reminderIds,
            "created_at" to createdAt,
            "updated_at" to updatedAt,
            "is_active" to isActive,
            "order" to order
        )
    }
}

/**
 * 공유 리스트 멤버 정보
 */
data class SharedListMember(
    val userId: String,
    val userName: String,
    val userEmail: String,
    val permission: com.reminder.sharing.Permission,
    val joinedAt: Long
)

/**
 * 공유 리스트 요약 정보 (UI 표시용)
 */
data class SharedListSummary(
    val listId: String,
    val name: String,
    val description: String,
    val color: String,
    val icon: String,
    val ownerName: String,
    val memberCount: Int,
    val reminderCount: Int,
    val lastModified: Long
)
