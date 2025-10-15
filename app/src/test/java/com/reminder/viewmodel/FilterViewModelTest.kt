package com.reminder.viewmodel

import com.reminder.analytics.AnalyticsHelper
import com.reminder.data.dao.SavedFilterDao
import com.reminder.data.entity.Priority
import com.reminder.data.entity.ReminderEntity
import com.reminder.data.entity.SavedFilterEntity
import com.reminder.filter.FilterEngine
import com.reminder.filter.ReminderFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.whenever

/**
 * FilterViewModel 테스트
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FilterViewModelTest {

    @get:Rule
    val mockitoRule: MockitoRule = MockitoJUnit.rule()

    private val testDispatcher = UnconfinedTestDispatcher()

    @Mock
    private lateinit var savedFilterDao: SavedFilterDao

    @Mock
    private lateinit var analyticsHelper: AnalyticsHelper

    private lateinit var viewModel: FilterViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        // FilterViewModel 초기화 시 savedFilters StateFlow를 위한 Mock 설정
        whenever(savedFilterDao.getAllSavedFilters()).thenReturn(flowOf(emptyList()))
        viewModel = FilterViewModel(savedFilterDao, analyticsHelper)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `savedFilters StateFlow is initialized`() {
        // given: FilterViewModel이 초기화됨 (setup에서)

        // when: savedFilters 접근
        val filters = viewModel.savedFilters.value

        // then: 빈 리스트로 초기화됨 (setup Mock)
        assert(filters.isEmpty())
    }

    @Test
    fun `applyFilter sets current filter`() {
        // given: 필터
        val filter = ReminderFilter(priorities = listOf(Priority.HIGH))

        // when: applyFilter 호출
        viewModel.applyFilter(filter)

        // then: currentFilter가 설정되고 analytics 로깅
        assert(viewModel.currentFilter.value == filter)
        verify(analyticsHelper).logFilterApplied()
    }

    @Test
    fun `clearFilter resets current filter to null`() {
        // given: 필터가 적용된 상태
        val filter = ReminderFilter(priorities = listOf(Priority.HIGH))
        viewModel.applyFilter(filter)

        // when: clearFilter 호출
        viewModel.clearFilter()

        // then: currentFilter가 null이 되고 analytics 로깅
        assert(viewModel.currentFilter.value == null)
        verify(analyticsHelper).logFilterCleared()
    }

    @Test
    fun `getFilteredRemindersWithFilter returns filtered list`() {
        // given: 리마인더 리스트와 필터
        val reminders = listOf(
            ReminderEntity(id = 1, title = "High Priority", priority = Priority.HIGH),
            ReminderEntity(id = 2, title = "Medium Priority", priority = Priority.MEDIUM),
            ReminderEntity(id = 3, title = "Low Priority", priority = Priority.LOW)
        )
        val filter = ReminderFilter(priorities = listOf(Priority.HIGH))
        viewModel.applyFilter(filter)

        // when: getFilteredRemindersWithFilter 호출
        val result = viewModel.getFilteredRemindersWithFilter(reminders)

        // then: HIGH priority 리마인더만 반환
        assert(result.size == 1)
        assert(result[0].priority == Priority.HIGH)
    }

    @Test
    fun `getFilteredRemindersWithFilter returns all when no filter`() {
        // given: 리마인더 리스트 (필터 없음)
        val reminders = listOf(
            ReminderEntity(id = 1, title = "High Priority", priority = Priority.HIGH),
            ReminderEntity(id = 2, title = "Medium Priority", priority = Priority.MEDIUM)
        )

        // when: getFilteredRemindersWithFilter 호출
        val result = viewModel.getFilteredRemindersWithFilter(reminders)

        // then: 모든 리마인더 반환
        assert(result.size == 2)
    }

    @Ignore("viewModelScope 테스트 환경 이슈 - 추후 수정 필요")
    @Test
    fun `saveFilter inserts saved filter`() = runBlocking {
        // given: 필터 정보
        val name = "My Filter"
        val icon = "🔴"
        val filter = ReminderFilter(priorities = listOf(Priority.HIGH))

        // when: saveFilter 호출
        viewModel.saveFilter(name, icon, filter)
        delay(1000) // 코루틴 완료 대기

        // then: dao insert 메서드 호출 및 analytics 로깅
        val captor = argumentCaptor<SavedFilterEntity>()
        verify(savedFilterDao).insertSavedFilter(captor.capture())
        val savedFilter = captor.firstValue

        assert(savedFilter.name == name)
        assert(savedFilter.icon == icon)
        assert(savedFilter.filterJson.isNotEmpty())
        verify(analyticsHelper).logFilterSaved()
    }

    @Ignore("viewModelScope 테스트 환경 이슈 - 추후 수정 필요")
    @Test
    fun `deleteSavedFilter deletes filter from dao`() = runBlocking {
        // given: 저장된 필터
        val filter = SavedFilterEntity(id = 1, name = "Filter 1", icon = "🔴", filterJson = "{}")

        // when: deleteSavedFilter 호출
        viewModel.deleteSavedFilter(filter)
        delay(1000) // 코루틴 완료 대기

        // then: dao delete 메서드 호출
        verify(savedFilterDao).deleteSavedFilter(filter)
    }
}
