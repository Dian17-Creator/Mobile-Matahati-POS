package id.my.matahati.pos.data.printer

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.util.Log
import id.my.matahati.pos.model.LocalPrinter
import id.my.matahati.pos.model.PrinterType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.util.UUID

class PrinterConnectionManager(private val context: Context) {
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    @SuppressLint("MissingPermission")
    suspend fun testConnection(printer: LocalPrinter): Result<String> = withContext(Dispatchers.IO) {
        if (printer.type == PrinterType.TCP_IP) {
            try {
                val ip = printer.address
                val port = printer.port ?: 9100
                val socket = Socket()
                // Timeout 3 detik untuk test connection TCP
                socket.connect(InetSocketAddress(ip, port), 3000)
                socket.close()
                Result.success("Printer ${printer.name} (TCP/IP) berhasil terhubung.")
            } catch (e: Exception) {
                Log.e("PrinterConnection", "TCP/IP connection failed: ${e.message}", e)
                Result.failure(Exception("Tidak dapat terhubung ke printer. Periksa jaringan, IP address, dan port."))
            }
        } else {
            // Bluetooth
            var socket: BluetoothSocket? = null
            try {
                if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
                    return@withContext Result.failure(Exception("Bluetooth tidak aktif atau tidak didukung di perangkat ini."))
                }

                val device = bluetoothAdapter.getRemoteDevice(printer.address)
                socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
                // Timeout untuk bluetooth sering kali dikendalikan oleh OS, tapi kita coba connect
                socket.connect()
                Result.success("Printer ${printer.name} (Bluetooth) berhasil terhubung.")
            } catch (e: IOException) {
                Log.e("PrinterConnection", "Bluetooth connection failed: ${e.message}", e)
                Result.failure(Exception("Koneksi Bluetooth gagal. Pastikan printer nyala dan sudah di-pair."))
            } catch (e: IllegalArgumentException) {
                Result.failure(Exception("Alamat Bluetooth tidak valid."))
            } finally {
                try {
                    socket?.close()
                } catch (e: Exception) {
                    // ignore
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun printData(printer: LocalPrinter, data: ByteArray): Result<Unit> = withContext(Dispatchers.IO) {
        val copies = printer.copies.coerceAtLeast(1)
        if (printer.type == PrinterType.TCP_IP) {
            var socket: Socket? = null
            try {
                val ip = printer.address
                val port = printer.port ?: 9100
                socket = Socket()
                socket.connect(InetSocketAddress(ip, port), 3000)
                val out = socket.getOutputStream()
                repeat(copies) { i ->
                    out.write(data)
                    out.flush()
                    if (i < copies - 1) {
                        kotlinx.coroutines.delay(500)
                    }
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e("PrinterConnection", "TCP/IP print failed: ${e.message}", e)
                Result.failure(Exception("Gagal mencetak ke printer TCP/IP."))
            } finally {
                try {
                    socket?.close()
                } catch (e: Exception) {}
            }
        } else {
            // Bluetooth
            var socket: BluetoothSocket? = null
            try {
                if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
                    return@withContext Result.failure(Exception("Bluetooth tidak aktif atau tidak didukung."))
                }
                val device = bluetoothAdapter.getRemoteDevice(printer.address)
                socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
                socket.connect()
                val out = socket.outputStream
                repeat(copies) { i ->
                    out.write(data)
                    out.flush()
                    if (i < copies - 1) {
                        kotlinx.coroutines.delay(500)
                    }
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e("PrinterConnection", "Bluetooth print failed: ${e.message}", e)
                Result.failure(Exception("Gagal mencetak ke printer Bluetooth."))
            } finally {
                try {
                    socket?.close()
                } catch (e: Exception) {}
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun getPairedBluetoothDevices(): List<BluetoothDevice> {
        return try {
            bluetoothAdapter?.bondedDevices?.toList() ?: emptyList()
        } catch (e: SecurityException) {
            emptyList()
        }
    }
}