package com.reminder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reminder.analytics.AnalyticsHelper
import com.reminder.data.entity.ReminderEntity
import com.reminder.data.repository.ReminderRepository
import com.reminder.location.LocationManager
import kotlinx.coroutines.launch
import java.time.LocalDateTime

/**
 * v1.68.1: Location 전용 ViewModel
 *
 * ReminderViewModel에서 분리됨 (God Class 리팩토링)
 * 위치 기반 리마인더 및 지오펜싱 기능 담당
 */
class LocationViewModel(
    private val locationManager: LocationManager,
    private val repository: ReminderRepository,
    private val analyticsHelper: AnalyticsHelper
) : ViewModel() {

    /**
     * 위치 권한 확인
     */
    fun hasLocationPermission(): Boolean {
        return locationManager.hasLocationPermission()
    }

    /**
     * 백그라운드 위치 권한 확인
     */
    fun hasBackgroundLocationPermission(): Boolean {
        return locationManager.hasBackgroundLocationPermission()
    }

    /**
     * 현재 위치 가져오기
     */
    suspend fun getCurrentLocation(): Pair<Double, Double>? {
        return try {
            locationManager.getCurrentLocation()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 마지막으로 알려진 위치 가져오기
     */
    suspend fun getLastKnownLocation(): Pair<Double, Double>? {
        return locationManager.getLastKnownLocation()
    }

    /**
     * 리마인더에 위치 추가
     *
     * v1.67.0: 지오펜스 자동 등록
     */
    fun addLocationToReminder(
        reminder: ReminderEntity,
        latitude: Double,
        longitude: Double,
        locationName: String,
        radius: Float = LocationManager.DEFAULT_RADIUS
    ) {
        viewModelScope.launch {
            val updated = reminder.copy(
                locationLatitude = latitude,
                locationLongitude = longitude,
                locationName = locationName,
                locationRadius = radius,
                updatedAt = LocalDateTime.now()
            )
            repository.updateReminder(updated)

            // v1.67.0: 지오펜스 등록
            locationManager.setupGeofence(
                reminderId = updated.id,
                latitude = latitude,
                longitude = longitude,
                radius = radius
            )

            // Analytics 이벤트 로깅
            analyticsHelper.logLocationAdded()
        }
    }

    /**
     * 리마인더에서 위치 제거
     *
     * v1.67.0: 지오펜스 자동 제거
     */
    fun removeLocationFromReminder(reminder: ReminderEntity) {
        viewModelScope.launch {
            val updated = reminder.copy(
                locationLatitude = null,
                locationLongitude = null,
                locationName = null,
                locationRadius = null,
                updatedAt = LocalDateTime.now()
            )
            repository.updateReminder(updated)

            // v1.67.0: 지오펜스 제거
            locationManager.removeGeofence(reminder.id)
        }
    }

    /**
     * 현재 위치가 리마인더 위치 범위 내에 있는지 확인
     */
    suspend fun isWithinReminderRadius(reminder: ReminderEntity): Boolean {
        val lat = reminder.locationLatitude ?: return false
        val lon = reminder.locationLongitude ?: return false
        val radius = reminder.locationRadius ?: LocationManager.DEFAULT_RADIUS

        return locationManager.isWithinRadius(lat, lon, radius)
    }
}
