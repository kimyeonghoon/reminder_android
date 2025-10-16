package com.reminder.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.reminder.data.entity.ReminderEntity
import com.reminder.domain.Quadrant
import com.reminder.domain.QuadrantStats
import com.reminder.domain.calculateQuadrantStats
import com.reminder.domain.getInfo
import com.reminder.utils.rememberHapticFeedback
import com.reminder.viewmodel.ReminderViewModel
import kotlin.math.roundToInt

/**
 * v1.68.3: QuadrantCard 컴포넌트 (EisenhowerMatrixScreen에서 분리)
 *
 * 쿼드런트 카드 및 관련 컴포넌트
 */
@Composable
fun QuadrantCard(
    quadrant: Quadrant,
    reminders: List<ReminderEntity>,
    count: Int,
    viewModel: ReminderViewModel,
    onReminderClick: (ReminderEntity) -> Unit,
    onNavigateToFocusMode: () -> Unit = {}, // v1.51.0
    modifier: Modifier = Modifier
) {
    val info = quadrant.getInfo()
    val allReminders by viewModel.allReminders.collectAsStateWithLifecycle()

    // v1.49.0: 통계 계산
    val stats = remember(allReminders) {
        allReminders.calculateQuadrantStats(quadrant)
    }

    Card(
        modifier = modifier
            .fillMaxHeight(),
        colors = CardDefaults.cardColors(
            containerColor = Color(info.color).copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // 헤더
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(info.color).copy(alpha = 0.2f))
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = info.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(info.color)
                    )
                    Badge(
                        containerColor = Color(info.color)
                    ) {
                        Text(
                            text = count.toString(),
                            color = Color.White
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = info.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // v1.49.0: 통계 표시
                if (stats.totalCount > 0) {
                    Spacer(modifier = Modifier.height(6.dp))
                    QuadrantStatisticsRow(stats)
                }

                // v1.51.0: DO_FIRST 쿼드런트에 포커스 모드 시작 버튼 추가
                if (quadrant == Quadrant.DO_FIRST && count > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onNavigateToFocusMode,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(info.color)
                        )
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("집중 시작", color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 리마인더 리스트
            if (reminders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "없음",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = reminders,
                        key = { it.id }
                    ) { reminder ->
                        QuadrantReminderCard(
                            reminder = reminder,
                            currentQuadrant = quadrant,
                            viewModel = viewModel,
                            onClick = { onReminderClick(reminder) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

/**
 * v1.50.0: 쿼드런트 전용 리마인더 카드 (이동 메뉴 포함)
 * v1.53.0: Long Press로 이동 메뉴 빠른 열기
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun QuadrantReminderCard(
    reminder: ReminderEntity,
    currentQuadrant: Quadrant,
    viewModel: ReminderViewModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = rememberHapticFeedback()
    var showMoveMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.combinedClickable(
            onClick = onClick,
            onLongClick = {
                haptic.longPress()
                showMoveMenu = true
            }
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 체크박스
            Checkbox(
                checked = reminder.isCompleted,
                onCheckedChange = { viewModel.toggleReminderCompletion(reminder) }
            )

            // 제목
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    text = reminder.title,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2
                )
            }

            // 이동 메뉴 버튼
            Box {
                IconButton(onClick = { showMoveMenu = true }) {
                    Icon(Icons.Default.MoreVert, "이동")
                }

                DropdownMenu(
                    expanded = showMoveMenu,
                    onDismissRequest = { showMoveMenu = false }
                ) {
                    Quadrant.entries.filter { it != currentQuadrant }.forEach { targetQuadrant ->
                        DropdownMenuItem(
                            text = { Text(targetQuadrant.getInfo().title) },
                            onClick = {
                                viewModel.moveReminderToQuadrant(reminder, targetQuadrant)
                                showMoveMenu = false
                            },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .background(
                                            Color(targetQuadrant.getInfo().color),
                                            RoundedCornerShape(4.dp)
                                        )
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * v1.49.0: 쿼드런트 통계 표시
 */
@Composable
private fun QuadrantStatisticsRow(stats: QuadrantStats) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 완료율
        QuadrantStatisticChip(
            label = "완료",
            value = "${stats.completionRate.roundToInt()}%"
        )

        // 평균 처리 시간
        if (stats.averageCompletionMinutes > 0) {
            val timeText = when {
                stats.averageCompletionMinutes < 60 -> "${stats.averageCompletionMinutes.roundToInt()}분"
                stats.averageCompletionMinutes < 1440 -> "${(stats.averageCompletionMinutes / 60).roundToInt()}시간"
                else -> "${(stats.averageCompletionMinutes / 1440).roundToInt()}일"
            }
            QuadrantStatisticChip(
                label = "평균",
                value = timeText
            )
        }
    }
}

/**
 * v1.49.0: 통계 칩
 */
@Composable
private fun QuadrantStatisticChip(
    label: String,
    value: String
) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
