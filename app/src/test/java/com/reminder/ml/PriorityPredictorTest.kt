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
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import java.time.LocalDateTime

/**
 * PriorityPredictor 테스트
 * TDD Red 단계 - 테스트 먼저 작성
 */
class PriorityPredictorTest {

    @Mock
    private lateinit var mlDao: MLTrainingDataDao

    private lateinit var predictor: PriorityPredictor

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        predictor = PriorityPredictor(mlDao)
    }

    @Test
    fun `학습 데이터가 없을 때 기본 우선순위 MEDIUM 반환`() = runTest {
        // Given
        val title = "새로운 작업"
        val description = "처음 보는 작업"
        `when`(mlDao.findSimilarData(MLDataType.PRIORITY, "$title $description", 10))
            .thenReturn(emptyList())

        // When
        val result = predictor.predictPriority(title, description)

        // Then
        assertEquals(Priority.MEDIUM, result.priority)
        assertEquals(0.0f, result.confidence, 0.01f)
    }

    @Test
    fun `유사한 제목이 있을 때 해당 우선순위 반환`() = runTest {
        // Given
        val title = "회의 준비"
        val description = "프로젝트 회의"
        val trainingData = listOf(
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

        `when`(mlDao.findSimilarData(MLDataType.PRIORITY, "$title $description", 10))
            .thenReturn(trainingData)

        // When
        val result = predictor.predictPriority(title, description)

        // Then
        assertEquals(Priority.HIGH, result.priority)
        assertTrue(result.confidence > 0.5f)
    }

    @Test
    fun `여러 패턴이 있을 때 가장 많이 사용된 우선순위 반환`() = runTest {
        // Given
        val title = "장보기"
        val description = ""
        val trainingData = listOf(
            MLTrainingDataEntity(
                id = 1,
                dataType = MLDataType.PRIORITY,
                inputText = "장보기 우유 사기",
                outputLabel = "LOW",
                usageCount = 10,  // 가장 많이 사용됨
                confidence = 0.8f,
                createdAt = LocalDateTime.now(),
                lastUsedAt = LocalDateTime.now()
            ),
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

        `when`(mlDao.findSimilarData(MLDataType.PRIORITY, "$title $description", 10))
            .thenReturn(trainingData)

        // When
        val result = predictor.predictPriority(title, description)

        // Then
        assertEquals(Priority.LOW, result.priority)
        assertTrue(result.confidence > 0.5f)
    }

    @Test
    fun `학습 데이터 저장 시 올바른 형식으로 저장`() = runTest {
        // Given
        val title = "보고서 작성"
        val description = "월간 보고서"
        val priority = Priority.HIGH
        `when`(mlDao.insert(any())).thenReturn(1L)

        // When
        predictor.learn(title, description, priority)

        // Then
        verify(mlDao).insert(any())
    }

    @Test
    fun `신뢰도 계산 - 사용 횟수가 많을수록 높은 신뢰도`() = runTest {
        // Given
        val title = "운동"
        val description = ""
        val trainingDataHighUsage = listOf(
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

        val trainingDataLowUsage = listOf(
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

        // When
        `when`(mlDao.findSimilarData(MLDataType.PRIORITY, "$title $description", 10))
            .thenReturn(trainingDataHighUsage)
        val resultHighUsage = predictor.predictPriority(title, description)

        `when`(mlDao.findSimilarData(MLDataType.PRIORITY, "$title $description", 10))
            .thenReturn(trainingDataLowUsage)
        val resultLowUsage = predictor.predictPriority(title, description)

        // Then
        assertTrue(resultHighUsage.confidence > resultLowUsage.confidence)
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
