package id.my.matahati.pos.ui.screen.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.my.matahati.pos.model.TransactionDetailModel
import id.my.matahati.pos.model.TransactionModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun TransactionDetailScreen(
    transaction: TransactionModel,
    onBack: () -> Unit = {},
    showBackButton: Boolean = true,
    currentDate: String = "",
    onDateClick: (() -> Unit)? = null,
    onSendToKitchen: () -> Unit = {},
    onVoidRefundClick: () -> Unit = {},
    onItemVoidRefundClick: ((TransactionDetailModel?) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID")).apply {
        maximumFractionDigits = 0
    }

    fun formatStringNum(str: String): String {
        return formatter.format(str.toDoubleOrNull() ?: 0.0)
    }

    val statusUpper = transaction.status.uppercase()
    val isCancelled = statusUpper in listOf("CANCELLED", "CANCEL", "VOID", "VOIDED", "REFUND", "REFUNDED")
    val cashierName = transaction.posUser?.user?.name ?: "Unknown"
    val hasPartialHistory = transaction.details?.any { it.qtyVoid > 0 || it.qtyRefund > 0 } == true

    val detailsList = transaction.details ?: emptyList()
    val activeItemsTotal = detailsList.sumOf { item ->
        val unitPrice = item.price.toDoubleOrNull() ?: 0.0
        unitPrice * item.qtyAvailable
    }
    val taxVal = transaction.tax.toDoubleOrNull() ?: 0.0
    val discountVal = transaction.discount.toDoubleOrNull() ?: 0.0
    val displayGrandTotal = if (detailsList.isNotEmpty()) {
        maxOf(0.0, activeItemsTotal - discountVal + taxVal)
    } else {
        transaction.grandTotal.toDoubleOrNull() ?: 0.0
    }

    var showPartialActionWarningDialog by remember { mutableStateOf(false) }

    fun formatTransactionDate(rawDate: String): String {
        if (rawDate.isBlank()) return ""
        return try {
            val cleanDate = rawDate
                .replace("T", " ")
                .replace("Z", "")
                .substringBefore(".")
                .trim()

            val localeId = Locale.forLanguageTag("id-ID")
            val inputPatterns = listOf(
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd HH:mm",
                "yyyy-MM-dd"
            )

            var parsedDate: java.util.Date? = null
            var matchedPattern = ""

            for (pattern in inputPatterns) {
                try {
                    val sdfInput = SimpleDateFormat(pattern, Locale.US)
                    val d = sdfInput.parse(cleanDate)
                    if (d != null) {
                        parsedDate = d
                        matchedPattern = pattern
                        break
                    }
                } catch (_: Exception) {
                }
            }

            if (parsedDate != null) {
                val hasTime = matchedPattern.contains("HH:mm")
                val outputPattern = if (hasTime) "EEEE, dd-MM-yyyy HH:mm" else "EEEE, dd-MM-yyyy"
                val sdfOutput = SimpleDateFormat(outputPattern, localeId)
                sdfOutput.format(parsedDate)
            } else {
                cleanDate
            }
        } catch (e: Exception) {
            rawDate.replace("T", " ").substringBefore(".")
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Header Bar
        Surface(
            color = Color(0xFF1565C0),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = transaction.transactionNo.ifBlank { "Detail Transaksi" },
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onDateClick != null && currentDate.isNotBlank()) {
                        Surface(
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                            color = Color.White.copy(alpha = 0.18f),
                            modifier = Modifier.clickable { onDateClick() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = currentDate,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = "Pilih Tanggal",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    if (showBackButton) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color.White)
                        }
                    }
                }
            }
        }

        // Content
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            // Info Atas
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoIconRow(
                    icon = Icons.Default.AccessTime,
                    text = formatTransactionDate(transaction.transactionDate)
                )
                InfoIconRow(
                    icon = Icons.Default.Person,
                    text = "($cashierName)"
                )
                InfoIconRow(
                    icon = Icons.Default.Restaurant,
                    text = "${transaction.orderType} (${transaction.visitorCount} Pax)"
                )
                if (!transaction.orderNote.isNullOrBlank()) {
                    InfoIconRow(
                        icon = Icons.Default.ChatBubbleOutline,
                        text = "Catatan: ${transaction.orderNote}"
                    )
                }
                InfoIconRow(
                    icon = Icons.Default.AttachMoney,
                    text = if (isCancelled) "null (VOIDED)" else "Rp ${formatStringNum(displayGrandTotal.toString())}",
                    textColor = if (isCancelled) Color.Red else Color.Black
                )
            }

            // Table Header
            Surface(
                color = Color(0xFFDBDBDB),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "Item",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray,
                        modifier = Modifier.weight(0.45f)
                    )
                    Text(
                        text = "Qty",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(0.2f)
                    )
                    Text(
                        text = "Total",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(0.35f)
                    )
                }
            }

            // List Items
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                val details = transaction.details ?: emptyList()
                items(details) { item ->
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(0.45f)) {
                                Text(
                                    text = item.productName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                if (item.qtyVoid > 0 || item.qtyRefund > 0) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        if (item.qtyVoid > 0) {
                                            Surface(
                                                color = Color(0xFFFFEBEE),
                                                shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "${item.qtyVoid}/${item.quantity} Voided",
                                                    fontSize = 10.sp,
                                                    color = Color.Red,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        if (item.qtyRefund > 0) {
                                            Surface(
                                                color = Color(0xFFFFF3E0),
                                                shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "${item.qtyRefund}/${item.quantity} Refunded",
                                                    fontSize = 10.sp,
                                                    color = Color(0xFFE65100),
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Text(
                                text = "${item.quantity}x",
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(0.2f)
                            )

                            Row(
                                modifier = Modifier.weight(0.35f),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val activeSubtotal = (item.price.toDoubleOrNull() ?: 0.0) * item.qtyAvailable
                                Text(
                                    text = formatStringNum(activeSubtotal.toString()),
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.End
                                )

                                if (!isCancelled && onItemVoidRefundClick != null) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    IconButton(
                                        onClick = { onItemVoidRefundClick(item) },
                                        enabled = item.qtyAvailable > 0,
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.RemoveCircleOutline,
                                            contentDescription = "Void/Refund Item",
                                            tint = if (item.qtyAvailable > 0) Color.Red else Color.LightGray,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        if (!item.note.isNullOrBlank()) {
                            val (cleanNote, _) = id.my.matahati.pos.model.CartItem.parseNoteAndSentQty(item.note)
                            if (cleanNote.isNotBlank()) {
                                Text(
                                    text = "Note: $cleanNote",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(start = 16.dp, bottom = 12.dp)
                                )
                            }
                        }
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                    }
                }

                // Summary inside LazyColumn to scroll together
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.Top
                    ) {
                        // Pajak row with background
                        Surface(
                            color = Color(0xFFF3F3F3),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "Pajak", fontSize = 12.sp, color = Color.Black)
                                Text(
                                    text = formatStringNum(transaction.tax),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }

                        // Catatan Pesanan row
                        if (!transaction.orderNote.isNullOrBlank()) {
                            Surface(
                                color = Color(0xFFF3F3F3),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "Catatan", fontSize = 12.sp, color = Color.Black)
                                    Text(
                                        text = transaction.orderNote,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black,
                                        textAlign = TextAlign.End
                                    )
                                }
                            }
                        }

                        // Jumlah Item row
                        Surface(
                            color = Color.White,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Jumlah Item: ${transaction.itemCount}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.DarkGray,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }

                        // Catatan Batal
                        if (isCancelled && !transaction.cancelNote.isNullOrBlank()) {
                            Surface(
                                color = Color.White,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Catatan: ${transaction.cancelNote}",
                                    fontSize = 12.sp,
                                    color = Color.Black,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }

                        // Dilayani Oleh row
                        Surface(
                            color = Color(0xFFF3F3F3),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Dilayani Oleh: $cashierName",
                                fontSize = 12.sp,
                                color = Color.Black,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        // Bottom Bar
        Surface(
            color = Color.White,
            shadowElevation = 0.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                HorizontalDivider(color = Color(0xFF1565C0), thickness = 2.dp)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Total Left
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Total",
                            fontSize = 11.sp,
                            color = Color(0xFF1565C0),
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.align(Alignment.TopStart)
                        )
                        Text(
                            text = "Rp ${formatStringNum(displayGrandTotal.toString())}",
                            fontSize = 24.sp,
                            color = Color(0xFF1565C0),
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    // Option Button Right with Pop-up Menu
                    var showOptionsMenu by remember { mutableStateOf(false) }

                    Box {
                        Surface(
                            color = Color(0xFF1565C0),
                            modifier = Modifier
                                .width(64.dp)
                                .fillMaxHeight()
                                .clickable { showOptionsMenu = true }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.List,
                                    contentDescription = "Opsi",
                                    tint = Color.White
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showOptionsMenu,
                            onDismissRequest = { showOptionsMenu = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Restaurant,
                                            contentDescription = null,
                                            tint = Color(0xFF1565C0),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text("Kirim ke Dapur", color = Color.DarkGray)
                                    }
                                },
                                onClick = {
                                    showOptionsMenu = false
                                    onSendToKitchen()
                                }
                            )

                            val canVoidRefund = statusUpper !in listOf("VOID", "REFUND", "CANCELLED", "CANCEL", "VOIDED", "REFUNDED")

                            if (canVoidRefund) {
                                if (onItemVoidRefundClick != null) {
                                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.RemoveCircleOutline,
                                                    contentDescription = null,
                                                    tint = Color(0xFF1565C0),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text("Void / Refund Per Item", color = Color(0xFF1565C0), fontWeight = FontWeight.SemiBold)
                                            }
                                        },
                                        onClick = {
                                            showOptionsMenu = false
                                            onItemVoidRefundClick(null)
                                        }
                                    )
                                }

                                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.MoneyOff,
                                                contentDescription = null,
                                                tint = if (hasPartialHistory) Color.Gray else Color.Red,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text("Pengembalian / Batal (Semua)", color = if (hasPartialHistory) Color.Gray else Color.Red, fontWeight = FontWeight.SemiBold)
                                        }
                                    },
                                    onClick = {
                                        showOptionsMenu = false
                                        if (hasPartialHistory) {
                                            showPartialActionWarningDialog = true
                                        } else {
                                            onVoidRefundClick()
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Warning Dialog for Full Transaction Void/Refund when Partial Actions exist
    if (showPartialActionWarningDialog) {
        AlertDialog(
            onDismissRequest = { showPartialActionWarningDialog = false },
            title = {
                Text(
                    text = "Tidak Dapat Membatalkan Penuh",
                    fontWeight = FontWeight.Bold,
                    color = Color.Red
                )
            },
            text = {
                Text("Transaksi ini sudah memiliki riwayat partial item action, tidak bisa dibatalkan secara penuh sekaligus.")
            },
            confirmButton = {
                Button(
                    onClick = { showPartialActionWarningDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
                ) {
                    Text("Mengerti")
                }
            }
        )
    }
}

@Composable
private fun InfoIconRow(
    icon: ImageVector,
    text: String,
    textColor: Color = Color.Black
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            shape = androidx.compose.foundation.shape.CircleShape,
            color = Color.Transparent,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray.copy(alpha = 0.5f)),
            modifier = Modifier.size(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = text, fontSize = 14.sp, color = textColor)
    }
}
