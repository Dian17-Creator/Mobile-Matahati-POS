package id.my.matahati.pos.model

import com.google.gson.annotations.SerializedName

data class TransactionResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: TransactionData?
)

data class TransactionHistoryResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: List<TransactionModel>?
)

data class TransactionData(
    @SerializedName("transaction") val transaction: TransactionModel?,
    @SerializedName("details") val details: List<TransactionDetailModel>?
)

data class TransactionModel(
    @SerializedName("nid") val id: String,
    @SerializedName("cnotransaction") val transactionNo: String,
    @SerializedName("dtransaction") val transactionDate: String,
    @SerializedName("dpayment_date") val paymentDate: String? = null,
    @SerializedName("nid_payment") val nidPayment: String? = null,
    @SerializedName("cname_customer") val customerName: String?,
    @SerializedName("cordertype") val orderType: String?,
    @SerializedName("ctable") val tableName: String?,
    @SerializedName("nvisitor") val visitorCount: Int,
    @SerializedName("nsubtotal") val subtotal: String,
    @SerializedName("ndiscount") val discount: String,
    @SerializedName("ntax") val tax: String,
    @SerializedName("ngrandtotal") val grandTotal: String,
    @SerializedName("npaid") val paidAmount: String,
    @SerializedName("nchange") val changeAmount: String,
    @SerializedName("cstatus") val status: String,
    @SerializedName("nitem") val itemCount: Int,
    @SerializedName("ccancel_note") val cancelNote: String? = null,
    @SerializedName("cvoid_note") val voidNote: String? = null,
    @SerializedName("crefund_note") val refundNote: String? = null,
    @SerializedName("cnote") val orderNote: String? = null,
    @SerializedName("details") val details: List<TransactionDetailModel>? = null,
    @SerializedName("pos_user") val posUser: PosUserModel? = null,
    @SerializedName("payment") val payment: PaymentMethodDto? = null,
    @SerializedName("nid_payment_dual") val nidPaymentDual: String? = null,
    @SerializedName("ndp_amount") val dpAmount: Double = 0.0,
    @SerializedName("nremaining_amount") val remainingAmount: Double = 0.0,
    @SerializedName("payment_dual") val paymentDual: PaymentMethodDto? = null,
    @SerializedName("sales_payments") val salesPayments: List<SalesPaymentItem>? = emptyList(),
    @SerializedName("outlet") val outlet: OutletModel? = null,
    @SerializedName("cpdf_url") val pdfUrl: String? = null,
    @SerializedName("nid_parent_transaction") val nidParentTransaction: String? = null,
    @SerializedName("reversals") val reversals: List<TransactionModel>? = null,
    @SerializedName("parent_transaction") val parentTransaction: TransactionModel? = null
)

data class SalesPaymentItem(
    @SerializedName("nid") val id: Long? = null,
    @SerializedName("nid_payment") val paymentId: Int? = null,
    @SerializedName("namount") val amount: Double = 0.0,
    @SerializedName("nchange") val change: Double = 0.0,
    @SerializedName("cpayment_type") val paymentType: String? = null, // "FULL", "DP", "PELUNASAN", "SPLIT"
    @SerializedName("creference_no") val referenceNo: String? = null,
    @SerializedName("cnote") val note: String? = null,
    @SerializedName("dpaid_at") val paidAt: String? = null,
    @SerializedName("payment") val paymentMethod: PaymentMethodDto? = null
)

data class OutletModel(
    @SerializedName("nid") val id: Int? = null,
    @SerializedName("cname") val name: String? = null
)

data class PosUserModel(
    @SerializedName("nid") val id: String,
    @SerializedName("user") val user: UserInternalModel?
)

data class UserInternalModel(
    @SerializedName("nid") val id: String,
    @SerializedName("cname") val name: String
)

data class TransactionDetailModel(
    @SerializedName("nid") val id: String,
    @SerializedName("nid_product") val productId: String,
    @SerializedName("cname") val productName: String,
    @SerializedName("nqty") val quantity: Int,
    @SerializedName("nqty_void") val qtyVoid: Int = 0,
    @SerializedName("nqty_refund") val qtyRefund: Int = 0,
    @SerializedName("nprice") val price: String,
    @SerializedName("nsubtotal") val subtotal: String,
    @SerializedName("cnote") val note: String? = null
) {
    val qtyAvailable: Int
        get() = (quantity - qtyVoid - qtyRefund).coerceAtLeast(0)
}
