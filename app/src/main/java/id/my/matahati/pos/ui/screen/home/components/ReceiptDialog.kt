package id.my.matahati.pos.ui.screen.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import id.my.matahati.pos.model.TransactionData
import id.my.matahati.pos.ui.theme.AppPrimaryColor
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ReceiptDialog(
    transactionData: TransactionData,
    cashierName: String,
    savedOutletName: String? = null,
    paymentMethods: List<id.my.matahati.pos.model.PaymentMethod> = emptyList(),
    onPrint: () -> Unit = {},
    isPrinting: Boolean = false,
    isOrderTypeSelected: Boolean = true,
    isPrintPreviewEnabled: Boolean = true,
    isPdfDialogEnabled: Boolean = true,
    onDismiss: () -> Unit
) {
    val trx = transactionData.transaction ?: return
    val details = transactionData.details ?: emptyList()
    val context = LocalContext.current

    var showPreviewModal by remember(trx.id, isPrintPreviewEnabled) { mutableStateOf(isPrintPreviewEnabled) }
    var showPdfConfirm by remember(trx.id, isPrintPreviewEnabled, isPdfDialogEnabled) { mutableStateOf(!isPrintPreviewEnabled && isPdfDialogEnabled) }
    
    // Prioritize cashier name from transaction data if available
    val finalCashierName = trx.posUser?.user?.name ?: cashierName
    
    val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID")).apply {
        maximumFractionDigits = 0
    }
    
    fun formatStringNum(str: String): String {
        return formatter.format(str.toDoubleOrNull() ?: 0.0)
    }

    fun formatDateTime(dateTime: String): String {
        return try {
            val inputFormat = if (dateTime.contains("T")) {
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            } else {
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            }
            val outputFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale("id", "ID"))
            val date = inputFormat.parse(dateTime.replace(".000000Z", "").replace("Z", ""))
            if (date != null) outputFormat.format(date) else dateTime
        } catch (e: Exception) {
            dateTime.replace("T", " ").replace(".000000Z", "").replace("Z", "")
        }
    }

    if (showPreviewModal) {
        Dialog(
            onDismissRequest = {
                showPreviewModal = false
                if (isPdfDialogEnabled) {
                    showPdfConfirm = true
                } else {
                    onDismiss()
                }
            },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White,
                    modifier = Modifier
                        .widthIn(max = 380.dp)
                        .fillMaxWidth(0.9f)
                        .fillMaxHeight(0.88f)
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Header (Fixed)
                        val outletHeader = (trx.outlet?.name ?: savedOutletName ?: "IQOS MATAHATI TULUNGAGUNG").uppercase()
                        Text(
                            text = outletHeader,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Scrollable Content Area
                        Box(modifier = Modifier.weight(1f, fill = false)) {
                            val scrollState = rememberScrollState()
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(scrollState)
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    TextRow("No. Trx", trx.transactionNo)
                                    TextRow("Waktu", formatDateTime(trx.transactionDate))
                                    TextRow("Kasir", finalCashierName)
                                    if (isOrderTypeSelected && !trx.orderType.isNullOrBlank()) {
                                        TextRow("Order", trx.orderType)
                                    }
                                    if (!trx.customerName.isNullOrBlank()) {
                                        TextRow("Customer", trx.customerName)
                                    }
                                    if (!trx.tableName.isNullOrBlank()) {
                                        TextRow("Meja", trx.tableName)
                                    }
                                    if (!trx.orderNote.isNullOrBlank()) {
                                        TextRow("Catatan", trx.orderNote)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = Color.Black, thickness = 1.dp)
                                Spacer(modifier = Modifier.height(8.dp))

                                details.forEach { item ->
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
                                            val (cleanNote, _) = id.my.matahati.pos.model.CartItem.parseNoteAndSentQty(item.note)
                                            if (cleanNote.isNotBlank()) {
                                                Text(
                                                    text = "Note: $cleanNote",
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

                                    val resolvedPayment1Name = trx.payment?.cname?.takeIf { it.isNotBlank() }
                                        ?: paymentMethods.find { it.id == trx.nidPayment }?.name
                                        ?: "CASH"
                                    val payment1Name = resolvedPayment1Name.uppercase()

                                    val resolvedPayment2Name = trx.paymentDual?.cname?.takeIf { it.isNotBlank() }
                                        ?: paymentMethods.find { it.id == trx.nidPaymentDual }?.name
                                    val payment2Name = resolvedPayment2Name?.uppercase()

                                    val isDual = !payment2Name.isNullOrBlank()
                                    val isDpPending = (trx.status.uppercase() == "PENDING" && trx.remainingAmount > 0) || (trx.dpAmount > 0 && trx.remainingAmount > 0)

                                    if (isDual) {
                                        val totalPaid = trx.paidAmount.toDoubleOrNull() ?: 0.0
                                        val dpVal = if (trx.dpAmount > 0) trx.dpAmount else totalPaid / 2
                                        val secondVal = maxOf(0.0, totalPaid - dpVal)
                                        RowAmount(payment1Name, formatStringNum(dpVal.toString()))
                                        RowAmount(payment2Name, formatStringNum(secondVal.toString()))
                                    } else if (isDpPending) {
                                        val dpVal = if (trx.dpAmount > 0) trx.dpAmount else (trx.paidAmount.toDoubleOrNull() ?: 0.0)
                                        RowAmount("DP ($payment1Name)", formatStringNum(dpVal.toString()))
                                        RowAmount("SISA TAGIHAN", formatStringNum(trx.remainingAmount.toString()))
                                    } else {
                                        Text(
                                            text = payment1Name,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(vertical = 1.dp)
                                        )
                                    }

                                    val totalPaid = trx.paidAmount.toDoubleOrNull() ?: 0.0
                                    val grandTotal = trx.grandTotal.toDoubleOrNull() ?: 0.0
                                    val changeCalculated = maxOf(0.0, totalPaid - grandTotal)
                                    val changeToDisplay = if (changeCalculated > 0) changeCalculated else (trx.changeAmount.toDoubleOrNull() ?: 0.0)

                                    RowAmount("Change", formatStringNum(changeToDisplay.toString()))
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Status: ${trx.status}",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Action Buttons (Fixed at bottom: CETAK & BATAL)
                        Button(
                            onClick = {
                                onPrint()
                                showPreviewModal = false
                                if (isPdfDialogEnabled) {
                                    showPdfConfirm = true
                                } else {
                                    onDismiss()
                                }
                            },
                            enabled = !isPrinting,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)), // Green
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            if (isPrinting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("MENCETAK...", color = Color.White)
                            } else {
                                Text("CETAK KE PRINTER FISIK", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                showPreviewModal = false
                                if (isPdfDialogEnabled) {
                                    showPdfConfirm = true
                                } else {
                                    onDismiss()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("BATAL", color = Color.White)
                        }
                    }
                }
            }
        }
    }

    if (showPdfConfirm) {
        Dialog(
            onDismissRequest = {
                showPdfConfirm = false
                onDismiss()
            },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    modifier = Modifier
                        .width(480.dp)
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "Cetak PDF",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF212121)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Apakah anda ingin cetak PDF?",
                            fontSize = 15.sp,
                            color = Color.DarkGray
                        )

                        Spacer(modifier = Modifier.height(28.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = {
                                    showPdfConfirm = false
                                    onDismiss()
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.DarkGray)
                            ) {
                                Text("TIDAK", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }

                            Button(
                                onClick = {
                                    val pdfUrl = trx.pdfUrl
                                    if (!pdfUrl.isNullOrBlank()) {
                                        android.widget.Toast.makeText(context, "Invoice PDF tersimpan di server", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                    showPdfConfirm = false
                                    onDismiss()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AppPrimaryColor),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Text("YA", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
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
