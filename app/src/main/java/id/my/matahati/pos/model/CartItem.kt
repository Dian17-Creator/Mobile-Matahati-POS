package id.my.matahati.pos.model

import java.text.NumberFormat
import java.util.Locale

data class CartItem(
    val product: Product,
    val quantity: Int,
    val note: String = "",
    val sentQuantity: Int = 0 // Track how many items already sent to kitchen/bar
) {
    val totalPrice: Double
        get() = product.price * quantity

    val formattedTotalPrice: String
        get() {
            val formatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"))
            formatter.maximumFractionDigits = 0
            return formatter.format(totalPrice)
        }

    fun encodeNoteWithSentQty(): String? {
        val cleanNote = note.replace(Regex("\\[SENT:\\d+\\]"), "").trim()
        if (sentQuantity <= 0) {
            return cleanNote.ifBlank { null }
        }
        return if (cleanNote.isBlank()) {
            "[SENT:$sentQuantity]"
        } else {
            "$cleanNote [SENT:$sentQuantity]"
        }
    }

    companion object {
        fun parseNoteAndSentQty(rawNote: String?): Pair<String, Int> {
            if (rawNote.isNullOrBlank()) return Pair("", 0)
            val match = Regex("\\[SENT:(\\d+)\\]").find(rawNote)
            val sentQty = match?.groupValues?.get(1)?.toIntOrNull() ?: 0
            val cleanNote = rawNote.replace(Regex("\\[SENT:\\d+\\]"), "").trim()
            return Pair(cleanNote, sentQty)
        }
    }
}
