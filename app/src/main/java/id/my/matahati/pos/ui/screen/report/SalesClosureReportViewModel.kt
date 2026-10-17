package id.my.matahati.pos.ui.screen.report

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.my.matahati.pos.data.printer.EscPosFormatter
import id.my.matahati.pos.data.printer.PrinterConnectionManager
import id.my.matahati.pos.data.remote.RetrofitClient
import id.my.matahati.pos.data.repository.PrinterRepository
import id.my.matahati.pos.model.LocalPrinter
import id.my.matahati.pos.model.PrinterRole
import id.my.matahati.pos.model.PrinterType
import id.my.matahati.pos.model.ShiftResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

data class DailySalesClosureData(
    val dateDisplay: String = "",
    val dateApi: String = "",
    val cashierNames: String = "-",
    val earliestOpenedAt: String? = null,
    val latestClosedAt: String? = null,
    val openingCash: Double = 0.0,
    val totalSales: Double = 0.0,
    val subtotal: Double = 0.0,
    val discountAmount: Double = 0.0,
    val cashSales: Double = 0.0,
    val nonCashSales: Double = 0.0,
    val refundCash: Double = 0.0,
    val cancellationCash: Double = 0.0,
    val netSales: Double = 0.0,
    val netCashMovement: Double = 0.0,
    val expectedCash: Double = 0.0,
    val actualCash: Double = 0.0,
    val difference: Double = 0.0,
    val totalPaxs: Int = 0,
    val totalReceipts: Int = 0,
    val shiftsCount: Int = 0,
    val paymentBreakdownList: List<id.my.matahati.pos.model.PaymentBreakdownItem> = emptyList()
)

