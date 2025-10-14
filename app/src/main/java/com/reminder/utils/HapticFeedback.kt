package com.reminder.utils

import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView

/**
 * 햅틱 피드백 헬퍼
 */
class HapticFeedback(private val view: View) {

    /**
     * 가벼운 클릭 피드백
     */
    fun click() {
        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
    }

    /**
     * 긴 누름 피드백
     */
    fun longPress() {
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
    }

    /**
     * 확인/성공 피드백
     *
     * @SuppressLint("InlinedApi"): CONFIRM은 API 30+이지만, 상수이므로 하위 버전에서도 안전
     */
    @android.annotation.SuppressLint("InlinedApi")
    fun confirm() {
        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
    }

    /**
     * 거부/취소 피드백
     *
     * @SuppressLint("InlinedApi"): REJECT는 API 30+이지만, 상수이므로 하위 버전에서도 안전
     */
    @android.annotation.SuppressLint("InlinedApi")
    fun reject() {
        view.performHapticFeedback(HapticFeedbackConstants.REJECT)
    }

    /**
     * 컨텍스트 클릭 피드백 (메뉴 등)
     */
    fun contextClick() {
        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
    }
}

/**
 * Composable에서 HapticFeedback 사용하기
 */
@Composable
fun rememberHapticFeedback(): HapticFeedback {
    val view = LocalView.current
    return remember(view) { HapticFeedback(view) }
}
