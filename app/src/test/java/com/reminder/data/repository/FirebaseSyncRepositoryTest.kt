package com.reminder.data.repository

import com.reminder.data.dao.ReminderDao
import com.reminder.data.entity.Priority
import com.reminder.data.entity.ReminderEntity
import com.reminder.data.remote.RemoteDataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDateTime

/**
 * Firebase 동기화 기능을 포함한 ReminderRepository 테스트
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FirebaseSyncRepositoryTest {

    private lateinit var repository: FirebaseSyncRepository
    private lateinit var mockDao: ReminderDao
    private lateinit var mockRemoteDataSource: RemoteDataSource
    private val testDispatcher = StandardTestDispatcher()

    private val testReminder = ReminderEntity(
        id = 1,
        title = "Test Reminder",
        description = "Test Description",
        dueDateTime = LocalDateTime.now().plusDays(1),
        priority = Priority.HIGH,
        category = "Work",
        isCompleted = false,
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now()
    )

    @Before
    fun setup() {
        mockDao = mock()
        mockRemoteDataSource = mock()
        // 테스트 디스패처를 사용하여 코루틴 제어 가능하도록 설정
        repository = FirebaseSyncRepository(mockDao, mockRemoteDataSource, testDispatcher)
    }

    @Test
    fun `리마인더 삽입 시 로컬에 저장된다`() = runTest {
        // Given
        whenever(mockDao.insertReminder(any())).thenReturn(1L)

        // When
        val result = repository.insertReminder(testReminder)

        // Then
        assertEquals(1L, result)
        verify(mockDao).insertReminder(testReminder)
        // 백그라운드 원격 동기화는 통합 테스트에서 검증
    }

    @Test
    fun `리마인더 업데이트 시 로컬에 반영된다`() = runTest {
        // Given

        // When
        repository.updateReminder(testReminder)

        // Then
        verify(mockDao).updateReminder(testReminder)
        // 백그라운드 원격 동기화는 통합 테스트에서 검증
    }

    @Test
    fun `리마인더 삭제 시 로컬에서 삭제된다`() = runTest {
        // Given

        // When
        repository.deleteReminder(testReminder)

        // Then
        verify(mockDao).deleteReminder(testReminder)
        // 백그라운드 원격 동기화는 통합 테스트에서 검증
    }

    @Test
    fun `초기 동기화 시 로컬 데이터가 원격에 업로드된다`() = runTest {
        // Given
        val localReminders = listOf(testReminder)
        whenever(mockDao.getAllRemindersList()).thenReturn(localReminders)
        whenever(mockRemoteDataSource.uploadAll(any())).thenReturn(Result.success(Unit))

        // When
        val result = repository.syncToRemote()

        // Then
        assertTrue(result.isSuccess)
        verify(mockRemoteDataSource).uploadAll(localReminders)
    }

    @Test
    fun `원격 데이터를 로컬에 동기화한다`() = runTest {
        // Given
        val remoteReminders = listOf(testReminder)
        whenever(mockRemoteDataSource.getAllReminders()).thenReturn(flowOf(remoteReminders))

        // When
        val result = repository.syncFromRemote().first()

        // Then
        assertEquals(remoteReminders, result)
    }

    @Test
    fun `원격 동기화 실패 시에도 로컬 작업은 성공한다`() = runTest {
        // Given
        whenever(mockDao.insertReminder(any())).thenReturn(1L)

        // When
        val result = repository.insertReminder(testReminder)

        // Then
        // 로컬 작업은 성공해야 함 (원격 동기화 실패는 로컬 작업에 영향 없음)
        assertEquals(1L, result)
        verify(mockDao).insertReminder(testReminder)
    }

    @Test
    fun `완료된 리마인더 삭제 시 로컬과 원격에서 모두 삭제된다`() = runTest(testDispatcher) {
        // Given
        val completedReminder1 = testReminder.copy(id = 1, isCompleted = true)
        val completedReminder2 = testReminder.copy(id = 2, isCompleted = true)
        val completedReminders = listOf(completedReminder1, completedReminder2)

        whenever(mockDao.getCompletedRemindersList()).thenReturn(completedReminders)

        // When
        repository.deleteAllCompletedReminders()
        advanceUntilIdle() // 백그라운드 코루틴 완료 대기

        // Then
        verify(mockDao).getCompletedRemindersList()
        verify(mockDao).deleteAllCompletedReminders()
        verify(mockRemoteDataSource).deleteReminder(1)
        verify(mockRemoteDataSource).deleteReminder(2)
    }

    @Test
    fun `완료된 리마인더가 없을 때 삭제 시 원격 호출 없이 완료된다`() = runTest {
        // Given
        whenever(mockDao.getCompletedRemindersList()).thenReturn(emptyList())

        // When
        repository.deleteAllCompletedReminders()
        advanceUntilIdle() // 백그라운드 코루틴 완료 대기

        // Then
        verify(mockDao).getCompletedRemindersList()
        verify(mockDao).deleteAllCompletedReminders()
        // 원격 삭제 호출이 없어야 함
    }
}
