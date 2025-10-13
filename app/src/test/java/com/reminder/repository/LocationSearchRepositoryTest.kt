package com.reminder.repository

import com.reminder.api.kakao.*
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
        val mockPlaceResponse = KakaoPlaceResponse(
            documents = listOf(
                KakaoPlace(
                    placeName = "스타벅스 강남점",
                    addressName = "서울 강남구 강남대로 123",
                    longitude = "127.028",
                    latitude = "37.498"
                )
            )
        )
        val mockAddressResponse = KakaoAddressResponse(documents = emptyList())

        whenever(kakaoApi.searchPlaces(any(), any(), any())).thenReturn(mockPlaceResponse)
        whenever(kakaoApi.searchAddress(any(), any(), any())).thenReturn(mockAddressResponse)

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
        val emptyPlaceResponse = KakaoPlaceResponse(documents = emptyList())
        val emptyAddressResponse = KakaoAddressResponse(documents = emptyList())

        whenever(kakaoApi.searchPlaces(any(), any(), any())).thenReturn(emptyPlaceResponse)
        whenever(kakaoApi.searchAddress(any(), any(), any())).thenReturn(emptyAddressResponse)

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
        whenever(kakaoApi.searchAddress(any(), any(), any()))
            .thenThrow(RuntimeException("Network error"))

        // When
        val result = repository.searchPlaces(query)

        // Then
        assertTrue(result.isEmpty())
    }

    /**
     * 주소 검색 결과 반환 확인
     */
    @Test
    fun searchPlaces_withAddress_returnsAddressResults() = runTest {
        // Given
        val query = "서울 강남구 역삼동 123-45"
        val mockPlaceResponse = KakaoPlaceResponse(documents = emptyList())
        val mockAddressResponse = KakaoAddressResponse(
            documents = listOf(
                KakaoAddress(
                    addressName = "서울 강남구 역삼동 123-45",
                    addressType = "REGION_ADDR",
                    longitude = "127.028",
                    latitude = "37.498",
                    roadAddress = null
                )
            )
        )

        whenever(kakaoApi.searchPlaces(any(), any(), any())).thenReturn(mockPlaceResponse)
        whenever(kakaoApi.searchAddress(any(), any(), any())).thenReturn(mockAddressResponse)

        // When
        val result = repository.searchPlaces(query)

        // Then
        assertEquals(1, result.size)
        assertEquals("서울 강남구 역삼동 123-45", result[0].placeName)
        assertEquals("127.028", result[0].longitude)
        assertEquals("37.498", result[0].latitude)
    }

    /**
     * 장소 검색과 주소 검색 결과 합치기 확인
     */
    @Test
    fun searchPlaces_combinesPlaceAndAddressResults() = runTest {
        // Given
        val query = "강남"
        val mockPlaceResponse = KakaoPlaceResponse(
            documents = listOf(
                KakaoPlace(
                    placeName = "강남역",
                    addressName = "서울 강남구 역삼동",
                    longitude = "127.028",
                    latitude = "37.498"
                )
            )
        )
        val mockAddressResponse = KakaoAddressResponse(
            documents = listOf(
                KakaoAddress(
                    addressName = "서울 강남구 역삼동 123",
                    addressType = "REGION_ADDR",
                    longitude = "127.030",
                    latitude = "37.500",
                    roadAddress = null
                )
            )
        )

        whenever(kakaoApi.searchPlaces(any(), any(), any())).thenReturn(mockPlaceResponse)
        whenever(kakaoApi.searchAddress(any(), any(), any())).thenReturn(mockAddressResponse)

        // When
        val result = repository.searchPlaces(query)

        // Then
        assertEquals(2, result.size)
        // 키워드 검색 결과가 먼저
        assertEquals("강남역", result[0].placeName)
        assertEquals("서울 강남구 역삼동 123", result[1].placeName)
    }
}
