package id.my.matahati.pos.ui.screen.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import id.my.matahati.pos.model.TransactionModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun TransactionHistoryList(
    transactions: List<TransactionModel>,
    isLoading: Boolean,
    onTransactionClick: (TransactionModel) -> Unit,
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
            // Simple split for "2026-09-11T..." -> "2026-09-11"
            it.transactionDate.substringBefore("T")
        } catch (_: Exception) {
            "Unknown"
        }
    }

    LazyColumn(modifier = modifier.fillMaxSize()) {
        grouped.forEach { (date, items) ->
            item {
                TransactionDateHeader(date)
            }
            items(items) { trx ->
                TransactionItemCard(trx, onClick = { onTransactionClick(trx) })
                HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
fun TransactionDateHeader(date: String) {
    Surface(
        color = Color(0xFF66BB6A), // Green header similar to image 2
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = date,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun TransactionItemCard(
    transaction: TransactionModel,
    onClick: () -> Unit
) {
    val isCancelled = transaction.status == "CANCELLED"
    
    val timeStr = try {
        // "2026-09-11T13:53:34.000000Z" -> "13:53"
        val timePart = transaction.transactionDate.substringAfter("T").substringBefore(":")
        val minutePart = transaction.transactionDate.substringAfter("T").substringAfter(":").substringBefore(":")
        "$timePart:$minutePart"
    } catch (e: Exception) {
        ""
    }

    val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID")).apply {
        maximumFractionDigits = 0
    }
    val amountStr = formatter.format(transaction.grandTotal.toDoubleOrNull() ?: 0.0)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon
        Surface(
            shape = CircleShape,
            color = if (isCancelled) Color(0xFFFEEBEE) else Color(0xFFE3F2FD),
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (isCancelled) Icons.Default.Block else Icons.Default.Receipt,
                    contentDescription = null,
                    tint = if (isCancelled) Color.Red else OlseraBlueHeader,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.transactionNo,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray
            )
            Text(
                text = if (transaction.customerName.isNullOrBlank()) "Pelanggan Umum" else transaction.customerName,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        // Amount & Time
        Column(horizontalAlignment = Alignment.End) {
            Text(text = timeStr, fontSize = 12.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isCancelled) "0 (Dibatalkan)" else "Rp $amountStr",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCancelled) Color.Red else Color.Black
                )
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
            }
        }
    }
}


