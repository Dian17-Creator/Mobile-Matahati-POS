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

    @SerializedName("user") val user: UserInternalModel? = null
)

typealias Shift = ShiftResponse
