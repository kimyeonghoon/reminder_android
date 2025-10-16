package com.reminder.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.reminder.data.entity.ReminderEntity
import com.reminder.domain.Quadrant
import com.reminder.domain.countByQuadrant
import com.reminder.domain.filterByQuadrant
import com.reminder.ui.components.QuadrantCard
import com.reminder.ui.components.TrendAnalysisDialog
import com.reminder.viewmodel.ReminderViewModel

/**
 * v1.47.0: Eisenhower Matrix 화면
 * v1.68.3: 컴포넌트 분리 (QuadrantCard, TrendAnalysisDialog)
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
