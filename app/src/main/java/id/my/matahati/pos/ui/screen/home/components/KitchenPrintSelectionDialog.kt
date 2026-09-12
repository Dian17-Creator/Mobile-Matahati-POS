package id.my.matahati.pos.ui.screen.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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

@Composable
fun KitchenPrintSelectionDialog(
    changesCount: Int,
    availableStations: List<String>,
    selectedStations: List<String>,
    onToggleStation: (String) -> Unit,
    onConfirmPrint: (String) -> Unit, // "PERUBAHAN" or "ULANG"
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color.White,
            modifier = Modifier.width(420.dp).padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Cetak ke Dapur/Bar",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "$changesCount Tambahan ditemukan. Apakah Anda ingin mencetak perubahan pesanan saja?",
                    fontSize = 15.sp,
                    lineHeight = 20.sp,
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                availableStations.forEach { station ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggleStation(station) }
                            .padding(vertical = 8.dp)
                    ) {
                        Checkbox(
                            checked = selectedStations.contains(station),
                            onCheckedChange = { onToggleStation(station) },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF0288D1))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Printer $station",
                                fontSize = 16.sp,
                                color = Color.DarkGray
                            )
                            Text(
                                text = "Simulated Station",
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Action Buttons
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "CETAK PERUBAHAN SAJA",
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        modifier = Modifier.clickable { onConfirmPrint("PERUBAHAN") }
                    )
                    
                    Text(
                        text = "CETAK ULANG PESANAN",
                        color = Color(0xFF0288D1),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        modifier = Modifier.clickable { onConfirmPrint("ULANG") }
                    )
                    
                    Text(
                        text = "TANPA CETAK",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        modifier = Modifier.clickable { onDismiss() }
                    )
                }
            }
        }
    }
}
