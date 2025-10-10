package com.reminder.sharing

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

/**
 * v1.36.0: PermissionManager 테스트
 *
 * TDD Red Phase - 테스트 먼저 작성
 */
class PermissionManagerTest {

    @Mock
    private lateinit var firestore: FirebaseFirestore

    private lateinit var permissionManager: PermissionManager

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        permissionManager = PermissionManager(firestore)
    }

    @Test
    fun `권한 확인 - OWNER는 모든 권한 보유`() = runTest {
        // Given
        val userId = "user1"
        val reminderId = "reminder1"
        // Owner로 설정된 상태 가정

        // When
        val hasOwnerPermission = permissionManager.hasPermission(userId, reminderId, Permission.OWNER)
        val hasEditorPermission = permissionManager.hasPermission(userId, reminderId, Permission.EDITOR)
        val hasViewerPermission = permissionManager.hasPermission(userId, reminderId, Permission.VIEWER)

        // Then
        assertTrue(hasOwnerPermission)
        assertTrue(hasEditorPermission)
        assertTrue(hasViewerPermission)
    }

    @Test
    fun `권한 확인 - EDITOR는 편집 및 보기 권한 보유`() = runTest {
        // Given
        val userId = "user2"
        val reminderId = "reminder1"
        // Editor로 설정된 상태 가정

        // When
        val hasOwnerPermission = permissionManager.hasPermission(userId, reminderId, Permission.OWNER)
        val hasEditorPermission = permissionManager.hasPermission(userId, reminderId, Permission.EDITOR)
        val hasViewerPermission = permissionManager.hasPermission(userId, reminderId, Permission.VIEWER)

        // Then
        assertFalse(hasOwnerPermission)
        assertTrue(hasEditorPermission)
        assertTrue(hasViewerPermission)
    }

    @Test
    fun `권한 확인 - VIEWER는 보기 권한만 보유`() = runTest {
        // Given
        val userId = "user3"
        val reminderId = "reminder1"
        // Viewer로 설정된 상태 가정

        // When
        val hasOwnerPermission = permissionManager.hasPermission(userId, reminderId, Permission.OWNER)
        val hasEditorPermission = permissionManager.hasPermission(userId, reminderId, Permission.EDITOR)
        val hasViewerPermission = permissionManager.hasPermission(userId, reminderId, Permission.VIEWER)

        // Then
        assertFalse(hasOwnerPermission)
        assertFalse(hasEditorPermission)
        assertTrue(hasViewerPermission)
    }

    @Test
    fun `권한 부여 - 새 사용자에게 EDITOR 권한 부여`() = runTest {
        // Given
        val userId = "user4"
        val reminderId = "reminder1"

        // When
        permissionManager.grantPermission(userId, reminderId, Permission.EDITOR)
        val hasPermission = permissionManager.hasPermission(userId, reminderId, Permission.EDITOR)

        // Then
        assertTrue(hasPermission)
    }

    @Test
    fun `권한 부여 - 기존 VIEWER를 EDITOR로 승격`() = runTest {
        // Given
        val userId = "user5"
        val reminderId = "reminder1"
        permissionManager.grantPermission(userId, reminderId, Permission.VIEWER)

        // When
        permissionManager.grantPermission(userId, reminderId, Permission.EDITOR)
        val hasEditorPermission = permissionManager.hasPermission(userId, reminderId, Permission.EDITOR)

        // Then
        assertTrue(hasEditorPermission)
    }

    @Test
    fun `권한 회수 - EDITOR 권한 제거`() = runTest {
        // Given
        val userId = "user6"
        val reminderId = "reminder1"
        permissionManager.grantPermission(userId, reminderId, Permission.EDITOR)

        // When
        permissionManager.revokePermission(userId, reminderId)
        val hasPermission = permissionManager.hasPermission(userId, reminderId, Permission.VIEWER)

        // Then
        assertFalse(hasPermission)
    }

    @Test
    fun `특정 리마인더의 모든 협업자 조회`() = runTest {
        // Given
        val reminderId = "reminder1"
        permissionManager.grantPermission("user1", reminderId, Permission.OWNER)
        permissionManager.grantPermission("user2", reminderId, Permission.EDITOR)
        permissionManager.grantPermission("user3", reminderId, Permission.VIEWER)

        // When
        val collaborators = permissionManager.getCollaborators(reminderId)

        // Then
        assertEquals(3, collaborators.size)
        assertTrue(collaborators.any { it.userId == "user1" && it.permission == Permission.OWNER })
        assertTrue(collaborators.any { it.userId == "user2" && it.permission == Permission.EDITOR })
        assertTrue(collaborators.any { it.userId == "user3" && it.permission == Permission.VIEWER })
    }

    @Test
    fun `사용자가 접근 가능한 모든 공유 리마인더 조회`() = runTest {
        // Given
        val userId = "user1"
        permissionManager.grantPermission(userId, "reminder1", Permission.OWNER)
        permissionManager.grantPermission(userId, "reminder2", Permission.EDITOR)
        permissionManager.grantPermission(userId, "reminder3", Permission.VIEWER)

        // When
        val sharedReminders = permissionManager.getSharedReminders(userId)

        // Then
        assertEquals(3, sharedReminders.size)
        assertTrue(sharedReminders.contains("reminder1"))
        assertTrue(sharedReminders.contains("reminder2"))
        assertTrue(sharedReminders.contains("reminder3"))
    }

    @Test
    fun `권한 없는 사용자는 접근 불가`() = runTest {
        // Given
        val userId = "unauthorized_user"
        val reminderId = "reminder1"

        // When
        val hasPermission = permissionManager.hasPermission(userId, reminderId, Permission.VIEWER)

        // Then
        assertFalse(hasPermission)
    }

    @Test
    fun `OWNER만 협업자 권한 변경 가능`() = runTest {
        // Given
        val ownerId = "owner"
        val editorId = "editor"
        val reminderId = "reminder1"
        permissionManager.grantPermission(ownerId, reminderId, Permission.OWNER)
        permissionManager.grantPermission(editorId, reminderId, Permission.EDITOR)

        // When
        val ownerCanModify = permissionManager.canModifyPermissions(ownerId, reminderId)
        val editorCanModify = permissionManager.canModifyPermissions(editorId, reminderId)

        // Then
        assertTrue(ownerCanModify)
        assertFalse(editorCanModify)
    }
}
