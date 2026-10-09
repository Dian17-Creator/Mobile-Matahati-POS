package id.my.matahati.pos.ui.screen.home.components

import id.my.matahati.pos.ui.theme.AppPrimaryColor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import id.my.matahati.pos.model.Product
import java.text.NumberFormat
import java.util.Locale

@Composable
fun OlseraProductGrid(
    products: List<Product>,
    onAddToCart: (Product) -> Unit,
    modifier: Modifier = Modifier,
    columnsCount: Int = 4,
    orderType: String = ""
) {
    if (products.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Produk tidak ditemukan 🔍",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(columnsCount),
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = products,
                key = { it.id }
            ) { product ->
                OlseraProductCard(
                    product = product,
                    orderType = orderType,
                    onClick = { onAddToCart(product) }
                )
            }
        }
    }
}

@Composable
fun OlseraProductCard(
    product: Product,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    orderType: String = ""
) {
    val isOnline = orderType.equals("ONLINE", ignoreCase = true)
    val displayPrice = product.getEffectivePrice(isOnline)
    val isOutOfStock = product.stock <= 0

    Card(
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(containerColor = AppPrimaryColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Product Image Container (Square)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.25f)
                    .background(Color(0xFFFDFDFD)),
                contentAlignment = Alignment.Center
            ) {
                if (!product.imageUrl.isNullOrBlank()) {
                    val fullImageUrl = if (product.imageUrl.startsWith("http")) {
                        product.imageUrl
                    } else {
                        "http://localhost:8000/${product.imageUrl.removePrefix("/")}"
                    }
                    AsyncImage(
                        model = fullImageUrl,
                        contentDescription = product.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = product.iconEmoji,
                        fontSize = 36.sp
                    )
                }

                // Overlay if out of stock
                if (isOutOfStock) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.35f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            color = Color(0xFFD32F2F),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "STOK HABIS",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Product Text Container (Name, Stock & Price)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFFFFF))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Text(
                    text = product.name.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF37474F),
                    minLines = 2,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 14.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Stock info in bottom-left
                    Text(
                        text = "Stok: ${product.stock}",
                        fontSize = 10.sp,
                        fontWeight = if (isOutOfStock) FontWeight.ExtraBold else FontWeight.SemiBold,
                        color = if (isOutOfStock) Color(0xFFD32F2F) else Color(0xFF555555)
                    )

                    // Right: Price
                    Text(
                        text = formatRawCurrency(displayPrice),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AppPrimaryColor,
                        textAlign = TextAlign.End
                    )
                }

                val secondaryText = when {
                    isOnline -> "Toko: Rp ${formatRawCurrency(product.price)}"
                    product.onlinePrice > 0.0 -> "Online: Rp ${formatRawCurrency(product.onlinePrice)}"
                    else -> " "
                }

                Text(
                    text = secondaryText,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.Gray,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

private fun formatRawCurrency(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"))
    formatter.maximumFractionDigits = 0
    return formatter.format(amount)
}
