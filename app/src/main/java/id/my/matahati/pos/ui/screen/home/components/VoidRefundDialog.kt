package id.my.matahati.pos.ui.screen.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

enum class VoidRefundActionType {
    VOID,
    REFUND
}

@Composable
fun VoidRefundDialog(
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onSubmit: (type: VoidRefundActionType, reason: String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedAction by remember { mutableStateOf(VoidRefundActionType.VOID) }
    var reasonText by remember { mutableStateOf("") }
    var showErrorValidation by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = { if (!isLoading) onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            modifier = Modifier
                .width(400.dp)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                // Header Title
                Text(
                    text = "Pengembalian / Pembatalan",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1565C0)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Action Type Selection (Radio Buttons)
                Text(
                    text = "Pilih Tipe Aksi:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.DarkGray
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(modifier = Modifier.selectableGroup()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = selectedAction == VoidRefundActionType.VOID,
                            onClick = { selectedAction = VoidRefundActionType.VOID },
                            enabled = !isLoading
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Pembatalan (Void)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = selectedAction == VoidRefundActionType.REFUND,
                            onClick = { selectedAction = VoidRefundActionType.REFUND },
                            enabled = !isLoading
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Pengembalian (Refund)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // TextField for Reason
                OutlinedTextField(
                    value = reasonText,
                    onValueChange = {
                        reasonText = it
                        if (showErrorValidation && it.isNotBlank()) {
                            showErrorValidation = false
                        }
                    },
                    label = { Text("Alasan (Wajib diisi)") },
                    isError = showErrorValidation,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    maxLines = 3
                )

                if (showErrorValidation) {
                    Text(
                        text = "Alasan harus diisi",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }

                if (!errorMessage.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isLoading
                    ) {
                        Text("Batal")
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = {
                            if (reasonText.isBlank()) {
                                showErrorValidation = true
                            } else {
                                onSubmit(selectedAction, reasonText.trim())
                            }
                        },
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Memproses...")
                        } else {
                            Text("Submit")
                        }
                    }
                }
            }
        }
    }
}
