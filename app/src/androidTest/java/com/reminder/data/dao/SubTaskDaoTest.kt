package com.reminder.data.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.reminder.data.database.ReminderDatabase
import com.reminder.data.entity.Priority
import com.reminder.data.entity.ReminderEntity
import com.reminder.data.entity.SubTask
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime

/**
 * SubTaskDao 통합 테스트 (TDD)
 */
@RunWith(AndroidJUnit4::class)
class SubTaskDaoTest {

    private lateinit var database: ReminderDatabase
    private lateinit var reminderDao: ReminderDao
    private lateinit var subTaskDao: SubTaskDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            ReminderDatabase::class.java
        ).build()
        reminderDao = database.reminderDao()
        subTaskDao = database.subTaskDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndGetSubTask() = runTest {
        // Given: 리마인더 생성
        val reminder = ReminderEntity(
            title = "메인 할 일",
            description = "",
            priority = Priority.MEDIUM
        )
        val reminderId = reminderDao.insertReminder(reminder)

        // Given: 서브태스크 생성
        val subTask = SubTask(
            reminderId = reminderId,
            title = "서브 할 일 1"
        )

        // When: 서브태스크 저장
        val subTaskId = subTaskDao.insert(subTask)

        // Then: 저장된 서브태스크 조회
        val loaded = subTaskDao.getSubTaskById(subTaskId)
        assertNotNull(loaded)
        assertEquals(subTaskId, loaded?.id)
        assertEquals("서브 할 일 1", loaded?.title)
        assertEquals(reminderId, loaded?.reminderId)
        assertFalse(loaded?.isCompleted == true)
    }

    @Test
    fun getSubTasksByReminderId() = runTest {
        // Given: 리마인더 생성
        val reminder = ReminderEntity(
            title = "메인 할 일",
            description = "",
            priority = Priority.HIGH
        )
        val reminderId = reminderDao.insertReminder(reminder)

        // Given: 여러 서브태스크 생성
        val subTask1 = SubTask(reminderId = reminderId, title = "서브1", position = 0)
        val subTask2 = SubTask(reminderId = reminderId, title = "서브2", position = 1)
        val subTask3 = SubTask(reminderId = reminderId, title = "서브3", position = 2)

        subTaskDao.insert(subTask1)
        subTaskDao.insert(subTask2)
        subTaskDao.insert(subTask3)

        // When: 리마인더별 서브태스크 조회
        val subTasks = subTaskDao.getSubTasksByReminderId(reminderId).first()

        // Then: 3개 모두 조회됨, position 순서로 정렬됨
        assertEquals(3, subTasks.size)
        assertEquals("서브1", subTasks[0].title)
        assertEquals("서브2", subTasks[1].title)
        assertEquals("서브3", subTasks[2].title)
    }

    @Test
    fun updateSubTask() = runTest {
        // Given: 리마인더 및 서브태스크 생성
        val reminder = ReminderEntity(
            title = "메인",
            description = "",
            priority = Priority.LOW
        )
        val reminderId = reminderDao.insertReminder(reminder)

        val subTask = SubTask(
            reminderId = reminderId,
            title = "서브 할 일"
        )
        val subTaskId = subTaskDao.insert(subTask)

        // When: 서브태스크 완료 처리
        val loaded = subTaskDao.getSubTaskById(subTaskId)!!
        val updated = loaded.copy(isCompleted = true)
        subTaskDao.update(updated)

        // Then: 업데이트된 상태 확인
        val reloaded = subTaskDao.getSubTaskById(subTaskId)
        assertTrue(reloaded?.isCompleted == true)
    }

    @Test
    fun deleteSubTask() = runTest {
        // Given: 리마인더 및 서브태스크 생성
        val reminder = ReminderEntity(
            title = "메인",
            description = "",
            priority = Priority.MEDIUM
        )
        val reminderId = reminderDao.insertReminder(reminder)

        val subTask = SubTask(
            reminderId = reminderId,
            title = "삭제될 서브태스크"
        )
        val subTaskId = subTaskDao.insert(subTask)

        // When: 서브태스크 삭제
        val loaded = subTaskDao.getSubTaskById(subTaskId)!!
        subTaskDao.delete(loaded)

        // Then: 삭제 확인
        val deleted = subTaskDao.getSubTaskById(subTaskId)
        assertNull(deleted)
    }

    @Test
    fun cascadeDeleteWhenReminderDeleted() = runTest {
        // Given: 리마인더 및 서브태스크 생성
        val reminder = ReminderEntity(
            title = "삭제될 메인",
            description = "",
            priority = Priority.HIGH
        )
        val reminderId = reminderDao.insertReminder(reminder)

        val subTask1 = SubTask(reminderId = reminderId, title = "서브1")
        val subTask2 = SubTask(reminderId = reminderId, title = "서브2")

        subTaskDao.insert(subTask1)
        subTaskDao.insert(subTask2)

        // When: 리마인더 삭제 (CASCADE)
        val loadedReminder = reminderDao.getReminderById(reminderId)!!
        reminderDao.deleteReminder(loadedReminder)

        // Then: 서브태스크도 함께 삭제됨
        val subTasks = subTaskDao.getSubTasksByReminderId(reminderId).first()
        assertEquals(0, subTasks.size)
    }

    @Test
    fun getCompletedSubTasksCount() = runTest {
        // Given: 리마인더 및 서브태스크 생성
        val reminder = ReminderEntity(
            title = "메인",
            description = "",
            priority = Priority.MEDIUM
        )
        val reminderId = reminderDao.insertReminder(reminder)

        val subTask1 = SubTask(reminderId = reminderId, title = "서브1", isCompleted = true)
        val subTask2 = SubTask(reminderId = reminderId, title = "서브2", isCompleted = false)
        val subTask3 = SubTask(reminderId = reminderId, title = "서브3", isCompleted = true)

        subTaskDao.insert(subTask1)
        subTaskDao.insert(subTask2)
        subTaskDao.insert(subTask3)

        // When: 완료된 서브태스크 개수 조회
        val completedCount = subTaskDao.getCompletedSubTasksCount(reminderId)

        // Then: 2개가 완료됨
        assertEquals(2, completedCount)
    }
}
