package com.reminder.data.entity

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDateTime

/**
 * ReminderImage 엔티티 테스트 (TDD)
 */
class ReminderImageTest {

    @Test
    fun `ReminderImage 생성 시 기본값이 올바르게 설정된다`() {
        // Given
        val reminderId = 1L
        val imageUri = "content://media/external/images/media/123"

        // When
        val image = ReminderImage(
            reminderId = reminderId,
            imageUri = imageUri
        )

        // Then
        assertEquals(reminderId, image.reminderId)
        assertEquals(imageUri, image.imageUri)
        assertNotNull(image.createdAt)
    }

    @Test
    fun `동일한 reminderId에 여러 이미지를 추가할 수 있다`() {
        // Given
        val reminderId = 1L

        // When
        val image1 = ReminderImage(reminderId = reminderId, imageUri = "uri1")
        val image2 = ReminderImage(reminderId = reminderId, imageUri = "uri2")
        val image3 = ReminderImage(reminderId = reminderId, imageUri = "uri3")

        // Then
        assertEquals(reminderId, image1.reminderId)
        assertEquals(reminderId, image2.reminderId)
        assertEquals(reminderId, image3.reminderId)
        assertNotEquals(image1.imageUri, image2.imageUri)
        assertNotEquals(image2.imageUri, image3.imageUri)
    }
}
