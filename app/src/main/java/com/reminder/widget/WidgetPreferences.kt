package com.reminder.widget

import android.content.Context
import android.content.SharedPreferences
import com.reminder.data.entity.FilterDate
import com.reminder.data.entity.FilterPriority
import com.reminder.data.entity.SortOption

/**
 * v1.34.0: 위젯 설정 관리
 *
 * 각 위젯 ID별로 독립적인 설정 저장
 */
class WidgetPreferences(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "widget_preferences"
        private const val KEY_THEME_PRESET = "theme_preset_"
        private const val KEY_FILTER_PRIORITY = "filter_priority_"
        private const val KEY_FILTER_DATE = "filter_date_"
        private const val KEY_SORT_BY = "sort_by_"
        private const val KEY_MAX_ITEMS = "max_items_"
        private const val KEY_WIDGET_SIZE = "widget_size_"

        // 위젯 크기 상수
        const val SIZE_SMALL = "small"   // 2x2
        const val SIZE_MEDIUM = "medium" // 4x2
        const val SIZE_LARGE = "large"   // 4x4
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * 위젯 설정 데이터 클래스
     */
    data class WidgetConfig(
        val themePreset: String = "default",
        val filterPriority: FilterPriority = FilterPriority.ALL,
        val filterDate: FilterDate = FilterDate.ALL,
        val sortBy: SortOption = SortOption.BY_DATE_ASC,
        val maxItems: Int = 5,
        val widgetSize: String = SIZE_MEDIUM
    )

    /**
     * 위젯 설정 저장
     */
    fun saveWidgetConfig(widgetId: Int, config: WidgetConfig) {
        prefs.edit().apply {
            putString(KEY_THEME_PRESET + widgetId, config.themePreset)
            putString(KEY_FILTER_PRIORITY + widgetId, config.filterPriority.name)
            putString(KEY_FILTER_DATE + widgetId, config.filterDate.name)
            putString(KEY_SORT_BY + widgetId, config.sortBy.name)
            putInt(KEY_MAX_ITEMS + widgetId, config.maxItems)
            putString(KEY_WIDGET_SIZE + widgetId, config.widgetSize)
            apply()
        }
    }

    /**
     * 위젯 설정 불러오기
     */
    fun getWidgetConfig(widgetId: Int): WidgetConfig {
        return WidgetConfig(
            themePreset = prefs.getString(KEY_THEME_PRESET + widgetId, "default") ?: "default",
            filterPriority = try {
                FilterPriority.valueOf(prefs.getString(KEY_FILTER_PRIORITY + widgetId, FilterPriority.ALL.name) ?: FilterPriority.ALL.name)
            } catch (e: Exception) {
                FilterPriority.ALL
            },
            filterDate = try {
                FilterDate.valueOf(prefs.getString(KEY_FILTER_DATE + widgetId, FilterDate.ALL.name) ?: FilterDate.ALL.name)
            } catch (e: Exception) {
                FilterDate.ALL
            },
            sortBy = try {
                SortOption.valueOf(prefs.getString(KEY_SORT_BY + widgetId, SortOption.BY_DATE_ASC.name) ?: SortOption.BY_DATE_ASC.name)
            } catch (e: Exception) {
                SortOption.BY_DATE_ASC
            },
            maxItems = prefs.getInt(KEY_MAX_ITEMS + widgetId, 5),
            widgetSize = prefs.getString(KEY_WIDGET_SIZE + widgetId, SIZE_MEDIUM) ?: SIZE_MEDIUM
        )
    }

    /**
     * 위젯 설정 삭제 (위젯 제거 시)
     */
    fun deleteWidgetConfig(widgetId: Int) {
        prefs.edit().apply {
            remove(KEY_THEME_PRESET + widgetId)
            remove(KEY_FILTER_PRIORITY + widgetId)
            remove(KEY_FILTER_DATE + widgetId)
            remove(KEY_SORT_BY + widgetId)
            remove(KEY_MAX_ITEMS + widgetId)
            remove(KEY_WIDGET_SIZE + widgetId)
            apply()
        }
    }

    /**
     * 테마 프리셋 설정
     */
    fun setThemePreset(widgetId: Int, themePreset: String) {
        prefs.edit().putString(KEY_THEME_PRESET + widgetId, themePreset).apply()
    }

    /**
     * 테마 프리셋 가져오기
     */
    fun getThemePreset(widgetId: Int): String {
        return prefs.getString(KEY_THEME_PRESET + widgetId, "default") ?: "default"
    }

    /**
     * 위젯 크기 설정
     */
    fun setWidgetSize(widgetId: Int, size: String) {
        prefs.edit().putString(KEY_WIDGET_SIZE + widgetId, size).apply()
    }

    /**
     * 위젯 크기 가져오기
     */
    fun getWidgetSize(widgetId: Int): String {
        return prefs.getString(KEY_WIDGET_SIZE + widgetId, SIZE_MEDIUM) ?: SIZE_MEDIUM
    }

    /**
     * 최대 항목 수 설정
     */
    fun setMaxItems(widgetId: Int, maxItems: Int) {
        prefs.edit().putInt(KEY_MAX_ITEMS + widgetId, maxItems).apply()
    }

    /**
     * 최대 항목 수 가져오기
     */
    fun getMaxItems(widgetId: Int): Int {
        return prefs.getInt(KEY_MAX_ITEMS + widgetId, when (getWidgetSize(widgetId)) {
            SIZE_SMALL -> 3
            SIZE_MEDIUM -> 5
            SIZE_LARGE -> 10
            else -> 5
        })
    }
}
