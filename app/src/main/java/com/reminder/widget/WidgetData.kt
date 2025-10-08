package com.reminder.widget

import com.reminder.data.entity.Priority

/**
 * 위젯에 표시될 리마인더 데이터
 */
data class WidgetData(
    val id: Long,
    val title: String,
    val priority: Priority,
    val formattedDueDate: String
)
