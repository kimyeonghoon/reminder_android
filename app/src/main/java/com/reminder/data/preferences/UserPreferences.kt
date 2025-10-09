package com.reminder.data.preferences

/**
 * 사용자 설정 데이터 모델
 */
data class UserPreferences(
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
    val notificationLed: Boolean = true  // LED 표시등
)
