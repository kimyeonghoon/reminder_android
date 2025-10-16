package com.reminder.data.dao

import com.reminder.data.entity.MLDataType
import com.reminder.data.entity.MLTrainingDataEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.time.LocalDateTime
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class MLTrainingDataDaoTest {

    private lateinit var dao: MLTrainingDataDao

    @Before
    fun setup() {
        dao = mock()
    }

    /** insert는 ML 학습 데이터를 삽입하고 ID를 반환한다 */
    @Test
    fun testInsertInsertsDataAndReturnsId() = runTest {
        // Given
        val trainingData = MLTrainingDataEntity(
            dataType = MLDataType.PRIORITY,
            inputText = "회의 준비",
            outputLabel = "HIGH"
        )
        val insertedId = 1L
        whenever(dao.insert(trainingData)).thenReturn(insertedId)

        // When
        val result = dao.insert(trainingData)

        // Then
        verify(dao).insert(trainingData)
        assertEquals(insertedId, result)
    }

    /** update는 ML 학습 데이터를 업데이트한다 */
    @Test
    fun testUpdateUpdatesTrainingData() = runTest {
        // Given
        val trainingData = MLTrainingDataEntity(
            id = 1L,
            dataType = MLDataType.CATEGORY,
            inputText = "장보기",
            outputLabel = "개인",
            confidence = 0.8f
        )

        // When
        dao.update(trainingData)

        // Then
        verify(dao).update(trainingData)
    }

    /** getDataByType은 특정 타입의 ML 학습 데이터를 조회한다 */
    @Test
    fun testGetDataByTypeReturnsDataByType() = runTest {
        // Given
        val dataType = MLDataType.PRIORITY
        val trainingDataList = listOf(
            MLTrainingDataEntity(
                id = 1L,
                dataType = dataType,
                inputText = "긴급 회의",
                outputLabel = "HIGH"
            ),
            MLTrainingDataEntity(
                id = 2L,
                dataType = dataType,
                inputText = "이메일 확인",
                outputLabel = "LOW"
            )
        )
        whenever(dao.getDataByType(dataType)).thenReturn(flowOf(trainingDataList))

        // When
        dao.getDataByType(dataType)

        // Then
        verify(dao).getDataByType(dataType)
    }

    /** findSimilarData는 유사한 입력 텍스트를 가진 학습 데이터를 조회한다 */
    @Test
    fun testFindSimilarDataReturnsMatchingData() = runTest {
        // Given
        val dataType = MLDataType.CATEGORY
        val searchText = "회의"
        val limit = 5
        val similarData = listOf(
            MLTrainingDataEntity(
                id = 1L,
                dataType = dataType,
                inputText = "팀 회의 준비",
                outputLabel = "업무",
                usageCount = 10
            ),
            MLTrainingDataEntity(
                id = 2L,
                dataType = dataType,
                inputText = "주간 회의",
                outputLabel = "업무",
                usageCount = 8
            )
        )
        whenever(dao.findSimilarData(dataType, searchText, limit)).thenReturn(similarData)

        // When
        val result = dao.findSimilarData(dataType, searchText, limit)

        // Then
        verify(dao).findSimilarData(dataType, searchText, limit)
        assertEquals(similarData, result)
    }

    /** getDataByTypeAndCategory는 타입과 카테고리로 학습 데이터를 조회한다 */
    @Test
    fun testGetDataByTypeAndCategoryReturnsFilteredData() = runTest {
        // Given
        val dataType = MLDataType.DUE_DATE
        val category = "업무"
        val categoryData = listOf(
            MLTrainingDataEntity(
                id = 1L,
                dataType = dataType,
                inputText = "보고서 작성",
                outputLabel = "3",
                category = category
            ),
            MLTrainingDataEntity(
                id = 2L,
                dataType = dataType,
                inputText = "프레젠테이션 준비",
                outputLabel = "7",
                category = category
            )
        )
        whenever(dao.getDataByTypeAndCategory(dataType, category)).thenReturn(categoryData)

        // When
        val result = dao.getDataByTypeAndCategory(dataType, category)

        // Then
        verify(dao).getDataByTypeAndCategory(dataType, category)
        assertEquals(categoryData, result)
    }

    /** getNotificationTimeByDayOfWeek는 요일별 알림 시간 학습 데이터를 조회한다 */
    @Test
    fun testGetNotificationTimeByDayOfWeekReturnsDataForDay() = runTest {
        // Given
        val dayOfWeek = 1 // 월요일
        val limit = 3
        val notificationData = listOf(
            MLTrainingDataEntity(
                id = 1L,
                dataType = MLDataType.NOTIFICATION_TIME,
                inputText = "아침 운동",
                outputLabel = "07:00",
                dayOfWeek = dayOfWeek,
                usageCount = 15,
                confidence = 0.9f
            ),
            MLTrainingDataEntity(
                id = 2L,
                dataType = MLDataType.NOTIFICATION_TIME,
                inputText = "출근 준비",
                outputLabel = "08:00",
                dayOfWeek = dayOfWeek,
                usageCount = 12,
                confidence = 0.85f
            )
        )
        whenever(dao.getNotificationTimeByDayOfWeek(dayOfWeek, limit)).thenReturn(notificationData)

        // When
        val result = dao.getNotificationTimeByDayOfWeek(dayOfWeek, limit)

        // Then
        verify(dao).getNotificationTimeByDayOfWeek(dayOfWeek, limit)
        assertEquals(notificationData, result)
    }

    /** incrementUsageCount는 사용 횟수를 증가시키고 마지막 사용 시각을 갱신한다 */
    @Test
    fun testIncrementUsageCountUpdatesCountAndTime() = runTest {
        // Given
        val dataId = 1L
        val currentTime = LocalDateTime.now().toString()

        // When
        dao.incrementUsageCount(dataId, currentTime)

        // Then
        verify(dao).incrementUsageCount(dataId, currentTime)
    }

    /** deleteLowConfidenceData는 낮은 신뢰도 데이터를 삭제하고 삭제 개수를 반환한다 */
    @Test
    fun testDeleteLowConfidenceDataDeletesAndReturnsCount() = runTest {
        // Given
        val threshold = 0.5f
        val deletedCount = 3
        whenever(dao.deleteLowConfidenceData(threshold)).thenReturn(deletedCount)

        // When
        val result = dao.deleteLowConfidenceData(threshold)

        // Then
        verify(dao).deleteLowConfidenceData(threshold)
        assertEquals(deletedCount, result)
    }

    /** deleteOldData는 지정된 날짜 이전의 오래된 데이터를 삭제한다 */
    @Test
    fun testDeleteOldDataDeletesDataBeforeThreshold() = runTest {
        // Given
        val thresholdDate = LocalDateTime.now().minusDays(90).toString()
        val deletedCount = 5
        whenever(dao.deleteOldData(thresholdDate)).thenReturn(deletedCount)

        // When
        val result = dao.deleteOldData(thresholdDate)

        // Then
        verify(dao).deleteOldData(thresholdDate)
        assertEquals(deletedCount, result)
    }

    /** getAllData는 모든 ML 학습 데이터를 조회한다 */
    @Test
    fun testGetAllDataReturnsAllTrainingData() = runTest {
        // Given
        val allData = listOf(
            MLTrainingDataEntity(
                id = 1L,
                dataType = MLDataType.PRIORITY,
                inputText = "작업 1",
                outputLabel = "HIGH"
            ),
            MLTrainingDataEntity(
                id = 2L,
                dataType = MLDataType.CATEGORY,
                inputText = "작업 2",
                outputLabel = "업무"
            ),
            MLTrainingDataEntity(
                id = 3L,
                dataType = MLDataType.DUE_DATE,
                inputText = "작업 3",
                outputLabel = "3"
            )
        )
        whenever(dao.getAllData()).thenReturn(flowOf(allData))

        // When
        dao.getAllData()

        // Then
        verify(dao).getAllData()
    }

    /** getDataCount는 특정 타입의 데이터 개수를 반환한다 */
    @Test
    fun testGetDataCountReturnsCountByType() = runTest {
        // Given
        val dataType = MLDataType.NOTIFICATION_TIME
        val count = 42
        whenever(dao.getDataCount(dataType)).thenReturn(count)

        // When
        val result = dao.getDataCount(dataType)

        // Then
        verify(dao).getDataCount(dataType)
        assertEquals(count, result)
    }

    /** deleteAll은 모든 ML 학습 데이터를 삭제한다 */
    @Test
    fun testDeleteAllDeletesAllTrainingData() = runTest {
        // When
        dao.deleteAll()

        // Then
        verify(dao).deleteAll()
    }
}
