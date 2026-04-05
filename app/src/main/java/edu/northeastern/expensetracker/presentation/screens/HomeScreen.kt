package edu.northeastern.expensetracker.presentation.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import edu.northeastern.expensetracker.presentation.components.TransactionItem
import edu.northeastern.expensetracker.presentation.home.ExpenseViewModel
import edu.northeastern.expensetracker.presentation.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: ExpenseViewModel = hiltViewModel()
) {
    // This connects our Compose UI to the Flow in the ViewModel
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expense Tracker") },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Analytics.route) }) {
                        Icon(Icons.Default.PieChart, contentDescription = "Analytics")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors()
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(Screen.AddTransaction.route)
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (state.transactions.isEmpty()) {
                Text(
                    text = "No transactions yet. Click + to add one!",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                // Group transactions by date
                val groupedTransactions = state.transactions.groupBy { it.date }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp) // Leave space for FAB
                ) {
                    groupedTransactions.forEach { (date, transactionsForDate) ->
                        // 1. The Date Header
                        item {
                            Text(
                                text = date.toString(),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                            )
                        }

                        // 2. The Transactions (NOW WRAPPED IN SWIPE-TO-DISMISS)
                        // Added 'key = { it.id }' so Compose doesn't get confused when animating the deletion
                        items(transactionsForDate, key = { it.id }) { transaction ->

                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = { dismissValue ->
                                    if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                        viewModel.deleteTransaction(transaction)
                                        true
                                    } else {
                                        false
                                    }
                                }
                            )

                            SwipeToDismissBox(
                                state = dismissState,
                                enableDismissFromStartToEnd = false, // Only allow right-to-left swipe
                                backgroundContent = {
                                    val color by animateColorAsState(
                                        targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart)
                                            MaterialTheme.colorScheme.errorContainer
                                        else Color.Transparent,
                                        label = "delete_color_anim"
                                    )

                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(color)
                                            .padding(horizontal = 20.dp),
                                        contentAlignment = Alignment.CenterEnd // Trash can sits on the right
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                }
                            ) {
                                // Your actual visual card sits on top of the swipe background!
                                TransactionItem(transaction = transaction)
                            }
                        }
                    }
                }
            }
        }
    }
}