package com.reminder.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.reminder.data.entity.ReminderEntity
import com.reminder.domain.Quadrant
import com.reminder.domain.TrendPeriod
import com.reminder.domain.calculateQuadrantTrend
import com.reminder.domain.calculateTimeDistribution
import com.reminder.domain.getInfo

/**
 * v1.68.3: TrendAnalysisDialog 컴포넌트 (EisenhowerMatrixScreen에서 분리)
 *
 * 트렌드 분석 다이얼로그 및 관련 컴포넌트
 */
@Composable
fun TrendAnalysisDialog(
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
