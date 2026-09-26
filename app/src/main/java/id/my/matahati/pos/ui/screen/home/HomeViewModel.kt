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

    // Transaction States
    var selectedTable by mutableStateOf("")
    var isSubmitting by mutableStateOf(false)
    var transactionError by mutableStateOf<String?>(null)
    var transactionSuccessMessage by mutableStateOf<String?>(null)
    var lastTransaction by mutableStateOf<TransactionData?>(null)
    var showReceiptDialog by mutableStateOf(false)
    var showShiftNotStartedDialog by mutableStateOf(false)

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

    fun deleteHeldOrder(context: android.content.Context, id: String, nidOutlet: String?) {
        removeHeldOrderLocal(id)
        viewModelScope.launch {
            try {
                RetrofitClient.apiService.deleteTransaction(id)
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
            val receiptBytes = formatter.formatReceipt(data, cashierName, savedOutletName)
            
            val result = connectionManager.printData(receiptPrinter, receiptBytes)
            if (result.isFailure) {
                printerError = "Gagal mencetak struk: ${result.exceptionOrNull()?.message ?: "Cek koneksi printer"}"
            }
            isPrinting = false
        }
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

    fun fetchData(nidOutlet: String? = null) {
        products.clear()
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                // Fetch Categories
                val catResponse = RetrofitClient.apiService.getCategories()
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
                val prodResponse = RetrofitClient.apiService.getProducts(outletId = nidOutlet)
                if (prodResponse.isSuccessful && prodResponse.body()?.success == true) {
                    val dtos = prodResponse.body()?.data ?: emptyList()
                    val fetchedProducts = dtos.map { it.toProduct() }
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

                // Fetch Customer Types
                try {
                    val custTypeResponse = RetrofitClient.apiService.getCustomerTypes()
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
        viewModelScope.launch {
            isProvincesLoading = true
            try {
                val list = try {
                    RegionRetrofitClient.secondaryRegionApiService.getProvinces()
                } catch (e: Exception) {
                    Log.w("HomeViewModel", "Secondary API provinces failed, trying primary API: ${e.message}")
                    RegionRetrofitClient.regionApiService.getProvinces()
                }
                provinces.clear()
                if (list.isNotEmpty()) {
                    provinces.addAll(list)
                } else {
                    provinces.addAll(defaultProvinces)
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Gagal load provinsi dari kedua API: ${e.message}", e)
                provinces.clear()
                provinces.addAll(defaultProvinces)
            } finally {
                isProvincesLoading = false
            }
        }
    }

    fun fetchRegencies(provinceId: String) {
        regencies.clear()
        districts.clear()
        viewModelScope.launch {
            isRegenciesLoading = true
            try {
                val list = try {
                    RegionRetrofitClient.secondaryRegionApiService.getRegencies(provinceId)
                } catch (e1: Exception) {
                    Log.w("HomeViewModel", "Secondary API regencies failed, trying primary API: ${e1.message}")
                    RegionRetrofitClient.regionApiService.getRegencies(provinceId)
                }
                regencies.clear()
                if (list.isNotEmpty()) {
                    regencies.addAll(list)
                } else if (provinceId == "35") {
                    regencies.addAll(jawaTimurRegencies)
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Gagal load regencies: ${e.message}", e)
                if (provinceId == "35") {
                    regencies.addAll(jawaTimurRegencies)
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

    private fun getFallbackDistricts(regencyId: String, regencyName: String = ""): List<District> {
        val cleanId = regencyId.replace(".", "")
        val nameUpper = regencyName.uppercase()

        return when {
            cleanId == "3504" || nameUpper.contains("TULUNGAGUNG") -> tulungagungDistricts
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
        districts.clear()
        viewModelScope.launch {
            isDistrictsLoading = true
            val cleanId = regencyId.replace(".", "")
            val dottedId = if (cleanId.length == 4) "${cleanId.substring(0, 2)}.${cleanId.substring(2)}" else regencyId

            try {
                val list = try {
                    // 1. Try Primary GitHub Raw Emsifa V2 API with dotted ID (e.g. 35.78.json)
                    RegionRetrofitClient.regionApiService.getDistricts(dottedId)
                } catch (e1: Exception) {
                    Log.w("HomeViewModel", "Primary Emsifa V2 districts failed for $dottedId: ${e1.message}, trying Secondary Ibnux API")
                    try {
                        // 2. Try Secondary Ibnux API with clean ID (e.g. 3578.json)
                        RegionRetrofitClient.secondaryRegionApiService.getDistricts(cleanId)
                    } catch (e2: Exception) {
                        RegionRetrofitClient.regionApiService.getDistricts(cleanId)
                    }
                }

                districts.clear()
                if (list.isNotEmpty()) {
                    districts.addAll(list)
                } else {
                    districts.addAll(getFallbackDistricts(cleanId, regencyName))
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Gagal load districts dari API: ${e.message}", e)
                districts.clear()
                districts.addAll(getFallbackDistricts(cleanId, regencyName))
            } finally {
                isDistrictsLoading = false
            }
        }
    }

    fun resetRegions() {
        regencies.clear()
        districts.clear()
    }

    fun createNewCustomer(
        request: id.my.matahati.pos.model.CreateCustomerRequest,
        onSuccess: (id.my.matahati.pos.model.Customer) -> Unit
    ) {
        viewModelScope.launch {
            isSubmitting = true
            try {
                val response = RetrofitClient.apiService.createCustomer(request)
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
        onSuccess: (id.my.matahati.pos.model.Customer) -> Unit
    ) {
        viewModelScope.launch {
            isSubmitting = true
            try {
                val response = RetrofitClient.apiService.updateCustomer(id, request)
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
        nidOutlet: String?
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
            cancelNote = null
        )
    }

    fun submitCancelTransaction(
        context: android.content.Context,
        cartItems: List<id.my.matahati.pos.model.CartItem>,
        orderType: String,
        selectedCustomer: Customer?,
        cancelNote: String,
        nidOutlet: String?
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
            cancelNote = cancelNote
        )
    }

    fun holdCurrentCart(
        cartItems: List<id.my.matahati.pos.model.CartItem>,
        orderType: String,
        selectedCustomer: Customer?,
        discount: Double,
        tax: Double,
        nidOutlet: String?,
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
        onSuccess: (() -> Unit)? = null
    ) {
        if (status != "CANCELLED" && cartItems.isEmpty()) {
            transactionError = "Cart kosong."
            return
        }
        if (status != "CANCELLED" && status != "DRAFT") {
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
        } else if (status == "DRAFT") {
            if (orderType.isBlank()) {
                transactionError = "Silakan pilih In/Aw (Order Type) terlebih dahulu."
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

        val request = TransactionRequest(
            nidCustomer = selectedCustomer?.id,
            nidOutlet = parsedOutlet,
            nidUser = selectedServedBy?.id,
            nidPayment = selectedPayment?.id,
            nidVoucher = nidVoucher,
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
                                openKitchenPrintDialog(context, cartItems)
                            }
                            onSuccess?.invoke()
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

    // Kitchen Printing logic
    fun openKitchenPrintDialog(context: android.content.Context, items: List<id.my.matahati.pos.model.CartItem>, isHistory: Boolean = false) {
        if (items.isEmpty()) return
        
        loadSavedPrinters(context)
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
        if (pendingShowReceipt) {
            pendingShowReceipt = false
            showReceiptDialog = true
        }
    }

    fun closeKitchenPrintDialog() {
        showKitchenPrintDialog = false
        if (pendingShowReceipt) {
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
}
