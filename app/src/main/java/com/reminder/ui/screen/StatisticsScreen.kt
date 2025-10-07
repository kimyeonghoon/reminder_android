package com.reminder.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.reminder.data.entity.Statistics
import com.reminder.viewmodel.StatisticsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statistics by viewModel.statistics.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("통계") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                }
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

            // 완료율 카드
            CompletionRateCard(statistics = statistics)

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
                        .height(24.dp),
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
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
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
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            )
        }
    }
}
