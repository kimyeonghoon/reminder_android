package com.reminder.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.reminder.data.entity.GoalEntity
import com.reminder.data.entity.GoalType
import com.reminder.goal.GoalProgress
import com.reminder.ui.components.GoalProgressCard
import com.reminder.viewmodel.StatisticsViewModel
import java.time.LocalDate

/**
 * v1.33.0: 목표 설정 화면
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalSettingScreen(
    viewModel: StatisticsViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeGoals by viewModel.activeGoals.collectAsState()
    var showAddGoalDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("목표 관리") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddGoalDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "목표 추가")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (activeGoals.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "설정된 목표가 없습니다.\n+ 버튼을 눌러 목표를 추가하세요.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(activeGoals) { goal ->
                    val progress by viewModel.getGoalProgress(goal).collectAsState()
                    GoalProgressCard(
                        goal = goal,
                        progress = progress
                    )
                }
            }
        }
    }

    // 목표 추가 다이얼로그
    if (showAddGoalDialog) {
        AddGoalDialog(
            onDismiss = { showAddGoalDialog = false },
            onConfirm = { goal ->
                viewModel.addGoal(goal)
                showAddGoalDialog = false
            }
        )
    }
}

/**
 * 목표 추가 다이얼로그
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddGoalDialog(
    onDismiss: () -> Unit,
    onConfirm: (GoalEntity) -> Unit
) {
    var selectedType by remember { mutableStateOf(GoalType.DAILY) }
    var targetCount by remember { mutableStateOf("5") }
    var category by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("새 목표 추가") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 목표 타입 선택
                Text("목표 타입", style = MaterialTheme.typography.labelMedium)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GoalType.entries.forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = {
                                Text(
                                    when (type) {
                                        GoalType.DAILY -> "일일"
                                        GoalType.WEEKLY -> "주간"
                                        GoalType.MONTHLY -> "월간"
                                    }
                                )
                            }
                        )
                    }
                }

                // 목표 개수
                OutlinedTextField(
                    value = targetCount,
                    onValueChange = { targetCount = it.filter { char -> char.isDigit() } },
                    label = { Text("목표 개수") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // 카테고리 (선택사항)
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("카테고리 (선택사항)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val target = targetCount.toIntOrNull() ?: return@TextButton
                    if (target <= 0) return@TextButton

                    val (startDate, endDate) = when (selectedType) {
                        GoalType.DAILY -> {
                            val today = LocalDate.now()
                            today to today
                        }
                        GoalType.WEEKLY -> {
                            val today = LocalDate.now()
                            val startOfWeek = today.minusDays(today.dayOfWeek.value.toLong() - 1)
                            val endOfWeek = startOfWeek.plusDays(6)
                            startOfWeek to endOfWeek
                        }
                        GoalType.MONTHLY -> {
                            val today = LocalDate.now()
                            val startOfMonth = today.withDayOfMonth(1)
                            val endOfMonth = today.withDayOfMonth(today.lengthOfMonth())
                            startOfMonth to endOfMonth
                        }
                    }

                    val goal = GoalEntity(
                        type = selectedType,
                        targetCount = target,
                        category = category.takeIf { it.isNotBlank() },
                        startDate = startDate,
                        endDate = endDate
                    )
                    onConfirm(goal)
                }
            ) {
                Text("추가")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}
