package com.reminder.data.dao

import androidx.room.*
import com.reminder.data.entity.ReminderTemplate
import kotlinx.coroutines.flow.Flow

/**
 * 리마인더 템플릿 Data Access Object
 */
@Dao
interface ReminderTemplateDao {

    /**
     * 모든 템플릿 조회
     */
    @Query("SELECT * FROM reminder_templates ORDER BY name ASC")
    fun getAllTemplates(): Flow<List<ReminderTemplate>>

    /**
     * ID로 템플릿 조회
     */
    @Query("SELECT * FROM reminder_templates WHERE id = :id")
    suspend fun getTemplateById(id: Long): ReminderTemplate?

    /**
     * 템플릿 추가
     */
    @Insert
    suspend fun insert(template: ReminderTemplate): Long

    /**
     * 템플릿 업데이트
     */
    @Update
    suspend fun update(template: ReminderTemplate)

    /**
     * 템플릿 삭제
     */
    @Delete
    suspend fun delete(template: ReminderTemplate)

    /**
     * 모든 템플릿 삭제
     */
    @Query("DELETE FROM reminder_templates")
    suspend fun deleteAll()
}
