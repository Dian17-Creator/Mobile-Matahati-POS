package id.my.matahati.pos.ui.screen.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.my.matahati.pos.model.Customer

private val PanelBlueHeader = Color(0xFF1565C0)
private val PanelBgColor = Color(0xFFEBF5FE)

@Composable
fun CustomerSelectionPanel(
    customers: List<Customer>,
    onCustomerSelected: (Customer) -> Unit,
    onClose: () -> Unit,
    onOpenAddCustomerPanel: (() -> Unit)? = null,
    onEditCustomer: ((Customer) -> Unit)? = null,
    onAddCustomer: ((name: String, phone: String, email: String, address: String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    val filteredCustomers = remember(searchQuery, customers) {
        if (searchQuery.isBlank()) customers
        else customers.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.phone.contains(searchQuery, ignoreCase = true)
        }
    }

    Surface(
        color = Color.White,
        shadowElevation = 8.dp,
        modifier = modifier
            .fillMaxHeight()
            .widthIn(min = 650.dp, max = 700.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar (Blue Bar)
            Surface(
                color = PanelBlueHeader,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Button '+' (Tambah Pelanggan)
                    IconButton(onClick = {
                        if (onOpenAddCustomerPanel != null) {
                            onOpenAddCustomerPanel()
                        } else {
                            showAddDialog = true
                        }
                    }) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Tambah Pelanggan",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Search Field matching OlseraProductPanel
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Cari",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                singleLine = true,
                                textStyle = TextStyle(
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                cursorBrush = SolidColor(Color.White),
                                decorationBox = { innerTextField ->
                                    Box(contentAlignment = Alignment.CenterStart) {
                                        if (searchQuery.isEmpty()) {
                                            Text(
                                                text = "Cari pelanggan...",
                                                color = Color.White.copy(alpha = 0.7f),
                                                fontSize = 13.sp
                                            )
                                        }
                                        innerTextField()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Button 'X' (Close Panel)
                    IconButton(onClick = onClose) {
                        Surface(
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.25f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Tutup Panel",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Customer List Area
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredCustomers, key = { it.id }) { customer ->
                    CustomerItemRow(
                        customer = customer,
                        onClick = {
                            onCustomerSelected(customer)
                            onClose()
                        },
                        onEdit = {
                            onEditCustomer?.invoke(customer)
                        }
                    )
                }
            }
        }
    }

    // Add Customer Dialog
    if (showAddDialog) {
        AddCustomerDialog(
            onDismiss = { showAddDialog = false },
            onSubmit = { name, phone, email, address ->
                onAddCustomer?.invoke(name, phone, email, address)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun CustomerItemRow(
    customer: Customer,
    onClick: () -> Unit,
    onEdit: (() -> Unit)? = null
) {
    val initialLetter = customer.name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "A"

    val avatarBgColor = remember(customer.name) {
        val colors = listOf(
            Color(0xFF0288D1), Color(0xFF00897B), Color(0xFF7CB342),
            Color(0xFF8E24AA), Color(0xFFE53935), Color(0xFFD81B60), Color(0xFFF57C00)
        )
        val index = kotlin.math.abs(customer.name.hashCode()) % colors.size
        colors[index]
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 0.5.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circle Avatar
            Surface(
                shape = CircleShape,
                color = avatarBgColor,
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = initialLetter,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = customer.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF222222),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (customer.phone.isNotBlank()) {
                    Text(
                        text = "P. ${customer.phone}",
                        fontSize = 12.sp,
                        color = Color(0xFF666666)
                    )
                }

                val subInfo = if (customer.address.isNotBlank()) customer.address
                else if (customer.email.isNotBlank()) customer.email
                else "Guest"

                Text(
                    text = subInfo,
                    fontSize = 11.sp,
                    color = Color(0xFF888888),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Action Button (Edit - 3 Dots)
            IconButton(
                onClick = {
                    if (onEdit != null) {
                        onEdit()
                    } else {
                        onClick()
                    }
                },
                modifier = Modifier.size(32.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = PanelBgColor,
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.MoreHoriz,
                            contentDescription = "Edit Pelanggan",
                            tint = PanelBlueHeader,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AddCustomerDialog(
    onDismiss: () -> Unit,
    onSubmit: (name: String, phone: String, email: String, address: String) -> Unit
) {
    var nameInput by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var addressInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Pelanggan Baru", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Nama Pelanggan *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phoneInput,
                    onValueChange = { phoneInput = it },
                    label = { Text("Nomor Telepon / WA") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it },
                    label = { Text("Email") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = addressInput,
                    onValueChange = { addressInput = it },
                    label = { Text("Alamat") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nameInput.isNotBlank()) {
                        onSubmit(nameInput.trim(), phoneInput.trim(), emailInput.trim(), addressInput.trim())
                    }
                },
                enabled = nameInput.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = PanelBlueHeader)
            ) {
                Text("Simpan", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
