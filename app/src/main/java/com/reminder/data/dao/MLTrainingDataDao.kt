package com.reminder.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.reminder.data.entity.MLDataType
import com.reminder.data.entity.MLTrainingDataEntity
import kotlinx.coroutines.flow.Flow

/**
 * ML 학습 데이터 DAO
 * 머신러닝 모델 학습 및 추론에 사용되는 데이터 관리
 */
@Dao
interface MLTrainingDataDao {

    /**
     * ML 학습 데이터 삽입
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: MLTrainingDataEntity): Long

    /**
     * ML 학습 데이터 업데이트
     */
    @Update
    suspend fun update(data: MLTrainingDataEntity)

    /**
     * 타입별 ML 학습 데이터 조회
     */
    @Query("SELECT * FROM ml_training_data WHERE dataType = :dataType ORDER BY lastUsedAt DESC")
    fun getDataByType(dataType: MLDataType): Flow<List<MLTrainingDataEntity>>

    /**
     * 타입과 입력 텍스트로 유사한 학습 데이터 조회
     * @param dataType 데이터 타입
     * @param searchText 검색할 텍스트 (LIKE 쿼리용)
     * @param limit 결과 개수 제한
     */
    @Query("""
        SELECT * FROM ml_training_data
        WHERE dataType = :dataType
        AND inputText LIKE '%' || :searchText || '%'
        ORDER BY usageCount DESC, confidence DESC
        LIMIT :limit
    """)
    suspend fun findSimilarData(
        dataType: MLDataType,
        searchText: String,
        limit: Int = 10
    ): List<MLTrainingDataEntity>

    /**
     * 카테고리별 학습 데이터 조회
     */
    @Query("""
        SELECT * FROM ml_training_data
        WHERE dataType = :dataType
        AND category = :category
        ORDER BY lastUsedAt DESC
    """)
    suspend fun getDataByTypeAndCategory(
        dataType: MLDataType,
        category: String
    ): List<MLTrainingDataEntity>

    /**
     * 요일별 알림 시간 학습 데이터 조회
     */
    @Query("""
        SELECT * FROM ml_training_data
        WHERE dataType = 'NOTIFICATION_TIME'
        AND dayOfWeek = :dayOfWeek
        ORDER BY usageCount DESC, confidence DESC
        LIMIT :limit
    """)
    suspend fun getNotificationTimeByDayOfWeek(
        dayOfWeek: Int,
        limit: Int = 5
    ): List<MLTrainingDataEntity>

    /**
     * 사용 횟수 증가
     */
    @Query("""
        UPDATE ml_training_data
        SET usageCount = usageCount + 1,
            lastUsedAt = :currentTime
        WHERE id = :id
    """)
    suspend fun incrementUsageCount(id: Long, currentTime: String)

    /**
     * 낮은 신뢰도 데이터 삭제 (정리용)
     * @param threshold 신뢰도 임계값 (기본 0.3)
     */
    @Query("DELETE FROM ml_training_data WHERE confidence < :threshold")
    suspend fun deleteLowConfidenceData(threshold: Float = 0.3f): Int

    /**
     * 오래된 데이터 삭제 (90일 이상)
     */
    @Query("""
        DELETE FROM ml_training_data
        WHERE lastUsedAt < :thresholdDate
    """)
    suspend fun deleteOldData(thresholdDate: String): Int

    /**
     * 모든 학습 데이터 조회 (디버깅/통계용)
     */
    @Query("SELECT * FROM ml_training_data ORDER BY lastUsedAt DESC")
    fun getAllData(): Flow<List<MLTrainingDataEntity>>

    /**
     * 타입별 데이터 개수 조회
     */
    @Query("SELECT COUNT(*) FROM ml_training_data WHERE dataType = :dataType")
    suspend fun getDataCount(dataType: MLDataType): Int

    /**
     * 모든 학습 데이터 삭제 (재학습용)
     */
    @Query("DELETE FROM ml_training_data")
    suspend fun deleteAll()
}
