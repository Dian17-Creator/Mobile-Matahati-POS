package id.my.matahati.pos.ui.screen.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import id.my.matahati.pos.model.TransactionData
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ReceiptDialog(
    transactionData: TransactionData,
    cashierName: String,
    onDismiss: () -> Unit
) {
    val trx = transactionData.transaction ?: return
    val details = transactionData.details ?: emptyList()
    
    val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID")).apply {
        maximumFractionDigits = 0
    }
    
    fun formatStringNum(str: String): String {
        return formatter.format(str.toDoubleOrNull() ?: 0.0)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "OUTLET MH TA",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    TextRow("No. Trx", trx.transactionNo)
                    TextRow("Waktu", trx.transactionDate) // Simplified
                    TextRow("Kasir", cashierName)
                    TextRow("Order", trx.orderType)
                    if (trx.customerName != null) {
                        TextRow("Customer", trx.customerName)
                    }
                    if (trx.tableName != null) {
                        TextRow("Meja", trx.tableName)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color.Black, thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                    items(details) { item ->
                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Text(text = item.productName, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(
                                    text = "${item.quantity} x ${formatStringNum(item.price)}",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = formatStringNum(item.subtotal),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp
                                )
                            }
                            if (!item.note.isNullOrBlank()) {
                                Text(
                                    text = "Note: ${item.note}",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = Color.DarkGray
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color.Black, thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    RowAmount("Subtotal", formatStringNum(trx.subtotal))
                    RowAmount("Discount", formatStringNum(trx.discount))
                    RowAmount("Tax", formatStringNum(trx.tax))
                    Spacer(modifier = Modifier.height(4.dp))
                    HorizontalDivider(color = Color.Black, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(4.dp))
                    RowAmount("GRAND TOTAL", formatStringNum(trx.grandTotal), true)
                    RowAmount("Paid", formatStringNum(trx.paidAmount))
                    RowAmount("Change", formatStringNum(trx.changeAmount))
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Status: ${trx.status}",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Tutup")
                }
            }
        }
    }
}

@Composable
fun TextRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(text = "$label: ", fontFamily = FontFamily.Monospace, fontSize = 12.sp, modifier = Modifier.width(80.dp))
        Text(text = value, fontFamily = FontFamily.Monospace, fontSize = 12.sp, modifier = Modifier.weight(1f))
    }
}

@Composable
fun RowAmount(label: String, amount: String, isBold: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            text = label,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = amount,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
        )
    }
}
