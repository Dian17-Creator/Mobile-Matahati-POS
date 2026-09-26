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
import id.my.matahati.pos.model.OpenShiftRequest
import id.my.matahati.pos.model.ShiftResponse
import kotlinx.coroutines.launch
import org.json.JSONObject

class ShiftViewModel : ViewModel() {
    var currentShift by mutableStateOf<ShiftResponse?>(null)
        private set

    val shiftHistory = mutableStateListOf<ShiftResponse>()

    var selectedShiftDetail by mutableStateOf<ShiftResponse?>(null)
        private set

    var isCurrentShiftLoading by mutableStateOf(false)
        private set

    var isHistoryLoading by mutableStateOf(false)
        private set

    var isSubmitting by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)

    var successMessage by mutableStateOf<String?>(null)

    fun checkCurrentShift(outletId: String?, onResult: ((hasActiveShift: Boolean) -> Unit)? = null) {
        val parsedOutlet = outletId?.toIntOrNull() ?: 1
        isCurrentShiftLoading = true
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getCurrentShift(parsedOutlet)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null && body.data.status.uppercase() == "OPEN") {
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

    fun openShift(outletId: String?, openingCashAmount: Double, onSuccess: (() -> Unit)? = null) {
        if (openingCashAmount < 0) {
            errorMessage = "Kas awal tidak boleh bernilai negatif."
            return
        }

        val parsedOutlet = outletId?.toIntOrNull() ?: 1
        isSubmitting = true
        errorMessage = null

        viewModelScope.launch {
            try {
                val request = OpenShiftRequest(
                    outletId = parsedOutlet,
                    openingCash = openingCashAmount
                )
                val response = RetrofitClient.apiService.openShift(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    currentShift = response.body()?.data
                    successMessage = "Shift berhasil dibuka."
                    onSuccess?.invoke()
                } else {
                    val errStr = response.errorBody()?.string()
                    errorMessage = parseError(errStr, response.code())
                }
            } catch (e: Exception) {
                errorMessage = "Gagal membuka shift: ${e.localizedMessage}"
            } finally {
                isSubmitting = false
            }
        }
    }

    fun startShift(outletId: String?, openingCashAmount: Double, onSuccess: (() -> Unit)? = null) {
        openShift(outletId, openingCashAmount, onSuccess)
    }

    fun cashIn(amount: Double, outletId: String? = null, onSuccess: (() -> Unit)? = null) {
        if (amount <= 0) {
            errorMessage = "Nominal harus lebih dari 0."
            return
        }

        isSubmitting = true
        errorMessage = null

        viewModelScope.launch {
            try {
                val request = CashMovementRequest(amount = amount)
                val response = RetrofitClient.apiService.cashIn(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    currentShift = response.body()?.data
                    successMessage = "Kas Masuk berhasil dicatat."
                    checkCurrentShift(outletId)
                    onSuccess?.invoke()
                } else {
                    val errStr = response.errorBody()?.string()
                    errorMessage = parseError(errStr, response.code())
                }
            } catch (e: Exception) {
                errorMessage = "Gagal mencatat Kas Masuk: ${e.localizedMessage}"
            } finally {
                isSubmitting = false
            }
        }
    }

    fun cashOut(amount: Double, outletId: String? = null, onSuccess: (() -> Unit)? = null) {
        if (amount <= 0) {
            errorMessage = "Nominal harus lebih dari 0."
            return
        }

        isSubmitting = true
        errorMessage = null

        viewModelScope.launch {
            try {
                val request = CashMovementRequest(amount = amount)
                val response = RetrofitClient.apiService.cashOut(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    currentShift = response.body()?.data
                    successMessage = "Kas Keluar berhasil dicatat."
                    checkCurrentShift(outletId)
                    onSuccess?.invoke()
                } else {
                    val errStr = response.errorBody()?.string()
                    errorMessage = parseError(errStr, response.code())
                }
            } catch (e: Exception) {
                errorMessage = "Gagal mencatat Kas Keluar: ${e.localizedMessage}"
            } finally {
                isSubmitting = false
            }
        }
    }

    fun closeShift(
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
                val response = RetrofitClient.apiService.closeShift(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    currentShift = null
                    successMessage = "Shift berhasil ditutup."
                    fetchShiftHistory(outletId)
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

    fun fetchShiftHistory(outletId: String?, date: String? = null) {
        val parsedOutlet = outletId?.toIntOrNull()
        isHistoryLoading = true
        shiftHistory.clear()

        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getShiftHistory(parsedOutlet, date)
                if (response.isSuccessful && response.body()?.success == true) {
                    val list = response.body()?.data
                    shiftHistory.clear()
                    if (!list.isNullOrEmpty()) {
                        shiftHistory.addAll(list)
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

    fun selectShiftDetail(shift: ShiftResponse) {
        selectedShiftDetail = shift
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
