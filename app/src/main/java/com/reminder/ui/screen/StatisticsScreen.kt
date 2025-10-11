package com.reminder.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.reminder.data.entity.Statistics
import com.reminder.ui.components.TrendChart
import com.reminder.viewmodel.StatisticsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel,
    onCompletionHistoryClick: () -> Unit = {},
    onPatternAnalysisClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val statistics by viewModel.statistics.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("통계") }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 전체 통계 카드
            OverallStatisticsCard(statistics = statistics)

            // 완료 이력 달력 버튼
            Button(
                onClick = onCompletionHistoryClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("📅 완료 이력 달력 보기")
            }

            // v1.26.0: 완료 패턴 분석 버튼
            OutlinedButton(
                onClick = onPatternAnalysisClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("📊 완료 패턴 분석 보기")
            }

            // 완료율 카드
            CompletionRateCard(statistics = statistics)

            // v1.28.0: 주간 트렌드 차트
            WeeklyTrendCard(statistics = statistics)

            // v1.28.0: 월간 트렌드 차트
            MonthlyTrendCard(statistics = statistics)

            // 우선순위별 분포 차트
            PriorityDistributionChart(statistics = statistics)

            // 카테고리별 분포 차트
            if (statistics.categoryDistribution.isNotEmpty()) {
                CategoryDistributionChart(statistics = statistics)
            }
        }
    }
}

@Composable
fun OverallStatisticsCard(
    statistics: Statistics,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "전체 통계",
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "전체",
                    value = statistics.totalReminders.toString(),
                    color = MaterialTheme.colorScheme.primary
                )
                StatItem(
                    label = "완료",
                    value = statistics.completedReminders.toString(),
                    color = MaterialTheme.colorScheme.tertiary
                )
                StatItem(
                    label = "미완료",
                    value = statistics.pendingReminders.toString(),
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
fun StatItem(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun CompletionRateCard(
    statistics: Statistics,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "완료율",
                style = MaterialTheme.typography.titleMedium
            )

            val completionPercentage = (statistics.completionRate * 100).toInt()

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LinearProgressIndicator(
                    progress = statistics.completionRate,
                    modifier = Modifier
                        .weight(1f)
                        .height(24.dp)
                        .semantics {
                            contentDescription = "완료율 $completionPercentage 퍼센트"
                        },
                    color = MaterialTheme.colorScheme.tertiary
                )

                Text(
                    text = "$completionPercentage%",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

@Composable
fun PriorityDistributionChart(
    statistics: Statistics,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "우선순위별 분포",
                style = MaterialTheme.typography.titleMedium
            )

            if (statistics.totalReminders > 0) {
                AndroidView(
                    factory = { context ->
                        PieChart(context).apply {
                            description.isEnabled = false
                            legend.isEnabled = true
                            setDrawEntryLabels(true)
                            setUsePercentValues(true)
                            holeRadius = 40f
                            transparentCircleRadius = 45f
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .semantics {
                            contentDescription = "우선순위별 분포 차트. 높음 ${statistics.highPriorityCount}개, " +
                                    "중간 ${statistics.mediumPriorityCount}개, 낮음 ${statistics.lowPriorityCount}개"
                        },
                    update = { chart ->
                        val entries = mutableListOf<PieEntry>()
                        val colors = mutableListOf<Int>()

                        if (statistics.highPriorityCount > 0) {
                            entries.add(PieEntry(statistics.highPriorityCount.toFloat(), "높음"))
                            colors.add(Color.Red.copy(alpha = 0.7f).toArgb())
                        }
                        if (statistics.mediumPriorityCount > 0) {
                            entries.add(PieEntry(statistics.mediumPriorityCount.toFloat(), "중간"))
                            colors.add(Color(0xFFFFA500).copy(alpha = 0.7f).toArgb())
                        }
                        if (statistics.lowPriorityCount > 0) {
                            entries.add(PieEntry(statistics.lowPriorityCount.toFloat(), "낮음"))
                            colors.add(Color.Green.copy(alpha = 0.7f).toArgb())
                        }

                        val dataSet = PieDataSet(entries, "").apply {
                            setColors(colors)
                            valueTextSize = 14f
                            sliceSpace = 2f
                        }

                        chart.data = PieData(dataSet)
                        chart.invalidate()
                    }
                )
            } else {
                Text(
                    text = "데이터가 없습니다",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(32.dp)
                )
            }
        }
    }
}

@Composable
fun CategoryDistributionChart(
    statistics: Statistics,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "카테고리별 분포",
                style = MaterialTheme.typography.titleMedium
            )

            val categoryText = statistics.categoryDistribution
                .map { (category, count) -> "$category $count 개" }
                .joinToString(", ")

            AndroidView(
                factory = { context ->
                    PieChart(context).apply {
                        description.isEnabled = false
                        legend.isEnabled = true
                        setDrawEntryLabels(true)
                        setUsePercentValues(true)
                        holeRadius = 40f
                        transparentCircleRadius = 45f
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .semantics {
                        contentDescription = "카테고리별 분포 차트. $categoryText"
                    },
                update = { chart ->
                    val entries = statistics.categoryDistribution.map { (category, count) ->
                        PieEntry(count.toFloat(), category)
                    }

                    val colors = listOf(
                        Color(0xFF2196F3).toArgb(), // Blue
                        Color(0xFF4CAF50).toArgb(), // Green
                        Color(0xFFFFC107).toArgb(), // Amber
                        Color(0xFF9C27B0).toArgb(), // Purple
                        Color(0xFFFF5722).toArgb(), // Deep Orange
                        Color(0xFF00BCD4).toArgb(), // Cyan
                        Color(0xFFFF9800).toArgb(), // Orange
                        Color(0xFF673AB7).toArgb()  // Deep Purple
                    )

                    val dataSet = PieDataSet(entries, "").apply {
                        setColors(colors)
                        valueTextSize = 14f
                        sliceSpace = 2f
                    }

                    chart.data = PieData(dataSet)
                    chart.invalidate()
                }
            )
        }
    }
}

@Composable
fun WeeklyTrendCard(
    statistics: Statistics,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "최근 7일 완료 트렌드",
                style = MaterialTheme.typography.titleMedium
            )

            if (statistics.weeklyCompleted.sum() > 0) {
                val labels = listOf("오늘", "1일 전", "2일 전", "3일 전", "4일 전", "5일 전", "6일 전")

                TrendChart(
                    data = statistics.weeklyCompleted,
                    labels = labels,
                    lineColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            } else {
                Text(
                    text = "완료된 리마인더가 없습니다",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(32.dp)
                )
            }
        }
    }
}

@Composable
fun MonthlyTrendCard(
    statistics: Statistics,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "최근 30일 완료 트렌드",
                style = MaterialTheme.typography.titleMedium
            )

            if (statistics.monthlyCompleted.sum() > 0) {
                // 30일 라벨: 0일 전, 5일 전, 10일 전, 15일 전, 20일 전, 25일 전, 29일 전 (간격을 두고 표시)
                val labels = (0 until 30).map {
                    if (it % 5 == 0) "${it}일 전" else ""
                }

                TrendChart(
                    data = statistics.monthlyCompleted,
                    labels = labels,
                    lineColor = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            } else {
                Text(
                    text = "완료된 리마인더가 없습니다",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(32.dp)
                )
            }
        }
    }
}
