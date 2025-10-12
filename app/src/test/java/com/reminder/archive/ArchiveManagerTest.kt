package com.reminder.archive

import com.reminder.data.dao.ReminderDao
import com.reminder.data.entity.Priority
import com.reminder.data.entity.ReminderEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.time.LocalDateTime

/**
 * v1.43.0: ArchiveManager 유닛 테스트
 *
 * TDD Red Phase: 테스트 먼저 작성
 */
class ArchiveManagerTest {

    private lateinit var reminderDao: ReminderDao
    private lateinit var archiveManager: ArchiveManager

    @Before
    fun setup() {
        reminderDao = mock()
        archiveManager = ArchiveManager(reminderDao)
    }

    /** 테스트 1: 리마인더를 아카이브할 수 있다 */
    @Test
    fun testArchiveReminder() = runTest {
        // Given
        val reminder = createTestReminder(id = 1)

        // When
        archiveManager.archiveReminder(reminder)

        // Then
        verify(reminderDao).updateReminder(
            check { require(it.id == 1L && it.isArchived) }
        )
    }

    /** 테스트 2: 아카이브된 리마인더를 복원할 수 있다 */
    @Test
    fun testUnarchiveReminder() = runTest {
        // Given
        val archivedReminder = createTestReminder(id = 1, isArchived = true)

        // When
        archiveManager.unarchiveReminder(archivedReminder)

        // Then
        verify(reminderDao).updateReminder(
            check { require(it.id == 1L && !it.isArchived) }
        )
    }

    /** 테스트 3: N일 이상 완료된 리마인더를 자동 아카이브할 수 있다 */
    @Test
    fun testAutoArchiveOldCompletedReminders() = runTest {
        // Given
        val now = LocalDateTime.now()
        val old = now.minusDays(35) // 35일 전 완료
        val recent = now.minusDays(5) // 5일 전 완료

        val oldReminder = createTestReminder(id = 1, isCompleted = true, updatedAt = old)
        val recentReminder = createTestReminder(id = 2, isCompleted = true, updatedAt = recent)

        whenever(reminderDao.getAllCompletedReminders())
            .thenReturn(flowOf(listOf(oldReminder, recentReminder)))

        // When
        val archivedCount = archiveManager.autoArchiveOldCompletedReminders(daysThreshold = 30)

        // Then
        assertEquals(1, archivedCount) // 35일 전 항목만 아카이브
        verify(reminderDao).updateReminder(
            check { require(it.id == 1L && it.isArchived) }
        )
    }

    /** 테스트 4: 이미 아카이브된 리마인더는 다시 아카이브하지 않는다 */
    @Test
    fun testSkipAlreadyArchivedReminders() = runTest {
        // Given
        val now = LocalDateTime.now()
        val old = now.minusDays(35)

        val alreadyArchived = createTestReminder(
            id = 1,
            isCompleted = true,
            isArchived = true,
            updatedAt = old
        )

        whenever(reminderDao.getAllCompletedReminders())
            .thenReturn(flowOf(listOf(alreadyArchived)))

        // When
        val archivedCount = archiveManager.autoArchiveOldCompletedReminders(daysThreshold = 30)

        // Then
        assertEquals(0, archivedCount) // 이미 아카이브됨
        verify(reminderDao, never()).updateReminder(any())
    }

    /** 테스트 5: 아카이브된 리마인더 목록을 조회할 수 있다 */
    @Test
    fun testGetArchivedReminders() = runTest {
        // Given
        val archived1 = createTestReminder(id = 1, isArchived = true)
        val archived2 = createTestReminder(id = 2, isArchived = true)

        whenever(reminderDao.getArchivedReminders())
            .thenReturn(flowOf(listOf(archived1, archived2)))

        // When
        val result = archiveManager.getArchivedReminders().first()

        // Then
        assertEquals(2, result.size)
        assertTrue(result.all { it.isArchived })
    }

    /** 테스트 6: 아카이브된 리마인더를 영구 삭제할 수 있다 */
    @Test
    fun testDeleteArchivedReminderPermanently() = runTest {
        // Given
        val archived = createTestReminder(id = 1, isArchived = true)

        // When
        archiveManager.deleteArchivedReminder(archived)

        // Then
        verify(reminderDao).deleteReminder(archived)
    }

    /** 테스트 7: 모든 아카이브를 일괄 삭제할 수 있다 */
    @Test
    fun testDeleteAllArchivedReminders() = runTest {
        // Given
        val archived1 = createTestReminder(id = 1, isArchived = true)
        val archived2 = createTestReminder(id = 2, isArchived = true)

        whenever(reminderDao.getArchivedReminders())
            .thenReturn(flowOf(listOf(archived1, archived2)))

        // When
        val deletedCount = archiveManager.deleteAllArchived()

        // Then
        assertEquals(2, deletedCount)
        verify(reminderDao, times(2)).deleteReminder(any())
    }

    /**
     * Helper: 테스트용 리마인더 생성
     */
    private fun createTestReminder(
        id: Long = 1,
        title: String = "Test Reminder",
        isCompleted: Boolean = false,
        isArchived: Boolean = false,
        updatedAt: LocalDateTime = LocalDateTime.now()
    ) = ReminderEntity(
        id = id,
        title = title,
        description = "",
        priority = Priority.MEDIUM,
        category = "",
        isCompleted = isCompleted,
        isArchived = isArchived,
        createdAt = LocalDateTime.now(),
        updatedAt = updatedAt
    )
}
