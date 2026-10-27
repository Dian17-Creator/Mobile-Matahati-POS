package id.my.matahati.pos.model

import com.google.gson.annotations.SerializedName

data class SettlePaymentRequest(
    @SerializedName("nid_payment") val paymentId: Int,
    @SerializedName("namount") val amount: Double,
    @SerializedName("creference_no") val referenceNo: String? = null,
    @SerializedName("cnote") val note: String? = "Pelunasan Reservasi"
)
