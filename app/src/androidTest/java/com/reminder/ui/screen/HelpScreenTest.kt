package com.reminder.ui.screen

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HelpScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private var onNavigateBackCalled = false

    @Before
    fun setup() {
        onNavigateBackCalled = false
    }

    @Test
    fun 도움말_화면_제목이_표시된다() {
        // Given
        composeTestRule.setContent {
            HelpScreen(onNavigateBack = {})
        }

        // Then
        composeTestRule.onNodeWithText("도움말").assertIsDisplayed()
    }

    @Test
    fun 뒤로가기_버튼_클릭_시_onNavigateBack_콜백이_호출된다() {
        // Given
        composeTestRule.setContent {
            HelpScreen(onNavigateBack = { onNavigateBackCalled = true })
        }

        // When
        composeTestRule.onNodeWithContentDescription("뒤로가기").performClick()

        // Then
        assert(onNavigateBackCalled)
    }

    @Test
    fun 주요_기능_섹션이_표시된다() {
        // Given
        composeTestRule.setContent {
            HelpScreen(onNavigateBack = {})
        }

        // Then
        composeTestRule.onNodeWithText("주요 기능").assertIsDisplayed()
    }

    @Test
    fun 할_일_추가하기_도움말이_표시된다() {
        // Given
        composeTestRule.setContent {
            HelpScreen(onNavigateBack = {})
        }

        // Then
        composeTestRule.onNodeWithText("• 할 일 추가하기")
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(
            "홈 화면 우측 하단의 + 버튼을 눌러 새로운 할 일을 추가할 수 있습니다."
        ).assertExists()
    }

    @Test
    fun 할_일_완료하기_도움말이_표시된다() {
        // Given
        composeTestRule.setContent {
            HelpScreen(onNavigateBack = {})
        }

        // Then
        composeTestRule.onNodeWithText("• 할 일 완료하기").assertIsDisplayed()
        composeTestRule.onNodeWithText(
            "할 일 카드의 체크박스를 누르면 완료 표시가 됩니다."
        ).assertIsDisplayed()
    }

    @Test
    fun 우선순위_설정_도움말이_표시된다() {
        // Given
        composeTestRule.setContent {
            HelpScreen(onNavigateBack = {})
        }

        // Then
        composeTestRule.onNodeWithText("• 우선순위 설정").assertIsDisplayed()
        composeTestRule.onNodeWithText(
            "할 일 추가/수정 시 높음(빨강), 중간(주황), 낮음(초록) 중 선택할 수 있습니다."
        ).assertIsDisplayed()
    }

    @Test
    fun 설정_기능_섹션이_표시된다() {
        // Given
        composeTestRule.setContent {
            HelpScreen(onNavigateBack = {})
        }

        // Then
        composeTestRule.onNodeWithText("설정 기능").assertIsDisplayed()
    }

    @Test
    fun 테마_변경_도움말이_표시된다() {
        // Given
        composeTestRule.setContent {
            HelpScreen(onNavigateBack = {})
        }

        // Then
        composeTestRule.onNodeWithText("• 테마 변경")
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(
            "설정에서 라이트/다크/시스템 테마를 선택할 수 있습니다."
        ).assertExists()
    }

    @Test
    fun 간편_모드_도움말이_표시된다() {
        // Given
        composeTestRule.setContent {
            HelpScreen(onNavigateBack = {})
        }

        // Then
        composeTestRule.onNodeWithText("• 간편 모드")
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(
            "복잡한 기능을 숨기고 더 큰 버튼으로 사용할 수 있는 모드입니다."
        ).assertExists()
    }

    @Test
    fun 자주_묻는_질문_섹션이_표시된다() {
        // Given
        composeTestRule.setContent {
            HelpScreen(onNavigateBack = {})
        }

        // Then
        composeTestRule.onNodeWithText("자주 묻는 질문 (FAQ)")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun FAQ_알림이_오지_않아요_질문이_표시된다() {
        // Given
        composeTestRule.setContent {
            HelpScreen(onNavigateBack = {})
        }

        // Then
        composeTestRule.onNodeWithText("Q. 알림이 오지 않아요")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun FAQ_카드_클릭_시_답변이_표시된다() {
        // Given
        composeTestRule.setContent {
            HelpScreen(onNavigateBack = {})
        }

        // FAQ 항목으로 스크롤
        composeTestRule.onNodeWithText("Q. 알림이 오지 않아요")
            .performScrollTo()

        // 처음에는 답변이 표시되지 않음
        composeTestRule.onNodeWithText(
            "A. 설정 → 앱 → Reminder → 알림 권한을 확인해주세요. Android 12 이상에서는 정확한 알람 권한도 필요합니다."
        ).assertDoesNotExist()

        // When - FAQ 클릭
        composeTestRule.onNodeWithText("Q. 알림이 오지 않아요").performClick()

        // Then - 답변이 표시됨
        composeTestRule.onNodeWithText(
            "A. 설정 → 앱 → Reminder → 알림 권한을 확인해주세요. Android 12 이상에서는 정확한 알람 권한도 필요합니다."
        ).assertExists()
    }

    @Test
    fun FAQ_완료한_할일_질문이_표시된다() {
        // Given
        composeTestRule.setContent {
            HelpScreen(onNavigateBack = {})
        }

        // Then
        composeTestRule.onNodeWithText("Q. 완료한 할 일은 어디서 볼 수 있나요?")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun FAQ_데이터_백업_질문이_표시된다() {
        // Given
        composeTestRule.setContent {
            HelpScreen(onNavigateBack = {})
        }

        // Then
        composeTestRule.onNodeWithText("Q. 데이터를 백업하고 싶어요")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun FAQ_위젯_업데이트_질문이_표시된다() {
        // Given
        composeTestRule.setContent {
            HelpScreen(onNavigateBack = {})
        }

        // Then
        composeTestRule.onNodeWithText("Q. 위젯이 업데이트되지 않아요")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun FAQ_글씨가_너무_작아요_질문이_표시된다() {
        // Given
        composeTestRule.setContent {
            HelpScreen(onNavigateBack = {})
        }

        // Then
        composeTestRule.onNodeWithText("Q. 글씨가 너무 작아요")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun 문의하기_카드가_표시된다() {
        // Given
        composeTestRule.setContent {
            HelpScreen(onNavigateBack = {})
        }

        // Then
        composeTestRule.onNodeWithText("문제가 해결되지 않으셨나요?")
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("앱을 개선하는데 도움을 주셔서 감사합니다.")
            .assertExists()
    }

    @Test
    fun FAQ_두_번_클릭_시_답변이_숨겨진다() {
        // Given
        composeTestRule.setContent {
            HelpScreen(onNavigateBack = {})
        }

        // FAQ 항목으로 스크롤
        composeTestRule.onNodeWithText("Q. 알림이 오지 않아요")
            .performScrollTo()

        // When - FAQ 첫 번째 클릭 (답변 표시)
        composeTestRule.onNodeWithText("Q. 알림이 오지 않아요").performClick()

        // Then - 답변이 표시됨
        composeTestRule.onNodeWithText(
            "A. 설정 → 앱 → Reminder → 알림 권한을 확인해주세요. Android 12 이상에서는 정확한 알람 권한도 필요합니다."
        ).assertExists()

        // When - FAQ 두 번째 클릭 (답변 숨김)
        composeTestRule.onNodeWithText("Q. 알림이 오지 않아요").performClick()

        // Then - 답변이 숨겨짐
        composeTestRule.onNodeWithText(
            "A. 설정 → 앱 → Reminder → 알림 권한을 확인해주세요. Android 12 이상에서는 정확한 알람 권한도 필요합니다."
        ).assertDoesNotExist()
    }
}
