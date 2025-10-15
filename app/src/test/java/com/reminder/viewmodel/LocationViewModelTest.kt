package com.reminder.viewmodel

import com.reminder.analytics.AnalyticsHelper
import com.reminder.data.entity.Priority
import com.reminder.data.entity.ReminderEntity
import com.reminder.data.repository.ReminderRepository
import com.reminder.location.LocationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.whenever
import java.time.LocalDateTime

/**
 * LocationViewModel 테스트
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LocationViewModelTest {

    @get:Rule
    val mockitoRule: MockitoRule = MockitoJUnit.rule()

    private val testDispatcher = UnconfinedTestDispatcher()

    @Mock
    private lateinit var locationManager: LocationManager

    @Mock
    private lateinit var repository: ReminderRepository

    @Mock
    private lateinit var analyticsHelper: AnalyticsHelper

    private lateinit var viewModel: LocationViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = LocationViewModel(locationManager, repository, analyticsHelper)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `hasLocationPermission returns permission status`() {
        // given: 위치 권한이 있음
        whenever(locationManager.hasLocationPermission()).thenReturn(true)

        // when: hasLocationPermission 호출
        val result = viewModel.hasLocationPermission()

        // then: true 반환
        assert(result == true)
        verify(locationManager).hasLocationPermission()
    }

    @Test
    fun `hasBackgroundLocationPermission returns permission status`() {
        // given: 백그라운드 위치 권한이 없음
        whenever(locationManager.hasBackgroundLocationPermission()).thenReturn(false)

        // when: hasBackgroundLocationPermission 호출
        val result = viewModel.hasBackgroundLocationPermission()

        // then: false 반환
        assert(result == false)
        verify(locationManager).hasBackgroundLocationPermission()
    }

    @Test
    fun `getCurrentLocation returns current location`() = runTest(testDispatcher) {
        // given: 현재 위치
        val expectedLocation = Pair(37.5665, 126.9780) // 서울시청
        whenever(locationManager.getCurrentLocation()).thenReturn(expectedLocation)

        // when: getCurrentLocation 호출
        val result = viewModel.getCurrentLocation()

        // then: 위치 반환
        assert(result == expectedLocation)
    }

    @Test
    fun `getCurrentLocation returns null on exception`() = runTest(testDispatcher) {
        // given: 위치 가져오기 실패
        whenever(locationManager.getCurrentLocation()).thenThrow(RuntimeException("Location error"))

        // when: getCurrentLocation 호출
        val result = viewModel.getCurrentLocation()

        // then: null 반환
        assert(result == null)
    }

    @Test
    fun `getLastKnownLocation returns last known location`() = runTest(testDispatcher) {
        // given: 마지막 알려진 위치
        val expectedLocation = Pair(37.5665, 126.9780)
        whenever(locationManager.getLastKnownLocation()).thenReturn(expectedLocation)

        // when: getLastKnownLocation 호출
        val result = viewModel.getLastKnownLocation()

        // then: 위치 반환
        assert(result == expectedLocation)
    }

    @Test
    fun `addLocationToReminder updates reminder with location`() = runTest(testDispatcher) {
        // given: 리마인더와 위치 정보
        val reminder = ReminderEntity(
            id = 1,
            title = "Test Reminder",
            priority = Priority.MEDIUM
        )
        val latitude = 37.5665
        val longitude = 126.9780
        val locationName = "서울시청"
        val radius = 100f

        // when: addLocationToReminder 호출
        viewModel.addLocationToReminder(reminder, latitude, longitude, locationName, radius)
        testScheduler.advanceUntilIdle()

        // then: repository update 및 geofence 등록, analytics 로깅
        val captor = argumentCaptor<ReminderEntity>()
        verify(repository).updateReminder(captor.capture())
        val updatedReminder = captor.firstValue

        assert(updatedReminder.locationLatitude == latitude)
        assert(updatedReminder.locationLongitude == longitude)
        assert(updatedReminder.locationName == locationName)
        assert(updatedReminder.locationRadius == radius)

        verify(locationManager).setupGeofence(
            reminderId = reminder.id,
            latitude = latitude,
            longitude = longitude,
            radius = radius
        )
        verify(analyticsHelper).logLocationAdded()
    }

    @Test
    fun `removeLocationFromReminder clears location from reminder`() = runTest(testDispatcher) {
        // given: 위치가 있는 리마인더
        val reminder = ReminderEntity(
            id = 1,
            title = "Test Reminder",
            priority = Priority.MEDIUM,
            locationLatitude = 37.5665,
            locationLongitude = 126.9780,
            locationName = "서울시청",
            locationRadius = 100f
        )

        // when: removeLocationFromReminder 호출
        viewModel.removeLocationFromReminder(reminder)
        testScheduler.advanceUntilIdle()

        // then: repository update 및 geofence 제거
        val captor = argumentCaptor<ReminderEntity>()
        verify(repository).updateReminder(captor.capture())
        val updatedReminder = captor.firstValue

        assert(updatedReminder.locationLatitude == null)
        assert(updatedReminder.locationLongitude == null)
        assert(updatedReminder.locationName == null)
        assert(updatedReminder.locationRadius == null)

        verify(locationManager).removeGeofence(reminder.id)
    }

    @Test
    fun `isWithinReminderRadius returns true when within radius`() = runTest(testDispatcher) {
        // given: 위치가 있는 리마인더
        val reminder = ReminderEntity(
            id = 1,
            title = "Test Reminder",
            priority = Priority.MEDIUM,
            locationLatitude = 37.5665,
            locationLongitude = 126.9780,
            locationRadius = 100f
        )
        whenever(locationManager.isWithinRadius(37.5665, 126.9780, 100f)).thenReturn(true)

        // when: isWithinReminderRadius 호출
        val result = viewModel.isWithinReminderRadius(reminder)

        // then: true 반환
        assert(result == true)
    }

    @Test
    fun `isWithinReminderRadius returns false when location is null`() = runTest(testDispatcher) {
        // given: 위치가 없는 리마인더
        val reminder = ReminderEntity(
            id = 1,
            title = "Test Reminder",
            priority = Priority.MEDIUM
        )

        // when: isWithinReminderRadius 호출
        val result = viewModel.isWithinReminderRadius(reminder)

        // then: false 반환
        assert(result == false)
    }
}
