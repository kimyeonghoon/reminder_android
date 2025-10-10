package com.reminder.data.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.reminder.data.database.ReminderDatabase
import com.reminder.data.entity.ActionType
import com.reminder.data.entity.PendingActionEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime

/**
 * v1.38.0: PendingActionDao 통합 테스트
 *
 * TDD Red 단계: 실패하는 테스트 먼저 작성
 */
@RunWith(AndroidJUnit4::class)
class PendingActionDaoTest {

    private lateinit var database: ReminderDatabase
    private lateinit var pendingActionDao: PendingActionDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            ReminderDatabase::class.java
        ).build()
        pendingActionDao = database.pendingActionDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `대기 작업을 삽입하고 ID를 반환한다`() = runTest {
        // Given
        val action = PendingActionEntity(
            reminderId = 1L,
            actionType = ActionType.INSERT,
            createdAt = LocalDateTime.now()
        )

        // When
        val id = pendingActionDao.insertPendingAction(action)

        // Then
        assertTrue("ID should be greater than 0", id > 0)
    }

    @Test
    fun `모든 대기 작업을 조회한다`() = runTest {
        // Given
        val action1 = PendingActionEntity(
            reminderId = 1L,
            actionType = ActionType.INSERT,
            createdAt = LocalDateTime.now()
        )
        val action2 = PendingActionEntity(
            reminderId = 2L,
            actionType = ActionType.UPDATE,
            createdAt = LocalDateTime.now().plusMinutes(1)
        )
        pendingActionDao.insertPendingAction(action1)
        pendingActionDao.insertPendingAction(action2)

        // When
        val actions = pendingActionDao.getAllPendingActions().first()

        // Then
        assertEquals("Should have 2 actions", 2, actions.size)
        assertEquals("First action should be oldest", ActionType.INSERT, actions[0].actionType)
    }

    @Test
    fun `특정 리마인더의 대기 작업을 조회한다`() = runTest {
        // Given
        val action1 = PendingActionEntity(
            reminderId = 1L,
            actionType = ActionType.INSERT,
            createdAt = LocalDateTime.now()
        )
        val action2 = PendingActionEntity(
            reminderId = 2L,
            actionType = ActionType.UPDATE,
            createdAt = LocalDateTime.now()
        )
        val action3 = PendingActionEntity(
            reminderId = 1L,
            actionType = ActionType.DELETE,
            createdAt = LocalDateTime.now()
        )
        pendingActionDao.insertPendingAction(action1)
        pendingActionDao.insertPendingAction(action2)
        pendingActionDao.insertPendingAction(action3)

        // When
        val actions = pendingActionDao.getPendingActionsByReminderId(1L)

        // Then
        assertEquals("Should have 2 actions for reminder 1", 2, actions.size)
        assertTrue("All actions should be for reminder 1", actions.all { it.reminderId == 1L })
    }

    @Test
    fun `재시도 횟수가 낮은 작업만 조회한다`() = runTest {
        // Given
        val action1 = PendingActionEntity(
            reminderId = 1L,
            actionType = ActionType.INSERT,
            createdAt = LocalDateTime.now(),
            retryCount = 1
        )
        val action2 = PendingActionEntity(
            reminderId = 2L,
            actionType = ActionType.UPDATE,
            createdAt = LocalDateTime.now(),
            retryCount = 5 // 재시도 한도 초과
        )
        pendingActionDao.insertPendingAction(action1)
        pendingActionDao.insertPendingAction(action2)

        // When
        val actions = pendingActionDao.getPendingActionsForRetry(maxRetryCount = 3)

        // Then
        assertEquals("Should have 1 action for retry", 1, actions.size)
        assertEquals("Should be action with low retry count", 1, actions[0].retryCount)
    }

    @Test
    fun `대기 작업의 필드를 업데이트한다`() = runTest {
        // Given
        val action = PendingActionEntity(
            reminderId = 1L,
            actionType = ActionType.INSERT,
            createdAt = LocalDateTime.now(),
            retryCount = 0
        )
        val id = pendingActionDao.insertPendingAction(action)

        // When
        val updatedAction = action.copy(
            id = id,
            retryCount = 1,
            lastRetryAt = LocalDateTime.now(),
            errorMessage = "Network error"
        )
        pendingActionDao.updatePendingAction(updatedAction)

        // Then
        val actions = pendingActionDao.getAllPendingActions().first()
        assertEquals("Retry count should be updated", 1, actions[0].retryCount)
        assertEquals("Error message should be set", "Network error", actions[0].errorMessage)
        assertNotNull("Last retry time should be set", actions[0].lastRetryAt)
    }

    @Test
    fun `대기 작업을 삭제한다`() = runTest {
        // Given
        val action = PendingActionEntity(
            reminderId = 1L,
            actionType = ActionType.INSERT,
            createdAt = LocalDateTime.now()
        )
        val id = pendingActionDao.insertPendingAction(action)

        // When
        pendingActionDao.deletePendingAction(action.copy(id = id))

        // Then
        val actions = pendingActionDao.getAllPendingActions().first()
        assertEquals("Should have no actions", 0, actions.size)
    }

    @Test
    fun `모든 대기 작업을 삭제한다`() = runTest {
        // Given
        pendingActionDao.insertPendingAction(
            PendingActionEntity(reminderId = 1L, actionType = ActionType.INSERT, createdAt = LocalDateTime.now())
        )
        pendingActionDao.insertPendingAction(
            PendingActionEntity(reminderId = 2L, actionType = ActionType.UPDATE, createdAt = LocalDateTime.now())
        )

        // When
        pendingActionDao.deleteAllPendingActions()

        // Then
        val actions = pendingActionDao.getAllPendingActions().first()
        assertEquals("Should have no actions", 0, actions.size)
    }

    @Test
    fun `특정 리마인더의 대기 작업을 삭제한다`() = runTest {
        // Given
        pendingActionDao.insertPendingAction(
            PendingActionEntity(reminderId = 1L, actionType = ActionType.INSERT, createdAt = LocalDateTime.now())
        )
        pendingActionDao.insertPendingAction(
            PendingActionEntity(reminderId = 2L, actionType = ActionType.UPDATE, createdAt = LocalDateTime.now())
        )
        pendingActionDao.insertPendingAction(
            PendingActionEntity(reminderId = 1L, actionType = ActionType.DELETE, createdAt = LocalDateTime.now())
        )

        // When
        pendingActionDao.deletePendingActionsByReminderId(1L)

        // Then
        val actions = pendingActionDao.getAllPendingActions().first()
        assertEquals("Should have 1 action", 1, actions.size)
        assertEquals("Remaining action should be for reminder 2", 2L, actions[0].reminderId)
    }

    @Test
    fun `대기 작업 수를 정확하게 반환한다`() = runTest {
        // Given
        pendingActionDao.insertPendingAction(
            PendingActionEntity(reminderId = 1L, actionType = ActionType.INSERT, createdAt = LocalDateTime.now())
        )
        pendingActionDao.insertPendingAction(
            PendingActionEntity(reminderId = 2L, actionType = ActionType.UPDATE, createdAt = LocalDateTime.now())
        )

        // When
        val count = pendingActionDao.getPendingActionsCount().first()

        // Then
        assertEquals("Count should be 2", 2, count)
    }

    @Test
    fun `작업 타입이 올바르게 저장된다`() = runTest {
        // Given
        val insertAction = PendingActionEntity(
            reminderId = 1L,
            actionType = ActionType.INSERT,
            createdAt = LocalDateTime.now()
        )
        val updateAction = PendingActionEntity(
            reminderId = 2L,
            actionType = ActionType.UPDATE,
            createdAt = LocalDateTime.now()
        )
        val deleteAction = PendingActionEntity(
            reminderId = 3L,
            actionType = ActionType.DELETE,
            createdAt = LocalDateTime.now()
        )

        // When
        pendingActionDao.insertPendingAction(insertAction)
        pendingActionDao.insertPendingAction(updateAction)
        pendingActionDao.insertPendingAction(deleteAction)

        // Then
        val actions = pendingActionDao.getAllPendingActions().first()
        assertEquals("Should have INSERT type", ActionType.INSERT, actions[0].actionType)
        assertEquals("Should have UPDATE type", ActionType.UPDATE, actions[1].actionType)
        assertEquals("Should have DELETE type", ActionType.DELETE, actions[2].actionType)
    }
}
