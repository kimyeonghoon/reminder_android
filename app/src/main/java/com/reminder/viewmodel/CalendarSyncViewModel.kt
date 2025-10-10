package com.reminder.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reminder.calendar.CalendarSyncManager
import com.reminder.calendar.DeviceCalendar
import com.reminder.calendar.DeviceCalendarProvider
import com.reminder.data.dao.ReminderDao
import com.reminder.data.entity.CalendarSyncConfig
import com.reminder.data.entity.SyncDirection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * v1.40.1: 캘린더 동기화 ViewModel
 *
 * 캘린더 동기화 설정 및 실행을 관리합니다.
 */
class CalendarSyncViewModel(
    private val calendarSyncManager: CalendarSyncManager,
    private val deviceCalendarProvider: DeviceCalendarProvider,
    private val reminderDao: ReminderDao
) : ViewModel() {

    /**
     * 동기화 상태
     */
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    /**
     * 동기화 에러 메시지
     */
    private val _syncError = MutableStateFlow<String?>(null)
    val syncError: StateFlow<String?> = _syncError.asStateFlow()

    /**
     * 기기 캘린더 목록
     */
    private val _deviceCalendars = MutableStateFlow<List<DeviceCalendar>>(emptyList())
    val deviceCalendars: StateFlow<List<DeviceCalendar>> = _deviceCalendars.asStateFlow()

    /**
     * 캘린더 권한 상태
     */
    private val _hasCalendarPermission = MutableStateFlow(false)
    val hasCalendarPermission: StateFlow<Boolean> = _hasCalendarPermission.asStateFlow()

    init {
        checkCalendarPermission()
    }

    /**
     * 캘린더 권한 확인
     */
    fun checkCalendarPermission() {
        _hasCalendarPermission.value = deviceCalendarProvider.hasCalendarPermission()

        if (_hasCalendarPermission.value) {
            loadDeviceCalendars()
        }
    }

    /**
     * 기기 캘린더 목록 로드
     */
    fun loadDeviceCalendars() {
        viewModelScope.launch {
            try {
                val calendars = calendarSyncManager.getDeviceCalendars()
                _deviceCalendars.value = calendars
            } catch (e: Exception) {
                _syncError.value = "캘린더 목록을 불러오는 중 오류가 발생했습니다: ${e.message}"
            }
        }
    }

    /**
     * 캘린더 동기화 활성화/비활성화
     */
    fun toggleCalendarSync(calendarId: String, isEnabled: Boolean) {
        viewModelScope.launch {
            try {
                calendarSyncManager.toggleCalendarSync(calendarId, isEnabled)
            } catch (e: Exception) {
                _syncError.value = "동기화 설정 변경 중 오류가 발생했습니다: ${e.message}"
            }
        }
    }

    /**
     * 새 캘린더 동기화 추가
     */
    fun addCalendarSync(
        deviceCalendar: DeviceCalendar,
        syncDirection: SyncDirection = SyncDirection.TWO_WAY
    ) {
        viewModelScope.launch {
            try {
                calendarSyncManager.addCalendarSyncConfig(
                    calendarId = deviceCalendar.id,
                    calendarName = deviceCalendar.name,
                    accountName = deviceCalendar.accountName,
                    calendarColor = deviceCalendar.color,
                    syncDirection = syncDirection
                )
            } catch (e: Exception) {
                _syncError.value = "캘린더 추가 중 오류가 발생했습니다: ${e.message}"
            }
        }
    }

    /**
     * 모든 리마인더 동기화 실행
     */
    fun syncAllReminders() {
        viewModelScope.launch {
            try {
                _isSyncing.value = true
                _syncError.value = null

                calendarSyncManager.syncAllReminders()

                _isSyncing.value = false
            } catch (e: Exception) {
                _isSyncing.value = false
                _syncError.value = "동기화 중 오류가 발생했습니다: ${e.message}"
            }
        }
    }

    /**
     * 에러 메시지 초기화
     */
    fun clearError() {
        _syncError.value = null
    }
}
