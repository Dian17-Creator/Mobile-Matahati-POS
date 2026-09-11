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
    var lastTransaction by mutableStateOf<TransactionData?>(null)
    var showReceiptDialog by mutableStateOf(false)

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
        if (cartItems.isEmpty()) {
            transactionError = "Cart kosong."
            return
        }
        if (orderType.isBlank()) {
            transactionError = "Silakan pilih In/Aw (Order Type) terlebih dahulu."
            return
        }
        if (orderType == "DINE_IN" && selectedTable.isBlank()) {
            transactionError = "Silakan pilih meja terlebih dahulu."
            return
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

        val parsedOutlet = nidOutlet?.toIntOrNull() ?: 1 // Default 1 if null to avoid 422

        val request = TransactionRequest(
            nidCustomer = selectedCustomer?.id,
            nidOutlet = parsedOutlet,
            nidPayment = selectedPayment.id,
            nidVoucher = null,
            customerName = selectedCustomer?.name,
            orderType = orderType,
            visitorCount = 1,
            tableName = selectedTable.ifBlank { null },
            discount = discount,
            tax = tax,
            paidAmount = paidAmount,
            details = details
        )

        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.submitTransaction(request)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        lastTransaction = body.data
                        showReceiptDialog = true
                    } else {
                        transactionError = body?.message ?: "Gagal membuat transaksi."
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

    fun closeReceiptDialog() {
        showReceiptDialog = false
        lastTransaction = null
        selectedTable = ""
    }
}
