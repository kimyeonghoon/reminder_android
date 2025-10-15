package com.reminder.ml

import com.reminder.data.dao.MLTrainingDataDao
import com.reminder.data.entity.MLDataType
import com.reminder.data.entity.MLTrainingDataEntity
import com.reminder.data.entity.Priority
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

/**
 * PriorityPredictor 테스트
 * TDD Red 단계 - 테스트 먼저 작성
 *
 * Fake 구현체 사용으로 Mockito suspend 함수 문제 해결
 */
class PriorityPredictorTest {

    private lateinit var mlDao: FakeMLTrainingDataDao
    private lateinit var predictor: PriorityPredictor

    @Before
    fun setup() {
        mlDao = FakeMLTrainingDataDao()
        predictor = PriorityPredictor(mlDao)
    }

    /** 학습 데이터가 없을 때 기본 우선순위 MEDIUM 반환 */
    @Test
    fun returnDefaultMediumPriorityWhenNoTrainingData() = runTest {
        // Given
        val title = "새로운 작업"
        val description = "처음 보는 작업"
        // 데이터 없음 - 기본 상태

        // When
        val result = predictor.predictPriority(title, description)

        // Then
        assertEquals(Priority.MEDIUM, result.priority)
        assertEquals(0.0f, result.confidence, 0.01f)
    }

    /** 유사한 제목이 있을 때 해당 우선순위 반환 */
    @Test
    fun returnMatchingPriorityWhenSimilarTitleExists() = runTest {
        // Given
        val title = "회의 준비"
        val description = "프로젝트 회의"
        mlDao.addTrainingData(
            MLTrainingDataEntity(
                id = 1,
                dataType = MLDataType.PRIORITY,
                inputText = "회의 준비 자료 작성",
                outputLabel = "HIGH",
                usageCount = 5,
                confidence = 0.9f,
                createdAt = LocalDateTime.now(),
                lastUsedAt = LocalDateTime.now()
            )
        )

        // When
        val result = predictor.predictPriority(title, description)

        // Then
        assertEquals(Priority.HIGH, result.priority)
        assertTrue(result.confidence > 0.5f)
    }

    /** 여러 패턴이 있을 때 가장 많이 사용된 우선순위 반환 */
    @Test
    fun returnMostUsedPriorityWhenMultiplePatternsExist() = runTest {
        // Given
        val title = "장보기"
        val description = ""
        mlDao.addTrainingData(
            MLTrainingDataEntity(
                id = 1,
                dataType = MLDataType.PRIORITY,
                inputText = "장보기 우유 사기",
                outputLabel = "LOW",
                usageCount = 10,  // 가장 많이 사용됨
                confidence = 0.8f,
                createdAt = LocalDateTime.now(),
                lastUsedAt = LocalDateTime.now()
            )
        )
        mlDao.addTrainingData(
            MLTrainingDataEntity(
                id = 2,
                dataType = MLDataType.PRIORITY,
                inputText = "장보기 리스트 작성",
                outputLabel = "MEDIUM",
                usageCount = 3,
                confidence = 0.6f,
                createdAt = LocalDateTime.now(),
                lastUsedAt = LocalDateTime.now()
            )
        )

        // When
        val result = predictor.predictPriority(title, description)

        // Then
        assertEquals(Priority.LOW, result.priority)
        assertTrue(result.confidence > 0.5f)
    }

    /** 학습 데이터 저장 시 올바른 형식으로 저장 */
    @Test
    fun saveTrainingDataInCorrectFormat() = runTest {
        // Given
        val title = "보고서 작성"
        val description = "월간 보고서"
        val priority = Priority.HIGH

        // When
        predictor.learn(title, description, priority)

        // Then
        val inserted = mlDao.getAllTestData()
        assertEquals(1, inserted.size)
        assertTrue(inserted[0].inputText.contains(title))
        assertEquals("HIGH", inserted[0].outputLabel)
    }

