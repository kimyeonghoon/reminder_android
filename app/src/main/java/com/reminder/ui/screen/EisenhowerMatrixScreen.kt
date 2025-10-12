package com.reminder.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
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
import com.reminder.domain.TrendPeriod
import com.reminder.domain.calculateQuadrantStats
import com.reminder.domain.calculateQuadrantTrend
import com.reminder.domain.calculateTimeDistribution
import com.reminder.domain.countByQuadrant
import com.reminder.domain.filterByQuadrant
import com.reminder.domain.getInfo
import com.reminder.domain.getQuadrant
import com.reminder.ui.components.ReminderCard
import com.reminder.util.rememberHapticFeedback
import com.reminder.viewmodel.ReminderViewModel
import kotlin.math.roundToInt

/**
 * v1.47.0: Eisenhower Matrix 화면
 *
 * 리마인더를 중요도(Priority)와 긴급도(Urgency)에 따라
 * 4개의 쿼드런트로 분류하여 보여주는 화면
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EisenhowerMatrixScreen(
    viewModel: ReminderViewModel,
    onNavigateBack: () -> Unit,
    onReminderClick: (ReminderEntity) -> Unit,
    onNavigateToFocusMode: () -> Unit = {}, // v1.51.0: 포커스 모드로 이동
    modifier: Modifier = Modifier
) {
    val allReminders by viewModel.allReminders.collectAsStateWithLifecycle()

    // 완료되지 않은 리마인더만 표시
    val activeReminders = remember(allReminders) {
        allReminders.filter { !it.isCompleted && !it.isArchived }
    }

    // 쿼드런트별 개수 계산
    val quadrantCounts = remember(activeReminders) {
        activeReminders.countByQuadrant()
    }

    var showTrendDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("아이젠하워 매트릭스") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "뒤로가기")
                    }
                },
                actions = {
                    IconButton(onClick = { showTrendDialog = true }) {
                        Icon(Icons.AutoMirrored.Filled.TrendingUp, "트렌드 분석")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 안내 카드
            InfoCard()

            // 2x2 그리드
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 상단 행: Q1 (DO_FIRST) | Q2 (SCHEDULE)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuadrantCard(
                        quadrant = Quadrant.DO_FIRST,
                        reminders = activeReminders.filterByQuadrant(Quadrant.DO_FIRST),
                        count = quadrantCounts[Quadrant.DO_FIRST] ?: 0,
                        viewModel = viewModel,
                        onReminderClick = onReminderClick,
                        onNavigateToFocusMode = onNavigateToFocusMode, // v1.51.0
                        modifier = Modifier.weight(1f)
                    )

                    QuadrantCard(
                        quadrant = Quadrant.SCHEDULE,
                        reminders = activeReminders.filterByQuadrant(Quadrant.SCHEDULE),
                        count = quadrantCounts[Quadrant.SCHEDULE] ?: 0,
                        viewModel = viewModel,
                        onReminderClick = onReminderClick,
                        modifier = Modifier.weight(1f)
                    )
                }

                // 하단 행: Q3 (DELEGATE) | Q4 (DELETE)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuadrantCard(
                        quadrant = Quadrant.DELEGATE,
                        reminders = activeReminders.filterByQuadrant(Quadrant.DELEGATE),
                        count = quadrantCounts[Quadrant.DELEGATE] ?: 0,
                        viewModel = viewModel,
                        onReminderClick = onReminderClick,
                        modifier = Modifier.weight(1f)
                    )

                    QuadrantCard(
                        quadrant = Quadrant.DELETE,
                        reminders = activeReminders.filterByQuadrant(Quadrant.DELETE),
                        count = quadrantCounts[Quadrant.DELETE] ?: 0,
                        viewModel = viewModel,
                        onReminderClick = onReminderClick,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // v1.50.0: 트렌드 분석 다이얼로그
        if (showTrendDialog) {
            TrendAnalysisDialog(
                allReminders = allReminders,
                onDismiss = { showTrendDialog = false }
            )
        }
    }
}

/**
 * 안내 카드
 */
