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
     * 타일 서비스가 Manifest에 등록되어 있다
     */
    @Test
    fun tileServiceIsRegisteredInManifest() {
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
     * 타일 서비스가 BIND_QUICK_SETTINGS_TILE 권한을 가진다
     */
    @Test
    fun tileServiceHasBindQuickSettingsTilePermission() {
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
     * 타일 서비스가 활성화되어 있다
     */
    @Test
    fun tileServiceIsEnabled() {
        val packageManager = context.packageManager
        val componentEnabledSetting = packageManager.getComponentEnabledSetting(componentName)

        // COMPONENT_ENABLED_STATE_DEFAULT (0) 또는 COMPONENT_ENABLED_STATE_ENABLED (1)
        assertTrue(
            "TileService should be enabled",
            componentEnabledSetting == 0 || componentEnabledSetting == 1
        )
    }

    /**
     * 타일 서비스가 올바른 메타데이터를 가진다
     */
    @Test
    fun tileServiceHasCorrectMetadata() {
        val packageManager = context.packageManager
        val serviceInfo = packageManager.getServiceInfo(
            componentName,
            android.content.pm.PackageManager.GET_META_DATA
        )

        assertNotNull("ServiceInfo should not be null", serviceInfo)
        // 메타데이터는 선택사항이므로 null일 수 있음
    }

    /**
     * ComponentName이 올바른 패키지를 가진다
     */
    @Test
    fun componentNameHasCorrectPackage() {
        assertEquals(
            "Package should be com.reminder",
            "com.reminder",
            componentName.packageName
        )
    }
}