    /** 신뢰도 계산 - 사용 횟수가 많을수록 높은 신뢰도 */
    @Test
    fun calculateConfidence_HigherUsageCountGivesHigherConfidence() = runTest {
        // Given
        val title = "운동"
        val description = ""
        mlDao.addTrainingData(
            MLTrainingDataEntity(
                id = 1,
                dataType = MLDataType.PRIORITY,
                inputText = "운동하기",
                outputLabel = "MEDIUM",
                usageCount = 20,  // 높은 사용 횟수
                confidence = 0.9f,
                createdAt = LocalDateTime.now(),
                lastUsedAt = LocalDateTime.now()
            )
        )

        // When
        val resultHighUsage = predictor.predictPriority(title, description)

        // 데이터 변경
        mlDao.clearData()
        mlDao.addTrainingData(
            MLTrainingDataEntity(
                id = 2,
                dataType = MLDataType.PRIORITY,
                inputText = "운동하기",
                outputLabel = "MEDIUM",
                usageCount = 2,  // 낮은 사용 횟수
                confidence = 0.9f,
                createdAt = LocalDateTime.now(),
                lastUsedAt = LocalDateTime.now()
            )
        )
        val resultLowUsage = predictor.predictPriority(title, description)

        // Then
        assertTrue(resultHighUsage.confidence > resultLowUsage.confidence)
    }
}

/**
 * 테스트용 Fake MLTrainingDataDao
 * Mockito suspend 함수 문제를 우회하기 위한 in-memory 구현체
 */
class FakeMLTrainingDataDao : MLTrainingDataDao {
    private val data = mutableListOf<MLTrainingDataEntity>()
    private var nextId = 1L

    fun addTrainingData(entity: MLTrainingDataEntity) {
        data.add(entity)
    }

    fun clearData() {
        data.clear()
    }

    fun getAllTestData() = data.toList()

    override suspend fun insert(data: MLTrainingDataEntity): Long {
        val id = nextId++
        this.data.add(data.copy(id = id))
        return id
    }

    override suspend fun update(data: MLTrainingDataEntity) {
        val index = this.data.indexOfFirst { it.id == data.id }
        if (index >= 0) {
            this.data[index] = data
        }
    }

    override fun getDataByType(dataType: MLDataType) = kotlinx.coroutines.flow.flowOf(emptyList<MLTrainingDataEntity>())

    override suspend fun findSimilarData(
        dataType: MLDataType,
        searchText: String,
        limit: Int
    ): List<MLTrainingDataEntity> {
        return data
            .filter { it.dataType == dataType }
            .filter { entity ->
                // 단어 단위로 매칭 (더 관대한 검색)
                val searchWords = searchText.split(" ", "\n", "\t").filter { it.isNotBlank() }
                val inputWords = entity.inputText.split(" ", "\n", "\t").filter { it.isNotBlank() }
                searchWords.any { searchWord ->
                    inputWords.any { inputWord ->
                        searchWord.contains(inputWord, ignoreCase = true) || inputWord.contains(searchWord, ignoreCase = true)
                    }
                }
            }
            .sortedByDescending { it.usageCount }
            .take(limit)
    }

    override suspend fun getDataByTypeAndCategory(dataType: MLDataType, category: String) = emptyList<MLTrainingDataEntity>()

    override suspend fun getNotificationTimeByDayOfWeek(dayOfWeek: Int, limit: Int) = emptyList<MLTrainingDataEntity>()

    override suspend fun incrementUsageCount(id: Long, currentTime: String) {
        val index = data.indexOfFirst { it.id == id }
        if (index >= 0) {
            val entity = data[index]
            data[index] = entity.copy(usageCount = entity.usageCount + 1)
        }
    }

    override suspend fun deleteLowConfidenceData(threshold: Float) = 0

    override suspend fun deleteOldData(thresholdDate: String) = 0

    override fun getAllData() = kotlinx.coroutines.flow.flowOf(data.toList())

    override suspend fun getDataCount(dataType: MLDataType) = data.count { it.dataType == dataType }

    override suspend fun deleteAll() {
        data.clear()
    }
}

/**
 * 우선순위 예측 결과 데이터 클래스
 */
data class PriorityPrediction(
    val priority: Priority,
    val confidence: Float,  // 0.0 ~ 1.0
    val reason: String = "" // 예측 이유 (디버깅용)
)
