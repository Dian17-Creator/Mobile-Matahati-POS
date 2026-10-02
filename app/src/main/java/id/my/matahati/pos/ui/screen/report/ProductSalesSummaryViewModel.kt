package id.my.matahati.pos.ui.screen.report

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.my.matahati.pos.data.printer.BluetoothPrinterManager
import id.my.matahati.pos.data.printer.EscPosFormatter
import id.my.matahati.pos.data.remote.RetrofitClient
import id.my.matahati.pos.model.ProductSalesSummaryHeader
import id.my.matahati.pos.model.ProductSalesSummaryItem
import kotlinx.coroutines.launch

class ProductSalesSummaryViewModel : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var summaryHeader by mutableStateOf<ProductSalesSummaryHeader?>(null)
        private set

    var itemsList by mutableStateOf<List<ProductSalesSummaryItem>>(emptyList())
        private set

    var showBottomSheet by mutableStateOf(false)
    var selectedDayDisplay by mutableStateOf("")
    var selectedDayApi by mutableStateOf("")

    var showReceiptPreview by mutableStateOf(false)
    var isPrinting by mutableStateOf(false)
    var printMessage by mutableStateOf<String?>(null)

    fun openDaySummary(displayLabel: String, apiDate: String, nidOutlet: String? = null) {
        selectedDayDisplay = displayLabel
        selectedDayApi = apiDate
        showBottomSheet = true
        fetchSummary(apiDate = apiDate, nidOutlet = nidOutlet)
    }

    fun closeBottomSheet() {
        showBottomSheet = false
    }

    fun openReceiptPreview() {
        showReceiptPreview = true
    }

    fun closeReceiptPreview() {
        showReceiptPreview = false
    }

    fun fetchSummary(apiDate: String, nidOutlet: String? = null) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val outletIdInt = nidOutlet?.toIntOrNull()
                val response = RetrofitClient.apiService.getProductSalesSummary(
                    dateFrom = apiDate,
                    outletId = outletIdInt
                )

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.success && body.data != null) {
                        summaryHeader = body.data.summary
                        itemsList = body.data.items
                    } else {
                        errorMessage = body.message ?: "Gagal mengambil ringkasan penjualan produk"
                    }
                } else {
                    val errText = response.errorBody()?.string()
                    errorMessage = "Gagal mengambil data (${response.code()}): $errText"
                }
            } catch (e: Exception) {
                errorMessage = "Terjadi kesalahan jaringan: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    fun printToPhysicalPrinter(context: Context, savedOutletName: String? = null) {
        viewModelScope.launch {
            isPrinting = true
            printMessage = null
            try {
                val prefs = context.getSharedPreferences("pos_prefs", Context.MODE_PRIVATE)
                val address = prefs.getString("selected_printer_address", null)

                if (address.isNullOrBlank()) {
                    printMessage = "Printer belum terhubung. Silakan atur printer terlebih dahulu di menu Pengaturan."
                    isPrinting = false
                    return@launch
                }

                val printerManager = BluetoothPrinterManager(context)
                val formatter = EscPosFormatter()
                val printBytes = formatter.formatProductSalesSummaryReceipt(
                    dateDisplay = selectedDayDisplay,
                    items = itemsList,
                    header = summaryHeader,
                    savedOutletName = savedOutletName
                )

                val result = printerManager.printData(address, printBytes)
                if (result.isSuccess) {
                    printMessage = "Berhasil mencetak ke printer fisik"
                } else {
                    printMessage = "Gagal mencetak: ${result.exceptionOrNull()?.localizedMessage}"
                }
            } catch (e: Exception) {
                printMessage = "Terjadi kesalahan saat mencetak: ${e.localizedMessage}"
            } finally {
                isPrinting = false
            }
        }
    }

    fun clearPrintMessage() {
        printMessage = null
    }
}
