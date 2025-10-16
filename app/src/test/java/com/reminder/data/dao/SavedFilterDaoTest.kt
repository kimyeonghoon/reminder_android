package com.reminder.data.dao

import com.reminder.data.entity.SavedFilterEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.time.LocalDateTime
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class SavedFilterDaoTest {

    private lateinit var dao: SavedFilterDao

    @Before
    fun setup() {
        dao = mock()
    }

    /** getAllSavedFilters는 order 순서대로 정렬된 저장된 필터 목록을 반환한다 */
    @Test
    fun testGetAllSavedFiltersReturnsOrderedFilters() = runTest {
        // Given
        val filters = listOf(
            SavedFilterEntity(
                id = 1,
                name = "오늘의 긴급 업무",
                icon = "priority_high",
                filterJson = """{"priority":"HIGH"}""",
                order = 0
            ),
            SavedFilterEntity(
                id = 2,
                name = "이번 주 할 일",
                icon = "calendar_today",
                filterJson = """{"dateRange":"THIS_WEEK"}""",
                order = 1
            )
        )
        whenever(dao.getAllSavedFilters()).thenReturn(flowOf(filters))

        // When
        val result = dao.getAllSavedFilters()

        // Then
        result.collect { list ->
            assertEquals(2, list.size)
            assertEquals("오늘의 긴급 업무", list[0].name)
            assertEquals("이번 주 할 일", list[1].name)
            assertTrue(list[0].order < list[1].order)
        }
    }

    /** getAllSavedFilters는 빈 목록을 반환할 수 있다 */
    @Test
    fun testGetAllSavedFiltersReturnsEmptyList() = runTest {
        // Given
        whenever(dao.getAllSavedFilters()).thenReturn(flowOf(emptyList()))

        // When
        val result = dao.getAllSavedFilters()

        // Then
        result.collect { list ->
            assertTrue(list.isEmpty())
        }
    }

    /** getSavedFilterById는 ID로 저장된 필터를 조회한다 */
    @Test
    fun testGetSavedFilterByIdReturnsFilterWhenExists() = runTest {
        // Given
        val filterId = 1L
        val filter = SavedFilterEntity(
            id = filterId,
            name = "긴급 업무",
            icon = "priority_high",
            filterJson = """{"priority":"HIGH"}"""
        )
        whenever(dao.getSavedFilterById(filterId)).thenReturn(filter)

        // When
        val result = dao.getSavedFilterById(filterId)

        // Then
        assertNotNull(result)
        assertEquals(filterId, result?.id)
        assertEquals("긴급 업무", result?.name)
        verify(dao).getSavedFilterById(filterId)
    }

    /** getSavedFilterById는 존재하지 않는 ID에 대해 null을 반환한다 */
    @Test
    fun testGetSavedFilterByIdReturnsNullWhenNotExists() = runTest {
        // Given
        val filterId = 999L
        whenever(dao.getSavedFilterById(filterId)).thenReturn(null)

        // When
        val result = dao.getSavedFilterById(filterId)

        // Then
        assertNull(result)
        verify(dao).getSavedFilterById(filterId)
    }

    /** insertSavedFilter는 저장된 필터를 추가하고 ID를 반환한다 */
    @Test
    fun testInsertSavedFilterInsertsFilterAndReturnsId() = runTest {
        // Given
        val filter = SavedFilterEntity(
            name = "새 필터",
            icon = "filter_list",
            filterJson = """{"category":"업무"}"""
        )
        val insertedId = 5L
        whenever(dao.insertSavedFilter(filter)).thenReturn(insertedId)

        // When
        val result = dao.insertSavedFilter(filter)

        // Then
        assertEquals(insertedId, result)
        verify(dao).insertSavedFilter(filter)
    }

    /** insertSavedFilter는 REPLACE 전략으로 동일한 ID의 필터를 대체한다 */
    @Test
    fun testInsertSavedFilterReplacesExistingFilterWithSameId() = runTest {
        // Given
        val filter = SavedFilterEntity(
            id = 1,
            name = "업데이트된 필터",
            icon = "update",
            filterJson = """{"priority":"MEDIUM"}"""
        )
        whenever(dao.insertSavedFilter(filter)).thenReturn(1L)

        // When
        val result = dao.insertSavedFilter(filter)

        // Then
        assertEquals(1L, result)
        verify(dao).insertSavedFilter(filter)
    }

    /** updateSavedFilter는 저장된 필터를 업데이트한다 */
    @Test
    fun testUpdateSavedFilterUpdatesFilterInDao() = runTest {
        // Given
        val filter = SavedFilterEntity(
            id = 1,
            name = "수정된 필터",
            icon = "edit",
            filterJson = """{"priority":"LOW"}""",
            order = 2
        )

        // When
        dao.updateSavedFilter(filter)

        // Then
        verify(dao).updateSavedFilter(filter)
    }

    /** deleteSavedFilter는 저장된 필터를 삭제한다 */
    @Test
    fun testDeleteSavedFilterDeletesFilterInDao() = runTest {
        // Given
        val filter = SavedFilterEntity(
            id = 1,
            name = "삭제할 필터",
            icon = "delete",
            filterJson = """{"category":"개인"}"""
        )

        // When
        dao.deleteSavedFilter(filter)

        // Then
        verify(dao).deleteSavedFilter(filter)
    }

    /** deleteSavedFilterById는 ID로 저장된 필터를 삭제한다 */
    @Test
    fun testDeleteSavedFilterByIdDeletesFilterByIdInDao() = runTest {
        // Given
        val filterId = 1L

        // When
        dao.deleteSavedFilterById(filterId)

        // Then
        verify(dao).deleteSavedFilterById(filterId)
    }

    /** deleteAllSavedFilters는 모든 저장된 필터를 삭제한다 */
    @Test
    fun testDeleteAllSavedFiltersDeletesAllFiltersInDao() = runTest {
        // When
        dao.deleteAllSavedFilters()

        // Then
        verify(dao).deleteAllSavedFilters()
    }

    /** getCount는 저장된 필터의 개수를 반환한다 */
    @Test
    fun testGetCountReturnsFilterCount() = runTest {
        // Given
        val count = 5
        whenever(dao.getCount()).thenReturn(count)

        // When
        val result = dao.getCount()

        // Then
        assertEquals(count, result)
        verify(dao).getCount()
    }

    /** getCount는 필터가 없을 때 0을 반환한다 */
    @Test
    fun testGetCountReturnsZeroWhenNoFilters() = runTest {
        // Given
        whenever(dao.getCount()).thenReturn(0)

        // When
        val result = dao.getCount()

        // Then
        assertEquals(0, result)
        verify(dao).getCount()
    }

    /** insertSavedFilter는 타임스탬프가 자동으로 설정된 필터를 삽입한다 */
    @Test
    fun testInsertSavedFilterWithDefaultTimestamp() = runTest {
        // Given
        val now = LocalDateTime.now()
        val filter = SavedFilterEntity(
            name = "타임스탬프 테스트",
            icon = "schedule",
            filterJson = """{"category":"테스트"}""",
            createdAt = now
        )
        whenever(dao.insertSavedFilter(filter)).thenReturn(1L)

        // When
        val result = dao.insertSavedFilter(filter)

        // Then
        assertEquals(1L, result)
        verify(dao).insertSavedFilter(argThat { f ->
            f.name == "타임스탬프 테스트" &&
            f.createdAt == now
        })
    }

    /** getAllSavedFilters는 같은 order 값일 때 createdAt 역순으로 정렬된다 */
    @Test
    fun testGetAllSavedFiltersSortsByCreatedAtDescWhenOrderIsSame() = runTest {
        // Given
        val now = LocalDateTime.now()
        val filters = listOf(
            SavedFilterEntity(
                id = 1,
                name = "최신 필터",
                icon = "new_releases",
                filterJson = """{}""",
                createdAt = now,
                order = 0
            ),
            SavedFilterEntity(
                id = 2,
                name = "오래된 필터",
                icon = "history",
                filterJson = """{}""",
                createdAt = now.minusDays(1),
                order = 0
            )
        )
        whenever(dao.getAllSavedFilters()).thenReturn(flowOf(filters))

        // When
        val result = dao.getAllSavedFilters()

        // Then
        result.collect { list ->
            assertEquals(2, list.size)
            assertEquals("최신 필터", list[0].name)
            assertEquals("오래된 필터", list[1].name)
        }
    }
}
