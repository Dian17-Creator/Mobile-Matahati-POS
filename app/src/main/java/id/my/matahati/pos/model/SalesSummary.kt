package id.my.matahati.pos.model

import java.text.NumberFormat
import java.util.Locale

data class SalesSummary(
    val totalSalesToday: Double,
    val totalTransactions: Int,
    val activeShift: String,
    val cashierName: String
) {
    val formattedTotalSales: String
        get() {
            val formatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"))
            formatter.maximumFractionDigits = 0
            return formatter.format(totalSalesToday)
        }
}
