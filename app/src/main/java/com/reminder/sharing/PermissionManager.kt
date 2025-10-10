package com.reminder.sharing

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * v1.36.0: 권한 관리 시스템
 *
 * Firestore 기반 협업 권한 관리
 * 컬렉션 구조: shared_reminders/{reminderId}/collaborators/{userId}
 */
class PermissionManager(
    private val firestore: FirebaseFirestore
) {

    companion object {
        private const val COLLECTION_SHARED_REMINDERS = "shared_reminders"
        private const val COLLECTION_COLLABORATORS = "collaborators"
    }

    /**
     * 사용자가 특정 권한을 가지고 있는지 확인
     *
     * @param userId 사용자 ID
     * @param reminderId 리마인더 ID
     * @param required 요구되는 권한 레벨
     * @return 권한 보유 여부
     */
    suspend fun hasPermission(
        userId: String,
        reminderId: String,
        required: Permission
    ): Boolean {
        return try {
            val doc = firestore
                .collection(COLLECTION_SHARED_REMINDERS)
                .document(reminderId)
                .collection(COLLECTION_COLLABORATORS)
                .document(userId)
                .get()
                .await()

            if (!doc.exists()) {
                return false
            }

            val permissionStr = doc.getString("permission") ?: return false
            val userPermission = Permission.valueOf(permissionStr)

            userPermission.hasPermission(required)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 권한 부여
     *
     * @param userId 권한을 부여할 사용자 ID
     * @param reminderId 리마인더 ID
     * @param permission 부여할 권한
     * @param grantedBy 권한을 부여한 사용자 ID (optional)
     */
    suspend fun grantPermission(
        userId: String,
        reminderId: String,
        permission: Permission,
        grantedBy: String? = null
    ) {
        val data = hashMapOf(
            "permission" to permission.name,
            "sharedAt" to System.currentTimeMillis(),
            "sharedBy" to grantedBy
        )

        firestore
            .collection(COLLECTION_SHARED_REMINDERS)
            .document(reminderId)
            .collection(COLLECTION_COLLABORATORS)
            .document(userId)
            .set(data)
            .await()
    }

    /**
     * 권한 회수
     *
     * @param userId 권한을 회수할 사용자 ID
     * @param reminderId 리마인더 ID
     */
    suspend fun revokePermission(
        userId: String,
        reminderId: String
    ) {
        firestore
            .collection(COLLECTION_SHARED_REMINDERS)
            .document(reminderId)
            .collection(COLLECTION_COLLABORATORS)
            .document(userId)
            .delete()
            .await()
    }

    /**
     * 특정 리마인더의 모든 협업자 조회
     *
     * @param reminderId 리마인더 ID
     * @return 협업자 목록
     */
    suspend fun getCollaborators(reminderId: String): List<Collaborator> {
        return try {
            val snapshot = firestore
                .collection(COLLECTION_SHARED_REMINDERS)
                .document(reminderId)
                .collection(COLLECTION_COLLABORATORS)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                val permissionStr = doc.getString("permission") ?: return@mapNotNull null
                val permission = try {
                    Permission.valueOf(permissionStr)
                } catch (e: Exception) {
                    return@mapNotNull null
                }

                Collaborator(
                    userId = doc.id,
                    permission = permission,
                    sharedAt = doc.getLong("sharedAt") ?: System.currentTimeMillis(),
                    sharedBy = doc.getString("sharedBy")
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * 사용자가 접근 가능한 모든 공유 리마인더 ID 조회
     *
     * @param userId 사용자 ID
     * @return 공유 리마인더 ID 목록
     */
    suspend fun getSharedReminders(userId: String): List<String> {
        return try {
            val snapshot = firestore
                .collectionGroup(COLLECTION_COLLABORATORS)
                .whereEqualTo("__name__", userId) // Document ID로 검색
                .get()
                .await()

            // 각 협업자 문서의 부모 리마인더 ID 추출
            snapshot.documents.mapNotNull { doc ->
                doc.reference.parent.parent?.id
            }.distinct()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * 사용자가 권한을 수정할 수 있는지 확인 (OWNER만 가능)
     *
     * @param userId 사용자 ID
     * @param reminderId 리마인더 ID
     * @return 권한 수정 가능 여부
     */
    suspend fun canModifyPermissions(
        userId: String,
        reminderId: String
    ): Boolean {
        return hasPermission(userId, reminderId, Permission.OWNER)
    }

    /**
     * 리마인더 소유자 변경
     *
     * @param reminderId 리마인더 ID
     * @param currentOwnerId 현재 소유자 ID
     * @param newOwnerId 새 소유자 ID
     */
    suspend fun transferOwnership(
        reminderId: String,
        currentOwnerId: String,
        newOwnerId: String
    ): Result<Unit> {
        return try {
            // 현재 사용자가 OWNER인지 확인
            if (!hasPermission(currentOwnerId, reminderId, Permission.OWNER)) {
                return Result.failure(SecurityException("Only owner can transfer ownership"))
            }

            // 새 소유자를 OWNER로 변경
            grantPermission(newOwnerId, reminderId, Permission.OWNER, currentOwnerId)

            // 이전 소유자를 EDITOR로 강등
            grantPermission(currentOwnerId, reminderId, Permission.EDITOR, newOwnerId)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
