package com.reminder.viewmodel

import androidx.lifecycle.ViewModel
import com.reminder.data.entity.PomodoroSession
import com.reminder.data.entity.SessionType
import com.reminder.pomodoro.PomodoroManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * v1.46.0: Pomodoro ViewModel (TDD 재개발)
 *
 * 간단하고 테스트 가능한 구현
 */
class PomodoroViewModel(
    private val pomodoroManager: PomodoroManager,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main
) : ViewModel() {

    // CoroutineScope for testing
    private val scope = CoroutineScope(dispatcher + SupervisorJob())

    // 현재 상태
    private val _currentState = MutableStateFlow(PomodoroState.IDLE)
    val currentState: StateFlow<PomodoroState> = _currentState.asStateFlow()

    // 남은 시간 (초)
    private val _remainingSeconds = MutableStateFlow(0)
    val remainingSeconds: StateFlow<Int> = _remainingSeconds.asStateFlow()

    // 현재 세션 ID
    private var currentSessionId: Long? = null

    // 타이머 Job
    private var timerJob: Job? = null

    // UI가 필요로 하는 나머지 속성들
    val todaySessions = MutableStateFlow<List<PomodoroSession>>(emptyList()).asStateFlow()
    val todayCompletedSessions = MutableStateFlow(0).asStateFlow()
    val totalCompletedSessions = MutableStateFlow(0).asStateFlow()
    val streakDays = MutableStateFlow(0).asStateFlow()
    val totalFocusMinutes = MutableStateFlow(0).asStateFlow()
    val isLoading = MutableStateFlow(false).asStateFlow()
    val errorMessage = MutableStateFlow<String?>(null).asStateFlow()
    val successMessage = MutableStateFlow<String?>(null).asStateFlow()
    val isRunning = MutableStateFlow(false).asStateFlow()
    val currentSessionType = MutableStateFlow(SessionType.FOCUS).asStateFlow()

    /**
     * 집중 세션 시작
     */
    fun startFocusSession(reminderId: Long? = null) {
        scope.launch {
            val sessionId = pomodoroManager.startSession(reminderId, SessionType.FOCUS)
            currentSessionId = sessionId
            _currentState.value = PomodoroState.FOCUS
            _remainingSeconds.value = pomodoroManager.getFocusSessionDuration() * 60
            startTimer()
        }
    }

    /**
     * 짧은 휴식 시작
     */
    fun startShortBreak() {
        scope.launch {
            val sessionId = pomodoroManager.startSession(null, SessionType.SHORT_BREAK)
            currentSessionId = sessionId
            _currentState.value = PomodoroState.SHORT_BREAK
            _remainingSeconds.value = pomodoroManager.getShortBreakDuration() * 60
            startTimer()
        }
    }

    /**
     * 긴 휴식 시작
     */
    fun startLongBreak() {
        scope.launch {
            val sessionId = pomodoroManager.startSession(null, SessionType.LONG_BREAK)
            currentSessionId = sessionId
            _currentState.value = PomodoroState.LONG_BREAK
            _remainingSeconds.value = pomodoroManager.getLongBreakDuration() * 60
            startTimer()
        }
    }

    /**
     * 세션 완료
     */
    fun completeSession() {
        scope.launch {
            currentSessionId?.let { sessionId ->
                pomodoroManager.completeSession(sessionId)
                resetSession()
            }
        }
    }

    /**
     * 세션 취소
     */
    fun cancelSession() {
        scope.launch {
            currentSessionId?.let { sessionId ->
                pomodoroManager.cancelSession(sessionId)
                resetSession()
            }
        }
    }

    /**
     * 세션 리셋
     */
    private fun resetSession() {
        stopTimer()
        currentSessionId = null
        _currentState.value = PomodoroState.IDLE
        _remainingSeconds.value = 0
    }

    /**
     * 타이머 시작 (1초마다 카운트다운)
     */
    private fun startTimer() {
        stopTimer() // 기존 타이머 중지
        timerJob = scope.launch {
            while (_remainingSeconds.value > 0) {
                delay(1000) // 1초 대기
                _remainingSeconds.value -= 1

                // 시간이 0이 되면 자동 완료
                if (_remainingSeconds.value <= 0) {
                    completeSession()
                }
            }
        }
    }

    /**
     * 타이머 중지
     */
    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    // 나머지 빈 메서드들
    fun startSession() {}
    fun togglePause() {}
    fun stopSession() {}
    fun clearErrorMessage() {}
    fun clearMessages() {}

    override fun onCleared() {
        super.onCleared()
        scope.cancel()
    }
}

/**
 * Pomodoro 상태
 */
enum class PomodoroState {
    IDLE,
    FOCUS,
    SHORT_BREAK,
    LONG_BREAK
}
