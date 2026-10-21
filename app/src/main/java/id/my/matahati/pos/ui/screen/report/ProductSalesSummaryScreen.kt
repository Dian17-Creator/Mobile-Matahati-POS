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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import id.my.matahati.pos.model.ProductSalesSummaryHeader
import id.my.matahati.pos.model.ProductSalesSummaryItem
import id.my.matahati.pos.ui.screen.home.components.OlseraDateFilterDialog
import id.my.matahati.pos.ui.screen.report.components.ProductSalesReceiptDialog
import id.my.matahati.pos.ui.theme.AppPrimaryColor
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val OlseraBlueHeader = AppPrimaryColor
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
    val configuration = LocalConfiguration.current
    val isTabletScreen = configuration.screenWidthDp >= 600
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
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color.White
                        )
                    }

                    Text(
                        text = "Ringkasan Penjualan Produk",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )

                    if (isTabletScreen) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.Black.copy(alpha = 0.20f),
                            modifier = Modifier.clickable { showDateFilterDialog = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = selectedHeaderDateText,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = "Filter Tanggal",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.width(48.dp))
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White)
        ) {
            val isTablet = maxWidth >= 600.dp

            if (!isTablet) {
                // Mobile Phone 1-Column View
                MobileProductSalesSummaryContent(
                    activeSelectedDayDisplay = activeSelectedDayDisplay,
                    summaryHeader = summaryViewModel.summaryHeader,
                    itemsList = summaryViewModel.itemsList,
                    isLoading = summaryViewModel.isLoading,
                    errorMessage = summaryViewModel.errorMessage,
                    onDateFilterClick = { showDateFilterDialog = true },
                    onPrintClick = {
                        summaryViewModel.selectedDayDisplay = activeSelectedDayDisplay
                        summaryViewModel.selectedDayApi = activeSelectedDayApi
                        summaryViewModel.openReceiptPreview()
                    }
                )
            } else {
                // Tablet 2-Panel Split Layout
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                ) {
                    // PANEL KIRI: DAFTAR TANGGAL (35% Width)
                    Column(
                        modifier = Modifier
                            .weight(0.5f)
                            .fillMaxHeight()
                            .background(Color.White)
                    ) {
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

                    VerticalDivider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 1.dp)

                    // PANEL KANAN: DETAIL RINGKASAN PRODUK & CETAK (65% Width)
                    Column(
                        modifier = Modifier
                            .weight(0.5f)
                            .fillMaxHeight()
                            .background(Color.White)
                    ) {
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

                        Button(
                            onClick = {
                                summaryViewModel.selectedDayDisplay = activeSelectedDayDisplay
                                summaryViewModel.selectedDayApi = activeSelectedDayApi
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

    // Receipt Preview Dialog
    if (summaryViewModel.showReceiptPreview) {
        ProductSalesReceiptDialog(
            items = summaryViewModel.itemsList,
            summaryHeader = summaryViewModel.summaryHeader,
            dateDisplay = summaryViewModel.selectedDayDisplay,
            apiDate = summaryViewModel.selectedDayApi,
            savedOutletName = savedOutletName,
            isPrinting = summaryViewModel.isPrinting,
            onPrintToPhysicalPrinter = {
                summaryViewModel.printToPhysicalPrinter(
                    context = context,
                    savedOutletName = savedOutletName
                )
            },
            onDismiss = {
                summaryViewModel.closeReceiptPreview()
            }
        )
    }
}

@Composable
private fun MobileProductSalesSummaryContent(
    activeSelectedDayDisplay: String,
    summaryHeader: ProductSalesSummaryHeader?,
    itemsList: List<ProductSalesSummaryItem>,
    isLoading: Boolean,
    errorMessage: String?,
    onDateFilterClick: () -> Unit,
    onPrintClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Date Selector Header Bar
        Surface(
            color = OlseraBlueHeader,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDateFilterClick() }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Pilih Tanggal",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = activeSelectedDayDisplay.ifBlank { "Pilih Tanggal" },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Text(
                    text = "Ganti",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }

        // Summary Info Banner (Grand Total Qty & Sales)
        if (summaryHeader != null && !isLoading && itemsList.isNotEmpty()) {
            Surface(
                color = Color(0xFFE3F2FD),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
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

        // Main Content List / Loading / Empty State
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
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
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

                    // Button Cetak (Mobile Version)
                    Button(
                        onClick = onPrintClick,
                        colors = ButtonDefaults.buttonColors(containerColor = OlseraGreenButton),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(48.dp)
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
    }
}

private fun generateDailyItemObjects(startDateStr: String, endDateStr: String): List<DayItem> {
    val items = mutableListOf<DayItem>()
    try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val displayDaySdf = SimpleDateFormat("EEE, dd MMM yyyy", Locale.forLanguageTag("id-ID"))

        val start = inputFormat.parse(startDateStr)
        val end = inputFormat.parse(endDateStr)

        if (start != null && end != null) {
            val cal = Calendar.getInstance().apply { time = start }
            while (!cal.time.after(end)) {
                val curDate = cal.time
                items.add(
                    DayItem(
                        displayLabel = displayDaySdf.format(curDate),
                        apiDate = inputFormat.format(curDate)
                    )
                )
                cal.add(Calendar.DATE, 1)
            }
        }
    } catch (_: Exception) {
        val today = Date()
        val displayDaySdf = SimpleDateFormat("EEE, dd MMM yyyy", Locale.forLanguageTag("id-ID"))
        val apiSdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        items.add(DayItem(displayLabel = displayDaySdf.format(today), apiDate = apiSdf.format(today)))
    }

    if (items.isEmpty()) {
        val today = Date()
        val displayDaySdf = SimpleDateFormat("EEE, dd MMM yyyy", Locale.forLanguageTag("id-ID"))
        val apiSdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        items.add(DayItem(displayLabel = displayDaySdf.format(today), apiDate = apiSdf.format(today)))
    }

    return items
}
