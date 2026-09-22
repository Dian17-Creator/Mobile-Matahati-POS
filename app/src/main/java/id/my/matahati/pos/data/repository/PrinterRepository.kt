package id.my.matahati.pos.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import id.my.matahati.pos.model.LocalPrinter

class PrinterRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("printer_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val PRINTERS_KEY = "saved_printers"

    fun getPrinters(): List<LocalPrinter> {
        val json = prefs.getString(PRINTERS_KEY, null)
        return if (json != null) {
            val type = object : TypeToken<List<LocalPrinter>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }

    fun savePrinters(printers: List<LocalPrinter>) {
        val json = gson.toJson(printers)
        prefs.edit().putString(PRINTERS_KEY, json).apply()
    }

    fun addOrUpdatePrinter(printer: LocalPrinter) {
        val currentPrinters = getPrinters().toMutableList()
        // Ensure only one printer per role for now, so if there's a printer with the same role, replace it
        // Or if the same ID, update it.
        val existingRoleIndex = currentPrinters.indexOfFirst { it.role == printer.role }
        val existingIdIndex = currentPrinters.indexOfFirst { it.id == printer.id }

        if (existingIdIndex >= 0) {
            currentPrinters[existingIdIndex] = printer
        } else if (existingRoleIndex >= 0) {
            currentPrinters[existingRoleIndex] = printer
        } else {
            currentPrinters.add(printer)
        }
        savePrinters(currentPrinters)
    }

    fun deletePrinter(printerId: String) {
        val currentPrinters = getPrinters().toMutableList()
        currentPrinters.removeAll { it.id == printerId }
        savePrinters(currentPrinters)
    }
}