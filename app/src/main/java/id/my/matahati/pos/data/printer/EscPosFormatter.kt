package id.my.matahati.pos.data.printer

import id.my.matahati.pos.model.CartItem
import id.my.matahati.pos.model.TransactionData
import id.my.matahati.pos.model.getCashCancellationAmount
import id.my.matahati.pos.model.getExpectedCashAmount
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class EscPosFormatter(private val cols: Int = 32) {

    private val ESC: Byte = 0x1B
    private val GS: Byte = 0x1D
    
    private val INIT = byteArrayOf(ESC, 0x40)
    private val FONT_A = byteArrayOf(ESC, 0x4D, 0x00) // Standard font (Font A - 12x24)
    private val FONT_B = byteArrayOf(ESC, 0x4D, 0x01) // Smaller font (Font B - 9x17)
    private val ALIGN_LEFT = byteArrayOf(ESC, 0x61, 0x00)
    private val ALIGN_CENTER = byteArrayOf(ESC, 0x61, 0x01)
    
    private val BOLD_ON = byteArrayOf(ESC, 0x45, 0x01)
    private val BOLD_OFF = byteArrayOf(ESC, 0x45, 0x00)
    
    private val SIZE_NORMAL = byteArrayOf(GS, 0x21, 0x00)
    private val SIZE_DOUBLE = byteArrayOf(GS, 0x21, 0x11) // Double width & height

    fun formatReceipt(
        data: TransactionData,
        cashierName: String,
        savedOutletName: String? = null,
        isOrderTypeSelected: Boolean = true,
        paymentMethods: List<id.my.matahati.pos.model.PaymentMethod> = emptyList(),
        taxSetting: id.my.matahati.pos.model.TaxSettingItem? = null
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
        out.addAll(FONT_A.toList())
        out.addAll(ALIGN_CENTER.toList())

        // 2. Receipt Title Header from Tax Setting (No Outlet Name Header)
        val titleStr = taxSetting?.receiptTitle?.trim()
        if (!titleStr.isNullOrBlank()) {
            val titleLines = titleStr.lines()
            titleLines.forEachIndexed { index, line ->
                if (index == 0) {
                    out.addAll(BOLD_ON.toList())
                    out.addAll("$line\n".toByteArray().toList())
                    out.addAll(BOLD_OFF.toList())
                } else {
                    out.addAll("$line\n".toByteArray().toList())
                }
            }
            out.addAll("\n".toByteArray().toList())
        }

        // 3. Info Transaksi Atas (Left Aligned)
        out.addAll(ALIGN_LEFT.toList())
        out.addAll("${formatDateTime(trx.transactionDate)}\n".toByteArray().toList())
        out.addAll(formatAlignedLabelValue("Kasir", finalCashierName, 12).toList())
        if (!trx.customerName.isNullOrBlank()) {
            out.addAll(formatAlignedLabelValue("Pelanggan", trx.customerName, 12).toList())
        }
        if (taxSetting?.hideOrderNo != true && trx.transactionNo.isNotBlank()) {
            val formattedTrxNo = if (trx.transactionNo.startsWith("#")) trx.transactionNo else "#${trx.transactionNo}"
            out.addAll("$formattedTrxNo\n".toByteArray().toList())
        }

        // 4. Separator 1
        out.addAll(drawLine("-").toByteArray().toList())

        // 5. Antrian & Order Type (Center Aligned)
        out.addAll(ALIGN_CENTER.toList())
        val orderTypeDisplay = (trx.orderType ?: "DINE_IN").replace("_", "-").uppercase()
        val queueStr = trx.queueNo?.takeIf { it.isNotBlank() }
        val queueLine = if (!queueStr.isNullOrBlank()) "Antrian $queueStr ($orderTypeDisplay)" else "($orderTypeDisplay)"
        out.addAll("$queueLine\n".toByteArray().toList())
        out.addAll(ALIGN_LEFT.toList())

        // 6. Items
        details.forEach { item ->
            val wrappedName = wrapText(item.productName, cols)
            wrappedName.forEach { line ->
                out.addAll("$line\n".toByteArray().toList())
            }
            
            val qtyLine = "    Rp. ${formatNum(item.price)}  x${item.quantity}"
            val subtotalLine = "Rp. ${formatNum(item.subtotal)}"
            
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

        // 7. Separator 2
        out.addAll(drawLine("-").toByteArray().toList())

        // 8. Totals
        out.addAll(drawTwoColumns("Subtotal", "Rp. ${formatNum(trx.subtotal)}").toByteArray().toList())
        
        val scPercent = taxSetting?.serviceChargePercent ?: 0.0
        if (scPercent > 0) {
            val scAmount = (trx.subtotal.toDoubleOrNull() ?: 0.0) * (scPercent / 100.0)
            out.addAll(drawTwoColumns("Service charge $scPercent%", "Rp. ${formatNum(scAmount.toString())}").toByteArray().toList())
        }

        val discountVal = trx.discount.toDoubleOrNull() ?: 0.0
        val voucherCode = trx.voucher?.ckode?.takeIf { it.isNotBlank() }
        if (!voucherCode.isNullOrBlank()) {
            val voucherLabel = "Voucher ($voucherCode)"
            out.addAll(drawTwoColumns(voucherLabel, "-Rp. ${formatNum(trx.discount)}").toByteArray().toList())
        } else if (discountVal > 0) {
            out.addAll(drawTwoColumns("Diskon", "-Rp. ${formatNum(trx.discount)}").toByteArray().toList())
        }

        val categoryStr = trx.transactionCategory ?: taxSetting?.category ?: "OFFLINE"
        val isOnlineCategory = categoryStr.equals("ONLINE", ignoreCase = true)
        val taxVal = trx.tax.toDoubleOrNull() ?: 0.0
        val taxMode = trx.taxMode ?: (if (taxSetting?.isTaxIncluded == true) "INCLUDE" else if (taxVal > 0) "EXCLUDE" else "NONE")
        val taxName = taxSetting?.taxName?.takeIf { it.isNotBlank() } ?: "PPN"
        val taxPercent = trx.taxPercent ?: (taxSetting?.taxPercent ?: 0.0)

        if (!isOnlineCategory && (taxVal > 0 || taxMode == "INCLUDE")) {
            val taxLabelText = if (taxPercent > 0) "$taxName ${taxPercent.toInt()}%" else taxName
            out.addAll(drawTwoColumns(taxLabelText, "Rp. ${formatNum(trx.tax)}").toByteArray().toList())
        }
        
        // 9. Separator 3
        out.addAll(drawLine("-").toByteArray().toList())
        
        // 10. Total & Pembayaran
        out.addAll(BOLD_ON.toList())
        out.addAll(drawTwoColumns("Total", "Rp. ${formatNum(trx.grandTotal)}").toByteArray().toList())
        out.addAll(BOLD_OFF.toList())

        val resolvedPayment1Name = trx.payment?.cname?.takeIf { it.isNotBlank() }
            ?: paymentMethods.find { it.id == trx.nidPayment }?.name
            ?: "CASH"
        val rawP1 = resolvedPayment1Name.uppercase()
        val payment1Name = if (rawP1.contains("CASH") || rawP1.contains("TUNAI")) "Tunai" else rawP1

        val resolvedPayment2Name = trx.paymentDual?.cname?.takeIf { it.isNotBlank() }
            ?: paymentMethods.find { it.id == trx.nidPaymentDual }?.name
        val payment2Name = resolvedPayment2Name?.uppercase()

        val isDualPayment = !payment2Name.isNullOrBlank()
        val salesPayments = trx.salesPayments

        val totalPaid = trx.paidAmount.toDoubleOrNull() ?: 0.0
        val grandTotalNum = trx.grandTotal.toDoubleOrNull() ?: 0.0
        val changeCalculated = maxOf(0.0, totalPaid - grandTotalNum)
        val changeToDisplay = if (changeCalculated > 0) changeCalculated else (trx.changeAmount.toDoubleOrNull() ?: 0.0)

        if (!salesPayments.isNullOrEmpty() && salesPayments.size > 1) {
            salesPayments.forEachIndexed { index, sp ->
                val pName = sp.paymentMethod?.cname ?: "Bayar ${index + 1}"
                out.addAll(drawTwoColumns(pName, "Rp. ${formatNum(sp.amount.toString())}").toByteArray().toList())
            }
            out.addAll(drawTwoColumns("Kembalian", "Rp. ${formatNum(changeToDisplay.toString())}").toByteArray().toList())
        } else if (isDualPayment) {
            val dpVal = if (trx.dpAmount > 0) trx.dpAmount else totalPaid / 2
            val secondVal = maxOf(0.0, totalPaid - dpVal)
            out.addAll(drawTwoColumns(payment1Name, "Rp. ${formatNum(dpVal.toString())}").toByteArray().toList())
            out.addAll(drawTwoColumns(payment2Name!!, "Rp. ${formatNum(secondVal.toString())}").toByteArray().toList())
            out.addAll(drawTwoColumns("Kembalian", "Rp. ${formatNum(changeToDisplay.toString())}").toByteArray().toList())
        } else {
            out.addAll(drawTwoColumns(payment1Name, "Rp. ${formatNum(trx.paidAmount)}").toByteArray().toList())
            if (payment1Name.equals("Tunai", ignoreCase = true) || changeToDisplay > 0 || totalPaid > 0) {
                out.addAll(drawTwoColumns("Kembalian", "Rp. ${formatNum(changeToDisplay.toString())}").toByteArray().toList())
            }
        }

        out.addAll("\n".toByteArray().toList())
        val itemCount = if (trx.itemCount > 0) trx.itemCount else details.sumOf { it.quantity }
        out.addAll("Jumlah item : $itemCount\n".toByteArray().toList())
        out.addAll("Dilayani oleh : $finalCashierName\n".toByteArray().toList())
        
        // 11. Footer Note (Center Aligned)
        val noteStr = taxSetting?.receiptNote?.trim()
        if (!noteStr.isNullOrBlank()) {
            out.addAll("\n".toByteArray().toList())
            out.addAll(ALIGN_CENTER.toList())
            noteStr.lines().forEach { line ->
                out.addAll("$line\n".toByteArray().toList())
            }
            out.addAll("\n".toByteArray().toList())
        }
        
        // 12. Paper Feed
        out.addAll("\n\n\n\n\n".toByteArray().toList())

        return out.toByteArray()
        
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
        paxCount: Int = 1,
        taxSetting: id.my.matahati.pos.model.TaxSettingItem? = null
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
        out.addAll(FONT_A.toList())
        out.addAll(ALIGN_CENTER.toList())

        // 2. Title: BUKAN RESI PEMBAYARAN
        out.addAll(BOLD_ON.toList())
        out.addAll("BUKAN RESI PEMBAYARAN\n".toByteArray().toList())
        out.addAll(BOLD_OFF.toList())

        if (!taxSetting?.receiptTitle.isNullOrBlank()) {
            taxSetting?.receiptTitle?.lines()?.forEach { line ->
                if (line.isNotBlank()) {
                    out.addAll("$line\n".toByteArray().toList())
                }
            }
        }
        out.addAll("\n".toByteArray().toList())

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
        if (orderType.isNotBlank()) {
            out.addAll(ALIGN_CENTER.toList())
            out.addAll(BOLD_ON.toList())
            val displayOrderType = orderType.replace("_", "-").uppercase()
            out.addAll("$displayOrderType\n".toByteArray().toList())
            out.addAll(BOLD_OFF.toList())
            out.addAll(ALIGN_LEFT.toList())
        }

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

        val taxName = taxSetting?.taxName?.takeIf { it.isNotBlank() } ?: "Pajak"
        val taxPercent = taxSetting?.taxPercent ?: 0.0
        val isInclude = taxSetting?.isTaxIncluded == true

        if (taxAmount > 0 || isInclude) {
            val taxLabelText = when {
                isInclude && taxPercent > 0 -> "$taxName ${taxPercent.toInt()}% (Include)"
                isInclude -> "$taxName (Include)"
                taxPercent > 0 -> "$taxName ${taxPercent.toInt()}%"
                else -> taxName
            }
            out.addAll(drawTwoColumns(taxLabelText, formatNum(taxAmount)).toByteArray().toList())
        }

        val grandTotal = (subtotal - discountAmount + taxAmount).coerceAtLeast(0.0)
        out.addAll(BOLD_ON.toList())
        out.addAll(drawTwoColumns("Grand Total", "Rp ${formatNum(grandTotal)}").toByteArray().toList())
        out.addAll(BOLD_OFF.toList())

        out.addAll("\n".toByteArray().toList())
        val totalQty = cartItems.sumOf { it.quantity }
        out.addAll("Jumlah Item: $totalQty\n".toByteArray().toList())

        if (!taxSetting?.receiptNote.isNullOrBlank()) {
            out.addAll("\n".toByteArray().toList())
            taxSetting?.receiptNote?.lines()?.forEach { line ->
                if (line.isNotBlank()) {
                    out.addAll("$line\n".toByteArray().toList())
                }
            }
        }

        // 9. Paper Feed
        out.addAll("\n\n\n\n\n".toByteArray().toList())

        return out.toByteArray()
    }

    fun formatKitchenTicket(tickets: Map<String, List<CartItem>>): ByteArray {
        val out = mutableListOf<Byte>()

        // Initialize
        out.addAll(INIT.toList())
        out.addAll(FONT_A.toList())
        
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

    fun formatBarItemLabel(
        item: CartItem,
        transactionNo: String,
        dateTimeStr: String? = null
    ): ByteArray {
        val out = mutableListOf<Byte>()
        val now = dateTimeStr ?: SimpleDateFormat("dd MMM yyyy HH:mm", Locale("id", "ID")).format(java.util.Date())
        val cleanTrxNo = if (transactionNo.isNotBlank()) transactionNo else "001"

        // Initialize
        out.addAll(INIT.toList())
        out.addAll(FONT_A.toList())
        out.addAll(ALIGN_LEFT.toList())

        // 1. Transaction / Order No (e.g. No: 001)
        out.addAll(BOLD_ON.toList())
        out.addAll("No: $cleanTrxNo\n".toByteArray().toList())
        out.addAll(BOLD_OFF.toList())

        // 2. Product Name (Upcase / Bold)
        out.addAll(BOLD_ON.toList())
        val uppercaseName = item.product.name.uppercase()
        val nameLines = wrapText(uppercaseName, cols)
        nameLines.forEach { line ->
            out.addAll("$line\n".toByteArray().toList())
        }
        out.addAll(BOLD_OFF.toList())

        // 3. Note (if any)
        if (!item.note.isNullOrBlank()) {
            val (cleanNote, _) = CartItem.parseNoteAndSentQty(item.note)
            if (cleanNote.isNotBlank()) {
                val wrappedNote = wrapText("Note: $cleanNote", cols)
                wrappedNote.forEach { line ->
                    out.addAll("$line\n".toByteArray().toList())
                }
            }
        }

        out.addAll("\n".toByteArray().toList())

        // 4. Date & Time
        out.addAll("$now\n".toByteArray().toList())

        // 5. Paper Feed
        out.addAll("\n\n\n\n".toByteArray().toList())

        return out.toByteArray()
    }

    private fun formatLabelValue(label: String, value: String): ByteArray {
        return formatAlignedLabelValue(label, value, 12)
    }

    private fun formatAlignedLabelValue(label: String, value: String, labelWidth: Int = 12): ByteArray {
        val paddedLabel = label.padEnd(labelWidth) + ": "
        val availableForValue = cols - paddedLabel.length
        if (availableForValue <= 0) return "$label: $value\n".toByteArray()

        if (value.length <= availableForValue) {
            return "$paddedLabel$value\n".toByteArray()
        } else {
            val lines = wrapText(value, availableForValue)
            val result = StringBuilder()
            result.append("$paddedLabel${lines[0]}\n")
            for (i in 1 until lines.size) {
                result.append(" ".repeat(paddedLabel.length) + lines[i] + "\n")
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
        out.addAll(FONT_A.toList())
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
        out.addAll(FONT_A.toList())
        out.addAll(ALIGN_CENTER.toList())

        val outletName = (savedOutletName ?: "IQOS MATAHATI TULUNGAGUNG").uppercase()
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
        val outletName = (savedOutletName ?: "IQOS MATAHATI TULUNGAGUNG").uppercase()

        val cashierName = shift.user?.name ?: userName ?: "Kasir"
        val rawReceipts = shift.totalReceipts ?: 0
        val totalSales = shift.totalSales
        val totalReceipts = if (rawReceipts > 0) rawReceipts else if (totalSales > 0) 1 else 0
        val totalPax = if ((shift.totalPax ?: 0) > 0) shift.totalPax!! else totalReceipts

        val discountVal = shift.discountAmount ?: 0.0
        val subtotalVal = shift.subtotal ?: (totalSales + discountVal)

        val cashSalesVal = shift.cashSales ?: 0.0
        val refundCashVal = shift.cashRefund ?: shift.refundCash
        val cancellationCashVal = shift.getCashCancellationAmount()
        val totalCancellationVal = shift.totalCancellation ?: shift.cancellationCash
        val netCashMovement = shift.cashIn - shift.cashOut
        val expectedCashVal = shift.getExpectedCashAmount()

        val out = mutableListOf<Byte>()
        out.addAll(INIT.toList())
        out.addAll(FONT_A.toList())
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
        out.addAll("Dicetak Oleh : $cashierName\n".toByteArray().toList())
        out.addAll("Tanggal      : ${formatShiftDay(shift.openedAt)}\n\n".toByteArray().toList())

        // Tamu & Resi
        out.addAll(drawTwoColumns("Jumlah Tamu", "$totalPax").toByteArray().toList())
        out.addAll(drawLine("-").toByteArray().toList())
        out.addAll(drawTwoColumns("Resi", "$totalReceipts").toByteArray().toList())
        out.addAll(drawLine("-").toByteArray().toList())
        out.addAll(drawTwoColumns("Pengembalian", formatNum(refundCashVal)).toByteArray().toList())
        out.addAll(drawLine("-").toByteArray().toList())

        // Penjualan & Subtotal
        val netSalesVal = maxOf(0.0, totalSales - totalCancellationVal)
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
        out.addAll(drawTwoColumns("  Penjualan nett", formatNum(netSalesVal)).toByteArray().toList())
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

        // Non-Cash Payment Methods Breakdown
        shift.paymentBreakdown?.forEach { pm ->
            val nameUpper = pm.name.trim().uppercase()
            if (nameUpper != "CASH" && nameUpper != "TUNAI" && nameUpper != "KAS") {
                out.addAll(BOLD_ON.toList())
                out.addAll(drawTwoColumns(pm.name, formatNum(pm.net)).toByteArray().toList())
                out.addAll(BOLD_OFF.toList())
                out.addAll(drawLine("-").toByteArray().toList())

                out.addAll(drawTwoColumns("  Penjualan", formatNum(pm.sales)).toByteArray().toList())
                out.addAll(drawTwoColumns("  Pengembalian", formatNum(pm.refund)).toByteArray().toList())
                out.addAll(drawTwoColumns("  Batal", formatNum(pm.voidAmount)).toByteArray().toList())
                out.addAll(drawLine("-").toByteArray().toList())
            }
        }

        out.addAll(BOLD_ON.toList())
        out.addAll(drawTwoColumns("Total Diharapkan", formatNum(expectedCashVal)).toByteArray().toList())
        out.addAll(BOLD_OFF.toList())

        out.addAll("\n\n\n\n\n".toByteArray().toList())

        return out.toByteArray()
    }

    fun formatDailySalesClosureReceipt(
        data: id.my.matahati.pos.ui.screen.report.DailySalesClosureData,
        savedOutletName: String? = null,
        userName: String? = null
    ): ByteArray {
        val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID")).apply {
            maximumFractionDigits = 0
        }
        fun formatNum(num: Double): String {
            return formatter.format(num)
        }

        fun formatDayText(dateStr: String): String {
            if (dateStr.isBlank()) return "-"
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val outputFormat = SimpleDateFormat("EEE, dd MMM yyyy", Locale("id", "ID"))
                val date = inputFormat.parse(dateStr)
                if (date != null) outputFormat.format(date) else dateStr
            } catch (e: Exception) {
                dateStr
            }
        }

        val currentPrintedTime = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.forLanguageTag("id-ID")).format(java.util.Date())
        val outletName = (savedOutletName ?: "IQOS MATAHATI TULUNGAGUNG").uppercase()
        val cashierName = userName ?: data.cashierNames.ifBlank { "Kasir" }

        val out = mutableListOf<Byte>()
        out.addAll(INIT.toList())
        out.addAll(FONT_A.toList())
        out.addAll(ALIGN_CENTER.toList())

        // Outlet Name & Title
        out.addAll(BOLD_ON.toList())
        out.addAll(SIZE_DOUBLE.toList())
        out.addAll("$outletName\n".toByteArray().toList())
        out.addAll(SIZE_NORMAL.toList())
        out.addAll("Penutupan Penjualan Harian\n\n".toByteArray().toList())
        out.addAll(BOLD_OFF.toList())

        // Header Metadata
        out.addAll(ALIGN_LEFT.toList())
        out.addAll("Tercetak     : $currentPrintedTime\n".toByteArray().toList())
        out.addAll("Kasir        : $cashierName\n".toByteArray().toList())
        out.addAll("Tanggal      : ${formatDayText(data.dateApi)}\n\n".toByteArray().toList())

        // Tamu & Resi
        out.addAll(drawTwoColumns("Jumlah Tamu", "${data.totalPaxs}").toByteArray().toList())
        out.addAll(drawLine("-").toByteArray().toList())
        out.addAll(drawTwoColumns("Resi", "${data.totalReceipts}").toByteArray().toList())
        out.addAll(drawLine("-").toByteArray().toList())
        out.addAll(drawTwoColumns("Pengembalian", formatNum(data.refundCash)).toByteArray().toList())
        out.addAll(drawLine("-").toByteArray().toList())

        // Penjualan & Subtotal
        out.addAll(BOLD_ON.toList())
        out.addAll(drawTwoColumns("Total Penjualan", formatNum(data.totalSales)).toByteArray().toList())
        out.addAll(BOLD_OFF.toList())
        out.addAll(drawLine("-").toByteArray().toList())

        val subtotalVal = if (data.subtotal > 0) data.subtotal else data.totalSales
        out.addAll(drawTwoColumns("  Subtotal", formatNum(subtotalVal)).toByteArray().toList())
        if (data.discountAmount > 0) {
            out.addAll(drawTwoColumns("  Diskon Bill", "-${formatNum(data.discountAmount)}").toByteArray().toList())
        }
        out.addAll(drawTwoColumns("  Penjualan nett", formatNum(data.netSales)).toByteArray().toList())
        out.addAll("\n".toByteArray().toList())

        // Kas Section
        out.addAll(BOLD_ON.toList())
        out.addAll(drawTwoColumns("Kas", formatNum(data.expectedCash)).toByteArray().toList())
        out.addAll(BOLD_OFF.toList())
        out.addAll(drawLine("-").toByteArray().toList())

        out.addAll(drawTwoColumns("  Kas Penjualan", formatNum(data.cashSales)).toByteArray().toList())
        out.addAll(drawTwoColumns("  Kas Pengembalian", formatNum(data.refundCash)).toByteArray().toList())
        out.addAll(drawTwoColumns("  Kas Pembatalan", formatNum(data.cancellationCash)).toByteArray().toList())
        out.addAll(drawTwoColumns("  Kas Masuk-Keluar", formatNum(data.netCashMovement)).toByteArray().toList())
        out.addAll(drawLine("-").toByteArray().toList())

        // Non-Cash Payment Methods Breakdown
        data.paymentBreakdownList.forEach { pm ->
            val nameUpper = pm.name.trim().uppercase()
            if (nameUpper != "CASH" && nameUpper != "TUNAI" && nameUpper != "KAS") {
                out.addAll(BOLD_ON.toList())
                out.addAll(drawTwoColumns(pm.name, formatNum(pm.net)).toByteArray().toList())
                out.addAll(BOLD_OFF.toList())
                out.addAll(drawLine("-").toByteArray().toList())

                out.addAll(drawTwoColumns("  Penjualan", formatNum(pm.sales)).toByteArray().toList())
                out.addAll(drawTwoColumns("  Pengembalian", formatNum(pm.refund)).toByteArray().toList())
                out.addAll(drawTwoColumns("  Batal", formatNum(pm.voidAmount)).toByteArray().toList())
                out.addAll(drawLine("-").toByteArray().toList())
            }
        }

        out.addAll(BOLD_ON.toList())
        out.addAll(drawTwoColumns("Total Diharapkan", formatNum(data.expectedCash)).toByteArray().toList())
        out.addAll(BOLD_OFF.toList())

        out.addAll("\n\n\n\n\n".toByteArray().toList())

        return out.toByteArray()
    }
}
