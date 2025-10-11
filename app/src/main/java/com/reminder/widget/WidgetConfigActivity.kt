package com.reminder.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.reminder.data.entity.FilterDate
import com.reminder.data.entity.FilterPriority
import com.reminder.data.entity.SortOption
import com.reminder.ui.theme.ReminderTheme

/**
 * v1.34.0: 위젯 설정 Activity
 *
 * 위젯 추가 시 표시되는 설정 화면
 */
class WidgetConfigActivity : ComponentActivity() {

    private var widgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private lateinit var widgetPreferences: WidgetPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 위젯 추가 취소 시 RESULT_CANCELED 반환하도록 설정
        setResult(RESULT_CANCELED)

        // 위젯 ID 가져오기
        widgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        // 유효하지 않은 위젯 ID면 종료
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        widgetPreferences = WidgetPreferences(this)

        setContent {
            ReminderTheme {
                WidgetConfigScreen(
                    onSaveClick = { config ->
                        saveWidgetConfig(config)
                    },
                    onCancelClick = {
                        finish()
                    }
                )
            }
        }
    }

    private fun saveWidgetConfig(config: WidgetPreferences.WidgetConfig) {
        // 설정 저장
        widgetPreferences.saveWidgetConfig(widgetId, config)

        // 위젯 업데이트
        val appWidgetManager = AppWidgetManager.getInstance(this)
        ReminderWidgetProvider().onUpdate(
            this,
            appWidgetManager,
            intArrayOf(widgetId)
        )

        // 주기적 업데이트 예약
        WidgetUpdateWorker.schedulePeriodicUpdate(this)

        // 성공 결과 반환
        val resultValue = Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        }
        setResult(Activity.RESULT_OK, resultValue)
        finish()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WidgetConfigScreen(
    onSaveClick: (WidgetPreferences.WidgetConfig) -> Unit,
    onCancelClick: () -> Unit
) {
    var selectedTheme by remember { mutableStateOf("default") }
    var selectedPriority by remember { mutableStateOf(FilterPriority.ALL) }
    var selectedDate by remember { mutableStateOf(FilterDate.ALL) }
    var selectedSort by remember { mutableStateOf(SortOption.BY_DATE_ASC) }
    var selectedSize by remember { mutableStateOf(WidgetPreferences.SIZE_MEDIUM) }
    var maxItems by remember { mutableIntStateOf(5) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("위젯 설정") }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 위젯 크기 선택
            item {
                Text("위젯 크기", style = MaterialTheme.typography.titleMedium)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "Small (2x2)" to WidgetPreferences.SIZE_SMALL,
                        "Medium (4x2)" to WidgetPreferences.SIZE_MEDIUM,
                        "Large (4x4)" to WidgetPreferences.SIZE_LARGE
                    ).forEach { (label, size) ->
                        FilterChip(
                            selected = selectedSize == size,
                            onClick = {
                                selectedSize = size
                                maxItems = when (size) {
                                    WidgetPreferences.SIZE_SMALL -> 3
                                    WidgetPreferences.SIZE_MEDIUM -> 5
                                    WidgetPreferences.SIZE_LARGE -> 10
                                    else -> 5
                                }
                            },
                            label = { Text(label) }
                        )
                    }
                }
            }

            // 우선순위 필터
            item {
                Text("우선순위 필터", style = MaterialTheme.typography.titleMedium)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterPriority.entries.forEach { priority ->
                        FilterChip(
                            selected = selectedPriority == priority,
                            onClick = { selectedPriority = priority },
                            label = {
                                Text(
                                    when (priority) {
                                        FilterPriority.ALL -> "전체"
                                        FilterPriority.HIGH -> "높음"
                                        FilterPriority.MEDIUM -> "중간"
                                        FilterPriority.LOW -> "낮음"
                                    }
                                )
                            }
                        )
                    }
                }
            }

            // 날짜 필터
            item {
                Text("날짜 필터", style = MaterialTheme.typography.titleMedium)
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterDate.entries.forEach { date ->
                        FilterChip(
                            selected = selectedDate == date,
                            onClick = { selectedDate = date },
                            label = {
                                Text(
                                    when (date) {
                                        FilterDate.ALL -> "전체"
                                        FilterDate.TODAY -> "오늘"
                                        FilterDate.THIS_WEEK -> "이번 주"
                                        FilterDate.THIS_MONTH -> "이번 달"
                                        FilterDate.OVERDUE -> "기한 초과"
                                    }
                                )
                            }
                        )
                    }
                }
            }

            // 정렬 방식
            item {
                Text("정렬 방식", style = MaterialTheme.typography.titleMedium)
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        SortOption.BY_DATE_ASC to "날짜순 (빠른 순)",
                        SortOption.BY_DATE_DESC to "날짜순 (늦은 순)",
                        SortOption.BY_PRIORITY_HIGH_FIRST to "우선순위순",
                        SortOption.BY_CREATED_ASC to "생성일순"
                    ).forEach { (sort, label) ->
                        FilterChip(
                            selected = selectedSort == sort,
                            onClick = { selectedSort = sort },
                            label = { Text(label) }
                        )
                    }
                }
            }

            // 테마 선택
            item {
                Text("테마", style = MaterialTheme.typography.titleMedium)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("default" to "기본", "dark" to "다크", "light" to "라이트").forEach { (theme, label) ->
                        FilterChip(
                            selected = selectedTheme == theme,
                            onClick = { selectedTheme = theme },
                            label = { Text(label) }
                        )
                    }
                }
            }

            // 버튼
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancelClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("취소")
                    }
                    Button(
                        onClick = {
                            onSaveClick(
                                WidgetPreferences.WidgetConfig(
                                    themePreset = selectedTheme,
                                    filterPriority = selectedPriority,
                                    filterDate = selectedDate,
                                    sortBy = selectedSort,
                                    maxItems = maxItems,
                                    widgetSize = selectedSize
                                )
                            )
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("저장")
                    }
                }
            }
        }
    }
}
