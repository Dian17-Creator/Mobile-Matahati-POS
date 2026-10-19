package id.my.matahati.pos.ui.screen.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import id.my.matahati.pos.model.CartItem
import id.my.matahati.pos.model.TransactionModel
import id.my.matahati.pos.ui.theme.AppPrimaryColor
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun PesananRealtimeDialog(
    activeOrders: List<TransactionModel>,
    completedOrders: List<TransactionModel>,
    isLoading: Boolean = false,
    onRefresh: () -> Unit,
    onMarkDone: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Pesanan Sekarang, 1 = Riwayat

    val configuration = LocalConfiguration.current
    val isPhone = configuration.screenWidthDp < 600

    val numberFormatter = remember {
        NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID")).apply {
            maximumFractionDigits = 0
        }
    }

    fun formatAmount(amountStr: String?): String {
        val num = amountStr?.toDoubleOrNull() ?: 0.0
        return "Rp ${numberFormatter.format(num)}"
    }

    fun formatTime(dateStr: String?): String {
        if (dateStr.isNullOrBlank()) return "-"
        return try {
            val inputFormat = if (dateStr.contains("T")) {
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            } else {
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            }
            val outputFormat = SimpleDateFormat("HH:mm", Locale.forLanguageTag("id-ID"))
            val parsed = inputFormat.parse(dateStr.replace(".000000Z", "").replace("Z", ""))
            if (parsed != null) outputFormat.format(parsed) else dateStr
        } catch (_: Exception) {
            dateStr
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = !isPhone)
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            modifier = if (isPhone) {
                Modifier
                    .fillMaxWidth()
                    .heightIn(max = 680.dp)
                    .padding(16.dp)
            } else {
                Modifier
                    .width(520.dp)
                    .heightIn(max = 680.dp)
                    .padding(12.dp)
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header Dialog
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Detail Pesanan Real-Time",
                        fontSize = if (isPhone) 16.sp else 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onRefresh,
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = AppPrimaryColor
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh Pesanan",
                                    tint = AppPrimaryColor
                                )
                            }
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Tutup",
                                tint = Color.Gray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tab Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF1F3F5), RoundedCornerShape(8.dp))
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (selectedTab == 0) AppPrimaryColor else Color.Transparent,
                                RoundedCornerShape(6.dp)
                            )
                            .clickable { selectedTab = 0 }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isPhone) "Pesanan (${activeOrders.size})" else "Pesanan Sekarang (${activeOrders.size})",
                            fontSize = if (isPhone) 12.sp else 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == 0) Color.White else Color.DarkGray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (selectedTab == 1) AppPrimaryColor else Color.Transparent,
                                RoundedCornerShape(6.dp)
                            )
                            .clickable { selectedTab = 1 }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Riwayat (${completedOrders.size})",
                            fontSize = if (isPhone) 12.sp else 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == 1) Color.White else Color.DarkGray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // List Items
                Box(modifier = Modifier.weight(1f)) {
                    val currentList = if (selectedTab == 0) activeOrders else completedOrders

                    if (currentList.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (selectedTab == 0) "Belum ada pesanan aktif sekarang." else "Belum ada riwayat pesanan selesai.",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(currentList, key = { it.id }) { order ->
                                OrderRealtimeCard(
                                    order = order,
                                    isCompletedTab = (selectedTab == 1),
                                    formattedTime = formatTime(order.transactionDate),
                                    formattedTotal = formatAmount(order.grandTotal),
                                    onMarkDone = { onMarkDone(order.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderRealtimeCard(
    order: TransactionModel,
    isCompletedTab: Boolean,
    formattedTime: String,
    formattedTotal: String,
    onMarkDone: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Card: No Trx & Waktu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "No: ${order.transactionNo}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• $formattedTime",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                val displayOrderType = (order.orderType ?: "OFFLINE").replace("_", "-").uppercase()
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (displayOrderType.contains("TAKE")) Color(0xFFFFF3E0) else Color(0xFFE8F5E9)
                ) {
                    Text(
                        text = displayOrderType,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (displayOrderType.contains("TAKE")) Color(0xFFE65100) else Color(0xFF2E7D32),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Customer / Table info
            val custInfo = order.customerName?.takeIf { it.isNotBlank() } ?: "Pelanggan General"
            val tableInfo = order.tableName?.takeIf { it.isNotBlank() }?.let { " (Meja: $it)" } ?: ""
            Text(
                text = "$custInfo$tableInfo",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.DarkGray,
                modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
            )

            HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // Details List
            val details = order.details ?: emptyList()
            details.forEach { detail ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${detail.quantity}x ${detail.productName}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (!detail.note.isNullOrBlank()) {
                    val (cleanNote, _) = CartItem.parseNoteAndSentQty(detail.note)
                    if (cleanNote.isNotBlank()) {
                        Text(
                            text = "Note: $cleanNote",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }

            if (!order.orderNote.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Catatan Pesanan: ${order.orderNote}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppPrimaryColor
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Row: Total Amount & Checkbox / Selesai
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total: $formattedTotal",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppPrimaryColor
                )

                if (isCompletedTab) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Selesai",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Disajikan",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
                        )
                    }
                } else {
                    Button(
                        onClick = onMarkDone,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Mark Selesai",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Selesai",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
