package id.my.matahati.pos.model

import java.util.UUID

enum class PrinterType {
    BLUETOOTH,
    TCP_IP
}

enum class PrinterRole {
    RECEIPT,
    KITCHEN,
    BAR
}

data class LocalPrinter(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: PrinterType,
    val address: String, // Bluetooth MAC Address or IP Address
    val port: Int? = null, // Only used for TCP_IP
    val role: PrinterRole
)