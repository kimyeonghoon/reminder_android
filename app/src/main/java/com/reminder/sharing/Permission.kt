package com.reminder.sharing

/**
 * v1.36.0: 협업 권한 레벨
 *
 * 권한 계층 구조: OWNER > EDITOR > VIEWER
 */
enum class Permission(val level: Int) {
    /**
     * 보기 전용 - 리마인더 조회만 가능
     */
    VIEWER(1),

    /**
     * 편집 권한 - 리마인더 조회 및 수정 가능
     */
    EDITOR(2),

    /**
     * 소유자 - 모든 권한 (삭제, 공유 관리 포함)
     */
    OWNER(3);

    /**
     * 요구된 권한 레벨 이상인지 확인
     */
    fun hasPermission(required: Permission): Boolean {
        return this.level >= required.level
    }
}

/**
 * 협업자 정보
 */
data class Collaborator(
    val userId: String,
    val permission: Permission,
    val sharedAt: Long = System.currentTimeMillis(),
    val sharedBy: String? = null
)
