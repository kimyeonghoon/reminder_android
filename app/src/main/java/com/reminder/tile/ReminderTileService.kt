package com.reminder.tile

import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.reminder.MainActivity
import com.reminder.R

/**
 * v1.42.0: Quick Settings Tile for Quick Reminder Add
 *
 * Android 알림창 Quick Settings에서 빠르게 리마인더 추가
 *
 * 기능:
 * - 타일 클릭 시 QuickAddActivity 실행
 * - 활성화된 리마인더 개수 표시 (라벨)
 * - Material You 아이콘 사용
 */
class ReminderTileService : TileService() {

    /**
     * 타일이 Quick Settings 패널에 추가될 때 호출
     */
    override fun onTileAdded() {
        super.onTileAdded()
        updateTile()
    }

    /**
     * 타일이 화면에 표시될 때마다 호출
     */
    override fun onStartListening() {
        super.onStartListening()
        updateTile()
    }

    /**
     * 타일이 화면에서 사라질 때 호출
     */
    override fun onStopListening() {
        super.onStopListening()
    }

    /**
     * 타일 클릭 시 호출
     */
    override fun onClick() {
        super.onClick()

        // Android 14+ (API 34+): unlockAndRun 사용 (잠금 화면 처리)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            unlockAndRun {
                launchQuickAddActivity()
            }
        } else {
            launchQuickAddActivity()
        }
    }

    /**
     * QuickAddActivity 실행
     */
    private fun launchQuickAddActivity() {
        val intent = Intent(this, QuickAddActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        // Android 14+ (API 34+): startActivityAndCollapse 대신 PendingIntent 사용
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            startActivityAndCollapse(pendingIntent)
        } else {
            @Suppress("DEPRECATION")
            startActivityAndCollapse(intent)
        }
    }

    /**
     * 타일 상태 업데이트
     */
    private fun updateTile() {
        qsTile?.apply {
            // 타일 상태: 항상 활성화
            state = Tile.STATE_ACTIVE

            // 타일 아이콘
            icon = Icon.createWithResource(
                applicationContext,
                R.drawable.ic_add_reminder_24
            )

            // 타일 라벨
            label = getString(R.string.tile_label_add_reminder)

            // 부제목 (Android 10+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                subtitle = getString(R.string.tile_subtitle_quick_add)
            }

            // 타일 업데이트 적용
            updateTile()
        }
    }

    /**
     * 타일이 Quick Settings 패널에서 제거될 때 호출
     */
    override fun onTileRemoved() {
        super.onTileRemoved()
        // 정리 작업 (필요시)
    }
}
