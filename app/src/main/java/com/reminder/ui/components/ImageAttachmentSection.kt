package com.reminder.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.reminder.data.entity.ReminderImage

/**
 * v1.68.2: 이미지 첨부 섹션 컴포넌트
 * AddEditReminderScreen에서 추출됨
 */
@Composable
fun ImageAttachmentSection(
    images: List<ReminderImage>,
    onAddImageClick: () -> Unit,
    onImageClick: (String) -> Unit,
    onImageDelete: (ReminderImage) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        HorizontalDivider()

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "이미지",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            FilledIconButton(
                onClick = onAddImageClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "이미지 추가",
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // 이미지 목록 (가로 스크롤)
        if (images.isNotEmpty()) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(images) { image ->
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(8.dp)
                            )
                    ) {
                        // Coil AsyncImage로 실제 이미지 표시
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(image.imageUri.toUri())
                                .crossfade(true)
                                .build(),
                            contentDescription = "첨부된 이미지",
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable { onImageClick(image.imageUri) },
                            contentScale = ContentScale.Crop,
                            placeholder = androidx.compose.ui.graphics.painter.ColorPainter(
                                MaterialTheme.colorScheme.surfaceVariant
                            ),
                            error = androidx.compose.ui.graphics.painter.ColorPainter(
                                MaterialTheme.colorScheme.errorContainer
                            )
                        )

                        // 삭제 버튼
                        IconButton(
                            onClick = { onImageDelete(image) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "이미지 삭제",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        } else {
            Text(
                text = "첨부된 이미지가 없습니다",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}
