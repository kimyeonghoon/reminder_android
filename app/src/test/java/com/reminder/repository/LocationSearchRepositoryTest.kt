package com.reminder.repository

import com.reminder.api.kakao.KakaoLocalApi
import com.reminder.api.kakao.KakaoPlace
import com.reminder.api.kakao.KakaoPlaceResponse
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * v1.67.0: 장소 검색 Repository 테스트
 *
 * TDD Red 단계: 실패하는 테스트 먼저 작성
 */
class LocationSearchRepositoryTest {

    private lateinit var kakaoApi: KakaoLocalApi
    private lateinit var repository: LocationSearchRepository

    @Before
    fun setup() {
        kakaoApi = mock()
        repository = LocationSearchRepository(kakaoApi, "test_api_key")
    }

    /**
     * 장소 검색이 성공하면 결과 반환
     */
    @Test
    fun searchPlaces_withValidQuery_returnsResults() = runTest {
        // Given
        val query = "스타벅스"
        val mockResponse = KakaoPlaceResponse(
            documents = listOf(
                KakaoPlace(
                    placeName = "스타벅스 강남점",
                    addressName = "서울 강남구 강남대로 123",
                    longitude = "127.028",
                    latitude = "37.498"
                )
            )
        )
        whenever(kakaoApi.searchPlaces(any(), any(), any())).thenReturn(mockResponse)

        // When
        val result = repository.searchPlaces(query)

        // Then
        assertEquals(1, result.size)
        assertEquals("스타벅스 강남점", result[0].placeName)
        assertEquals("서울 강남구 강남대로 123", result[0].addressName)
        assertEquals("127.028", result[0].longitude)
        assertEquals("37.498", result[0].latitude)
    }

    /**
     * 검색 결과가 없으면 빈 리스트 반환
     */
    @Test
    fun searchPlaces_withNoResults_returnsEmptyList() = runTest {
        // Given
        val query = "존재하지않는장소"
        val emptyResponse = KakaoPlaceResponse(documents = emptyList())
        whenever(kakaoApi.searchPlaces(any(), any(), any())).thenReturn(emptyResponse)

        // When
        val result = repository.searchPlaces(query)

        // Then
        assertTrue(result.isEmpty())
    }

    /**
     * 빈 쿼리는 빈 리스트 반환 (API 호출 안 함)
     */
    @Test
    fun searchPlaces_withEmptyQuery_returnsEmptyList() = runTest {
        // Given
        val query = ""

        // When
        val result = repository.searchPlaces(query)

        // Then
        assertTrue(result.isEmpty())
    }

    /**
     * 공백 쿼리는 빈 리스트 반환 (API 호출 안 함)
     */
    @Test
    fun searchPlaces_withBlankQuery_returnsEmptyList() = runTest {
        // Given
        val query = "   "

        // When
        val result = repository.searchPlaces(query)

        // Then
        assertTrue(result.isEmpty())
    }

    /**
     * API 호출 실패 시 빈 리스트 반환
     */
    @Test
    fun searchPlaces_whenApiThrowsException_returnsEmptyList() = runTest {
        // Given
        val query = "스타벅스"
        whenever(kakaoApi.searchPlaces(any(), any(), any()))
            .thenThrow(RuntimeException("Network error"))

        // When
        val result = repository.searchPlaces(query)

        // Then
        assertTrue(result.isEmpty())
    }
}
