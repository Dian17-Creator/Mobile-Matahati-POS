package id.my.matahati.pos.ui.screen.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import id.my.matahati.pos.ui.theme.AppPrimaryColor

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
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier
                .width(440.dp)
                .padding(16.dp),
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                // Header Title
                Text(
                    text = "Pengembalian / Pembatalan",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppPrimaryColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Action Type Selection Header
                Text(
                    text = "Pilih Tipe Aksi :",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Modern Choice Cards (Atas - Bawah)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ActionChoiceCard(
                        title = "Pembatalan (Void)",
                        description = "Membatalkan pesanan karena kesalahan input atau batal beli.",
                        icon = Icons.Default.Block,
                        isSelected = selectedAction == VoidRefundActionType.VOID,
                        onClick = { if (!isLoading) selectedAction = VoidRefundActionType.VOID },
                        modifier = Modifier.fillMaxWidth()
                    )

                    ActionChoiceCard(
                        title = "Pengembalian (Refund)",
                        description = "Pengembalian dana atau produk yang dikembalikan oleh pelanggan.",
                        icon = Icons.Default.Refresh,
                        isSelected = selectedAction == VoidRefundActionType.REFUND,
                        onClick = { if (!isLoading) selectedAction = VoidRefundActionType.REFUND },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // TextField for Reason
                OutlinedTextField(
                    value = reasonText,
                    onValueChange = {
                        reasonText = it
                        if (showErrorValidation && it.isNotBlank()) {
                            showErrorValidation = false
                        }
                    },
                    placeholder = { Text("Alasan (Wajib diisi)", color = Color.Gray) },
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black),
                    isError = showErrorValidation,
                    enabled = !isLoading,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = AppPrimaryColor,
                        unfocusedBorderColor = Color.LightGray,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    minLines = 3,
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

                Spacer(modifier = Modifier.height(28.dp))

                // Action Buttons: Full Width Side-by-Side
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isLoading,
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Batal", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            if (reasonText.isBlank()) {
                                showErrorValidation = true
                            } else {
                                onSubmit(selectedAction, reasonText.trim())
                            }
                        },
                        enabled = !isLoading,
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppPrimaryColor,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Submit", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionChoiceCard(
    title: String,
    description: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) AppPrimaryColor else Color.LightGray.copy(alpha = 0.5f)
    val bgColor = if (isSelected) AppPrimaryColor.copy(alpha = 0.08f) else Color.White
    val contentColor = if (isSelected) AppPrimaryColor else Color.Gray

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected) AppPrimaryColor.copy(alpha = 0.15f) else Color(0xFFF0F0F0),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (isSelected) AppPrimaryColor else Color.DarkGray
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.Gray
                )
            }
        }
    }
}


