package com.reminder.api.kakao

import com.google.gson.annotations.SerializedName

/**
 * v1.67.0: 카카오 로컬 API 장소 검색 응답
 */
data class KakaoPlaceResponse(
    @SerializedName("documents")
    val documents: List<KakaoPlace>
)

/**
 * 카카오 API 장소 정보
 */
data class KakaoPlace(
    @SerializedName("place_name")
    val placeName: String,        // 장소명 (예: "스타벅스 강남점")

    @SerializedName("address_name")
    val addressName: String,      // 주소 (예: "서울 강남구 강남대로 123")

    @SerializedName("x")
    val longitude: String,        // 경도 (longitude)

    @SerializedName("y")
    val latitude: String          // 위도 (latitude)
)
