package id.my.matahati.pos.ui.screen.home

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import id.my.matahati.pos.data.printer.BluetoothPrinterManager
import id.my.matahati.pos.model.CartItem
import id.my.matahati.pos.model.Product
import id.my.matahati.pos.ui.screen.home.components.*
import id.my.matahati.pos.ui.theme.MobileMatahati_POSTheme
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    userId: String? = null,
    userName: String = "Kasir",
    nidOutlet: String? = null,
    outletName: String? = null,
    roleOwner: Boolean = false,
    roleCashier: Boolean = true,
    roleCaptain: Boolean = false,
    onLogout: () -> Unit = {},
    viewModel: HomeViewModel = viewModel(),
    shiftViewModel: id.my.matahati.pos.ui.screen.shift.ShiftViewModel = viewModel()
) {
    val context = LocalContext.current
    val printerManager = remember { BluetoothPrinterManager(context) }
    
    // Drawer State
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    // Data State from ViewModel
    val categories = viewModel.categories
    val allProducts = viewModel.products

    // Interactive UI State
    var currentScreen by remember { mutableStateOf("pos") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf("all") }
    var orderType by remember { mutableStateOf("") }
    var showOrderTypeDialog by remember { mutableStateOf(false) }
    var selectedRightTab by remember { mutableStateOf("Produk") }
    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }
    var showNotificationPopup by remember { mutableStateOf(false) }
    
    var selectedCartItem by remember { mutableStateOf<CartItem?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDiscountDialog by remember { mutableStateOf(false) }
    var showDateFilterDialog by remember { mutableStateOf(false) }
    var showPaymentTypeDialog by remember { mutableStateOf(false) }
    var showTableInputDialog by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var validationWarningMessage by remember { mutableStateOf<String?>(null) }
    
    val today = remember { Calendar.getInstance().time }
    val displaySdf = remember { SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")) }
    val apiSdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    
    var selectedDateRange by remember { mutableStateOf(displaySdf.format(today)) }
    var selectedStartDate by remember { mutableStateOf(apiSdf.format(today)) }
    var selectedEndDate by remember { mutableStateOf(apiSdf.format(today)) }
    
    var transaksiSearchQuery by remember { mutableStateOf("") }
    var selectedHistoryTransaction by remember { mutableStateOf<id.my.matahati.pos.model.TransactionModel?>(null) }
    var selectedPaymentType by remember { mutableStateOf("Semua Tipe Pembayaran") }
    var selectedCustomer by remember { mutableStateOf<id.my.matahati.pos.model.Customer?>(null) }
    var selectedVoucher by remember { mutableStateOf<id.my.matahati.pos.model.Voucher?>(null) }
    var manualDiscountInput by remember { mutableStateOf("") }
    
    val cartItems = remember { mutableStateListOf<CartItem>() }

    val selectedCategoryName = categories.find { it.id == selectedCategoryId }?.name ?: "Semua Kategori"

    val orderTypes by viewModel.orderTypes.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all { it.value }
        if (granted) {
            viewModel.openPrinterSelection(printerManager)
        } else {
            viewModel.printerError = "Izin Bluetooth diperlukan untuk menghubungkan printer."
        }
    }

    val runWithBluetoothPermission: (() -> Unit) -> Unit = { onPermissionGranted ->
        if (android.os.Build.VERSION.SDK_INT >= 31) {
            val permissions = arrayOf(
                android.Manifest.permission.BLUETOOTH_CONNECT,
                android.Manifest.permission.BLUETOOTH_SCAN
            )
            val allGranted = permissions.all {
                ContextCompat.checkSelfPermission(context, it) == android.content.pm.PackageManager.PERMISSION_GRANTED
            }
            if (allGranted) {
                onPermissionGranted()
            } else {
                permissionLauncher.launch(permissions)
            }
        } else {
            onPermissionGranted()
        }
    }

    fun checkAndPrint(data: id.my.matahati.pos.model.TransactionData) {
        runWithBluetoothPermission {
            if (viewModel.selectedPrinterAddress == null) {
                viewModel.openPrinterSelection(printerManager)
            } else {
                viewModel.printReceipt(context, data, userName, outletName)
            }
        }
    }

    val triggerCheckout: () -> Unit = {
        if (cartItems.isNotEmpty()) {
            if (shiftViewModel.currentShift == null) {
                validationWarningMessage = "Kasir belum memulai shift. Silakan buka shift terlebih dahulu di menu Shift."
            } else if (orderType.isBlank()) {
                validationWarningMessage = "Silahkan pilih tipe pesanan"
            } else if (orderType == "DINE_IN" && viewModel.selectedTable.isBlank()) {
                validationWarningMessage = "Silahkan isi nomor meja"
            } else if (selectedCustomer == null) {
                validationWarningMessage = "Silahkan pilih customer"
            } else {
                val prefs = context.getSharedPreferences("pos_prefs", android.content.Context.MODE_PRIVATE)
                val isConfirmEnabled = prefs.getBoolean("confirm_order_before_payment", true)
                if (isConfirmEnabled) {
                    viewModel.showCashierConfirmDialog = true
                } else {
                    viewModel.showPaymentScreen = true
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadOrderTypes()
        viewModel.loadPrinterSettings(context)
    }

    LaunchedEffect(nidOutlet, userId, userName) {
        val outletId = nidOutlet?.toIntOrNull() ?: 1
        viewModel.fetchData(nidOutlet)
        viewModel.loadServedByUsers(outletId, currentUserId = userId, currentUserName = userName)
        viewModel.fetchHeldOrders(nidOutlet)
        shiftViewModel.checkCurrentShift(nidOutlet)
    }

    LaunchedEffect(currentScreen, transaksiSearchQuery, selectedStartDate, selectedEndDate, selectedPaymentType) {
        if (currentScreen == "transaksi") {
            val selectedPaymentMethod = viewModel.paymentMethods.find { 
                it.name.equals(selectedPaymentType, ignoreCase = true) 
            }
            val paymentId = if (selectedPaymentType == "Semua Tipe Pembayaran" || selectedPaymentType.equals("all", ignoreCase = true)) {
                null
            } else {
                selectedPaymentMethod?.id
            }

            viewModel.fetchTransactionHistory(
                startDate = selectedStartDate,
                endDate = selectedEndDate,
                search = transaksiSearchQuery.ifBlank { null },
                nidOutlet = nidOutlet,
                nidPayment = paymentId
            )
        }
    }

    // Cart Helper Functions
    val onAddToCart: (Product) -> Unit = { product ->
        val existingIndex = cartItems.indexOfFirst { it.product.id == product.id }
        if (existingIndex >= 0) {
            val item = cartItems[existingIndex]
            cartItems[existingIndex] = item.copy(quantity = item.quantity + 1)
        } else {
            cartItems.add(CartItem(product = product, quantity = 1))
        }
    }

    val onIncreaseQuantity: (CartItem) -> Unit = { item ->
        val index = cartItems.indexOfFirst { it.product.id == item.product.id }
        if (index >= 0) {
            cartItems[index] = item.copy(quantity = item.quantity + 1)
        }
    }

    val onDecreaseQuantity: (CartItem) -> Unit = { item ->
        val index = cartItems.indexOfFirst { it.product.id == item.product.id }
        if (index >= 0) {
            val currentItem = cartItems[index]
            if (currentItem.quantity > 1) {
                val newQty = currentItem.quantity - 1
                cartItems[index] = currentItem.copy(
                    quantity = newQty,
                    sentQuantity = currentItem.sentQuantity.coerceAtMost(newQty)
                )
            } else {
                cartItems.removeAt(index)
            }
        }
    }

    val onUpdateCartItem: (String, Int, String) -> Unit = { productId, newQty, newNote ->
        val index = cartItems.indexOfFirst { it.product.id == productId }
        if (index >= 0) {
            val currentItem = cartItems[index]
            cartItems[index] = currentItem.copy(
                quantity = newQty,
                note = newNote,
                sentQuantity = currentItem.sentQuantity.coerceAtMost(newQty)
            )
        }
    }

    val onRemoveCartItem: (String) -> Unit = { productId ->
        val index = cartItems.indexOfFirst { it.product.id == productId }
        if (index >= 0) {
            cartItems.removeAt(index)
        }
    }

    // Filtered Products Calculation
    val filteredProducts by remember {
        derivedStateOf {
            allProducts.filter { product ->
                val matchesCategory = (selectedCategoryId == "all") || (product.categoryId == selectedCategoryId)
                val matchesSearch = product.name.contains(searchQuery, ignoreCase = true)
                matchesCategory && matchesSearch
            }
        }
    }

    // Filtered Transactions Calculation
    val filteredTransactions by remember {
        derivedStateOf {
            if (selectedPaymentType == "Semua Tipe Pembayaran" || selectedPaymentType.equals("all", ignoreCase = true) || selectedPaymentType.isBlank()) {
                viewModel.transactionHistory
            } else {
                val matchingMethod = viewModel.paymentMethods.find { it.name.equals(selectedPaymentType, ignoreCase = true) }
                viewModel.transactionHistory.filter { trx ->
                    val matchesId = matchingMethod != null && trx.nidPayment == matchingMethod.id
                    val matchesName = trx.payment?.cname?.equals(selectedPaymentType, ignoreCase = true) == true
                    matchesId || matchesName
                }
            }
        }
    }

    // Cart Total Calculation
    val cartSubtotal by remember {
        derivedStateOf { cartItems.sumOf { it.totalPrice } }
    }

    val discountAmount by remember {
        derivedStateOf {
            val voucherDiscount = selectedVoucher?.let { voucher ->
                if (cartSubtotal >= voucher.minSpend) {
                    if (voucher.discountPercent > 0.0) {
                        cartSubtotal * (voucher.discountPercent / 100.0)
                    } else {
                        voucher.discountAmount
                    }
                } else 0.0
            } ?: 0.0

            val manualDiscount = parseManualDiscount(manualDiscountInput, cartSubtotal)
            voucherDiscount + manualDiscount
        }
    }

    val cartGrandTotal by remember {
        derivedStateOf { (cartSubtotal - discountAmount).coerceAtLeast(0.0) }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isTablet = maxWidth >= 600.dp

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                SidebarDrawer(
                    userName = userName,
                    roleOwner = roleOwner,
                    roleCashier = roleCashier,
                    roleCaptain = roleCaptain,
                    currentScreen = currentScreen,
                    onNavigateToPos = {
                        currentScreen = "pos"
                        coroutineScope.launch { drawerState.close() }
                    },
                    onNavigateToTransaksi = {
                        currentScreen = "transaksi"
                        coroutineScope.launch { drawerState.close() }
                    },
                    onNavigateToShift = {
                        currentScreen = "shift"
                        coroutineScope.launch { drawerState.close() }
                    },
                    onNavigateToPengaturan = {
                        currentScreen = "pengaturan"
                        coroutineScope.launch { drawerState.close() }
                    },
                    onLogout = onLogout
                )
            }
        ) {
            Scaffold(
                topBar = {
                    if (currentScreen == "pos") {
                        OlseraHeaderBar(
                            selectedOrderType = orderType,
                            onInAwayClick = { showOrderTypeDialog = true },
                            selectedTable = viewModel.selectedTable,
                            onTableClick = { showTableInputDialog = true },
                            selectedRightTab = selectedRightTab,
                            onRightTabSelected = { selectedRightTab = it },
                            heldOrdersCount = viewModel.heldOrders.size,
                            onHeldOrdersClick = { viewModel.showHeldOrdersDialog = true },
                            onMenuClick = {
                                coroutineScope.launch {
                                    if (drawerState.isClosed) drawerState.open() else drawerState.close()
                                }
                            }
                        )
                    } else if (currentScreen == "transaksi") {
                        TransaksiHeaderBar(
                            searchQuery = transaksiSearchQuery,
                            onSearchQueryChange = { transaksiSearchQuery = it },
                            currentDate = selectedDateRange,
                            onDateClick = { showDateFilterDialog = true },
                            selectedPaymentType = selectedPaymentType,
                            onPaymentTypeClick = { showPaymentTypeDialog = true },
                            onMenuClick = {
                                coroutineScope.launch {
                                    if (drawerState.isClosed) drawerState.open() else drawerState.close()
                                }
                            }
                        )
                    } else if (currentScreen == "pengaturan") {
                        PengaturanHeaderBar(
                            onMenuClick = {
                                coroutineScope.launch {
                                    if (drawerState.isClosed) drawerState.open() else drawerState.close()
                                }
                            }
                        )
                    }
                },
                modifier = Modifier.fillMaxSize()
            ) { innerPadding ->
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = {
                        if (targetState == "transaksi_detail") {
                            slideInHorizontally(initialOffsetX = { it }) + fadeIn() togetherWith
                            slideOutHorizontally(targetOffsetX = { -it / 2 }) + fadeOut()
                        } else if (initialState == "transaksi_detail") {
                            // Keluar dari detail dibuat langsung menghilang tanpa animasi geser
                            EnterTransition.None togetherWith ExitTransition.None
                        } else {
                            fadeIn() togetherWith fadeOut()
                        }
                    },
                    label = "screen_transition"
                ) { targetScreen ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        when (targetScreen) {
                            "transaksi" -> {
                                TransactionHistoryList(
                                    transactions = filteredTransactions,
                                    isLoading = viewModel.isHistoryLoading,
                                    onTransactionClick = { 
                                        selectedHistoryTransaction = it
                                        currentScreen = "transaksi_detail"
                                    },
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(innerPadding)
                                        .background(Color.White)
                                )
                            }
                            "transaksi_detail" -> {
                                if (selectedHistoryTransaction != null) {
                                    TransactionDetailScreen(
                                        transaction = selectedHistoryTransaction!!,
                                        onBack = { currentScreen = "transaksi" },
                                        onSendToKitchen = {
                                            viewModel.openKitchenPrintDialogFromHistory(context, selectedHistoryTransaction!!)
                                        },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                            "shift" -> {
                                id.my.matahati.pos.ui.screen.shift.ShiftScreen(
                                    shiftViewModel = shiftViewModel,
                                    nidOutlet = nidOutlet,
                                    userName = userName,
                                    onMenuClick = {
                                        coroutineScope.launch {
                                            if (drawerState.isClosed) drawerState.open() else drawerState.close()
                                        }
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            "pengaturan" -> {
                                id.my.matahati.pos.ui.screen.settings.SettingsScreen(
                                    modifier = Modifier.padding(innerPadding)
                                )
                            }
                            else -> {
                                // Default POS Screen
                                if (viewModel.isLoading && allProducts.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(innerPadding),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                } else {
                                    if (isTablet) {
                                        // ==========================================
                                        // OLSERA POS TABLET LANDSCAPE SPLIT-PANE
                                        // ==========================================
                                        Row(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(innerPadding)
                                                .background(Color.White)
                                        ) {
                                            // Left Side: Olsera Order & Cart Panel (40% width)
                                            OlseraCartPanel(
                                                cartItems = cartItems,
                                                customers = viewModel.customers,
                                                orderType = orderType,
                                                onIncreaseQuantity = onIncreaseQuantity,
                                                onDecreaseQuantity = onDecreaseQuantity,
                                                onItemClick = { item ->
                                                    selectedCartItem = item
                                                    showEditDialog = true
                                                },
                                                onDiscountClick = { showDiscountDialog = true },
                                                onClearCart = { 
                                                    showCancelDialog = true
                                                },
                                                onHoldCart = {
                                                    if (cartItems.isNotEmpty()) {
                                                        viewModel.holdCurrentCart(
                                                            cartItems = cartItems,
                                                            orderType = orderType,
                                                            selectedCustomer = selectedCustomer,
                                                            discount = discountAmount,
                                                            tax = 0.0,
                                                            nidOutlet = nidOutlet,
                                                            onSuccess = {
                                                                cartItems.clear()
                                                                orderType = ""
                                                                viewModel.selectedTable = ""
                                                                selectedCustomer = null
                                                                selectedVoucher = null
                                                                manualDiscountInput = ""
                                                            }
                                                        )
                                                    }
                                                },
                                                onSendToKitchenClick = {
                                                    viewModel.openKitchenPrintDialog(context, cartItems)
                                                },
                                                onCheckoutClick = triggerCheckout,
                                                selectedCustomerName = selectedCustomer?.name ?: "",
                                                onCustomerSelected = { customer ->
                                                    selectedCustomer = customer
                                                },
                                                selectedTable = viewModel.selectedTable,
                                                cashierName = userName,
                                                servedByName = viewModel.selectedServedBy?.name ?: userName,
                                                onServedByClick = { viewModel.showServedByDialog = true },
                                                discountAmount = discountAmount,
                                                grandTotal = cartGrandTotal,
                                                modifier = Modifier
                                                    .weight(0.40f)
                                                    .fillMaxHeight()
                                            )

                                            VerticalDivider(color = DividerDefaults.color.copy(alpha = 0.5f))

                                            // Right Side: Dynamic Content based on selectedRightTab (Produk, Barcode, Custom)
                                            Column(
                                                modifier = Modifier
                                                    .weight(0.60f)
                                                    .fillMaxHeight()
                                                    .background(Color(0xFF1565C0)),
                                            ) {
                                                when (selectedRightTab) {
                                                    "Produk" -> {
                                                        // Top sub-bar for Search and Category Filter
                                                        Surface(
                                                            color = Color(0xFF1565C0),
                                                            modifier = Modifier.fillMaxWidth()
                                                        ) {
                                                            Row(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                horizontalArrangement = Arrangement.SpaceBetween
                                                            ) {
                                                                // Search Bar (Left)
                                                                Surface(
                                                                    shape = RoundedCornerShape(20.dp),
                                                                    color = Color.White.copy(alpha = 0.15f),
                                                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                                                                    modifier = Modifier
                                                                        .weight(1f)
                                                                        .height(38.dp)
                                                                ) {
                                                                    Row(
                                                                        modifier = Modifier
                                                                            .fillMaxSize()
                                                                            .padding(horizontal = 12.dp),
                                                                        verticalAlignment = Alignment.CenterVertically
                                                                    ) {
                                                                        Icon(
                                                                            imageVector = Icons.Default.Search,
                                                                            contentDescription = "Cari",
                                                                            tint = Color.White,
                                                                            modifier = Modifier.size(16.dp)
                                                                        )
                                                                        Spacer(modifier = Modifier.width(8.dp))
                                                                        BasicTextField(
                                                                            value = searchQuery,
                                                                            onValueChange = { searchQuery = it },
                                                                            singleLine = true,
                                                                            textStyle = TextStyle(color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium),
                                                                            cursorBrush = SolidColor(Color.White),
                                                                            decorationBox = { innerTextField ->
                                                                                Box(
                                                                                    contentAlignment = Alignment.CenterStart
                                                                                ) {
                                                                                    if (searchQuery.isEmpty()) {
                                                                                        Text(
                                                                                            text = "Cari produk...",
                                                                                            color = Color.White.copy(alpha = 0.6f),
                                                                                            fontSize = 13.sp
                                                                                        )
                                                                                    }
                                                                                    innerTextField()
                                                                                }
                                                                            },
                                                                            modifier = Modifier.fillMaxWidth()
                                                                        )
                                                                    }
                                                                }

                                                                Spacer(modifier = Modifier.width(12.dp))

                                                                // Category Dropdown Filter (Right)
                                                                Box {
                                                                    Surface(
                                                                        shape = RoundedCornerShape(16.dp),
                                                                        color = Color.White.copy(alpha = 0.18f),
                                                                        modifier = Modifier.clickable { isCategoryDropdownExpanded = true }
                                                                    ) {
                                                                        Row(
                                                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                                                            verticalAlignment = Alignment.CenterVertically
                                                                        ) {
                                                                            Text(
                                                                                text = selectedCategoryName,
                                                                                color = Color.White,
                                                                                fontSize = 12.sp,
                                                                                fontWeight = FontWeight.SemiBold,
                                                                                maxLines = 1,
                                                                                overflow = TextOverflow.Ellipsis
                                                                            )
                                                                            Spacer(modifier = Modifier.width(4.dp))
                                                                            Icon(
                                                                                imageVector = Icons.Default.ArrowDropDown,
                                                                                contentDescription = "Pilih Kategori",
                                                                                tint = Color.White,
                                                                                modifier = Modifier.size(18.dp)
                                                                            )
                                                                        }
                                                                    }

                                                                    DropdownMenu(
                                                                        expanded = isCategoryDropdownExpanded,
                                                                        onDismissRequest = { isCategoryDropdownExpanded = false }
                                                                    ) {
                                                                        categories.forEach { category ->
                                                                            DropdownMenuItem(
                                                                                text = { Text(category.name) },
                                                                                onClick = {
                                                                                    selectedCategoryId = category.id
                                                                                    isCategoryDropdownExpanded = false
                                                                                }
                                                                            )
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }

                                                        // Product Grid
                                                        OlseraProductGrid(
                                                            products = filteredProducts,
                                                            onAddToCart = onAddToCart,
                                                            columnsCount = 4,
                                                            modifier = Modifier
                                                                .weight(1f)
                                                                .fillMaxWidth()
                                                        )
                                                    }
                                                    "Barcode" -> {
                                                        BarcodeTabContent(
                                                            products = allProducts,
                                                            onAddToCart = onAddToCart,
                                                            modifier = Modifier.fillMaxSize()
                                                        )
                                                    }
                                                    "Custom" -> {
                                                        CustomDepositTabContent(
                                                            onAddToCart = onAddToCart,
                                                            modifier = Modifier.fillMaxSize()
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        // ==========================================
                                        // PHONE PORTRAIT / SINGLE COLUMN LAYOUT
                                        // ==========================================
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(innerPadding)
                                                .background(Color.White)
                                        ) {
                                            Column(
                                                modifier = Modifier.fillMaxSize()
                                            ) {
                                                when (selectedRightTab) {
                                                    "Produk" -> {
                                                        OlseraProductGrid(
                                                            products = filteredProducts,
                                                            onAddToCart = onAddToCart,
                                                            columnsCount = 2,
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                    }
                                                    "Barcode" -> {
                                                        BarcodeTabContent(
                                                            products = allProducts,
                                                            onAddToCart = onAddToCart,
                                                            modifier = Modifier.weight(1f).fillMaxWidth()
                                                        )
                                                    }
                                                    "Custom" -> {
                                                        CustomDepositTabContent(
                                                            onAddToCart = onAddToCart,
                                                            modifier = Modifier.weight(1f).fillMaxWidth()
                                                        )
                                                    }
                                                }

                                                // Bottom Green Pay Bar
                                                Surface(
                                                    color = OlseraGreenPay,
                                                    shape = RoundedCornerShape(0.dp),
                                                    modifier = Modifier.fillMaxWidth(),
                                                    onClick = triggerCheckout
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(vertical = 14.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = "Rp ${formatRawCurrency(cartGrandTotal)}",
                                                            fontSize = 18.sp,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            color = Color.White
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // =============================================================
    // DATE FILTER DIALOG
    // =============================================================
    if (showDateFilterDialog) {
        OlseraDateFilterDialog(
            onDismiss = { showDateFilterDialog = false },
            onDateSelected = { result ->
                selectedDateRange = result.displayLabel
                selectedStartDate = result.startDate
                selectedEndDate = result.endDate
            }
        )
    }

    // =============================================================
    // PAYMENT TYPE DIALOG
    // =============================================================
    if (showPaymentTypeDialog) {
        OlseraPaymentTypeDialog(
            paymentMethods = viewModel.paymentMethods,
            onDismiss = { showPaymentTypeDialog = false },
            onPaymentSelected = { method ->
                selectedPaymentType = method.name
            }
        )
    }

    // Order Type (In/Away) Selection Dialog
    if (showOrderTypeDialog) {
        OlseraOrderTypeDialog(
            orderTypes = orderTypes,
            onDismiss = { showOrderTypeDialog = false },
            onOrderTypeSelected = { item ->
                orderType = item.value
            }
        )
    }

    // =============================================================
    // CENTER NOTIFICATION POPUP (MODAL OVERLAY)
    // =============================================================
    AnimatedVisibility(
        visible = showNotificationPopup,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    showNotificationPopup = false
                },
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = showNotificationPopup,
                enter = scaleIn(
                    initialScale = 0.1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioHighBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ),
                exit = fadeOut()
            ) {
                Surface(
                    modifier = Modifier
                        .width(600.dp)
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Notifikasi",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF333333)
                            )
                            
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { showNotificationPopup = false },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "✕",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Light,
                                    color = Color.Gray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color(0xFFEEEEEE))
                        )

                        Spacer(modifier = Modifier.height(40.dp))

                        // Empty State Icon & Text
                        Surface(
                            shape = RoundedCornerShape(40.dp),
                            color = Color(0xFFF5F5F5),
                            modifier = Modifier.size(80.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = Color(0xFFBDBDBD),
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Belum Ada Notifikasi",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF424242)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Pemberitahuan transaksi dan informasi toko\nakan muncul di sini.",
                            fontSize = 14.sp,
                            color = Color(0xFF757575),
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(40.dp))

                        // Close Button
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clickable { showNotificationPopup = false },
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF1565C0)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "MENGERTI",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // =============================================================
    // EDIT ITEM DIALOG
    // =============================================================
    if (showEditDialog && selectedCartItem != null) {
        OlseraEditItemDialog(
            cartItem = selectedCartItem!!,
            onDismiss = {
                showEditDialog = false
                selectedCartItem = null
            },
            onConfirmUpdate = { newQty, newNote ->
                onUpdateCartItem(selectedCartItem!!.product.id, newQty, newNote)
            },
            onRemoveItem = {
                onRemoveCartItem(selectedCartItem!!.product.id)
            }
        )
    }

    // =============================================================
    // DISCOUNT / VOUCHER DIALOG
    // =============================================================
    if (showDiscountDialog) {
        OlseraDiscountDialog(
            vouchers = viewModel.vouchers,
            cartSubtotal = cartSubtotal,
            initialManualDiscount = manualDiscountInput,
            onDismiss = { showDiscountDialog = false },
            onVoucherSelected = { voucher ->
                selectedVoucher = voucher
            },
            onManualDiscountApplied = { input ->
                manualDiscountInput = input
            }
        )
    }
    // =============================================================
    // TABLE INPUT DIALOG
    // =============================================================
    if (showTableInputDialog) {
        OlseraTableInputDialog(
            initialTable = viewModel.selectedTable,
            onDismiss = { showTableInputDialog = false },
            onSave = { newTable ->
                viewModel.selectedTable = newTable
                showTableInputDialog = false
            }
        )
    }

    if (viewModel.showServedByDialog) {
        ServedByDialog(
            users = viewModel.servedByUsers,
            selectedUser = viewModel.selectedServedBy,
            onUserSelected = { user ->
                viewModel.selectedServedBy = user
                viewModel.showServedByDialog = false
            },
            onDismiss = { viewModel.showServedByDialog = false }
        )
    }

    // =============================================================
    // CASHIER CONFIRMATION & PAYMENT SCREEN
    // =============================================================
    if (viewModel.showCashierConfirmDialog) {
        CashierConfirmationDialog(
            cashierName = viewModel.selectedServedBy?.name ?: userName,
            onDismiss = { viewModel.showCashierConfirmDialog = false },
            onConfirm = {
                viewModel.showCashierConfirmDialog = false
                viewModel.showPaymentScreen = true
            }
        )
    }

    if (viewModel.showPaymentScreen) {
        PaymentScreen(
            grandTotal = cartGrandTotal,
            paymentMethods = viewModel.paymentMethods,
            isSubmitting = viewModel.isSubmitting,
            errorMessage = viewModel.transactionError,
            onBack = { viewModel.showPaymentScreen = false },
            onPay = { method, amount ->
                viewModel.submitTransaction(
                    context = context,
                    cartItems = cartItems,
                    orderType = orderType,
                    selectedCustomer = selectedCustomer,
                    selectedPayment = method,
                    discount = discountAmount,
                    tax = 0.0,
                    paidAmount = amount,
                    nidVoucher = selectedVoucher?.id?.toIntOrNull(),
                    nidOutlet = nidOutlet
                )
            }
        )
    }

    // =============================================================
    // CANCEL ORDER DIALOG
    // =============================================================
    if (showCancelDialog) {
        CancelOrderDialog(
            onDismiss = { showCancelDialog = false },
            onConfirm = { note ->
                showCancelDialog = false
                viewModel.submitCancelTransaction(
                    context = context,
                    cartItems = cartItems,
                    orderType = orderType,
                    selectedCustomer = selectedCustomer,
                    cancelNote = note,
                    nidOutlet = nidOutlet
                )
            }
        )
    }

    if (viewModel.transactionSuccessMessage != null) {
        AlertDialog(
            onDismissRequest = { 
                viewModel.clearTransactionSuccess() 
                cartItems.clear()
                orderType = ""
                selectedCustomer = null
                viewModel.selectedTable = ""
                selectedVoucher = null
                manualDiscountInput = ""
            },
            containerColor = Color.White,
            modifier = Modifier.width(480.dp).wrapContentHeight(),
            title = { Text("Berhasil", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
            text = { Text(viewModel.transactionSuccessMessage ?: "", fontSize = 16.sp) },
            confirmButton = {
                Button(
                    onClick = { 
                        viewModel.clearTransactionSuccess() 
                        cartItems.clear()
                        orderType = ""
                        selectedCustomer = null
                        viewModel.selectedTable = ""
                        selectedVoucher = null
                        manualDiscountInput = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Selesai", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        )
    }

    if (viewModel.transactionError != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearTransactionError() },
            title = { Text("Transaksi Gagal") },
            text = { Text(viewModel.transactionError ?: "") },
            confirmButton = {
                TextButton(onClick = { viewModel.clearTransactionError() }) {
                    Text("OK")
                }
            }
        )
    }

    if (viewModel.showShiftNotStartedDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.showShiftNotStartedDialog = false },
            title = { Text("Shift Belum Dimulai", fontWeight = FontWeight.Bold) },
            text = { Text("Kasir belum memulai shift. Silakan buka shift terlebih dahulu untuk dapat melakukan transaksi.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.showShiftNotStartedDialog = false
                        currentScreen = "shift"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
                ) {
                    Text("Buka Shift", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.showShiftNotStartedDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    if (validationWarningMessage != null) {
        AlertDialog(
            onDismissRequest = { validationWarningMessage = null },
            title = { Text("Peringatan") },
            text = { Text(validationWarningMessage ?: "") },
            confirmButton = {
                TextButton(onClick = { validationWarningMessage = null }) {
                    Text("OK")
                }
            }
        )
    }

    // =============================================================
    // KITCHEN PRINT DIALOGS
    // =============================================================
    // (Handled inside PaymentScreen during checkout flow)

    if (viewModel.showHeldOrdersDialog) {
        HeldOrdersDialog(
            heldOrders = viewModel.heldOrders,
            onOpenOrder = { heldOrder ->
                cartItems.clear()
                heldOrder.details?.forEach { detail ->
                    val product = allProducts.find { it.id == detail.productId } ?: Product(
                        id = detail.productId,
                        name = detail.productName,
                        price = detail.price.toDoubleOrNull() ?: 0.0,
                        categoryId = "1",
                        stock = 99
                    )
                    val (cleanNote, sentQty) = CartItem.parseNoteAndSentQty(detail.note)
                    cartItems.add(
                        CartItem(
                            product = product,
                            quantity = detail.quantity,
                            note = cleanNote,
                            sentQuantity = sentQty
                        )
                    )
                }
                viewModel.selectedTable = heldOrder.tableName ?: ""
                orderType = heldOrder.orderType ?: ""
                selectedCustomer = viewModel.customers.find { it.name == heldOrder.customerName }
                val draftDiscount = heldOrder.discount.toDoubleOrNull() ?: 0.0
                if (draftDiscount > 0) {
                    manualDiscountInput = draftDiscount.toLong().toString()
                } else {
                    manualDiscountInput = ""
                }
                selectedVoucher = null
                viewModel.removeHeldOrderLocal(heldOrder.id)
                viewModel.deleteHeldOrder(context, heldOrder.id, nidOutlet)
                viewModel.showHeldOrdersDialog = false
            },
            onDismiss = { viewModel.showHeldOrdersDialog = false }
        )
    }

    if (viewModel.showKitchenPrintDialog) {
        KitchenPrintSelectionDialog(
            changesCount = viewModel.printChangesCount,
            availableStations = viewModel.availableStations,
            selectedStations = viewModel.selectedStations,
            savedPrinters = viewModel.savedPrinters,
            onToggleStation = { viewModel.toggleStationSelection(it) },
            onConfirmPrint = { type ->
                viewModel.onConfirmKitchenPrint(
                    type = type,
                    onUpdateActiveCart = { updatedItems ->
                        // Perbarui status sentQuantity di keranjang lokal
                        updatedItems.forEach { updated ->
                            val idx = cartItems.indexOfFirst { it.product.id == updated.product.id }
                            if (idx >= 0) {
                                cartItems[idx] = updated
                            }
                        }
                    }
                )
            },
            onDismiss = { viewModel.closeKitchenPrintDialog() }
        )
    }

    if (viewModel.showSimulatedReceipt) {
        SimulatedReceiptDialog(
            tickets = viewModel.receiptTickets,
            isPrinting = viewModel.isPrinting,
            onPrint = {
                viewModel.printKitchenTickets(context, viewModel.receiptTickets)
            },
            onDismiss = { viewModel.closeSimulatedReceipt() }
        )
    }

    if (viewModel.showReceiptDialog && viewModel.lastTransaction != null) {
        ReceiptDialog(
            transactionData = viewModel.lastTransaction!!,
            cashierName = userName,
            savedOutletName = outletName,
            isPrinting = viewModel.isPrinting,
            onPrint = {
                if (viewModel.selectedPrinterAddress == null) {
                    viewModel.openPrinterSelection(printerManager)
                } else {
                    viewModel.printReceipt(context, viewModel.lastTransaction!!, userName, outletName)
                }
            },
            onDismiss = {
                viewModel.closeReceiptDialog()
                // Jika ini adalah akhir dari transaksi yang baru saja disubmit
                if (currentScreen == "pos") {
                    cartItems.clear()
                    orderType = ""
                    viewModel.selectedTable = ""
                    selectedCustomer = null
                    selectedVoucher = null
                    manualDiscountInput = ""
                    viewModel.showPaymentScreen = false
                }
            }
        )
    }

    if (viewModel.isSubmitting) {
        // Loading Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color.White)
        }
    }
}

private fun parseManualDiscount(input: String, subtotal: Double): Double {
    if (input.isBlank()) return 0.0
    return try {
        if (input.endsWith("%")) {
            val percent = input.removeSuffix("%").trim().toDoubleOrNull() ?: 0.0
            subtotal * (percent / 100.0)
        } else {
            input.trim().toDoubleOrNull() ?: 0.0
        }
    } catch (e: Exception) {
        0.0
    }
}

private fun formatRawCurrency(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"))
    formatter.maximumFractionDigits = 0
    return formatter.format(amount)
}

@Preview(name = "Phone Portrait", showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPhonePreview() {
    MobileMatahati_POSTheme {
        HomeScreen()
    }
}

@Preview(
    name = "Tablet Landscape Olsera POS",
    device = "spec:width=1280dp,height=800dp,dpi=240",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun HomeScreenTabletPreview() {
    MobileMatahati_POSTheme {
        HomeScreen()
    }
}
