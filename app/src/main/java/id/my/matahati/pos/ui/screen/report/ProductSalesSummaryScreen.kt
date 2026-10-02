package id.my.matahati.pos.ui.screen.report

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import id.my.matahati.pos.ui.screen.home.components.OlseraDateFilterDialog
import id.my.matahati.pos.ui.screen.report.components.ProductSalesReceiptDialog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val OlseraBlueHeader = Color(0xFF1565C0)
private val GreenSubHeader = Color(0xFF4CAF50)
private val OlseraGreenButton = Color(0xFF4CAF50)

data class DayItem(
    val displayLabel: String,
    val apiDate: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductSalesSummaryScreen(
    nidOutlet: String? = null,
    savedOutletName: String? = null,
    onBack: () -> Unit,
    summaryViewModel: ProductSalesSummaryViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showDateFilterDialog by remember { mutableStateOf(false) }

    val today = remember { Date() }
    val apiSdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val displayHeaderSdf = remember { SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("id-ID")) }
    val displayDaySdf = remember { SimpleDateFormat("EEE, dd MMM yyyy", Locale.forLanguageTag("id-ID")) }

    var selectedHeaderDateText by remember { mutableStateOf(displayHeaderSdf.format(today)) }
    var selectedDayItems by remember {
        mutableStateOf(
            listOf(DayItem(displayLabel = displayDaySdf.format(today), apiDate = apiSdf.format(today)))
        )
    }

    var activeSelectedDayDisplay by remember { mutableStateOf(displayDaySdf.format(today)) }
    var activeSelectedDayApi by remember { mutableStateOf(apiSdf.format(today)) }

    // Auto-select first date item when screen is loaded or date range changes
    LaunchedEffect(selectedDayItems) {
        if (selectedDayItems.isNotEmpty()) {
            val firstItem = selectedDayItems.first()
            activeSelectedDayDisplay = firstItem.displayLabel
            activeSelectedDayApi = firstItem.apiDate
            summaryViewModel.fetchSummary(apiDate = firstItem.apiDate, nidOutlet = nidOutlet)
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = OlseraBlueHeader,
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(56.dp)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left: Back Arrow
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color.White
                        )
                    }

                    // Middle: Date Text
                    Text(
                        text = selectedHeaderDateText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    // Right: Calendar Filter Icon
                    IconButton(onClick = { showDateFilterDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Filter Tanggal",
                            tint = Color.White
                        )
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White)
        ) {
            // =========================================================
            // PANEL KIRI: DAFTAR TANGGAL (35% Width)
            // =========================================================
            Column(
                modifier = Modifier
                    .weight(0.5f)
                    .fillMaxHeight()
                    .background(Color.White)
            ) {
                // Sub-Header Bar "Tanggal"
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(GreenSubHeader)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "Tanggal",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Date List Content
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(selectedDayItems) { dayItem ->
                        val isSelected = (dayItem.apiDate == activeSelectedDayApi)
                        val rowBgColor = if (isSelected) Color(0xFFE3F2FD) else Color.White
                        val textColor = if (isSelected) OlseraBlueHeader else Color(0xFF424242)
                        val textWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(rowBgColor)
                                .clickable {
                                    activeSelectedDayDisplay = dayItem.displayLabel
                                    activeSelectedDayApi = dayItem.apiDate
                                    summaryViewModel.fetchSummary(
                                        apiDate = dayItem.apiDate,
                                        nidOutlet = nidOutlet
                                    )
                                }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp)
                            ) {
                                Text(
                                    text = dayItem.displayLabel,
                                    fontSize = 15.sp,
                                    fontWeight = textWeight,
                                    color = textColor
                                )
                            }

                            HorizontalDivider(
                                color = Color.LightGray.copy(alpha = 0.4f),
                                thickness = 0.8.dp
                            )
                        }
                    }
                }
            }

            // Vertical Divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(Color.LightGray.copy(alpha = 0.5f))
            )

            // =========================================================
            // PANEL KANAN: DETAIL RINGKASAN PRODUK & CETAK (65% Width)
            // =========================================================
            Column(
                modifier = Modifier
                    .weight(0.5f)
                    .fillMaxHeight()
                    .background(Color.White)
            ) {
                // Header Bar dengan Tanggal Terpilih (Biru)
                Surface(
                    color = OlseraBlueHeader,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = activeSelectedDayDisplay.ifBlank { "Detail Laporan" },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Summary Info Banner (Grand Total Qty & Sales)
                val summaryHeader = summaryViewModel.summaryHeader
                val isLoading = summaryViewModel.isLoading
                val itemsList = summaryViewModel.itemsList
                val errorMessage = summaryViewModel.errorMessage

                if (summaryHeader != null && !isLoading && itemsList.isNotEmpty()) {
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

                // Main Content List / Loading / Empty State Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
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
                    } else if (errorMessage != null || itemsList.isEmpty()) {
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
                            items(itemsList) { item ->
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

                // Sticky Bottom Button "Cetak" di Panel Kanan (Full Width Edge-to-Edge)
                Button(
                    onClick = {
                        summaryViewModel.selectedDayDisplay = activeSelectedDayDisplay
                        summaryViewModel.openReceiptPreview()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OlseraGreenButton),
                    shape = RoundedCornerShape(0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                ) {
                    Text(
                        text = "CETAK",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }

    // Date Filter Dialog
    if (showDateFilterDialog) {
        OlseraDateFilterDialog(
            onDismiss = { showDateFilterDialog = false },
            onDateSelected = { result ->
                selectedHeaderDateText = result.displayLabel
                selectedDayItems = generateDailyItemObjects(result.startDate, result.endDate)
            }
        )
    }

    // Modal Preview Hasil Cetak Struk
    if (summaryViewModel.showReceiptPreview) {
        ProductSalesReceiptDialog(
            dateDisplay = activeSelectedDayDisplay,
            items = summaryViewModel.itemsList,
            summaryHeader = summaryViewModel.summaryHeader,
            savedOutletName = savedOutletName,
            isPrinting = summaryViewModel.isPrinting,
            onPrintToPhysicalPrinter = {
                summaryViewModel.printToPhysicalPrinter(context, savedOutletName)
            },
            onDismiss = { summaryViewModel.closeReceiptPreview() }
        )
    }

    // Alert Dialog untuk Status Cetak Printer
    if (summaryViewModel.printMessage != null) {
        AlertDialog(
            onDismissRequest = { summaryViewModel.clearPrintMessage() },
            title = { Text("Informasi Cetak", fontWeight = FontWeight.Bold) },
            text = { Text(summaryViewModel.printMessage ?: "") },
            confirmButton = {
                TextButton(onClick = { summaryViewModel.clearPrintMessage() }) {
                    Text("OK", color = OlseraBlueHeader, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

private fun generateDailyItemObjects(startDateStr: String, endDateStr: String): List<DayItem> {
    val items = mutableListOf<DayItem>()
    try {
        val apiSdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val displayDaySdf = SimpleDateFormat("EEE, dd MMM yyyy", Locale.forLanguageTag("id-ID"))

        val startDate = apiSdf.parse(startDateStr) ?: Date()
        val endDate = apiSdf.parse(endDateStr) ?: startDate

        val calendar = Calendar.getInstance().apply { time = startDate }
        val endCalendar = Calendar.getInstance().apply { time = endDate }

        while (!calendar.after(endCalendar)) {
            val date = calendar.time
            items.add(
                DayItem(
                    displayLabel = displayDaySdf.format(date),
                    apiDate = apiSdf.format(date)
                )
            )
            calendar.add(Calendar.DATE, 1)
        }
    } catch (_: Exception) {
        items.add(DayItem(displayLabel = startDateStr, apiDate = startDateStr))
    }
    return if (items.isEmpty()) listOf(DayItem(displayLabel = startDateStr, apiDate = startDateStr)) else items
}
