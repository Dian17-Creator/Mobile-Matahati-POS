package id.my.matahati.pos.ui.screen.report.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.my.matahati.pos.model.ProductSalesSummaryHeader
import id.my.matahati.pos.model.ProductSalesSummaryItem
import id.my.matahati.pos.ui.screen.report.ReportViewModel

private val OlseraBlueHeader = Color(0xFF24BBCC)
private val OlseraGreenButton = Color(0xFF4CAF50)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductSalesSummaryDetailBottomSheet(
    dateDisplay: String,
    isLoading: Boolean,
    errorMessage: String?,
    summaryHeader: ProductSalesSummaryHeader?,
    items: List<ProductSalesSummaryItem>,
    onDismiss: () -> Unit,
    onPrintClick: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        containerColor = Color.White,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            // Header Bar dengan latar belakang Biru
            Surface(
                color = OlseraBlueHeader,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = dateDisplay.ifBlank { "Detail Laporan" },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = Color.White
                        )
                    }
                }
            }

            // Summary Info Header Banner (Grand Total Qty & Sales)
            if (summaryHeader != null && !isLoading && items.isNotEmpty()) {
                Surface(
                    color = Color(0xFFE3F2FD),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Total Item Terjual",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = "${summaryHeader.grandTotalQty} Item",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = OlseraBlueHeader
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Total Penjualan",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = ReportViewModel.formatRupiah(summaryHeader.grandTotalSales),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = OlseraBlueHeader
                            )
                        }
                    }
                }
            }

            // Main Content Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 250.dp, max = 420.dp)
            ) {
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = OlseraBlueHeader)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Memuat ringkasan produk...",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                } else if (errorMessage != null || items.isEmpty()) {
                    // EMPTY STATE
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                shape = RoundedCornerShape(32.dp),
                                color = Color(0xFFF5F5F5),
                                modifier = Modifier.size(64.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Receipt,
                                        contentDescription = null,
                                        tint = Color(0xFFBDBDBD),
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Tidak ada transaksi ditemukan",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF424242),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Tidak ada transaksi untuk tanggal yang dipilih",
                                fontSize = 14.sp,
                                color = Color(0xFF757575),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    // PRODUCT SUMMARY LIST
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(items) { item ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.productName.ifBlank { "Produk Tanpa Nama" },
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF212121)
                                        )

                                        if (item.categoryName.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = item.categoryName,
                                                fontSize = 12.sp,
                                                color = Color.Gray
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "${ReportViewModel.formatRupiah(item.price)}  x${item.soldQty}",
                                            fontSize = 13.sp,
                                            color = Color(0xFF616161)
                                        )
                                    }

                                    Text(
                                        text = ReportViewModel.formatRupiah(item.totalSales),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = OlseraBlueHeader
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                            }
                        }
                    }
                }
            }

            // Sticky Bottom Button "Cetak"
            Surface(
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Button(
                        onClick = onPrintClick,
                        colors = ButtonDefaults.buttonColors(containerColor = OlseraGreenButton),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(
                            text = "Cetak",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
