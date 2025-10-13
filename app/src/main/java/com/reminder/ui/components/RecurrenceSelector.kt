package com.reminder.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import java.time.DayOfWeek
import java.time.LocalDateTime

/**
 * TODO v1.65.0: RecurrenceRule 기반 반복 설정 UI 재구현 필요
 *
 * v1.64.0에서 레거시 RecurrencePattern이 제거되어 임시로 빈 composable로 변경됨
 *
 * v1.65.0 구현 계획:
 * - RecurrenceRule 기반 UI
 * - RecurrenceEnd 설정 UI
 * - 다음 발생 시간 미리보기 (RecurrenceCalculator 필요)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurrenceSelector(
    recurrencePattern: Any? = null,  // Deprecated, kept for compatibility
    onPatternChange: (Any) -> Unit = {},
    recurrenceInterval: Int = 1,
    onIntervalChange: (Int) -> Unit = {},
    recurrenceDaysOfWeek: Set<DayOfWeek>? = null,
    onDaysOfWeekChange: (Set<DayOfWeek>?) -> Unit = {},
    recurrenceEndDate: LocalDateTime? = null,
    onEndDateChange: (LocalDateTime?) -> Unit = {},
    startDateTime: LocalDateTime? = null,
    modifier: Modifier = Modifier
) {
    // TODO v1.65.0: Implement RecurrenceRule-based UI here
    // 현재는 빈 composable (반복 기능 일시적으로 비활성화)
}
