package com.reminder.viewmodel

import com.reminder.archive.ArchiveManager
import com.reminder.data.entity.ReminderEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
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
import java.time.LocalDateTime

/**
 * ArchiveViewModel 테스트
 *
 * AAA 패턴 (Given-When-Then)으로 모든 메서드 검증
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ArchiveViewModelTest {

    private lateinit var archiveManager: ArchiveManager
    private lateinit var viewModel: ArchiveViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        archiveManager = mock(ArchiveManager::class.java)
        // 기본 빈 리스트로 초기화
        `when`(archiveManager.getArchivedReminders()).thenReturn(flowOf(emptyList()))
        viewModel = ArchiveViewModel(archiveManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /** 초기 상태는 빈 리스트와 로딩 중 아님이다 */
    @Test
    fun initialStateIsEmptyListAndNotLoading() {
        assertEquals(emptyList<ReminderEntity>(), viewModel.archivedReminders.value)
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.errorMessage.value)
        assertNull(viewModel.successMessage.value)
    }

    /** archivedReminders는 ArchiveManager의 Flow를 사용한다 */
    @Test
    fun archivedRemindersUsesArchiveManagerFlow() = runTest {
        // Given & When (setup에서 이미 초기화됨)

        // Then
        verify(archiveManager).getArchivedReminders()
        assertNotNull(viewModel.archivedReminders.value)
    }

    /** archiveReminder는 리마인더를 아카이브하고 성공 메시지를 표시한다 */
    @Test
    fun archiveReminderArchivesAndShowsSuccessMessage() = runTest {
        // Given
        val reminder = createReminder(id = 1, title = "테스트")
        whenever(archiveManager.archiveReminder(any())).thenReturn(Unit)

        // When
        viewModel.archiveReminder(reminder)
        advanceUntilIdle()

        // Then
        verify(archiveManager).archiveReminder(reminder)
        assertEquals("리마인더가 아카이브되었습니다", viewModel.successMessage.value)
        assertFalse(viewModel.isLoading.value)
    }

    /** archiveReminder는 실행 중 로딩 상태를 true로 설정한다 */
    @Test
    fun archiveReminderSetsLoadingToTrueDuringExecution() = runTest {
        // Given
        val reminder = createReminder(id = 1)
        var loadingDuringExecution = false
        whenever(archiveManager.archiveReminder(any())).then {
            loadingDuringExecution = viewModel.isLoading.value
            Unit
        }

        // When
        viewModel.archiveReminder(reminder)
        advanceUntilIdle()

        // Then
        assertTrue(loadingDuringExecution)
        assertFalse(viewModel.isLoading.value) // 완료 후에는 false
    }

    /** archiveReminder는 오류 발생 시 에러 메시지를 표시한다 */
    @Test
    fun archiveReminderShowsErrorMessageOnFailure() = runTest {
        // Given
        val reminder = createReminder(id = 1)
        val errorMsg = "아카이브 실패"
        whenever(archiveManager.archiveReminder(any())).thenThrow(RuntimeException(errorMsg))

        // When
        viewModel.archiveReminder(reminder)
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.errorMessage.value?.contains(errorMsg) == true)
        assertFalse(viewModel.isLoading.value)
    }

    /** unarchiveReminder는 리마인더를 복원하고 성공 메시지를 표시한다 */
    @Test
    fun unarchiveReminderRestoresAndShowsSuccessMessage() = runTest {
        // Given
        val reminder = createReminder(id = 1, isArchived = true)
        whenever(archiveManager.unarchiveReminder(any())).thenReturn(Unit)

        // When
        viewModel.unarchiveReminder(reminder)
        advanceUntilIdle()

        // Then
        verify(archiveManager).unarchiveReminder(reminder)
        assertEquals("리마인더가 복원되었습니다", viewModel.successMessage.value)
        assertFalse(viewModel.isLoading.value)
    }

    /** unarchiveReminder는 실행 중 로딩 상태를 true로 설정한다 */
    @Test
    fun unarchiveReminderSetsLoadingToTrueDuringExecution() = runTest {
        // Given
        val reminder = createReminder(id = 1, isArchived = true)
        var loadingDuringExecution = false
        whenever(archiveManager.unarchiveReminder(any())).then {
            loadingDuringExecution = viewModel.isLoading.value
            Unit
        }

        // When
        viewModel.unarchiveReminder(reminder)
        advanceUntilIdle()

        // Then
        assertTrue(loadingDuringExecution)
        assertFalse(viewModel.isLoading.value)
    }

    /** unarchiveReminder는 오류 발생 시 에러 메시지를 표시한다 */
    @Test
    fun unarchiveReminderShowsErrorMessageOnFailure() = runTest {
        // Given
        val reminder = createReminder(id = 1, isArchived = true)
        val errorMsg = "복원 실패"
        whenever(archiveManager.unarchiveReminder(any())).thenThrow(RuntimeException(errorMsg))

        // When
        viewModel.unarchiveReminder(reminder)
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.errorMessage.value?.contains(errorMsg) == true)
        assertFalse(viewModel.isLoading.value)
    }

    /** autoArchiveOldCompleted는 오래된 리마인더를 아카이브하고 개수를 표시한다 */
    @Test
    fun autoArchiveOldCompletedArchivesOldRemindersAndShowsCount() = runTest {
        // Given
        val archivedCount = 5
        whenever(archiveManager.autoArchiveOldCompletedReminders(30)).thenReturn(archivedCount)

        // When
        viewModel.autoArchiveOldCompleted(30)
        advanceUntilIdle()

        // Then
        verify(archiveManager).autoArchiveOldCompletedReminders(30)
        assertEquals("5개의 리마인더가 아카이브되었습니다", viewModel.successMessage.value)
        assertFalse(viewModel.isLoading.value)
    }

    /** autoArchiveOldCompleted는 기본값 30일을 사용한다 */
    @Test
    fun autoArchiveOldCompletedUsesDefault30Days() = runTest {
        // Given
        whenever(archiveManager.autoArchiveOldCompletedReminders(30)).thenReturn(3)

        // When
        viewModel.autoArchiveOldCompleted()
        advanceUntilIdle()

        // Then
        verify(archiveManager).autoArchiveOldCompletedReminders(30)
    }

    /** autoArchiveOldCompleted는 실행 중 로딩 상태를 true로 설정한다 */
    @Test
    fun autoArchiveOldCompletedSetsLoadingToTrueDuringExecution() = runTest {
        // Given
        var loadingDuringExecution = false
        whenever(archiveManager.autoArchiveOldCompletedReminders(any())).then {
            loadingDuringExecution = viewModel.isLoading.value
            0
        }

        // When
        viewModel.autoArchiveOldCompleted()
        advanceUntilIdle()

        // Then
        assertTrue(loadingDuringExecution)
        assertFalse(viewModel.isLoading.value)
    }

    /** autoArchiveOldCompleted는 오류 발생 시 에러 메시지를 표시한다 */
    @Test
    fun autoArchiveOldCompletedShowsErrorMessageOnFailure() = runTest {
        // Given
        val errorMsg = "자동 아카이브 실패"
        whenever(archiveManager.autoArchiveOldCompletedReminders(any()))
            .thenThrow(RuntimeException(errorMsg))

        // When
        viewModel.autoArchiveOldCompleted()
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.errorMessage.value?.contains(errorMsg) == true)
        assertFalse(viewModel.isLoading.value)
    }

    /** deleteArchivedReminder는 아카이브된 리마인더를 영구 삭제한다 */
    @Test
    fun deleteArchivedReminderPermanentlyDeletesReminder() = runTest {
        // Given
        val reminder = createReminder(id = 1, isArchived = true)
        whenever(archiveManager.deleteArchivedReminder(any())).thenReturn(Unit)

        // When
        viewModel.deleteArchivedReminder(reminder)
        advanceUntilIdle()

        // Then
        verify(archiveManager).deleteArchivedReminder(reminder)
        assertEquals("리마인더가 영구 삭제되었습니다", viewModel.successMessage.value)
        assertFalse(viewModel.isLoading.value)
    }

    /** deleteArchivedReminder는 실행 중 로딩 상태를 true로 설정한다 */
    @Test
    fun deleteArchivedReminderSetsLoadingToTrueDuringExecution() = runTest {
        // Given
        val reminder = createReminder(id = 1, isArchived = true)
        var loadingDuringExecution = false
        whenever(archiveManager.deleteArchivedReminder(any())).then {
            loadingDuringExecution = viewModel.isLoading.value
            Unit
        }

        // When
        viewModel.deleteArchivedReminder(reminder)
        advanceUntilIdle()

        // Then
        assertTrue(loadingDuringExecution)
        assertFalse(viewModel.isLoading.value)
    }

    /** deleteArchivedReminder는 오류 발생 시 에러 메시지를 표시한다 */
    @Test
    fun deleteArchivedReminderShowsErrorMessageOnFailure() = runTest {
        // Given
        val reminder = createReminder(id = 1, isArchived = true)
        val errorMsg = "삭제 실패"
        whenever(archiveManager.deleteArchivedReminder(any())).thenThrow(RuntimeException(errorMsg))

        // When
        viewModel.deleteArchivedReminder(reminder)
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.errorMessage.value?.contains(errorMsg) == true)
        assertFalse(viewModel.isLoading.value)
    }

    /** deleteAllArchived는 모든 아카이브를 일괄 삭제하고 개수를 표시한다 */
    @Test
    fun deleteAllArchivedDeletesAllAndShowsCount() = runTest {
        // Given
        val deletedCount = 10
        whenever(archiveManager.deleteAllArchived()).thenReturn(deletedCount)

        // When
        viewModel.deleteAllArchived()
        advanceUntilIdle()

        // Then
        verify(archiveManager).deleteAllArchived()
        assertEquals("10개의 리마인더가 영구 삭제되었습니다", viewModel.successMessage.value)
        assertFalse(viewModel.isLoading.value)
    }

    /** deleteAllArchived는 실행 중 로딩 상태를 true로 설정한다 */
    @Test
    fun deleteAllArchivedSetsLoadingToTrueDuringExecution() = runTest {
        // Given
        var loadingDuringExecution = false
        whenever(archiveManager.deleteAllArchived()).then {
            loadingDuringExecution = viewModel.isLoading.value
            0
        }

        // When
        viewModel.deleteAllArchived()
        advanceUntilIdle()

        // Then
        assertTrue(loadingDuringExecution)
        assertFalse(viewModel.isLoading.value)
    }

    /** deleteAllArchived는 오류 발생 시 에러 메시지를 표시한다 */
    @Test
    fun deleteAllArchivedShowsErrorMessageOnFailure() = runTest {
        // Given
        val errorMsg = "일괄 삭제 실패"
        whenever(archiveManager.deleteAllArchived()).thenThrow(RuntimeException(errorMsg))

        // When
        viewModel.deleteAllArchived()
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.errorMessage.value?.contains(errorMsg) == true)
        assertFalse(viewModel.isLoading.value)
    }

    /** clearMessages는 에러와 성공 메시지를 초기화한다 */
    @Test
    fun clearMessagesClearsErrorAndSuccessMessages() = runTest {
        // Given
        val reminder = createReminder(id = 1)
        whenever(archiveManager.archiveReminder(any())).thenReturn(Unit)
        viewModel.archiveReminder(reminder)
        advanceUntilIdle()
        assertNotNull(viewModel.successMessage.value)

        // When
        viewModel.clearMessages()

        // Then
        assertNull(viewModel.errorMessage.value)
        assertNull(viewModel.successMessage.value)
    }

    /** clearMessages는 에러 메시지도 초기화한다 */
    @Test
    fun clearMessagesClearsErrorMessage() = runTest {
        // Given
        val reminder = createReminder(id = 1)
        whenever(archiveManager.archiveReminder(any())).thenThrow(RuntimeException("오류"))
        viewModel.archiveReminder(reminder)
        advanceUntilIdle()
        assertNotNull(viewModel.errorMessage.value)

        // When
        viewModel.clearMessages()

        // Then
        assertNull(viewModel.errorMessage.value)
        assertNull(viewModel.successMessage.value)
    }

    /** 여러 리마인더를 연속으로 아카이브할 수 있다 */
    @Test
    fun canArchiveMultipleRemindersSequentially() = runTest {
        // Given
        val reminder1 = createReminder(id = 1)
        val reminder2 = createReminder(id = 2)
        val reminder3 = createReminder(id = 3)
        whenever(archiveManager.archiveReminder(any())).thenReturn(Unit)

        // When
        viewModel.archiveReminder(reminder1)
        advanceUntilIdle()
        viewModel.archiveReminder(reminder2)
        advanceUntilIdle()
        viewModel.archiveReminder(reminder3)
        advanceUntilIdle()

        // Then
        verify(archiveManager, times(3)).archiveReminder(any())
    }

    /** 아카이브와 복원을 반복할 수 있다 */
    @Test
    fun canArchiveAndUnarchiveRepeatedly() = runTest {
        // Given
        val reminder = createReminder(id = 1)
        whenever(archiveManager.archiveReminder(any())).thenReturn(Unit)
        whenever(archiveManager.unarchiveReminder(any())).thenReturn(Unit)

        // When
        viewModel.archiveReminder(reminder)
        advanceUntilIdle()
        viewModel.unarchiveReminder(reminder)
        advanceUntilIdle()
        viewModel.archiveReminder(reminder)
        advanceUntilIdle()

        // Then
        verify(archiveManager, times(2)).archiveReminder(reminder)
        verify(archiveManager, times(1)).unarchiveReminder(reminder)
    }

    /** 서로 다른 임계값으로 자동 아카이브를 실행할 수 있다 */
    @Test
    fun canRunAutoArchiveWithDifferentThresholds() = runTest {
        // Given
        whenever(archiveManager.autoArchiveOldCompletedReminders(any())).thenReturn(0)

        // When
        viewModel.autoArchiveOldCompleted(7)
        advanceUntilIdle()
        viewModel.autoArchiveOldCompleted(30)
        advanceUntilIdle()
        viewModel.autoArchiveOldCompleted(90)
        advanceUntilIdle()

        // Then
        verify(archiveManager).autoArchiveOldCompletedReminders(7)
        verify(archiveManager).autoArchiveOldCompletedReminders(30)
        verify(archiveManager).autoArchiveOldCompletedReminders(90)
    }

    // Helper function
    private fun createReminder(
        id: Long,
        title: String = "Test Reminder $id",
        isArchived: Boolean = false
    ) = ReminderEntity(
        id = id,
        title = title,
        isArchived = isArchived,
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now()
    )
}
