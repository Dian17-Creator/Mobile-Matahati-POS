package id.my.matahati.pos.ui.screen.settings

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import id.my.matahati.pos.model.LocalPrinter
import id.my.matahati.pos.model.PrinterRole
import id.my.matahati.pos.model.PrinterType

val SettingsBlue = Color(0xFF1565C0)

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel()
) {
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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Konfigurasi Printer",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray,
                    modifier = Modifier.weight(1f, fill = false),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = { requestBluetoothAndAdd() },
                    colors = ButtonDefaults.buttonColors(containerColor = SettingsBlue),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Tambah",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Tambah Printer",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (printers.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Belum ada printer yang dikonfigurasi.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(printers) { printer ->
                        PrinterCard(
                            printer = printer,
                            onEdit = {
                                viewModel.editingPrinter = printer
                                viewModel.showAddDialog = true
                            },
                            onDelete = { viewModel.deletePrinter(printer.id) }
                        )
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 720.dp)
            .clickable { onEdit() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Printer Icon Container
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SettingsBlue.copy(alpha = 0.1f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (printer.type == PrinterType.BLUETOOTH) Icons.Default.BluetoothConnected else Icons.Default.Print,
                        contentDescription = null,
                        tint = SettingsBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

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
                        fontSize = 16.sp,
                        color = Color(0xFF212121)
                    )
                    
                    // Role Badge (Pill)
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when (printer.role) {
                            PrinterRole.RECEIPT -> Color(0xFF1565C0) // Blue
                            PrinterRole.KITCHEN -> Color(0xFFE65100) // Orange
                            PrinterRole.BAR -> Color(0xFF6A1B9A) // Purple
                        }
                    ) {
                        Text(
                            text = printer.role.name,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (printer.type == PrinterType.BLUETOOTH) Icons.Default.BluetoothConnected else Icons.Default.Lan,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (printer.type == PrinterType.TCP_IP) "${printer.address}:${printer.port}" else printer.address,
                        fontSize = 13.sp,
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