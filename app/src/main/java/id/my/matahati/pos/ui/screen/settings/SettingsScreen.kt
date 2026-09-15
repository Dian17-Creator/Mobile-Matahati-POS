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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
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
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().clickable { onEdit() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = printer.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Fungsi: ${printer.role.name}",
                        fontSize = 13.sp,
                        color = Color.DarkGray,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Tipe: ${printer.type.name}",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                    if (printer.type == PrinterType.TCP_IP) {
                        Text(
                            text = "${printer.address}:${printer.port}",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    } else {
                        Text(
                            text = printer.address,
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                }

                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color.Red)
                }
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
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 24.dp)
            .fillMaxWidth()
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

                // Type Selection
                Text("Tipe Koneksi", fontSize = 12.sp, color = Color.Gray)
                Row(modifier = Modifier.fillMaxWidth()) {
                    RadioButton(
                        selected = selectedType == PrinterType.BLUETOOTH,
                        onClick = { selectedType = PrinterType.BLUETOOTH }
                    )
                    Text("Bluetooth", modifier = Modifier.align(Alignment.CenterVertically))
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(
                        selected = selectedType == PrinterType.TCP_IP,
                        onClick = { selectedType = PrinterType.TCP_IP }
                    )
                    Text("TCP/IP", modifier = Modifier.align(Alignment.CenterVertically))
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
                                role = selectedRole
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
                                    role = selectedRole
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
}