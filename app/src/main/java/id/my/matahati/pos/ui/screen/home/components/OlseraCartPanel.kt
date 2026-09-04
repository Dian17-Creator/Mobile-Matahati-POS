package id.my.matahati.pos.ui.screen.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import java.text.NumberFormat
import java.util.Locale

val OlseraGreenPay = Color(0xFF4CAF50)
val OlseraHeaderBlue = Color(0xFF1565C0)
val OlseraLightBg = Color(0xFFEBF3FA)

@Composable
fun OlseraCartPanel(
    cartItems: List<CartItem>,
    onIncreaseQuantity: (CartItem) -> Unit,
    onDecreaseQuantity: (CartItem) -> Unit,
    onClearCart: () -> Unit,
    onCheckoutClick: () -> Unit,
    modifier: Modifier = Modifier,
    customerName: String = "A.N DITO",
    orderType: String = "DINE-IN",
    cashierName: String = "april"
) {
    val totalAmount = cartItems.sumOf { it.totalPrice }
    val totalItemsCount = cartItems.sumOf { it.quantity }

    Surface(
        color = OlseraLightBg,
        modifier = modifier.fillMaxHeight()
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
        ) {
            // Header Status: DINE-IN A.N DITO + Button Add Customer
            Surface(
                color = Color.White,
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "$orderType",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = OlseraHeaderBlue
                        )
                        Text(
                            text = "$customerName",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = OlseraHeaderBlue
                        )
                    }

                    IconButton(
                        onClick = { },
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(OlseraHeaderBlue)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Tambah Pelanggan",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
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

            // Quick Action Circular Buttons Bar (Olsera Style)
            Surface(
                color = OlseraLightBg,
                modifier = Modifier.fillMaxWidth()
            ) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item { QuickActionButton(icon = Icons.Default.LocalOffer, label = "Disc. Pesanan", onClick = {}) }
                    item { QuickActionButton(icon = Icons.Default.LocalShipping, label = "Ongkos Kirim", onClick = {}) }
                    item { QuickActionButton(icon = Icons.Default.ChatBubbleOutline, label = "Catatan Pesanan", onClick = {}) }
                    item { QuickActionButton(icon = Icons.Default.Person, label = "Dilayani Oleh", onClick = {}) }
                    item { QuickActionButton(icon = Icons.Default.Restaurant, label = "Kirim ke Dapur", onClick = {}) }
                    item { QuickActionButton(icon = Icons.Default.Redeem, label = "Tebus Point", onClick = {}) }
                    item {
                        QuickActionButton(
                            icon = Icons.Default.Close,
                            label = "Batal Pesanan",
                            isDestructive = true,
                            onClick = onClearCart
                        )
                    }
                }
            }

            // Big Green Pay Bar at the bottom (Olsera Style)
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
                    text = "${formatRawCurrency(cartItem.product.price)}",
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
            .width(58.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(if (isDestructive) Color(0xFFE53935) else Color(0xFF0288D1)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            color = if (isDestructive) Color(0xFFD32F2F) else OlseraHeaderBlue,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun formatRawCurrency(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"))
    formatter.maximumFractionDigits = 0
    return formatter.format(amount)
}
