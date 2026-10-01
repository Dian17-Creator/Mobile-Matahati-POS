package id.my.matahati.pos.model

import com.google.gson.annotations.SerializedName

data class DashboardResponse(
    @SerializedName("success")
    val success: Boolean = false,
    
    @SerializedName("message")
    val message: String? = null,
    
    @SerializedName("data")
    val data: DashboardData? = null
)

data class DashboardData(
    @SerializedName("date_from")
    val dateFrom: String? = null,
    
    @SerializedName("date_to")
    val dateTo: String? = null,
    
    @SerializedName("total_sales")
    val totalSales: Double = 0.0,
    
    @SerializedName("total_refund")
    val totalRefund: Double = 0.0,
    
    @SerializedName("sales_by_date")
    val salesByDate: List<SalesByDate> = emptyList(),
    
    @SerializedName("top_product_groups")
    val topProductGroups: List<TopProductGroup> = emptyList()
)

data class SalesByDate(
    @SerializedName("date")
    val date: String = "",
    
    @SerializedName("total")
    val total: Double = 0.0
)

data class TopProductGroup(
    @SerializedName("nid")
    val nid: Int? = null,
    
    @SerializedName("name")
    val name: String = "",
    
    @SerializedName("qty")
    val qty: Int = 0,
    
    @SerializedName("total")
    val total: Double = 0.0
)
