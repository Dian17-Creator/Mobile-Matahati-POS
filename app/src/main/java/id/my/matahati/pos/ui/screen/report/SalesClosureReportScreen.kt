package id.my.matahati.pos.ui.screen.report

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import id.my.matahati.pos.ui.screen.home.components.OlseraDateFilterDialog
import id.my.matahati.pos.ui.screen.shift.components.ShiftCloseSalesReceiptDialog
import id.my.matahati.pos.ui.theme.AppPrimaryColor
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val OlseraBlueHeader = AppPrimaryColor
private val OlseraGreenButton = Color(0xFF4CAF50)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesClosureReportScreen(
    nidOutlet: String? = null,
    savedOutletName: String? = null,
    userName: String? = null,
    onBack: () -> Unit,
    reportViewModel: SalesClosureReportViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showDateFilterDialog by remember { mutableStateOf(false) }
    var showCloseSalesReceiptDialog by remember { mutableStateOf(false) }

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

    val dailyData = reportViewModel.dailyClosureData
    val isLoading = reportViewModel.isLoading

    val numberFormatter = remember {
        NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID")).apply {
            maximumFractionDigits = 0
        }
    }

    fun formatNumberDisplay(value: Double): String {
        return numberFormatter.format(value)
    }

    fun formatTimeDisplay(rawDateTime: String?): String {
        if (rawDateTime.isNullOrBlank()) return "-"
        if (rawDateTime == "Masih Berjalan") return "Masih Berjalan"
        return try {
            val clean = rawDateTime.replace("T", " ").replace(".000000Z", "").replace("Z", "")
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            val outputFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm", Locale("id", "ID"))
            val d = inputFormat.parse(clean)
            if (d != null) outputFormat.format(d) else clean
        } catch (e: Exception) {
            rawDateTime
        }
    }

    LaunchedEffect(selectedDayItems) {
        if (selectedDayItems.isNotEmpty()) {
            val firstItem = selectedDayItems.first()
            activeSelectedDayDisplay = firstItem.displayLabel
            activeSelectedDayApi = firstItem.apiDate
            reportViewModel.fetchDailySummary(apiDate = firstItem.apiDate, nidOutlet = nidOutlet, userName = userName)
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
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color.White
                        )
                    }

                    Text(
                        text = "Penutupan Penjualan",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(48.dp))
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8F9FA))
        ) {
            val isTablet = maxWidth >= 600.dp

            if (!isTablet) {
                // Mobile Phone 1-Column Layout
                MobileSalesClosureReportContent(
                    activeSelectedDayDisplay = activeSelectedDayDisplay,
                    dailyData = dailyData,
                    isLoading = isLoading,
                    isPrinting = reportViewModel.isPrinting,
                    onDateFilterClick = { showDateFilterDialog = true },
                    onPrintClick = {
                        if (reportViewModel.isShowPrintPreviewEnabled(context)) {
                            showCloseSalesReceiptDialog = true
                        } else {
                            reportViewModel.printDailyClosure(
                                context = context,
                                data = dailyData,
                                savedOutletName = savedOutletName,
                                userName = userName
                            )
                        }
                    },
                    formatTimeDisplay = ::formatTimeDisplay,
                    formatNumberDisplay = ::formatNumberDisplay
                )
            } else {
                // Tablet 2-Pane Split Layout
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF8F9FA))
                ) {
                    // LEFT PANE: DATE LIST (50% Width)
                    Column(
                        modifier = Modifier
                            .weight(0.50f)
                            .fillMaxHeight()
                            .background(Color.White)
                    ) {
                        Surface(
                            color = Color(0xFF4CAF50),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Tanggal",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                            )
                        }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(selectedDayItems, key = { it.apiDate }) { item ->
                                val isSelected = item.apiDate == activeSelectedDayApi
                                Surface(
                                    color = if (isSelected) Color(0xFFE3F2FD) else Color.White,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            activeSelectedDayDisplay = item.displayLabel
                                            activeSelectedDayApi = item.apiDate
                                            reportViewModel.fetchDailySummary(apiDate = item.apiDate, nidOutlet = nidOutlet, userName = userName)
                                        }
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 14.dp)
                                    ) {
                                        Text(
                                            text = item.displayLabel,
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) AppPrimaryColor else Color(0xFF333333),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
                            }
                        }
                    }

                    VerticalDivider(color = Color(0xFFE0E0E0), thickness = 1.dp)

                    // RIGHT PANE: DAILY CLOSURE SUMMARY (50% Width)
                    Box(
                        modifier = Modifier
                            .weight(0.50f)
                            .fillMaxHeight()
                            .background(Color.White)
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Surface(
                                color = OlseraBlueHeader,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = activeSelectedDayDisplay,
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                                )
                            }

                            if (isLoading) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = OlseraBlueHeader)
                                }
                            } else if (dailyData == null || dailyData.shiftsCount == 0) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Tidak ada data penutupan penjualan untuk tanggal ini",
                                        color = Color.Gray,
                                        fontSize = 14.sp
                                    )
                                }
                            } else {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .verticalScroll(rememberScrollState())
                                ) {
                                    ReportShiftRow("Kasir", dailyData.cashierNames, isBold = true, verticalPadding = 18.dp)
                                    HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 0.8.dp)

                                    ReportShiftRow("Mulai Shift", formatTimeDisplay(dailyData.earliestOpenedAt), isBold = true, verticalPadding = 18.dp)
                                    HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 0.8.dp)

                                    ReportShiftRow("Shift Berakhir", formatTimeDisplay(dailyData.latestClosedAt), isBold = true, verticalPadding = 18.dp)
                                    HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 0.8.dp)

                                    ReportShiftRow("Total Penjualan", formatNumberDisplay(dailyData.totalSales), isBold = true, verticalPadding = 18.dp)
                                    HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 0.8.dp)

                                    val subtotalVal = if (dailyData.subtotal > 0) dailyData.subtotal else dailyData.totalSales
                                    ReportShiftRow("  • Subtotal", formatNumberDisplay(subtotalVal), startPadding = 32.dp, verticalPadding = 14.dp)
                                    HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 0.8.dp)

                                    if (dailyData.discountAmount > 0) {
                                        ReportShiftRow("  • Diskon Bill", "-${formatNumberDisplay(dailyData.discountAmount)}", startPadding = 32.dp, verticalPadding = 14.dp)
                                        HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 0.8.dp)
                                    }

                                    ReportShiftRow("  • Penjualan nett", formatNumberDisplay(dailyData.netSales), isItalic = true, startPadding = 32.dp, verticalPadding = 14.dp)
                                    HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 0.8.dp)

                                    // Kas Section
                                    ReportShiftRow("Kas", formatNumberDisplay(dailyData.expectedCash), isBold = true, verticalPadding = 18.dp)
                                    HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 0.8.dp)

                                    ReportShiftRow("  Kas Penjualan", formatNumberDisplay(dailyData.cashSales), startPadding = 32.dp, verticalPadding = 16.dp)
                                    HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 0.8.dp)

                                    ReportShiftRow("  Kas Pengembalian", formatNumberDisplay(dailyData.refundCash), startPadding = 32.dp, verticalPadding = 16.dp)
                                    HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 0.8.dp)

                                    ReportShiftRow("  Kas Pembatalan", formatNumberDisplay(dailyData.cancellationCash), startPadding = 32.dp, verticalPadding = 16.dp)
                                    HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 0.8.dp)

                                    ReportShiftRow("  Kas Masuk-Keluar", formatNumberDisplay(dailyData.netCashMovement), startPadding = 32.dp, verticalPadding = 16.dp)
                                    HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 0.8.dp)

                                    // Non-Cash Payment Methods Breakdown
                                    dailyData.paymentBreakdownList.forEach { pm ->
                                        val nameUpper = pm.name.trim().uppercase()
                                        if (nameUpper != "CASH" && nameUpper != "TUNAI" && nameUpper != "KAS") {
                                            ReportShiftRow(pm.name, formatNumberDisplay(pm.net), isBold = true, verticalPadding = 18.dp)
                                            HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 0.8.dp)

                                            ReportShiftRow("  Penjualan", formatNumberDisplay(pm.sales), startPadding = 32.dp, verticalPadding = 14.dp)
                                            HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 0.8.dp)

                                            ReportShiftRow("  Pengembalian", formatNumberDisplay(pm.refund), startPadding = 32.dp, verticalPadding = 14.dp)
                                            HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 0.8.dp)

                                            ReportShiftRow("  Batal", formatNumberDisplay(pm.voidAmount), startPadding = 32.dp, verticalPadding = 14.dp)
                                            HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 0.8.dp)
                                        }
                                    }

                                    ReportShiftRow("Total Kas Diharapkan (Laci)", formatNumberDisplay(dailyData.expectedCash), isBold = true, verticalPadding = 18.dp)
                                    HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 0.8.dp)
                                }

                                Surface(
                                    color = Color.White,
                                    shadowElevation = 8.dp,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                if (reportViewModel.isShowPrintPreviewEnabled(context)) {
                                                    showCloseSalesReceiptDialog = true
                                                } else {
                                                    reportViewModel.printDailyClosure(
                                                        context = context,
                                                        data = dailyData,
                                                        savedOutletName = savedOutletName,
                                                        userName = userName
                                                    )
                                                }
                                            },
                                            enabled = !reportViewModel.isPrinting,
                                            colors = ButtonDefaults.buttonColors(containerColor = OlseraGreenButton),
                                            shape = RoundedCornerShape(4.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(48.dp)
                                        ) {
                                            if (reportViewModel.isPrinting) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(20.dp),
                                                    color = Color.White,
                                                    strokeWidth = 2.dp
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("MENCETAK...", color = Color.White)
                                            } else {
                                                Text(
                                                    text = "CETAK",
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Date Range Filter Dialog
    if (showDateFilterDialog) {
        OlseraDateFilterDialog(
            onDismiss = { showDateFilterDialog = false },
            onDateSelected = { result ->
                selectedHeaderDateText = result.displayLabel
                showDateFilterDialog = false

                val dayList = mutableListOf<DayItem>()
                try {
                    val cal = Calendar.getInstance()
                    val startD = apiSdf.parse(result.startDate)
                    val endD = apiSdf.parse(result.endDate)
                    if (startD != null && endD != null) {
                        cal.time = startD
                        while (!cal.time.after(endD)) {
                            val curDate = cal.time
                            dayList.add(
                                DayItem(
                                    displayLabel = displayDaySdf.format(curDate),
                                    apiDate = apiSdf.format(curDate)
                                )
                            )
                            cal.add(Calendar.DATE, 1)
                        }
                    }
                } catch (_: Exception) {
                }

                if (dayList.isNotEmpty()) {
                    selectedDayItems = dayList
                }
            }
        )
    }

    // Modal Preview Struk Penutupan Penjualan
    if (showCloseSalesReceiptDialog && dailyData != null) {
        val shiftForPreview = reportViewModel.toShiftResponse(dailyData, userName)
        ShiftCloseSalesReceiptDialog(
            shift = shiftForPreview,
            savedOutletName = savedOutletName,
            userName = userName,
            isPrinting = reportViewModel.isPrinting,
            onPrintToPhysicalPrinter = {
                reportViewModel.printDailyClosure(
                    context = context,
                    data = dailyData,
                    savedOutletName = savedOutletName,
                    userName = userName
                )
            },
            onDismiss = {
                showCloseSalesReceiptDialog = false
            }
        )
    }
}

@Composable
private fun MobileSalesClosureReportContent(
    activeSelectedDayDisplay: String,
    dailyData: DailySalesClosureData?,
    isLoading: Boolean,
    isPrinting: Boolean,
    onDateFilterClick: () -> Unit,
    onPrintClick: () -> Unit,
    formatTimeDisplay: (String?) -> String,
    formatNumberDisplay: (Double) -> String
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
                    CircularProgressIndicator(color = OlseraBlueHeader)
                }
            } else if (dailyData == null || dailyData.shiftsCount == 0) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tidak ada data penutupan penjualan untuk tanggal ini",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 16.dp)
                ) {
                    ReportShiftRow("Kasir", dailyData.cashierNames, isBold = true, verticalPadding = 14.dp)
                    HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 0.8.dp)

                    ReportShiftRow("Mulai Shift", formatTimeDisplay(dailyData.earliestOpenedAt), isBold = true, verticalPadding = 14.dp)
                    HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 0.8.dp)

                    ReportShiftRow("Shift Berakhir", formatTimeDisplay(dailyData.latestClosedAt), isBold = true, verticalPadding = 14.dp)
                    HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 0.8.dp)

                    ReportShiftRow("Total Penjualan", formatNumberDisplay(dailyData.totalSales), isBold = true, verticalPadding = 14.dp)
                    HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 0.8.dp)

                    val subtotalVal = if (dailyData.subtotal > 0) dailyData.subtotal else dailyData.totalSales
                    ReportShiftRow("  • Subtotal", formatNumberDisplay(subtotalVal), startPadding = 16.dp, verticalPadding = 10.dp)
                    HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 0.8.dp)

                    if (dailyData.discountAmount > 0) {
                        ReportShiftRow("  • Diskon Bill", "-${formatNumberDisplay(dailyData.discountAmount)}", startPadding = 16.dp, verticalPadding = 10.dp)
                        HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 0.8.dp)
                    }

                    ReportShiftRow("  • Penjualan nett", formatNumberDisplay(dailyData.netSales), isItalic = true, startPadding = 16.dp, verticalPadding = 10.dp)
                    HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 0.8.dp)

                    // Kas Section
                    ReportShiftRow("Kas", formatNumberDisplay(dailyData.expectedCash), isBold = true, verticalPadding = 14.dp)
                    HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 0.8.dp)

                    ReportShiftRow("  Kas Penjualan", formatNumberDisplay(dailyData.cashSales), startPadding = 16.dp, verticalPadding = 12.dp)
                    HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 0.8.dp)

                    ReportShiftRow("  Kas Pengembalian", formatNumberDisplay(dailyData.refundCash), startPadding = 16.dp, verticalPadding = 12.dp)
                    HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 0.8.dp)

                    ReportShiftRow("  Kas Pembatalan", formatNumberDisplay(dailyData.cancellationCash), startPadding = 16.dp, verticalPadding = 12.dp)
                    HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 0.8.dp)

                    ReportShiftRow("  Kas Masuk-Keluar", formatNumberDisplay(dailyData.netCashMovement), startPadding = 16.dp, verticalPadding = 12.dp)
                    HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 0.8.dp)

                    // Non-Cash Payment Methods Breakdown
                    dailyData.paymentBreakdownList.forEach { pm ->
                        val nameUpper = pm.name.trim().uppercase()
                        if (nameUpper != "CASH" && nameUpper != "TUNAI" && nameUpper != "KAS") {
                            ReportShiftRow(pm.name, formatNumberDisplay(pm.net), isBold = true, verticalPadding = 14.dp)
                            HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 0.8.dp)

                            ReportShiftRow("  Penjualan", formatNumberDisplay(pm.sales), startPadding = 16.dp, verticalPadding = 10.dp)
                            HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 0.8.dp)

                            ReportShiftRow("  Pengembalian", formatNumberDisplay(pm.refund), startPadding = 16.dp, verticalPadding = 10.dp)
                            HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 0.8.dp)

                            ReportShiftRow("  Batal", formatNumberDisplay(pm.voidAmount), startPadding = 16.dp, verticalPadding = 10.dp)
                            HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 0.8.dp)
                        }
                    }

                    ReportShiftRow("Total Kas Diharapkan (Laci)", formatNumberDisplay(dailyData.expectedCash), isBold = true, verticalPadding = 14.dp)
                    HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 0.8.dp)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Button CETAK (Mobile Version)
                    Button(
                        onClick = onPrintClick,
                        enabled = !isPrinting,
                        colors = ButtonDefaults.buttonColors(containerColor = OlseraGreenButton),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(48.dp)
                    ) {
                        if (isPrinting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("MENCETAK...", color = Color.White)
                        } else {
                            Text(text = "CETAK", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportShiftRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    isItalic: Boolean = false,
    valueColor: Color = Color(0xFF333333),
    startPadding: Dp = 16.dp,
    verticalPadding: Dp = 18.dp
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = startPadding, end = 16.dp, top = verticalPadding, bottom = verticalPadding),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = if (isBold) 15.sp else 14.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal,
            color = if (isBold) Color.Black else Color(0xFF555555)
        )
        Text(
            text = value,
            fontSize = if (isBold) 16.sp else 14.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
            fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal,
            color = valueColor,
            textAlign = TextAlign.End
        )
    }
}
