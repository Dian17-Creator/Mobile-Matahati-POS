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
    val dstart: String,
    @SerializedName("dend")
    val dend: String,
    @SerializedName("nqty")
    val nqty: Int,
    @SerializedName("nredeem")
    val nredeem: Int,
    @SerializedName("nmin_spend")
    val nminSpend: Double?,
    @SerializedName("ndisc_percent")
    val ndiscPercent: Double?,
    @SerializedName("ndisc_amount")
    val ndiscAmount: Double?,
    @SerializedName("cstatus")
    val cstatus: String?
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

        return Voucher(
            id = nid.toString(),
            code = ckode,
            description = cdesc ?: "",
            startDate = dstart,
            endDate = dend,
            quota = nqty,
            redeemed = nredeem,
            minSpend = nminSpend ?: 0.0,
            discountPercent = ndiscPercent ?: 0.0,
            discountAmount = ndiscAmount ?: 0.0,
            discountDisplay = discountText,
            status = cstatus ?: "Active"
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
    val status: String
) {
    val remainingQuota: Int
        get() = (quota - redeemed).coerceAtLeast(0)

    val formattedMinSpend: String
        get() {
            if (minSpend <= 0) return "Tanpa Min. Belanja"
            val formatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"))
            formatter.maximumFractionDigits = 0
            return "Min. ${formatter.format(minSpend)}"
        }
}
