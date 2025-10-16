package com.reminder.data.dao

import com.reminder.data.entity.Priority
import com.reminder.data.entity.ReminderEntity
import com.reminder.data.entity.Urgency
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.time.LocalDateTime
import org.junit.Assert.*

/**
 * ReminderDao 테스트
 *
 * ReminderDao의 모든 메서드를 테스트합니다.
 * 이것은 가장 중요한 핵심 DAO이므로 모든 26개 메서드를 커버합니다.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ReminderDaoTest {

    private lateinit var dao: ReminderDao

    @Before
    fun setup() {
        dao = mock()
    }

    // ========== Flow 반환 메서드 (10개) ==========

    /** getAllReminders는 모든 리마인더를 생성일 기준 내림차순으로 반환한다 */
    @Test
    fun testGetAllRemindersReturnsAllRemindersOrderedByCreatedAtDesc() = runTest {
        // Given
        val now = LocalDateTime.now()
        val reminders = listOf(
            ReminderEntity(id = 1, title = "최신", createdAt = now),
            ReminderEntity(id = 2, title = "과거", createdAt = now.minusDays(1))
        )
        whenever(dao.getAllReminders()).thenReturn(flowOf(reminders))

        // When
        val result = dao.getAllReminders().first()

        // Then
        verify(dao).getAllReminders()
        assertEquals(2, result.size)
        assertEquals("최신", result[0].title)
    }

    /** getActiveReminders는 미완료 리마인더를 마감일/우선순위 순으로 반환한다 */
    @Test
    fun testGetActiveRemindersReturnsIncompleteRemindersOrderedByDueDateAndPriority() = runTest {
        // Given
        val reminders = listOf(
            ReminderEntity(id = 1, title = "긴급", isCompleted = false, priority = Priority.HIGH),
            ReminderEntity(id = 2, title = "보통", isCompleted = false, priority = Priority.MEDIUM)
        )
        whenever(dao.getActiveReminders()).thenReturn(flowOf(reminders))

        // When
        val result = dao.getActiveReminders().first()

        // Then
        verify(dao).getActiveReminders()
        assertEquals(2, result.size)
        result.forEach { assertFalse(it.isCompleted) }
    }

    /** getCompletedReminders는 완료된 리마인더를 수정일 기준 내림차순으로 반환한다 */
    @Test
    fun testGetCompletedRemindersReturnsCompletedRemindersOrderedByUpdatedAtDesc() = runTest {
        // Given
        val now = LocalDateTime.now()
        val reminders = listOf(
            ReminderEntity(id = 1, title = "완료1", isCompleted = true, updatedAt = now),
            ReminderEntity(id = 2, title = "완료2", isCompleted = true, updatedAt = now.minusHours(1))
        )
        whenever(dao.getCompletedReminders()).thenReturn(flowOf(reminders))

        // When
        val result = dao.getCompletedReminders().first()

        // Then
        verify(dao).getCompletedReminders()
        assertEquals(2, result.size)
        result.forEach { assertTrue(it.isCompleted) }
    }

    /** getRemindersByCategory는 특정 카테고리의 리마인더를 반환한다 */
    @Test
    fun testGetRemindersByCategoryReturnsRemindersBySpecificCategory() = runTest {
        // Given
        val category = "업무"
        val reminders = listOf(
            ReminderEntity(id = 1, title = "회의", category = category),
            ReminderEntity(id = 2, title = "보고서", category = category)
        )
        whenever(dao.getRemindersByCategory(category)).thenReturn(flowOf(reminders))

        // When
        val result = dao.getRemindersByCategory(category).first()

        // Then
        verify(dao).getRemindersByCategory(category)
        assertEquals(2, result.size)
        result.forEach { assertEquals(category, it.category) }
    }

    /** getSnoozedReminders는 스누즈된 리마인더를 스누즈 시간 순으로 반환한다 */
    @Test
    fun testGetSnoozedRemindersReturnsSnoozedRemindersOrderedBySnoozeTime() = runTest {
        // Given
        val now = LocalDateTime.now()
        val reminders = listOf(
            ReminderEntity(id = 1, title = "스누즈1", snoozeUntil = now.plusMinutes(10), isCompleted = false),
            ReminderEntity(id = 2, title = "스누즈2", snoozeUntil = now.plusMinutes(30), isCompleted = false)
        )
        whenever(dao.getSnoozedReminders()).thenReturn(flowOf(reminders))

        // When
        val result = dao.getSnoozedReminders().first()

        // Then
        verify(dao).getSnoozedReminders()
        assertEquals(2, result.size)
        result.forEach {
            assertNotNull(it.snoozeUntil)
            assertFalse(it.isCompleted)
        }
    }

    /** getArchivedReminders는 아카이브된 리마인더를 수정일 기준 내림차순으로 반환한다 */
    @Test
    fun testGetArchivedRemindersReturnsArchivedRemindersOrderedByUpdatedAtDesc() = runTest {
        // Given
        val now = LocalDateTime.now()
        val reminders = listOf(
            ReminderEntity(id = 1, title = "아카이브1", isArchived = true, updatedAt = now),
            ReminderEntity(id = 2, title = "아카이브2", isArchived = true, updatedAt = now.minusDays(1))
        )
        whenever(dao.getArchivedReminders()).thenReturn(flowOf(reminders))

        // When
        val result = dao.getArchivedReminders().first()

        // Then
        verify(dao).getArchivedReminders()
        assertEquals(2, result.size)
        result.forEach { assertTrue(it.isArchived) }
    }

    /** getAllCompletedReminders는 완료된 모든 리마인더를 반환한다 */
    @Test
    fun testGetAllCompletedRemindersReturnsAllCompletedReminders() = runTest {
        // Given
        val reminders = listOf(
            ReminderEntity(id = 1, title = "완료1", isCompleted = true),
            ReminderEntity(id = 2, title = "완료2", isCompleted = true),
            ReminderEntity(id = 3, title = "완료3", isCompleted = true)
        )
        whenever(dao.getAllCompletedReminders()).thenReturn(flowOf(reminders))

        // When
        val result = dao.getAllCompletedReminders().first()

        // Then
        verify(dao).getAllCompletedReminders()
        assertEquals(3, result.size)
        result.forEach { assertTrue(it.isCompleted) }
    }

    // ========== suspend 단일 조회 메서드 (1개) ==========

    /** getReminderById는 ID로 리마인더를 조회한다 */
    @Test
    fun testGetReminderByIdReturnsReminderById() = runTest {
        // Given
        val reminderId = 42L
        val reminder = ReminderEntity(id = reminderId, title = "특정 할일")
        whenever(dao.getReminderById(reminderId)).thenReturn(reminder)

        // When
        val result = dao.getReminderById(reminderId)

        // Then
        verify(dao).getReminderById(reminderId)
        assertNotNull(result)
        assertEquals(reminderId, result!!.id)
        assertEquals("특정 할일", result.title)
    }

    /** getReminderById는 존재하지 않는 ID로 조회 시 null을 반환한다 */
    @Test
    fun testGetReminderByIdReturnsNullForNonExistentId() = runTest {
        // Given
        val nonExistentId = 999L
        whenever(dao.getReminderById(nonExistentId)).thenReturn(null)

        // When
        val result = dao.getReminderById(nonExistentId)

        // Then
        verify(dao).getReminderById(nonExistentId)
        assertNull(result)
    }

    // ========== Insert/Update/Delete 메서드 (5개) ==========

    /** insertReminder는 리마인더를 삽입하고 생성된 ID를 반환한다 */
    @Test
    fun testInsertReminderInsertsReminderAndReturnsGeneratedId() = runTest {
        // Given
        val reminder = ReminderEntity(title = "새 할일")
        val generatedId = 100L
        whenever(dao.insertReminder(reminder)).thenReturn(generatedId)

        // When
        val result = dao.insertReminder(reminder)

        // Then
        verify(dao).insertReminder(reminder)
        assertEquals(generatedId, result)
    }

    /** updateReminder는 리마인더를 업데이트한다 */
    @Test
    fun testUpdateReminderUpdatesExistingReminder() = runTest {
        // Given
        val reminder = ReminderEntity(id = 1, title = "수정된 할일")

        // When
        dao.updateReminder(reminder)

        // Then
        verify(dao).updateReminder(reminder)
    }

    /** deleteReminder는 리마인더를 삭제한다 */
    @Test
    fun testDeleteReminderDeletesReminder() = runTest {
        // Given
        val reminder = ReminderEntity(id = 1, title = "삭제할 할일")

        // When
        dao.deleteReminder(reminder)

        // Then
        verify(dao).deleteReminder(reminder)
    }

    /** deleteReminderById는 ID로 리마인더를 삭제한다 */
    @Test
    fun testDeleteReminderByIdDeletesReminderById() = runTest {
        // Given
        val reminderId = 1L

        // When
        dao.deleteReminderById(reminderId)

        // Then
        verify(dao).deleteReminderById(reminderId)
    }

    /** deleteAllCompletedReminders는 완료된 모든 리마인더를 삭제한다 */
    @Test
    fun testDeleteAllCompletedRemindersDeletesAllCompletedReminders() = runTest {
        // When
        dao.deleteAllCompletedReminders()

        // Then
        verify(dao).deleteAllCompletedReminders()
    }

    // ========== Sync 메서드 (3개) ==========

    /** getAllRemindersList는 모든 리마인더를 리스트로 반환한다 */
    @Test
    fun testGetAllRemindersListReturnsAllRemindersAsList() = runTest {
        // Given
        val reminders = listOf(
            ReminderEntity(id = 1, title = "할일1"),
            ReminderEntity(id = 2, title = "할일2"),
            ReminderEntity(id = 3, title = "할일3")
        )
        whenever(dao.getAllRemindersList()).thenReturn(reminders)

        // When
        val result = dao.getAllRemindersList()

        // Then
        verify(dao).getAllRemindersList()
        assertEquals(3, result.size)
    }

    /** getCompletedRemindersList는 완료된 리마인더를 리스트로 반환한다 */
    @Test
    fun testGetCompletedRemindersListReturnsCompletedRemindersAsList() = runTest {
        // Given
        val reminders = listOf(
            ReminderEntity(id = 1, title = "완료1", isCompleted = true),
            ReminderEntity(id = 2, title = "완료2", isCompleted = true)
        )
        whenever(dao.getCompletedRemindersList()).thenReturn(reminders)

        // When
        val result = dao.getCompletedRemindersList()

        // Then
        verify(dao).getCompletedRemindersList()
        assertEquals(2, result.size)
        result.forEach { assertTrue(it.isCompleted) }
    }

    /** getRemindersModifiedAfter는 특정 시간 이후 수정된 리마인더를 반환한다 */
    @Test
    fun testGetRemindersModifiedAfterReturnsRemindersModifiedAfterTimestamp() = runTest {
        // Given
        val timestamp = LocalDateTime.now().minusDays(1)
        val now = LocalDateTime.now()
        val reminders = listOf(
            ReminderEntity(id = 1, title = "최근 수정", updatedAt = now),
            ReminderEntity(id = 2, title = "최근 수정2", updatedAt = now.minusHours(2))
        )
        whenever(dao.getRemindersModifiedAfter(timestamp)).thenReturn(reminders)

        // When
        val result = dao.getRemindersModifiedAfter(timestamp)

        // Then
        verify(dao).getRemindersModifiedAfter(timestamp)
        assertEquals(2, result.size)
    }

    // ========== Completion History 메서드 (2개) ==========

    /** getCompletedRemindersByDate는 특정 날짜에 완료된 리마인더를 반환한다 */
    @Test
    fun testGetCompletedRemindersByDateReturnsRemindersCompletedOnSpecificDate() = runTest {
        // Given
        val date = LocalDateTime.now()
        val reminders = listOf(
            ReminderEntity(id = 1, title = "오늘 완료1", isCompleted = true, updatedAt = date),
            ReminderEntity(id = 2, title = "오늘 완료2", isCompleted = true, updatedAt = date)
        )
        whenever(dao.getCompletedRemindersByDate(date)).thenReturn(reminders)

        // When
        val result = dao.getCompletedRemindersByDate(date)

        // Then
        verify(dao).getCompletedRemindersByDate(date)
        assertEquals(2, result.size)
        result.forEach { assertTrue(it.isCompleted) }
    }

    /** getCompletedRemindersInRange는 기간 내 완료된 리마인더를 반환한다 */
    @Test
    fun testGetCompletedRemindersInRangeReturnsRemindersCompletedInDateRange() = runTest {
        // Given
        val startDate = LocalDateTime.now().minusDays(7)
        val endDate = LocalDateTime.now()
        val reminders = listOf(
            ReminderEntity(id = 1, title = "기간내 완료1", isCompleted = true, updatedAt = startDate.plusDays(1)),
            ReminderEntity(id = 2, title = "기간내 완료2", isCompleted = true, updatedAt = endDate.minusDays(1))
        )
        whenever(dao.getCompletedRemindersInRange(startDate, endDate)).thenReturn(reminders)

        // When
        val result = dao.getCompletedRemindersInRange(startDate, endDate)

        // Then
        verify(dao).getCompletedRemindersInRange(startDate, endDate)
        assertEquals(2, result.size)
        result.forEach { assertTrue(it.isCompleted) }
    }

    // ========== Snooze 메서드 (4개) ==========

    /** snoozeReminder는 리마인더를 스누즈 처리한다 */
    @Test
    fun testSnoozeReminderSetsSnoozeUntilTime() = runTest {
        // Given
        val reminderId = 1L
        val snoozeUntil = LocalDateTime.now().plusMinutes(30)
        val updatedAt = LocalDateTime.now()

        // When
        dao.snoozeReminder(reminderId, snoozeUntil, updatedAt)

        // Then
        verify(dao).snoozeReminder(reminderId, snoozeUntil, updatedAt)
    }

    /** cancelSnooze는 스누즈를 취소한다 */
    @Test
    fun testCancelSnoozeCancelsSnoozeForReminder() = runTest {
        // Given
        val reminderId = 1L
        val updatedAt = LocalDateTime.now()

        // When
        dao.cancelSnooze(reminderId, updatedAt)

        // Then
        verify(dao).cancelSnooze(reminderId, updatedAt)
    }

    /** getSnoozedRemindersDue는 스누즈 시간이 도달한 리마인더를 반환한다 */
    @Test
    fun testGetSnoozedRemindersDueReturnsSnoozedRemindersReachedSnoozeTime() = runTest {
        // Given
        val now = LocalDateTime.now()
        val reminders = listOf(
            ReminderEntity(id = 1, title = "스누즈 만료", snoozeUntil = now.minusMinutes(1), isCompleted = false),
            ReminderEntity(id = 2, title = "스누즈 만료2", snoozeUntil = now.minusMinutes(5), isCompleted = false)
        )
        whenever(dao.getSnoozedRemindersDue(now)).thenReturn(reminders)

        // When
        val result = dao.getSnoozedRemindersDue(now)

        // Then
        verify(dao).getSnoozedRemindersDue(now)
        assertEquals(2, result.size)
        result.forEach {
            assertNotNull(it.snoozeUntil)
            assertFalse(it.isCompleted)
        }
    }

    // ========== Archive 메서드 (1개) ==========

    /** updateArchiveStatus는 아카이브 상태를 업데이트한다 */
    @Test
    fun testUpdateArchiveStatusUpdatesArchiveStatusForReminder() = runTest {
        // Given
        val reminderId = 1L
        val isArchived = true
        val updatedAt = LocalDateTime.now()

        // When
        dao.updateArchiveStatus(reminderId, isArchived, updatedAt)

        // Then
        verify(dao).updateArchiveStatus(reminderId, isArchived, updatedAt)
    }

    // ========== Toggle 메서드 (1개) ==========

    /** toggleReminderCompletion은 완료 상태를 토글한다 */
    @Test
    fun testToggleReminderCompletionTogglesCompletionStatus() = runTest {
        // Given
        val reminderId = 1L
        val completedAt = LocalDateTime.now()
        val updatedAt = LocalDateTime.now()

        // When
        dao.toggleReminderCompletion(reminderId, completedAt, updatedAt)

        // Then
        verify(dao).toggleReminderCompletion(reminderId, completedAt, updatedAt)
    }

    /** toggleReminderCompletion은 기본 파라미터로 토글한다 */
    @Test
    fun testToggleReminderCompletionTogglesWithDefaultParameters() = runTest {
        // Given
        val reminderId = 1L

        // When
        dao.toggleReminderCompletion(reminderId)

        // Then
        verify(dao).toggleReminderCompletion(eq(reminderId), any(), any())
    }

    // ========== 추가 통합 테스트 ==========

    /** insertReminder는 OnConflict REPLACE 전략으로 동작한다 */
    @Test
    fun testInsertReminderReplacesOnConflict() = runTest {
        // Given
        val reminder = ReminderEntity(id = 1, title = "기존 할일")
        val updatedReminder = reminder.copy(title = "수정된 할일")
        whenever(dao.insertReminder(updatedReminder)).thenReturn(1L)

        // When
        val result = dao.insertReminder(updatedReminder)

        // Then
        verify(dao).insertReminder(updatedReminder)
        assertEquals(1L, result)
    }

    /** getRemindersByCategory는 빈 카테고리도 필터링한다 */
    @Test
    fun testGetRemindersByCategoryHandlesEmptyCategory() = runTest {
        // Given
        val emptyCategory = ""
        val reminders = listOf(
            ReminderEntity(id = 1, title = "카테고리 없음", category = emptyCategory)
        )
        whenever(dao.getRemindersByCategory(emptyCategory)).thenReturn(flowOf(reminders))

        // When
        val result = dao.getRemindersByCategory(emptyCategory).first()

        // Then
        verify(dao).getRemindersByCategory(emptyCategory)
        assertEquals(1, result.size)
        assertEquals("", result[0].category)
    }

    /** getActiveReminders는 스누즈된 리마인더도 포함한다 */
    @Test
    fun testGetActiveRemindersIncludesSnoozedReminders() = runTest {
        // Given
        val now = LocalDateTime.now()
        val reminders = listOf(
            ReminderEntity(id = 1, title = "일반", isCompleted = false),
            ReminderEntity(id = 2, title = "스누즈", isCompleted = false, snoozeUntil = now.plusMinutes(10))
        )
        whenever(dao.getActiveReminders()).thenReturn(flowOf(reminders))

        // When
        val result = dao.getActiveReminders().first()

        // Then
        verify(dao).getActiveReminders()
        assertEquals(2, result.size)
    }

    /** getCompletedReminders는 아카이브된 완료 리마인더도 포함한다 */
    @Test
    fun testGetCompletedRemindersIncludesArchivedCompletedReminders() = runTest {
        // Given
        val reminders = listOf(
            ReminderEntity(id = 1, title = "완료", isCompleted = true, isArchived = false),
            ReminderEntity(id = 2, title = "완료+아카이브", isCompleted = true, isArchived = true)
        )
        whenever(dao.getCompletedReminders()).thenReturn(flowOf(reminders))

        // When
        val result = dao.getCompletedReminders().first()

        // Then
        verify(dao).getCompletedReminders()
        assertEquals(2, result.size)
        result.forEach { assertTrue(it.isCompleted) }
    }

    // ========== Helper 함수 ==========

    private fun assertFalse(condition: Boolean, message: String = "Expected false but was true") {
        assertTrue(message, !condition)
    }
}
