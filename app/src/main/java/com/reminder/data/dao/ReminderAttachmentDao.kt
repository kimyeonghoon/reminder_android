package com.reminder.data.dao

import androidx.room.*
import com.reminder.data.entity.FileType
import com.reminder.data.entity.ReminderAttachment
import kotlinx.coroutines.flow.Flow

/**
 * v1.39.0: 리마인더 첨부파일 DAO
 */
@Dao
interface ReminderAttachmentDao {

    /**
     * 특정 리마인더의 모든 첨부파일 조회
     */
    @Query("SELECT * FROM reminder_attachments WHERE reminderId = :reminderId ORDER BY createdAt DESC")
    fun getAttachmentsByReminderId(reminderId: Long): Flow<List<ReminderAttachment>>

    /**
     * 특정 리마인더의 특정 타입 첨부파일 조회
     */
    @Query("SELECT * FROM reminder_attachments WHERE reminderId = :reminderId AND fileType = :fileType ORDER BY createdAt DESC")
    suspend fun getAttachmentsByType(reminderId: Long, fileType: FileType): List<ReminderAttachment>

    /**
     * 모든 첨부파일 조회 (관리 화면용)
     */
    @Query("SELECT * FROM reminder_attachments ORDER BY createdAt DESC")
    fun getAllAttachments(): Flow<List<ReminderAttachment>>

    /**
     * 특정 타입의 모든 첨부파일 조회
     */
    @Query("SELECT * FROM reminder_attachments WHERE fileType = :fileType ORDER BY createdAt DESC")
    fun getAttachmentsByTypeAll(fileType: FileType): Flow<List<ReminderAttachment>>

    /**
     * 업로드되지 않은 첨부파일 조회 (동기화 필요)
     */
    @Query("SELECT * FROM reminder_attachments WHERE isUploaded = 0")
    suspend fun getUnuploadedAttachments(): List<ReminderAttachment>

    /**
     * 첨부파일 추가
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttachment(attachment: ReminderAttachment): Long

    /**
     * 첨부파일 업데이트
     */
    @Update
    suspend fun updateAttachment(attachment: ReminderAttachment)

    /**
     * 첨부파일 삭제
     */
    @Delete
    suspend fun deleteAttachment(attachment: ReminderAttachment)

    /**
     * 특정 리마인더의 모든 첨부파일 삭제
     */
    @Query("DELETE FROM reminder_attachments WHERE reminderId = :reminderId")
    suspend fun deleteAttachmentsByReminderId(reminderId: Long)

    /**
     * ID로 첨부파일 조회
     */
    @Query("SELECT * FROM reminder_attachments WHERE id = :attachmentId")
    suspend fun getAttachmentById(attachmentId: Long): ReminderAttachment?

    /**
     * 총 첨부파일 용량 계산
     */
    @Query("SELECT SUM(fileSize) FROM reminder_attachments")
    suspend fun getTotalStorageUsed(): Long?

    /**
     * 특정 리마인더의 첨부파일 개수
     */
    @Query("SELECT COUNT(*) FROM reminder_attachments WHERE reminderId = :reminderId")
    suspend fun getAttachmentCount(reminderId: Long): Int

    /**
     * 타입별 첨부파일 개수
     */
    @Query("SELECT COUNT(*) FROM reminder_attachments WHERE fileType = :fileType")
    suspend fun getAttachmentCountByType(fileType: FileType): Int
}
