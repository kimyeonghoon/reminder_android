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

/**
 * v1.67.0: 카카오 주소 검색 API 응답
 */
data class KakaoAddressResponse(
    @SerializedName("documents")
    val documents: List<KakaoAddress>
)

/**
 * 카카오 API 주소 정보
 */
data class KakaoAddress(
    @SerializedName("address_name")
    val addressName: String,      // 전체 주소

    @SerializedName("address_type")
    val addressType: String,      // "REGION_ADDR" 또는 "ROAD_ADDR"

    @SerializedName("x")
    val longitude: String,        // 경도

    @SerializedName("y")
    val latitude: String,         // 위도

    @SerializedName("road_address")
    val roadAddress: RoadAddress? // 도로명 주소 (nullable)
) {
    /**
     * KakaoPlace로 변환
     * placeName은 주소를 그대로 사용
     */
    fun toKakaoPlace(): KakaoPlace {
        // 도로명 주소가 있으면 우선 사용, 없으면 지번 주소 사용
        val displayName = roadAddress?.addressName ?: addressName
        return KakaoPlace(
            placeName = displayName,
            addressName = addressName,
            longitude = longitude,
            latitude = latitude
        )
    }
}

/**
 * 도로명 주소 상세 정보
 */
data class RoadAddress(
    @SerializedName("address_name")
    val addressName: String,      // 도로명 주소

    @SerializedName("building_name")
    val buildingName: String?     // 건물명 (nullable)
)
