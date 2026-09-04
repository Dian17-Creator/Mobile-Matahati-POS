package id.my.matahati.pos.model

import java.text.NumberFormat
import java.util.Locale

data class CartItem(
    val product: Product,
    val quantity: Int
) {
    val totalPrice: Double
        get() = product.price * quantity

    val formattedTotalPrice: String
        get() {
            val formatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"))
            formatter.maximumFractionDigits = 0
            return formatter.format(totalPrice)
        }
}
