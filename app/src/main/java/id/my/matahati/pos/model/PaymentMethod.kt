package id.my.matahati.pos.model

import com.google.gson.annotations.SerializedName

data class PaymentMethodResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("data")
    val data: List<PaymentMethodDto>
)

data class PaymentMethodDto(
    @SerializedName("nid")
    val nid: Int,
    @SerializedName("cname")
    val cname: String
) {
    fun toPaymentMethod(): PaymentMethod {
        return PaymentMethod(
            id = nid.toString(),
            name = cname
        )
    }
}

data class PaymentMethod(
    val id: String,
    val name: String
)
