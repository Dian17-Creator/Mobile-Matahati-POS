package id.my.matahati.pos.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.my.matahati.pos.data.DummyData
import id.my.matahati.pos.model.CartItem
import id.my.matahati.pos.model.Product
import id.my.matahati.pos.ui.screen.home.components.OlseraCartPanel
import id.my.matahati.pos.ui.screen.home.components.OlseraGreenPay
import id.my.matahati.pos.ui.screen.home.components.OlseraHeaderBar
import id.my.matahati.pos.ui.screen.home.components.OlseraProductGrid
import id.my.matahati.pos.ui.screen.home.components.SidebarDrawer
import id.my.matahati.pos.ui.theme.MobileMatahati_POSTheme
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@Composable
fun HomeScreen(
    userName: String = "Kasir",
    roleOwner: Boolean = false,
    roleCashier: Boolean = true,
    roleCaptain: Boolean = false,
    onLogout: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Drawer State
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    // Dummy Data State
    val categories = remember { DummyData.categories }
    val allProducts = remember { DummyData.products }

    // Interactive UI State
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf("all") }
    val cartItems = remember { mutableStateListOf<CartItem>() }

    // Pre-populate initial dummy cart item (Burger Ayam 1x = Rp 21.000) matching reference photo
    androidx.compose.runtime.LaunchedEffect(Unit) {
        if (cartItems.isEmpty()) {
            val burgerProduct = allProducts.find { it.name == "BURGER AYAM" }
            if (burgerProduct != null) {
                cartItems.add(CartItem(product = burgerProduct, quantity = 1))
            }
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
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        categories = categories,
                        selectedCategoryId = selectedCategoryId,
                        onCategorySelected = { selectedCategoryId = it },
                        onMenuClick = {
                            coroutineScope.launch {
                                if (drawerState.isClosed) drawerState.open() else drawerState.close()
                            }
                        }
                    )
                },
                modifier = Modifier.fillMaxSize()
            ) { innerPadding ->
                if (isTablet) {
                    // ==========================================
                    // OLSERA POS TABLET LANDSCAPE SPLIT-PANE
                    // ==========================================
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(Color(0xFFECEFF1))
                    ) {
                        // Left Side: Olsera Order & Cart Panel (40% width)
                        OlseraCartPanel(
                            cartItems = cartItems,
                            onIncreaseQuantity = onIncreaseQuantity,
                            onDecreaseQuantity = onDecreaseQuantity,
                            onClearCart = { cartItems.clear() },
                            onCheckoutClick = { },
                            customerName = "A.N DITO",
                            orderType = "DINE-IN",
                            cashierName = userName,
                            modifier = Modifier
                                .weight(0.40f)
                                .fillMaxHeight()
                        )

                        VerticalDivider(color = DividerDefaults.color.copy(alpha = 0.5f))

                        // Right Side: Scrollable Olsera Product Grid (60% width)
                        OlseraProductGrid(
                            products = filteredProducts,
                            onAddToCart = onAddToCart,
                            columnsCount = 4,
                            modifier = Modifier
                                .weight(0.60f)
                                .fillMaxHeight()
                        )
                    }
                } else {
                    // ==========================================
                    // PHONE PORTRAIT / SINGLE COLUMN LAYOUT
                    // ==========================================
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(Color(0xFFECEFF1))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            OlseraProductGrid(
                                products = filteredProducts,
                                onAddToCart = onAddToCart,
                                columnsCount = 2,
                                modifier = Modifier.weight(1f)
                            )

                            // Bottom Green Pay Bar
                            Surface(
                                color = OlseraGreenPay,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { }
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
