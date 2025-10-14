package com.reminder.viewmodel

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * v1.68.0: MapViewModel 유닛 테스트
 *
 * 지도 상태 관리 및 위치 선택 로직 검증
 */
class MapViewModelTest {

    private lateinit var viewModel: MapViewModel

    @Before
    fun setup() {
        viewModel = MapViewModel()
    }

    /**
     * 초기 상태에서 선택된 위치가 없어야 한다
     */
    @Test
    fun initialStateShouldHaveNoSelectedLocation() {
        // Given: 초기 ViewModel

        // When: 초기 상태 확인

        // Then: 선택된 위치가 없음
        assertNull(viewModel.selectedLocation.value)
    }

    /**
     * 위치를 설정하면 선택된 위치가 업데이트되어야 한다
     */
    @Test
    fun setLocationShouldUpdateSelectedLocation() {
        // Given: 테스트 위치 (서울 시청)
        val latitude = 37.5666805
        val longitude = 126.9784147
        val name = "서울 시청"

        // When: 위치 설정
        viewModel.setLocation(latitude, longitude, name)

        // Then: 선택된 위치가 업데이트됨
        val location = viewModel.selectedLocation.value
        assertNotNull(location)
        assertEquals(latitude, location!!.latitude, 0.0001)
        assertEquals(longitude, location.longitude, 0.0001)
        assertEquals(name, location.name)
    }

    /**
     * 지도 중심점을 변경하면 반영되어야 한다
     */
    @Test
    fun updateMapCenterShouldChangeMapCenter() {
        // Given: 초기 위치 설정
        viewModel.setLocation(37.5666805, 126.9784147, "서울 시청")

        // When: 지도 중심점 변경 (강남역)
        val newLatitude = 37.4979462
        val newLongitude = 127.0276368
        viewModel.updateMapCenter(newLatitude, newLongitude)

        // Then: 지도 중심점이 변경됨
        val center = viewModel.mapCenter.value
        assertNotNull(center)
        assertEquals(newLatitude, center!!.first, 0.0001)
        assertEquals(newLongitude, center.second, 0.0001)
    }

    /**
     * 마커를 추가하면 마커 위치가 설정되어야 한다
     */
    @Test
    fun addMarkerShouldSetMarkerPosition() {
        // Given: 테스트 위치
        val latitude = 37.5666805
        val longitude = 126.9784147

        // When: 마커 추가
        viewModel.addMarker(latitude, longitude)

        // Then: 마커 위치가 설정됨
        val marker = viewModel.markerPosition.value
        assertNotNull(marker)
        assertEquals(latitude, marker!!.first, 0.0001)
        assertEquals(longitude, marker.second, 0.0001)
    }

    /**
     * 마커를 제거하면 마커 위치가 null이 되어야 한다
     */
    @Test
    fun clearMarkerShouldRemoveMarkerPosition() {
        // Given: 마커가 있는 상태
        viewModel.addMarker(37.5666805, 126.9784147)
        assertNotNull(viewModel.markerPosition.value)

        // When: 마커 제거
        viewModel.clearMarker()

        // Then: 마커 위치가 null
        assertNull(viewModel.markerPosition.value)
    }

    /**
     * 선택된 위치를 초기화하면 모든 상태가 초기화되어야 한다
     */
    @Test
    fun clearLocationShouldResetAllStates() {
        // Given: 위치와 마커가 설정된 상태
        viewModel.setLocation(37.5666805, 126.9784147, "서울 시청")
        viewModel.addMarker(37.5666805, 126.9784147)
        assertNotNull(viewModel.selectedLocation.value)
        assertNotNull(viewModel.markerPosition.value)

        // When: 위치 초기화
        viewModel.clearLocation()

        // Then: 모든 상태가 초기화됨
        assertNull(viewModel.selectedLocation.value)
        assertNull(viewModel.markerPosition.value)
    }

    /**
     * 지도 줌 레벨을 변경하면 반영되어야 한다
     */
    @Test
    fun setZoomLevelShouldUpdateZoomLevel() {
        // Given: 초기 줌 레벨 (기본값)

        // When: 줌 레벨 변경
        val newZoomLevel = 15
        viewModel.setZoomLevel(newZoomLevel)

        // Then: 줌 레벨이 변경됨
        assertEquals(newZoomLevel, viewModel.zoomLevel.value)
    }

    /**
     * 현재 위치로 이동 요청 시 상태가 업데이트되어야 한다
     */
    @Test
    fun requestCurrentLocationShouldUpdateState() {
        // Given: 초기 상태

        // When: 현재 위치 요청
        viewModel.requestCurrentLocation()

        // Then: 현재 위치 요청 상태가 true
        assertTrue(viewModel.shouldMoveToCurrentLocation.value)
    }

    /**
     * 현재 위치 이동 완료 후 상태를 리셋해야 한다
     */
    @Test
    fun completeCurrentLocationMoveShouldResetState() {
        // Given: 현재 위치 요청 상태
        viewModel.requestCurrentLocation()
        assertTrue(viewModel.shouldMoveToCurrentLocation.value)

        // When: 이동 완료
        viewModel.completeCurrentLocationMove()

        // Then: 상태가 false로 리셋
        assertFalse(viewModel.shouldMoveToCurrentLocation.value)
    }
}
