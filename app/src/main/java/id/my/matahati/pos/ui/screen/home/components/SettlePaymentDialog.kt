package id.my.matahati.pos.ui.screen.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.my.matahati.pos.model.PaymentMethod
import id.my.matahati.pos.model.SettlePaymentRequest
import java.text.NumberFormat
import java.util.Locale

@Composable
fun SettlePaymentDialog(
    transactionNo: String,
    remainingAmount: Double,
    paymentMethods: List<PaymentMethod>,
    isSubmitting: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSubmit: (SettlePaymentRequest) -> Unit
) {
    val methods = remember(paymentMethods) { paymentMethods.filter { it.id != "all" } }
    var selectedPayment by remember {
        mutableStateOf(methods.find { it.name.uppercase().contains("CASH") || it.name.uppercase().contains("TUNAI") } ?: methods.firstOrNull())
    }
    var noteStr by remember { mutableStateOf("Pelunasan Reservasi") }
    var refNoStr by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID")).apply {
        maximumFractionDigits = 0
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Pelunasan Reservasi / DP",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFFE65100)
                )
                Text(
                    text = "No. Transaksi: $transactionNo",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Sisa Tagihan Card
                Surface(
                    color = Color(0xFFFFF3E0),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SISA TAGIHAN",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE65100)
                        )
                        Text(
                            text = "Rp ${formatter.format(remainingAmount)}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFE65100)
                        )
                    }
                }

                Text(
                    text = "Pilih Metode Pembayaran Pelunasan",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )

                LazyColumn(
                    modifier = Modifier.height(130.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(methods) { method ->
                        val isSelected = selectedPayment?.id == method.id
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) Color(0xFFFFF3E0) else Color(0xFFFAFAFA),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) Color(0xFFE65100) else Color(0xFFE0E0E0)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedPayment = method }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = isSelected, onClick = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = method.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (isSelected) Color(0xFFE65100) else Color.DarkGray
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = refNoStr,
                    onValueChange = { refNoStr = it },
                    label = { Text("No. Referensi / Transaksi Non-Tunai (Opsional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = noteStr,
                    onValueChange = { noteStr = it },
                    label = { Text("Catatan Pelunasan") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                val err = localError ?: errorMessage
                if (err != null) {
                    Text(text = err, color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedPayment == null) {
                        localError = "Pilih metode pembayaran"
                    } else {
                        val paymentIdInt = selectedPayment!!.id.toIntOrNull() ?: 1
                        val request = SettlePaymentRequest(
                            paymentId = paymentIdInt,
                            amount = remainingAmount,
                            referenceNo = refNoStr.ifBlank { null },
                            note = noteStr.ifBlank { "Pelunasan Reservasi" }
                        )
                        onSubmit(request)
                    }
                },
                enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100))
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                } else {
                    Text("BAYAR PELUNASAN", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        },
        shape = RoundedCornerShape(12.dp)
    )
}
