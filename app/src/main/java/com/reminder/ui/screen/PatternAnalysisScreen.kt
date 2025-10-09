package com.reminder.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.reminder.analytics.CompletionPatternAnalyzer
import com.reminder.viewmodel.ReminderViewModel
import java.time.DayOfWeek

/**
 * v1.26.0: 완료 패턴 분석 대시보드 화면
 *
 * 사용자의 리마인더 완료 패턴을 시각화하고
 * 최적의 시간대와 요일을 제안합니다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatternAnalysisScreen(
    viewModel: ReminderViewModel,
    onNavigateBack: () -> Unit
) {
    var pattern by remember { mutableStateOf<CompletionPatternAnalyzer.CompletionPattern?>(null) }
    var summary by remember { mutableStateOf("") }

    // 패턴 분석 로드
    LaunchedEffect(Unit) {
        pattern = viewModel.analyzeCompletionPattern()
        summary = viewModel.getPatternSummary()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📊 완료 패턴 분석") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                }
            )
        }
    ) { paddingValues ->
        val currentPattern = pattern
        if (currentPattern == null) {
            // 로딩 또는 데이터 없음
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = summary.ifBlank { "패턴을 분석하는 중..." },
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            }
        } else {
            // 패턴 분석 결과 표시
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 요약 카드
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "📝 요약",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = summary,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                // 완료율
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "✅ 전체 완료율",
                                style = MaterialTheme.typography.titleMedium
                            )
                            LinearProgressIndicator(
                                progress = { currentPattern.completionRate.toFloat() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(12.dp),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                            Text(
                                text = "${(currentPattern.completionRate * 100).toInt()}%",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // 가장 생산적인 시간대
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "⏰ 가장 생산적인 시간대",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "${currentPattern.mostProductiveHour}:00",
                                style = MaterialTheme.typography.displaySmall,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "시간대별 완료율",
                                style = MaterialTheme.typography.labelLarge
                            )

                            // 상위 5개 시간대 표시
                            currentPattern.hourlyCompletionRate.entries
                                .sortedByDescending { it.value }
                                .take(5)
                                .forEach { (hour, rate) ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${hour}:00",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            LinearProgressIndicator(
                                                progress = { rate.toFloat() },
                                                modifier = Modifier
                                                    .width(100.dp)
                                                    .height(6.dp)
                                            )
                                            Text(
                                                text = "${(rate * 100).toInt()}%",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                }
                        }
                    }
                }

                // 가장 생산적인 요일
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "📅 가장 생산적인 요일",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = getDayOfWeekKorean(currentPattern.mostProductiveDay),
                                style = MaterialTheme.typography.displaySmall,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "요일별 완료율",
                                style = MaterialTheme.typography.labelLarge
                            )

                            // 모든 요일 표시
                            currentPattern.dailyCompletionRate.entries
                                .sortedByDescending { it.value }
                                .forEach { (day, rate) ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = getDayOfWeekKorean(day),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            LinearProgressIndicator(
                                                progress = { rate.toFloat() },
                                                modifier = Modifier
                                                    .width(100.dp)
                                                    .height(6.dp)
                                            )
                                            Text(
                                                text = "${(rate * 100).toInt()}%",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                }
                        }
                    }
                }

                // 평균 완료 시간
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "⏱️ 평균 완료 시간",
                                style = MaterialTheme.typography.titleMedium
                            )
                            val hours = (currentPattern.averageCompletionTime / 60).toInt()
                            val minutes = (currentPattern.averageCompletionTime % 60).toInt()
                            Text(
                                text = if (hours > 0) {
                                    "${hours}시간 ${minutes}분"
                                } else {
                                    "${minutes}분"
                                },
                                style = MaterialTheme.typography.displaySmall,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 요일을 한글로 변환
 */
private fun getDayOfWeekKorean(day: DayOfWeek): String {
    return when (day) {
        DayOfWeek.MONDAY -> "월요일"
        DayOfWeek.TUESDAY -> "화요일"
        DayOfWeek.WEDNESDAY -> "수요일"
        DayOfWeek.THURSDAY -> "목요일"
        DayOfWeek.FRIDAY -> "금요일"
        DayOfWeek.SATURDAY -> "토요일"
        DayOfWeek.SUNDAY -> "일요일"
    }
}
