package id.my.matahati.pos.ui.screen.report

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import id.my.matahati.pos.model.SalesByDate
import id.my.matahati.pos.model.TopProductGroup
import id.my.matahati.pos.ui.screen.home.components.OlseraDateFilterDialog

import id.my.matahati.pos.ui.theme.AppPrimaryColor

private val OlseraBlueHeader = AppPrimaryColor
private val GreenSalesCard = Color(0xFF4CAF50)
private val OrangeRefundCard = Color(0xFFFB8C00)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    nidOutlet: String? = null,
    onMenuClick: () -> Unit = {},
    onNavigateToTransaksi: () -> Unit = {},
    onNavigateToProductSalesSummary: () -> Unit = {},
    viewModel: ReportViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    var showDateFilterDialog by remember { mutableStateOf(false) }
    var showReportTypeDialog by remember { mutableStateOf(false) }
    var selectedReportType by remember { mutableStateOf("Penutupan Penjualan") }

    LaunchedEffect(nidOutlet) {
        viewModel.fetchDashboard(nidOutlet = nidOutlet)
    }

    val dashboardData = viewModel.dashboardData
    val isLoading = viewModel.isLoading
    val errorMessage = viewModel.errorMessage

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
                    // Left: Menu Button & Dashboard Pill Selector Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onMenuClick) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu Drawer",
                                tint = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.Black.copy(alpha = 0.20f),
                            modifier = Modifier.clickable { showReportTypeDialog = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (selectedReportType == "Penutupan Penjualan") "Dashboard" else selectedReportType,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Pilih Jenis Laporan",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // Right: Date Selector
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.clickable { showDateFilterDialog = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Kalender",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = viewModel.selectedDateLabel,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF5F7FA))
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
                            text = "Memuat data dashboard...",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            } else if (errorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Gagal Mengambil Data",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Red
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage ?: "Terjadi kesalahan pada server",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.fetchDashboard(nidOutlet = nidOutlet) },
                            colors = ButtonDefaults.buttonColors(containerColor = OlseraBlueHeader)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Coba Lagi"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Coba Lagi")
                        }
                    }
                }
            } else {
                val totalSales = dashboardData?.totalSales ?: 0.0
                val totalRefund = dashboardData?.totalRefund ?: 0.0
                val salesByDate = dashboardData?.salesByDate ?: emptyList()
                val topProductGroups = dashboardData?.topProductGroups ?: emptyList()

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // B. Summary Cards
                    item {
                        SummaryCardsSection(
                            totalSales = totalSales,
                            totalRefund = totalRefund
                        )
                    }

                    // C. Sales By Date Chart Section
                    item {
                        SalesByDateChartCard(
                            salesByDate = salesByDate
                        )
                    }

                    // D. Top Product Groups Section Header
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        ) {
                            Text(
                                text = "Top Penjualan Grup Produk",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = OlseraBlueHeader
                            )
                        }
                    }

                    // Top Product Groups List
                    if (topProductGroups.isEmpty()) {
                        item {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Belum ada data penjualan grup produk untuk periode ini",
                                        fontSize = 14.sp,
                                        color = Color.Gray,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    } else {
                        items(topProductGroups) { group ->
                            TopProductGroupItemCard(group = group)
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
                viewModel.updateDateRange(
                    label = result.displayLabel,
                    startDate = result.startDate,
                    endDate = result.endDate,
                    nidOutlet = nidOutlet
                )
            }
        )
    }

    // Report Type Selection Dialog
    if (showReportTypeDialog) {
        ReportTypeSelectionDialog(
            currentSelection = selectedReportType,
            onDismiss = { showReportTypeDialog = false },
            onConfirm = { selection ->
                selectedReportType = selection
                showReportTypeDialog = false
                if (selection == "Log Transaksi") {
                    onNavigateToTransaksi()
                } else if (selection == "Ringkasan Penjualan Produk") {
                    onNavigateToProductSalesSummary()
                }
            }
        )
    }
}

@Composable
private fun ReportTypeSelectionDialog(
    currentSelection: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val options = listOf("Penutupan Penjualan", "Ringkasan Penjualan Produk", "Log Transaksi")
    var tempSelection by remember { mutableStateOf(currentSelection) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(12.dp),
        containerColor = Color.White,
        title = {
            Text(
                text = "Laporan",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { tempSelection = option }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (tempSelection == option),
                            onClick = { tempSelection = option },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = AppPrimaryColor
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = option,
                            fontSize = 15.sp,
                            color = Color(0xFF424242)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(tempSelection) }
            ) {
                Text(
                    text = "PILIH",
                    fontWeight = FontWeight.Bold,
                    color = AppPrimaryColor
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = "BATAL",
                    fontWeight = FontWeight.Bold,
                    color = AppPrimaryColor
                )
            }
        }
    )
}

