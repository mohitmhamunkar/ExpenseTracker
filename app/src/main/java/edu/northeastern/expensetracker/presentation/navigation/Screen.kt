package edu.northeastern.expensetracker.presentation.navigation

// This seals our routes so we don't make typos later!
sealed class Screen(val route: String) {
    object Home : Screen("home_screen")
    object AddTransaction : Screen("add_transaction_screen")
    object Analytics : Screen("analytics_screen")
}