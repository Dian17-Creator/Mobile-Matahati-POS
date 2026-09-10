package id.my.matahati.pos.ui.screen.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import id.my.matahati.pos.model.CartItem
import java.text.NumberFormat
import java.util.Locale

@Composable
fun OlseraEditItemDialog(
    cartItem: CartItem,
    onDismiss: () -> Unit,
    onConfirmUpdate: (Int, String) -> Unit,
    onRemoveItem: () -> Unit
) {
    var quantity by remember { mutableIntStateOf(cartItem.quantity) }
    var note by remember { mutableStateOf(cartItem.note) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Hapus Item?", fontWeight = FontWeight.Bold) },
            text = { Text("Apakah Anda yakin ingin menghapus ${cartItem.product.name} dari pesanan?") },
            confirmButton = {
                TextButton(onClick = {
                    onRemoveItem()
                    showDeleteConfirm = false
                    onDismiss()
                }) {
                    Text("HAPUS", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("BATAL", color = Color.Gray)
                }
            }
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .width(400.dp)
                .clip(RoundedCornerShape(16.dp)),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header: Product Name
                Text(
                    text = cartItem.product.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Price Info
                Text(
                    text = "Harga: Rp ${formatRawCurrency(cartItem.product.price)}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = Color(0xFFEEEEEE))
                Spacer(modifier = Modifier.height(24.dp))

                // Quantity Control
                Text(
                    text = "Quantity",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Minus Button
                    IconButton(
                        onClick = {
                            if (quantity > 1) {
                                quantity--
                            } else {
                                showDeleteConfirm = true
                            }
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .border(1.dp, Color.LightGray, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Kurang",
                            tint = OlseraHeaderBlue
                        )
                    }

                    Spacer(modifier = Modifier.width(32.dp))

                    Text(
                        text = "$quantity",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.DarkGray
                    )

                    Spacer(modifier = Modifier.width(32.dp))

                    // Plus Button
                    IconButton(
                        onClick = { quantity++ },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(OlseraHeaderBlue)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Tambah",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Note Input
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Catatan") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = OlseraHeaderBlue,
                        unfocusedIndicatorColor = Color.LightGray
                    )
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Action Buttons: Batal (Left) & Simpan (Right)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Batal Button
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFEEBEE)
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Text(
                            "BATAL",
                            color = Color.Red,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    // Simpan Button
                    Button(
                        onClick = {
                            onConfirmUpdate(quantity, note)
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OlseraHeaderBlue
                        )
                    ) {
                        Text(
                            "SIMPAN",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

private fun formatRawCurrency(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"))
    formatter.maximumFractionDigits = 0
    return formatter.format(amount)
}
