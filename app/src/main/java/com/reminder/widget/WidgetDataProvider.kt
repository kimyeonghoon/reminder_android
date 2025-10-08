package com.reminder.widget

import com.reminder.data.entity.ReminderEntity
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 위젯에 표시할 리마인더 데이터를 준비하는 클래스
 */
class WidgetDataProvider {

    companion object {
        private const val MAX_WIDGET_ITEMS = 10
        private val DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    }

    /**
     * 리마인더 목록을 위젯용 데이터로 변환
     * - 완료되지 않은 리마인더만 포함
     * - 마감일이 가까운 순서로 정렬
     * - 최대 10개만 반환
     */
    fun prepareWidgetData(reminders: List<ReminderEntity>): List<WidgetData> {
        return reminders
            .filter { !it.isCompleted }
            .sortedWith(compareBy(
                { it.dueDateTime == null }, // null을 뒤로
                { it.dueDateTime }           // 날짜 오름차순
            ))
            .take(MAX_WIDGET_ITEMS)
            .map { reminder ->
                WidgetData(
                    id = reminder.id,
                    title = reminder.title,
                    priority = reminder.priority,
                    formattedDueDate = formatDueDate(reminder.dueDateTime)
                )
            }
    }

    private fun formatDueDate(dueDateTime: LocalDateTime?): String {
        return dueDateTime?.format(DATE_TIME_FORMATTER) ?: ""
    }
}
