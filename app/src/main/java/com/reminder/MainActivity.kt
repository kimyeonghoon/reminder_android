package com.reminder

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.reminder.data.entity.ReminderEntity
import com.reminder.data.preferences.Language
import com.reminder.data.preferences.PreferencesRepository
import com.reminder.data.preferences.ThemeMode
import com.reminder.ui.screen.AddEditReminderScreen
import com.reminder.ui.screen.HelpScreen
import com.reminder.ui.screen.HomeScreen
import com.reminder.ui.screen.OnboardingScreen
import com.reminder.ui.screen.PatternAnalysisScreen
import com.reminder.ui.screen.SettingsScreen
import com.reminder.ui.screen.StatisticsScreen
import com.reminder.ui.theme.ReminderTheme
import com.reminder.util.LocaleHelper
import com.reminder.viewmodel.ReminderViewModel
import com.reminder.viewmodel.ReminderViewModelFactory
import com.reminder.viewmodel.SettingsViewModel
import com.reminder.viewmodel.SettingsViewModelFactory
import com.reminder.viewmodel.StatisticsViewModel
import com.reminder.viewmodel.StatisticsViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {

    private val activityScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // v1.30.0: 현재 언어 추적 (재생성 여부 판단용)
    private var currentLanguage: Language? = null

    // 알림 권한 요청 런처 (Android 13+)
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            // 권한 거부 시 설정으로 이동하도록 안내할 수 있음
        }
    }

    /**
     * v1.30.0: Activity 시작 시 저장된 언어 설정 적용
     */
    override fun attachBaseContext(newBase: Context?) {
        if (newBase == null) {
            super.attachBaseContext(newBase)
            return
        }

        // DataStore에서 언어 설정 읽기 (동기적으로)
        val preferences = runBlocking {
            PreferencesRepository.create(newBase).userPreferences.first()
        }

        currentLanguage = preferences.language

        // 저장된 언어로 Context 업데이트
        val updatedContext = LocaleHelper.updateLocale(newBase, preferences.language)
        super.attachBaseContext(updatedContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 초기 동기화 (백그라운드)
        val app = application as ReminderApplication
        activityScope.launch {
            app.syncManager.initialSync()
        }

        // Android 13+ 알림 권한 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Android 12+ 정확한 알람 권한 확인 (설정 페이지로 안내)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmScheduler = (application as ReminderApplication).alarmScheduler
            if (!alarmScheduler.canScheduleExactAlarms()) {
                // 정확한 알람 권한이 없으면 설정으로 이동하도록 안내
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            }
        }

        setContent {
            ReminderApp()
        }
    }
}

@Composable
fun ReminderApp() {
    val navController = rememberNavController()
    val activity = navController.context as MainActivity
    val app = activity.application as ReminderApplication
    val scope = rememberCoroutineScope()

    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(app.preferencesRepository, app.analyticsHelper)
    )

    val userPreferences by settingsViewModel.userPreferences.collectAsState()

    // v1.30.0: 언어 변경 감지 및 Activity 재생성
    LaunchedEffect(userPreferences.language) {
        if (activity.currentLanguage != null &&
            activity.currentLanguage != userPreferences.language) {
            // 언어가 변경되었으므로 Activity 재생성
            activity.recreate()
        }
    }

    // 테마 모드에 따라 다크 테마 여부 결정
    val darkTheme = when (userPreferences.themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    ReminderTheme(
        darkTheme = darkTheme,
        themePreset = userPreferences.themePreset,
        dynamicColor = userPreferences.dynamicColor,
        highContrastMode = userPreferences.highContrastMode,
        fontSize = userPreferences.fontSize
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            // 온보딩 완료 여부에 따라 화면 분기
            if (!userPreferences.onboardingCompleted) {
                OnboardingScreen(
                    onFinished = {
                        scope.launch {
                            settingsViewModel.setOnboardingCompleted()
                        }
                    }
                )
            } else {
                ReminderAppContent(settingsViewModel = settingsViewModel)
            }
        }
    }
}

@Composable
fun ReminderAppContent(
    settingsViewModel: SettingsViewModel
) {
    val navController = rememberNavController()
    val app = (navController.context as MainActivity).application as ReminderApplication

    val viewModel: ReminderViewModel = viewModel(
        factory = ReminderViewModelFactory(app)
    )

    val statisticsViewModel: StatisticsViewModel = viewModel(
        factory = StatisticsViewModelFactory(app.repository)
    )

    val userPreferences by settingsViewModel.userPreferences.collectAsState()
    var selectedReminder by remember { mutableStateOf<ReminderEntity?>(null) }

    NavHost(navController = navController, startDestination = "home") {
        composable(
            "home",
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            }
        ) {
            selectedReminder = null
            HomeScreen(
                viewModel = viewModel,
                onAddClick = { navController.navigate("add_edit") },
                onReminderClick = { reminder ->
                    selectedReminder = reminder
                    navController.navigate("add_edit")
                },
                onStatisticsClick = { navController.navigate("statistics") },
                onSettingsClick = { navController.navigate("settings") },
                simpleMode = userPreferences.simpleMode
            )
        }
        composable(
            "add_edit",
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            }
        ) {
            AddEditReminderScreen(
                viewModel = viewModel,
                reminder = selectedReminder,
                onNavigateBack = { navController.popBackStack() },
                simpleMode = userPreferences.simpleMode
            )
        }
        composable(
            "statistics",
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            }
        ) {
            StatisticsScreen(
                viewModel = statisticsViewModel,
                onNavigateBack = { navController.popBackStack() },
                onCompletionHistoryClick = { navController.navigate("completion_history") },
                onPatternAnalysisClick = { navController.navigate("pattern_analysis") }
            )
        }
        composable(
            "completion_history",
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            }
        ) {
            com.reminder.ui.screen.CompletionHistoryScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            "settings",
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            }
        ) {
            SettingsScreen(
                viewModel = settingsViewModel,
                backupManager = app.backupManager,
                onNavigateBack = { navController.popBackStack() },
                onHelpClick = { navController.navigate("help") }
            )
        }
        composable(
            "help",
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            }
        ) {
            HelpScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            "pattern_analysis",
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            }
        ) {
            PatternAnalysisScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
