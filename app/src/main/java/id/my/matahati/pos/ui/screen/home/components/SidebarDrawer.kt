package id.my.matahati.pos.ui.screen.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.my.matahati.pos.R
import id.my.matahati.pos.model.OutletItem

@Composable
fun SidebarDrawer(
    userName: String,
    roleOwner: Boolean,
    roleCashier: Boolean,
    roleCaptain: Boolean,
    currentScreen: String,
    onNavigateToPos: () -> Unit,
    onNavigateToTransaksi: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToShift: () -> Unit = {},
    onNavigateToLaporan: () -> Unit = {},
    onNavigateToPengaturan: () -> Unit = {},
    outletName: String? = null,
    activeOutletId: String? = null,
    outlets: List<OutletItem> = emptyList(),
    onSelectOutlet: (OutletItem) -> Unit = {}
) {
    var isAccountMenuExpanded by remember { mutableStateOf(false) }
    var showOutletDialog by remember { mutableStateOf(false) }

    val rolesList = mutableListOf<String>()
    if (roleOwner) rolesList.add("Owner")
    if (roleCashier) rolesList.add("Kasir")
    if (roleCaptain) rolesList.add("Captain")

    val displayStoreName = if (!outletName.isNullOrBlank()) outletName else "IQOS Matahati Tulungagung"
    val canSwitchOutlet = outlets.size > 1

    ModalDrawerSheet(
        modifier = modifier.width(300.dp),
        drawerShape = RoundedCornerShape(topEnd = 0.dp, bottomEnd = 0.dp),
        drawerContainerColor = Color.White,
        windowInsets = WindowInsets(0.dp)
    ) {
        // Drawer Header with blue background extending to the top
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(OlseraBlueHeader)
                .statusBarsPadding()
                .padding(top = 24.dp, start = 12.dp, end = 12.dp, bottom = 12.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_pos_profile),
                        contentDescription = "Profile Avatar",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { isAccountMenuExpanded = !isAccountMenuExpanded }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = displayStoreName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = userName,
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }

                    Icon(
                        imageVector = if (isAccountMenuExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                        contentDescription = "Toggle Account Menu",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (isAccountMenuExpanded) {
            // Account Submenu
            NavigationDrawerItem(
                icon = {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_pos_profile),
                            contentDescription = "User Avatar",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                },
                label = {
                    Text(
                        text = userName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = OlseraBlueHeader
                    )
                },
                selected = true,
                onClick = { /* Stay on current account */ },
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = Color(0xFFE3F2FD)
                ),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Opsi Switch Outlet (Diletakkan di atas tombol Keluar)
            if (canSwitchOutlet) {
                NavigationDrawerItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = "Ganti Outlet",
                            tint = OlseraBlueHeader
                        )
                    },
                    label = {
                        Column {
                            Text(
                                text = "Ganti Outlet",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = OlseraBlueHeader
                            )
                            Text(
                                text = displayStoreName,
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    },
                    selected = false,
                    onClick = { showOutletDialog = true },
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            val logoutRed = Color(0xFFE53935)
            NavigationDrawerItem(
                icon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Logout",
                        tint = logoutRed
                    )
                },
                label = {
                    Text(
                        text = "Keluar",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = logoutRed
                    )
                },
                selected = false,
                onClick = onLogout,
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        } else {
            // Main Navigation Menu
            NavigationDrawerItem(
                icon = { Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = "Point of Sale") },
                label = { Text("Point of Sale", fontWeight = FontWeight.SemiBold, fontSize = 15.sp) },
                selected = currentScreen == "pos",
                onClick = onNavigateToPos,
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )

            NavigationDrawerItem(
                icon = { Icon(imageVector = Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = "Transaksi") },
                label = { Text("Transaksi", fontWeight = FontWeight.SemiBold, fontSize = 15.sp) },
                selected = currentScreen == "transaksi",
                onClick = onNavigateToTransaksi,
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )

            NavigationDrawerItem(
                icon = { Icon(imageVector = Icons.Default.Schedule, contentDescription = "Shift") },
                label = { Text("Shift", fontWeight = FontWeight.SemiBold, fontSize = 15.sp) },
                selected = currentScreen == "shift",
                onClick = onNavigateToShift,
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )

            NavigationDrawerItem(
                icon = { Icon(imageVector = Icons.Default.BarChart, contentDescription = "Laporan") },
                label = { Text("Laporan", fontWeight = FontWeight.SemiBold, fontSize = 15.sp) },
                selected = currentScreen == "laporan",
                onClick = onNavigateToLaporan,
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = Color.LightGray.copy(alpha = 0.5f)
            )

            NavigationDrawerItem(
                icon = { Icon(imageVector = Icons.Default.Settings, contentDescription = "Pengaturan") },
                label = { Text("Pengaturan", fontWeight = FontWeight.SemiBold, fontSize = 15.sp) },
                selected = currentScreen == "pengaturan",
                onClick = onNavigateToPengaturan,
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        }
    }

    if (showOutletDialog) {
        OutletSelectionDialog(
            outlets = outlets,
            currentOutletId = activeOutletId,
            onSelect = { selectedOutlet ->
                showOutletDialog = false
                if (selectedOutlet.id.toString() != activeOutletId) {
                    onSelectOutlet(selectedOutlet)
                }
            },
            onDismiss = { showOutletDialog = false }
        )
    }
}

@Composable
fun OutletSelectionDialog(
    outlets: List<OutletItem>,
    currentOutletId: String?,
    onSelect: (OutletItem) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        modifier = Modifier
            .width(360.dp)
            .wrapContentHeight(),
        title = {
            Text(
                text = "Pilih Outlet",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                outlets.forEach { outlet ->
                    val isSelected = outlet.id.toString() == currentOutletId
                    Surface(
                        onClick = { onSelect(outlet) },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) Color(0xFFE3F2FD) else Color(0xFFF8F9FA),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) OlseraBlueHeader else Color.LightGray.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = outlet.name,
                                fontSize = 15.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) OlseraBlueHeader else Color.DarkGray,
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = OlseraBlueHeader,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = Color.Gray, fontWeight = FontWeight.Bold)
            }
        }
    )
}
