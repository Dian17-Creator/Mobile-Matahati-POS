package id.my.matahati.pos.ui.screen.home.components

import id.my.matahati.pos.ui.theme.AppPrimaryColor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.my.matahati.pos.model.TransactionModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun TransactionHistoryList(
    transactions: List<TransactionModel>,
    isLoading: Boolean,
    onTransactionClick: (TransactionModel) -> Unit,
    selectedTransactionId: String? = null,
    modifier: Modifier = Modifier
) {
    if (isLoading && transactions.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (transactions.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Tidak ada transaksi ditemukan", color = Color.Gray)
        }
        return
    }

    // Grouping by date (Safe for API 24)
    val grouped = transactions.groupBy {
        try {
            if (it.transactionDate.length >= 10) {
                it.transactionDate.substring(0, 10)
            } else {
                it.transactionDate
            }
        } catch (_: Exception) {
            "Unknown"
        }
    }

    fun formatDateHeader(dateStr: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("id-ID"))
            val date = inputFormat.parse(dateStr)
            if (date != null) outputFormat.format(date) else dateStr
        } catch (e: Exception) {
            dateStr
        }
    }

    LazyColumn(modifier = modifier.fillMaxSize()) {
        grouped.forEach { (date, items) ->
            item {
                TransactionDateHeader(formatDateHeader(date))
            }
            items(items, key = { it.id }) { trx ->
                val isSelected = (trx.id == selectedTransactionId)
                TransactionItemCard(
                    transaction = trx,
                    isSelected = isSelected,
                    onClick = { onTransactionClick(trx) }
                )
                HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
fun TransactionDateHeader(date: String) {
    Surface(
        color = Color(0xFF66BB6A), // Green header
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = date,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun TransactionItemCard(
    transaction: TransactionModel,
    onClick: () -> Unit,
    isSelected: Boolean = false
) {
    val statusUpper = transaction.status.uppercase()
    val isCancelled = statusUpper in listOf("CANCELLED", "CANCEL", "VOID", "VOIDED", "REFUND", "REFUNDED")

    val timeStr = try {
        val dateTime = transaction.transactionDate.replace("T", " ")
        if (dateTime.contains(" ")) {
            dateTime.substringAfter(" ").substring(0, 5)
        } else {
            ""
        }
    } catch (e: Exception) {
        ""
    }

    val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID")).apply {
        maximumFractionDigits = 0
    }
    val amountNum = transaction.grandTotal.toDoubleOrNull() ?: 0.0
    val amountStr = formatter.format(amountNum)
    val paymentName = transaction.payment?.cname?.uppercase() ?: "CASH"
    val customerStr = if (!transaction.customerName.isNullOrBlank()) " (${transaction.customerName})" else ""
    val itemsSummary = transaction.details?.joinToString(", ") { "${it.quantity}x ${it.productName}" } ?: ""

    val cardBg = if (isSelected) Color(0xFFEBF3FA) else Color.White

    Surface(
        color = cardBg,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Surface(
                shape = CircleShape,
                color = if (isCancelled) Color(0xFFFEEBEE) else Color(0xFFE0F7FA),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isCancelled) Icons.Default.Block else Icons.Default.Receipt,
                        contentDescription = null,
                        tint = if (isCancelled) Color.Red else AppPrimaryColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Main Details
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = transaction.transactionNo,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF222222)
                    )
                    Text(text = timeStr, fontSize = 11.sp, color = Color.Gray)
                }

                if (itemsSummary.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = itemsSummary,
                        fontSize = 11.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (!transaction.orderNote.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Catatan: ${transaction.orderNote}",
                        fontSize = 11.sp,
                        color = Color(0xFFE65100),
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = if (isCancelled) "0 (Dibatalkan) - $paymentName" else "$amountStr - $paymentName$customerStr",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCancelled) Color.Red else AppPrimaryColor
                )
            }
        }
    }
}
