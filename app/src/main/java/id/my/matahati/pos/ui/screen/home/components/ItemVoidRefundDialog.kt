package id.my.matahati.pos.ui.screen.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
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
import id.my.matahati.pos.model.ItemVoidRefundRequestItem
import id.my.matahati.pos.model.TransactionDetailModel
import id.my.matahati.pos.ui.theme.AppPrimaryColor

@Composable
fun ItemVoidRefundDialog(
    items: List<TransactionDetailModel>,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    initialTargetItem: TransactionDetailModel? = null,
    onSubmit: (type: VoidRefundActionType, requestItems: List<ItemVoidRefundRequestItem>, reason: String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedAction by remember { mutableStateOf(VoidRefundActionType.VOID) }
    var reasonText by remember { mutableStateOf("") }
    var showReasonError by remember { mutableStateOf(false) }
    var validationErrorMessage by remember { mutableStateOf<String?>(null) }

    // Map of detail_id (int) to selected quantity
    val itemQuantities = remember {
        mutableStateMapOf<Int, Int>().apply {
            items.forEach { item ->
                val detailId = item.id.toIntOrNull() ?: 0
                if (detailId > 0 && item.qtyAvailable > 0) {
                    // Default to 1 if initialTargetItem is selected, or 0/1
                    if (initialTargetItem != null && item.id == initialTargetItem.id) {
                        put(detailId, 1)
                    } else if (initialTargetItem == null) {
                        put(detailId, 1)
                    } else {
                        put(detailId, 0)
                    }
                }
            }
        }
    }

    Dialog(onDismissRequest = { if (!isLoading) onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier
                .width(480.dp)
                .padding(16.dp),
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                // Header
                Text(
                    text = "Void / Refund Per Item",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppPrimaryColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Action Type Selection
                Text(
                    text = "Pilih Tipe Aksi:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.DarkGray
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Modern Choice Cards (Atas - Bawah)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ActionChoiceCard(
                        title = "Pembatalan (Void)",
                        description = "Membatalkan item karena kesalahan input atau batal beli.",
                        icon = Icons.Default.Block,
                        isSelected = selectedAction == VoidRefundActionType.VOID,
                        onClick = { if (!isLoading) selectedAction = VoidRefundActionType.VOID },
                        modifier = Modifier.fillMaxWidth()
                    )

                    ActionChoiceCard(
                        title = "Pengembalian (Refund)",
                        description = "Pengembalian dana atau item yang dikembalikan oleh pelanggan.",
                        icon = Icons.Default.Refresh,
                        isSelected = selectedAction == VoidRefundActionType.REFUND,
                        onClick = { if (!isLoading) selectedAction = VoidRefundActionType.REFUND },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Pilih Item & Jumlah Qty:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.DarkGray
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Item List with Stepper Qty
                Surface(
                    color = Color(0xFFF9F9F9),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(12.dp)
                    ) {
                        val availableItems = items.filter { it.qtyAvailable > 0 }
                        if (availableItems.isEmpty()) {
                            item {
                                Text(
                                    text = "Tidak ada item yang dapat di-void/refund.",
                                    fontSize = 13.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        } else {
                            itemsIndexed(availableItems) { index, item ->
                                val detailId = item.id.toIntOrNull() ?: 0
                                val currentQty = itemQuantities[detailId] ?: 0

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.productName,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "Tersedia: ${item.qtyAvailable} (dari ${item.quantity})",
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                    }

                                    // Stepper
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
                                            .background(Color.White)
                                    ) {
                                        IconButton(
                                            onClick = {
                                                if (currentQty > 0) {
                                                    itemQuantities[detailId] = currentQty - 1
                                                }
                                            },
                                            enabled = !isLoading && currentQty > 0,
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Remove,
                                                contentDescription = "Kurangi",
                                                tint = if (currentQty > 0) Color.DarkGray else Color.LightGray,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        Text(
                                            text = "$currentQty",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp)
                                        )

                                        IconButton(
                                            onClick = {
                                                if (currentQty < item.qtyAvailable) {
                                                    itemQuantities[detailId] = currentQty + 1
                                                }
                                            },
                                            enabled = !isLoading && currentQty < item.qtyAvailable,
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Tambah",
                                                tint = if (currentQty < item.qtyAvailable) Color.DarkGray else Color.LightGray,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                                if (index < availableItems.lastIndex) {
                                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Reason TextField
                OutlinedTextField(
                    value = reasonText,
                    onValueChange = {
                        reasonText = it
                        if (showReasonError && it.isNotBlank()) {
                            showReasonError = false
                        }
                    },
                    placeholder = { Text("Alasan (Wajib diisi)", color = Color.Gray) },
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black),
                    isError = showReasonError,
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

                if (showReasonError) {
                    Text(
                        text = "Alasan harus diisi",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }

                if (!validationErrorMessage.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = validationErrorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.fillMaxWidth()
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

                // Buttons: Full Width Side-by-Side
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
                            validationErrorMessage = null
                            showReasonError = false

                            if (reasonText.isBlank()) {
                                showReasonError = true
                                return@Button
                            }

                            val selectedRequestItems = itemQuantities.entries
                                .filter { it.value > 0 }
                                .map { ItemVoidRefundRequestItem(detailId = it.key, qty = it.value) }

                            if (selectedRequestItems.isEmpty()) {
                                validationErrorMessage = "Pilih minimal 1 item dengan qty > 0"
                                return@Button
                            }

                            // Validate max available qty
                            var invalidQtyItem = false
                            for (reqItem in selectedRequestItems) {
                                val origItem = items.find { (it.id.toIntOrNull() ?: 0) == reqItem.detailId }
                                if (origItem != null && reqItem.qty > origItem.qtyAvailable) {
                                    invalidQtyItem = true
                                    break
                                }
                            }

                            if (invalidQtyItem) {
                                validationErrorMessage = "Jumlah Qty melebihi Qty yang tersedia"
                                return@Button
                            }

                            onSubmit(selectedAction, selectedRequestItems, reasonText.trim())
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
