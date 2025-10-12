package com.reminder.qa

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.reminder.MainActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 간단한 QA 기능 테스트 - 기본 CRUD
 *
 * ADB 대신 Espresso를 사용하여 한글 입력 및 UI 자동화 테스트
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
class SimpleCRUDTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        // Wait for UI to settle
        Thread.sleep(2000)

        // Skip onboarding if it appears
        try {
            composeTestRule.onNodeWithText("건너뛰기", substring = true)
                .assertExists()
                .performClick()
            Thread.sleep(1000)
        } catch (e: AssertionError) {
            // Onboarding already skipped
        }

        // Handle permission dialog if it appears
        try {
            composeTestRule.onNodeWithText("허용", substring = true)
                .assertExists()
                .performClick()
            Thread.sleep(1000)
        } catch (e: AssertionError) {
            // Permission already granted
        }

        // Wait for main screen to load
        composeTestRule.waitForIdle()
    }

    /**
     * Test 2.1: 홈 화면 UI 요소 확인
     */
    @Test
    fun test21_verifyHomeScreenElements() {
        // Bottom Navigation 확인
        composeTestRule.onNodeWithText("홈", substring = true, ignoreCase = true).assertExists()
        composeTestRule.onNodeWithText("통계", substring = true, ignoreCase = true).assertExists()

        // 필터 버튼 확인 (v1.63.0: 한글로 변경됨)
        composeTestRule.onNodeWithText("전체", substring = true).assertExists()
        composeTestRule.onNodeWithText("높음", substring = true).assertExists()
        composeTestRule.onNodeWithText("중간", substring = true).assertExists()
        composeTestRule.onNodeWithText("낮음", substring = true).assertExists()
    }

    /**
     * Test 2.2: FAB 버튼 확인
     */
    @Test
    fun test22_verifyFabButton() {
        // FAB 버튼 찾기
        val fabNode = composeTestRule.onAllNodesWithContentDescription("리마인더 추가", substring = true, ignoreCase = true)
            .onFirst()

        fabNode.assertExists("FAB 버튼을 찾을 수 없습니다")
    }
}
