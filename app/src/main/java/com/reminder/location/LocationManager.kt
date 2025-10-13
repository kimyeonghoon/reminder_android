package com.reminder.location

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * 위치 관리자
 *
 * 사용자의 현재 위치를 가져오고, 위치 기반 리마인더 기능을 제공합니다.
 * v1.67.0: Geofencing API 통합 (자동 알림)
 */
class LocationManager(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val geofencingClient: GeofencingClient =
        LocationServices.getGeofencingClient(context)

    /**
     * 위치 권한이 부여되었는지 확인
     */
    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * 백그라운드 위치 권한이 부여되었는지 확인 (Android 10+)
     */
    fun hasBackgroundLocationPermission(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Android 10 미만에서는 항상 true
        }
    }

    /**
     * 현재 위치를 가져옵니다
     *
     * @return 현재 위치 (위도, 경도)
     * @throws SecurityException 위치 권한이 없는 경우
     * @throws Exception 위치를 가져올 수 없는 경우
     */
    suspend fun getCurrentLocation(): Pair<Double, Double> = suspendCancellableCoroutine { continuation ->
        if (!hasLocationPermission()) {
            continuation.resumeWithException(SecurityException("위치 권한이 필요합니다"))
            return@suspendCancellableCoroutine
        }

        try {
            val cancellationTokenSource = CancellationTokenSource()

            continuation.invokeOnCancellation {
                cancellationTokenSource.cancel()
            }

            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).addOnSuccessListener { location: Location? ->
                if (location != null) {
                    continuation.resume(Pair(location.latitude, location.longitude))
                } else {
                    continuation.resumeWithException(Exception("위치를 가져올 수 없습니다"))
                }
            }.addOnFailureListener { exception ->
                continuation.resumeWithException(exception)
            }
        } catch (e: SecurityException) {
            continuation.resumeWithException(e)
        }
    }

    /**
     * 마지막으로 알려진 위치를 가져옵니다 (빠르지만 오래된 위치일 수 있음)
     *
     * @return 마지막 위치 (위도, 경도) 또는 null
     */
    suspend fun getLastKnownLocation(): Pair<Double, Double>? = suspendCancellableCoroutine { continuation ->
        if (!hasLocationPermission()) {
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }

        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        continuation.resume(Pair(location.latitude, location.longitude))
                    } else {
                        continuation.resume(null)
                    }
                }
                .addOnFailureListener {
                    continuation.resume(null)
                }
        } catch (e: SecurityException) {
            continuation.resume(null)
        }
    }

    /**
     * 두 위치 간의 거리를 계산합니다 (미터 단위)
     *
     * @param lat1 위치 1의 위도
     * @param lon1 위치 1의 경도
     * @param lat2 위치 2의 위도
     * @param lon2 위치 2의 경도
     * @return 거리 (미터)
     */
    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0]
    }

    /**
     * 현재 위치가 특정 위치의 반경 내에 있는지 확인합니다
     *
     * @param targetLat 목표 위치의 위도
     * @param targetLon 목표 위치의 경도
     * @param radius 반경 (미터)
     * @return 반경 내에 있으면 true
     */
    suspend fun isWithinRadius(targetLat: Double, targetLon: Double, radius: Float): Boolean {
        return try {
            val (currentLat, currentLon) = getCurrentLocation()
            val distance = calculateDistance(currentLat, currentLon, targetLat, targetLon)
            distance <= radius
        } catch (e: Exception) {
            false
        }
    }

    /**
     * v1.67.0: 지오펜스 ID 생성
     *
     * @param reminderId 리마인더 ID
     * @return 지오펜스 ID (예: "reminder_geofence_123")
     */
    fun generateGeofenceId(reminderId: Long): String {
        return "reminder_geofence_$reminderId"
    }

    /**
     * v1.67.0: 지오펜스 파라미터 유효성 검증
     *
     * @param latitude 위도 (-90 ~ 90)
     * @param longitude 경도 (-180 ~ 180)
     * @param radius 반경 (최소 50m)
     * @return 유효하면 true
     */
    fun validateGeofenceParams(latitude: Double, longitude: Double, radius: Float): Boolean {
        return latitude in -90.0..90.0 &&
                longitude in -180.0..180.0 &&
                radius >= MIN_GEOFENCE_RADIUS
    }

    /**
     * v1.67.0: 지오펜스 등록 (위치 진입 시 자동 알림)
     *
     * @param reminderId 리마인더 ID
     * @param latitude 위도
     * @param longitude 경도
     * @param radius 반경 (미터)
     * @return 성공 여부
     */
    suspend fun setupGeofence(
        reminderId: Long,
        latitude: Double,
        longitude: Double,
        radius: Float
    ): Boolean = suspendCancellableCoroutine { continuation ->
        Log.d(TAG, "setupGeofence called: reminderId=$reminderId, lat=$latitude, lon=$longitude, radius=$radius")

        // 권한 확인
        if (!hasLocationPermission() || !hasBackgroundLocationPermission()) {
            Log.w(TAG, "setupGeofence failed: missing permissions")
            continuation.resume(false)
            return@suspendCancellableCoroutine
        }

        // 파라미터 검증
        if (!validateGeofenceParams(latitude, longitude, radius)) {
            Log.w(TAG, "setupGeofence failed: invalid parameters")
            continuation.resume(false)
            return@suspendCancellableCoroutine
        }

        try {
            val geofenceId = generateGeofenceId(reminderId)
            Log.d(TAG, "Generated geofence ID: $geofenceId")

            // 지오펜스 생성
            val geofence = Geofence.Builder()
                .setRequestId(geofenceId)
                .setCircularRegion(latitude, longitude, radius)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build()

            // 지오펜싱 요청 생성
            val geofencingRequest = GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build()

            // 지오펜스 등록
            geofencingClient.addGeofences(geofencingRequest, getGeofencePendingIntent())
                .addOnSuccessListener {
                    Log.i(TAG, "Geofence registered successfully: $geofenceId")
                    continuation.resume(true)
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Geofence registration failed: ${exception.message}", exception)
                    continuation.resume(false)
                }
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException during geofence setup: ${e.message}")
            continuation.resume(false)
        } catch (e: Exception) {
            Log.e(TAG, "Exception during geofence setup: ${e.message}", e)
            continuation.resume(false)
        }
    }

    /**
     * v1.67.0: 지오펜스 제거
     *
     * @param reminderId 리마인더 ID
     * @return 성공 여부
     */
    suspend fun removeGeofence(reminderId: Long): Boolean = suspendCancellableCoroutine { continuation ->
        try {
            val geofenceId = generateGeofenceId(reminderId)
            geofencingClient.removeGeofences(listOf(geofenceId))
                .addOnSuccessListener {
                    continuation.resume(true)
                }
                .addOnFailureListener {
                    continuation.resume(false)
                }
        } catch (e: Exception) {
            continuation.resume(false)
        }
    }

    /**
     * v1.67.0: 지오펜스 PendingIntent 생성
     *
     * BroadcastReceiver로 지오펜스 이벤트를 전달합니다.
     */
    private fun getGeofencePendingIntent(): PendingIntent {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    companion object {
        private const val TAG = "LocationManager"
        const val DEFAULT_RADIUS = 100f // 기본 반경 100미터
        const val MIN_GEOFENCE_RADIUS = 50f // 지오펜스 최소 반경 50미터
    }
}
