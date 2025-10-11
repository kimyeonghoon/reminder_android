package com.reminder.util

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi

/**
 * v1.54.0: 방해 금지 모드 관리 유틸리티
 *
 * 포커스 세션 중 알림 차단 기능
 */
class DndManager(private val context: Context) {

    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    /**
     * DND 권한이 있는지 확인
     */
    @RequiresApi(Build.VERSION_CODES.M)
    fun hasPermission(): Boolean {
        return notificationManager.isNotificationPolicyAccessGranted
    }

    /**
     * DND 권한 요청 화면으로 이동
     */
    fun requestPermission(): Intent {
        return Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
    }

    /**
     * DND 활성화
     * @param allowCalls 긴급 전화 허용 여부
     * @param allowAlarms 알람 허용 여부
     */
    @RequiresApi(Build.VERSION_CODES.M)
    fun enableDnd(allowCalls: Boolean = true, allowAlarms: Boolean = true) {
        if (!hasPermission()) return

        // 이전 상태 저장
        savePreviousInterruptionFilter()

        // Priority Only 모드로 설정
        notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)

        // Policy 설정 (API 23+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val policyCategories = buildPolicyCategories(allowCalls, allowAlarms)

            val policy = NotificationManager.Policy(
                policyCategories,
                NotificationManager.Policy.PRIORITY_SENDERS_STARRED, // 연락처에 있는 사람만
                NotificationManager.Policy.PRIORITY_SENDERS_ANY
            )

            notificationManager.setNotificationPolicy(policy)
        }
    }

    /**
     * DND 비활성화 (이전 상태로 복원)
     */
    @RequiresApi(Build.VERSION_CODES.M)
    fun disableDnd() {
        if (!hasPermission()) return

        // 이전 상태로 복원
        val previousFilter = getPreviousInterruptionFilter()
        notificationManager.setInterruptionFilter(previousFilter)

        // 저장된 상태 삭제
        clearPreviousInterruptionFilter()
    }

    /**
     * 현재 DND가 활성화되어 있는지 확인
     */
    @RequiresApi(Build.VERSION_CODES.M)
    fun isEnabled(): Boolean {
        if (!hasPermission()) return false

        val filter = notificationManager.currentInterruptionFilter
        return filter == NotificationManager.INTERRUPTION_FILTER_PRIORITY ||
                filter == NotificationManager.INTERRUPTION_FILTER_ALARMS ||
                filter == NotificationManager.INTERRUPTION_FILTER_NONE
    }

    /**
     * Policy 카테고리 빌드
     */
    private fun buildPolicyCategories(allowCalls: Boolean, allowAlarms: Boolean): Int {
        var categories = 0

        if (allowCalls) {
            categories = categories or NotificationManager.Policy.PRIORITY_CATEGORY_CALLS
            categories = categories or NotificationManager.Policy.PRIORITY_CATEGORY_REPEAT_CALLERS
        }

        if (allowAlarms) {
            categories = categories or NotificationManager.Policy.PRIORITY_CATEGORY_ALARMS
        }

        // 미디어 소리는 항상 허용
        categories = categories or NotificationManager.Policy.PRIORITY_CATEGORY_MEDIA

        return categories
    }

    /**
     * 이전 Interruption Filter 저장
     */
    @RequiresApi(Build.VERSION_CODES.M)
    private fun savePreviousInterruptionFilter() {
        val currentFilter = notificationManager.currentInterruptionFilter
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_PREVIOUS_FILTER, currentFilter)
            .apply()
    }

    /**
     * 이전 Interruption Filter 조회
     */
    private fun getPreviousInterruptionFilter(): Int {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_PREVIOUS_FILTER, NotificationManager.INTERRUPTION_FILTER_ALL)
    }

    /**
     * 저장된 Interruption Filter 삭제
     */
    private fun clearPreviousInterruptionFilter() {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_PREVIOUS_FILTER)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "dnd_manager"
        private const val KEY_PREVIOUS_FILTER = "previous_filter"
    }
}
