package com.reminder.filter

import com.reminder.data.entity.Priority
import java.time.LocalDateTime

/**
 * 기본 제공 필터 프리셋
 *
 * 자주 사용하는 필터 조합을 미리 정의
 */
object FilterPresets {

    /**
     * 오늘 마감되는 리마인더
     */
    fun today(): ReminderFilter {
        val now = LocalDateTime.now()
        val endOfDay = now.toLocalDate().atTime(23, 59, 59)
        return ReminderFilter(
            dateRange = DateRange(
                start = now,
                end = endOfDay
            ),
            isCompleted = false
        )
    }

    /**
     * 이번 주 (7일 이내) 마감되는 리마인더
     */
    fun thisWeek(): ReminderFilter {
        val now = LocalDateTime.now()
        val endOfWeek = now.plusWeeks(1)
        return ReminderFilter(
            dateRange = DateRange(
                start = now,
                end = endOfWeek
            ),
            isCompleted = false
        )
    }

    /**
     * 높은 우선순위 리마인더
     */
    fun important(): ReminderFilter {
        return ReminderFilter(
            priorities = listOf(Priority.HIGH),
            isCompleted = false
        )
    }

    /**
     * 긴급 리마인더 (오늘 마감 + 높은 우선순위)
     */
    fun urgent(): ReminderFilter {
        val now = LocalDateTime.now()
        val endOfDay = now.toLocalDate().atTime(23, 59, 59)
        return ReminderFilter(
            priorities = listOf(Priority.HIGH),
            dateRange = DateRange(
                start = now,
                end = endOfDay
            ),
            isCompleted = false
        )
    }

    /**
     * 미완료 리마인더
     */
    fun incomplete(): ReminderFilter {
        return ReminderFilter(
            isCompleted = false
        )
    }

    /**
     * 완료된 리마인더
     */
    fun completed(): ReminderFilter {
        return ReminderFilter(
            isCompleted = true
        )
    }

    /**
     * 위치가 설정된 리마인더
     */
    fun withLocation(): ReminderFilter {
        return ReminderFilter(
            hasLocation = true,
            isCompleted = false
        )
    }

    /**
     * 반복 작업 리마인더
     */
    fun recurring(): ReminderFilter {
        return ReminderFilter(
            hasRecurrence = true,
            isCompleted = false
        )
    }

    /**
     * 웹 링크가 첨부된 리마인더
     */
    fun withWebLink(): ReminderFilter {
        return ReminderFilter(
            hasWebLink = true,
            isCompleted = false
        )
    }

    /**
     * TTS가 설정된 리마인더
     */
    fun withTts(): ReminderFilter {
        return ReminderFilter(
            hasTts = true,
            isCompleted = false
        )
    }

    /**
     * 모든 프리셋 목록
     */
    fun getAllPresets(): List<FilterPreset> {
        return listOf(
            FilterPreset(
                id = "today",
                name = "오늘",
                icon = "today",
                filter = today()
            ),
            FilterPreset(
                id = "this_week",
                name = "이번 주",
                icon = "date_range",
                filter = thisWeek()
            ),
            FilterPreset(
                id = "important",
                name = "중요",
                icon = "priority_high",
                filter = important()
            ),
            FilterPreset(
                id = "urgent",
                name = "긴급",
                icon = "error",
                filter = urgent()
            ),
            FilterPreset(
                id = "incomplete",
                name = "미완료",
                icon = "radio_button_unchecked",
                filter = incomplete()
            ),
            FilterPreset(
                id = "completed",
                name = "완료",
                icon = "check_circle",
                filter = completed()
            ),
            FilterPreset(
                id = "with_location",
                name = "위치 설정됨",
                icon = "location_on",
                filter = withLocation()
            ),
            FilterPreset(
                id = "recurring",
                name = "반복 작업",
                icon = "repeat",
                filter = recurring()
            ),
            FilterPreset(
                id = "with_web_link",
                name = "웹 링크",
                icon = "link",
                filter = withWebLink()
            ),
            FilterPreset(
                id = "with_tts",
                name = "음성 알림",
                icon = "volume_up",
                filter = withTts()
            )
        )
    }

    /**
     * ID로 프리셋 조회
     */
    fun getPresetById(id: String): FilterPreset? {
        return getAllPresets().find { it.id == id }
    }
}

/**
 * 필터 프리셋 데이터 클래스
 */
data class FilterPreset(
    val id: String,
    val name: String,
    val icon: String,
    val filter: ReminderFilter
)
