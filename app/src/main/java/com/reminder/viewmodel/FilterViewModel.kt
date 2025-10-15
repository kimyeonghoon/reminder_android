package com.reminder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reminder.analytics.AnalyticsHelper
import com.reminder.data.dao.SavedFilterDao
import com.reminder.data.entity.ReminderEntity
import com.reminder.data.entity.SavedFilterEntity
import com.reminder.filter.FilterEngine
import com.reminder.filter.ReminderFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * v1.68.1: Filter 전용 ViewModel
 *
 * ReminderViewModel에서 분리됨 (God Class 리팩토링)
 * 고급 필터 시스템 (v1.32.0) 담당
 */
class FilterViewModel(
    private val savedFilterDao: SavedFilterDao,
    private val analyticsHelper: AnalyticsHelper
) : ViewModel() {

    private val filterEngine = FilterEngine()

    /**
     * 현재 적용된 필터
     */
    private val _currentFilter = MutableStateFlow<ReminderFilter?>(null)
    val currentFilter: StateFlow<ReminderFilter?> = _currentFilter.asStateFlow()

    /**
     * 저장된 필터 목록 조회
     */
    val savedFilters = savedFilterDao.getAllSavedFilters()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * 필터를 적용하여 필터링된 리마인더 목록 반환
     */
    fun applyFilter(filter: ReminderFilter) {
        _currentFilter.value = filter

        // Analytics 이벤트 로깅
        analyticsHelper.logFilterApplied()
    }

    /**
     * 필터 초기화
     */
    fun clearFilter() {
        _currentFilter.value = null

        // Analytics 이벤트 로깅
        analyticsHelper.logFilterCleared()
    }

    /**
     * 현재 필터가 적용된 리마인더 목록 반환
     */
    fun getFilteredRemindersWithFilter(reminders: List<ReminderEntity>): List<ReminderEntity> {
        val filter = _currentFilter.value ?: return reminders
        return filterEngine.applyFilter(reminders, filter)
    }

    /**
     * 필터 프리셋 적용
     */
    fun applyFilterPreset(presetId: String) {
        val preset = com.reminder.filter.FilterPresets.getPresetById(presetId)
        if (preset != null) {
            applyFilter(preset.filter)

            // Analytics 이벤트 로깅
            analyticsHelper.logPresetUsed(presetId)
        }
    }

    /**
     * 필터를 저장된 필터로 저장
     */
    fun saveFilter(name: String, icon: String, filter: ReminderFilter) {
        viewModelScope.launch {
            val filterJson = serializeFilter(filter)
            val savedFilter = SavedFilterEntity(
                name = name,
                icon = icon,
                filterJson = filterJson
            )
            savedFilterDao.insertSavedFilter(savedFilter)

            // Analytics 이벤트 로깅
            analyticsHelper.logFilterSaved()
        }
    }

    /**
     * 저장된 필터 삭제
     */
    fun deleteSavedFilter(filter: SavedFilterEntity) {
        viewModelScope.launch {
            savedFilterDao.deleteSavedFilter(filter)
        }
    }

    /**
     * 저장된 필터 적용
     */
    fun applySavedFilter(savedFilter: SavedFilterEntity) {
        val filter = deserializeFilter(savedFilter.filterJson)
        if (filter != null) {
            applyFilter(filter)
        }
    }

    /**
     * ReminderFilter를 JSON으로 직렬화
     */
    private fun serializeFilter(filter: ReminderFilter): String {
        return com.google.gson.Gson().toJson(filter)
    }

    /**
     * JSON을 ReminderFilter로 역직렬화
     */
    private fun deserializeFilter(json: String): ReminderFilter? {
        return try {
            com.google.gson.Gson().fromJson(json, ReminderFilter::class.java)
        } catch (e: Exception) {
            null
        }
    }
}
