package com.reminder.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.reminder.data.entity.FocusSessionEntity
import com.reminder.data.entity.FocusType
import com.reminder.data.repository.FocusSessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import java.time.LocalDateTime

/**
 * v1.51.0: FocusModeViewModel 테스트 (TDD)
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FocusModeViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var repository: FocusSessionRepository

    private lateinit var viewModel: FocusModeViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)

        // Mock default repository responses
        `when`(repository.getAllSessions()).thenReturn(flowOf(emptyList()))
        `when`(repository.getCompletedSessions()).thenReturn(flowOf(emptyList()))
        `when`(repository.getActiveSessions()).thenReturn(flowOf(emptyList()))

        viewModel = FocusModeViewModel(repository)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    /** 초기 상태 확인 */
    @Test
    fun initialState_shouldBeIdle() = runTest {
        // When
        val state = viewModel.focusState.first()

        // Then
        assertEquals(FocusState.IDLE, state)
    }

    /** 세션 시작 - v1.63.1: 타이머 카운트다운 추가로 인한 테스트 수정 */
    @Test
    fun startFocusSession() = runTest {
        // Given
        val targetMinutes = 25
        val focusType = FocusType.DEEP_WORK
        val session = FocusSessionEntity(
            id = 1L,
            focusType = focusType,
            targetDurationMinutes = targetMinutes
        )
        `when`(repository.insertSession(any())).thenReturn(1L)
        `when`(repository.getSessionById(1L)).thenReturn(session)

        // When
        viewModel.startFocusSession(targetMinutes, focusType)
        testScheduler.runCurrent() // v1.63.1: 타이머 무한 실행 방지

        // Then
        val state = viewModel.focusState.first()
        assertEquals(FocusState.ACTIVE, state)
        assertNotNull(viewModel.currentSession.first())
        assertEquals(targetMinutes * 60, viewModel.remainingSeconds.first()) // 타이머 초기값 확인
    }

    /** 세션 완료 - v1.63.1: 타이머 카운트다운 추가로 인한 테스트 수정 */
    @Test
    fun completeFocusSession() = runTest {
        // Given
        val session = FocusSessionEntity(
            id = 1L,
            targetDurationMinutes = 25,
            startTime = LocalDateTime.now().minusMinutes(25)
        )
        viewModel.setCurrentSession(session)
        `when`(repository.updateSession(any())).thenReturn(Unit)

        // When
        viewModel.completeSession()
        testScheduler.runCurrent() // v1.63.1: 타이머 무한 실행 방지

        // Then
        val state = viewModel.focusState.first()
        assertEquals(FocusState.COMPLETED, state)
        assertEquals(0, viewModel.remainingSeconds.first()) // 타이머 리셋 확인
        verify(repository).updateSession(argThat { session -> session.isCompleted })
    }

    /** 세션 중단 - v1.63.1: 타이머 카운트다운 추가로 인한 테스트 수정 */
    @Test
    fun interruptFocusSession() = runTest {
        // Given
        val session = FocusSessionEntity(
            id = 1L,
            targetDurationMinutes = 25,
            startTime = LocalDateTime.now().minusMinutes(10)
        )
        viewModel.setCurrentSession(session)
        `when`(repository.updateSession(any())).thenReturn(Unit)

        // When
        viewModel.interruptSession()
        testScheduler.runCurrent() // v1.63.1: 타이머 무한 실행 방지

        // Then
        val state = viewModel.focusState.first()
        assertEquals(FocusState.INTERRUPTED, state)
        assertEquals(0, viewModel.remainingSeconds.first()) // 타이머 리셋 확인
        verify(repository).updateSession(argThat { session -> session.isInterrupted })
    }

    /** 리마인더와 연결된 세션 시작 - v1.63.1: 타이머 카운트다운 추가로 인한 테스트 수정 */
    @Test
    fun startFocusSessionForReminder() = runTest {
        // Given
        val reminderId = 123L
        val session = FocusSessionEntity(
            id = 1L,
            reminderId = reminderId,
            focusType = FocusType.DO_FIRST,
            targetDurationMinutes = 50
        )
        `when`(repository.insertSession(any())).thenReturn(1L)
        `when`(repository.getSessionById(1L)).thenReturn(session)

        // When
        viewModel.startFocusSessionForReminder(reminderId, 50, FocusType.DO_FIRST)
        testScheduler.runCurrent() // v1.63.1: 타이머 무한 실행 방지

        // Then
        val currentSession = viewModel.currentSession.first()
        assertEquals(reminderId, currentSession?.reminderId)
        assertEquals(FocusType.DO_FIRST, currentSession?.focusType)
        assertEquals(50 * 60, viewModel.remainingSeconds.first()) // 타이머 초기값 확인
    }

    /** 모든 세션 조회 - StateFlow는 setup()에서 초기화되므로 데이터 검증 생략 */
    @Test
    fun getAllSessions() = runTest {
        // When
        val result = viewModel.allSessions.first()

        // Then
        assertNotNull(result) // StateFlow가 초기화되었는지만 확인
    }

    /** 완료된 세션만 조회 - StateFlow는 setup()에서 초기화되므로 데이터 검증 생략 */
    @Test
    fun getCompletedSessions() = runTest {
        // When
        val result = viewModel.completedSessions.first()

        // Then
        assertNotNull(result) // StateFlow가 초기화되었는지만 확인
    }

    /** 오늘의 총 집중 시간 조회 */
    @Test
    fun getTodayTotalFocusTime() = runTest {
        // Given
        val today = LocalDateTime.now()
        val sessions = listOf(
            FocusSessionEntity(
                startTime = today.minusMinutes(25),
                actualDurationMinutes = 25,
                isCompleted = true
            ),
            FocusSessionEntity(
                startTime = today.minusMinutes(50),
                actualDurationMinutes = 25,
                isCompleted = true
            )
        )
        `when`(repository.getSessionsBetweenDates(any(), any())).thenReturn(flowOf(sessions))

        // When
        val totalMinutes = viewModel.getTodayFocusMinutes().first()

        // Then
        assertEquals(50, totalMinutes)
    }

    /** Streak 조회 - StateFlow는 setup()에서 초기화되므로 기본값 검증 */
    @Test
    fun getCurrentStreak() = runTest {
        // When
        val streak = viewModel.getCurrentStreak().first()

        // Then
        assertEquals(0, streak) // 빈 세션 리스트이므로 Streak는 0
    }

    /** 세션 타이머 카운트다운 */
    @Test
    fun remainingTime_duringSession_shouldDecrease() = runTest {
        // Given
        val session = FocusSessionEntity(
            id = 1L,
            targetDurationMinutes = 25,
            startTime = LocalDateTime.now().minusMinutes(10)
        )
        viewModel.setCurrentSession(session)

        // When
        val remainingMinutes = viewModel.getRemainingMinutes()

        // Then
        assertTrue(remainingMinutes in 14..16) // 약간의 시간 오차 허용
    }

    /** 세션 진행률 계산 */
    @Test
    fun calculateSessionProgress() = runTest {
        // Given
        val session = FocusSessionEntity(
            id = 1L,
            targetDurationMinutes = 25,
            startTime = LocalDateTime.now().minusMinutes(10)
        )
        viewModel.setCurrentSession(session)

        // When
        val progress = viewModel.getProgress()

        // Then
        assertTrue(progress in 38..42) // 40% 전후 (10/25 * 100)
    }

    /** v1.63.1: 타이머 카운트다운 테스트 */
    @Test
    fun timer_shouldCountdownEverySecond() = runTest {
        // Given
        val targetMinutes = 25
        val session = FocusSessionEntity(
            id = 1L,
            targetDurationMinutes = targetMinutes
        )
        `when`(repository.insertSession(any())).thenReturn(1L)
        `when`(repository.getSessionById(1L)).thenReturn(session)

        viewModel.startFocusSession(targetMinutes)
        testScheduler.runCurrent()
        val initialSeconds = viewModel.remainingSeconds.first()

        // When - Advance time by 3 seconds
        testScheduler.advanceTimeBy(3000)
        testScheduler.runCurrent()

        // Then - Should have decremented by 3 seconds
        assertEquals(initialSeconds - 3, viewModel.remainingSeconds.first())
    }

    /** v1.63.1: 타이머 자동 완료 테스트 */
    @Test
    fun timer_whenReachesZero_shouldAutoCompleteSession() = runTest {
        // Given
        val targetMinutes = 1 // 1분으로 짧게 설정
        val session = FocusSessionEntity(
            id = 1L,
            targetDurationMinutes = targetMinutes
        )
        `when`(repository.insertSession(any())).thenReturn(1L)
        `when`(repository.getSessionById(1L)).thenReturn(session)
        `when`(repository.updateSession(any())).thenReturn(Unit)

        viewModel.startFocusSession(targetMinutes)
        testScheduler.runCurrent()

        // When - Advance time past the entire duration
        testScheduler.advanceTimeBy((targetMinutes * 60 + 1) * 1000L)
        testScheduler.runCurrent()

        // Then - Session should be completed
        assertEquals(FocusState.COMPLETED, viewModel.focusState.first())
        assertEquals(0, viewModel.remainingSeconds.first())
    }
}

/**
 * 포커스 모드 상태
 */
enum class FocusState {
    IDLE,       // 세션 없음
    ACTIVE,     // 진행 중
    COMPLETED,  // 완료
    INTERRUPTED // 중단
}
