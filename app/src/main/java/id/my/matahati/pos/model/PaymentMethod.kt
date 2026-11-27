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
    val cname: String,
    @SerializedName("nid_outlet")
    val nidOutlet: Int? = null
) {
    fun toPaymentMethod(): PaymentMethod {
        return PaymentMethod(
            id = nid.toString(),
            name = cname,
            nidOutlet = nidOutlet
        )
    }
}

data class PaymentMethod(
    val id: String,
    val name: String,
    val nidOutlet: Int? = null
)
