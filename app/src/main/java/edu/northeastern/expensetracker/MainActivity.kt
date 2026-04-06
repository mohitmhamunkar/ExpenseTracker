package edu.northeastern.expensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import edu.northeastern.expensetracker.presentation.home.ExpenseViewModel
import edu.northeastern.expensetracker.presentation.navigation.Screen
import edu.northeastern.expensetracker.presentation.screens.AddTransactionScreen
import edu.northeastern.expensetracker.presentation.screens.HomeScreen
import edu.northeastern.expensetracker.presentation.screens.AnalyticsScreen
import edu.northeastern.expensetracker.presentation.settings.SettingsScreen
import edu.northeastern.expensetracker.ui.theme.ExpenseTrackerTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ExpenseTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    // 1. Create ONE shared ViewModel for the entire app
                    val sharedViewModel: ExpenseViewModel = hiltViewModel()

                    NavHost(
                        navController = navController,
                        startDestination = Screen.Home.route
                    ) {
                        // 2. Pass the shared instance to every screen
                        composable(route = Screen.Home.route) {
                            HomeScreen(
                                navController = navController,
                                viewModel = sharedViewModel,
                                onNavigateToSettings = { navController.navigate("settings") }
                            )
                        }

                        composable(route = Screen.AddTransaction.route) {
                            AddTransactionScreen(
                                navController = navController,
                                viewModel = sharedViewModel
                            )
                        }

                        composable(route = Screen.Analytics.route) {
                            AnalyticsScreen(
                                navController = navController,
                                viewModel = sharedViewModel
                            )
                        }

                        composable("settings") {
                            SettingsScreen()
                        }
                    }
                }
            }
        }
    }
}