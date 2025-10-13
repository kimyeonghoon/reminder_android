package com.reminder.repository

import com.reminder.api.kakao.KakaoLocalApi
import com.reminder.api.kakao.KakaoPlace
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

/**
 * v1.67.0: 장소 검색 Repository
 *
 * 카카오 로컬 API를 사용하여 장소 및 주소를 검색합니다.
 * - 키워드 검색: 장소명, 업체명
 * - 주소 검색: 지번주소, 도로명주소
 */
class LocationSearchRepository(
    private val kakaoApi: KakaoLocalApi,
    private val apiKey: String
) {

    /**
     * 장소 및 주소 검색 (병렬 실행)
     *
     * @param query 검색 키워드 또는 주소
     * @return 검색 결과 리스트 (최대 5개, 실패 시 빈 리스트)
     */
    suspend fun searchPlaces(query: String): List<KakaoPlace> {
        // 빈 쿼리는 API 호출 안 함
        if (query.isBlank()) {
            return emptyList()
        }

        return try {
            val authorization = "KakaoAK $apiKey"

            // 두 API를 병렬로 호출
            coroutineScope {
                val keywordDeferred = async {
                    try {
                        kakaoApi.searchPlaces(
                            authorization = authorization,
                            query = query,
                            size = 5
                        ).documents
                    } catch (e: Exception) {
                        emptyList()
                    }
                }

                val addressDeferred = async {
                    try {
                        kakaoApi.searchAddress(
                            authorization = authorization,
                            query = query,
                            size = 5
                        ).documents.map { it.toKakaoPlace() }
                    } catch (e: Exception) {
                        emptyList()
                    }
                }

                // 두 결과를 합침 (키워드 우선, 중복 제거)
                val keywordResults = keywordDeferred.await()
                val addressResults = addressDeferred.await()

                // 중복 제거: 좌표가 같으면 중복으로 간주
                val combined = (keywordResults + addressResults)
                    .distinctBy { "${it.latitude},${it.longitude}" }
                    .take(5)  // 최대 5개

                combined
            }
        } catch (e: Exception) {
            // API 오류 시 빈 리스트 반환 (앱 크래시 방지)
            emptyList()
        }
    }
}
