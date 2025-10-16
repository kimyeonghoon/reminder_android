package com.reminder.data.dao

import com.reminder.data.entity.ReminderImage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.time.LocalDateTime
import org.junit.Assert.*

/**
 * ReminderImageDao 테스트
 *
 * Mockito + kotlin.test + runTest 사용
 * AAA 패턴 (Given-When-Then)
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ReminderImageDaoTest {

    private lateinit var dao: ReminderImageDao

    @Before
    fun setup() {
        dao = mock()
    }

    // ========================================
    // insert() 테스트
    // ========================================

    /** insert는 이미지를 삽입하고 ID를 반환한다 */
    @Test
    fun testInsertInsertsImageAndReturnsId() = runTest {
        // Given
        val image = ReminderImage(
            reminderId = 1L,
            imageUri = "content://media/external/images/1",
            createdAt = LocalDateTime.now()
        )
        val insertedId = 10L
        whenever(dao.insert(image)).thenReturn(insertedId)

        // When
        val result = dao.insert(image)

        // Then
        verify(dao).insert(image)
        assertEquals(insertedId, result)
    }

    /** insert는 여러 이미지를 삽입할 수 있다 */
    @Test
    fun testInsertMultipleImages() = runTest {
        // Given
        val image1 = ReminderImage(
            reminderId = 1L,
            imageUri = "content://media/external/images/1"
        )
        val image2 = ReminderImage(
            reminderId = 1L,
            imageUri = "content://media/external/images/2"
        )
        whenever(dao.insert(image1)).thenReturn(1L)
        whenever(dao.insert(image2)).thenReturn(2L)

        // When
        val id1 = dao.insert(image1)
        val id2 = dao.insert(image2)

        // Then
        verify(dao).insert(image1)
        verify(dao).insert(image2)
        assertEquals(1L, id1)
        assertEquals(2L, id2)
    }

    // ========================================
    // delete() 테스트
    // ========================================

    /** delete는 이미지를 삭제한다 */
    @Test
    fun testDeleteDeletesImage() = runTest {
        // Given
        val image = ReminderImage(
            id = 1L,
            reminderId = 1L,
            imageUri = "content://media/external/images/1"
        )

        // When
        dao.delete(image)

        // Then
        verify(dao).delete(image)
    }

    /** delete는 존재하는 이미지만 삭제한다 */
    @Test
    fun testDeleteOnlyDeletesExistingImage() = runTest {
        // Given
        val existingImage = ReminderImage(
            id = 5L,
            reminderId = 1L,
            imageUri = "content://media/external/images/5"
        )

        // When
        dao.delete(existingImage)

        // Then
        verify(dao, times(1)).delete(existingImage)
    }

    // ========================================
    // getImagesByReminderId() 테스트
    // ========================================

    /** getImagesByReminderId는 리마인더의 모든 이미지를 반환한다 */
    @Test
    fun testGetImagesByReminderIdReturnsAllImages() = runTest {
        // Given
        val reminderId = 1L
        val images = listOf(
            ReminderImage(
                id = 1L,
                reminderId = reminderId,
                imageUri = "content://media/external/images/1",
                createdAt = LocalDateTime.of(2025, 10, 16, 10, 0)
            ),
            ReminderImage(
                id = 2L,
                reminderId = reminderId,
                imageUri = "content://media/external/images/2",
                createdAt = LocalDateTime.of(2025, 10, 16, 11, 0)
            )
        )
        whenever(dao.getImagesByReminderId(reminderId)).thenReturn(flowOf(images))

        // When
        val result = dao.getImagesByReminderId(reminderId)

        // Then
        verify(dao).getImagesByReminderId(reminderId)
        assertNotNull(result)
    }

    /** getImagesByReminderId는 createdAt 오름차순으로 정렬된 이미지를 반환한다 */
    @Test
    fun testGetImagesByReminderIdReturnsSortedByCreatedAt() = runTest {
        // Given
        val reminderId = 1L
        val now = LocalDateTime.now()
        val images = listOf(
            ReminderImage(
                id = 1L,
                reminderId = reminderId,
                imageUri = "content://media/external/images/1",
                createdAt = now.minusHours(2)
            ),
            ReminderImage(
                id = 2L,
                reminderId = reminderId,
                imageUri = "content://media/external/images/2",
                createdAt = now.minusHours(1)
            ),
            ReminderImage(
                id = 3L,
                reminderId = reminderId,
                imageUri = "content://media/external/images/3",
                createdAt = now
            )
        )
        whenever(dao.getImagesByReminderId(reminderId)).thenReturn(flowOf(images))

        // When
        dao.getImagesByReminderId(reminderId)

        // Then
        verify(dao).getImagesByReminderId(reminderId)
    }

    /** getImagesByReminderId는 이미지가 없을 경우 빈 리스트를 반환한다 */
    @Test
    fun testGetImagesByReminderIdReturnsEmptyListWhenNoImages() = runTest {
        // Given
        val reminderId = 999L
        whenever(dao.getImagesByReminderId(reminderId)).thenReturn(flowOf(emptyList()))

        // When
        val result = dao.getImagesByReminderId(reminderId)

        // Then
        verify(dao).getImagesByReminderId(reminderId)
        assertNotNull(result)
    }

    // ========================================
    // getImagesCount() 테스트
    // ========================================

    /** getImagesCount는 리마인더의 이미지 개수를 반환한다 */
    @Test
    fun testGetImagesCountReturnsCorrectCount() = runTest {
        // Given
        val reminderId = 1L
        val count = 3
        whenever(dao.getImagesCount(reminderId)).thenReturn(count)

        // When
        val result = dao.getImagesCount(reminderId)

        // Then
        verify(dao).getImagesCount(reminderId)
        assertEquals(count, result)
    }

    /** getImagesCount는 이미지가 없을 경우 0을 반환한다 */
    @Test
    fun testGetImagesCountReturnsZeroWhenNoImages() = runTest {
        // Given
        val reminderId = 999L
        whenever(dao.getImagesCount(reminderId)).thenReturn(0)

        // When
        val result = dao.getImagesCount(reminderId)

        // Then
        verify(dao).getImagesCount(reminderId)
        assertEquals(0, result)
    }

    /** getImagesCount는 올바른 reminderId를 전달한다 */
    @Test
    fun testGetImagesCountPassesCorrectReminderId() = runTest {
        // Given
        val reminderId = 42L
        whenever(dao.getImagesCount(reminderId)).thenReturn(5)

        // When
        dao.getImagesCount(reminderId)

        // Then
        verify(dao).getImagesCount(eq(42L))
    }

    // ========================================
    // deleteAllByReminderId() 테스트
    // ========================================

    /** deleteAllByReminderId는 리마인더의 모든 이미지를 삭제한다 */
    @Test
    fun testDeleteAllByReminderIdDeletesAllImages() = runTest {
        // Given
        val reminderId = 1L

        // When
        dao.deleteAllByReminderId(reminderId)

        // Then
        verify(dao).deleteAllByReminderId(reminderId)
    }

    /** deleteAllByReminderId는 특정 리마인더의 이미지만 삭제한다 */
    @Test
    fun testDeleteAllByReminderIdOnlyDeletesSpecificReminderImages() = runTest {
        // Given
        val reminderId = 5L

        // When
        dao.deleteAllByReminderId(reminderId)

        // Then
        verify(dao, times(1)).deleteAllByReminderId(eq(5L))
        verify(dao, never()).deleteAllByReminderId(argThat { id -> id != 5L })
    }

    /** deleteAllByReminderId는 이미지가 없어도 정상 실행된다 */
    @Test
    fun testDeleteAllByReminderIdWorksWhenNoImages() = runTest {
        // Given
        val reminderId = 999L

        // When
        dao.deleteAllByReminderId(reminderId)

        // Then
        verify(dao).deleteAllByReminderId(reminderId)
    }
}
