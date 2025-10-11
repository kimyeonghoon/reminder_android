package com.reminder.data

/**
 * v1.54.0: 방해 금지 모드 설정
 */
data class DndSettings(
    val enabled: Boolean = false,          // DND 기능 활성화 여부
    val allowCalls: Boolean = true,        // 긴급 전화 허용
    val allowAlarms: Boolean = true,       // 알람 허용
    val autoEnable: Boolean = true         // 포커스 세션 시작 시 자동 활성화
)
