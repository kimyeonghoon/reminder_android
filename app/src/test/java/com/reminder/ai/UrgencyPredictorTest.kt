package com.reminder.ai

import com.reminder.data.entity.Urgency
import org.junit.Test
import org.junit.Assert.*

/**
 * v1.48.0: AI 긴급도 예측 테스트
 *
 * TDD: UrgencyPredictor 클래스의 키워드 기반 긴급도 예측 로직 검증
 */
class UrgencyPredictorTest {

    private val predictor = UrgencyPredictor()

    /** 긴급 키워드가 포함된 제목은 HIGH 긴급도를 예측한다 */
    @Test
    fun predictHighUrgencyWhenTitleContainsUrgentKeyword() {
        // Given
        val title = "긴급 회의 자료 준비"
        val description = "내일까지 완료 필요"

        // When
        val predicted = predictor.predictUrgency(title, description)

        // Then
        assertEquals(Urgency.HIGH, predicted)
    }

    /** urgent 키워드는 HIGH 긴급도를 예측한다 */
    @Test
    fun predictHighUrgencyForUrgentKeyword() {
        // Given
        val title = "urgent: fix production bug"
        val description = ""

        // When
        val predicted = predictor.predictUrgency(title, description)

        // Then
        assertEquals(Urgency.HIGH, predicted)
    }

    /** asap 키워드는 HIGH 긴급도를 예측한다 */
    @Test
    fun predictHighUrgencyForAsapKeyword() {
        // Given
        val title = "Send email asap"
        val description = "Very important"

        // When
        val predicted = predictor.predictUrgency(title, description)

        // Then
        assertEquals(Urgency.HIGH, predicted)
    }

    /** 지금, 바로 키워드는 HIGH 긴급도를 예측한다 */
    @Test
    fun predictHighUrgencyForNowRightAwayKeywords() {
        // Given
        val title = "지금 바로 전화하기"
        val description = ""

        // When
        val predicted = predictor.predictUrgency(title, description)

        // Then
        assertEquals(Urgency.HIGH, predicted)
    }

    /** 오늘 키워드는 HIGH 긴급도를 예측한다 */
    @Test
    fun predictHighUrgencyForTodayKeyword() {
        // Given
        val title = "오늘 안에 보고서 제출"
        val description = ""

        // When
        val predicted = predictor.predictUrgency(title, description)

        // Then
        assertEquals(Urgency.HIGH, predicted)
    }

    /** 이번 주 키워드는 MEDIUM 긴급도를 예측한다 */
    @Test
    fun predictMediumUrgencyForThisWeekKeyword() {
        // Given
        val title = "이번 주 중 미팅 일정 잡기"
        val description = ""

        // When
        val predicted = predictor.predictUrgency(title, description)

        // Then
        assertEquals(Urgency.MEDIUM, predicted)
    }

    /** this week 키워드는 MEDIUM 긴급도를 예측한다 */
    @Test
    fun predictMediumUrgencyForThisWeekEnglishKeyword() {
        // Given
        val title = "Finish project this week"
        val description = ""

        // When
        val predicted = predictor.predictUrgency(title, description)

        // Then
        assertEquals(Urgency.MEDIUM, predicted)
    }

    /** soon 키워드는 MEDIUM 긴급도를 예측한다 */
    @Test
    fun predictMediumUrgencyForSoonKeyword() {
        // Given
        val title = "Review code soon"
        val description = ""

        // When
        val predicted = predictor.predictUrgency(title, description)

        // Then
        assertEquals(Urgency.MEDIUM, predicted)
    }

    /** 나중에 키워드는 LOW 긴급도를 예측한다 */
    @Test
    fun predictLowUrgencyForLaterKeyword() {
        // Given
        val title = "나중에 책 읽기"
        val description = ""

        // When
        val predicted = predictor.predictUrgency(title, description)

        // Then
        assertEquals(Urgency.LOW, predicted)
    }

    /** later 키워드는 LOW 긴급도를 예측한다 */
    @Test
    fun predictLowUrgencyForLaterEnglishKeyword() {
        // Given
        val title = "Do this later"
        val description = ""

        // When
        val predicted = predictor.predictUrgency(title, description)

        // Then
        assertEquals(Urgency.LOW, predicted)
    }

    /** someday 키워드는 LOW 긴급도를 예측한다 */
    @Test
    fun predictLowUrgencyForSomedayKeyword() {
        // Given
        val title = "Learn piano someday"
        val description = ""

        // When
        val predicted = predictor.predictUrgency(title, description)

        // Then
        assertEquals(Urgency.LOW, predicted)
    }

    /** 언젠가 키워드는 LOW 긴급도를 예측한다 */
    @Test
    fun predictLowUrgencyForSomedayKoreanKeyword() {
        // Given
        val title = "언젠가 여행 가기"
        val description = ""

        // When
        val predicted = predictor.predictUrgency(title, description)

        // Then
        assertEquals(Urgency.LOW, predicted)
    }

    /** 키워드가 없으면 기본값 MEDIUM을 반환한다 */
    @Test
    fun returnDefaultMediumUrgencyWhenNoKeywords() {
        // Given
        val title = "Regular task"
        val description = "Just a normal task"

        // When
        val predicted = predictor.predictUrgency(title, description)

        // Then
        assertEquals(Urgency.MEDIUM, predicted)
    }

    /** 여러 긴급도 키워드가 있으면 가장 높은 긴급도를 반환한다 */
    @Test
    fun returnHighestUrgencyWhenMultipleKeywordsExist() {
        // Given
        val title = "긴급: 나중에 처리할 문서"
        val description = "someday"

        // When
        val predicted = predictor.predictUrgency(title, description)

        // Then
        assertEquals(Urgency.HIGH, predicted) // 긴급이 우선
    }

    /** description에만 키워드가 있어도 감지한다 */
    @Test
    fun detectKeywordsInDescriptionOnly() {
        // Given
        val title = "Task"
        val description = "This is urgent and needs to be done asap"

        // When
        val predicted = predictor.predictUrgency(title, description)

        // Then
        assertEquals(Urgency.HIGH, predicted)
    }

    /** 대소문자 구분 없이 키워드를 감지한다 */
    @Test
    fun detectKeywordsCaseInsensitively() {
        // Given
        val title = "URGENT Task"
        val description = "ASAP please"

        // When
        val predicted = predictor.predictUrgency(title, description)

        // Then
        assertEquals(Urgency.HIGH, predicted)
    }

    /** 빈 문자열은 기본값 MEDIUM을 반환한다 */
    @Test
    fun returnDefaultMediumUrgencyForEmptyStrings() {
        // Given
        val title = ""
        val description = ""

        // When
        val predicted = predictor.predictUrgency(title, description)

        // Then
        assertEquals(Urgency.MEDIUM, predicted)
    }
}
