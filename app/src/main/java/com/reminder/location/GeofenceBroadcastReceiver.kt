package com.reminder.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.reminder.ReminderApplication
import com.reminder.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * v1.67.0: 지오펜스 이벤트 수신기
 *
 * 사용자가 지정된 위치에 진입하면 자동으로 리마인더 알림을 발송합니다.
 */
class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive called")
        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        // 에러 확인
        if (geofencingEvent == null) {
            Log.w(TAG, "GeofencingEvent is null")
            return
        }

        if (geofencingEvent.hasError()) {
            Log.e(TAG, "GeofencingEvent has error: ${geofencingEvent.errorCode}")
            return
        }

        // ENTER 이벤트만 처리 (위치 진입)
        val geofenceTransition = geofencingEvent.geofenceTransition
        Log.d(TAG, "Geofence transition: $geofenceTransition")

        if (!isEnterEvent(geofenceTransition)) {
            Log.d(TAG, "Not an ENTER event, ignoring")
            return
        }

        // 트리거된 지오펜스 목록
        val triggeringGeofences = geofencingEvent.triggeringGeofences
        if (triggeringGeofences == null) {
            Log.w(TAG, "No triggering geofences")
            return
        }

        Log.i(TAG, "Processing ${triggeringGeofences.size} triggered geofences")

        // 각 지오펜스에 대해 알림 발송
        triggeringGeofences.forEach { geofence ->
            Log.d(TAG, "Processing geofence: ${geofence.requestId}")
            val reminderId = extractReminderId(geofence.requestId)
            if (reminderId != null) {
                Log.i(TAG, "Sending notification for reminder ID: $reminderId")
                sendNotification(context, reminderId)
            } else {
                Log.w(TAG, "Could not extract reminder ID from geofence: ${geofence.requestId}")
            }
        }
    }

    /**
     * 지오펜스 ID에서 리마인더 ID 추출
     *
     * @param geofenceId 지오펜스 ID (예: "reminder_geofence_123")
     * @return 리마인더 ID (예: 123L) 또는 null
     */
    fun extractReminderId(geofenceId: String): Long? {
        return try {
            val prefix = "reminder_geofence_"
            if (geofenceId.startsWith(prefix)) {
                geofenceId.removePrefix(prefix).toLongOrNull()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * ENTER 이벤트인지 확인
     *
     * @param transitionType 지오펜스 전환 타입
     * @return ENTER 이벤트면 true
     */
    fun isEnterEvent(transitionType: Int): Boolean {
        return transitionType == Geofence.GEOFENCE_TRANSITION_ENTER
    }

    /**
     * 리마인더 알림 발송
     *
     * @param context Context
     * @param reminderId 리마인더 ID
     */
    private fun sendNotification(context: Context, reminderId: Long) {
        // CoroutineScope를 사용하여 비동기로 데이터베이스 조회
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val app = context.applicationContext as? ReminderApplication ?: return@launch
                val reminder = app.repository.getReminderById(reminderId) ?: return@launch

                // 알림 발송
                val notificationHelper = NotificationHelper(context)
                notificationHelper.showNotification(reminder)
            } catch (e: Exception) {
                // 에러 무시 (백그라운드 작업)
            }
        }
    }

    companion object {
        private const val TAG = "GeofenceBroadcastReceiver"
    }
}
