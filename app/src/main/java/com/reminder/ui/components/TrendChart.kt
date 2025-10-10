package com.reminder.ui.components

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

/**
 * 트렌드 라인 차트 컴포넌트
 * 일별 완료 개수를 시각화하는 라인 차트
 *
 * @param data 일별 완료 개수 리스트 (0일 전 = 오늘, 1일 전 = 어제, ...)
 * @param labels X축 날짜 라벨
 * @param lineColor 라인 색상
 * @param modifier Modifier
 */
@Composable
fun TrendChart(
    data: List<Int>,
    labels: List<String>,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = isSystemInDarkTheme()
    val textColor = if (isDarkTheme) AndroidColor.WHITE else AndroidColor.BLACK
    val gridColor = if (isDarkTheme) AndroidColor.DKGRAY else AndroidColor.LTGRAY

    // Entry 리스트 생성 (역순으로 - 오래된 날짜가 왼쪽)
    val entries = remember(data) {
        data.asReversed().mapIndexed { index, count ->
            Entry(index.toFloat(), count.toFloat())
        }
    }

    AndroidView(
        factory = { context ->
            LineChart(context).apply {
                description.isEnabled = false
                setTouchEnabled(true)
                isDragEnabled = true
                setScaleEnabled(false)
                setPinchZoom(false)
                setDrawGridBackground(false)
                legend.isEnabled = false

                // X축 설정
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(true)
                    gridColor = gridColor
                    textColor = textColor
                    granularity = 1f
                    labelRotationAngle = -45f
                    valueFormatter = IndexAxisValueFormatter(labels.asReversed())
                }

                // 왼쪽 Y축 설정
                axisLeft.apply {
                    setDrawGridLines(true)
                    gridColor = gridColor
                    textColor = textColor
                    axisMinimum = 0f
                    granularity = 1f
                }

                // 오른쪽 Y축 비활성화
                axisRight.isEnabled = false

                // 애니메이션
                animateX(800)
            }
        },
        update = { chart ->
            // 데이터셋 생성
            val dataSet = LineDataSet(entries, "완료 개수").apply {
                color = lineColor.toArgb()
                lineWidth = 2.5f
                setCircleColor(lineColor.toArgb())
                circleRadius = 4f
                circleHoleRadius = 2f
                setDrawValues(true)
                valueTextSize = 9f
                valueTextColor = textColor
                mode = LineDataSet.Mode.CUBIC_BEZIER // 부드러운 곡선
                cubicIntensity = 0.2f
                setDrawFilled(true)
                fillColor = lineColor.toArgb()
                fillAlpha = 50
            }

            // 차트에 데이터 설정
            chart.data = LineData(dataSet)
            chart.invalidate() // 차트 새로고침
        },
        modifier = modifier
    )
}
