package com.reminder.attachment

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import com.reminder.data.dao.ReminderAttachmentDao
import com.reminder.data.entity.FileType
import com.reminder.data.entity.ReminderAttachment
import com.reminder.data.entity.getFileTypeFromMimeType
import java.io.File
import java.time.LocalDateTime

/**
 * v1.39.0: 파일 첨부 관리자
 *
 * 다양한 파일 형식(PDF, DOC, XLS, TXT, IMAGE)의 첨부 및 관리를 담당합니다.
 */
class FileAttachmentManager(
    private val context: Context,
    private val attachmentDao: ReminderAttachmentDao
) {

    companion object {
        const val MAX_FILE_SIZE = 10 * 1024 * 1024L // 10MB
    }

    /**
     * 파일 첨부
     *
     * @param reminderId 리마인더 ID
     * @param uri 파일 URI
     * @return 첨부파일 ID, 실패 시 null
     */
    suspend fun attachFile(reminderId: Long, uri: Uri): Long? {
        try {
            // 파일 정보 가져오기
            val contentResolver = context.contentResolver
            val fileName = getFileName(uri) ?: return null
            val fileSize = getFileSize(uri) ?: return null
            val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"

            // 파일 크기 제한 확인
            if (fileSize > MAX_FILE_SIZE) {
                throw IllegalArgumentException("File size exceeds 10MB limit")
            }

            // 파일 타입 결정
            val fileType = getFileTypeFromMimeType(mimeType)

            // 로컬 복사 (앱 내부 저장소)
            val localPath = copyToInternalStorage(uri, fileName) ?: return null

            // ReminderAttachment 엔티티 생성
            val attachment = ReminderAttachment(
                reminderId = reminderId,
                fileName = fileName,
                fileType = fileType,
                localPath = localPath,
                fileSize = fileSize,
                mimeType = mimeType,
                isUploaded = false,
                createdAt = LocalDateTime.now()
            )

            // DB에 저장
            return attachmentDao.insertAttachment(attachment)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * 파일 삭제
     */
    suspend fun deleteAttachment(attachment: ReminderAttachment) {
        // 로컬 파일 삭제
        val file = File(attachment.localPath)
        if (file.exists()) {
            file.delete()
        }

        // DB에서 삭제
        attachmentDao.deleteAttachment(attachment)
    }

    /**
     * URI에서 파일 이름 추출
     */
    private fun getFileName(uri: Uri): String? {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (it.moveToFirst() && nameIndex != -1) {
                it.getString(nameIndex)
            } else {
                null
            }
        }
    }

    /**
     * URI에서 파일 크기 가져오기
     */
    private fun getFileSize(uri: Uri): Long? {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            val sizeIndex = it.getColumnIndex(android.provider.OpenableColumns.SIZE)
            if (it.moveToFirst() && sizeIndex != -1) {
                it.getLong(sizeIndex)
            } else {
                null
            }
        }
    }

    /**
     * 파일을 앱 내부 저장소로 복사
     */
    private fun copyToInternalStorage(uri: Uri, fileName: String): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val outputFile = File(context.filesDir, "attachments/$fileName")
            outputFile.parentFile?.mkdirs()

            outputFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }

            outputFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 총 첨부파일 용량 계산
     */
    suspend fun getTotalStorageUsed(): Long {
        return attachmentDao.getTotalStorageUsed() ?: 0L
    }
}
