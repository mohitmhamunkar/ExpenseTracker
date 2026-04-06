package edu.northeastern.expensetracker.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import edu.northeastern.expensetracker.domain.model.Transaction
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

@Composable
fun TransactionItem(
    transaction: Transaction,
    homeCurrency: String // <--- THIS IS THE FIX: Added the parameter here!
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Category Icon
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = getCategoryIcon(transaction.categoryId),
                contentDescription = transaction.categoryId,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // 2. Category Name & Notes
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.categoryId,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            if (transaction.notes.isNotBlank()) {
                Text(
                    text = transaction.notes,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 3. The Amounts
        Column(horizontalAlignment = Alignment.End) {
            if (!transaction.isSynced) {
                // If offline, show the original amount formatted to 2 decimals
                Text(
                    text = "${transaction.originalCurrency} ${"%.2f".format(transaction.originalAmount)}",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Pending Sync...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                // It synced successfully, show the converted Home Currency formatted to 2 decimals
                Text(
                    text = "$homeCurrency ${"%.2f".format(transaction.baseAmount)}",
                    style = MaterialTheme.typography.titleMedium
                )
                // If it was a foreign transaction, show the original amount underneath
                if (transaction.originalCurrency != homeCurrency) {
                    Text(
                        text = "${transaction.originalCurrency} ${"%.2f".format(transaction.originalAmount)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// Your existing icon helper
fun getCategoryIcon(category: String): ImageVector {
    return when (category) {
        "Food" -> Icons.Default.ShoppingCart
        "Transport" -> Icons.Default.Commute
        "Bills" -> Icons.Default.Receipt
        "Entertainment" -> Icons.Default.Movie
        else -> Icons.AutoMirrored.Filled.List
    }
}

// Helper functions for later UI polish
fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    format.maximumFractionDigits = 2
    return format.format(amount)
}

fun getCurrencySymbol(currencyCode: String): String {
    return try {
        Currency.getInstance(currencyCode).symbol
    } catch (e: Exception) {
        currencyCode
    }
}