package id.my.matahati.pos.data.printer

import id.my.matahati.pos.model.CartItem
import id.my.matahati.pos.model.TransactionData
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class EscPosFormatter(private val cols: Int = 32) {

    private val ESC: Byte = 0x1B
    private val GS: Byte = 0x1D
    
    private val INIT = byteArrayOf(ESC, 0x40)
    private val FONT_B = byteArrayOf(ESC, 0x4D, 0x01) // Smaller font (Font B)
    private val ALIGN_LEFT = byteArrayOf(ESC, 0x61, 0x00)
    private val ALIGN_CENTER = byteArrayOf(ESC, 0x61, 0x01)
    
    private val BOLD_ON = byteArrayOf(ESC, 0x45, 0x01)
    private val BOLD_OFF = byteArrayOf(ESC, 0x45, 0x00)
    
    private val SIZE_NORMAL = byteArrayOf(GS, 0x21, 0x00)
    private val SIZE_DOUBLE = byteArrayOf(GS, 0x21, 0x11) // Double width & height

    fun formatReceipt(
        data: TransactionData,
        cashierName: String,
        savedOutletName: String? = null
    ): ByteArray {
        val trx = data.transaction ?: return byteArrayOf()
        val details = data.details ?: emptyList()
        
        // Prioritize cashier name from transaction data
        val finalCashierName = trx.posUser?.user?.name ?: cashierName
        
        val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID")).apply {
            maximumFractionDigits = 0
        }
        
        fun formatNum(str: String): String {
            return formatter.format(str.toDoubleOrNull() ?: 0.0)
        }

        fun formatDateTime(dateTime: String): String {
            return try {
                val inputFormat = if (dateTime.contains("T")) {
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
                } else {
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                }
                val outputFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale("id", "ID"))
                val date = inputFormat.parse(dateTime.replace(".000000Z", "").replace("Z", ""))
                if (date != null) outputFormat.format(date) else dateTime
            } catch (e: Exception) {
                dateTime.replace("T", " ").replace(".000000Z", "").replace("Z", "")
            }
        }

        val out = mutableListOf<Byte>()

        // 1. Initialize & Center Alignment
        out.addAll(INIT.toList())
        out.addAll(FONT_B.toList())
        out.addAll(ALIGN_CENTER.toList())
        
        // 2. Header Outlet (Bold & Double Size if <= 16 chars)
        val outletName = (trx.outlet?.name ?: savedOutletName ?: "MATA HATI CAFE").uppercase()
        out.addAll(BOLD_ON.toList())
        if (outletName.length <= 16) {
            out.addAll(SIZE_DOUBLE.toList())
            out.addAll("$outletName\n".toByteArray().toList())
            out.addAll(SIZE_NORMAL.toList())
        } else {
            out.addAll(SIZE_NORMAL.toList())
            out.addAll("$outletName\n".toByteArray().toList())
        }
        out.addAll(BOLD_OFF.toList())
        out.addAll("\n".toByteArray().toList())

        // 3. Info Transaksi (Left Aligned)
        out.addAll(ALIGN_LEFT.toList())
        out.addAll(formatLabelValue("No. Trx", trx.transactionNo).toList())
        out.addAll(formatLabelValue("Waktu", formatDateTime(trx.transactionDate)).toList())
        out.addAll(formatLabelValue("Kasir", finalCashierName).toList())
        out.addAll(formatLabelValue("Order", trx.orderType).toList())
        if (trx.customerName != null) {
            out.addAll(formatLabelValue("Customer", trx.customerName).toList())
        }
        if (trx.tableName != null) {
            out.addAll(formatLabelValue("Meja", trx.tableName).toList())
        }
        if (!trx.orderNote.isNullOrBlank()) {
            out.addAll(formatLabelValue("Catatan", trx.orderNote).toList())
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
                val (cleanNote, _) = CartItem.parseNoteAndSentQty(item.note)
                if (cleanNote.isNotBlank()) {
                    val wrappedNote = wrapText("Note: $cleanNote", cols - 2)
                    wrappedNote.forEach { line ->
                        out.addAll("  $line\n".toByteArray().toList())
                    }
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

    fun formatCheckPrint(
        cartItems: List<CartItem>,
        orderType: String,
        cashierName: String,
        customerName: String? = null,
        tableName: String? = null,
        discountAmount: Double = 0.0,
        taxAmount: Double = 0.0,
        paxCount: Int = 1
    ): ByteArray {
        val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID")).apply {
            maximumFractionDigits = 0
        }
        fun formatNum(num: Double): String {
            return formatter.format(num)
        }

        val now = SimpleDateFormat("dd MMM yyyy HH:mm", Locale("id", "ID")).format(java.util.Date())
        val out = mutableListOf<Byte>()

        // 1. Initialize & Center Alignment
        out.addAll(INIT.toList())
        out.addAll(FONT_B.toList())
        out.addAll(ALIGN_CENTER.toList())

        // 2. Title: BUKAN RESI PEMBAYARAN
        out.addAll(BOLD_ON.toList())
        out.addAll("BUKAN RESI PEMBAYARAN\n\n".toByteArray().toList())
        out.addAll(BOLD_OFF.toList())

        // 3. Info Pemesanan
        out.addAll(ALIGN_LEFT.toList())
        out.addAll(drawTwoColumns("Waktu Pemesanan", "Dilayani Oleh").toByteArray().toList())
        val cashierText = cashierName.ifBlank { "kasir" }
        out.addAll(drawTwoColumns(now, cashierText).toByteArray().toList())

        if (!tableName.isNullOrBlank()) {
            out.addAll(drawTwoColumns("Meja", tableName).toByteArray().toList())
        }
        if (!customerName.isNullOrBlank()) {
            out.addAll(drawTwoColumns("Customer", customerName).toByteArray().toList())
        }
        out.addAll(drawTwoColumns("Jumlah Tamu", "$paxCount Tamu").toByteArray().toList())

        // 4. Separator
        out.addAll(drawLine("-").toByteArray().toList())

        // 5. Order Type
        out.addAll(ALIGN_CENTER.toList())
        out.addAll(BOLD_ON.toList())
        val displayOrderType = if (orderType.isNotBlank()) orderType.replace("_", "-").uppercase() else "DINE-IN"
        out.addAll("$displayOrderType\n".toByteArray().toList())
        out.addAll(BOLD_OFF.toList())
        out.addAll(ALIGN_LEFT.toList())

        // 6. Items
        cartItems.forEach { item ->
            // Item Name
            val nameLines = wrapText(item.product.name, cols)
            nameLines.forEach { line ->
                out.addAll("$line\n".toByteArray().toList())
            }

            // Price x Qty -> Subtotal
            val priceStr = formatNum(item.product.price)
            val qtyLine = "    $priceStr x${item.quantity}"
            val itemSubtotalStr = formatNum(item.totalPrice)
            out.addAll(drawTwoColumns(qtyLine, itemSubtotalStr).toByteArray().toList())

            // Note / Sub-items if any
            if (!item.note.isNullOrBlank()) {
                val (cleanNote, _) = CartItem.parseNoteAndSentQty(item.note)
                if (cleanNote.isNotBlank()) {
                    val wrappedNote = wrapText(cleanNote, cols - 2)
                    wrappedNote.forEach { line ->
                        out.addAll("  $line\n".toByteArray().toList())
                    }
                }
            }
        }

        // 7. Separator
        out.addAll(drawLine("-").toByteArray().toList())

        // 8. Totals
        val subtotal = cartItems.sumOf { it.totalPrice }
        out.addAll(drawTwoColumns("Subtotal", formatNum(subtotal)).toByteArray().toList())
        if (discountAmount > 0) {
            out.addAll(drawTwoColumns("Discount", "-${formatNum(discountAmount)}").toByteArray().toList())
        }
        if (taxAmount > 0) {
            out.addAll(drawTwoColumns("Tax", formatNum(taxAmount)).toByteArray().toList())
        }

        val grandTotal = (subtotal - discountAmount + taxAmount).coerceAtLeast(0.0)
        out.addAll(BOLD_ON.toList())
        out.addAll(drawTwoColumns("Grand Total", "Rp ${formatNum(grandTotal)}").toByteArray().toList())
        out.addAll(BOLD_OFF.toList())

        out.addAll("\n".toByteArray().toList())
        val totalQty = cartItems.sumOf { it.quantity }
        out.addAll("Jumlah Item: $totalQty\n".toByteArray().toList())

        // 9. Paper Feed
        out.addAll("\n\n\n\n\n".toByteArray().toList())

        return out.toByteArray()
    }

    fun formatKitchenTicket(tickets: Map<String, List<CartItem>>): ByteArray {
        val out = mutableListOf<Byte>()

        // Initialize
        out.addAll(INIT.toList())
        out.addAll(FONT_B.toList())
        
        tickets.forEach { (station, items) ->
            if (items.isEmpty()) return@forEach

            // Station Header
            out.addAll(ALIGN_CENTER.toList())
            out.addAll(drawLine("-").toByteArray().toList())
            out.addAll(BOLD_ON.toList())
            out.addAll(SIZE_DOUBLE.toList())
            out.addAll("$station\n".toByteArray().toList())
            out.addAll(SIZE_NORMAL.toList())
            out.addAll(BOLD_OFF.toList())
            out.addAll(drawLine("-").toByteArray().toList())
            out.addAll("\n".toByteArray().toList())

            // Items
            out.addAll(ALIGN_LEFT.toList())
            items.forEach { item ->
                // Format: Qty x ProductName
                val qtyPart = "${item.quantity} x "
                val nameLines = wrapText(item.product.name, cols - qtyPart.length)
                
                out.addAll(BOLD_ON.toList())
                out.addAll("$qtyPart${nameLines[0]}\n".toByteArray().toList())
                for (i in 1 until nameLines.size) {
                    out.addAll((" ".repeat(qtyPart.length) + nameLines[i] + "\n").toByteArray().toList())
                }
                out.addAll(BOLD_OFF.toList())

                if (!item.note.isNullOrBlank()) {
                    val wrappedNote = wrapText("Note: ${item.note}", cols - 2)
                    wrappedNote.forEach { line ->
                        out.addAll("  $line\n".toByteArray().toList())
                    }
                }
                out.addAll("\n".toByteArray().toList())
            }
            
            out.addAll("\n".toByteArray().toList())
        }

        // Footer Separator
        out.addAll(ALIGN_CENTER.toList())
        out.addAll(drawLine("=").toByteArray().toList())
        
        // Date/Time for kitchen
        val now = SimpleDateFormat("dd MMM yyyy HH:mm", Locale("id", "ID")).format(java.util.Date())
        out.addAll("Waktu: $now\n".toByteArray().toList())
        
        // Paper Feed
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
        val availableLeft = cols - right.length - 1
        return if (left.length <= availableLeft) {
            val padding = cols - left.length - right.length
            left + " ".repeat(padding.coerceAtLeast(0)) + right + "\n"
        } else if (availableLeft > 3) {
            val truncatedLeft = left.take(availableLeft)
            val padding = cols - truncatedLeft.length - right.length
            truncatedLeft + " ".repeat(padding.coerceAtLeast(0)) + right + "\n"
        } else {
            left + "\n" + " ".repeat((cols - right.length).coerceAtLeast(0)) + right + "\n"
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
        val nowStr = SimpleDateFormat("dd MMM yyyy HH:mm", Locale("id", "ID")).format(java.util.Date())
        val out = mutableListOf<Byte>()
        out.addAll(INIT.toList())
        out.addAll(FONT_B.toList())
        out.addAll(ALIGN_CENTER.toList())
        out.addAll(BOLD_ON.toList())
        out.addAll(SIZE_DOUBLE.toList())
        out.addAll("MATAHATI POS\n".toByteArray().toList())
        out.addAll(SIZE_NORMAL.toList())
        out.addAll("TEST PRINT\n".toByteArray().toList())
        out.addAll(BOLD_OFF.toList())
        out.addAll("\n".toByteArray().toList())
        out.addAll(ALIGN_LEFT.toList())
        out.addAll("Waktu: $nowStr\n".toByteArray().toList())
        out.addAll("Koneksi Printer: OK\n".toByteArray().toList())
        out.addAll(drawLine("=").toByteArray().toList())
        out.addAll("Printable width: $cols columns\n".toByteArray().toList())
        out.addAll(drawLine("-").toByteArray().toList())
        out.addAll("\n\n\n\n\n".toByteArray().toList())
        return out.toByteArray()
    }

    fun formatProductSalesSummaryReceipt(
        dateDisplay: String,
        items: List<id.my.matahati.pos.model.ProductSalesSummaryItem>,
        header: id.my.matahati.pos.model.ProductSalesSummaryHeader?,
        savedOutletName: String? = null,
        apiDate: String? = null
    ): ByteArray {
        val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID")).apply {
            maximumFractionDigits = 0
        }
        fun formatNum(num: Double): String {
            return formatter.format(num)
        }

        val reportDateFormatted = try {
            if (!apiDate.isNullOrBlank()) {
                val inputSdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val outputSdf = SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("id-ID"))
                val parsed = inputSdf.parse(apiDate)
                if (parsed != null) outputSdf.format(parsed) else dateDisplay
            } else {
                val cleaned = dateDisplay.substringAfter(", ").trim()
                cleaned.ifBlank { dateDisplay }
            }
        } catch (_: Exception) {
            dateDisplay
        }

        val currentPrintedTime = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.forLanguageTag("id-ID")).format(java.util.Date())

        val out = mutableListOf<Byte>()
        out.addAll(INIT.toList())
        out.addAll(FONT_B.toList())
        out.addAll(ALIGN_CENTER.toList())

        val outletName = "MATA HATI CAFE"
        out.addAll(BOLD_ON.toList())
        out.addAll(SIZE_DOUBLE.toList())
        out.addAll("$outletName\n".toByteArray().toList())
        out.addAll(SIZE_NORMAL.toList())
        out.addAll("Ringkasan Penjualan Produk\n\n".toByteArray().toList())
        out.addAll(BOLD_OFF.toList())

        out.addAll(ALIGN_LEFT.toList())
        out.addAll("Mulai      : $reportDateFormatted 00:00\n".toByteArray().toList())
        out.addAll("Akhir      : $reportDateFormatted 23:50\n".toByteArray().toList())
        out.addAll("Tercetak   : $currentPrintedTime\n\n".toByteArray().toList())

        val priorityMap = mapOf("MAKANAN" to 1, "MINUMAN" to 2, "SNACK" to 3)
        val sortedGroupedItems = items.groupBy { it.categoryName.ifBlank { "LAINNYA" }.uppercase() }
            .entries
            .sortedWith(compareBy({ priorityMap[it.key] ?: 99 }, { it.key }))

        sortedGroupedItems.forEach { (categoryName, categoryItems) ->
            val catQty = categoryItems.sumOf { it.soldQty }
            val catSales = categoryItems.sumOf { it.totalSales }
            val catRightStr = "$catQty / ${formatNum(catSales)}"

            out.addAll(BOLD_ON.toList())
            out.addAll(drawTwoColumns(categoryName, catRightStr).toByteArray().toList())
            out.addAll(BOLD_OFF.toList())
            out.addAll(drawLine("-").toByteArray().toList())

            categoryItems.forEach { item ->
                val itemLeftStr = item.productName.ifBlank { "Produk" }
                val itemRightStr = "${item.soldQty} / ${formatNum(item.totalSales)}"
                out.addAll(drawTwoColumns(itemLeftStr, itemRightStr).toByteArray().toList())
            }
            out.addAll("\n".toByteArray().toList())
        }

        out.addAll("\n\n\n\n\n".toByteArray().toList())

        return out.toByteArray()
    }

    fun formatShiftReceipt(
        shift: id.my.matahati.pos.model.ShiftResponse,
        savedOutletName: String? = null,
        userName: String? = null
    ): ByteArray {
        val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID")).apply {
            maximumFractionDigits = 0
        }
        fun formatNum(num: Double): String {
            return formatter.format(num)
        }

        fun formatShiftDay(dateTime: String?): String {
            if (dateTime.isNullOrBlank()) return "-"
            return try {
                val inputFormat = if (dateTime.contains("T")) {
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
                } else {
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                }
                val outputFormat = SimpleDateFormat("EEE, dd MMM yyyy", Locale("id", "ID"))
                val date = inputFormat.parse(dateTime.replace(".000000Z", "").replace("Z", ""))
                if (date != null) outputFormat.format(date) else dateTime
            } catch (e: Exception) {
                dateTime
            }
        }

        val currentPrintedTime = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.forLanguageTag("id-ID")).format(java.util.Date())
        val outletName = (savedOutletName ?: "MATA HATI CAFE").uppercase()

        val cashierName = shift.user?.name ?: userName ?: "Kasir"
        val rawReceipts = shift.totalReceipts ?: 0
        val totalSales = shift.totalSales
        val totalReceipts = if (rawReceipts > 0) rawReceipts else if (totalSales > 0) 1 else 0
        val totalPax = if ((shift.totalPax ?: 0) > 0) shift.totalPax!! else totalReceipts

        val discountVal = shift.discountAmount ?: 0.0
        val subtotalVal = shift.subtotal ?: (totalSales + discountVal)

        val cashSalesVal = shift.cashSales ?: totalSales
        val refundCashVal = shift.refundCash
        val cancellationCashVal = shift.cancellationCash
        val netCashMovement = shift.cashIn - shift.cashOut
        val expectedCashVal = shift.expectedCash ?: (shift.openingCash + cashSalesVal + netCashMovement - refundCashVal - cancellationCashVal)

        val out = mutableListOf<Byte>()
        out.addAll(INIT.toList())
        out.addAll(FONT_B.toList())
        out.addAll(ALIGN_CENTER.toList())

        // Outlet Name & Title
        out.addAll(BOLD_ON.toList())
        out.addAll(SIZE_DOUBLE.toList())
        out.addAll("$outletName\n".toByteArray().toList())
        out.addAll(SIZE_NORMAL.toList())
        out.addAll("Penutupan Penjualan\n\n".toByteArray().toList())
        out.addAll(BOLD_OFF.toList())

        // Header Metadata
        out.addAll(ALIGN_LEFT.toList())
        out.addAll("Tercetak     : $currentPrintedTime\n".toByteArray().toList())
        out.addAll("Dicetak Oleh : $cashierName\n\n".toByteArray().toList())
        out.addAll("Tanggal      : ${formatShiftDay(shift.openedAt)}\n\n".toByteArray().toList())

        // Tamu & Resi
        out.addAll(drawTwoColumns("Jumlah Tamu", "$totalPax").toByteArray().toList())
        out.addAll(drawLine("-").toByteArray().toList())
        out.addAll(drawTwoColumns("Resi", "$totalReceipts").toByteArray().toList())
        out.addAll(drawLine("-").toByteArray().toList())
        out.addAll(drawTwoColumns("Pengembalian", formatNum(refundCashVal)).toByteArray().toList())
        out.addAll(drawLine("-").toByteArray().toList())

        // Penjualan & Subtotal
        out.addAll(BOLD_ON.toList())
        out.addAll(drawTwoColumns("Total Penjualan", formatNum(totalSales)).toByteArray().toList())
        out.addAll(BOLD_OFF.toList())
        out.addAll(drawLine("-").toByteArray().toList())

        if (discountVal != 0.0) {
            out.addAll(drawTwoColumns("  Subtotal", formatNum(subtotalVal)).toByteArray().toList())
            val discText = if (discountVal > 0) "-${formatNum(discountVal)}" else formatNum(discountVal)
            out.addAll(drawTwoColumns("  Diskon Bill", discText).toByteArray().toList())
            out.addAll(drawTwoColumns("", formatNum(totalSales)).toByteArray().toList())
        } else {
            out.addAll(drawTwoColumns("  Subtotal", formatNum(totalSales)).toByteArray().toList())
        }
        out.addAll("\n".toByteArray().toList())

        // Kas Section
        out.addAll(BOLD_ON.toList())
        out.addAll(drawTwoColumns("Kas", formatNum(expectedCashVal)).toByteArray().toList())
        out.addAll(BOLD_OFF.toList())
        out.addAll(drawLine("-").toByteArray().toList())

        out.addAll(drawTwoColumns("  Kas Penjualan", formatNum(cashSalesVal)).toByteArray().toList())
        out.addAll(drawTwoColumns("  Kas Pengembalian", formatNum(refundCashVal)).toByteArray().toList())
        out.addAll(drawTwoColumns("  Kas Pembatalan", formatNum(cancellationCashVal)).toByteArray().toList())
        out.addAll(drawTwoColumns("  Kas Masuk-Keluar", formatNum(netCashMovement)).toByteArray().toList())
        out.addAll(drawLine("-").toByteArray().toList())

        out.addAll(BOLD_ON.toList())
        out.addAll(drawTwoColumns("Total Diharapkan", formatNum(expectedCashVal)).toByteArray().toList())
        out.addAll(BOLD_OFF.toList())

        out.addAll("\n\n\n\n\n".toByteArray().toList())

        return out.toByteArray()
    }
}
