package id.my.matahati.pos.ui.screen.settings

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothDevice
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import id.my.matahati.pos.data.printer.EscPosFormatter
import id.my.matahati.pos.data.printer.PrinterConnectionManager
import id.my.matahati.pos.data.repository.PrinterRepository
import id.my.matahati.pos.model.LocalPrinter
import id.my.matahati.pos.model.PrinterRole
import id.my.matahati.pos.model.PrinterType
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = PrinterRepository(application)
    private val connectionManager = PrinterConnectionManager(application)
    private val formatter = EscPosFormatter()
    private val prefs = application.getSharedPreferences("pos_prefs", android.content.Context.MODE_PRIVATE)

    var printers = mutableStateListOf<LocalPrinter>()

    var showAddDialog by mutableStateOf(false)
    var editingPrinter by mutableStateOf<LocalPrinter?>(null)
    var isTestingConnection by mutableStateOf(false)
    var testResultMessage by mutableStateOf<String?>(null)

    var isConfirmOrderEnabled by mutableStateOf(true)
        private set

    init {
        loadPrinters()
        loadPaymentSettings()
    }

    fun loadPaymentSettings() {
        isConfirmOrderEnabled = prefs.getBoolean("confirm_order_before_payment", true)
    }

    fun updateConfirmOrderEnabled(enabled: Boolean) {
        isConfirmOrderEnabled = enabled
        prefs.edit().putBoolean("confirm_order_before_payment", enabled).apply()
    }

    fun loadPrinters() {
        printers.clear()
        printers.addAll(repository.getPrinters())
    }

    fun addOrUpdatePrinter(printer: LocalPrinter) {
        repository.addOrUpdatePrinter(printer)
        loadPrinters()
        showAddDialog = false
        editingPrinter = null
    }

    fun deletePrinter(printerId: String) {
        repository.deletePrinter(printerId)
        loadPrinters()
    }

    fun getPairedBluetoothDevices(): List<BluetoothDevice> {
        return connectionManager.getPairedBluetoothDevices()
    }

    fun testConnection(printer: LocalPrinter) {
        isTestingConnection = true
        testResultMessage = null
        viewModelScope.launch {
            val result = connectionManager.testConnection(printer)
            isTestingConnection = false
            testResultMessage = result.getOrNull() ?: result.exceptionOrNull()?.message ?: "Gagal tes koneksi."
        }
    }

    fun testPrint(printer: LocalPrinter) {
        isTestingConnection = true
        testResultMessage = null
        viewModelScope.launch {
            val data = formatter.formatTestPrint()
            val result = connectionManager.printData(printer, data)
            isTestingConnection = false
            if (result.isSuccess) {
                testResultMessage = "Test print berhasil dikirim ke printer."
            } else {
                testResultMessage = result.exceptionOrNull()?.message ?: "Gagal test print."
            }
        }
    }

    fun clearTestMessage() {
        testResultMessage = null
    }
}