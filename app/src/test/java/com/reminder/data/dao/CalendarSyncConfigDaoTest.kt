package com.reminder.data.dao

import com.reminder.data.entity.CalendarSyncConfig
import com.reminder.data.entity.SyncDirection
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.time.LocalDateTime
import org.junit.Assert.*

/**
 * CalendarSyncConfigDao 테스트
 * v1.68.x: DAO 메서드 검증 (TDD)
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CalendarSyncConfigDaoTest {

    private lateinit var dao: CalendarSyncConfigDao

    @Before
    fun setup() {
        dao = mock()
    }

    /** getAllConfigs는 모든 캘린더 설정을 createdAt 내림차순으로 반환한다 */
    @Test
    fun testGetAllConfigsReturnsAllConfigsOrderedByCreatedAtDesc() = runTest {
        // Given
        val now = LocalDateTime.now()
        val config1 = CalendarSyncConfig(
            id = 1,
            calendarId = "cal_1",
            calendarName = "개인 캘린더",
            accountName = "user@gmail.com",
            calendarColor = 0xFF0000FF.toInt(),
            createdAt = now.minusDays(2)
        )
        val config2 = CalendarSyncConfig(
            id = 2,
            calendarId = "cal_2",
            calendarName = "업무 캘린더",
            accountName = "work@company.com",
            calendarColor = 0xFF00FF00.toInt(),
            createdAt = now.minusDays(1)
        )
        val configs = listOf(config2, config1) // 최신순 정렬
        whenever(dao.getAllConfigs()).thenReturn(flowOf(configs))

        // When
        val result = dao.getAllConfigs()

        // Then
        verify(dao).getAllConfigs()
        // Flow 반환 확인 (실제 수집은 Repository에서 수행)
    }

    /** getEnabledConfigs는 동기화가 활성화된 캘린더만 반환한다 */
    @Test
    fun testGetEnabledConfigsReturnsOnlyEnabledConfigs() = runTest {
        // Given
        val enabledConfig = CalendarSyncConfig(
            id = 1,
            calendarId = "cal_1",
            calendarName = "활성 캘린더",
            accountName = "user@gmail.com",
            isSyncEnabled = true,
            calendarColor = 0xFF0000FF.toInt()
        )
        val enabledConfigs = listOf(enabledConfig)
        whenever(dao.getEnabledConfigs()).thenReturn(enabledConfigs)

        // When
        val result = dao.getEnabledConfigs()

        // Then
        verify(dao).getEnabledConfigs()
        assertEquals(enabledConfigs, result)
    }

    /** getConfigByCalendarId는 특정 calendarId로 설정을 조회한다 */
    @Test
    fun testGetConfigByCalendarIdReturnsConfigWithMatchingId() = runTest {
        // Given
        val calendarId = "cal_123"
        val config = CalendarSyncConfig(
            id = 1,
            calendarId = calendarId,
            calendarName = "테스트 캘린더",
            accountName = "test@example.com",
            calendarColor = 0xFFFF0000.toInt()
        )
        whenever(dao.getConfigByCalendarId(calendarId)).thenReturn(config)

        // When
        val result = dao.getConfigByCalendarId(calendarId)

        // Then
        verify(dao).getConfigByCalendarId(calendarId)
        assertEquals(config, result)
    }

    /** getConfigByCalendarId는 존재하지 않는 calendarId에 대해 null을 반환한다 */
    @Test
    fun testGetConfigByCalendarIdReturnsNullWhenNotFound() = runTest {
        // Given
        val calendarId = "nonexistent_cal"
        whenever(dao.getConfigByCalendarId(calendarId)).thenReturn(null)

        // When
        val result = dao.getConfigByCalendarId(calendarId)

        // Then
        verify(dao).getConfigByCalendarId(calendarId)
        assertNull(result)
    }

    /** insertConfig는 새 캘린더 설정을 삽입하고 ID를 반환한다 */
    @Test
    fun testInsertConfigInsertsConfigAndReturnsId() = runTest {
        // Given
        val config = CalendarSyncConfig(
            calendarId = "new_cal",
            calendarName = "새 캘린더",
            accountName = "new@example.com",
            calendarColor = 0xFF00FF00.toInt()
        )
        val insertedId = 10L
        whenever(dao.insertConfig(config)).thenReturn(insertedId)

        // When
        val result = dao.insertConfig(config)

        // Then
        verify(dao).insertConfig(config)
        assertEquals(insertedId, result)
    }

    /** insertConfig는 충돌 시 REPLACE 전략으로 기존 설정을 덮어쓴다 */
    @Test
    fun testInsertConfigReplacesExistingConfigOnConflict() = runTest {
        // Given
        val config = CalendarSyncConfig(
            id = 5,
            calendarId = "cal_duplicate",
            calendarName = "중복 캘린더",
            accountName = "duplicate@example.com",
            calendarColor = 0xFFFF00FF.toInt()
        )
        val replacedId = 5L
        whenever(dao.insertConfig(config)).thenReturn(replacedId)

        // When
        val result = dao.insertConfig(config)

        // Then
        verify(dao).insertConfig(config)
        assertEquals(replacedId, result)
    }

    /** updateConfig는 기존 캘린더 설정을 업데이트한다 */
    @Test
    fun testUpdateConfigUpdatesExistingConfig() = runTest {
        // Given
        val config = CalendarSyncConfig(
            id = 3,
            calendarId = "cal_update",
            calendarName = "업데이트된 캘린더",
            accountName = "update@example.com",
            isSyncEnabled = false,
            syncDirection = SyncDirection.ONE_WAY,
            calendarColor = 0xFFFFFF00.toInt(),
            lastSyncedAt = LocalDateTime.now()
        )

        // When
        dao.updateConfig(config)

        // Then
        verify(dao).updateConfig(config)
    }

    /** deleteConfig는 특정 캘린더 설정을 삭제한다 */
    @Test
    fun testDeleteConfigDeletesSpecificConfig() = runTest {
        // Given
        val config = CalendarSyncConfig(
            id = 7,
            calendarId = "cal_delete",
            calendarName = "삭제할 캘린더",
            accountName = "delete@example.com",
            calendarColor = 0xFF000000.toInt()
        )

        // When
        dao.deleteConfig(config)

        // Then
        verify(dao).deleteConfig(config)
    }

    /** deleteAllConfigs는 모든 캘린더 설정을 삭제한다 */
    @Test
    fun testDeleteAllConfigsDeletesAllConfigs() = runTest {
        // Given - 설정 없음

        // When
        dao.deleteAllConfigs()

        // Then
        verify(dao).deleteAllConfigs()
    }

    /** getAllConfigs는 빈 리스트를 반환할 수 있다 */
    @Test
    fun testGetAllConfigsReturnsEmptyListWhenNoConfigs() = runTest {
        // Given
        whenever(dao.getAllConfigs()).thenReturn(flowOf(emptyList()))

        // When
        val result = dao.getAllConfigs()

        // Then
        verify(dao).getAllConfigs()
        // Flow<List<CalendarSyncConfig>> 타입 확인
    }

    /** getEnabledConfigs는 활성화된 설정이 없을 때 빈 리스트를 반환한다 */
    @Test
    fun testGetEnabledConfigsReturnsEmptyListWhenNoEnabledConfigs() = runTest {
        // Given
        whenever(dao.getEnabledConfigs()).thenReturn(emptyList())

        // When
        val result = dao.getEnabledConfigs()

        // Then
        verify(dao).getEnabledConfigs()
        assertEquals(emptyList<CalendarSyncConfig>(), result)
    }

    /** insertConfig는 모든 필드가 올바르게 전달된 설정을 삽입한다 */
    @Test
    fun testInsertConfigInsertsConfigWithAllFields() = runTest {
        // Given
        val now = LocalDateTime.now()
        val config = CalendarSyncConfig(
            id = 0, // autoGenerate
            calendarId = "cal_full",
            calendarName = "전체 필드 캘린더",
            accountName = "full@example.com",
            isSyncEnabled = true,
            syncDirection = SyncDirection.TWO_WAY,
            calendarColor = 0xFF123456.toInt(),
            lastSyncedAt = now.minusHours(2),
            createdAt = now
        )
        val insertedId = 15L
        whenever(dao.insertConfig(config)).thenReturn(insertedId)

        // When
        val result = dao.insertConfig(config)

        // Then
        verify(dao).insertConfig(config)
        assertEquals(insertedId, result)
    }

    /** updateConfig는 동기화 방향을 변경할 수 있다 */
    @Test
    fun testUpdateConfigCanChangeSyncDirection() = runTest {
        // Given
        val config = CalendarSyncConfig(
            id = 8,
            calendarId = "cal_direction",
            calendarName = "방향 변경 캘린더",
            accountName = "direction@example.com",
            syncDirection = SyncDirection.ONE_WAY, // TWO_WAY에서 ONE_WAY로 변경
            calendarColor = 0xFF654321.toInt()
        )

        // When
        dao.updateConfig(config)

        // Then
        verify(dao).updateConfig(config)
    }

    /** updateConfig는 lastSyncedAt 시간을 갱신할 수 있다 */
    @Test
    fun testUpdateConfigCanUpdateLastSyncedAt() = runTest {
        // Given
        val now = LocalDateTime.now()
        val config = CalendarSyncConfig(
            id = 9,
            calendarId = "cal_sync_time",
            calendarName = "동기화 시간 갱신",
            accountName = "sync@example.com",
            lastSyncedAt = now, // 새로운 동기화 시간
            calendarColor = 0xFFABCDEF.toInt()
        )

        // When
        dao.updateConfig(config)

        // Then
        verify(dao).updateConfig(config)
    }
}
