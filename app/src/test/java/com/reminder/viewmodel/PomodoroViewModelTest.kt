package com.reminder.viewmodel

import com.reminder.data.dao.PomodoroSessionDao
import com.reminder.data.entity.PomodoroSession
import com.reminder.data.entity.SessionType
import com.reminder.pomodoro.PomodoroManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import java.time.LocalDate

/**
 * v1.46.0: PomodoroViewModel 완전 재개발 (TDD)
 *
 * 간단한 테스트들 - Fake 구현체 사용
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PomodoroViewModelTest {

    private lateinit var pomodoroManager: FakePomodoroManager
    private lateinit var viewModel: PomodoroViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        pomodoroManager = FakePomodoroManager()
        viewModel = PomodoroViewModel(pomodoroManager, testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /** 초기 상태는 IDLE이다 */
    @Test
    fun initialState_shouldBeIdle() {
        assertEquals(PomodoroState.IDLE, viewModel.currentState.value)
    }

    /** 집중 세션을 시작하면 상태가 FOCUS로 변경된다 */
    @Test
    fun startFocusSession_shouldChangeStateToFocus() = runTest(testDispatcher) {
        // When
        viewModel.startFocusSession()
        testDispatcher.scheduler.runCurrent() // Only run immediate tasks, not delays

        // Then
        assertEquals(PomodoroState.FOCUS, viewModel.currentState.value)
        assertEquals(25 * 60, viewModel.remainingSeconds.value)
    }

    /** 짧은 휴식을 시작하면 상태가 SHORT_BREAK로 변경된다 */
    @Test
    fun startShortBreak_shouldChangeStateToShortBreak() = runTest(testDispatcher) {
        // When
        viewModel.startShortBreak()
        testDispatcher.scheduler.runCurrent() // Only run immediate tasks, not delays

        // Then
        assertEquals(PomodoroState.SHORT_BREAK, viewModel.currentState.value)
        assertEquals(5 * 60, viewModel.remainingSeconds.value)
    }

    /** 긴 휴식을 시작하면 상태가 LONG_BREAK로 변경된다 */
    @Test
    fun startLongBreak_shouldChangeStateToLongBreak() = runTest(testDispatcher) {
        // When
        viewModel.startLongBreak()
        testDispatcher.scheduler.runCurrent() // Only run immediate tasks, not delays

        // Then
        assertEquals(PomodoroState.LONG_BREAK, viewModel.currentState.value)
        assertEquals(15 * 60, viewModel.remainingSeconds.value)
    }

    /** 세션을 완료하면 상태가 IDLE로 돌아간다 */
    @Test
    fun completeSession_shouldReturnToIdle() = runTest(testDispatcher) {
        // Given
        viewModel.startFocusSession()
        testDispatcher.scheduler.runCurrent() // Only run immediate tasks, not delays

        // When
        viewModel.completeSession()
        testDispatcher.scheduler.runCurrent()

        // Then
        assertTrue(pomodoroManager.completedSessions.contains(1L))
        assertEquals(PomodoroState.IDLE, viewModel.currentState.value)
    }

    /** 세션을 취소하면 상태가 IDLE로 돌아간다 */
    @Test
    fun cancelSession_shouldReturnToIdle() = runTest(testDispatcher) {
        // Given
        viewModel.startFocusSession()
        testDispatcher.scheduler.runCurrent() // Only run immediate tasks, not delays

        // When
        viewModel.cancelSession()
        testDispatcher.scheduler.runCurrent()

        // Then
        assertTrue(pomodoroManager.cancelledSessions.contains(1L))
        assertEquals(PomodoroState.IDLE, viewModel.currentState.value)
    }

    /** 타이머가 1초마다 카운트다운 된다 */
    @Test
    fun timer_shouldCountdownEverySecond() = runTest {
        // Given
        viewModel.startFocusSession()
        testScheduler.runCurrent() // Start the timer
        val initialSeconds = viewModel.remainingSeconds.value

        // When - Advance time by 3 seconds
        testScheduler.advanceTimeBy(3000)
        testScheduler.runCurrent() // Process the delays

        // Then - Should have decremented by 3 seconds
        assertEquals(initialSeconds - 3, viewModel.remainingSeconds.value)
    }

    /** 타이머가 0에 도달하면 세션이 자동 완료된다 */
    @Test
    fun timer_whenReachesZero_shouldAutoCompleteSession() = runTest {
        // Given
        viewModel.startFocusSession()
        testScheduler.runCurrent()

        // When - Advance time past the entire duration
        val totalTime = (25 * 60 + 1) * 1000L
        testScheduler.advanceTimeBy(totalTime)
        testScheduler.runCurrent()

        // Then - Session should be completed
        assertEquals(PomodoroState.IDLE, viewModel.currentState.value)
        assertEquals(0, viewModel.remainingSeconds.value)
        assertTrue(pomodoroManager.completedSessions.contains(1L))
    }
}

/**
 * 테스트용 Fake PomodoroManager
 * Mockito의 suspend 함수 모킹 문제를 우회하기 위한 간단한 구현체
 */
class FakePomodoroManager : PomodoroManager(mock<PomodoroSessionDao>()) {
    private var nextSessionId = 1L
    val completedSessions = mutableListOf<Long>()
    val cancelledSessions = mutableListOf<Long>()

    override suspend fun startSession(reminderId: Long?, sessionType: SessionType): Long {
        return nextSessionId++
    }

    override suspend fun completeSession(sessionId: Long) {
        completedSessions.add(sessionId)
    }

    override suspend fun cancelSession(sessionId: Long) {
        cancelledSessions.add(sessionId)
    }

    override fun getFocusSessionDuration(): Int = 25
    override fun getShortBreakDuration(): Int = 5
    override fun getLongBreakDuration(): Int = 15

    // 사용하지 않는 메서드들 - 기본 구현
    override fun getAllSessions(): Flow<List<PomodoroSession>> = flowOf(emptyList())
    override fun getTodaySessions(): Flow<List<PomodoroSession>> = flowOf(emptyList())
    override suspend fun getTodayCompletedSessions(): Int = 0
    override suspend fun getTotalCompletedSessions(): Int = 0
    override suspend fun getStreakDays(): Int = 0
    override suspend fun getTotalFocusMinutes(): Int = 0
    override fun getSessionsForReminder(reminderId: Long): Flow<List<PomodoroSession>> = flowOf(emptyList())
    override fun getSessionsByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<PomodoroSession>> = flowOf(emptyList())
}
