package id.my.matahati.pos.model

import com.google.gson.annotations.SerializedName
import kotlin.math.max

data class TaxSettingsResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: TaxSettingsData? = null
)

data class TaxSettingsData(
    @SerializedName("offline") val offline: TaxSettingItem? = null,
    @SerializedName("online") val online: TaxSettingItem? = null
)

data class TaxSettingItem(
    @SerializedName("nid_outlet") val nidOutlet: Int? = null,
    @SerializedName("ccategory") val category: String? = null,
    @SerializedName("ctax_name") val taxName: String? = null,
    @SerializedName("ntax_percent") val taxPercent: Double = 0.0,
    @SerializedName("bis_tax_included") val isTaxIncluded: Boolean = false,
    @SerializedName("bppn_11_12") val ppn1112: Boolean = false,
    @SerializedName("btakeaway_tax_exempt") val takeawayTaxExempt: Boolean = false,
    @SerializedName("ballow_manual_tax_exempt") val allowManualTaxExempt: Boolean = false,
    @SerializedName("btax_before_discount") val taxBeforeDiscount: Boolean = false,
    @SerializedName("nservice_charge_percent") val serviceChargePercent: Double = 0.0,
    @SerializedName("creceipt_title") val receiptTitle: String? = null,
    @SerializedName("creceipt_note") val receiptNote: String? = null,
    @SerializedName("bhide_order_no") val hideOrderNo: Boolean = false
)

data class TaxCalculationResult(
    val taxAmount: Double,
    val grandTotal: Double,
    val taxMode: String, // "NONE", "INCLUDE", "EXCLUDE"
    val taxPercent: Double,
    val dpp: Double,
    val taxName: String,
    val isTaxIncluded: Boolean,
    val isTaxExempt: Boolean
)

object TaxCalculator {
    fun calculateTax(
        transactionCategory: String, // "ONLINE" or "OFFLINE"
        orderType: String, // e.g. "TAKE_AWAY", "DINE_IN"
        subtotal: Double,
        discount: Double,
        offlineSetting: TaxSettingItem?,
        onlineSetting: TaxSettingItem?,
        isManualTaxExempt: Boolean
    ): TaxCalculationResult {
        val categoryUpper = transactionCategory.uppercase()
        val isOnline = categoryUpper == "ONLINE"
        val setting = if (isOnline) onlineSetting else offlineSetting
        val defaultTaxName = setting?.taxName?.takeIf { it.isNotBlank() } ?: "Pajak"

        if (isOnline || setting == null || setting.taxPercent <= 0.0) {
            val netSubtotal = max(0.0, subtotal - discount)
            return TaxCalculationResult(
                taxAmount = 0.0,
                grandTotal = netSubtotal,
                taxMode = "NONE",
                taxPercent = setting?.taxPercent ?: 0.0,
                dpp = 0.0,
                taxName = defaultTaxName,
                isTaxIncluded = false,
                isTaxExempt = isOnline
            )
        }

        val orderTypeUpper = orderType.uppercase()
        val isTakeaway = orderTypeUpper == "TAKE_AWAY" || orderTypeUpper == "TAKEAWAY" || orderTypeUpper == "BUNGKUS"
        val isTakeawayExempt = setting.takeawayTaxExempt && isTakeaway
        val isManualExempt = setting.allowManualTaxExempt && isManualTaxExempt

        if (isTakeawayExempt || isManualExempt) {
            val netSubtotal = max(0.0, subtotal - discount)
            return TaxCalculationResult(
                taxAmount = 0.0,
                grandTotal = netSubtotal,
                taxMode = "NONE",
                taxPercent = setting.taxPercent,
                dpp = 0.0,
                taxName = defaultTaxName,
                isTaxIncluded = false,
                isTaxExempt = true
            )
        }

        val rate = setting.taxPercent
        val taxBase = if (setting.taxBeforeDiscount) subtotal else max(0.0, subtotal - discount)
        val effectiveRate = if (setting.ppn1112) rate * 11.0 / 12.0 else rate

        return if (setting.isTaxIncluded) {
            val netSubtotal = max(0.0, subtotal - discount)
            val hargaDasar = taxBase / (1.0 + (effectiveRate / 100.0))
            val taxVal = taxBase - hargaDasar
            TaxCalculationResult(
                taxAmount = taxVal,
                grandTotal = netSubtotal,
                taxMode = "INCLUDE",
                taxPercent = rate,
                dpp = hargaDasar,
                taxName = defaultTaxName,
                isTaxIncluded = true,
                isTaxExempt = false
            )
        } else {
            val netSubtotal = max(0.0, subtotal - discount)
            val dppVal = if (setting.ppn1112) taxBase * 11.0 / 12.0 else taxBase
            val taxVal = dppVal * (rate / 100.0)
            val grandTotalVal = netSubtotal + taxVal
            TaxCalculationResult(
                taxAmount = taxVal,
                grandTotal = grandTotalVal,
                taxMode = "EXCLUDE",
                taxPercent = rate,
                dpp = dppVal,
                taxName = defaultTaxName,
                isTaxIncluded = false,
                isTaxExempt = false
            )
        }
    }
}
