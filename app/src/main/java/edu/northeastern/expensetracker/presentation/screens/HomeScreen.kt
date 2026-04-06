package edu.northeastern.expensetracker.presentation.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
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
    viewModel: ExpenseViewModel = hiltViewModel(),
    onNavigateToSettings: () -> Unit
) {
    // 1. Listen to the Transactions List
    val state by viewModel.state.collectAsState()

    // 2. THIS IS THE FIX: Listen to the Dynamic Home Currency from DataStore!
    val homeCurrency by viewModel.userHomeCurrency.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expense Tracker") },
                actions = {
                    IconButton(onClick = { onNavigateToSettings() }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }
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
                        // The Date Header
                        item {
                            Text(
                                text = date.toString(),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                            )
                        }

                        // The Transactions (WITH SWIPE-TO-DISMISS)
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
                                // 3. THIS IS THE FIX: Pass the homeCurrency into the item!
                                TransactionItem(
                                    transaction = transaction,
                                    homeCurrency = homeCurrency
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}