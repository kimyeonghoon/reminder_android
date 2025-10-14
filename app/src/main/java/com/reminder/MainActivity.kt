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
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.reminder.ui.screen.ArchiveScreen
import com.reminder.ui.screen.CalendarSyncScreen
import com.reminder.ui.screen.EisenhowerMatrixScreen
import com.reminder.ui.screen.FocusModeScreen
import com.reminder.ui.screen.HabitTrackerScreen
import com.reminder.ui.screen.HelpScreen
import com.reminder.ui.screen.HomeScreen
import com.reminder.ui.screen.OnboardingScreen
import com.reminder.ui.screen.PatternAnalysisScreen
import com.reminder.ui.screen.PomodoroScreen
import com.reminder.ui.screen.SettingsScreen
import com.reminder.ui.screen.StatisticsScreen
import com.reminder.ui.theme.ReminderTheme
import com.reminder.utils.LocaleHelper
import com.reminder.viewmodel.ArchiveViewModel
import com.reminder.viewmodel.ArchiveViewModelFactory
import com.reminder.viewmodel.CalendarSyncViewModel
import com.reminder.viewmodel.CalendarSyncViewModelFactory
import com.reminder.viewmodel.HabitViewModel
import com.reminder.viewmodel.HabitViewModelFactory
import com.reminder.viewmodel.PomodoroViewModel
import com.reminder.viewmodel.PomodoroViewModelFactory
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
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState

/**
 * v1.46.0: Bottom Navigation 아이템 정의
 */
data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

class MainActivity : ComponentActivity() {

    private val activityScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // v1.30.0: 현재 언어 추적 (재생성 여부 판단용)
    internal var currentLanguage: Language? = null

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

