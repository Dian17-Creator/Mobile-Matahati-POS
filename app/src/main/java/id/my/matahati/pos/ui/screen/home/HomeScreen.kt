package id.my.matahati.pos.ui.screen.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
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
import id.my.matahati.pos.ui.screen.home.components.OlseraCartPanel
import id.my.matahati.pos.ui.screen.home.components.OlseraGreenPay
import id.my.matahati.pos.ui.screen.home.components.OlseraHeaderBar
import id.my.matahati.pos.ui.screen.home.components.OlseraHeaderBlue
import id.my.matahati.pos.ui.screen.home.components.OlseraProductGrid
import id.my.matahati.pos.ui.screen.home.components.SidebarDrawer
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
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf("all") }
    var orderType by remember { mutableStateOf("") }
    var showOrderTypeDialog by remember { mutableStateOf(false) }
    var selectedRightTab by remember { mutableStateOf("Produk") }
    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }
    val cartItems = remember { mutableStateListOf<CartItem>() }

    val selectedCategoryName = categories.find { it.id == selectedCategoryId }?.name ?: "Semua Kategori"

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
                    onLogout = onLogout
                )
            }
        ) {
            Scaffold(
                topBar = {
                    OlseraHeaderBar(
                        selectedOrderType = orderType,
                        onInAwayClick = { showOrderTypeDialog = true },
                        selectedRightTab = selectedRightTab,
                        onRightTabSelected = { selectedRightTab = it },
                        onMenuClick = {
                            coroutineScope.launch {
                                if (drawerState.isClosed) drawerState.open() else drawerState.close()
                            }
                        }
                    )
                },
                modifier = Modifier.fillMaxSize()
            ) { innerPadding ->
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

    // Order Type (In/Away) Selection Dialog
    if (showOrderTypeDialog) {
        AlertDialog(
            onDismissRequest = { showOrderTypeDialog = false },
            title = { Text("In/Away", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    val types = listOf("DINE-IN", "TAKE-AWAY", "DELIVERY", "GOFOOD", "GRABFOOD", "SHOPEEFOOD", "TRAVELOKA-EATS", "MAXIMFOOD", "+REMARK")
                    items(types) { type ->
                        TextButton(
                            onClick = {
                                orderType = type
                                showOrderTypeDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = type,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.DarkGray,
                                textAlign = TextAlign.Start,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showOrderTypeDialog = false }) {
                    Text("BATAL", color = OlseraHeaderBlue, fontWeight = FontWeight.Bold)
                }
            }
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
