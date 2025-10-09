package com.reminder.data.dao

import androidx.room.*
import com.reminder.data.entity.SubTask
import kotlinx.coroutines.flow.Flow

/**
 * 서브태스크 Data Access Object
 *
 * 서브태스크 테이블에 대한 데이터베이스 작업을 정의합니다.
 */
@Dao
interface SubTaskDao {

    /**
     * 서브태스크 추가
     *
     * @param subTask 추가할 서브태스크
     * @return 생성된 서브태스크 ID
     */
    @Insert
    suspend fun insert(subTask: SubTask): Long

    /**
     * 여러 서브태스크 추가
     *
     * @param subTasks 추가할 서브태스크 리스트
     */
    @Insert
    suspend fun insertAll(subTasks: List<SubTask>)

    /**
     * 서브태스크 업데이트
     *
     * @param subTask 업데이트할 서브태스크
     */
    @Update
    suspend fun update(subTask: SubTask)

    /**
     * 여러 서브태스크 업데이트 (재정렬 시 사용)
     *
     * @param subTasks 업데이트할 서브태스크 리스트
     */
    @Update
    suspend fun updateAll(subTasks: List<SubTask>)

    /**
     * 서브태스크 삭제
     *
     * @param subTask 삭제할 서브태스크
     */
    @Delete
    suspend fun delete(subTask: SubTask)

    /**
     * ID로 서브태스크 조회
     *
     * @param id 서브태스크 ID
     * @return 서브태스크 또는 null
     */
    @Query("SELECT * FROM subtasks WHERE id = :id")
    suspend fun getSubTaskById(id: Long): SubTask?

    /**
     * 리마인더별 서브태스크 조회 (position 순서로 정렬)
     *
     * @param reminderId 리마인더 ID
     * @return 서브태스크 Flow 리스트
     */
    @Query("SELECT * FROM subtasks WHERE reminderId = :reminderId ORDER BY position ASC")
    fun getSubTasksByReminderId(reminderId: Long): Flow<List<SubTask>>

    /**
     * 리마인더의 완료된 서브태스크 개수 조회
     *
     * @param reminderId 리마인더 ID
     * @return 완료된 서브태스크 개수
     */
    @Query("SELECT COUNT(*) FROM subtasks WHERE reminderId = :reminderId AND isCompleted = 1")
    suspend fun getCompletedSubTasksCount(reminderId: Long): Int

    /**
     * 리마인더의 전체 서브태스크 개수 조회
     *
     * @param reminderId 리마인더 ID
     * @return 전체 서브태스크 개수
     */
    @Query("SELECT COUNT(*) FROM subtasks WHERE reminderId = :reminderId")
    suspend fun getTotalSubTasksCount(reminderId: Long): Int

    /**
     * 리마인더의 모든 서브태스크 삭제
     *
     * @param reminderId 리마인더 ID
     */
    @Query("DELETE FROM subtasks WHERE reminderId = :reminderId")
    suspend fun deleteAllByReminderId(reminderId: Long)
}
