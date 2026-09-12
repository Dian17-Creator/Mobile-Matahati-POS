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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.my.matahati.pos.model.TransactionModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun TransactionDetailScreen(
    transaction: TransactionModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID")).apply {
        maximumFractionDigits = 0
    }

    fun formatStringNum(str: String): String {
        return formatter.format(str.toDoubleOrNull() ?: 0.0)
    }

    val isCancelled = transaction.status == "CANCELLED"
    val cashierName = transaction.posUser?.user?.name ?: "Unknown"

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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Detail Transaksi",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color.White)
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
                    text = transaction.transactionDate.replace("T", " ").substringBefore(".")
                )
                InfoIconRow(
                    icon = Icons.Default.Person,
                    text = "($cashierName)"
                )
                InfoIconRow(
                    icon = Icons.Default.Restaurant,
                    text = "${transaction.orderType} (${transaction.visitorCount} Pax)"
                )
                InfoIconRow(
                    icon = Icons.Default.AttachMoney,
                    text = if (isCancelled) "null (VOIDED)" else "Rp ${formatStringNum(transaction.grandTotal)}",
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
                        modifier = Modifier.weight(0.5f)
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
                        modifier = Modifier.weight(0.3f)
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
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.productName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(0.5f)
                            )
                            Text(
                                text = "${item.quantity}x",
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(0.2f)
                            )
                            Text(
                                text = formatStringNum(item.subtotal),
                                fontSize = 14.sp,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(0.3f)
                            )
                        }
                        if (!item.note.isNullOrBlank()) {
                            Text(
                                text = "Note: ${item.note}",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(start = 16.dp, bottom = 12.dp)
                            )
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

                        // Catatan Batal (White background like Item List or separate)
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

                        // Dilayani Oleh row with background
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
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Total Left
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total",
                        fontSize = 14.sp,
                        color = Color(0xFF1565C0),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Rp ${formatStringNum(transaction.grandTotal)}",
                        fontSize = 22.sp,
                        color = Color(0xFF1565C0),
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                // Option Button Right
                Surface(
                    color = Color(0xFF1565C0),
                    modifier = Modifier
                        .width(64.dp)
                        .fillMaxHeight()
                        .clickable { /* Opsi action */ }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.List,
                            contentDescription = "Opsi",
                            tint = Color.White
                        )
                    }
                }
            }
        }
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


