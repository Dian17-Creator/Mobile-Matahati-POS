package id.my.matahati.pos.ui.screen.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import id.my.matahati.pos.model.Voucher
import java.text.NumberFormat
import java.util.Locale

@Composable
fun OlseraDiscountDialog(
    vouchers: List<Voucher>,
    cartSubtotal: Double,
    onDismiss: () -> Unit,
    onVoucherSelected: (Voucher) -> Unit = {}
) {
    var manualDiscount by remember { mutableStateOf("") }
    var voucherCodeInput by remember { mutableStateOf("") }

    val formatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID")).apply {
        maximumFractionDigits = 0
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .width(480.dp)
                .clip(RoundedCornerShape(12.dp)),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
            ) {
                // Title Area with Close Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Terapkan Diskon",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Manual Discount Input
                OutlinedTextField(
                    value = manualDiscount,
                    onValueChange = { manualDiscount = it },
                    label = { Text("Diskon Manual (Rp atau %)") },
                    placeholder = { Text("Contoh: 10000 atau 10%") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1565C0),
                        unfocusedBorderColor = Color.LightGray
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), color = Color.LightGray.copy(alpha = 0.5f))

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Voucher Tersedia",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Voucher List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 350.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(vouchers) { voucher ->
                        val isEligible = cartSubtotal >= voucher.minSpend
                        val isOutOfStock = voucher.remainingQuota <= 0

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isEligible && !isOutOfStock) Color(0xFFF8FAFC) else Color(0xFFF1F5F9).copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, if (isEligible && !isOutOfStock) Color(0xFFE2E8F0) else Color.Transparent),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = isEligible && !isOutOfStock) { 
                                    onVoucherSelected(voucher)
                                    onDismiss()
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ConfirmationNumber,
                                    contentDescription = null,
                                    tint = if (isEligible) Color(0xFF1565C0) else Color.Gray,
                                    modifier = Modifier.size(32.dp)
                                )
                                
                                Spacer(modifier = Modifier.width(16.dp))
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = voucher.code,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isEligible) Color.Black else Color.Gray
                                    )
                                    Text(
                                        text = voucher.description,
                                        fontSize = 12.sp,
                                        color = Color.DarkGray,
                                        maxLines = 1
                                    )
                                    
                                    Spacer(modifier = Modifier.height(4.dp))
                                    
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Potongan: ${voucher.discountDisplay}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isEligible) Color(0xFF2E7D32) else Color.Gray
                                        )
                                    }
                                    
                                    Text(
                                        text = "Min. Belanja: ${formatter.format(voucher.minSpend)}",
                                        fontSize = 11.sp,
                                        color = if (isEligible) Color.DarkGray else Color(0xFFD32F2F)
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Sisa: ${voucher.remainingQuota}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isOutOfStock) Color.Red else Color.DarkGray
                                    )
                                    if (!isEligible) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = "Syarat tidak terpenuhi",
                                            tint = Color(0xFFD32F2F),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Full-width SIMPAN Button
                Button(
                    onClick = {
                        // Manual discount logic could be added here if needed
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(48.dp)
                ) {
                    Text("SIMPAN", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}
