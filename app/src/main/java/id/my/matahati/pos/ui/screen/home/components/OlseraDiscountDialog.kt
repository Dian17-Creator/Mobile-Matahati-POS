package id.my.matahati.pos.ui.screen.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
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

@Composable
fun OlseraDiscountDialog(
    vouchers: List<Voucher>,
    onDismiss: () -> Unit,
    onVoucherSelected: (Voucher) -> Unit = {}
) {
    var manualDiscount by remember { mutableStateOf("") }
    var voucherCodeInput by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .width(420.dp)
                .clip(RoundedCornerShape(8.dp)),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                // Title
                Text(
                    text = "Diskon",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Manual Discount Input
                OutlinedTextField(
                    value = manualDiscount,
                    onValueChange = { manualDiscount = it },
                    placeholder = { Text("[Masukkan Jumlah/Persen Diskon]") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = Color(0xFF0288D1),
                        unfocusedIndicatorColor = Color.LightGray
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Voucher Code Input
                OutlinedTextField(
                    value = voucherCodeInput,
                    onValueChange = { voucherCodeInput = it },
                    placeholder = { Text("[Masukkan Kode Voucher]") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = Color(0xFF0288D1),
                        unfocusedIndicatorColor = Color.LightGray
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Voucher List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    items(vouchers) { voucher ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { 
                                    onVoucherSelected(voucher)
                                    onDismiss()
                                }
                                .padding(horizontal = 20.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = "${voucher.description} (${voucher.code})",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black
                            )
                            Text(
                                text = "Disc. ${voucher.discountDisplay}",
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Footer Actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = "BATAL",
                            color = Color(0xFF0288D1),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
