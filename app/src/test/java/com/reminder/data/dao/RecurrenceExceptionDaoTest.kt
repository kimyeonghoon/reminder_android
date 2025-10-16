package com.reminder.data.dao

import com.reminder.data.entity.RecurrenceExceptionEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.time.LocalDate
import org.junit.Assert.*

/**
 * RecurrenceExceptionDao 테스트
 * - 모든 DAO 메서드 테스트 (10개)
 * - Mockito + kotlin.test + runTest 사용
 * - AAA 패턴 (Given-When-Then)
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RecurrenceExceptionDaoTest {

    private lateinit var dao: RecurrenceExceptionDao

    @Before
    fun setup() {
        dao = mock()
    }

    /** getExceptionsByReminderId는 특정 리마인더의 모든 예외 날짜를 Flow로 반환한다 */
    @Test
    fun testGetExceptionsByReminderIdReturnsFlowOfExceptions() = runTest {
        // Given
        val reminderId = 1L
        val exceptions = listOf(
            RecurrenceExceptionEntity(id = 1, reminderId = reminderId, exceptionDate = LocalDate.of(2025, 10, 20)),
            RecurrenceExceptionEntity(id = 2, reminderId = reminderId, exceptionDate = LocalDate.of(2025, 10, 25))
        )
        whenever(dao.getExceptionsByReminderId(reminderId)).thenReturn(flowOf(exceptions))

        // When
        val result = dao.getExceptionsByReminderId(reminderId)

        // Then
        verify(dao).getExceptionsByReminderId(reminderId)
    }

    /** getExceptionsByReminderIdOnce는 특정 리마인더의 모든 예외 날짜를 일회성으로 반환한다 */
    @Test
    fun testGetExceptionsByReminderIdOnceReturnsListOfExceptions() = runTest {
        // Given
        val reminderId = 1L
        val exceptions = listOf(
            RecurrenceExceptionEntity(id = 1, reminderId = reminderId, exceptionDate = LocalDate.of(2025, 10, 20)),
            RecurrenceExceptionEntity(id = 2, reminderId = reminderId, exceptionDate = LocalDate.of(2025, 10, 25))
        )
        whenever(dao.getExceptionsByReminderIdOnce(reminderId)).thenReturn(exceptions)

        // When
        val result = dao.getExceptionsByReminderIdOnce(reminderId)

        // Then
        verify(dao).getExceptionsByReminderIdOnce(reminderId)
        assertEquals(exceptions, result)
    }

    /** insertException은 예외 날짜를 추가하고 ID를 반환한다 */
    @Test
    fun testInsertExceptionInsertsExceptionAndReturnsId() = runTest {
        // Given
        val exception = RecurrenceExceptionEntity(
            reminderId = 1L,
            exceptionDate = LocalDate.of(2025, 10, 20)
        )
        val insertedId = 5L
        whenever(dao.insertException(exception)).thenReturn(insertedId)

        // When
        val result = dao.insertException(exception)

        // Then
        verify(dao).insertException(exception)
        assertEquals(insertedId, result)
    }

    /** insertExceptions는 여러 예외 날짜를 한 번에 추가한다 */
    @Test
    fun testInsertExceptionsInsertsMultipleExceptions() = runTest {
        // Given
        val exceptions = listOf(
            RecurrenceExceptionEntity(reminderId = 1L, exceptionDate = LocalDate.of(2025, 10, 20)),
            RecurrenceExceptionEntity(reminderId = 1L, exceptionDate = LocalDate.of(2025, 10, 25)),
            RecurrenceExceptionEntity(reminderId = 1L, exceptionDate = LocalDate.of(2025, 10, 30))
        )

        // When
        dao.insertExceptions(exceptions)

        // Then
        verify(dao).insertExceptions(exceptions)
    }

    /** deleteException은 특정 예외 날짜를 삭제한다 */
    @Test
    fun testDeleteExceptionDeletesException() = runTest {
        // Given
        val exception = RecurrenceExceptionEntity(
            id = 1L,
            reminderId = 1L,
            exceptionDate = LocalDate.of(2025, 10, 20)
        )

        // When
        dao.deleteException(exception)

        // Then
        verify(dao).deleteException(exception)
    }

    /** deleteExceptionsByReminderId는 특정 리마인더의 모든 예외 날짜를 삭제한다 */
    @Test
    fun testDeleteExceptionsByReminderIdDeletesAllExceptionsForReminder() = runTest {
        // Given
        val reminderId = 1L

        // When
        dao.deleteExceptionsByReminderId(reminderId)

        // Then
        verify(dao).deleteExceptionsByReminderId(reminderId)
    }

    /** deleteExceptionByDate는 특정 리마인더의 특정 날짜 예외를 삭제한다 */
    @Test
    fun testDeleteExceptionByDateDeletesSpecificException() = runTest {
        // Given
        val reminderId = 1L
        val date = LocalDate.of(2025, 10, 20)

        // When
        dao.deleteExceptionByDate(reminderId, date)

        // Then
        verify(dao).deleteExceptionByDate(reminderId, date)
    }

    /** isExceptionDate는 특정 날짜가 예외 날짜인 경우 true를 반환한다 */
    @Test
    fun testIsExceptionDateReturnsTrueWhenDateIsException() = runTest {
        // Given
        val reminderId = 1L
        val date = LocalDate.of(2025, 10, 20)
        whenever(dao.isExceptionDate(reminderId, date)).thenReturn(true)

        // When
        val result = dao.isExceptionDate(reminderId, date)

        // Then
        verify(dao).isExceptionDate(reminderId, date)
        assertTrue(result)
    }

    /** isExceptionDate는 특정 날짜가 예외 날짜가 아닌 경우 false를 반환한다 */
    @Test
    fun testIsExceptionDateReturnsFalseWhenDateIsNotException() = runTest {
        // Given
        val reminderId = 1L
        val date = LocalDate.of(2025, 10, 20)
        whenever(dao.isExceptionDate(reminderId, date)).thenReturn(false)

        // When
        val result = dao.isExceptionDate(reminderId, date)

        // Then
        verify(dao).isExceptionDate(reminderId, date)
        assertFalse(result)
    }

    /** getExceptionCount는 특정 리마인더의 예외 날짜 개수를 반환한다 */
    @Test
    fun testGetExceptionCountReturnsExceptionCount() = runTest {
        // Given
        val reminderId = 1L
        val count = 3
        whenever(dao.getExceptionCount(reminderId)).thenReturn(count)

        // When
        val result = dao.getExceptionCount(reminderId)

        // Then
        verify(dao).getExceptionCount(reminderId)
        assertEquals(count, result)
    }

    /** getExceptionCount는 예외가 없는 경우 0을 반환한다 */
    @Test
    fun testGetExceptionCountReturnsZeroWhenNoExceptions() = runTest {
        // Given
        val reminderId = 1L
        whenever(dao.getExceptionCount(reminderId)).thenReturn(0)

        // When
        val result = dao.getExceptionCount(reminderId)

        // Then
        verify(dao).getExceptionCount(reminderId)
        assertEquals(0, result)
    }
}
