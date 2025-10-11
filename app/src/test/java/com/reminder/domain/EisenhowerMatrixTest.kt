package com.reminder.domain

import com.reminder.data.entity.Priority
import com.reminder.data.entity.ReminderEntity
import com.reminder.data.entity.Urgency
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime

/**
 * v1.47.0: Eisenhower Matrix 계산 로직 테스트 (TDD)
 *
 * Eisenhower Matrix (아이젠하워 매트릭스):
 * - DO_FIRST (Q1): 중요하고 긴급함 - 즉시 처리
 * - SCHEDULE (Q2): 중요하지만 긴급하지 않음 - 계획 수립
 * - DELEGATE (Q3): 긴급하지만 중요하지 않음 - 위임
 * - DELETE (Q4): 중요하지도 긴급하지도 않음 - 제거
 */
class EisenhowerMatrixTest {

    /**
     * Quadrant 1 (DO_FIRST) 테스트
     * - Priority.HIGH + Urgency.HIGH
     * - Priority.HIGH + Urgency.MEDIUM
     * - Priority.MEDIUM + Urgency.HIGH
     */
    @Test
    fun `HIGH 우선순위와 HIGH 긴급도는 DO_FIRST 쿼드런트`() {
        val reminder = createReminder(Priority.HIGH, Urgency.HIGH)
        assertEquals(Quadrant.DO_FIRST, reminder.getQuadrant())
    }

    @Test
    fun `HIGH 우선순위와 MEDIUM 긴급도는 DO_FIRST 쿼드런트`() {
        val reminder = createReminder(Priority.HIGH, Urgency.MEDIUM)
        assertEquals(Quadrant.DO_FIRST, reminder.getQuadrant())
    }

    @Test
    fun `MEDIUM 우선순위와 HIGH 긴급도는 DO_FIRST 쿼드런트`() {
        val reminder = createReminder(Priority.MEDIUM, Urgency.HIGH)
        assertEquals(Quadrant.DO_FIRST, reminder.getQuadrant())
    }

    /**
     * Quadrant 2 (SCHEDULE) 테스트
     * - Priority.HIGH + Urgency.LOW
     * - Priority.MEDIUM + Urgency.MEDIUM
     * - Priority.MEDIUM + Urgency.LOW
     */
    @Test
    fun `HIGH 우선순위와 LOW 긴급도는 SCHEDULE 쿼드런트`() {
        val reminder = createReminder(Priority.HIGH, Urgency.LOW)
        assertEquals(Quadrant.SCHEDULE, reminder.getQuadrant())
    }

    @Test
    fun `MEDIUM 우선순위와 MEDIUM 긴급도는 SCHEDULE 쿼드런트`() {
        val reminder = createReminder(Priority.MEDIUM, Urgency.MEDIUM)
        assertEquals(Quadrant.SCHEDULE, reminder.getQuadrant())
    }

    @Test
    fun `MEDIUM 우선순위와 LOW 긴급도는 SCHEDULE 쿼드런트`() {
        val reminder = createReminder(Priority.MEDIUM, Urgency.LOW)
        assertEquals(Quadrant.SCHEDULE, reminder.getQuadrant())
    }

    /**
     * Quadrant 3 (DELEGATE) 테스트
     * - Priority.LOW + Urgency.HIGH
     * - Priority.LOW + Urgency.MEDIUM
     */
    @Test
    fun `LOW 우선순위와 HIGH 긴급도는 DELEGATE 쿼드런트`() {
        val reminder = createReminder(Priority.LOW, Urgency.HIGH)
        assertEquals(Quadrant.DELEGATE, reminder.getQuadrant())
    }

    @Test
    fun `LOW 우선순위와 MEDIUM 긴급도는 DELEGATE 쿼드런트`() {
        val reminder = createReminder(Priority.LOW, Urgency.MEDIUM)
        assertEquals(Quadrant.DELEGATE, reminder.getQuadrant())
    }

    /**
     * Quadrant 4 (DELETE) 테스트
     * - Priority.LOW + Urgency.LOW
     */
    @Test
    fun `LOW 우선순위와 LOW 긴급도는 DELETE 쿼드런트`() {
        val reminder = createReminder(Priority.LOW, Urgency.LOW)
        assertEquals(Quadrant.DELETE, reminder.getQuadrant())
    }

    /**
     * 쿼드런트별 필터링 테스트
     */
    @Test
    fun `리마인더 리스트를 쿼드런트별로 필터링할 수 있다`() {
        val reminders = listOf(
            createReminder(Priority.HIGH, Urgency.HIGH),    // DO_FIRST
            createReminder(Priority.HIGH, Urgency.LOW),     // SCHEDULE
            createReminder(Priority.LOW, Urgency.HIGH),     // DELEGATE
            createReminder(Priority.LOW, Urgency.LOW),      // DELETE
            createReminder(Priority.MEDIUM, Urgency.HIGH),  // DO_FIRST
        )

        val doFirst = reminders.filterByQuadrant(Quadrant.DO_FIRST)
        val schedule = reminders.filterByQuadrant(Quadrant.SCHEDULE)
        val delegate = reminders.filterByQuadrant(Quadrant.DELEGATE)
        val delete = reminders.filterByQuadrant(Quadrant.DELETE)

        assertEquals(2, doFirst.size)
        assertEquals(1, schedule.size)
        assertEquals(1, delegate.size)
        assertEquals(1, delete.size)
    }

    /**
     * 쿼드런트별 카운트 테스트
     */
    @Test
    fun `리마인더 리스트의 쿼드런트별 개수를 계산할 수 있다`() {
        val reminders = listOf(
            createReminder(Priority.HIGH, Urgency.HIGH),
            createReminder(Priority.HIGH, Urgency.HIGH),
            createReminder(Priority.HIGH, Urgency.LOW),
            createReminder(Priority.LOW, Urgency.LOW)
        )

        val counts = reminders.countByQuadrant()

        assertEquals(2, counts[Quadrant.DO_FIRST])
        assertEquals(1, counts[Quadrant.SCHEDULE])
        assertEquals(0, counts[Quadrant.DELEGATE])
        assertEquals(1, counts[Quadrant.DELETE])
    }

    // Helper 함수
    private fun createReminder(
        priority: Priority,
        urgency: Urgency,
        title: String = "테스트 리마인더"
    ): ReminderEntity {
        return ReminderEntity(
            id = 0,
            title = title,
            priority = priority,
            urgency = urgency,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }
}
