package com.reminder.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
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
 */
class LocationManager(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

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

    companion object {
        const val DEFAULT_RADIUS = 100f // 기본 반경 100미터
    }
}
