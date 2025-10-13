package com.reminder.api.kakao

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

/**
 * v1.67.0: 카카오 로컬 API 인터페이스
 *
 * 문서: https://developers.kakao.com/docs/latest/ko/local/dev-guide
 */
interface KakaoLocalApi {

    /**
     * 키워드로 장소 검색
     *
     * @param authorization REST API 키 ("KakaoAK {REST_API_KEY}")
     * @param query 검색 키워드 (예: "스타벅스 강남")
     * @param size 검색 결과 개수 (기본 5개)
     * @return 검색 결과
     */
    @GET("v2/local/search/keyword.json")
    suspend fun searchPlaces(
        @Header("Authorization") authorization: String,
        @Query("query") query: String,
        @Query("size") size: Int = 5
    ): KakaoPlaceResponse

    /**
     * 주소 검색 (지번, 도로명)
     *
     * @param authorization REST API 키 ("KakaoAK {REST_API_KEY}")
     * @param query 검색할 주소 (예: "서울 강남구 역삼동 123-45" 또는 "서울 강남구 강남대로 123")
     * @param size 검색 결과 개수 (기본 5개)
     * @return 주소 검색 결과
     */
    @GET("v2/local/search/address.json")
    suspend fun searchAddress(
        @Header("Authorization") authorization: String,
        @Query("query") query: String,
        @Query("size") size: Int = 5
    ): KakaoAddressResponse
}
