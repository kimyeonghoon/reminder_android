package com.reminder.attachment

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import com.reminder.data.dao.ReminderAttachmentDao
import com.reminder.data.entity.FileType
import com.reminder.data.entity.ReminderAttachment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.ByteArrayInputStream
import java.io.File
import java.time.LocalDateTime

/**
 * FileAttachmentManager 테스트
 *
 * AAA 패턴 (Given-When-Then)으로 파일 첨부 메서드 검증
 *
 * Note: Context 및 Android 의존성으로 인해 핵심 비즈니스 로직만 테스트
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FileAttachmentManagerTest {

    private lateinit var context: Context
    private lateinit var attachmentDao: ReminderAttachmentDao
    private lateinit var contentResolver: ContentResolver
    private lateinit var manager: FileAttachmentManager

    @Before
    fun setup() {
        context = mock(Context::class.java)
        attachmentDao = mock(ReminderAttachmentDao::class.java)
        contentResolver = mock(ContentResolver::class.java)

        `when`(context.contentResolver).thenReturn(contentResolver)
        `when`(context.filesDir).thenReturn(File("/tmp"))

        manager = FileAttachmentManager(context, attachmentDao)
    }

    /** attachFile는 파일 크기가 10MB를 초과하면 실패한다 */
    @Test
    fun attachFileFailsWhenFileSizeExceeds10MB() = runTest {
        // Given
        val reminderId = 1L
        val uri = mock(Uri::class.java)
        val cursor = mock(Cursor::class.java)

        // 파일 크기 11MB로 설정
        val oversizedFileSize = 11 * 1024 * 1024L
        `when`(contentResolver.query(any(), any(), any(), any(), any())).thenReturn(cursor)
        `when`(cursor.moveToFirst()).thenReturn(true)
        `when`(cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)).thenReturn(0)
        `when`(cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)).thenReturn(1)
        `when`(cursor.getString(0)).thenReturn("large_file.pdf")
        `when`(cursor.getLong(1)).thenReturn(oversizedFileSize)
        `when`(contentResolver.getType(uri)).thenReturn("application/pdf")

        // When
        val result = manager.attachFile(reminderId, uri)

        // Then
        assertNull(result)
        verify(attachmentDao, never()).insertAttachment(any())
    }

    /** deleteAttachment는 첨부파일을 삭제한다 */
    @Test
    fun deleteAttachmentDeletesAttachment() = runTest {
        // Given
        val attachment = createAttachment(
            id = 1L,
            reminderId = 1L,
            fileName = "test.pdf",
            localPath = "/tmp/test.pdf"
        )
        whenever(attachmentDao.deleteAttachment(any())).thenReturn(Unit)

        // When
        manager.deleteAttachment(attachment)

        // Then
        verify(attachmentDao).deleteAttachment(attachment)
    }

    /** deleteAttachment는 로컬 파일도 함께 삭제한다 */
    @Test
    fun deleteAttachmentDeletesLocalFile() = runTest {
        // Given
        // Note: 실제 파일 삭제는 통합 테스트에서 검증
        // 단위 테스트에서는 메서드 호출만 확인
        val attachment = createAttachment(
            id = 1L,
            reminderId = 1L,
            fileName = "test.pdf",
            localPath = "/nonexistent/test.pdf" // 존재하지 않는 경로
        )

        // When
        manager.deleteAttachment(attachment)

        // Then
        verify(attachmentDao).deleteAttachment(attachment)
    }

    /** getTotalStorageUsed는 총 저장 공간을 반환한다 */
    @Test
    fun getTotalStorageUsedReturnsTotalStorage() = runTest {
        // Given
        val totalStorage = 5 * 1024 * 1024L // 5MB
        whenever(attachmentDao.getTotalStorageUsed()).thenReturn(totalStorage)

        // When
        val result = manager.getTotalStorageUsed()

        // Then
        assertEquals(totalStorage, result)
        verify(attachmentDao).getTotalStorageUsed()
    }

    /** getTotalStorageUsed는 null일 때 0을 반환한다 */
    @Test
    fun getTotalStorageUsedReturnsZeroWhenNull() = runTest {
        // Given
        whenever(attachmentDao.getTotalStorageUsed()).thenReturn(null)

        // When
        val result = manager.getTotalStorageUsed()

        // Then
        assertEquals(0L, result)
    }

    /** MAX_FILE_SIZE는 10MB이다 */
    @Test
    fun maxFileSizeIs10MB() {
        assertEquals(10 * 1024 * 1024L, FileAttachmentManager.MAX_FILE_SIZE)
    }

    /** attachFile는 파일 이름이 null이면 실패한다 */
    @Test
    fun attachFileFailsWhenFileNameIsNull() = runTest {
        // Given
        val reminderId = 1L
        val uri = mock(Uri::class.java)
        val cursor = mock(Cursor::class.java)

        `when`(contentResolver.query(any(), any(), any(), any(), any())).thenReturn(cursor)
        `when`(cursor.moveToFirst()).thenReturn(true)
        `when`(cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)).thenReturn(-1) // Invalid index

        // When
        val result = manager.attachFile(reminderId, uri)

        // Then
        assertNull(result)
    }

    /** attachFile는 파일 크기가 null이면 실패한다 */
    @Test
    fun attachFileFailsWhenFileSizeIsNull() = runTest {
        // Given
        val reminderId = 1L
        val uri = mock(Uri::class.java)
        val cursor = mock(Cursor::class.java)

        `when`(contentResolver.query(any(), any(), any(), any(), any())).thenReturn(cursor)
        `when`(cursor.moveToFirst()).thenReturn(true)
        `when`(cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)).thenReturn(0)
        `when`(cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)).thenReturn(-1) // Invalid index
        `when`(cursor.getString(0)).thenReturn("test.pdf")

        // When
        val result = manager.attachFile(reminderId, uri)

        // Then
        assertNull(result)
    }

    /** attachFile는 예외 발생 시 null을 반환한다 */
    @Test
    fun attachFileReturnsNullOnException() = runTest {
        // Given
        val reminderId = 1L
        val uri = mock(Uri::class.java)

        `when`(contentResolver.query(any(), any(), any(), any(), any()))
            .thenThrow(RuntimeException("Query failed"))

        // When
        val result = manager.attachFile(reminderId, uri)

        // Then
        assertNull(result)
    }

    /** 여러 첨부파일을 연속으로 삭제할 수 있다 */
    @Test
    fun canDeleteMultipleAttachmentsSequentially() = runTest {
        // Given
        val attachment1 = createAttachment(id = 1L, reminderId = 1L, fileName = "file1.pdf")
        val attachment2 = createAttachment(id = 2L, reminderId = 1L, fileName = "file2.pdf")
        whenever(attachmentDao.deleteAttachment(any())).thenReturn(Unit)

        // When
        manager.deleteAttachment(attachment1)
        manager.deleteAttachment(attachment2)

        // Then
        verify(attachmentDao, times(2)).deleteAttachment(any())
    }

    // Helper function
    private fun createAttachment(
        id: Long,
        reminderId: Long,
        fileName: String,
        localPath: String = "/tmp/$fileName",
        fileType: FileType = FileType.PDF,
        fileSize: Long = 1024L,
        mimeType: String = "application/pdf",
        isUploaded: Boolean = false
    ) = ReminderAttachment(
        id = id,
        reminderId = reminderId,
        fileName = fileName,
        fileType = fileType,
        localPath = localPath,
        fileSize = fileSize,
        mimeType = mimeType,
        isUploaded = isUploaded,
        createdAt = LocalDateTime.now()
    )
}
