package edu.northeastern.expensetracker.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.flow.collectLatest
import edu.northeastern.expensetracker.presentation.home.ExpenseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    navController: NavController,
    viewModel: ExpenseViewModel = hiltViewModel()
) {
    var amountInput by remember { mutableStateOf("") }
    var noteInput by remember { mutableStateOf("") }

    // 1. Currency Dropdown State
    val currencies = listOf("INR", "USD", "EUR", "GBP")
    var expandedCurrency by remember { mutableStateOf(false) }
    var selectedCurrency by remember { mutableStateOf(currencies[0]) } // Defaults to INR

    // 2. Category Dropdown State (Matching your icon names)
    val categories = listOf("Food", "Transport", "Bills", "Entertainment", "Shopping")
    var expandedCategory by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(categories[0]) }

    // --- ADD THIS BLOCK ---
    val context = LocalContext.current.applicationContext
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            if (event != "SUCCESS") {
                Toast.makeText(context, event, Toast.LENGTH_LONG).show()
            }
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Expense") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // CURRENCY & AMOUNT ROW
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Currency Selector
                ExposedDropdownMenuBox(
                    expanded = expandedCurrency,
                    onExpandedChange = { expandedCurrency = !expandedCurrency },
                    modifier = Modifier.weight(1f) // Takes up 1/3 of the row
                ) {
                    OutlinedTextField(
                        value = selectedCurrency,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Currency") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCurrency) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCurrency,
                        onDismissRequest = { expandedCurrency = false }
                    ) {
                        currencies.forEach { currency ->
                            DropdownMenuItem(
                                text = { Text(currency) },
                                onClick = {
                                    selectedCurrency = currency
                                    expandedCurrency = false
                                }
                            )
                        }
                    }
                }

                // Amount Input
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(2f) // Takes up 2/3 of the row
                )
            }

            // CATEGORY SELECTOR
            ExposedDropdownMenuBox(
                expanded = expandedCategory,
                onExpandedChange = { expandedCategory = !expandedCategory },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedCategory,
                    onDismissRequest = { expandedCategory = false }
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                selectedCategory = category
                                expandedCategory = false
                            }
                        )
                    }
                }
            }

            // NOTES INPUT
            OutlinedTextField(
                value = noteInput,
                onValueChange = { noteInput = it },
                label = { Text("Notes (Optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f)) // Pushes the button to the bottom

            // THE FIX: The Save Button now calls the new ViewModel logic!
            Button(
                onClick = {
                    val amount = amountInput.toDoubleOrNull()
                    if (amount != null && amount > 0) {
                        // Pass the raw data to the ViewModel, let it do the math and API calls
                        viewModel.addTransaction(
                            amount = amount,
                            selectedCurrency = selectedCurrency,
                            categoryId = selectedCategory,
                            notes = noteInput
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Save Expense")
            }
        }
    }
}