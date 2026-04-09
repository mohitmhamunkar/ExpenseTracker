package edu.northeastern.expensetracker.presentation.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import edu.northeastern.expensetracker.domain.model.Transaction
import edu.northeastern.expensetracker.presentation.home.ExpenseViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    navController: NavController,
    viewModel: ExpenseViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val currentMonth by viewModel.currentMonth.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()

    val monthlyTransactions = state.transactions.filter {
        it.date.year == currentMonth.year && it.date.month == currentMonth.month
    }

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
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {

            // --- 1. THE MONTH SELECTOR ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.changeMonth(-1) }) {
                    Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Previous Month")
                }
                Text(
                    text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = { viewModel.changeMonth(1) }) {
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Next Month")
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))

            if (monthlyTransactions.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(400.dp), contentAlignment = Alignment.Center) {
                    Text("No data to analyze for ${currentMonth.format(DateTimeFormatter.ofPattern("MMMM"))}.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                Spacer(modifier = Modifier.height(16.dp))

                // --- 2. CATEGORY PIE CHART ---
                Text(
                    text = "Category Breakdown",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                CategoryPieChart(transactions = monthlyTransactions)

                Spacer(modifier = Modifier.height(24.dp))

                // --- 3. UPGRADED DAILY BAR GRAPH ---
                Text(
                    text = "Daily Spending",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                DailySpendingBarChart(
                    transactions = monthlyTransactions,
                    daysInMonth = currentMonth.lengthOfMonth(),
                    currencySymbol = currencySymbol
                )

                Spacer(modifier = Modifier.height(24.dp))

                // --- 4. TOP SPENDING INSIGHTS ---
                Text(
                    text = "Monthly Insights",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                InsightsGrid(transactions = monthlyTransactions, currencySymbol = currencySymbol)

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

// ==========================================
// PIE CHART
// ==========================================
@Composable
fun CategoryPieChart(transactions: List<Transaction>) {
    val categoryTotals = transactions.groupBy { it.categoryId }
        .mapValues { entry -> entry.value.sumOf { it.baseAmount } }

    val totalSpent = categoryTotals.values.sum()

    val colors = listOf(
        Color(0xFF6200EA), Color(0xFF03DAC5), Color(0xFFFF4081),
        Color(0xFFFFC107), Color(0xFF03A9F4), Color(0xFF8BC34A)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Canvas(modifier = Modifier.size(120.dp)) {
                var startAngle = -90f
                categoryTotals.entries.forEachIndexed { index, entry ->
                    val sweepAngle = ((entry.value / totalSpent) * 360f).toFloat()
                    drawArc(
                        color = colors[index % colors.size],
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true
                    )
                    startAngle += sweepAngle
                }
            }

            Spacer(modifier = Modifier.width(24.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                categoryTotals.entries.forEachIndexed { index, entry ->
                    val percentage = (entry.value / totalSpent) * 100
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(colors[index % colors.size])
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${entry.key} (${"%.0f".format(percentage)}%)",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// UPGRADED: DETAILED CANVAS BAR CHART (WITH ROUNDED AXIS)
// ==========================================
@Composable
fun DailySpendingBarChart(
    transactions: List<Transaction>,
    daysInMonth: Int,
    currencySymbol: String
) {
    val dailyTotals = FloatArray(daysInMonth) { 0f }
    transactions.forEach { tx ->
        val dayIndex = tx.date.dayOfMonth - 1
        dailyTotals[dayIndex] += tx.baseAmount.toFloat()
    }

    val rawMax = dailyTotals.maxOrNull()?.takeIf { it > 0f } ?: 1f

    // --- THE FIX: CALCULATE A "NICE" UPPER LIMIT ---
    // Dynamically choose what to round by based on the size of the spend
    val roundFactor = when {
        rawMax > 10000 -> 1000f
        rawMax > 1000 -> 500f
        rawMax > 100 -> 100f
        else -> 10f
    }

    // Divide, round up to the nearest whole number, then multiply back
    val chartMax = kotlin.math.ceil((rawMax / roundFactor).toDouble()).toFloat() * roundFactor

    val barColor = MaterialTheme.colorScheme.primary

    val textMeasurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.labelSmall.copy(
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(240.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Using our new rounded 'chartMax' instead of the raw number
            val maxText = "$currencySymbol${chartMax.toInt()}"
            val midText = "$currencySymbol${(chartMax / 2).toInt()}"
            val maxTextResult = textMeasurer.measure(maxText, labelStyle)

            val textHeight = maxTextResult.size.height.toFloat()
            val textWidth = maxTextResult.size.width.toFloat()

            val yAxisWidth = textWidth + 12.dp.toPx()
            val xAxisHeight = 24.dp.toPx()
            val topPadding = textHeight / 2f

            val chartWidth = size.width - yAxisWidth
            val chartHeight = size.height - xAxisHeight - topPadding

            val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            val gridColor = Color.Gray.copy(alpha = 0.3f)

            // 1. Draw Y-Axis Labels
            val topY = topPadding
            drawText(textMeasurer = textMeasurer, text = maxText, style = labelStyle, topLeft = Offset(0f, topY - (textHeight / 2f)))
            drawLine(color = gridColor, start = Offset(yAxisWidth, topY), end = Offset(size.width, topY), pathEffect = pathEffect, strokeWidth = 2f)

            val midY = topPadding + (chartHeight / 2f)
            drawText(textMeasurer = textMeasurer, text = midText, style = labelStyle, topLeft = Offset(0f, midY - (textHeight / 2f)))
            drawLine(color = gridColor, start = Offset(yAxisWidth, midY), end = Offset(size.width, midY), pathEffect = pathEffect, strokeWidth = 2f)

            val bottomY = topPadding + chartHeight
            drawText(textMeasurer = textMeasurer, text = "$currencySymbol 0", style = labelStyle, topLeft = Offset(0f, bottomY - (textHeight / 2f)))
            drawLine(color = gridColor, start = Offset(yAxisWidth, bottomY), end = Offset(size.width, bottomY), strokeWidth = 2f)

            // 2. Draw Bars and X-Axis Labels
            val barWidth = chartWidth / (daysInMonth * 1.5f)
            val spacing = (chartWidth - (barWidth * daysInMonth)) / (daysInMonth - 1)

            dailyTotals.forEachIndexed { index, amount ->
                val xOffset = yAxisWidth + (index * (barWidth + spacing))

                // Calculate height against the new 'chartMax' limit
                val barHeight = (amount / chartMax) * chartHeight
                if (barHeight > 0) {
                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(xOffset, bottomY - barHeight),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                    )
                }

                val day = index + 1
                if (day == 1 || day % 5 == 0 || day == daysInMonth) {
                    val dayTextResult = textMeasurer.measure(day.toString(), labelStyle)
                    val textX = xOffset + (barWidth / 2f) - (dayTextResult.size.width / 2f)
                    val safeTextX = textX.coerceAtMost(size.width - dayTextResult.size.width)

                    drawText(
                        textMeasurer = textMeasurer,
                        text = day.toString(),
                        style = labelStyle,
                        topLeft = Offset(
                            x = safeTextX,
                            y = bottomY + 8.dp.toPx()
                        )
                    )
                }
            }
        }
    }
}

// ==========================================
// INSIGHTS
// ==========================================
@Composable
fun InsightsGrid(transactions: List<Transaction>, currencySymbol: String) {
    val totalSpent = transactions.sumOf { it.baseAmount }

    val topCategory = transactions
        .groupBy { it.categoryId }
        .maxByOrNull { entry -> entry.value.sumOf { it.baseAmount } }

    val activeDaysCount = transactions.map { it.date.dayOfMonth }.distinct().size
    val dailyAverage = if (activeDaysCount > 0) totalSpent / activeDaysCount else 0.0

    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        InsightRow(
            title = "Total Spent",
            value = "$currencySymbol ${"%.2f".format(totalSpent)}",
            isHighlight = true
        )

        topCategory?.let {
            InsightRow(
                title = "Top Category",
                value = "${it.key} ($currencySymbol ${"%.2f".format(it.value.sumOf { tx -> tx.baseAmount })})"
            )
        }

        if (dailyAverage > 0.0) {
            InsightRow(
                title = "Daily Average (Active Days)",
                value = "$currencySymbol ${"%.2f".format(dailyAverage)}"
            )
        }
    }
}

@Composable
fun InsightRow(title: String, value: String, isHighlight: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (isHighlight) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isHighlight) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (isHighlight) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
    }
}