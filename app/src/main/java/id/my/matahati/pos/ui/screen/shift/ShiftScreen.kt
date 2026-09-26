package id.my.matahati.pos.ui.screen.shift

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.my.matahati.pos.model.CashMovement
import id.my.matahati.pos.model.Shift
import id.my.matahati.pos.ui.screen.home.components.OlseraDateFilterDialog
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

private val OlseraBlueHeader = Color(0xFF1565C0)
private val OlseraGreenButton = Color(0xFF4CAF50)

@Composable
fun ShiftScreen(
    shiftViewModel: ShiftViewModel,
    nidOutlet: String?,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showCashMovementDialog by remember { mutableStateOf(false) }
    var showCloseShiftDialog by remember { mutableStateOf(false) }
    var showDateFilterDialog by remember { mutableStateOf(false) }

    val today = remember { Calendar.getInstance().time }
    val displaySdf = remember { SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")) }
    var selectedDateText by remember { mutableStateOf(displaySdf.format(today)) }

    val currentShift = shiftViewModel.currentShift
    val shiftHistory = shiftViewModel.shiftHistory
    val selectedDetail = shiftViewModel.selectedShiftDetail

    LaunchedEffect(nidOutlet) {
        shiftViewModel.checkCurrentShift(nidOutlet)
        shiftViewModel.fetchShiftHistory(nidOutlet)
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.fillMaxWidth().background(OlseraBlueHeader)) {
                // Top App Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(56.dp)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onMenuClick) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu Drawer",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "Manajemen Shift",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Tabs: "Saat Ini" & "Riwayat"
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = OlseraBlueHeader,
                    contentColor = Color.White,
                    indicator = { tabPositions ->
                        if (selectedTabIndex < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                height = 3.dp,
                                color = Color.White
                            )
                        }
                    }
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = {
                            Text(
                                "Saat Ini",
                                fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 15.sp
                            )
                        }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = {
                            selectedTabIndex = 1
                            shiftViewModel.fetchShiftHistory(nidOutlet)
                        },
                        text = {
                            Text(
                                "Riwayat",
                                fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 15.sp
                            )
                        }
                    )
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White)
        ) {
            when (selectedTabIndex) {
                0 -> {
                    // TAB 0: SAAT INI
                    if (shiftViewModel.isCurrentShiftLoading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = OlseraBlueHeader)
                        }
                    } else if (currentShift == null) {
                        // NO ACTIVE SHIFT: Show "Mulai Shift" UI
                        StartShiftContent(
                            isSubmitting = shiftViewModel.isSubmitting,
                            onStartShift = { amount ->
                                shiftViewModel.startShift(nidOutlet, amount)
                            }
                        )
                    } else {
                        // ACTIVE SHIFT: Show Split 2-Column Dashboard (POS Tablet View)
                        ActiveShiftContent(
                            shift = currentShift,
                            onCashMovementClick = { showCashMovementDialog = true },
                            onCloseShiftClick = { showCloseShiftDialog = true }
                        )
                    }
                }
                1 -> {
                    // TAB 1: RIWAYAT
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Date Filter Selector Bar
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color.White,
                            shadowElevation = 1.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showDateFilterDialog = true }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFE1F5FE),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarToday,
                                            contentDescription = "Pilih Tanggal",
                                            tint = OlseraBlueHeader,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Text(
                                    text = selectedDateText,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OlseraBlueHeader
                                )
                            }
                        }

                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

                        if (shiftViewModel.isHistoryLoading) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = OlseraBlueHeader)
                            }
                        } else if (shiftHistory.isEmpty()) {
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
                                    Text(
                                        text = "Tidak ada transaksi ditemukan",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF757575)
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = "Tidak ada transaksi untuk tanggal yang dipilih",
                                        fontSize = 14.sp,
                                        color = Color(0xFFBDBDBD),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            // HISTORY LIST
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(shiftHistory) { shift ->
                                    ShiftHistoryCard(
                                        shift = shift,
                                        onClick = {
                                            shift.id?.let { shiftViewModel.fetchShiftDetail(it) }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog Cash Movement
    if (showCashMovementDialog && currentShift != null) {
        CashMovementDialog(
            isSubmitting = shiftViewModel.isSubmitting,
            onDismiss = { showCashMovementDialog = false },
            onSubmit = { type, amount, desc ->
                currentShift.id?.let { shiftId ->
                    shiftViewModel.addCashMovement(
                        shiftId = shiftId,
                        type = type,
                        amount = amount,
                        description = desc,
                        outletId = nidOutlet,
                        onSuccess = { showCashMovementDialog = false }
                    )
                }
            }
        )
    }

    // Dialog Tutup Shift
    if (showCloseShiftDialog && currentShift != null) {
        CloseShiftDialog(
            expectedCash = currentShift.expectedCash?.toDoubleOrNull() ?: 0.0,
            isSubmitting = shiftViewModel.isSubmitting,
            onDismiss = { showCloseShiftDialog = false },
            onSubmit = { actualCash ->
                currentShift.id?.let { shiftId ->
                    shiftViewModel.closeShift(
                        shiftId = shiftId,
                        actualCashAmount = actualCash,
                        outletId = nidOutlet,
                        onSuccess = { showCloseShiftDialog = false }
                    )
                }
            }
        )
    }

    // Dialog Date Filter
    if (showDateFilterDialog) {
        OlseraDateFilterDialog(
            onDismiss = { showDateFilterDialog = false },
            onDateSelected = { result ->
                selectedDateText = result.displayLabel
                shiftViewModel.fetchShiftHistory(nidOutlet, page = 1)
            }
        )
    }

    // Dialog Shift Detail
    if (selectedDetail != null) {
        ShiftDetailDialog(
            shift = selectedDetail,
            onDismiss = { shiftViewModel.clearShiftDetail() }
        )
    }

    // Success / Error Alerts
    if (shiftViewModel.errorMessage != null) {
        AlertDialog(
            onDismissRequest = { shiftViewModel.clearError() },
            title = { Text("Informasi", fontWeight = FontWeight.Bold) },
            text = { Text(shiftViewModel.errorMessage ?: "") },
            confirmButton = {
                TextButton(onClick = { shiftViewModel.clearError() }) {
                    Text("OK", color = OlseraBlueHeader, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (shiftViewModel.successMessage != null) {
        AlertDialog(
            onDismissRequest = { shiftViewModel.clearSuccess() },
            title = { Text("Berhasil", fontWeight = FontWeight.Bold) },
            text = { Text(shiftViewModel.successMessage ?: "") },
            confirmButton = {
                TextButton(onClick = { shiftViewModel.clearSuccess() }) {
                    Text("OK", color = OlseraBlueHeader, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun StartShiftContent(
    isSubmitting: Boolean,
    onStartShift: (Double) -> Unit
) {
    var rawInput by remember { mutableStateOf("0") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.widthIn(max = 420.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Cash Register Icon / Graphic
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFF5F5F5),
                modifier = Modifier.size(120.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.PointOfSale,
                        contentDescription = "Kas Register",
                        tint = OlseraBlueHeader,
                        modifier = Modifier.size(72.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Text Label "Kas Awal di Laci"
            Text(
                text = "Kas Awal di Laci",
                fontSize = 13.sp,
                color = Color.LightGray,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Single line input for opening cash
            OutlinedTextField(
                value = rawInput,
                onValueChange = { input ->
                    val filtered = input.filter { it.isDigit() }
                    rawInput = if (filtered.isBlank()) "0" else filtered.trimStart('0').ifEmpty { "0" }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = OlseraBlueHeader
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Green Button "✔ Mulai Shift"
            Button(
                onClick = {
                    val amount = rawInput.toDoubleOrNull() ?: 0.0
                    onStartShift(amount)
                },
                enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = OlseraGreenButton),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Mulai Shift",
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

@Composable
private fun ActiveShiftContent(
    shift: Shift,
    onCashMovementClick: () -> Unit,
    onCloseShiftClick: () -> Unit
) {
    val openingCashVal = shift.openingCash?.toDoubleOrNull() ?: 0.0
    val cashSalesVal = shift.cashSales?.toDoubleOrNull() ?: 0.0
    val cashInVal = shift.cashIn?.toDoubleOrNull() ?: 0.0
    val cashOutVal = shift.cashOut?.toDoubleOrNull() ?: 0.0
    val cashRefundVal = shift.cashRefund?.toDoubleOrNull() ?: 0.0
    val cashCanceledVal = shift.cashCanceled?.toDoubleOrNull() ?: 0.0
    val netCashMovement = cashInVal - cashOutVal

    val expectedCashVal = shift.expectedCash?.toDoubleOrNull()
        ?: (openingCashVal + cashSalesVal + netCashMovement - cashRefundVal - cashCanceledVal)

    val totalKasVal = if (expectedCashVal > 0) expectedCashVal else (openingCashVal + cashSalesVal + netCashMovement)

    val cashierName = shift.user?.name ?: "wella"
    val formattedOpenedAt = formatShiftDate(shift.openedAt)

    Row(modifier = Modifier.fillMaxSize()) {
        // Left Column: Rincian Shift & Tombol Shift Berakhir
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(Color.White)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                ShiftInfoRow(
                    label = "Kasir",
                    value = cashierName,
                    isBold = true,
                    verticalPadding = 18.dp
                )
                HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 0.8.dp)

                ShiftInfoRow(
                    label = "Mulai Shift",
                    value = formattedOpenedAt,
                    isBold = true,
                    verticalPadding = 18.dp
                )
                HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 0.8.dp)

                ShiftInfoRow(
                    label = "Kas",
                    value = formatNumberDisplay(totalKasVal),
                    isBold = true,
                    verticalPadding = 18.dp
                )
                HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 0.8.dp)

                ShiftInfoRow(
                    label = "Awal di Laci",
                    value = formatNumberDisplay(openingCashVal),
                    startPadding = 32.dp,
                    verticalPadding = 18.dp
                )
                HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 0.8.dp)

                ShiftInfoRow(
                    label = "Kas Penjualan",
                    value = formatNumberDisplay(cashSalesVal),
                    startPadding = 32.dp,
                    verticalPadding = 18.dp
                )
                HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 0.8.dp)

                ShiftInfoRow(
                    label = "Kas Pengembalian",
                    value = formatNumberDisplay(cashRefundVal),
                    startPadding = 32.dp,
                    verticalPadding = 18.dp
                )
                HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 0.8.dp)

                ShiftInfoRow(
                    label = "Kas Pembatalan",
                    value = formatNumberDisplay(cashCanceledVal),
                    startPadding = 32.dp,
                    verticalPadding = 18.dp
                )
                HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 0.8.dp)

                ShiftInfoRow(
                    label = "Kas Masuk-Keluar",
                    value = formatNumberDisplay(netCashMovement),
                    startPadding = 32.dp,
                    verticalPadding = 18.dp
                )
                HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 0.8.dp)

                ShiftInfoRow(
                    label = "Total Diharapkan",
                    value = formatNumberDisplay(expectedCashVal),
                    isBold = true,
                    verticalPadding = 18.dp
                )
                HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 0.8.dp)
            }

            // Button "Shift Berakhir" at the bottom of the left column
            Button(
                onClick = onCloseShiftClick,
                colors = ButtonDefaults.buttonColors(containerColor = OlseraGreenButton),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .height(52.dp)
            ) {
                Text(
                    text = "Shift Berakhir",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // Vertical divider separating left and right column
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(1.dp)
                .background(Color(0xFFE0E0E0))
        )

        // Right Column: Kas Masuk-Keluar
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(Color(0xFFF9FAFB))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Kas Masuk-Keluar",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )
                    Text(
                        text = "${shift.cashMovements?.size ?: 0}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )
                }

                HorizontalDivider(color = Color(0xFFE0E0E0))

                val movements = shift.cashMovements ?: emptyList()
                if (movements.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Tidak ada Kas Masuk/Keluar",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF424242)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Tekan tombol + untuk menambah",
                                fontSize = 13.sp,
                                color = Color(0xFF757575)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(movements) { mov ->
                            CashMovementCard(mov)
                        }
                    }
                }
            }

            // Floating Action Button (+) at bottom right
            FloatingActionButton(
                onClick = onCashMovementClick,
                containerColor = Color(0xFF1976D2),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Tambah Kas Masuk/Keluar"
                )
            }
        }
    }
}

@Composable
private fun ShiftInfoRow(
    label: String,
    value: String,
    isBold: Boolean = false,
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
            fontSize = 14.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
            color = if (isBold) Color.Black else Color(0xFF555555)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
            color = valueColor,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun CashMovementCard(movement: CashMovement) {
    val amountVal = movement.amount?.toDoubleOrNull() ?: 0.0
    val isCashIn = movement.type == "CASH_IN"

    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isCashIn) "Kas Masuk" else "Kas Keluar",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCashIn) Color(0xFF2E7D32) else Color(0xFFC62828)
                )
                if (!movement.description.isNullOrBlank()) {
                    Text(
                        text = movement.description,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Text(
                text = "${if (isCashIn) "+" else "-"} ${formatNumberDisplay(amountVal)}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isCashIn) Color(0xFF2E7D32) else Color(0xFFC62828)
            )
        }
    }
}

@Composable
private fun ShiftDetailRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    valueColor: Color = Color(0xFF333333)
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = if (isBold) 15.sp else 14.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = if (isBold) Color.Black else Color(0xFF666666)
        )
        Text(
            text = value,
            fontSize = if (isBold) 16.sp else 14.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold,
            color = valueColor
        )
    }
}

@Composable
private fun ShiftHistoryCard(
    shift: Shift,
    onClick: () -> Unit
) {
    val openingCash = shift.openingCash?.toDoubleOrNull() ?: 0.0
    val actualCash = shift.actualCash?.toDoubleOrNull() ?: 0.0
    val difference = shift.difference?.toDoubleOrNull() ?: 0.0

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Shift #${shift.shiftNo ?: "-"}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = OlseraBlueHeader
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFECEFF1)
                ) {
                    Text(
                        text = shift.status ?: "CLOSED",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF455A64),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Buka: ${shift.openedAt ?: "-"}",
                fontSize = 12.sp,
                color = Color.Gray
            )

            if (!shift.closedAt.isNullOrBlank()) {
                Text(
                    text = "Tutup: ${shift.closedAt}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFFF0F0F0))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Kas Awal", fontSize = 11.sp, color = Color.Gray)
                    Text(formatCurrency(openingCash), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Kas Fisik (Aktual)", fontSize = 11.sp, color = Color.Gray)
                    Text(formatCurrency(actualCash), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = OlseraBlueHeader)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Selisih", fontSize = 11.sp, color = Color.Gray)
                    val diffColor = when {
                        difference < 0 -> Color(0xFFC62828)
                        difference > 0 -> Color(0xFF2E7D32)
                        else -> Color.Gray
                    }
                    Text(formatCurrency(difference), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = diffColor)
                }
            }
        }
    }
}

