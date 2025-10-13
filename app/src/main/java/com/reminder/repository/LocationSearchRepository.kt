package com.reminder.repository

import com.reminder.api.kakao.KakaoLocalApi
import com.reminder.api.kakao.KakaoPlace

/**
 * v1.67.0: 장소 검색 Repository
 *
 * 카카오 로컬 API를 사용하여 장소를 검색합니다.
 */
class LocationSearchRepository(
    private val kakaoApi: KakaoLocalApi,
    private val apiKey: String
) {

    /**
     * 장소 검색
     *
     * @param query 검색 키워드 (예: "스타벅스 강남")
     * @return 검색 결과 리스트 (실패 시 빈 리스트)
     */
    suspend fun searchPlaces(query: String): List<KakaoPlace> {
        // 빈 쿼리는 API 호출 안 함
        if (query.isBlank()) {
            return emptyList()
        }

        return try {
            val authorization = "KakaoAK $apiKey"
            val response = kakaoApi.searchPlaces(
                authorization = authorization,
                query = query,
                size = 5
            )
            response.documents
        } catch (e: Exception) {
            // API 오류 시 빈 리스트 반환 (앱 크래시 방지)
            // Note: 프로덕션에서는 로깅 추가 가능
            emptyList()
        }
    }
}