@Composable
private fun SummaryCardsSection(
    totalSales: Double,
    totalRefund: Double
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val isWideScreen = maxWidth >= 500.dp

        if (isWideScreen) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SummaryCardItem(
                    title = "Total Penjualan",
                    amount = totalSales,
                    backgroundColor = GreenSalesCard,
                    modifier = Modifier.weight(1f)
                )
                SummaryCardItem(
                    title = "Total Pengembalian",
                    amount = totalRefund,
                    backgroundColor = OrangeRefundCard,
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryCardItem(
                    title = "Total Penjualan",
                    amount = totalSales,
                    backgroundColor = GreenSalesCard,
                    modifier = Modifier.fillMaxWidth()
                )
                SummaryCardItem(
                    title = "Total Pengembalian",
                    amount = totalRefund,
                    backgroundColor = OrangeRefundCard,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun SummaryCardItem(
    title: String,
    amount: Double,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.9f)
                )

                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = ReportViewModel.formatRupiah(amount),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun SalesByDateChartCard(
    salesByDate: List<SalesByDate>
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                    contentDescription = null,
                    tint = OlseraBlueHeader,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Penjualan berdasarkan Tanggal",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = OlseraBlueHeader
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (salesByDate.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tidak ada data grafik untuk rentang tanggal ini",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
            } else {
                SalesLineAreaCanvasChart(
                    data = salesByDate,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }
        }
    }
}

@Composable
private fun SalesLineAreaCanvasChart(
    data: List<SalesByDate>,
    modifier: Modifier = Modifier
) {
    val maxTotal = remember(data) {
        val max = data.maxOfOrNull { it.total } ?: 1.0
        if (max == 0.0) 1.0 else max
    }

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(top = 8.dp, bottom = 4.dp, start = 8.dp, end = 8.dp)
        ) {
            val width = size.width
            val height = size.height

            if (data.isEmpty() || width <= 0 || height <= 0) return@Canvas

            val numPoints = data.size
            val stepX = if (numPoints > 1) width / (numPoints - 1) else width / 2

            val points = data.mapIndexed { index, item ->
                val x = if (numPoints == 1) width / 2 else index * stepX
                val normalizedY = (item.total / maxTotal).toFloat()
                val y = height - (normalizedY * (height - 20.dp.toPx())) - 10.dp.toPx()
                Pair(x, y)
            }

            // Draw horizontal reference grid lines
            val gridColor = Color.LightGray.copy(alpha = 0.3f)
            val gridLineCount = 3
            for (i in 0..gridLineCount) {
                val gridY = (height / gridLineCount) * i
                drawLine(
                    color = gridColor,
                    start = androidx.compose.ui.geometry.Offset(0f, gridY),
                    end = androidx.compose.ui.geometry.Offset(width, gridY),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Path for Line & Gradient Fill
            val linePath = Path().apply {
                points.forEachIndexed { i, p ->
                    if (i == 0) moveTo(p.first, p.second)
                    else lineTo(p.first, p.second)
                }
            }

            val fillPath = Path().apply {
                points.forEachIndexed { i, p ->
                    if (i == 0) moveTo(p.first, p.second)
                    else lineTo(p.first, p.second)
                }
                lineTo(points.last().first, height)
                lineTo(points.first().first, height)
                close()
            }

            // Draw Gradient Area Under Chart
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        OlseraBlueHeader.copy(alpha = 0.35f),
                        OlseraBlueHeader.copy(alpha = 0.02f)
                    )
                )
            )

            // Draw Chart Line
            drawPath(
                path = linePath,
                color = OlseraBlueHeader,
                style = Stroke(width = 3.dp.toPx())
            )

            // Draw Points (Circles)
            points.forEach { p ->
                drawCircle(
                    color = Color.White,
                    radius = 5.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(p.first, p.second)
                )
                drawCircle(
                    color = OlseraBlueHeader,
                    radius = 3.5.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(p.first, p.second)
                )
            }
        }

        // X-Axis Date Labels Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val displayItems = if (data.size > 5) {
                listOf(data.first(), data[data.size / 2], data.last())
            } else {
                data
            }

            displayItems.forEach { item ->
                Text(
                    text = ReportViewModel.formatDateDisplay(item.date),
                    fontSize = 11.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun TopProductGroupItemCard(
    group: TopProductGroup
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFE3F2FD),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = null,
                            tint = OlseraBlueHeader,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = group.name.ifBlank { "Grup Tanpa Nama" },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF1F8E9)
                    ) {
                        Text(
                            text = "${group.qty} Item",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF33691E),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Text(
                text = ReportViewModel.formatRupiah(group.total),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = OlseraBlueHeader
            )
        }
    }
}
