package id.my.matahati.pos.data.printer

import id.my.matahati.pos.model.TransactionData
import java.text.NumberFormat
import java.util.Locale

class EscPosFormatter(private val cols: Int = 32) {

    private val ESC: Byte = 0x1B
    private val GS: Byte = 0x1D
    
    private val INIT = byteArrayOf(ESC, 0x40)
    private val ALIGN_LEFT = byteArrayOf(ESC, 0x61, 0x00)
    private val ALIGN_CENTER = byteArrayOf(ESC, 0x61, 0x01)
    
    private val BOLD_ON = byteArrayOf(ESC, 0x45, 0x01)
    private val BOLD_OFF = byteArrayOf(ESC, 0x45, 0x00)
    
    private val SIZE_NORMAL = byteArrayOf(GS, 0x21, 0x00)
    private val SIZE_DOUBLE = byteArrayOf(GS, 0x21, 0x11) // Double width & height

    fun formatReceipt(data: TransactionData, cashierName: String): ByteArray {
        val trx = data.transaction ?: return byteArrayOf()
        val details = data.details ?: emptyList()
        
        val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID")).apply {
            maximumFractionDigits = 0
        }
        
        fun formatNum(str: String): String {
            return formatter.format(str.toDoubleOrNull() ?: 0.0)
        }

        val out = mutableListOf<Byte>()

        // 1. Initialize & Center Alignment
        out.addAll(INIT.toList())
        out.addAll(ALIGN_CENTER.toList())
        
        // 2. Header Outlet (Bold & Double Size)
        out.addAll(BOLD_ON.toList())
        out.addAll(SIZE_DOUBLE.toList())
        out.addAll("OUTLET MH TA\n".toByteArray().toList())
        out.addAll(SIZE_NORMAL.toList())
        out.addAll(BOLD_OFF.toList())
        out.addAll("\n".toByteArray().toList())

        // 3. Info Transaksi (Left Aligned)
        out.addAll(ALIGN_LEFT.toList())
        out.addAll(formatLabelValue("No. Trx", trx.transactionNo).toList())
        out.addAll(formatLabelValue("Waktu", trx.transactionDate).toList())
        out.addAll(formatLabelValue("Kasir", cashierName).toList())
        out.addAll(formatLabelValue("Order", trx.orderType).toList())
        if (trx.customerName != null) {
            out.addAll(formatLabelValue("Customer", trx.customerName).toList())
        }
        if (trx.tableName != null) {
            out.addAll(formatLabelValue("Meja", trx.tableName).toList())
        }

        // 4. Separator
        out.addAll(drawLine("-").toByteArray().toList())

        // 5. Items
        details.forEach { item ->
            // Wrap product name if too long
            val wrappedName = wrapText(item.productName, cols)
            wrappedName.forEach { line ->
                out.addAll("$line\n".toByteArray().toList())
            }
            
            val qtyLine = "${item.quantity} x ${formatNum(item.price)}"
            val subtotalLine = formatNum(item.subtotal)
            
            out.addAll(drawTwoColumns(qtyLine, subtotalLine).toByteArray().toList())
            
            if (!item.note.isNullOrBlank()) {
                val wrappedNote = wrapText("Note: ${item.note}", cols - 2)
                wrappedNote.forEach { line ->
                    out.addAll("  $line\n".toByteArray().toList())
                }
            }
        }

        // 6. Separator
        out.addAll(drawLine("-").toByteArray().toList())

        // 7. Totals
        out.addAll(drawTwoColumns("Subtotal", formatNum(trx.subtotal)).toByteArray().toList())
        out.addAll(drawTwoColumns("Discount", formatNum(trx.discount)).toByteArray().toList())
        out.addAll(drawTwoColumns("Tax", formatNum(trx.tax)).toByteArray().toList())
        
        out.addAll(drawLine("-").toByteArray().toList())
        