        // 테스트 더미 데이터 추가 (한 번만 실행)
        val prefs = getSharedPreferences("test_data", MODE_PRIVATE)
        if (!prefs.getBoolean("dummy_data_inserted", false)) {
            activityScope.launch {
                insertDummyData(app)
                prefs.edit().putBoolean("dummy_data_inserted", true).apply()
            }
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

    private suspend fun insertDummyData(app: ReminderApplication) {
        val now = java.time.LocalDateTime.now()
        val dummyReminders = listOf(
            com.reminder.data.entity.ReminderEntity(
                title = "장보기",
                description = "우유, 계란, 빵 사기",
                dueDateTime = now.plusHours(8),
                priority = com.reminder.data.entity.Priority.MEDIUM,
                urgency = com.reminder.data.entity.Urgency.MEDIUM,
                category = "집안일",
                isCompleted = false,
                createdAt = now,
                updatedAt = now
            ),
            com.reminder.data.entity.ReminderEntity(
                title = "병원 예약",
                description = "치과 검진 예약하기",
                dueDateTime = now.plusDays(1).withHour(10).withMinute(0),
                priority = com.reminder.data.entity.Priority.HIGH,
                urgency = com.reminder.data.entity.Urgency.HIGH,
                category = "건강",
                isCompleted = false,
                createdAt = now,
                updatedAt = now
            ),
            com.reminder.data.entity.ReminderEntity(
                title = "약 먹기",
                description = "고혈압약 복용",
                dueDateTime = now.withHour(20).withMinute(0),
                priority = com.reminder.data.entity.Priority.HIGH,
                urgency = com.reminder.data.entity.Urgency.HIGH,
                category = "건강",
                isCompleted = false,
                createdAt = now,
                updatedAt = now
            ),
            com.reminder.data.entity.ReminderEntity(
                title = "손주 전화하기",
                description = "손주에게 안부 전화",
                dueDateTime = now.withHour(15).withMinute(0),
                priority = com.reminder.data.entity.Priority.MEDIUM,
                urgency = com.reminder.data.entity.Urgency.MEDIUM,
                category = "가족",
                isCompleted = false,
                createdAt = now,
                updatedAt = now
            ),
            com.reminder.data.entity.ReminderEntity(
                title = "운동하기",
                description = "동네 한 바퀴 산책",
                dueDateTime = now.plusDays(1).withHour(7).withMinute(0),
                priority = com.reminder.data.entity.Priority.LOW,
                urgency = com.reminder.data.entity.Urgency.LOW,
                category = "건강",
                isCompleted = false,
                createdAt = now,
                updatedAt = now
            ),
            com.reminder.data.entity.ReminderEntity(
                title = "청구서 납부",
                description = "전기세 납부하기",
                dueDateTime = now.plusDays(2).withHour(12).withMinute(0),
                priority = com.reminder.data.entity.Priority.HIGH,
                urgency = com.reminder.data.entity.Urgency.HIGH,
                category = "생활",
                isCompleted = false,
                createdAt = now,
                updatedAt = now
            ),
            com.reminder.data.entity.ReminderEntity(
                title = "텃밭 물주기",
                description = "토마토랑 상추 물주기",
                dueDateTime = now.withHour(17).withMinute(0),
                priority = com.reminder.data.entity.Priority.LOW,
                urgency = com.reminder.data.entity.Urgency.LOW,
                category = "취미",
                isCompleted = false,
                createdAt = now,
                updatedAt = now
            ),
            com.reminder.data.entity.ReminderEntity(
                title = "경로당 가기",
                description = "오후 2시 경로당 모임",
                dueDateTime = now.plusDays(1).withHour(14).withMinute(0),
                priority = com.reminder.data.entity.Priority.MEDIUM,
                urgency = com.reminder.data.entity.Urgency.MEDIUM,
                category = "사회활동",
                isCompleted = false,
                createdAt = now,
                updatedAt = now
            )
        )

        dummyReminders.forEach { reminder ->
            app.repository.insertReminder(reminder)
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
            // TODO: BUG_003 - 온보딩 Skip 버튼이 작동하지 않음 (테스트용 임시 비활성화)
            if (false && !userPreferences.onboardingCompleted) {
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

@OptIn(ExperimentalMaterial3Api::class)
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
        factory = StatisticsViewModelFactory(app.repository, app.database.goalDao())
    )

    // v1.40.1: CalendarSyncViewModel 추가
    val calendarSyncViewModel: CalendarSyncViewModel = viewModel(
        factory = CalendarSyncViewModelFactory(
            app.calendarSyncManager,
            app.deviceCalendarProvider,
            app.database.reminderDao()
        )
    )

    // v1.43.0: ArchiveViewModel 추가
    val archiveViewModel: ArchiveViewModel = viewModel(
        factory = ArchiveViewModelFactory(app.archiveManager)
    )

    // v1.44.0: HabitViewModel 추가
    val habitViewModel: HabitViewModel = viewModel(
        factory = HabitViewModelFactory(app.habitManager)
    )

    // v1.46.0: PomodoroViewModel 추가
    val pomodoroViewModel: PomodoroViewModel = viewModel(
        factory = PomodoroViewModelFactory(app.pomodoroManager)
    )

    val userPreferences by settingsViewModel.userPreferences.collectAsState()
    var selectedReminder by remember { mutableStateOf<ReminderEntity?>(null) }

    // v1.46.0: Bottom Navigation Items
    // v1.52.0: Settings 제거, Focus Mode 추가
    val bottomNavItems = listOf(
        BottomNavItem("home", Icons.Default.Home, "홈"),
        BottomNavItem("statistics", Icons.Default.BarChart, "통계"),
        BottomNavItem("pomodoro", Icons.Default.Timer, "포모도로"),
        BottomNavItem("focus_mode", Icons.Default.Adjust, "집중"),
        BottomNavItem("habit_tracker", Icons.Default.CheckBox, "습관")
    )

    // 현재 라우트 추적
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Bottom Bar를 표시할 라우트 목록
    val bottomBarRoutes = bottomNavItems.map { it.route }
    val showBottomBar = currentRoute in bottomBarRoutes

    // HomeScreen state (for TopAppBar)
    var homeScreenTopAppBarContent by remember { mutableStateOf<(@Composable () -> Unit)?>(null) }

    Scaffold(
        topBar = {
            // Centralized TopAppBar based on current route
            when (currentRoute) {
                "home" -> {
                    // HomeScreen's TopAppBar (complex: SearchBar, SelectionMode, Normal)
                    homeScreenTopAppBarContent?.invoke()
                }
                "statistics" -> {
                    TopAppBar(
                        title = { Text("통계") }
                    )
                }
                "pomodoro" -> {
                    TopAppBar(
                        title = { Text("포모도로 타이머") },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
                "focus_mode" -> {
                    TopAppBar(
                        title = { Text("집중 모드") }
                    )
                }
                "habit_tracker" -> {
                    TopAppBar(
                        title = { Text("습관 추적기") }
                    )
                }
            }
        },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = navBackStackEntry?.destination?.hierarchy?.any { it.route == item.route } == true,
                            onClick = {
                                navController.navigate(item.route) {
                                    // 백스택을 정리하여 시작 화면으로 돌아갈 때 중복 방지
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    // 같은 아이템을 다시 선택했을 때 중복 방지
                                    launchSingleTop = true
                                    // 이전 상태 복원
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues)
        ) {
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
            // selectedReminder = null 제거 (버그 수정)
            HomeScreen(
                viewModel = viewModel,
                onAddClick = { navController.navigate("add_edit") },
                onReminderClick = { reminder ->
                    selectedReminder = reminder
                    navController.navigate("add_edit")
                },
                onEisenhowerMatrixClick = { navController.navigate("eisenhower_matrix") },
                onSettingsClick = { navController.navigate("settings") }, // v1.52.0
                simpleMode = userPreferences.simpleMode,
                onTopAppBarContent = { content ->
                    homeScreenTopAppBarContent = content
                }
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
                onNavigateBack = {
                    selectedReminder = null // 뒤로가기 시 초기화
                    navController.popBackStack()
                },
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
                onHelpClick = { navController.navigate("help") },
                onCalendarSyncClick = { navController.navigate("calendar_sync") },
                onArchiveClick = { navController.navigate("archive") }
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
        // v1.40.1: CalendarSyncScreen 라우트
        composable(
            "calendar_sync",
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
            CalendarSyncScreen(
                viewModel = calendarSyncViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        // v1.43.0: ArchiveScreen 라우트
        composable(
            "archive",
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
            ArchiveScreen(
                viewModel = archiveViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        // v1.44.0: HabitTrackerScreen 라우트
        composable(
            "habit_tracker",
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
            HabitTrackerScreen(
                viewModel = habitViewModel
            )
        }
        // v1.46.0: PomodoroScreen 라우트
        composable(
            "pomodoro",
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
            PomodoroScreen(
                viewModel = pomodoroViewModel
            )
        }
        // v1.47.0: EisenhowerMatrixScreen 라우트
        composable(
            "eisenhower_matrix",
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
            EisenhowerMatrixScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onReminderClick = { reminder ->
                    selectedReminder = reminder
                    navController.navigate("add_edit")
                },
                onNavigateToFocusMode = { navController.navigate("focus_mode") }
            )
        }
        // v1.51.0: FocusModeScreen 라우트
        composable(
            "focus_mode",
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
            FocusModeScreen(
                application = app
            )
        }
        }
    }
}
