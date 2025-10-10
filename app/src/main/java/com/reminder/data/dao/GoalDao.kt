package com.reminder.data.dao

import androidx.room.*
import com.reminder.data.entity.GoalEntity
import com.reminder.data.entity.GoalType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * v1.33.0: 목표 데이터 접근 객체
 */
@Dao
interface GoalDao {

    /**
     * 모든 활성 목표 조회 (Flow)
     */
    @Query("SELECT * FROM goals WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getAllActiveGoals(): Flow<List<GoalEntity>>

    /**
     * 모든 목표 조회 (활성/비활성 포함)
     */
    @Query("SELECT * FROM goals ORDER BY createdAt DESC")
    fun getAllGoals(): Flow<List<GoalEntity>>

    /**
     * 특정 타입의 활성 목표 조회
     */
    @Query("SELECT * FROM goals WHERE type = :type AND isActive = 1 ORDER BY createdAt DESC")
    fun getGoalsByType(type: GoalType): Flow<List<GoalEntity>>

    /**
     * 특정 기간 내의 활성 목표 조회
     */
    @Query("""
        SELECT * FROM goals
        WHERE isActive = 1
        AND startDate <= :endDate
        AND endDate >= :startDate
        ORDER BY createdAt DESC
    """)
    fun getGoalsByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<GoalEntity>>

    /**
     * ID로 목표 조회
     */
    @Query("SELECT * FROM goals WHERE id = :goalId")
    suspend fun getGoalById(goalId: Long): GoalEntity?

    /**
     * 현재 진행 중인 일일 목표 조회
     */
    @Query("""
        SELECT * FROM goals
        WHERE type = 'DAILY'
        AND isActive = 1
        AND startDate <= :today
        AND endDate >= :today
    """)
    suspend fun getCurrentDailyGoals(today: LocalDate = LocalDate.now()): List<GoalEntity>

    /**
     * 현재 진행 중인 주간 목표 조회
     */
    @Query("""
        SELECT * FROM goals
        WHERE type = 'WEEKLY'
        AND isActive = 1
        AND startDate <= :today
        AND endDate >= :today
    """)
    suspend fun getCurrentWeeklyGoals(today: LocalDate = LocalDate.now()): List<GoalEntity>

    /**
     * 현재 진행 중인 월간 목표 조회
     */
    @Query("""
        SELECT * FROM goals
        WHERE type = 'MONTHLY'
        AND isActive = 1
        AND startDate <= :today
        AND endDate >= :today
    """)
    suspend fun getCurrentMonthlyGoals(today: LocalDate = LocalDate.now()): List<GoalEntity>

    /**
     * 목표 추가
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity): Long

    /**
     * 목표 수정
     */
    @Update
    suspend fun updateGoal(goal: GoalEntity)

    /**
     * 목표 삭제
     */
    @Delete
    suspend fun deleteGoal(goal: GoalEntity)

    /**
     * 목표 비활성화
     */
    @Query("UPDATE goals SET isActive = 0 WHERE id = :goalId")
    suspend fun deactivateGoal(goalId: Long)

    /**
     * 목표 활성화
     */
    @Query("UPDATE goals SET isActive = 1 WHERE id = :goalId")
    suspend fun activateGoal(goalId: Long)

    /**
     * 만료된 목표 자동 비활성화
     */
    @Query("UPDATE goals SET isActive = 0 WHERE endDate < :today AND isActive = 1")
    suspend fun deactivateExpiredGoals(today: LocalDate = LocalDate.now())
}
