package com.reminder.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * v1.68.0: 지도 화면 ViewModel
 *
 * 지도 상태 관리 (중심점, 마커, 줌 레벨 등)
 */
class MapViewModel : ViewModel() {

    /**
     * 선택된 위치 정보
     */
    data class LocationInfo(
        val latitude: Double,
        val longitude: Double,
        val name: String
    )

    // 선택된 위치
    private val _selectedLocation = MutableStateFlow<LocationInfo?>(null)
    val selectedLocation: StateFlow<LocationInfo?> = _selectedLocation.asStateFlow()

    // 지도 중심점 (위도, 경도)
    private val _mapCenter = MutableStateFlow<Pair<Double, Double>?>(null)
    val mapCenter: StateFlow<Pair<Double, Double>?> = _mapCenter.asStateFlow()

    // 마커 위치 (위도, 경도)
    private val _markerPosition = MutableStateFlow<Pair<Double, Double>?>(null)
    val markerPosition: StateFlow<Pair<Double, Double>?> = _markerPosition.asStateFlow()

    // 줌 레벨 (기본값: 15 - 도시 블록 레벨)
    private val _zoomLevel = MutableStateFlow(15)
    val zoomLevel: StateFlow<Int> = _zoomLevel.asStateFlow()

    // 현재 위치로 이동 요청 플래그
    private val _shouldMoveToCurrentLocation = MutableStateFlow(false)
    val shouldMoveToCurrentLocation: StateFlow<Boolean> = _shouldMoveToCurrentLocation.asStateFlow()

    /**
     * 위치 설정 (장소 검색 결과 또는 직접 선택)
     */
    fun setLocation(latitude: Double, longitude: Double, name: String) {
        _selectedLocation.value = LocationInfo(latitude, longitude, name)
        _mapCenter.value = Pair(latitude, longitude)
        _markerPosition.value = Pair(latitude, longitude)
    }

    /**
     * 지도 중심점 변경 (사용자가 지도를 드래그할 때)
     */
    fun updateMapCenter(latitude: Double, longitude: Double) {
        _mapCenter.value = Pair(latitude, longitude)
    }

    /**
     * 마커 추가 (지도 롱프레스 등)
     */
    fun addMarker(latitude: Double, longitude: Double) {
        _markerPosition.value = Pair(latitude, longitude)
    }

    /**
     * 마커 제거
     */
    fun clearMarker() {
        _markerPosition.value = null
    }

    /**
     * 선택된 위치 초기화
     */
    fun clearLocation() {
        _selectedLocation.value = null
        _markerPosition.value = null
        _mapCenter.value = null
    }

    /**
     * 줌 레벨 변경
     *
     * @param level 줌 레벨 (1~21, 낮을수록 넓은 범위)
     */
    fun setZoomLevel(level: Int) {
        _zoomLevel.value = level.coerceIn(1, 21)
    }

    /**
     * 현재 위치로 이동 요청
     */
    fun requestCurrentLocation() {
        _shouldMoveToCurrentLocation.value = true
    }

    /**
     * 현재 위치 이동 완료 처리
     */
    fun completeCurrentLocationMove() {
        _shouldMoveToCurrentLocation.value = false
    }
}
