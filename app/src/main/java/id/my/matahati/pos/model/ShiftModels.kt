package id.my.matahati.pos.model

import com.google.gson.annotations.SerializedName

data class BaseResponse<T>(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: T? = null
)

data class OpenShiftRequest(
    @SerializedName("nid_outlet") val outletId: Int,
    @SerializedName("nopening_cash") val openingCash: Double
)

data class CloseShiftRequest(
    @SerializedName("nactual_cash") val actualCash: Double
)

data class CashMovementRequest(
    @SerializedName("amount") val amount: Double
)

data class ShiftResponse(
    @SerializedName("nid") val nid: Int,
    @SerializedName("cshift_no") val shiftNo: String,
    @SerializedName("nid_outlet") val outletId: Int,
    @SerializedName("nid_user") val userId: Int,
    
    // Waktu buka & tutup
    @SerializedName("dopened_at") val openedAt: String,
    @SerializedName("dclosed_at") val closedAt: String? = null,
    
    // Komponen Uang
    @SerializedName("nopening_cash") val openingCash: Double,
    
    // PENTING: nsales_cash adalah TOTAL GROSS SALES (Semua Metode Pembayaran termasuk QRIS/Debit)
    @SerializedName("nsales_cash") val totalSales: Double, 
    
    // PENTING: cash_sales adalah HANYA CASH (hanya ada saat shift OPEN / dari endpoint current)
    @SerializedName("cash_sales") val cashSales: Double? = null, 
    
    @SerializedName("nrefund_cash") val refundCash: Double = 0.0,
    @SerializedName("ncancellation_cash") val cancellationCash: Double = 0.0,
    @SerializedName("ncash_in") val cashIn: Double = 0.0,
    @SerializedName("ncash_out") val cashOut: Double = 0.0,
    
    // Kalkulasi akhir (Saat shift OPEN, backend mengirim expected_cash secara runtime)
    @SerializedName("nexpected_cash") val expectedCash: Double? = null,
    @SerializedName("nactual_cash") val actualCash: Double? = null,
    @SerializedName("ndifference") val difference: Double? = null,
    
    @SerializedName("cstatus") val status: String,

    @SerializedName("user") val user: UserInternalModel? = null,

    // Field Opsional Laporan Penutupan
    @SerializedName("total_pax", alternate = ["guest_count", "pax_count", "total_guests", "ntotal_guests", "nvisitor", "total_visitors", "npax", "pax"]) val totalPax: Int? = null,
    @SerializedName("total_receipts", alternate = ["receipt_count", "total_orders", "order_count", "ntotal_orders", "ntotal_transactions", "total_transactions", "transaction_count", "ntransaction", "ntransactions", "norder", "norders", "nreceipt", "nreceipts"]) val totalReceipts: Int? = null,
    @SerializedName("subtotal", alternate = ["nsubtotal", "gross_subtotal"]) val subtotal: Double? = null,
    @SerializedName("discount_amount", alternate = ["bill_discount", "ndiscount", "total_discount"]) val discountAmount: Double? = null,
    @SerializedName("cash_refund") val cashRefund: Double? = null,
    @SerializedName("cash_cancellation") val cashCancellation: Double? = null,
    @SerializedName("total_cancellation") val totalCancellation: Double? = null,
    @SerializedName("payment_breakdown") val paymentBreakdown: List<PaymentBreakdownItem>? = null
)

data class PaymentBreakdownItem(
    @SerializedName("name") val name: String = "",
    @SerializedName("sales") val sales: Double = 0.0,
    @SerializedName("refund") val refund: Double = 0.0,
    @SerializedName("void") val voidAmount: Double = 0.0,
    @SerializedName("net") val net: Double = 0.0
)

typealias Shift = ShiftResponse

fun ShiftResponse.getTotalCancellationAmount(): Double {
    return this.totalCancellation ?: this.cancellationCash
}

fun ShiftResponse.getCashCancellationAmount(): Double {
    if (this.cashCancellation != null) return this.cashCancellation
    val cashPm = this.paymentBreakdown?.find { it.name.trim().uppercase() in listOf("CASH", "TUNAI", "KAS") }
    if (cashPm != null) return cashPm.voidAmount
    val nonCashCancellationSum = this.paymentBreakdown
        ?.filter { pm -> pm.name.trim().uppercase() !in listOf("CASH", "TUNAI", "KAS") }
        ?.sumOf { it.voidAmount } ?: 0.0
    val totalCancel = this.totalCancellation ?: this.cancellationCash
    return (totalCancel - nonCashCancellationSum).coerceAtLeast(0.0)
}

fun ShiftResponse.getExpectedCashAmount(): Double {
    val cashSalesVal = this.cashSales ?: 0.0
    val refundCashVal = this.cashRefund ?: this.refundCash
    val cancellationCashVal = this.getCashCancellationAmount()
    val netCashMovement = this.cashIn - this.cashOut
    return this.openingCash + cashSalesVal + netCashMovement - refundCashVal - cancellationCashVal
}
