package com.reminder.qa

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 간단한 QA 기능 테스트 - UI Automator 사용 (API 36 호환)
 *
 * Espresso의 InputManager 이슈를 피하기 위해 UI Automator 사용
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
class UIAutomatorCRUDTest {

    private lateinit var device: UiDevice

    @Before
    fun setup() {
        // Initialize UiDevice instance
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        // Start from the home screen
        device.pressHome()

        // Wait for launcher
        val launcherPackage = device.launcherPackageName
        assertNotNull(launcherPackage)
        device.wait(Until.hasObject(By.pkg(launcherPackage).depth(0)), 5000)

        // Launch the app
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val intent = context.packageManager.getLaunchIntentForPackage("com.reminder")?.apply {
            addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        context.startActivity(intent)

        // Wait for the app to appear
        device.wait(Until.hasObject(By.pkg("com.reminder").depth(0)), 10000)

        // Handle onboarding if it appears
        Thread.sleep(2000) // Wait for UI to settle

        // Check for "건너뛰기" button (Skip onboarding)
        val skipButton = device.findObject(By.text("건너뛰기"))
        if (skipButton != null) {
            skipButton.click()
            Thread.sleep(1000)
        }

        // Check for permission dialog
        val allowButton = device.findObject(By.text("허용"))
        if (allowButton != null) {
            allowButton.click()
            Thread.sleep(1000)
        }

        // Wait for main screen
        device.wait(Until.hasObject(By.text("홈")), 10000)
    }

    /**
     * Test 2.1: 홈 화면 UI 요소 확인
     */
    @Test
    fun test21_verifyHomeScreenElements() {
        // Bottom Navigation 확인
        val homeTab = device.wait(Until.findObject(By.text("홈")), 5000)
        assertNotNull("홈 탭을 찾을 수 없습니다", homeTab)

        val statsTab = device.findObject(By.text("통계"))
        assertNotNull("통계 탭을 찾을 수 없습니다", statsTab)

        // 필터 버튼 확인 (v1.63.0: 한글로 변경됨)
        val allFilter = device.findObject(By.text("전체"))
        assertNotNull("전체 필터를 찾을 수 없습니다", allFilter)

        val highFilter = device.findObject(By.text("높음"))
        assertNotNull("높음 필터를 찾을 수 없습니다", highFilter)

        val mediumFilter = device.findObject(By.text("중간"))
        assertNotNull("중간 필터를 찾을 수 없습니다", mediumFilter)

        val lowFilter = device.findObject(By.text("낮음"))
        assertNotNull("낮음 필터를 찾을 수 없습니다", lowFilter)
    }

    /**
     * Test 2.2: FAB 버튼 확인 및 클릭
     */
    @Test
    fun test22_verifyAndClickFabButton() {
        // FAB 버튼 찾기 (Content Description 사용)
        val fabButton = device.wait(Until.findObject(By.desc("리마인더 추가")), 5000)
        assertNotNull("FAB 버튼을 찾을 수 없습니다", fabButton)

        // FAB 버튼 클릭
        fabButton.click()

        // 추가 화면으로 이동했는지 확인
        val titleField = device.wait(Until.findObject(By.text("제목")), 5000)
        assertNotNull("추가 화면으로 이동하지 못했습니다", titleField)
    }

    /**
     * Test 2.3: 리마인더 추가 (한글 입력)
     */
    @Test
    fun test23_addReminderWithKoreanText() {
        // FAB 버튼 클릭
        val fabButton = device.wait(Until.findObject(By.desc("리마인더 추가")), 5000)
        assertNotNull("FAB 버튼을 찾을 수 없습니다", fabButton)
        fabButton.click()

        // 제목 입력
        val titleField = device.wait(Until.findObject(By.text("제목")), 5000)
        assertNotNull("제목 입력란을 찾을 수 없습니다", titleField)
        titleField.click()
        device.wait(Until.findObject(By.focused(true)), 2000)
        titleField.text = "테스트 리마인더"

        // 저장 (뒤로가기)
        device.pressBack()
        Thread.sleep(1000)

        // 홈 화면으로 돌아왔는지 확인
        val homeTab = device.wait(Until.findObject(By.text("홈")), 5000)
        assertNotNull("홈 화면으로 돌아오지 못했습니다", homeTab)

        // 추가된 리마인더 확인
        val addedReminder = device.wait(Until.findObject(By.textContains("테스트 리마인더")), 5000)
        assertNotNull("추가된 리마인더를 찾을 수 없습니다", addedReminder)
    }
}
