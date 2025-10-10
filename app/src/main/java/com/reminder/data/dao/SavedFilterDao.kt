package com.reminder.data.dao

import androidx.room.*
import com.reminder.data.entity.SavedFilterEntity
import kotlinx.coroutines.flow.Flow

/**
 * 저장된 필터 DAO
 *
 * 사용자 정의 필터 조합을 저장하고 관리
 */
@Dao
interface SavedFilterDao {

    /**
     * 모든 저장된 필터 조회 (order 순서대로)
     */
    @Query("SELECT * FROM saved_filters ORDER BY `order` ASC, createdAt DESC")
    fun getAllSavedFilters(): Flow<List<SavedFilterEntity>>

    /**
     * ID로 저장된 필터 조회
     */
    @Query("SELECT * FROM saved_filters WHERE id = :id")
    suspend fun getSavedFilterById(id: Long): SavedFilterEntity?

    /**
     * 저장된 필터 추가
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedFilter(filter: SavedFilterEntity): Long

    /**
     * 저장된 필터 업데이트
     */
    @Update
    suspend fun updateSavedFilter(filter: SavedFilterEntity)

    /**
     * 저장된 필터 삭제
     */
    @Delete
    suspend fun deleteSavedFilter(filter: SavedFilterEntity)

    /**
     * ID로 저장된 필터 삭제
     */
    @Query("DELETE FROM saved_filters WHERE id = :id")
    suspend fun deleteSavedFilterById(id: Long)

    /**
     * 모든 저장된 필터 삭제
     */
    @Query("DELETE FROM saved_filters")
    suspend fun deleteAllSavedFilters()

    /**
     * 저장된 필터 개수 조회
     */
    @Query("SELECT COUNT(*) FROM saved_filters")
    suspend fun getCount(): Int
}
