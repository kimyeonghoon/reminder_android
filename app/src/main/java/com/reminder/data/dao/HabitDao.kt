package com.reminder.data.dao

import androidx.room.*
import com.reminder.data.entity.HabitCompletion
import com.reminder.data.entity.HabitEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * v1.44.0: Habit DAO
 *
 * 습관 추적 데이터 접근 객체
 */
@Dao
interface HabitDao {

    // === Habit CRUD ===

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity): Long

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Query("DELETE FROM habits WHERE id = :habitId")
    suspend fun deleteHabit(habitId: Long)

    @Query("SELECT * FROM habits WHERE id = :habitId")
    suspend fun getHabitById(habitId: Long): HabitEntity?

    @Query("SELECT * FROM habits WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getAllHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits ORDER BY createdAt DESC")
    fun getAllHabitsIncludingInactive(): Flow<List<HabitEntity>>

    // === Habit Completion CRUD ===

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletion(completion: HabitCompletion)

    @Query("DELETE FROM habit_completions WHERE habitId = :habitId AND completedDate = :date")
    suspend fun deleteCompletion(habitId: Long, date: LocalDate)

    @Query("DELETE FROM habit_completions WHERE habitId = :habitId")
    suspend fun deleteAllCompletionsForHabit(habitId: Long)

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId AND completedDate = :date")
    suspend fun getCompletion(habitId: Long, date: LocalDate): HabitCompletion?

    // === Streak Calculation ===

    @Query("""
        SELECT completedDate FROM habit_completions
        WHERE habitId = :habitId
        ORDER BY completedDate DESC
    """)
    suspend fun getCompletionDates(habitId: Long): List<LocalDate>

    // === Statistics ===

    @Query("""
        SELECT COUNT(*) FROM habit_completions
        WHERE habitId = :habitId
        AND completedDate BETWEEN :startDate AND :endDate
    """)
    suspend fun getCompletionCountInPeriod(
        habitId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): Int

    @Query("""
        SELECT COUNT(*) FROM habit_completions
        WHERE habitId = :habitId
    """)
    suspend fun getTotalCompletionCount(habitId: Long): Int

    @Query("""
        SELECT completedDate FROM habit_completions
        WHERE habitId = :habitId
        AND completedDate BETWEEN :startDate AND :endDate
        ORDER BY completedDate ASC
    """)
    suspend fun getCompletionsInPeriod(
        habitId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<LocalDate>
}
