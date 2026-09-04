package id.my.matahati.pos.model

import java.text.NumberFormat
import java.util.Locale

data class Product(
    val id: String,
    val name: String,
    val price: Double,
    val categoryId: String,
    val stock: Int,
    val iconEmoji: String = "📦",
    val isAvailable: Boolean = true
) {
    val formattedPrice: String
        get() {
            val formatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"))
            formatter.maximumFractionDigits = 0
            return formatter.format(price)
        }
}
