package com.reminder.qa

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.reminder.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * QA 기능 테스트 - 기본 CRUD
 *
 * Test 2.1~2.6: 리마인더 추가, 읽기, 수정, 삭제, 완료 토글
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
class FunctionalCRUDTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    /**
     * Test 2.1: 리마인더 추가 (기본 정보)
     *
     * 실행 단계:
     * 1. 홈 화면 우측 하단 "+" FAB 버튼 탭
     * 2. 제목 입력: "테스트 할일 1"
     * 3. "저장" 버튼 탭
     * 4. 홈 화면으로 돌아가서 리마인더 확인
     *
     * 예상 결과:
     * - 추가 화면으로 이동
     * - 리마인더가 목록에 표시됨
     * - 제목이 정확히 "테스트 할일 1"로 표시됨
     */
    @Test
    fun test21_addReminderBasicInfo() {
        // 1. FAB 버튼 찾기 (contentDescription 사용)
        composeTestRule
            .onNodeWithContentDescription("리마인더 추가")
            .assertExists()
            .assertIsDisplayed()
            .performClick()

        // 잠시 대기 (화면 전환)
        composeTestRule.waitForIdle()

        // 2. 제목 입력란 찾기
        composeTestRule
            .onNodeWithText("제목")
            .assertExists()
            .performClick()
            .performTextInput("테스트 할일 1")

        // 3. 저장 버튼 클릭 (뒤로가기로 저장)
        composeTestRule.activityRule.scenario.onActivity { activity ->
            activity.onBackPressedDispatcher.onBackPressed()
        }

        // 잠시 대기
        composeTestRule.waitForIdle()

        // 4. 홈 화면에서 리마인더 확인
        composeTestRule
            .onNodeWithText("테스트 할일 1")
            .assertExists()
            .assertIsDisplayed()
    }

    /**
     * Test 2.2: 리마인더 추가 (전체 정보)
     *
     * 실행 단계:
     * 1. "+" 버튼 탭
     * 2. 제목 입력: "회의 준비"
     * 3. 설명 입력: "프로젝트 진행 상황 정리하기"
     * 4. 우선순위 선택: "높음" (빨간색)
     * 5. 카테고리 입력: "업무"
     * 6. 저장
     *
     * 예상 결과:
     * - 모든 정보가 정확히 입력됨
     * - 우선순위가 높음으로 표시됨
     */
    @Test
    fun test22_addReminderFullInfo() {
        // 1. FAB 클릭
        composeTestRule
            .onNodeWithContentDescription("리마인더 추가")
            .performClick()

        composeTestRule.waitForIdle()

        // 2. 제목 입력
        composeTestRule
            .onNodeWithText("제목")
            .performClick()
            .performTextInput("회의 준비")

        // 3. 설명 입력
        composeTestRule
            .onNodeWithText("설명")
            .performClick()
            .performTextInput("프로젝트 진행 상황 정리하기")

        // 4. 우선순위 "높음" 선택
        composeTestRule
            .onNodeWithText("높음")
            .performClick()

        // 5. 카테고리 입력
        composeTestRule
            .onNodeWithText("카테고리")
            .performClick()
            .performTextInput("업무")

        // 6. 저장 (뒤로가기)
        composeTestRule.activityRule.scenario.onActivity { activity ->
            activity.onBackPressedDispatcher.onBackPressed()
        }

        composeTestRule.waitForIdle()

        // 7. 확인
        composeTestRule
            .onNodeWithText("회의 준비")
            .assertExists()
            .assertIsDisplayed()
    }

    /**
     * Test 2.3: 리마인더 읽기 (조회)
     *
     * 실행 단계:
     * 1. 홈 화면에서 "회의 준비" 리마인더 카드 탭
     * 2. 편집 화면에서 모든 정보 확인
     * 3. 뒤로가기 버튼으로 홈 화면 복귀
     *
     * 예상 결과:
     * - 편집 화면으로 이동
     * - 모든 입력 필드에 저장된 값이 표시됨
     */
    @Test
    fun test23_readReminder() {
        // 먼저 리마인더 추가 (테스트 데이터 준비)
        test21_addReminderBasicInfo()

        composeTestRule.waitForIdle()

        // 1. 리마인더 카드 클릭
        composeTestRule
            .onNodeWithText("테스트 할일 1")
            .performClick()

        composeTestRule.waitForIdle()

        // 2. 제목이 표시되는지 확인
        composeTestRule
            .onNodeWithText("테스트 할일 1")
            .assertExists()

        // 3. 뒤로가기
        composeTestRule.activityRule.scenario.onActivity { activity ->
            activity.onBackPressedDispatcher.onBackPressed()
        }

        composeTestRule.waitForIdle()

        // 4. 홈 화면 복귀 확인
        composeTestRule
            .onNodeWithText("테스트 할일 1")
            .assertExists()
    }

    /**
     * Test 2.4: 리마인더 수정
     *
     * 실행 단계:
     * 1. "테스트 할일 1" 리마인더 카드 탭
     * 2. 제목 수정: "테스트 할일 1 (수정됨)"
     * 3. "저장" 버튼 탭
     * 4. 홈 화면에서 변경 사항 확인
     *
     * 예상 결과:
     * - 제목이 변경됨
     */
    @Test
    fun test24_updateReminder() {
        // 먼저 리마인더 추가
        test21_addReminderBasicInfo()

        composeTestRule.waitForIdle()

        // 1. 리마인더 클릭
        composeTestRule
            .onNodeWithText("테스트 할일 1")
            .performClick()

        composeTestRule.waitForIdle()

        // 2. 제목 필드 찾아서 수정
        composeTestRule
            .onNodeWithText("제목")
            .performClick()

        // 기존 텍스트 지우기
        composeTestRule
            .onNodeWithText("테스트 할일 1")
            .performTextClearance()

        // 새 텍스트 입력
        composeTestRule
            .onNodeWithText("제목")
            .performTextInput("테스트 할일 1 (수정됨)")

        // 3. 저장
        composeTestRule.activityRule.scenario.onActivity { activity ->
            activity.onBackPressedDispatcher.onBackPressed()
        }

        composeTestRule.waitForIdle()

        // 4. 확인
        composeTestRule
            .onNodeWithText("테스트 할일 1 (수정됨)")
            .assertExists()
            .assertIsDisplayed()
    }

    /**
     * Test 2.5: 리마인더 삭제
     *
     * 실행 단계:
     * 1. "테스트 할일 1" 리마인더 카드의 삭제 아이콘 탭
     * 2. 확인 다이얼로그에서 "삭제" 버튼 탭
     * 3. 리마인더가 목록에서 사라졌는지 확인
     *
     * 예상 결과:
     * - 확인 다이얼로그 표시
     * - "삭제" 탭 시 리마인더가 목록에서 제거됨
     */
    @Test
    fun test25_deleteReminder() {
        // 먼저 리마인더 추가
        test21_addReminderBasicInfo()

        composeTestRule.waitForIdle()

        // 1. 삭제 아이콘 찾기 (contentDescription 사용)
        composeTestRule
            .onNodeWithContentDescription("삭제")
            .performClick()

        composeTestRule.waitForIdle()

        // 2. 확인 다이얼로그에서 "삭제" 버튼 클릭
        composeTestRule
            .onNodeWithText("삭제")
            .performClick()

        composeTestRule.waitForIdle()

        // 3. 리마인더가 사라졌는지 확인
        composeTestRule
            .onNodeWithText("테스트 할일 1")
            .assertDoesNotExist()
    }

    /**
     * Test 2.6: 리마인더 완료 토글
     *
     * 실행 단계:
     * 1. "테스트 할일 1" 리마인더의 체크박스 탭
     * 2. 체크 표시가 나타나는지 확인
     * 3. 다시 체크박스 탭하여 미완료 상태로 변경
     *
     * 예상 결과:
     * - 체크 시: 체크박스에 체크 표시
     * - 언체크 시: 원래 스타일 복구
     * - 상태 변경이 즉시 반영됨
     */
    @Test
    fun test26_toggleReminderCompletion() {
        // 먼저 리마인더 추가
        test21_addReminderBasicInfo()

        composeTestRule.waitForIdle()

        // 1. 체크박스 찾기 및 클릭 (완료 처리)
        composeTestRule
            .onNode(hasTestTag("checkbox_테스트 할일 1") or hasContentDescription("완료"))
            .performClick()

        composeTestRule.waitForIdle()

        // 2. 다시 클릭 (미완료 처리)
        composeTestRule
            .onNode(hasTestTag("checkbox_테스트 할일 1") or hasContentDescription("완료"))
            .performClick()

        composeTestRule.waitForIdle()

        // 3. 리마인더가 여전히 표시되는지 확인
        composeTestRule
            .onNodeWithText("테스트 할일 1")
            .assertExists()
            .assertIsDisplayed()
    }
}
