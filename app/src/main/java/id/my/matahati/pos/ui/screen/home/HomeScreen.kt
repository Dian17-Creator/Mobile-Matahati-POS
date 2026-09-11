package id.my.matahati.pos.ui.screen.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import id.my.matahati.pos.model.CartItem
import id.my.matahati.pos.model.Product
import id.my.matahati.pos.ui.screen.home.components.*
import id.my.matahati.pos.ui.theme.MobileMatahati_POSTheme
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    userName: String = "Kasir",
    roleOwner: Boolean = false,
    roleCashier: Boolean = true,
    roleCaptain: Boolean = false,
    onLogout: () -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
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
    var selectedDateRange by remember { mutableStateOf("10 Sep 2026") }
    var selectedPaymentType by remember { mutableStateOf("Semua Tipe Pembayaran") }
    
    val cartItems = remember { mutableStateListOf<CartItem>() }

    val selectedCategoryName = categories.find { it.id == selectedCategoryId }?.name ?: "Semua Kategori"

    val orderTypes by viewModel.orderTypes.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadOrderTypes()
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
            if (item.quantity > 1) {
                cartItems[index] = item.copy(quantity = item.quantity - 1)
            } else {
                cartItems.removeAt(index)
            }
        }
    }

    val onUpdateCartItem: (String, Int, String) -> Unit = { productId, newQty, newNote ->
        val index = cartItems.indexOfFirst { it.product.id == productId }
        if (index >= 0) {
            cartItems[index] = cartItems[index].copy(quantity = newQty, note = newNote)
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

    // Cart Total Calculation
    val cartTotalAmount by remember {
        derivedStateOf { cartItems.sumOf { it.totalPrice } }
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
                            selectedRightTab = selectedRightTab,
                            onRightTabSelected = { selectedRightTab = it },
                            onNotificationClick = { showNotificationPopup = true },
                            onMenuClick = {
                                coroutineScope.launch {
                                    if (drawerState.isClosed) drawerState.open() else drawerState.close()
                                }
                            }
                        )
                    } else {
                        var transaksiSearchQuery by remember { mutableStateOf("") }
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
                    }
                },
                modifier = Modifier.fillMaxSize()
            ) { innerPadding ->
                if (currentScreen == "transaksi") {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Tidak ada transaksi ditemukan",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFBDBDBD)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tidak ada transaksi untuk tanggal yang dipilih",
                                fontSize = 14.sp,
                                color = Color(0xFFE0E0E0),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else if (viewModel.isLoading && allProducts.isEmpty()) {
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
                                onClearCart = { cartItems.clear() },
                                onCheckoutClick = { },
                                cashierName = userName,
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
                                    .background(Color.White)
                            ) {
                                when (selectedRightTab) {
                                    "Produk" -> {
                                        // Top sub-bar for Search and Category Filter
                                        Surface(
                                            color = Color(0xFF1E88E5),
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
                                                                text = { Text("${category.iconEmoji} ${category.name}") },
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
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "Halaman Barcode",
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.DarkGray
                                            )
                                        }
                                    }
                                    "Custom" -> {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "Halaman Custom",
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.DarkGray
                                            )
                                        }
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
                                        Box(
                                            modifier = Modifier.weight(1f).fillMaxWidth(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "Halaman Barcode",
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.DarkGray
                                            )
                                        }
                                    }
                                    "Custom" -> {
                                        Box(
                                            modifier = Modifier.weight(1f).fillMaxWidth(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "Halaman Custom",
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.DarkGray
                                            )
                                        }
                                    }
                                }

                                // Bottom Green Pay Bar
                                Surface(
                                    color = OlseraGreenPay,
                                    shape = RoundedCornerShape(0.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = { }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 14.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Rp ${formatRawCurrency(cartTotalAmount)}",
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

    // =============================================================
    // DATE FILTER DIALOG
    // =============================================================
    if (showDateFilterDialog) {
        OlseraDateFilterDialog(
            onDismiss = { showDateFilterDialog = false },
            onDateSelected = { newDateRange ->
                selectedDateRange = newDateRange
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
            onDismiss = { showDiscountDialog = false }
        )
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
