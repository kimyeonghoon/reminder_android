package com.reminder.snooze

import com.reminder.data.dao.ReminderDao
import java.time.LocalDateTime

/**
 * 스누즈 관리자
 *
 * 리마인더 스누즈 기능을 관리합니다.
 *
 * @param reminderDao ReminderDao 인스턴스
 */
class SnoozeManager(
    private val reminderDao: ReminderDao
) {

    /**
     * 리마인더를 스누즈합니다
     *
     * @param reminderId 리마인더 ID
     * @param option 스누즈 옵션
     */
    suspend fun snoozeReminder(reminderId: Long, option: SnoozeOption) {
        val snoozeUntil = option.calculateSnoozeTime()
        val updatedAt = LocalDateTime.now()

        reminderDao.snoozeReminder(
            id = reminderId,
            snoozeUntil = snoozeUntil,
            updatedAt = updatedAt
        )
    }

    /**
     * 리마인더의 스누즈를 취소합니다
     *
     * @param reminderId 리마인더 ID
     */
    suspend fun cancelSnooze(reminderId: Long) {
        val updatedAt = LocalDateTime.now()
        reminderDao.cancelSnooze(
            id = reminderId,
            updatedAt = updatedAt
        )
    }

    /**
     * 스누즈 시간이 도래한 리마인더 목록을 가져옵니다
     *
     * @return 스누즈 시간이 도래한 리마인더 목록
     */
    suspend fun getSnoozedRemindersDue(): List<Long> {
        val currentTime = LocalDateTime.now()
        val reminders = reminderDao.getSnoozedRemindersDue(currentTime)

        // 스누즈가 도래한 리마인더들의 스누즈를 자동으로 취소
        reminders.forEach { reminder ->
            cancelSnooze(reminder.id)
        }

        return reminders.map { it.id }
    }

    /**
     * 특정 시간까지 스누즈합니다 (커스텀 시간)
     *
     * @param reminderId 리마인더 ID
     * @param snoozeUntil 스누즈 종료 시간
     */
    suspend fun snoozeUntilCustomTime(reminderId: Long, snoozeUntil: LocalDateTime) {
        val updatedAt = LocalDateTime.now()

        reminderDao.snoozeReminder(
            id = reminderId,
            snoozeUntil = snoozeUntil,
            updatedAt = updatedAt
        )
    }
}
