package id.my.matahati.pos.ui.screen.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
    initialDpAmount: Double? = null,
    initialPaymentId: String? = null,
    initialPaymentDualId: String? = null,
    onBack: () -> Unit,
    onPay: (payment1: PaymentMethod, paidAmount: Double, payment2: PaymentMethod?, dpAmount: Double?) -> Unit,
    onStateChange: ((isDual: Boolean, payment1: PaymentMethod?, payment2: PaymentMethod?, dpAmount: Double?) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    val methods = paymentMethods.filter { it.id != "all" }.toMutableList()
    if (methods.none { it.name.uppercase().contains("CASH") || it.name.uppercase().contains("TUNAI") }) {
        methods.add(0, PaymentMethod(id = "1", name = "CASH"))
    }

    val hasInitialDp = initialDpAmount != null && initialDpAmount > 0

    var isDualPayment by remember { mutableStateOf(hasInitialDp) }

    var selectedPayment1 by remember {
        mutableStateOf(
            if (!initialPaymentId.isNullOrBlank()) methods.find { it.id == initialPaymentId } ?: (methods.find { it.name.uppercase().contains("CASH") || it.name.uppercase().contains("TUNAI") } ?: methods.firstOrNull())
            else methods.find { it.name.uppercase().contains("CASH") || it.name.uppercase().contains("TUNAI") } ?: methods.firstOrNull()
        )
    }
    var selectedPayment2 by remember {
        mutableStateOf<PaymentMethod?>(
            if (!initialPaymentDualId.isNullOrBlank()) methods.find { it.id == initialPaymentDualId }
            else methods.find { it.id != selectedPayment1?.id }
        )
    }

    var paidStr1 by remember {
        mutableStateOf(
            if (hasInitialDp) initialDpAmount.toLong().toString()
            else grandTotal.toLong().toString()
        )
    }
    var paidStr2 by remember {
        mutableStateOf(
            if (hasInitialDp) (grandTotal - initialDpAmount).coerceAtLeast(0.0).toLong().toString()
            else "0"
        )
    }

    var isInitialGrandTotal by remember { mutableStateOf(!hasInitialDp) }
    var isInitialTarget2 by remember { mutableStateOf(true) }
    var activeInputTarget by remember { mutableStateOf(1) } // 1 for Method 1, 2 for Method 2
    var localErrorMsg by remember { mutableStateOf<String?>(null) }

    val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID")).apply {
        maximumFractionDigits = 0
    }

    val paidAmount1 = paidStr1.toDoubleOrNull() ?: 0.0
    val paidAmount2 = paidStr2.toDoubleOrNull() ?: 0.0

    // Report state changes to parent (HomeScreen / ViewModel)
    LaunchedEffect(isDualPayment, selectedPayment1, selectedPayment2, paidAmount1) {
        onStateChange?.invoke(
            isDualPayment,
            selectedPayment1,
            selectedPayment2,
            if (isDualPayment) paidAmount1 else null
        )
    }

    // Dynamic recalculation of Nominal 2 (paidStr2) whenever Nominal 1 changes in Dual Payment mode
    LaunchedEffect(paidStr1, isDualPayment) {
        if (isDualPayment && activeInputTarget == 1) {
            val p1 = paidStr1.toDoubleOrNull() ?: 0.0
            val remaining = (grandTotal - p1).coerceAtLeast(0.0).toLong()
            paidStr2 = remaining.toString()
            isInitialTarget2 = true
        }
    }

    val appendKey: (String) -> Unit = { key ->
        val isTarget1 = activeInputTarget == 1
        var targetStr = if (isTarget1) paidStr1 else paidStr2
        val isInitial = if (isTarget1) isInitialGrandTotal else isInitialTarget2

        if (key == "C") {
            targetStr = "0"
            if (isTarget1) isInitialGrandTotal = false else isInitialTarget2 = false
        } else if (key == "BS") {
            if (isInitial) {
                targetStr = "0"
                if (isTarget1) isInitialGrandTotal = false else isInitialTarget2 = false
            } else {
                targetStr = if (targetStr.length > 1) targetStr.dropLast(1) else "0"
            }
        } else {
            if (isInitial) {
                targetStr = key
                if (isTarget1) isInitialGrandTotal = false else isInitialTarget2 = false
            } else {
                targetStr = if (targetStr == "0") key else targetStr + key
            }
        }

        if (isTarget1) {
            paidStr1 = targetStr
        } else {
            paidStr2 = targetStr
        }
    }

    val isCash1 = selectedPayment1?.name?.uppercase()?.contains("CASH") == true ||
            selectedPayment1?.name?.uppercase()?.contains("TUNAI") == true ||
            selectedPayment1 == null

    val triggerPay = {
        localErrorMsg = null
        if (selectedPayment1 == null) {
            localErrorMsg = "Pilih metode pembayaran utama"
        } else if (!isDualPayment) {
            val finalPaid = if (isCash1) paidAmount1 else grandTotal
            if (isCash1 && finalPaid < grandTotal) {
                // Treated as DP reservation
                onPay(selectedPayment1!!, finalPaid, null, finalPaid)
            } else {
                onPay(selectedPayment1!!, finalPaid, null, null)
            }
        } else {
            // Dual Payment mode
            val dp = paidAmount1
            val second = paidAmount2
            if (dp <= 0) {
                localErrorMsg = "Nominal pembayaran 1 / DP tidak boleh 0"
            } else if (selectedPayment2 != null && selectedPayment2?.id == selectedPayment1?.id) {
                localErrorMsg = "Metode pembayaran 1 dan 2 tidak boleh sama"
            } else if (selectedPayment2 != null) {
                // Split Payment (2 Methods)
                val totalPaid = dp + second
                if (totalPaid < grandTotal) {
                    localErrorMsg = "Total pembayaran kurang dari total tagihan (Rp ${formatter.format(grandTotal)})"
                } else {
                    onPay(selectedPayment1!!, totalPaid, selectedPayment2, dp)
                }
            } else {
                // DP Only (Reservation)
                onPay(selectedPayment1!!, dp, null, dp)
            }
        }
    }

    Surface(
        color = Color(0xFFF5F5F5),
        modifier = modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar with Dual Payment Switch on the Right
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
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
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

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Dual Payment",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Switch(
                            checked = isDualPayment,
                            onCheckedChange = { checked ->
                                localErrorMsg = null
                                isDualPayment = checked
                                if (checked) {
                                    if (paidStr1 == grandTotal.toLong().toString() || paidStr1 == "0") {
                                        val half = (grandTotal / 2).toLong()
                                        paidStr1 = half.toString()
                                        paidStr2 = (grandTotal.toLong() - half).toString()
                                    } else {
                                        val remaining = (grandTotal - paidAmount1).coerceAtLeast(0.0).toLong()
                                        paidStr2 = remaining.toString()
                                    }
                                    if (selectedPayment2 == null || selectedPayment2?.id == selectedPayment1?.id) {
                                        selectedPayment2 = methods.find { it.id != selectedPayment1?.id }
                                    }
                                } else {
                                    paidStr1 = grandTotal.toLong().toString()
                                    paidStr2 = "0"
                                    isInitialGrandTotal = true
                                    activeInputTarget = 1
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AppPrimaryColor,
                                checkedTrackColor = Color.White,
                                uncheckedThumbColor = Color.LightGray,
                                uncheckedTrackColor = Color.White.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            }

            if (isTablet) {
                // TABLET LAYOUT
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Left Column: Payment Methods Selection
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
                                text = if (isDualPayment) "METODE 1 (DP / BAYAR UTAMA)" else "METODE PEMBAYARAN",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppPrimaryColor
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                items(methods) { method ->
                                    val isSelected = selectedPayment1?.id == method.id
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) Color(0xFFE0F7FA) else Color(0xFFFAFAFA),
                                        border = BorderStroke(1.dp, if (isSelected) AppPrimaryColor else Color(0xFFE0E0E0)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedPayment1 = method
                                                activeInputTarget = 1
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(selected = isSelected, onClick = null)
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
                            }

                            if (isDualPayment) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "METODE 2 (SISA TAGIHAN / SPLIT)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE65100)
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    items(methods) { method ->
                                        val isSelected2 = selectedPayment2?.id == method.id
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isSelected2) Color(0xFFFFF3E0) else Color(0xFFFAFAFA),
                                            border = BorderStroke(1.dp, if (isSelected2) Color(0xFFE65100) else Color(0xFFE0E0E0)),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    selectedPayment2 = method
                                                    activeInputTarget = 2
                                                    isInitialTarget2 = true
                                                }
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                RadioButton(selected = isSelected2, onClick = null)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = method.name,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = if (isSelected2) Color(0xFFE65100) else Color.DarkGray
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Right Column: Summary & Inputs
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
                                verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                            .padding(14.dp),
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

                                if (isDualPayment) {
                                    // Row Side-by-Side: Nominal 1 (Left) & Nominal 2 (Right)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable {
                                                    activeInputTarget = 1
                                                    isInitialGrandTotal = true
                                                }
                                        ) {
                                            OutlinedTextField(
                                                value = "Rp ${formatter.format(paidAmount1)}",
                                                onValueChange = {},
                                                readOnly = true,
                                                enabled = false,
                                                label = { Text("Nominal 1 (${selectedPayment1?.name ?: ""})") },
                                                modifier = Modifier.fillMaxWidth(),
                                                textStyle = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AppPrimaryColor),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    disabledBorderColor = if (activeInputTarget == 1) AppPrimaryColor else Color.LightGray,
                                                    disabledLabelColor = if (activeInputTarget == 1) AppPrimaryColor else Color.Gray,
                                                    disabledTextColor = AppPrimaryColor
                                                )
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable {
                                                    activeInputTarget = 2
                                                    isInitialTarget2 = true
                                                }
                                        ) {
                                            OutlinedTextField(
                                                value = "Rp ${formatter.format(paidAmount2)}",
                                                onValueChange = {},
                                                readOnly = true,
                                                enabled = false,
                                                label = { Text("Nominal 2 (${selectedPayment2?.name ?: ""})") },
                                                modifier = Modifier.fillMaxWidth(),
                                                textStyle = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100)),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    disabledBorderColor = if (activeInputTarget == 2) Color(0xFFE65100) else Color.LightGray,
                                                    disabledLabelColor = if (activeInputTarget == 2) Color(0xFFE65100) else Color.Gray,
                                                    disabledTextColor = Color(0xFFE65100)
                                                )
                                            )
                                        }
                                    }
                                } else {
                                    if (isCash1) {
                                        OutlinedTextField(
                                            value = "Rp ${formatter.format(paidAmount1)}",
                                            onValueChange = {},
                                            readOnly = true,
                                            label = { Text("Nominal Pembayaran") },
                                            modifier = Modifier.fillMaxWidth(),
                                            textStyle = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                        )
                                    } else {
                                        Text(
                                            text = "Pembayaran non-tunai (${selectedPayment1?.name}) akan diproses secara otomatis sesuai total tagihan.",
                                            color = Color.Gray,
                                            fontSize = 14.sp,
                                            modifier = Modifier.padding(vertical = 16.dp)
                                        )
                                    }
                                }

                                // Quick Nominal Buttons (10k, 20k, 50k, 100k)
                                if (isDualPayment || isCash1) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        listOf(10000.0, 20000.0, 50000.0, 100000.0).forEach { nominal ->
                                            OutlinedButton(
                                                onClick = {
                                                    if (activeInputTarget == 1) {
                                                        paidStr1 = nominal.toLong().toString()
                                                        isInitialGrandTotal = false
                                                    } else {
                                                        paidStr2 = nominal.toLong().toString()
                                                        isInitialTarget2 = false
                                                    }
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
                                                            .height(85.dp)
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
                                }
                            }

                            Column(modifier = Modifier.fillMaxWidth()) {
                                val displayErr = localErrorMsg ?: errorMessage
                                if (displayErr != null) {
                                    Text(
                                        text = displayErr,
                                        color = Color.Red,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Button(
                                    onClick = triggerPay,
                                    enabled = !isSubmitting,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                ) {
                                    if (isSubmitting) {
                                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                    } else {
                                        Text(
                                            text = if (isDualPayment) "PROSES DUAL PAYMENT" else "BAYAR",
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
                // MOBILE PHONE LAYOUT
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(12.dp)
                        .navigationBarsPadding(),
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

                    // 2. Metode Pembayaran 1
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
                                text = if (isDualPayment) "METODE 1 (DP / UTAMA)" else "METODE PEMBAYARAN",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppPrimaryColor
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            val methodRows = remember(methods) { methods.chunked(2) }
                            Column(
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                methodRows.forEach { rowMethods ->
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        rowMethods.forEach { method ->
                                            val isSelected = selectedPayment1?.id == method.id
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = if (isSelected) Color(0xFFE0F7FA) else Color(0xFFFAFAFA),
                                                border = BorderStroke(1.dp, if (isSelected) AppPrimaryColor else Color(0xFFE0E0E0)),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable {
                                                        selectedPayment1 = method
                                                        activeInputTarget = 1
                                                    }
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 10.dp, vertical = 10.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.Center
                                                ) {
                                                    RadioButton(selected = isSelected, onClick = null, modifier = Modifier.size(18.dp))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = method.name,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp,
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

                    // 3. Metode Pembayaran 2 (If Dual Payment)
                    if (isDualPayment) {
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
                                    text = "METODE 2 (SISA TAGIHAN / SPLIT)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE65100)
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                val methodRows = remember(methods) { methods.chunked(2) }
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    methodRows.forEach { rowMethods ->
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            rowMethods.forEach { method ->
                                                val isSelected2 = selectedPayment2?.id == method.id
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = if (isSelected2) Color(0xFFFFF3E0) else Color(0xFFFAFAFA),
                                                    border = BorderStroke(1.dp, if (isSelected2) Color(0xFFE65100) else Color(0xFFE0E0E0)),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clickable {
                                                            selectedPayment2 = method
                                                            activeInputTarget = 2
                                                            isInitialTarget2 = true
                                                        }
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(horizontal = 10.dp, vertical = 10.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.Center
                                                    ) {
                                                        RadioButton(selected = isSelected2, onClick = null, modifier = Modifier.size(18.dp))
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(
                                                            text = method.name,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 13.sp,
                                                            color = if (isSelected2) Color(0xFFE65100) else Color.DarkGray
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
                    }

                    // 4. Detail Pembayaran & Numpad
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

                            if (isDualPayment) {
                                // Row Side-by-Side: Nominal 1 (Left) & Nominal 2 (Right)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                activeInputTarget = 1
                                                isInitialGrandTotal = true
                                            }
                                    ) {
                                        OutlinedTextField(
                                            value = "Rp ${formatter.format(paidAmount1)}",
                                            onValueChange = {},
                                            readOnly = true,
                                            enabled = false,
                                            label = { Text("Nominal 1 (${selectedPayment1?.name ?: ""})") },
                                            modifier = Modifier.fillMaxWidth(),
                                            textStyle = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AppPrimaryColor),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                disabledBorderColor = if (activeInputTarget == 1) AppPrimaryColor else Color.LightGray,
                                                disabledLabelColor = if (activeInputTarget == 1) AppPrimaryColor else Color.Gray,
                                                disabledTextColor = AppPrimaryColor
                                            )
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                activeInputTarget = 2
                                                isInitialTarget2 = true
                                            }
                                    ) {
                                        OutlinedTextField(
                                            value = "Rp ${formatter.format(paidAmount2)}",
                                            onValueChange = {},
                                            readOnly = true,
                                            enabled = false,
                                            label = { Text("Nominal 2 (${selectedPayment2?.name ?: ""})") },
                                            modifier = Modifier.fillMaxWidth(),
                                            textStyle = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100)),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                disabledBorderColor = if (activeInputTarget == 2) Color(0xFFE65100) else Color.LightGray,
                                                disabledLabelColor = if (activeInputTarget == 2) Color(0xFFE65100) else Color.Gray,
                                                disabledTextColor = Color(0xFFE65100)
                                            )
                                        )
                                    }
                                }
                            } else {
                                if (isCash1) {
                                    OutlinedTextField(
                                        value = "Rp ${formatter.format(paidAmount1)}",
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Nominal Pembayaran") },
                                        modifier = Modifier.fillMaxWidth(),
                                        textStyle = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    )
                                } else {
                                    Text(
                                        text = "Pembayaran non-tunai (${selectedPayment1?.name}) akan diproses secara otomatis sesuai total tagihan.",
                                        color = Color.Gray,
                                        fontSize = 13.sp,
                                        modifier = Modifier.padding(vertical = 12.dp)
                                    )
                                }
                            }

                            if (isDualPayment || isCash1) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf(10000.0, 20000.0, 50000.0, 100000.0).forEach { nominal ->
                                        OutlinedButton(
                                            onClick = {
                                                if (activeInputTarget == 1) {
                                                    paidStr1 = nominal.toLong().toString()
                                                    isInitialGrandTotal = false
                                                } else {
                                                    paidStr2 = nominal.toLong().toString()
                                                    isInitialTarget2 = false
                                                }
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
                                                        .height(45.dp)
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
                            }
                        }
                    }

                    val displayErr = localErrorMsg ?: errorMessage
                    if (displayErr != null) {
                        Text(
                            text = displayErr,
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
                            .height(45.dp)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                text = if (isDualPayment) "PROSES DUAL PAYMENT" else "BAYAR",
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