@Composable
private fun InfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "생산성 향상을 위한 아이젠하워 매트릭스",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "중요도와 긴급도에 따라 작업을 분류하여 효율적으로 관리하세요.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

/**
 * 쿼드런트 카드
 */
@Composable
private fun QuadrantCard(
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
                    StatisticsRow(stats)
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
private fun StatisticsRow(stats: QuadrantStats) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 완료율
        StatisticChip(
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
            StatisticChip(
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
private fun StatisticChip(
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

/**
 * v1.50.0: 트렌드 분석 다이얼로그
 */
@Composable
private fun TrendAnalysisDialog(
    allReminders: List<ReminderEntity>,
    onDismiss: () -> Unit
) {
    var selectedQuadrant by remember { mutableStateOf(Quadrant.DO_FIRST) }
    var selectedPeriod by remember { mutableStateOf(TrendPeriod.WEEKLY) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("쿼드런트 트렌드 분석") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 쿼드런트 선택
                Text("쿼드런트", style = MaterialTheme.typography.titleSmall)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Quadrant.entries.forEach { quadrant ->
                        FilterChip(
                            selected = selectedQuadrant == quadrant,
                            onClick = { selectedQuadrant = quadrant },
                            label = { Text(quadrant.getInfo().title, style = MaterialTheme.typography.labelSmall) },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(
                                            Color(quadrant.getInfo().color),
                                            RoundedCornerShape(2.dp)
                                        )
                                )
                            }
                        )
                    }
                }

                // 기간 선택
                Text("기간", style = MaterialTheme.typography.titleSmall)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedPeriod == TrendPeriod.WEEKLY,
                        onClick = { selectedPeriod = TrendPeriod.WEEKLY },
                        label = { Text("주간") }
                    )
                    FilterChip(
                        selected = selectedPeriod == TrendPeriod.MONTHLY,
                        onClick = { selectedPeriod = TrendPeriod.MONTHLY },
                        label = { Text("월간") }
                    )
                }

                HorizontalDivider()

                // 트렌드 차트
                val trend = remember(allReminders, selectedQuadrant, selectedPeriod) {
                    allReminders.calculateQuadrantTrend(selectedQuadrant, selectedPeriod)
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "완료 트렌드 (최근 ${selectedPeriod.days}일)",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "총 완료: ${trend.totalCompleted}개",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // 간단한 막대 차트
                    SimpleTrendChart(
                        trend = trend,
                        quadrantColor = Color(selectedQuadrant.getInfo().color)
                    )
                }

                HorizontalDivider()

                // 시간대별 분포
                val timeDistribution = remember(allReminders, selectedQuadrant) {
                    allReminders.calculateTimeDistribution(selectedQuadrant)
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "시간대별 완료 분포",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TimeDistributionItem("오전", timeDistribution.morning)
                        TimeDistributionItem("오후", timeDistribution.afternoon)
                        TimeDistributionItem("저녁", timeDistribution.evening)
                        TimeDistributionItem("심야", timeDistribution.night)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("닫기")
            }
        }
    )
}

/**
 * v1.50.0: 간단한 트렌드 차트
 */
@Composable
private fun SimpleTrendChart(
    trend: com.reminder.domain.QuadrantTrend,
    quadrantColor: Color
) {
    val maxCount = trend.dataPoints.maxOfOrNull { it.count } ?: 1
    val showCount = 7 // 최근 7개만 표시

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        trend.dataPoints.take(showCount).forEach { dataPoint ->
            val heightFraction = if (maxCount > 0) dataPoint.count.toFloat() / maxCount else 0f

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (dataPoint.count > 0) {
                    Text(
                        text = "${dataPoint.count}",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(heightFraction.coerceAtLeast(0.1f))
                        .background(quadrantColor, RoundedCornerShape(4.dp))
                )
            }
        }
    }
}

/**
 * v1.50.0: 시간대별 분포 아이템
 */
@Composable
private fun TimeDistributionItem(
    label: String,
    count: Int
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
