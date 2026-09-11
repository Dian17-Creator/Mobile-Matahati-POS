package id.my.matahati.pos.ui.screen.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.my.matahati.pos.model.PaymentMethod
import java.text.NumberFormat
import java.util.Locale

@Composable
fun PaymentInputDialog(
    grandTotal: Double,
    paymentMethods: List<PaymentMethod>,
    onDismiss: () -> Unit,
    onSubmit: (PaymentMethod, Double) -> Unit
) {
    var selectedPayment by remember { mutableStateOf<PaymentMethod?>(null) }
    var paidAmountStr by remember { mutableStateOf(grandTotal.toLong().toString()) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val formatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID")).apply {
        maximumFractionDigits = 0
    }
    val formattedTotal = formatter.format(grandTotal)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Pembayaran", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = "Total Tagihan: $formattedTotal", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "Metode Pembayaran", fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                
                // Exclude "Semua Tipe Pembayaran"
                val methods = paymentMethods.filter { it.id != "all" }
                
                LazyColumn(modifier = Modifier.height(120.dp)) {
                    items(methods) { method ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedPayment = method }
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = selectedPayment?.id == method.id,
                                onClick = { selectedPayment = method }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = method.name)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = paidAmountStr,
                    onValueChange = { 
                        if (it.all { char -> char.isDigit() }) {
                            paidAmountStr = it
                            errorMsg = null
                        }
                    },
                    label = { Text("Nominal Pembayaran") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    isError = errorMsg != null
                )
                if (errorMsg != null) {
                    Text(text = errorMsg!!, color = Color.Red, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val paid = paidAmountStr.toDoubleOrNull() ?: 0.0
                    if (selectedPayment == null) {
                        errorMsg = "Pilih metode pembayaran"
                    } else if (paid < grandTotal) {
                        errorMsg = "Nominal kurang dari tagihan"
                    } else {
                        onSubmit(selectedPayment!!, paid)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = OlseraGreenPay)
            ) {
                Text("Bayar", color = Color.White)
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
