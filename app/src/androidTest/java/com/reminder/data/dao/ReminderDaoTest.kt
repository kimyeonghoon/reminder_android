package com.reminder.data.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.reminder.data.database.ReminderDatabase
import com.reminder.data.entity.Priority
import com.reminder.data.entity.ReminderEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime

@RunWith(AndroidJUnit4::class)
class ReminderDaoTest {

    private lateinit var database: ReminderDatabase
    private lateinit var dao: ReminderDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            ReminderDatabase::class.java
        ).build()
        dao = database.reminderDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertReminder_AndGetById() = runTest {
        // Given
        val reminder = ReminderEntity(
            title = "테스트 할일",
            description = "테스트 설명",
            priority = Priority.HIGH
        )

        // When
        val insertedId = dao.insertReminder(reminder)
        val loaded = dao.getReminderById(insertedId)

        // Then
        assertNotNull(loaded)
        assertEquals("테스트 할일", loaded?.title)
        assertEquals("테스트 설명", loaded?.description)
        assertEquals(Priority.HIGH, loaded?.priority)
    }

    @Test
    fun getAllReminders_ReturnsAllReminders() = runTest {
        // Given
        dao.insertReminder(ReminderEntity(title = "할일 1"))
        dao.insertReminder(ReminderEntity(title = "할일 2"))
        dao.insertReminder(ReminderEntity(title = "할일 3"))

        // When
        val allReminders = dao.getAllReminders().first()

        // Then
        assertEquals(3, allReminders.size)
    }

    @Test
    fun getActiveReminders_OnlyReturnsIncompleteReminders() = runTest {
        // Given
        dao.insertReminder(ReminderEntity(title = "활성 1", isCompleted = false))
        dao.insertReminder(ReminderEntity(title = "완료", isCompleted = true))
        dao.insertReminder(ReminderEntity(title = "활성 2", isCompleted = false))

        // When
        val activeReminders = dao.getActiveReminders().first()

        // Then
        assertEquals(2, activeReminders.size)
        assertTrue(activeReminders.all { !it.isCompleted })
    }

    @Test
    fun getCompletedReminders_OnlyReturnsCompletedReminders() = runTest {
        // Given
        dao.insertReminder(ReminderEntity(title = "활성", isCompleted = false))
        dao.insertReminder(ReminderEntity(title = "완료 1", isCompleted = true))
        dao.insertReminder(ReminderEntity(title = "완료 2", isCompleted = true))

        // When
        val completedReminders = dao.getCompletedReminders().first()

        // Then
        assertEquals(2, completedReminders.size)
        assertTrue(completedReminders.all { it.isCompleted })
    }

    @Test
    fun getRemindersByCategory_FiltersByCategory() = runTest {
        // Given
        dao.insertReminder(ReminderEntity(title = "업무 1", category = "업무"))
        dao.insertReminder(ReminderEntity(title = "개인", category = "개인"))
        dao.insertReminder(ReminderEntity(title = "업무 2", category = "업무"))

        // When
        val workReminders = dao.getRemindersByCategory("업무").first()

        // Then
        assertEquals(2, workReminders.size)
        assertTrue(workReminders.all { it.category == "업무" })
    }

    @Test
    fun updateReminder_UpdatesExistingReminder() = runTest {
        // Given
        val reminder = ReminderEntity(title = "원본 제목")
        val id = dao.insertReminder(reminder)

        // When
        val loaded = dao.getReminderById(id)!!
        val updated = loaded.copy(title = "수정된 제목", description = "새로운 설명")
        dao.updateReminder(updated)

        // Then
        val result = dao.getReminderById(id)
        assertEquals("수정된 제목", result?.title)
        assertEquals("새로운 설명", result?.description)
    }

    @Test
    fun deleteReminder_RemovesReminder() = runTest {
        // Given
        val reminder = ReminderEntity(title = "삭제할 할일")
        val id = dao.insertReminder(reminder)

        // When
        val loaded = dao.getReminderById(id)!!
        dao.deleteReminder(loaded)

        // Then
        val result = dao.getReminderById(id)
        assertNull(result)
    }

    @Test
    fun deleteReminderById_RemovesReminderById() = runTest {
        // Given
        val reminder = ReminderEntity(title = "삭제할 할일")
        val id = dao.insertReminder(reminder)

        // When
        dao.deleteReminderById(id)

        // Then
        val result = dao.getReminderById(id)
        assertNull(result)
    }

    @Test
    fun deleteAllCompletedReminders_OnlyDeletesCompletedOnes() = runTest {
        // Given
        dao.insertReminder(ReminderEntity(title = "활성 1", isCompleted = false))
        dao.insertReminder(ReminderEntity(title = "완료 1", isCompleted = true))
        dao.insertReminder(ReminderEntity(title = "활성 2", isCompleted = false))
        dao.insertReminder(ReminderEntity(title = "완료 2", isCompleted = true))

        // When
        dao.deleteAllCompletedReminders()

        // Then
        val remaining = dao.getAllReminders().first()
        assertEquals(2, remaining.size)
        assertTrue(remaining.all { !it.isCompleted })
    }

    @Test
    fun activeReminders_OrderedByDueDateAndPriority() = runTest {
        // Given
        val now = LocalDateTime.now()
        dao.insertReminder(
            ReminderEntity(
                title = "낮은 우선순위, 빠른 마감",
                priority = Priority.LOW,
                dueDateTime = now.plusDays(1),
                isCompleted = false
            )
        )
        dao.insertReminder(
            ReminderEntity(
                title = "높은 우선순위, 늦은 마감",
                priority = Priority.HIGH,
                dueDateTime = now.plusDays(3),
                isCompleted = false
            )
        )
        dao.insertReminder(
            ReminderEntity(
                title = "높은 우선순위, 빠른 마감",
                priority = Priority.HIGH,
                dueDateTime = now.plusDays(1),
                isCompleted = false
            )
        )

        // When
        val activeReminders = dao.getActiveReminders().first()

        // Then
        assertEquals(3, activeReminders.size)
        // dueDateTime ASC, priority DESC 순서로 정렬
        assertEquals("높은 우선순위, 빠른 마감", activeReminders[0].title)
    }

    @Test
    fun insertReminder_WithAllFields() = runTest {
        // Given
        val now = LocalDateTime.now()
        val reminder = ReminderEntity(
            title = "완전한 할일",
            description = "상세 설명",
            dueDateTime = now.plusDays(7),
            priority = Priority.MEDIUM,
            category = "업무",
            isCompleted = false
        )

        // When
        val id = dao.insertReminder(reminder)
        val loaded = dao.getReminderById(id)

        // Then
        assertNotNull(loaded)
        assertEquals("완전한 할일", loaded?.title)
        assertEquals("상세 설명", loaded?.description)
        assertEquals(Priority.MEDIUM, loaded?.priority)
        assertEquals("업무", loaded?.category)
        assertEquals(false, loaded?.isCompleted)
        assertNotNull(loaded?.dueDateTime)
    }
}
