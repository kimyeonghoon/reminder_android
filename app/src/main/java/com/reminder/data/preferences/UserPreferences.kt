package com.reminder.data.preferences

/**
 * 사용자 설정 데이터 모델
 */
data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,  // Material You 동적 컬러 사용 여부
    val onboardingCompleted: Boolean = false  // 온보딩 완료 여부
)
