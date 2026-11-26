package id.my.matahati.pos.ui.screen.settings

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import id.my.matahati.pos.model.CartMenuItem
import id.my.matahati.pos.model.LocalPrinter
import id.my.matahati.pos.model.PrinterRole
import id.my.matahati.pos.model.PrinterType

import id.my.matahati.pos.ui.theme.AppPrimaryColor

val SettingsBlue = AppPrimaryColor

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel()
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    val printers = viewModel.printers

    // Handle permissions for Bluetooth
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all { it.value }
        if (granted) {
            viewModel.showAddDialog = true
        }
    }

    val requestBluetoothAndAdd = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN
                )
            )
        } else {
            viewModel.showAddDialog = true
        }
    }

    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // =========================================================
            // SECTION 1: KONFIGURASI PRINTER
            // =========================================================
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header Printer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SettingsBlue.copy(alpha = 0.1f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Print,
                                    contentDescription = null,
                                    tint = SettingsBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Konfigurasi Printer",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF212121)
                            )
                            Text(
                                text = "Kelola printer struk, dapur, dan bar",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    if (isTablet) {
                        Button(
                            onClick = { requestBluetoothAndAdd() },
                            colors = ButtonDefaults.buttonColors(containerColor = SettingsBlue),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Tambah",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Tambah Printer",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    } else {
                        Surface(
                            onClick = { requestBluetoothAndAdd() },
                            shape = RoundedCornerShape(8.dp),
                            color = SettingsBlue,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Tambah Printer",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }

                // Cards List or Empty State
                if (printers.isEmpty()) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Print,
                                    contentDescription = null,
                                    tint = Color.LightGray,
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    text = "Belum ada printer yang dikonfigurasi.",
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        printers.forEach { printer ->
                            PrinterCard(
                                printer = printer,
                                onEdit = {
                                    viewModel.editingPrinter = printer
                                    viewModel.showAddDialog = true
                                },
                                onDelete = { viewModel.printerToDelete = printer }
                            )
                        }
                    }
                }
            }

            // =========================================================
            // SECTION 2: PEMBAYARAN (PAYMENT SETTINGS)
            // =========================================================
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header Pembayaran
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SettingsBlue.copy(alpha = 0.1f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Payments,
                                contentDescription = null,
                                tint = SettingsBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Pembayaran",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF212121)
                        )
                        Text(
                            text = "Pengaturan alur konfirmasi dan transaksi",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                // Payment Options Card (Combined Konfirmasi Kasir, Preview Cetak, and Tampilkan Dialog Cetak PDF)
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Setting 1: Konfirmasi Kasir
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 16.dp)
                            ) {
                                Text(
                                    text = "Konfirmasi Kasir",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF212121)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Tampilkan opsi konfirmasi kasir sebelum masuk ke halaman pembayaran",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    lineHeight = 16.sp
                                )
                            }

                            Switch(
                                checked = viewModel.isConfirmOrderEnabled,
                                onCheckedChange = { viewModel.updateConfirmOrderEnabled(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = SettingsBlue,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = Color.LightGray
                                )
                            )
                        }

                        HorizontalDivider(color = Color(0xFFEEEEEE))

                        // Setting 2: Preview Cetak
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 16.dp)
                            ) {
                                Text(
                                    text = "Preview Cetak",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF212121)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Tampilkan pop-up preview invoice dan pesanan sebelum mencetak ke printer",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    lineHeight = 16.sp
                                )
                            }

                            Switch(
                                checked = viewModel.isShowPrintPreviewEnabled,
                                onCheckedChange = { viewModel.updateShowPrintPreviewEnabled(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = SettingsBlue,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = Color.LightGray
                                )
                            )
                        }

                        HorizontalDivider(color = Color(0xFFEEEEEE))

                        // Setting 3: Tampilkan Dialog Cetak PDF
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 16.dp)
                            ) {
                                Text(
                                    text = "Tampilkan Dialog Cetak PDF",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF212121)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Tampilkan pop-up konfirmasi cetak PDF setelah transaksi diselesaikan",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    lineHeight = 16.sp
                                )
                            }

                            Switch(
                                checked = viewModel.isShowPdfDialogEnabled,
                                onCheckedChange = { viewModel.updateShowPdfDialogEnabled(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = SettingsBlue,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = Color.LightGray
                                )
                            )
                        }
                    }
                }
            }

            // =========================================================
            // SECTION 3: TAMPILAN MENU KERANJANG
            // =========================================================
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header Tampilan Menu Keranjang
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SettingsBlue.copy(alpha = 0.1f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = null,
                                tint = SettingsBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Tampilan Menu Keranjang",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF212121)
                        )
                        Text(
                            text = "Pilih menu tombol aksi cepat yang akan ditampilkan pada keranjang",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                // List of Cart Menu Options Card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CartMenuItem.entries.forEachIndexed { index, menuItem ->
                            val isChecked = viewModel.cartMenuStates[menuItem] ?: menuItem.defaultEnabled
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (menuItem.isDestructive) Color(0xFFFFEBEE) else SettingsBlue.copy(alpha = 0.1f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        val resId = menuItem.iconResId
                                        if (resId != null) {
                                            Icon(
                                                painter = painterResource(id = resId),
                                                contentDescription = null,
                                                tint = if (menuItem.isDestructive) Color(0xFFD32F2F) else SettingsBlue,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        } else {
                                            Icon(
                                                imageVector = menuItem.icon,
                                                contentDescription = null,
                                                tint = if (menuItem.isDestructive) Color(0xFFD32F2F) else SettingsBlue,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 16.dp)
                                ) {
                                    Text(
                                        text = menuItem.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF212121)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = menuItem.description,
                                        fontSize = 12.sp,
                                        color = Color.Gray,
                                        lineHeight = 16.sp
                                    )
                                }

                                Switch(
                                    checked = isChecked,
                                    onCheckedChange = { enabled ->
                                        viewModel.updateCartMenuItemEnabled(menuItem, enabled)
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = SettingsBlue,
                                        uncheckedThumbColor = Color.White,
                                        uncheckedTrackColor = Color.LightGray
                                    )
                                )
                            }

                            if (index < CartMenuItem.entries.size - 1) {
                                HorizontalDivider(
                                    color = Color.LightGray.copy(alpha = 0.3f),
                                    thickness = 1.dp,
                                    modifier = Modifier.padding(horizontal = 20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (viewModel.printerToDelete != null) {
        val printer = viewModel.printerToDelete!!
        Dialog(onDismissRequest = { viewModel.printerToDelete = null }) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color.White,
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Konfirmasi Hapus",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Apakah Anda yakin ingin menghapus printer ${printer.name}?",
                        fontSize = 15.sp,
                        color = Color.DarkGray
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.printerToDelete = null },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.DarkGray)
                        ) {
                            Text("BATAL", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        Button(
                            onClick = {
                                viewModel.deletePrinter(printer.id)
                                viewModel.printerToDelete = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Text("HAPUS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }

    if (viewModel.showAddDialog) {
        PrinterConfigDialog(
            viewModel = viewModel,
            initialPrinter = viewModel.editingPrinter,
            onDismiss = { 
                viewModel.showAddDialog = false
                viewModel.editingPrinter = null
            },
            onSave = { printer ->
                viewModel.addOrUpdatePrinter(printer)
            },
            onTestPrint = { printer ->
                viewModel.testPrint(printer)
            }
        )
    }

    if (viewModel.testResultMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearTestMessage() },
            title = { Text("Informasi") },
            text = { Text(viewModel.testResultMessage ?: "") },
            confirmButton = {
                TextButton(onClick = { viewModel.clearTestMessage() }) {
                    Text("OK")
                }
            }
        )
    }

    if (viewModel.isTestingConnection) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color.White)
        }
    }
}

@Composable
fun PrinterCard(
    printer: LocalPrinter,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
//            // Printer Icon Container
//            Surface(
//                shape = RoundedCornerShape(10.dp),
//                color = SettingsBlue.copy(alpha = 0.1f),
//                modifier = Modifier.size(44.dp)
//            ) {
//                Box(contentAlignment = Alignment.Center) {
//                    Icon(
//                        imageVector = if (printer.type == PrinterType.BLUETOOTH) Icons.Default.BluetoothConnected else Icons.Default.Print,
//                        contentDescription = null,
//                        tint = SettingsBlue,
//                        modifier = Modifier.size(22.dp)
//                    )
//                }
//            }
//
//            Spacer(modifier = Modifier.width(16.dp))

            // Printer Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = printer.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF212121)
                    )
                    
                    // Role Badge (Pill)
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when (printer.role) {
                            PrinterRole.RECEIPT -> AppPrimaryColor // Blue
                            PrinterRole.KITCHEN -> Color(0xFFE65100) // Orange
                            PrinterRole.BAR -> Color(0xFF6A1B9A) // Purple
                        }
                    ) {
                        Text(
                            text = printer.role.name,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    if (printer.copies > 1) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFEEEEEE)
                        ) {
                            Text(
                                text = "${printer.copies}x Salinan",
                                color = Color.DarkGray,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (printer.type == PrinterType.BLUETOOTH) Icons.Default.BluetoothConnected else Icons.Default.Lan,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = if (printer.type == PrinterType.TCP_IP) "${printer.address}:${printer.port}" else printer.address,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            // Delete Action Button with subtle background
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFFFFEBEE), RoundedCornerShape(8.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Hapus",
                    tint = Color(0xFFD32F2F),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrinterConfigDialog(
    viewModel: SettingsViewModel,
    initialPrinter: LocalPrinter?,
    onDismiss: () -> Unit,
    onSave: (LocalPrinter) -> Unit,
    onTestPrint: (LocalPrinter) -> Unit
) {
    var selectedRole by remember { mutableStateOf(initialPrinter?.role ?: PrinterRole.RECEIPT) }
    var selectedType by remember { mutableStateOf(initialPrinter?.type ?: PrinterType.BLUETOOTH) }
    
    var printerName by remember { mutableStateOf(initialPrinter?.name ?: "") }
    var ipAddress by remember { 
        mutableStateOf(if (initialPrinter?.type == PrinterType.TCP_IP) initialPrinter.address else "") 
    }
    var port by remember { 
        mutableStateOf(if (initialPrinter?.type == PrinterType.TCP_IP) (initialPrinter.port?.toString() ?: "9100") else "9100") 
    }
    var copies by remember { mutableStateOf(initialPrinter?.copies ?: 1) }
    var showCopiesDialog by remember { mutableStateOf(false) }

    // Bluetooth selection
    val pairedDevices = remember { viewModel.getPairedBluetoothDevices() }
    var selectedMac by remember { 
        mutableStateOf(if (initialPrinter?.type == PrinterType.BLUETOOTH) initialPrinter.address else "") 
    }
    var selectedDeviceName by remember { 
        mutableStateOf(if (initialPrinter?.type == PrinterType.BLUETOOTH) initialPrinter.name else "Pilih Perangkat") 
    }
    var btDropdownExpanded by remember { mutableStateOf(false) }

    var roleDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        modifier = Modifier
            .padding(16.dp)
            .widthIn(max = 480.dp)
            .wrapContentHeight(),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (initialPrinter == null) "Tambah Printer" else "Edit Printer",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Black
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Tutup",
                        tint = Color.Gray
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Role Selection
                Text("Fungsi Printer", fontSize = 12.sp, color = Color.Gray)
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { roleDropdownExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(selectedRole.name)
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = roleDropdownExpanded,
                        onDismissRequest = { roleDropdownExpanded = false }
                    ) {
                        PrinterRole.entries.forEach { role ->
                            DropdownMenuItem(
                                text = { Text(role.name) },
                                onClick = { 
                                    selectedRole = role
                                    roleDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Type Selection (Modern Segmented Pill Toggle)
                Text("Tipe Koneksi", fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .background(Color(0xFFF0F0F0), RoundedCornerShape(22.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val isBt = selectedType == PrinterType.BLUETOOTH
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (isBt) SettingsBlue else Color.Transparent)
                            .clickable { selectedType = PrinterType.BLUETOOTH },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Bluetooth",
                            color = if (isBt) Color.White else Color.DarkGray,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }

                    val isTcp = selectedType == PrinterType.TCP_IP
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (isTcp) SettingsBlue else Color.Transparent)
                            .clickable { selectedType = PrinterType.TCP_IP },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "TCP/IP",
                            color = if (isTcp) Color.White else Color.DarkGray,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (selectedType == PrinterType.TCP_IP) {
                    OutlinedTextField(
                        value = printerName,
                        onValueChange = { printerName = it },
                        label = { Text("Nama Printer") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = ipAddress,
                        onValueChange = { ipAddress = it },
                        label = { Text("IP Address") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = port,
                        onValueChange = { port = it },
                        label = { Text("Port") },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text("Perangkat Bluetooth", fontSize = 12.sp, color = Color.Gray)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { btDropdownExpanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(selectedDeviceName)
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                        DropdownMenu(
                            expanded = btDropdownExpanded,
                            onDismissRequest = { btDropdownExpanded = false }
                        ) {
                            if (pairedDevices.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("Tidak ada perangkat paired") },
                                    onClick = { btDropdownExpanded = false }
                                )
                            } else {
                                pairedDevices.forEach { device ->
                                    DropdownMenuItem(
                                        text = { Text(device.name ?: device.address) },
                                        onClick = {
                                            selectedMac = device.address
                                            selectedDeviceName = device.name ?: device.address
                                            printerName = selectedDeviceName
                                            btDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                // Jumlah Salinan Section
                Text("Jumlah Salinan", fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedButton(
                    onClick = { showCopiesDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.DarkGray)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = copies.toString(), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.Gray)
                    }
                }
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Button(
                    onClick = {
                        val address = if (selectedType == PrinterType.TCP_IP) ipAddress else selectedMac
                        val pPort = if (selectedType == PrinterType.TCP_IP) port.toIntOrNull() ?: 9100 else null
                        if (printerName.isNotBlank() && address.isNotBlank()) {
                            val printerToTest = LocalPrinter(
                                id = initialPrinter?.id ?: java.util.UUID.randomUUID().toString(),
                                name = printerName,
                                type = selectedType,
                                address = address,
                                port = pPort,
                                role = selectedRole,
                                copies = copies
                            )
                            onTestPrint(printerToTest)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Test Cetak", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        val address = if (selectedType == PrinterType.TCP_IP) ipAddress else selectedMac
                        val pPort = if (selectedType == PrinterType.TCP_IP) port.toIntOrNull() ?: 9100 else null
                        if (printerName.isNotBlank() && address.isNotBlank()) {
                            onSave(
                                LocalPrinter(
                                    id = initialPrinter?.id ?: java.util.UUID.randomUUID().toString(),
                                    name = printerName,
                                    type = selectedType,
                                    address = address,
                                    port = pPort,
                                    role = selectedRole,
                                    copies = copies
                                )
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SettingsBlue),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Simpan", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = null
    )

    if (showCopiesDialog) {
        AlertDialog(
            onDismissRequest = { showCopiesDialog = false },
            containerColor = Color.White,
            title = { Text("Jumlah Salinan", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    listOf(1, 2, 3).forEach { count ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    copies = count
                                    showCopiesDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp)
                        ) {
                            Text(
                                text = count.toString(),
                                fontSize = 16.sp,
                                fontWeight = if (copies == count) FontWeight.Bold else FontWeight.Normal,
                                color = if (copies == count) SettingsBlue else Color.DarkGray
                            )
                        }
                        if (count < 3) {
                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f), thickness = 0.5.dp)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showCopiesDialog = false }) {
                    Text("BATAL", color = SettingsBlue, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
