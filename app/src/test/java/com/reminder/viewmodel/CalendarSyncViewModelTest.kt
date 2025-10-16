package com.reminder.viewmodel

import com.reminder.calendar.CalendarSyncManager
import com.reminder.calendar.DeviceCalendar
import com.reminder.calendar.DeviceCalendarProvider
import com.reminder.data.dao.ReminderDao
import com.reminder.data.entity.SyncDirection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

/**
 * CalendarSyncViewModel 테스트
 *
 * AAA 패턴 (Given-When-Then)으로 캘린더 동기화 메서드 검증
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CalendarSyncViewModelTest {

    private lateinit var calendarSyncManager: CalendarSyncManager
    private lateinit var deviceCalendarProvider: DeviceCalendarProvider
    private lateinit var reminderDao: ReminderDao
    private lateinit var viewModel: CalendarSyncViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        calendarSyncManager = mock(CalendarSyncManager::class.java)
        deviceCalendarProvider = mock(DeviceCalendarProvider::class.java)
        reminderDao = mock(ReminderDao::class.java)

        // 기본적으로 권한 없음으로 설정
        `when`(deviceCalendarProvider.hasCalendarPermission()).thenReturn(false)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /** 초기 상태는 동기화 중이 아니고 에러가 없다 */
    @Test
    fun initialStateIsNotSyncingAndNoError() {
        // When
        viewModel = CalendarSyncViewModel(calendarSyncManager, deviceCalendarProvider, reminderDao)

        // Then
        assertFalse(viewModel.isSyncing.value)
        assertNull(viewModel.syncError.value)
        assertEquals(emptyList<DeviceCalendar>(), viewModel.deviceCalendars.value)
    }

    /** checkCalendarPermission는 권한이 있으면 캘린더 목록을 로드한다 */
    @Test
    fun checkCalendarPermissionLoadsCalendarsWhenPermissionGranted() = runTest {
        // Given
        val calendars = listOf(
            DeviceCalendar("cal1", "캘린더 1", "account1", 0xFF0000),
            DeviceCalendar("cal2", "캘린더 2", "account2", 0x00FF00)
        )
        whenever(deviceCalendarProvider.hasCalendarPermission()).thenReturn(true)
        whenever(calendarSyncManager.getDeviceCalendars()).thenReturn(calendars)
        viewModel = CalendarSyncViewModel(calendarSyncManager, deviceCalendarProvider, reminderDao)

        // When
        viewModel.checkCalendarPermission()
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.hasCalendarPermission.value)
        assertEquals(2, viewModel.deviceCalendars.value.size)
    }

    /** checkCalendarPermission는 권한이 없으면 캘린더를 로드하지 않는다 */
    @Test
    fun checkCalendarPermissionDoesNotLoadCalendarsWhenPermissionDenied() {
        // Given
        whenever(deviceCalendarProvider.hasCalendarPermission()).thenReturn(false)
        viewModel = CalendarSyncViewModel(calendarSyncManager, deviceCalendarProvider, reminderDao)

        // When
        viewModel.checkCalendarPermission()

        // Then
        assertFalse(viewModel.hasCalendarPermission.value)
        assertEquals(emptyList<DeviceCalendar>(), viewModel.deviceCalendars.value)
        verify(calendarSyncManager, never()).getDeviceCalendars()
    }

    /** loadDeviceCalendars는 캘린더 목록을 로드한다 */
    @Test
    fun loadDeviceCalendarsLoadsCalendars() = runTest {
        // Given
        val calendars = listOf(
            DeviceCalendar("cal1", "캘린더 1", "account1", 0xFF0000)
        )
        whenever(calendarSyncManager.getDeviceCalendars()).thenReturn(calendars)
        viewModel = CalendarSyncViewModel(calendarSyncManager, deviceCalendarProvider, reminderDao)

        // When
        viewModel.loadDeviceCalendars()
        advanceUntilIdle()

        // Then
        assertEquals(1, viewModel.deviceCalendars.value.size)
        assertEquals("캘린더 1", viewModel.deviceCalendars.value[0].name)
    }

    /** loadDeviceCalendars는 오류 발생 시 에러 메시지를 설정한다 */
    @Test
    fun loadDeviceCalendarsSetsErrorOnFailure() = runTest {
        // Given
        val errorMsg = "캘린더 로드 실패"
        whenever(calendarSyncManager.getDeviceCalendars()).thenThrow(RuntimeException(errorMsg))
        viewModel = CalendarSyncViewModel(calendarSyncManager, deviceCalendarProvider, reminderDao)

        // When
        viewModel.loadDeviceCalendars()
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.syncError.value?.contains(errorMsg) == true)
    }

    /** toggleCalendarSync는 캘린더 동기화를 활성화한다 */
    @Test
    fun toggleCalendarSyncEnablesSync() = runTest {
        // Given
        viewModel = CalendarSyncViewModel(calendarSyncManager, deviceCalendarProvider, reminderDao)
        val calendarId = "cal1"
        whenever(calendarSyncManager.toggleCalendarSync(any(), any())).thenReturn(Unit)

        // When
        viewModel.toggleCalendarSync(calendarId, true)
        advanceUntilIdle()

        // Then
        verify(calendarSyncManager).toggleCalendarSync(calendarId, true)
    }

    /** toggleCalendarSync는 캘린더 동기화를 비활성화한다 */
    @Test
    fun toggleCalendarSyncDisablesSync() = runTest {
        // Given
        viewModel = CalendarSyncViewModel(calendarSyncManager, deviceCalendarProvider, reminderDao)
        val calendarId = "cal1"
        whenever(calendarSyncManager.toggleCalendarSync(any(), any())).thenReturn(Unit)

        // When
        viewModel.toggleCalendarSync(calendarId, false)
        advanceUntilIdle()

        // Then
        verify(calendarSyncManager).toggleCalendarSync(calendarId, false)
    }

    /** toggleCalendarSync는 오류 발생 시 에러 메시지를 설정한다 */
    @Test
    fun toggleCalendarSyncSetsErrorOnFailure() = runTest {
        // Given
        viewModel = CalendarSyncViewModel(calendarSyncManager, deviceCalendarProvider, reminderDao)
        val errorMsg = "동기화 설정 실패"
        whenever(calendarSyncManager.toggleCalendarSync(any(), any())).thenThrow(RuntimeException(errorMsg))

        // When
        viewModel.toggleCalendarSync("cal1", true)
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.syncError.value?.contains(errorMsg) == true)
    }

    /** addCalendarSync는 캘린더 동기화를 추가한다 */
    @Test
    fun addCalendarSyncAddsCalendarSync() = runTest {
        // Given
        viewModel = CalendarSyncViewModel(calendarSyncManager, deviceCalendarProvider, reminderDao)
        val deviceCalendar = DeviceCalendar("cal1", "캘린더 1", "account1", 0xFF0000)
        whenever(calendarSyncManager.addCalendarSyncConfig(any(), any(), any(), any(), any())).thenReturn(1L)

        // When
        viewModel.addCalendarSync(deviceCalendar)
        advanceUntilIdle()

        // Then
        verify(calendarSyncManager).addCalendarSyncConfig(
            calendarId = "cal1",
            calendarName = "캘린더 1",
            accountName = "account1",
            calendarColor = 0xFF0000,
            syncDirection = SyncDirection.TWO_WAY
        )
    }

    /** addCalendarSync는 사용자 지정 동기화 방향을 사용한다 */
    @Test
    fun addCalendarSyncUsesCustomSyncDirection() = runTest {
        // Given
        viewModel = CalendarSyncViewModel(calendarSyncManager, deviceCalendarProvider, reminderDao)
        val deviceCalendar = DeviceCalendar("cal1", "캘린더 1", "account1", 0xFF0000)
        whenever(calendarSyncManager.addCalendarSyncConfig(any(), any(), any(), any(), any())).thenReturn(1L)

        // When
        viewModel.addCalendarSync(deviceCalendar, SyncDirection.ONE_WAY)
        advanceUntilIdle()

        // Then
        verify(calendarSyncManager).addCalendarSyncConfig(
            calendarId = "cal1",
            calendarName = "캘린더 1",
            accountName = "account1",
            calendarColor = 0xFF0000,
            syncDirection = SyncDirection.ONE_WAY
        )
    }

    /** addCalendarSync는 오류 발생 시 에러 메시지를 설정한다 */
    @Test
    fun addCalendarSyncSetsErrorOnFailure() = runTest {
        // Given
        viewModel = CalendarSyncViewModel(calendarSyncManager, deviceCalendarProvider, reminderDao)
        val deviceCalendar = DeviceCalendar("cal1", "캘린더 1", "account1", 0xFF0000)
        val errorMsg = "캘린더 추가 실패"
        whenever(calendarSyncManager.addCalendarSyncConfig(any(), any(), any(), any(), any()))
            .thenThrow(RuntimeException(errorMsg))

        // When
        viewModel.addCalendarSync(deviceCalendar)
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.syncError.value?.contains(errorMsg) == true)
    }

    /** syncAllReminders는 모든 리마인더를 동기화한다 */
    @Test
    fun syncAllRemindersSyncsAllReminders() = runTest {
        // Given
        viewModel = CalendarSyncViewModel(calendarSyncManager, deviceCalendarProvider, reminderDao)
        whenever(calendarSyncManager.syncAllReminders()).thenReturn(Unit)

        // When
        viewModel.syncAllReminders()
        advanceUntilIdle()

        // Then
        verify(calendarSyncManager).syncAllReminders()
        assertFalse(viewModel.isSyncing.value)
        assertNull(viewModel.syncError.value)
    }

    /** syncAllReminders는 실행 중 동기화 상태를 true로 설정한다 */
    @Test
    fun syncAllRemindersSetsIsSyncingToTrueDuringExecution() = runTest {
        // Given
        viewModel = CalendarSyncViewModel(calendarSyncManager, deviceCalendarProvider, reminderDao)
        var syncingDuringExecution = false
        whenever(calendarSyncManager.syncAllReminders()).then {
            syncingDuringExecution = viewModel.isSyncing.value
            Unit
        }

        // When
        viewModel.syncAllReminders()
        advanceUntilIdle()

        // Then
        assertTrue(syncingDuringExecution)
        assertFalse(viewModel.isSyncing.value) // 완료 후에는 false
    }

    /** syncAllReminders는 오류 발생 시 에러 메시지를 설정하고 동기화 상태를 false로 변경한다 */
    @Test
    fun syncAllRemindersSetsErrorAndStopsSyncingOnFailure() = runTest {
        // Given
        viewModel = CalendarSyncViewModel(calendarSyncManager, deviceCalendarProvider, reminderDao)
        val errorMsg = "동기화 실패"
        whenever(calendarSyncManager.syncAllReminders()).thenThrow(RuntimeException(errorMsg))

        // When
        viewModel.syncAllReminders()
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.syncError.value?.contains(errorMsg) == true)
        assertFalse(viewModel.isSyncing.value)
    }

    /** clearError는 에러 메시지를 초기화한다 */
    @Test
    fun clearErrorClearsErrorMessage() = runTest {
        // Given
        viewModel = CalendarSyncViewModel(calendarSyncManager, deviceCalendarProvider, reminderDao)
        whenever(calendarSyncManager.getDeviceCalendars()).thenThrow(RuntimeException("오류"))
        viewModel.loadDeviceCalendars()
        advanceUntilIdle()
        assertNotNull(viewModel.syncError.value)

        // When
        viewModel.clearError()

        // Then
        assertNull(viewModel.syncError.value)
    }

    /** 여러 캘린더를 연속으로 추가할 수 있다 */
    @Test
    fun canAddMultipleCalendarsSequentially() = runTest {
        // Given
        viewModel = CalendarSyncViewModel(calendarSyncManager, deviceCalendarProvider, reminderDao)
        val calendar1 = DeviceCalendar("cal1", "캘린더 1", "account1", 0xFF0000)
        val calendar2 = DeviceCalendar("cal2", "캘린더 2", "account2", 0x00FF00)
        whenever(calendarSyncManager.addCalendarSyncConfig(any(), any(), any(), any(), any())).thenReturn(1L)

        // When
        viewModel.addCalendarSync(calendar1)
        advanceUntilIdle()
        viewModel.addCalendarSync(calendar2)
        advanceUntilIdle()

        // Then
        verify(calendarSyncManager, times(2)).addCalendarSyncConfig(any(), any(), any(), any(), any())
    }
}
