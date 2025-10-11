package com.reminder.domain.focus

import com.reminder.data.entity.FocusSessionEntity
import com.reminder.data.entity.FocusType
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDateTime

/**
 * v1.51.0: 포커스 세션 도메인 로직 테스트 (TDD)
 */
class FocusSessionTest {

    /**
     * 세션 시작 테스트
     */
    @Test
    fun `포커스 세션을 시작할 수 있다`() {
        // Given
        val targetMinutes = 25
        val focusType = FocusType.DEEP_WORK

        // When
        val session = FocusSessionEntity(
            focusType = focusType,
            targetDurationMinutes = targetMinutes
        )

        // Then
        assertNotNull(session.startTime)
        assertNull(session.endTime)
        assertEquals(targetMinutes, session.targetDurationMinutes)
        assertFalse(session.isCompleted)
        assertFalse(session.isInterrupted)
    }

    /**
     * 세션이 진행 중인지 확인
     */
    @Test
    fun `진행 중인 세션을 확인할 수 있다`() {
        // Given
        val session = FocusSessionEntity(
            targetDurationMinutes = 25
        )

        // When & Then
        assertTrue(session.isActive())
    }

    @Test
    fun `완료된 세션은 진행 중이 아니다`() {
        // Given
        val session = FocusSessionEntity(
            targetDurationMinutes = 25,
            endTime = LocalDateTime.now(),
            isCompleted = true
        )

        // When & Then
        assertFalse(session.isActive())
    }

    /**
     * 세션 완료
     */
    @Test
    fun `포커스 세션을 완료할 수 있다`() {
        // Given
        val startTime = LocalDateTime.now().minusMinutes(25)
        val session = FocusSessionEntity(
            startTime = startTime,
            targetDurationMinutes = 25
        )

        // When
        val completed = session.complete()

        // Then
        assertTrue(completed.isCompleted)
        assertNotNull(completed.endTime)
        assertEquals(25, completed.actualDurationMinutes)
        assertFalse(completed.isInterrupted)
    }

    /**
     * 세션 중단
     */
    @Test
    fun `포커스 세션을 중단할 수 있다`() {
        // Given
        val startTime = LocalDateTime.now().minusMinutes(10)
        val session = FocusSessionEntity(
            startTime = startTime,
            targetDurationMinutes = 25
        )

        // When
        val interrupted = session.interrupt()

        // Then
        assertTrue(interrupted.isInterrupted)
        assertFalse(interrupted.isCompleted)
        assertNotNull(interrupted.endTime)
        assertEquals(10, interrupted.actualDurationMinutes)
    }

    /**
     * 남은 시간 계산
     */
    @Test
    fun `남은 시간을 계산할 수 있다`() {
        // Given
        val startTime = LocalDateTime.now().minusMinutes(10)
        val session = FocusSessionEntity(
            startTime = startTime,
            targetDurationMinutes = 25
        )

        // When
        val remainingMinutes = session.getRemainingMinutes()

        // Then
        assertEquals(15, remainingMinutes)
    }

    @Test
    fun `목표 시간을 초과하면 남은 시간은 0`() {
        // Given
        val startTime = LocalDateTime.now().minusMinutes(30)
        val session = FocusSessionEntity(
            startTime = startTime,
            targetDurationMinutes = 25
        )

        // When
        val remainingMinutes = session.getRemainingMinutes()

        // Then
        assertEquals(0, remainingMinutes)
    }

    /**
     * 진행률 계산
     */
    @Test
    fun `진행률을 계산할 수 있다`() {
        // Given
        val startTime = LocalDateTime.now().minusMinutes(10)
        val session = FocusSessionEntity(
            startTime = startTime,
            targetDurationMinutes = 25
        )

        // When
        val progress = session.getProgress()

        // Then
        assertEquals(40, progress) // 10/25 * 100 = 40%
    }

    /**
     * 리마인더 연결
     */
    @Test
    fun `리마인더와 연결된 세션을 생성할 수 있다`() {
        // Given
        val reminderId = 123L

        // When
        val session = FocusSessionEntity(
            reminderId = reminderId,
            focusType = FocusType.DO_FIRST,
            targetDurationMinutes = 50
        )

        // Then
        assertEquals(reminderId, session.reminderId)
        assertEquals(FocusType.DO_FIRST, session.focusType)
    }

    /**
     * 세션 통계 계산
     */
    @Test
    fun `일별 집중 시간을 계산할 수 있다`() {
        // Given
        val today = LocalDateTime.now()
        val sessions = listOf(
            FocusSessionEntity(
                startTime = today.minusMinutes(25),
                endTime = today,
                actualDurationMinutes = 25,
                isCompleted = true
            ),
            FocusSessionEntity(
                startTime = today.minusMinutes(50),
                endTime = today.minusMinutes(25),
                actualDurationMinutes = 25,
                isCompleted = true
            ),
            // 미완료 세션 (제외)
            FocusSessionEntity(
                startTime = today.minusMinutes(10),
                actualDurationMinutes = 10,
                isCompleted = false
            )
        )

        // When
        val totalMinutes = sessions.calculateTotalFocusMinutes()

        // Then
        assertEquals(50, totalMinutes)
    }

    @Test
    fun `Streak를 계산할 수 있다`() {
        // Given
        val today = LocalDateTime.now()
        val sessions = listOf(
            // 오늘
            FocusSessionEntity(
                startTime = today,
                endTime = today.plusMinutes(25),
                isCompleted = true
            ),
            // 어제
            FocusSessionEntity(
                startTime = today.minusDays(1),
                endTime = today.minusDays(1).plusMinutes(25),
                isCompleted = true
            ),
            // 2일 전
            FocusSessionEntity(
                startTime = today.minusDays(2),
                endTime = today.minusDays(2).plusMinutes(25),
                isCompleted = true
            )
            // 3일 전 없음 (Streak 끊김)
        )

        // When
        val streak = sessions.calculateStreak()

        // Then
        assertEquals(3, streak)
    }

    @Test
    fun `연속 기록이 없으면 Streak는 0`() {
        // Given
        val today = LocalDateTime.now()
        val sessions = listOf(
            // 5일 전
            FocusSessionEntity(
                startTime = today.minusDays(5),
                endTime = today.minusDays(5).plusMinutes(25),
                isCompleted = true
            )
        )

        // When
        val streak = sessions.calculateStreak()

        // Then
        assertEquals(0, streak)
    }
}
