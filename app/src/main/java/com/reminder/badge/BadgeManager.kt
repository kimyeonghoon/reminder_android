package com.reminder.badge

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import me.leolin.shortcutbadger.ShortcutBadger

/**
 * 앱 아이콘 배지 관리자
 *
 * 미완료 리마인더 수를 앱 아이콘에 배지로 표시합니다.
 *
 * 기능:
 * - 배지 카운트 업데이트
 * - 배지 제거
 * - 배지 활성화/비활성화 설정 관리
 *
 * @param context Android Context
 */
class BadgeManager(private val context: Context) {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        name = "badge_preferences"
    )

    companion object {
        private val BADGE_ENABLED_KEY = booleanPreferencesKey("badge_enabled")
    }

    /**
     * 배지 활성화 상태 확인
     *
     * @return 배지 활성화 상태 Flow (기본값: true)
     */
    fun isBadgeEnabled(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[BADGE_ENABLED_KEY] ?: true // 기본값: 활성화
        }
    }

    /**
     * 배지 활성화 설정 변경
     *
     * @param enabled true: 활성화, false: 비활성화
     */
    suspend fun setBadgeEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[BADGE_ENABLED_KEY] = enabled
        }

        // 비활성화 시 배지 제거
        if (!enabled) {
            removeBadge()
        }
    }

    /**
     * 배지 카운트 업데이트
     *
     * @param count 배지에 표시할 숫자 (음수는 0으로 처리)
     * @return 성공 여부 (배지가 비활성화된 경우 false)
     */
    suspend fun updateBadgeCount(count: Int): Boolean {
        // 배지가 비활성화된 경우 업데이트하지 않음
        if (!isBadgeEnabled().first()) {
            return false
        }

        // 음수는 0으로 처리
        val safeCount = maxOf(0, count)

        return try {
            if (safeCount == 0) {
                // 카운트가 0이면 배지 제거
                ShortcutBadger.removeCount(context)
            } else {
                // 배지 카운트 업데이트
                ShortcutBadger.applyCount(context, safeCount)
            }
            true
        } catch (e: Exception) {
            // ShortcutBadger가 지원되지 않는 런처에서는 실패할 수 있음
            false
        }
    }

    /**
     * 배지 제거
     *
     * @return 성공 여부
     */
    fun removeBadge(): Boolean {
        return try {
            ShortcutBadger.removeCount(context)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 런처가 배지를 지원하는지 확인
     *
     * @return 지원 여부
     */
    fun isBadgeSupported(): Boolean {
        return ShortcutBadger.isBadgeCounterSupported(context)
    }
}
