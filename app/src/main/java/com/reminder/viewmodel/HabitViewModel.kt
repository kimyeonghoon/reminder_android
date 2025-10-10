package com.reminder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reminder.data.entity.HabitEntity
import com.reminder.habit.HabitManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * v1.44.0: Habit ViewModel
 *
 * 습관 추적 화면의 상태 관리
 */
class HabitViewModel(
    private val habitManager: HabitManager
) : ViewModel() {

    // 모든 습관 목록
    val allHabits: StateFlow<List<HabitEntity>> = habitManager.getAllHabits()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // 각 습관의 완료 상태 (habitId -> isCompleted)
    private val _habitCompletions = MutableStateFlow<Map<Long, Boolean>>(emptyMap())
    val habitCompletions: StateFlow<Map<Long, Boolean>> = _habitCompletions.asStateFlow()

    // 각 습관의 Streak (habitId -> streak count)
    private val _habitStreaks = MutableStateFlow<Map<Long, Int>>(emptyMap())
    val habitStreaks: StateFlow<Map<Long, Int>> = _habitStreaks.asStateFlow()

    // 로딩 상태
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // 에러 메시지
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // 성공 메시지
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    init {
        // 습관 목록이 변경될 때마다 완료 상태와 Streak 업데이트
        viewModelScope.launch {
            allHabits.collect { habits ->
                refreshHabitData(habits)
            }
        }
    }

    /**
     * 습관 데이터 새로고침 (완료 상태 및 Streak)
     */
    private suspend fun refreshHabitData(habits: List<HabitEntity>) {
        val completions = mutableMapOf<Long, Boolean>()
        val streaks = mutableMapOf<Long, Int>()
        val today = LocalDate.now()

        habits.forEach { habit ->
            completions[habit.id] = habitManager.isHabitCompletedToday(habit.id, today)
            streaks[habit.id] = habitManager.calculateStreak(habit.id)
        }

        _habitCompletions.value = completions
        _habitStreaks.value = streaks
    }

    /**
     * 습관 생성
     */
    fun createHabit(name: String, description: String = "", frequency: Int = 7) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val habit = HabitEntity(
                    name = name,
                    description = description,
                    frequency = frequency,
                    isActive = true,
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                )
                habitManager.createHabit(habit)
                _successMessage.value = "습관이 생성되었습니다"
            } catch (e: Exception) {
                _errorMessage.value = "습관 생성 실패: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 습관 완료 토글
     */
    fun toggleHabitCompletion(habitId: Long) {
        viewModelScope.launch {
            try {
                val today = LocalDate.now()
                val isCurrentlyCompleted = habitManager.isHabitCompletedToday(habitId, today)

                if (isCurrentlyCompleted) {
                    habitManager.uncompleteHabit(habitId, today)
                } else {
                    habitManager.completeHabit(habitId, today)
                }

                // 즉시 UI 업데이트
                refreshHabitData(allHabits.value)
            } catch (e: Exception) {
                _errorMessage.value = "완료 상태 변경 실패: ${e.message}"
            }
        }
    }

    /**
     * 습관 삭제
     */
    fun deleteHabit(habitId: Long) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                habitManager.deleteHabit(habitId)
                _successMessage.value = "습관이 삭제되었습니다"
            } catch (e: Exception) {
                _errorMessage.value = "습관 삭제 실패: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 습관 수정
     */
    fun updateHabit(habit: HabitEntity) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                habitManager.updateHabit(habit.copy(updatedAt = LocalDateTime.now()))
                _successMessage.value = "습관이 수정되었습니다"
            } catch (e: Exception) {
                _errorMessage.value = "습관 수정 실패: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 특정 기간의 완료율 조회
     */
    suspend fun getCompletionRate(habitId: Long, startDate: LocalDate, endDate: LocalDate): Double {
        return habitManager.getCompletionRate(habitId, startDate, endDate)
    }

    /**
     * 메시지 초기화
     */
    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
}
