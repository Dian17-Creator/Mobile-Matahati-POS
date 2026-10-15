package id.my.matahati.pos.model

import com.google.gson.annotations.SerializedName
import id.my.matahati.pos.data.remote.RetrofitClient
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
    @SerializedName("nprice_online")
    val npriceOnline: Double? = null,
    @SerializedName("nqty")
    val nqty: Int? = null,
    @SerializedName("cphotos")
    val cphotos: String?,
    @SerializedName("cstatus")
    val cstatus: String?,
    @SerializedName("ftrack_stock")
    val ftrackStock: Boolean? = true
) {
    fun toProduct(categories: List<Category> = emptyList()): Product {
        val matchedCategory = categories.find { it.id == nidCategory?.toString() }
        val effectiveTrackStock = ftrackStock ?: matchedCategory?.ftrackStock ?: true

        val catNameUpper = matchedCategory?.name?.trim()?.uppercase() ?: ""
        val prodNameUpper = cname.trim().uppercase()

        val isDrink = catNameUpper.contains("MINUM") ||
                catNameUpper.contains("DRINK") ||
                catNameUpper.contains("BEVERAGE") ||
                catNameUpper.contains("BAR") ||
                catNameUpper.contains("KOPI") ||
                catNameUpper.contains("TEA") ||
                nidCategory == 2 ||
                nidCategory == 10 || nidCategory == 11 || nidCategory == 12 || nidCategory == 13 ||
                prodNameUpper.contains("TEA") ||
                prodNameUpper.contains("KOPI") ||
                prodNameUpper.contains("COFFEE") ||
                prodNameUpper.contains("ICE") ||
                prodNameUpper.contains("ES ") ||
                prodNameUpper.startsWith("ES ") ||
                prodNameUpper.contains("JUS") ||
                prodNameUpper.contains("JUICE") ||
                prodNameUpper.contains("LATTE") ||
                prodNameUpper.contains("MILK") ||
                prodNameUpper.contains("BOBA") ||
                prodNameUpper.contains("SQUASH") ||
                prodNameUpper.contains("DRINK")

        val station = if (isDrink) "BAR" else "DAPUR"

        return Product(
            id = nid.toString(),
            name = cname,
            price = nprice,
            onlinePrice = npriceOnline ?: 0.0,
            categoryId = nidCategory?.toString() ?: "1",
            stock = nqty ?: 0,
            iconEmoji = "📦",
            imageUrl = cphotos,
            isAvailable = cstatus?.lowercase() != "inactive",
            ftrackStock = effectiveTrackStock,
            category = matchedCategory,
            stationName = station
        )
    }
}

data class Product(
    val id: String,
    val name: String,
    val price: Double,
    val onlinePrice: Double = 0.0,
    val categoryId: String,
    val stock: Int,
    val iconEmoji: String = "📦",
    val imageUrl: String? = null,
    val isAvailable: Boolean = true,
    val ftrackStock: Boolean = true,
    val category: Category? = null,
    val stationName: String = "DAPUR" // Default station
) {
    val isTrackStock: Boolean
        get() = ftrackStock && (category?.ftrackStock ?: true)

    val nqty: Int
        get() = stock
    val formattedPrice: String
        get() {
            val formatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"))
            formatter.maximumFractionDigits = 0
            return formatter.format(price)
        }

    val formattedOnlinePrice: String
        get() {
            val formatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"))
            formatter.maximumFractionDigits = 0
            return formatter.format(onlinePrice)
        }

    fun getEffectivePrice(isOnline: Boolean): Double {
        return if (isOnline && onlinePrice > 0.0) onlinePrice else price
    }

    val fullImageUrl: String?
        get() {
            val url = imageUrl?.trim() ?: return null
            if (url.isBlank()) return null
            return when {
                url.startsWith("http://", ignoreCase = true) || url.startsWith("https://", ignoreCase = true) -> url
                else -> {
                    val baseUrl = RetrofitClient.BASE_URL.removeSuffix("/")
                    val path = url.removePrefix("/")
                    "$baseUrl/$path"
                }
            }
        }
}
