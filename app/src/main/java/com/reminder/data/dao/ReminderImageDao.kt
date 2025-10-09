package com.reminder.data.dao

import androidx.room.*
import com.reminder.data.entity.ReminderImage
import kotlinx.coroutines.flow.Flow

/**
 * 리마인더 이미지 Data Access Object
 */
@Dao
interface ReminderImageDao {

    /**
     * 이미지 추가
     */
    @Insert
    suspend fun insert(image: ReminderImage): Long

    /**
     * 이미지 삭제
     */
    @Delete
    suspend fun delete(image: ReminderImage)

    /**
     * 리마인더의 모든 이미지 조회
     */
    @Query("SELECT * FROM reminder_images WHERE reminderId = :reminderId ORDER BY createdAt ASC")
    fun getImagesByReminderId(reminderId: Long): Flow<List<ReminderImage>>

    /**
     * 리마인더의 이미지 개수 조회
     */
    @Query("SELECT COUNT(*) FROM reminder_images WHERE reminderId = :reminderId")
    suspend fun getImagesCount(reminderId: Long): Int

    /**
     * 리마인더의 모든 이미지 삭제
     */
    @Query("DELETE FROM reminder_images WHERE reminderId = :reminderId")
    suspend fun deleteAllByReminderId(reminderId: Long)
}
