package com.reminder.data.dao

import androidx.room.*
import com.reminder.data.entity.CalendarSyncConfig
import kotlinx.coroutines.flow.Flow

/**
 * v1.40.0: 캘린더 동기화 설정 DAO
 */
@Dao
interface CalendarSyncConfigDao {

    /**
     * 모든 캘린더 설정 조회
     */
    @Query("SELECT * FROM calendar_sync_config ORDER BY createdAt DESC")
    fun getAllConfigs(): Flow<List<CalendarSyncConfig>>

    /**
     * 동기화 활성화된 캘린더만 조회
     */
    @Query("SELECT * FROM calendar_sync_config WHERE isSyncEnabled = 1")
    suspend fun getEnabledConfigs(): List<CalendarSyncConfig>

    /**
     * 특정 캘린더 설정 조회
     */
    @Query("SELECT * FROM calendar_sync_config WHERE calendarId = :calendarId")
    suspend fun getConfigByCalendarId(calendarId: String): CalendarSyncConfig?

    /**
     * 캘린더 설정 추가
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfig(config: CalendarSyncConfig): Long

    /**
     * 캘린더 설정 업데이트
     */
    @Update
    suspend fun updateConfig(config: CalendarSyncConfig)

    /**
     * 캘린더 설정 삭제
     */
    @Delete
    suspend fun deleteConfig(config: CalendarSyncConfig)

    /**
     * 모든 설정 삭제
     */
    @Query("DELETE FROM calendar_sync_config")
    suspend fun deleteAllConfigs()
}
