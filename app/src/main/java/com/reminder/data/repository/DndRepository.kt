package com.reminder.data.repository

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import com.reminder.data.DndSettings
import com.reminder.utils.DndManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * v1.54.0: 방해 금지 모드 Repository
 *
 * DND 설정 관리 및 알림 차단 기능
 */
class DndRepository(context: Context) {

    private val dndManager = DndManager(context)
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _dndSettings = MutableStateFlow(loadSettings())
    val dndSettings: StateFlow<DndSettings> = _dndSettings.asStateFlow()

    /**
     * DND 권한이 있는지 확인
     */
    fun hasPermission(): Boolean {
        return dndManager.hasPermission()
    }

    /**
     * DND 권한 요청 Intent 가져오기
     */
    fun getPermissionIntent(): Intent {
        return dndManager.requestPermission()
    }

    /**
     * DND 활성화
     */
    fun enableDnd() {
        val settings = _dndSettings.value
        if (settings.enabled && settings.autoEnable) {
            dndManager.enableDnd(
                allowCalls = settings.allowCalls,
                allowAlarms = settings.allowAlarms
            )
        }
    }

    /**
     * DND 비활성화
     */
    fun disableDnd() {
        dndManager.disableDnd()
    }

    /**
     * 현재 DND가 활성화되어 있는지 확인
     */
    fun isEnabled(): Boolean {
        return dndManager.isEnabled()
    }

    /**
     * DND 설정 업데이트
     */
    fun updateSettings(settings: DndSettings) {
        _dndSettings.value = settings
        saveSettings(settings)
    }

    /**
     * 설정 로드
     */
    private fun loadSettings(): DndSettings {
        return DndSettings(
            enabled = prefs.getBoolean(KEY_ENABLED, false),
            allowCalls = prefs.getBoolean(KEY_ALLOW_CALLS, true),
            allowAlarms = prefs.getBoolean(KEY_ALLOW_ALARMS, true),
            autoEnable = prefs.getBoolean(KEY_AUTO_ENABLE, true)
        )
    }

    /**
     * 설정 저장
     */
    private fun saveSettings(settings: DndSettings) {
        prefs.edit()
            .putBoolean(KEY_ENABLED, settings.enabled)
            .putBoolean(KEY_ALLOW_CALLS, settings.allowCalls)
            .putBoolean(KEY_ALLOW_ALARMS, settings.allowAlarms)
            .putBoolean(KEY_AUTO_ENABLE, settings.autoEnable)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "dnd_settings"
        private const val KEY_ENABLED = "enabled"
        private const val KEY_ALLOW_CALLS = "allow_calls"
        private const val KEY_ALLOW_ALARMS = "allow_alarms"
        private const val KEY_AUTO_ENABLE = "auto_enable"
    }
}
