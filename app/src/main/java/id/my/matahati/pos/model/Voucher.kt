package id.my.matahati.pos.model

import com.google.gson.annotations.SerializedName
import java.text.NumberFormat
import java.util.Locale

data class VoucherResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: List<VoucherDto>
)

data class VoucherDto(
    @SerializedName("nid")
    val nid: Int,
    @SerializedName("ckode")
    val ckode: String,
    @SerializedName("cdesc")
    val cdesc: String?,
    @SerializedName("dstart")
    val dstart: String?,
    @SerializedName("dend")
    val dend: String?,
    @SerializedName("nqty")
    val nqty: Int?,
    @SerializedName("nredeem")
    val nredeem: Int?,
    @SerializedName("nmin_spend")
    val nminSpend: Double?,
    @SerializedName("ndisc_percent")
    val ndiscPercent: Double?,
    @SerializedName("ndisc_amount")
    val ndiscAmount: Double?,
    @SerializedName("cstatus")
    val cstatus: String?,
    @SerializedName("is_unlimited_date")
    val isUnlimitedDate: Boolean? = null,
    @SerializedName("is_unlimited_qty")
    val isUnlimitedQty: Boolean? = null,
    @SerializedName("nid_outlet")
    val nidOutlet: Int? = null
) {
    fun toVoucher(): Voucher {
        val discountText = when {
            (ndiscPercent ?: 0.0) > 0 -> {
                val pct = ndiscPercent ?: 0.0
                if (pct == pct.toLong().toDouble()) "${pct.toInt()}%" else "$pct%"
            }
            (ndiscAmount ?: 0.0) > 0 -> {
                val formatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"))
                formatter.maximumFractionDigits = 0
                formatter.format(ndiscAmount ?: 0.0)
            }
            else -> "Diskon"
        }

        val isUnlDate = isUnlimitedDate == true || dend.isNullOrBlank() || dend == "2099-12-31"
        val isUnlQty = isUnlimitedQty == true || nqty == null || nqty == 999999

        return Voucher(
            id = nid.toString(),
            code = ckode,
            description = cdesc ?: "",
            startDate = dstart ?: "",
            endDate = dend ?: "",
            quota = nqty ?: 0,
            redeemed = nredeem ?: 0,
            minSpend = nminSpend ?: 0.0,
            discountPercent = ndiscPercent ?: 0.0,
            discountAmount = ndiscAmount ?: 0.0,
            discountDisplay = discountText,
            status = cstatus ?: "Active",
            isUnlimitedDate = isUnlDate,
            isUnlimitedQty = isUnlQty,
            nidOutlet = nidOutlet
        )
    }
}

data class Voucher(
    val id: String,
    val code: String,
    val description: String,
    val startDate: String,
    val endDate: String,
    val quota: Int,
    val redeemed: Int,
    val minSpend: Double,
    val discountPercent: Double,
    val discountAmount: Double,
    val discountDisplay: String,
    val status: String,
    val isUnlimitedDate: Boolean = false,
    val isUnlimitedQty: Boolean = false,
    val nidOutlet: Int? = null
) {
    val remainingQuota: Int
        get() = if (isUnlimitedQty) Int.MAX_VALUE else (quota - redeemed).coerceAtLeast(0)

    val formattedMinSpend: String
        get() {
            if (minSpend <= 0) return "Tanpa Min. Belanja"
            val formatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"))
            formatter.maximumFractionDigits = 0
            return "Min. ${formatter.format(minSpend)}"
        }
}
