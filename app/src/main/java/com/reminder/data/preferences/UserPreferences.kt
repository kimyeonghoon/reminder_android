package com.reminder.data.preferences

import java.util.Locale

/**
 * 사용자 설정 데이터 모델
 */
data class UserPreferences(
    val language: Language = Language.SYSTEM,  // v1.30.0: 언어 설정
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val themePreset: ThemePreset = ThemePreset.PURPLE,  // 테마 프리셋 색상
    val dynamicColor: Boolean = true,  // Material You 동적 컬러 사용 여부
    val highContrastMode: Boolean = false,  // 고대비 모드 (접근성)
    val onboardingCompleted: Boolean = false,  // 온보딩 완료 여부
    val fontSize: FontSize = FontSize.NORMAL,  // 글씨 크기
    val simpleMode: Boolean = false,  // 간편 모드

    // 알림 설정
    val notificationSound: Boolean = true,  // 알림 소리
    val notificationVibration: Boolean = true,  // 진동
    val notificationLed: Boolean = true,  // LED 표시등

    // 배지 설정
    val badgeEnabled: Boolean = true  // 앱 아이콘 배지 표시
)

/**
 * v1.30.0: 언어 설정 enum
 */
enum class Language(val code: String, val displayName: String) {
    SYSTEM("system", "System Default"),
    KOREAN("ko", "한국어"),
    ENGLISH("en", "English"),
    CHINESE("zh", "中文");

    fun toLocale(): Locale? {
        return when (this) {
            SYSTEM -> null  // 시스템 기본값 사용
            KOREAN -> Locale.KOREAN
            ENGLISH -> Locale.ENGLISH
            CHINESE -> Locale.CHINESE
        }
    }

    companion object {
        fun fromCode(code: String): Language {
            return values().find { it.code == code } ?: SYSTEM
        }
    }
}
