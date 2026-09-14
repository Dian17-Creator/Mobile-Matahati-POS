package id.my.matahati.pos.ui.screen.home.components

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@SuppressLint("MissingPermission")
@Composable
fun PrinterSelectionDialog(
    pairedDevices: List<BluetoothDevice>,
    selectedAddress: String? = null,
    onDeviceSelected: (BluetoothDevice) -> Unit,
    onTestPrint: () -> Unit = {},
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Pilih Printer Bluetooth",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (pairedDevices.isEmpty()) {
                    Text(
                        text = "Tidak ada perangkat Bluetooth yang dipairing. Silakan pairing printer Panda di pengaturan Bluetooth HP Anda.",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                        items(pairedDevices) { device ->
                            val isSelected = device.address == selectedAddress
                            DeviceItem(
                                name = device.name ?: "Unknown Device",
                                address = device.address,
                                isSelected = isSelected,
                                onClick = { onDeviceSelected(device) }
                            )
                            HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                if (selectedAddress != null) {
                    OutlinedButton(
                        onClick = onTestPrint,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1E88E5))
                    ) {
                        Text("TEST PRINT")
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("TUTUP", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun DeviceItem(name: String, address: String, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name, 
                fontSize = 16.sp, 
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, 
                color = if (isSelected) Color(0xFF1E88E5) else Color.Black
            )
            Text(text = address, fontSize = 12.sp, color = Color.Gray)
        }
        if (isSelected) {
            Text(
                text = "✓",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E88E5),
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}
