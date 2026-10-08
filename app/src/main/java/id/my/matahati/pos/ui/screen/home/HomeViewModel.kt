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
import id.my.matahati.pos.data.remote.RegionRetrofitClient
import id.my.matahati.pos.data.remote.Province
import id.my.matahati.pos.data.remote.Regency
import id.my.matahati.pos.data.remote.District
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
    val customerTypes = mutableStateListOf<id.my.matahati.pos.model.CustomerTypeDto>()
    val vouchers = mutableStateListOf<Voucher>()
    val paymentMethods = mutableStateListOf<PaymentMethod>()
    private val _orderTypes = MutableStateFlow<List<OrderTypeItem>>(emptyList())

    val orderTypes: StateFlow<List<OrderTypeItem>> = _orderTypes

    // Region / Dependent Dropdown States
    val provinces = mutableStateListOf<Province>()
    val regencies = mutableStateListOf<Regency>()
    val districts = mutableStateListOf<District>()

    var isProvincesLoading by mutableStateOf(false)
        private set
    var isRegenciesLoading by mutableStateOf(false)
        private set
    var isDistrictsLoading by mutableStateOf(false)
        private set

    var regionError by mutableStateOf<String?>(null)
        private set

    private var provincesJob: kotlinx.coroutines.Job? = null
    private var regenciesJob: kotlinx.coroutines.Job? = null
    private var districtsJob: kotlinx.coroutines.Job? = null

    // Transaction States
    var selectedTable by mutableStateOf("")
    var isSubmitting by mutableStateOf(false)
    var transactionError by mutableStateOf<String?>(null)
    var transactionSuccessMessage by mutableStateOf<String?>(null)
    var lastTransaction by mutableStateOf<TransactionData?>(null)
    var showReceiptDialog by mutableStateOf(false)
    var showShiftNotStartedDialog by mutableStateOf(false)
    var isOrderTypeSelectedByUser by mutableStateOf(true)

    // Kitchen & Check Print States
    var showKitchenPrintDialog by mutableStateOf(false)
    var showCheckPrintDialog by mutableStateOf(false)
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

    // Printer States
    var isPrinting by mutableStateOf(false)
    var printerError by mutableStateOf<String?>(null)
    var showPrinterSelection by mutableStateOf(false)
    val pairedDevices = mutableStateListOf<android.bluetooth.BluetoothDevice>()
    var selectedPrinterAddress by mutableStateOf<String?>(null)
        private set

    // Held Orders States (Backend DRAFT Status)
    val heldOrders = mutableStateListOf<TransactionModel>()
    var showHeldOrdersDialog by mutableStateOf(false)
    var currentDraftId by mutableStateOf<String?>(null)
    var showCashierConfirmDialog by mutableStateOf(false)
    var showPaymentScreen by mutableStateOf(false)
    var pendingShowReceipt by mutableStateOf(false)

    // Served By States
    val servedByUsers = mutableStateListOf<id.my.matahati.pos.model.PosUser>()
    var selectedServedBy by mutableStateOf<id.my.matahati.pos.model.PosUser?>(null)
    var showServedByDialog by mutableStateOf(false)

    fun loadServedByUsers(
        outletId: Int?,
        currentUserId: String? = null,
        currentUserName: String? = null
    ) {
        servedByUsers.clear()
        viewModelScope.launch {
            try {
                val validOutlet = if (outletId != null && outletId > 0) outletId else 1
                val response = RetrofitClient.apiService.getPosUsers(validOutlet)
                if (response.isSuccessful && response.body()?.success == true) {
                    servedByUsers.addAll(response.body()?.data ?: emptyList())
                    
                    // Prioritize matching current logged-in user
                    val matchedUser = servedByUsers.find { posUser ->
                        (currentUserId != null && posUser.nidUser.toString() == currentUserId) ||
                        (currentUserName != null && posUser.name.equals(currentUserName, ignoreCase = true))
                    }
                    
                    selectedServedBy = matchedUser ?: servedByUsers.firstOrNull()
                }
            } catch (e: Exception) {
                Log.e("ServedBy", "Gagal mengambil daftar dilayani oleh", e)
            }
        }
    }

    fun fetchHeldOrders(nidOutlet: String?) {
        heldOrders.clear()
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getTransactions(outletId = nidOutlet)
                if (response.isSuccessful && response.body()?.success == true) {
                    val allTrx = response.body()?.data ?: emptyList()
                    val drafts = allTrx.filter { it.status.uppercase() == "DRAFT" }
                    heldOrders.addAll(drafts)
                }
            } catch (e: Exception) {
                Log.e("HeldOrders", "Error fetching DRAFT orders: ${e.message}", e)
            }
        }
    }

    fun removeHeldOrderLocal(id: String) {
        heldOrders.removeAll { it.id == id }
    }

    fun deleteHeldOrder(context: android.content.Context? = null, id: String, nidOutlet: String?) {
        removeHeldOrderLocal(id)
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.deleteTransaction(id)
                if (response.isSuccessful) {
                    Log.d("HeldOrders", "Draft order $id successfully deleted.")
                } else {
                    Log.e("HeldOrders", "Failed to delete draft order $id: code=${response.code()} error=${response.errorBody()?.string()}")
                }
                fetchHeldOrders(nidOutlet)
            } catch (e: Exception) {
                Log.e("HeldOrders", "Error deleting draft: ${e.message}", e)
            }
        }
    }

    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        // Data loading is handled by LaunchedEffect(nidOutlet) in HomeScreen with proper outletId
    }

    fun loadPrinterSettings(context: android.content.Context) {
        val prefs = context.getSharedPreferences("printer_prefs", android.content.Context.MODE_PRIVATE)
        selectedPrinterAddress = prefs.getString("selected_printer_address", null)
    }

    fun savePrinterSettings(context: android.content.Context, address: String) {
        val prefs = context.getSharedPreferences("printer_prefs", android.content.Context.MODE_PRIVATE)
        prefs.edit().putString("selected_printer_address", address).apply()
        selectedPrinterAddress = address
    }

    fun openPrinterSelection(printerManager: id.my.matahati.pos.data.printer.BluetoothPrinterManager) {
        pairedDevices.clear()
        pairedDevices.addAll(printerManager.getPairedDevices())
        showPrinterSelection = true
    }

    fun selectPrinter(context: android.content.Context, deviceAddress: String) {
        savePrinterSettings(context, deviceAddress)
        showPrinterSelection = false
    }

    val savedPrinters = mutableStateListOf<id.my.matahati.pos.model.LocalPrinter>()

    fun loadSavedPrinters(context: android.content.Context) {
        savedPrinters.clear()
        savedPrinters.addAll(id.my.matahati.pos.data.repository.PrinterRepository(context).getPrinters())
    }

    fun printReceipt(
        context: android.content.Context,
        data: TransactionData,
        cashierName: String,
        savedOutletName: String? = null
    ) {
        val repo = id.my.matahati.pos.data.repository.PrinterRepository(context)
        val printers = repo.getPrinters()
        val receiptPrinter = printers.find { it.role == id.my.matahati.pos.model.PrinterRole.RECEIPT }

        if (receiptPrinter == null) {
            printerError = "Printer Struk Kasir (RECEIPT) belum dikonfigurasi di Pengaturan."
            return
        }

        isPrinting = true
        printerError = null

        viewModelScope.launch {
            val connectionManager = id.my.matahati.pos.data.printer.PrinterConnectionManager(context)
            val formatter = id.my.matahati.pos.data.printer.EscPosFormatter()
            val receiptBytes = formatter.formatReceipt(data, cashierName, savedOutletName, isOrderTypeSelectedByUser)
            
            val result = connectionManager.printData(receiptPrinter, receiptBytes)
            if (result.isFailure) {
                printerError = "Gagal mencetak struk: ${result.exceptionOrNull()?.message ?: "Cek koneksi printer"}"
            }
            isPrinting = false
        }
    }

    fun printCheckReceipt(
        context: android.content.Context,
        cartItems: List<id.my.matahati.pos.model.CartItem>,
        orderType: String,
        cashierName: String,
        customerName: String? = null,
        tableName: String? = null,
        discountAmount: Double = 0.0,
        taxAmount: Double = 0.0,
        paxCount: Int = 1
    ) {
        val repo = id.my.matahati.pos.data.repository.PrinterRepository(context)
        val printers = repo.getPrinters()
        val receiptPrinter = printers.find { it.role == id.my.matahati.pos.model.PrinterRole.RECEIPT }

        if (receiptPrinter == null) {
            printerError = "Printer Struk Kasir (RECEIPT) belum dikonfigurasi di Pengaturan."
            return
        }

        isPrinting = true
        printerError = null

        viewModelScope.launch {
            val connectionManager = id.my.matahati.pos.data.printer.PrinterConnectionManager(context)
            val formatter = id.my.matahati.pos.data.printer.EscPosFormatter()
            val checkBytes = formatter.formatCheckPrint(
                cartItems = cartItems,
                orderType = orderType,
                cashierName = cashierName,
                customerName = customerName,
                tableName = tableName,
                discountAmount = discountAmount,
                taxAmount = taxAmount,
                paxCount = paxCount
            )

            val result = connectionManager.printData(receiptPrinter, checkBytes)
            if (result.isFailure) {
                printerError = "Gagal mencetak periksa: ${result.exceptionOrNull()?.message ?: "Cek koneksi printer"}"
            } else {
                closeCheckPrintDialog()
            }
            isPrinting = false
        }
    }

    fun openCheckPrintDialog() {
        showCheckPrintDialog = true
    }

    fun closeCheckPrintDialog() {
        showCheckPrintDialog = false
    }

    fun testPrint(context: android.content.Context) {
        val address = selectedPrinterAddress
        if (address == null) {
            printerError = "Printer belum dipilih."
            return
        }

        isPrinting = true
        printerError = null

        viewModelScope.launch {
            val printerManager = id.my.matahati.pos.data.printer.BluetoothPrinterManager(context)
            val formatter = id.my.matahati.pos.data.printer.EscPosFormatter()
            val testBytes = formatter.formatTestPrint()
            
            val result = printerManager.printData(address, testBytes)
            if (result.isFailure) {
                printerError = "Test print gagal: ${result.exceptionOrNull()?.message}"
            }
            isPrinting = false
        }
    }

    fun printKitchenTickets(context: android.content.Context, tickets: Map<String, List<id.my.matahati.pos.model.CartItem>>) {
        val repo = id.my.matahati.pos.data.repository.PrinterRepository(context)
        val printers = repo.getPrinters()
        
        val kitchenPrinter = printers.find { it.role == id.my.matahati.pos.model.PrinterRole.KITCHEN }
        val barPrinter = printers.find { it.role == id.my.matahati.pos.model.PrinterRole.BAR }

        val hasDapurItems = tickets["DAPUR"]?.isNotEmpty() == true
        val hasBarItems = tickets["BAR"]?.isNotEmpty() == true

        if (hasDapurItems && kitchenPrinter == null) {
            printerError = "Printer Dapur (KITCHEN) belum dikonfigurasi di Pengaturan."
            return
        }
        if (hasBarItems && barPrinter == null) {
            printerError = "Printer Bar (BAR) belum dikonfigurasi di Pengaturan."
            return
        }

        if (tickets.isEmpty()) {
            printerError = "Tidak ada tiket untuk dicetak."
            return
        }

        isPrinting = true
        printerError = null

        viewModelScope.launch {
            val connectionManager = id.my.matahati.pos.data.printer.PrinterConnectionManager(context)
            val formatter = id.my.matahati.pos.data.printer.EscPosFormatter()

            var anyFailure = false
            val errors = mutableListOf<String>()

            tickets.forEach { (station, items) ->
                if (items.isNotEmpty()) {
                    val targetPrinter = if (station.uppercase() == "BAR") barPrinter else kitchenPrinter
                    if (targetPrinter != null) {
                        val singleStationTicket = mapOf(station to items)
                        val bytes = formatter.formatKitchenTicket(singleStationTicket)
                        val result = connectionManager.printData(targetPrinter, bytes)
                        if (result.isFailure) {
                            anyFailure = true
                            errors.add("Station $station: ${result.exceptionOrNull()?.message}")
                        }
                    } else {
                        anyFailure = true
                        errors.add("Printer untuk station $station belum diatur.")
                    }
                }
            }

            if (anyFailure) {
                printerError = "Gagal mencetak pesanan: ${errors.joinToString(", ")}"
            }
            isPrinting = false
        }
    }

    fun fetchProducts(nidOutlet: String?, customerId: String? = null) {
        viewModelScope.launch {
            try {
                val prodResponse = RetrofitClient.apiService.getProducts(
                    outletId = nidOutlet,
                    customerId = customerId
                )
                if (prodResponse.isSuccessful && prodResponse.body()?.success == true) {
                    val dtos = prodResponse.body()?.data ?: emptyList()
                    val fetchedProducts = dtos.map { it.toProduct() }
                    products.clear()
                    products.addAll(fetchedProducts)
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Gagal memuat produk: ${e.message}", e)
            }
        }
    }

    fun fetchData(nidOutlet: String? = null, customerId: String? = null) {
        products.clear()
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val outletIdInt = nidOutlet?.toIntOrNull()

                // Fetch Categories
                val catResponse = RetrofitClient.apiService.getCategories(outletId = outletIdInt)
                if (catResponse.isSuccessful && catResponse.body()?.success == true) {
                    val dtos = catResponse.body()?.data ?: emptyList()
                    val fetchedCategories = mutableListOf<Category>(
                        Category(id = "all", name = "Semua Kategori")
                    )
                    fetchedCategories.addAll(dtos.map { it.toCategory() })
                    categories.clear()
                    categories.addAll(fetchedCategories)
                }

                // Fetch Products per Outlet
                val prodResponse = RetrofitClient.apiService.getProducts(outletId = nidOutlet, customerId = customerId)
                if (prodResponse.isSuccessful && prodResponse.body()?.success == true) {
                    val dtos = prodResponse.body()?.data ?: emptyList()
                    val fetchedProducts = dtos.map { it.toProduct() }
                    products.clear()
                    products.addAll(fetchedProducts)
                }

                // Fetch Customers
                val custResponse = RetrofitClient.apiService.getCustomers(outletId = outletIdInt)
                if (custResponse.isSuccessful && custResponse.body()?.success == true) {
                    val dtos = custResponse.body()?.data ?: emptyList()
                    val fetchedCustomers = dtos.map { it.toCustomer() }
                    customers.clear()
                    customers.addAll(fetchedCustomers)
                }

                // Fetch Customer Types
                try {
                    val custTypeResponse = RetrofitClient.apiService.getCustomerTypes(outletId = outletIdInt)
                    if (custTypeResponse.isSuccessful && custTypeResponse.body()?.success == true) {
                        val types = custTypeResponse.body()?.data ?: emptyList()
                        customerTypes.clear()
                        customerTypes.addAll(types)
                    }
                } catch (e: Exception) {
                    Log.e("HomeViewModel", "Gagal memuat customer types: ${e.message}")
                }

                // Fetch Vouchers
                fetchVouchers()

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

    private val defaultProvinces = listOf(
        Province("35", "JAWA TIMUR"),
        Province("32", "JAWA BARAT"),
        Province("33", "JAWA TENGAH"),
        Province("31", "DKI JAKARTA"),
        Province("36", "BANTEN"),
        Province("34", "DI YOGYAKARTA"),
        Province("51", "BALI"),
        Province("12", "SUMATERA UTARA"),
        Province("16", "SUMATERA SELATAN"),
        Province("14", "RIAU"),
        Province("18", "LAMPUNG"),
        Province("64", "KALIMANTAN TIMUR"),
        Province("61", "KALIMANTAN BARAT"),
        Province("63", "KALIMANTAN SELATAN"),
        Province("73", "SULAWESI SELATAN"),
        Province("71", "SULAWESI UTARA"),
        Province("11", "ACEH"),
        Province("13", "SUMATERA BARAT"),
        Province("15", "JAMBI"),
        Province("17", "BENGKULU"),
        Province("19", "KEPULAUAN BANGKA BELITUNG"),
        Province("21", "KEPULAUAN RIAU"),
        Province("52", "NUSA TENGGARA BARAT"),
        Province("53", "NUSA TENGGARA TIMUR"),
        Province("62", "KALIMANTAN TENGAH"),
        Province("65", "KALIMANTAN UTARA"),
        Province("72", "SULAWESI TENGAH"),
        Province("74", "SULAWESI TENGGARA"),
        Province("75", "GORONTALO"),
        Province("76", "SULAWESI BARAT"),
        Province("81", "MALUKU"),
        Province("82", "MALUKU UTARA"),
        Province("91", "PAPUA BARAT"),
        Province("94", "PAPUA")
    )

    private val jawaTimurRegencies = listOf(
        Regency("3501", "35", "KABUPATEN PACITAN"),
        Regency("3502", "35", "KABUPATEN PONOROGO"),
        Regency("3503", "35", "KABUPATEN TRENGGALEK"),
        Regency("3504", "35", "KABUPATEN TULUNGAGUNG"),
        Regency("3505", "35", "KABUPATEN BLITAR"),
        Regency("3506", "35", "KABUPATEN KEDIRI"),
        Regency("3507", "35", "KABUPATEN MALANG"),
        Regency("3508", "35", "KABUPATEN LUMAJANG"),
        Regency("3509", "35", "KABUPATEN JEMBER"),
        Regency("3510", "35", "KABUPATEN BANYUWANGI"),
        Regency("3511", "35", "KABUPATEN BONDOWOSO"),
        Regency("3512", "35", "KABUPATEN SITUBONDO"),
        Regency("3513", "35", "KABUPATEN PROBOLINGGO"),
        Regency("3514", "35", "KABUPATEN PASURUAN"),
        Regency("3515", "35", "KABUPATEN SIDOARJO"),
        Regency("3516", "35", "KABUPATEN MOJOKERTO"),
        Regency("3517", "35", "KABUPATEN JOMBANG"),
        Regency("3518", "35", "KABUPATEN NGANJUK"),
        Regency("3519", "35", "KABUPATEN MADIUN"),
        Regency("3520", "35", "KABUPATEN MAGETAN"),
        Regency("3521", "35", "KABUPATEN NGAWI"),
        Regency("3522", "35", "KABUPATEN BOJONEGORO"),
        Regency("3523", "35", "KABUPATEN TUBAN"),
        Regency("3524", "35", "KABUPATEN LAMONGAN"),
        Regency("3525", "35", "KABUPATEN GRESIK"),
        Regency("3526", "35", "KABUPATEN BANGKALAN"),
        Regency("3527", "35", "KABUPATEN SAMPANG"),
        Regency("3528", "35", "KABUPATEN PAMEKASAN"),
        Regency("3529", "35", "KABUPATEN SUMENEP"),
        Regency("3571", "35", "KOTA KEDIRI"),
        Regency("3572", "35", "KOTA BLITAR"),
        Regency("3573", "35", "KOTA MALANG"),
        Regency("3574", "35", "KOTA PROBOLINGGO"),
        Regency("3575", "35", "KOTA PASURUAN"),
        Regency("3576", "35", "KOTA MOJOKERTO"),
        Regency("3577", "35", "KOTA MADIUN"),
        Regency("3578", "35", "KOTA SURABAYA"),
        Regency("3579", "35", "KOTA BATU")
    )

    fun fetchProvinces() {
        if (provinces.isNotEmpty()) return
        provincesJob?.cancel()
        provincesJob = viewModelScope.launch {
            isProvincesLoading = true
            regionError = null
            try {
                Log.d("RegionFetch", "Fetching provinces from Primary Emsifa API...")
                val list = try {
                    RegionRetrofitClient.regionApiService.getProvinces()
                } catch (e1: Exception) {
                    if (e1 is kotlinx.coroutines.CancellationException) throw e1
                    Log.w("RegionFetch", "Primary Emsifa provinces failed: ${e1.message}, trying Secondary Ibnux API")
                    RegionRetrofitClient.secondaryRegionApiService.getProvinces()
                }
                provinces.clear()
                if (list.isNotEmpty()) {
                    provinces.addAll(list)
                } else {
                    provinces.addAll(defaultProvinces)
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                Log.e("RegionFetch", "Gagal load provinsi dari kedua API: ${e.message}", e)
                regionError = "Gagal memuat daftar provinsi: ${e.localizedMessage}"
                provinces.clear()
                provinces.addAll(defaultProvinces)
            } finally {
                isProvincesLoading = false
            }
        }
    }

    private val jawaTengahRegencies = listOf(
        Regency("3374", "33", "KOTA SEMARANG"),
        Regency("3322", "33", "KABUPATEN SEMARANG"),
        Regency("3371", "33", "KOTA MAGELANG"),
        Regency("3308", "33", "KABUPATEN MAGELANG"),
        Regency("3372", "33", "KOTA SURAKARTA"),
        Regency("3309", "33", "KABUPATEN BOYOLALI"),
        Regency("3310", "33", "KABUPATEN KLATEN"),
        Regency("3311", "33", "KABUPATEN SUKOHARJO"),
        Regency("3312", "33", "KABUPATEN WONOGIRI"),
        Regency("3313", "33", "KABUPATEN KARANGANYAR"),
        Regency("3314", "33", "KABUPATEN SRAGEN"),
        Regency("3315", "33", "KABUPATEN GROBOGAN"),
        Regency("3316", "33", "KABUPATEN BLORA"),
        Regency("3317", "33", "KABUPATEN REMBANG"),
        Regency("3318", "33", "KABUPATEN PATI"),
        Regency("3319", "33", "KABUPATEN KUDUS"),
        Regency("3320", "33", "KABUPATEN JEPARA"),
        Regency("3321", "33", "KABUPATEN DEMAK"),
        Regency("3323", "33", "KABUPATEN TEMANGGUNG"),
        Regency("3324", "33", "KABUPATEN KENDAL"),
        Regency("3325", "33", "KABUPATEN BATANG"),
        Regency("3326", "33", "KABUPATEN PEKALONGAN"),
        Regency("3375", "33", "KOTA PEKALONGAN"),
        Regency("3327", "33", "KABUPATEN PEMALANG"),
        Regency("3328", "33", "KABUPATEN TEGAL"),
        Regency("3376", "33", "KOTA TEGAL"),
        Regency("3329", "33", "KABUPATEN BREBES"),
        Regency("3302", "33", "KABUPATEN BANYUMAS"),
        Regency("3303", "33", "KABUPATEN PURBALINGGA"),
        Regency("3304", "33", "KABUPATEN BANJARNEGARA"),
        Regency("3305", "33", "KABUPATEN KEBUMEN"),
        Regency("3306", "33", "KABUPATEN PURWOREJO"),
        Regency("3373", "33", "KOTA SALATIGA")
    )

    private val jawaBaratRegencies = listOf(
        Regency("3273", "32", "KOTA BANDUNG"),
        Regency("3204", "32", "KABUPATEN BANDUNG"),
        Regency("3217", "32", "KABUPATEN BANDUNG BARAT"),
        Regency("3277", "32", "KOTA CIMAHI"),
        Regency("3271", "32", "KOTA BOGOR"),
        Regency("3201", "32", "KABUPATEN BOGOR"),
        Regency("3276", "32", "KOTA DEPOK"),
        Regency("3275", "32", "KOTA BEKASI"),
        Regency("3216", "32", "KABUPATEN BEKASI"),
        Regency("3215", "32", "KABUPATEN KARAWANG"),
        Regency("3214", "32", "KABUPATEN PURWAKARTA"),
        Regency("3213", "32", "KABUPATEN SUBANG"),
        Regency("3202", "32", "KABUPATEN SUKABUMI"),
        Regency("3272", "32", "KOTA SUKABUMI"),
        Regency("3203", "32", "KABUPATEN CIANJUR"),
        Regency("3205", "32", "KABUPATEN GARUT"),
        Regency("3206", "32", "KABUPATEN TASIKMALAYA"),
        Regency("3278", "32", "KOTA TASIKMALAYA"),
        Regency("3207", "32", "KABUPATEN CIAMIS"),
        Regency("3279", "32", "KOTA BANJAR"),
        Regency("3218", "32", "KABUPATEN PANGANDARAN"),
        Regency("3208", "32", "KABUPATEN KUNINGAN"),
        Regency("3209", "32", "KABUPATEN CIREBON"),
        Regency("3274", "32", "KOTA CIREBON"),
        Regency("3210", "32", "KABUPATEN MAJALENGKA"),
        Regency("3212", "32", "KABUPATEN INDRAMAYU"),
        Regency("3211", "32", "KABUPATEN SUMEDANG")
    )

    private val dkiJakartaRegencies = listOf(
        Regency("3174", "31", "KOTA JAKARTA SELATAN"),
        Regency("3172", "31", "KOTA JAKARTA TIMUR"),
        Regency("3173", "31", "KOTA JAKARTA PUSAT"),
        Regency("3175", "31", "KOTA JAKARTA BARAT"),
        Regency("3171", "31", "KOTA JAKARTA UTARA"),
        Regency("3101", "31", "KABUPATEN KEPULAUAN SERIBU")
    )

    private val jogjaRegencies = listOf(
        Regency("3471", "34", "KOTA YOGYAKARTA"),
        Regency("3404", "34", "KABUPATEN SLEMAN"),
        Regency("3402", "34", "KABUPATEN BANTUL"),
        Regency("3403", "34", "KABUPATEN GUNUNGKIDUL"),
        Regency("3401", "34", "KABUPATEN KULON PROGO")
    )

    private fun getFallbackRegencies(provinceId: String): List<Regency> {
        val cleanId = provinceId.replace(".", "")
        return when (cleanId) {
            "35" -> jawaTimurRegencies
            "33" -> jawaTengahRegencies
            "32" -> jawaBaratRegencies
            "31" -> dkiJakartaRegencies
            "34" -> jogjaRegencies
            else -> listOf(
                Regency("${cleanId}01", cleanId, "KOTA UTAMA"),
                Regency("${cleanId}02", cleanId, "KABUPATEN PUSAT")
            )
        }
    }

    fun fetchRegencies(provinceId: String) {
        regenciesJob?.cancel()
        districtsJob?.cancel()
        regencies.clear()
        districts.clear()
        if (provinceId.isBlank()) return

        regenciesJob = viewModelScope.launch {
            isRegenciesLoading = true
            regionError = null
            val cleanId = provinceId.replace(".", "")
            val dottedId = cleanId

            try {
                Log.d("RegionFetch", "Fetching regencies for provinceId=$provinceId (clean=$cleanId)...")
                val list = try {
                    RegionRetrofitClient.regionApiService.getRegencies(cleanId)
                } catch (e1: Exception) {
                    if (e1 is kotlinx.coroutines.CancellationException) throw e1
                    Log.w("RegionFetch", "Primary API regencies failed for $cleanId: ${e1.message}, trying secondary API")
                    RegionRetrofitClient.secondaryRegionApiService.getRegencies(dottedId)
                }

                regencies.clear()
                if (list.isNotEmpty()) {
                    regencies.addAll(list)
                } else {
                    regencies.addAll(getFallbackRegencies(cleanId))
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                Log.e("RegionFetch", "Gagal load regencies for provinceId=$provinceId: ${e.message}", e)
                val fallback = getFallbackRegencies(cleanId)
                regencies.clear()
                regencies.addAll(fallback)
                if (fallback.isEmpty()) {
                    regionError = "Gagal memuat kota/kabupaten. Silakan periksa koneksi internet."
                }
            } finally {
                isRegenciesLoading = false
            }
        }
    }

    private val tulungagungDistricts = listOf(
        District("3504010", "3504", "Tulungagung"),
        District("3504020", "3504", "Boyolangu"),
        District("3504030", "3504", "Kedungwaru"),
        District("3504040", "3504", "Ngantru"),
        District("3504050", "3504", "Kauman"),
        District("3504060", "3504", "Pagerwojo"),
        District("3504070", "3504", "Sendang"),
        District("3504080", "3504", "Karangrejo"),
        District("3504090", "3504", "Gondang"),
        District("3504100", "3504", "Sumbergempol"),
        District("3504110", "3504", "Ngunut"),
        District("3504120", "3504", "Pucanglaban"),
        District("3504130", "3504", "Rejotangan"),
        District("3504140", "3504", "Kalidawir"),
        District("3504150", "3504", "Besuki"),
        District("3504160", "3504", "Campurdarat"),
        District("3504170", "3504", "Bandung"),
        District("3504180", "3504", "Pakel"),
        District("3504190", "3504", "Tanggunggunung")
    )

    private val surabayaDistricts = listOf(
        District("3578010", "3578", "Tegalsari"),
        District("3578020", "3578", "Simokerto"),
        District("3578030", "3578", "Genteng"),
        District("3578040", "3578", "Bubutan"),
        District("3578050", "3578", "Gubeng"),
        District("3578060", "3578", "Wonokromo"),
        District("3578070", "3578", "Joyoboyo"),
        District("3578080", "3578", "Sukolilo"),
        District("3578090", "3578", "Rungkut"),
        District("3578100", "3578", "Jambangan"),
        District("3578110", "3578", "Gayungan"),
        District("3578120", "3578", "Wonocolo"),
        District("3578130", "3578", "Tenggilis Mejoyo"),
        District("3578140", "3578", "Gunung Anyar"),
        District("3578150", "3578", "Mulyorejo"),
        District("3578160", "3578", "Sukomanunggal"),
        District("3578170", "3578", "Tandes"),
        District("3578180", "3578", "Sambikerep"),
        District("3578190", "3578", "Lakarsantri"),
        District("3578200", "3578", "Benowo"),
        District("3578210", "3578", "Pakal"),
        District("3578220", "3578", "Asemrowo"),
        District("3578230", "3578", "Krembangan"),
        District("3578240", "3578", "Semampir"),
        District("3578250", "3578", "Pabean Cantikan"),
        District("3578260", "3578", "Bulak"),
        District("3578270", "3578", "Kenjeran"),
        District("3578280", "3578", "Tambaksari"),
        District("3578290", "3578", "Sawahan"),
        District("3578300", "3578", "Wiyung"),
        District("3578310", "3578", "Karangpilang")
    )

    private val malangDistricts = listOf(
        District("3573010", "3573", "Blimbing"),
        District("3573020", "3573", "Lowokwaru"),
        District("3573030", "3573", "Klojen"),
        District("3573040", "3573", "Sukun"),
        District("3573050", "3573", "Kedungkandang")
    )

    private val kediriKabDistricts = listOf(
        District("3506010", "3506", "Pare"),
        District("3506020", "3506", "Gurah"),
        District("3506030", "3506", "Ngasem"),
        District("3506040", "3506", "Badas"),
        District("3506050", "3506", "Kandangan"),
        District("3506060", "3506", "Kepung"),
        District("3506070", "3506", "Plosoklaten"),
        District("3506080", "3506", "Wates"),
        District("3506090", "3506", "Ngancar"),
        District("3506100", "3506", "Ngadiluwih"),
        District("3506110", "3506", "Kras"),
        District("3506120", "3506", "Kandat"),
        District("3506130", "3506", "Semen"),
        District("3506140", "3506", "Mojo"),
        District("3506150", "3506", "Banyakan"),
        District("3506160", "3506", "Grogol"),
        District("3506170", "3506", "Tarokan"),
        District("3506180", "3506", "Papar"),
        District("3506190", "3506", "Purwoasri")
    )

    private val sidoarjoDistricts = listOf(
        District("3515010", "3515", "Sidoarjo"),
        District("3515020", "3515", "Candi"),
        District("3515030", "3515", "Porong"),
        District("3515040", "3515", "Jabon"),
        District("3515050", "3515", "Tanggulangin"),
        District("3515060", "3515", "Tulangan"),
        District("3515070", "3515", "Krembung"),
        District("3515080", "3515", "Prambon"),
        District("3515090", "3515", "Wonoayu"),
        District("3515100", "3515", "Sukodono"),
        District("3515110", "3515", "Krian"),
        District("3515120", "3515", "Balongbendo"),
        District("3515130", "3515", "Tarik"),
        District("3515140", "3515", "Taman"),
        District("3515150", "3515", "Waru"),
        District("3515160", "3515", "Gedangan"),
        District("3515170", "3515", "Sedati"),
        District("3515180", "3515", "Buduran")
    )

    private val gresikDistricts = listOf(
        District("3525010", "3525", "Gresik"),
        District("3525020", "3525", "Kebomas"),
        District("3525030", "3525", "Manyar"),
        District("3525040", "3525", "Cerme"),
        District("3525050", "3525", "Benjeng"),
        District("3525060", "3525", "Balongpanggang"),
        District("3525070", "3525", "Duduksampeyan"),
        District("3525080", "3525", "Kedamean"),
        District("3525090", "3525", "Menganti"),
        District("3525100", "3525", "Driyorejo"),
        District("3525110", "3525", "Sidayu"),
        District("3525120", "3525", "Ujungpangkah"),
        District("3525130", "3525", "Panceng"),
        District("3525140", "3525", "Dukun"),
        District("3525150", "3525", "Bungah")
    )

    private val trenggalekDistricts = listOf(
        District("3503010", "3503", "Trenggalek"),
        District("3503020", "3503", "Pogalan"),
        District("3503030", "3503", "Durenan"),
        District("3503040", "3503", "Gondang"),
        District("3503050", "3503", "Tugu"),
        District("3503060", "3503", "Karangan"),
        District("3503070", "3503", "Pule"),
        District("3503080", "3503", "Suruh"),
        District("3503090", "3503", "Kampak"),
        District("3503100", "3503", "Dongko"),
        District("3503110", "3503", "Watulimo"),
        District("3503120", "3503", "Munjungan"),
        District("3503130", "3503", "Panggul"),
        District("3503140", "3503", "Bendungan")
    )

    private val blitarDistricts = listOf(
        District("3505010", "3505", "Kanigoro"),
        District("3505020", "3505", "Garum"),
        District("3505030", "3505", "Talun"),
        District("3505040", "3505", "Gandusari"),
        District("3505050", "3505", "Wlingi"),
        District("3505060", "3505", "Doko"),
        District("3505070", "3505", "Selorejo"),
        District("3505080", "3505", "Kesamben"),
        District("3505090", "3505", "Sutojayan"),
        District("3505100", "3505", "Kademangan"),
        District("3505110", "3505", "Nglegok"),
        District("3505120", "3505", "Sanankulon"),
        District("3505130", "3505", "Srengat"),
        District("3505140", "3505", "Ponggok")
    )

    private val ponorogoDistricts = listOf(
        District("3502010", "3502", "Ponorogo"),
        District("3502020", "3502", "Babadan"),
        District("3502030", "3502", "Jenangan"),
        District("3502040", "3502", "Siman"),
        District("3502050", "3502", "Kauman"),
        District("3502060", "3502", "Sukorejo"),
        District("3502070", "3502", "Mlarak"),
        District("3502080", "3502", "Jetis"),
        District("3502090", "3502", "Sambit"),
        District("3502100", "3502", "Sawoo"),
        District("3502110", "3502", "Slahung"),
        District("3502120", "3502", "Balong"),
        District("3502130", "3502", "Badegan"),
        District("3502140", "3502", "Sampung"),
        District("3502150", "3502", "Bungkal"),
        District("3502160", "3502", "Jambon"),
        District("3502170", "3502", "Pulung"),
        District("3502180", "3502", "Ngebel")
    )

    private val pacitanDistricts = listOf(
        District("3501010", "3501", "Pacitan"),
        District("3501020", "3501", "Kebonagung"),
        District("3501030", "3501", "Arjosari"),
        District("3501040", "3501", "Nawangan"),
        District("3501050", "3501", "Bandar"),
        District("3501060", "3501", "Tegalombo"),
        District("3501070", "3501", "Tulakan"),
        District("3501080", "3501", "Ngadirojo"),
        District("3501090", "3501", "Sudimoro"),
        District("3501100", "3501", "Donorojo"),
        District("3501110", "3501", "Punung"),
        District("3501120", "3501", "Pringkuku")
    )

    private fun getFallbackDistricts(regencyId: String, regencyName: String = ""): List<District> {
        val cleanId = regencyId.replace(".", "")
        val nameUpper = regencyName.uppercase()

        return when {
            cleanId == "3504" || nameUpper.contains("TULUNGAGUNG") -> tulungagungDistricts
            cleanId == "3503" || nameUpper.contains("TRENGGALEK") -> trenggalekDistricts
            cleanId == "3502" || nameUpper.contains("PONOROGO") -> ponorogoDistricts
            cleanId == "3501" || nameUpper.contains("PACITAN") -> pacitanDistricts
            cleanId == "3505" || cleanId == "3572" || nameUpper.contains("BLITAR") -> blitarDistricts
            cleanId == "3578" || nameUpper.contains("SURABAYA") -> surabayaDistricts
            cleanId == "3573" || nameUpper.contains("MALANG") -> malangDistricts
            cleanId == "3506" || cleanId == "3571" || nameUpper.contains("KEDIRI") -> kediriKabDistricts
            cleanId == "3515" || nameUpper.contains("SIDOARJO") -> sidoarjoDistricts
            cleanId == "3525" || nameUpper.contains("GRESIK") -> gresikDistricts
            else -> listOf(
                District("${cleanId}01", regencyId, "Kecamatan Pusat / Kota"),
                District("${cleanId}02", regencyId, "Kecamatan Wilayah Utara"),
                District("${cleanId}03", regencyId, "Kecamatan Wilayah Selatan"),
                District("${cleanId}04", regencyId, "Kecamatan Wilayah Barat"),
                District("${cleanId}05", regencyId, "Kecamatan Wilayah Timur")
            )
        }
    }

    fun fetchDistricts(regencyId: String, regencyName: String = "") {
        districtsJob?.cancel()
        districts.clear()
        if (regencyId.isBlank()) return

        districtsJob = viewModelScope.launch {
            isDistrictsLoading = true
            regionError = null
            val cleanId = regencyId.replace(".", "")
            val dottedId = if (cleanId.length == 4) "${cleanId.substring(0, 2)}.${cleanId.substring(2)}" else regencyId

            try {
                Log.d("RegionFetch", "Fetching districts for regencyId=$regencyId ($regencyName, clean=$cleanId, dotted=$dottedId)...")
                val list = try {
                    // 1. Try Primary Ibnux API with clean ID (e.g. 3503.json)
                    RegionRetrofitClient.regionApiService.getDistricts(cleanId)
                } catch (e1: Exception) {
                    if (e1 is kotlinx.coroutines.CancellationException) throw e1
                    Log.w("RegionFetch", "Primary Ibnux API districts failed for $cleanId: ${e1.message}, trying Secondary Emsifa V2 API")
                    try {
                        // 2. Try Secondary Emsifa V2 API with dotted ID (e.g. 35.03.json)
                        RegionRetrofitClient.secondaryRegionApiService.getDistricts(dottedId)
                    } catch (e2: Exception) {
                        if (e2 is kotlinx.coroutines.CancellationException) throw e2
                        RegionRetrofitClient.secondaryRegionApiService.getDistricts(cleanId)
                    }
                }

                districts.clear()
                if (list.isNotEmpty()) {
                    districts.addAll(list)
                } else {
                    val fallback = getFallbackDistricts(cleanId, regencyName)
                    districts.addAll(fallback)
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                Log.e("RegionFetch", "Gagal load districts for regencyId=$regencyId ($regencyName): ${e.message}", e)
                val fallback = getFallbackDistricts(cleanId, regencyName)
                districts.clear()
                districts.addAll(fallback)
                if (fallback.isEmpty()) {
                    regionError = "Gagal memuat kecamatan: Koneksi jaringan terputus."
                }
            } finally {
                isDistrictsLoading = false
            }
        }
    }

    fun resetRegions() {
        provincesJob?.cancel()
        regenciesJob?.cancel()
        districtsJob?.cancel()
        regencies.clear()
        districts.clear()
        regionError = null
    }

    fun fetchCustomers(nidOutlet: String? = null, search: String? = null) {
        viewModelScope.launch {
            try {
                val outletIdInt = nidOutlet?.toIntOrNull()
                val response = RetrofitClient.apiService.getCustomers(outletId = outletIdInt, search = search)
                if (response.isSuccessful && response.body()?.success == true) {
                    val dtos = response.body()?.data ?: emptyList()
                    val fetchedCustomers = dtos.map { it.toCustomer() }
                    customers.clear()
                    customers.addAll(fetchedCustomers)
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Gagal memuat customers: ${e.message}")
            }
        }
    }

    fun createNewCustomer(
        request: id.my.matahati.pos.model.CreateCustomerRequest,
        nidOutlet: String? = null,
        onSuccess: (id.my.matahati.pos.model.Customer) -> Unit
    ) {
        viewModelScope.launch {
            isSubmitting = true
            try {
                val payload = if (request.nidOutlet == null && nidOutlet != null) {
                    request.copy(nidOutlet = nidOutlet.toIntOrNull())
                } else {
                    request
                }
                val response = RetrofitClient.apiService.createCustomer(payload)
                if (response.isSuccessful && response.body()?.success == true) {
                    val dto = response.body()?.data
                    val newCustomer = dto?.toCustomer() ?: id.my.matahati.pos.model.Customer(
                        id = System.currentTimeMillis().toString(),
                        name = request.name,
                        phone = request.phone ?: "",
                        email = request.email ?: "",
                        address = request.address ?: "",
                        customerType = request.customerType ?: "Guest",
                        gender = request.gender ?: "",
                        membershipNo = request.membershipNo ?: "",
                        birthDate = request.birthDate ?: "",
                        notes = request.notes ?: "",
                        postalCode = request.postalCode ?: "",
                        country = request.country ?: "Indonesia",
                        province = request.province ?: "",
                        city = request.city ?: "",
                        district = request.district ?: ""
                    )
                    customers.add(0, newCustomer)
                    onSuccess(newCustomer)
                } else {
                    val newCustomer = id.my.matahati.pos.model.Customer(
                        id = System.currentTimeMillis().toString(),
                        name = request.name,
                        phone = request.phone ?: "",
                        email = request.email ?: "",
                        address = request.address ?: "",
                        customerType = request.customerType ?: "Guest",
                        gender = request.gender ?: "",
                        membershipNo = request.membershipNo ?: "",
                        birthDate = request.birthDate ?: "",
                        notes = request.notes ?: "",
                        postalCode = request.postalCode ?: "",
                        country = request.country ?: "Indonesia",
                        province = request.province ?: "",
                        city = request.city ?: "",
                        district = request.district ?: ""
                    )
                    customers.add(0, newCustomer)
                    onSuccess(newCustomer)
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error createNewCustomer: ${e.message}", e)
                val newCustomer = id.my.matahati.pos.model.Customer(
                    id = System.currentTimeMillis().toString(),
                    name = request.name,
                    phone = request.phone ?: "",
                    email = request.email ?: "",
                    address = request.address ?: "",
                    customerType = request.customerType ?: "Guest",
                    gender = request.gender ?: "",
                    membershipNo = request.membershipNo ?: "",
                    birthDate = request.birthDate ?: "",
                    notes = request.notes ?: "",
                    postalCode = request.postalCode ?: "",
                    country = request.country ?: "Indonesia",
                    province = request.province ?: "",
                    city = request.city ?: "",
                    district = request.district ?: ""
                )
                customers.add(0, newCustomer)
                onSuccess(newCustomer)
            } finally {
                isSubmitting = false
            }
        }
    }

    fun updateCustomer(
        id: String,
        request: id.my.matahati.pos.model.CreateCustomerRequest,
        nidOutlet: String? = null,
        onSuccess: (id.my.matahati.pos.model.Customer) -> Unit
    ) {
        viewModelScope.launch {
            isSubmitting = true
            try {
                val payload = if (request.nidOutlet == null && nidOutlet != null) {
                    request.copy(nidOutlet = nidOutlet.toIntOrNull())
                } else {
                    request
                }
                val response = RetrofitClient.apiService.updateCustomer(id, payload)
                val dto = response.body()?.data
                val updatedCustomer = dto?.toCustomer() ?: Customer(
                    id = id,
                    nidType = request.nidType,
                    name = request.name,
                    phone = request.phone ?: "",
                    email = request.email ?: "",
                    address = request.address ?: "",
                    customerType = request.customerType ?: "Guest",
                    gender = request.gender ?: "",
                    membershipNo = request.membershipNo ?: "",
                    birthDate = request.birthDate ?: "",
                    notes = request.notes ?: "",
                    postalCode = request.postalCode ?: "",
                    country = request.country ?: "Indonesia",
                    province = request.province ?: "",
                    city = request.city ?: "",
                    district = request.district ?: ""
                )
                val index = customers.indexOfFirst { it.id == id }
                if (index >= 0) {
                    customers[index] = updatedCustomer
                } else {
                    customers.add(0, updatedCustomer)
                }
                onSuccess(updatedCustomer)
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error updateCustomer: ${e.message}", e)
                val updatedCustomer = Customer(
                    id = id,
                    nidType = request.nidType,
                    name = request.name,
                    phone = request.phone ?: "",
                    email = request.email ?: "",
                    address = request.address ?: "",
                    customerType = request.customerType ?: "Guest",
                    gender = request.gender ?: "",
                    membershipNo = request.membershipNo ?: "",
                    birthDate = request.birthDate ?: "",
                    notes = request.notes ?: "",
                    postalCode = request.postalCode ?: "",
                    country = request.country ?: "Indonesia",
                    province = request.province ?: "",
                    city = request.city ?: "",
                    district = request.district ?: ""
                )
                val index = customers.indexOfFirst { it.id == id }
                if (index >= 0) {
                    customers[index] = updatedCustomer
                }
                onSuccess(updatedCustomer)
            } finally {
                isSubmitting = false
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

    fun fetchVouchers() {
        viewModelScope.launch {
            try {
                val vouchResponse = RetrofitClient.apiService.getVouchers()
                if (vouchResponse.isSuccessful && vouchResponse.body()?.success == true) {
                    val dtos = vouchResponse.body()?.data ?: emptyList()
                    val fetchedVouchers = dtos.map { it.toVoucher() }
                    vouchers.clear()
                    vouchers.addAll(fetchedVouchers)
                }
            } catch (e: Exception) {
                Log.e("Vouchers", "Gagal memuat voucher", e)
            }
        }
    }

    fun submitTransaction(
        context: android.content.Context,
        cartItems: List<id.my.matahati.pos.model.CartItem>,
        orderType: String,
        selectedCustomer: Customer?,
        selectedPayment: PaymentMethod,
        discount: Double,
        tax: Double,
        paidAmount: Double,
        nidVoucher: Int? = null,
        nidOutlet: String?,
        orderNote: String? = null,
        userName: String = "",
        outletName: String = "",
        onSuccess: (() -> Unit)? = null
    ) {
        executeTransaction(
            context = context,
            cartItems = cartItems,
            orderType = orderType,
            selectedCustomer = selectedCustomer,
            selectedPayment = selectedPayment,
            discount = discount,
            tax = tax,
            paidAmount = paidAmount,
            nidVoucher = nidVoucher,
            nidOutlet = nidOutlet,
            status = null,
            cancelNote = null,
            orderNote = orderNote,
            userName = userName,
            outletName = outletName,
            onSuccess = onSuccess
        )
    }

    fun submitCancelTransaction(
        context: android.content.Context,
        cartItems: List<id.my.matahati.pos.model.CartItem>,
        orderType: String,
        selectedCustomer: Customer?,
        cancelNote: String,
        nidOutlet: String?,
        orderNote: String? = null
    ) {
        executeTransaction(
            context = context,
            cartItems = cartItems,
            orderType = orderType,
            selectedCustomer = selectedCustomer,
            selectedPayment = null,
            discount = 0.0,
            tax = 0.0,
            paidAmount = 0.0,
            nidOutlet = nidOutlet,
            status = "CANCELLED",
            cancelNote = cancelNote,
            orderNote = orderNote
        )
    }

    fun holdCurrentCart(
        cartItems: List<id.my.matahati.pos.model.CartItem>,
        orderType: String,
        selectedCustomer: Customer?,
        discount: Double,
        tax: Double,
        nidOutlet: String?,
        orderNote: String? = null,
        onSuccess: () -> Unit = {}
    ) {
        executeTransaction(
            context = null,
            cartItems = cartItems,
            orderType = orderType,
            selectedCustomer = selectedCustomer,
            selectedPayment = null,
            discount = discount,
            tax = tax,
            paidAmount = 0.0,
            nidOutlet = nidOutlet,
            status = "DRAFT",
            cancelNote = null,
            orderNote = orderNote,
            onSuccess = onSuccess
        )
    }

    private fun executeTransaction(
        context: android.content.Context? = null,
        cartItems: List<id.my.matahati.pos.model.CartItem>,
        orderType: String,
        selectedCustomer: Customer?,
        selectedPayment: PaymentMethod?,
        discount: Double,
        tax: Double,
        paidAmount: Double,
        nidVoucher: Int? = null,
        nidOutlet: String?,
        status: String?,
        cancelNote: String?,
        orderNote: String? = null,
        userName: String = "",
        outletName: String = "",
        onSuccess: (() -> Unit)? = null
    ) {
        if (status != "CANCELLED" && cartItems.isEmpty()) {
            transactionError = "Cart kosong."
            return
        }
        if (status != "CANCELLED" && status != "DRAFT") {
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
                note = it.encodeNoteWithSentQty()
            )
        }

        val parsedOutlet = nidOutlet?.toIntOrNull() ?: 1
        isOrderTypeSelectedByUser = orderType.isNotBlank()
        val defaultOrderType = "ONLINE"

        val request = TransactionRequest(
            nid = currentDraftId?.toIntOrNull(),
            nidCustomer = selectedCustomer?.id?.toIntOrNull(),
            nidOutlet = parsedOutlet,
            nidUser = selectedServedBy?.id,
            nidPayment = selectedPayment?.id,
            nidVoucher = nidVoucher,
            customerName = selectedCustomer?.name,
            orderType = orderType.ifBlank { defaultOrderType },
            visitorCount = 1,
            tableName = selectedTable.ifBlank { null },
            discount = discount,
            tax = tax,
            paidAmount = paidAmount,
            status = status,
            cancelNote = cancelNote,
            orderNote = orderNote?.ifBlank { null },
            details = details
        )

        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.submitTransaction(request)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        val previousDraftId = currentDraftId
                        if (previousDraftId != null) {
                            currentDraftId = null
                            removeHeldOrderLocal(previousDraftId)
                            try {
                                val delRes = RetrofitClient.apiService.deleteTransaction(previousDraftId)
                                if (delRes.isSuccessful) {
                                    Log.d("HeldOrders", "Old draft $previousDraftId successfully deleted from backend.")
                                } else {
                                    Log.e("HeldOrders", "Failed to delete old draft $previousDraftId: code=${delRes.code()} error=${delRes.errorBody()?.string()}")
                                }
                            } catch (e: Exception) {
                                Log.e("HeldOrders", "Error deleting old draft: ${e.message}", e)
                            }
                        }
                        if (status == "CANCELLED") {
                            closeReceiptDialog()
                            transactionSuccessMessage = "Pesanan berhasil dibatalkan dan dicatat."
                            onSuccess?.invoke()
                        } else if (status == "DRAFT") {
                            transactionSuccessMessage = "Pesanan berhasil digantung (Draft)."
                            fetchHeldOrders(nidOutlet)
                            onSuccess?.invoke()
                        } else {
                            lastTransaction = body.data
                            pendingShowReceipt = true
                            fetchVouchers() // Refresh voucher quota after success
                            if (context != null) {
                                openKitchenPrintDialog(
                                    context = context,
                                    items = cartItems,
                                    userName = userName,
                                    outletName = outletName,
                                    onAutoFinishCheckout = onSuccess
                                )
                            } else {
                                onSuccess?.invoke()
                            }
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
                            if (parsedError.contains("belum memulai shift", ignoreCase = true) || response.code() == 422 && parsedError.contains("shift", ignoreCase = true)) {
                                showShiftNotStartedDialog = true
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

    fun isShowPrintPreviewEnabled(context: android.content.Context): Boolean {
        val prefs = context.getSharedPreferences("pos_prefs", android.content.Context.MODE_PRIVATE)
        return prefs.getBoolean("show_print_preview", true)
    }

    fun handlePendingReceipt(
        context: android.content.Context,
        userName: String = "",
        outletName: String = "",
        onAutoFinishCheckout: (() -> Unit)? = null
    ) {
        if (pendingShowReceipt) {
            pendingShowReceipt = false
            if (isShowPrintPreviewEnabled(context)) {
                showReceiptDialog = true
            } else {
                lastTransaction?.let { trx ->
                    printReceipt(context, trx, userName, outletName)
                }
                closeReceiptDialog()
                onAutoFinishCheckout?.invoke()
            }
        }
    }

    // Kitchen Printing logic
    fun openKitchenPrintDialog(
        context: android.content.Context,
        items: List<id.my.matahati.pos.model.CartItem>,
        isHistory: Boolean = false,
        userName: String = "",
        outletName: String = "",
        onAutoFinishCheckout: (() -> Unit)? = null
    ) {
        if (items.isEmpty()) return
        
        loadSavedPrinters(context)

        val hasKitchenOrBarPrinter = savedPrinters.any { 
            it.role == id.my.matahati.pos.model.PrinterRole.KITCHEN || it.role == id.my.matahati.pos.model.PrinterRole.BAR 
        }

        if (!hasKitchenOrBarPrinter && !isHistory) {
            handlePendingReceipt(context, userName, outletName, onAutoFinishCheckout)
            return
        }

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

    fun openKitchenPrintDialogFromHistory(context: android.content.Context, transaction: TransactionModel) {
        val items = transaction.details?.map { detail ->
            // Try to find the original product to get the correct station name
            val originalProduct = products.find { it.id == detail.productId }
            val station = originalProduct?.stationName ?: if (detail.productName.lowercase().contains("tea") || 
                                     detail.productName.lowercase().contains("kopi") || 
                                     detail.productName.lowercase().contains("ice")) "BAR" else "DAPUR"

            val (cleanNote, sentQty) = id.my.matahati.pos.model.CartItem.parseNoteAndSentQty(detail.note)

            id.my.matahati.pos.model.CartItem(
                product = Product(
                    id = detail.productId,
                    name = detail.productName,
                    price = detail.price.toDoubleOrNull() ?: 0.0,
                    categoryId = "1",
                    stock = 0,
                    stationName = station
                ),
                quantity = detail.quantity,
                note = cleanNote,
                sentQuantity = sentQty
            )
        } ?: emptyList()
        
        openKitchenPrintDialog(context, items, isHistory = true)
    }

    fun onConfirmKitchenPrint(
        type: String,
        context: android.content.Context? = null,
        userName: String = "",
        outletName: String = "",
        onAutoFinishCheckout: (() -> Unit)? = null,
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
            if (context != null && !isShowPrintPreviewEnabled(context)) {
                printKitchenTickets(context, receiptTickets)
                closeSimulatedReceipt(context, userName, outletName, onAutoFinishCheckout)
            } else {
                showSimulatedReceipt = true
            }
        } else {
            showKitchenPrintDialog = false
            if (context != null) {
                handlePendingReceipt(context, userName, outletName, onAutoFinishCheckout)
            }
        }
    }

    fun toggleStationSelection(station: String) {
        if (selectedStations.contains(station)) {
            selectedStations.remove(station)
        } else {
            selectedStations.add(station)
        }
    }

    fun closeSimulatedReceipt(
        context: android.content.Context? = null,
        userName: String = "",
        outletName: String = "",
        onAutoFinishCheckout: (() -> Unit)? = null
    ) {
        showSimulatedReceipt = false
        receiptTickets.clear()
        if (context != null) {
            handlePendingReceipt(context, userName, outletName, onAutoFinishCheckout)
        } else if (pendingShowReceipt) {
            pendingShowReceipt = false
            showReceiptDialog = true
        }
    }

    fun closeKitchenPrintDialog(
        context: android.content.Context? = null,
        userName: String = "",
        outletName: String = "",
        onAutoFinishCheckout: (() -> Unit)? = null
    ) {
        showKitchenPrintDialog = false
        if (context != null) {
            handlePendingReceipt(context, userName, outletName, onAutoFinishCheckout)
        } else if (pendingShowReceipt) {
            pendingShowReceipt = false
            showReceiptDialog = true
        }
    }

    fun fetchTransactionHistory(
        startDate: String? = null,
        endDate: String? = null,
        search: String? = null,
        nidOutlet: String? = null,
        nidPayment: String? = null
    ) {
        transactionHistory.clear()
        viewModelScope.launch {
            isHistoryLoading = true
            try {
                val response = RetrofitClient.apiService.getTransactions(
                    startDate = startDate,
                    endDate = endDate,
                    search = search,
                    outletId = nidOutlet,
                    paymentId = nidPayment
                )
                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()?.data ?: emptyList()
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

    var isVoidRefundLoading by mutableStateOf(false)
        private set
    var voidRefundError by mutableStateOf<String?>(null)

    fun voidTransaction(
        transactionId: String,
        note: String,
        nidOutlet: String? = null,
        onSuccess: (id.my.matahati.pos.model.TransactionData?) -> Unit
    ) {
        viewModelScope.launch {
            isVoidRefundLoading = true
            voidRefundError = null
            try {
                val request = id.my.matahati.pos.model.VoidTransactionRequest(voidNote = note)
                val response = RetrofitClient.apiService.voidTransaction(transactionId, request)
                if (response.isSuccessful && response.body()?.success == true) {
                    val updatedData = response.body()?.data
                    fetchTransactionHistory(nidOutlet = nidOutlet)
                    onSuccess(updatedData)
                } else {
                    val msg = response.body()?.message ?: "Gagal membatalkan transaksi: ${response.message()}"
                    voidRefundError = msg
                }
            } catch (e: Exception) {
                Log.e("VoidTransaction", "Error: ${e.message}", e)
                voidRefundError = "Gagal membatalkan transaksi: ${e.message}"
            } finally {
                isVoidRefundLoading = false
            }
        }
    }

    fun refundTransaction(
        transactionId: String,
        note: String,
        nidOutlet: String? = null,
        onSuccess: (id.my.matahati.pos.model.TransactionData?) -> Unit
    ) {
        viewModelScope.launch {
            isVoidRefundLoading = true
            voidRefundError = null
            try {
                val request = id.my.matahati.pos.model.RefundTransactionRequest(refundNote = note)
                val response = RetrofitClient.apiService.refundTransaction(transactionId, request)
                if (response.isSuccessful && response.body()?.success == true) {
                    val updatedData = response.body()?.data
                    fetchTransactionHistory(nidOutlet = nidOutlet)
                    onSuccess(updatedData)
                } else {
                    val msg = response.body()?.message ?: "Gagal merefund transaksi: ${response.message()}"
                    voidRefundError = msg
                }
            } catch (e: Exception) {
                Log.e("RefundTransaction", "Error: ${e.message}", e)
                voidRefundError = "Gagal merefund transaksi: ${e.message}"
            } finally {
                isVoidRefundLoading = false
            }
        }
    }

    fun voidItemTransaction(
        transactionId: String,
        items: List<id.my.matahati.pos.model.ItemVoidRefundRequestItem>,
        reason: String,
        nidOutlet: String? = null,
        onSuccess: (id.my.matahati.pos.model.TransactionData?) -> Unit
    ) {
        viewModelScope.launch {
            isVoidRefundLoading = true
            voidRefundError = null
            try {
                val request = id.my.matahati.pos.model.ItemVoidRefundRequest(items = items, reason = reason)
                val response = RetrofitClient.apiService.voidItemTransaction(transactionId, request)
                if (response.isSuccessful && response.body()?.success == true) {
                    val updatedData = response.body()?.data
                    fetchTransactionHistory(nidOutlet = nidOutlet)
                    onSuccess(updatedData)
                } else {
                    val msg = response.body()?.message ?: "Gagal membatalkan item: ${response.message()}"
                    voidRefundError = msg
                }
            } catch (e: Exception) {
                Log.e("VoidItemTransaction", "Error: ${e.message}", e)
                voidRefundError = "Gagal membatalkan item: ${e.message}"
            } finally {
                isVoidRefundLoading = false
            }
        }
    }

    fun refundItemTransaction(
        transactionId: String,
        items: List<id.my.matahati.pos.model.ItemVoidRefundRequestItem>,
        reason: String,
        nidOutlet: String? = null,
        onSuccess: (id.my.matahati.pos.model.TransactionData?) -> Unit
    ) {
        viewModelScope.launch {
            isVoidRefundLoading = true
            voidRefundError = null
            try {
                val request = id.my.matahati.pos.model.ItemVoidRefundRequest(items = items, reason = reason)
                val response = RetrofitClient.apiService.refundItemTransaction(transactionId, request)
                if (response.isSuccessful && response.body()?.success == true) {
                    val updatedData = response.body()?.data
                    fetchTransactionHistory(nidOutlet = nidOutlet)
                    onSuccess(updatedData)
                } else {
                    val msg = response.body()?.message ?: "Gagal merefund item: ${response.message()}"
                    voidRefundError = msg
                }
            } catch (e: Exception) {
                Log.e("RefundItemTransaction", "Error: ${e.message}", e)
                voidRefundError = "Gagal merefund item: ${e.message}"
            } finally {
                isVoidRefundLoading = false
            }
        }
    }
}
