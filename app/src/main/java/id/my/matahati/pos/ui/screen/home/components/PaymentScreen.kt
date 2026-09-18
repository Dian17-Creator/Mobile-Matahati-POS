package id.my.matahati.pos.ui.screen.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.my.matahati.pos.model.PaymentMethod
import id.my.matahati.pos.ui.screen.home.HomeViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun PaymentScreen(
    grandTotal: Double,
    paymentMethods: List<PaymentMethod>,
    isSubmitting: Boolean,
    errorMessage: String?,
    viewModel: HomeViewModel,
    cashierName: String,
    context: android.content.Context,
    onBack: () -> Unit,
    onPay: (PaymentMethod, Double) -> Unit,
    onFinishPayment: () -> Unit,
    modifier: Modifier = Modifier
) {
    val methods = paymentMethods.filter { it.id != "all" }.toMutableList()
    if (methods.none { it.name.uppercase().contains("CASH") || it.name.uppercase().contains("TUNAI") }) {
        methods.add(0, PaymentMethod(id = "1", name = "CASH"))
    }

    var selectedPayment by remember { 
        mutableStateOf(methods.find { it.name.uppercase().contains("CASH") || it.name.uppercase().contains("TUNAI") } ?: methods.firstOrNull()) 
    }
    var paidStr by remember { mutableStateOf(grandTotal.toLong().toString()) }
    var isInitialGrandTotal by remember { mutableStateOf(true) }

    val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID")).apply {
        maximumFractionDigits = 0
    }

    val paidAmount = paidStr.toDoubleOrNull() ?: 0.0

    val appendKey: (String) -> Unit = { key ->
        if (key == "C") {
            paidStr = "0"
            isInitialGrandTotal = false
        } else if (key == "BS") {
            if (isInitialGrandTotal) {
                paidStr = "0"
                isInitialGrandTotal = false
            } else {
                paidStr = if (paidStr.length > 1) paidStr.dropLast(1) else "0"
            }
        } else {
            if (isInitialGrandTotal) {
                paidStr = key
                isInitialGrandTotal = false
            } else {
                paidStr = if (paidStr == "0") key else paidStr + key
            }
        }
    }

    Surface(
        color = Color(0xFFF5F5F5),
        modifier = modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Surface(
                color = Color(0xFF1565C0),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(56.dp)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PEMBAYARAN",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Two Columns Layout
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left Column: Payment Methods
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    modifier = Modifier
                        .weight(0.4f)
                        .fillMaxHeight()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "METODE PEMBAYARAN",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(methods) { method ->
                                val isSelected = selectedPayment?.id == method.id
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) Color(0xFFE3F2FD) else Color(0xFFFAFAFA),
                                    border = BorderStroke(1.dp, if (isSelected) Color(0xFF1565C0) else Color(0xFFE0E0E0)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedPayment = method }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = null
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = method.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = if (isSelected) Color(0xFF1565C0) else Color.DarkGray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Right Column: Summary & Keypad
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    modifier = Modifier
                        .weight(0.6f)
                        .fillMaxHeight()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "DETAIL PEMBAYARAN",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )

                            // Total Tagihan Card
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFEBF3FA),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Total Tagihan", fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                    Text(
                                        text = "Rp ${formatter.format(grandTotal)}",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 18.sp,
                                        color = Color(0xFF1565C0)
                                    )
                                }
                            }

                            val isCash = selectedPayment?.name?.uppercase()?.contains("CASH") == true || 
                                         selectedPayment?.name?.uppercase()?.contains("TUNAI") == true ||
                                         selectedPayment == null

                            if (isCash) {
                                OutlinedTextField(
                                    value = "Rp ${formatter.format(paidAmount)}",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Nominal Pembayaran") },
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf(10000.0, 20000.0, 50000.0, 100000.0).forEach { nominal ->
                                        OutlinedButton(
                                            onClick = { 
                                                paidStr = nominal.toLong().toString()
                                                isInitialGrandTotal = false
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = "${(nominal / 1000).toInt()}rb",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                val keys = listOf(
                                    listOf("1", "2", "3"),
                                    listOf("4", "5", "6"),
                                    listOf("7", "8", "9"),
                                    listOf("C", "0", "BS")
                                )
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                ) {
                                    keys.forEach { row ->
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .weight(1f)
                                        ) {
                                            row.forEach { key ->
                                                Button(
                                                    onClick = { appendKey(key) },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF0F0F0)),
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .fillMaxHeight()
                                                ) {
                                                    if (key == "BS") {
                                                        Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = "Delete", tint = Color.DarkGray)
                                                    } else {
                                                        Text(
                                                            text = key,
                                                            fontSize = 20.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color.DarkGray
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.height(24.dp))
                                Text(
                                    text = "Pembayaran non-tunai (${selectedPayment?.name}) akan diproses secara otomatis sesuai total tagihan.",
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        Column(modifier = Modifier.fillMaxWidth()) {
                            if (errorMessage != null) {
                                Text(
                                    text = errorMessage,
                                    color = Color.Red,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            Button(
                                onClick = {
                                    if (selectedPayment != null) {
                                        val finalPaid = if (selectedPayment?.name?.uppercase()?.contains("CASH") == true || 
                                                            selectedPayment?.name?.uppercase()?.contains("TUNAI") == true) {
                                            paidAmount
                                        } else {
                                            grandTotal
                                        }
                                        onPay(selectedPayment!!, finalPaid)
                                    }
                                },
                                enabled = !isSubmitting,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top=16.dp)
                                    .height(52.dp)
                            ) {
                                if (isSubmitting) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                } else {
                                    Text(
                                        text = "BAYAR",
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (viewModel.showKitchenPrintDialog) {
        KitchenPrintSelectionDialog(
            changesCount = viewModel.printChangesCount,
            availableStations = viewModel.availableStations,
            selectedStations = viewModel.selectedStations,
            savedPrinters = viewModel.savedPrinters,
            onToggleStation = { viewModel.toggleStationSelection(it) },
            onConfirmPrint = { type ->
                viewModel.onConfirmKitchenPrint(
                    type = type,
                    onUpdateActiveCart = { _ -> }
                )
            },
            onDismiss = { viewModel.closeKitchenPrintDialog() }
        )
    }

    if (viewModel.showSimulatedReceipt) {
        SimulatedReceiptDialog(
            tickets = viewModel.receiptTickets,
            isPrinting = viewModel.isPrinting,
            onPrint = {
                viewModel.printKitchenTickets(context, viewModel.receiptTickets)
            },
            onDismiss = { viewModel.closeSimulatedReceipt() }
        )
    }

    if (viewModel.showReceiptDialog && viewModel.lastTransaction != null) {
        ReceiptDialog(
            transactionData = viewModel.lastTransaction!!,
            cashierName = cashierName,
            isPrinting = viewModel.isPrinting,
            onPrint = {
                if (viewModel.selectedPrinterAddress == null) {
                    viewModel.openPrinterSelection(id.my.matahati.pos.data.printer.BluetoothPrinterManager(context))
                } else {
                    viewModel.printReceipt(context, viewModel.lastTransaction!!, cashierName)
                }
            },
            onDismiss = {
                viewModel.closeReceiptDialog()
                onFinishPayment()
            }
        )
    }
}