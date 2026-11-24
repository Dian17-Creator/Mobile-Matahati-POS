package id.my.matahati.pos.model

import com.google.gson.annotations.SerializedName

data class TransactionResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: TransactionData? = null
)

data class TransactionHistoryResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: List<TransactionModel>? = null
)

data class TransactionData(
    @SerializedName("transaction") val transaction: TransactionModel? = null,
    @SerializedName("details") val details: List<TransactionDetailModel>? = null
)

data class TransactionModel(
    @SerializedName("nid") val id: String = "",
    @SerializedName("cnotransaction") val transactionNo: String = "",
    @SerializedName("dtransaction") val transactionDate: String = "",
    @SerializedName("dpayment_date") val paymentDate: String? = null,
    @SerializedName("nid_payment") val nidPayment: String? = null,
    @SerializedName("cname_customer") val customerName: String? = null,
    @SerializedName("cordertype") val orderType: String? = null,
    @SerializedName("ctable") val tableName: String? = null,
    @SerializedName("nvisitor") val visitorCount: Int = 1,
    @SerializedName("nsubtotal") val subtotal: String = "0",
    @SerializedName("ndiscount") val discount: String = "0",
    @SerializedName("ntax") val tax: String = "0",
    @SerializedName("ngrandtotal") val grandTotal: String = "0",
    @SerializedName("npaid") val paidAmount: String = "0",
    @SerializedName("nchange") val changeAmount: String = "0",
    @SerializedName("cstatus") val status: String = "",
    @SerializedName("nitem") val itemCount: Int = 0,
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
    @SerializedName("ctransaction_category") val transactionCategory: String? = null,
    @SerializedName("ctax_mode") val taxMode: String? = null,
    @SerializedName("ntax_percent") val taxPercent: Double? = null,
    @SerializedName("ndpp") val dpp: Double? = null,
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
    @SerializedName("nid") val id: String = "",
    @SerializedName("user") val user: UserInternalModel? = null
)

data class UserInternalModel(
    @SerializedName("nid") val id: String = "",
    @SerializedName("cname") val name: String = ""
)

data class TransactionDetailModel(
    @SerializedName("nid") val id: String = "",
    @SerializedName("nid_product") val productId: String = "",
    @SerializedName("cname") val productName: String = "",
    @SerializedName("nqty") val quantity: Int = 0,
    @SerializedName("nqty_void") val qtyVoid: Int = 0,
    @SerializedName("nqty_refund") val qtyRefund: Int = 0,
    @SerializedName("nprice") val price: String = "0",
    @SerializedName("nsubtotal") val subtotal: String = "0",
    @SerializedName("cnote") val note: String? = null
) {
    val qtyAvailable: Int
        get() = (quantity - qtyVoid - qtyRefund).coerceAtLeast(0)
}
