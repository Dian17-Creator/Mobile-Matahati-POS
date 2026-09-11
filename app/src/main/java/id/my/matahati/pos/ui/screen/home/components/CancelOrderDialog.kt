package id.my.matahati.pos.ui.screen.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CancelOrderDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var cancelNote by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Batal Pesanan", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Apakah Anda yakin ingin membatalkan pesanan ini?",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = cancelNote,
                    onValueChange = { 
                        cancelNote = it
                        if (it.isNotBlank()) errorMsg = null
                    },
                    label = { Text("Alasan Pembatalan") },
                    placeholder = { Text("Contoh: Customer tidak jadi beli") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = errorMsg != null,
                    minLines = 3
                )
                if (errorMsg != null) {
                    Text(
                        text = errorMsg!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (cancelNote.isBlank()) {
                        errorMsg = "Alasan pembatalan wajib diisi"
                    } else {
                        onConfirm(cancelNote)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Konfirmasi Batal", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup")
            }
        },
        shape = RoundedCornerShape(12.dp)
    )
}
