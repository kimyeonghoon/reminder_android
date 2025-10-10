package com.reminder.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.reminder.data.entity.RecurrenceExceptionEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * v1.35.0: 반복 예외 날짜 DAO
 */
@Dao
interface RecurrenceExceptionDao {

    /**
     * 특정 리마인더의 모든 예외 날짜 조회
     */
    @Query("SELECT * FROM recurrence_exceptions WHERE reminderId = :reminderId ORDER BY exceptionDate ASC")
    fun getExceptionsByReminderId(reminderId: Long): Flow<List<RecurrenceExceptionEntity>>

    /**
     * 특정 리마인더의 모든 예외 날짜 조회 (일회성)
     */
    @Query("SELECT * FROM recurrence_exceptions WHERE reminderId = :reminderId ORDER BY exceptionDate ASC")
    suspend fun getExceptionsByReminderIdOnce(reminderId: Long): List<RecurrenceExceptionEntity>

    /**
     * 예외 날짜 추가
     */
    @Insert
    suspend fun insertException(exception: RecurrenceExceptionEntity): Long

    /**
     * 여러 예외 날짜 추가
     */
    @Insert
    suspend fun insertExceptions(exceptions: List<RecurrenceExceptionEntity>)

    /**
     * 예외 날짜 삭제
     */
    @Delete
    suspend fun deleteException(exception: RecurrenceExceptionEntity)

    /**
     * 특정 리마인더의 모든 예외 날짜 삭제
     */
    @Query("DELETE FROM recurrence_exceptions WHERE reminderId = :reminderId")
    suspend fun deleteExceptionsByReminderId(reminderId: Long)

    /**
     * 특정 리마인더의 특정 날짜 예외 삭제
     */
    @Query("DELETE FROM recurrence_exceptions WHERE reminderId = :reminderId AND exceptionDate = :date")
    suspend fun deleteExceptionByDate(reminderId: Long, date: LocalDate)

    /**
     * 특정 날짜가 예외 날짜인지 확인
     */
    @Query("SELECT COUNT(*) > 0 FROM recurrence_exceptions WHERE reminderId = :reminderId AND exceptionDate = :date")
    suspend fun isExceptionDate(reminderId: Long, date: LocalDate): Boolean

    /**
     * 특정 리마인더의 예외 날짜 개수
     */
    @Query("SELECT COUNT(*) FROM recurrence_exceptions WHERE reminderId = :reminderId")
    suspend fun getExceptionCount(reminderId: Long): Int
}