        // 8. GRAND TOTAL (Bold)
        out.addAll(BOLD_ON.toList())
        out.addAll(drawTwoColumns("GRAND TOTAL", formatNum(trx.grandTotal)).toByteArray().toList())
        out.addAll(BOLD_OFF.toList())
        
        out.addAll(drawTwoColumns("Paid", formatNum(trx.paidAmount)).toByteArray().toList())
        out.addAll(drawTwoColumns("Change", formatNum(trx.changeAmount)).toByteArray().toList())

        // 9. Status & Footer (Center)
        out.addAll("\n".toByteArray().toList())
        out.addAll(ALIGN_CENTER.toList())
        out.addAll(drawLine("=").toByteArray().toList())
        out.addAll(BOLD_ON.toList())
        out.addAll("Status: ${trx.status}\n".toByteArray().toList())
        out.addAll(BOLD_OFF.toList())
        out.addAll(drawLine("=").toByteArray().toList())
        
        out.addAll("\n".toByteArray().toList())
        out.addAll("TERIMA KASIH\n".toByteArray().toList())
        
        // 10. Paper Feed
        out.addAll("\n\n\n\n\n".toByteArray().toList())

        return out.toByteArray()
    }

    private fun formatLabelValue(label: String, value: String): ByteArray {
        val labelPart = "$label: "
        val availableForValue = cols - labelPart.length
        
        if (value.length <= availableForValue) {
            return "$labelPart$value\n".toByteArray()
        } else {
            val lines = wrapText(value, availableForValue)
            val result = StringBuilder()
            result.append("$labelPart${lines[0]}\n")
            for (i in 1 until lines.size) {
                result.append(" ".repeat(labelPart.length) + lines[i] + "\n")
            }
            return result.toString().toByteArray()
        }
    }

    private fun drawLine(char: String): String {
        return char.repeat(cols) + "\n"
    }

    private fun drawTwoColumns(left: String, right: String): String {
        val padding = cols - left.length - right.length
        return if (padding > 0) {
            left + " ".repeat(padding) + right + "\n"
        } else {
            left + "\n" + " ".repeat(cols - right.length) + right + "\n"
        }
    }

    private fun wrapText(text: String, width: Int): List<String> {
        if (text.length <= width) return listOf(text)
        
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = StringBuilder()
        
        for (word in words) {
            if (currentLine.length + word.length + 1 <= width) {
                if (currentLine.isNotEmpty()) currentLine.append(" ")
                currentLine.append(word)
            } else {
                if (currentLine.isNotEmpty()) {
                    lines.add(currentLine.toString())
                    currentLine = StringBuilder()
                }
                
                var remainingWord = word
                while (remainingWord.length > width) {
                    lines.add(remainingWord.substring(0, width))
                    remainingWord = remainingWord.substring(width)
                }
                currentLine.append(remainingWord)
            }
        }
        if (currentLine.isNotEmpty()) lines.add(currentLine.toString())
        
        return lines
    }
    
    fun formatTestPrint(): ByteArray {
        val out = mutableListOf<Byte>()
        out.addAll(INIT.toList())
        out.addAll(ALIGN_CENTER.toList())
        out.addAll(BOLD_ON.toList())
        out.addAll(SIZE_DOUBLE.toList())
        out.addAll("MATAHATI POS\n".toByteArray().toList())
        out.addAll(SIZE_NORMAL.toList())
        out.addAll("TEST PRINT\n".toByteArray().toList())
        out.addAll(BOLD_OFF.toList())
        out.addAll("\n".toByteArray().toList())
        out.addAll(ALIGN_LEFT.toList())
        out.addAll("Bluetooth connection OK\n".toByteArray().toList())
        out.addAll(drawLine("=").toByteArray().toList())
        out.addAll("Printable width: $cols columns\n".toByteArray().toList())
        out.addAll(drawLine("-").toByteArray().toList())
        out.addAll("\n\n\n\n\n".toByteArray().toList())
        return out.toByteArray()
    }
}
