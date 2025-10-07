package com.reminder.data.repository

import com.reminder.data.dao.ReminderDao
import com.reminder.data.entity.Priority
import com.reminder.data.entity.ReminderEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.time.LocalDateTime
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class ReminderRepositoryTest {

    private lateinit var dao: ReminderDao
    private lateinit var repository: ReminderRepository

    @Before
    fun setup() {
        dao = mock()
        // Repository 생성 전에 mock 설정
        whenever(dao.getAllReminders()).thenReturn(flowOf(emptyList()))
        whenever(dao.getActiveReminders()).thenReturn(flowOf(emptyList()))
        whenever(dao.getCompletedReminders()).thenReturn(flowOf(emptyList()))
        repository = ReminderRepository(dao)
    }

    @Test
    fun `allReminders는 DAO의 getAllReminders를 호출한다`() = runTest {
        // Repository 생성 시 이미 getAllReminders()가 호출됨
        // Then
        verify(dao).getAllReminders()
    }

    @Test
    fun `activeReminders는 DAO의 getActiveReminders를 호출한다`() = runTest {
        // Repository 생성 시 이미 getActiveReminders()가 호출됨
        // Then
        verify(dao).getActiveReminders()
    }

    @Test
    fun `completedReminders는 DAO의 getCompletedReminders를 호출한다`() = runTest {
        // Repository 생성 시 이미 getCompletedReminders()가 호출됨
        // Then
        verify(dao).getCompletedReminders()
    }

    @Test
    fun `getReminderById는 DAO에 ID를 전달하여 리마인더를 가져온다`() = runTest {
        // Given
        val reminderId = 1L
        val reminder = ReminderEntity(id = reminderId, title = "테스트 할일")
        whenever(dao.getReminderById(reminderId)).thenReturn(reminder)

        // When
        val result = repository.getReminderById(reminderId)

        // Then
        verify(dao).getReminderById(reminderId)
        assertEquals(reminder, result)
    }

    @Test
    fun `getRemindersByCategory는 DAO에 카테고리를 전달하여 리마인더를 가져온다`() = runTest {
        // Given
        val category = "업무"
        val reminders = listOf(
            ReminderEntity(id = 1, title = "회의", category = category)
        )
        whenever(dao.getRemindersByCategory(category)).thenReturn(flowOf(reminders))

        // When
        val result = repository.getRemindersByCategory(category)

        // Then
        verify(dao).getRemindersByCategory(category)
    }

    @Test
    fun `insertReminder는 DAO에 리마인더를 삽입하고 ID를 반환한다`() = runTest {
        // Given
        val reminder = ReminderEntity(title = "새 할일")
        val insertedId = 5L
        whenever(dao.insertReminder(reminder)).thenReturn(insertedId)

        // When
        val result = repository.insertReminder(reminder)

        // Then
        verify(dao).insertReminder(reminder)
        assertEquals(insertedId, result)
    }

    @Test
    fun `updateReminder는 DAO에 리마인더 업데이트를 요청한다`() = runTest {
        // Given
        val reminder = ReminderEntity(id = 1, title = "수정된 할일")

        // When
        repository.updateReminder(reminder)

        // Then
        verify(dao).updateReminder(reminder)
    }

    @Test
    fun `deleteReminder는 DAO에 리마인더 삭제를 요청한다`() = runTest {
        // Given
        val reminder = ReminderEntity(id = 1, title = "삭제할 할일")

        // When
        repository.deleteReminder(reminder)

        // Then
        verify(dao).deleteReminder(reminder)
    }

    @Test
    fun `deleteReminderById는 DAO에 ID로 삭제를 요청한다`() = runTest {
        // Given
        val reminderId = 1L

        // When
        repository.deleteReminderById(reminderId)

        // Then
        verify(dao).deleteReminderById(reminderId)
    }

    @Test
    fun `deleteAllCompletedReminders는 DAO에 완료된 리마인더 전체 삭제를 요청한다`() = runTest {
        // When
        repository.deleteAllCompletedReminders()

        // Then
        verify(dao).deleteAllCompletedReminders()
    }

    @Test
    fun `toggleReminderCompletion은 완료 상태를 반전시키고 업데이트 시간을 갱신한다`() = runTest {
        // Given
        val now = LocalDateTime.now()
        val reminder = ReminderEntity(
            id = 1,
            title = "토글 할일",
            isCompleted = false,
            updatedAt = now.minusHours(1)
        )

        // When
        repository.toggleReminderCompletion(reminder)

        // Then
        verify(dao).updateReminder(argThat { r ->
            r.id == reminder.id &&
            r.isCompleted == true &&
            r.updatedAt.isAfter(reminder.updatedAt)
        })
    }

    @Test
    fun `toggleReminderCompletion은 완료된 리마인더를 미완료로 변경한다`() = runTest {
        // Given
        val reminder = ReminderEntity(
            id = 1,
            title = "완료된 할일",
            isCompleted = true
        )

        // When
        repository.toggleReminderCompletion(reminder)

        // Then
        verify(dao).updateReminder(argThat { r ->
            r.id == reminder.id && r.isCompleted == false
        })
    }
}
