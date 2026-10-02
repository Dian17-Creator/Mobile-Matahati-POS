package id.my.matahati.pos.model

import com.google.gson.annotations.SerializedName

data class ProductSalesSummaryResponse(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("data")
    val data: ProductSalesSummaryData? = null
)

data class ProductSalesSummaryData(
    @SerializedName("summary")
    val summary: ProductSalesSummaryHeader? = null,

    @SerializedName("items")
    val items: List<ProductSalesSummaryItem> = emptyList()
)

data class ProductSalesSummaryHeader(
    @SerializedName("grand_total_qty")
    val grandTotalQty: Int = 0,

    @SerializedName("grand_total_sales")
    val grandTotalSales: Double = 0.0
)

data class ProductSalesSummaryItem(
    @SerializedName("product_id")
    val productId: Long = 0,

    @SerializedName("product_name")
    val productName: String = "",

    @SerializedName("category_name")
    val categoryName: String = "",

    @SerializedName("price")
    val price: Double = 0.0,

    @SerializedName("sold_qty")
    val soldQty: Int = 0,

    @SerializedName("total_sales")
    val totalSales: Double = 0.0
)
