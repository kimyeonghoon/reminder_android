package com.reminder.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * v1.39.0: 리마인더 첨부파일 엔티티
 *
 * 이미지 외에 PDF, DOC, XLS, TXT 등 다양한 파일 형식을 지원합니다.
 * 기존 ReminderImage를 대체하며 하위 호환성을 유지합니다.
 */
@Entity(
    tableName = "reminder_attachments",
    foreignKeys = [
        ForeignKey(
            entity = ReminderEntity::class,
            parentColumns = ["id"],
            childColumns = ["reminderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["reminderId"]),
        Index(value = ["fileType"])
    ]
)
data class ReminderAttachment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /**
     * 상위 리마인더 ID
     */
    val reminderId: Long,

    /**
     * 파일 이름
     */
    val fileName: String,

    /**
     * 파일 타입 (IMAGE, PDF, DOC, XLS, TXT, OTHER)
     */
    val fileType: FileType,

    /**
     * 로컬 파일 경로 (content:// 또는 file://)
     */
    val localPath: String,

    /**
     * Firebase Storage URL (클라우드 백업)
     */
    val cloudUrl: String? = null,

    /**
     * 파일 크기 (bytes)
     */
    val fileSize: Long,

    /**
     * MIME 타입 (image/jpeg, application/pdf 등)
     */
    val mimeType: String,

    /**
     * 업로드 완료 여부
     */
    val isUploaded: Boolean = false,

    /**
     * 생성 일시
     */
    val createdAt: LocalDateTime = LocalDateTime.now(),

    /**
     * OCR 추출 텍스트 (이미지/PDF에서 추출한 텍스트)
     */
    val extractedText: String? = null
)

/**
 * 파일 타입 Enum
 */
enum class FileType {
    IMAGE,      // 이미지 (jpg, png, gif 등)
    PDF,        // PDF 문서
    DOC,        // Word 문서 (doc, docx)
    XLS,        // Excel 문서 (xls, xlsx)
    TXT,        // 텍스트 파일
    OTHER       // 기타 파일
}

/**
 * MIME 타입으로 FileType 결정
 */
fun getFileTypeFromMimeType(mimeType: String): FileType {
    return when {
        mimeType.startsWith("image/") -> FileType.IMAGE
        mimeType == "application/pdf" -> FileType.PDF
        mimeType.contains("word") || mimeType.contains("document") -> FileType.DOC
        mimeType.contains("excel") || mimeType.contains("spreadsheet") -> FileType.XLS
        mimeType == "text/plain" -> FileType.TXT
        else -> FileType.OTHER
    }
}

/**
 * 파일 확장자로 FileType 결정
 */
fun getFileTypeFromExtension(fileName: String): FileType {
    val extension = fileName.substringAfterLast('.', "").lowercase()
    return when (extension) {
        "jpg", "jpeg", "png", "gif", "bmp", "webp" -> FileType.IMAGE
        "pdf" -> FileType.PDF
        "doc", "docx" -> FileType.DOC
        "xls", "xlsx" -> FileType.XLS
        "txt" -> FileType.TXT
        else -> FileType.OTHER
    }
}

/**
 * 파일 크기를 사람이 읽기 쉬운 형식으로 변환
 */
fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> "${bytes / (1024 * 1024)} MB"
    }
}
