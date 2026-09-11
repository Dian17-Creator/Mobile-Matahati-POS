package id.my.matahati.pos.model

import com.google.gson.annotations.SerializedName

data class TransactionResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: TransactionData?
)

data class TransactionData(
    @SerializedName("transaction") val transaction: TransactionModel?,
    @SerializedName("details") val details: List<TransactionDetailModel>?
)

data class TransactionModel(
    @SerializedName("nid") val id: String,
    @SerializedName("cnotransaction") val transactionNo: String,
    @SerializedName("dtransaction") val transactionDate: String,
    @SerializedName("cname_customer") val customerName: String?,
    @SerializedName("cordertype") val orderType: String,
    @SerializedName("ctable") val tableName: String?,
    @SerializedName("nvisitor") val visitorCount: Int,
    @SerializedName("nsubtotal") val subtotal: String,
    @SerializedName("ndiscount") val discount: String,
    @SerializedName("ntax") val tax: String,
    @SerializedName("ngrandtotal") val grandTotal: String,
    @SerializedName("npaid") val paidAmount: String,
    @SerializedName("nchange") val changeAmount: String,
    @SerializedName("cstatus") val status: String,
    @SerializedName("nitem") val itemCount: Int
)

data class TransactionDetailModel(
    @SerializedName("nid") val id: String,
    @SerializedName("nid_product") val productId: String,
    @SerializedName("cname") val productName: String,
    @SerializedName("nqty") val quantity: Int,
    @SerializedName("nprice") val price: String,
    @SerializedName("nsubtotal") val subtotal: String,
    @SerializedName("cnote") val note: String?
)
