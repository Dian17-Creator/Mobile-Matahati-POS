package id.my.matahati.pos.ui.screen.report.components

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
import id.my.matahati.pos.model.ProductSalesSummaryHeader
import id.my.matahati.pos.model.ProductSalesSummaryItem
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProductSalesReceiptDialog(
    dateDisplay: String,
    items: List<ProductSalesSummaryItem>,
    summaryHeader: ProductSalesSummaryHeader?,
    savedOutletName: String? = null,
    apiDate: String? = null,
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

    val reportDateFormatted = try {
        if (!apiDate.isNullOrBlank()) {
            val inputSdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val outputSdf = SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("id-ID"))
            val parsed = inputSdf.parse(apiDate)
            if (parsed != null) outputSdf.format(parsed) else dateDisplay
        } else {
            val cleaned = dateDisplay.substringAfter(", ").trim()
            cleaned.ifBlank { dateDisplay }
        }
    } catch (_: Exception) {
        dateDisplay
    }

    val currentPrintedTime = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.forLanguageTag("id-ID")).format(Date())
    val outletHeader = "MATA HATI CAFE"

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
                    text = "Ringkasan Penjualan Produk",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
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
                                text = "Mulai      : $reportDateFormatted 00:00",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Akhir      : $reportDateFormatted 23:50",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Tercetak   : $currentPrintedTime",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (items.isEmpty()) {
                            Text(
                                text = "Tidak ada transaksi produk pada tanggal ini.",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp)
                            )
                        } else {
                            val priorityMap = mapOf("MAKANAN" to 1, "MINUMAN" to 2, "SNACK" to 3)
                            val sortedGroupedItems = items.groupBy { it.categoryName.ifBlank { "LAINNYA" }.uppercase() }
                                .entries
                                .sortedWith(compareBy({ priorityMap[it.key] ?: 99 }, { it.key }))

                            sortedGroupedItems.forEach { (categoryName, categoryItems) ->
                                val catQty = categoryItems.sumOf { it.soldQty }
                                val catSales = categoryItems.sumOf { it.totalSales }

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = categoryName,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "$catQty / ${formatNum(catSales)}",
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    DashedDivider()
                                    Spacer(modifier = Modifier.height(4.dp))

                                    categoryItems.forEach { item ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 2.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = item.productName.ifBlank { "Produk" },
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 12.sp,
                                                modifier = Modifier.weight(1f, fill = false)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "${item.soldQty} / ${formatNum(item.totalSales)}",
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
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
