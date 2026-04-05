package edu.northeastern.expensetracker.presentation.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import edu.northeastern.expensetracker.presentation.components.formatCurrency
import edu.northeastern.expensetracker.presentation.home.ExpenseViewModel

// A helper to assign stable colors to our categories
fun getCategoryColor(category: String): Color {
    return when (category) {
        "Food" -> Color(0xFFE57373)        // Red/Pink
        "Transport" -> Color(0xFF64B5F6)   // Blue
        "Bills" -> Color(0xFFFFB74D)       // Orange
        "Entertainment" -> Color(0xFF9575CD)// Purple
        "Shopping" -> Color(0xFF4DB6AC)    // Teal
        else -> Color(0xFFA1887F)          // Brown
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    navController: NavController,
    viewModel: ExpenseViewModel = hiltViewModel()
) {
    val state = viewModel.state.value

    // 1. Crunch the numbers: Group by category and sum the amounts
    val categoryTotals = state.transactions
        .groupBy { it.categoryId }
        .mapValues { entry -> entry.value.sumOf { it.baseAmount } }
        .toList()
        .sortedByDescending { it.second } // Sort highest spend to lowest

    val totalSpend = categoryTotals.sumOf { it.second }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analytics") },
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
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            if (totalSpend == 0.0) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No data to display yet.")
                }
                return@Scaffold
            }

            // 2. The Donut Chart (Canvas)
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    var startAngle = -90f // Start at the top (12 o'clock)
                    val strokeWidth = 100f // Thickness of the donut

                    categoryTotals.forEach { (category, amount) ->
                        val sweepAngle = ((amount / totalSpend) * 360f).toFloat()

                        drawArc(
                            color = getCategoryColor(category),
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                        )
                        startAngle += sweepAngle
                    }
                }

                // Text inside the donut hole
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total Spent", style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = formatCurrency(totalSpend),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. The Legend (List of categories and amounts)
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                items(categoryTotals) { (category, amount) ->
                    val percentage = (amount / totalSpend) * 100

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Color Dot
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(getCategoryColor(category))
                        )
                        Spacer(modifier = Modifier.width(16.dp))

                        // Category Name
                        Text(
                            text = category,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )

                        // Percentage and Amount
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = formatCurrency(amount),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = String.format("%.1f%%", percentage),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                }
            }
        }
    }
}