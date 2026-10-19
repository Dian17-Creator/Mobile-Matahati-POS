package id.my.matahati.pos.ui.screen.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.my.matahati.pos.model.PaymentMethod
import id.my.matahati.pos.ui.theme.AppPrimaryColor
import java.text.NumberFormat
import java.util.Locale

@Composable
fun PaymentScreen(
    grandTotal: Double,
    paymentMethods: List<PaymentMethod>,
    isSubmitting: Boolean,
    errorMessage: String?,
    onBack: () -> Unit,
    onPay: (PaymentMethod, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

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

    val isCash = selectedPayment?.name?.uppercase()?.contains("CASH") == true ||
            selectedPayment?.name?.uppercase()?.contains("TUNAI") == true ||
            selectedPayment == null

    val triggerPay = {
        if (selectedPayment != null) {
            val finalPaid = if (isCash) paidAmount else grandTotal
            onPay(selectedPayment!!, finalPaid)
        }
    }

    Surface(
        color = Color(0xFFF5F5F5),
        modifier = modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Surface(
                color = AppPrimaryColor,
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

            if (isTablet) {
                // ==========================================
                // TABLET LAYOUT (SPLIT 2 COLUMNS: 40% & 60%)
                // ==========================================
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
                                        color = if (isSelected) Color(0xFFE0F7FA) else Color(0xFFFAFAFA),
                                        border = BorderStroke(1.dp, if (isSelected) AppPrimaryColor else Color(0xFFE0E0E0)),
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
                                                color = if (isSelected) AppPrimaryColor else Color.DarkGray
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
                                    color = Color(0xFFE0F7FA),
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
                                            color = AppPrimaryColor
                                        )
                                    }
                                }

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
                                    onClick = triggerPay,
                                    enabled = !isSubmitting,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 16.dp)
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
            } else {
                // ==========================================
                // MOBILE PHONE LAYOUT (SINGLE COLUMN VERTICAL)
                // ==========================================
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 1. Total Tagihan Card
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFE0F7FA),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total Tagihan", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.DarkGray)
                            Text(
                                text = "Rp ${formatter.format(grandTotal)}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                color = AppPrimaryColor
                            )
                        }
                    }

                    // 2. Metode Pembayaran Chips / Cards
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "METODE PEMBAYARAN",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            val methodRows = remember(methods) { methods.chunked(2) }
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                methodRows.forEach { rowMethods ->
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        rowMethods.forEach { method ->
                                            val isSelected = selectedPayment?.id == method.id
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = if (isSelected) Color(0xFFE0F7FA) else Color(0xFFFAFAFA),
                                                border = BorderStroke(1.dp, if (isSelected) AppPrimaryColor else Color(0xFFE0E0E0)),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable { selectedPayment = method }
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.Center
                                                ) {
                                                    RadioButton(
                                                        selected = isSelected,
                                                        onClick = null,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = method.name,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp,
                                                        color = if (isSelected) AppPrimaryColor else Color.DarkGray
                                                    )
                                                }
                                            }
                                        }
                                        if (rowMethods.size < 2) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 3. Detail Pembayaran (Numpad / Quick Nominal jika Cash)
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "DETAIL PEMBAYARAN",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )

                            if (isCash) {
                                OutlinedTextField(
                                    value = "Rp ${formatter.format(paidAmount)}",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Nominal Pembayaran") },
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                )

                                // Quick Cash Buttons (Uang Pas, 10rb, 20rb, 50rb, 100rb)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
//                                    OutlinedButton(
//                                        onClick = {
//                                            paidStr = grandTotal.toLong().toString()
//                                            isInitialGrandTotal = false
//                                        },
//                                        modifier = Modifier.weight(1f),
//                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
//                                        shape = RoundedCornerShape(6.dp)
//                                    ) {
//                                        Text(
//                                            text = "Uang Pas",
//                                            fontSize = 11.sp,
//                                            fontWeight = FontWeight.Bold
//                                        )
//                                    }

                                    listOf(10000.0, 20000.0, 50000.0, 100000.0).forEach { nominal ->
                                        OutlinedButton(
                                            onClick = {
                                                paidStr = nominal.toLong().toString()
                                                isInitialGrandTotal = false
                                            },
                                            modifier = Modifier.weight(1f),
                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = "${(nominal / 1000).toInt()}rb",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                // Keypad / Numpad Grid
                                val keys = listOf(
                                    listOf("1", "2", "3"),
                                    listOf("4", "5", "6"),
                                    listOf("7", "8", "9"),
                                    listOf("C", "0", "BS")
                                )
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    keys.forEach { row ->
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            row.forEach { key ->
                                                Button(
                                                    onClick = { appendKey(key) },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF0F0F0)),
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(48.dp)
                                                ) {
                                                    if (key == "BS") {
                                                        Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = "Delete", tint = Color.DarkGray)
                                                    } else {
                                                        Text(
                                                            text = key,
                                                            fontSize = 18.sp,
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
                                Text(
                                    text = "Pembayaran non-tunai (${selectedPayment?.name}) akan diproses secara otomatis sesuai total tagihan.",
                                    color = Color.Gray,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(vertical = 12.dp)
                                )
                            }
                        }
                    }

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage,
                            color = Color.Red,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = triggerPay,
                        enabled = !isSubmitting,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
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
