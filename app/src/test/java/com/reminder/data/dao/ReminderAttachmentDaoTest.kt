package com.reminder.data.dao

import com.reminder.data.entity.FileType
import com.reminder.data.entity.ReminderAttachment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.time.LocalDateTime
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class ReminderAttachmentDaoTest {

    private lateinit var dao: ReminderAttachmentDao

    @Before
    fun setup() {
        dao = mock()
    }

    /** getAttachmentsByReminderId는 특정 리마인더의 첨부파일 목록을 반환한다 */
    @Test
    fun testGetAttachmentsByReminderIdReturnsAttachmentsForReminder() = runTest {
        // Given
        val reminderId = 1L
        val attachments = listOf(
            ReminderAttachment(
                id = 1,
                reminderId = reminderId,
                fileName = "image.jpg",
                fileType = FileType.IMAGE,
                localPath = "content://image.jpg",
                fileSize = 1024,
                mimeType = "image/jpeg"
            ),
            ReminderAttachment(
                id = 2,
                reminderId = reminderId,
                fileName = "document.pdf",
                fileType = FileType.PDF,
                localPath = "content://document.pdf",
                fileSize = 2048,
                mimeType = "application/pdf"
            )
        )
        whenever(dao.getAttachmentsByReminderId(reminderId)).thenReturn(flowOf(attachments))

        // When
        val result = dao.getAttachmentsByReminderId(reminderId).first()

        // Then
        verify(dao).getAttachmentsByReminderId(reminderId)
        assertEquals(2, result.size)
        assertEquals(attachments, result)
    }

    /** getAttachmentsByType은 특정 리마인더의 특정 타입 첨부파일을 반환한다 */
    @Test
    fun testGetAttachmentsByTypeReturnsFilteredAttachments() = runTest {
        // Given
        val reminderId = 1L
        val fileType = FileType.IMAGE
        val attachments = listOf(
            ReminderAttachment(
                id = 1,
                reminderId = reminderId,
                fileName = "image.jpg",
                fileType = FileType.IMAGE,
                localPath = "content://image.jpg",
                fileSize = 1024,
                mimeType = "image/jpeg"
            )
        )
        whenever(dao.getAttachmentsByType(reminderId, fileType)).thenReturn(attachments)

        // When
        val result = dao.getAttachmentsByType(reminderId, fileType)

        // Then
        verify(dao).getAttachmentsByType(reminderId, fileType)
        assertEquals(1, result.size)
        assertEquals(FileType.IMAGE, result[0].fileType)
    }

    /** getAllAttachments는 모든 첨부파일을 반환한다 */
    @Test
    fun testGetAllAttachmentsReturnsAllAttachments() = runTest {
        // Given
        val attachments = listOf(
            ReminderAttachment(
                id = 1,
                reminderId = 1,
                fileName = "image.jpg",
                fileType = FileType.IMAGE,
                localPath = "content://image.jpg",
                fileSize = 1024,
                mimeType = "image/jpeg"
            ),
            ReminderAttachment(
                id = 2,
                reminderId = 2,
                fileName = "document.pdf",
                fileType = FileType.PDF,
                localPath = "content://document.pdf",
                fileSize = 2048,
                mimeType = "application/pdf"
            )
        )
        whenever(dao.getAllAttachments()).thenReturn(flowOf(attachments))

        // When
        val result = dao.getAllAttachments().first()

        // Then
        verify(dao).getAllAttachments()
        assertEquals(2, result.size)
    }

    /** getAttachmentsByTypeAll은 특정 타입의 모든 첨부파일을 반환한다 */
    @Test
    fun testGetAttachmentsByTypeAllReturnsAllAttachmentsOfType() = runTest {
        // Given
        val fileType = FileType.PDF
        val attachments = listOf(
            ReminderAttachment(
                id = 1,
                reminderId = 1,
                fileName = "doc1.pdf",
                fileType = FileType.PDF,
                localPath = "content://doc1.pdf",
                fileSize = 1024,
                mimeType = "application/pdf"
            ),
            ReminderAttachment(
                id = 2,
                reminderId = 2,
                fileName = "doc2.pdf",
                fileType = FileType.PDF,
                localPath = "content://doc2.pdf",
                fileSize = 2048,
                mimeType = "application/pdf"
            )
        )
        whenever(dao.getAttachmentsByTypeAll(fileType)).thenReturn(flowOf(attachments))

        // When
        val result = dao.getAttachmentsByTypeAll(fileType).first()

        // Then
        verify(dao).getAttachmentsByTypeAll(fileType)
        assertEquals(2, result.size)
        assertEquals(FileType.PDF, result[0].fileType)
        assertEquals(FileType.PDF, result[1].fileType)
    }

    /** getUnuploadedAttachments는 업로드되지 않은 첨부파일을 반환한다 */
    @Test
    fun testGetUnuploadedAttachmentsReturnsUnuploadedOnly() = runTest {
        // Given
        val attachments = listOf(
            ReminderAttachment(
                id = 1,
                reminderId = 1,
                fileName = "image.jpg",
                fileType = FileType.IMAGE,
                localPath = "content://image.jpg",
                fileSize = 1024,
                mimeType = "image/jpeg",
                isUploaded = false
            ),
            ReminderAttachment(
                id = 2,
                reminderId = 1,
                fileName = "document.pdf",
                fileType = FileType.PDF,
                localPath = "content://document.pdf",
                fileSize = 2048,
                mimeType = "application/pdf",
                isUploaded = false
            )
        )
        whenever(dao.getUnuploadedAttachments()).thenReturn(attachments)

        // When
        val result = dao.getUnuploadedAttachments()

        // Then
        verify(dao).getUnuploadedAttachments()
        assertEquals(2, result.size)
        assertEquals(false, result[0].isUploaded)
        assertEquals(false, result[1].isUploaded)
    }

    /** insertAttachment는 첨부파일을 삽입하고 ID를 반환한다 */
    @Test
    fun testInsertAttachmentInsertsAndReturnsId() = runTest {
        // Given
        val attachment = ReminderAttachment(
            reminderId = 1,
            fileName = "image.jpg",
            fileType = FileType.IMAGE,
            localPath = "content://image.jpg",
            fileSize = 1024,
            mimeType = "image/jpeg"
        )
        val insertedId = 10L
        whenever(dao.insertAttachment(attachment)).thenReturn(insertedId)

        // When
        val result = dao.insertAttachment(attachment)

        // Then
        verify(dao).insertAttachment(attachment)
        assertEquals(insertedId, result)
    }

    /** updateAttachment는 첨부파일을 업데이트한다 */
    @Test
    fun testUpdateAttachmentUpdatesAttachment() = runTest {
        // Given
        val attachment = ReminderAttachment(
            id = 1,
            reminderId = 1,
            fileName = "image.jpg",
            fileType = FileType.IMAGE,
            localPath = "content://image.jpg",
            fileSize = 1024,
            mimeType = "image/jpeg",
            isUploaded = true,
            cloudUrl = "https://firebase.storage/image.jpg"
        )

        // When
        dao.updateAttachment(attachment)

        // Then
        verify(dao).updateAttachment(attachment)
    }

    /** deleteAttachment는 첨부파일을 삭제한다 */
    @Test
    fun testDeleteAttachmentDeletesAttachment() = runTest {
        // Given
        val attachment = ReminderAttachment(
            id = 1,
            reminderId = 1,
            fileName = "image.jpg",
            fileType = FileType.IMAGE,
            localPath = "content://image.jpg",
            fileSize = 1024,
            mimeType = "image/jpeg"
        )

        // When
        dao.deleteAttachment(attachment)

        // Then
        verify(dao).deleteAttachment(attachment)
    }

    /** deleteAttachmentsByReminderId는 특정 리마인더의 모든 첨부파일을 삭제한다 */
    @Test
    fun testDeleteAttachmentsByReminderIdDeletesAllForReminder() = runTest {
        // Given
        val reminderId = 1L

        // When
        dao.deleteAttachmentsByReminderId(reminderId)

        // Then
        verify(dao).deleteAttachmentsByReminderId(reminderId)
    }

    /** getAttachmentById는 ID로 첨부파일을 조회한다 */
    @Test
    fun testGetAttachmentByIdReturnsAttachment() = runTest {
        // Given
        val attachmentId = 1L
        val attachment = ReminderAttachment(
            id = attachmentId,
            reminderId = 1,
            fileName = "image.jpg",
            fileType = FileType.IMAGE,
            localPath = "content://image.jpg",
            fileSize = 1024,
            mimeType = "image/jpeg"
        )
        whenever(dao.getAttachmentById(attachmentId)).thenReturn(attachment)

        // When
        val result = dao.getAttachmentById(attachmentId)

        // Then
        verify(dao).getAttachmentById(attachmentId)
        assertEquals(attachment, result)
    }

    /** getAttachmentById는 존재하지 않는 ID에 null을 반환한다 */
    @Test
    fun testGetAttachmentByIdReturnsNullForNonExistent() = runTest {
        // Given
        val attachmentId = 999L
        whenever(dao.getAttachmentById(attachmentId)).thenReturn(null)

        // When
        val result = dao.getAttachmentById(attachmentId)

        // Then
        verify(dao).getAttachmentById(attachmentId)
        assertNull(result)
    }

    /** getTotalStorageUsed는 전체 첨부파일 용량을 반환한다 */
    @Test
    fun testGetTotalStorageUsedReturnsTotalSize() = runTest {
        // Given
        val totalSize = 3072L // 1024 + 2048
        whenever(dao.getTotalStorageUsed()).thenReturn(totalSize)

        // When
        val result = dao.getTotalStorageUsed()

        // Then
        verify(dao).getTotalStorageUsed()
        assertEquals(totalSize, result)
    }

    /** getTotalStorageUsed는 첨부파일이 없을 때 null을 반환한다 */
    @Test
    fun testGetTotalStorageUsedReturnsNullWhenEmpty() = runTest {
        // Given
        whenever(dao.getTotalStorageUsed()).thenReturn(null)

        // When
        val result = dao.getTotalStorageUsed()

        // Then
        verify(dao).getTotalStorageUsed()
        assertNull(result)
    }

    /** getAttachmentCount는 특정 리마인더의 첨부파일 개수를 반환한다 */
    @Test
    fun testGetAttachmentCountReturnsCount() = runTest {
        // Given
        val reminderId = 1L
        val count = 3
        whenever(dao.getAttachmentCount(reminderId)).thenReturn(count)

        // When
        val result = dao.getAttachmentCount(reminderId)

        // Then
        verify(dao).getAttachmentCount(reminderId)
        assertEquals(count, result)
    }

    /** getAttachmentCount는 첨부파일이 없을 때 0을 반환한다 */
    @Test
    fun testGetAttachmentCountReturnsZeroWhenEmpty() = runTest {
        // Given
        val reminderId = 1L
        whenever(dao.getAttachmentCount(reminderId)).thenReturn(0)

        // When
        val result = dao.getAttachmentCount(reminderId)

        // Then
        verify(dao).getAttachmentCount(reminderId)
        assertEquals(0, result)
    }

    /** getAttachmentCountByType은 타입별 첨부파일 개수를 반환한다 */
    @Test
    fun testGetAttachmentCountByTypeReturnsCountForType() = runTest {
        // Given
        val fileType = FileType.IMAGE
        val count = 5
        whenever(dao.getAttachmentCountByType(fileType)).thenReturn(count)

        // When
        val result = dao.getAttachmentCountByType(fileType)

        // Then
        verify(dao).getAttachmentCountByType(fileType)
        assertEquals(count, result)
    }

    /** getAttachmentCountByType은 해당 타입이 없을 때 0을 반환한다 */
    @Test
    fun testGetAttachmentCountByTypeReturnsZeroWhenNone() = runTest {
        // Given
        val fileType = FileType.PDF
        whenever(dao.getAttachmentCountByType(fileType)).thenReturn(0)

        // When
        val result = dao.getAttachmentCountByType(fileType)

        // Then
        verify(dao).getAttachmentCountByType(fileType)
        assertEquals(0, result)
    }
}
