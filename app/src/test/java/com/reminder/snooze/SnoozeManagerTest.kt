package com.reminder.snooze

import com.reminder.data.dao.ReminderDao
import com.reminder.data.entity.ReminderEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.time.LocalDateTime
import org.junit.Assert.*

/**
 * SnoozeManager 테스트
 *
 * 스누즈 관리자의 모든 메서드를 검증합니다.
 * - AAA 패턴 (Given-When-Then)
 * - Mockito를 사용한 단위 테스트
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SnoozeManagerTest {

    private lateinit var reminderDao: ReminderDao
    private lateinit var snoozeManager: SnoozeManager

    @Before
    fun setup() {
        reminderDao = mock()
        snoozeManager = SnoozeManager(reminderDao)
    }

    /** snoozeReminder는 옵션에 따라 리마인더를 스누즈한다 */
    @Test
    fun testSnoozeReminderSnoozesReminderWithOption() = runTest {
        // Given
        val reminderId = 1L
        val option = SnoozeOption.TEN_MINUTES

        // When
        snoozeManager.snoozeReminder(reminderId, option)

        // Then
        verify(reminderDao).snoozeReminder(
            eq(reminderId),
            any(),
            any()
        )
    }

    /** snoozeReminder는 TEN_MINUTES 옵션으로 10분 후 스누즈한다 */
    @Test
    fun testSnoozeReminderWithTenMinutesOption() = runTest {
        // Given
        val reminderId = 1L
        val option = SnoozeOption.TEN_MINUTES

        // When
        snoozeManager.snoozeReminder(reminderId, option)

        // Then
        verify(reminderDao).snoozeReminder(
            eq(reminderId),
            argThat { snoozeTime ->
                snoozeTime.isAfter(LocalDateTime.now().plusMinutes(9)) &&
                snoozeTime.isBefore(LocalDateTime.now().plusMinutes(11))
            },
            any()
        )
    }

    /** snoozeReminder는 THIRTY_MINUTES 옵션으로 30분 후 스누즈한다 */
    @Test
    fun testSnoozeReminderWithThirtyMinutesOption() = runTest {
        // Given
        val reminderId = 1L
        val option = SnoozeOption.THIRTY_MINUTES

        // When
        snoozeManager.snoozeReminder(reminderId, option)

        // Then
        verify(reminderDao).snoozeReminder(
            eq(reminderId),
            argThat { snoozeTime ->
                snoozeTime.isAfter(LocalDateTime.now().plusMinutes(29)) &&
                snoozeTime.isBefore(LocalDateTime.now().plusMinutes(31))
            },
            any()
        )
    }

    /** snoozeReminder는 ONE_HOUR 옵션으로 1시간 후 스누즈한다 */
    @Test
    fun testSnoozeReminderWithOneHourOption() = runTest {
        // Given
        val reminderId = 1L
        val option = SnoozeOption.ONE_HOUR

        // When
        snoozeManager.snoozeReminder(reminderId, option)

        // Then
        verify(reminderDao).snoozeReminder(eq(reminderId), any(), any())
    }

    /** snoozeReminder는 TWO_HOURS 옵션으로 2시간 후 스누즈한다 */
    @Test
    fun testSnoozeReminderWithTwoHoursOption() = runTest {
        // Given
        val reminderId = 1L
        val option = SnoozeOption.TWO_HOURS

        // When
        snoozeManager.snoozeReminder(reminderId, option)

        // Then
        verify(reminderDao).snoozeReminder(eq(reminderId), any(), any())
    }

    /** snoozeReminder는 TOMORROW 옵션으로 내일 아침 스누즈한다 */
    @Test
    fun testSnoozeReminderWithTomorrowOption() = runTest {
        // Given
        val reminderId = 1L
        val option = SnoozeOption.TOMORROW

        // When
        snoozeManager.snoozeReminder(reminderId, option)

        // Then
        verify(reminderDao).snoozeReminder(eq(reminderId), any(), any())
    }

    /** snoozeReminder는 updatedAt을 현재 시간으로 설정한다 */
    @Test
    fun testSnoozeReminderSetsUpdatedAtToCurrentTime() = runTest {
        // Given
        val reminderId = 1L
        val option = SnoozeOption.TEN_MINUTES
        val beforeTime = LocalDateTime.now().minusSeconds(1)
        val afterTime = LocalDateTime.now().plusSeconds(1)

        // When
        snoozeManager.snoozeReminder(reminderId, option)

        // Then
        verify(reminderDao).snoozeReminder(
            eq(reminderId),
            any(),
            argThat { updatedAt ->
                updatedAt.isAfter(beforeTime) && updatedAt.isBefore(afterTime)
            }
        )
    }

    /** cancelSnooze는 리마인더의 스누즈를 취소한다 */
    @Test
    fun testCancelSnoozeCancelsSnoozeForReminder() = runTest {
        // Given
        val reminderId = 1L

        // When
        snoozeManager.cancelSnooze(reminderId)

        // Then
        verify(reminderDao).cancelSnooze(eq(reminderId), any())
    }

    /** cancelSnooze는 updatedAt을 현재 시간으로 설정한다 */
    @Test
    fun testCancelSnoozeSetsUpdatedAtToCurrentTime() = runTest {
        // Given
        val reminderId = 1L
        val beforeTime = LocalDateTime.now().minusSeconds(1)
        val afterTime = LocalDateTime.now().plusSeconds(1)

        // When
        snoozeManager.cancelSnooze(reminderId)

        // Then
        verify(reminderDao).cancelSnooze(
            eq(reminderId),
            argThat { updatedAt ->
                updatedAt.isAfter(beforeTime) && updatedAt.isBefore(afterTime)
            }
        )
    }

    /** getSnoozedRemindersDue는 스누즈 도래한 리마인더 목록을 반환한다 */
    @Test
    fun testGetSnoozedRemindersDueReturnsListOfDueReminderIds() = runTest {
        // Given
        val now = LocalDateTime.now()
        val dueReminders = listOf(
            ReminderEntity(id = 1, title = "스누즈1", snoozeUntil = now.minusMinutes(5)),
            ReminderEntity(id = 2, title = "스누즈2", snoozeUntil = now.minusMinutes(10)),
            ReminderEntity(id = 3, title = "스누즈3", snoozeUntil = now.minusHours(1))
        )
        whenever(reminderDao.getSnoozedRemindersDue(any())).thenReturn(dueReminders)

        // When
        val result = snoozeManager.getSnoozedRemindersDue()

        // Then
        assertEquals(3, result.size)
        assertEquals(listOf(1L, 2L, 3L), result)
    }

    /** getSnoozedRemindersDue는 반환 전에 자동으로 스누즈를 취소한다 */
    @Test
    fun testGetSnoozedRemindersDueAutomaticallyCancelsSnooze() = runTest {
        // Given
        val now = LocalDateTime.now()
        val dueReminders = listOf(
            ReminderEntity(id = 1, title = "스누즈1", snoozeUntil = now.minusMinutes(5)),
            ReminderEntity(id = 2, title = "스누즈2", snoozeUntil = now.minusMinutes(10))
        )
        whenever(reminderDao.getSnoozedRemindersDue(any())).thenReturn(dueReminders)

        // When
        snoozeManager.getSnoozedRemindersDue()

        // Then
        verify(reminderDao, times(2)).cancelSnooze(any(), any())
        verify(reminderDao).cancelSnooze(eq(1L), any())
        verify(reminderDao).cancelSnooze(eq(2L), any())
    }

    /** getSnoozedRemindersDue는 스누즈 도래한 리마인더가 없으면 빈 리스트를 반환한다 */
    @Test
    fun testGetSnoozedRemindersDueReturnsEmptyListWhenNoDueReminders() = runTest {
        // Given
        whenever(reminderDao.getSnoozedRemindersDue(any())).thenReturn(emptyList())

        // When
        val result = snoozeManager.getSnoozedRemindersDue()

        // Then
        assertTrue(result.isEmpty())
        verify(reminderDao, never()).cancelSnooze(any(), any())
    }

    /** getSnoozedRemindersDue는 현재 시간을 기준으로 조회한다 */
    @Test
    fun testGetSnoozedRemindersDueUsesCurrentTime() = runTest {
        // Given
        val beforeTime = LocalDateTime.now().minusSeconds(1)
        val afterTime = LocalDateTime.now().plusSeconds(1)
        whenever(reminderDao.getSnoozedRemindersDue(any())).thenReturn(emptyList())

        // When
        snoozeManager.getSnoozedRemindersDue()

        // Then
        verify(reminderDao).getSnoozedRemindersDue(
            argThat { currentTime ->
                currentTime.isAfter(beforeTime) && currentTime.isBefore(afterTime)
            }
        )
    }

    /** snoozeUntilCustomTime은 커스텀 시간으로 스누즈한다 */
    @Test
    fun testSnoozeUntilCustomTimeSnoozesWithCustomTime() = runTest {
        // Given
        val reminderId = 1L
        val customTime = LocalDateTime.of(2025, 10, 20, 14, 30)

        // When
        snoozeManager.snoozeUntilCustomTime(reminderId, customTime)

        // Then
        verify(reminderDao).snoozeReminder(
            eq(reminderId),
            eq(customTime),
            any()
        )
    }

    /** snoozeUntilCustomTime은 과거 시간도 설정할 수 있다 */
    @Test
    fun testSnoozeUntilCustomTimeAllowsPastTime() = runTest {
        // Given
        val reminderId = 1L
        val pastTime = LocalDateTime.now().minusDays(1)

        // When
        snoozeManager.snoozeUntilCustomTime(reminderId, pastTime)

        // Then
        verify(reminderDao).snoozeReminder(
            eq(reminderId),
            eq(pastTime),
            any()
        )
    }

    /** snoozeUntilCustomTime은 미래 시간도 설정할 수 있다 */
    @Test
    fun testSnoozeUntilCustomTimeAllowsFutureTime() = runTest {
        // Given
        val reminderId = 1L
        val futureTime = LocalDateTime.now().plusDays(7)

        // When
        snoozeManager.snoozeUntilCustomTime(reminderId, futureTime)

        // Then
        verify(reminderDao).snoozeReminder(
            eq(reminderId),
            eq(futureTime),
            any()
        )
    }

    /** snoozeUntilCustomTime은 updatedAt을 현재 시간으로 설정한다 */
    @Test
    fun testSnoozeUntilCustomTimeSetsUpdatedAtToCurrentTime() = runTest {
        // Given
        val reminderId = 1L
        val customTime = LocalDateTime.of(2025, 10, 20, 14, 30)
        val beforeTime = LocalDateTime.now().minusSeconds(1)
        val afterTime = LocalDateTime.now().plusSeconds(1)

        // When
        snoozeManager.snoozeUntilCustomTime(reminderId, customTime)

        // Then
        verify(reminderDao).snoozeReminder(
            eq(reminderId),
            any(),
            argThat { updatedAt ->
                updatedAt.isAfter(beforeTime) && updatedAt.isBefore(afterTime)
            }
        )
    }

    /** 여러 리마인더를 연속으로 스누즈할 수 있다 */
    @Test
    fun testCanSnoozeMultipleRemindersSequentially() = runTest {
        // Given
        val reminderIds = listOf(1L, 2L, 3L, 4L, 5L)
        val option = SnoozeOption.ONE_HOUR

        // When
        reminderIds.forEach { id ->
            snoozeManager.snoozeReminder(id, option)
        }

        // Then
        verify(reminderDao, times(5)).snoozeReminder(any(), any(), any())
        reminderIds.forEach { id ->
            verify(reminderDao).snoozeReminder(eq(id), any(), any())
        }
    }

    /** 스누즈와 취소를 반복할 수 있다 */
    @Test
    fun testCanSnoozeAndCancelRepeatedly() = runTest {
        // Given
        val reminderId = 1L

        // When
        snoozeManager.snoozeReminder(reminderId, SnoozeOption.TEN_MINUTES)
        snoozeManager.cancelSnooze(reminderId)
        snoozeManager.snoozeReminder(reminderId, SnoozeOption.ONE_HOUR)
        snoozeManager.cancelSnooze(reminderId)

        // Then
        verify(reminderDao, times(2)).snoozeReminder(eq(reminderId), any(), any())
        verify(reminderDao, times(2)).cancelSnooze(eq(reminderId), any())
    }
}
