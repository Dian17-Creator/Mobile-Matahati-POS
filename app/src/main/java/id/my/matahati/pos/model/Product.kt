package id.my.matahati.pos.model

import com.google.gson.annotations.SerializedName
import java.text.NumberFormat
import java.util.Locale

data class ProductResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("data")
    val data: List<ProductDto>
)

data class ProductDto(
    @SerializedName("nid")
    val nid: Int,
    @SerializedName("nid_category")
    val nidCategory: Int?,
    @SerializedName("cname")
    val cname: String,
    @SerializedName("nprice")
    val nprice: Double,
    @SerializedName("cphotos")
    val cphotos: String?,
    @SerializedName("cstatus")
    val cstatus: String?
) {
    fun toProduct(): Product {
        return Product(
            id = nid.toString(),
            name = cname,
            price = nprice,
            categoryId = nidCategory?.toString() ?: "1",
            stock = 99,
            iconEmoji = "🍽️",
            imageUrl = cphotos,
            isAvailable = cstatus?.lowercase() != "inactive",
            stationName = if (cname.lowercase().contains("tea") || cname.lowercase().contains("kopi") || cname.lowercase().contains("ice")) "BAR" else "DAPUR"
        )
    }
}

data class Product(
    val id: String,
    val name: String,
    val price: Double,
    val categoryId: String,
    val stock: Int,
    val iconEmoji: String = "📦",
    val imageUrl: String? = null,
    val isAvailable: Boolean = true,
    val stationName: String = "DAPUR" // Default station
) {
    val formattedPrice: String
        get() {
            val formatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"))
            formatter.maximumFractionDigits = 0
            return formatter.format(price)
        }
}
