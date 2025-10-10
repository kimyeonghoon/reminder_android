package com.reminder.filter

import com.reminder.data.entity.Priority
import com.reminder.data.entity.ReminderEntity
import com.reminder.data.entity.RecurrencePattern
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

/**
 * FilterEngine 유닛 테스트 (TDD Red 단계)
 *
 * 테스트 대상:
 * - ReminderFilter 복합 필터 적용
 * - 우선순위 필터링
 * - 카테고리 필터링
 * - 태그 필터링
 * - 날짜 범위 필터링
 * - 완료 상태 필터링
 * - 위치/웹링크/TTS 필터링
 */
class FilterEngineTest {

    private lateinit var filterEngine: FilterEngine
    private lateinit var testReminders: List<ReminderEntity>

    @Before
    fun setup() {
        filterEngine = FilterEngine()

        // 테스트용 리마인더 데이터 생성
        val now = LocalDateTime.now()
        testReminders = listOf(
            // 1. 높은 우선순위, 업무, 태그(긴급), 오늘 마감
            ReminderEntity(
                id = 1,
                title = "긴급 회의",
                priority = Priority.HIGH,
                category = "업무",
                tags = "긴급,회의",
                dueDateTime = now.plusHours(2),
                isCompleted = false
            ),
            // 2. 중간 우선순위, 개인, 태그(운동), 내일 마감
            ReminderEntity(
                id = 2,
                title = "헬스장 가기",
                priority = Priority.MEDIUM,
                category = "개인",
                tags = "운동,건강",
                dueDateTime = now.plusDays(1),
                isCompleted = false
            ),
            // 3. 낮은 우선순위, 쇼핑, 완료됨
            ReminderEntity(
                id = 3,
                title = "우유 사기",
                priority = Priority.LOW,
                category = "쇼핑",
                tags = "장보기",
                isCompleted = true,
                dueDateTime = now.minusDays(1)
            ),
            // 4. 위치 설정된 리마인더
            ReminderEntity(
                id = 4,
                title = "집 도착하면 청소하기",
                priority = Priority.MEDIUM,
                category = "집안일",
                locationLatitude = 37.5665,
                locationLongitude = 126.9780,
                locationName = "집",
                isCompleted = false
            ),
            // 5. 웹 링크가 있는 리마인더
            ReminderEntity(
                id = 5,
                title = "기사 읽기",
                priority = Priority.LOW,
                category = "학습",
                webLink = "https://example.com/article",
                isCompleted = false
            ),
            // 6. TTS 설정된 리마인더
            ReminderEntity(
                id = 6,
                title = "약 먹기",
                priority = Priority.HIGH,
                category = "건강",
                readAloud = true,
                isCompleted = false
            ),
            // 7. 날짜 없는 리마인더
            ReminderEntity(
                id = 7,
                title = "언젠가 할 일",
                priority = Priority.LOW,
                category = "기타",
                dueDateTime = null,
                isCompleted = false
            ),
            // 8. 일주일 후 마감
            ReminderEntity(
                id = 8,
                title = "프로젝트 제출",
                priority = Priority.HIGH,
                category = "업무",
                tags = "프로젝트",
                dueDateTime = now.plusWeeks(1),
                isCompleted = false
            )
        )
    }

    @Test
    fun `빈 필터 적용 시 모든 리마인더 반환`() {
        // Given
        val emptyFilter = ReminderFilter()

        // When
        val result = filterEngine.applyFilter(testReminders, emptyFilter)

        // Then
        assertEquals(8, result.size)
        assertEquals(testReminders, result)
    }

    @Test
    fun `우선순위 필터 - 높은 우선순위만`() {
        // Given
        val filter = ReminderFilter(priorities = listOf(Priority.HIGH))

        // When
        val result = filterEngine.applyFilter(testReminders, filter)

        // Then
        assertEquals(3, result.size)
        assertTrue(result.all { it.priority == Priority.HIGH })
        assertTrue(result.any { it.id == 1L }) // 긴급 회의
        assertTrue(result.any { it.id == 6L }) // 약 먹기
        assertTrue(result.any { it.id == 8L }) // 프로젝트 제출
    }

    @Test
    fun `우선순위 필터 - 여러 우선순위`() {
        // Given
        val filter = ReminderFilter(priorities = listOf(Priority.HIGH, Priority.MEDIUM))

        // When
        val result = filterEngine.applyFilter(testReminders, filter)

        // Then
        assertEquals(5, result.size)
        assertTrue(result.all { it.priority in listOf(Priority.HIGH, Priority.MEDIUM) })
    }

