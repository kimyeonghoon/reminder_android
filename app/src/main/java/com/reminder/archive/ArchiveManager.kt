package com.reminder.archive

import com.reminder.data.dao.ReminderDao
import com.reminder.data.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * v1.43.0: Archive Manager
 *
 * 완료된 리마인더를 자동으로 아카이브하여 메인 화면을 깔끔하게 유지
 *
 * 주요 기능:
 * - 리마인더 아카이브/복원
 * - N일 이상 완료된 리마인더 자동 아카이브
 * - 아카이브된 리마인더 영구 삭제
 * - 모든 아카이브 일괄 삭제
 */
class ArchiveManager(
    private val reminderDao: ReminderDao
) {

    /**
     * 리마인더를 아카이브 처리
     */
    suspend fun archiveReminder(reminder: ReminderEntity) {
        val updated = reminder.copy(
            isArchived = true,
            updatedAt = LocalDateTime.now()
        )
        reminderDao.updateReminder(updated)
    }

    /**
     * 아카이브된 리마인더를 복원
     */
    suspend fun unarchiveReminder(reminder: ReminderEntity) {
        val updated = reminder.copy(
            isArchived = false,
            updatedAt = LocalDateTime.now()
        )
        reminderDao.updateReminder(updated)
    }

    /**
     * N일 이상 완료된 리마인더를 자동 아카이브
     *
     * @param daysThreshold 아카이브할 기준 일수 (기본 30일)
     * @return 아카이브된 리마인더 개수
     */
    suspend fun autoArchiveOldCompletedReminders(daysThreshold: Int = 30): Int {
        val now = LocalDateTime.now()
        val completed = reminderDao.getAllCompletedReminders().first()

        var archivedCount = 0
        completed.forEach { reminder ->
            // 이미 아카이브된 항목은 건너뛰기
            if (reminder.isArchived) return@forEach

            // N일 이상 경과한 완료 항목만 아카이브
            val daysSinceCompleted = ChronoUnit.DAYS.between(reminder.updatedAt, now)
            if (daysSinceCompleted >= daysThreshold) {
                archiveReminder(reminder)
                archivedCount++
            }
        }

        return archivedCount
    }

    /**
     * 아카이브된 리마인더 목록 조회
     */
    fun getArchivedReminders(): Flow<List<ReminderEntity>> {
        return reminderDao.getArchivedReminders()
    }

    /**
     * 아카이브된 리마인더 영구 삭제
     */
    suspend fun deleteArchivedReminder(reminder: ReminderEntity) {
        reminderDao.deleteReminder(reminder)
    }

    /**
     * 모든 아카이브 일괄 삭제
     *
     * @return 삭제된 리마인더 개수
     */
    suspend fun deleteAllArchived(): Int {
        val archived = reminderDao.getArchivedReminders().first()
        archived.forEach { reminder ->
            reminderDao.deleteReminder(reminder)
        }
        return archived.size
    }
}
