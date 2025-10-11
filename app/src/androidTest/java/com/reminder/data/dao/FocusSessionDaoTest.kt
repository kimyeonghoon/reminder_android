package com.reminder.data.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.reminder.data.database.ReminderDatabase
import com.reminder.data.entity.FocusSessionEntity
import com.reminder.data.entity.FocusType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime

/**
 * v1.51.0: FocusSessionDao 통합 테스트 (TDD)
 */
@RunWith(AndroidJUnit4::class)
class FocusSessionDaoTest {

    private lateinit var database: ReminderDatabase
    private lateinit var dao: FocusSessionDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, ReminderDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.focusSessionDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    /**
     * 삽입 및 조회
     */
    @Test
    fun insertSession_andRetrieve() = runTest {
        // Given
        val session = FocusSessionEntity(
            focusType = FocusType.DEEP_WORK,
            targetDurationMinutes = 25
        )

        // When
        val id = dao.insertSession(session)
        val retrieved = dao.getSessionById(id)

        // Then
        assertNotNull(retrieved)
        assertEquals(FocusType.DEEP_WORK, retrieved?.focusType)
        assertEquals(25, retrieved?.targetDurationMinutes)
    }

    /**
     * 모든 세션 조회 (Flow)
     */
    @Test
    fun getAllSessions_returnsFlowOfSessions() = runTest {
        // Given
        val session1 = FocusSessionEntity(focusType = FocusType.DEEP_WORK, targetDurationMinutes = 25)
        val session2 = FocusSessionEntity(focusType = FocusType.POMODORO, targetDurationMinutes = 50)
        dao.insertSession(session1)
        dao.insertSession(session2)

        // When
        val sessions = dao.getAllSessions().first()

        // Then
        assertEquals(2, sessions.size)
    }

    /**
     * 활성 세션 조회
     */
    @Test
    fun getActiveSessions_returnsOnlyActiveSessions() = runTest {
        // Given
        val activeSession = FocusSessionEntity(
            focusType = FocusType.DEEP_WORK,
            targetDurationMinutes = 25,
            endTime = null
        )
        val completedSession = FocusSessionEntity(
            focusType = FocusType.POMODORO,
            targetDurationMinutes = 25,
            endTime = LocalDateTime.now(),
            isCompleted = true
        )
        dao.insertSession(activeSession)
        dao.insertSession(completedSession)

        // When
        val activeSessions = dao.getActiveSessions().first()

        // Then
        assertEquals(1, activeSessions.size)
        assertNull(activeSessions[0].endTime)
    }

    /**
     * 리마인더 ID로 세션 조회
     */
    @Test
    fun getSessionsByReminderId_returnsMatchingSessions() = runTest {
        // Given
        val reminderId = 123L
        val session1 = FocusSessionEntity(reminderId = reminderId, targetDurationMinutes = 25)
        val session2 = FocusSessionEntity(reminderId = reminderId, targetDurationMinutes = 50)
        val session3 = FocusSessionEntity(reminderId = 999L, targetDurationMinutes = 30)
        dao.insertSession(session1)
        dao.insertSession(session2)
        dao.insertSession(session3)

        // When
        val sessions = dao.getSessionsByReminderId(reminderId).first()

        // Then
        assertEquals(2, sessions.size)
        assertTrue(sessions.all { it.reminderId == reminderId })
    }

    /**
     * 날짜 범위로 세션 조회
     */
    @Test
    fun getSessionsBetweenDates_returnsSessionsInRange() = runTest {
        // Given
        val today = LocalDateTime.now()
        val yesterday = today.minusDays(1)
        val twoDaysAgo = today.minusDays(2)

        val session1 = FocusSessionEntity(startTime = today, targetDurationMinutes = 25)
        val session2 = FocusSessionEntity(startTime = yesterday, targetDurationMinutes = 25)
        val session3 = FocusSessionEntity(startTime = twoDaysAgo, targetDurationMinutes = 25)
        dao.insertSession(session1)
        dao.insertSession(session2)
        dao.insertSession(session3)

        // When
        val sessions = dao.getSessionsBetweenDates(yesterday, today).first()

        // Then
        assertEquals(2, sessions.size)
        assertTrue(sessions.none { it.startTime.isBefore(yesterday) })
    }

