package com.reminder.habit

import com.reminder.data.dao.HabitDao
import com.reminder.data.entity.HabitCompletion
import com.reminder.data.entity.HabitEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * v1.44.0: Habit Manager
 *
 * 습관 추적 비즈니스 로직 관리
 */
class HabitManager(private val habitDao: HabitDao) {

    /**
     * 습관 생성
     */
    suspend fun createHabit(habit: HabitEntity): Long {
        return habitDao.insertHabit(habit)
    }

    /**
     * 습관 수정
     */
    suspend fun updateHabit(habit: HabitEntity) {
        habitDao.updateHabit(habit)
    }

    /**
     * 습관 삭제 (완료 기록도 함께 삭제)
     */
    suspend fun deleteHabit(habitId: Long) {
        habitDao.deleteAllCompletionsForHabit(habitId)
        habitDao.deleteHabit(habitId)
    }

    /**
     * 습관 완료 체크
     */
    suspend fun completeHabit(habitId: Long, date: LocalDate = LocalDate.now()) {
        val completion = HabitCompletion(habitId = habitId, completedDate = date)
        habitDao.insertCompletion(completion)
    }

    /**
     * 습관 완료 체크 해제
     */
    suspend fun uncompleteHabit(habitId: Long, date: LocalDate = LocalDate.now()) {
        habitDao.deleteCompletion(habitId, date)
    }

    /**
     * 오늘 습관 완료 여부 확인
     */
    suspend fun isHabitCompletedToday(habitId: Long, date: LocalDate = LocalDate.now()): Boolean {
        return habitDao.getCompletion(habitId, date) != null
    }

    /**
     * Streak (연속 달성 일수) 계산
     *
     * 오늘을 포함해서 연속으로 완료한 일수를 반환
     * 오늘 완료하지 않았으면 0 반환
     */
    suspend fun calculateStreak(habitId: Long): Int {
        val completionDates = habitDao.getCompletionDates(habitId)
        if (completionDates.isEmpty()) return 0

        val today = LocalDate.now()

        // 오늘 완료하지 않았으면 streak는 0
        if (!completionDates.contains(today)) return 0

        var streak = 0
        var currentDate = today

        // 어제부터 역순으로 확인하면서 연속 일수 계산
        for (date in completionDates) {
            if (date == currentDate) {
                streak++
                currentDate = currentDate.minusDays(1)
            } else if (date.isBefore(currentDate)) {
                // 날짜가 건너뛰었으면 연속 끊김
                break
            }
        }

        return streak
    }

    /**
     * 기간 내 완료율 계산
     *
     * @return 완료율 (0.0 ~ 100.0)
     */
    suspend fun getCompletionRate(
        habitId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): Double {
        val totalDays = ChronoUnit.DAYS.between(startDate, endDate).toInt() + 1
        if (totalDays <= 0) return 0.0

        val completedDays = habitDao.getCompletionCountInPeriod(habitId, startDate, endDate)

        return (completedDays.toDouble() / totalDays.toDouble()) * 100.0
    }

    /**
     * 모든 활성 습관 조회
     */
    fun getAllHabits(): Flow<List<HabitEntity>> {
        return habitDao.getAllHabits()
    }

    /**
     * 습관 ID로 조회
     */
    suspend fun getHabitById(habitId: Long): HabitEntity? {
        return habitDao.getHabitById(habitId)
    }

    /**
     * 기간 내 완료 날짜 목록 조회
     */
    suspend fun getCompletionsInPeriod(
        habitId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<LocalDate> {
        return habitDao.getCompletionsInPeriod(habitId, startDate, endDate)
    }

    /**
     * 전체 완료 횟수 조회
     */
    suspend fun getTotalCompletionCount(habitId: Long): Int {
        return habitDao.getTotalCompletionCount(habitId)
    }
}
