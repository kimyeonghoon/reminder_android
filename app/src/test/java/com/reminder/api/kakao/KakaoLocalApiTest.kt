package com.reminder.api.kakao

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * v1.67.0: 카카오 로컬 API 테스트
 *
 * TDD Red 단계: 실패하는 테스트 먼저 작성
 */
class KakaoLocalApiTest {

    private lateinit var api: KakaoLocalApi

    @Before
    fun setup() {
        api = mock()
    }

    /**
     * 장소 검색 시 응답 파싱 확인
     */
    @Test
    fun searchPlaces_returnsValidResponse() = runTest {
        // Given
        val query = "스타벅스 강남"
        val expectedResponse = KakaoPlaceResponse(
            documents = listOf(
                KakaoPlace(
                    placeName = "스타벅스 강남점",
                    addressName = "서울 강남구 강남대로 123",
                    longitude = "127.028",
                    latitude = "37.498"
                )
            )
        )

        whenever(
            api.searchPlaces(
                authorization = "KakaoAK test_key",
                query = query,
                size = 5
            )
        ).thenReturn(expectedResponse)

        // When
        val result = api.searchPlaces(
            authorization = "KakaoAK test_key",
            query = query
        )

        // Then
        assertEquals(1, result.documents.size)
        assertEquals("스타벅스 강남점", result.documents[0].placeName)
        assertEquals("서울 강남구 강남대로 123", result.documents[0].addressName)
        assertEquals("127.028", result.documents[0].longitude)
        assertEquals("37.498", result.documents[0].latitude)
    }

    /**
     * 빈 검색 결과 처리
     */
    @Test
    fun searchPlaces_withNoResults_returnsEmptyList() = runTest {
        // Given
        val query = "존재하지않는장소12345"
        val emptyResponse = KakaoPlaceResponse(documents = emptyList())

        whenever(
            api.searchPlaces(
                authorization = "KakaoAK test_key",
                query = query,
                size = 5
            )
        ).thenReturn(emptyResponse)

        // When
        val result = api.searchPlaces(
            authorization = "KakaoAK test_key",
            query = query
        )

        // Then
        assertEquals(0, result.documents.size)
    }

    /**
     * 여러 검색 결과 반환 확인
     */
    @Test
    fun searchPlaces_withMultipleResults_returnsAllResults() = runTest {
        // Given
        val query = "스타벅스"
        val multipleResponse = KakaoPlaceResponse(
            documents = listOf(
                KakaoPlace("스타벅스 강남점", "서울 강남구", "127.0", "37.5"),
                KakaoPlace("스타벅스 역삼점", "서울 강남구", "127.1", "37.4"),
                KakaoPlace("스타벅스 삼성점", "서울 강남구", "127.2", "37.3")
            )
        )

        whenever(
            api.searchPlaces(
                authorization = "KakaoAK test_key",
                query = query,
                size = 5
            )
        ).thenReturn(multipleResponse)

        // When
        val result = api.searchPlaces(
            authorization = "KakaoAK test_key",
            query = query
        )

        // Then
        assertEquals(3, result.documents.size)
        assertEquals("스타벅스 강남점", result.documents[0].placeName)
        assertEquals("스타벅스 역삼점", result.documents[1].placeName)
        assertEquals("스타벅스 삼성점", result.documents[2].placeName)
    }
}
