package com.reminder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reminder.data.entity.PomodoroSession
import com.reminder.data.entity.SessionType
import com.reminder.pomodoro.PomodoroManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * v1.45.0: 포모도로 타이머 ViewModel
 *
 * 포모도로 타이머 화면 상태 관리
 */
class PomodoroViewModel(
    private val pomodoroManager: PomodoroManager
) : ViewModel() {

    // 현재 세션 ID
    private val _currentSessionId = MutableStateFlow<Long?>(null)
    val currentSessionId: StateFlow<Long?> = _currentSessionId.asStateFlow()

    // 현재 세션 타입
    private val _currentSessionType = MutableStateFlow(SessionType.FOCUS)
    val currentSessionType: StateFlow<SessionType> = _currentSessionType.asStateFlow()

    // 타이머 남은 시간 (초 단위)
    private val _remainingSeconds = MutableStateFlow(0)
    val remainingSeconds: StateFlow<Int> = _remainingSeconds.asStateFlow()

    // 타이머 실행 중 여부
    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    // 오늘 완료한 세션 개수
    private val _todayCompletedSessions = MutableStateFlow(0)
    val todayCompletedSessions: StateFlow<Int> = _todayCompletedSessions.asStateFlow()

    // 전체 집중 시간 (분)
    private val _totalFocusMinutes = MutableStateFlow(0)
    val totalFocusMinutes: StateFlow<Int> = _totalFocusMinutes.asStateFlow()

    // 연속 완료 일수 (Streak)
    private val _streakDays = MutableStateFlow(0)
    val streakDays: StateFlow<Int> = _streakDays.asStateFlow()

    // 오늘의 모든 세션
    val todaySessions = pomodoroManager.getTodaySessions()

    // 에러 메시지
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // 타이머 Job
    private var timerJob: Job? = null

    init {
        loadStatistics()
    }

    /**
     * 통계 로드
     */
    private fun loadStatistics() {
        viewModelScope.launch {
            try {
                _todayCompletedSessions.value = pomodoroManager.getTodayCompletedSessions()
                _totalFocusMinutes.value = pomodoroManager.getTotalFocusMinutes()
                _streakDays.value = pomodoroManager.getStreakDays()
            } catch (e: Exception) {
                _errorMessage.value = "통계 로드 실패: ${e.message}"
            }
        }
    }

    /**
     * 세션 시작
     *
     * @param sessionType 세션 타입
     * @param reminderId 연결된 리마인더 ID (선택사항)
     */
    fun startSession(sessionType: SessionType = SessionType.FOCUS, reminderId: Long? = null) {
        viewModelScope.launch {
            try {
                // 이미 실행 중이면 중단
                if (_isRunning.value) {
                    stopSession()
                }

                // 세션 시작
                val sessionId = pomodoroManager.startSession(reminderId, sessionType)
                _currentSessionId.value = sessionId
                _currentSessionType.value = sessionType

                // 타이머 시작
                val durationMinutes = when (sessionType) {
                    SessionType.FOCUS -> pomodoroManager.getFocusSessionDuration()
                    SessionType.SHORT_BREAK -> pomodoroManager.getShortBreakDuration()
                    SessionType.LONG_BREAK -> pomodoroManager.getLongBreakDuration()
                }
                _remainingSeconds.value = durationMinutes * 60
                _isRunning.value = true

                startTimer()
            } catch (e: Exception) {
                _errorMessage.value = "세션 시작 실패: ${e.message}"
            }
        }
    }

    /**
     * 타이머 시작
     */
    private fun startTimer() {
        timerJob = viewModelScope.launch {
            while (_remainingSeconds.value > 0 && _isRunning.value) {
                delay(1000) // 1초 대기
                _remainingSeconds.value -= 1
            }

            // 타이머 종료
            if (_remainingSeconds.value == 0) {
                completeSession()
            }
        }
    }

    /**
     * 세션 일시정지/재개
     */
    fun togglePause() {
        if (_isRunning.value) {
            pauseSession()
        } else {
            resumeSession()
        }
    }

    /**
     * 세션 일시정지
     */
    private fun pauseSession() {
        _isRunning.value = false
        timerJob?.cancel()
    }

    /**
     * 세션 재개
     */
    private fun resumeSession() {
        if (_remainingSeconds.value > 0) {
            _isRunning.value = true
            startTimer()
        }
    }

    /**
     * 세션 중지 (취소)
     */
    fun stopSession() {
        viewModelScope.launch {
            try {
                _currentSessionId.value?.let { sessionId ->
                    pomodoroManager.cancelSession(sessionId)
                }

                resetSession()
            } catch (e: Exception) {
                _errorMessage.value = "세션 중지 실패: ${e.message}"
            }
        }
    }

    /**
     * 세션 완료
     */
    private fun completeSession() {
        viewModelScope.launch {
            try {
                _currentSessionId.value?.let { sessionId ->
                    pomodoroManager.completeSession(sessionId)
                }

                resetSession()
                loadStatistics() // 통계 갱신
            } catch (e: Exception) {
                _errorMessage.value = "세션 완료 실패: ${e.message}"
            }
        }
    }

    /**
     * 세션 리셋
     */
    private fun resetSession() {
        _isRunning.value = false
        _remainingSeconds.value = 0
        _currentSessionId.value = null
        timerJob?.cancel()
    }

    /**
     * 에러 메시지 클리어
     */
    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
