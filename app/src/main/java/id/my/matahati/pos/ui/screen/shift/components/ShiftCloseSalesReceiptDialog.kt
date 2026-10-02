package id.my.matahati.pos.ui.screen.shift.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import id.my.matahati.pos.model.ShiftResponse
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ShiftCloseSalesReceiptDialog(
    shift: ShiftResponse,
    savedOutletName: String? = null,
    userName: String? = null,
    isPrinting: Boolean = false,
    onPrintToPhysicalPrinter: () -> Unit,
    onDismiss: () -> Unit
) {
    val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID")).apply {
        maximumFractionDigits = 0
    }

    fun formatNum(amount: Double): String {
        return formatter.format(amount)
    }

    fun formatShiftDay(dateTime: String?): String {
        if (dateTime.isNullOrBlank()) return "-"
        return try {
            val inputFormat = if (dateTime.contains("T")) {
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            } else {
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            }
            val outputFormat = SimpleDateFormat("EEE, dd MMM yyyy", Locale("id", "ID"))
            val date = inputFormat.parse(dateTime.replace(".000000Z", "").replace("Z", ""))
            if (date != null) outputFormat.format(date) else dateTime
        } catch (e: Exception) {
            dateTime
        }
    }

    val currentPrintedTime = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.forLanguageTag("id-ID")).format(Date())
    val outletHeader = (savedOutletName ?: "MATA HATI CAFE").uppercase()

    val cashierName = shift.user?.name ?: userName ?: "Kasir"
    val rawReceipts = shift.totalReceipts ?: 0
    val totalSales = shift.totalSales
    val totalReceipts = if (rawReceipts > 0) rawReceipts else if (totalSales > 0) 1 else 0
    val totalPax = if ((shift.totalPax ?: 0) > 0) shift.totalPax!! else totalReceipts

    val discountVal = shift.discountAmount ?: 0.0
    val subtotalVal = shift.subtotal ?: (totalSales + discountVal)

    val cashSalesVal = shift.cashSales ?: totalSales
    val refundCashVal = shift.refundCash
    val cancellationCashVal = shift.cancellationCash
    val netCashMovement = shift.cashIn - shift.cashOut
    val expectedCashVal = shift.expectedCash ?: (shift.openingCash + cashSalesVal + netCashMovement - refundCashVal - cancellationCashVal)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color.White,
            modifier = Modifier
                .width(400.dp)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Title Struk (Monospace)
                Text(
                    text = outletHeader,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Penutupan Penjualan",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center,
                    color = Color.DarkGray
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Scrollable Struk Content
                Box(modifier = Modifier.weight(1f, fill = false)) {
                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(scrollState)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Tercetak     : $currentPrintedTime",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Dicetak Oleh : $cashierName",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Tanggal      : ${formatShiftDay(shift.openedAt)}",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Tamu
                        ReceiptRow(label = "Jumlah Tamu", value = "$totalPax")
                        Spacer(modifier = Modifier.height(6.dp))
                        DashedDivider()
                        Spacer(modifier = Modifier.height(6.dp))

                        // Resi
                        ReceiptRow(label = "Resi", value = "$totalReceipts")
                        Spacer(modifier = Modifier.height(6.dp))
                        DashedDivider()
                        Spacer(modifier = Modifier.height(6.dp))

                        // Pengembalian
                        ReceiptRow(label = "Pengembalian", value = formatNum(refundCashVal))
                        Spacer(modifier = Modifier.height(6.dp))
                        DashedDivider()
                        Spacer(modifier = Modifier.height(6.dp))

                        // Total Penjualan
                        ReceiptRow(label = "Total Penjualan", value = formatNum(totalSales), isBold = true)
                        Spacer(modifier = Modifier.height(6.dp))
                        DashedDivider()
                        Spacer(modifier = Modifier.height(6.dp))

                        // Subtotal & Diskon
                        if (discountVal != 0.0) {
                            ReceiptRow(label = "  Subtotal", value = formatNum(subtotalVal))
                            val discText = if (discountVal > 0) "-${formatNum(discountVal)}" else formatNum(discountVal)
                            ReceiptRow(label = "  Diskon Bill", value = discText)
                            ReceiptRow(label = "", value = formatNum(totalSales))
                        } else {
                            ReceiptRow(label = "  Subtotal", value = formatNum(totalSales))
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Section Kas dengan Total Kas di Kanan
                        ReceiptRow(label = "Kas", value = formatNum(expectedCashVal), isBold = true)
                        Spacer(modifier = Modifier.height(4.dp))
                        DashedDivider()
                        Spacer(modifier = Modifier.height(6.dp))

                        ReceiptRow(label = "  Kas Penjualan", value = formatNum(cashSalesVal))
                        ReceiptRow(label = "  Kas Pengembalian", value = formatNum(refundCashVal))
                        ReceiptRow(label = "  Kas Pembatalan", value = formatNum(cancellationCashVal))
                        ReceiptRow(label = "  Kas Masuk-Keluar", value = formatNum(netCashMovement))

                        Spacer(modifier = Modifier.height(6.dp))
                        DashedDivider()
                        Spacer(modifier = Modifier.height(6.dp))

                        // Total Diharapkan
                        ReceiptRow(label = "Total Diharapkan", value = formatNum(expectedCashVal), isBold = true)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Bottom Action Button 1: CETAK KE PRINTER FISIK
                Button(
                    onClick = onPrintToPhysicalPrinter,
                    enabled = !isPrinting,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    if (isPrinting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("MENCETAK...", color = Color.White, fontWeight = FontWeight.Bold)
                    } else {
                        Text("CETAK KE PRINTER FISIK", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Bottom Action Button 2: TUTUP
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("TUTUP", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ReceiptRow(
    label: String,
    value: String,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = value,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun DashedDivider(
    modifier: Modifier = Modifier,
    color: Color = Color.DarkGray,
    dashWidth: Float = 10f,
    gapWidth: Float = 8f,
    strokeWidth: Float = 2f
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
    ) {
        drawLine(
            color = color,
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashWidth, gapWidth), 0f),
            strokeWidth = strokeWidth
        )
    }
}
