package id.my.matahati.pos.ui.screen.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun OlseraTableInputDialog(
    initialTable: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var tableName by remember { mutableStateOf(initialTable) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Input Meja", fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                OutlinedTextField(
                    value = tableName,
                    onValueChange = { tableName = it },
                    label = { Text("Nama/Nomor Meja") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(tableName) },
                colors = ButtonDefaults.buttonColors(containerColor = OlseraBlueHeader)
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        },
        shape = RoundedCornerShape(12.dp)
    )
}
