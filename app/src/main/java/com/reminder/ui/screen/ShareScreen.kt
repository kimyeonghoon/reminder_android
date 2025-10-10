package com.reminder.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.reminder.sharing.Collaborator
import com.reminder.sharing.Permission

/**
 * v1.36.0: 리마인더 공유 화면
 *
 * 사용자를 초대하고 권한을 관리하는 화면
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareScreen(
    reminderId: Long,
    currentUserId: String,
    collaborators: List<Collaborator>,
    onInviteClick: (String, Permission) -> Unit,
    onPermissionChange: (Collaborator, Permission) -> Unit,
    onRemoveCollaborator: (Collaborator) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showInviteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("공유 관리") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, "닫기")
                    }
                },
                actions = {
                    IconButton(onClick = { showInviteDialog = true }) {
                        Icon(Icons.Default.PersonAdd, "사용자 초대")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 안내 카드
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "이메일로 사용자를 초대하고 권한을 설정하세요",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // 협업자 목록
            item {
                Text(
                    text = "협업자 (${collaborators.size}명)",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(collaborators) { collaborator ->
                CollaboratorCard(
                    collaborator = collaborator,
                    isCurrentUser = collaborator.userId == currentUserId,
                    onPermissionChange = { newPermission ->
                        onPermissionChange(collaborator, newPermission)
                    },
                    onRemove = {
                        onRemoveCollaborator(collaborator)
                    }
                )
            }

            // 빈 상태
            if (collaborators.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAddAlt,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "아직 협업자가 없습니다",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "+ 버튼을 눌러 사용자를 초대하세요",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    // 초대 다이얼로그
    if (showInviteDialog) {
        InviteDialog(
            onDismiss = { showInviteDialog = false },
            onInvite = { email, permission ->
                onInviteClick(email, permission)
                showInviteDialog = false
            }
        )
    }
}

/**
 * 협업자 카드
 */
@Composable
private fun CollaboratorCard(
    collaborator: Collaborator,
    isCurrentUser: Boolean,
    onPermissionChange: (Permission) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showPermissionMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 사용자 아이콘
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(16.dp))

            // 사용자 정보
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = collaborator.userId + if (isCurrentUser) " (나)" else "",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = getPermissionLabel(collaborator.permission),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 권한 변경 버튼
            if (!isCurrentUser && collaborator.permission != Permission.OWNER) {
                IconButton(onClick = { showPermissionMenu = true }) {
                    Icon(Icons.Default.MoreVert, "옵션")
                }

                DropdownMenu(
                    expanded = showPermissionMenu,
                    onDismissRequest = { showPermissionMenu = false }
                ) {
                    Permission.entries.forEach { permission ->
                        DropdownMenuItem(
                            text = { Text(getPermissionLabel(permission)) },
                            onClick = {
                                onPermissionChange(permission)
                                showPermissionMenu = false
                            }
                        )
                    }
                    Divider()
                    DropdownMenuItem(
                        text = {
                            Text(
                                "제거",
                                color = MaterialTheme.colorScheme.error
                            )
                        },
                        onClick = {
                            onRemove()
                            showPermissionMenu = false
                        }
                    )
                }
            }
        }
    }
}

/**
 * 사용자 초대 다이얼로그
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InviteDialog(
    onDismiss: () -> Unit,
    onInvite: (String, Permission) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var selectedPermission by remember { mutableStateOf(Permission.VIEWER) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("사용자 초대") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("이메일") },
                    placeholder = { Text("user@example.com") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "권한 선택",
                    style = MaterialTheme.typography.labelMedium
                )

                Permission.entries.forEach { permission ->
                    FilterChip(
                        selected = selectedPermission == permission,
                        onClick = { selectedPermission = permission },
                        label = {
                            Column {
                                Text(getPermissionLabel(permission))
                                Text(
                                    text = getPermissionDescription(permission),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onInvite(email, selectedPermission) },
                enabled = email.isNotBlank()
            ) {
                Text("초대")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}

/**
 * 권한 레이블 반환
 */
private fun getPermissionLabel(permission: Permission): String {
    return when (permission) {
        Permission.OWNER -> "소유자"
        Permission.EDITOR -> "편집 가능"
        Permission.VIEWER -> "보기 전용"
    }
}

/**
 * 권한 설명 반환
 */
private fun getPermissionDescription(permission: Permission): String {
    return when (permission) {
        Permission.OWNER -> "모든 권한 (삭제, 공유 관리 포함)"
        Permission.EDITOR -> "리마인더 조회 및 수정"
        Permission.VIEWER -> "리마인더 조회만 가능"
    }
}
