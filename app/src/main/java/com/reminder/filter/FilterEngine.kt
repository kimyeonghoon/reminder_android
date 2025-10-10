package com.reminder.filter

import com.reminder.data.entity.Priority
import com.reminder.data.entity.ReminderEntity
import java.time.LocalDateTime

/**
 * 날짜 범위 필터
 */
data class DateRange(
    val start: LocalDateTime,
    val end: LocalDateTime
)

/**
 * 리마인더 복합 필터
 *
 * 모든 필드는 nullable이며, null인 경우 해당 필터를 적용하지 않음
 */
data class ReminderFilter(
    val priorities: List<Priority>? = null,      // 우선순위 필터
    val categories: List<String>? = null,        // 카테고리 필터
    val tags: List<String>? = null,              // 태그 필터
    val dateRange: DateRange? = null,            // 날짜 범위 필터
    val isCompleted: Boolean? = null,            // 완료 상태 필터
    val hasLocation: Boolean? = null,            // 위치 설정 여부
    val hasWebLink: Boolean? = null,             // 웹 링크 첨부 여부
    val hasTts: Boolean? = null,                 // TTS 설정 여부
    val hasRecurrence: Boolean? = null           // 반복 설정 여부
)

/**
 * 리마인더 필터링 엔진
 *
 * 복합 필터를 적용하여 조건에 맞는 리마인더만 반환
 */
class FilterEngine {

    /**
     * 필터를 적용하여 조건에 맞는 리마인더 목록 반환
     *
     * @param reminders 필터링할 리마인더 목록
     * @param filter 적용할 필터
     * @return 필터링된 리마인더 목록
     */
    fun applyFilter(
        reminders: List<ReminderEntity>,
        filter: ReminderFilter
    ): List<ReminderEntity> {
        return reminders.filter { reminder ->
            matchesFilter(reminder, filter)
        }
    }

    /**
     * 단일 리마인더가 필터 조건을 만족하는지 확인
     */
    private fun matchesFilter(
        reminder: ReminderEntity,
        filter: ReminderFilter
    ): Boolean {
        // 우선순위 필터
        if (filter.priorities != null && reminder.priority !in filter.priorities) {
            return false
        }

        // 카테고리 필터
        if (filter.categories != null && reminder.category !in filter.categories) {
            return false
        }

        // 태그 필터 (콤마로 구분된 태그 중 하나라도 일치하면 통과)
        if (filter.tags != null) {
            val reminderTags = reminder.tags.split(",").map { it.trim() }
            val hasMatchingTag = filter.tags.any { filterTag ->
                reminderTags.contains(filterTag)
            }
            if (!hasMatchingTag) {
                return false
            }
        }

        // 날짜 범위 필터
        if (filter.dateRange != null) {
            val dueDateTime = reminder.dueDateTime ?: return false
            if (dueDateTime.isBefore(filter.dateRange.start) ||
                dueDateTime.isAfter(filter.dateRange.end)
            ) {
                return false
            }
        }

        // 완료 상태 필터
        if (filter.isCompleted != null && reminder.isCompleted != filter.isCompleted) {
            return false
        }

        // 위치 설정 여부 필터
        if (filter.hasLocation != null) {
            val hasLocation = reminder.locationLatitude != null &&
                    reminder.locationLongitude != null
            if (hasLocation != filter.hasLocation) {
                return false
            }
        }

        // 웹 링크 첨부 여부 필터
        if (filter.hasWebLink != null) {
            val hasWebLink = !reminder.webLink.isNullOrBlank()
            if (hasWebLink != filter.hasWebLink) {
                return false
            }
        }

        // TTS 설정 여부 필터
        if (filter.hasTts != null) {
            if (reminder.readAloud != filter.hasTts) {
                return false
            }
        }

        // 반복 설정 여부 필터
        if (filter.hasRecurrence != null) {
            val hasRecurrence = reminder.recurrencePattern != com.reminder.data.entity.RecurrencePattern.NONE
            if (hasRecurrence != filter.hasRecurrence) {
                return false
            }
        }

        return true
    }
}
