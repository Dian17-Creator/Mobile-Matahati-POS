package id.my.matahati.pos.ui.screen.report

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.my.matahati.pos.data.remote.RetrofitClient
import id.my.matahati.pos.model.DashboardData
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReportViewModel : ViewModel() {

    var isLoading by mutableStateOf(value = false)
        private set

    var errorMessage by mutableStateOf<String?>(value = null)
        private set

    var dashboardData by mutableStateOf<DashboardData?>(value = null)
        private set

    private val apiSdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val displaySdf = SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("id-ID"))

    var selectedStartDate by mutableStateOf(apiSdf.format(Date()))
        private set

    var selectedEndDate by mutableStateOf(apiSdf.format(Date()))
        private set

    var selectedDateLabel by mutableStateOf(displaySdf.format(Date()))
        private set

    fun updateDateRange(label: String, startDate: String, endDate: String, nidOutlet: String?) {
        selectedDateLabel = label
        selectedStartDate = startDate
        selectedEndDate = endDate
        fetchDashboard(nidOutlet = nidOutlet)
    }

    fun fetchDashboard(nidOutlet: String? = null) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val outletIdInt = nidOutlet?.toIntOrNull()
                val response = RetrofitClient.apiService.getDashboard(
                    dateFrom = selectedStartDate,
                    dateTo = selectedEndDate,
                    outletId = outletIdInt
                )

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.success) {
                        dashboardData = body.data
                    } else {
                        errorMessage = body.message ?: "Gagal mengambil data dashboard"
                    }
                } else {
                    val errText = response.errorBody()?.string()
                    errorMessage = "Gagal mengambil data dashboard (${response.code()}): $errText"
                }
            } catch (e: Exception) {
                errorMessage = "Terjadi kesalahan jaringan: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    fun clearError() {
        errorMessage = null
    }

    companion object {
        fun formatRupiah(amount: Double): String {
            val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"))
            formatter.maximumFractionDigits = 0
            return "Rp ${formatter.format(amount)}"
        }

        fun formatDateDisplay(dateStr: String): String {
            if (dateStr.isBlank()) return "-"
            return try {
                val inputSdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val date = inputSdf.parse(dateStr)
                if (date != null) {
                    val outputSdf = SimpleDateFormat("dd MMM", Locale.forLanguageTag("id-ID"))
                    outputSdf.format(date)
                } else {
                    dateStr
                }
            } catch (_: Exception) {
                dateStr
            }
        }
    }
}
