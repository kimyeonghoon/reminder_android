package com.reminder.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reminder.data.DndSettings
import com.reminder.data.entity.FocusSessionEntity
import com.reminder.data.entity.FocusType
import com.reminder.data.repository.DndRepository
import com.reminder.data.repository.FocusSessionRepository
import com.reminder.domain.focus.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime

/**
 * v1.51.0: 포커스 모드 ViewModel
 * v1.54.0: 방해 금지 모드 통합
 *
 * 포커스 세션 관리 및 타이머 기능
 */
class FocusModeViewModel(
    private val repository: FocusSessionRepository,
    private val dndRepository: DndRepository? = null // v1.54.0: DND 지원 (API 23+)
) : ViewModel() {

    // 현재 진행 중인 세션
    private val _currentSession = MutableStateFlow<FocusSessionEntity?>(null)
    val currentSession: StateFlow<FocusSessionEntity?> = _currentSession.asStateFlow()

    // 포커스 모드 상태
    private val _focusState = MutableStateFlow(FocusState.IDLE)
    val focusState: StateFlow<FocusState> = _focusState.asStateFlow()

    // 모든 세션 목록
    val allSessions: StateFlow<List<FocusSessionEntity>> = repository.getAllSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 완료된 세션 목록
    val completedSessions: StateFlow<List<FocusSessionEntity>> = repository.getCompletedSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 활성 세션 목록
    val activeSessions: StateFlow<List<FocusSessionEntity>> = repository.getActiveSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // v1.54.0: DND 설정
    val dndSettings: StateFlow<DndSettings> = dndRepository?.dndSettings
        ?: MutableStateFlow(DndSettings()).asStateFlow()

    /**
     * 포커스 세션 시작
     * v1.54.0: DND 자동 활성화
     */
    fun startFocusSession(
        targetMinutes: Int,
        focusType: FocusType = FocusType.DEEP_WORK,
        reminderId: Long? = null
    ) {
        viewModelScope.launch {
            val session = FocusSessionEntity(
                reminderId = reminderId,
                focusType = focusType,
                targetDurationMinutes = targetMinutes
            )

            val sessionId = repository.insertSession(session)
            val insertedSession = repository.getSessionById(sessionId)

            _currentSession.value = insertedSession
            _focusState.value = FocusState.ACTIVE

            // v1.54.0: DND 자동 활성화 (API 23+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                dndRepository?.enableDnd()
            }
        }
    }

    /**
     * 리마인더와 연결하여 세션 시작
     */
    fun startFocusSessionForReminder(
        reminderId: Long,
        targetMinutes: Int,
        focusType: FocusType = FocusType.DO_FIRST
    ) {
        startFocusSession(targetMinutes, focusType, reminderId)
    }

    /**
     * 세션 완료
     * v1.54.0: DND 자동 비활성화
     */
    fun completeSession() {
        viewModelScope.launch {
            _currentSession.value?.let { session ->
                val completedSession = session.complete()
                repository.updateSession(completedSession)

                _currentSession.value = completedSession
                _focusState.value = FocusState.COMPLETED

                // v1.54.0: DND 자동 비활성화 (API 23+)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    dndRepository?.disableDnd()
                }
            }
        }
    }

    /**
     * 세션 중단
     * v1.54.0: DND 자동 비활성화
     */
    fun interruptSession() {
        viewModelScope.launch {
            _currentSession.value?.let { session ->
                val interruptedSession = session.interrupt()
                repository.updateSession(interruptedSession)

                _currentSession.value = interruptedSession
                _focusState.value = FocusState.INTERRUPTED

                // v1.54.0: DND 자동 비활성화 (API 23+)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    dndRepository?.disableDnd()
                }
            }
        }
    }

    /**
     * 세션 초기화 (다음 세션 준비)
     */
    fun resetSession() {
        _currentSession.value = null
        _focusState.value = FocusState.IDLE
    }

    /**
     * 남은 시간 계산 (분)
     */
    fun getRemainingMinutes(): Int {
        return _currentSession.value?.getRemainingMinutes() ?: 0
    }

    /**
     * 진행률 계산 (0-100%)
     */
    fun getProgress(): Int {
        return _currentSession.value?.getProgress() ?: 0
    }

    /**
     * 오늘의 총 집중 시간 (Flow)
     */
    fun getTodayFocusMinutes(): Flow<Int> {
        val today = LocalDateTime.now()
        val startOfDay = today.toLocalDate().atStartOfDay()
        val endOfDay = today.toLocalDate().atTime(23, 59, 59)

        return repository.getSessionsBetweenDates(startOfDay, endOfDay)
            .map { sessions -> sessions.calculateTotalFocusMinutes() }
    }

    /**
     * 현재 Streak 계산 (Flow)
     */
    fun getCurrentStreak(): Flow<Int> {
        return allSessions.map { sessions -> sessions.calculateStreak() }
    }

    /**
     * 특정 리마인더의 세션 히스토리 조회
     */
    fun getSessionsForReminder(reminderId: Long): Flow<List<FocusSessionEntity>> {
        return repository.getSessionsByReminderId(reminderId)
    }

    /**
     * 세션 삭제
     */
    fun deleteSession(session: FocusSessionEntity) {
        viewModelScope.launch {
            repository.deleteSession(session)
        }
    }

    /**
     * 오래된 세션 정리 (90일 이상)
     */
    fun cleanupOldSessions() {
        viewModelScope.launch {
            val cutoffDate = LocalDateTime.now().minusDays(90)
            repository.deleteOldSessions(cutoffDate)
        }
    }

    // v1.54.0: DND 관련 함수들

    /**
     * DND 권한이 있는지 확인
     */
    @RequiresApi(Build.VERSION_CODES.M)
    fun hasDndPermission(): Boolean {
        return dndRepository?.hasPermission() ?: false
    }

    /**
     * DND 권한 요청 Intent 가져오기
     */
    fun getDndPermissionIntent() = dndRepository?.getPermissionIntent()

    /**
     * DND 설정 업데이트
     */
    fun updateDndSettings(settings: DndSettings) {
        dndRepository?.updateSettings(settings)
    }

    /**
     * 현재 DND가 활성화되어 있는지 확인
     */
    @RequiresApi(Build.VERSION_CODES.M)
    fun isDndEnabled(): Boolean {
        return dndRepository?.isEnabled() ?: false
    }

    // 테스트용: 현재 세션 설정
    internal fun setCurrentSession(session: FocusSessionEntity) {
        _currentSession.value = session
        _focusState.value = if (session.isActive()) FocusState.ACTIVE else FocusState.IDLE
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
