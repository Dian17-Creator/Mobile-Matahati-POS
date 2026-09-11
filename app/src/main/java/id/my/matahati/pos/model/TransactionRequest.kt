package id.my.matahati.pos.model

import com.google.gson.annotations.SerializedName

data class TransactionRequest(
    @SerializedName("nid_customer") val nidCustomer: String?,
    @SerializedName("nid_outlet") val nidOutlet: Int,
    @SerializedName("nid_payment") val nidPayment: String,
    @SerializedName("nid_voucher") val nidVoucher: String?,
    @SerializedName("cname_customer") val customerName: String?,
    @SerializedName("cordertype") val orderType: String,
    @SerializedName("nvisitor") val visitorCount: Int = 1, // Default 1
    @SerializedName("ctable") val tableName: String?,
    @SerializedName("ndiscount") val discount: Double,
    @SerializedName("ntax") val tax: Double,
    @SerializedName("npaid") val paidAmount: Double,
    @SerializedName("ccancel_note") val cancelNote: String? = null,
    @SerializedName("cstatus") val status: String? = null,
    @SerializedName("details") val details: List<TransactionDetailRequest>
)

data class TransactionDetailRequest(
    @SerializedName("nid_product") val productId: String,
    @SerializedName("nqty") val quantity: Int,
    @SerializedName("cnote") val note: String?
)