@Composable
private fun CashMovementDialog(
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (type: String, amount: Double, desc: String) -> Unit
) {
    var selectedType by remember { mutableStateOf("CASH_IN") }
    var amountInput by remember { mutableStateOf("") }
    var descInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Catat Cash In / Cash Out", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedType == "CASH_IN",
                        onClick = { selectedType = "CASH_IN" },
                        label = { Text("Cash In (+)") },
                        leadingIcon = {
                            Icon(Icons.Default.AddCircleOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFE8F5E9),
                            selectedLabelColor = Color(0xFF2E7D32)
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    FilterChip(
                        selected = selectedType == "CASH_OUT",
                        onClick = { selectedType = "CASH_OUT" },
                        label = { Text("Cash Out (-)") },
                        leadingIcon = {
                            Icon(Icons.Default.RemoveCircleOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFFEBEE),
                            selectedLabelColor = Color(0xFFC62828)
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { input -> amountInput = input.filter { it.isDigit() } },
                    label = { Text("Nominal (Rp)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = descInput,
                    onValueChange = { descInput = it },
                    label = { Text("Deskripsi / Keterangan") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountInput.toDoubleOrNull() ?: 0.0
                    onSubmit(selectedType, amount, descInput)
                },
                enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = OlseraBlueHeader)
            ) {
                Text("Simpan", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
private fun CloseShiftDialog(
    expectedCash: Double,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (actualCash: Double) -> Unit
) {
    var actualCashInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tutup Shift", fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    color = Color(0xFFF5F5F5),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Ekspektasi Kas di Laci:", fontSize = 12.sp, color = Color.Gray)
                        Text(formatCurrency(expectedCash), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = OlseraBlueHeader)
                    }
                }

                OutlinedTextField(
                    value = actualCashInput,
                    onValueChange = { input -> actualCashInput = input.filter { it.isDigit() } },
                    label = { Text("Kas Fisik di Laci (Rp)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val actual = actualCashInput.toDoubleOrNull() ?: 0.0
                    onSubmit(actual)
                },
                enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
            ) {
                Text("Tutup Shift", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
private fun ShiftDetailDialog(
    shift: Shift,
    onDismiss: () -> Unit
) {
    val openingCash = shift.openingCash?.toDoubleOrNull() ?: 0.0
    val cashSales = shift.cashSales?.toDoubleOrNull() ?: 0.0
    val cashIn = shift.cashIn?.toDoubleOrNull() ?: 0.0
    val cashOut = shift.cashOut?.toDoubleOrNull() ?: 0.0
    val expectedCash = shift.expectedCash?.toDoubleOrNull() ?: 0.0
    val actualCash = shift.actualCash?.toDoubleOrNull() ?: 0.0
    val difference = shift.difference?.toDoubleOrNull() ?: 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Detail Shift #${shift.shiftNo ?: "-"}", fontWeight = FontWeight.Bold, color = OlseraBlueHeader)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ShiftDetailRow(label = "Kas Awal", value = formatCurrency(openingCash))
                ShiftDetailRow(label = "Penjualan Tunai", value = formatCurrency(cashSales))
                ShiftDetailRow(label = "Cash In", value = formatCurrency(cashIn))
                ShiftDetailRow(label = "Cash Out", value = formatCurrency(cashOut))
                ShiftDetailRow(label = "Ekspektasi Kas", value = formatCurrency(expectedCash))
                ShiftDetailRow(label = "Kas Fisik (Aktual)", value = formatCurrency(actualCash), isBold = true)
                ShiftDetailRow(
                    label = "Selisih",
                    value = formatCurrency(difference),
                    isBold = true,
                    valueColor = if (difference < 0) Color.Red else Color(0xFF2E7D32)
                )

                if (!shift.cashMovements.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Riwayat Cash Movement:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    shift.cashMovements.forEach { mov ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${mov.type} - ${mov.description ?: ""}",
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            val amountVal = mov.amount?.toDoubleOrNull() ?: 0.0
                            Text(
                                text = formatCurrency(amountVal),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (mov.type == "CASH_IN") Color(0xFF2E7D32) else Color(0xFFC62828)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup", color = OlseraBlueHeader, fontWeight = FontWeight.Bold)
            }
        }
    )
}

private fun formatShiftDate(dateStr: String?): String {
    if (dateStr.isNullOrBlank()) return "Kam, 24 Sep 2026 07:34"
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date = inputFormat.parse(dateStr)
        if (date != null) {
            val outputFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm", Locale("id", "ID"))
            outputFormat.format(date)
        } else {
            dateStr
        }
    } catch (e: Exception) {
        dateStr
    }
}

private fun formatNumberDisplay(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"))
    formatter.maximumFractionDigits = 0
    return formatter.format(amount)
}

private fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"))
    formatter.maximumFractionDigits = 0
    return "Rp ${formatter.format(amount)}"
}
