package com.reminder.data.entity

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDateTime

/**
 * SubTask 엔티티 테스트 (TDD)
 */
class SubTaskTest {

    /** SubTask 생성 시 기본값이 올바르게 설정된다 */
    @Test
    fun subTaskCreationSetsDefaultValuesCorrectly() {
        // Given
        val reminderId = 1L
        val title = "서브 할 일"

        // When
        val subTask = SubTask(
            reminderId = reminderId,
            title = title
        )

        // Then
        assertEquals(reminderId, subTask.reminderId)
        assertEquals(title, subTask.title)
        assertFalse(subTask.isCompleted)
        assertEquals(0, subTask.position)
        assertNotNull(subTask.createdAt)
    }

    /** SubTask 완료 상태 토글이 올바르게 동작한다 */
    @Test
    fun subTaskCompletionToggleWorksCorrectly() {
        // Given
        val subTask = SubTask(
            reminderId = 1L,
            title = "테스트"
        )
        val originalStatus = subTask.isCompleted

        // When
        val updatedSubTask = subTask.copy(isCompleted = !originalStatus)

        // Then
        assertEquals(!originalStatus, updatedSubTask.isCompleted)
    }

    /** SubTask position 설정이 올바르게 동작한다 */
    @Test
    fun subTaskPositionSettingWorksCorrectly() {
        // Given
        val position = 3

        // When
        val subTask = SubTask(
            reminderId = 1L,
            title = "테스트",
            position = position
        )

        // Then
        assertEquals(position, subTask.position)
    }

    /** 동일한 reminderId를 가진 여러 SubTask를 생성할 수 있다 */
    @Test
    fun canCreateMultipleSubTasksWithSameReminderId() {
        // Given
        val reminderId = 1L

        // When
        val subTask1 = SubTask(reminderId = reminderId, title = "서브1", position = 0)
        val subTask2 = SubTask(reminderId = reminderId, title = "서브2", position = 1)
        val subTask3 = SubTask(reminderId = reminderId, title = "서브3", position = 2)

        // Then
        assertEquals(reminderId, subTask1.reminderId)
        assertEquals(reminderId, subTask2.reminderId)
        assertEquals(reminderId, subTask3.reminderId)
        assertNotEquals(subTask1.title, subTask2.title)
        assertNotEquals(subTask2.title, subTask3.title)
    }
}