class SalesClosureReportViewModel : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var dailyClosureData by mutableStateOf<DailySalesClosureData?>(null)
        private set

    var isPrinting by mutableStateOf(false)
    var printMessage by mutableStateOf<String?>(null)

    fun fetchDailySummary(apiDate: String, nidOutlet: String? = null, userName: String? = null) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val outletIdInt = nidOutlet?.toIntOrNull()
                val response = RetrofitClient.apiService.getShiftHistory(
                    outletId = outletIdInt,
                    date = apiDate,
                    perPage = 50
                )

                if (response.isSuccessful && response.body() != null) {
                    val shifts = response.body()?.data ?: emptyList()
                    dailyClosureData = aggregateShiftsToDailyData(apiDate, shifts, userName)
                } else {
                    errorMessage = response.body()?.message ?: "Gagal mengambil data penutupan penjualan harian"
                    dailyClosureData = DailySalesClosureData(dateApi = apiDate)
                }
            } catch (e: Exception) {
                errorMessage = "Terjadi kesalahan jaringan: ${e.localizedMessage}"
                dailyClosureData = DailySalesClosureData(dateApi = apiDate)
            } finally {
                isLoading = false
            }
        }
    }

    private fun aggregateShiftsToDailyData(apiDate: String, shifts: List<ShiftResponse>, userName: String? = null): DailySalesClosureData {
        if (shifts.isEmpty()) {
            return DailySalesClosureData(dateApi = apiDate)
        }

        val cashierSet = shifts.mapNotNull { it.user?.name }.filter { it.isNotBlank() }.toSet()
        val cashierNamesStr = if (cashierSet.isNotEmpty()) cashierSet.joinToString(", ") else (userName ?: "Kasir")

        val sortedByOpened = shifts.sortedBy { it.openedAt }
        val earliestOpen = sortedByOpened.firstOrNull()?.openedAt
        val hasActiveShift = shifts.any { it.status.uppercase() == "OPEN" }
        val latestClose = if (hasActiveShift) "Masih Berjalan" else sortedByOpened.lastOrNull { !it.closedAt.isNullOrBlank() }?.closedAt

        val totalOpeningCash = shifts.sumOf { it.openingCash }
        val totalSalesSum = shifts.sumOf { it.totalSales }
        val subtotalSum = shifts.sumOf { it.subtotal ?: (it.totalSales + (it.discountAmount ?: 0.0)) }
        val discountAmountSum = shifts.sumOf { it.discountAmount ?: 0.0 }
        val totalCashSalesSum = shifts.sumOf { it.cashSales ?: 0.0 }
        val totalNonCashSalesSum = maxOf(0.0, totalSalesSum - totalCashSalesSum)
        val totalRefundSum = shifts.sumOf { it.refundCash }
        val totalCancellationSum = shifts.sumOf { it.cancellationCash }
        val netSalesSum = maxOf(0.0, totalSalesSum - totalCancellationSum)
        val totalCashIn = shifts.sumOf { it.cashIn }
        val totalCashOut = shifts.sumOf { it.cashOut }
        val netCashMovementSum = totalCashIn - totalCashOut

        val totalExpectedCash = shifts.sumOf { 
            it.expectedCash ?: (it.openingCash + (it.cashSales ?: 0.0) + (it.cashIn - it.cashOut) - it.refundCash - it.cancellationCash)
        }
        val totalActualCash = shifts.sumOf { it.actualCash ?: 0.0 }
        val totalDifference = shifts.sumOf { it.difference ?: (it.actualCash?.minus(totalExpectedCash) ?: 0.0) }

        val totalPaxsSum = shifts.sumOf { it.totalPax ?: 0 }
        val totalReceiptsSum = shifts.sumOf { it.totalReceipts ?: 0 }

        // Aggregate non-cash payment method breakdowns across all daily shifts
        val breakdownMap = mutableMapOf<String, MutableList<id.my.matahati.pos.model.PaymentBreakdownItem>>()
        shifts.forEach { shift ->
            shift.paymentBreakdown?.forEach { item ->
                val nameUpper = item.name.trim().uppercase()
                if (nameUpper != "CASH" && nameUpper != "TUNAI" && nameUpper != "KAS") {
                    val key = item.name.trim()
                    if (!breakdownMap.containsKey(key)) {
                        breakdownMap[key] = mutableListOf()
                    }
                    breakdownMap[key]?.add(item)
                }
            }
        }

        val aggregatedBreakdownList = breakdownMap.map { (name, items) ->
            val sales = items.sumOf { it.sales }
            val refund = items.sumOf { it.refund }
            val voidAmount = items.sumOf { it.voidAmount }
            val net = maxOf(0.0, sales - refund - voidAmount)
            id.my.matahati.pos.model.PaymentBreakdownItem(
                name = name,
                sales = sales,
                refund = refund,
                voidAmount = voidAmount,
                net = net
            )
        }

        return DailySalesClosureData(
            dateApi = apiDate,
            cashierNames = cashierNamesStr,
            earliestOpenedAt = earliestOpen,
            latestClosedAt = latestClose,
            openingCash = totalOpeningCash,
            totalSales = totalSalesSum,
            subtotal = subtotalSum,
            discountAmount = discountAmountSum,
            cashSales = totalCashSalesSum,
            nonCashSales = totalNonCashSalesSum,
            refundCash = totalRefundSum,
            cancellationCash = totalCancellationSum,
            netSales = netSalesSum,
            netCashMovement = netCashMovementSum,
            expectedCash = totalExpectedCash,
            actualCash = totalActualCash,
            difference = totalDifference,
            totalPaxs = totalPaxsSum,
            totalReceipts = totalReceiptsSum,
            shiftsCount = shifts.size,
            paymentBreakdownList = aggregatedBreakdownList
        )
    }

    fun isShowPrintPreviewEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences("pos_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("show_print_preview", true)
    }

    fun toShiftResponse(data: DailySalesClosureData, userName: String? = null): ShiftResponse {
        return ShiftResponse(
            nid = 0,
            shiftNo = "DAILY",
            outletId = 0,
            userId = 0,
            openedAt = if (data.earliestOpenedAt.isNullOrBlank()) data.dateApi else data.earliestOpenedAt!!,
            closedAt = data.latestClosedAt,
            openingCash = data.openingCash,
            totalSales = data.totalSales,
            cashSales = data.cashSales,
            refundCash = data.refundCash,
            cancellationCash = data.cancellationCash,
            cashIn = if (data.netCashMovement > 0) data.netCashMovement else 0.0,
            cashOut = if (data.netCashMovement < 0) -data.netCashMovement else 0.0,
            expectedCash = data.expectedCash,
            actualCash = data.actualCash,
            difference = data.difference,
            status = "CLOSED",
            totalPax = data.totalPaxs,
            totalReceipts = data.totalReceipts,
            subtotal = data.subtotal,
            discountAmount = data.discountAmount,
            paymentBreakdown = data.paymentBreakdownList
        )
    }

    fun printDailyClosure(
        context: Context,
        data: DailySalesClosureData?,
        savedOutletName: String? = null,
        userName: String? = null
    ) {
        if (data == null) {
            printMessage = "Tidak ada data penutupan penjualan harian untuk dicetak"
            return
        }

        isPrinting = true
        printMessage = null

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val repository = PrinterRepository(context)
                val printers = repository.getPrinters()
                val receiptPrinter = printers.find { it.role == PrinterRole.RECEIPT }

                val connectionManager = PrinterConnectionManager(context)
                val formatter = EscPosFormatter(cols = 32)
                val printBytes = formatter.formatDailySalesClosureReceipt(
                    data = data,
                    savedOutletName = savedOutletName,
                    userName = userName
                )

                val result = if (receiptPrinter != null) {
                    connectionManager.printData(receiptPrinter, printBytes)
                } else {
                    val prefs = context.getSharedPreferences("printer_prefs", Context.MODE_PRIVATE)
                    val address = prefs.getString("selected_printer_address", null)
                    if (!address.isNullOrBlank()) {
                        val fallbackPrinter = LocalPrinter(
                            id = "fallback",
                            name = "Saved Printer",
                            type = PrinterType.BLUETOOTH,
                            address = address,
                            role = PrinterRole.RECEIPT
                        )
                        connectionManager.printData(fallbackPrinter, printBytes)
                    } else {
                        Result.failure(Exception("Printer Kasir belum dikonfigurasi di Pengaturan."))
                    }
                }

                if (result.isSuccess) {
                    printMessage = "Struk penutupan penjualan harian berhasil dicetak."
                } else {
                    printMessage = "Gagal mencetak: ${result.exceptionOrNull()?.message ?: "Periksa printer"}"
                }
            } catch (e: Exception) {
                printMessage = "Error cetak: ${e.localizedMessage}"
            } finally {
                isPrinting = false
            }
        }
    }

    fun clearPrintMessage() {
        printMessage = null
    }
}