    @Test
    fun `카테고리 필터 - 단일 카테고리`() {
        // Given
        val filter = ReminderFilter(categories = listOf("업무"))

        // When
        val result = filterEngine.applyFilter(testReminders, filter)

        // Then
        assertEquals(2, result.size)
        assertTrue(result.all { it.category == "업무" })
        assertTrue(result.any { it.id == 1L }) // 긴급 회의
        assertTrue(result.any { it.id == 8L }) // 프로젝트 제출
    }

    @Test
    fun `카테고리 필터 - 여러 카테고리`() {
        // Given
        val filter = ReminderFilter(categories = listOf("업무", "개인"))

        // When
        val result = filterEngine.applyFilter(testReminders, filter)

        // Then
        assertEquals(3, result.size)
        assertTrue(result.all { it.category in listOf("업무", "개인") })
    }

    @Test
    fun `태그 필터 - 단일 태그`() {
        // Given
        val filter = ReminderFilter(tags = listOf("긴급"))

        // When
        val result = filterEngine.applyFilter(testReminders, filter)

        // Then
        assertEquals(1, result.size)
        assertEquals(1L, result[0].id) // 긴급 회의
        assertTrue(result[0].tags.contains("긴급"))
    }

    @Test
    fun `날짜 범위 필터 - 오늘부터 일주일`() {
        // Given
        val now = LocalDateTime.now()
        val filter = ReminderFilter(
            dateRange = DateRange(
                start = now,
                end = now.plusWeeks(1)
            )
        )

        // When
        val result = filterEngine.applyFilter(testReminders, filter)

        // Then
        // 날짜가 있고 범위 내에 있는 것들만
        assertTrue(result.all { reminder ->
            reminder.dueDateTime != null &&
                    !reminder.dueDateTime!!.isBefore(now) &&
                    !reminder.dueDateTime!!.isAfter(now.plusWeeks(1))
        })
    }

    @Test
    fun `완료 상태 필터 - 미완료만`() {
        // Given
        val filter = ReminderFilter(isCompleted = false)

        // When
        val result = filterEngine.applyFilter(testReminders, filter)

        // Then
        assertEquals(7, result.size)
        assertTrue(result.all { !it.isCompleted })
        assertFalse(result.any { it.id == 3L }) // 우유 사기 (완료됨)
    }

    @Test
    fun `완료 상태 필터 - 완료된 것만`() {
        // Given
        val filter = ReminderFilter(isCompleted = true)

        // When
        val result = filterEngine.applyFilter(testReminders, filter)

        // Then
        assertEquals(1, result.size)
        assertTrue(result.all { it.isCompleted })
        assertEquals(3L, result[0].id) // 우유 사기
    }

    @Test
    fun `위치 필터 - 위치가 설정된 것만`() {
        // Given
        val filter = ReminderFilter(hasLocation = true)

        // When
        val result = filterEngine.applyFilter(testReminders, filter)

        // Then
        assertEquals(1, result.size)
        assertEquals(4L, result[0].id) // 집 도착하면 청소하기
        assertNotNull(result[0].locationLatitude)
        assertNotNull(result[0].locationLongitude)
    }

    @Test
    fun `웹 링크 필터 - 웹 링크가 있는 것만`() {
        // Given
        val filter = ReminderFilter(hasWebLink = true)

        // When
        val result = filterEngine.applyFilter(testReminders, filter)

        // Then
        assertEquals(1, result.size)
        assertEquals(5L, result[0].id) // 기사 읽기
        assertNotNull(result[0].webLink)
    }

    @Test
    fun `TTS 필터 - TTS가 설정된 것만`() {
        // Given
        val filter = ReminderFilter(hasTts = true)

        // When
        val result = filterEngine.applyFilter(testReminders, filter)

        // Then
        assertEquals(1, result.size)
        assertEquals(6L, result[0].id) // 약 먹기
        assertTrue(result[0].readAloud)
    }

    @Test
    fun `복합 필터 - 높은 우선순위 + 업무 카테고리 + 미완료`() {
        // Given
        val filter = ReminderFilter(
            priorities = listOf(Priority.HIGH),
            categories = listOf("업무"),
            isCompleted = false
        )

        // When
        val result = filterEngine.applyFilter(testReminders, filter)

        // Then
        assertEquals(2, result.size)
        assertTrue(result.all {
            it.priority == Priority.HIGH &&
                    it.category == "업무" &&
                    !it.isCompleted
        })
    }

    @Test
    fun `복합 필터 - 조건에 맞는 것이 없으면 빈 리스트 반환`() {
        // Given
        val filter = ReminderFilter(
            priorities = listOf(Priority.HIGH),
            categories = listOf("존재하지않는카테고리")
        )

        // When
        val result = filterEngine.applyFilter(testReminders, filter)

        // Then
        assertEquals(0, result.size)
        assertTrue(result.isEmpty())
    }
}
