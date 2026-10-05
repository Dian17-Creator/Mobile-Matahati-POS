package id.my.matahati.pos.ui.screen.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AltRoute
import androidx.compose.material.icons.automirrored.filled.CallSplit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import id.my.matahati.pos.model.CartItem
import java.text.NumberFormat
import java.util.Locale

private val OlseraPanelBg = Color(0xFF1E2638)
private val OlseraPanelHeaderTabBg = Color(0xFF181F2E)
private val OlseraPanelCardBg = Color(0xFF263248)
private val OlseraPanelBlueBtn = Color(0xFF24BBCC)
private val OlseraPanelGreenBtn = Color(0xFF2E7D32)
private val OlseraPanelRedBtn = Color(0xFFE53935)
private val OlseraTextLight = Color(0xFFECEFF1)
private val OlseraTextMuted = Color(0xFF90A4AE)

@Composable
fun OlseraEditItemPanel(
    cartItem: CartItem,
    onDismiss: () -> Unit,
    onConfirmUpdate: (newQty: Int, newNote: String) -> Unit,
    onRemoveItem: () -> Unit,
    modifier: Modifier = Modifier
) {
    var quantity by remember(cartItem) { mutableIntStateOf(cartItem.quantity) }
    var note by remember(cartItem) { mutableStateOf(cartItem.note) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Item Pesanan, 1 = Items Paket, 2 = Add-Ons
    var selectedOrderType by remember { mutableStateOf("DINE-IN") }
    var discountInput by remember { mutableStateOf("0") }
    var isPercentageDiscount by remember { mutableStateOf(false) }

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

    Surface(
        modifier = modifier
            .fillMaxHeight()
            .width(660.dp),
        color = OlseraPanelBg,
        shadowElevation = 16.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // =========================================================
            // 1. TOP HEADER: Tabs & Close Button X
            // =========================================================
            Surface(
                color = OlseraPanelHeaderTabBg,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Header Tabs
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        PanelTabItem(
                            title = "Item Pesanan",
                            isSelected = selectedTab == 0,
                            onClick = { selectedTab = 0 }
                        )
                        PanelTabItem(
                            title = "Items Paket",
                            isSelected = selectedTab == 1,
                            onClick = { selectedTab = 1 }
                        )
                        PanelTabItem(
                            title = "Add-Ons",
                            isSelected = selectedTab == 2,
                            onClick = { selectedTab = 2 }
                        )
                    }

                    // Red 'X' Close Button
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(OlseraPanelRedBtn.copy(alpha = 0.2f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = OlseraPanelRedBtn,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

            // =========================================================
            // MAIN BODY: Scrollable content
            // =========================================================
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // =====================================================
                // 2. SELECTED MENU ROW: Avatar + Product Name + In/Aw Toggle
                // =====================================================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Product Image Avatar
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!cartItem.product.imageUrl.isNullOrBlank()) {
                                val fullImageUrl = if (cartItem.product.imageUrl.startsWith("http")) {
                                    cartItem.product.imageUrl
                                } else {
                                    "http://localhost:8000/${cartItem.product.imageUrl.removePrefix("/")}"
                                }
                                AsyncImage(
                                    model = fullImageUrl,
                                    contentDescription = cartItem.product.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                )
                            } else {
                                Text(
                                    text = cartItem.product.iconEmoji.ifBlank { "🍔" },
                                    fontSize = 24.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = cartItem.product.name.uppercase(),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = OlseraTextLight,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // DINE-IN / TAKEAWAY Switch
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.12f),
                        modifier = Modifier.clickable {
                            selectedOrderType = if (selectedOrderType == "DINE-IN") "TAKEAWAY" else "DINE-IN"
                        }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (selectedOrderType == "DINE-IN") Color(0xFF4CAF50) else Color(0xFFFF9800))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = selectedOrderType,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = OlseraTextLight
                            )
                        }
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                // =====================================================
                // 3. HARGA & QUANTITY ROW
                // =====================================================
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Harga Column
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Harga",
                                fontSize = 13.sp,
                                color = OlseraTextMuted
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = formatRawCurrency(cartItem.product.price),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = OlseraTextLight
                            )
                        }

                        // Qty Number Column
                        Column(
                            horizontalAlignment = Alignment.Start,
                            modifier = Modifier.weight(0.8f)
                        ) {
                            Text(
                                text = "Qty",
                                fontSize = 13.sp,
                                color = OlseraTextMuted
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$quantity",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = OlseraTextLight
                            )
                        }

                        // Stepper Controls [ - | + ]
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
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
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Kurang",
                                    tint = OlseraTextLight,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Vertical Line Separator
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(24.dp)
                                    .background(Color.White.copy(alpha = 0.2f))
                            )

                            // Plus Button
                            IconButton(
                                onClick = { quantity++ },
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Tambah",
                                    tint = OlseraTextLight,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Garis bawah terpisah untuk Harga dan Quantity, tanpa garis bawah di bawah button [- | +]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Garis bawah untuk Harga
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 16.dp)
                        ) {
                            HorizontalDivider(color = Color.White.copy(alpha = 0.12f))
                        }

                        // Garis bawah untuk Qty
                        Box(
                            modifier = Modifier
                                .weight(0.8f)
                                .padding(end = 16.dp)
                        ) {
                            HorizontalDivider(color = Color.White.copy(alpha = 0.12f))
                        }

                        // Area kosong di bawah button stepper (tanpa garis)
                        Box(
                            modifier = Modifier.width(80.dp)
                        )
                    }
                }

                // =====================================================
                // 4. DISKON SECTION
                // =====================================================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Diskon",
                            fontSize = 13.sp,
                            color = OlseraTextMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = discountInput.ifBlank { "0" },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = OlseraTextLight
                        )
                    }

                    // Mode Diskon Switch (Rp / %)
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.12f),
                        modifier = Modifier.clickable {
                            isPercentageDiscount = !isPercentageDiscount
                        }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sell,
                                contentDescription = "Diskon Mode",
                                tint = OlseraTextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isPercentageDiscount) "%" else "Rp",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = OlseraTextLight
                            )
                        }
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                // =====================================================
                // 5. CATATAN SECTION (Floating Label)
                // =====================================================
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextField(
                        value = note,
                        onValueChange = { note = it },
                        label = {
                            Text(
                                text = "Catatan",
                                color = OlseraTextMuted
                            )
                        },
                        placeholder = {
                            Text(
                                text = "Tambahkan catatan pesanan...",
                                fontSize = 15.sp,
                                color = OlseraTextMuted.copy(alpha = 0.5f)
                            )
                        },
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = OlseraTextLight
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedLabelColor = OlseraTextMuted,
                            unfocusedLabelColor = OlseraTextMuted
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                }
            }

            // =========================================================
            // BOTTOM FOOTER: Action Buttons + Simpan Button
            // =========================================================
            Surface(
                color = OlseraPanelBg,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 6. ACTION BUTTONS ROW: Split, Multi Split, Batal
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Split Button
                        Button(
                            onClick = { /* Split Item Feature */ },
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = OlseraPanelGreenBtn),
                            elevation = ButtonDefaults.buttonElevation(0.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.CallSplit,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Split", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        // Multi Split Button
                        Button(
                            onClick = { /* Multi Split Item Feature */ },
                            modifier = Modifier
                                .weight(1.2f)
                                .height(42.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = OlseraPanelGreenBtn),
                            elevation = ButtonDefaults.buttonElevation(0.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.AltRoute,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Multi Split", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        // Batal / Hapus Button
                        Button(
                            onClick = {
                                showDeleteConfirm = true
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = OlseraPanelRedBtn),
                            elevation = ButtonDefaults.buttonElevation(0.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Batal", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    // 7. SIMPAN BUTTON (Full-Width Blue)
                    Button(
                        onClick = {
                            onConfirmUpdate(quantity, note)
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OlseraPanelBlueBtn)
                    ) {
                        Text(
                            text = "Simpan",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PanelTabItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White else OlseraTextMuted
        )

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .height(2.dp)
                .width(40.dp)
                .background(if (isSelected) OlseraPanelBlueBtn else Color.Transparent)
        )
    }
}

private fun formatRawCurrency(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"))
    formatter.maximumFractionDigits = 0
    return formatter.format(amount)
}
