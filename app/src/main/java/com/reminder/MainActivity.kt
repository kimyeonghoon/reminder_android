package com.reminder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.reminder.data.entity.ReminderEntity
import com.reminder.ui.screen.AddEditReminderScreen
import com.reminder.ui.screen.HomeScreen
import com.reminder.ui.theme.ReminderTheme
import com.reminder.viewmodel.ReminderViewModel
import com.reminder.viewmodel.ReminderViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ReminderTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ReminderApp()
                }
            }
        }
    }
}

@Composable
fun ReminderApp() {
    val navController = rememberNavController()
    val viewModel: ReminderViewModel = viewModel(
        factory = ReminderViewModelFactory(
            (navController.context as MainActivity).application as ReminderApplication
        )
    )
    var selectedReminder by remember { mutableStateOf<ReminderEntity?>(null) }

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            selectedReminder = null
            HomeScreen(
                viewModel = viewModel,
                onAddClick = { navController.navigate("add_edit") },
                onReminderClick = { reminder ->
                    selectedReminder = reminder
                    navController.navigate("add_edit")
                }
            )
        }
        composable("add_edit") {
            AddEditReminderScreen(
                viewModel = viewModel,
                reminder = selectedReminder,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