    /**
     * 세션 업데이트
     */
    @Test
    fun updateSession_modifiesExistingSession() = runTest {
        // Given
        val session = FocusSessionEntity(targetDurationMinutes = 25)
        val id = dao.insertSession(session)
        val inserted = dao.getSessionById(id)!!

        // When
        val updated = inserted.copy(
            endTime = LocalDateTime.now(),
            actualDurationMinutes = 25,
            isCompleted = true
        )
        dao.updateSession(updated)

        // Then
        val retrieved = dao.getSessionById(id)
        assertTrue(retrieved?.isCompleted == true)
        assertNotNull(retrieved?.endTime)
        assertEquals(25, retrieved?.actualDurationMinutes)
    }

    /**
     * 세션 삭제
     */
    @Test
    fun deleteSession_removesSession() = runTest {
        // Given
        val session = FocusSessionEntity(targetDurationMinutes = 25)
        val id = dao.insertSession(session)

        // When
        val inserted = dao.getSessionById(id)!!
        dao.deleteSession(inserted)

        // Then
        val retrieved = dao.getSessionById(id)
        assertNull(retrieved)
    }

    /**
     * 완료된 세션만 조회
     */
    @Test
    fun getCompletedSessions_returnsOnlyCompletedSessions() = runTest {
        // Given
        val completed = FocusSessionEntity(
            targetDurationMinutes = 25,
            endTime = LocalDateTime.now(),
            isCompleted = true
        )
        val active = FocusSessionEntity(targetDurationMinutes = 25)
        val interrupted = FocusSessionEntity(
            targetDurationMinutes = 25,
            endTime = LocalDateTime.now(),
            isInterrupted = true
        )
        dao.insertSession(completed)
        dao.insertSession(active)
        dao.insertSession(interrupted)

        // When
        val sessions = dao.getCompletedSessions().first()

        // Then
        assertEquals(1, sessions.size)
        assertTrue(sessions[0].isCompleted)
    }

    /**
     * 총 집중 시간 계산
     */
    @Test
    fun getTotalFocusMinutes_sumsCompletedSessions() = runTest {
        // Given
        val session1 = FocusSessionEntity(
            targetDurationMinutes = 25,
            actualDurationMinutes = 25,
            endTime = LocalDateTime.now(),
            isCompleted = true
        )
        val session2 = FocusSessionEntity(
            targetDurationMinutes = 50,
            actualDurationMinutes = 50,
            endTime = LocalDateTime.now(),
            isCompleted = true
        )
        val session3 = FocusSessionEntity(
            targetDurationMinutes = 25,
            actualDurationMinutes = 10,
            endTime = LocalDateTime.now(),
            isInterrupted = true
        )
        dao.insertSession(session1)
        dao.insertSession(session2)
        dao.insertSession(session3)

        // When
        val total = dao.getTotalFocusMinutes()

        // Then
        assertEquals(75, total) // 25 + 50, interrupted는 제외
    }

    /**
     * 오래된 세션 삭제 (90일 이상)
     */
    @Test
    fun deleteOldSessions_removesSessionsOlderThan90Days() = runTest {
        // Given
        val recent = FocusSessionEntity(
            startTime = LocalDateTime.now().minusDays(30),
            targetDurationMinutes = 25
        )
        val old = FocusSessionEntity(
            startTime = LocalDateTime.now().minusDays(100),
            targetDurationMinutes = 25
        )
        dao.insertSession(recent)
        dao.insertSession(old)

        // When
        val cutoffDate = LocalDateTime.now().minusDays(90)
        dao.deleteOldSessions(cutoffDate)

        // Then
        val allSessions = dao.getAllSessions().first()
        assertEquals(1, allSessions.size)
        assertTrue(allSessions[0].startTime.isAfter(cutoffDate))
    }
}
