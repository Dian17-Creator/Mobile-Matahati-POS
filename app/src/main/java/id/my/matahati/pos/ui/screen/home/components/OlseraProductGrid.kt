package id.my.matahati.pos.ui.screen.home.components

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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
    columnsCount: Int = 4
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
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                    .background(Color(0xFFE8ECEF)),
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
            }

            // Product Text Container (Name & Price)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE3F2FD))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Text(
                    text = product.name.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF37474F),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 14.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = formatRawCurrency(product.price),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0D47A1),
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
