package com.reminder.calendar

import com.reminder.data.dao.CalendarSyncConfigDao
import com.reminder.data.dao.ReminderDao
import com.reminder.data.entity.CalendarSyncConfig
import com.reminder.data.entity.ReminderEntity
import com.reminder.data.entity.SyncDirection
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime

/**
 * v1.40.0: 캘린더 동기화 관리자
 *
 * 리마인더와 기기 캘린더 간의 동기화를 관리합니다.
 */
class CalendarSyncManager(
    private val deviceCalendarProvider: DeviceCalendarProvider,
    private val calendarSyncConfigDao: CalendarSyncConfigDao,
    private val reminderDao: ReminderDao
) {

    /**
     * 리마인더를 캘린더에 동기화
     */
    suspend fun syncReminderToCalendar(reminder: ReminderEntity) {
        // 동기화 활성화된 캘린더 목록 가져오기
        val enabledConfigs = calendarSyncConfigDao.getEnabledConfigs()

        for (config in enabledConfigs) {
            // 리마인더를 캘린더 이벤트로 추가
            val eventId = deviceCalendarProvider.addReminderToCalendar(
                reminder,
                config.calendarId
            )

            if (eventId != null) {
                // 동기화 시간 업데이트
                calendarSyncConfigDao.updateConfig(
                    config.copy(lastSyncedAt = LocalDateTime.now())
                )
            }
        }
    }

    /**
     * 모든 리마인더 동기화
     */
    suspend fun syncAllReminders() {
        val reminders = reminderDao.getAllReminders().first()
        val enabledConfigs = calendarSyncConfigDao.getEnabledConfigs()

        for (config in enabledConfigs) {
            for (reminder in reminders) {
                if (reminder.dueDateTime != null && !reminder.isCompleted) {
                    deviceCalendarProvider.addReminderToCalendar(
                        reminder,
                        config.calendarId
                    )
                }
            }

            // 동기화 시간 업데이트
            calendarSyncConfigDao.updateConfig(
                config.copy(lastSyncedAt = LocalDateTime.now())
            )
        }
    }

    /**
     * 캘린더 동기화 설정 추가
     */
    suspend fun addCalendarSyncConfig(
        calendarId: String,
        calendarName: String,
        accountName: String,
        calendarColor: Int,
        syncDirection: SyncDirection = SyncDirection.TWO_WAY
    ): Long {
        val config = CalendarSyncConfig(
            calendarId = calendarId,
            calendarName = calendarName,
            accountName = accountName,
            calendarColor = calendarColor,
            syncDirection = syncDirection,
            isSyncEnabled = true
        )

        return calendarSyncConfigDao.insertConfig(config)
    }

    /**
     * 캘린더 동기화 활성화/비활성화
     */
    suspend fun toggleCalendarSync(calendarId: String, isEnabled: Boolean) {
        val config = calendarSyncConfigDao.getConfigByCalendarId(calendarId) ?: return
        calendarSyncConfigDao.updateConfig(config.copy(isSyncEnabled = isEnabled))
    }

    /**
     * 기기 캘린더 목록 가져오기
     */
    fun getDeviceCalendars(): List<DeviceCalendar> {
        return deviceCalendarProvider.getDeviceCalendars()
    }
}
