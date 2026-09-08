package id.my.matahati.pos.model

import com.google.gson.annotations.SerializedName

data class CustomerResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("data")
    val data: List<CustomerDto>
)

data class CustomerDto(
    @SerializedName("nid")
    val nid: Int,
    @SerializedName("cname")
    val cname: String,
    @SerializedName("cphone")
    val cphone: String?,
    @SerializedName("cemail")
    val cemail: String?,
    @SerializedName("caddress")
    val caddress: String?
) {
    fun toCustomer(): Customer {
        return Customer(
            id = nid.toString(),
            name = cname,
            phone = cphone ?: "",
            email = cemail ?: "",
            address = caddress ?: ""
        )
    }
}

data class Customer(
    val id: String,
    val name: String,
    val phone: String,
    val email: String,
    val address: String
)
