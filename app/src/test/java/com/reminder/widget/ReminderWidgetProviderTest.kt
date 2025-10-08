package com.reminder.widget

import org.junit.Assert.*
import org.junit.Test

/**
 * ReminderWidgetProvider 유닛 테스트
 *
 * 참고: AppWidgetProvider는 Android 프레임워크에 강하게 의존하므로
 * 실제 위젯 동작은 통합 테스트나 수동 테스트로 검증합니다.
 */
class ReminderWidgetProviderTest {

    @Test
    fun `위젯 업데이트 액션 상수가 올바르게 정의되어 있다`() {
        // Given & When & Then
        assertEquals("com.reminder.widget.ACTION_UPDATE_WIDGET",
            ReminderWidgetProvider.ACTION_UPDATE_WIDGET)
    }

    @Test
    fun `위젯 아이템 클릭 액션 상수가 올바르게 정의되어 있다`() {
        // Given & When & Then
        assertEquals("com.reminder.widget.ACTION_ITEM_CLICK",
            ReminderWidgetProvider.ACTION_ITEM_CLICK)
    }
}
