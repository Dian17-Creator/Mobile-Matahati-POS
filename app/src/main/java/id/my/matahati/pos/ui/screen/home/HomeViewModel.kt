package id.my.matahati.pos.ui.screen.home

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.NewInstanceFactory.Companion.instance
import androidx.lifecycle.viewModelScope
import id.my.matahati.pos.data.DummyData
import id.my.matahati.pos.data.remote.RetrofitClient
import id.my.matahati.pos.model.Category
import id.my.matahati.pos.model.Customer
import id.my.matahati.pos.model.OrderTypeItem
import id.my.matahati.pos.model.PaymentMethod
import id.my.matahati.pos.model.Product
import id.my.matahati.pos.model.Voucher
import id.my.matahati.pos.model.TransactionRequest
import id.my.matahati.pos.model.TransactionDetailRequest
import id.my.matahati.pos.model.TransactionData
import id.my.matahati.pos.model.TransactionModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    val categories = mutableStateListOf<Category>()
    val products = mutableStateListOf<Product>()
    val customers = mutableStateListOf<Customer>()
    val vouchers = mutableStateListOf<Voucher>()
    val paymentMethods = mutableStateListOf<PaymentMethod>()
    private val _orderTypes = MutableStateFlow<List<OrderTypeItem>>(emptyList())

    val orderTypes: StateFlow<List<OrderTypeItem>> = _orderTypes

    // Transaction States
    var selectedTable by mutableStateOf("")
    var isSubmitting by mutableStateOf(false)
    var transactionError by mutableStateOf<String?>(null)
    var transactionSuccessMessage by mutableStateOf<String?>(null)
    var lastTransaction by mutableStateOf<TransactionData?>(null)
    var showReceiptDialog by mutableStateOf(false)

    // Kitchen Print States
    var showKitchenPrintDialog by mutableStateOf(false)
    var showSimulatedReceipt by mutableStateOf(false)
    var printChangesCount by mutableStateOf(0)
    var availableStations = mutableStateListOf<String>()
    var selectedStations = mutableStateListOf<String>()
    var receiptTickets = mutableMapOf<String, List<id.my.matahati.pos.model.CartItem>>()
    var currentPrintType by mutableStateOf("")
    var isPrintingFromHistory by mutableStateOf(false)
    private var itemsToPrint: List<id.my.matahati.pos.model.CartItem> = emptyList()

    // History States
    val transactionHistory = mutableStateListOf<TransactionModel>()
    var isHistoryLoading by mutableStateOf(false)
        private set

    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        fetchData()
    }

    fun fetchData() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                // Fetch Categories
                val catResponse = RetrofitClient.apiService.getCategories()
                if (catResponse.isSuccessful && catResponse.body()?.success == true) {
                    val dtos = catResponse.body()?.data ?: emptyList()
                    val fetchedCategories = mutableListOf<Category>(
                        Category(id = "all", name = "Semua Kategori", iconEmoji = "🏷️")
                    )
                    fetchedCategories.addAll(dtos.map { it.toCategory() })
                    categories.clear()
                    categories.addAll(fetchedCategories)
                }

                // Fetch Products
                val prodResponse = RetrofitClient.apiService.getProducts()
                if (prodResponse.isSuccessful && prodResponse.body()?.success == true) {
                    val dtos = prodResponse.body()?.data ?: emptyList()
                    val fetchedProducts = dtos.map { it.toProduct() }
                    products.clear()
                    products.addAll(fetchedProducts)
                }

                // Fetch Customers
                val custResponse = RetrofitClient.apiService.getCustomers()
                if (custResponse.isSuccessful && custResponse.body()?.success == true) {
                    val dtos = custResponse.body()?.data ?: emptyList()
                    val fetchedCustomers = dtos.map { it.toCustomer() }
                    customers.clear()
                    customers.addAll(fetchedCustomers)
                }

                // Fetch Vouchers
                val vouchResponse = RetrofitClient.apiService.getVouchers()
                if (vouchResponse.isSuccessful && vouchResponse.body()?.success == true) {
                    val dtos = vouchResponse.body()?.data ?: emptyList()
                    val fetchedVouchers = dtos.map { it.toVoucher() }
                    vouchers.clear()
                    vouchers.addAll(fetchedVouchers)
                }

                // Fetch Payment Methods
                val payResponse = RetrofitClient.apiService.getPaymentMethods()
                if (payResponse.isSuccessful && payResponse.body()?.success == true) {
                    val dtos = payResponse.body()?.data ?: emptyList()
                    val fetchedPayments = mutableListOf<PaymentMethod>(
                        PaymentMethod(id = "all", name = "Semua Tipe Pembayaran")
                    )
                    fetchedPayments.addAll(dtos.map { it.toPaymentMethod() })
                    paymentMethods.clear()
                    paymentMethods.addAll(fetchedPayments)
                }
            } catch (e: Exception) {
                errorMessage = "Gagal memuat data dari server: ${e.localizedMessage}"
                // Fallback to dummy data if network fails so POS is still usable
                if (categories.isEmpty()) {
                    categories.clear()
                    categories.addAll(DummyData.categories)
                }
                if (products.isEmpty()) {
                    products.clear()
                    products.addAll(DummyData.products)
                }
                if (customers.isEmpty()) {
                    customers.clear()
                    customers.add(Customer(id = "1", name = "A.N DITO", phone = "", email = "", address = ""))
                }
            } finally {
                isLoading = false
            }
        }
    }

    fun loadOrderTypes() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getOrderTypes()

                if (response.isSuccessful) {
                    val body = response.body()

                    if (body != null && body.success) {
                        _orderTypes.value = body.data
                    }
                }

            } catch (e: Exception) {
                Log.e("OrderType", "Gagal mengambil order type", e)
            }
        }
    }

    fun submitTransaction(
        cartItems: List<id.my.matahati.pos.model.CartItem>,
        orderType: String,
        selectedCustomer: Customer?,
        selectedPayment: PaymentMethod,
        discount: Double,
        tax: Double,
        paidAmount: Double,
        nidOutlet: String?
    ) {
        executeTransaction(
            cartItems = cartItems,
            orderType = orderType,
            selectedCustomer = selectedCustomer,
            selectedPayment = selectedPayment,
            discount = discount,
            tax = tax,
            paidAmount = paidAmount,
            nidOutlet = nidOutlet,
            status = null,
            cancelNote = null
        )
    }

    fun submitCancelTransaction(
        cartItems: List<id.my.matahati.pos.model.CartItem>,
        orderType: String,
        selectedCustomer: Customer?,
        cancelNote: String,
        nidOutlet: String?
    ) {
        executeTransaction(
            cartItems = cartItems,
            orderType = orderType,
            selectedCustomer = selectedCustomer,
            selectedPayment = null,
            discount = 0.0,
            tax = 0.0,
            paidAmount = 0.0,
            nidOutlet = nidOutlet,
            status = "CANCELLED",
            cancelNote = cancelNote
        )
    }

    private fun executeTransaction(
        cartItems: List<id.my.matahati.pos.model.CartItem>,
        orderType: String,
        selectedCustomer: Customer?,
        selectedPayment: PaymentMethod?,
        discount: Double,
        tax: Double,
        paidAmount: Double,
        nidOutlet: String?,
        status: String?,
        cancelNote: String?
    ) {
        if (status != "CANCELLED" && cartItems.isEmpty()) {
            transactionError = "Cart kosong."
            return
        }
        if (status != "CANCELLED") {
            if (orderType.isBlank()) {
                transactionError = "Silakan pilih In/Aw (Order Type) terlebih dahulu."
                return
            }
            if (orderType == "DINE_IN" && selectedTable.isBlank()) {
                transactionError = "Silakan pilih meja terlebih dahulu."
                return
            }
            if (selectedPayment == null) {
                transactionError = "Silakan pilih metode pembayaran."
                return
            }
        }

        isSubmitting = true
        transactionError = null

        val details = cartItems.map {
            TransactionDetailRequest(
                productId = it.product.id,
                quantity = it.quantity,
                note = it.note.ifBlank { null }
            )
        }

        val parsedOutlet = nidOutlet?.toIntOrNull() ?: 1

        val request = TransactionRequest(
            nidCustomer = selectedCustomer?.id,
            nidOutlet = parsedOutlet,
            nidPayment = selectedPayment?.id ?: "1", // Fallback to 1 if cancelled
            nidVoucher = null,
            customerName = selectedCustomer?.name,
            orderType = orderType.ifBlank { "TAKE_AWAY" },
            visitorCount = 1,
            tableName = selectedTable.ifBlank { null },
            discount = discount,
            tax = tax,
            paidAmount = paidAmount,
            status = status,
            cancelNote = cancelNote,
            details = details
        )

        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.submitTransaction(request)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        if (status == "CANCELLED") {
                            // If cancelled, just reset directly without receipt
                            closeReceiptDialog()
                            transactionSuccessMessage = "Pesanan berhasil dibatalkan dan dicatat."
                        } else {
                            lastTransaction = body.data
                            showReceiptDialog = true
                        }
                    } else {
                        transactionError = body?.message ?: "Gagal memproses transaksi."
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    transactionError = if (!errorBody.isNullOrBlank()) {
                        try {
                            val json = org.json.JSONObject(errorBody)
                            var parsedError = json.optString("message", "Error: ${response.code()}")
                            
                            // Parse detailed Laravel validation errors
                            if (json.has("errors")) {
                                val errorsObj = json.getJSONObject("errors")
                                val errorList = mutableListOf<String>()
                                val keys = errorsObj.keys()
                                while (keys.hasNext()) {
                                    val key = keys.next()
                                    val errorArray = errorsObj.getJSONArray(key)
                                    for (i in 0 until errorArray.length()) {
                                        errorList.add(errorArray.getString(i))
                                    }
                                }
                                if (errorList.isNotEmpty()) {
                                    parsedError = errorList.joinToString("\n")
                                }
                            }
                            parsedError
                        } catch (e: Exception) {
                            "Error: ${response.code()}\nRaw: $errorBody"
                        }
                    } else {
                        "Error: ${response.code()}"
                    }
                }
            } catch (e: Exception) {
                transactionError = "Koneksi gagal: ${e.localizedMessage}"
            } finally {
                isSubmitting = false
            }
        }
    }

    fun clearTransactionError() {
        transactionError = null
    }

    fun clearTransactionSuccess() {
        transactionSuccessMessage = null
    }

    fun closeReceiptDialog() {
        showReceiptDialog = false
        lastTransaction = null
        selectedTable = ""
    }

    // Kitchen Printing logic
    fun openKitchenPrintDialog(items: List<id.my.matahati.pos.model.CartItem>, isHistory: Boolean = false) {
        if (items.isEmpty()) return
        
        itemsToPrint = items
        isPrintingFromHistory = isHistory
        
        var totalChanges = 0
        val stations = mutableSetOf<String>()
        
        items.forEach { item ->
            val delta = if (isHistory) item.quantity else (item.quantity - item.sentQuantity).coerceAtLeast(0)
            totalChanges += delta
            stations.add(item.product.stationName)
        }
        
        printChangesCount = totalChanges
        availableStations.clear()
        availableStations.addAll(stations)
        selectedStations.clear()
        selectedStations.addAll(stations)
        
        showKitchenPrintDialog = true
    }

    fun openKitchenPrintDialogFromHistory(transaction: TransactionModel) {
        val items = transaction.details?.map { detail ->
            id.my.matahati.pos.model.CartItem(
                product = Product(
                    id = detail.productId,
                    name = detail.productName,
                    price = detail.price.toDoubleOrNull() ?: 0.0,
                    categoryId = "1",
                    stock = 0,
                    stationName = if (detail.productName.lowercase().contains("tea") || 
                                     detail.productName.lowercase().contains("kopi") || 
                                     detail.productName.lowercase().contains("ice")) "BAR" else "DAPUR"
                ),
                quantity = detail.quantity,
                note = detail.note ?: "",
                sentQuantity = 0 // History is always "new" to the printer
            )
        } ?: emptyList()
        
        openKitchenPrintDialog(items, isHistory = true)
    }

    fun onConfirmKitchenPrint(
        type: String,
        onUpdateActiveCart: (List<id.my.matahati.pos.model.CartItem>) -> Unit
    ) {
        val tickets = mutableMapOf<String, MutableList<id.my.matahati.pos.model.CartItem>>()
        val itemsToProcess = itemsToPrint.toMutableList()
        
        if (type == "PERUBAHAN") {
            itemsToProcess.forEachIndexed { index, item ->
                val delta = if (isPrintingFromHistory) item.quantity else (item.quantity - item.sentQuantity).coerceAtLeast(0)
                if (delta > 0 && selectedStations.contains(item.product.stationName)) {
                    val station = item.product.stationName
                    if (!tickets.containsKey(station)) tickets[station] = mutableListOf()
                    tickets[station]?.add(item.copy(quantity = delta))
                    
                    if (!isPrintingFromHistory) {
                        itemsToProcess[index] = item.copy(sentQuantity = item.quantity)
                    }
                }
            }
            if (!isPrintingFromHistory) {
                onUpdateActiveCart(itemsToProcess)
            }
        } else if (type == "ULANG") {
            itemsToProcess.forEachIndexed { index, item ->
                if (selectedStations.contains(item.product.stationName)) {
                    val station = item.product.stationName
                    if (!tickets.containsKey(station)) tickets[station] = mutableListOf()
                    tickets[station]?.add(item)
                    
                    if (!isPrintingFromHistory) {
                        itemsToProcess[index] = item.copy(sentQuantity = item.quantity)
                    }
                }
            }
            if (!isPrintingFromHistory) {
                onUpdateActiveCart(itemsToProcess)
            }
        }
        
        if (tickets.isNotEmpty()) {
            receiptTickets = tickets.mapValues { it.value.toList() }.toMutableMap()
            currentPrintType = if (type == "PERUBAHAN") "PERUBAHAN PESANAN" else "CETAK ULANG PESANAN"
            showKitchenPrintDialog = false
            showSimulatedReceipt = true
        } else {
            showKitchenPrintDialog = false
        }
    }

    fun toggleStationSelection(station: String) {
        if (selectedStations.contains(station)) {
            selectedStations.remove(station)
        } else {
            selectedStations.add(station)
        }
    }

    fun closeSimulatedReceipt() {
        showSimulatedReceipt = false
        receiptTickets.clear()
    }

    fun fetchTransactionHistory(
        startDate: String? = null,
        endDate: String? = null,
        search: String? = null,
        nidOutlet: String? = null
    ) {
        viewModelScope.launch {
            isHistoryLoading = true
            try {
                val response = RetrofitClient.apiService.getTransactions(
                    startDate = startDate,
                    endDate = endDate,
                    search = search,
                    outletId = nidOutlet
                )
                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()?.data ?: emptyList()
                    transactionHistory.clear()
                    transactionHistory.addAll(data)
                } else {
                    Log.e("TransactionHistory", "Gagal: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("TransactionHistory", "Error: ${e.message}", e)
            } finally {
                isHistoryLoading = false
            }
        }
    }
}
