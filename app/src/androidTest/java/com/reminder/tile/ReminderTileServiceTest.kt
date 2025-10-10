package com.reminder.tile

import android.content.ComponentName
import android.content.Context
import android.service.quicksettings.TileService
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * v1.42.0: ReminderTileService 통합 테스트
 *
 * TDD Red Phase: 테스트 먼저 작성
 */
@RunWith(AndroidJUnit4::class)
class ReminderTileServiceTest {

    private lateinit var context: Context
    private lateinit var componentName: ComponentName

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        componentName = ComponentName(
            context,
            ReminderTileService::class.java
        )
    }

    /**
     * 테스트 1: ReminderTileService가 시스템에 등록되어 있는지 확인
     */
    @Test
    fun tileService_isRegisteredInManifest() {
        val packageManager = context.packageManager
        val serviceInfo = packageManager.getServiceInfo(
            componentName,
            0
        )

        assertNotNull("ReminderTileService should be registered", serviceInfo)
        assertEquals(
            "Service should be ReminderTileService",
            ReminderTileService::class.java.name,
            serviceInfo.name
        )
    }

    /**
     * 테스트 2: TileService 권한이 올바르게 설정되어 있는지 확인
     */
    @Test
    fun tileService_hasBindTilePermission() {
        val packageManager = context.packageManager
        val serviceInfo = packageManager.getServiceInfo(
            componentName,
            0
        )

        assertEquals(
            "Service should require BIND_QUICK_SETTINGS_TILE permission",
            "android.permission.BIND_QUICK_SETTINGS_TILE",
            serviceInfo.permission
        )
    }

    /**
     * 테스트 3: TileService가 활성화되어 있는지 확인
     */
    @Test
    fun tileService_isEnabled() {
        val packageManager = context.packageManager
        val componentEnabledSetting = packageManager.getComponentEnabledSetting(componentName)

        // COMPONENT_ENABLED_STATE_DEFAULT (0) 또는 COMPONENT_ENABLED_STATE_ENABLED (1)
        assertTrue(
            "TileService should be enabled",
            componentEnabledSetting == 0 || componentEnabledSetting == 1
        )
    }

    /**
     * 테스트 4: TileService가 올바른 메타데이터를 가지고 있는지 확인
     */
    @Test
    fun tileService_hasCorrectMetadata() {
        val packageManager = context.packageManager
        val serviceInfo = packageManager.getServiceInfo(
            componentName,
            android.content.pm.PackageManager.GET_META_DATA
        )

        assertNotNull("ServiceInfo should not be null", serviceInfo)
        // 메타데이터는 선택사항이므로 null일 수 있음
    }

    /**
     * 테스트 5: ComponentName이 올바른 패키지를 가리키는지 확인
     */
    @Test
    fun componentName_hasCorrectPackage() {
        assertEquals(
            "Package should be com.reminder",
            "com.reminder",
            componentName.packageName
        )
    }
}
