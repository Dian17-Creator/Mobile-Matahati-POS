package id.my.matahati.pos.model

import com.google.gson.annotations.SerializedName

data class Shift(
    @SerializedName("nid") val id: String? = null,
    @SerializedName("cshift_no") val shiftNo: String? = null,
    @SerializedName("nid_outlet") val nidOutlet: String? = null,
    @SerializedName("nid_user") val nidUser: String? = null,
    @SerializedName("nopening_cash") val openingCash: String? = null,
    @SerializedName("cash_sales") val cashSales: String? = null,
    @SerializedName("cash_in") val cashIn: String? = null,
    @SerializedName("cash_out") val cashOut: String? = null,
    @SerializedName("cash_refund") val cashRefund: String? = null,
    @SerializedName("cash_canceled") val cashCanceled: String? = null,
    @SerializedName("expected_cash") val expectedCash: String? = null,
    @SerializedName("nactual_cash") val actualCash: String? = null,
    @SerializedName("ndifference") val difference: String? = null,
    @SerializedName("cstatus") val status: String? = null,
    @SerializedName("dopened_at") val openedAt: String? = null,
    @SerializedName("dclosed_at") val closedAt: String? = null,
    @SerializedName("user") val user: UserInternalModel? = null,
    @SerializedName("cash_movements") val cashMovements: List<CashMovement>? = null
)

data class CashMovement(
    @SerializedName("nid") val id: String? = null,
    @SerializedName("nid_shift") val nidShift: String? = null,
    @SerializedName("ctype") val type: String? = null, // "CASH_IN" or "CASH_OUT"
    @SerializedName("namount") val amount: String? = null,
    @SerializedName("cdescription") val description: String? = null,
    @SerializedName("dcreated_at") val createdAt: String? = null
)

data class CurrentShiftResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: Shift? = null
)

data class ShiftDetailResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: Shift? = null
)

data class ShiftHistoryResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: ShiftPaginationData? = null
)

data class ShiftPaginationData(
    @SerializedName("current_page") val currentPage: Int = 1,
    @SerializedName("last_page") val lastPage: Int = 1,
    @SerializedName("total") val total: Int = 0,
    @SerializedName("per_page") val perPage: Int = 15,
    @SerializedName("data") val data: List<Shift> = emptyList()
)

data class CashMovementResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: CashMovement? = null
)

data class StartShiftRequest(
    @SerializedName("nid_outlet") val nidOutlet: Int,
    @SerializedName("nopening_cash") val openingCash: Double
)

data class CashMovementRequest(
    @SerializedName("nid_shift") val nidShift: String,
    @SerializedName("ctype") val type: String, // "CASH_IN" or "CASH_OUT"
    @SerializedName("namount") val amount: Double,
    @SerializedName("cdescription") val description: String
)

data class CloseShiftRequest(
    @SerializedName("nactual_cash") val actualCash: Double
)
