package com.reminder.utils

import android.webkit.URLUtil

/**
 * URL 유효성 검사 유틸리티
 */
object UrlValidator {

    /**
     * URL이 유효한지 확인
     *
     * @param url 검사할 URL
     * @return 유효하면 true
     */
    fun isValidUrl(url: String?): Boolean {
        if (url.isNullOrBlank()) return false

        return URLUtil.isValidUrl(url) &&
                (URLUtil.isHttpUrl(url) || URLUtil.isHttpsUrl(url))
    }

    /**
     * URL을 정규화 (http:// 또는 https:// 접두사 추가)
     *
     * @param url 정규화할 URL
     * @return 정규화된 URL
     */
    fun normalizeUrl(url: String): String {
        if (url.isBlank()) return ""

        val trimmedUrl = url.trim()

        // 이미 http:// 또는 https://로 시작하면 그대로 반환
        if (trimmedUrl.startsWith("http://", ignoreCase = true) ||
            trimmedUrl.startsWith("https://", ignoreCase = true)
        ) {
            return trimmedUrl
        }

        // 그렇지 않으면 https:// 접두사 추가
        return "https://$trimmedUrl"
    }

    /**
     * URL에서 도메인 추출
     *
     * @param url URL
     * @return 도메인 (추출 실패 시 원본 URL)
     */
    fun extractDomain(url: String?): String {
        if (url.isNullOrBlank()) return ""

        return try {
            val normalizedUrl = normalizeUrl(url)
            val uri = java.net.URI(normalizedUrl)
            uri.host ?: url
        } catch (e: Exception) {
            url
        }
    }
}
