package id.my.matahati.pos.ui.screen.shift

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.my.matahati.pos.data.remote.RetrofitClient
import id.my.matahati.pos.model.CashMovementRequest
import id.my.matahati.pos.model.CloseShiftRequest
import id.my.matahati.pos.model.Shift
import id.my.matahati.pos.model.StartShiftRequest
import kotlinx.coroutines.launch
import org.json.JSONObject

class ShiftViewModel : ViewModel() {
    var currentShift by mutableStateOf<Shift?>(null)
        private set

    val shiftHistory = mutableStateListOf<Shift>()

    var selectedShiftDetail by mutableStateOf<Shift?>(null)
        private set

    var isCurrentShiftLoading by mutableStateOf(false)
        private set

    var isHistoryLoading by mutableStateOf(false)
        private set

    var isSubmitting by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)

    var successMessage by mutableStateOf<String?>(null)

    var currentPage by mutableStateOf(1)
        private set
    var lastPage by mutableStateOf(1)
        private set

    fun checkCurrentShift(outletId: String?, onResult: ((hasActiveShift: Boolean) -> Unit)? = null) {
        isCurrentShiftLoading = true
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getCurrentShift(outletId)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null && body.data.status?.uppercase() == "OPEN") {
                        currentShift = body.data
                        onResult?.invoke(true)
                    } else {
                        currentShift = null
                        onResult?.invoke(false)
                    }
                } else {
                    currentShift = null
                    onResult?.invoke(false)
                }
            } catch (e: Exception) {
                Log.e("ShiftViewModel", "Error checkCurrentShift: ${e.message}", e)
                currentShift = null
                onResult?.invoke(false)
            } finally {
                isCurrentShiftLoading = false
            }
        }
    }

    fun startShift(outletId: String?, openingCashAmount: Double, onSuccess: (() -> Unit)? = null) {
        if (openingCashAmount < 0) {
            errorMessage = "Kas awal tidak boleh bernilai negatif."
            return
        }

        val parsedOutlet = outletId?.toIntOrNull() ?: 1
        isSubmitting = true
        errorMessage = null

        viewModelScope.launch {
            try {
                val request = StartShiftRequest(
                    nidOutlet = parsedOutlet,
                    openingCash = openingCashAmount
                )
                val response = RetrofitClient.apiService.startShift(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    currentShift = response.body()?.data
                    successMessage = "Shift berhasil dimulai."
                    onSuccess?.invoke()
                } else {
                    val errStr = response.errorBody()?.string()
                    errorMessage = parseError(errStr, response.code())
                }
            } catch (e: Exception) {
                errorMessage = "Gagal memulai shift: ${e.localizedMessage}"
            } finally {
                isSubmitting = false
            }
        }
    }

    fun addCashMovement(
        shiftId: String,
        type: String, // "CASH_IN" or "CASH_OUT"
        amount: Double,
        description: String,
        outletId: String? = null,
        onSuccess: (() -> Unit)? = null
    ) {
        if (amount <= 0) {
            errorMessage = "Nominal harus lebih dari 0."
            return
        }
        if (description.isBlank()) {
            errorMessage = "Deskripsi/Keterangan wajib diisi."
            return
        }

        isSubmitting = true
        errorMessage = null

        viewModelScope.launch {
            try {
                val request = CashMovementRequest(
                    nidShift = shiftId,
                    type = type,
                    amount = amount,
                    description = description
                )
                val response = RetrofitClient.apiService.addCashMovement(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    successMessage = if (type == "CASH_IN") "Cash In berhasil dicatat." else "Cash Out berhasil dicatat."
                    checkCurrentShift(outletId) // refresh current shift stats
                    onSuccess?.invoke()
                } else {
                    val errStr = response.errorBody()?.string()
                    errorMessage = parseError(errStr, response.code())
                }
            } catch (e: Exception) {
                errorMessage = "Gagal mencatat perpindahan kas: ${e.localizedMessage}"
            } finally {
                isSubmitting = false
            }
        }
    }

    fun closeShift(
        shiftId: String,
        actualCashAmount: Double,
        outletId: String? = null,
        onSuccess: (() -> Unit)? = null
    ) {
        if (actualCashAmount < 0) {
            errorMessage = "Jumlah uang kas fisik tidak boleh bernilai negatif."
            return
        }

        isSubmitting = true
        errorMessage = null

        viewModelScope.launch {
            try {
                val request = CloseShiftRequest(actualCash = actualCashAmount)
                val response = RetrofitClient.apiService.closeShift(shiftId, request)
                if (response.isSuccessful && response.body()?.success == true) {
                    currentShift = null
                    successMessage = "Shift berhasil ditutup."
                    fetchShiftHistory(outletId, page = 1)
                    onSuccess?.invoke()
                } else {
                    val errStr = response.errorBody()?.string()
                    errorMessage = parseError(errStr, response.code())
                }
            } catch (e: Exception) {
                errorMessage = "Gagal menutup shift: ${e.localizedMessage}"
            } finally {
                isSubmitting = false
            }
        }
    }

    fun fetchShiftHistory(outletId: String?, page: Int = 1) {
        isHistoryLoading = true
        if (page == 1) shiftHistory.clear()

        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getShiftHistory(outletId, page)
                if (response.isSuccessful && response.body()?.success == true) {
                    val pagData = response.body()?.data
                    if (pagData != null) {
                        currentPage = pagData.currentPage
                        lastPage = pagData.lastPage
                        if (page == 1) shiftHistory.clear()
                        shiftHistory.addAll(pagData.data)
                    }
                } else {
                    Log.e("ShiftViewModel", "Gagal memuat riwayat shift: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("ShiftViewModel", "Error fetchShiftHistory: ${e.message}", e)
            } finally {
                isHistoryLoading = false
            }
        }
    }

    fun fetchShiftDetail(shiftId: String) {
        selectedShiftDetail = null
        isSubmitting = true
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getShiftDetail(shiftId)
                if (response.isSuccessful && response.body()?.success == true) {
                    selectedShiftDetail = response.body()?.data
                } else {
                    errorMessage = "Gagal mengambil detail shift."
                }
            } catch (e: Exception) {
                errorMessage = "Error detail shift: ${e.localizedMessage}"
            } finally {
                isSubmitting = false
            }
        }
    }

    fun clearShiftDetail() {
        selectedShiftDetail = null
    }

    fun clearError() {
        errorMessage = null
    }

    fun clearSuccess() {
        successMessage = null
    }

    private fun parseError(errorBody: String?, statusCode: Int): String {
        if (errorBody.isNullOrBlank()) return "Error ($statusCode)"
        return try {
            val json = JSONObject(errorBody)
            json.optString("message", "Error ($statusCode)")
        } catch (e: Exception) {
            "Error ($statusCode): $errorBody"
        }
    }
}
