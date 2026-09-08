package id.my.matahati.pos.ui.screen.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.my.matahati.pos.model.CartItem
import id.my.matahati.pos.model.Customer
import java.text.NumberFormat
import java.util.Locale

val OlseraGreenPay = Color(0xFF4CAF50)
val OlseraHeaderBlue = Color(0xFF1565C0)
val OlseraLightBg = Color(0xFFEBF3FA)

@Composable
fun OlseraCartPanel(
    cartItems: List<CartItem>,
    customers: List<Customer>,
    onIncreaseQuantity: (CartItem) -> Unit,
    onDecreaseQuantity: (CartItem) -> Unit,
    onClearCart: () -> Unit,
    onCheckoutClick: () -> Unit,
    modifier: Modifier = Modifier,
    cashierName: String = "april"
) {
    val totalAmount = cartItems.sumOf { it.totalPrice }
    val totalItemsCount = cartItems.sumOf { it.quantity }
    var showMore by remember { mutableStateOf(false) }

    var orderType by remember { mutableStateOf("") }
    var customerName by remember { mutableStateOf("") }
    var showOrderTypeDialog by remember { mutableStateOf(false) }
    var showCustomerDialog by remember { mutableStateOf(false) }

    Surface(
        color = OlseraLightBg,
        modifier = modifier.fillMaxHeight()
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
        ) {
            // Header Status: Left = Customer Icon, Center = Clickable "Pesanan Baru" / Order Type & Customer Text, Right = Plus Button
            Surface(
                color = Color.White,
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Customer Icon Button
                    IconButton(
                        onClick = { showCustomerDialog = true },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(OlseraHeaderBlue.copy(alpha = 0.1f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Pilih Pelanggan",
                            tint = OlseraHeaderBlue,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Center: Order Type & Customer Name Text (Clickable to open In/Away dialog)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showOrderTypeDialog = true },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (orderType.isBlank() && customerName.isBlank()) {
                            Text(
                                text = "Pesanan Baru",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = OlseraHeaderBlue,
                                textAlign = TextAlign.Center
                            )
                        } else {
                            if (orderType.isNotBlank()) {
                                Text(
                                    text = orderType,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OlseraHeaderBlue,
                                    textAlign = TextAlign.Center
                                )
                            }
                            if (customerName.isNotBlank()) {
                                Text(
                                    text = customerName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = OlseraHeaderBlue,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Right: Plus Button
                    IconButton(
                        onClick = { showCustomerDialog = true },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(OlseraHeaderBlue)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Tambah/Pilih Pelanggan",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // Table Header: Item | Qty | Total
            Surface(
                color = Color(0xFFCFD8DC),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Item",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray,
                        modifier = Modifier.weight(0.45f)
                    )
                    Text(
                        text = "Qty",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(0.3f)
                    )
                    Text(
                        text = "Total",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(0.25f)
                    )
                }
            }

            // Cart Items Scrollable List
            if (cartItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada item dipesan",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(
                        items = cartItems,
                        key = { it.product.id }
                    ) { cartItem ->
                        OlseraCartItemRow(
                            cartItem = cartItem,
                            onIncrease = { onIncreaseQuantity(cartItem) },
                            onDecrease = { onDecreaseQuantity(cartItem) }
                        )
                    }
                }
            }

            // Metadata info below items list
            Surface(
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Pajak", fontSize = 12.sp, color = Color.Gray)
                        Text(text = "0", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Jumlah Item: $totalItemsCount", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.DarkGray)
                        Text(text = "Dilayani Oleh: $cashierName", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }

            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))

            // Quick Action Buttons Bar
            Surface(
                color = OlseraLightBg,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        QuickActionButton(
                            icon = if (showMore) Icons.Default.ExpandLess else Icons.Default.GridView,
                            label = if (showMore) "Tutup" else "Lainnya",
                            onClick = { showMore = !showMore }
                        )
                        QuickActionButton(icon = Icons.Default.LocalOffer, label = "Disc. Pesanan", onClick = {})
                        QuickActionButton(icon = Icons.Default.LocalShipping, label = "Ongkos Kirim", onClick = {})
                        QuickActionButton(icon = Icons.Default.ChatBubbleOutline, label = "Catatan Pesanan", onClick = {})
                        QuickActionButton(icon = Icons.Default.Person, label = "Dilayani Oleh", onClick = {})
                        QuickActionButton(icon = Icons.Default.Restaurant, label = "Kirim ke Dapur", onClick = {})
                        QuickActionButton(
                            icon = Icons.Default.Close,
                            label = "Batal Pesanan",
                            isDestructive = true,
                            onClick = onClearCart
                        )
                    }

                    if (showMore) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            QuickActionButton(icon = Icons.Default.Print, label = "Cetak Periksa", onClick = {})
                            QuickActionButton(icon = Icons.Default.Redeem, label = "Tebus Point", onClick = {})
                            QuickActionButton(icon = Icons.Default.Share, label = "Share Pesanan", onClick = {})
                            QuickActionButton(icon = Icons.Default.GroupAdd, label = "Referral Pelanggan", onClick = {})
                            Spacer(modifier = Modifier.width(50.dp))
                            Spacer(modifier = Modifier.width(50.dp))
                            Spacer(modifier = Modifier.width(50.dp))
                        }
                    }
                }
            }

            // Big Green Pay Bar at the bottom
            Surface(
                color = OlseraGreenPay,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (cartItems.isNotEmpty()) onCheckoutClick()
                    }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Rp ${formatRawCurrency(totalAmount)}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }
        }
    }

    // Order Type (In/Away) Selection Dialog
    if (showOrderTypeDialog) {
        AlertDialog(
            onDismissRequest = { showOrderTypeDialog = false },
            title = { Text("In/Away", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    val types = listOf("DINE-IN", "TAKE-AWAY", "DELIVERY", "GOFOOD", "GRABFOOD", "SHOPEEFOOD", "TRAVELOKA-EATS", "MAXIMFOOD", "+REMARK")
                    types.forEach { type ->
                        TextButton(
                            onClick = {
                                orderType = type
                                showOrderTypeDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = type,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.DarkGray,
                                textAlign = TextAlign.Start,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showOrderTypeDialog = false }) {
                    Text("BATAL", color = OlseraHeaderBlue, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Customer Selection Dialog
    if (showCustomerDialog) {
        AlertDialog(
            onDismissRequest = { showCustomerDialog = false },
            title = { Text("Pilih Pelanggan", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().height(300.dp)
                ) {
                    items(customers) { customer ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF1F5F9),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    customerName = "A.N ${customer.name}"
                                    showCustomerDialog = false
                                }
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(text = customer.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.DarkGray)
                                if (customer.phone.isNotBlank()) {
                                    Text(text = customer.phone, fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCustomerDialog = false }) {
                    Text("Tutup")
                }
            }
        )
    }
}

@Composable
private fun OlseraCartItemRow(
    cartItem: CartItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Item Name
            Text(
                text = cartItem.product.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(0.45f)
            )

            // Price & Qty Adjuster Controls
            Row(
                modifier = Modifier.weight(0.3f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatRawCurrency(cartItem.product.price),
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFE0E0E0))
                        .clickable { onDecrease() }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(text = "-", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Text(
                    text = "${cartItem.quantity}x",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFE0E0E0))
                        .clickable { onIncrease() }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(text = "+", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Line Total
            Text(
                text = formatRawCurrency(cartItem.totalPrice),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(0.25f)
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .width(50.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (isDestructive) Color(0xFFE53935) else Color(0xFF0288D1)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            color = if (isDestructive) Color(0xFFD32F2F) else OlseraHeaderBlue,
            textAlign = TextAlign.Center,
            maxLines = 2,
            minLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 11.sp
        )
    }
}

private fun formatRawCurrency(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"))
    formatter.maximumFractionDigits = 0
    return formatter.format(amount)
}
