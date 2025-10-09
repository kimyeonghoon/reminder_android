package com.reminder.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.reminder.data.entity.SubTask

/**
 * 서브태스크 아이템 컴포넌트
 *
 * @param subTask 서브태스크 데이터
 * @param onCheckedChange 체크박스 상태 변경 콜백
 * @param onDelete 삭제 버튼 클릭 콜백
 * @param modifier Modifier
 */
@Composable
fun SubTaskItem(
    subTask: SubTask,
    onCheckedChange: (Boolean) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 드래그 핸들 (향후 재정렬 기능용)
        Icon(
            imageVector = Icons.Default.DragHandle,
            contentDescription = "순서 변경",
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(20.dp)
        )

        // 완료 체크박스
        Checkbox(
            checked = subTask.isCompleted,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.semantics {
                contentDescription = "서브태스크 완료 여부"
            }
        )

        // 제목
        Text(
            text = subTask.title,
            style = MaterialTheme.typography.bodyMedium,
            textDecoration = if (subTask.isCompleted) TextDecoration.LineThrough else null,
            color = if (subTask.isCompleted) {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            modifier = Modifier.weight(1f)
        )

        // 삭제 버튼
        IconButton(
            onClick = onDelete,
            modifier = Modifier
                .size(36.dp)
                .semantics {
                    contentDescription = "서브태스크 삭제"
                }
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
