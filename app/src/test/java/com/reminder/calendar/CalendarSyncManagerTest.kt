package com.reminder.calendar

import com.reminder.data.dao.CalendarSyncConfigDao
import com.reminder.data.dao.ReminderDao
import com.reminder.data.entity.CalendarSyncConfig
import com.reminder.data.entity.ReminderEntity
import com.reminder.data.entity.SyncDirection
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.time.LocalDateTime
import org.junit.Assert.*

/**
 * CalendarSyncManager 테스트
 *
 * AAA 패턴 (Given-When-Then)으로 캘린더 동기화 검증
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CalendarSyncManagerTest {

    private lateinit var deviceCalendarProvider: DeviceCalendarProvider
    private lateinit var calendarSyncConfigDao: CalendarSyncConfigDao
    private lateinit var reminderDao: ReminderDao
    private lateinit var manager: CalendarSyncManager

    @Before
    fun setup() {
        deviceCalendarProvider = mock()
        calendarSyncConfigDao = mock()
        reminderDao = mock()
        manager = CalendarSyncManager(deviceCalendarProvider, calendarSyncConfigDao, reminderDao)
    }

    /** syncReminderToCalendar는 활성화된 캘린더에 리마인더를 동기화한다 */
    @Test
    fun syncReminderToCalendarSyncsToEnabledCalendars() = runTest {
        // Given
        val reminder = createReminder(id = 1, title = "테스트")
        val config = createConfig(calendarId = "cal1", isSyncEnabled = true)
        whenever(calendarSyncConfigDao.getEnabledConfigs()).thenReturn(listOf(config))
        whenever(deviceCalendarProvider.addReminderToCalendar(any(), any())).thenReturn(1L)

        // When
        manager.syncReminderToCalendar(reminder)

        // Then
        verify(deviceCalendarProvider).addReminderToCalendar(reminder, "cal1")
        verify(calendarSyncConfigDao).updateConfig(any())
    }

    /** syncReminderToCalendar는 이벤트 생성 실패 시 동기화 시간을 업데이트하지 않는다 */
    @Test
    fun syncReminderToCalendarDoesNotUpdateOnFailure() = runTest {
        // Given
        val reminder = createReminder(id = 1)
        val config = createConfig(calendarId = "cal1")
        whenever(calendarSyncConfigDao.getEnabledConfigs()).thenReturn(listOf(config))
        whenever(deviceCalendarProvider.addReminderToCalendar(any(), any())).thenReturn(null)

        // When
        manager.syncReminderToCalendar(reminder)

        // Then
        verify(calendarSyncConfigDao, never()).updateConfig(any())
    }

    /** syncAllReminders는 모든 미완료 리마인더를 동기화한다 */
    @Test
    fun syncAllRemindersSyncsAllIncompleteReminders() = runTest {
        // Given
        val reminders = listOf(
            createReminder(id = 1, dueDateTime = LocalDateTime.now(), isCompleted = false),
            createReminder(id = 2, dueDateTime = LocalDateTime.now(), isCompleted = false),
            createReminder(id = 3, dueDateTime = null), // 날짜 없음
            createReminder(id = 4, dueDateTime = LocalDateTime.now(), isCompleted = true) // 완료됨
        )
        val config = createConfig(calendarId = "cal1")
        whenever(reminderDao.getAllReminders()).thenReturn(flowOf(reminders))
        whenever(calendarSyncConfigDao.getEnabledConfigs()).thenReturn(listOf(config))
        whenever(deviceCalendarProvider.addReminderToCalendar(any(), any())).thenReturn(1L)

        // When
        manager.syncAllReminders()

        // Then
        verify(deviceCalendarProvider, times(2)).addReminderToCalendar(any(), eq("cal1"))
        verify(calendarSyncConfigDao).updateConfig(any())
    }

    /** syncAllReminders는 여러 캘린더에 동기화한다 */
    @Test
    fun syncAllRemindersSyncsToMultipleCalendars() = runTest {
        // Given
        val reminders = listOf(createReminder(id = 1, dueDateTime = LocalDateTime.now()))
        val configs = listOf(
            createConfig(calendarId = "cal1"),
            createConfig(calendarId = "cal2")
        )
        whenever(reminderDao.getAllReminders()).thenReturn(flowOf(reminders))
        whenever(calendarSyncConfigDao.getEnabledConfigs()).thenReturn(configs)
        whenever(deviceCalendarProvider.addReminderToCalendar(any(), any())).thenReturn(1L)

        // When
        manager.syncAllReminders()

        // Then
        verify(deviceCalendarProvider).addReminderToCalendar(any(), eq("cal1"))
        verify(deviceCalendarProvider).addReminderToCalendar(any(), eq("cal2"))
        verify(calendarSyncConfigDao, times(2)).updateConfig(any())
    }

    /** addCalendarSyncConfig는 캘린더 동기화 설정을 추가한다 */
    @Test
    fun addCalendarSyncConfigAddsConfig() = runTest {
        // Given
        val calendarId = "cal1"
        val calendarName = "내 캘린더"
        val accountName = "test@gmail.com"
        val calendarColor = 0xFF0000
        whenever(calendarSyncConfigDao.insertConfig(any())).thenReturn(1L)

        // When
        val result = manager.addCalendarSyncConfig(
            calendarId = calendarId,
            calendarName = calendarName,
            accountName = accountName,
            calendarColor = calendarColor
        )

        // Then
        verify(calendarSyncConfigDao).insertConfig(argThat { config ->
            config.calendarId == calendarId &&
            config.calendarName == calendarName &&
            config.isSyncEnabled == true
        })
        assertEquals(1L, result)
    }

    /** addCalendarSyncConfig는 기본 동기화 방향을 TWO_WAY로 설정한다 */
    @Test
    fun addCalendarSyncConfigUsesDefaultTwoWaySync() = runTest {
        // Given
        whenever(calendarSyncConfigDao.insertConfig(any())).thenReturn(1L)

        // When
        manager.addCalendarSyncConfig("cal1", "캘린더", "account", 0xFF0000)

        // Then
        verify(calendarSyncConfigDao).insertConfig(argThat { config ->
            config.syncDirection == SyncDirection.TWO_WAY
        })
    }

    /** toggleCalendarSync는 캘린더 동기화를 활성화한다 */
    @Test
    fun toggleCalendarSyncEnablesSync() = runTest {
        // Given
        val calendarId = "cal1"
        val config = createConfig(calendarId = calendarId, isSyncEnabled = false)
        whenever(calendarSyncConfigDao.getConfigByCalendarId(calendarId)).thenReturn(config)

        // When
        manager.toggleCalendarSync(calendarId, true)

        // Then
        verify(calendarSyncConfigDao).updateConfig(argThat { c ->
            c.isSyncEnabled == true
        })
    }

    /** toggleCalendarSync는 캘린더 동기화를 비활성화한다 */
    @Test
    fun toggleCalendarSyncDisablesSync() = runTest {
        // Given
        val calendarId = "cal1"
        val config = createConfig(calendarId = calendarId, isSyncEnabled = true)
        whenever(calendarSyncConfigDao.getConfigByCalendarId(calendarId)).thenReturn(config)

        // When
        manager.toggleCalendarSync(calendarId, false)

        // Then
        verify(calendarSyncConfigDao).updateConfig(argThat { c ->
            c.isSyncEnabled == false
        })
    }

    /** toggleCalendarSync는 존재하지 않는 캘린더 ID는 무시한다 */
    @Test
    fun toggleCalendarSyncIgnoresNonExistentCalendar() = runTest {
        // Given
        val calendarId = "nonexistent"
        whenever(calendarSyncConfigDao.getConfigByCalendarId(calendarId)).thenReturn(null)

        // When
        manager.toggleCalendarSync(calendarId, true)

        // Then
        verify(calendarSyncConfigDao, never()).updateConfig(any())
    }

    /** getDeviceCalendars는 기기 캘린더 목록을 반환한다 */
    @Test
    fun getDeviceCalendarsReturnsDeviceCalendars() {
        // Given
        val calendars = listOf(
            DeviceCalendar("cal1", "캘린더 1", "account1", 0xFF0000),
            DeviceCalendar("cal2", "캘린더 2", "account2", 0x00FF00)
        )
        whenever(deviceCalendarProvider.getDeviceCalendars()).thenReturn(calendars)

        // When
        val result = manager.getDeviceCalendars()

        // Then
        assertEquals(2, result.size)
        assertEquals("캘린더 1", result[0].name)
        assertEquals("캘린더 2", result[1].name)
    }

    // Helper functions
    private fun createReminder(
        id: Long,
        title: String = "Test Reminder $id",
        dueDateTime: LocalDateTime? = null,
        isCompleted: Boolean = false
    ) = ReminderEntity(
        id = id,
        title = title,
        dueDateTime = dueDateTime,
        isCompleted = isCompleted,
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now()
    )

    private fun createConfig(
        calendarId: String,
        calendarName: String = "Test Calendar",
        isSyncEnabled: Boolean = true
    ) = CalendarSyncConfig(
        id = 1,
        calendarId = calendarId,
        calendarName = calendarName,
        accountName = "test@gmail.com",
        calendarColor = 0xFF0000,
        syncDirection = SyncDirection.TWO_WAY,
        isSyncEnabled = isSyncEnabled
    )
}
