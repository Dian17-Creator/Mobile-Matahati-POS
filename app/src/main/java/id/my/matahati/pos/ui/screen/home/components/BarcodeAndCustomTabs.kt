package id.my.matahati.pos.ui.screen.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.my.matahati.pos.model.Product

@Composable
fun BarcodeTabContent(
    products: List<Product> = emptyList(),
    onAddToCart: (Product) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var qtyText by remember { mutableStateOf("1") }
    var barcodeInput by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Qty Form with Stepper (- / +)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedTextField(
                value = qtyText,
                onValueChange = { qtyText = it },
                label = { Text("Qty", fontSize = 12.sp, color = Color(0xFF1E88E5)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF1E88E5),
                    unfocusedBorderColor = Color(0xFF1E88E5)
                )
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Stepper buttons [-] [+]
            Row(
                modifier = Modifier
                    .height(56.dp)
                    .border(BorderStroke(1.dp, Color(0xFF1E88E5)), RoundedCornerShape(4.dp)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .fillMaxHeight()
                        .clickable {
                            val current = qtyText.toIntOrNull() ?: 1
                            if (current > 1) qtyText = (current - 1).toString()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("-", color = Color(0xFF1E88E5), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }

                HorizontalDivider(
                    color = Color(0xFF1E88E5),
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                )

                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .fillMaxHeight()
                        .clickable {
                            val current = qtyText.toIntOrNull() ?: 1
                            qtyText = (current + 1).toString()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("+", color = Color(0xFF1E88E5), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Pindai Barcode TextField
        OutlinedTextField(
            value = barcodeInput,
            onValueChange = { barcodeInput = it },
            placeholder = { Text("Pindai Barcode...", color = Color(0xFF1E88E5)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF1E88E5),
                unfocusedBorderColor = Color(0xFF1E88E5)
            )
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Buka Pemindai Barcode Button
        OutlinedButton(
            onClick = {
                val qty = qtyText.toIntOrNull() ?: 1
                val foundProduct = products.find { it.name.contains(barcodeInput, ignoreCase = true) }
                if (foundProduct != null) {
                    repeat(qty) { onAddToCart(foundProduct) }
                    barcodeInput = ""
                } else if (barcodeInput.isNotBlank()) {
                    val dummyProduct = Product(
                        id = "bc_${System.currentTimeMillis()}",
                        name = "Item $barcodeInput",
                        price = 10000.0,
                        categoryId = "0",
                        stock = 99
                    )
                    repeat(qty) { onAddToCart(dummyProduct) }
                    barcodeInput = ""
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(4.dp),
            border = BorderStroke(1.dp, Color(0xFF1E88E5)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1E88E5))
        ) {
            Text("Buka Pemindai Barcode", fontWeight = FontWeight.Medium, fontSize = 15.sp)
        }
    }
}

@Composable
fun CustomDepositTabContent(
    onAddToCart: (Product) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var nameInput by remember { mutableStateOf("") }
    var priceInput by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Nama Field with [...] button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = nameInput,
                onValueChange = { nameInput = it },
                placeholder = { Text("Nama", color = Color(0xFF1E88E5)) },
                singleLine = true,
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF1E88E5),
                    unfocusedBorderColor = Color(0xFF1E88E5)
                )
            )

            Spacer(modifier = Modifier.width(12.dp))

            Surface(
                shape = CircleShape,
                border = BorderStroke(1.dp, Color(0xFF1E88E5)),
                color = Color.White,
                modifier = Modifier
                    .size(44.dp)
                    .clickable { /* Extra options */ }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("...", color = Color(0xFF1E88E5), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Harga Field
        OutlinedTextField(
            value = priceInput,
            onValueChange = { priceInput = it },
            placeholder = { Text("Harga", color = Color(0xFF1E88E5)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF1E88E5),
                unfocusedBorderColor = Color(0xFF1E88E5)
            )
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Tambah Item Button (Blue)
        Button(
            onClick = {
                val price = priceInput.toDoubleOrNull() ?: 0.0
                val name = nameInput.ifBlank { "Custom Item" }
                if (price > 0) {
                    val customProduct = Product(
                        id = "custom_${System.currentTimeMillis()}",
                        name = name,
                        price = price,
                        categoryId = "0",
                        stock = 99
                    )
                    onAddToCart(customProduct)
                    nameInput = ""
                    priceInput = ""
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(4.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
        ) {
            Text("Tambah Item", color = Color.White, fontWeight = FontWeight.Medium, fontSize = 15.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tambah Deposit Button (Green)
        Button(
            onClick = {
                val price = priceInput.toDoubleOrNull() ?: 0.0
                val name = if (nameInput.isNotBlank()) "Deposit - $nameInput" else "Deposit"
                if (price > 0) {
                    val depositProduct = Product(
                        id = "deposit_${System.currentTimeMillis()}",
                        name = name,
                        price = price,
                        categoryId = "0",
                        stock = 99
                    )
                    onAddToCart(depositProduct)
                    nameInput = ""
                    priceInput = ""
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(4.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
        ) {
            Text("Tambah Deposit", color = Color.White, fontWeight = FontWeight.Medium, fontSize = 15.sp)
        }
    }
}
